package peony.service.tong;

import java.text.MessageFormat;
import java.util.List;

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
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
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
			Tong tong = tongService.getPlayerTong(p.id,true);
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
							OpCode.TONG_QUEST_CLIENT, peony.Messages.STRING_00468);
					return;
				}
				int day = tong.pool.getInt(Tong.PROPERTY_TONG_QUEST + this.id);
				if (day != Time.day) {
					if (tong.money < Tong.MIN_QUEST_MONEY) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATION_QUEST_CLIENT, peony.Messages.STRING_00469);
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
								tong.modify = true;
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
																peony.Messages.STRING_00470,
																quest.getGameQuest().getName(),
																(quest.getGameQuest().getLevel() - 6),
																GameObject.getFactionName(p.faction)),
												tong.id);
								
								//统计玩家成功发布一次军团任务成就
								try{
									StatService statService = Server.server.getServiceRegistry().getStatService();
									PvpInfo pvpInfo = statService.getPvpInfo(p.id, p.faction);
									Achievement a = statService.getAchievementById(111);
									if(a!=null){
									    int type = Integer.parseInt(a.param1);
									    if(type == 3){
											if(pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_TONGQUEST).equals("")){
												pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_TONGQUEST, statService.getFinishTime(System.currentTimeMillis()));
												statService.setMessage(p, a, false,true);
											}
									    }
									}
								}catch(Exception e){
									
								}
								
							} catch (NoEnoughValueException e) {
								ErrorHandler.sendErrorMessage(session, serial,
										OpCode.NATION_QUEST_CLIENT,
										peony.Messages.STRING_00471);
							}
						} else {
							tx.rollback();
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.NATION_QUEST_CLIENT, peony.Messages.STRING_00472);
						}
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.NATION_QUEST_CLIENT, peony.Messages.STRING_00473);
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_QUEST_CLIENT, peony.Messages.STRING_00474);
			}
		}
	}

}
