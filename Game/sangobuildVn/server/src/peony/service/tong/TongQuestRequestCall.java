package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

public class TongQuestRequestCall extends ClientSessionAsyncCall {

	protected int serial;
	
	public TongQuestRequestCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id);
			if(tong==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_QUEST_REQUEST_CLIENT, "尚未加入军团");
				return;
			}
			int[] questIds = TongService.TONG_QUESTS_ID[p.faction];
			ASMQuest[] quests = new ASMQuest[questIds.length];
			for(int i=0;i<questIds.length;i++){
				quests[i] = ASMQuestUtil.getQuest(questIds[i]);
			}
			Packet pt = new Packet(OpCode.TONG_QUEST_REQUEST_SERVER);
			pt.putInt(serial);
			pt.putInt(tong.money);
			pt.putShort(quests.length);
			for(ASMQuest quest:quests){
				pt.putInt(quest.getId());
				pt.putString(quest.getGameQuest().getName());
				pt.put(tong.pool.getInt(Tong.PROPERTY_TONG_QUEST+quest.getId(), 0)==Time.day?1:0);
			}
			session.send(pt);
			addToClientSession();
		}
	}

}
