package com.pip.itimes.server.stage;

public class RemoveItemEffect extends Effect {
	private int removeItem;		// ¿ØÖÆÊÇ·ñÉ¾³ý
	
	public RemoveItemEffect(int removeItem) {
		this.removeItem = removeItem;
	}
	
	public int getRemoveItem() {
		return removeItem;
	}

	public byte getType() {
		return 60;
	}

}
