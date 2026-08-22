/*
 * Tests for the core syscall layer:
 *   node --test web/client/src/vm/syscalls-core.test.mjs
 *
 * Two layers, like vm-exec.test.mjs:
 *  1. direct host.handle() calls against a bare VM (object round-trips through
 *     makeTempObject/followPointer included),
 *  2. hand-assembled scripts that reach the host through the real SYSCALL opcode,
 *     so pointer plumbing through the interpreter is covered too.
 */
import assert from 'node:assert/strict';
import test from 'node:test';

import { OP } from './isa.js';
import { VM } from './vm.js';
import { CoreHost, getNextRnd } from './syscalls-core.js';
import { JavaVector, SortHashtable } from './runtime.js';
import { UASegment } from './ua-segment.js';
import { encodeModifiedUTF } from '../net/ua-codec.js';
import { JavaRandom } from '../assets/java-random.js';

/* ------------------------------------------------------------- harness */

const i32b = (v) => [(v >> 24) & 0xff, (v >> 16) & 0xff, (v >> 8) & 0xff, v & 0xff];
const i16b = (v) => [(v >> 8) & 0xff, v & 0xff];

/** SYSCALL: [op][i16 funcID][u8 parCount][u8 hasRet] — 5 bytes total. */
const syscall = (id, parCount, hasRet) => [OP.SYSCALL, ...i16b(id), parCount, hasRet];

function makeVM({ host = null, strings = [] } = {}) {
  const errors = [];
  const vm = new VM({ host, onError: (err) => errors.push(err) });
  vm.init({
    languageVersion: 1, fileVersion: 0, libraryID: 0, heapSize: 64, stackSize: 512,
    name: 'test', description: '', stringTable: strings,
    functions: [{ paramCount: 0, localVariables: 0, start: 0, end: 1 }],
    functionTable: new Int32Array([0, 0, 1]),
    code: new Uint8Array([OP.RET]), // RET
    callbacks: new Map(), libNames: [],
  });
  vm.link(() => { throw new Error('no libraries'); });
  vm.errors = errors;
  return vm;
}

/** call a syscall directly and return its int result */
function call(vm, host, id, params = []) {
  return host.handle(vm, id, Int32Array.from(params));
}

/** follow a pointer the syscall returned and hand back the JS value */
function value(vm, ptr) {
  return vm.followPointer(ptr);
}

/* --------------------------------------------------------- unit tests */

test('string syscalls round-trip through the VM heap', () => {
  const vm = makeVM();
  const host = new CoreHost();

  const a = call(vm, host, 0x000f, [42]);            // IntToStr
  assert.equal(value(vm, a), '42');

  const b = call(vm, host, 0x0022, [a, a]);          // String_Append
  assert.equal(value(vm, b), '4242');

  const c = call(vm, host, 0x0026, [a, -7]);         // String_AppendInt
  assert.equal(value(vm, c), '42-7');

  assert.equal(call(vm, host, 0x0023, [b]), 4);      // String_Length
  assert.equal(call(vm, host, 0x0027, [b, b]), 1);   // String_Equal
  assert.equal(call(vm, host, 0x0025, [b, a, 0]), 0); // String_Find
  assert.equal(call(vm, host, 0x0028, [b, 2]), 0x34); // String_CharAt -> '4'
});

test('StrToInt follows Java Integer.parseInt semantics', () => {
  const vm = makeVM();
  const host = new CoreHost();
  const str = (s) => vm.makeTempObject(s);

  assert.equal(call(vm, host, 0x0005, [str('123')]), 123);
  assert.equal(call(vm, host, 0x0005, [str('-45')]), -45);
  assert.equal(call(vm, host, 0x0005, [str('u1f')]), 31);   // hex via 'u' prefix
  assert.equal(call(vm, host, 0x0005, [str('12abc')]), 0);  // Java throws -> 0
  assert.equal(call(vm, host, 0x0005, [str('')]), 0);
  assert.equal(call(vm, host, 0x0005, [str(' 7')]), 0);     // no whitespace in Java
});

test('Realize moves an object out of the temp ring', () => {
  const vm = makeVM();
  const host = new CoreHost();

  const tmp = vm.makeTempObject('hello');
  assert.ok((tmp & 0xfff) < vm.tempSpace, 'starts in the temp ring');

  const fixed = call(vm, host, 0x0010, [tmp]);
  assert.ok((fixed & 0xfff) >= vm.tempSpace, 'lands in the dynamic heap');
  assert.equal(value(vm, fixed), 'hello');
  assert.equal(vm.dynamicHeap[tmp & 0xfff], null, 'temp cell is released');

  assert.equal(call(vm, host, 0x0010, [0]), 0, 'null stays null');
});

test('vector and hashtable syscalls keep Java ordering and equality', () => {
  const vm = makeVM();
  const host = new CoreHost();

  const v = call(vm, host, 0x0091);
  assert.ok(value(vm, v) instanceof JavaVector);
  call(vm, host, 0x0093, [v, vm.makeTempObject('x')]);
  call(vm, host, 0x0093, [v, vm.makeTempObject('y')]);
  assert.equal(call(vm, host, 0x0092, [v]), 2);
  assert.equal(value(vm, call(vm, host, 0x0095, [v, 1])), 'y');
  call(vm, host, 0x0094, [v, 0]);
  assert.equal(call(vm, host, 0x0092, [v]), 1);
  assert.equal(value(vm, call(vm, host, 0x0095, [v, 0])), 'y');

  const h = call(vm, host, 0x00a1);
  assert.ok(value(vm, h) instanceof SortHashtable);
  const k1 = call(vm, host, 0x00b3, [5]);   // IntToObj(5)
  const k2 = call(vm, host, 0x00b3, [5]);   // a DIFFERENT box, same value
  call(vm, host, 0x00a2, [h, k1, vm.makeTempObject('five')]);
  assert.equal(value(vm, call(vm, host, 0x00a3, [h, k2])), 'five',
    'boxed Integer keys compare by value, like Java');
  assert.equal(call(vm, host, 0x00a7, [h]), 1);
  assert.equal(value(vm, call(vm, host, 0x00a8, [h, 0])).value, 5);
  call(vm, host, 0x00a4, [h, k1]);
  assert.equal(call(vm, host, 0x00a7, [h]), 0);
});

test('stream syscalls write and read back big-endian + modified UTF-8', () => {
  const vm = makeVM();
  const host = new CoreHost();
  const TEXT = 'Xin chào'; // 'à' is 2 bytes in modified UTF-8

  const out = call(vm, host, 0x0052);                 // Stream_Create2
  call(vm, host, 0x0058, [out, 0x01020304]);          // WriteInt
  call(vm, host, 0x0059, [out, -2]);                  // WriteShort
  call(vm, host, 0x005b, [out, 0x7f]);                // WriteByte
  call(vm, host, 0x005a, [out, vm.makeTempObject(TEXT)]); // WriteUTF

  // Stream_Length counts BYTES: the UTF body is encodeModifiedUTF minus its prefix
  const utfBytes = encodeModifiedUTF(TEXT).length - 2;
  assert.equal(utfBytes, TEXT.length + 1, 'the accented char really is 2 bytes');
  assert.equal(call(vm, host, 0x005d, [out]), 4 + 2 + 1 + 2 + utfBytes);

  const bytes = value(vm, call(vm, host, 0x005e, [out]));
  const inp = call(vm, host, 0x0051, [vm.makeTempObject(bytes)]);
  assert.equal(call(vm, host, 0x0053, [inp]), 0x01020304);
  assert.equal(call(vm, host, 0x0054, [inp]), -2);
  assert.equal(call(vm, host, 0x0055, [inp]), 0x7f);
  assert.equal(value(vm, call(vm, host, 0x0057, [inp])), TEXT);
});

test('UWAP segment syscalls build a sendable request', () => {
  const vm = makeVM();
  const host = new CoreHost();
  let sent = null;
  host.platform.sendRequest = (seg) => { sent = seg; return 77; };

  const seg = call(vm, host, 0x0071, [0x1234, 1]);    // UWAP_Create(type, needSerial)
  const s = value(vm, seg);
  assert.ok(s instanceof UASegment);
  assert.equal(s.type, 0x1234);
  assert.ok(s.serial > 0);
  call(vm, host, 0x0079, [seg, 0xdeadbeef | 0]);      // WriteInt
  call(vm, host, 0x007b, [seg, vm.makeTempObject('abc')]); // WriteString
  call(vm, host, 0x008d, [seg, 1]);                   // SetNeedResponse
  assert.equal(call(vm, host, 0x0088, [seg]), 77);    // SendRequest
  assert.equal(sent, s);
  assert.equal(sent.needResponse, true);

  // read back through the same object, like a script echoing fields
  assert.equal(call(vm, host, 0x0072, [seg]), 0x1234);
  assert.equal(call(vm, host, 0x008c, [seg]), s.serial);
  assert.equal(call(vm, host, 0x0074, [seg]), 0xdeadbeef | 0);
  assert.equal(value(vm, call(vm, host, 0x0078, [seg])), 'abc');
});

test('UWAP array writers/readers round-trip', () => {
  const vm = makeVM();
  const host = new CoreHost();
  const seg = call(vm, host, 0x0071, [1, 0]);
  // arrays go through the heap like every other object parameter
  call(vm, host, 0x0083, [seg, vm.makeTempObject(Int32Array.from([1, -2, 3]))]);
  call(vm, host, 0x0085, [seg, vm.makeTempObject(['a', 'b'])]);
  const ints = value(vm, call(vm, host, 0x007e, [seg]));
  assert.deepEqual([...ints], [1, -2, 3]);
  const strs = value(vm, call(vm, host, 0x0082, [seg]));
  assert.deepEqual(strs, ['a', 'b']);
});

test('global variable syscalls store ints, strings and objects', () => {
  const vm = makeVM();
  const host = new CoreHost();
  const name = vm.makeTempObject('quest_flag');

  call(vm, host, 0x1001, [name, 123]);
  assert.equal(call(vm, host, 0x1003, [name]), 123);
  call(vm, host, 0x1002, [name, vm.makeTempObject('on')]);
  assert.equal(call(vm, host, 0x1003, [name]), 0, 'a string global reads back as int 0');
  assert.equal(value(vm, call(vm, host, 0x1004, [name])), 'on');
  call(vm, host, 0x1000, [name]);
  assert.equal(value(vm, call(vm, host, 0x1004, [name])), null);
});

test('geometry helpers match Tool.java edge semantics', () => {
  const vm = makeVM();
  const host = new CoreHost();
  // rectIntersect: touching edges do NOT intersect
  assert.equal(call(vm, host, 0x1296, [0, 0, 10, 10, 10, 0, 5, 5]), 0);
  assert.equal(call(vm, host, 0x1296, [0, 0, 10, 10, 9, 0, 5, 5]), 1);
  // rectIn: inclusive point test
  assert.equal(call(vm, host, 0x127f, [0, 0, 10, 10, 10, 10]), 1);
  assert.equal(call(vm, host, 0x127f, [0, 0, 10, 10, 11, 5]), 0);
  // distance: 3-4-5
  assert.equal(call(vm, host, 0x110c, [0, 0, 3, 4]), 5);
});

test('PauseUICycle blocks the VM and ResumeUICycle hands back the value', () => {
  const host = new CoreHost();
  // VM.host is a FUNCTION; CoreHost is an object with .handle
  const vm = makeVM({ host: (v, id, p) => host.handle(v, id, p) });
  // func 0 with ONE local: PauseUICycle (no return); the resume value lands in
  // the top stack slot (that is Java's contract), which VRET then returns.
  const code = new Uint8Array([
    ...syscall(0x2001, 0, 0),
    OP.VRET,
  ]);
  vm.init({
    languageVersion: 1, fileVersion: 0, libraryID: 0, heapSize: 64, stackSize: 512,
    name: 'test', description: '', stringTable: [],
    // functionTable word = (paramCount << 16) | localVariables
    functions: [{ paramCount: 0, localVariables: 1, start: 0, end: code.length }],
    functionTable: new Int32Array([1, 0, code.length]),
    code,
    callbacks: new Map(), libNames: [],
  });
  vm.link(() => { throw new Error('no libraries'); });

  vm.execute(0);
  assert.equal(vm.isBlock(), true, 'script parks on PauseUICycle');
  vm.continueProcess(99);
  vm.resume();
  assert.equal(vm.isBlock(), false);
  assert.equal(vm.stack[vm.esp], 99, 'the resume value replaced the top stack slot');
});

test('getNextRnd mirrors Tool.getNextRnd bounds', () => {
  const rnd = new JavaRandom(12345);
  for (let i = 0; i < 1000; i++) {
    const v = getNextRnd(rnd, 10, 20);
    assert.ok(v >= 10 && v < 20, `out of range: ${v}`);
  }
  assert.equal(getNextRnd(rnd, 5, 5), 5, 'max <= min yields min');
});

/* ------------------------------------------- integration through SYSCALL */

test('a script calling IntToStr + String_Append through the SYSCALL opcode', () => {
  const host = new CoreHost();
  const vm = makeVM({ host: (v, id, p) => host.handle(v, id, p), strings: ['abc', 'def'] });
  const code = new Uint8Array([
    ...[OP.LOAD32, ...i32b(7)],
    ...syscall(0x000f, 1, 1),               // IntToStr -> "7"
    ...[OP.LOAD32, ...i32b(8)],
    ...syscall(0x000f, 1, 1),               // IntToStr -> "8"
    ...syscall(0x0022, 2, 1),               // String_Append -> "78"
    OP.VRET,
  ]);
  vm.init({
    languageVersion: 1, fileVersion: 0, libraryID: 0, heapSize: 64, stackSize: 512,
    name: 'test', description: '', stringTable: ['abc', 'def'],
    functions: [{ paramCount: 0, localVariables: 0, start: 0, end: code.length }],
    functionTable: new Int32Array([0, 0, code.length]),
    code,
    callbacks: new Map(), libNames: [],
  });
  vm.link(() => { throw new Error('no libraries'); });

  const ret = vm.callback(0);
  assert.deepEqual(vm.errors, []);
  assert.equal(vm.followPointer(ret), '78');
});