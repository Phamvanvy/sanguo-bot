package peony.teleport.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.Creature;
import peony.game.DataService;
import peony.game.ErrorHandler;
import peony.game.GameObject;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.account.Account;
import peony.service.account.AccountService;
import peony.service.activity.CleanBossActivity;
import peony.service.activity.OppositeSexQuestActivity;
import peony.service.pluginstance.LanternFestivalService;
import peony.service.pluginstance.MayDayFestivalService;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.TeleportSet;
import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.TeleportSet.Teleport;
import com.pip.sanguo.data.map.GameMapExit;

public class TeleportService implements Service {
	
	private static Logger log = Logger.getLogger(TeleportService.class);
	public ProjectData projectData = Server.server.getServiceRegistry().getDataService().data;
	public static int[] RECALLMAPID = {1169, 1121, 1122, 1123}; //具有哪进哪出传送点的地图
	public static Class[] SPECIALMAPMANAGER = {CleanBossActivity.class, OppositeSexQuestActivity.class};

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		
	}

	public void getTeleportList(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int teleportSetId = packet.getInt();
		TeleportSet teleportSet = (TeleportSet) projectData.findObject(TeleportSet.class, teleportSetId);
		Player p = (Player) session.getClient();
		if(teleportSet != null){
			if(p != null){
				Packet pt = new Packet(OpCode.TELEPORT_LIST_SERVER);
				pt.putInt(serial);
				List<Teleport> list = new ArrayList<Teleport>();
				list = teleportSet.items;
				pt.putInt(list.size());
				if(list != null){
					for(Teleport teleport : list){
						pt.putInt(teleport.mapID);
						pt.putInt(teleport.x);
						pt.putInt(teleport.y);
						pt.putString(teleport.name);
						StringBuffer strb = new StringBuffer();
						strb.append("(");
						for(BuyRequirement buyRequirement : teleport.requirements){
//							if(buyRequirement.type != Shop.TYPE_RANK && buyRequirement.type != Shop.TYPE_MONEY 
//									&& buyRequirement.toString() != null){
//								strb.append(buyRequirement.toString()+"、");
//							}else 
							if(buyRequirement.type == Shop.TYPE_MONEY && buyRequirement.toString() != null){
								strb.append(MessageFormat.format(peony.Messages.STRING_01152, buyRequirement.amount));
							}else if(buyRequirement.type == Shop.TYPE_RANK){
								DataService ds = Server.server.getServiceRegistry().getDataService();
								Rank rank = (Rank)ds.data.findDictObject(Rank.class, buyRequirement.amount);
								strb.append(MessageFormat.format(peony.Messages.STRING_01153, rank.title));
							}else if(buyRequirement.type == Shop.TYPE_LEVEL){
								strb.append(MessageFormat.format(peony.Messages.STRING_01154, buyRequirement.amount));
							}else if(buyRequirement.type == Shop.TYPE_HONOR){
								strb.append(MessageFormat.format(peony.Messages.STRING_01155, buyRequirement.amount));
							}else if(buyRequirement.type == Shop.TYPE_IMONEY){
								strb.append(MessageFormat.format(peony.Messages.STRING_01156, buyRequirement.amount));
							}else if(buyRequirement.type == Shop.TYPE_ITEM){
								strb.append(MessageFormat.format(peony.Messages.STRING_01157, buyRequirement.amount));
							}
						}
						String require = strb.substring(0, strb.length()-1);
						if(require.equals("")){
							pt.putString("");
						}else{
							pt.putString(require+")");
						}
					}
				}
				p.send(pt);
			}
		}
	}

	public void teleport(Packet packet, ClientSession session) {
		int serial = packet.getInt();
		int npcId = packet.getInt();
		int teleportId = packet.getInt();
//		int mapId = packet.getInt();
		int mapId = -1;
		int mapIndex = packet.getInt();
		Player p = (Player) session.getClient();
		AccountService as = Server.server.getServiceRegistry().getAccountService();
		Account account = as.getAccount(p.accountId);
		if(p != null){
			GameObject npc = p.getVMap().instanceid2objects.get(npcId);
			if(npc != null && npc.type == GameObject.TYPE_CREATURE ){
				if(((Creature)npc).touchAction==null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT,peony.Messages.STRING_01158);
					return;
				}
				log.info("[TELEPORT]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]TRY");
				int distance = npc.distance(p);
				int nearestDist = 50 * 50;
				if(distance < nearestDist){
					int x = 0;
					int y = 0;
					TeleportSet teleportSet = (TeleportSet) projectData.findObject(TeleportSet.class, teleportId);
					if (teleportSet == null) {
						return;
					}
					List<BuyRequirement> requirements = null;
					PlayerTransaction tx = p.newTransaction("TEL");
//					for(Teleport teleport : teleportSet.items){
//						if(teleport.mapID == mapId){
//							x = teleport.x;
//							y = teleport.y;
//							requirements = teleport.requirements;
//						}
//					}
					if(teleportSet.items.get(mapIndex) != null){
						mapId = teleportSet.items.get(mapIndex).mapID;
						requirements = teleportSet.items.get(mapIndex).requirements;
						x = teleportSet.items.get(mapIndex).x;
						y = teleportSet.items.get(mapIndex).y;
					}
					if(requirements != null){
						for(BuyRequirement buyRequirement : requirements){
							int amount = buyRequirement.amount;
							if (buyRequirement.type == Shop.TYPE_MONEY && buyRequirement.deduct) {
								try {
									p.decMoney(amount, tx, false);
									log.info("[TELEPORT]"+LogUtil.getPlayerLogString(p)+"DECMONEY["+amount+"]");
								} catch (NoEnoughValueException e) {
									tx.rollback();
									log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
									ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT,peony.Messages.STRING_00129);
									return;
								}
							} else if (buyRequirement.type == Shop.TYPE_HONOR && buyRequirement.deduct) {
								try {
									p.decCredit(amount, tx, false);
								} catch (NoEnoughValueException e) {
									tx.rollback();
									log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
									ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT,peony.Messages.STRING_01159);
									return;
								}
							} else if(buyRequirement.type == Shop.TYPE_IMONEY){
								if(amount > account.getLongIMoney() / 100){
									tx.rollback();
									log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
									ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, peony.Messages.STRING_01160);
									return;
								}else{
									
								}
							} else if(buyRequirement.type == Shop.TYPE_RANK){
								DataService ds = Server.server.getServiceRegistry().getDataService();
								Rank rank = (Rank)ds.data.findDictObject(Rank.class, amount);
								if (rank == null) {
									ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, peony.Messages.STRING_01161);
									tx.rollback();
									log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
									return;
								}else{
									if(p.getRank() < rank.id){
										tx.rollback();
										log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
										ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, 
												MessageFormat.format(peony.Messages.STRING_01162, rank.title));
										return;
									}
								}
							} else if (buyRequirement.type == Shop.TYPE_ITEM && buyRequirement.deduct) {
								if (buyRequirement.item != null) {
									int itemId0 = buyRequirement.item.id;
									if (p.bag.getGameItemCount(itemId0) < amount) {
										ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, 
												MessageFormat.format(peony.Messages.STRING_01163, buyRequirement.item.title));
										tx.rollback();
										log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
										return;
									} else {
										p.bag.removeGameItem(itemId0, p.bag.getGameItem(itemId0).instanceId, amount,tx, false);
										log.info("[TELEPORT]"+LogUtil.getPlayerLogString(p)+"DECITEM["
												+LogUtil.getGameItemString(p.bag.getGameItem(itemId0), amount)+"]");
									}
								}
							} else if(buyRequirement.type == Shop.TYPE_VARIABLE){
								int value = p.asmVm.getGlobalValue(buyRequirement.varName);
							    if (value < buyRequirement.amount) {
							    	tx.rollback();
							    	log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
							    	ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, 
							    			MessageFormat.format(peony.Messages.STRING_01164, buyRequirement.varDesc));
							    	return;
							    }
							} else if(buyRequirement.type == Shop.TYPE_LEVEL){
								int level = buyRequirement.amount;
								if(p.level < level){
									tx.rollback();
									log.info("[TELEPORTFAILED]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
									ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, 
							    			peony.Messages.STRING_01165);
							    	return;
								}
							}
						}
					}
					try {
						int oldMapId = p.map.getId();
						int oldX = p.x;
						int oldY = p.y;
						p.goMap(mapId, x, y);
						// 特殊处理
						VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
						if(isSpecialVMapManager(manager.getClass())){
							if(manager.getClass().equals(CleanBossActivity.class)){
								CleanBossActivity act = (CleanBossActivity)manager;
								if(act.getActivity().isActive() && act.isForbid(p, mapId)){
									tx.rollback();
									return;
								}
							}
							if(manager.getClass().equals(OppositeSexQuestActivity.class)){
								OppositeSexQuestActivity act = (OppositeSexQuestActivity)manager;
								if(!act.getActivity().isActive()){
									tx.rollback();
									return;
								}
							}
						}
						//元宵活动初始化
						LanternFestivalService lService = Server.server.getServiceRegistry().getLanternFestivalService();
						lService.initState(p);
						//五一活动初始化
						MayDayFestivalService mService = Server.server.getServiceRegistry().getMayDayFestivalService();
						mService.initState(p);
						tx.commit();
						if(isRecallMap(mapId)){
							for(GameMapExit exit : VMapUtil.getExits(mapId)){
								if(exit.exitType == GameMapExit.TYPE_RECALL){
									p.pool.setString(exit.positionVarName, oldMapId+","+oldX+","+oldY);
								}
							}
						}
						log.info("[TELEPORTSUCCESS]"+LogUtil.getPlayerLogString(p)+"BALANCE["+p.money+"]");
					} catch (VMapException e) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, e.getMessage());
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TELEPORT_CLIENT, peony.Messages.STRING_01166);
				}
			}
		}
	}
	
	private boolean isRecallMap(int mapId){
		for(int id : RECALLMAPID){
			if(id==mapId){
				return true;
			}
		}
		return false;
	}
	
	private boolean isSpecialVMapManager(Class cla){
		for(Class c : SPECIALMAPMANAGER){
			if(c.equals(cla))
				return true;
		}
		return false;
	}

}
