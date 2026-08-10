package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TransferDoor {

    private String name;
    private int mapId;
    private int x,y;
    
    private int newMap;
    private int level;
    
    public int getNewMap() {
		return newMap;
	}

	public void setNewMap(int newMap) {
		this.newMap = newMap;
	}

	public int getNewX() {
		return newX;
	}

	public void setNewX(int newX) {
		this.newX = newX;
	}

	public int getNewY() {
		return newY;
	}

	public void setNewY(int newY) {
		this.newY = newY;
	}

	private int newX,newY;
    public TransferDoor() {
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public String getName() {
        return name;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMapId() {
        return mapId;
    }
    
    public void setLevel(int level) {
    	this.level = level;
    }
    
    public int getLevel() {
    	return level;
    }

    private static Map transfers = new HashMap();

    public static TransferDoor getTransferDoor(String name){
        return (TransferDoor)transfers.get(name);
    }

    public static void addTransferDoor(TransferDoor door){
        transfers.put(door.getName(),door);
    }
}
