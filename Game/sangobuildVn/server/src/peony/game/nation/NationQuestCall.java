package peony.game.nation;

import java.text.MessageFormat;
import java.util.List;

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
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
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
								"指定任务错误");
						return;
					}
					int day = nation.pool.getInt(Nation.PROPERTY_FACTION_QUEST+this.id);
					if (day != Time.day) {
						if (nation.money < Nation.QUEST_MIN_MONEY) {
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.NATION_QUEST_CLIENT,
									"国家任务最少需要100000的国库资金");
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
												MessageFormat.format("好消息,国公发布丰厚奖励的{0}任务，大家可以去大司农处领取", quest.getGameQuest().getName()));
								
								//统计玩家成功发布一次国家任务成就
								try{
									StatService statService = Server.server.getServiceRegistry().getStatService();
									PvpInfo pvpInfo = statService.getPvpInfo(p.id, p.faction);
									List<Achievement> aList = statService.getAchievementList(StatService.OTHERTYPE_ONCE_ACHIEVETYPE);
									if(aList!=null){
										for(Achievement a:aList){
										    int type = Integer.parseInt(a.param1);
										    if(type == 2){
												if(pvpInfo.pool.getString(StatService.PROPERTY_FINISHTIME_NATIONQUEST) == ""){
													pvpInfo.pool.setString(StatService.PROPERTY_FINISHTIME_NATIONQUEST, statService.getFinishTime(System.currentTimeMillis()));
											        statService.setMessage(p, a, false);
												}
										    }
										}
									}
								}catch(Exception e){
									
								}
							} else {
								tx.rollback();
								ErrorHandler.sendErrorMessage(session, serial,
										OpCode.NATION_QUEST_CLIENT,
										"需要国家任务令才能发布国家任务");
							}
						}
					} else {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.NATION_QUEST_CLIENT,
								"今天已经发布了国家任务，不能再发布国家任务");
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.NATION_QUEST_CLIENT, "您不是国公，不能发布国家任务");
			}
		}
	}

}
