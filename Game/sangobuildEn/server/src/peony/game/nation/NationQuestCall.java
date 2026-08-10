package peony.game.nation;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

public class NationQuestCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int id;

	public NationQuestCall(ClientSession session, Packet pt) {
		super(session);
		this.serial = pt.getInt();
		this.id = pt.getInt();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			NationService service = Server.server.getServiceRegistry()
					.getNationService();
			Nation nation = service.getNationByFaction(p.faction);
			if (service.isKing(p)) {
				synchronized (nation) {
					int[] ids = NationService.getNationQuestsId(p.faction);
					boolean contains = false;
					for(int id:ids){
						if(id==this.id){
							contains = true;
							break;
						}
					}
					if(!contains){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATION_QUEST_CLIENT,
								"指定任務錯誤");
						return;
					}
					int day = nation.pool.getInt(Nation.PROPERTY_FACTION_QUEST+this.id);
					if (day != Time.day) {
						if (nation.money < Nation.QUEST_MIN_MONEY) {
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.NATION_QUEST_CLIENT,
									"國家任務最少需要100000的國庫資金");
						} else {
							int v = 100000;
							if (nation.money >= Nation.QUEST_MONEY) {
								v = (int) (nation.money * 0.05f);
							}
							PlayerTransaction tx = p.newTransaction("NQE");
							if (p.bag.removeGameItem(ItemUtil.ITEM_NATION_QUEST,
											-1, 1, tx, true) != null) {
								nation.decMoney(v);
								nation.pool
										.setInt(Nation.PROPERTY_FACTION_QUEST+this.id,
												Time.day);
								tx.commit();
								Packet pt = new Packet(
										OpCode.NATION_QUEST_SERVER);
								pt.putInt(serial);
								pt.putInt(v);
								p.send(pt);
								ASMQuest quest = ASMQuestUtil.getQuest(this.id);
								Server.server
										.getServiceRegistry()
										.getChatService()
										.sendFactionSystemMessage(p.faction,
												MessageFormat.format("好消息,國公發布丰厚獎勵的{0}任務,大家可以去大司農處領取", quest.getGameQuest().getName()));
							} else {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial,
										OpCode.NATION_QUEST_CLIENT,
										"需要國家任務令才能發布國家任務");
							}
						}
					} else {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATION_QUEST_CLIENT,
								"今天已經發布了國家任務,不能再發布國家任務");
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_QUEST_CLIENT, "您不是國公,不能發布國家任務");
			}
		}
	}

}
