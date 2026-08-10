package peony.game.directory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import peony.clientguid.GuidTrigger;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.association.Association;
import peony.game.association.AssociationService;
import peony.game.nation.NationRel;
import peony.game.nation.NationService;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;

/**
 * 引导(记录玩家当前建议做的各种事情以及状态信息)
 * @author dchen
 */
public class ClientDirectory {
	
	/**  引导项 */
	public static class Directory{
		
		/** 引导ID */
		public int id;
		/** 引导名称 */
		public String name;
		/** 目标位置 */
	    public int[] location;
	    /** 周 */
	    public int[] weeks;
	    /** 时间段 小时 */
	    public int[] timeScheduleHour;
	    /** 时间段 分钟 */
	    public int[] timeScheduleMin;
	    /** 活动描述 */
	    public String description;
	    /** 阵营 */
	    public int faction;
	    /** 职业 */
	    public int clazz;
	    /** 级别对应的评分 */
//	    public Map<Integer, Integer> scores = new HashMap<Integer, Integer>();
	    public int[] scores = new int[10];
		/** 引导条件集合 */
		public List<GuidTrigger> triggerList = new ArrayList<GuidTrigger>();
		/** 经验 */
	    public int exp;
	    /** 金钱 */
	    public int money;
	    /** 战功 */
	    public int rank;
	    /** 珍珠 */
	    public int pearl;
	    /** 奖励物品 */
	    public int item1, count1;
	    /** 奖励物品 */
	    public int item2, count2;
	    /** 奖励物品 */
	    public int item3, count3;
	    /** 奖励物品 */
	    public int item4, count4;
	    /** 奖励描述 */
	    public String rewardDesc;
	    /** 级别下限 */
	    public int minLevel;
	    /** 级别上限 */
	    public int maxLevel;
	    /** 活动明细 */
	    public String directoryList;
	    /** 难度 */
	    public int difficulty;
	    /** 工资 */
	    public int salary;
		
		@SuppressWarnings("unchecked")
		public static Class getParamClass(int paramType) throws Exception{
			switch(paramType){
			case 0:
				return Integer.class;
			case 1:
				return String.class;
			}
			throw new Exception(peony.Messages.STRING_01095);
		}
		
		public int getRewardCount(){
			int rewardCount = 0;
			if(item1>0 && count1>0)
				rewardCount++;
			if(item2>0 && count2>0)
				rewardCount++;
			if(item3>0 && count3>0)
				rewardCount++;
			if(item4>0 && count4>0)
				rewardCount++;
			return rewardCount;
		}
		
		public boolean inScoreLevel(int equipScore){
			for(int i=0;i<10;i+=2){
				int minScore = 0;
				try {minScore = scores[i];} catch (Exception e) {}
				int maxScore = 0;
				try{maxScore = scores[i+1];}catch(Exception e){}
				if(equipScore>=minScore && equipScore<=maxScore)
					return true;
			}
			return false;
		}
		
		public boolean hasThisWeekDay(int weekDay){
			for(int w : weeks){
				if(w==weekDay)
					return true;
			}
			return false;
		}
		
	}
	
	private static final Logger log = Logger.getLogger(ClientDirectory.class);
	
	/** 玩家 */
	public Player player;
	/** 装备积分 */
	public int totalEqupScore;
	
	/** 所有引导的集合 */
	public static Map<Integer, Directory> id2directory = new HashMap<Integer, Directory>();
	public static Directory[] alldirectory;
	public static Directory[] exp2directory;
	public static Directory[] money2directory;
	public static Directory[] rank2directory;
	public static Directory[] pearl2directory;
	
	/** 引导变量池:key为triggerId或者directoryId_triggerId */
	protected Map<String, int[]> varStores = new HashMap<String, int[]>();
	
	/** 引导状态(true为完成、false为未完成) */
	protected Map<Integer, Boolean> states = new HashMap<Integer, Boolean>();
	
	public int currentDay;
	
	protected static Map<String, Method> methods = new HashMap<String, Method>();
	
	public ClientDirectory(Player owner){
		this.player = owner;
	}
	
	public void update(int diff){
		if(Time.day!=currentDay){
			currentDay = Time.day;
			states.clear();
		}
		for(Directory directory : alldirectory){
			try {
				Boolean dState = states.get(directory.id);
				if(isInLevel(player.level, directory) && isInFaction(player.faction, directory) && isInClazz(player.clazz, directory) && isInSchedule(directory) && directory.inScoreLevel(totalEqupScore)){
					if(dState==null || !dState){
						//trigger集合进行&&判断
						boolean resultOk = false;
						for(GuidTrigger trigger : directory.triggerList){
							resultOk = executeMethod(diff, directory.id, trigger.id, trigger.functionName, trigger.paramType, trigger.paramValue, trigger.paramSign);
							if(!resultOk)
								break;
						}
						if(resultOk)
							states.put(directory.id, resultOk);
					}
				}else{
					if(dState==null || dState)
						states.put(directory.id, false);
				}
			} catch (Exception e) {
//				log.error(e.getMessage());
			}
		}
	}
	
	private boolean executeMethod(int diff, int directoryId, int triggerId, String functionName, int[] paramTypes, Object[] paramValue, int[] paramSign){
		Method method = null;
		boolean result = false;
		try {
			method = methods.get(functionName);
			if(method==null){
				method = ClientDirectory.class.getMethod(functionName, Integer.class, Integer.class, Integer.class, int[].class, Object[].class, int[].class);
				methods.put(functionName, method);
			}
			result = (Boolean) method.invoke(this, diff, directoryId, triggerId, paramTypes, paramValue, paramSign);
		} catch (Exception e) {
		}
		return result;
	}
	
	/**
	 * 获取对应条件的引导项列表
	 * @param weekDay,按周获取（中国习惯）
	 * @param exp,按经验排序
	 * @param money,按金钱排序
	 * @param rank,按战功排序
	 * @param pearl,按珍珠排序
	 * @return
	 */
	public List<Directory> getDirectorys(int weekDay, boolean exp, boolean money, boolean rank, boolean pearl){
		ArrayList<Directory> list = new ArrayList<Directory>();
		if(weekDay>0){
			for(Directory d : alldirectory){
				if(isInLevel(player.level, d) && isInFaction(player.faction, d) && isInClazz(player.clazz, d) && d.hasThisWeekDay(weekDay) && d.inScoreLevel(totalEqupScore))
					list.add(d);
			}
		}else if(exp || money || rank || pearl){
			Directory[] directorys = null;
			if(exp)
				directorys = exp2directory;
			else if(money)
				directorys = money2directory;
			else if(rank)
				directorys = rank2directory;
			else if(pearl) 
				directorys = pearl2directory;
			for(Directory d : directorys){
				if(accordNormalCondition(player, d))
					list.add(d);
			}
		}else{
			for(Directory d : alldirectory){
				if(accordNormalCondition(player, d))
					list.add(d);
			}
		}
		return list;
	}
	
	public void setState(int id, boolean state){
		states.put(id, state);
	}
	
	public boolean getState(int id){
		Boolean result = states.get(id);
		return result==null ? false : result;
	}
	
	protected void setVarStore(String key, int[] arr){
		varStores.put(key, arr);
	}
	
	protected void removeVarStore(String key){
		varStores.remove(key);
	}
	
	protected int[] getVarStore(String key){
		return varStores.get(key);
	}
	
	public static int getWeekDay(){
		int weekDay = Time.currentWeekDay;
		return weekDay-1<1 ? 7 : weekDay-1;
	}
	
	public boolean isInLevel(int playerLevel, Directory directory){
		return playerLevel>=directory.minLevel && playerLevel<=directory.maxLevel;
	}
	
	public boolean isInClazz(int playerClazz, Directory directory){
		return playerClazz==directory.clazz || directory.clazz==4;
	}
	
	public boolean isInFaction(int faction, Directory directory){
		return directory.faction==faction || directory.faction==0;
	}
	
	public boolean accordNormalCondition(Player player, Directory d){
		return isInLevel(player.level, d) && isInFaction(player.faction, d) && isInClazz(player.clazz, d) && isInSchedule(d) 
			&& d.inScoreLevel(totalEqupScore) && (states.get(d.id)==null || !states.get(d.id));
	}
	
	public static boolean isInSchedule(Directory directory){
		int[] weeks = directory.weeks;
		int[] hours = directory.timeScheduleHour;
		int[] mins = directory.timeScheduleMin;
		for(int w : weeks){
			w += 1;
			w = w>7 ? 1 : w;
			for(int i=0;i<hours.length;i+=2){
				int sHour = hours[i];
				int sMin = mins[i];
				int eHour = hours[i+1];
				int eMin = mins[i+1];
				if(inTime(w, sHour, sMin, eHour, eMin))
					return true;
			}
		}
		return false;
	}
	
	public static boolean inTime(int week, int sHour, int sMin, int eHour, int eMin){
		if(week==Time.currentWeekDay){
			boolean accordStime = false;
			boolean accordEtime = false;
			if(Time.currentHour>sHour || (Time.currentHour==sHour && Time.currentMin>=sMin))
				accordStime = true;
			if(Time.currentHour<eHour || (Time.currentHour==eHour && Time.currentMin<=eMin))
				accordEtime = true;
			if(accordStime && accordEtime)
				return true;
		}
		return false;
	}
	
	/** 玩家是否完成某任务 */
	public boolean g_finisnQuest(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int questId = ((Integer)paramValue[0]).intValue();
		int type = ((Integer)paramValue[1]).intValue();
		int ok = player.asmVm.finishConditionOk(questId);
		if(type==1)
			return (ok==1 ? false : true);
		else
			return (ok==1 ? true : false);
	}
	
	/** 玩家靠近某位置 */
	public boolean g_closeToNPC(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int mapId = ((Integer)paramValue[0]).intValue();
		int x = ((Integer)paramValue[1]).intValue();
		int y = ((Integer)paramValue[2]).intValue();
		int dis = ((Integer)paramValue[3]).intValue();
		if(player.map.getId()==mapId){
			if(Math.abs(player.x-x)<=dis && Math.abs(player.y-y)<=dis)
				return true;
		}
		return false;
	}
	
	/** 玩家进入场景 */
	public boolean m_enterScene(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int mapId = ((Integer)paramValue[0]).intValue();
		int[] store = getVarStore(String.valueOf(triggerId));
		if(store[0]==mapId)
			return true;
		return false;
	}
	
	/** 玩家杀死怪物 */
	public boolean m_kILLMonsterID(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int npcId = ((Integer)paramValue[0]).intValue();
		int count = ((Integer)paramValue[1]).intValue();
		int c = player.getKillCreatureCount(npcId);
		String varKey = directoryId.intValue() + "_" + triggerId;
		int[] store = getVarStore(varKey);
		if(store==null){
			store = new int[]{c};
		}else{
			store[0] = store[0] + c;
		}
		setVarStore(varKey, store);
		if(store[0]>=count){
			removeVarStore(varKey);
			return true;
		}
		return false;
	}
	
	/** 玩家被怪物杀死 */
	public boolean m_monsterKillPlayer(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		return player.dieCause == 2 ? true : false;
	}
	
	/** 玩家杀死其他玩家 */
	public boolean m_playerKillOTHEN(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int[] arr = getVarStore(String.valueOf(triggerId));
		if(arr!=null && arr[0]>0){
			removeVarStore(String.valueOf(triggerId));
			return true;
		}
		return false;
	}
	
	/** 玩家被其他玩家杀死 */
	public boolean m_otherKillPlayer(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int[] arr = getVarStore(String.valueOf(triggerId));
		if(arr!=null && arr[0]>0){
			removeVarStore(String.valueOf(triggerId));
			return true;
		}
		return false;
	}
	
	/** 玩家职业 */
	public boolean g_roleJob(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int clazz = ((Integer)paramValue[0]).intValue();
		if(player.clazz==clazz)
			return true;
		return false;
	}
	
	/** 正在进行某一任务 */
	public boolean g_doingQuest(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int questId = ((Integer)paramValue[0]).intValue();
		if(player.asmVm.hasTask(questId)==1)
			return true;
		return false;
	}
	
	/** 提交已完成的任务 */
	public boolean g_submitFinisnQuest(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		if(diff.intValue()%50==0){
			int questId = ((Integer)paramValue[0]).intValue();
			ASMQuest quest = ASMQuestUtil.getQuest(questId);
			if(quest==null)
				return false;
			return player.asmVm.hasFinished(quest, new DateTime());
		}
		return false;
	}
	
	/** 玩家touchNpc */
	public boolean g_openQuestGruide(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int npcId = ((Integer)paramValue[0]).intValue();
		int[] arr = getVarStore(String.valueOf(triggerId));
		if(arr!=null && arr[0]==npcId){
			removeVarStore(String.valueOf(triggerId));
			return true;
		}
		return false;
	}
	
	/** 玩家是否是国公 */
	public boolean g_isKing(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		return player.isKing()==1 ? true : false;
	}
	
	/** 玩家是否有血盟 */
	public boolean g_isAssociationMember(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		AssociationService service = Server.server.getServiceRegistry().getAssociationService();
		Association association = service.getAssociationByPlayerId(player.id);
		return association!=null;
	}
	
	/** 玩家是否有配偶 */
	public boolean g_isMates(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		if(player.relations!=null)
			return player.relations.mateId > 0;
		return false;
	}
	
	/** 玩家是否有师徒关系 */
	public boolean g_isApprenticeMember(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		if(player.relations!=null)
			return player.relations.hasApprenticeRelation(player);
		return false;
	}
	
	/** 玩家是否开启宣战 */
	public boolean g_nationBattleOpen(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		int faction = player.faction;
		NationRel[] rels = nationService.rels[faction];
		for(NationRel rel : rels){
			if(rel!=null && rel.type==NationRel.TYPE_WAR_REQUEST)
				return true;
		}
		return false;
	}
	
	/** 玩家是否开启反击战 */
	public boolean g_nationDeclarBattleOpen(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		NationService nationService = Server.server.getServiceRegistry().getNationService();
		int faction = player.faction;
		NationRel[] rels = nationService.rels[faction];
		for(NationRel rel : rels){
			if(rel!=null && rel.type==NationRel.TYPE_SNEAK_REQUEST)
				return true;
		}
		return false;
	}
	
	/** 玩家获得某个物品 */
	public boolean g_getItem(Integer diff, Integer directoryId, Integer triggerId, int[] paramTypes, Object[] paramValue, int[] paramSign){
		int itemId = ((Integer)paramValue[0]).intValue();
		int[] store = getVarStore(String.valueOf(triggerId));
		if(store!=null && store[0]==itemId)
			return true;
		return false;
	}
	
}
