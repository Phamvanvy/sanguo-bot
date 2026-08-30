const API = "http://127.0.0.1:8765/api";
const CLIENT_ID_KEY = "sanguo-controller-client-id";

async function clientId() {
  const stored = await chrome.storage.local.get(CLIENT_ID_KEY);
  if (stored[CLIENT_ID_KEY]) return stored[CLIENT_ID_KEY];
  const value = crypto.randomUUID();
  await chrome.storage.local.set({ [CLIENT_ID_KEY]: value });
  return value;
}

async function sessionContext(message, sender) {
  const process = message.browserProcess || "chrome.exe";
  const client = await clientId();
  const windowId = sender.tab.windowId;
  return {
    sessionId: `${process}:${client}:${windowId}`,
    windowToken: `SG-${client.slice(0, 8)}-${windowId}`,
    browserProcess: process,
  };
}

function statusPath(path, sessionId) {
  if (path !== "/status") return path;
  return `${path}?sessionId=${encodeURIComponent(sessionId)}`;
}

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.error || "Controller error");
  return data;
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  void (async () => {
    if (message.type !== "api") throw new Error("Unknown extension message");
    if (!sender.tab) throw new Error("API request is not associated with a browser tab");
    const context = await sessionContext(message, sender);
    const options = { ...(message.options || {}) };
    if (message.path === "/run") {
      const windowInfo = await chrome.windows.get(sender.tab.windowId);
      await chrome.scripting.executeScript({
        target: { tabId: sender.tab.id },
        func: (token) => {
          const clean = document.title.replace(/\s+\[SG-[^\]]+\]$/, "");
          document.title = `${clean} [${token}]`;
        },
        args: [context.windowToken],
      });
      await new Promise((resolve) => setTimeout(resolve, 120));
      const body = JSON.parse(options.body || "{}");
      options.body = JSON.stringify({
        ...body,
        tabTitle: sender.tab.title,
        tabUrl: sender.tab.url,
        fullscreen: Boolean(message.fullscreen || windowInfo.state === "fullscreen"),
        ...context,
      });
    } else if (message.path === "/stop" || message.path === "/network-event") {
      const body = JSON.parse(options.body || "{}");
      options.body = JSON.stringify({ ...body, sessionId: context.sessionId });
    }
    return apiRequest(statusPath(message.path, context.sessionId), options);
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
