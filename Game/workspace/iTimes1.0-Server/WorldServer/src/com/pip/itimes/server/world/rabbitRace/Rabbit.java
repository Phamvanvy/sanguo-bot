package com.pip.itimes.server.world.rabbitRace;

import java.util.Calendar;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pip.itimes.server.util.Utils;

/**
 * @author xuan.zhang 兔子赛跑中的兔子类
 */
public class Rabbit {
	static private Logger log = Logger.getLogger(Rabbit.class);
	/*
	 * 兔子的三种类型 三种兔子拥有不同的技能
	 */
	public static final int TYPE_FIR = 0;
	public static final int TYPE_SEC = 1;
	public static final int TYPE_THI = 2;

	// 技能释放时间计数器
	public static int skillCounter;

	// 随机数生成器
	public static Random rnd = new Random();

	public static Calendar cal = Calendar.getInstance();
	
	// 初始速度每秒10米
	private final int speedInit = 10;

	// 5s可以使用一次技能，最大一次5s的数值
	private final int useSkillMaxNum = 9;

	private int type; // 当前兔子类型，三种，各个技能不同
	private int loc; // 当前兔子所在位置
	private int cheerNum; // 当前兔子的支持总数
	private int speedNow; // 当前兔子的速度，每秒加的距离
	private int skillNum; // 当前兔子可以使用的技能次数，0-5随机数，初始化一次
	private int[] isUseSkill; // 保存是否使用技能，每5s尝试释放一次技能，50s内共有9次可以释放

	public Rabbit(int type, int skillNum) {
		isUseSkill = new int[useSkillMaxNum + 1];
		setType(type); // 初始化兔子类型
		setRabbitRunInfo(skillNum);
	}

	/**
	 * 设置赛跑前的数据
	 */
	private void setRabbitRunInfo(int skillNum) {
		setSpeed(speedInit); // 初始化速度
		setSkillNum(skillNum); // 初始化释放技能的次数
		setIsUseSkill(); // 初始化是否释放技能的数组
	}

	// 重置兔子的全部数据
	public void resetRabbit(int skillNum) {
		setLoc(0);
		setSpeed(speedInit);
		setSkillNum(skillNum); // 初始化释放技能的次数
		setIsUseSkill(); // 初始化是否释放技能的数组
	}

	// 初始化是否释放技能的数组，不使用设置0，使用设置1
	private void setIsUseSkill() {
		resetIsUseSkill(isUseSkill);
		for (int i = 0; i < skillNum; i++) {
			int index = Utils.getRandom(rnd, 1, useSkillMaxNum);
			if (isUseSkill[index] == 1 || isUseSkill[index] == 2) {
				// 如果当前随机产生的位置已经为1，则此次赋值无效
				i--;
			} else if (type == TYPE_SEC) {
				isUseSkill[index] = 1;
			} else {
				//加一个生成随机数的算法，结果受月份和日期影响
				int day = cal.get(Calendar.DATE);
				int month = cal.get(Calendar.MONTH) + 1;
				int random = Utils.getRandom(rnd, 1, 2);
				int result = (random + month * month + day * day) % 2;
				result++;
				isUseSkill[index] = result;
			}
		}
		
//		for(int i = 0; i <= useSkillMaxNum; i++){
//			log.info("Number of 5 second [" + i + "],isUseSkill is [" + isUseSkill[i]+"]");
//		}
	}

	// 初始化或重置是否使用技能数组
	private void resetIsUseSkill(int[] isUseSkill) {
		for (int i = 0; i < isUseSkill.length; i++) {
			isUseSkill[i] = 0;
		}
	}

	/*
	 * 兔子使用技能 技能使用的原则： 每5s尝试释放一次技能 50s必然有一只兔子到达250m处 50s内共有9次可以释放
	 * 在初始时已经初始化了释放技能的次数skillNum 具体9次中哪次释放也初始化了数组
	 */

	/**
	 * 使用技能
	 * 
	 * @param skillCount
	 *            第几次使用技能
	 * @param timeCount
	 *            本次使用技能的第几秒，兔子一技能使用两秒
	 */
	public void useSkill(int skillCount, int timeCount) {
		// 只在有意义的范围内触发技能，超出范围无意义
		if (skillCount >= 0 && skillCount <= useSkillMaxNum) {
			if (isUseSkill[skillCount] == 1 || isUseSkill[skillCount] == 2) {
				// 触发技能
				useSkillOfEachType(skillCount, timeCount);
			} else if (timeCount == 1 && getRaceCounter() != 1) {
				// 兔子一的技能一已经被触发，且已经进行了1s，并保证技能一的第二步不发生在最初的5s内
				useSkillFir(skillCount, timeCount);
			} else {
				resetSpeed();
			}
		}
	}

	public void useSkillOfEachType(int skillCount, int timeCount) {
		if (type == TYPE_FIR) {
			useSkillFir(skillCount, timeCount);
		}
		if (type == TYPE_SEC) {
			useSkillSec();
		}
		if (type == TYPE_THI) {
			useSkillThi(skillCount);
		}
	}

	private void useSkillFir(int index, int timeCount) {
		if (isUseSkill[index] == 1) {
			if (timeCount == 1) {
				speedNow = speedInit + 15;
			} else {
				setUseSkillSign();
				speedNow = speedInit + 15;
			}
		} else if (isUseSkill[index] == 2) {
			if (timeCount == 1) {
				speedNow = speedInit - 5;
			} else {
				setUseSkillSign();
				speedNow = speedInit - 5;
			}
		}

	}

	private void useSkillSec() {
		// setUseSkillSign();
		speedNow = 2 * speedInit;
	}

	private void useSkillThi(int index) {
		// setUseSkillSign();
		if (isUseSkill[index] == 1) {
			speedNow = 4 * speedInit;
		} else if (isUseSkill[index] == 2) {
			speedNow = 0;
		}
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLoc() {
		return loc;
	}

	public void setLoc(int loc) {
		this.loc = loc;
	}

	public int getCheerNum() {
		return cheerNum;
	}

	public void setCheerNum(int cheerNum) {
		this.cheerNum = cheerNum;
	}

	public int getSpeed() {
		return speedNow;
	}

	public void setSpeed(int speed) {
		this.speedNow = speed;
	}

	// 限定可以使用的技能次数
	private void setSkillNum(int skillNum) {
		// skillNum = Utils.getRandom(rnd, 0, 5);
		this.skillNum = skillNum;
	}

	// 获取当前技能使用时间计数器
	private int getRaceCounter() {
		return RabbitRace.raceCounter;
	}

	// 设置当前技能使用时间的标志，只是用于兔子一的一技能释放
	private void setUseSkillSign() {
		if (type == TYPE_FIR)
			RabbitRace.useSkillFirCou = getRaceCounter();
	}

	// 重置速度
	public void resetSpeed() {
		setSpeed(speedInit);
	}

	public int[] getIsUseSkill() {
		return isUseSkill;
	}
}
