package peony.game;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;


@Embeddable
@AccessType("field")
public class VMapReference {
	@Column(name="mapid",nullable=false)
	public int id;
	@Transient
	public VMap map;
	
	public int getId() {
		return id;
	}
	
	public GameObject[] getUnits(GameObject u,int dist){
		if(map==null)
			throw new IllegalStateException();
		return map.getUnits(u, dist);
	}
	
	public void getEnemies(GameObject u,GameObject ref,int dist, int max, Set<Unit> candidates) {
		if(map==null)
			throw new IllegalStateException();
		map.getEnemies(u, ref, dist, max, candidates);
	}
	
	public void getAidUnits(GameObject u,GameObject ref,int dist, int max, Set<Unit> candidates) {
		if(map==null)
			throw new IllegalStateException();
		map.getAidUnits(u, ref, dist, max, candidates);
	}
	
	public void setMap(VMap map){
		this.id = map.getId();
		this.map = map;
	}
	
//	public void broadcastMove(GameObject u ,Player p,short type){
//		if(map==null)
//			throw new IllegalStateException();
//		map.broadcastMove(u, p, type);
//	}
//	
//	
//	public void broadcast(Packet packet,Player p){
//		if(map==null)
//			throw new IllegalStateException();
//		map.broadcast(packet, p);
//	}
	
	public void removeGameObject(GameObject player,boolean notify){
		if(map==null)
			throw new IllegalStateException();
		map.removeGameObject(player,notify);
	}
	
	public void playerLoadingFinished(Player player){
		if(map==null)
			throw new IllegalStateException();
		map.playerLoadingFinished(player);
	}
	
//	public Creature getCreature(int id){
//		if(map==null)
//			throw new IllegalStateException();
//		return map.getCreature(id);
//	}
	
	public int getStageId(){
		return id>>4;
	}
	
//	public GameObject getUnit(byte type,int id){
//		if(map==null)
//			throw new IllegalStateException();
//		return map.getUnit(type, id);
//	}
}
