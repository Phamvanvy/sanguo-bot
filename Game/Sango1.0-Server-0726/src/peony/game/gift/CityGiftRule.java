package peony.game.gift;

import peony.game.Server;

public class CityGiftRule {
	public static final int TYPE_EVERYACCOUNT = 0; //每个账号只给一次礼物
	public static final int TYPE_EVERYACTOR = 1; //每个角色给一次礼物
	
	public String city;
	public int itemId;
	public int count;
	public int type;
	public String message;
	public String revision;
	
	public CityGiftRule(String city,int itemId,int count,int type,String message,String revision){
		this.city = city;
		this.itemId = itemId;
		this.count = count;
		this.type = type;
		this.message = message;
		this.revision = revision;
	}
	
	public boolean inCity(String city){
		return this.city.equals(city);
	}
	
	public boolean inRevision(String revision){
		return this.revision.equals(Server.server.revision);
	}
}
