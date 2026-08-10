package peony.game.party;

import peony.game.Player;

public class PartyMember {
	public Player player;
	public boolean isLeader;
	
	public PartyMember(Player player,boolean isLeader){
		this.player = player;
		this.isLeader = isLeader;
	}
	
	public int getId(){
		return player.id;
	}
	
	public String getName(){
		return player.name;
	}
	
	public int getClazz(){
		return player.clazz;
	}
	
	public int getSex(){
		return player.sex;
	}
	
	public int getLevel(){
		return player.level;
	}
	
	public byte getStatus(){
		if(isLeader)
			return (byte)0x80;
		return 0;
	}
	
	public int getHpPercent(){
		return player.maxhp==0?200:player.hp*200/player.maxhp;
	}

	public int getMpPercent(){
		return player.maxmp==0?200:player.mp*200/player.maxmp;
	}
}


