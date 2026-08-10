package peony.game.nation;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import peony.game.Creature;
import peony.game.CreatureAI;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.Instance;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.buff.GodBuff;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.service.ServiceEvent;
import peony.service.stat.StatService;

public class NationBattleFieldInstance implements Instance {
	
	private static final Logger log = Logger.getLogger(NationBattleFieldInstance.class);
	
	protected static final AtomicInteger ids = new AtomicInteger(0);
	
	protected Date startTime,endTime;
	protected NationBattleFieldDef def;
	protected VMap map;
	protected boolean canAttackKing;
	protected int id;
	
	protected Set<GameObjectRef> sourcePlayerRefs = new HashSet<GameObjectRef>(50);  //防守方
	protected Set<GameObjectRef> destPlayerRefs = new HashSet<GameObjectRef>(50); //进攻方
	protected Creature[] guards;
	protected Creature king;
	protected CreatureAI kingAi;
	protected int state = STATE_STARTED;
	
	public static final int STATE_STARTED = 0;
	public static final int STATE_END = 1;
	
	public static final int VICTORY_SOURCE = 0; //防守方胜利
	public static final int VICTORY_DEST = 1; //进攻方胜利
	
	protected NationService manager;
	
	public static int HEALTH_VALUE = 10;
	
	
	public NationBattleFieldInstance(NationService manager,VMap map,NationBattleFieldDef def,Date startTime,Date endTime){
		this.id = ids.incrementAndGet();
		this.manager = manager;
		this.map = map;
		map.instance = this;
		this.def = def;
		this.startTime = startTime;
		this.endTime = endTime;
		this.guards = new Creature[def.guards.length];
		for(int i=0;i<def.guards.length;i++){
			for(GameObject o:map.instanceid2objects.values()){
				if(def.guards[i]==o.id){
					guards[i] = (Creature)o;
					break;
				}
			}
		}
		for(GameObject o:map.instanceid2objects.values()){
			if(o.id==def.kingId){
				king = (Creature)o;
				this.kingAi = king.getCreatureAI(); //将原来的国王的AI保存起来,并将过往的ai设置成空,等国王被激活以后再重新设置上
				king.setAI(null);
				king.buffs.addBuff(new GodBuff());
			}
		}
	}
	
	public NationBattleFieldDef getDef(){
		return def;
	}

	public void addPlayer(Player player) throws VMapException {
		if(state!=STATE_STARTED)
			throw new VMapException(peony.Messages.STRING_00041);
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
	

	public int getId() {
		return id;
	}
	
	public String getName() {
		return peony.Messages.STRING_00782;
	}

	public VMap getMap(int mapId) {
		return map;
	}

	public void loadingFinished(Player player) {

	}

	public void removePlayer(Player player) {
		if(player.flag!=null&&state==STATE_END){
			player.setFlag(null);
		}
	}

	/**
	 * 如果战场结束返回ture，否则返回false
	 */
	public void update(int diff) {
		if(state==STATE_STARTED){
			if(map!=null)
				map.update(diff);
			checkKingCanAttack();
			checkBattleValue();
			checkEnd();
		}
	}
	
	//检查战斗数值，达到某个值给战场里的人加血
	protected void checkBattleValue(){
		Nation sourceNation = manager.getNationByFaction(def.sourceFaction); //防守方
		Nation destNation = manager.getNationByFaction(def.destFaction); //进攻方
		if(sourceNation.pool.getInt(Nation.PROPERTY_DEFENSE_VALUE)>=HEALTH_VALUE){
			sourceNation.pool.changeValue(Nation.PROPERTY_DEFENSE_VALUE, -HEALTH_VALUE);
			for(GameObject o:map.instanceid2objects.values()){
				if(o.faction==def.sourceFaction&&o.type==GameObject.TYPE_PLAYER&&o.isAlive()){
					addHp((Player)o,2000);
				}
			}
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(def.sourceFaction, 
					MessageFormat.format(peony.Messages.STRING_00783, 
							GameObject.getFactionName(def.sourceFaction)));
		}
		if(destNation.pool.getInt(Nation.PROPERTY_ATTACK_VALUE)>=HEALTH_VALUE){
			destNation.pool.changeValue(Nation.PROPERTY_ATTACK_VALUE, -HEALTH_VALUE);
			for(GameObject o:map.instanceid2objects.values()){
				if(o.faction==def.destFaction&&o.type==GameObject.TYPE_PLAYER&&o.isAlive()){
					addHp((Player)o,2000);
				}
			}
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(def.destFaction, 
					MessageFormat.format(peony.Messages.STRING_00784, 
							GameObject.getFactionName(def.destFaction)));
		}
		
	}
	
	protected void addHp(Player p, int value){
		int v = Math.min(p.maxhp,p.hp+value);
		p.setHp(v, true);
	}
	
	protected void checkEnd(){ //有两种情况会导致战场的结束
		if(!king.isAlive()){ //如果国王死亡结束
			state = STATE_END;
			win(); //进攻方胜利
		}else{
			if(endTime.before(Time.currDate)){ //如果时间到期
				state = STATE_END;
				fail(); //进攻方失败
			}
		}
	}
	
	/**
	 * 如果进攻方失败，那么两国的关系都转变成和平关系，这种情况下是可以被任何国家宣战的
	 */
	protected void fail(){
		// 记录日志
		LogUtil.logNationBattleEnd(def.sourceFaction, this.sourcePlayerRefs, def.destFaction, this.destPlayerRefs, 
				def.sourceFaction, System.currentTimeMillis() - this.startTime.getTime());

		NationRel rel1 = manager.rels[def.destFaction][def.sourceFaction];
		NationRel rel2 = manager.rels[def.sourceFaction][def.destFaction];
		rel1.type = NationRel.TYPE_PEACE;
		rel1.createTime = Time.currDate;
		rel1.endTime = null;
		rel2.type = NationRel.TYPE_PEACE;
		rel2.createTime = Time.currDate;
		rel2.endTime = null;
		Nation sourceNation = manager.getNationByFaction(def.sourceFaction);  //防守方
		Nation destNation = manager.getNationByFaction(def.destFaction); //进攻方
		sourceNation.pool.changeValue(Nation.PROPERTY_BATTLE_DEFENSE, -1);
		destNation.pool.changeValue(Nation.PROPERTY_BATTLE_ATTACK, -1);
		int value = (int)(destNation.money * 0.1);
		if(value>0){
			sourceNation.addMoney(value);
			destNation.decMoney(value);
		}
		String winpro = Nation.PROPERTY_WIN_PREFIX+def.destFaction;
		String failpro = Nation.PROPERTY_FAIL_PREFIX+def.sourceFaction;
		
		sourceNation.pool.changeValue(winpro, 1);
		destNation.pool.changeValue(failpro, 1);
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendFactionSystemMessage(def.destFaction, peony.Messages.STRING_00785);
		chatService.sendFactionSystemMessage(def.destFaction, peony.Messages.STRING_00786);
		chatService.sendFactionSystemMessage(def.sourceFaction, peony.Messages.STRING_00787);
		chatService.sendFactionSystemMessage(def.sourceFaction, peony.Messages.STRING_00788);
		NationSkill skill = manager.getNationByFaction(def.sourceFaction).skills.get(5);
		int itemAccount = ((NationSkill5)skill).getItemAccount(skill.level);
		sendMails(sourcePlayerRefs,itemAccount,VICTORY_SOURCE);
		transPlayers();
	}
	
	/**
	 * 进攻方胜利
	 */
	protected void win(){
		// 记录日志
		LogUtil.logNationBattleEnd(def.sourceFaction, this.sourcePlayerRefs, def.destFaction, this.destPlayerRefs, 
				def.destFaction, System.currentTimeMillis() - this.startTime.getTime());
		
		NationRel rel1 = manager.rels[def.destFaction][def.sourceFaction];
		NationRel rel2 = manager.rels[def.sourceFaction][def.destFaction];
		rel1.type = NationRel.TYPE_WIN;
		rel1.createTime = Time.currDate;
		rel1.endTime = new Date(Time.currDate.getTime() + NationService.FAIL_TAX_TIME);
		rel2.type = NationRel.TYPE_FAIL;
		rel2.createTime = Time.currDate;
		rel2.endTime = new Date(Time.currDate.getTime() + NationService.FAIL_TAX_TIME);
		Nation sourceNation = manager.getNationByFaction(def.sourceFaction);  //防守方
		Nation destNation = manager.getNationByFaction(def.destFaction); //进攻方
		sourceNation.pool.changeValue(Nation.PROPERTY_BATTLE_DEFENSE, -1);
		destNation.pool.changeValue(Nation.PROPERTY_BATTLE_ATTACK, -1);
		sourceNation.guardTime = new Date(Time.currDate.getTime() + NationService.FAIL_GUARD_TIME);
		int value = (int)(sourceNation.money * 0.2);
		if(value>0){
			destNation.addMoney(value);
			sourceNation.decMoney(value);
			rel1.money = value;
			rel2.money = value;
		}else{
			rel1.money = 0;
			rel2.money = 0;
		}
		String winpro = Nation.PROPERTY_WIN_PREFIX+def.sourceFaction;
		String failpro = Nation.PROPERTY_FAIL_PREFIX+def.destFaction;
		destNation.pool.changeValue(winpro, 1);
		sourceNation.pool.changeValue(failpro, 1);
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendFactionSystemMessage(def.destFaction, peony.Messages.STRING_00789);
		chatService.sendFactionSystemMessage(def.destFaction,peony.Messages.STRING_00790);
		chatService.sendFactionSystemMessage(def.sourceFaction, peony.Messages.STRING_00791);
		chatService.sendFactionSystemMessage(def.sourceFaction,peony.Messages.STRING_00792);
		Nation nation = manager.getNationByFaction(def.destFaction);
		NationSkill skill = nation.skills.get(5);
		int itemAccount = ((NationSkill5)skill).getItemAccount(skill.level);
		sendMails(destPlayerRefs,itemAccount,VICTORY_DEST);
		transPlayers();
		
	}
	
	protected void sendMails(Set<GameObjectRef> refs, int itemAccount,int victorySide) {
		MailService mailService = Server.server.getServiceRegistry()
				.getMailService();
		for (GameObjectRef ref : refs) {
			mailService.sendSystemMailAsync(ref.id, peony.Messages.STRING_00004, peony.Messages.STRING_00793, "", 0,
					ObjectAccessor
							.createGameItem(ItemUtil.ITME_NATIONBATTLE_WIN), itemAccount, "NBT");
			//国战胜利事件
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_BATTLE_WIN,ref.id,0,victorySide));
			//统计十分钟内取得国战胜利成就
			StatService service = Server.server.getServiceRegistry().getStatService();
			service.getNationWinInTen(ref.id, startTime);
		}
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
					p.pool.setInt(Player.PROPERTY_KILLENEMY_ONENBATTLE, 0);//单场国战中玩家杀敌国玩家个数国战结束时清零
				} catch (VMapException e) {
					log.error(e, e); //不应该出现这种情况
				}
			}
		}
	}
	
	/**
	 * 如果所有的大将都死亡，那么将过往身上的GodBuff移除变成可以攻击
	 */
	protected void checkKingCanAttack(){
		if(!canAttackKing){
			for(Creature c:guards){
				if(c!=null&&c.isAlive())
					return;
			}
			canAttackKing = true;
			king.setAI(kingAi); 
			king.buffs.removeBuff(GodBuff.GOD_BUFFID); 
		}
	}
	
	/** 判断玩家是否在进行国战 */
	public int isInNatinBattle(Player p){
		if(p!=null){
			for(GameObjectRef playerRef : sourcePlayerRefs){
				if(playerRef.id==p.id && this.map!=null && p.map.id==this.map.getId())
					return 1;
			}
			for(GameObjectRef playerRef : destPlayerRefs){
				if(playerRef.id==p.id && this.map!=null && p.map.id==this.map.getId())
					return 1;
			}
		}
		return 0;
	}
}
