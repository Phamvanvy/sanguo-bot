package com.pip.itimes.server.world;

import java.util.*;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.Friend;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FriendService {

    private MultiMap name2name = new MultiMap();

    private ConnectService connectService;
    private PlayerService playerService;

    public FriendService() {
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void addedFriend(PlayerData src,int id,String name){
        name2name.put(name,src.getPlayerName());
    }

    public void removeFriend(PlayerData src,String name){
        name2name.remove(name,src.getPlayerName());
    }

    public void registry(IPlayerData player) {
        Friend[] friends = player.getFriends();
        if (friends.length > 0) {
            for (int i = 0; i < friends.length; i++) {
                name2name.put(friends[i].getName(), player.getPlayerName());
            }
        }
        if(player.getPlayerName().toLowerCase().startsWith("gm"))
            return;
        Object[] names = name2name.get(player.getPlayerName());
        for (int i = 0; i < names.length; i++) {
            String name = (String) names[i];
            WorldPlayer p = playerService.getWorldPlayer(name);
            if (p != null && p.getState() == WorldPlayer.ONLINE) {
                notifyOnline(player, p);
            }
        }
    }

    public void sendOnlineFriends(WorldPlayer player) {
        Friend[] friends = player.getFriends();
        if(friends.length==0)
            return;
        UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
        seg.write((byte)friends.length);
        long now = new Date().getTime();
        for(int i=0;i<friends.length;i++){
            WorldPlayer p = playerService.getWorldPlayer(friends[i].getId());
            seg.writeInt(friends[i].getId());
            seg.writeBoolean(p != null&&p.getState()==WorldPlayer.ONLINE);
            seg.writeShort((short)friends[i].getFavorite());
            if(p != null && p.online()){
            	seg.writeInt(0);
            }else{
            	seg.writeInt(Utils.getLoginTimeSecond(now, friends[i].getLoginTime()));
            }
        }
        connectService.writeTo(seg,player.getId());
    }
    
    /**
     * 判断指定的ID是不是好友列表中的
     * @param player
     * @param id
     * @return
     */
    public boolean isFriend(WorldPlayer player, int id){
    	Friend[] friends = player.getFriends();
        if(friends.length==0)
            return false;
        for(int i=0;i<friends.length;i++){
            if(friends[i].getId() == id) return true;
        }
        return false;
    }

    private void notifyOnline(IPlayerData src,IPlayerData dest){
        UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
        seg.write((byte)1);
        seg.writeInt(src.getId());
        seg.writeBoolean(true);
        short favorite = 0;
        if(dest instanceof WorldPlayer && src instanceof WorldPlayer){
        	WorldPlayer wp = (WorldPlayer)src;
        	favorite = (short)dest.getFriendFavorite(wp);
        }
        seg.writeShort(favorite);
        seg.writeInt(0);
        connectService.writeTo(seg,dest.getId());
    }

    private void notifyOffline(IPlayerData src,IPlayerData dest){
        UWAPSegment seg = new UWAPSegment(ClientConstants.FRIENDS_STATUS);
        seg.write((byte)1);
        seg.writeInt(src.getId());
        seg.writeBoolean(false);
        short favorite = 0;
        if(dest instanceof WorldPlayer && src instanceof WorldPlayer){
        	WorldPlayer wp = (WorldPlayer)src;
        	favorite = (short)dest.getFriendFavorite(wp);
        }
        seg.writeShort(favorite);
        seg.writeInt(0);
        connectService.writeTo(seg,dest.getId());
    }

    public void unRegistry(IPlayerData player){
        Object[] names = name2name.get(player.getPlayerName());
        for(int i=0;i<names.length;i++){
            String name = (String)names[i];
            IPlayerData p = playerService.getWorldPlayer(name);
            if(p!=null){
                notifyOffline(player,p);
            }
        }
    }
}

class MultiMap{

    private Map map = new HashMap();

    public void put(Object key,Object value){
        synchronized(this){
            Set v = getAndCreate(key);
            v.add(value);
        }
    }

    private Set getAndCreate(Object key){
        synchronized(this){
            Set ret = (Set) map.get(key);
            if (ret == null) {
                ret = new HashSet();
                map.put(key, ret);
            }
            return ret;
        }
    }

    public Object[] get(Object key){
        synchronized(this){
            Set s = (Set) map.get(key);
            if(s!=null){
                Object[] ret = new Object[s.size()];
                s.toArray(ret);
                return ret;
            }
            return new Object[0];
        }
    }

    public void remove(Object key,Object value){
        synchronized(this){
            Set s = (Set) map.get(key);
            if(s!=null){
                s.remove(value);
            }
        }
    }

}
