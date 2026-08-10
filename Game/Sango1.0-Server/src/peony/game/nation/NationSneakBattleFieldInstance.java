package peony.game.nation;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.chat.ChatService;
import peony.net.Packet;
import peony.service.ServiceEvent;

public class NationSneakBattleFieldInstance implements Instance{
	
	private static final Logger log = Logger.getLogger(NationSneakBattleFieldInstance.class);
	
	public static final int STATE_STARTED = 0;
	public static final int STATE_END = 1;
	
	protected static final AtomicInteger ids = new AtomicInteger(0);
	
	protected Set<GameObjectRef> sourcePlayerRefs = new HashSet<GameObjectRef>(16);
	protected Set<GameObjectRef> destPlayerRefs = new HashSet<GameObjectRef>(16);
	
	public int state;
	public NationSneakBattleFieldDef def;
	protected Date startTime,endTime;
	protected VMap map;
	protected int id;
	
	protected NationService manager;
	
	//testmodify
	protected static int WIN_SCORE = 500;
//	protected static int WIN_SCORE = 10;
	
	protected int score = 0;
	
	protected int money;
	
	protected boolean[] shouted = new boolean[5];
	
	public NationSneakBattleFieldInstance(NationService manager,VMap map,NationSneakBattleFieldDef def,Date startTime,Date endTime,int money){
		this.id = ids.incrementAndGet();
		this.manager = manager;
		this.def = def;
		this.map = map;
		this.startTime = startTime;
		this.endTime = endTime;
		this.money = money;
	}

	public void addPlayer(Player player) throws VMapException {
		if(state!=STATE_STARTED)
			throw new VMapException(peony.Messages.STRING_00041);
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return peony.Messages.STRING_00042;
	}

	public NationSneakBattleFieldDef getDef(){
		return def;
	}
	
	public VMap getMap(int mapId) {
		return map;
	}

	public void loadingFinished(Player player) {
		Packet pt = new Packet(OpCode.MAP_INFO_SERVER);
		pt.putShort(map.getId());
		pt.putString(player.faction==def.sourceFaction?getDefenseMessage():getAttackMessage());
		player.send(pt);
		if(state == STATE_END){
			out(player);
		}
	}
	
	protected void out(Player player){
		int[] out = def.getOutPoint(player.faction);
		try {
			player.goMap(out[0], out[1], out[2]);
		} catch (VMapException e) {
			log.error(e, e); //不应该出现这种情况
		}
	}

	public void removePlayer(Player player) {
		
	}
	
	public void join(Player player) {
		Set<GameObjectRef> refs = getRefs(player.faction);
		refs.add(player.ref());
	}
	
	public boolean contains(Player player){
		Set<GameObjectRef> refs = getRefs(player.faction);
		return refs.contains(player.ref());
	}
	
	public int getSignupPlayerCount(int faction){
		Set<GameObjectRef> refs = getRefs(faction);
		return refs.size();
	}
	
	protected Set<GameObjectRef> getRefs(int faction){
		if(faction==def.sourceFaction){
			return sourcePlayerRefs;
		}
		else if(faction==def.destFaction){
			return destPlayerRefs;
		}
		return null;
	}
	
	protected boolean atAim(Player p){
		return p.distance(def.backPoint[0],def.backPoint[1]) <= 48*48;
	}
	
	public void moveAt(Player p) {
		if (state == STATE_STARTED) {
			if (p.faction == def.destFaction && p.flag != null && atAim(p)) {
				SneakFlag flag = (SneakFlag) p.flag;
				p.setFlag(null);
				score += flag.getScore();
				// map.shout(p.getFactionName()+p.name+"成功交还一面旗帜", 0xff5555,
				// 5000);
				notifyScore();
				if (score >= WIN_SCORE) {
					win();
				}
			}
		}
//		}else if( state == STATE_END){
//			out(p);
//		}
	}
	
	public void notifyScore(){
		map.notifyMapInfo(getDefenseMessage(),def.sourceFaction);
		map.notifyMapInfo(getAttackMessage(),def.destFaction);
	}
	
	protected String getDefenseMessage(){
		return MessageFormat.format(peony.Messages.STRING_00043, WIN_SCORE-score,WIN_SCORE);
	}
	
	protected String getAttackMessage(){
		return MessageFormat.format(peony.Messages.STRING_00044, score,WIN_SCORE);
	}
	
	//进攻方胜利,那么将进攻方和防守方的关系改成和平，进攻方的保护时间消失
	protected void win(){
		// 记录日志
		LogUtil.logNationSneakBattleEnd(def.sourceFaction, this.sourcePlayerRefs, def.destFaction, this.destPlayerRefs, 
				def.destFaction, System.currentTimeMillis() - this.startTime.getTime());
		
		state = STATE_END;
		NationRel rel1 = manager.rels[def.destFaction][def.sourceFaction];
		NationRel rel2 = manager.rels[def.sourceFaction][def.destFaction];
		Date now = new Date();
		rel1.type = NationRel.TYPE_PEACE;
		rel1.createTime = now;
		rel1.endTime = null;
		rel2.type = NationRel.TYPE_PEACE;
		rel2.createTime = now;
		rel2.endTime = null;
		Nation sourceNation = manager.getNationByFaction(def.sourceFaction); //防守方
		Nation destNation  = manager.getNationByFaction(def.destFaction); //进攻方
		destNation.guardTime = null;
		log.info("[GUARDTIME]FACTION["+destNation.faction+"]");
		sourceNation.pool.changeValue(Nation.PROPERTY_SNEAK_DEFENSE, -1);
		destNation.pool.changeValue(Nation.PROPERTY_SNEAK_ATTACK, -1);
		int value = (int)(money * 0.8);
		if(value>0){
			sourceNation.decMoney(value);
			destNation.addMoney(value);
		}
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendFactionSystemMessage(def.destFaction, 
				MessageFormat.format(peony.Messages.STRING_00045, GameObject.getFactionName(def.sourceFaction)));
		chatService.sendFactionSystemMessage(def.sourceFaction, 
				MessageFormat.format(peony.Messages.STRING_00046, GameObject.getFactionName(def.destFaction)));
		transPlayers();
		notifyPlayerSneekBattleWin(def.destFaction,NationBattleFieldInstance.VICTORY_DEST);
	}
	
	//进攻方失败
	protected void fail(){
		// 记录日志
		LogUtil.logNationSneakBattleEnd(def.sourceFaction, this.sourcePlayerRefs, def.destFaction, this.destPlayerRefs, 
				def.sourceFaction, System.currentTimeMillis() - this.startTime.getTime());

		state = STATE_END;
		Nation destNation  = manager.getNationByFaction(def.destFaction); //进攻方
		Nation sourceNation  = manager.getNationByFaction(def.sourceFaction); //防守方
		sourceNation.pool.changeValue(Nation.PROPERTY_SNEAK_DEFENSE, -1);
		destNation.pool.changeValue(Nation.PROPERTY_SNEAK_ATTACK, -1);
		int value = (int)(destNation.money * 0.05);
		if(value>0){
			destNation.decMoney(value);
			sourceNation.addMoney(value);
		}
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendFactionSystemMessage(def.destFaction, 
				MessageFormat.format(peony.Messages.STRING_00047, GameObject.getFactionName(def.sourceFaction)));
		chatService.sendFactionSystemMessage(def.sourceFaction, 
				MessageFormat.format(peony.Messages.STRING_00048, GameObject.getFactionName(def.destFaction)));
		transPlayers();
	}
	
	protected void transPlayers(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				int[] out = def.getOutPoint(p.faction);
				try {
					p.goMap(out[0], out[1], out[2]);
				} catch (VMapException e) {
					log.error(e, e); //不应该出现这种情况
				}
			}
		}
	}
	
	public void notifyPlayerSneekBattleWin(int winFaction,int victorySide){
		for (GameObject o : map.instanceid2objects.values()) {
			if(o!=null && o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				if(p.faction==winFaction){
					//战场胜利事件
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_BATTLE_WIN,p.id,3,victorySide));
				}
			}
		}
	}

	public void update(int diff) {
		map.update(diff);
		checkShout();
		if (state == STATE_STARTED) {
			if (Time.currDate.after(endTime)) {  //如果到时间进攻方都没有胜利，那么就判防守方胜利
				fail();
			}
		}
	}
	
	protected void checkShout(){ //每降低100喊一次
		int v = WIN_SCORE - score;
		int x = (v-1) / 100;
		if(x>=0&&x<shouted.length){
			if(!shouted[x]){
				shouted[x] = true;
				map.shout(
						MessageFormat.format(peony.Messages.STRING_00049, GameObject.getFactionName(def.sourceFaction)), 
						0xff5555, 5000);
			}
		}
	}

}
