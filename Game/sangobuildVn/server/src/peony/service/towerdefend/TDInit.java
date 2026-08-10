package peony.service.towerdefend;

public class TDInit {

	public int type; //0为防守方,1为进攻方
	
	public int itemId; //初始物品
	
	public int count; //物品数量
	
	public TDInit(int type, int itemId, int count){
		this.type = type;
		this.itemId = itemId;
		this.count = count;
	}
	
}
