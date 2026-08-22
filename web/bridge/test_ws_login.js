#!/usr/bin/env node
/*
 * G1 end-to-end integration test: log in to the world server THROUGH the WebSocket bridge,
 * using the SAME shared codec the browser client will use (web/client/src/net/*).
 *
 * This is the WS-transport twin of selfhost/probe_login.py. It exercises the whole G1 stack:
 *   opcodes -> UASegmentWriter -> frameSegment -> WS -> bridge -> TCP -> world
 *   world -> TCP -> bridge -> WS -> UAFrameAccumulator -> UASegmentReader
 *
 * Prereqs: world server up, and `node bridge.js` running.
 * Usage: node test_ws_login.js --url ws://127.0.0.1:8090 --name vypv1 --password 123456
 * Credentials come from CLI/env (SANGO_USER/SANGO_PASS), never hardcoded/committed.
 */
import { WebSocket } from 'ws';
import { UASegmentWriter, UASegmentReader } from '../client/src/net/ua-codec.js';
import { frameSegment, UAFrameAccumulator } from '../client/src/net/ua-framing.js';
import { OpCode } from '../client/src/net/opcodes.js';

function arg(name, def) {
  const i = process.argv.indexOf('--' + name);
  return i >= 0 && i + 1 < process.argv.length ? process.argv[i + 1] : def;
}
const URL = arg('url', process.env.BRIDGE_URL || 'ws://127.0.0.1:8090');
const NAME = arg('name', process.env.SANGO_USER || 'vypv1');
const PASSWORD = arg('password', process.env.SANGO_PASS || '123456');
const MODEL = arg('model', 'GenericMidp2/GenericMidp2');   // server splits on '/': MODEL/JVMCODE
const VERSION = arg('version', '2.4-CCCCCPiP');            // server splits on '-': VERSION-CHANNEL

function buildLoginFrame() {
  // ACCOUNT_LOGIN_CLIENT body (PlayerPacketHandler.accountLogin):
  //   int serial | UTF name | UTF password | UTF model | UTF version
  //   | UTF realPhone | int playerID | UTF IMEI
  const w = new UASegmentWriter(OpCode.ACCOUNT_LOGIN_CLIENT, /*needSerial*/ true, /*serial*/ 1);
  w.writeString(NAME).writeString(PASSWORD).writeString(MODEL).writeString(VERSION)
   .writeString('').writeInt(-1).writeString('');
  return frameSegment(w.toBytes(), 'A');
}

const hex = (b, n = 48) => Buffer.from(b.subarray(0, n)).toString('hex').replace(/(..)/g, '$1 ').trim();

function main() {
  const ws = new WebSocket(URL);
  ws.binaryType = 'nodebuffer';
  const acc = new UAFrameAccumulator();
  const timeout = setTimeout(() => { console.error('[!] TIMEOUT waiting for login response'); process.exit(1); }, 12000);

  ws.on('open', () => {
    const pkt = buildLoginFrame();
    console.log(`[*] WS open ${URL}`);
    console.log(`[>] TX ACCOUNT_LOGIN_CLIENT(${OpCode.ACCOUNT_LOGIN_CLIENT}) ${pkt.length}B: ${hex(pkt)}`);
    ws.send(pkt, { binary: true });
  });

  ws.on('message', (data) => {
    const chunk = Buffer.isBuffer(data) ? data : Buffer.from(data);
    let frames;
    try { frames = acc.push(chunk); } catch (e) { console.error('[!] framing error:', e.message); process.exit(1); }
    for (const f of frames) {
      const r = new UASegmentReader(f.segment);
      console.log(`[<] RX UA frame: opcode=${r.opcode} segLen=${f.segment.length}`);
      if (r.opcode === OpCode.ACCOUNT_LOGIN_SERVER) {
        const serial = r.readInt();
        const accountId = r.readInt();
        const name = r.readString();
        const iMoney = r.readInt();
        if (!(accountId > 0 && name === NAME)) {
          console.error(`[!] FAIL: response mismatch accountId=${accountId} name=${JSON.stringify(name)}`);
          process.exit(1);
        }
        console.log(`[=] serial=${serial} accountId=${accountId} name=${JSON.stringify(name)} iMoney=${iMoney}`);
        console.log('[=] RESULT: *** LOGIN SUCCESS via WebSocket bridge + shared codec *** — G1 PASS');
        clearTimeout(timeout); ws.close(); process.exit(0);
      } else if (r.opcode === OpCode.ACCOUNT_LOGIN_CLIENT) {
        console.error('[!] login rejected by server (framing OK)'); clearTimeout(timeout); process.exit(2);
      }
    }
  });

  ws.on('error', (err) => { console.error('[!] WS error:', err.message); process.exit(1); });
}
main();
