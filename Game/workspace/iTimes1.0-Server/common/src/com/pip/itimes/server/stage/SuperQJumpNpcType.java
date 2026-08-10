package com.pip.itimes.server.stage;

public class SuperQJumpNpcType extends TaskNpcType {
	public int trancetype;
	public short mapId;
	public short x;
	public short y;
	public short mapId1 = 0;
	public short x1 = 0;
	public short y1 = 0;
	public SuperQJumpNpcType(int id, String name, int type) {
        super(id, name, type);
    }
	public short getMapId() {
		return mapId;
	}
	public void setMapId(short mapId) {
		this.mapId = mapId;
	}
	public short getX() {
		return x;
	}
	public void setX(short x) {
		this.x = x;
	}
	public short getY() {
		return y;
	}
	public void setY(short y) {
		this.y = y;
	}
	public int getTrancetype() {
		return trancetype;
	}
	public void setTrancetype(int trancetype) {
		this.trancetype = trancetype;
	}
	public short getMapId1() {
		return mapId1;
	}
	public void setMapId1(short mapId1) {
		this.mapId1 = mapId1;
	}
	public short getX1() {
		return x1;
	}
	public void setX1(short x1) {
		this.x1 = x1;
	}
	public short getY1() {
		return y1;
	}
	public void setY1(short y1) {
		this.y1 = y1;
	}
	
}
