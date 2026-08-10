package peony.game;

import java.util.HashSet;
import java.util.Set;

public class ThreatGroup {
	
	protected Set<GameObjectRef> refs = new HashSet<GameObjectRef>();
	
	protected GameObjectRef target;
	
	protected VMap map;
	
	public ThreatGroup(GameObjectRef target,VMap map){
		this.target = target;
		this.map = map;
	}
	
	public void addCreature(Creature u){
		refs.add(u.ref());
		if(u.threatGroup==null){
			u.threatGroup = this;
		}
	}
	
	/**
	 * 是否可以脱离战斗，如果都没有收到攻击5秒并且出了追击范围，那么就可以脱离战斗了
	 * @return
	 */
	public boolean outOfBattle(){
		Unit u = (Unit)ObjectAccessor.getGameObject(target);
		if(u==null)
			return true;
		if(u.getVMap()!=map)
			return true;
		for(GameObjectRef ref:refs){
			Creature c = (Creature)ObjectAccessor.getGameObject(ref);
			if(c==null)
				continue;
			if(!c.ai.canOutOfBattle())
				return false;
		}
		return true;
	}
	
	
	public void backState(){
		for(GameObjectRef ref:refs){
			Creature c = (Creature)ObjectAccessor.getGameObject(ref);
			if(c!=null&&c.isVisibleAndAlive())
				c.ai.backState();
		}
	}
	
//	public void broadcastThreat(Unit source,Unit target,boolean direct){
//		for(GameObjectRef ref:refs){
//			Creature c = (Creature)ObjectAccessor.getGameObject(ref);
//			if(c!=null&&c!=source){
//				
//			}
//		}
//	}
}
