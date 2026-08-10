package com.pip.itimes.server.world.toplist;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.TongDao;
import com.pip.itimes.server.world.TongData;
import com.pip.itimes.server.world.TongService;
import com.pip.itimes.server.world.WorldPlayer;

public class TongTopList extends TopList{
    private static final Logger log = Logger.getLogger(TongTopList.class);
    
    private TongService tongService;
    private TongDao tongDao;
    
    private Date lastTopListTime;
    private List<TongOnline> tongMaxOnline = new Vector<TongOnline>();
    private Map<Integer, TongOnline> allTongData = new HashMap<Integer, TongOnline>();
    
    private List<Tong> tongOnlineCache = new Vector<Tong>();
    private List<Tong> tongHotCache = new Vector<Tong>();
    
    private static final long TONG_MAKE_TOP_LIST_TIME = (long) 2 * 3600 * 1000;
    private static final long TONG_TOP_LIST_PERIOD = (long)24 * 3600 * 1000;
    private static final long TONG_TOP_LIST_SPACE = (long)1 * 3600 * 1000;
    
    public TongTopList(){
        lastTopListTime = new Date(getTodayStart() + getMakeTime());
        tongDao = new TongDao();
    }
    
    public void setTongService(TongService tongService){
        this.tongService = tongService;
    }
    
    protected long getMakeTime(){
        return TONG_MAKE_TOP_LIST_TIME;
    }
    
    protected long getLastMakeTime(){
        return lastTopListTime.getTime();
    }

    protected long getPeriod(){
        return TONG_TOP_LIST_PERIOD;
    }

    protected long getSpace(){
        return TONG_TOP_LIST_SPACE;
    }

    public void processTopList(){
        if(testTopListTime()){
            makeTopList();
        }
        
        makeMaxOnlineList();
    }

    private void makeMaxOnlineList(){
        Integer[] tongIds = tongService.getAllTongId();
        
        List<TongOnline> maxOnline = new Vector<TongOnline>();
        Map<Integer, TongOnline> allTong = new HashMap<Integer, TongOnline>();
        
        for(int i = 0; i < tongIds.length; i++){
            TongData tongData = tongService.getTongData(tongIds[i].intValue());
            
            TongOnline tongOnline = new TongOnline(tongData.getId(), tongData.getTongName(), tongData.getOnlineMax());
            
            allTong.put(tongOnline.getTongId(), tongOnline);
            maxOnline.add(tongOnline);
        }
        
        Collections.sort(maxOnline);
        
        tongMaxOnline = maxOnline;
        allTongData = allTong;
        
        log.info("Make Tong Top List MaxOnlie OK at " + new Date(System.currentTimeMillis()));
    }
    
    private void makeTopList(){
        Iterator<Integer> it = allTongData.keySet().iterator();
        
        while(it.hasNext()){
            int tongId = it.next();
            TongData tongData = tongService.getTongData(tongId);
            
            synchronized(tongData){
                tongData.saveTopList();
                tongService.saveTongData(tongData);
            }
        }
        
        lastTopListTime = new Date(System.currentTimeMillis());
        
        List<Tong> onlineTmp = new Vector<Tong>();
        List<Tong> hotTmp = new Vector<Tong>();
        
        tongOnlineCache = onlineTmp;
        tongHotCache = hotTmp;
        
        log.info("Make Yesterday Tong Top List HOT and Online OK at " + lastTopListTime);
    }
    
    public List<String> getTongTopListOnline(WorldPlayer player, int num){
        List<String> result = new Vector<String>();
        
        try{
            Tong[] onlines;
            
            if(tongOnlineCache.size() >= num){
                onlines = new Tong[tongOnlineCache.size()];
                tongOnlineCache.toArray(onlines);
            }else{
                onlines = tongDao.getTongTopListOnlineOrder(num);
                
                List<Tong> tmp = new Vector<Tong>();
                
                for(int i = 0; i < onlines.length; i++){
                    tmp.add(onlines[i]);
                }
                
                tongOnlineCache = tmp;
            }
            
            if(onlines.length > 0){
                int playerTongId = player.getTongId();
                boolean flag = true;
                
                for(int i = 0; i < onlines.length; i++){
                    if(onlines[i].getTopListOnline() == 0){
                        break;
                    }
                    
                    String tmp = "" + (i + 1) + ". " + onlines[i].getTongName() + " " + onlines[i].getTopListOnline() + "人";
                    result.add(tmp);
                    
                    if(onlines[i].getId() == playerTongId){
                        flag = false;
                    }
                }
                
                if(flag){
                    TongData myTongData = tongService.getTongData(player.getTongId());
                    
                    if(myTongData != null){
                        Tong myTong = myTongData.getTong();
                        
                        if(myTong.getTopListOnline() > 0){
                            int index = tongDao.getTopListOnlineOrder(myTong);
                            String tmp = "" + (index + 1) + ". " + myTong.getTongName() + " " + myTong.getTopListOnline() + "人";
                            result.add(tmp);
                        }
                    }
                }
            }
            
        }catch(DataAccessException e){
            log.error(e, e);
        }
        
        return result;
    }
    
    public List<String> getTongTopListHot(WorldPlayer player, int num){
        List<String> result = new Vector<String>();
        
        try{
            Tong[] hots;
            
            if(tongHotCache.size() >= num){
                hots = new Tong[tongHotCache.size()];
                tongHotCache.toArray(hots);
            }else{
                hots = tongDao.getTongTopListHotOrder(num);
                
                List<Tong> tmp = new Vector<Tong>();
                
                for(int i = 0; i < hots.length; i++){
                    tmp.add(hots[i]);
                }
                
                tongHotCache = tmp;
            }
            
            if(hots.length > 0){
                int playerTongId = player.getTongId();
                boolean flag = true;
                
                for(int i = 0; i < hots.length; i++){
                    if(hots[i].getTopListOnline() == 0){
                        break;
                    }
                    
                    String tmp = "" + (i + 1) + ". " + hots[i].getTongName() + " " + hots[i].getTopListHot() + "人";
                    result.add(tmp);
                    
                    if(hots[i].getId() == playerTongId){
                        flag = false;
                    }
                }
                
                if(flag){
                    TongData myTongData = tongService.getTongData(player.getTongId());
                    
                    if(myTongData != null){
                        Tong myTong = myTongData.getTong();
                        
                        if(myTong.getTopListOnline() > 0){
                            int index = tongDao.getTopListHotOrder(myTong);
                            String tmp = "" + (index + 1) + ". " + myTong.getTongName() + " " + myTong.getTopListHot() + "人";
                            result.add(tmp);
                        }
                    }
                }
            }
            
        }catch(DataAccessException e){
            log.error(e, e);
        }
        
        return result;
    }
    
    public List<String> getTongTopListMaxOnline(WorldPlayer player, int num){
        List<String> result = new Vector<String>();
        
        if(tongMaxOnline.size() > 0){
            int actNum = Math.min(tongMaxOnline.size(), num);
            boolean flag = true;
            int playerTongId = player.getTongId();
            
            for(int i = 0; i < actNum; i++){
                TongOnline tongOnline = tongMaxOnline.get(i);
                
                if(tongOnline.online == 0){
                    break;
                }
                
                String tmp = "" + (i + 1) + ". " + tongOnline.getTongName() + " " + tongOnline.getOnline() + "人";
                result.add(tmp);
                
                if(tongOnline.getTongId() == playerTongId){
                    flag = false;
                }
            }
            
            if(flag){
                TongOnline tongOnline = allTongData.get(playerTongId);
                
                if(tongOnline != null){
                    if(tongOnline.online > 0){
                        String tmp = "" + tongMaxOnline.indexOf(tongOnline) + "." + tongOnline.getTongName() + " " + tongOnline.getOnline() + "人";
                        result.add(tmp);
                    }
                }
            }
        }
        
        return result;
    }
    
    public class TongOnline implements Comparable<TongOnline>{
        private int tongId;
        private String tongName;
        private int online;

        public TongOnline(int tongId, String tongName, int online){
            this.tongId = tongId;
            this.tongName = tongName;
            this.online = online;
        }

        public int getTongId(){
            return tongId;
        }

        public String getTongName(){
            return tongName;
        }

        public int getOnline(){
            return online;
        }

        public int compareTo(TongOnline in){
            if(online < in.online){
                return 1;
            }else if(online > in.online){
                return -1;
            }else{
                return 0;
            }
        }
    }
}
