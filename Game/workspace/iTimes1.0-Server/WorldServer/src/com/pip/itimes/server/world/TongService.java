package com.pip.itimes.server.world;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.pip.itimes.net.*;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.TongDao;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.TongUser;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.Utils;
import org.apache.log4j.Logger;
import com.pip.itimes.server.bean.TongIsland;
import java.util.concurrent.ConcurrentHashMap;
import com.pip.itimes.server.world.game.WorldService;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.dao.TongIslandDao;
import java.text.SimpleDateFormat;
import org.quartz.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TongService implements Runnable{

    private static final Logger log = Logger.getLogger(TongService.class);
    
    private TongDao tongDao;
    private TongIslandDao tongIslandDao;
    private Map id2tong = new TreeMap();
    private Map name2tong = new TreeMap();
    private Object lock = new Object();
    private PlayerService playerService;
    private StageService stageService;
    private ConnectService connectService;
    private ChatService chatService;
    private WorldService worldService;
    private BbsService bbsService;
    private Map<Integer,TongBathHouse> tongBathHouses = new HashMap<Integer,TongBathHouse>();
    private ConcurrentHashMap<Integer,TongIslandDef> defs = new ConcurrentHashMap<Integer,TongIslandDef>();
    private ConcurrentHashMap<Integer,TongIsland> tongIslands = new ConcurrentHashMap<Integer,TongIsland>();
    private Map<Integer,ArrayList<TongAuction>> auctions = new HashMap<Integer,ArrayList<TongAuction>>();
    private boolean isAuction = false;
    private Date beginAuction,endAuction,endIsland;
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    private static String cron_start = "30 29 19 ? * TUE";
//    private static String cron_start = "30 29 14 ? * WED";
//    private static String cron_start = "30 29 7 ? * THU";
    private static final boolean[][] ACTION1 = {
        {false, false, true, true, true},
        {false, false, false, true, true},
        {false, false, false, false, false},
        {false,false,false,false,false},
        {false,false,false,false,false}
    };

    private static final boolean[][] ACTION2 = {
    {false, true, true, true, false},
    {false, false, true, true, false},
    {false, false, false, false, false},
    {false,false,false,false,false},
    {false,false,false,false,false}
    };

    public TongService(TongDao tongDao,TongIslandDao tongIslandDao) {
        this.tongDao = tongDao;
        this.tongIslandDao = tongIslandDao;
        new Thread(this,"TongBathHouse").start();
        new Thread(new CheckIsland(),"CheckIsland").start();
        JobDetail jd0 = new JobDetail("island0", "islandauction", StartIslandJob.class);
        try {
            CronTrigger trigger0 = new CronTrigger("islandt0", "islandg1", "island0", "islandauction",cron_start);
            SchedulerManager.addJob(jd0, trigger0);
        } catch (Exception ex1) {
            log.error(ex1, ex1);
        }

    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }

    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }

    public void setWorldService(WorldService worldService){
        this.worldService = worldService;
    }

    public void setBbsService(BbsService bbsService){
        this.bbsService = bbsService;
    }

    public void process(ConnectSession session,UWAPData data,WorldPlayer player) throws Exception{
        byte type = data.getAppType();
        switch(type){
            case ClientConstants.REQUEST_TONG_MEMBERS:
                processTongMembers(session,data,player);
                break;
            case ClientConstants.TONG_GRANT:
                processTongGrant(session,data,player);
                break;
            case ClientConstants.TONG_MODIFY_TITLE:
                processModifyTitle(session,data,player);
                break;
        }
    }

    public void saveTongData(TongData tongData){
        try {
            tongDao.makePersistent(tongData.tong);
        } catch (DataAccessException ex) {
        }
    }


    public void addTongIslandDef(TongIslandDef def){
        defs.put(def.getId(),def);
    }

    public boolean prepareStartIslandAuction(Date prepare, Date beginAuction, Date prepareEndAuction,Date endAuction, Date endIsland) throws
            Exception {
        if (endIsland.getTime() > endAuction.getTime() && endAuction.getTime() > beginAuction.getTime() &&
            beginAuction.getTime() > prepare.getTime() && prepare.getTime() > System.currentTimeMillis()) {
            long t1 = beginAuction.getTime() - prepare.getTime();
            String msg = "岛屿目前已经无公会占领，请公会会长于" + format.format(beginAuction) + "开始到“岛屿争夺管理员”处争夺岛屿占领权！";
            JobDetail jd1 = new JobDetail("island1", "islandauction", ChatJob.class);
            jd1.getJobDataMap().put("channel", "system");
            jd1.getJobDataMap().put("message", msg);
            SimpleTrigger trigger1 = new SimpleTrigger("islandt1", "islandg1", "island1", "islandauction", prepare, null,
                    1, t1 / 2);
            SchedulerManager.addJob(jd1, trigger1);
            long t2 = endAuction.getTime() - beginAuction.getTime();
            msg = format.format(beginAuction)  +
                  "的岛屿争夺已经开始，持续约2至3小时，请各公会会长到到\"岛屿争夺管理员\"处争夺岛屿的7天占领权！";
            JobDetail jd2 = new JobDetail("island2", "islandauction", ChatJob.class);
            jd2.getJobDataMap().put("channel", "system");
            jd2.getJobDataMap().put("message", msg);
            SimpleTrigger trigger2 = new SimpleTrigger("islandt2", "islandg1", "island2", "islandauction", beginAuction, prepareEndAuction,
                    3, t2 / 4);
            SchedulerManager.addJob(jd2, trigger2);
            msg = "岛屿争夺将在60分钟范围内随时结束，请公会会长抓紧时间和机会，尽快竞价争夺！";
            long t3 = endAuction.getTime() - prepareEndAuction.getTime();
            JobDetail jd7 = new JobDetail("island7", "islandauction", ChatJob.class);
            jd7.getJobDataMap().put("channel", "system");
            jd7.getJobDataMap().put("message", msg);
            SimpleTrigger trigger7 = new SimpleTrigger("islandt7", "islandg1", "island7", "islandauction", prepareEndAuction, endAuction,
                    3, t3 / 4);
            SchedulerManager.addJob(jd7, trigger7);
            JobDetail jd3 = new JobDetail("island3", "islandauction", StartIslandAuctionJob.class);
            jd3.getJobDataMap().put("beginauction",beginAuction);
            jd3.getJobDataMap().put("endauction",endAuction);
            jd3.getJobDataMap().put("endisland",endIsland);
            SimpleTrigger trigger3 = new SimpleTrigger("islandt3", "islandg1", "island3", "islandauction", beginAuction, null,
                    0, 0);
            SchedulerManager.addJob(jd3, trigger3);
            JobDetail jd4 = new JobDetail("island4", "islandauction", EndIslandAuctionJob.class);
            SimpleTrigger trigger4 = new SimpleTrigger("islandt4", "islandg1", "island4", "islandauction", endAuction, null,
                    0, 0);
            SchedulerManager.addJob(jd4, trigger4);
            
            JobDetail jd5 = new JobDetail("island5", "islandauction", EndIslandJob.class);
            SimpleTrigger trigger5 = new SimpleTrigger("islandt5", "islandg1", "island5", "islandauction", endIsland, null,
                    0, 0);
            SchedulerManager.addJob(jd5, trigger5);
            
            log.info("Island Started");
            return true;
        } else
            return false;
    }

    public void startIslandAuction(Date beginAuction,Date endAuction,Date endIsland){
        synchronized(this){
            isAuction = true;
            this.beginAuction = beginAuction;
            this.endAuction = endAuction;
            this.endIsland = endIsland;
            auctions.clear();
            Iterator<Map.Entry<Integer,TongIslandDef>> ite = defs.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry<Integer,TongIslandDef> entry = ite.next();
                auctions.put(entry.getKey(),new ArrayList<TongAuction>());
            }
            log.info("Island Auction Started");
        }
    }

    public void endIslandAuction(){
        synchronized(this){
            if(isAuction){
                isAuction = false;
                updateNewWinners();
                auctions.clear();
                log.info("Island Auction Ended");
            }
        }
    }

    protected void updateNewWinners(){
    	int countmsg = 0;
        for(int id:auctions.keySet()){
            TongAuction ta = getCurrentAuction(id);
            if(ta!=null){
                TongIsland ti = new TongIsland();
                ti.setId(ta.getIslandId());
                ti.setBeginTime(endAuction);
                ti.setEndTime(endIsland);
                ti.setTongId(ta.getTongId());
                try {
                    tongIslandDao.save(ti);
                } catch (DataAccessException ex) {
                    log.error(ex,ex);
                }
                tongIslands.put(ti.getId(),ti);
                TongIslandDef def = getTongIslandDef(ta.getIslandId());
                String msg = ta.getTongName()+"公会通过竞价争夺获得了"+def.getName()+"岛的7天占领权！全体公会成员将享受岛屿上一切优厚待遇！";
                JobDetail jd1 = new JobDetail("island1"+countmsg, "islandauction", ChatJob.class);
                jd1.getJobDataMap().put("channel", "system");
                jd1.getJobDataMap().put("message", msg);
                SimpleTrigger trigger1 = new SimpleTrigger("islandt1"+countmsg, "islandg1", "island1"+countmsg, "islandauction",
                        TriggerUtils.getNextGivenSecondDate(null, 5), null,
                        1, (60*10*1000) / 2);
                try {
                    SchedulerManager.addJob(jd1, trigger1);
                } catch (SchedulerException ex1) {

                }
                countmsg++;
            }
        }
    }

    public void endIsland(){
        synchronized(this){
            deleteOldWinners();
        }
    }

    protected void deleteOldWinners(){
        Collection<TongIsland> l = tongIslands.values();
        for(TongIsland t:l){
            try {
                tongIslandDao.remove(t);
                TongIslandDef def = getTongIslandDef(t.getId());
                if(def!=null){
                    bbsService.deleteAllBbs(def.getBbsId());
                }
                log.info("TongID["+t.getTongId()+"]IslandEnded");
            } catch (DataAccessException ex) {
                log.error(ex,ex);
            }
        }
        tongIslands.clear();
    }


    public void setAuction(boolean isAuction){
        this.isAuction = isAuction;
    }

    public boolean isAuction(){
        return isAuction;
    }

    public void initIslands(){
        TongIslandDef[] defs = stageService.getTongIslandDefs();
        for(int i=0;i<defs.length;i++){
            addTongIslandDef(defs[i]);
        }
        try {
            TongIsland[] islands = tongIslandDao.getAll();
            long current = System.currentTimeMillis();
            Date endTime = null;
            for(int i=0;i<islands.length;i++){
                if(islands[i].getEndTime().getTime()<current){
                    tongIslandDao.remove(islands[i]);
                }else{
                	endTime = islands[i].getEndTime();
                	tongIslands.put(islands[i].getId(),islands[i]);
                	
                	TongData td = getTongData(islands[i].getTongId());
                	if (td == null){
                		Tong tong = findTong(islands[i].getTongId());
                        if (tong != null) {
                        	td = cacheTong(tong);
                        	loadTongMembers(td);
                        }
                	}
//                    tongIslands.put(islands[i].getId(),islands[i]);
//                    JobDetail jd5 = new JobDetail("island5"+i, "islandauction", EndIslandJob.class);
//                    SimpleTrigger trigger5 = new SimpleTrigger("islandt5"+i, "islandg1", "island5"+i, "islandauction",
//                            islands[i].getEndTime(), null,
//                            0, 0);
//                    try {
//                        SchedulerManager.addJob(jd5, trigger5);
//                    } catch (SchedulerException ex1) {
//                        log.error(ex1,ex1);
//                    }
                }
            }
            if(endTime!=null){
                JobDetail jd5 = new JobDetail("island5", "islandauction", EndIslandJob.class);
                SimpleTrigger trigger5 = new SimpleTrigger("islandt5", "islandg1", "island5", "islandauction",
                        endTime, null,
                        0, 0);
                try {
                    SchedulerManager.addJob(jd5, trigger5);
                } catch (SchedulerException ex1) {
                    log.error(ex1,ex1);
                }
            }
        } catch (DataAccessException ex) {
            log.error(ex,ex);
        }
    }

    public void addAuction(int tongId,int islandId,int price) throws TongException{
        synchronized(this){
            if(!isAuction)
                throw new TongException("岛屿拍卖还未开始");
            //岛屿争夺出价限制
            TongIslandDef def = defs.get(islandId);
            if (def.getId() == 1){//"布鲁岛"  500
            	if(price<500)
                    throw new TongException(def.getName() + "出价最少为500点");
            }else if (def.getId() == 2){//"新怀特岛"  800
            	if(price<800)
                    throw new TongException(def.getName() + "出价最少为800点");
            }else if (def.getId() == 3){//"海蒂岛" 200
            	if(price<200)
                    throw new TongException(def.getName() + "出价最少为200点");
            }
            
            TongData tong = getTongData(tongId);
            if(tong!=null){
                
                if(def!=null){
                    TongAuction cTa = getCurrentAuction(islandId);
                    if(tong.getCredit()<price)
                        throw new TongException("您的公会没有足够的公会荣誉点！");
                    if(cTa!=null){
                        if(tong.getCredit()<price)
                            throw new TongException("您的公会没有足够的公会荣誉点！");
                        if(cTa.getPrice()>=price)
                            throw new TongException("目前最高出价"+cTa.getPrice()+"点，请高于此次出价！");
                        int v = Math.max(cTa.getPriceDiff()/100,1);
                        int v1 = cTa.getPrice()-v;
                        TongData oldTong = getTongData(cTa.getTongId());
                        log.info("TongId["+oldTong.getId()+"]Credit["+oldTong.getCredit()+"]Deprecated");
                        oldTong.addCredit(v1);
                        log.info("TongId["+oldTong.getId()+"]Credit["+oldTong.getCredit()+"]Deprecated");
                        saveTongData(oldTong);
                        log.info("TongId["+tong.getId()+"]Credit["+tong.getCredit()+"]New");
                        tong.decCredit(price);
                        log.info("TongId["+tong.getId()+"]Credit["+tong.getCredit()+"]New");
                        saveTongData(tong);
                        TongAuction ta = new TongAuction(tong.getId(),tong.getTongName(),price,islandId,price-cTa.getPrice());
                        addAuction(ta);
                    }else{
                        log.info("TongId["+tong.getId()+"]Credit["+tong.getCredit()+"]New");
                        tong.decCredit(price);
                        log.info("TongId["+tong.getId()+"]Credit["+tong.getCredit()+"]New");
                        saveTongData(tong);
                        TongAuction ta = new TongAuction(tong.getId(),tong.getTongName(),price,islandId,price);
                        addAuction(ta);
                    }
                }
            }
        }
    }

    public TongAuction[] getTop9List(int islandId){
        synchronized(this){
            ArrayList<TongAuction> l = auctions.get(islandId);
            if(l==null){
                return new TongAuction[0];
            }
            int size = l.size();
            if(l.size()>9)
                size = 9;
            TongAuction[] ret = new TongAuction[size];
            for(int i=0;i<size;i++){
                ret[i] = l.get(l.size()-i-1);
            }
            return ret;
        }
    }

    public TongIslandDef getTongIslandDef(int islandId){
        return defs.get(islandId);
    }

    public TongIslandDef[] getTongIslandDefs(){
        Collection<TongIslandDef> c = defs.values();
        TongIslandDef[] ret = new TongIslandDef[c.size()];
        c.toArray(ret);
        return ret;
    }

    public TongIsland getTongIsland(int islandId){
        return tongIslands.get(islandId);
    }

    public Collection<TongIsland> getAllTongIsland(){
        return  tongIslands.values();
    }
    
    public boolean hasTongIsland(int tongId){
        if(tongId==-1)
            return false;
        Collection<TongIsland> c = tongIslands.values();
        for(TongIsland t:c){
            if(t.getTongId()==tongId)
                return true;
        }
        return false;
    }

    protected TongAuction getCurrentAuction(int islandId){
        ArrayList<TongAuction> l = auctions.get(islandId);
        if(l==null||l.isEmpty())
            return null;
        TongAuction ta = l.get(l.size()-1);
        return ta;
    }

    protected void addAuction(TongAuction ta){
        ArrayList<TongAuction> l = auctions.get(ta.getIslandId());
        l.add(ta);
    }


    public void sendGotoMap(int playerId, short mapId, short x, short y) {
        byte[] bytes = stageService.getTaskBytes((short) 31004,
                                                 new String[] {"" + mapId,
                                                 "" + x, "" + y});
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK);
        seg.writeShort((short) 31004);
        seg.writeShort((short) 2);
        seg.write(bytes);
        connectService.writeTo(seg, playerId);
    }

    private void processModifyTitle(ConnectSession session, UWAPData data,WorldPlayer owner) throws
            Exception {
//        int playerId = data.readInt();
        String name = data.readString();
        String title = data.readString();
        IPlayerData player = playerService.getWorldPlayerAndCatch(name);
//        if (player == null) {
//            Player p = playerService.getPlayer(name);
//            if (p != null) {
//                player = new WorldPlayer(p);
//            }
//        }
        if (owner != null && player != null) {
            if (owner.getTongId() != player.getTongId()){
            	playerService.releasePlayer((WorldPlayer)player);
                throw new ITimesException("目标人物不属于公会", data.getSerial(),
                                          data.getSessionId(),
                                          data.getAppType());
            }
            if (owner.getTongDuty() != Tong.OWNER){
            	playerService.releasePlayer((WorldPlayer)player);
                throw new ITimesException("没有此权限", data.getSerial(),
                                          data.getSessionId(),
                                          data.getAppType());
            }
            if (title.getBytes("GBK").length > 24) {
            	playerService.releasePlayer((WorldPlayer)player);
                throw new ITimesException("称号过长", data.getSerial(),
                                          data.getSessionId(),
                                          data.getAppType());
            }
            if(!Utils.checkString(title,false)){
            	playerService.releasePlayer((WorldPlayer)player);
                throw new ITimesException("称号存在非法字符",data.getSerial(),data.getSessionId(),data.getAppType());
            }
            synchronized (player) {
                player.setTongTitle(title);
                TongData tong = getTongData(player.getTongId());
                tong.modifyPlayer(player);
//                playerService.savePlayer(player);
            }
        }
        playerService.releasePlayer((WorldPlayer)player);
    }


    private void processTongGrant(ConnectSession session, UWAPData data,WorldPlayer owner) throws
            Exception {
//        int playerId = data.readInt();
        String name = data.readString();
        byte action = data.readByte();
        WorldPlayer player = playerService.getWorldPlayer(name);
        if(player==null){
            Player p = playerService.getPlayer(name);
            if(p!=null){
                player = new WorldPlayer(p);
            }
        }
        if (owner != null && player != null) {
            if (owner == player) {
                throw new ITimesException("不能对自己进行操作", data.getSerial(),
                                          data.getSessionId(), data.getAppType());
            }
            synchronized(player){
                if (!hasPrivilge(owner, action)) {
                    throw new ITimesException("没有此权限", data.getSerial(),
                                              data.getSessionId(),
                                              data.getAppType());
                }
                if (action == 1) { //提升
                    if (owner.getTongId() != player.getTongId())
                        throw new ITimesException("目标人物不属于公会", data.getSerial(),
                                                  data.getSessionId(),
                                                  data.getAppType());
                    if (owner.getTongDuty() <= player.getTongDuty())
                        throw new ITimesException("没有此权限", data.getSerial(),
                                                  data.getSessionId(),
                                                  data.getAppType());
                    if(!hasPrivilge(owner.getTongDuty(),player.getTongDuty(),action)){
                        if(owner.getTongDuty() == Tong.OWNER){
                            throw new ITimesException("无法再提升了", data.getSerial(),
                                            data.getSessionId(),
                                            data.getAppType());
                        }else{
                            throw new ITimesException("没有此权限", data.getSerial(),
                                            data.getSessionId(),
                                            data.getAppType());
                        }
                    }

                    byte duty = getNextDuty((byte) player.getTongDuty(), true);

                    if (duty == Tong.OWNER)
                        throw new ITimesException("无效提升", data.getSerial(),
                                                  data.getSessionId(),
                                                  data.getAppType());
                    player.setTongDuty(duty);
                    playerService.savePlayer(player);
                    chatService.sendTongMessage(player.getTongId(),-1,"系统",player.getPlayerName()+"被提升为"+getDutyName(duty));
                    UWAPSegment seg = new UWAPSegment(ClientConstants.TONG_GRANT_OK,data.getSerial());
                    seg.writeInt(player.getId());
                    seg.writeString(player.getTongName());
                    seg.write((byte)player.getTongDuty());
                    connectService.writeTo(seg,player.getId());
                    modifyPlayer(player);
                } else if (action == 2) { //降级
                    if (owner.getTongId() != player.getTongId())
                        throw new ITimesException("目标人物不属于公会", data.getSerial(),
                                                  data.getSessionId(),
                                                  data.getAppType());
                    if (!hasPrivilge(owner.getTongDuty(), player.getTongDuty(),
                                     action)) {
                        if(player.getTongDuty() == Tong.MUTE_MEMBER){
                            throw new ITimesException("无法再降级了", data.getSerial(),
                                            data.getSessionId(),
                                            data.getAppType());
                        }else{
                            throw new ITimesException("没有此权限", data.getSerial(),
                                            data.getSessionId(),
                                            data.getAppType());
                        }
}

//                    if (owner.getTongDuty() <= player.getTongDuty())
//                        throw new ITimesException("没有此权限", data.getSerial(),
//                                                  data.getSessionId(),
//                                                  data.getAppType());
                    byte duty = getNextDuty((byte) player.getTongDuty(), false);
                    if (duty == Tong.NONE)
                        throw new ITimesException("无效降级", data.getSerial(),
                                                  data.getSessionId(),
                                                  data.getAppType());
                    player.setTongDuty(duty);
                    playerService.savePlayer(player);
                    chatService.sendTongMessage(player.getTongId(),-1,"系统",player.getPlayerName()+"被降级为"+getDutyName(duty));
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            TONG_GRANT_OK, data.getSerial());
                    seg.writeInt(player.getId());
                    seg.writeString(player.getTongName());
                    seg.write((byte) player.getTongDuty());
                    connectService.writeTo(seg, player.getId());
                    modifyPlayer(player);
                } else if (action == 3) { //踢出公会
                    if (owner.getTongId() != player.getTongId())
                        throw new ITimesException("目标人物不属于公会", data.getSerial(),
                                                  data.getSessionId(),
                                                  data.getAppType());
                    if (owner.getTongDuty() <= player.getTongDuty())
                        throw new ITimesException("没有此权限", data.getSerial(),
                                                  data.getSessionId(),
                                                  data.getAppType());
                    int tongId = player.getTongId();
                    kickPlayer(player);
                    chatService.sendTongMessage(tongId,-1,"系统",player.getPlayerName()+"离开了公会("+owner.getPlayerName()+")");
                    chatService.sendPrivateMessage(-1,"系统",player.getId(),"你已经离开了公会("+owner.getPlayerName()+")");
                    UWAPSegment seg = new UWAPSegment(ClientConstants.TONG_GRANT_OK,data.getSerial());
                    seg.writeInt(player.getId());
                    seg.writeString(player.getTongName());
                    seg.write((byte)player.getTongDuty());
                    connectService.writeTo(seg,owner.getId());
                } else if (action == 4) { //邀请
                    if(player.getTongId()!=-1){
                        if (player.getTongId() == owner.getTongId()) {
                            throw new ITimesException("目标已经是公会成员",
                                    data.getSerial(), data.getSessionId(),
                                    data.getAppType());
                        } else {
                            throw new ITimesException("目标已经有所属公会",
                                    data.getSerial(), data.getSessionId(),
                                    data.getAppType());
                        }
                    }else{
                        TongData tong = getTongData(owner.getTongId());
                        if(tong.size()>=200){ //规模不能超过200人
                            chatService.sendPrivateMessage(-1,"系统",owner.getId(),"公会总人数已达到上限，不能继续增加公会成员。");
                            return;
                        }
                        if(player.offlinemode()){
                            throw new ITimesException("目标处于省流量模式",
                                    data.getSerial(), data.getSessionId(),
                                    data.getAppType());
                        }
                        byte[] bytes = stageService.getTaskBytes((short) 31002,
                                                                 new String[] {owner.getPlayerName() +
                                                                 "邀请你加入{" + owner.getTongName() +
                                                                 "}\n1.是\n2.否",
                                                                 "tong_join " + owner.getTongId()});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                          GET_FILE_OK, data.getSerial());
                        seg.writeShort((short) 31002);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                    }
                } else if (action == 5) { //提升为会长
                    byte[] bytes = stageService.getTaskBytes((short) 31002,
                            new String[] {"是否转让会长?\n1.是\n2.否",
                            "change_tong_owner "+ player.getId()});
                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                            GET_FILE_OK, data.getSerial(),
                            data.getSessionId());
                    seg.writeShort((short) 31002);
                    seg.writeShort((short) 2);
                    seg.write(bytes);
                    connectService.writeTo(seg,owner.getId());
//                    if (owner.getTongId() != player.getTongId())
//                        throw new ITimesException("目标人物不属于公会", data.getSerial(),
//                                                  data.getSessionId(),
//                                                  data.getAppType());
//                    player.setTongDuty(Tong.OWNER);
//                    owner.setTongDuty(Tong.MEMBER);
//                    if("会长".equals(owner.getTongTitle())){
//                        owner.setTongTitle("曾经的会长");
//                    }
//                    playerService.savePlayer(player);
//                    playerService.savePlayer(owner);
//                    TongData tongData = getTongData(player.getTongId());
//                    tongData.setTongOwner(player.getId());
//                    saveTongData(tongData);
//                    chatService.sendTongMessage(player.getTongId(),-1,"系统",player.getPlayerName()+"成为会长");
//                    UWAPSegment seg = new UWAPSegment(ClientConstants.TONG_GRANT_OK,data.getSerial());
//                    seg.writeInt(player.getId());
//                    seg.writeString(player.getTongName());
//                    seg.write((byte)player.getTongDuty());
//                    connectService.writeTo(seg,player.getId());
//                    seg = new UWAPSegment(ClientConstants.TONG_GRANT_OK,data.getSerial());
//                    seg.writeInt(owner.getId());
//                    seg.writeString(owner.getTongName());
//                    seg.write((byte)owner.getTongDuty());
//                    connectService.writeTo(seg,owner.getId());
//                    modifyPlayer(player);
//                    modifyPlayer(owner);
                }
            }
        }
    }

//    private void sendGrantOk(PlayerData player,int serial,int sessionId){
//        UWAPSegment seg = new UWAPSegment(ClientConstants.TONG_GRANT_OK,serial,sessionId);
//        seg.writeInt(player.getId());
//        seg.write
//    }

    public void changeOwner(WorldPlayer owner,int playerId,int serial) throws TongException{
    	WorldPlayer player = playerService.getWorldPlayerAndCatch(playerId);
        if (owner != null && player != null) {
            if (owner.getTongDuty() != Tong.OWNER){
            	playerService.releasePlayer(player);
                throw new TongException("没有权限");
            }
            if (owner.getTongId() != player.getTongId()){
            	playerService.releasePlayer(player);            	
            	throw new TongException("目标人物不属于公会");
            }
            player.setTongDuty(Tong.OWNER);
            owner.setTongDuty(Tong.MEMBER);
            if ("会长".equals(owner.getTongTitle())) {
                owner.setTongTitle("");
            }
            playerService.savePlayer(player);
            playerService.savePlayer(owner);
            TongData tongData = getTongData(player.getTongId());
            tongData.setTongOwner(player.getId());
            saveTongData(tongData);
            chatService.sendTongMessage(player.getTongId(), -1, "系统",
                                        player.getPlayerName() + "成为会长");
            UWAPSegment seg = new UWAPSegment(ClientConstants.TONG_GRANT_OK,
                                              serial);
            seg.writeInt(player.getId());
            seg.writeString(player.getTongName());
            seg.write((byte) player.getTongDuty());
            connectService.writeTo(seg, player.getId());
            seg = new UWAPSegment(ClientConstants.TONG_GRANT_OK, serial);
            seg.writeInt(owner.getId());
            seg.writeString(owner.getTongName());
            seg.write((byte) owner.getTongDuty());
            connectService.writeTo(seg, owner.getId());
            modifyPlayer(player);
            modifyPlayer(owner);
            log.info("ID["+owner.getId()+"]GrantTongOwner Dest["+player.getId()+"]Tong["+tongData.getId()+"]");
        }
        playerService.releasePlayer(player);
    }

     public void kickPlayer(IPlayerData player){
         int tongId = player.getTongId();
         player.setTongDuty(-1);
         player.setTongId(-1);
         player.setTongName("");
         player.setTongTitle("");
         player.setContribution(0);
         playerService.savePlayer(player);
         TongData tongData = getTongData(tongId);
         tongData.removePlayer(player);
     }

     public String getDutyName(byte duty){
         if(duty == Tong.OWNER)
             return "会长";
         else if(duty == Tong.VICE_OWNER)
             return "副会长";
         else if(duty == Tong.ADVANCED_MEMEBER)
             return "精英";
         else if(duty == Tong.MEMBER)
             return "会员";
         else if(duty == Tong.MUTE_MEMBER)
             return "禁闭中";
         return "";
     }

     /**
      *
      * @param duty byte
      * @param b boolean true 升级 false 降级
      * @return byte
      */
     public byte getNextDuty(byte duty,boolean b){
         if(b){
             if(duty == Tong.OWNER)
                 return Tong.OWNER;
             else if(duty == Tong.VICE_OWNER)
                 return Tong.OWNER;
             else if(duty == Tong.MUTE_MEMBER)
                 return Tong.MEMBER;
             else if(duty == Tong.ADVANCED_MEMEBER)
                 return Tong.VICE_OWNER;
             else if(duty == Tong.MEMBER)
                 return Tong.ADVANCED_MEMEBER;
         }else{
             if(duty == Tong.OWNER)
                 return Tong.VICE_OWNER;
             else if(duty == Tong.VICE_OWNER)
                 return Tong.ADVANCED_MEMEBER;
             else if(duty == Tong.MUTE_MEMBER)
                 return Tong.ADVANCED_MEMEBER;
             else if(duty == Tong.ADVANCED_MEMEBER)
                 return Tong.MEMBER;
             else if(duty == Tong.MEMBER)
                 return Tong.MUTE_MEMBER;
             else if(duty == Tong.MUTE_MEMBER)
                 return Tong.NONE;
         }
         return Tong.NONE;
     }

     /**
      *
      * @param ownerDuty int
      * @param destDuty int
      * @param action int 1 提升 2 降级
      * @return boolean
      */
     public boolean hasPrivilge(int ownerDuty,int destDuty,int action){
         int index1 = getDutyIndex(ownerDuty);
         int index2 = getDutyIndex(destDuty);
         if(action==1){
             return ACTION1[index1][index2];
         }
         if(action==2){
             return ACTION2[index1][index2];
         }
         return false;
     }

     public int getDutyIndex(int duty){
         if(duty==Tong.OWNER)
             return 0;
         if(duty==Tong.VICE_OWNER)
             return 1;
         if(duty==Tong.ADVANCED_MEMEBER)
             return 2;
         if(duty==Tong.MEMBER)
             return 3;
         if(duty==Tong.MUTE_MEMBER)
             return 4;
         return -1;
     }

    /**
     *
     * @param player PlayerData
     * @param priv int 1 提升 2 降级 3 踢出公会 4 邀请 5 提升为会长
     * @return boolean
     */
    public boolean hasPrivilge(PlayerData player,int priv){
        if(priv==5){
            return player.getTongDuty()==Tong.OWNER;
        }
        if(player.getTongDuty()>Tong.MEMBER)
            return true;
        return false;
    }

    private void processTongMembers(ConnectSession session, UWAPData data,PlayerData player) throws
            Exception {
//        int playerId = data.readInt();
        int pageNo = data.readInt();
        byte type = data.readByte();
        if (player != null) {
            if (player.getTongId() == -1) {
                throw new ITimesException("没有所属公会", data.getSerial(), data.getSessionId(),
                                      data.getAppType());
            }
            TongUser[] members = getTongMembers(player.getTongId(),type);
            int start = pageNo*50;
            int pageCount = members.length/50;
            if(members.length%50!=0)
                pageCount++;
            if(start>=members.length)
                throw new ITimesException("没有可显示会员", data.getSerial(), data.getSessionId(),
                                      data.getAppType());
            int leavings = members.length-start;
            int count = Math.min(leavings,50);
            TongData tong = getTongData(player.getTongId());
            UWAPSegment seg = new UWAPSegment(ClientConstants.TONG_MEMBERS,data.getSerial(),data.getSessionId());
            seg.writeInt(tong.getCredit());
            seg.writeInt(pageNo);
            seg.writeInt(pageCount);
            seg.writeShort((short)count);
            for(int i=0;i<count;i++){
                TongUser user = members[start+i];
                seg.writeInt(user.id);
                seg.writeString(user.name);
                seg.write((byte)user.tongDuty);
                seg.writeShort((short)user.level);
                seg.writeString(user.tongTitle);
                seg.writeBoolean(user.online);
                seg.writeInt(user.contribute);
            }
            session.write(seg);
        }
    }

    public void registry(IPlayerData player){
        int tongId = player.getTongId();
        if(tongId==-1)
            return;
        synchronized(lock){
            TongData tongData = getTongData(tongId);
            if (tongData != null) {
                tongData.addPlayer(player);
            } else {
                Tong tong = findTong(tongId);
                if (tong != null) {
                    tongData = cacheTong(tong);
                }
                loadTongMembers(tongData);
            }
        }
    }

    public void unRegistry(PlayerData player){
        int tongId = player.getTongId();
        if(tongId==-1)
            return;
        TongData tongData = getTongData(tongId);
        if(tongData != null){
            tongData.offline(player);
        }
    }

    private void loadTongMembers(TongData tongData){

        TongUser[] members = tongDao.getTongMembers(tongData.getId());
        for (int i = 0; i < members.length; i++) {
        	IPlayerData player = playerService.getWorldPlayer(members[i].id);
            if (player != null) {
                tongData.addPlayer(player);
            } else {
                tongData.addPlayer(members[i]);
            }
        }

    }

    /**
     *
     * @param tongId int
     * @param type int 1 在线 2 所有
     * @return int
     */
    public TongUser[] getTongMembers(int tongId,int type){
        if(tongId==-1)
            return new TongUser[0];
        TongData tongData = getTongData(tongId);
        if(tongData!=null){
            return tongData.getTongMembers(type);
        }
        return new TongUser[0];
    }

    public int getCreateTongMoney(){
        return 30000;
    }

    public int getCreateTongLevel(){
        return 20;
    }

    public Tong creatTong(IPlayerData player,String name) throws TongException{
        synchronized(player){
            if (player.getTongId() != -1)
                throw new TongException("已经有所属公会");
            if (player.getLevel() < getCreateTongLevel())
                throw new TongException("只有达到20级才能公会");
            if (player.getMoeny() < getCreateTongMoney())
                throw new TongException("建公会需要30000的资金");
            if(KeywordsUtil.isInvalidName(name.toLowerCase()) || !KeywordsUtil.isLegitimate(name))
                throw new TongException("公会名出现非法字符");
            if(!Utils.checkString(name,false))
                throw new TongException("公会名出现非法字符");
            if (name == null || name.length() == 0)
                throw new TongException("公会名字错误");
            try {
                if (name.getBytes("GBK").length > 12) {
                    throw new TongException("名字太长");
                }
            }  catch (UnsupportedEncodingException ex1) {
            }
            boolean hasTongName = false;
            try{
            	Tong oldTong = tongDao.getTongByName(name);
            	if(oldTong != null){
            		hasTongName = true;
            	}
            }catch(Exception e){
            	log.info(e, e);
            }
            
            if(hasTongName){
            	throw new TongException("存在相同公会名称");
            }
            
            Tong tong = new Tong();
            tong.setTongName(name);
            tong.setCreateTime(new Date());
            tong.setHealth(0);
            tong.setMoney(30000);
            tong.setResource(0);
            tong.setSlogan("");
            tong.setLevel(1);
            tong.setOwner(player.getId());
            tong.setCredit(0);
            tong.setLastRepairTime(new Date());
            tong.setTopListHot(1);
            tong.setTopListOnline(1);
            tong.setValid(true);
            try {
                tongDao.addTong(tong);
                player.setMoeny(player.getMoeny() - 30000);
                player.setTongId(tong.getId());
                player.setTongName(tong.getTongName());
                player.setTongDuty(Tong.OWNER);
                player.setTongTitle("会长");
                cacheTong(tong);
                addTongPlayer(tong,player);
                playerService.savePlayer(player);
                return tong;
            } catch (DataAccessException ex) {
                throw new TongException("建立公会错误");
            }
        }
    }

    public void modifyPlayer(IPlayerData player){
        TongData tongData = getTongData(player.getTongId());
        if(tongData!=null){
            tongData.modifyPlayer(player);
        }
    }

    public void nameModified(String oldName,WorldPlayer player){
        TongData tongData = getTongData(player.getTongId());
        if(tongData!=null){
            tongData.nameModified(oldName,player);
        }
    }

    public void join(IPlayerData player,int tongId) throws TongException{
        synchronized(player){
            if(player.getTongId()!=-1)
                throw new TongException("已经有所属公会");
            TongData tongData = getTongData(tongId);
            if (tongData == null)
                throw new TongException("没有找到此公会");
            player.setTongId(tongData.getId());
            player.setTongName(tongData.getTongName());
            player.setTongDuty(Tong.MEMBER);
            player.setTongTitle("");
            player.setContribution(0);
//            playerService.savePlayer(player);
            tongData.addPlayer(player);
        }
    }

    public void quit(IPlayerData player) throws TongException{
        synchronized(player){
            if(player.getTongId()==-1)
                return;
            byte type = 2;
            TongUser[] members = getTongMembers(player.getTongId(),type);
            if(members.length > 1 && player.getTongDuty()==Tong.OWNER){
        		throw new TongException("会长只能在没有会员或将会长转给别人的情况下才能退出公会");
            }
            boolean owner = false;
            if(player.getTongDuty() == Tong.OWNER){
            	owner = true;
            }
            int tongId = player.getTongId();
            player.setTongId(-1);
            player.setTongDuty(-1);
            player.setTongName("");
            player.setTongTitle("");
            player.setContribution(0);
//            playerService.savePlayer(player);
            TongData tongData = getTongData(tongId);
            if(tongData!=null)
                tongData.removePlayer(player);
            if(owner){
            	tongData.tong.setValid(false);
            	try{
            		tongDao.makeTransient(tongData.tong);
            	}catch(Exception e){
            		log.info(e, e);
            	}
            }
        }
    }

    private TongData cacheTong(Tong tong){
        TongData tongData = new TongData(tong);
        id2tong.put(new Integer(tong.getId()),tongData);
        name2tong.put(tong.getTongName(),tongData);
        return tongData;
    }

    private void addTongPlayer(Tong tong,IPlayerData player){
        TongData tongData = getTongData(tong.getId());
        tongData.addPlayer(player);
    }

    public TongData getTongData(int id){
        return (TongData)id2tong.get(new Integer(id));
    }

    public String getTongName(int id){
        return getTongData(id).getTongName();
    }

    public Tong findTong(int id) {
        try {
            return (Tong) tongDao.getObject(Tong.class, new Integer(id));
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public int getTongOwnerId(int id){
        Tong tong = findTong(id);
        if(tong==null)
            return -1;
        return tong.getOwner();
    }

    public Integer[] getAllTongId(){
        Integer[] ids = new Integer[id2tong.size()];

        id2tong.keySet().toArray(ids);

        return ids;
    }

    public String getTongCreditOrderInfo(WorldPlayer player){
        try {
            TongData tongData = getTongData(player.getTongId());
            Tong[] tong = tongDao.getTongOrder();
            int order = tongDao.getOrder(tongData.getTong());
            StringBuilder sb = new StringBuilder(500);
            for (int i = 0; i < tong.length; i++) {
                sb.append(i + 1);
                sb.append(".");
                sb.append(tong[i].getTongName());
                sb.append("  荣誉:");
                sb.append(tong[i].getCredit());
                sb.append(".\n");
            }
            sb.append("你所在公会的排名:");
            sb.append(order+1);
            sb.append("  荣誉:");
            sb.append(tongData.getCredit());
            return sb.toString();
        } catch (DataAccessException ex) {
            return "查询错误";
        }
    }

    public void run(){
        while(true){
            try {
                Thread.sleep(60*1000L);
            } catch (InterruptedException ex) {
            }
            checkBathHouses();
        }
    }

    public void checkBathHouses(){
        synchronized(tongBathHouses){
            long current = System.currentTimeMillis();
            Iterator<TongBathHouse> ite = tongBathHouses.values().iterator();
            while(ite.hasNext()){
                TongBathHouse th = ite.next();
                if((current-th.time.getTime())>=th.bathHouse.getTime()){
                    playerService.addTongBathHouseExp(th.tongId,th.bathHouse);
                    ite.remove();
                }
            }
        }
    }

    public  void addTongBathHouseRequest(WorldPlayer player,BathHouse bath) throws TongException{
        synchronized(tongBathHouses){
            TongData tongData = getTongData(player.getTongId());
            if (tongData == null || tongData.getTongOwner() != player.getId())
                throw new TongException("你必须是某公会的会长才能开启此功能!");
            if (tongBathHouses.containsKey(player.getTongId()))
                throw new TongException("你已经开启了公会浴");
            if (tongData.getCredit() < 500)
                throw new TongException("公会荣誉点数不够,请去公会战场获得荣誉点数吧!");
            tongData.setCredit(tongData.getCredit() - 500);
            saveTongData(tongData);
            TongBathHouse request = new TongBathHouse(tongData.getId(), new Date(),bath);
            tongBathHouses.put(request.tongId, request);
        }
    }

    class TongBathHouse{
        public Date time;
        public int tongId;
        public BathHouse bathHouse;
        public TongBathHouse(int tongId,Date time,BathHouse bathHouse){
            this.tongId = tongId;
            this.time = time;
            this.bathHouse = bathHouse;
        }
    }

    class CheckIsland implements Runnable{
        public void run(){
            while(true){
                try {
                    for (TongIslandDef def : defs.values()) {
                        TongIsland island = tongIslands.get(def.getId());
                        check(def, island);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                try {
                    Thread.sleep(60 * 1000L);
                } catch (InterruptedException ex1) {
                }
            }
        }

        private void check(TongIslandDef def,TongIsland island){
            int tongId = -1;
            if(island!=null)
                tongId = island.getTongId();
            TongData td = getTongData(tongId);
            short[] maps = def.getMapIds();
            for(int i=0;i<maps.length;i++){
                GameMap map = worldService.getNoInstanceMap(maps[i]);
                if(map!=null){
                    WorldPlayer[] players = map.getPlayers();
                    for(int j=0;j<players.length;j++){
                    	//只有Player真的在本地图的时候 才会进行传送
                    	if(players[j].getMapId() == map.getMapId()){
                    		if(tongId==-1||players[j].getTongId()!=tongId){
                    			sendGotoMap(players[j].getId(),(short)353,(short)4,(short)41);
                    			chatService.sendPrivateMessage(-1,"系统",players[j].getId(),"岛屿占领已经过期，你将被传送离开此岛。");
                    		}else{
                    			if(td!=null&&(td.getLeastCredit()>players[j].getContribution())){
                    				sendGotoMap(players[j].getId(),(short)353,(short)4,(short)41);
                    				chatService.sendPrivateMessage(-1,"系统",players[j].getId(),"没有达到公会最低贡献度要求，即将被传送出此岛屿。");
                    			}
                    		}
                    	}else{
                    		//将角色移除本地图
                    		map.removePlayer(players[j], true);
                    		log.info("Tong check playerID[" + players[j].getId() + "] is in Tong map error TongMapID[" + map.getMapId() + "] player mapID[" + players[j].getMapId() + "]");
                    	}
                    }
                }
            }
        }
    }
}

