package peony.service.tong.battle;

import java.util.HashSet;
import java.util.Set;

import peony.service.tong.Tong;

public class TongBattleSide {
	public Tong tong;
	public TongBattleSideDef def;
	public Set<Integer> playersId = new HashSet<Integer>();
	public int minorFaction;
	public int faction;
	public static int MAXPLAYERS = 12;
	
	public TongBattleSide(Tong tong,TongBattleSideDef def, int faction,int minorFaction){
		this.tong = tong;
		this.def = def;
		this.faction = faction;
		this.minorFaction = minorFaction;
	}
	
	public boolean isNpc(){
		return tong == null;
	}
	
	public void addPlayerId(int id){
		playersId.add(id);
	}
	
	public boolean containsPlayer(int id){
		return playersId.contains(id);
	}
	
	public boolean isFull(){
		return playersId.size()>=MAXPLAYERS;
	}
}
