package com.pip.itimes.server.connect.event;

import com.pip.itimes.server.bean.Player;

/**
 * @author Jeffery
 * @version 1.0
 */
public class PositionChangedEvent {
    private Player player;
    private short oldMap;
    private short oldX;
    private short oldY;
    private short map;
    private short x;
    private short y;

    public PositionChangedEvent(Player player, short oldMap, short oldX,
                                short oldY, short map, short x, short y) {
        this.player = player;
        this.oldMap = oldMap;
        this.oldX = oldX;
        this.oldY = oldY;
        this.map = map;
        this.x = x;
        this.y = y;
    }

    public Player getSource(){
        return player;
    }

    public short getOldMap(){
        return oldMap;
    }

    public short getOldX(){
        return oldX;
    }

    public short getOldY(){
        return oldY;
    }

    public short getMap(){
        return map;
    }

    public short getX(){
        return x;
    }

    public short getY(){
        return y;
    }
}
