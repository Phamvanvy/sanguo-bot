package peony.service.tong;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameObject;
import peony.game.ItemUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

public class TongQuestCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int id;

	public TongQuestCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			TongService tongService = Server.server.getServiceRegistry()
					.getTongService();
			Tong tong = tongService.getPlayerTong(p.id);
			if (tong.getChairmanName().equals(p.name)) {
				int[] ids = TongService.getTongQuestIds(p.faction);
				boolean contains = false;
				for (int id : ids) {
					if (id == this.id) {
						contains = true;
						break;
					}
				}
				if (!contains) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.TONG_QUEST_CLIENT, "指定任務錯誤");
					return;
				}
				int day = tong.pool.getInt(Tong.PROPERTY_TONG_QUEST + this.id);
				if (day != Time.day) {
					if (tong.money < Tong.MIN_QUEST_MONEY) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATION_QUEST_CLIENT, "發布失敗,當前軍團資金不足");
					} else {
						int v = 50000;
						if (tong.money >= Tong.QUEST_MONEY) {
							v = (int) (tong.money * 0.05f);
						}
						PlayerTransaction tx = p.newTransaction("TQE");
						if (p.bag.removeGameItem(ItemUtil.ITEM_TONG_QUEST, -1,
								1, tx, true) != null) {
							try {
								tong.decMoney(v);
								tong.pool.setInt(Tong.PROPERTY_TONG_QUEST
										+ this.id, Time.day);
								tx.commit();
								Packet pt = new Packet(OpCode.TONG_QUEST_SERVER);
								pt.putInt(serial);
								pt.putInt(v);
								p.send(pt);
								ASMQuest quest = ASMQuestUtil.getQuest(this.id);
								Server.server
										.getServiceRegistry()
										.getChatService()
										.sendGuildSystemMessage(
												MessageFormat
														.format(
																"好消息,都督發布丰厚獎勵的{0}任務,{1}級可接,大家可以去西域{2}校尉領取",
																quest.getGameQuest().getName(),
																(quest.getGameQuest().getLevel() - 6),
																GameObject.getFactionName(p.faction)),
												tong.id);
							} catch (NoEnoughValueException e) {
								ErrorHandler.sendErrorMessage(session, serial,
										OpCode.NATION_QUEST_CLIENT,
										"發布失敗,當前軍團金庫金額不足");
							}
						} else {
							tx.rollback();
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.NATION_QUEST_CLIENT, "發布失敗,沒有軍團任務令");
						}
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_QUEST_CLIENT, "你已發布過該任務,每日只可發布一次");
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_QUEST_CLIENT, "您不是都督,不能發布軍團任務");
			}
		}
	}

}
