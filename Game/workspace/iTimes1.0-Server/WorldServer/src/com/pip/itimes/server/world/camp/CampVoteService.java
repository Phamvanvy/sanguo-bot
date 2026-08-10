package com.pip.itimes.server.world.camp;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.CampCandidate;
import com.pip.itimes.server.bean.CampVote;
import com.pip.itimes.server.camp.CampConfig;
import com.pip.itimes.server.dao.CampCandidateDao;
import com.pip.itimes.server.dao.CampVoteDao;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.WorldPlayer;

public class CampVoteService implements JobProcessor{
    private static final Logger log = Logger.getLogger(CampVoteService.class);

    private CampMainService campMainService;
    private MailService mailService;
    private CampCandidateDao campCandidateDao;
    private CampVoteDao campVoteDao;
    private HashMap<Integer, CampCandidate> player2Candidate = new HashMap<Integer, CampCandidate>();
    private HashMap<Integer, CampVote> player2Vote = new HashMap<Integer, CampVote>();
    private TreeSet<CampCandidate> darkSet = new TreeSet<CampCandidate>();
    private TreeSet<CampCandidate> lightSet = new TreeSet<CampCandidate>();

    private int state;

    public static final int STATE_CLOSED = 0;
    public static final int STATE_STARTED = 1;
    
    public static String darkKingname = "";
    public static String lightKingname ="";

    public CampVoteService(CampMainService campMainService, MailService mailService){
        this.campMainService = campMainService;
        campCandidateDao = new CampCandidateDao();
        campVoteDao = new CampVoteDao();
        this.mailService = mailService;
        init();
        WorldPlayer darkKingplayer = this.campMainService.getPlayerService().getWorldPlayerAndCatch(campMainService.getKingId(CampMainService.CAMP_DARK));
        if(darkKingplayer == null){
        	darkKingname = "";
        }else{
        	darkKingname = darkKingplayer.getPlayerName();
        }
        this.campMainService.getPlayerService().releasePlayer(darkKingplayer);
        WorldPlayer  lightKingplayer = this.campMainService.getPlayerService().getWorldPlayerAndCatch(campMainService.getKingId(CampMainService.CAMP_LIGHT));
        if(lightKingplayer == null){
        	lightKingname = "";
        }else{
        	lightKingname = lightKingplayer.getPlayerName();
        }
        this.campMainService.getPlayerService().releasePlayer(lightKingplayer);
    }

    /**
     * 初始化
     */
    private void init(){
        int startWeek = CampConfig.campVoteConfig.getStartWeek();
        int startHour = CampConfig.campVoteConfig.getStartHour();
        int startMinute = CampConfig.campVoteConfig.getStartMinute();
        int endWeek = CampConfig.campVoteConfig.getEndWeek();
        int endHour = CampConfig.campVoteConfig.getEndHour();
        int endMinute = CampConfig.campVoteConfig.getEndMinute();

        //投票
        CampJob voteJob = new CampJob(CampJob.ID_VOTE, this, CampJob.REPEAT_TYPE_NONE);
        if(!voteJob.init(startWeek, startHour, startMinute, endWeek, endHour, endMinute)){
	        long now = System.currentTimeMillis();
	        if(voteJob.getState() == CampJob.STATE_WAIT_START && now > voteJob.nextProcessTime && now < voteJob.endTime){
	        	try{
	        		startVote();
	        	}catch(Exception e){
	        	}
	        }
        }

        //投票广告
        CampJob adJob = new CampJob(CampJob.ID_VOTE_AD, this, CampJob.REPEAT_TYPE_HOUR);
        adJob.init(startWeek, startHour, startMinute, endWeek, endHour, endMinute);

        campMainService.addJob(voteJob);
        campMainService.addJob(adJob);
    }

    /**
     * 处理任务开始
     */
    public void processStart(int id, long time){
        try{
            switch(id){
                case CampJob.ID_VOTE:
                    startVote();
                    log.info("CAMP VOTE Started");

                    break;
                case CampJob.ID_VOTE_AD:
                    campMainService.sendWorldMessage(CampConfig.campVoteConfig.getAdMessage(), CampMainService.CAMP_NONE);
                    log.info("CAMP VOTE AD Started");

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
                case CampJob.ID_VOTE:
                    stopVote();
                    log.info("CAMP VOTE Ended");

                    break;
                case CampJob.ID_VOTE_AD:
                    log.info("CAMP VOTE AD Ended");

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
     * 获取候选人数据
     * @param playerId
     * @return
     */
    public CampCandidate getCandidate(int playerId){
        return player2Candidate.get(playerId);
    }

    /**
     * 保存候选人数据
     * @param campCandidate
     */
    public void saveCandidate(CampCandidate campCandidate) throws Exception{
        campCandidate.setLasttime(new Date());
        player2Candidate.put(campCandidate.getPlayerid(), campCandidate);

        darkSet.clear();
        lightSet.clear();
        
        for(CampCandidate tmp : player2Candidate.values()){
            if(tmp.getCamp() == CampMainService.CAMP_DARK){
                darkSet.add(tmp);
            }else{
                lightSet.add(tmp);
            }
        }

        campCandidateDao.makePersistent(campCandidate);
    }

    /**
     * 获取投票人数据，如果不存在则创建新
     * @param playerId
     * @return
     */
    public CampVote getAndCreateVote(int playerId, int camp){
        CampVote result = player2Vote.get(playerId);

        if(result == null){
            result = new CampVote();
            result.setPlayerid(playerId);
            result.setCamp(camp);
            result.setCreatetime(new Date());
            result.setLasttime(new Date());
            result.setTotalvote(0);
            result.setNormalvote(0);
            result.setItemvote(0);
            result.setIshopitemvote(0);
            result.setMagicvote(0);
            result.setEggvote(0);
            result.setValid(true);
        }

        return result;
    }

    /**
     * 保存投票人数据
     * @param campVote
     * @throws Exception
     */
    public void saveVote(CampVote campVote) throws Exception{
        campVote.setLasttime(new Date());
        player2Vote.put(campVote.getPlayerid(), campVote);
        campVoteDao.makePersistent(campVote);
    }

    /**
     * 投入魔力分享
     * @param campCandidate
     * @param magicCount
     */
    public void processMagic(CampCandidate campCandidate, int magicCount){
        campCandidate.setMagicremain(campCandidate.getMagicremain() + magicCount * CampMainService.VOTE_COUNT_MAGIC);
    }

    /**
     * 尝试用魔力分享投票，失败的话就进行真心支持
     * @param campCandidate
     * @param campVote
     * @return
     */
    public boolean tryNormalVoteWithMagic(CampCandidate campCandidate, CampVote campVote){
        boolean result = false;

        if(campCandidate.getMagicremain() >= CampMainService.VOTE_COUNT_NORMAL){
            result = true;

            campCandidate.setMagicremain(campCandidate.getMagicremain() - CampMainService.VOTE_COUNT_NORMAL);
            campCandidate.setMagicvote(campCandidate.getMagicvote() + CampMainService.VOTE_COUNT_NORMAL);
            campCandidate.setTotalvote(campCandidate.getTotalvote() + CampMainService.VOTE_COUNT_NORMAL);
            campVote.setMagicvote(campVote.getMagicvote() + CampMainService.VOTE_COUNT_NORMAL);
            campVote.setTotalvote(campVote.getTotalvote() + CampMainService.VOTE_COUNT_NORMAL);
        }else{
            campCandidate.setNormalvote(campCandidate.getNormalvote() + CampMainService.VOTE_COUNT_NORMAL);
            campCandidate.setTotalvote(campCandidate.getTotalvote() + CampMainService.VOTE_COUNT_NORMAL);
            campVote.setNormalvote(campVote.getNormalvote() + CampMainService.VOTE_COUNT_NORMAL);
            campVote.setTotalvote(campVote.getTotalvote() + CampMainService.VOTE_COUNT_NORMAL);
        }

        return result;
    }
    
    public boolean checkVoteWithMagic(CampCandidate campCandidate){
    	if(campCandidate.getMagicremain() >= CampMainService.VOTE_COUNT_NORMAL){
            return true;
    	}
    	return false;
    }
    
    public void useVoteWithMagic(CampCandidate campCandidate){
    	campCandidate.setMagicremain(campCandidate.getMagicremain() - CampMainService.VOTE_COUNT_NORMAL);
    }

    /**
     * 尝试用物品投票，鲜花，蓝色妖姬，臭鸡蛋
     * @param campCandidate
     * @param campVote
     * @param itemId
     * @param count
     */
    public void tryVoteByItem(CampCandidate campCandidate, CampVote campVote, int itemId, int count){
        switch(itemId){
            case CampMainService.VOTE_ITEM_ID:
                campCandidate.setItemvote(campCandidate.getItemvote() + CampMainService.VOTE_COUNT_ITEM);
                campCandidate.setTotalvote(campCandidate.getTotalvote() + CampMainService.VOTE_COUNT_ITEM);
                campVote.setItemvote(campVote.getItemvote() + CampMainService.VOTE_COUNT_ITEM);
                campVote.setTotalvote(campVote.getTotalvote() + CampMainService.VOTE_COUNT_ITEM);

                break;
            case CampMainService.VOTE_ISHOP_ITEM_ID:
            	int addcount = CampMainService.VOTE_COUNT_ISHOP_ITEM * count;
                campCandidate.setIshopitemvote(campCandidate.getIshopitemvote() + addcount);
                campCandidate.setTotalvote(campCandidate.getTotalvote() + addcount);
                campVote.setIshopitemvote(campVote.getIshopitemvote() + addcount);
                campVote.setTotalvote(campVote.getTotalvote() + addcount);
                break;
            case CampMainService.VOTE_EGG_ITEM_ID:
                campCandidate.setEggvote(campCandidate.getEggvote() + CampMainService.VOTE_COUNT_EGG);
                campCandidate.setTotalvote(campCandidate.getTotalvote() - CampMainService.VOTE_COUNT_EGG);
                campVote.setEggvote(campVote.getEggvote() + CampMainService.VOTE_COUNT_EGG);
                campVote.setTotalvote(campVote.getTotalvote() - CampMainService.VOTE_COUNT_EGG);

                break;
        }
    }

    /**
     * 设置竞选宣言
     * @param campCandidate
     * @param slogan
     */
    public void setSlogan(CampCandidate campCandidate, String slogan){
        campCandidate.setSlogan(slogan);
    }

    /**
     * 获取候选人列表
     * @param camp
     * @return
     */
    public CampCandidate[] getCandidateList(int camp){
        CampCandidate[] result = null;

        if(camp == CampMainService.CAMP_DARK){
            result = new CampCandidate[darkSet.size()];
            darkSet.toArray(result);
        }else{
            result = new CampCandidate[lightSet.size()];
            lightSet.toArray(result);
        }

        return result;
    }

    /**
     * 清理数据
     */
    private void clear(){
        darkSet.clear();
        lightSet.clear();
        player2Vote.clear();
        player2Candidate.clear();
    }

    /**
     * 投票初始化
     * @throws Exception
     */
    private void startVote() throws Exception{
        clear();

        //读取黑暗阵营的保存数据
        List<CampCandidate> listDark = campCandidateDao.getAll(CampMainService.CAMP_DARK);

        for(CampCandidate campCandidate : listDark){
            darkSet.add(campCandidate);
            player2Candidate.put(campCandidate.getPlayerid(), campCandidate);
        }

        //读取光明阵营的保存数据
        List<CampCandidate> listLight = campCandidateDao.getAll(CampMainService.CAMP_LIGHT);

        for(CampCandidate campCandidate : listLight){
            lightSet.add(campCandidate);
            player2Candidate.put(campCandidate.getPlayerid(), campCandidate);
        }

        //读取投票记录
        List<CampVote> listVote = campVoteDao.getAll();

        for(CampVote campVote : listVote){
            player2Vote.put(campVote.getPlayerid(), campVote);
        }

        //设置状态
        state = STATE_STARTED;
    }

    
    /**
     * 投票结束
     */
    private void stopVote() throws Exception{
        //设置状态
        state = STATE_CLOSED;
        
        //官员数据清除
        campMainService.clearOfficial();

        //将票数冠军设为新一代的领袖
        //处理黑暗阵营
        if(darkSet.size() > 0){
            CampCandidate darkKing = darkSet.first();
            campMainService.processNewKing(darkKing);
            
            String message = CampConfig.campVoteConfig.getElectionMessage();
            campMainService.sendRoarMessage(message, CampMainService.CAMP_DARK);
            campMainService.sendCampMessage(message, CampMainService.CAMP_DARK);
            campMainService.sendPrivateMessage("恭喜您，正式当选Campname阵营的领袖！接受这万众瞩目的荣耀吧！", CampMainService.CAMP_DARK, darkKing.getPlayerid());
        
            IItem di = Items.getTemplate(201507).newInstance();		//物品领袖效果
			byte[] att = ItemUtils.item2dbAttachment(di, 1);
			WorldPlayer darkKingplayer = campMainService.getPlayerService().getWorldPlayerAndCatch(darkKing.getPlayerid());
			darkKingname = darkKingplayer.getPlayerName();
			mailService.sendMail(darkKing.getPlayerid(), darkKingname, -1, "振奋人心", di.getName() + "*" + 1, "恭喜您成为黑暗阵营的领袖，获得了一个“振奋人心”的效果。", att, 0, true);
			campMainService.getPlayerService().releasePlayer(darkKingplayer);
        }

        //处理光明阵营
        if(lightSet.size() > 0){
            CampCandidate lightKing = lightSet.first();
            campMainService.processNewKing(lightKing);
            
            String message = CampConfig.campVoteConfig.getElectionMessage();
            campMainService.sendRoarMessage(message, CampMainService.CAMP_LIGHT);
            campMainService.sendCampMessage(message, CampMainService.CAMP_LIGHT);
            campMainService.sendPrivateMessage("恭喜您，正式当选Campname阵营的领袖！接受这万众瞩目的荣耀吧！", CampMainService.CAMP_LIGHT, lightKing.getPlayerid());
            
            IItem di = Items.getTemplate(201507).newInstance();		//物品领袖效果
			byte[] att = ItemUtils.item2dbAttachment(di, 1);
			WorldPlayer lightKingplayer = campMainService.getPlayerService().getWorldPlayerAndCatch(lightKing.getPlayerid());
			lightKingname = lightKingplayer.getPlayerName();
			mailService.sendMail(lightKing.getPlayerid(), lightKingname, -1, "振奋人心", di.getName() + "*" + 1, "恭喜您成为光明阵营的领袖，获得了一个“振奋人心”的效果。", att, 0, true);
			campMainService.getPlayerService().releasePlayer(lightKingplayer);
        }
        
        
        
        //保存阵营数据
        campMainService.saveCamp();

        //将所有选举数据的valid修改后存盘
        for(CampCandidate tmp : player2Candidate.values()){
            tmp.setValid(false);
            campCandidateDao.makePersistent(tmp);
        }

        //将所有投票数据的valid修改后存盘
        for(CampVote tmp : player2Vote.values()){
            tmp.setValid(false);
            campVoteDao.makePersistent(tmp);
        }

        //执行清理
        clear();
        
        //重新添加任务以便下周运行
        init();
    }
}
