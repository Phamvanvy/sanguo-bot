package peony.game.nation;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

public class NationQuestRequestCall extends ClientSessionAsyncCall {

	protected int serial;
	
	
	
	public NationQuestRequestCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
	}
	
	public void callFinish() throws Exception {

	}

	/**
	 * 发布国家任务请求返回
	 * serial							int
	 * 当前国库的金钱					int
	 * 任务数量							short
	 * 循环n次
	 * 	任务ID							int
	 * 	任务名字							String
	 * 	任务状态							(0 没开启 1 开启)
	 */
	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			NationService service = Server.server.getServiceRegistry().getNationService();
			Nation nation = service.getNationByFaction(p.faction);
			int[] ids = NationService.getNationQuestsId(p.faction);
			ASMQuest[] quests = new ASMQuest[ids.length];
			for(int i=0;i<quests.length;i++){
				quests[i] = ASMQuestUtil.getQuest(ids[i]);
			}
			Packet pt = new Packet(OpCode.NATION_QUEST_REQUEST_SERVER);
			pt.putInt(serial);
			pt.putShort(quests.length+1);
			for(ASMQuest quest:quests){
				pt.putInt(quest.getId());
				pt.putString(quest.getGameQuest().getName());
				pt.put(nation.pool.getInt(Nation.PROPERTY_FACTION_QUEST+quest.getId(), 0)==Time.day?1:0);
			}
			pt.putInt(-1);
			pt.putString("國家押運");
			pt.put(Server.server.getServiceRegistry().getNationConvoyService().isConvoying(p.faction)?1:0);
			p.send(pt);
		}
	}

}
