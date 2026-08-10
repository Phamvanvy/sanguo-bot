package com.pip.itimes.server.world;

import java.util.Map;
import java.util.HashMap;
import com.pip.itimes.server.bean.Player;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.ArrayIntList;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ChatChannel implements IChatChannel {

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

    public void registry(Player player){
        synchronized(ids){
            ids.add(player.getId());
        }
    }

    public void unRegistry(Player player){
        synchronized(ids){
            ids.removeElement(player.getId());
        }
    }

    public int[] getPlayers(){
        return ids.toArray();
    }

    public boolean isPrivateChannle() {
        return false;
    }


}
