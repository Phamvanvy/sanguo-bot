package peony.game.itemenhance;

import peony.game.GameItem;

public class AutoEquipEnhance {
	public int serial;
	public int itemId;
	public int instanceId;
	public int level;
	public int money;
	public int count;
	public int specialAtt=-1;
	public GameItem item;
	public int decMoney;
	public int leftTimes;
	public Object owner;
	public int cause;
	public AutoEquipEnhance(int serial, GameItem item, int level, int decMoney,int leftTimes
			, Object owner, int itemId, int instanceId) {
		super();
		this.serial = serial;
		this.item = item;
		this.level = level;
		this.decMoney = decMoney;
		this.leftTimes = leftTimes;
		this.owner = owner;
		this.itemId = itemId;
		this.instanceId = instanceId;
	}
}
