package com.pip.itimes.server.stage;

public class ExchangeGroupNpcType extends TaskNpcType{
    public ExchangeGroupNpcType(int id, String name, int type) {
        super(id, name, type);
    }
 	private int exchangeGroupId;
	public int getExchangeGroupId() {
		return exchangeGroupId;
	}
	public void setExchangeGroupId(int exchangeGroupId) {
		this.exchangeGroupId = exchangeGroupId;
	}

}
