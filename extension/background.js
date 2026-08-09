const API = "http://127.0.0.1:8765/api";
const NATIVE_SPEED_FACTOR = 0.7;
let nativeFlow = { state: "idle", message: "Sẵn sàng" };
let nativeToken = null;

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const nativeDelay = (seconds) => delay(Math.max(120, Number(seconds) * 1000 * NATIVE_SPEED_FACTOR));

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.error || "Controller error");
  return data;
}

function debuggerCall(target, method, params = {}) {
  return new Promise((resolve, reject) => {
    chrome.debugger.sendCommand(target, method, params, (result) => {
      const error = chrome.runtime.lastError;
      if (error) reject(new Error(error.message));
      else resolve(result || {});
    });
  });
}

function attachDebugger(target) {
  return new Promise((resolve, reject) => {
    chrome.debugger.attach(target, "1.3", () => {
      const error = chrome.runtime.lastError;
      if (error) reject(new Error(`${error.message}. Hãy đóng DevTools của tab game rồi thử lại.`));
      else resolve();
    });
  });
}

function detachDebugger(target) {
  return new Promise((resolve) => chrome.debugger.detach(target, resolve));
}

function ensureActive(token) {
  if (token.cancelled) throw new Error("FLOW_STOPPED");
}

async function viewport(target) {
  const metrics = await debuggerCall(target, "Page.getLayoutMetrics");
  return metrics.cssLayoutViewport || metrics.layoutViewport;
}

async function clickAt(target, token, point) {
  ensureActive(token);
  const view = await viewport(target);
  const x = Number(point[0]) * view.clientWidth;
  const y = Number(point[1]) * view.clientHeight;
  await debuggerCall(target, "Input.dispatchMouseEvent", { type: "mouseMoved", x, y });
  await debuggerCall(target, "Input.dispatchMouseEvent", { type: "mousePressed", x, y, button: "left", clickCount: 1 });
  await debuggerCall(target, "Input.dispatchMouseEvent", { type: "mouseReleased", x, y, button: "left", clickCount: 1 });
}

async function pressKey(target, key, code, virtualKeyCode, modifiers = 0) {
  const params = { key, code, windowsVirtualKeyCode: virtualKeyCode, nativeVirtualKeyCode: virtualKeyCode, modifiers };
  await debuggerCall(target, "Input.dispatchKeyEvent", { ...params, type: "keyDown" });
  await debuggerCall(target, "Input.dispatchKeyEvent", { ...params, type: "keyUp" });
}

async function clearAndType(target, token, text) {
  ensureActive(token);
  await pressKey(target, "a", "KeyA", 65, 2);
  await pressKey(target, "Backspace", "Backspace", 8);
  await debuggerCall(target, "Input.insertText", { text });
}

function expandCodes(macro) {
  const codes = (macro.codes || []).map(String).filter(Boolean);
  for (const range of macro.code_ranges || []) {
    for (let number = Number(range.start || 1); number <= Number(range.end || 0); number += 1) {
      codes.push(`${range.prefix || ""}${number}`);
    }
  }
  return codes;
}

async function runBlessing(target, token, macro) {
  const openPoint = macro.open_point || [0.73, 0.07];
  const tenPoint = macro.ten_times_point || [0.66, 0.84];
  const okPoint = macro.ok_point || [0.70, 0.64];
  const maxCycles = Number(macro.max_cycles || 0);
  await clickAt(target, token, openPoint);
  await nativeDelay(macro.open_delay_seconds || 1.5);
  for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
    for (let click = 0; click < (cycle === 0 ? 1 : 2); click += 1) {
      await clickAt(target, token, tenPoint);
      await nativeDelay(macro.click_delay_seconds || 0.55);
    }
    await nativeDelay(macro.confirm_delay_seconds || 0.8);
    await clickAt(target, token, okPoint);
    nativeFlow = { state: "running", flow: "blessing", message: `Cầu phúc: ${cycle + 1} lượt` };
    await nativeDelay(macro.result_delay_seconds || 1.5);
  }
}

async function runCodeRedeem(target, token, macro) {
  const codes = expandCodes(macro);
  for (let index = 0; index < codes.length; index += 1) {
    ensureActive(token);
    await clickAt(target, token, macro.npc_point || [0.29, 0.39]);
    await nativeDelay(macro.open_delay_seconds || 1);
    await clickAt(target, token, macro.option_point || [0.50, 0.43]);
    await nativeDelay(macro.option_delay_seconds || 1);
    await clickAt(target, token, macro.input_point || [0.50, 0.51]);
    await clearAndType(target, token, codes[index]);
    await clickAt(target, token, macro.submit_point || [0.50, 0.57]);
    await nativeDelay(macro.submit_delay_seconds || 1.4);
    await pressKey(target, "Enter", "Enter", 13);
    await nativeDelay(macro.dismiss_delay_seconds || 0.8);
    await pressKey(target, "Escape", "Escape", 27);
    await nativeDelay(macro.dismiss_delay_seconds || 0.8);
    nativeFlow = {
      state: "running",
      flow: "code_redeem",
      message: `Đổi code ${index + 1}/${codes.length}: ${codes[index]}`,
    };
  }
}

async function startNativeFlow(tabId, flow, macro) {
  if (nativeToken && !nativeToken.cancelled) throw new Error("Một flow không-chuột khác đang chạy");
  const controller = await apiRequest("/status");
  if (controller.state === "running") throw new Error("Một flow Python khác đang chạy");
  const target = { tabId };
  const token = { cancelled: false };
  nativeToken = token;
  nativeFlow = { state: "running", flow, message: `Đang chạy: ${flow}` };
  try {
    await attachDebugger(target);
  } catch (error) {
    token.cancelled = true;
    nativeToken = null;
    nativeFlow = { state: "error", flow, message: error.message };
    throw error;
  }
  void (async () => {
    try {
      if (flow === "blessing") await runBlessing(target, token, macro);
      else if (flow === "code_redeem") await runCodeRedeem(target, token, macro);
      else throw new Error(`Flow không-chuột chưa hỗ trợ: ${flow}`);
      nativeFlow = { state: "done", flow, message: "Flow hoàn tất" };
    } catch (error) {
      nativeFlow = token.cancelled || error.message === "FLOW_STOPPED"
        ? { state: "stopped", flow, message: "Đã dừng flow" }
        : { state: "error", flow, message: error.message };
    } finally {
      token.cancelled = true;
      if (nativeToken === token) nativeToken = null;
      await detachDebugger(target);
    }
  })();
  return { ok: true, ...nativeFlow };
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  void (async () => {
    if (message.type === "api") {
      const options = { ...(message.options || {}) };
      if (message.path === "/run") {
        nativeFlow = { state: "idle", message: "Sẵn sàng" };
        const windowInfo = await chrome.windows.get(sender.tab.windowId);
        const body = JSON.parse(options.body || "{}");
        options.body = JSON.stringify({
          ...body,
          tabTitle: sender.tab.title,
          tabUrl: sender.tab.url,
          fullscreen: Boolean(message.fullscreen || windowInfo.state === "fullscreen"),
        });
      }
      return apiRequest(message.path, options);
    }
    if (message.type === "native-status") return nativeFlow;
    if (message.type === "run-native") return startNativeFlow(sender.tab.id, message.flow, message.macro);
    if (message.type === "stop-native") {
      if (nativeToken) nativeToken.cancelled = true;
      return { ok: true, state: "stopping", message: "Đang dừng flow" };
    }
    throw new Error("Unknown extension message");
  })().then(sendResponse, (error) => sendResponse({ error: error.message }));
  return true;
});

chrome.action.onClicked.addListener(async (tab) => {
  if (!tab.id || !tab.url?.startsWith("https://play.minhchauh5.com/")) return;
  try {
    await chrome.tabs.sendMessage(tab.id, { type: "toggle-panel" });
  } catch (_) {
    await chrome.scripting.insertCSS({ target: { tabId: tab.id }, files: ["content.css"] });
    await chrome.scripting.executeScript({ target: { tabId: tab.id }, files: ["content.js"] });
  }
});
