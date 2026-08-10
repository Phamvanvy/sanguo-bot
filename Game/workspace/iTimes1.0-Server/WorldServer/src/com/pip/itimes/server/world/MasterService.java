package com.pip.itimes.server.world;

import java.util.*;

import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.bean.Mate;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.MasterDao;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.log4j.Logger;
import org.mortbay.log.Log;

import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.util.Utils;

public class MasterService {
	private Logger log = Logger.getLogger(MasterService.class);
	
	public final static int APPRENTICE_MAX = 2;			//徒弟最大个数
	public final static int APPRENTICE_LEVEL_MIN = 1;	//徒弟最低等级
	public final static int APPRENTICE_LEVEL_MAX = 59;	//徒弟最高等级 包括
	public final static int MASTER_LEVEL_MIN = 60;		//师傅最低等级
	public final static int MASTER_LEVEL_MAX = 100;		//师傅最高等级
	public final static int MASTER_RANDOM_MAX = 10;		//随机师傅的最大个数
	
    private MasterDao dao;

    private Map<Integer,ArrayList<Master>> masterid2relations = new HashMap<Integer,ArrayList<Master>>();
    private Map<Integer,Master> prenticeid2relation = new HashMap<Integer,Master>();

    private Map<Integer,Request> id2request = new HashMap<Integer,Request>();
    private AtomicInteger id = new AtomicInteger(0);
    private PlayerService playerService;

    public MasterService(MasterDao dao) {
        this.dao = dao;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void loadMasters() throws Exception{
        Master[] masters = dao.loadAllMasters(Master.CURRENT);
        for(int i=0;i<masters.length;i++){
            addMasterRelation(masters[i]);
            prenticeid2relation.put(masters[i].getPrenticeId(),masters[i]);
        }
    }
    
    /**
     * 用于显示爱徒列表
     * @param state
     * @param masterid
     * @return
     * @throws Exception
     */
    public Master[] loadMasterApprentices(int state, int masterid) throws Exception{
    	Master[] masters = dao.loadAllApprentices(state, masterid);
        return masters;
    }

    protected void addMasterRelation(Master m){
        ArrayList l = masterid2relations.get(m.getMasterId());
        if(l==null)
            l = new ArrayList(3);
        l.add(m);
        masterid2relations.put(m.getMasterId(),l);
    }

    protected void removeMasterRelation(Master m){
        ArrayList l = masterid2relations.get(m.getMasterId());
        if(l!=null){
            l.remove(m);
            if(l.size()==0)
                masterid2relations.remove(m.getMasterId());
        }
    }

    public static int TOP_ID = (int)550003;
    public static int TOP_MASTER_ID = (int)550004;
    public static int APPITEM_ID = (int)200916;

    private Master makeRelation(WorldPlayer master,WorldPlayer prentice,Changed changed1,Changed changed2) throws MasterException{
        if(isPrentice(master)||isPrentice(prentice)||isMaster(prentice))
            throw new MasterException("关系混乱");
//        if(getPrenticeCount(master)>=3)
        if(getPrenticeCount(master) >= APPRENTICE_MAX)
            throw new MasterException("最多只能收" + APPRENTICE_MAX + "个徒弟");
//        if(prentice.getLevel()<7)
        if(prentice.getLevel() < APPRENTICE_LEVEL_MIN)
            throw new MasterException("徒弟等级必须大于或等于" + APPRENTICE_LEVEL_MIN + "级");
//        if(master.getLevel()<30)
        if(master.getLevel() < MASTER_LEVEL_MIN)
            throw new MasterException("师傅等级必须大于或等于" + MASTER_LEVEL_MIN + "级");
        Master m = new Master();
        m.setMasterId(master.getId());
        m.setMasterName(master.getPlayerName());
        m.setPrenticeId(prentice.getId());
        m.setPrenticeName(prentice.getPlayerName());
        m.setBeginLevel(prentice.getLevel());
        m.setState(Master.CURRENT);
        m.setFame(master.getFame());
        try {
            dao.makePersistent(m);
            log.info("MakeRelation MasterID[" + m.getMasterId() + " ApprenticeID[" + m.getPrenticeId() + "]");
            addMasterRelation(m);
            prenticeid2relation.put(m.getPrenticeId(),m);
            if(!prentice.hasItem(TOP_ID))
                prentice.addItem(TOP_ID,1,changed2, prentice.getClientDataVersion());
            //同时给师傅发个称谓物品
            if(!master.hasItem(TOP_MASTER_ID))
            	master.addItem(TOP_MASTER_ID,1,changed1, master.getClientDataVersion());
            
            return m;
        } catch (DataAccessException ex) {
            throw new MasterException("拜师出错",ex);
        }
    }

    public synchronized Master makeRelation(WorldPlayer master,int id,Changed changed1,Changed changed2) throws MasterException{
        Request request = id2request.get(id);
        if(request==null)
            throw new MasterException("请求失效");
        if(request.sourceId != master.getId())
            throw new MasterException("请求错误");
        WorldPlayer prentice = playerService.getWorldPlayer(request.destId);
        if(prentice==null){
            id2request.remove(id);
            throw new MasterException("对方已经不在线");
        }
        return makeRelation(master,prentice,changed1,changed2);
    }
    
    /**
     * 保存关系
     * @param player
     * @throws Exception
     */
    public void saveRelation(IPlayerData player) throws Exception{
    	synchronized (player) {
			if(isMaster(player)){
				Master[] apps = getRelation(player);
				if(apps != null && apps.length > 0){
					for(int i=0; i<apps.length; i++){
						dao.makePersistent(apps[i]);
					}
				}
			}else if(isPrentice(player)){
				Master master = getMasterRelation(player);
				if(master != null){
					dao.makePersistent(master);
				}
			}
		}
    }
    
    /**
     * 设置师傅的声望
     * @param player
     * @param fame
     */
    public void setMasterFame(IPlayerData player, int fame) throws Exception{
    	synchronized (player) {
			if(isMaster(player)){
				Master[] apps = getRelation(player);
				if(apps != null && apps.length > 0){
					for(int i=0; i<apps.length; i++){
						apps[i].setFame(fame);
					}
				}
			}
		}
    }
    
    
    public synchronized Master apprenticeMakeRelation(WorldPlayer apprentice,int id,Changed changed1,Changed changed2) throws MasterException{
        Request request = id2request.get(id);
        if(request==null)
            throw new MasterException("请求失效");
        if(request.destId != apprentice.getId())
            throw new MasterException("请求错误");
        WorldPlayer master = playerService.getWorldPlayer(request.sourceId);
        if(master==null){
            id2request.remove(id);
            throw new MasterException("对方已经不在线");
        }
        return makeRelation(master,apprentice,changed1,changed2);
    }

    /**
     * 取消关系
     * @param player 师傅
     * @param id
     * @return
     * @throws MasterException
     */
    public synchronized Request cancelRelation(WorldPlayer player,int id) throws MasterException{
        Request request = id2request.get(id);
        if(request==null)
            throw new MasterException("请求错误");
//        if(request.destId!=player.getId())
        if(request.sourceId != player.getId())
            throw new MasterException("请求错误");
        return id2request.remove(id);
    }
    
    public synchronized Request apprenticeCancelRelation(WorldPlayer player,int id) throws MasterException{
        Request request = id2request.get(id);
        if(request==null)
            throw new MasterException("请求错误");
//        if(request.destId!=player.getId())
        if(request.destId != player.getId())
            throw new MasterException("请求错误");
        return id2request.remove(id);
    }

    //正常出师
    public synchronized Master unRelation(WorldPlayer prentice, Changed changed1,Changed changed2) throws MasterException {
        Master relation = prenticeid2relation.get(prentice.getId());
        if (relation == null)
            throw new MasterException("你没有师傅");
//        if (prentice.getLevel() - relation.getBeginLevel() < 15)
//            throw new MasterException("你还不够出师的条件.");
        if(prentice.getLevel() <= APPRENTICE_LEVEL_MAX)
        	throw new MasterException("你还不够出师的条件.");
        WorldPlayer master = playerService.getWorldPlayerAndCatch(relation.getMasterId());
        try {
//            WorldPlayer master = playerService.loadWorldPlayer(relation.getMasterId());
//            boolean acquire = false;
            if(master!=null){
//                playerService.acquire(master);
//                acquire = true;
                synchronized (master) {
                	
                	int credit = relation.getIntimacy();
                	if(credit >= 200){
                		credit = 200 + (relation.getIntimacy() - 200) / 100;
                	}
                	
                	if(relation.getBeginLevel() <= 25){
                		credit = credit * 120 / 100;
                	}else{
                		credit = credit * (MASTER_LEVEL_MIN - relation.getBeginLevel()) * 3 / 100;
                	}
                	
                    relation.setState(Master.SUCCESS);
                    dao.makePersistent(relation);
                    removeMasterRelation(relation);
                    prenticeid2relation.remove(prentice.getId());
                    master.addCredit(credit, changed2);
//                    master.addMoney(30000, changed2);
//                    prentice.addCredit(100, changed1);
//                    prentice.addMoney(10000, changed1);
                    
                    //2个3级宝石原石定向包
                    if(changed1 != null){
                    	prentice.addExp(300000, changed1);
                    	prentice.addItem(APPITEM_ID, 2, changed1, prentice.getClientDataVersion());
                    }
                    resetTitle(prentice);
                    log.info("UnRelation MasterID[" + relation.getMasterId() + "] ApprenticeID[" + relation.getPrenticeId() + "]" + " Master Get Credit[" + credit + "]");
                }
//                if(acquire){
//                    playerService.release(master);
//                }
//                playerService.savePlayer(master);
                return relation;
            }else{
                 throw new MasterException("解除关系错误");
            }
        } catch (Exception ex) {
            throw new MasterException("解除关系错误", ex);
        } finally{
        	playerService.releasePlayer(master);
        }
    }

    //徒弟单方面解除关系
    public synchronized Master IllegalUnRelation(WorldPlayer prentice,Changed changed) throws MasterException{
        synchronized(prentice){
            Master relation = prenticeid2relation.get(prentice.getId());
            if (relation == null)
                throw new MasterException("关系不存在");
            try {
//                int money = ((prentice.getLevel() - relation.getBeginLevel()) + 1) * 500;
//                if(prentice.getMoeny()<money){
//                    throw new MasterException("没有足够的金钱");
//                }
//                int credit = ((prentice.getLevel() - relation.getBeginLevel()) + 1) * 100;
            	int credit = 100;
                if(prentice.getCredit()<credit){
                    throw new MasterException("没有足够的荣誉");
                }
//                WorldPlayer master = playerService.loadWorldPlayer(relation.getMasterId());
                relation.setState(Master.FAIL);
                dao.makePersistent(relation);
                removeMasterRelation(relation);
                prenticeid2relation.remove(prentice.getId());
//                prentice.decMoney(money, changed);
                prentice.decCredit(credit, changed);
                resetTitle(prentice);
                
                log.info("Apprentic Illeagal Unrelation MasterID[" + relation.getMasterId() + "] ApprenticeID[" + relation.getPrenticeId() + "]");
                return relation;
            }catch(MasterException ex1){
                throw ex1;
            }
            catch (Exception ex) {
                throw new MasterException("解除关系错误", ex);
            }
        }
    }
    
    //徒弟正常解除关系 不扣费
    public synchronized Master unRelation(WorldPlayer prentice,Changed changed) throws MasterException{
        synchronized(prentice){
            Master relation = prenticeid2relation.get(prentice.getId());
            if (relation == null)
                throw new MasterException("关系不存在");
            try {
                relation.setState(Master.FAIL);
                dao.makePersistent(relation);
                removeMasterRelation(relation);
                prenticeid2relation.remove(prentice.getId());
                resetTitle(prentice);
                log.info("Apprentic Unrelation MasterID[" + relation.getMasterId() + "] ApprenticeID[" + relation.getPrenticeId() + "]");
                return relation;
            }
            catch (Exception ex) {
                throw new MasterException("解除关系错误", ex);
            }
        }
    }

    //师傅单方面解除关系
    public Master IllegalUnRelation(WorldPlayer master,int prenticeId,Changed changed) throws MasterException{
        synchronized(master){
            Master relation = prenticeid2relation.get(prenticeId);
            if (relation == null)
                throw new MasterException("关系不存在");
            if (relation.getMasterId() != master.getId())
                throw new MasterException("关系错误");
            WorldPlayer prentice = playerService.getWorldPlayerAndCatch(prenticeId);
            try {
//                WorldPlayer prentice = playerService.loadWorldPlayer(prenticeId);
//                boolean acquire = false;
                if(prentice!=null){
//                    playerService.acquire(prentice);
//                    acquire = true;
//                    int money = ((prentice.getLevel() - relation.getBeginLevel()) + 1) * 500;
//                    if (master.getMoeny() < money) {
//                        throw new MasterException("没有足够的钱");
//                    }
                    int credit = 100;
//                    if(prentice.getLevel() >= 14){
//                    	credit= ((prentice.getLevel() - relation.getBeginLevel()) + 1) * 100;
//                    }
                    if (master.getCredit() < credit) {
                    	playerService.releasePlayer(prentice);
                        throw new MasterException("没有足够的荣誉");
                    }
                    relation.setState(Master.FAIL);
                    dao.makePersistent(relation);
                    removeMasterRelation(relation);
                    prenticeid2relation.remove(prentice.getId());
//                    if (prentice.getLastLoginTime().getTime() + 15 * 3600 * 24L <= System.currentTimeMillis() ||
//                        prentice.getLevel() <= 13)
//                        ;
//                    else {
//                        master.decMoney(money, changed);
                        master.decCredit(credit, changed);
//                    }
                    resetTitle(prentice);
//                    if(acquire){
//                        playerService.release(prentice);
//                    }
                    log.info("Master Illeagal Unrelation MasterID[" + relation.getMasterId() + "] ApprenticeID[" + relation.getPrenticeId() + "]");
                    return relation;
                }else{
                    throw new MasterException("解除关系错误");
                }
            } catch (MasterException ex1){
                throw ex1;
            } catch (Exception ex) {
                throw new MasterException("解除关系错误", ex);
            } finally{
	            if(prentice != null){
	            	playerService.releasePlayer(prentice);
	            }
            }
        }
    }
    
  //师傅单方面正常解除关系
    public Master unRelation(WorldPlayer master,int prenticeId,Changed changed) throws MasterException{
        synchronized(master){
            Master relation = prenticeid2relation.get(prenticeId);
            if (relation == null)
                throw new MasterException("关系不存在");
            if (relation.getMasterId() != master.getId())
                throw new MasterException("关系错误");
            WorldPlayer prentice = playerService.getWorldPlayerAndCatch(prenticeId);
            try {
//                WorldPlayer prentice = playerService.loadWorldPlayer(prenticeId);
//                boolean acquire = false;
                if(prentice!=null){
//                    playerService.acquire(prentice);
//                    acquire = true;
//                    int money = ((prentice.getLevel() - relation.getBeginLevel()) + 1) * 500;
//                    if (master.getMoeny() < money) {
//                        throw new MasterException("没有足够的钱");
//                    }
                    relation.setState(Master.FAIL);
                    dao.makePersistent(relation);
                    removeMasterRelation(relation);
                    prenticeid2relation.remove(prentice.getId());
                    resetTitle(prentice);
//                    if(acquire){
//                        playerService.release(prentice);
//                    }
                    log.info("Master Unrelation MasterID[" + relation.getMasterId() + "] ApprenticeID[" + relation.getPrenticeId() + "]");
                    return relation;
                }else{
                    throw new MasterException("解除关系错误");
                }
            } catch (MasterException ex1){
                throw ex1;
            } catch (Exception ex) {
                throw new MasterException("解除关系错误", ex);
            } finally{
            	if(prentice != null){
            		playerService.releasePlayer(prentice);
            	}
            }
        }
    }

    //徒弟使用物品单方面解除关系
    public Master itemUnRelation(WorldPlayer prentice,Changed changed) throws Exception{
        synchronized(prentice){
            Master relation = prenticeid2relation.get(prentice.getId());
            if (relation == null)
                throw new MasterException("关系不存在");
            try {
//                WorldPlayer master = playerService.loadWorldPlayer(relation.getMasterId());
                relation.setState(Master.FAIL);
                dao.makePersistent(relation);
                removeMasterRelation(relation);
                prenticeid2relation.remove(prentice.getId());
                resetTitle(prentice);
                log.info("Apprentice UseItem Unrelation MasterID[" + relation.getMasterId() + "] ApprenticeID[" + relation.getPrenticeId() + "]");
                return relation;
            } catch (Exception ex) {
                throw new MasterException("解除关系错误", ex);
            }
        }
    }

    //师傅使用物品单方面解除关系
    public Master itemUnRelation(WorldPlayer master,int prenticeId,Changed changed) throws MasterException{
        synchronized(master){
            Master relation = prenticeid2relation.get(prenticeId);
            if (relation == null)
                throw new MasterException("关系不存在");
            if (relation.getMasterId() != master.getId())
                throw new MasterException("关系错误");
            WorldPlayer prentice = playerService.getWorldPlayerAndCatch(prenticeId);
            try {
//                WorldPlayer prentice = playerService.loadWorldPlayer(prenticeId);
//                boolean acquire = false;
                if(prentice!=null){
//                    playerService.acquire(prentice);
//                    acquire = true;
                    relation.setState(Master.FAIL);
                    dao.makePersistent(relation);
                    removeMasterRelation(relation);
                    prenticeid2relation.remove(prentice.getId());
                    resetTitle(prentice);
//                    if(acquire){
//                        playerService.release(prentice);
//                    }
                    log.info("Master UseItem Unrelation MasterID[" + relation.getMasterId() + "] ApprenticeID[" + relation.getPrenticeId() + "]");
                    return relation;
                }else{
                    throw new MasterException("解除关系错误");
                }
            } catch (Exception ex) {
                throw new MasterException("解除关系错误", ex);
            } finally{
            	if(prentice != null){
            		playerService.releasePlayer(prentice);
            	}
            }
        }
    }

    public void resetTitle(WorldPlayer player){
        if(player.getTitle()!=null&&player.getTitle().endsWith("徒弟]")){
            player.setTitle("");
        }
    }


    public Request requestRelation(WorldPlayer master,WorldPlayer prentice) throws MasterException{
        if(isPrentice(master)||isPrentice(prentice)||isMaster(prentice))
            throw new MasterException("关系混乱");
//        if(getPrenticeCount(master)>=3)
        if(getPrenticeCount(master) >= APPRENTICE_MAX)
            throw new MasterException("最多只能收" + APPRENTICE_MAX + "个徒弟");
//        if(prentice.getLevel()<7)
        if(prentice.getLevel() < APPRENTICE_LEVEL_MIN)
            throw new MasterException("徒弟等级必须大于或等于" + APPRENTICE_LEVEL_MIN + "级");
//        if(master.getLevel()<30)
        if(master.getLevel() < MASTER_LEVEL_MIN)
            throw new MasterException("师傅等级必须大于或等于" + MASTER_LEVEL_MIN + "级");
        Request ret = new Request(id.incrementAndGet(),master.getId(),prentice.getId());
        id2request.put(ret.id,ret);
        return ret;
    }
    
    /**
     * 检测指定的师傅是否达到了要求，如果达到要求返回null 不然返回达不到要求的描述
     * @param master
     * @param player
     * @return
     */
    public String checkMaster(WorldPlayer master, WorldPlayer player){
    	if(master == null){
    		return "没有找到该玩家。";
    	}else{
    		if(master.getCamp() != player.getCamp()){
    			return "您所要拜的师傅与你非同阵营哦。";
    		}
    		if(master.getLevel() < MasterService.MASTER_LEVEL_MIN){
    			return "该玩家等级没有达到" + MasterService.MASTER_LEVEL_MIN + "级。";
    		}
    		if(getPrenticeCount(master) >= MasterService.APPRENTICE_MAX){
    			return "您所要拜的师傅所收徒弟已达上限。";
    		}
    	}
    	return null;
    }
    
    /**
     * 获得随机的师傅列表
     * @param camp
     * @return
     */
    public WorldPlayer[] getRandomMaster(byte camp){
    	int count = MASTER_RANDOM_MAX;
    	int length = playerService.getMasterPlayerCount();
    	if(length < count){
    		count = length;
    	}
    	WorldPlayer[] masters = playerService.getMasterPlayers();
    	List validMasters = new ArrayList();
    	for(int i=0; i<masters.length; i++){
    		if(masters[i].getCamp() == camp && masters[i].online()){
    			validMasters.add(masters[i]);
    		}
    	}
    	if(validMasters.size() == 0) return null;
    	if(count > validMasters.size()){
    		count = validMasters.size();
    	}
    	length = validMasters.size() - 1;
    	Random rnd = new Random();
    	int[] counts = Utils.getCounts(rnd, 0, length, count);
    	WorldPlayer[] tmpMasters = new WorldPlayer[count];
    	for(int i=0; i<count; i++){
    		tmpMasters[i] = (WorldPlayer)validMasters.get(counts[i]);
    	}
    	return tmpMasters;
    }


    public boolean isMaster(IPlayerData p){
        return masterid2relations.containsKey(p.getId());
    }

    public boolean isPrentice(IPlayerData p){
        return prenticeid2relation.containsKey(p.getId());
    }

    public boolean hasRelation(IPlayerData p1,IPlayerData p2){
        if(p1.getId()==p2.getId())
            return false;
        Master m = prenticeid2relation.get(p1.getId());
        if(m==null){
            m = prenticeid2relation.get(p2.getId());
            if (m != null && m.getMasterId() == p1.getId())
                return true;
            return false;
        }else{
            if(m.getMasterId()==p2.getId())
                return true;
        }
        return false;
    }

    private static final Master[] EMPTY_MASTER = new Master[0];


    //师傅获取关系，可能有多个
    public Master[] getRelation(IPlayerData p){
        ArrayList<Master> l = masterid2relations.get(p.getId());
        if(l==null)
            return null;
        else{
           Master[] ret = new Master[l.size()];
           l.toArray(ret);
           return ret;
        }
    }

    public Master getRelation(WorldPlayer p,int precentid){
        ArrayList<Master> l = masterid2relations.get(p.getId());
        if(l==null)
            return null;
        else{
            for(Master master:l){
                if(master.getPrenticeId()==precentid){
                    return master;
                }
            }
            return null;
        }
    }

    //徒弟获取关系，最多只能有一个
    public Master getMasterRelation(IPlayerData p){
        return prenticeid2relation.get(p.getId());
    }

    public int getPrenticeCount(WorldPlayer p){
        ArrayList l = masterid2relations.get(p.getId());
        if(l==null)
            return 0;
        return l.size();
    }

    public int getSuccessCount(WorldPlayer p){
        try {
            return dao.getSuccessCount(p.getId());
        } catch (DataAccessException ex) {
            return 0;
        }
    }



    public String getMasterTitle(WorldPlayer p){
        int count = getSuccessCount(p);
        //加上当前徒弟的个数
        count += getPrenticeCount(p);
        if(count==0)
            return "";
        if(count==1)
            return "[初为人师]";
        if(count>=2&&count<=4)
            return "[徒拥之师]";
        if(count>=5&&count<=8)
            return "[众人之师]";
        if(count>=9&&count<=14)
            return "[天下之师]";
        if(count>=15&&count<=30)
            return "[师之圣者]";
        if(count>=30)
            return "[师之至尊]";
        return "";
    }

    public static class Request{
        public int id;
        public int sourceId;
        public int destId;
        public Request(int id,int sourceId,int destId){
            this.id = id;
            this.sourceId = sourceId;
            this.destId = destId;
        }
    }
    public void nameModified(String oldName,WorldPlayer player){
    	Master master = getMasterRelation(player);
        if(master!=null){
        	
        	if (player.getId() == master.getMasterId()){
        		master.setMasterName(player.getPlayerName());
        		log.info("Master Modify Name MasterID[" + master.getMasterId() + "] ApprenticeID[" + master.getPrenticeId() + "]");
        	}else if (player.getId() == master.getPrenticeId()){
        		master.setPrenticeName(player.getPlayerName());
        		log.info("Apprentice Modify Name MasterID[" + master.getMasterId() + "] ApprenticeID[" + master.getPrenticeId() + "]");
        	}
        	
        	try {
				dao.makePersistent(master);
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        Master[] masters = getRelation(player);
        if (masters!=null){
            for(int i=0;i<masters.length;i++){
            	master = masters[i];
            	if(master!=null){
                	
                	if (player.getId() == master.getMasterId()){
                		master.setMasterName(player.getPlayerName());
                		log.info("Master Modify Name MasterID[" + master.getMasterId() + "] ApprenticeID[" + master.getPrenticeId() + "]");
                	}else if (player.getId() == master.getPrenticeId()){
                		master.setPrenticeName(player.getPlayerName());
                		log.info("Apprentice Modify Name MasterID[" + master.getMasterId() + "] ApprenticeID[" + master.getPrenticeId() + "]");
                	}
                	
                	try {
        				dao.makePersistent(master);
        			} catch (DataAccessException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                }
            }
        }
    }
}

