package peony.db;

import java.util.ArrayList;
import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.Creature;
import peony.game.ErrorHandler;
import peony.game.GameObject;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.VMap;
import peony.net.ClientSession;
import peony.net.Packet;

public class NpcDescCall extends ClientSessionAsyncCall {

	public int serial;
	public int id;
	
	public NpcDescCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player) session.getClient();
		if(p != null){
			VMap map = p.getVMap();
			if(map != null){
				for(GameObject o:map.instanceid2objects.values()){
					if(o.id==id){
						if(o.type==GameObject.TYPE_CREATURE){
							Creature c = (Creature)o;
							Packet pt = new Packet(OpCode.NPC_DESC_SERVER);
							pt.putInt(serial);
							pt.putInt(id);
							pt.putString(c.searchName==null?"":c.searchName);
							p.send(pt);
							return;
						}
					}
				}
				ErrorHandler.sendErrorMessage(session, serial, OpCode.NPC_DESC_CLIENT, "沒找到指定NPC");
			}
		}
	}

}
