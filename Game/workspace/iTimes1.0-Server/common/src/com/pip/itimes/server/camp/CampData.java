package com.pip.itimes.server.camp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.pip.itimes.server.bean.Camp;
import com.pip.itimes.server.util.PropertyPool;

public class CampData{
    private Camp camp;
    private HashMap<Integer, CampSkillData> skills = new HashMap<Integer, CampSkillData>();
    private long lastSetTaxRateTime;
    private HashMap<Integer, Integer> player2AmerceTimes = new HashMap<Integer, Integer>();
    private int amerceTime;
    private HashMap<Integer, CampSilence> player2Silence = new HashMap<Integer, CampSilence>();
    private PropertyPool pool = new PropertyPool();
    
    private final String CHR_ITEM_TOTAL = "chrItemTotal";
    private int chrItemTotal;			//圣诞节捐的物品的总个数
    
    private Map<Integer, CampOfficial> officials = new HashMap<Integer, CampOfficial>();			//官员列表
    private int amerceTimeOfficial;		//官员的罚款次数
    private static SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
    private int silenceTime;			//官员的禁言次数
    
    public CampData(Camp camp, int campId) throws Exception{
        if(camp == null){
            camp = new Camp();

            camp.setCamp(campId);
            camp.setKingid(-1);
            camp.setCreatetime(new Date());
            camp.setLasttime(new Date());
            camp.setMoney(0);
            camp.setTaxrate(CampConfig.taxDefault);
            camp.setSlogan("");
            camp.setPool("");
            camp.setValid(true);
            
            initDefaultSkillData();
            this.camp = camp;
        }else{
            this.camp = camp;
            load();
        }
        
        lastSetTaxRateTime = -1;
    }
    
    public Map getOfficial(){
    	return officials;
    }
    
    private CampData(){
        
    }
    
    public CampData copyAndCreate() throws Exception{
        CampData result = new CampData();

        result.camp = new Camp();
        result.camp.setCamp(this.camp.getCamp());
        result.camp.setKingid(this.camp.getKingid());
        result.camp.setCreatetime(new Date());
        result.camp.setLasttime(new Date());
        result.camp.setMoney(this.camp.getMoney());
        result.camp.setTaxrate(this.camp.getTaxrate());
        result.camp.setSkills(this.camp.getSkills());
        result.camp.setSlogan("");
        result.camp.setPool(this.camp.getPool());
        result.camp.setValid(true);
        
        result.load();
        result.amerceTime = 0;
        result.lastSetTaxRateTime = -1;
        
        return result;
        
    }
    
    private void initDefaultSkillData(){
        skills.clear();
        Iterator<Integer> it = CampConfig.campSkills.keySet().iterator();
        
        while(it.hasNext()){
            int effect = it.next();
            CampSkillData tmp = new CampSkillData();
            tmp.setEffect(effect);
            tmp.setLevel(0);
            tmp.setLastUpgradeTime(-1);
            tmp.setLastMaintTime(-1);
            
            skills.put(effect, tmp);
        }
    }
    
    /**
     * 初始化pool变量池
     */
    private void initPool(){
    	setChrItemTotal(pool.getInt(CHR_ITEM_TOTAL));
    	initOfficial(pool);
    }
    
    private void load() throws Exception{
        //读取技能
        skills.clear();
        
        List<CampSkillData> list = CampSkill.fromDbBytes(camp.getSkills());
        
        for(CampSkillData tmp : list){
            skills.put(tmp.getEffect(), tmp);
        }
        
        //新增加的科技 需要添加进去
        if(CampConfig.campSkills.size() > skills.size()){
	        Iterator<Integer> it = CampConfig.campSkills.keySet().iterator();
	        while(it.hasNext()){
	            int effect = it.next();
	            if(!skills.containsKey(effect)){
		            CampSkillData tmp = new CampSkillData();
		            tmp.setEffect(effect);
		            tmp.setLevel(0);
		            tmp.setLastUpgradeTime(-1);
		            tmp.setLastMaintTime(-1);
		            skills.put(effect, tmp);
	            }
	        }
        }
        
        //读取pool
        pool.parse(camp.getPool());
        //需要初始化pool
        initPool();
    }
    
    public void save() throws Exception{
        List<CampSkillData> list = new ArrayList<CampSkillData>(skills.values());
        camp.setSkills(CampSkill.toDbBytes(list));
        //保存pool
        resetPool();
    }
    
    public Camp getCamp(){
        return camp;
    }
    
    public long getMoney(){
        return camp.getMoney();
    }
    
    public int getKingId(){
        return camp.getKingid();
    }
    
    public List<CampSkillData> getSkillDataList(){
        return new ArrayList<CampSkillData>(skills.values());
    }
    
    public CampSkillData getSkillData(int effect){
        return skills.get(effect);
    }
    
    public void addMoney(int money){
        camp.setMoney(camp.getMoney() + money);
    }
    
    public int getTaxRate(){
        return camp.getTaxrate();
    }
    
    public void setTaxRate(int rate){
        camp.setTaxrate(rate);
        lastSetTaxRateTime = System.currentTimeMillis();
    }
    
    public long getLastSetTaxRateTime(){
        return lastSetTaxRateTime;
    }
    
    public boolean testCanAmerce(int targetId, boolean isKing){
    	if(isKing){
	        if(amerceTime >= CampConfig.amerceLimit){
	            return false;
	        }
    	}else{
    		if(amerceTimeOfficial >= CampConfig.amerceLimitOfficial){
	            return false;
	        }
    	}
        
        Integer times = (Integer)player2AmerceTimes.get(targetId);
        
        if(times != null && times >= CampConfig.amercePlayerLimit){
            return false;
        }
        
        return true;
    }
    
    public void amercePlayer(int targetId, boolean isKing){
    	if(isKing){
    		amerceTime++;
    	}else{
    		amerceTimeOfficial++;
    	}
        Integer times = (Integer)player2AmerceTimes.get(targetId);
        
        if(times == null){
            times = new Integer(0);
        }
        
        player2AmerceTimes.put(targetId, new Integer(times.intValue() + 1));
    }
    
    public boolean hasSilenced(int targetId){
        return player2Silence.containsKey(targetId);
    }
    
    public Iterator getSilenceTimes(){
        return player2Silence.values().iterator();
    }
    
    public boolean addSilence(int targetId, boolean isKing){
    	if(!isKing){
    		if(silenceTime >= CampConfig.SILENCE_OFFICIAL_COUNT){
    			return false;
    		}
    		silenceTime ++;
    	}
    	long startTime = System.currentTimeMillis();
    	long endTime = startTime + (isKing ?  CampConfig.SILENCE_TIME :  CampConfig.SILENCE_TIME_OFFICIAL);
        player2Silence.put(targetId, new CampSilence(startTime, endTime));
        return true;
    }
    
    public void removeSilence (int targetId) {
    	player2Silence.remove(targetId);
    }
    
    public void setValid(boolean valid){
        camp.setValid(valid);
    }
    
    public void setKingId(int kingId){
        camp.setKingid(kingId);
    }
    
    public void clear(){
        lastSetTaxRateTime = -1;
        player2AmerceTimes.clear();
        amerceTime = 0;
        amerceTimeOfficial = 0;
        silenceTime = 0;
        player2Silence.clear();
    }
    
    public int getChrItemTotal(){
    	return chrItemTotal;
    }
    
    public void setChrItemTotal(int total){
    	this.chrItemTotal = total;
    }
    
    public void resetPool(){
    	pool.setInt(CHR_ITEM_TOTAL, getChrItemTotal());
    	saveOfficial(pool);
    	camp.setPool(pool.toString());
    }
    
    public void clearOfficial(){
    	if(officials != null){
    		officials.clear();
    	}
    	officials = new HashMap<Integer, CampOfficial>();
    	resetPool();
    }
    
    public void initOfficial(PropertyPool pool){
    	int count = pool.getInt(CampOfficial.STR_OFFICIAL_COUNT);
    	officials = new HashMap<Integer, CampOfficial>();
    	for(int i=0; i<count; i++){
    		CampOfficial official = new CampOfficial();
    		official.setPost((byte)pool.getInt(CampOfficial.STR_OFFICIAL_POST + i));
    		official.setPlayerID(pool.getInt(CampOfficial.STR_OFFICIAL_PLAYERID + i));
    		String taskDate = pool.getString(CampOfficial.STR_OFFICIAL_TASKTIME + i);
    		if(taskDate != null){
    			try{
    				official.setTaskTime(format.parse(taskDate).getTime());
    			}catch(Exception e){
    			}
    		}
    		officials.put(new Integer(official.getPost()), official);
    	}
    }
    
    public void saveOfficial(PropertyPool pool){
    	if(officials == null){
    		pool.setInt(CampOfficial.STR_OFFICIAL_COUNT, 0);
    	}else{
    		int count = officials.size();
    		pool.setInt(CampOfficial.STR_OFFICIAL_COUNT, count);
    		Iterator iter = officials.values().iterator();
    		int index = 0;
    		while(iter.hasNext()){
    			CampOfficial official = (CampOfficial)iter.next();
    			pool.setInt(CampOfficial.STR_OFFICIAL_POST + index, official.getPost());
    			pool.setInt(CampOfficial.STR_OFFICIAL_PLAYERID + index, official.getPlayerID());
    			pool.setString(CampOfficial.STR_OFFICIAL_TASKTIME + index, format.format(new Date(official.getTaskTime())));
    			index++;
    		}
    	}
    }
    
    /**
     * 返回被替换掉的角色的ID 返回-1时表示该职位原先没有角色任职
     * @param post
     * @param playerid
     * @return
     */
    public int changeOfficial(byte post, int playerid){
    	if(officials.containsKey(new Integer(post))){
    		CampOfficial official = officials.get(new Integer(post));
    		int oldID = official.getPlayerID();
    		official.setPlayerID(playerid);
    		return oldID;
    	}else{
    		CampOfficial official = new CampOfficial();
    		official.setPost(post);
    		official.setPlayerID(playerid);
    		officials.put(new Integer(post), official);
    	}
    	return -1;
    }
    
    public CampOfficial testOfficial(int playerid){
    	Iterator iter = officials.values().iterator();
		while(iter.hasNext()){
			CampOfficial official = (CampOfficial)iter.next();
			if(official.getPlayerID() == playerid){
				return official;
			}
		}
		return null;
    }
}
