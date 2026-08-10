package peony.game.pk;

import java.util.Iterator;

import org.apache.log4j.Logger;

import peony.game.ChatOption;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.friend.PlayerRelation;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;
import ch.javasoft.util.intcoll.IntHashMap;

public class PkService implements Service,ServiceEventListener{
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(PkService.class);
	
	protected int lastUpdateTime = 0;
	
	public int[] getEventTypes() {
		return new int[] {
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_PLAYER_PK_MOVE,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_PLAYER_PK_ATTACKED,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			checkPkEnd(event);
			break;
		case ServiceEvent.EVENT_PLAYER_PK_MOVE:
			pkMove((Player)event.param1,(Integer)event.param2,(Integer)event.param3);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			logout((Player)event.param1);
			break;
		case ServiceEvent.EVENT_PLAYER_PK_ATTACKED:
			pkAttacked((Player)event.param1);
			break;
		case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
			playerRemoveFromMap((Player)event.param2);
			break;
		}
	}
	
	protected void playerRemoveFromMap(Player p){
		if(p.pkInfo!=null&&p.pkInfo.state==PkInfo.STATE_STARTED){
			Player winner = ObjectAccessor.getPlayer(p.pkInfo.getVictimRef(p.id).id);
			PkEnd(p.pkInfo,winner,p,0);
		}
	}
	
	protected void logout(Player player){
		if(player.pkInfo!=null){
			if(player.pkInfo.state==PkInfo.STATE_STARTED){
				Player winner = ObjectAccessor.getPlayer(player.pkInfo.getVictimRef(player.id).id);
				PkEnd(player.pkInfo,winner,player,0);
			}else if(player.pkInfo.state==PkInfo.STATE_INIT){
				player.pkInfo.state = PkInfo.STATE_END;
				player.canPK=0;
				player.pkInfo = null;
			}
		}
	}
	
	/**
	 * 判断玩家的移动是否超出PK范围，如果超出PK范围一定时间，那么将判负
	 */
	protected void pkMove(Player player,int x,int y){
//		System.out.println(String.format("pkmove:%d,%d,%d", player.id,x,y));
		PkInfo pkInfo = player.pkInfo;
		if(pkInfo!=null&&pkInfo.state==PkInfo.STATE_STARTED){
			if(player.id==pkInfo.source.id){
				if(!pkInfo.inRange(player.getVMap(),x, y)){
					if(pkInfo.isSourceTimeOut(Time.currTime)){
						PkEnd(pkInfo,(Player)ObjectAccessor.getGameObject(pkInfo.target),player,0);
					}
				}else{
					pkInfo.sourceLastOutTime = 0;
				}
			}else{
				if(!pkInfo.inRange(player.getVMap(),x, y)){
					if(pkInfo.isTargetTimeOut(Time.currTime)){
						PkEnd(pkInfo,(Player)ObjectAccessor.getGameObject(pkInfo.source),player,0);
					}
				}else{
					pkInfo.targetLastOutTime = 0;
				}
			}

		}
	}
	
	protected void pkAttacked(Player p) {
		PkEnd(p.pkInfo, (Player) ObjectAccessor.getGameObject(p.pkInfo.source),
				(Player) ObjectAccessor.getGameObject(p.pkInfo.target), 1);
	}
	
	
	protected void checkPkEnd(ServiceEvent event){
		Unit u1 = (Unit)event.param1;
		Unit u2 = (Unit)event.param2;
		if(u1.type==GameObject.TYPE_PLAYER&&u2.type==GameObject.TYPE_PLAYER){
			Player p1 = (Player)u1;
			Player p2 = (Player)u2;
			if(p1.pkInfo!=null && p1.pkInfo.state == PkInfo.STATE_STARTED){  
				if(p2!=p1&&p2.pkInfo==p1.pkInfo){  //有可能u1==u2所以要加入一个判定，这是个bug，需要修复
					PkInfo pkInfo = p2.pkInfo;
					PkEnd(pkInfo,p2,p1,0);
					p1.relive(1, p1.mp);
				}else{
					if(p2==p1){
						log.info("[PKERROR]DUP");
						return;
					}else{
						log.info("[PKERROR]INFO");
					}
					GameObjectRef ref = p1.pkInfo.getVictimRef(p1.id);
					p2 = ObjectAccessor.getPlayer(ref.id);
					if(p2!=null){
						PkEnd(p1.pkInfo,p2,p1,0);
						p1.relive(1, p1.mp);
					}else{
						log.info("[PKERROR]");
					}
				}
			}
//			if(p1.pkInfo!=null&&p1.pkInfo==p2.pkInfo){ //如果pkInfo相同，并且不为空说明是在pk状态
//				PkInfo pkInfo = p2.pkInfo;
//				PkEnd(pkInfo,p2,p1,0);
//				p1.relive(1, p1.mp);
//			}
		}
	}
	
	/**
	 * 在result为0时winner,loser代表的是胜利者和失败者，当result为1时winner,loser只是代表pk的两个人，没有输赢之分
	 * @param pkInfo
	 * @param winner
	 * @param loser
	 * @param result
	 */
	protected void PkEnd(PkInfo pkInfo, Player winner, Player loser, int result) {
		pkInfo.state = PkInfo.STATE_END;
		winner.canPK=0;
		loser.canPK=0;
		winner.pkInfo = null;
		loser.pkInfo = null;
		if (result == 0) {
			if (pkInfo.wager > 0) {
				PlayerTransaction tx = winner.newTransaction("PKW");
				winner.addMoney(pkInfo.wager * 2, tx, true);
				tx.commit();
			}
		} else {
			if (pkInfo.wager > 0) {
				PlayerTransaction tx = winner.newTransaction("PKC");
				winner.addMoney(pkInfo.wager, tx, false);
				tx.commit();
				tx = loser.newTransaction("PKC");
				loser.addMoney(pkInfo.wager, tx, false);
				tx.commit();
			}
		}
		winner.buffs.removeDebuffs(loser.ref());
		loser.buffs.removeDebuffs(winner.ref());
		winner.removeThreatUnit(loser.ref(),true);
		Packet pt = new Packet(OpCode.PK_OVER_SERVER);
		pt.put(result); 
		pt.putInt(winner.instanceId);
		winner.send(pt);
		loser.send(pt);
		if (result == 0) {
			ChatService chatService = Server.server.getServiceRegistry()
					.getChatService();
			ChatMessage cm = new ChatMessage(ChatOption.AREA,-1,-1,peony.Messages.STRING_00004,winner.map.id,String.format(
					peony.Messages.STRING_01769, winner.name, loser.name),null);
			chatService.addChatMessage(cm);
			
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PK_END,winner));	
		}
//		if(winner!=null&&loser!=null){
//			Server.server.getEventManager().addEvent(
//					new ServiceEvent(ServiceEvent.EVENT_INTERACT, winner,
//							loser,
//							PlayerRelation.INTERACT_PK));
//		}
	}

	protected IntHashMap<PkInfo> infos  = new IntHashMap<PkInfo>();
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public void addPkInfo(PkInfo pkInfo){
		infos.put(pkInfo.id, pkInfo);
	}
	
	public PkInfo getPkInfo(int id){
		return infos.get(id);
	}
	
	public void update(){
		try {
			if(Time.currTime-lastUpdateTime<1000){
				return;
			}
			lastUpdateTime = Time.currTime;
			Iterator<PkInfo> ite = infos.values().iterator();
			while(ite.hasNext()){
				PkInfo info = ite.next();
				if(info.state==PkInfo.STATE_INIT&&Time.currTime-info.startTime>=PkInfo.INIT_TIME){
					info.state=PkInfo.STATE_END;
					Player p = (Player)ObjectAccessor.getGameObject(info.source);
					Player p2 = (Player)ObjectAccessor.getGameObject(info.target);
					if(p2!=null){
						p2.canPK=0;
					}
					if(p!=null){
						p.canPK=0;
						p.pkInfo = null;
						Packet pt = new Packet(OpCode.PK_REFUSE_SERVER);
						pt.putString(peony.Messages.STRING_01770);
						p.send(pt);
					}
				}
				if(info.state==PkInfo.STATE_END){
					ite.remove();
				}
			}
		} catch (Exception e) {
			log.error(e,e);
		}
	}
}
