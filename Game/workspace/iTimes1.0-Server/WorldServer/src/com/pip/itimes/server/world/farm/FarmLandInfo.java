package com.pip.itimes.server.world.farm;

import java.util.HashMap;

import com.pip.itimes.server.world.farm.FarmConfig;
import com.pip.itimes.server.world.farm.FarmSeedData;

public class FarmLandInfo {
	public static final long MinuteMillis = 60 * 1000;
	public static final int FERTILIZE_MAXCOUNT = 1;		//施肥次数
	public static final int LEVEL_MAX = 1;			//最大级数	初始是0 算1级 目前只能升1级
	
	private int level = 0;			//土地等级
	private int seed = 0;			//种子 0为没有种子 其它为种子编号
	private long createTime = 0;	//土地创建的时间
	private byte fertilize = 0;		//施肥次数
	private int fruitCurrentCount = 0;		//果实当前个数
	private int resultsCount = 0;			//入侵后果实个数
	private HashMap<Integer, Integer> stealPlayer = null;	//窃取的PlayerID
	private boolean results = false;	//是否能够收获果实
	
	public FarmLandInfo(int level, int seed, long createTime, byte fertilize){
		this.level = level;
		this.seed = seed;
		this.createTime = createTime;
		this.fertilize = fertilize;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void setSeed(int seed){
		this.seed = seed;
	}
	public int getSeed(){
		return seed;
	}
	
	public void setCreateTime(long createTime){
		this.createTime = createTime;
	}
	
	public long getCreateTime(){
		return createTime;
	}
	
	public void setFruitCurrentCount(int fruitCurrentCount){
		this.fruitCurrentCount = fruitCurrentCount;
	}
	
	public int getFruitCurrentCount(){
		return fruitCurrentCount;
	}
	
	public void setFertilize(byte fertilize){
		this.fertilize = fertilize;
	}
	
	public byte getFertilize(){
		return fertilize;
	}
	
	public void setResults(boolean results){
		this.results = results;
	}
	
	public boolean getResults(){
		return results;
	}
	
	public void setResultsCount(int resultsCount){
		this.resultsCount = resultsCount;
	}
	
	public int getResultsCount(){
		return resultsCount;
	}
	
	public void setStealPlayer(HashMap<Integer, Integer> stealPlayer){
		this.stealPlayer = stealPlayer;
	}
	
	public void addStealPlayer(int playerid){
		if(stealPlayer == null){
			stealPlayer = new HashMap<Integer, Integer>();
		}
		stealPlayer.put(playerid, null);
	}
	
	public boolean checkStealPlayer(int playerid){
		if(stealPlayer == null) return false;
		return stealPlayer.containsKey(playerid);
	}
	
	public HashMap<Integer, Integer> getStealPlayer(){
		return stealPlayer;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("土地等级：");
		sb.append(level + 1);
		if(fertilize > 0){
			sb.append("（已施肥）");
		}
		if(seed > 0){
			FarmSeedData fsd = FarmConfig.getFarmSeed(seed);
			if(fsd != null){
				sb.append("\n种植了");
				sb.append(fsd.getName());
			}
			if(results){
				sb.append("\n剩余果实数：");
				sb.append(fruitCurrentCount);
				sb.append("攻击力：");
				sb.append(fsd.getAP());
				sb.append("\n可收获");
			}else{
				long now = System.currentTimeMillis();
				if(createTime + MinuteMillis * fsd.getGrowCycle() > now){
					long minute = (createTime + MinuteMillis * fsd.getGrowCycle() - now) / 1000 / 60 + 1;
					sb.append("\n现在处于生长期，还有");
					sb.append(minute);
					sb.append("分钟就可以具备防御僵尸的能力。");
				}else{
					sb.append("\n果实数：");
					sb.append(fruitCurrentCount);
					sb.append("攻击力：");
					sb.append(fsd.getAP());
					sb.append("生命力：");
					sb.append(fsd.getHP());
					sb.append("\n现在处于成熟期，收获期将在每天凌晨4点以后");
				}
			}
		}else{
			sb.append("\n该土地还未播种，请赶快播种哦~");
		}
		return sb.toString();
	}
	
	/**
	 * 指定的作物是否成熟了
	 * @param index
	 * @return
	 */
	public boolean isMature(){
		if(seed <= 0) return false;
		FarmSeedData fsd = FarmConfig.getFarmSeed(seed);
		if(fsd == null) return false;
		long now = System.currentTimeMillis();
		if(createTime + MinuteMillis * fsd.getGrowCycle() > now){ 
			return false;
		}
		return true;
	}
	
	public static final byte STEAL_NOSEED = 0;	//没有种子
	public static final byte STEAL_NOFINDSEED = 1;	//没有找到种子
	public static final byte STEAL_NOFRUIT = 2;	//已经没有果实了 在这儿表示已经被偷光了
	public static final byte STEAL_CANSTEAL = 3;	//可以窃取
	public byte canSteal(){
		if(seed <= 0) return STEAL_NOSEED;
		if(fruitCurrentCount <= resultsCount){
			return STEAL_NOFRUIT;
		}
		int stealCount = fruitCurrentCount * 10 / 100;
		if(stealCount <= 0) return STEAL_NOFRUIT;
		return STEAL_CANSTEAL;
	}
}
