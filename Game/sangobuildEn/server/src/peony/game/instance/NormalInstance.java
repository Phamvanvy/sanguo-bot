package peony.game.instance;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.Instance;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import ch.javasoft.util.intcoll.IntHashMap;

public class NormalInstance implements Instance{
	
	protected static final AtomicInteger ids = new AtomicInteger(0);
	
	public int id;
	public int createTime;
	public NormalInstanceDefinition definition;
	public int lastLeaveTime;
	public int playerCount;
	public IntHashMap<VMap> maps = new IntHashMap<VMap>();
	public boolean timeOut;
	public Set<GameObjectRef> refs = new HashSet<GameObjectRef>();
	public int lastBossDieTime;
	public List<LeavePartyTransferScheduleAction> leavePartyTransfers = new LinkedList<LeavePartyTransferScheduleAction>();
	
	public NormalInstance(NormalInstanceDefinition definition,int createTime){
		this.id = ids.incrementAndGet();
		this.definition = definition;
		this.createTime = createTime;
		this.lastLeaveTime = createTime;
	}
	
	public int getId(){
		return id;
	}
	
	public String getName() {
		return definition.name;
	}
	
	public void update(int diff){
		if(playerCount==0&&(Time.currTime-lastLeaveTime)>=definition.refreshSecond*1000){
			timeOut = true;
		}
		if(playerCount==0&&((id%10)!=(Time.tick%10)))
			return;
		for(VMap map:maps.values()){
			map.update(diff);
		}
		Iterator<LeavePartyTransferScheduleAction> ite = leavePartyTransfers.iterator();
		while(ite.hasNext()){
			LeavePartyTransferScheduleAction transfer = ite.next();
			if(transfer.update())
				break;
		}
	}
	
	public void addPartyLeaveAction(Player p){
		for(LeavePartyTransferScheduleAction action:leavePartyTransfers){
			if(action.ref.id == p.id)
				return;
		}
		leavePartyTransfers.add(new LeavePartyTransferScheduleAction(p.ref()));
	}
	
	public void removePartyLeaveAction(Player p){
		Iterator<LeavePartyTransferScheduleAction> ite = leavePartyTransfers.iterator();
		while(ite.hasNext()){
			LeavePartyTransferScheduleAction action = ite.next();
			if(action.ref.id == p.id){
				ite.remove();
				break;
			}
		}
	}
	
	public void addVMap(VMap map){
		maps.put(map.getId(), map);
		map.instance = this;
	}
	
	public VMap getMap(int mapId){
		return maps.get(mapId);
	}
	
	public void addPlayer(Player player) throws VMapException{
		if(playerCount>=definition.maxPlayer){
			throw new VMapException("已經超過最大人數");
		}
		if(player.level<definition.minLevel){
			throw new VMapException(MessageFormat.format("副本需要{0}才能進入", definition.minLevel));
		}
		playerCount++;
		attach(player.ref());
	}
	
	public void removePlayer(Player player){
		playerCount--;
		if(playerCount==0){
			lastLeaveTime = Time.currTime;
		}
		Iterator<LeavePartyTransferScheduleAction> ite = leavePartyTransfers.iterator();
		while(ite.hasNext()){
			LeavePartyTransferScheduleAction transfer = ite.next();
			if(transfer.ref.id == player.id){
				ite.remove();
			}
		}
	}
	
	public void out(Player p){
		GameMapDefinition mapDef = VMapUtil.getDefinition(p.getVMap().getId());
		int[] relivePoint = null;
		if (p.faction == GameObject.FACTION_WEI)
			relivePoint = mapDef.mapInfo.renascenceWei;
		else if (p.faction == GameObject.FACTION_SHU)
			relivePoint = mapDef.mapInfo.renascenceShu;
		else if (p.faction == GameObject.FACTION_WU)
			relivePoint = mapDef.mapInfo.renascenceWu;
		try {
			p.goMap(relivePoint[0],  relivePoint[1], relivePoint[2]);
		} catch (VMapException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isTimeOut(){
		return timeOut;
	}
	
	public void attach(GameObjectRef ref){
		refs.add(ref);
	}
	
	public void unAttach(GameObjectRef ref){
		refs.remove(ref);
	}
	
	public void loadingFinished(Player player){
		
	}
	
	class LeavePartyTransferScheduleAction extends ScheduleAction{
		
		public GameObjectRef ref;
		public String[] timeMessage = {"一分鐘","30秒","10秒","5秒"};
		
		public LeavePartyTransferScheduleAction(GameObjectRef ref){
			super(new int[]{Time.currTime,Time.currTime+30000,Time.currTime+50000,Time.currTime+55000,Time.currTime+60000});
			this.ref = ref;
		}
		
		public boolean action(int index){
			if(index>=0&&index<=3){
				Player p = ObjectAccessor.getPlayer(ref.id);
				if(p!=null){
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, "你將在"+timeMessage[index]+"以后被傳出副本");
				}
				return false;
			}
			else if(index==4){
				Player p = ObjectAccessor.getPlayer(ref.id);
				if(p!=null){
					out(p);
				}
				return true;
			}
			return false;
		}
	}
}


