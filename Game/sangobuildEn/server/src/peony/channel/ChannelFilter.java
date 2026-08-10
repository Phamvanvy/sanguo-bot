package peony.channel;

import peony.net.ClientSession;

public interface ChannelFilter {
	public void filter(ClientSession session,Object object,Channel channel);
}
