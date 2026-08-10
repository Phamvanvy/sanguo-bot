package peony.net;

public interface PacketHandler {
	public void handle(Packet packet,ClientSession session,int diff) throws Exception;
	public void disconnected(ClientSession session);
}
