package peony.game.itemenhance;

import peony.game.GameItem;

public class AutoNaturalEnhance {
	
	public int serial;
	public int itemId;
	public int instanceId;
	public int level;
	public int money;
	public int count;
	public GameItem item;
	public int decMoney;
	public Object owner;
	public int cause;
	
	public AutoNaturalEnhance(int serial, GameItem item, int level, int decMoney
			, Object owner, int itemId, int instanceId) {
		super();
		this.serial = serial;
		this.item = item;
		this.level = level;
		this.decMoney = decMoney;
		this.owner = owner;
		this.itemId = itemId;
		this.instanceId = instanceId;
	}
	
}
