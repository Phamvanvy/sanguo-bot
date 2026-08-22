/*
 * Core syscall layer — VM.java's syscall2 cases that need no world, no renderer
 * and no widgets: strings, objects, vectors, hashtables, streams, UWAP segments,
 * global variables, keys/time/random and the UI-cycle pause/resume pair.
 *
 * Everything environment-specific goes through the `platform` object so the same
 * code runs in Node tests and in the browser:
 *
 *   platform = {
 *     screenWidth(), screenHeight(), fontHeight(), lineHeight(),
 *     font,                      // default Font: { stringWidth(s), height }
 *     keyPressed(code, clear), noKeyPressed(), multiKeyCheck(keys, clear), clearKeys(),
 *     getTimeStamp(),            // ms since client boot   (Utilities.getTimeStamp)
 *     getTick(),                 // frame tick             (GameMain.tick)
 *     getSystemTime(),           // unix seconds           (Utilities.getServerTime fallback)
 *     graphics,                  // current screen Graphics (Utilities.graphics)
 *     sendRequest(segment),      // -> serial/int          (Utilities.sendRequest)
 *     broadcast(segment),
 *     getNextPacket(),           // -> UASegment | null
 *     exitGame(), closeConnection(),
 *     loadFile(name), saveFile(name, data), deleteFile(name),   // RMS
 *     loadResourceFile(name),                                   // jar resource
 *     formatText(text, width, font),                            // Tool.formatText
 *     unimplemented(id, name),   // called once per unknown/unported id
 *   }
 *
 * Every case cites its VM.java line-semantics; where Java caught exceptions and
 * returned 0/FALSE, so does this.
 */
import { JavaVector, SortHashtable, VMInteger, DataInputStream, ByteArrayOutputStream } from './runtime.js';
import { UASegment } from './ua-segment.js';
import { splitString, mergeString, mergeString2, distance, rectIn, rectIntersect, rectContain, isqrt } from './tool.js';
import { JavaRandom } from '../assets/java-random.js';

export const TRUE = 1;
export const FALSE = 0;

/**
 * Sentinel a layered host returns for "this id is not mine", so several syscall
 * layers can be composed (core -> gfx -> widgets -> game) without each one
 * needing to know the full id map.
 */
export const UNHANDLED = Symbol('syscall-unhandled');

/** java.lang.Long boxed, for 0x5703/0x5704. */
export class VMLong {
  constructor(value) {
    this.value = BigInt.asIntN(64, value);
  }
}

/** Tool.getNextRnd(min, max): min + |rnd.nextInt()| % (max - min). */
export function getNextRnd(rnd, min, max) {
  if (max <= min) return min;
  let n = rnd.nextInt();
  // Math.abs of Integer.MIN_VALUE overflows in Java; replicate via unsigned shift
  const abs = n < 0 ? -n : n;
  return (min + (abs % (max - min))) | 0;
}

export class CoreHost {
  /**
   * @param {Partial<typeof CoreHost.prototype.defaults>} [platform]
   */
  constructor(platform = {}) {
    /** per-client global variable store (Tool.setGlobalValue et al.) */
    this.globals = new Map();
    this.rnd = new JavaRandom((Date.now() ^ (Math.random() * 0xffffffff)) | 0);
    this.platform = Object.assign(this.defaults(), platform);
    this._missingReported = new Set();
  }

  defaults() {
    return {
      screenWidth: () => 240,
      screenHeight: () => 320,
      fontHeight: () => 16,
      lineHeight: () => 18,
      font: { stringWidth: (s) => s.length * 8 },
      keyPressed: () => false,
      noKeyPressed: () => true,
      multiKeyCheck: () => -1,
      clearKeys: () => {},
      getTimeStamp: () => Date.now(),
      getTick: () => Date.now(),
      getSystemTime: () => Math.floor(Date.now() / 1000),
      graphics: null,
      sendRequest: () => -1,
      broadcast: () => {},
      getNextPacket: () => null,
      exitGame: () => {},
      closeConnection: () => {},
      loadFile: () => null,
      saveFile: () => false,
      deleteFile: () => {},
      loadResourceFile: () => null,
      formatText: (text, width, font) => splitString(text),
      unimplemented: () => {},
    };
  }

  _missing(id, why) {
    if (!this._missingReported.has(id)) {
      this._missingReported.add(id);
      this.platform.unimplemented(id, why);
    }
    return 0;
  }

  /**
   * The host function handed to `new VM({ host })`.
   * @returns {number} the syscall's int result
   */
  handle(vm, funcID, params) {
    const P = this.platform;

    switch (funcID) {
      /* ---------------------------------------------------- keys / time */
      case 0x0001: return P.keyPressed(params[0], params[1] !== 0) ? TRUE : FALSE;
      case 0x0002: return P.noKeyPressed() ? TRUE : FALSE;
      case 0x0003: return getNextRnd(this.rnd, 0, 10000);
      case 0x0004: return P.getTimeStamp() | 0;
      case 0x00CF: P.clearKeys(); return 0;
      case 0x00E0: return P.multiKeyCheck(vm.followPointer(params[0]), params[1] !== 0) | 0;

      /* --------------------------------------------- strings and objects */
      case 0x0005: { // StrToInt — "u"-prefixed means hex, bad input yields 0
        try {
          const str = vm.followPointer(params[0]) ?? '';
          if (str.startsWith('u')) {
            if (!/^[0-9a-fA-F]+$/.test(str.substring(1))) return 0;
            return parseInt(str.substring(1), 16) | 0;
          }
          if (!/^[+-]?[0-9]+$/.test(str)) return 0;
          const v = Number(str);
          if (!Number.isSafeInteger(v)) {
            // Java would throw on overflow; scripts never rely on it — clamp like wrap
            return v > 0 ? (v % 4294967296) | 0 : 0;
          }
          return v | 0;
        } catch {
          return 0;
        }
      }
      case 0x000E: return vm.followPointer(params[0]) == null ? TRUE : FALSE;
      case 0x000F: return vm.makeTempObject(String(params[0]));
      case 0x0010: { // Realize — move out of the temp ring into the dynamic heap
        const obj = vm.followPointer(params[0]);
        if (obj == null) return 0;
        const isTemp = ((params[0] & 0x80000000) === 0) && ((params[0] & 0xfff) < vm.tempSpace);
        if (isTemp) vm.dynamicHeap[params[0] & 0xfff] = null;
        const newaddr = vm.heapAlloc();
        vm.dynamicHeap[newaddr] = obj;
        return ((params[0] & 0xfffff000) | newaddr) | 0;
      }
      case 0x0021: return vm.makeTempObject(vm.followPointer(params[0]));
      case 0x0022: return vm.makeTempObject(String(vm.followPointer(params[0])) + String(vm.followPointer(params[1])));
      case 0x0023: return String(vm.followPointer(params[0]) ?? '').length;
      case 0x0024: {
        const s = String(vm.followPointer(params[0]) ?? '');
        return vm.makeTempObject(s.substring(params[1], params[1] + params[2]));
      }
      case 0x0025: return String(vm.followPointer(params[0]) ?? '').indexOf(String(vm.followPointer(params[1])), params[2]);
      case 0x0026: return vm.makeTempObject(String(vm.followPointer(params[0])) + params[1]);
      case 0x0027: return String(vm.followPointer(params[0])) === String(vm.followPointer(params[1])) ? TRUE : FALSE;
      case 0x0028: return String(vm.followPointer(params[0]) ?? '').charCodeAt(params[1]) || 0;
      case 0x0029: return vm.makeTempObject(String(vm.followPointer(params[0]) ?? '').trim());
      case 0x002A: { // String_Create2(byte[] data, String encoding)
        try {
          const bytes = vm.followPointer(params[0]);
          const enc = String(vm.followPointer(params[1]) ?? 'UTF-8').toLowerCase();
          if (enc.includes('utf-16')) {
            let out = '';
            const be = !enc.includes('le');
            for (let i = 0; i + 1 < bytes.length; i += 2) {
              out += String.fromCharCode(be
                ? ((bytes[i] & 0xff) << 8) | (bytes[i + 1] & 0xff)
                : ((bytes[i + 1] & 0xff) << 8) | (bytes[i] & 0xff));
            }
            return vm.makeTempObject(out);
          }
          return vm.makeTempObject(new TextDecoder(enc).decode(bytes));
        } catch {
          return 0;
        }
      }
      case 0x002B: {
        const str = String(vm.followPointer(params[0]) ?? '');
        const src = String(vm.followPointer(params[1])).charAt(0);
        const dest = String(vm.followPointer(params[2])).charAt(0);
        return vm.makeTempObject(str.split(src).join(dest));
      }
      case 0x002C: return vm.makeTempObject(String(vm.followPointer(params[0])) + String.fromCharCode(params[1]));
      case 0x002D: {
        const str = String(vm.followPointer(params[0]) ?? '');
        return vm.makeTempObject(str.substring(0, params[1]) + String(vm.followPointer(params[2])) +
          str.substring(params[1]));
      }
      case 0x002E: return String(vm.followPointer(params[0]) ?? '').startsWith(String(vm.followPointer(params[1]))) ? TRUE : FALSE;
      case 0x002F: return vm.makeTempObject(mergeString(vm.followPointer(params[0])));
      case 0x0030: return String(vm.followPointer(params[0]) ?? '').endsWith(String(vm.followPointer(params[1]))) ? TRUE : FALSE;
      case 0x1249: return vm.followPointer(params[0]) === vm.followPointer(params[1]) ? TRUE : FALSE;
      case 0x00B3: return vm.makeTempObject(new VMInteger(params[0]));
      case 0x00C3: {
        const o = vm.followPointer(params[0]);
        return o == null ? 0 : o.value | 0;
      }
      case 0x00C2: { // Length
        const o = vm.followPointer(params[0]);
        if (o == null) return 0;
        if (o instanceof Uint8Array || o instanceof Int8Array ||
            o instanceof Int16Array || o instanceof Int32Array ||
            o instanceof Float32Array || o instanceof Float64Array ||
            Array.isArray(o)) return o.length;
        return 1;
      }
      case 0x00B7: { // GC — drop the temp ring contents
        for (let i = vm.tempSpace - 1; i >= 0; i--) vm.dynamicHeap[i] = null;
        return 0;
      }
      case 0x2000: return vm.makeTempObject(splitString(String(vm.followPointer(params[0]) ?? '')));
      case 0x2003: return vm.makeTempObject((params[0] >>> 0).toString(16));
      case 0x2010: {
        const str = String(vm.followPointer(params[0]) ?? '');
        const delim = String(vm.followPointer(params[1]) ?? ' ');
        return vm.makeTempObject(splitString(str, delim.charAt(0)));
      }
      case 0x201D: return ~params[0];
      case 0x3040: return vm.makeTempObject(new Array(params[0]).fill(null)); // String[count]
      case 0x3041: return vm.makeTempObject(new Array(params[0]).fill(null)); // ImageSet[count]
      case 0x3070: { // ArrayCopy(int[] src, srcPos, des, desPos, length)
        if (params[4] > 0) {
          const src = vm.followPointer(params[0]);
          const dst = vm.followPointer(params[2]);
          dst.set(src.subarray(params[1], params[1] + params[4]), params[3]);
        }
        return 0;
      }
      case 0x3091: return vm.makeTempObject({ sb: '' });       // StringBuffer_Create
      case 0x3092: { vm.followPointer(params[0]).sb += String(vm.followPointer(params[1]) ?? ''); return 0; }
      case 0x3093: { const sb = vm.followPointer(params[0]); sb.sb = sb.sb.substring(0, params[1]); return 0; }
      case 0x3094: return vm.makeTempObject(vm.followPointer(params[0]).sb);
      case 0x5703: return vm.makeTempObject(new VMLong(BigInt.asIntN(64, BigInt(params[0]) | (BigInt(params[1]) << 32n))));
      case 0x5704: {
        const l = vm.followPointer(params[0]).value;
        return vm.makeTempObject(Int32Array.of(Number(BigInt.asIntN(32, l)), Number(BigInt.asIntN(32, l >> 32n))));
      }
      case 0x571B: return isqrt(params[0]);
      case 0x571C: return Math.abs(params[0]) | 0;

      /* ------------------------------------------------------- geometry */
      case 0x110C: return distance(params[0], params[1], params[2], params[3]);
      case 0x127F: return rectIn(params[0], params[1], params[2], params[3], params[4], params[5]) ? TRUE : FALSE;
      case 0x1296: return rectIntersect(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]) ? TRUE : FALSE;
      case 0x1297: return rectContain(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]) ? TRUE : FALSE;

      /* -------------------------------------------------------- streams */
      case 0x004F: return vm.followPointer(params[0]).readUnsignedByte();
      case 0x0050: return vm.followPointer(params[0]).readUnsignedShort();
      case 0x0051: return vm.makeTempObject(new DataInputStream(vm.followPointer(params[0])));
      case 0x0052: return vm.makeTempObject(new ByteArrayOutputStream());
      case 0x0053: return vm.followPointer(params[0]).readInt();
      case 0x0054: return vm.followPointer(params[0]).readShort();
      case 0x0055: return vm.followPointer(params[0]).readByte();
      case 0x0056: return vm.followPointer(params[0]).readBoolean() ? TRUE : FALSE;
      case 0x0057: return vm.makeTempObject(vm.followPointer(params[0]).readUTF());
      case 0x0058: vm.followPointer(params[0]).writeInt(params[1]); return 0;
      case 0x0059: vm.followPointer(params[0]).writeShort(params[1]); return 0;
      case 0x005A: vm.followPointer(params[0]).writeUTF(String(vm.followPointer(params[1]) ?? '')); return 0;
      case 0x005B: vm.followPointer(params[0]).writeByte(params[1]); return 0;
      case 0x005C: vm.followPointer(params[0]).writeBoolean(params[1] !== 0); return 0;
      case 0x005D: return vm.followPointer(params[0]).size();
      case 0x005E: return vm.makeTempObject(vm.followPointer(params[0]).toByteArray());
      case 0x005F: vm.followPointer(params[0]).readFully(vm.followPointer(params[1])); return 0;
      case 0x0060: vm.followPointer(params[0]).writeBytes(vm.followPointer(params[1])); return 0;

      /* --------------------------------------------------- UWAP segment */
      case 0x0071: return vm.makeTempObject(new UASegment(params[0], params[1] === TRUE));
      case 0x0072: return vm.followPointer(params[0]).type;
      case 0x0073: vm.followPointer(params[0]).reset(); return 0;
      case 0x0074: return vm.followPointer(params[0]).readInt();
      case 0x0075: return vm.followPointer(params[0]).readShort();
      case 0x0076: return vm.followPointer(params[0]).readByte();
      case 0x0077: return vm.followPointer(params[0]).readBoolean() ? TRUE : FALSE;
      case 0x0078: return vm.makeTempObject(vm.followPointer(params[0]).readString());
      case 0x0079: vm.followPointer(params[0]).writeInt(params[1]); return 0;
      case 0x007A: vm.followPointer(params[0]).writeShort(params[1]); return 0;
      case 0x007B: vm.followPointer(params[0]).writeString(String(vm.followPointer(params[1]) ?? '')); return 0;
      case 0x007C: vm.followPointer(params[0]).writeByte(params[1]); return 0;
      case 0x007D: vm.followPointer(params[0]).writeBoolean(params[1] !== 0); return 0;
      case 0x007E: return vm.makeTempObject(Int32Array.from(vm.followPointer(params[0]).readInts()));
      case 0x007F: return vm.makeTempObject(Int16Array.from(vm.followPointer(params[0]).readShorts()));
      case 0x0080: return vm.makeTempObject(vm.followPointer(params[0]).readBytes());
      case 0x0081: return vm.makeTempObject(Uint8Array.from(vm.followPointer(params[0]).readBooleans().map((b) => (b ? 1 : 0))));
      case 0x0082: return vm.makeTempObject(vm.followPointer(params[0]).readStrings());
      case 0x0083: vm.followPointer(params[0]).writeInts(vm.followPointer(params[1])); return 0;
      case 0x0084: vm.followPointer(params[0]).writeShorts(vm.followPointer(params[1])); return 0;
      case 0x0085: vm.followPointer(params[0]).writeStrings(vm.followPointer(params[1])); return 0;
      case 0x0086: vm.followPointer(params[0]).writeBooleans(vm.followPointer(params[1])); return 0;
      case 0x0087: vm.followPointer(params[0]).writeBytes(vm.followPointer(params[1])); return 0;
      case 0x0088: { // SendRequest
        const seg = vm.followPointer(params[0]);
        seg.flush();
        return P.sendRequest(seg) | 0;
      }
      case 0x0089: return vm.makeTempObject(P.getNextPacket());
      case 0x008A: vm.followPointer(params[0]).handled = params[1] === TRUE; return 0;
      case 0x008B: { // BroadcastPacket
        const seg = vm.followPointer(params[0]);
        seg.flush();
        P.broadcast(seg);
        return 0;
      }
      case 0x008C: return vm.followPointer(params[0]).serial;
      case 0x008D: vm.followPointer(params[0]).needResponse = params[1] === TRUE; return 0;
      case 0x008E: return vm.followPointer(params[0]).readUnsignedByte();
      case 0x008F: return vm.followPointer(params[0]).readUnsignedShort();

      /* --------------------------------------------------------- vector */
      case 0x0091: return vm.makeTempObject(new JavaVector());
      case 0x0092: return vm.followPointer(params[0]).size();
      case 0x0093: vm.followPointer(params[0]).addElement(vm.followPointer(params[1])); return 0;
      case 0x0094: vm.followPointer(params[0]).removeElementAt(params[1]); return 0;
      case 0x0095: return vm.makeTempObject(vm.followPointer(params[0]).elementAt(params[1]));
      case 0x0096: { const v = vm.followPointer(params[0]); if (v != null) v.removeAllElements(); return 0; }
      case 0x0097: vm.followPointer(params[0]).insertElementAt(vm.followPointer(params[1]), params[2]); return 0;
      case 0x0098: {
        const vec = vm.followPointer(params[0]);
        return vm.makeTempObject(vec != null ? vec.toArray() : null);
      }

      /* ------------------------------------------------------ hashtable */
      case 0x00A0: vm.followPointer(params[0]).clear(); return 0;
      case 0x00A1: return vm.makeTempObject(new SortHashtable());
      case 0x00A2: vm.followPointer(params[0]).put(vm.followPointer(params[1]), vm.followPointer(params[2])); return 0;
      case 0x00A3: return vm.makeTempObject(vm.followPointer(params[0]).get(vm.followPointer(params[1])));
      case 0x00A4: vm.followPointer(params[0]).remove(vm.followPointer(params[1])); return 0;
      case 0x00A5: return vm.makeTempObject(vm.followPointer(params[0]).keys());
      case 0x00A6: return vm.makeTempObject(vm.followPointer(params[0]).values());
      case 0x00A7: return vm.followPointer(params[0]).size();
      case 0x00A8: return vm.makeTempObject(vm.followPointer(params[0]).getKey(params[1]));
      case 0x00A9: return vm.makeTempObject(vm.followPointer(params[0]).getValue(params[1]));

      /* ----------------------------------------------------- globals/etc */
      case 0x00B1: console.log('[vm]', params[0]); return 0;
      case 0x00B2: console.log('[vm]', String(vm.followPointer(params[0]))); return 0;
      case 0x1000: this.globals.delete(String(vm.followPointer(params[0]))); return 0;
      case 0x1001: this.globals.set(String(vm.followPointer(params[0])), params[1] | 0); return 0;
      case 0x1002: this.globals.set(String(vm.followPointer(params[0])), String(vm.followPointer(params[1]) ?? '')); return 0;
      case 0x1003: { const v = this.globals.get(String(vm.followPointer(params[0]))); return typeof v === 'number' ? v | 0 : 0; }
      case 0x1004: { const v = this.globals.get(String(vm.followPointer(params[0]))); return vm.makeTempObject(typeof v === 'string' ? v : null); }
      case 0x1005: return vm.makeTempObject(this.globals.get(String(vm.followPointer(params[0]))) ?? null);
      case 0x1006: this.globals.set(String(vm.followPointer(params[0])), vm.followPointer(params[1])); return 0;
      case 0x1008: return vm.makeTempObject(null); // System.getProperty — nothing meaningful in a browser

      /* -------------------------------------------------- misc lifecycle */
      case 0x2001: vm.pauseProcess(); return 0;               // PauseUICycle
      case 0x2002: vm.continueProcess(params[0]); return 0;   // ResumeUICycle
      case 0x2004: return P.getSystemTime() | 0;
      case 0x2012: P.exitGame(); return 0;
      case 0x2013: P.closeConnection(); return 0;
      case 0x201C: return P.getTick() | 0;
      case 0x00D0: return vm.makeTempObject(P.loadFile(String(vm.followPointer(params[0]) ?? '')));
      case 0x00D1: return P.saveFile(String(vm.followPointer(params[0]) ?? ''), vm.followPointer(params[1])) ? TRUE : FALSE;
      case 0x00D2: P.deleteFile(String(vm.followPointer(params[0]) ?? '')); return 0;
      case 0x00C5: return vm.makeTempObject(P.loadResourceFile(String(vm.followPointer(params[0]) ?? '')));
      case 0x00C7: // SplitString(msg, width) — Tool.formatText
        return vm.makeTempObject(P.formatText(String(vm.followPointer(params[0]) ?? ''), params[1], P.font));

      default:
        return UNHANDLED;
    }
  }
}

/**
 * Compose syscall layers. The FIRST layer that claims an id wins; if none do,
 * the platform's unimplemented() hook fires once per id and the result is 0 —
 * exactly what VM.java's fall-through does, but visible.
 */
export function composeHost(platform = null, ...hosts) {
  return (vm, funcID, params) => {
    for (const host of hosts) {
      const result = host.handle(vm, funcID, params);
      if (result !== UNHANDLED) return result;
    }
    platform?.unimplemented?.(funcID, 'no syscall layer implements this');
    return 0;
  };
}
