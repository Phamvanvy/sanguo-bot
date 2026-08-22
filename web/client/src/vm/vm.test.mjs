/*
 * Tests for the GTVM container + instruction set:
 *   node --test web/client/src/vm/vm.test.mjs
 *
 * The strong test here is the sweep at the bottom: every .etf the server ships for a
 * UI model is parsed and fully disassembled. Instruction lengths have to be exactly
 * right for that to work — one wrong length desynchronises the decoder and it walks
 * into a byte that is not an opcode — so a clean sweep over 250k instructions pins
 * the whole table at once.
 *
 * Tests needing selfhost/runtime/data skip themselves when it is absent.
 */
import assert from 'node:assert/strict';
import test from 'node:test';
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

import { parseETF } from './etf.js';
import { OP, OP_NAME, INSTRUCTION_LENGTH, instructionLength, instructions } from './isa.js';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../..');
const DATA = process.env.SANGUO_DATA ?? path.join(REPO, 'selfhost/runtime/data');
const MODEL = process.env.SANGUO_UIMODEL ?? 'Flash';
const SCRIPTS = path.join(DATA, 'scripts', MODEL);

const haveScripts = fs.existsSync(SCRIPTS);
const skipNoData = haveScripts ? false : `no scripts at ${SCRIPTS}`;

/** @param {string} id e.g. "lib_builtin" */
function loadEtf(id) {
  const file = path.join(SCRIPTS, `${id}_${MODEL}.etf.gz`);
  return parseETF(new Uint8Array(zlib.gunzipSync(fs.readFileSync(file))));
}

test('instruction lengths match the operands the VM reads', () => {
  assert.equal(INSTRUCTION_LENGTH[OP.SYSCALL], 5);        // id:i16, parCount:u8, hasRet:u8
  assert.equal(INSTRUCTION_LENGTH[OP.SYSCALLSAVEVS], 9);  // ...plus the i32 target slot
  assert.equal(INSTRUCTION_LENGTH[OP.CALL], 4);           // parCount:u8, funcId:i16
  assert.equal(INSTRUCTION_LENGTH[OP.CALLPTR], 2);        // parCount:u8, callee on the stack
  assert.equal(INSTRUCTION_LENGTH[OP.RET], 1);
  assert.equal(INSTRUCTION_LENGTH[OP.JMP], 3);
  assert.equal(OP_NAME[OP.LOADVSSTLOAD8], 'LOADVSSTLOAD8');
});

test('TSWITCH length covers its dense jump table', () => {
  // op | default:i16 | first:i32 | last:i32 | (last-first+1) x i16
  const code = new Uint8Array(11 + 2 * 4);
  code[0] = OP.TSWITCH;
  code.set([0, 0, 0, 3], 3);  // first = 3
  code.set([0, 0, 0, 6], 7);  // last  = 6
  assert.equal(instructionLength(code, 0), 19);
});

test('LSWITCH length scales with its value width', () => {
  // op | default:i16 | count:i16 | condBytes:u8 | count x (condBytes + i16)
  const code = new Uint8Array(64);
  code[0] = OP.LSWITCH;
  code[3] = 0; code[4] = 5; // count = 5
  code[5] = 4;              // 4-byte case values
  assert.equal(instructionLength(code, 0), 6 + 5 * 6);
  code[5] = 1;
  assert.equal(instructionLength(code, 0), 6 + 5 * 3);
});

test('an unknown opcode is an error, not a silent resync', () => {
  assert.throws(() => instructionLength(Uint8Array.of(0x00), 0), /unknown opcode/);
  assert.throws(() => instructionLength(Uint8Array.of(0xff), 0), /unknown opcode/);
});

test('rejects anything that is not an ETF image', () => {
  assert.throws(() => parseETF(Uint8Array.of(0x50, 0x4b, 0x03, 0x04, 0, 0, 0, 0)), /bad magic/);
});

test('lib_builtin parses to the structure the client expects', { skip: skipNoData }, () => {
  const etf = parseETF(new Uint8Array(zlib.gunzipSync(
    fs.readFileSync(path.join(SCRIPTS, `lib_builtin_${MODEL}.etf.gz`)))));
  assert.equal(etf.languageVersion, 1);          // EGL1: has the callback + library sections
  assert.equal(etf.name, 'lib_builtin');
  assert.ok(etf.functions.length > 100, `functions: ${etf.functions.length}`);
  assert.ok(etf.stringTable.length > 100, `strings: ${etf.stringTable.length}`);

  // The .info file beside it is the compiler's symbol dump: callback ids must agree.
  const info = fs.readFileSync(path.join(SCRIPTS, `lib_builtin_${MODEL}.info`), 'latin1');
  const declared = new Map();
  for (const m of info.matchAll(/^(\d+)=CALLBACK (\S+)/gm)) declared.set(m[2], Number(m[1]));
  assert.ok(declared.size > 0, 'no CALLBACK lines in the .info file');
  for (const [name, id] of declared) assert.equal(etf.callbacks.get(name), id, `callback ${name}`);
});

test('the Vietnamese strings survive UTF-16 decoding', { skip: skipNoData }, () => {
  const etf = loadEtf('lib_builtin');
  const joined = etf.stringTable.join('\n');
  assert.match(joined, /[À-ỹ]/, 'expected Vietnamese diacritics in the string table');
});

test('game_world links the libraries it calls into', { skip: skipNoData }, () => {
  const etf = loadEtf('game_world');
  assert.ok(etf.libNames.includes('lib_builtin'), `libs: ${etf.libNames.join(', ')}`);
});

test('every shipped script disassembles end to end', { skip: skipNoData }, () => {
  const files = fs.readdirSync(SCRIPTS).filter((f) => f.endsWith('.etf.gz'));
  assert.ok(files.length >= 100, `only ${files.length} scripts found`);

  let totalInstructions = 0;
  const seen = new Set();
  for (const file of files) {
    const etf = parseETF(new Uint8Array(zlib.gunzipSync(fs.readFileSync(path.join(SCRIPTS, file)))));
    for (const fn of etf.functions) {
      let last = fn.start;
      for (const { eip, op, len } of instructions(etf.code, fn.start, fn.end)) {
        seen.add(op);
        totalInstructions++;
        last = eip + len;
      }
      // Landing exactly on the boundary is the real assertion: a wrong length would
      // either overshoot into the next function or stop short inside this one.
      assert.equal(last, fn.end, `${file}: function decode ended at ${last}, not ${fn.end}`);
    }
  }
  assert.ok(totalInstructions > 100000, `only ${totalInstructions} instructions`);
  for (const op of seen) assert.ok(OP_NAME[op], `opcode 0x${op.toString(16)} has no name`);
});
