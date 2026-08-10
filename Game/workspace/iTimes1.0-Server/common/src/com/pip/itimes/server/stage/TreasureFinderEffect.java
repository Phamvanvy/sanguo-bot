package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TreasureFinderEffect extends Effect{
	private int delete;
    public TreasureFinderEffect(int delete) {
    	this.delete = delete;
    }

    public int getDelete() {
		return delete;
	}

	public void setDelete(int delete) {
		this.delete = delete;
	}

	public byte getType(){
        return 11;
    }
}
