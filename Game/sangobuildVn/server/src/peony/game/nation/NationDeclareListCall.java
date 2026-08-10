package peony.game.nation;

import java.util.Date;

import peony.common.ClientSessionAsyncCall;
import peony.game.CommonUtil;
import peony.game.GameObject;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class NationDeclareListCall extends ClientSessionAsyncCall{

	int serial;
	
	public NationDeclareListCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
	}
	
	public void callFinish() throws Exception {
		// TODO Auto-generated method stub

	}

	public void run() {
		Player player = (Player) session.getClient();
		if (player != null) {
			NationService service = Server.server.getServiceRegistry()
					.getNationService();
			Nation wei = service.getNationByFaction(GameObject.FACTION_WEI);
			Nation shu = service.getNationByFaction(GameObject.FACTION_SHU);
			Nation wu = service.getNationByFaction(GameObject.FACTION_WU);
			Packet pt = new Packet(OpCode.NATION_DECLARE_LIST_SERVER);
			pt.putInt(serial);
			pt.putShort(6);
			for (int i = 1; i < 4; i++) {
				for (int j = 1; j < 4; j++) {
					NationRel rel = service.getRel(i, j);
					if (rel != null) {
						pt.putInt(rel.sourceFaction);
						pt.putInt(rel.destFaction);
						int type = rel.type;
						Date time = rel.endTime;
						if(rel.type == NationRel.TYPE_WIN){
							NationSneakBattleFieldInstance inst = service.getSneakInstance(rel.sourceFaction, rel.destFaction);
							if(inst != null){
								type = NationRel.TYPE_SNEAKED;
								time = inst.endTime;
							}
						} else if(rel.type == NationRel.TYPE_FAIL){
							NationSneakBattleFieldInstance inst = service.getSneakInstance(rel.destFaction, rel.sourceFaction);
							if(inst != null){
								type = NationRel.TYPE_SNEAK;
								time = inst.endTime;
							}
						}
						pt.put(type);
						pt.putString(CommonUtil.getDateString(time));
					}
				}
			}
			pt.putString(CommonUtil.getDateString(wei.guardTime));
			pt.putString(CommonUtil.getDateString(shu.guardTime));
			pt.putString(CommonUtil.getDateString(wu.guardTime));
			player.send(pt);
		}
	}

}
