/*
 * Execution tests for the GTVM interpreter port:
 *   node --test web/client/src/vm/vm-exec.test.mjs
 *
 * Two layers:
 *  1. Hand-assembled programs, one per instruction family. The expected values are
 *     Java's, not JavaScript's — int overflow wraps, division truncates toward zero,
 *     `>>` is arithmetic — because the scripts were compiled for a Java VM.
 *  2. A smoke pass over the real scripts the server ships: link them, call every
 *     CALLBACK they export, and require that the interpreter never fails to DECODE
 *     (unknown opcode / bad jump). Syscalls are stubbed, so scripts are free to
 *     take nonsense paths; what is under test is the machine, not the script.
 */
import assert from 'node:assert/strict';
import test from 'node:test';
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

import { parseETF } from './etf.js';
import { OP } from './isa.js';
import { VM, VMError, INIT, CYCLE, PROCESSPACKET, CYCLEUI, PAINT, DESTROY } from './vm.js';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../..');
const DATA = process.env.SANGUO_DATA ?? path.join(REPO, 'selfhost/runtime/data');
const MODEL = process.env.SANGUO_UIMODEL ?? 'Flash';
const SCRIPTS = path.join(DATA, 'scripts', MODEL);
const skipNoData = fs.existsSync(SCRIPTS) ? false : `no scripts at ${SCRIPTS}`;

/* ------------------------------------------------------------------ assembler */

const i32b = (v) => [(v >> 24) & 0xff, (v >> 16) & 0xff, (v >> 8) & 0xff, v & 0xff];
const i16b = (v) => [(v >> 8) & 0xff, v & 0xff];

/** `{ label }` marks a position; `{ ref }` is a 2-byte jump operand naming one. */
const label = (name) => ({ label: name });
const ref = (name) => ({ ref: name });

/**
 * Assemble a list of [op, ...operands] into bytes. Operands are numbers (already
 * byte-sized), arrays of bytes, or `ref('x')` placeholders resolved to the offset
 * of `label('x')` from the start of the function — which is exactly what JMP/JEQ/JNE
 * take, since the VM adds them to the function's code start.
 */
function assemble(items) {
  const out = [];
  const labels = new Map();
  const fixups = [];
  for (const item of items) {
    if (item.label !== undefined) { labels.set(item.label, out.length); continue; }
    const [op, ...operands] = item;
    out.push(op);
    for (const operand of operands) {
      if (operand && operand.ref !== undefined) {
        fixups.push({ at: out.length, name: operand.ref });
        out.push(0, 0);
      } else if (Array.isArray(operand)) {
        out.push(...operand);
      } else {
        out.push(operand & 0xff);
      }
    }
  }
  for (const { at, name } of fixups) {
    assert.ok(labels.has(name), `undefined label ${name}`);
    const target = labels.get(name);
    out[at] = (target >> 8) & 0xff;
    out[at + 1] = target & 0xff;
  }
  return Uint8Array.from(out);
}

/**
 * Build a runnable VM out of assembled functions, bypassing the file format.
 * @param {Array<{locals?: number, params?: number, code: Uint8Array}>} fns
 */
function makeVM(fns, { strings = [], heapSize = 64, stackSize = 256, host = null } = {}) {
  const code = new Uint8Array(fns.reduce((n, f) => n + f.code.length, 0));
  const functionTable = new Int32Array(fns.length * 3);
  let pos = 0;
  fns.forEach((f, i) => {
    code.set(f.code, pos);
    functionTable[i * 3] = ((f.params ?? 0) << 16) | (f.locals ?? 0);
    functionTable[i * 3 + 1] = pos;
    functionTable[i * 3 + 2] = pos + f.code.length;
    pos += f.code.length;
  });
  const errors = [];
  const vm = new VM({ host, onError: (err) => errors.push(err) });
  vm.init({
    languageVersion: 1, fileVersion: 0, libraryID: 0, heapSize, stackSize,
    name: 'test', description: '', stringTable: strings,
    functions: fns.map((f, i) => ({
      paramCount: f.params ?? 0, localVariables: f.locals ?? 0,
      start: functionTable[i * 3 + 1], end: functionTable[i * 3 + 2],
    })),
    functionTable, code, callbacks: new Map(), libNames: [],
  });
  vm.link(() => { throw new Error('no libraries in this test'); });
  vm.errors = errors;
  return vm;
}

/** Run function 0 and return the value it leaves on top of the stack. */
function run(vm, params = null) {
  const ret = vm.callback(0, params);
  assert.deepEqual(vm.errors, [], `VM reported: ${vm.errors.map((e) => e.message).join('; ')}`);
  return ret;
}

const push32 = (v) => [OP.LOAD32, i32b(v)];

/* ---------------------------------------------------------------- unit tests */

test('arithmetic follows Java int semantics', () => {
  // (7 * 6 - 2) / 4  ->  10
  const vm = makeVM([{ code: assemble([
    push32(7), push32(6), [OP.MUL], push32(2), [OP.SUB], push32(4), [OP.DIV], [OP.VRET],
  ]) }]);
  assert.equal(run(vm), 10);
});

test('multiplication wraps at 32 bits like Java, not like a double', () => {
  const vm = makeVM([{ code: assemble([push32(0x40000000), push32(4), [OP.MUL], [OP.VRET]]) }]);
  assert.equal(run(vm), 0);

  const vm2 = makeVM([{ code: assemble([push32(123456789), push32(987654321), [OP.MUL], [OP.VRET]]) }]);
  assert.equal(run(vm2), Math.imul(123456789, 987654321)); // -67153019, not 1.2e17
});

test('division and modulo truncate toward zero', () => {
  const div = makeVM([{ code: assemble([push32(-7), push32(2), [OP.DIV], [OP.VRET]]) }]);
  assert.equal(run(div), -3);
  const mod = makeVM([{ code: assemble([push32(-7), push32(2), [OP.MOD], [OP.VRET]]) }]);
  assert.equal(run(mod), -1);
});

test('RSHIFT is arithmetic and LSHIFT wraps', () => {
  const shr = makeVM([{ code: assemble([push32(-8), push32(1), [OP.RSHIFT], [OP.VRET]]) }]);
  assert.equal(run(shr), -4);
  const shl = makeVM([{ code: assemble([push32(1), push32(31), [OP.LSHIFT], [OP.VRET]]) }]);
  assert.equal(run(shl), -2147483648);
});

test('logical AND/OR yield 1/0, not the operands', () => {
  const vm = makeVM([{ code: assemble([push32(5), push32(9), [OP.AND], [OP.VRET]]) }]);
  assert.equal(run(vm), 1);
  const vm2 = makeVM([{ code: assemble([push32(0), push32(0), [OP.OR], [OP.VRET]]) }]);
  assert.equal(run(vm2), 0);
});

test('a JNE loop sums 1..10', () => {
  // local 0 = i, local 1 = total
  const vm = makeVM([{ locals: 2, code: assemble([
    push32(0), [OP.SAVEVS, i32b(1)],                  // total = 0
    push32(1), [OP.SAVEVS, i32b(0)],                  // i = 1
    label('top'),
    [OP.LOADVS, i32b(0)], push32(10), [OP.GT],        // i > 10 ?
    [OP.JEQ, ref('done')],
    [OP.LOADVS, i32b(1)], [OP.LOADVS, i32b(0)], [OP.ADD], [OP.SAVEVS, i32b(1)],
    [OP.INCVS, i32b(0)],
    [OP.JMP, ref('top')],
    label('done'),
    [OP.LOADVS, i32b(1)], [OP.VRET],
  ]) }]);
  assert.equal(run(vm), 55);
});

test('CALL and VRET restore the caller frame', () => {
  const caller = assemble([
    push32(21),
    [OP.CALL, 1, i16b(1)],   // double(21)
    [OP.VRET],
  ]);
  const callee = assemble([
    [OP.LOADVS, i32b(0)], [OP.LOADVS, i32b(0)], [OP.ADD], [OP.VRET],
  ]);
  const vm = makeVM([{ code: caller }, { params: 1, locals: 1, code: callee }]);
  assert.equal(run(vm), 42);
  assert.equal(vm.callCount, 0, 'call depth should unwind');
});

test('TSWITCH picks a dense branch and falls back to its default', () => {
  // switch (arg) { case 1: 100; case 2: 200; default: 999 }
  // Assembled by hand: TSWITCH carries its own jump table, and its offsets are
  // added to the END of the instruction rather than to the function start.
  const prefix = assemble([[OP.LOADVS, i32b(0)]]);
  const instLen = 11 + 2 * 2; // first = 1, last = 2
  const tail = assemble([
    label('c1'), push32(100), [OP.VRET],
    label('c2'), push32(200), [OP.VRET],
    label('def'), push32(999), [OP.VRET],
  ]);
  const base = prefix.length + instLen;
  const c1 = 0, c2 = 6, def = 12; // offsets inside `tail`
  const tsw = Uint8Array.from([
    OP.TSWITCH, ...i16b(def), ...i32b(1), ...i32b(2), ...i16b(c1), ...i16b(c2),
  ]);
  assert.equal(tsw.length, instLen);
  const code = new Uint8Array(prefix.length + tsw.length + tail.length);
  code.set(prefix, 0); code.set(tsw, prefix.length); code.set(tail, base);

  for (const [arg, expected] of [[1, 100], [2, 200], [7, 999], [0, 999]]) {
    const vm = makeVM([{ params: 1, locals: 1, code }]);
    assert.equal(run(vm, Int32Array.of(arg)), expected, `TSWITCH(${arg})`);
  }
});

test('LSWITCH binary-searches its sparse table', () => {
  const prefix = assemble([[OP.LOADVS, i32b(0)]]);
  const cases = [[100, 0], [5000, 6], [70000, 12]]; // value -> offset into the tail
  const instLen = 6 + cases.length * (4 + 2);
  const tail = assemble([
    push32(11), [OP.VRET],
    push32(22), [OP.VRET],
    push32(33), [OP.VRET],
    push32(44), [OP.VRET],   // default
  ]);
  const lsw = Uint8Array.from([
    OP.LSWITCH, ...i16b(18), ...i16b(cases.length), 4,
    ...cases.flatMap(([v, off]) => [...i32b(v), ...i16b(off)]),
  ]);
  assert.equal(lsw.length, instLen);
  const code = new Uint8Array(prefix.length + lsw.length + tail.length);
  code.set(prefix, 0); code.set(lsw, prefix.length); code.set(tail, prefix.length + lsw.length);

  for (const [arg, expected] of [[100, 11], [5000, 22], [70000, 33], [1, 44], [999999, 44]]) {
    const vm = makeVM([{ params: 1, locals: 1, code }]);
    assert.equal(run(vm, Int32Array.of(arg)), expected, `LSWITCH(${arg})`);
  }
});

test('int arrays round-trip through ALLOC/ASAVE/ALOAD', () => {
  // arr = new int[4]; arr[2] = 1234; return arr[2]
  const vm = makeVM([{ locals: 1, code: assemble([
    push32(4), [OP.ALLOC, 3], [OP.SAVEVS, i32b(0)],
    push32(1234), [OP.LOADVS, i32b(0)], push32(2), [OP.ASAVE],
    [OP.LOADVS, i32b(0)], push32(2), [OP.ALOAD], [OP.VRET],
  ]) }]);
  assert.equal(run(vm), 1234);
});

test('pointers carry their data type in the top bits', () => {
  const vm = makeVM([{ code: assemble([push32(4), [OP.ALLOC, 3], [OP.VRET]]) }]);
  const ptr = run(vm);
  assert.equal((ptr >> 26) & 0x1f, 19, 'int[] should be data type 3 + 16');
  assert.ok(vm.followPointer(ptr) instanceof Int32Array);
  assert.equal(vm.followPointer(ptr).length, 4);
});

test('object arrays store realized objects, and element pointers resolve back', () => {
  const vm = makeVM([{ locals: 2, code: assemble([
    push32(2), [OP.ALLOC, 11], [OP.SAVEVS, i32b(0)],              // String[2]
    [OP.LOADVS, i32b(1)],                                          // value: a temp object
    [OP.LOADVS, i32b(0)], push32(1), [OP.ASAVE],
    [OP.LOADVS, i32b(0)], push32(1), [OP.ALOAD], [OP.VRET],
  ]) }]);
  const strPtr = vm.makeTempObject('xin chào');
  const elemPtr = run(vm, Int32Array.of(0, strPtr));
  assert.equal(vm.followPointer(elemPtr), 'xin chào');
  assert.ok((elemPtr & 0x02000000) !== 0, 'should be an element pointer');
});

test('structs are int arrays reached through STALLOC/STSAVE8/STLOAD8', () => {
  const vm = makeVM([{ locals: 1, code: assemble([
    [OP.STALLOC, i16b(4)], [OP.SAVEVS, i32b(0)],
    push32(77), [OP.LOADVS, i32b(0)], [OP.STSAVE8, 3],
    [OP.LOADVS, i32b(0)], [OP.STLOAD8, 3], [OP.VRET],
  ]) }]);
  assert.equal(run(vm), 77);
});

test('string-table pointers resolve, including through a library', () => {
  const vm = makeVM([{ code: assemble([[OP.VRET]]) }], { strings: ['một', 'hai'] });
  assert.equal(vm.followPointer(0x80000000 | 1), 'hai');
  const lib = makeVM([{ code: assemble([[OP.VRET]]) }], { strings: ['lib0'] });
  lib.libraryID = 7;
  vm.libraries = [vm, lib];
  assert.equal(vm.followPointer(0x80000000 | (7 << 16) | 0), 'lib0');
  assert.equal(vm.followPointer(0), null);
});

test('SYSCALL passes its arguments and pushes the result', () => {
  const seen = [];
  const vm = makeVM([{ code: assemble([
    push32(11), push32(22), [OP.SYSCALL, i16b(0x1234), 2, 1], [OP.VRET],
  ]) }], {
    host: (_vm, id, params) => { seen.push([id, [...params]]); return 99; },
  });
  assert.equal(run(vm), 99);
  assert.deepEqual(seen, [[0x1234, [11, 22]]]);
});

test('SYSCALLSAVEVS stores straight into a local', () => {
  const vm = makeVM([{ locals: 2, code: assemble([
    push32(5), [OP.SYSCALLSAVEVS, i16b(0x0004), 1, 1, i32b(1)],
    [OP.LOADVS, i32b(1)], [OP.VRET],
  ]) }], { host: () => 4242 });
  assert.equal(run(vm), 4242);
});

test('a throwing syscall yields 0 and is reported, as Java did', () => {
  const errors = [];
  const vm = makeVM([{ code: assemble([[OP.SYSCALL, i16b(0x0099), 0, 1], [OP.VRET]]) }], {
    host: () => { throw new Error('not ported yet'); },
  });
  vm.onError = (err) => errors.push(err);
  assert.equal(vm.callback(0, null), 0);
  assert.equal(errors.length, 1);
  assert.match(errors[0].message, /not ported yet/);
});

test('an unknown opcode stops the script instead of running on', () => {
  const errors = [];
  const vm = makeVM([{ code: Uint8Array.of(0x7f, OP.VRET) }]);
  vm.onError = (err) => errors.push(err);
  vm.callback(0, null);
  assert.equal(errors.length, 1);
  assert.ok(errors[0] instanceof VMError);
  assert.match(errors[0].message, /unknown opcode/);
});

test('the temp-object ring wraps at tempSpace and never gets freed', () => {
  const vm = makeVM([{ code: assemble([[OP.VRET]]) }]);
  const first = vm.makeTempObject('first');           // lands in slot 0
  assert.equal(vm.followPointer(first), 'first');
  for (let i = 0; i < vm.tempSpace; i++) vm.makeTempObject(`filler ${i}`);
  // slots 1..31 then wrap: the last filler overwrites slot 0
  assert.equal(vm.followPointer(first), `filler ${vm.tempSpace - 1}`, 'ring should have wrapped');
  vm.free(first);
  assert.equal(vm.followPointer(first), `filler ${vm.tempSpace - 1}`, 'temp slots survive free()');
});

/* ------------------------------------------------------- real bytecode smoke */

function loadScript(id) {
  return parseETF(new Uint8Array(zlib.gunzipSync(
    fs.readFileSync(path.join(SCRIPTS, `${id}_${MODEL}.etf.gz`)))));
}

test('the real scripts link and decode when actually executed', { skip: skipNoData }, () => {
  const wanted = ['lib_builtin', 'game_init', 'game_world', 'game_panel', 'ui_bag'];
  /** @type {Map<string, VM>} */
  const loaded = new Map();
  const decodeErrors = [];
  const syscallIds = new Set();

  const host = (_vm, id) => { syscallIds.add(id & 0xffff); return 0; };
  const onError = (err) => {
    // Only DECODE failures indict the interpreter. With every syscall stubbed to 0,
    // scripts legitimately divide by zero, index a null the host should have given
    // them, or spin waiting for a reply — the Java client swallowed exactly those
    // the same way, and the budget below turns the spinning ones into an error.
    if (/unknown opcode|budget exhausted/.test(err.message)) decodeErrors.push(err.message);
  };

  const load = (id) => {
    if (loaded.has(id)) return loaded.get(id);
    const etf = loadScript(id);
    const vm = new VM({ host, onError });
    vm.init(etf);
    vm.instructionLimit = 2_000_000;
    loaded.set(id, vm);
    for (const lib of etf.libNames) load(lib);
    return vm;
  };
  for (const id of wanted) load(id);
  for (const vm of loaded.values()) vm.link((name) => loaded.get(name));

  let called = 0;
  for (const [id, vm] of loaded) {
    // the six fixed interfaces the client drives every script through...
    for (const iface of [INIT, CYCLE, PROCESSPACKET, CYCLEUI, PAINT, DESTROY]) {
      vm.execute(iface, null);
      called++;
      assert.deepEqual(decodeErrors, [], `${id} interface ${iface}: ${decodeErrors[0]}`);
    }
    // ...plus everything it exported by name
    for (const [name, funcId] of vm.callbacks) {
      vm.callback(funcId, null);
      called++;
      assert.deepEqual(decodeErrors, [], `${id}.${name}: ${decodeErrors[0]}`);
    }
  }
  assert.ok(called > 60, `only ${called} entry points exercised`);
  assert.ok(syscallIds.size > 20, `only ${syscallIds.size} distinct syscalls reached`);
});
