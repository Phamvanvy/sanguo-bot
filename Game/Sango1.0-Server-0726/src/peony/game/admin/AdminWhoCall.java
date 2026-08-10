package peony.game.admin;

import java.util.ArrayList;
import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminWhoCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int mapId;
	
	public AdminWhoCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.mapId = pt.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		List<Player> players = new ArrayList<Player>(ObjectAccessor.players.values());
		List<Player> tmp = new ArrayList<Player>(players.size());
		for (Player p : players) {
			if (p != null)
				if (mapId == -1 || p.map.id == mapId)
					tmp.add(p);
		}
		players = tmp;
		Packet pt = new Packet(OpCode.ADMIN_WHO_SERVER);
		pt.putInt(serial);
		pt.putShort(players.size());
		for(Player p:players){
			pt.putInt(p.id);
			pt.putString(p.name);
			pt.put(p.sex);
			pt.put(p.level);
			pt.put(p.faction <<2 | p.clazz);
			pt.putInt(p.map.id);
			pt.putShort(p.x);
			pt.putShort(p.y);
			pt.putString(p.getGuildName());
		}
		session.send(pt);
	}

}
