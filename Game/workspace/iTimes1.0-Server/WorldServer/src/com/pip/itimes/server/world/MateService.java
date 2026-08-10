package com.pip.itimes.server.world;

import java.util.HashMap;
import java.util.Map;

import com.pip.itimes.server.bean.Mate;
import com.pip.itimes.server.dao.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.TongUser;

import java.util.Date;

public class MateService {

    private static final Logger log = Logger.getLogger(MateService.class);

    private MateDao dao;

    private Map<Integer,Mate> id2mates = new HashMap<Integer,Mate>();

    private Map<Integer,MarryRequest> id2request = new HashMap<Integer,MarryRequest>();
    private Map<Integer,UnMarryRequest> id2unRequest = new HashMap<Integer,UnMarryRequest>();

    private AtomicInteger id = new AtomicInteger(0);

    private PlayerService playerService;
    private MailService mailService;

    public MateService(MateDao dao) {
        this.dao = dao;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setMailService(MailService mailService){
        this.mailService = mailService;
    }

    public void loadMates() throws Exception{
        Mate[] mates = dao.loadAllMates();
        for(int i=0;i<mates.length;i++){
            id2mates.put(mates[i].getHusbandId(),mates[i]);
            id2mates.put(mates[i].getWifeId(),mates[i]);
        }
    }

    public boolean hasMate(WorldPlayer player){
        return id2mates.get(player.getId())!=null;
    }

    public Mate getMate(WorldPlayer player){
        return id2mates.get(player.getId());
    }

    public int getMateId(WorldPlayer player){
        Mate mate = id2mates.get(player.getId());
        if(mate!=null){
            if(player.getSex()==0)
                return mate.getWifeId();
            else
                return mate.getHusbandId();
        }
        return -1;
    }

    public boolean isMate(WorldPlayer p1,WorldPlayer p2){
        if(p1.getId()==p2.getId())
            return false;
        Mate mate = id2mates.get(p1.getId());
        if(mate==null)
            return false;
        if(mate.getHusbandId()==p2.getId()||mate.getWifeId()==p2.getId())
            return true;
        return false;
    }

//    public boolean canMarray(WorldPlayer p1,WorldPlayer p2){
//        return true;
//    }

    public MarryRequest requestMarry(WorldPlayer p1,WorldPlayer p2) throws MateException{
//        if(p1.getFriendFavorite(p2)<99)
//            throw new MateException("没有足够的友好度");
//        if(p2.getFriendFavorite(p1)<99)
//            throw new MateException("没有足够的友好度");
        if(p1.getLevel()<6||p2.getLevel()<6)
            throw new MateException("需要双方等级超过6级才能结婚");
        if(Math.abs(p2.getSex()-p1.getSex())!=1)
            throw new MateException("目前只有男女才允许结为夫妻哦.");
//        if(p1.getCredit()<10||p2.getCredit()<10)
//            throw new MateException("荣誉没有达到要求");
        if(p1.getMoeny()<(p1.getLevel()+p2.getLevel())*99)
            throw new MateException("没钱还想结婚呀");
        if(hasMate(p1)||hasMate(p2))
            throw new MateException("不能同时拥有两份婚姻哦");
        MarryRequest request = new MarryRequest(id.incrementAndGet(),p1.getId(),p2.getId());
        id2request.put(request.id,request);
        return request;
    }


    public synchronized Mate marry(WorldPlayer p,int requestId,Changed changed1,Changed changed2) throws MateException{
        MarryRequest request = id2request.get(requestId);
        if(request==null)
            throw new MateException("找不到结婚请求");
        if(request.destId!=p.getId())
            throw new MateException("结婚请求无效");
        WorldPlayer source = playerService.getWorldPlayer(request.sourceId);
        if(source==null){
            id2request.remove(requestId);
            throw new MateException("对方不在线");
        }
        return marry(source,p,changed1,changed2);
    }

    private static final int LOL_ID = (int)550002;

    private Mate marry(WorldPlayer p1, WorldPlayer p2, Changed changed1, Changed changed2) throws MateException {
        synchronized (p1) {
            synchronized (p2) {
                if (p1.getFriendFavorite(p2) < 99)
                    throw new MateException("没有足够的友好度");
                if (p2.getFriendFavorite(p1) < 99)
                    throw new MateException("没有足够的友好度");
                if (p1.getLevel() < 6 || p2.getLevel() < 6)
                    throw new MateException("需要双方等级超过6级才能结婚");
                if (Math.abs(p2.getSex() - p1.getSex()) != 1)
                    throw new MateException("目前只有男女才允许结为夫妻哦.");
                if (p1.getCredit() < 10 || p2.getCredit() < 10)
                    throw new MateException("荣誉没有达到要求");
                if (p1.getMoeny() < (p1.getLevel() + p2.getLevel()) * 99)
                    throw new MateException("没钱还想结婚呀");
                if (hasMate(p1) || hasMate(p2))
                    throw new MateException("不能同时拥有两份婚姻哦");
                p1.decMoney((p1.getLevel() + p2.getLevel()) * 99, changed1);
                if (!p1.hasItem(LOL_ID)) {
                    p1.addItem(LOL_ID, 1, changed1, p1.getClientDataVersion());
                }
                if (!p2.hasItem(LOL_ID)) {
                    p2.addItem(LOL_ID, 1, changed2, p2.getClientDataVersion());
                }
                Mate mate = new Mate();
                if (p1.getSex() == 0) {
                    mate.setHusbandId(p1.getId());
                    mate.setHusbandName(p1.getPlayerName());
                    mate.setWifeId(p2.getId());
                    mate.setWifeName(p2.getPlayerName());
                } else {
                    mate.setHusbandId(p2.getId());
                    mate.setHusbandName(p2.getPlayerName());
                    mate.setWifeId(p1.getId());
                    mate.setWifeName(p1.getPlayerName());
                }
                try {
                    mate.setCreateTime(new Date());
                    dao.makePersistent(mate);
                    id2mates.put(mate.getHusbandId(), mate);
                    id2mates.put(mate.getWifeId(), mate);
                    return mate;
                } catch (DataAccessException ex) {
                    throw new MateException("结婚错误", ex);
                }
            }
        }
    }

    public MarryRequest cancelMarry(WorldPlayer p,int id) throws MateException{
        MarryRequest request = id2request.get(id);
        if(request==null)
            throw new MateException("找不到结婚请求");
        if(request.destId!=p.getId())
            throw new MateException("结婚请求无效");
        return request;
    }

    public UnMarryRequest unMarryRequest(WorldPlayer p1,WorldPlayer p2) throws MateException{
        Mate mate = getMate(p1);
        if(mate==null)
            throw new MateException("你目前还未结婚");
        if(mate.getHusbandId()==p2.getId()||mate.getWifeId()==p2.getId()){
            UnMarryRequest request = new UnMarryRequest(id.incrementAndGet(),p1.getId(),p2.getId());
            id2unRequest.put(request.id,request);
            return request;
        }else{
            throw new MateException("随便找个人就想离婚呀");
        }
    }

    public synchronized Mate unMarry(WorldPlayer p,int id,Changed changed1,Changed changed2) throws MateException{
        UnMarryRequest request = id2unRequest.get(id);
        if(request==null)
            throw new MateException("找不到离婚请求");
        if(request.destId!=p.getId())
            throw new MateException("离婚请求无效");
        WorldPlayer source = playerService.getWorldPlayer(request.sourceId);
        if(source==null){
            id2request.remove(id);
            throw new MateException("对方不在线");
        }
        return unMarry(source,p,changed1,changed2);
    }

    public synchronized Mate itemUnMarry(WorldPlayer p1,Changed changed1,Changed changed2) throws MateException{
        synchronized(p1){
            Mate mate = getMate(p1);
            if (mate == null)
                throw new MateException("你目前还未结婚");
            WorldPlayer p2 = null;
            try {
                if (p1.getId() == mate.getHusbandId()) {
//                    p2 = playerService.loadWorldPlayer(mate.getWifeId());
                	p2 = playerService.getWorldPlayerAndCatch(mate.getWifeId());
                } else {
//                    p2 = playerService.loadWorldPlayer(mate.getHusbandId());
                	p2 = playerService.getWorldPlayerAndCatch(mate.getHusbandId());
                }
            } catch (Exception ex1) {
            	if(p2 != null){
            		playerService.releasePlayer(p2);
            	}
                log.error(ex1, ex1);
                throw new MateException("找不到对方");
            }

            try {
                dao.makeTransient(mate);
                id2mates.remove(mate.getHusbandId());
                id2mates.remove(mate.getWifeId());
                p1.setFriendFavorite(p2, p1.getFriendFavorite(p2) / 2);
                p2.setFriendFavorite(p1, p2.getFriendFavorite(p1) / 2);
                if(p1.getId()==mate.getHusbandId()){
                    resetTitle(p1,changed1);
                    resetTitle(p2,changed2);
                }else{
                    resetTitle(p1,changed2);
                    resetTitle(p2,changed1);
                }
                mailService.sendMail(p1.getId(), p1.getPlayerName(), -1, "系统", "离婚通知",
                                     "你已经与" + p2.getPlayerName() + "解除婚约", new byte[0], 0, true);
                mailService.sendMail(p2.getId(), p2.getPlayerName(), -1, "系统", "离婚通知",
                                              p1.getPlayerName() + "解除了婚约", new byte[0], 0, true);
                return mate;
            } catch (DataAccessException ex) {
                throw new MateException("离婚错误", ex);
            }finally{
            	playerService.releasePlayer(p2);
            }
        }
    }

    public synchronized UnMarryRequest cancelUnMarry(WorldPlayer p,int id) throws MateException{
        UnMarryRequest request = id2unRequest.get(id);
        if (request == null)
            throw new MateException("找不到离婚请求");
        if (request.destId != p.getId())
            throw new MateException("离婚请求无效");
        return request;
    }


    //协议离婚
    private Mate unMarry(WorldPlayer p1, WorldPlayer p2,Changed changed1,Changed changed2) throws MateException {
        synchronized (p1) {
            synchronized (p2) {
                Mate mate = getMate(p1);
                if (mate == null)
                    throw new MateException("你目前还未结婚");
                if (mate.getHusbandId() == p2.getId() || mate.getWifeId() == p2.getId()) {
                    try {
                        dao.makeTransient(mate);
//                        p1.decMoney(p1.getLevel() * 50, null);
                        id2mates.remove(p1.getId());
                        id2mates.remove(p2.getId());
                        p1.setFriendFavorite(p2, p1.getFriendFavorite(p2) / 2);
                        p2.setFriendFavorite(p1, p2.getFriendFavorite(p1) / 2);
                        if(p1.getId()==mate.getHusbandId()){
                            resetTitle(p1,changed1);
                            resetTitle(p2,changed2);
                            p1.removeRoleTitle("[" + mate.getWifeName() + "的丈夫]");
                            p2.removeRoleTitle("[" + mate.getHusbandName() + "的妻子]");
                        }else{
                            resetTitle(p1,changed2);
                            resetTitle(p2,changed1);
                            p1.removeRoleTitle("[" + mate.getHusbandName() + "的妻子]");
                            p2.removeRoleTitle("[" + mate.getWifeName() + "的丈夫]");
                        }
                        mailService.sendMail(p1.getId(), p1.getPlayerName(), -1, "系统", "离婚通知",
                                             "你已经与" + p2.getPlayerName() + "协议解除婚约", new byte[0], 0, true);
                        mailService.sendMail(p2.getId(), p2.getPlayerName(), -1, "系统", "离婚通知",
                                             "你已经与" + p1.getPlayerName() + "协议解除婚约", new byte[0], 0, true);
                        return mate;
                    } catch (DataAccessException ex) {
                        throw new MateException("离婚错误", ex);
                    }
                } else {
                    throw new MateException("随便找个人就想离婚呀");
                }
            }
        }
    }

    //单方面离婚
    public synchronized Mate unMarry(WorldPlayer p1,Changed changed1,Changed changed2) throws MateException {
        synchronized(p1){
            Mate mate = getMate(p1);
            if (mate == null)
                throw new MateException("你目前还未结婚");
            WorldPlayer p2 = null;
            try {
                if (p1.getId() == mate.getHusbandId()) {
//                    p2 = playerService.loadWorldPlayer(mate.getWifeId());
                	p2 = playerService.getWorldPlayerAndCatch(mate.getWifeId());
                } else {
//                    p2 = playerService.loadWorldPlayer(mate.getHusbandId());
                	p2 = playerService.getWorldPlayerAndCatch(mate.getHusbandId());
                }
            } catch (Exception ex1) {
            	playerService.releasePlayer(p2);
                log.error(ex1, ex1);
                throw new MateException("找不到对方");
            }

            if (p1.getMoeny() < ((p1.getLevel()+p2.getLevel()) * 200)){
            	playerService.releasePlayer(p2);
                throw new MateException("离婚费用不够");
            }
            try {
                dao.makeTransient(mate);
                if(p1.getId()==mate.getHusbandId()){
                    p1.decMoney(p1.getLevel() * 200, changed1);
                    p1.decCredit((p1.getLevel() + p2.getLevel()) * 30, changed1);
                }else{
                    p1.decMoney(p1.getLevel() * 200, changed2);
                    p1.decCredit((p1.getLevel() + p2.getLevel()) * 30, changed2);
                }
                p1.setFriendFavorite(p2,1);
                p2.setFriendFavorite(p1,1);
                id2mates.remove(mate.getHusbandId());
                id2mates.remove(mate.getWifeId());
                if(p1.getId()==mate.getHusbandId()){
                    resetTitle(p1,changed1);
                    resetTitle(p2,changed2);
                    p1.removeRoleTitle("[" + mate.getWifeName() + "的丈夫]");
                    p2.removeRoleTitle("[" + mate.getHusbandName() + "的妻子]");
                }else{
                    resetTitle(p1,changed2);
                    resetTitle(p2,changed1);
                    p1.removeRoleTitle("[" + mate.getHusbandName() + "的妻子]");
                    p2.removeRoleTitle("[" + mate.getWifeName() + "的丈夫]");
                }
                mailService.sendMail(p1.getId(), p1.getPlayerName(), -1, "系统", "离婚通知",
                                     "你已经与" + p2.getPlayerName() + "解除婚约", new byte[0], 0, true);
                mailService.sendMail(p2.getId(), p2.getPlayerName(), -1, "系统", "离婚通知",
                                              p1.getPlayerName() + "解除了婚约", new byte[0], 0, true);
                return mate;
            } catch (DataAccessException ex) {
                throw new MateException("离婚错误", ex);
            } finally{
            	playerService.releasePlayer(p2);
            }
        }
    }

    private void resetTitle(WorldPlayer player,Changed changed) {
        String title = player.getTitle();
        if (title != null && (title.endsWith("丈夫]") || title.endsWith("妻子]"))) {
            player.setTitle("");
            changed.setProperty(Changed.TITLE_STRING,"");
        }
    }

    public static class MarryRequest{
        public int sourceId;
        public int destId;
        public int id;
        public MarryRequest(int id,int sourceId,int destId){
            this.id = id;
            this.sourceId = sourceId;
            this.destId = destId;
        }
    }

    public static class UnMarryRequest{
        public int sourceId;
        public int destId;
        public int id;
        public UnMarryRequest(int id,int sourceId,int destId){
            this.id = id;
            this.sourceId = sourceId;
            this.destId = destId;
        }
    }
    public void nameModified(String oldName,WorldPlayer player){
        Mate mate = getMate(player);
        if(mate!=null){
        	
        	if (player.getId() == mate.getHusbandId()){
        		mate.setHusbandName(player.getPlayerName());
        	}else if (player.getId() == mate.getWifeId()){
        		mate.setWifeName(player.getPlayerName());
        	}
        	
        	try {
				dao.makePersistent(mate);
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}

