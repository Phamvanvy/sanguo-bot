/*
 * Syscall census over the original UI scripts.
 *
 * VM.java defines 796 syscalls, but a client only has to implement the ones the
 * scripts it loads actually call. This walks every .etf the server ships for a
 * UI model, disassembles each function, and reports which syscall ids appear —
 * so the port is scoped from the bytecode instead of from a guess.
 *
 *   node tools/vm/census.mjs [--model Flash] [--scripts <dir>] [--top N] [--json out.json]
 *
 * Syscall names come from the `case 0x1234: // 0x1234=ret Name(args)` comments in
 * the client's own VM.java, so an id we cannot name is a red flag, not a gap here.
 */
import { readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { gunzipSync } from 'node:zlib';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

import { parseETF } from '../../web/client/src/vm/etf.js';
import { OP, OP_NAME, instructions } from '../../web/client/src/vm/isa.js';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
/** the Java clients that could have compiled these scripts, newest listed first */
const VM_JAVA_SOURCES = {
  'Sanguo1.0-Client': 'Game/Sanguo1.0-Client/src/com/pip/ui/VM.java',
  sangobuildVn: 'Game/sangobuildVn/client/src/com/pip/ui/VM.java',
  sangobuildtw: 'Game/sangobuildtw/client/src/com/pip/ui/VM.java',
};

function parseArgs(argv) {
  const args = { model: 'Flash', scripts: null, top: 0, json: null, vm: 'Sanguo1.0-Client' };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--model') args.model = argv[++i];
    else if (a === '--scripts') args.scripts = argv[++i];
    else if (a === '--top') args.top = Number(argv[++i]);
    else if (a === '--json') args.json = argv[++i];
    else if (a === '--vm') args.vm = argv[++i];
    else throw new Error(`unknown argument ${a}`);
  }
  args.scripts ??= path.join(ROOT, 'selfhost/runtime/data/scripts', args.model);
  if (!VM_JAVA_SOURCES[args.vm]) {
    throw new Error(`--vm must be one of ${Object.keys(VM_JAVA_SOURCES).join(', ')}`);
  }
  return args;
}

/** id -> signature, read out of the given client's VM.java syscall switch. */
function syscallNames(vmJava) {
  // The 2014 sources are GBK; only the ASCII part of each comment is needed, and
  // latin1 keeps the byte offsets intact so the regex still matches.
  const src = readFileSync(vmJava, 'latin1');
  const names = new Map();
  // Most cases are commented `// 0xNNNN=ret Name(args)`, a minority just `// Name`.
  const re = /case\s+0x([0-9A-Fa-f]{1,4})\s*:\s*(?:\{\s*)?\/\/\s*(?:0x[0-9A-Fa-f]+\s*=\s*)?([^\r\n]*)/g;
  for (const m of src.matchAll(re)) {
    // strip the trailing Chinese prose, keep the signature
    const sig = m[2].replace(/[^\x20-\x7e]+.*$/, '').trim();
    names.set(parseInt(m[1], 16), sig);
  }
  return names;
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const names = syscallNames(path.join(ROOT, VM_JAVA_SOURCES[args.vm]));

  const files = readdirSync(args.scripts).filter((f) => f.endsWith('.etf.gz')).sort();
  if (files.length === 0) throw new Error(`no .etf.gz under ${args.scripts}`);

  /** @type {Map<number, {calls: number, scripts: Set<string>}>} */
  const syscalls = new Map();
  const opCounts = new Map();
  const scripts = [];
  const failures = [];

  for (const file of files) {
    const id = file.replace(/_[^_]*\.etf\.gz$/, '');
    try {
      const etf = parseETF(gunzipSync(readFileSync(path.join(args.scripts, file))));
      let instCount = 0;
      let scriptSyscalls = 0;
      for (const fn of etf.functions) {
        for (const { eip, op } of instructions(etf.code, fn.start, fn.end)) {
          instCount++;
          opCounts.set(op, (opCounts.get(op) ?? 0) + 1);
          if (op === OP.SYSCALL || op === OP.SYSCALLSAVEVS) {
            const sid = (etf.code[eip + 1] << 8) | etf.code[eip + 2];
            let e = syscalls.get(sid);
            if (!e) syscalls.set(sid, (e = { calls: 0, scripts: new Set() }));
            e.calls++;
            e.scripts.add(id);
            scriptSyscalls++;
          }
        }
      }
      scripts.push({
        id, name: etf.name, functions: etf.functions.length, strings: etf.stringTable.length,
        callbacks: etf.callbacks.size, libs: etf.libNames, instructions: instCount,
        syscalls: scriptSyscalls,
      });
    } catch (err) {
      failures.push({ file, error: String(err.message ?? err) });
    }
  }

  const totalInst = scripts.reduce((n, s) => n + s.instructions, 0);
  console.log(`scripts: ${scripts.length}/${files.length} parsed` +
    (failures.length ? `, ${failures.length} FAILED` : ''));
  for (const f of failures) console.log(`  FAIL ${f.file}: ${f.error}`);
  console.log(`functions: ${scripts.reduce((n, s) => n + s.functions, 0)}`);
  console.log(`instructions: ${totalInst}`);
  console.log(`distinct opcodes used: ${opCounts.size}`);
  console.log(`distinct syscalls used: ${syscalls.size} (of ${names.size} defined in VM.java)`);

  const unnamed = [...syscalls.keys()].filter((id) => !names.has(id));
  if (unnamed.length) {
    console.log(`syscalls with no VM.java case: ${unnamed.length} ` +
      `(${unnamed.slice(0, 12).map((i) => '0x' + i.toString(16)).join(' ')}${unnamed.length > 12 ? ' ...' : ''})`);
  }

  // Which syscalls does the always-loaded core need? Those are the ones that must
  // work before anything renders at all.
  const CORE = new Set(['lib_builtin', 'game_init', 'game_world', 'game_panel',
    'game_role', 'game_npc', 'game_netplayer', 'game_icon']);
  const coreIds = [...syscalls.entries()].filter(([, e]) => [...e.scripts].some((s) => CORE.has(s)));
  console.log(`syscalls reachable from the core scripts (${[...CORE].join(', ')}): ${coreIds.length}`);

  if (args.top > 0) {
    console.log(`\ntop ${args.top} syscalls by call sites:`);
    const ranked = [...syscalls.entries()].sort((a, b) => b[1].calls - a[1].calls).slice(0, args.top);
    for (const [id, e] of ranked) {
      const hex = '0x' + id.toString(16).toUpperCase().padStart(4, '0');
      console.log(`  ${hex}  ${String(e.calls).padStart(5)} calls  ` +
        `${String(e.scripts.size).padStart(3)} scripts  ${names.get(id) ?? '(unknown)'}`);
    }
    console.log(`\nopcodes by frequency:`);
    for (const [op, n] of [...opCounts.entries()].sort((a, b) => b[1] - a[1])) {
      console.log(`  0x${op.toString(16).padStart(2, '0')} ${(OP_NAME[op] ?? '?').padEnd(16)} ${n}`);
    }
  }

  if (args.json) {
    writeFileSync(args.json, JSON.stringify({
      model: args.model,
      scripts,
      failures,
      syscalls: [...syscalls.entries()]
        .sort((a, b) => a[0] - b[0])
        .map(([id, e]) => ({
          id, hex: '0x' + id.toString(16).toUpperCase().padStart(4, '0'),
          signature: names.get(id) ?? null, calls: e.calls, scripts: [...e.scripts].sort(),
        })),
    }, null, 2));
    console.log(`\nwrote ${args.json}`);
  }
}

main();
