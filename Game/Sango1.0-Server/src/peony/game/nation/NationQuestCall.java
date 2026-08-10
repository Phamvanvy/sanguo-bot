package peony.game.nation;

import java.text.MessageFormat;
import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

public class NationQuestCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int id;
	public static int REEARD_ITEM = 2416;

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
			if (nation.getOfficerByPlayerId(p.id)!= null) {
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
								peony.Messages.STRING_00468);
						return;
					}
					int day = nation.pool.getInt(Nation.PROPERTY_FACTION_QUEST+this.id);
					if (day != Time.day) {
						if (nation.money < Nation.QUEST_MIN_MONEY) {
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.NATION_QUEST_CLIENT,
									peony.Messages.STRING_01724);
						} else {
							int v = 1000000;//固定1百万
//							if (nation.money >= Nation.QUEST_MONEY) {
//								v = (int) (nation.money * 0.05f);
//							}
							PlayerTransaction tx = p.newTransaction("NQE");
							if (p.bag.removeGameItem(ItemUtil.ITEM_NATION_QUEST,
											-1, 1, tx, true) != null) {
								nation.decMoney(v);
								nation.pool
										.setInt(Nation.PROPERTY_FACTION_QUEST+this.id,
												Time.day);
								tx.commit();
								
								
								//任务发布成功 获得双倍战功道具
								MailService ms = Server.server.getServiceRegistry().getMailService();
								String itemName = ObjectAccessor.getItemTemplate(REEARD_ITEM).name;
								GameItem gameItem = ObjectAccessor.createGameItem(2416);
								ms.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01725, MessageFormat.format(peony.Messages.STRING_01726, itemName)
										, 0, gameItem, 1, "KING");
								
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
												MessageFormat.format(peony.Messages.STRING_01727, 
														nation.getOfficerByPlayerId(p.id).getName(), quest.getGameQuest().getName()));
								
								//统计玩家成功发布一次国家任务成就
								try{
									StatService statService = Server.server.getServiceRegistry().getStatService();
									PvpInfo pvpInfo = statService.getPvpInfo(p.id, p.faction);
									Achievement a = statService.getAchievementById(112);
									if(a!=null){
									    int type = Integer.parseInt(a.param1);
									    if(type == 2){
											if(pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_NATIONQUEST) == ""){
												pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_NATIONQUEST, statService.getFinishTime(System.currentTimeMillis()));
										        statService.setMessage(p, a, false,true);
											}
									    }
									}
								}catch(Exception e){
									
								}
							} else {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial,
										OpCode.NATION_QUEST_CLIENT,
										peony.Messages.STRING_01728);
							}
						}
					} else {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATION_QUEST_CLIENT,
								peony.Messages.STRING_01729);
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_QUEST_CLIENT, peony.Messages.STRING_01730);
			}
		}
	}

}
