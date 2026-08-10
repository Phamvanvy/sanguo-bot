package peony.game.mail;

import java.text.MessageFormat;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.Actor;
import peony.game.Creature;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ItemUtil;
import peony.game.NPCUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.party.Party;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;

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
					String msg = MessageFormat.format("{0}國王{1}被{2}卑鄙的殺害,舉國陷入悲痛", c.getFactionName(),c.name.substring(0,3),GameObject.getFactionName(actor.faction));
					Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
					//统计个人成就
					try {
						StatService statService = Server.server.getServiceRegistry().getStatService();
						PvpInfo pvpInfo = statService.getPvpInfo(actor.id, actor.faction);
						int index = statService.getIndex(actor.faction, faction);
						String[] achName = {"","奸雄隕落","夢碎白帝","江東無主"};
						if(pvpInfo.pool.getString(statService.getPropertyOfKillKing(index)) == ""){
							pvpInfo.pool.setString(statService.getPropertyOfKillKing(index), statService.getFinishTime(System.currentTimeMillis()));
							String msg2 = MessageFormat.format("恭喜英雄<cff0000>{0}</c>達成<cff0000>{1}</c>成就", actor.name,achName[faction]);
							Server.server.getServiceRegistry().getChatService()
								.sendWorldMessage(msg2);
						}
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
