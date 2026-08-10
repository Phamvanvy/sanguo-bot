package peony.game.chat;

import peony.channel.Channel;
import peony.channel.ChannelFilter;
import peony.game.Client;
import peony.game.Player;
import peony.net.ClientSession;
import peony.net.Packet;

public class WorldShoutFilter implements ChannelFilter {

	public void filter(ClientSession session, Object object, Channel channel) {
		Client client = session.getClient();
		if(client != null && client instanceof Player){
			if(((Player)client).level<20)
				return;
			session.send((Packet)object);
		}
	}

}
