/*
 * GTVM — a call-for-call port of com/pip/ui/VM.java (Game/sangobuildVn/client/src).
 *
 * This is the machine that draws the game. Every window, panel, button and list in
 * the original client is a function in an .etf script running on this interpreter;
 * the browser only has to stand in for the Java ME runtime underneath it.
 *
 * Nothing is "simplified": the pointer encoding, the heap layout, the stack frame
 * shape and the instruction semantics are the Java ones, because scripts depend on
 * all of them. In particular:
 *
 *  - A VM value is a 32-bit int that is EITHER a plain number OR a pointer.
 *    Pointer encoding (see followPointer / arrLoad):
 *      bit 31 set        -> string-table reference: bits 30..16 = library id,
 *                           bits 15..0 = index into that library's string table
 *      else bits 30..26  = data type: 4..19 a scalar/object cell, >= 20 an Object[]
 *                           (with bit 25 marking "pointer to one element", and
 *                            bits 24..12 holding that element's index)
 *           bits 11..0   = index into the dynamic heap
 *  - The dynamic heap's first `tempSpace` (32) cells are a ring buffer for objects
 *    handed to scripts by syscalls; heapFree deliberately ignores them.
 *  - Struct instances are int[] (STLOAD/STSAVE index into them), so the browser
 *    port uses Int32Array wherever Java used int[].
 *
 * Java `int` arithmetic is reproduced exactly: the stack and static heap are
 * Int32Array (stores truncate mod 2^32 like Java), MUL goes through Math.imul, and
 * every `codeData[...]` byte read is sign-extended the way a Java `byte` is.
 *
 * Syscalls are NOT here — VM.java's 800-case switch is the platform boundary, so it
 * is injected as a `host` object. That keeps this file a pure interpreter, and lets
 * the syscall layer be filled in feature by feature against the census.
 */
import { INSTRUCTION_LENGTH, STACK_EFFECT, OP } from './isa.js';

/** VM interface ids — the entry points the client calls into a script. */
export const INIT = 0;
export const CYCLE = 1;
export const PROCESSPACKET = 2;
export const CYCLEUI = 3;
export const PAINT = 4;
export const DESTROY = 5;

export const TRUE = 1;
export const FALSE = 0;

/** VM.TEMP_OBJECT_COUNT */
const TEMP_OBJECT_COUNT = 32;
const INITIAL_HEAP_SIZE = 128;

const OWNER_TYPE_UI = 0;
const OWNER_TYPE_PROCESSOR = 1;
const OWNER_TYPE_QUEST = 2;

/* --- byte-level reads, matching Tool.getShort/getInt and Java's signed byte --- */
const i8 = (d, at) => (d[at] << 24) >> 24;
const i16 = (d, at) => (((d[at] & 0xff) << 8) | (d[at + 1] & 0xff)) << 16 >> 16;
const i32 = (d, at) =>
  (((d[at] & 0xff) << 24) | ((d[at + 1] & 0xff) << 16) | ((d[at + 2] & 0xff) << 8) | (d[at + 3] & 0xff)) | 0;

/**
 * Thrown when the interpreter itself cannot continue (bad opcode, division by
 * zero, stack overflow). Java let these bubble out of processInst into
 * execute()'s catch; the port keeps that behaviour but makes them visible.
 */
export class VMError extends Error {}

export class VM {
  /**
   * @param {object} [options]
   * @param {object} [options.owner] the VMGame/Quest this script belongs to
   * @param {'ui'|'processor'|'quest'} [options.ownerType]
   * @param {(vm: VM, funcId: number, params: Int32Array) => number} [options.host]
   *   the syscall handler (VM.syscall/syscall2). Returns the syscall's int result.
   * @param {(err: Error, where: string, vm: VM) => void} [options.onError]
   */
  constructor({ owner = null, ownerType = 'ui', host = null, onError = null } = {}) {
    this.owner = owner;
    this.ownerType = ownerType === 'quest' ? OWNER_TYPE_QUEST
      : ownerType === 'processor' ? OWNER_TYPE_PROCESSOR : OWNER_TYPE_UI;
    this.host = host;
    this.onError = onError ?? ((err, where) => {
      console.error(`[gtvm] ${where}: ${err && err.stack ? err.stack : err}`);
    });

    /** @type {import('./etf.js').Etf|null} */
    this.etf = null;
    this.stringTable = [];
    this.codeData = new Uint8Array(0);
    this.functions = new Int32Array(0);
    /** @type {Map<string, number>} */
    this.callbacks = new Map();
    this.libNames = [];
    /** @type {VM[]} */
    this.libraries = [this];
    this.libraryID = 0;
    this.javaCallFunctionEnd = DESTROY;

    this.staticHeap = new Int32Array(0);
    this.stack = new Int32Array(0);
    this.esp = -1;
    this.stackBase = -1;
    this.currentVM = 0;
    this.callCount = 0;
    this.eip = 0;
    this.currentFunc = 0;
    this.funcBase = 0;

    /** @type {any[]} */
    this.dynamicHeap = [];
    /** @type {Int16Array} */
    this.freeSpaceList = new Int16Array(0);
    this.freeHead = 0;
    this.tempSpace = TEMP_OBJECT_COUNT;
    this.nextTemp = 0;

    this.blocked = false;
    /** @type {Int32Array|null} */
    this.blockPosition = null;
    this.resumeFlag = false;
    this.running = false;

    /** script id, e.g. "game_world" — set by init() from the ETF header */
    this.name = '';

    /**
     * Quest scripts mirror every static-variable write back to the server
     * (Tool.sendSyncVMVarialbe). Only meaningful for ownerType 'quest'; left
     * unset until the quest path is ported, but the call sites are in place so
     * the behaviour is not quietly lost.
     * @type {((questId: number, addr: number, value: number) => void)|null}
     */
    this.onQuestVarSync = null;

    /**
     * Not part of the original VM: a ceiling on instructions per processInst call,
     * 0 for unlimited (the default, matching Java). The Java client ran on a device
     * where a runaway script just froze the handset; in a browser tab a runaway
     * script freezes the page and takes the debugger with it, so tests and dev
     * builds set a budget. It only ever turns a hang into a reported VMError.
     */
    this.instructionLimit = 0;
  }

  /** VM.sendSyncVMVarialbe call sites — quest VMs only. */
  _syncVar(addr, value) {
    if (this.ownerType === OWNER_TYPE_QUEST && this.onQuestVarSync !== null) {
      this.onQuestVarSync(this.owner ? this.owner.id : 0, addr, value);
    }
  }

  /** VM.init — load a parsed script and prepare the execution state. */
  init(etf) {
    this.etf = etf;
    this.stringTable = etf.stringTable;
    this.codeData = etf.code;
    this.functions = etf.functionTable;
    this.callbacks = etf.callbacks;
    this.libNames = etf.libNames;
    this.libraryID = etf.libraryID;
    this.name = etf.name;
    if (etf.languageVersion === 1) {
      this.javaCallFunctionEnd = etf.fileVersion === 0
        ? DESTROY + etf.callbacks.size
        : etf.callbacks.size;
    }

    this.esp = -1;
    this.stackBase = -1;
    this.eip = 0;
    this.currentFunc = 0;

    if (etf.fileVersion === 0) {
      this.staticHeap = new Int32Array(etf.heapSize & 0xffff);
      this.stack = new Int32Array(etf.stackSize & 0xffff);

      let initHeapSize = INITIAL_HEAP_SIZE;
      this.tempSpace = TEMP_OBJECT_COUNT;
      if (this.ownerType === OWNER_TYPE_QUEST) {
        initHeapSize = 10;
        this.tempSpace = 8;
      }
      this.dynamicHeap = new Array(initHeapSize).fill(null);
      this.freeSpaceList = new Int16Array(initHeapSize);
      for (let i = this.tempSpace - 1; i < initHeapSize - 1; i++) this.freeSpaceList[i] = i + 1;
      this.freeSpaceList[initHeapSize - 1] = this.tempSpace - 1;
      this.freeHead = this.tempSpace - 1;
      this.nextTemp = 0;
    }
    return this;
  }

  /**
   * VM.link — bind the libraries this script calls into. libraries[0] is always
   * the script itself; a function id's top nibble selects the library.
   *
   * @param {(libName: string) => VM} resolve
   */
  link(resolve) {
    this.libraries = new Array(this.libNames.length + 1);
    this.libraries[0] = this;
    for (let i = 0; i < this.libNames.length; i++) {
      const lib = resolve(this.libNames[i]);
      if (!lib) throw new VMError(`${this.name}: unresolved library "${this.libNames[i]}"`);
      this.libraries[i + 1] = lib;
    }
    return this;
  }

  /* ------------------------------------------------------------------ heap */

  /** VM.heapAlloc */
  heapAlloc() {
    if (this.freeSpaceList[this.freeHead] === this.freeHead) {
      // only one cell left in the free list -> grow, as the Java version does
      const oldLen = this.dynamicHeap.length;
      const expandSize = (oldLen / 2) | 0;
      const newLen = oldLen + expandSize;
      const newFree = new Int16Array(newLen);
      newFree.set(this.freeSpaceList);
      for (let i = oldLen; i < newLen; i++) newFree[i] = i + 1;
      newFree[newLen - 1] = this.freeSpaceList[this.freeHead];
      newFree[this.freeHead] = oldLen;
      this.dynamicHeap.length = newLen;
      this.dynamicHeap.fill(null, oldLen);
      this.freeSpaceList = newFree;
    }
    const next = this.freeSpaceList[this.freeHead] & 0xffff;
    this.freeSpaceList[this.freeHead] = this.freeSpaceList[next];
    return next;
  }

  /** VM.heapFree — the temp ring is never freed. */
  heapFree(addr) {
    if ((addr & 0xfff) < this.tempSpace) return;
    this.dynamicHeap[addr] = null;
    const tmp = this.freeSpaceList[this.freeHead];
    this.freeSpaceList[this.freeHead] = addr;
    this.freeSpaceList[addr] = tmp;
  }

  /** VM.memLoad — bit 31 clear = static heap, set = stack slot. */
  memLoad(addr) {
    return (addr & 0x80000000) === 0
      ? this.staticHeap[addr & 0x3fffffff]
      : this.stack[this.stackBase + (addr & 0x3fffffff)];
  }

  /** VM.memSave */
  memSave(addr, value) {
    if ((addr & 0x80000000) === 0) {
      this.staticHeap[addr & 0x3fffffff] = value;
      this._syncVar(addr, value);
    } else {
      this.stack[this.stackBase + (addr & 0x3fffffff)] = value;
    }
  }

  /** VM.arrLoad */
  arrLoad(addr, offset) {
    const dataType = (addr >> 26) & 0x0f;
    if (dataType > 3) {
      // an Object[]: hand back a pointer to the element, index in bits 24..12
      return addr | (offset << 12) | 0x02000000;
    }
    const obj = this.dynamicHeap[addr & 0xfff];
    switch (dataType) {
      case 0: return obj[offset] ? 1 : 0;
      case 1: case 2: case 3: return obj[offset];
      default: return 0;
    }
  }

  /** VM.arrSave */
  arrSave(addr, offset, value) {
    const dataType = (addr >> 26) & 0x0f;
    const obj = this.dynamicHeap[addr & 0xfff];
    if (dataType > 3) {
      obj[offset] = this.followPointer(value);
      return;
    }
    switch (dataType) {
      case 0: obj[offset] = value === 0 ? 0 : 1; break;
      case 1: case 2: case 3: obj[offset] = value; break;
      default: break;
    }
  }

  /** VM.followPointer — resolve a pointer to the JS value behind it. */
  followPointer(pointer) {
    if (pointer === 0) return null;
    if ((pointer & 0x80000000) !== 0) {
      const libID = ((pointer >> 16) & 0x7fff) << 16 >> 16;
      if (libID === 0) return this.stringTable[pointer & 0xffff];
      for (let i = 1; i < this.libraries.length; i++) {
        if (libID === this.libraries[i].libraryID) return this.libraries[i].stringTable[pointer & 0xffff];
      }
      return null;
    }
    const dataType = (pointer >> 26) & 0x1f;
    if (dataType >= 4 && dataType <= 19) {
      return this.dynamicHeap[pointer & 0xfff];
    } else if (dataType >= 20) {
      const arr = this.dynamicHeap[pointer & 0xfff];
      if ((pointer & 0x02000000) !== 0) return arr[(pointer >> 12) & 0x1fff];
      return arr;
    }
    return null;
  }

  /** VM.getStringArrayFromParams */
  getStringArrayFromParams(pointer) {
    const obs = this.followPointer(pointer);
    return obs == null ? null : Array.from(obs, (o) => /** @type {string} */ (o));
  }

  /**
   * VM.alloc — the data type ids come from the compiler:
   * 0 boolean[], 1 byte[], 2 short[], 3 int[], 11 String[], anything else Object[].
   */
  alloc(dataType, length) {
    const ret = this.heapAlloc();
    switch (dataType) {
      case 0: this.dynamicHeap[ret] = new Uint8Array(length); break;
      case 1: this.dynamicHeap[ret] = new Int8Array(length); break;
      case 2: this.dynamicHeap[ret] = new Int16Array(length); break;
      case 3: this.dynamicHeap[ret] = new Int32Array(length); break;
      default: this.dynamicHeap[ret] = new Array(length).fill(null); break;
    }
    return ret | ((dataType + 16) << 26);
  }

  /** VM.free */
  free(addr) {
    if ((addr & 0x82000000) === 0) this.heapFree(addr & 0xfff);
  }

  /**
   * VM.makeTempObject — park a host object in the temp ring and return a pointer
   * to it. This is how every syscall that returns an object hands it back.
   */
  makeTempObject(obj) {
    if (obj === null || obj === undefined) return 0;
    this.dynamicHeap[this.nextTemp] = obj;
    const addr = this.nextTemp;
    this.nextTemp = (this.nextTemp + 1) & (this.tempSpace - 1);
    if (obj instanceof Uint8Array) return (16 << 26) | addr;
    if (obj instanceof Int8Array) return (17 << 26) | addr;
    if (obj instanceof Int16Array) return (18 << 26) | addr;
    if (obj instanceof Int32Array) return (19 << 26) | addr;
    if (Array.isArray(obj)) return (20 << 26) | addr;
    return (4 << 26) | addr;
  }

  /** VM.searchTable — binary search over an LSWITCH jump table. */
  searchTable(data, pos, count, byteLen, compare) {
    let start = 0;
    let end = count - 1;
    while (start <= end) {
      const mid = (start + end) >> 1;
      const pos2 = pos + mid * (byteLen + 2);
      const cmp = byteLen === 1 ? i8(data, pos2) : byteLen === 2 ? i16(data, pos2) : i32(data, pos2);
      if (cmp === compare) return i16(data, pos2 + byteLen);
      else if (cmp < compare) start = mid + 1;
      else end = mid - 1;
    }
    return -1;
  }

  /* ----------------------------------------------------------- suspension */

  isBlock() { return this.blocked; }

  /** VM.pauseProcess — a syscall parks the script until the answer arrives. */
  pauseProcess() { this.blocked = true; }

  /** VM.continueProcess — hand the pending syscall its result. */
  continueProcess(returnValue) {
    this.resumeFlag = true;
    if (this.blockPosition !== null) this.blockPosition[this.blockPosition.length - 1] = returnValue;
  }

  /** VM.saveStack */
  saveStack() {
    const ret = new Int32Array(this.esp + 6);
    ret[0] = this.stackBase;
    ret[1] = this.currentVM;
    ret[2] = this.eip;
    ret[3] = this.currentFunc;
    ret[4] = this.callCount;
    if (this.esp >= 0) ret.set(this.stack.subarray(0, this.esp + 1), 5);
    return ret;
  }

  /** VM.restoreStack */
  restoreStack(bp) {
    this.stackBase = bp[0];
    this.currentVM = bp[1];
    this.eip = bp[2];
    this.currentFunc = bp[3];
    this.callCount = bp[4];
    this.funcBase = this.currentFunc * 3;
    this.esp = bp.length - 6;
    if (this.esp >= 0) this.stack.set(bp.subarray(5, 5 + this.esp + 1), 0);
  }

  /** VM.resume */
  resume() {
    this.blocked = false;
    if (this.blockPosition !== null) {
      const bp = this.blockPosition;
      this.blockPosition = null;
      this.restoreStack(bp);
      try {
        this.processInst(false);
      } catch (err) {
        this.onError(err, `resume ${this.name}`, this);
      }
    }
  }

  /* ------------------------------------------------------------- dispatch */

  /**
   * VM.syscall — the platform boundary. Java caught Throwable here and yielded 0;
   * the port does the same but reports, so a missing syscall is visible instead of
   * turning into a silently wrong pixel.
   */
  syscall(funcID, params) {
    if (this.host === null) return 0;
    try {
      return this.host(this, funcID, params) | 0;
    } catch (err) {
      this.onError(err, `syscall 0x${(funcID & 0xffff).toString(16).padStart(4, '0')} in ${this.name}`, this);
      return 0;
    }
  }

  /**
   * VM.processInst — run instructions until the current function ends (or the
   * script blocks). This is the hot loop; it is deliberately one big switch, in
   * the same order as the Java one, so the two can be diffed by eye.
   *
   * @param {boolean} ignoreBlock keep running even while blocked
   */
  processInst(ignoreBlock) {
    const stack = this.stack;
    let functions = this.libraries[this.currentVM].functions;
    let codeData = this.libraries[this.currentVM].codeData;
    let eipmax = functions[this.funcBase + 2];
    let budget = this.instructionLimit;

    while (this.eip < eipmax) {
      if (!ignoreBlock && this.blocked) {
        this.blockPosition = this.saveStack();
        break;
      }
      if (budget !== 0 && --budget === 0) {
        throw new VMError(`${this.name}: instruction budget exhausted in func ${this.currentFunc}`);
      }
      const eip = this.eip;
      const inst = codeData[eip];
      const esp = this.esp;

      switch (inst) {
        case OP.ADD: stack[esp - 1] = stack[esp - 1] + stack[esp]; break;
        case OP.SUB: stack[esp - 1] = stack[esp - 1] - stack[esp]; break;
        case OP.MUL: stack[esp - 1] = Math.imul(stack[esp - 1], stack[esp]); break;
        case OP.DIV: {
          const d = stack[esp];
          if (d === 0) throw new VMError(`${this.name}: divide by zero at ${eip}`);
          stack[esp - 1] = (stack[esp - 1] / d) | 0;
          break;
        }
        case OP.MOD: {
          const d = stack[esp];
          if (d === 0) throw new VMError(`${this.name}: modulo by zero at ${eip}`);
          stack[esp - 1] = stack[esp - 1] % d;
          break;
        }
        case OP.AND:
          stack[esp - 1] = (stack[esp - 1] !== FALSE && stack[esp] !== FALSE) ? TRUE : FALSE; break;
        case OP.OR:
          stack[esp - 1] = (stack[esp - 1] !== FALSE || stack[esp] !== FALSE) ? TRUE : FALSE; break;
        case OP.ANDB: stack[esp - 1] = stack[esp - 1] & stack[esp]; break;
        case OP.ORB: stack[esp - 1] = stack[esp - 1] | stack[esp]; break;
        case OP.LSHIFT: stack[esp - 1] = stack[esp - 1] << stack[esp]; break;
        case OP.RSHIFT: stack[esp - 1] = stack[esp - 1] >> stack[esp]; break;

        case OP.INCV: {
          const addr = i32(codeData, eip + 1);
          this._syncVar(addr, ++this.staticHeap[addr]);
          break;
        }
        case OP.ADDV8:
          stack[esp + 1] = this.staticHeap[i32(codeData, eip + 1)] + i8(codeData, eip + 5); break;
        case OP.SUBV8:
          stack[esp + 1] = this.staticHeap[i32(codeData, eip + 1)] - i8(codeData, eip + 5); break;
        case OP.INCVS: stack[this.stackBase + i32(codeData, eip + 1)]++; break;
        case OP.ADDV8S:
          stack[esp + 1] = stack[this.stackBase + i32(codeData, eip + 1)] + i8(codeData, eip + 5); break;
        case OP.SUBV8S:
          stack[esp + 1] = stack[this.stackBase + i32(codeData, eip + 1)] - i8(codeData, eip + 5); break;

        case OP.EQ: stack[esp - 1] = (stack[esp - 1] === stack[esp]) ? TRUE : FALSE; break;
        case OP.GT: stack[esp - 1] = (stack[esp - 1] > stack[esp]) ? TRUE : FALSE; break;
        case OP.LT: stack[esp - 1] = (stack[esp - 1] < stack[esp]) ? TRUE : FALSE; break;
        case OP.EQ8: stack[esp] = (stack[esp] === i8(codeData, eip + 1)) ? TRUE : FALSE; break;
        case OP.GT8: stack[esp] = (stack[esp] > i8(codeData, eip + 1)) ? TRUE : FALSE; break;
        case OP.LT8: stack[esp] = (stack[esp] < i8(codeData, eip + 1)) ? TRUE : FALSE; break;
        case OP.NE8: stack[esp] = (stack[esp] === i8(codeData, eip + 1)) ? FALSE : TRUE; break;

        case OP.JMP:
          this.eip = functions[this.funcBase + 1] + (i16(codeData, eip + 1) & 0xffff);
          continue;
        case OP.JEQ:
          if (stack[esp] !== FALSE) {
            this.eip = functions[this.funcBase + 1] + (i16(codeData, eip + 1) & 0xffff);
            this.esp--;
            continue;
          }
          break;
        case OP.JNE:
          if (stack[esp] === FALSE) {
            this.eip = functions[this.funcBase + 1] + (i16(codeData, eip + 1) & 0xffff);
            this.esp--;
            continue;
          }
          break;

        case OP.CALL:
        case OP.CALLPTR: {
          // arguments are already on the stack
          const parCount = codeData[eip + 1] & 0xff;
          let callFunc;
          if (inst === OP.CALL) {
            callFunc = i16(codeData, eip + 2) & 0xffff;
          } else {
            callFunc = this.stack[this.esp] & 0xffff;
            this.esp--;
          }
          let callVM = 0;
          if ((callFunc & 0xf000) !== 0) {
            // a function in a library: the id is relative to the CALLER's library
            // list, so translate it into this VM's list
            callVM = (callFunc & 0xf000) >> 12;
            if (this.currentVM !== 0 && inst === OP.CALL) {
              const nextVM = this.libraries[this.currentVM].libraries[callVM];
              for (let i = 0; i < this.libraries.length; i++) {
                if (nextVM === this.libraries[i]) { callVM = i; break; }
              }
            }
            functions = this.libraries[callVM].functions;
            codeData = this.libraries[callVM].codeData;
            callFunc &= 0x0fff;
          } else if (inst === OP.CALL) {
            callVM = this.currentVM;
          } else {
            // a function pointer into the original script always resolves to VM 0
            callVM = 0;
            functions = this.libraries[callVM].functions;
            codeData = this.libraries[callVM].codeData;
          }

          const newStackBase = this.esp - parCount + 1;

          // reserve the locals, zeroed
          const localParamCount = functions[callFunc * 3] & 0xffff;
          for (let ii = this.esp + 1; ii <= this.esp + localParamCount; ii++) stack[ii] = 0;
          this.esp += localParamCount;

          // then the return frame: stackBase, VM, function, return address
          stack[this.esp + 1] = this.stackBase;
          stack[this.esp + 2] = this.currentVM;
          stack[this.esp + 3] = this.currentFunc;
          stack[this.esp + 4] = inst === OP.CALL ? eip + 4 : eip + 2;
          this.esp += 4;

          this.stackBase = newStackBase;
          this.currentVM = callVM;
          this.callCount++;
          this.currentFunc = callFunc;
          this.funcBase = this.currentFunc * 3;
          this.eip = functions[this.funcBase + 1];
          eipmax = functions[this.funcBase + 2];
          continue;
        }

        case OP.RET: {
          if (this.callCount === 0) return; // returned out of the entry function
          this.eip = stack[this.esp];
          this.currentFunc = stack[this.esp - 1];
          this.currentVM = stack[this.esp - 2];
          functions = this.libraries[this.currentVM].functions;
          codeData = this.libraries[this.currentVM].codeData;
          const newStackBase = this.stackBase;
          this.stackBase = stack[this.esp - 3];
          this.callCount--;
          this.esp = newStackBase - 1;
          this.funcBase = this.currentFunc * 3;
          eipmax = functions[this.funcBase + 2];
          continue;
        }
        case OP.VRET: {
          if (this.callCount === 0) return;
          const retValue = stack[this.esp];
          this.eip = stack[this.esp - 1];
          this.currentFunc = stack[this.esp - 2];
          this.currentVM = stack[this.esp - 3];
          functions = this.libraries[this.currentVM].functions;
          codeData = this.libraries[this.currentVM].codeData;
          const newStackBase = this.stackBase;
          this.stackBase = stack[this.esp - 4];
          this.callCount--;
          this.esp = newStackBase;
          stack[this.esp] = retValue;
          this.funcBase = this.currentFunc * 3;
          eipmax = functions[this.funcBase + 2];
          continue;
        }

        case OP.SYSCALL:
        case OP.SYSCALLSAVEVS: {
          const callFunc = i16(codeData, eip + 1);
          const parCount = codeData[eip + 3] & 0xff;
          const hasRet = codeData[eip + 4] === 1;
          const params = stack.slice(this.esp - parCount + 1, this.esp + 1);
          this.esp -= parCount;
          const ret = this.syscall(callFunc, params);
          if (hasRet) {
            if (inst === OP.SYSCALLSAVEVS) {
              stack[this.stackBase + i32(codeData, eip + 5)] = ret;
            } else {
              stack[this.esp + 1] = ret;
              this.esp++;
            }
          }
          break;
        }

        case OP.TSWITCH: {
          // i16 default | i32 first | i32 last | (last-first+1) x i16, all offsets
          // relative to the END of the instruction
          const first = i32(codeData, eip + 3);
          const last = i32(codeData, eip + 7);
          const cond = stack[esp];
          const instLen = 11 + 2 * (last - first + 1);
          if (cond >= first && cond <= last) {
            const off = i16(codeData, eip + 11 + (cond - first) * 2) & 0xffff;
            this.eip += off === 0xffff ? (i16(codeData, eip + 1) & 0xffff) : off;
          } else {
            this.eip += i16(codeData, eip + 1) & 0xffff;
          }
          this.eip += instLen;
          break;
        }
        case OP.LSWITCH: {
          // i16 default | i16 count | u8 valueBytes | sorted (value, i16 offset) pairs
          const switchCount = i16(codeData, eip + 3);
          const condBytes = i8(codeData, eip + 5);
          const cond = stack[esp];
          const instLen = 6 + switchCount * (condBytes + 2);
          const addr = this.searchTable(codeData, eip + 6, switchCount, condBytes, cond);
          this.eip += addr >= 0 ? addr : (i16(codeData, eip + 1) & 0xffff);
          this.eip += instLen;
          break;
        }

        case OP.LOAD: stack[esp] = this.memLoad(stack[esp]); break;
        case OP.SAVE: this.memSave(stack[esp], stack[esp - 1]); break;
        case OP.LOADV: stack[esp + 1] = this.staticHeap[i32(codeData, eip + 1)]; break;
        case OP.SAVEV: {
          const addr = i32(codeData, eip + 1);
          this.staticHeap[addr] = stack[esp];
          this._syncVar(addr, stack[esp]);
          break;
        }
        case OP.LOADVS: stack[esp + 1] = stack[this.stackBase + i32(codeData, eip + 1)]; break;
        case OP.SAVEVS: stack[this.stackBase + i32(codeData, eip + 1)] = stack[esp]; break;
        case OP.DUP: stack[esp + 1] = stack[esp - i8(codeData, eip + 1)]; break;

        case OP.LOAD8: stack[esp + 1] = i8(codeData, eip + 1); break;
        case OP.LOAD16: stack[esp + 1] = i16(codeData, eip + 1); break;
        case OP.LOAD32: stack[esp + 1] = i32(codeData, eip + 1); break;

        case OP.ALOAD: stack[esp - 1] = this.arrLoad(stack[esp - 1], stack[esp]); break;
        case OP.ASAVE: this.arrSave(stack[esp - 1], stack[esp], stack[esp - 2]); break;
        case OP.ALOAD8: stack[esp] = this.arrLoad(stack[esp], i8(codeData, eip + 1)); break;
        case OP.ASAVE8: this.arrSave(stack[esp], i8(codeData, eip + 1), stack[esp - 1]); break;
        case OP.ALLOC: stack[esp] = this.alloc(i8(codeData, eip + 1), stack[esp]); break;
        case OP.FREE: this.free(stack[esp]); break;

        case OP.STALLOC:
          stack[esp + 1] = this.makeTempObject(new Int32Array(i16(codeData, eip + 1))); break;
        case OP.STLOAD: {
          const arr = this.followPointer(stack[esp - 1]);
          stack[esp - 1] = arr[stack[esp] & 0x3fffffff];
          break;
        }
        case OP.STSAVE: {
          const arr = this.followPointer(stack[esp - 1]);
          arr[stack[esp] & 0x3fffffff] = stack[esp - 2];
          break;
        }
        case OP.STLOAD8: {
          const arr = this.followPointer(stack[esp]);
          stack[esp] = arr[i8(codeData, eip + 1)];
          break;
        }
        case OP.STSAVE8: {
          const arr = this.followPointer(stack[esp]);
          arr[i8(codeData, eip + 1) & 0x3fffffff] = stack[esp - 1];
          break;
        }

        case OP.LOADFUNC: {
          let funcID = i16(codeData, eip + 1);
          // a function pointer taken from a library is relative to that library
          if (this.currentVM !== 0) {
            let callVM = (funcID & 0xf000) >> 12;
            const nextVM = this.libraries[this.currentVM].libraries[callVM];
            for (let i = 0; i < this.libraries.length; i++) {
              if (nextVM === this.libraries[i]) { callVM = i; break; }
            }
            funcID = ((funcID & 0xfff) | (callVM << 12)) << 16 >> 16;
          }
          stack[esp + 1] = funcID;
          break;
        }

        /* --- fused forms the compiler emits for common sequences ---------- */
        case OP.LOADVS3:
          stack[esp + 1] = stack[this.stackBase + i32(codeData, eip + 1)];
          stack[esp + 2] = stack[this.stackBase + i32(codeData, eip + 5)];
          stack[esp + 3] = stack[this.stackBase + i32(codeData, eip + 9)];
          break;
        case OP.LOADVS2:
          stack[esp + 1] = stack[this.stackBase + i32(codeData, eip + 1)];
          stack[esp + 2] = stack[this.stackBase + i32(codeData, eip + 5)];
          break;
        case OP.LOAD88:
          stack[esp + 1] = i8(codeData, eip + 1);
          stack[esp + 2] = i8(codeData, eip + 2);
          break;
        case OP.LOAD8VS:
          stack[esp + 1] = i8(codeData, eip + 1);
          stack[esp + 2] = stack[this.stackBase + i32(codeData, eip + 2)];
          break;
        case OP.LOADVS8:
          stack[esp + 1] = stack[this.stackBase + i32(codeData, eip + 1)];
          stack[esp + 2] = i8(codeData, eip + 5);
          break;
        case OP.LOADVSSTLOAD8: {
          const arr = this.followPointer(stack[this.stackBase + i32(codeData, eip + 1)]);
          stack[esp + 1] = arr[i8(codeData, eip + 5)];
          break;
        }
        case OP.LOAD8VSSTLOAD8: {
          stack[esp + 1] = i8(codeData, eip + 1);
          const arr = this.followPointer(stack[this.stackBase + i32(codeData, eip + 2)]);
          stack[esp + 2] = arr[i8(codeData, eip + 6)];
          break;
        }
        case OP.LOADVSADDALOAD: {
          const value = stack[esp] + stack[this.stackBase + i32(codeData, eip + 1)];
          stack[esp - 1] = this.arrLoad(stack[esp - 1], value);
          break;
        }
        case OP.LOADVSALOAD: {
          const value = stack[this.stackBase + i32(codeData, eip + 1)];
          stack[esp] = this.arrLoad(stack[esp], value);
          break;
        }

        default:
          throw new VMError(
            `${this.name}: unknown opcode 0x${inst.toString(16)} at ${eip} (func ${this.currentFunc})`);
      }

      this.esp += STACK_EFFECT[inst];
      this.eip += INSTRUCTION_LENGTH[inst];
    }
  }

  /**
   * VM.execute — call a script function by id and run until it returns.
   * The id's top nibble is the library, the low 12 bits the function.
   *
   * @param {number} funcID
   * @param {Int32Array|number[]|null} [params]
   */
  execute(funcID, params = null) {
    if (this.running) return;
    try {
      this.running = true;
      if (this.resumeFlag && funcID === CYCLEUI) {
        this.resumeFlag = false;
        this.resume();
      } else if (!this.blocked || funcID !== CYCLEUI) {
        this.currentVM = (funcID >> 12) & 0x0f;
        this.currentFunc = funcID & 0xfff;
        this.funcBase = this.currentFunc * 3;

        let paramCount = 0;
        if (params !== null) {
          this.stack.set(params, 0);
          paramCount += params.length;
        }

        const lcount = this.libraries[this.currentVM].functions[this.funcBase] & 0xffff;
        this.esp = -1 + lcount + paramCount;
        this.stackBase = 0;
        this.callCount = 0;
        for (let i = 0; i < lcount; i++) this.stack[i + paramCount] = 0;

        this.eip = this.libraries[this.currentVM].functions[this.funcBase + 1];
        this.processInst(this.blocked);
      }
    } catch (err) {
      this.onError(err, `execute ${this.name}#${funcID}`, this);
    } finally {
      this.running = false;
    }
  }

  /**
   * VM.callback — call a function the script exported with the CALLBACK keyword,
   * by name or by id. Re-entrant: a callback fired while the VM is mid-execution
   * saves and restores the running frame.
   *
   * @param {string|number} func
   * @param {Int32Array|number[]|null} [params]
   */
  callback(func, params = null) {
    let funcId = -1;
    if (typeof func === 'string') {
      for (let i = 0; i < this.libraries.length; i++) {
        const id = this.libraries[i].callbacks.get(func);
        if (id !== undefined) funcId = (i << 12) + id;
      }
      if (funcId === -1) return 0;
    } else {
      funcId = func;
    }

    try {
      if (!this.running) {
        this.execute(funcId, params);
        return this.esp < 0 ? 0 : this.stack[this.esp];
      }
      const oldStack = this.saveStack();
      this.running = false;
      this.execute(funcId, params);
      const ret = this.esp >= 0 ? this.stack[this.esp] : 0;
      this.restoreStack(oldStack);
      this.running = true;
      return ret;
    } catch (err) {
      this.onError(err, `callback ${this.name}#${func}`, this);
      return 0;
    }
  }

  /** VM.getRealizeAdrr — syscall 0x0010 Realize(), used when handing an object back. */
  getRealizeAdrr(objAddr) {
    return this.syscall(0x0010, Int32Array.of(objAddr));
  }

  /** True if the script exported a callback under this name. */
  hasCallback(name) {
    return this.libraries.some((lib) => lib.callbacks.has(name));
  }
}
