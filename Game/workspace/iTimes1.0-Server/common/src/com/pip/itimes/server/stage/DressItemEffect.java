package com.pip.itimes.server.stage;

public class DressItemEffect extends Effect {
	private int faceId;		// ÐÎÏóid
	
	public DressItemEffect(int faceId) {
		this.faceId = faceId;
	}
	
	public int getFaceId() {
		return faceId;
	}

	public byte getType() {
		return 59;
	}
}
