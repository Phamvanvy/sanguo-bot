package peony.game.mail;

import java.text.MessageFormat;
import java.util.List;
import org.apache.log4j.Logger;
import peony.game.Actor;
import peony.game.ChatOption;
import peony.game.Creature;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.NPCUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.chat.ChatMessage;
import peony.game.party.Party;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import peony.service.weibo.WeiboService;

public class KillKingService implements Service,ServiceEventListener {
	
	protected static final Logger log = Logger.getLogger(KillKingService.class);
	


	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_UNIT_DIE,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit)event.param1,(Unit)event.param2);
			break;
		}
	}
	
	protected void unitDie(Unit u1, Unit u2){
		if(u1.type==GameObject.TYPE_CREATURE){
			int faction = NPCUtil.getKingFaction(u1);
			if(faction!=0){
				Creature c = (Creature)u1;
				Object o = c.battleContribList.getOwner();
				GameObjectRef ref = null;
				if(o instanceof Party){
					ref = ((Party)o).leader.player.ref();
				}else{
					ref = (GameObjectRef)o;
				}
				if(ref.type==GameObject.TYPE_PLAYER){
					Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(ref.id);
					log.info("[KINGKILLED]FACTION["+faction+"]SOURCEFACTION["+actor.faction+"]SOURCE["+actor.id+"]");
					String kingName = c.name.substring(0, 3);
					if(kingName.contains("曹操")){
						kingName = kingName.replace("操", "孟德");
					}
					String msg = MessageFormat.format(peony.Messages.STRING_01744,kingName,GameObject.getFactionName(actor.faction),c.getFactionName());
					Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
					//统计个人成就
					try {
						StatService statService = Server.server.getServiceRegistry().getStatService();
						Player p=ObjectAccessor.getPlayer(ref.id);
						if(p!=null){
							if(p.party!=null&&p.party.members!=null&&p.party.members.size()>0){
							   for(int j=0;j<p.party.members.size();j++){
								   Player tempPlayer=p.party.members.get(j).player;
								   if(tempPlayer!=null&&tempPlayer.inRange(p, 320)){
									   PvpInfo pvpInfo = statService.getPvpInfo(tempPlayer.id, tempPlayer.faction);
										int index = faction-1;//statService.getIndex(actor.faction, faction);
										Achievement a = statService.getAchievementById(statService.factionIndex[faction-1]);
										if(a!=null){
											if(pvpInfo.pool.getString(statService.getPropertyOfKillKing(index)).equals("")){
												pvpInfo.pool.setString(statService.getPropertyOfKillKing(index), statService.getFinishTime(System.currentTimeMillis()));
												String msg2 = MessageFormat.format(peony.Messages.STRING_00851, tempPlayer.name,a.achievementName);
												ChatMessage cm2 = new ChatMessage(ChatOption.WORLD, tempPlayer.id, -1,peony.Messages.STRING_00004+"(成就)", msg2+"#"+a.dec, null);
												Server.server.getServiceRegistry().getChatService().addChatMessage(cm2);
											}
										}
								   }
							   }
							}else{
								PvpInfo pvpInfo = statService.getPvpInfo(actor.id, actor.faction);
								int index = faction-1;//statService.getIndex(actor.faction, faction);
								Achievement a = statService.getAchievementById(statService.factionIndex[faction-1]);
								if(a!=null){
									if(pvpInfo.pool.getString(statService.getPropertyOfKillKing(index)).equals("")){
										pvpInfo.pool.setString(statService.getPropertyOfKillKing(index), statService.getFinishTime(System.currentTimeMillis()));
										String msg2 = MessageFormat.format(peony.Messages.STRING_00851, actor.name,a.achievementName);
										ChatMessage cm2 = new ChatMessage(ChatOption.WORLD, actor.id, -1,peony.Messages.STRING_00004+"(成就)", msg2+"#"+a.dec, null);
										Server.server.getServiceRegistry().getChatService().addChatMessage(cm2);
									}
								}
							}
						}
						
						
//						Achievement a = statService.getAchievement(StatService.KILL_KINT_ACHIEVETYPE, faction-1);
////						String[] achName = {"","奸雄陨落","梦碎白帝","江东无主"};
//						if(pvpInfo.pool.getString(statService.getPropertyOfKillKing(index)).equals("")){
//							pvpInfo.pool.setString(statService.getPropertyOfKillKing(index), statService.getFinishTime(System.currentTimeMillis()));
//							String msg2 = MessageFormat.format(peony.Messages.STRING_00851, actor.name,a.achievementName);
//							ChatMessage cm2 = new ChatMessage(ChatOption.WORLD, actor.id, -1,peony.Messages.STRING_00004+"(成就)", msg2+"#"+a.dec, null);
//							Server.server.getServiceRegistry().getChatService().addChatMessage(cm2);
//							LogUtil.logFinishAchievement(actor.id, a.achievementName);
							//完成成就触发发送微博
//								Player p = ObjectAccessor.getPlayer(actor.id);
//								if(p!=null){
//									if(p.pool.getInt(Player.PROPERTY_WEIBO_ACTIVE, 0)==0 || (p.pool.getInt(Player.PROPERTY_WEIBO_ACTIVE, 0)==1 && p.pool.getString(Player.PROPERTY_WEIBO_TOKEN)!="")){
//										WeiboService weiboService = Server.server.getServiceRegistry().getWeiboService();
//										String weiboMsg = MessageFormat.format(peony.Messages.STRING_00852,
//												weiboService.getDitrict(),p.name, a.dec,a.achievementName);
//										if(p.pool.getInt(Player.PROPERTY_WEIBO_ACTIVE, 0)==0){
//											   p.pool.setInt(Player.PROPERTY_WEIBO_ACTIVE, 1);
//										}
////										weiboService.showWeiboUI(p, weiboMsg);
//									}
//								}
//						}
					} catch (Exception e){
						log.error(e, e);
					}
					List<Player> l = c.getVMap().getPlayersByFaction(actor.faction);
					for(Player player:l){
						PlayerTransaction tx = player.newTransaction("KKG");
						player.addCredit(100, tx, true);
						player.bag.addGameItem(ObjectAccessor.createGameItem(ItemUtil.ITEM_KILLKING), 1, tx, true);
						player.addKillCreatureCount(c.template.getID(), l);
						tx.commit();
//						log.info("[KINGKILLED]"+LogUtil.getPlayerLogString(player));
					}
				}else{
					log.info("[KINGKILLEDERROR]SOURCE["+u2.id+"]");
				}
			}
		}
	}
}
