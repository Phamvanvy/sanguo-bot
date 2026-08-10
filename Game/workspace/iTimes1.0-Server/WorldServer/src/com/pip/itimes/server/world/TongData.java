package com.pip.itimes.server.world;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.TongUser;

public class TongData{

    public Tong tong;
    private Map onlines = new TreeMap();
    private Map offlines = new TreeMap();
    private Map actives = new TreeMap();
    private int onlineMax = 0;

    public TongData(Tong tong){
        this.tong = tong;
    }

    public int getId(){
        return tong.getId();
    }

    public String getTongName(){
        return tong.getTongName();
    }

    public Tong getTong(){
        return tong;
    }

    public void setTongOwner(int id){
        tong.setOwner(id);
    }

    public synchronized void addCredit(int credit){
        tong.setCredit(tong.getCredit()+credit);
    }

    public synchronized void decCredit(int credit){
        tong.setCredit(tong.getCredit()-credit);
    }

    public synchronized void setCredit(int credit){
        tong.setCredit(credit);
    }

    public int getCredit(){
        return tong.getCredit();
    }

    public int getTongOwner(){
        return tong.getOwner();
    }

    public int getOnlineMax(){
        return onlineMax;
    }

    public void setLeastCredit(int credit){
        if(credit<0)
            tong.setLeastCredit(0);
        else
            tong.setLeastCredit(credit);
    }

    public int getLeastCredit(){
        return tong.getLeastCredit();
    }

    public void saveTopList(){
        tong.setTopListHot(actives.size());
        tong.setTopListOnline(onlineMax);

        onlineMax = onlines.size();

        actives.clear();
        Iterator<TongUser> it = onlines.values().iterator();

        while(it.hasNext()){
            TongUser user = it.next();

            actives.put(user.id, user.id);
        }
    }

    /**
     *
     * @param type int 1 在线 2 在线 离线
     * @return TongUser[]
     */
    public TongUser[] getTongMembers(int type){
        synchronized(this){
            if(type==1){
                TongUser[] ret = new TongUser[onlines.size()];
                onlines.values().toArray(ret);
                return ret;
            }
            else if(type==2){
                TongUser[] ret = new TongUser[onlines.size()+offlines.size()];
                TongUser[] ret1 = new TongUser[offlines.size()];
                onlines.values().toArray(ret);
                offlines.values().toArray(ret1);
                System.arraycopy(ret1,0,ret,onlines.size(),ret1.length);
                return ret;
            }
            return new TongUser[0];
        }
    }

    public int size(){
        return onlines.size()+offlines.size();
    }

    public void modifyPlayer(IPlayerData player){
        if(player.getTongId()==tong.getId()){
            TongUser user = getMember(player.getPlayerName());
            if(user!=null){
                user.tongDuty = player.getTongDuty();
                user.level = player.getLevel();
                user.tongTitle = player.getTongTitle();
                user.contribute = player.getContribution();
            }
        }
    }

    public void nameModified(String oldName,WorldPlayer player){
        if(player.getTongId()==tong.getId()){
            TongUser user = getMember(oldName);
            if(user!=null){
                onlines.remove(oldName);
                offlines.remove(oldName);
                user.name = player.getPlayerName();
                onlines.put(user.name,user);
            }
        }
    }

    private TongUser getMember(String name){
        TongUser ret = (TongUser)onlines.get(name);
        if(ret!=null)
            return ret;
        else
            return (TongUser)offlines.get(name);
    }

    public void addPlayer(IPlayerData player) {
        if (player.getTongId() == tong.getId()) {
            synchronized (this) {
                TongUser user = new TongUser(player.getId(),
                                             player.getPlayerName(),
                                             player.getLevel(),
                                             player.getTongDuty(),
                                             player.getTongTitle(),true,player.getContribution());
                offlines.remove(user.name);
                onlines.put(user.name,user);
                actives.put(player.getId(), player.getId());
                onlineMax = Math.max(onlineMax, onlines.size());
            }
        }
    }

    public void addPlayer(int id, String name, int level, int duty,
                          String tongTitle, boolean online,int contribute) {
        synchronized(this){
            TongUser user = new TongUser(id,name,level,duty,tongTitle,online,contribute);
            if(online){
                offlines.remove(user.name);
                onlines.put(user.name, user);
                actives.put(id, id);
                onlineMax = Math.max(onlineMax, onlines.size());
            }
            else{
                onlines.remove(user.name);
                offlines.put(user.name, user);
            }
        }
    }

    public void addPlayer(TongUser tongUser){
        synchronized(this){
            if(tongUser.online){
                offlines.remove(tongUser.name);
                onlines.put(tongUser.name, tongUser);
                actives.put(tongUser.id, tongUser.id);
                onlineMax = Math.max(onlineMax, onlines.size());
            }
            else{
                onlines.remove(tongUser.name);
                offlines.put(tongUser.name, tongUser);
            }
        }
    }


    public void removePlayer(IPlayerData player){
        synchronized(this){
            onlines.remove(player.getPlayerName());
            offlines.remove(player.getPlayerName());
        }
    }

    public void offline(PlayerData player){
        synchronized(this){
            TongUser user = (TongUser)onlines.remove(player.getPlayerName());
            if(user!=null){
                user.online = false;
                offlines.put(user.name,user);
            }
        }
    }


}

