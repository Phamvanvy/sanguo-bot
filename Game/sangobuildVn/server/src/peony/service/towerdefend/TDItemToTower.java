package peony.service.towerdefend;

/**
 * 塔防物品见他转换信息
 * @author dchen
 */
public class TDItemToTower {

	public int itemId;
	public int towerId;
	public String type;
	public static String TYPE_DEFEND = "DEFEND";
	public static String TYPE_STATE = "STATE";
	public static String TYPE_ATTACK = "ATTACK";
	
	public TDItemToTower(int itemId, int towerId, String type){
		this.itemId = itemId;
		this.towerId = towerId;
		this.type = type;
	}
	
}
