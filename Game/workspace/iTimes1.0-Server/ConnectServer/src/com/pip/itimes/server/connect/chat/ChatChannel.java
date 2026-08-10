package com.pip.itimes.server.connect.chat;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ChatChannel implements IChatChannel{
    private int id;
    private String name;
    private IntList ids = new ArrayIntList();

    public ChatChannel(int id,String name) {
        this.id = id;
        this.name = name;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public void registry(int playerId){
        synchronized(ids){
            ids.add(playerId);
        }
    }

    public void unRegistry(int playerId){
        synchronized(ids){
            ids.removeElement(playerId);
        }
    }

    public int[] getPlayers(){
        return ids.toArray();
    }

    public boolean isPrivateChannle() {
        return false;
    }
}
