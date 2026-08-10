package peony.service.towerdefend;

import java.util.Date;
import peony.game.Player;

public class TowerDefend {

	public int id;
	
	public int type; //0为守方,1为攻方
	
	public int partyId;
	
	public int leader;
	
	public Date signTime;
	
	public int faction;
	
	public String leaderName;
	
	public TowerDefend(){
		
	}
	
	public TowerDefend(Player player){
		this.leader = player.id;
		this.signTime = new Date();
		this.partyId = player.party.id;
		this.faction = player.faction;
		this.leaderName = player.name;
	}
	
}
