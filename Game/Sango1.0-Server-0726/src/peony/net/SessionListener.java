package peony.net;

public interface SessionListener {
	public void sessionAdded(ClientSession session);
	public void sessionRemoved(ClientSession session);
}
