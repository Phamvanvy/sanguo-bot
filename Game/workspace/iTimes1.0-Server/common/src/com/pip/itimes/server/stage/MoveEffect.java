package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class MoveEffect extends Effect {
	
	public short getNewMapId() {
		return newMapId;
	}

	public void setNewMapId(short newMapId) {
		this.newMapId = newMapId;
	}

	public short getNewX() {
		return newX;
	}

	public void setNewX(short newX) {
		this.newX = newX;
	}

	public short getNewY() {
		return newY;
	}

	public void setNewY(short newY) {
		this.newY = newY;
	}

	private short newMapId;
	private short newX;
	private short newY;
	
    private short mapId;
    private short x;
    private short y;

    public MoveEffect() {
    }

    public byte getType() {
        return 3;
    }

    public short getY() {
        return y;
    }

    public short getX() {
        return x;
    }

    public void setMapId(short mapId) {
        this.mapId = mapId;
    }

    public void setY(short y) {
        this.y = y;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getMapId() {
        return mapId;
    }
}
