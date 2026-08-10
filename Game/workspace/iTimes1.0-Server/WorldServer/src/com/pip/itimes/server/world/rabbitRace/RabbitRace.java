package com.pip.itimes.server.world.rabbitRace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.WorldPlayer;

public class RabbitRace{
	static private Logger log = Logger.getLogger(RabbitRace.class);
	// private ConnectService connectService;

	// 比赛的三只兔子，各个技能不同
	public Rabbit rabbitFir;
	public Rabbit rabbitSec;
	public Rabbit rabbitThi;
	
	// 跑步持续时间
	public long raceRunTime = 0;
	
	// 各种赢的类型的总次数，用于统计
	public static int firWinNum = 0;
	public static int secWinNum = 0;
	public static int thiWinNum = 0;
	public static int fouWinNum = 0;
	public static int fifWinNum = 0;

	// 随机数生成器
	public static Random rnd = new Random();
	
	public static Calendar cal = Calendar.getInstance();
	
	// private int skillNum; //使用技能的次数

	// 计数器，每秒增长1，用以标记跑了多少秒，计数器是赛跑过程中公用的
	public static int raceCounter = 0;
	// 计数器，每秒增长1，用以标记跑了多少秒，计数器是赛跑过程中公用的
	public static int raceCounterForReloadOrShutDown = 0;

	// 技能使用计数器，用来记录技能一使用的时间
	public static int useSkillFirCou = 0;

	// 跑道长度
	public final int raceLength = 500;

	// 兔子一赢了
	public final int firWin = 0;
	// 兔子二赢了
	public final int secWin = 1;
	// 兔子三赢了
	public final int thiWin = 2;
	// 两只兔子同时到
	public final int twoWin = 3;
	// 三只兔子同时到
	public final int allWin = 4;

	// 胜利方式，用于下发，取值范围0-4
	private int winType = 0;

	// 标记是否胜利
	private boolean isFirWin = false;
	private boolean isSecWin = false;
	private boolean isThiWin = false;
	
	public static List<RabbitLocData> locList = new ArrayList<RabbitLocData>();
	// public static boolean isTowWin = false;
	// public static boolean isAllWin = false;

	public RabbitRace() {
		int skillNum = setSkillNum();
		rabbitFir = new Rabbit(Rabbit.TYPE_FIR, skillNum);
		rabbitSec = new Rabbit(Rabbit.TYPE_SEC, skillNum);
		rabbitThi = new Rabbit(Rabbit.TYPE_THI, skillNum);
		winType = 0;
	}

	// 限定可以使用的技能次数
	private int setSkillNum() {
		//加一个生成随机数的算法，结果受月份和日期影响
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int skillNum = Utils.getRandom(rnd, 0, 5);
		skillNum = (skillNum + month * month + day * day) % 6;
//		log.info("随机使用技能次数:" + skillNum);
		return skillNum;
	}

	/**
	 * 重置兔子赛跑相关数据，用于一局比赛后
	 */
	public void resetRabbitRace() {
		isFirWin = false;
		isSecWin = false;
		isThiWin = false;
		winType = 0;
		raceCounter = 0;
		useSkillFirCou = 0;
		resetRaceRunTime();
		int skillNum = setSkillNum();
		rabbitFir.resetRabbit(skillNum);
		rabbitSec.resetRabbit(skillNum);
		rabbitThi.resetRabbit(skillNum);
	}

	/**
	 * 开始跑步，每秒执行一次
	 */
	public void raceRun(int raceCounter, int useSkillFirCou) {
		useSkill(raceCounter, useSkillFirCou);

		int raceNumForReal = RabbitRaceConfig.raceNum;
//		log.info("RabbitRace:RaceNumber[" + raceNumForReal+ "]Counter is[" + raceCounter+"]");

		rabbitFir.setLoc(rabbitFir.getLoc() + rabbitFir.getSpeed());
		rabbitSec.setLoc(rabbitSec.getLoc() + rabbitSec.getSpeed());
		rabbitThi.setLoc(rabbitThi.getLoc() + rabbitThi.getSpeed());
		
//		log.info("RabbitRace:RaceNumber[" + raceNumForReal+ "] First Rabbit Loc = " + rabbitFir.getLoc());
//		log.info("RabbitRace:RaceNumber[" + raceNumForReal+ "] Second Rabbit Loc = " + rabbitSec.getLoc());
//		log.info("RabbitRace:RaceNumber[" + raceNumForReal+ "] Third Rabbit Loc = " + rabbitThi.getLoc());
	}

	/**
	 * 兔子们使用技能 具体此处是使用技能三个一个还是维持原速度由 兔子类内技能使用方法判断
	 */
	public void useSkill(int raceCounter, int useSkillFirCou) {
		useFirSkill(raceCounter, useSkillFirCou);
		useSecSkill(raceCounter);
		useThiSkill(raceCounter);
	}

	/**
	 * 使用技能一，技能一分为两步持续两秒
	 */
	public void useFirSkill(int raceCounter, int useSkillFirCou) {
		int timeRun = raceCounter - useSkillFirCou;
		rabbitFir.useSkill(getUseSkillNum(raceCounter), timeRun);
	}

	public void useSecSkill(int raceCounter) {
		rabbitSec.useSkill(getUseSkillNum(raceCounter), 0);
	}

	public void useThiSkill(int raceCounter) {
		rabbitThi.useSkill(getUseSkillNum(raceCounter), 0);
	}

	// 本次是第几个可以使用技能的5s，没5s可以使用一次技能
	public int getUseSkillNum(int raceCounter) {
		if (raceCounter % 5 == 0) {
			return raceCounter / 5;
		}
		return 0;
	}

	/**
	 * 判断比赛是否结束
	 * 
	 * @return
	 */
	public boolean isGameOver() {
		boolean isOver = false;
		if (rabbitFir.getLoc() >= raceLength) {
			isFirWin = true;
			isOver = true;
			// log.info("兔子1：Loc = " + rabbitFir.getLoc() + "到达终点");
		}
		if (rabbitSec.getLoc() >= raceLength) {
			isSecWin = true;
			isOver = true;
			// log.info("兔子2：Loc = " + rabbitSec.getLoc() + "到达终点");
		}
		if (rabbitThi.getLoc() >= raceLength) {
			isThiWin = true;
			isOver = true;
			// log.info("兔子3：Loc = " + rabbitThi.getLoc() + "到达终点");
		}
		return isOver;
	}

	public int getWinType() {
		return winType;
	}

	/**
	 * 判断是那种获胜方式 共五种，各个兔子单独获胜，两只兔子获胜，和全部兔子同时到达
	 * 
	 * @return
	 */
	public int getWinTypeAfterRaceEnd() {
		if (isFirWin == true && isSecWin == true && isThiWin == true) {
			winType = allWin;
			log.info("RabbitRace:WinType is[" + allWin + "].All rabbits win");
		} else if ((isFirWin == true && isSecWin == true)
				|| (isFirWin == true && isThiWin == true)
				|| (isThiWin == true && isSecWin == true)) {
			winType = twoWin;
			log.info("RabbitRace:WinType is[" + twoWin + "].Two rabbits win");
		} else if (isFirWin == true) {
			winType = firWin;
			log.info("RabbitRace:WinType is[" + firWin + "].First rabbit win");
		} else if (isSecWin == true) {
			winType = secWin;
			log.info("RabbitRace:WinType is[" + secWin + "].Second rabbit win");
		} else if (isThiWin == true) {
			winType = thiWin;
			log.info("RabbitRace:WinType is[" + thiWin + "].Third rabbit win");
		}
		return winType;
	}

//	@Override
//	public void run() {
//		while (!isGameOver()) {
//			winType = 0;
//			// 每秒执行一次
//			try {
//				Thread.sleep(1000L);
//			} catch (Exception e) {
//			}
//			raceCounter++;
//			raceRun(raceCounter, useSkillFirCou);
//			// sendOutRabbitInfo();
//		}
//		getWinTypeAfterRaceEnd();
//		getCountOfWinType();
//	}

	/**
	 * 保存跑步位置进入数组
	 */
	public void runAndSaveLocInList() {
		log.info("RabbitRaceConfig:runAndSaveLocInList()");
		resetLocList();
		//把最初的位置放在0处
		saveLocInList();
		while (!isGameOver()) {
			winType = 0;
			// 每秒执行一次
			raceCounter++;
			raceRun(raceCounter, useSkillFirCou);
			saveLocInList();
			// sendOutRabbitInfo();
			
		}
		getWinTypeAfterRaceEnd();
		setRaceRunTime(raceCounter);
		getCountOfWinType();
	}
	
	private void resetLocList() {
		locList.clear();
	}

	private void setRaceRunTime(int raceCounter) {
		raceRunTime = raceCounter * 1000;
	}

	public long getRaceRunTime() {
		return raceRunTime;
	}
	public void resetRaceRunTime(){
		raceRunTime = 0;
	}
	
	private void saveLocInList() {
		RabbitLocData locData = new RabbitLocData(rabbitFir.getLoc(), rabbitSec.getLoc(), rabbitThi.getLoc());
		locList.add(locData);
	}

	/**
	 * 直接获取比赛结果
	 */
	public void runForResultWhenNeedShutDown() {
		while (!isGameOver()) {
			winType = 0;
			// 每秒执行一次
			raceCounterForReloadOrShutDown++;
			raceRun(raceCounterForReloadOrShutDown, useSkillFirCou);
			// sendOutRabbitInfo();
		}
		getWinTypeAfterRaceEnd();
//		getCountOfWinType();
	}

	/**
	 * 获得兔子比赛结果的统计数据
	 */
	private void getCountOfWinType() {
		int winType = getWinType();
		if (winType == firWin) {
			firWinNum++;
		} else if (winType == secWin) {
			secWinNum++;
		} else if (winType == thiWin) {
			thiWinNum++;
		} else if (winType == twoWin) {
			fouWinNum++;
		} else if (winType == allWin) {
			fifWinNum++;
		}
		log.info("RabbitRace:Now First rabbit win number is[" + firWinNum + "]");
		log.info("RabbitRace:Now Second rabbit win number is[" + secWinNum + "]");
		log.info("RabbitRace:Now Third rabbit win number is[" + thiWinNum + "]");
		log.info("RabbitRace:Now Tow rabbits win number is[" + fouWinNum + "]");
		log.info("RabbitRace:Now All rabbits win number is[" + fifWinNum + "]");
		// log.info("兔子一赢的总次数为：" + firWinNum);
		// log.info("兔子二赢的总次数为：" + secWinNum);
		// log.info("兔子三赢的总次数为：" + thiWinNum);
		// log.info("两只兔子赢的总次数为：" + fouWinNum);
		// log.info("三只兔子赢的总次数为：" + fifWinNum);
	}

//	/**
//	 * 跑步开始
//	 */
//	public void raceStart() {
//		new Thread(this).start();
//	}

	/**
	 * 遍历玩家信息，向hashMap中保存的玩家发送兔子技能数值
	 */
	public void sendOutRabbitInfo() {
		log.info("Rabbit:sendOutRabbitInfo()");
		ConcurrentHashMap<Integer, Integer> map = RabbitRaceConfig.playerHashMap;
		Iterator<Entry<Integer, Integer>> iter = map.entrySet().iterator();
//		RabbitRacePlayerData player = null;
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			sendOutRabbitInfoToOnePlayer(RabbitRaceConfig.connectService,
					playerId);

		}

	}

	/**
	 * 向单个玩家发送兔子坐标
	 * 
	 * @param connectService
	 * @param player
	 */
	public void sendOutRabbitInfoToOnePlayer(ConnectService connectService,
			int playerId) {
		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
		seg.writeShort(ClientConstants.EXTEND_RABBIT_RACE);
		seg.writeInt(2);// 标识这是协议类型2
		seg.writeInts(rabbitFir.getIsUseSkill());
		seg.writeInts(rabbitSec.getIsUseSkill());
		seg.writeInts(rabbitThi.getIsUseSkill());
		seg.writeInt(RabbitRaceConfig.getCountDown());
		connectService.writeTo(seg, playerId);
	}

	public int getRabbitFirLoc(int index) {
		int maxIndex = locList.size()-1;
		if(index<0){
			index = 0;
		}else if(index > maxIndex){
			index = maxIndex;
		}
		return locList.get(index).getLocFir();
	}

	public int getRabbitSecLoc(int index) {
		int maxIndex = locList.size()-1;
		if(index<0){
			index = 0;
		}else if(index > maxIndex){
			index = maxIndex;
		}
		return locList.get(index).getLocSec();
	}

	public int getRabbitThiLoc(int index) {
		int maxIndex = locList.size()-1;
		if(index<0){
			index = 0;
		}else if(index > maxIndex){
			index = maxIndex;
		}
		return locList.get(index).getLocThi();
	}

//	public static int getCounter() {
//		return raceCounter;
//	}

	public void reset(){
		resetRabbitRace();
		runAndSaveLocInList();
	}
}
