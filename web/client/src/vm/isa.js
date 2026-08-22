/*
 * GTVM instruction set — a transcription of the tables in
 * Game/sangobuildVn/client/src/com/pip/ui/VM.java.
 *
 * The client's whole UI is bytecode: every window, panel and button in the game
 * is a function in an .etf file that this instruction set executes. Nothing here
 * is invented — the opcode numbers, lengths and stack effects are copied from the
 * Java constants so that a disassembly can be checked against the original.
 *
 * INSTRUCTION_LENGTH[op] === 0 means either "not an instruction" or "variable
 * length" (only TSWITCH and LSWITCH, which carry a jump table); `instructionLength()`
 * below sizes those two the same way VM.execute does.
 */

export const OP = {
  ADD: 0x01, SUB: 0x02, MUL: 0x03, DIV: 0x04, MOD: 0x05,
  AND: 0x06, OR: 0x07, ANDB: 0x08, ORB: 0x09, LSHIFT: 0x0a, RSHIFT: 0x0b,
  INCV: 0x0c, ADDV8: 0x0d, SUBV8: 0x0e,
  EQ: 0x11, GT: 0x12, LT: 0x13, EQ8: 0x14, GT8: 0x15, LT8: 0x16, NE8: 0x17,
  INCVS: 0x18, ADDV8S: 0x19, SUBV8S: 0x1a, LOADVS: 0x1b, SAVEVS: 0x1c, DUP: 0x1d,
  JMP: 0x21, JEQ: 0x22, JNE: 0x23, CALL: 0x24, RET: 0x25, VRET: 0x26, SYSCALL: 0x27,
  ALOAD8: 0x28, ASAVE8: 0x29, STLOAD8: 0x2a, STSAVE8: 0x2b,
  TSWITCH: 0x2c, LSWITCH: 0x2d, CALLPTR: 0x2e,
  LOAD: 0x31, SAVE: 0x32, LOAD32: 0x33, LOAD16: 0x34, LOAD8: 0x35,
  ALOAD: 0x36, ASAVE: 0x37, ALLOC: 0x38, FREE: 0x39,
  STALLOC: 0x3a, STLOAD: 0x3b, STSAVE: 0x3c, LOADV: 0x3d, SAVEV: 0x3e, LOADFUNC: 0x3f,
  LOADVS3: 0x41, LOADVS2: 0x42, LOAD88: 0x43, LOAD8VS: 0x44, LOADVS8: 0x45,
  SYSCALLSAVEVS: 0x46, LOADVSSTLOAD8: 0x47, LOAD8VSSTLOAD8: 0x48,
  LOADVSADDALOAD: 0x49, LOADVSALOAD: 0x4a,
};

export const INSTRUCTION_MAX = 0x4a;

/** VM.INSTRUCTION_LENGTH */
export const INSTRUCTION_LENGTH = Int8Array.from([
  0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 6, 6, 0,
  0, 1, 1, 1, 2, 2, 2, 2, 5, 6, 6, 5, 5, 2, 0, 0,
  0, 3, 3, 3, 4, 1, 1, 5, 2, 2, 2, 2, 0, 0, 2, 0,
  0, 1, 1, 5, 3, 2, 1, 1, 2, 1, 3, 1, 1, 5, 5, 3,
  0, 13, 9, 3, 6, 6, 9, 6, 7, 5, 5,
]);

/** VM.STACK_EFFECT (CALL/RET/VRET/SYSCALL/CALLPTR are special-cased by the VM) */
export const STACK_EFFECT = Int8Array.from([
  0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 1, 0,
  0, -1, -1, -1, 0, 0, 0, 0, 0, 1, 1, 1, -1, 1, 0, 0,
  0, 0, -1, -1, 0, 0, 0, 0, 0, -2, 0, -2, -1, -1, 0, 0,
  0, 0, -2, 1, 1, 1, -1, -3, 0, -1, 1, -1, -3, 1, -1, 1,
  0, 3, 2, 2, 2, 2, 0, 1, 2, -1, 0,
]);

/** name for each opcode, for disassembly */
export const OP_NAME = (() => {
  const names = new Array(INSTRUCTION_MAX + 1).fill(null);
  for (const [name, op] of Object.entries(OP)) names[op] = name;
  return names;
})();

const be16 = (code, at) => (code[at] << 8) | code[at + 1];
const be32 = (code, at) =>
  ((code[at] << 24) | (code[at + 1] << 16) | (code[at + 2] << 8) | code[at + 3]) | 0;

/**
 * Byte length of the instruction at `eip`, including the variable-length jump
 * tables. Mirrors the `instLen` arithmetic inside VM.execute.
 *
 * @param {Uint8Array} code @param {number} eip
 */
export function instructionLength(code, eip) {
  const op = code[eip];
  switch (op) {
    case OP.TSWITCH: {
      // i16 default offset | i32 first | i32 last | (last-first+1) x i16 offset
      const first = be32(code, eip + 3);
      const last = be32(code, eip + 7);
      return 11 + 2 * (last - first + 1);
    }
    case OP.LSWITCH: {
      // i16 default offset | i16 count | i8 condBytes | count x (condBytes value, i16 offset)
      const count = be16(code, eip + 3);
      const condBytes = code[eip + 5];
      return 6 + count * (condBytes + 2);
    }
    default: {
      const len = op <= INSTRUCTION_MAX ? INSTRUCTION_LENGTH[op] : 0;
      if (len === 0) throw new RangeError(`GTVM: unknown opcode 0x${op.toString(16)} at ${eip}`);
      return len;
    }
  }
}

/**
 * Walk one function's code, yielding {eip, op, len}. Stops at the end of the
 * range; a bad opcode throws rather than silently resyncing on garbage.
 *
 * @param {Uint8Array} code @param {number} start @param {number} end
 */
export function* instructions(code, start, end) {
  let eip = start;
  while (eip < end) {
    const op = code[eip];
    const len = instructionLength(code, eip);
    yield { eip, op, len };
    eip += len;
  }
}
