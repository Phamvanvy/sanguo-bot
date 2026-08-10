package peony.game.actlead;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import peony.util.TimeUtil;

/**
 * 活动指引实体类
 * @author dchen
 */
public class ActLeader {
	
	public static int TYPE_SERVICE = 1; //活动
	public static int TYPE_QUEST = 2; //任务 
	public static int TYPE_BOSS = 3; //怪物
	public static int TYPE_ITEM = 4; //商品
	
	public static int REWARD_TYPE_MONEY = 1; //金钱
	public static int REWARD_TYPE_ITEM = 2; //物品
	public static int REWARD_TYPE_RANK = 3; //荣誉
	public static int REWARD_TYPE_CREDIT = 4; //战功
	
	public static int LEVEL_TYPE_MIN = 1; //小于5级
	public static int LEVEL_TYPE_CURRENT = 0; //适用级别
	public static int LEVEL_TYPE_MAX = 2; //大于5级
	
	protected static int DISTANCE = 30 * 24 * 3600;
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	/** 活动类型(用,分割。1、活动 2、任务 3、怪物 4、商品) */
	public String type;
	private int[] typeArr;
	
	/** 活动名称 */
	public String name;
	
	/** 地图名称 */
	public String mapName;
	
	/** 活动创建时间 */
	public String createTime;
	
	/** 活动所在位置地图ID */
	public int mapId;
	
	/** 活动所在地图位置x坐标 */
	public int x;
	
	/** 活动所在地图位置y坐标 */
	public int y;
	
	/** 活动奖励类型（用,分割。1、金钱 2、珍珠 3、声望 4、战功） */
	public String rewardType;
	private int[] rewardTypeArr;
	
	/** 活动适用最低级别 */
	public int minLevel;
	
	/** 活动适用最高级别 */
	public int maxLevel;
	
	/** 活动说明 */
	public String instruction = "";
	
	/** 阵营（用,分割。1、魏国 2、蜀国 3、吴国) */
	public String faction;
	private int[] factionArr;
	
	public ActLeader(String type, String name, String mapName, String createTime, int mapId,
			int x, int y, String rewardType, int minLevel, 
			int maxLevel, String instruction, String faction) throws Exception {
		this.type = type;
		this.name = name;
		this.mapName = mapName;
		this.createTime = createTime;
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.rewardType = rewardType;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.instruction = instruction;
		this.faction = faction;
		parseConfig();
	}
	
	protected void parseConfig() throws Exception {
		try {
			String[] typeStr = type.split(",");
			typeArr = new int[typeStr.length];
			for(int i=0;i<typeStr.length;i++){
				int ti = Integer.parseInt(typeStr[i]);
				if(ti<1 || ti>4)
					throw new Exception(peony.Messages.STRING_00694);
				typeArr[i] = ti;
			}
			String[] rewardTypeStr = rewardType.split(",");
			rewardTypeArr = new int[rewardTypeStr.length];
			for(int i=0;i<rewardTypeStr.length;i++){
				int ti = Integer.parseInt(rewardTypeStr[i]);
				if(ti<1 || ti>4)
					throw new Exception(peony.Messages.STRING_00694);
				rewardTypeArr[i] = ti;
			}
			String[] factionStr = faction.split(",");
			factionArr = new int[factionStr.length];
			for(int i=0;i<factionStr.length;i++){
				int ti = Integer.parseInt(factionStr[i]);
				if(ti<1 || ti>3)
					throw new Exception(peony.Messages.STRING_00694);
				factionArr[i] = ti;
			}
			df.parse(createTime);
		} catch (Exception e) {
			throw new ActLeaderException(peony.Messages.STRING_00694);
		}
	}
	
	/** 活动是否属于指定地图 */
	public boolean isInMap(int mapId){
		if(mapId<=0)
			return true;
		return mapId==this.mapId;
	}
	
	/** 活动是否在级别范围内 */
	public boolean inLevel(int level){
		if(level<=0)
			return true;
		return (level>=minLevel && level<maxLevel);
	}
	
	/** 活动是否属于奖励类型 */
	public boolean belongRewardType(int rewardType){
		if(rewardType<=0)
			return true;
		for(int rt : rewardTypeArr){
			if(rewardType==rt)
				return true;
		}
		return false;
	}
	
	/** 活动是否属于类型 */
	public boolean belongType(int type){
		if(type<=0)
			return true;
		for(int t : typeArr){
			if(type==t)
				return true;
		}
		return false;
	}
	
	/** 活动是否在指定时间区内 */
	public boolean isInTime(Date date, int timeType){
		if(timeType==0){
			return true;
		}else if(timeType==1){
			Date createDate;
			try {
				createDate = df.parse(createTime);
			} catch (ParseException e) {
				return false;
			}
			Date conditionDate = TimeUtil.getDate(date, DISTANCE, false);
			return createDate.after(conditionDate);
		}
		return false;
	}
	
	/** 活动是否属于指定阵营 */
	public boolean isInFaction(int faction){
		if(faction<=0)
			return true;
		for(int t : factionArr){
			if(faction==t)
				return true;
		}
		return false;
	}
	
}
