package peony.service.tong.battle;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import peony.game.Creature;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.chat.ChatService;
import peony.service.ServiceEvent;
import peony.service.tong.Tong;
import peony.service.tong.apply.TongBattleApply;
import peony.service.tong.apply.TongBattleApplyService;

public class TongBattleFieldInstance implements Instance {
	
	public static final int STATE_PREPARE = 1;
	public static final int STATE_STARTED = 2;
	public static final int STATE_END = 3;
	
	protected AtomicInteger IDS = new AtomicInteger(1);
	
	protected TongBattleDef def;
	protected VMap map;
	
	protected TongBattleSide defend,attack1,attack2;
	
	protected int id;
	
	public int state;
	
	protected int startTime;
	
	protected int createTime,endTime,duration;
	
	protected Creature attackFlag1; // 进攻方A军旗
	protected Creature attackFlag2; // 进攻方B军旗
	protected Creature defenceFlag; // 防守方C军旗
	
	public TongBattleFieldInstance(TongBattleDef def){
		this.def = def;
		this.id = IDS.incrementAndGet();
		this.state = STATE_PREPARE;
		this.createTime = Time.currTime;
		this.duration = def.duration;
		this.endTime = Time.currTime + def.duration;
	}

	public void addPlayer(Player player) throws VMapException {
		if(state==STATE_END)
			throw new VMapException("城戰已經結束");
	}

	public int getId() {
		return id;
	}

	public VMap getMap(int mapId) {
		if(map.getId() == mapId){
			return map;
		}
		return null;
	}

	public String getName() {
		return def.name;
	}

	public void loadingFinished(Player player) {

	}

	public void removePlayer(Player player) {

	}

	public void update(int diff) {
		if(map != null){
			map.update(diff);
		}
		checkEnd();
		if (state == STATE_PREPARE && Time.currTime >= startTime) {
			state = STATE_STARTED;
			map.shout("城戰開始", 0xff5555, 5000);
		}
	}
	
	/** 检测城战是否处于结束状态 */
	protected void checkEnd(){
		if(!defenceFlag.isAlive() && defend==null){
			// 空城防守战
			if(attackFlag1!=null && attackFlag2!=null){
				if(!attackFlag1.isAlive() && !attackFlag2.isAlive()){
					state = STATE_END;
					failNotify(attack1,null,0);
					failNotify(attack2,null,0);
				}else if(attackFlag1.isAlive() && !attackFlag2.isAlive()){
					state = STATE_END;
					winNotify(attack1,null,0);
					failNotify(attack2,null,0);
				}else if(!attackFlag1.isAlive() && attackFlag2.isAlive()){
					state = STATE_END;
					winNotify(attack2,null,0);
					failNotify(attack1,null,0);
				}
			}
			TongBattleApplyService.currentCity = 0;
			Server.server.getServiceRegistry().getTongBattleApplyService().clearApplys(def.signMapId);
		}else if(!defenceFlag.isAlive() && defend!=null){
			if(attackFlag1!=null && attackFlag2!=null){
				// 三方争夺战
				if(!attackFlag1.isAlive() && !attackFlag2.isAlive()){
					state = STATE_END;
					win(defend,attack1);
					win(defend,attack2);
				}else if(attackFlag1.isAlive() && !attackFlag2.isAlive()){
					state = STATE_END;
					win(attack1,defend);
					win(attack1,attack2);
				}else if(!attackFlag1.isAlive() && attackFlag2.isAlive()){
					state = STATE_END;
					win(attack2,defend);
					win(attack2,attack1);
				}
			}else if(attackFlag1!=null && attackFlag2==null){
				// 双方争夺战
				if(attackFlag1.isAlive()){
					state = STATE_END;
					win(attack1,defend);
				}else{
					state = STATE_END;
					win(defend,attack1);
				}
			}
			TongBattleApplyService.currentCity = 0;
			Server.server.getServiceRegistry().getTongBattleApplyService().clearApplys(def.signMapId);
		}else if(defenceFlag.isAlive() && defend!=null){
			// 防守方胜利
			if(attackFlag1!=null && attackFlag2!=null && !attackFlag1.isAlive() && !attackFlag2.isAlive()){
				state = STATE_END;
				win(defend,attack1);
				win(defend,attack2);
				TongBattleApplyService.currentCity = 0;
				Server.server.getServiceRegistry().getTongBattleApplyService().clearApplys(def.signMapId);
			}else if(attackFlag1!=null && attackFlag2==null && !attackFlag1.isAlive()){
				state = STATE_END;
				win(defend,attack1);
				TongBattleApplyService.currentCity = 0;
				Server.server.getServiceRegistry().getTongBattleApplyService().clearApplys(def.signMapId);
			}
		}
		if(Time.currTime > this.endTime){
			this.state = STATE_END;
			transPlayers();
			if(defend!=null){
				win(defend,attack1);
				win(defend,attack2);
			}else{
				failNotify(attack1, null, 0);
				failNotify(attack2, null, 0);
			}
			TongBattleApplyService.currentCity = 0;
			Server.server.getServiceRegistry().getTongBattleApplyService().clearApplys(def.signMapId);
		}
		if(state==STATE_END && Server.server.getServiceRegistry().getTongBattleApplyService().battles.get(def.signMapId)!=null){
			Server.server.getServiceRegistry().getTongBattleApplyService().battles.put(def.signMapId, null);
		}
	}
	
	public int getState(){
		return this.state;
	}
	
	public int[] getRelivePoint(int playerId){
		if(defend!=null && defend.containsPlayer(playerId)){
			return new int[]{def.mapId,def.defend.relive.x,def.defend.relive.y};
		}else if(attack1!=null && attack1.containsPlayer(playerId)){
			return new int[]{def.mapId,def.attack1.relive.x,def.attack1.relive.y};
		}else if(attack2!=null && attack2.containsPlayer(playerId)){
			return new int[]{def.mapId,def.attack2.relive.x,def.attack2.relive.y};
		}
		return null;
	}
	
	public boolean contains(int playerId){
		if(defend!=null && defend.containsPlayer(playerId)){
			return true;
		}
		if(attack1!=null && attack1.containsPlayer(playerId)){
			return true;
		}
		if(attack2!=null && attack2.containsPlayer(playerId)){
			return true;
		}
		return false;
	}
	
	public TongBattleSide getSide(Tong tong){
		if(defend!=null && defend.tong == tong)
			return defend;
		if(attack1!=null && attack1.tong == tong)
			return attack1;
		if(attack2!=null && attack2.tong == tong)
			return attack2;
		return null;
	}
	
	public void winNotify(TongBattleSide side, TongBattleSide fail, int money){
		transPlayers();
		if(side==null)
			return;
		recordWin(side);
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		if(fail==null){
			chatService.sendGuildSystemMessage(MessageFormat.format(
					"{0}軍團進攻{1}胜利,獲得該城池的占領權", side.tong.name,side.def.battleDef.name), side.tong.id);
			chatService.sendFactionSystemMessage(side.faction, MessageFormat.format(
					"{0}軍團進攻{1}胜利,獲得該城池的占領權", side.tong.name,side.def.battleDef.name));
		}else{
			chatService.sendGuildSystemMessage(MessageFormat.format(
					"{0}軍團進攻{1}胜利,獲得該城池的占領權,并且獲得{2}軍團的資金{3}", 
					side.tong.name,side.def.battleDef.name,fail.tong.name, money), side.tong.id);
			chatService.sendFactionSystemMessage(side.faction, MessageFormat.format(
					"{0}軍團進攻{1}胜利,獲得該城池的占領權,并且獲得{2}軍團的資金{3}", 
					side.tong.name,side.def.battleDef.name,fail.tong.name, money));
		}
		//设置默认税率
		side.tong.taxRate = 0.05f;
		//城战胜利事件
		for(int playerId : side.playersId){
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_BATTLE_WIN,playerId,2));
		}
	}
	
	public void failNotify(TongBattleSide side, TongBattleSide win, int money){
		transPlayers();
		if(side==null)
			return;
		recordFail(side);
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		if(win==null){
			chatService.sendGuildSystemMessage(MessageFormat.format("{0}軍團進攻{1}失利",
					side.tong.name,side.def.battleDef.name), side.tong.id);
			chatService.sendFactionSystemMessage(side.faction, MessageFormat.format(
					"{0}軍團進攻{1}失利",side.tong.name,side.def.battleDef.name));
		}else{
			chatService.sendGuildSystemMessage(MessageFormat.format("{0}軍團進攻{1}失利,失去軍團資金{2}",
					side.tong.name,side.def.battleDef.name,money), side.tong.id);
			chatService.sendFactionSystemMessage(side.faction, MessageFormat.format(
					"{0}軍團進攻{1}失利失去軍團資金{2}",side.tong.name,side.def.battleDef.name,money));
		}
	}
	
	public void win(TongBattleSide win, TongBattleSide fail){
		if(win==null || fail==null)
			return;
		Tong winTong = win.tong;
		Tong failTong = fail.tong;
		int value;
		if(fail==defend){
			value = (int) (failTong.money * 0.3f);
		}else{
			value = (int) (failTong.money * 0.1f);
		}
		winNotify(win,fail,value);
		failNotify(fail,win,value);
		try {
			failTong.decMoney(value);
			winTong.addMoney(value);
			LogUtil.logTongBattleEnd(win, fail, value);
		} catch (NoEnoughValueException e) {
		}
	}
	
	protected void recordWin(TongBattleSide side){
		TongBattleApplyService service = Server.server.getServiceRegistry().getTongBattleApplyService();
		TongBattleApply apply = service.getApplyByTongId(side.tong.id);
		apply.state = 1;
		service.owners.put(apply.mapId, apply);
		side.tong.pool.setInt(Tong.PROPERTY_TONGBATTLE_WIN, side.tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_WIN,0)+1);
	}
	
	protected void recordFail(TongBattleSide side){
		side.tong.pool.setInt(Tong.PROPERTY_TONGBATTLE_FAIL, side.tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_FAIL,0)+1);
	}
	
	/** 战场结束将所有玩家传送出战场 */
	protected void transPlayers(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				int[] out = def.getOutPoint();
				try {
					p.goMap(def.signMapId, out[0], out[1]);
				} catch (VMapException e) {
					
				}
			}
		}
		if(this.attack1!=null){
			this.attack1.playersId.clear();
		}
		if(this.attack2!=null){
			this.attack2.playersId.clear();
		}
		if(this.defend!=null){
			this.defend.playersId.clear();
		}
	}
	
	/** 判断玩家是否在进行城战 */
	public int isInTongBattle(Player p){
		if(defend!=null){
			for(int playerId : defend.playersId){
				if(playerId==p.id)
					return 1;
			}
		}
		if(attack1!=null){
			for(int playerId : attack1.playersId){
				if(playerId==p.id)
					return 1;
			}
		}
		if(attack2!=null){
			for(int playerId : attack2.playersId){
				if(playerId==p.id)
					return 1;
			}
		}
		return 0;
	}
}
