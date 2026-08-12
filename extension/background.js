const API = "http://127.0.0.1:8765/api";

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
    const options = { ...(message.options || {}) };
    if (message.path === "/run") {
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
