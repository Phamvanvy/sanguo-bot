package peony.game.convoy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peony.game.Creature;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.nation.Nation;

/***
 * 国家押镖,每次国镖都新建一个此类的实例,记录所有此次押镖的相关信息
 * @author Jeffrey
 *
 */
public class NationConvoy {
	//押镖的国家
	public Nation nation;
	
	public NationConvoyDef def;
	
	//押金
	public int deposite;

	//押镖的所有人的ref
	public Set<GameObjectRef> sourceRefs = new HashSet<GameObjectRef>();
	//劫镖的所有人的ref
	public Set<GameObjectRef> destRefs = new HashSet<GameObjectRef>();

	//押镖的起始时间以及终止时间
	public int beginTime,endTime;
	
	public Creature npc;
	
	public NationConvoy(Nation nation,NationConvoyDef def,int deposite,int beginTime){
		this.nation = nation;
		this.def = def;
		this.deposite = deposite;
		this.beginTime = beginTime;
	}
	
	public List<Player> getSourcePlayers(){
		List<Player> l = new ArrayList<Player>();
		for(GameObjectRef ref:sourceRefs){
			Player p = ObjectAccessor.getPlayer(ref.id);
			if(p != null && p.level > 35){
				l.add(p);
			}
		}
		return l;
	}
	
	public List<Player> getDestPlayers(){
		List<Player> l = new ArrayList<Player>();
		for(GameObjectRef ref:destRefs){
			Player p = ObjectAccessor.getPlayer(ref.id);
			if(p != null && p.level > 35){
				l.add(p);
			}
		}
		return l;
	}
	
}
