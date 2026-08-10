package peony.service.towerdefend;

import java.util.ArrayList;
import java.util.List;

/**
 * 塔防系统配置信息管理器
 * @author dchen
 */
public class TowerDefendConfig {

	public int mapId; //TD战场地图ID
	public int[] defendPosition; //防守方传送点
	public int[] attackPosition; //进攻方传送点
	public int[][] out; //战场结束传出位置
	public int duration; //TD战场持续时间
	public List<int[]> centers = new ArrayList<int[]>(); //TD各个底座的中心坐标信息
	public List<TDItemToTower> itemTotowers = new ArrayList<TDItemToTower>(); //物品使用刷出箭塔信息
	public List<TDInit> inits = new ArrayList<TDInit>(); //战场初始化信息
	public List<Integer> referItems = new ArrayList<Integer>(); //塔防相关物品集合
	public int MINLEVEL; //报名最低级别
	public int[] flag; //战旗
	public static int BUFF = 278;
	public Reward winReward;
	public Reward failReward;
	public static int MAXINSTANCE = 20;
	
	
	public TowerDefendConfig(int mapId, int[] defendPosition, int[] attackPosition, int[][] out, 
			int duration, List<int[]> centers, List<TDItemToTower> itemTotowers, List<TDInit> inits, 
			int[] flag, int minLevel, Reward winReward, Reward failReward){
		this.mapId = mapId;
		this.defendPosition = defendPosition;
		this.attackPosition = attackPosition;
		this.out = out;
		this.duration = duration;
		this.centers = centers;
		this.itemTotowers = itemTotowers;
		this.inits = inits;
		this.flag = flag;
		this.MINLEVEL = minLevel;
		this.winReward = winReward;
		this.failReward = failReward;
		initReferItems();
	}
	
	protected void initReferItems(){
		int[] items = new int[]{962, 963, 964, 966, 967, 968, 969, 970, 921, 982};
		for(int itemId : items){
			referItems.add(itemId);
		}
	}
	
	/** 传入点 */
	public int[] getInPosition(int type){
		if(type==0)
			return defendPosition;
		else if(type==1)
			return attackPosition;
		return null;
	}
	
	public int getItemToTowerId(int itemId){
		for(TDItemToTower it : itemTotowers){
			if(it.itemId==itemId)
				return it.towerId;
		}
		return 0;
	}
	
	public TDItemToTower getItemToTower(int itemId){
		for(TDItemToTower it : itemTotowers){
			if(it.itemId==itemId)
				return it;
		}
		return null;
	}
	
	public boolean isTower(int id){
		for(TDItemToTower tdtt : itemTotowers){
			if(tdtt.towerId==id)
				return true;
		}
		return false;
	}
	
	public String getTowerType(int id){
		for(TDItemToTower tdtt : itemTotowers){
			if(tdtt.towerId==id)
				return tdtt.type;
		}
		return "";
	}
	
}

class Reward{
	
	public static int TYPE_WIN = 1;
	public static int TYPE_FAIL = 0;
	public int type;
	public List<Integer> itemIds;
	public List<Integer> count;
	public int rank;
	
	public Reward(int type, List<Integer> itemIds, List<Integer> count, int rank){
		this.type = type;
		this.itemIds = itemIds;
		this.count = count;
		this.rank = rank;
	}
	
}
