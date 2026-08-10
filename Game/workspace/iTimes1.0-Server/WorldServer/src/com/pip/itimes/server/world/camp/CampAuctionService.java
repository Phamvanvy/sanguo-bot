package com.pip.itimes.server.world.camp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.CampCandidate;
import com.pip.itimes.server.bean.CampQualification;
import com.pip.itimes.server.camp.CampConfig;
import com.pip.itimes.server.dao.CampAuctionDao;
import com.pip.itimes.server.dao.CampCandidateDao;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.WorldPlayer;

public class CampAuctionService implements JobProcessor{
    private static final Logger log = Logger.getLogger(CampAuctionService.class);
    
    private CampMainService campMainService;

    private CampAuctionDao campAuctionDao;
    private CampCandidateDao campCandidateDao;
    private HashMap<Integer, CampQualification> player2Qualification = new HashMap<Integer, CampQualification>();
    private TreeSet<CampQualification> darkSet = new TreeSet<CampQualification>();
    private TreeSet<CampQualification> lightSet = new TreeSet<CampQualification>();
    
    private int state;
    
    private PlayerService playerService;
    private ConnectService connectService;
    
    public static final int STATE_CLOSED = 0;
    public static final int STATE_STARTED = 1;
    
    public CampAuctionService(CampMainService campMainService, PlayerService playerService, ConnectService connectService){
        this.campMainService = campMainService;
        this.playerService = playerService;
        this.connectService = connectService;
        
        campAuctionDao = new CampAuctionDao();
        campCandidateDao = new CampCandidateDao();
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        int startWeek = CampConfig.campAuctionConfig.getStartWeek();
        int startHour = CampConfig.campAuctionConfig.getStartHour();
        int startMinute = CampConfig.campAuctionConfig.getStartMinute();
        int endWeek = CampConfig.campAuctionConfig.getEndWeek();
        int endHour = CampConfig.campAuctionConfig.getEndHour();
        int endMinute = CampConfig.campAuctionConfig.getEndMinute();

        //竞拍
        CampJob auctionJob = new CampJob(CampJob.ID_AUCTION, this, CampJob.REPEAT_TYPE_NONE);
        if(!auctionJob.init(startWeek, startHour, startMinute, endWeek, endHour, endMinute)){
        	long now = System.currentTimeMillis();
	        if(auctionJob.getState() == CampJob.STATE_WAIT_START && now > auctionJob.nextProcessTime && now < auctionJob.endTime){
	        	try{
	        		startAuction();
	        	}catch(Exception e){
	        	}
	        }
        }
        
        //选举宣传
        CampJob noticeJob = new CampJob(CampJob.ID_AUCTION_NOTICE, this, CampJob.REPEAT_TYPE_NONE);
        noticeJob.init(startWeek, startHour, startMinute - 5, startWeek, startHour, startMinute - 5);
        
        //竞拍广告
        CampJob adJob = new CampJob(CampJob.ID_AUCTION_AD, this, CampJob.REPEAT_TYPE_HOUR);
        adJob.init(startWeek, startHour, startMinute, endWeek, endHour, endMinute);

        campMainService.addJob(auctionJob);
        campMainService.addJob(noticeJob);
        campMainService.addJob(adJob);
    }

    /**
     * 处理任务开始
     */
    public void processStart(int id, long time){
        try{
            switch(id){
                case CampJob.ID_AUCTION:
                    startAuction();
                    log.info("CAMP AUTION Started");
                    
                    break;
                case CampJob.ID_AUCTION_AD:
                    campMainService.sendWorldMessage(CampConfig.campAuctionConfig.getAdMessage(), CampMainService.CAMP_NONE);
                    log.info("CAMP AUTION AD Started");
                    
                    break;
                case CampJob.ID_AUCTION_NOTICE:
                    campMainService.sendWorldMessage(CampConfig.campAuctionConfig.getNoticeMessage(), CampMainService.CAMP_NONE);
                    log.info("CAMP AUTION NOTICE Started");
                    
                    break;
            }
        }catch(Exception e){
            log.error(e, e);
        }
    }

    /**
     * 处理任务结束
     */
    public void processEnd(int id, long time){
        try{
            switch(id){
                case CampJob.ID_AUCTION:
                    stopAuction();
                    log.info("CAMP AUTION Ended");
                    
                    break;
                case CampJob.ID_AUCTION_AD:
                    log.info("CAMP AUTION AD Ended");
                    
                    break;
                case CampJob.ID_AUCTION_NOTICE:
                    log.info("CAMP AUTION NOTICE Ended");
                    
                    break;
            }
        }catch(Exception e){
            log.error(e, e);
        }
    }
    
    /**
     * 获取状态
     * @return
     */
    public int getState(){
        return state;
    }
    
    /**
     * 获取角色的竞拍记录
     * @param playerId
     * @return
     */
    public CampQualification getCampQualification(int playerId){
        return player2Qualification.get(playerId);
    }
    
    /**
     * 创建新的竞选人记录
     * @param playerId
     * @param camp
     * @return
     */
    public CampQualification getNewQualification(int playerId, int level, int camp, int credit, int remainCredit){
        CampQualification result = new CampQualification();
        result.setPlayerid(playerId);
        result.setCreatetime(new Date());
        result.setLasttime(new Date());
        result.setCamp(camp);
        result.setTotal(credit);
        result.setAdded(0);
        result.setAddcount(0);
        result.setRemain(remainCredit);
        result.setLevel(level);
        result.setValid(true);
        
        return result;
    }
    
    /**
     * 追加荣誉
     * @param campQualification
     * @param credit
     * @param remainCredit
     */
    public void addAuctionCredit(CampQualification campQualification, int credit, int remainCredit){
        int total = campQualification.getTotal();
        int add = campQualification.getAdded();
        int addCount = campQualification.getAddcount();
        
        campQualification.setTotal(total + credit);
        campQualification.setAdded(add + credit);
        campQualification.setAddcount(addCount + 1);
        campQualification.setRemain(remainCredit);
    }
    
    /**
     * 保存竞选记录
     * @param campQualification
     * @throws Exception
     */
    public void saveQualification(CampQualification campQualification) throws Exception{
        campQualification.setLasttime(new Date());

        player2Qualification.put(campQualification.getPlayerid(), campQualification);

        darkSet.clear();
        lightSet.clear();
        
        for(CampQualification tmp : player2Qualification.values()){
            if(tmp.getCamp() == CampMainService.CAMP_DARK){
                darkSet.add(tmp);
            }else{
                lightSet.add(tmp);
            }
        }
        
        campAuctionDao.makePersistent(campQualification);
    }
    
    /**
     * 取得竞选人列表，只取前5名
     * @return
     */
    public List<CampQualification> getQualificationList(int camp){
        List<CampQualification> result = new ArrayList<CampQualification>();
        TreeSet<CampQualification> tmpSet = null;
        int count = 0;
        
        if(camp == CampMainService.CAMP_DARK){
            tmpSet = darkSet;
        }else{
            tmpSet = lightSet;
        }
        
        for(CampQualification tmp : tmpSet){
            result.add(tmp);
            count++;
            
            if(count > 5){
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 清理数据
     */
    private void clear(){
        darkSet.clear();
        lightSet.clear();
        player2Qualification.clear();
    }
    
    /**
     * 竞拍初始化
     * @throws Exception
     */
    private void startAuction() throws Exception{
        clear();
        
        //读取黑暗阵营的保存数据
        List<CampQualification> listDark = campAuctionDao.getAll(CampMainService.CAMP_DARK);
        
        for(CampQualification campQualification : listDark){
            darkSet.add(campQualification);
            player2Qualification.put(campQualification.getPlayerid(), campQualification);
        }
        
        //读取光明阵营的保存数据
        List<CampQualification> listLight = campAuctionDao.getAll(CampMainService.CAMP_LIGHT);
        
        for(CampQualification campQualification : listLight){
            lightSet.add(campQualification);
            player2Qualification.put(campQualification.getPlayerid(), campQualification);
        }
        
        //设置状态
        state = STATE_STARTED;
    }

    /**
     * 竞拍结束
     */
    private void stopAuction() throws Exception{
        //设置状态
        state = STATE_CLOSED;

        //将每个阵营的前5名和上届领袖添加到候选人表中
        CampQualification[] darkArray = new CampQualification[darkSet.size()];
        darkSet.toArray(darkArray);
        CampQualification[] lightArray = new CampQualification[lightSet.size()];
        lightSet.toArray(lightArray);

        //处理黑暗阵营
        if(campMainService.getKingId(CampMainService.CAMP_DARK) >= 0){
            CampCandidate tmp = getNewCandidate(campMainService.getKingId(CampMainService.CAMP_DARK), CampMainService.CAMP_DARK);
            campCandidateDao.makePersistent(tmp);
        }
        
        for(int i = 0; i < darkArray.length; i++){
            if(i < 5){
                CampCandidate tmp = getNewCandidate(darkArray[i].getPlayerid(), CampMainService.CAMP_DARK);
                campCandidateDao.makePersistent(tmp);
                
                WorldPlayer player = playerService.getWorldPlayer(tmp.getPlayerid());
                
                if(player != null){
                    campMainService.sendPrivateMessage(CampConfig.campAuctionConfig.getSuccessMessage(), tmp.getCamp(), tmp.getPlayerid());
                }
            }else{
                //退还所有荣誉
                returnCredit(darkArray[i]);
            }
        }

        //处理光明阵营
        if(campMainService.getKingId(CampMainService.CAMP_LIGHT) >= 0){
            CampCandidate tmp = getNewCandidate(campMainService.getKingId(CampMainService.CAMP_LIGHT), CampMainService.CAMP_LIGHT);
            campCandidateDao.makePersistent(tmp);
        }
        
        for(int i = 0; i < lightArray.length; i++){
            if(i < 5){
                CampCandidate tmp = getNewCandidate(lightArray[i].getPlayerid(), CampMainService.CAMP_LIGHT);
                campCandidateDao.makePersistent(tmp);
                
                WorldPlayer player = playerService.getWorldPlayer(tmp.getPlayerid());
                
                if(player != null){
                    campMainService.sendPrivateMessage(CampConfig.campAuctionConfig.getSuccessMessage(), tmp.getCamp(), tmp.getPlayerid());
                }
            }else{
                //退还所有荣誉
                returnCredit(lightArray[i]);
            }
        }
        
        
        //将所有数据的valid修改后存盘
        for(CampQualification tmp : player2Qualification.values()){
            tmp.setValid(false);
            campAuctionDao.makePersistent(tmp);
        }
        
        //执行清理
        clear();
        
        //重新添加任务以便下周运行
        init();
    }
    
    /**
     * 退还落选人的荣誉
     * @param campQualification
     */
    private void returnCredit(CampQualification campQualification) throws Exception{
//        boolean needRelease = false;
        
//        WorldPlayer player = playerService.getWorldPlayer(campQualification.getPlayerid());

//        if(player == null){
//            player = playerService.loadWorldPlayer(campQualification.getPlayerid());
//            needRelease = true;
//        }
    	WorldPlayer player = playerService.getWorldPlayerAndCatch(campQualification.getPlayerid());
        Changed changed = new Changed();
        player.addCredit(campQualification.getTotal(), changed);

//        if(needRelease){
//            playerService.unRegistry(player);
//            playerService.savePlayer(player);
//        }else{
        if(player.online()){
            connectService.sendGetItem(changed, player.getId(),(byte) 0);
            campMainService.sendPrivateMessage("您竞拍的荣誉已经归还您了！", campQualification.getCamp(), campQualification.getPlayerid());
        }
//        }
        playerService.releasePlayer(player);
    }
    
    /**
     * 生成一个新的候选人记录
     * @param playerId
     * @param camp
     * @return
     */
    private CampCandidate getNewCandidate(int playerId, int camp){
        CampCandidate result = new CampCandidate();
        result.setPlayerid(playerId);
        result.setCreatetime(new Date());
        result.setLasttime(new Date());
        result.setCamp(camp);
        
        if(campMainService.getKingId(camp) == playerId){
            result.setPreking(true);
        }else{
            result.setPreking(false);
        }
        
        result.setTotalvote(0);
        result.setNormalvote(0);
        result.setItemvote(0);
        result.setIshopitemvote(0);
        result.setMagicvote(0);
        result.setMagicremain(0);
        result.setEggvote(0);
        result.setSlogan("选我当阵营领袖吧！谢谢大家支持！");
        result.setValid(true);
        
        return result;
    }
}
