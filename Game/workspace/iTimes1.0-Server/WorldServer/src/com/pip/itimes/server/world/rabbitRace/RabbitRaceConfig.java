package com.pip.itimes.server.world.rabbitRace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.activityService.ActivityServer;

public class RabbitRaceConfig {
	public static PlayerService playerService = null;
	public static ConnectService connectService = null;

	private static Logger log = Logger.getLogger(RabbitRaceConfig.class);

	// hashMap用来保存当前进入系统的玩家，用来确定需要给谁发送兔子坐标
	public static ConcurrentHashMap<Integer, Integer> playerHashMap = new ConcurrentHashMap<Integer, Integer>();
	public static ConcurrentHashMap<Integer, RabbitRacePlayerData> playerJettonHashMap = new ConcurrentHashMap<Integer, RabbitRacePlayerData>();

	// 以下5个列表用来保存押注信息(共5种)，用来确定需要给谁发奖
	public static ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeFirst = new ConcurrentHashMap<Integer, Integer>();
	public static ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeSecond = new ConcurrentHashMap<Integer, Integer>();
	public static ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeThird = new ConcurrentHashMap<Integer, Integer>();
	public static ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeFourth = new ConcurrentHashMap<Integer, Integer>();
	public static ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeFifth = new ConcurrentHashMap<Integer, Integer>();

	// 用以保存获得最大奖金的玩家的奖金数额，因为其奖金信息不是永久保存的内容，清除的很及时，所以用这个单独保存
	// public static HashMap<Integer, Integer> playerWinTheMostMoneyHashMap =
	// new HashMap<Integer, Integer>();
	// 用以保存获得金钱靠前的部分人的信息，每场比赛一清空
	public static List<WinPlayerData> winPlayerList = new ArrayList<WinPlayerData>();
	private static final int LIST_MAX_NUM = 5; // 保存多少个中大奖的人的信息

	private static int countDown = 0; // 倒计时
	// private static long countTime;

	public static final int moneyOfEachJetton = 10000; // 每一注1000J币

	public static final byte STATE_ACTION_WAIT = 0; // 等待开始前5分钟发公告活动开始状态
	public static final byte STATE_MESSAGE = 1; // 通告完等待活动开始状态
	public static final byte STATE_RACE_SELECT = 2; // 比赛押注状态(5分钟)
	public static final byte STATE_RACE_RUN = 3; // 比赛进行状态
	public static final byte STATE_RACE_AFTER_RUN = 4; // 比赛发奖状态
	public static final byte STATE_ACTION_END = 5; // 活动结束状态
	public static final byte STATE_RACE_PRIZE = 6;
	public static byte state = STATE_ACTION_WAIT; // 当前状态

	public static final double amendsRateOfFirWin = 3;
	public static final double amendsRateOfSecWin = 5;
	public static final double amendsRateOfThiWin = 3;
	public static final double amendsRateOfTwoWin = 4;
	public static final double amendsRateOfAllWin = 3;

	public static long SECOND = 1000; // 1秒钟
	public static long MINUTE = 60 * 1000; // 1分钟
	public static long MINUTE5 = 5 * 60 * 1000; // 5分钟
	public static long MINUTE10 = 10 * 60 * 1000; // 10分钟
	// public static long MINUTE = 10 * 1000; // 1分钟
	// public static long MINUTE5 = 30 * 1000; // 5分钟(测试用)
	// public static long MINUTE10 = 120 * 1000; // 10分钟(测试用)

	public static boolean isRunActionNow = false;
	// public static boolean isDoAfterLastRaceNow = false;
	// public static boolean isRunRunActionNow = false;
	// public static boolean isRunPrizeActionNow = false;
	// public static boolean isRunThisActionNow = false;

	// 活动开始和结束时间，用以判断
	public static long dayStartTime;
	public static long dayEndTime;

	// 用于计数器初始化时的值
	public static long raceStartTime;

	// 从xml中读出，用来设置活动时间，只用于设置活动起始和终止时间，不用与判断
	public static int startHour;
	public static int startMinute;
	public static int startSecond;
	public static int endHour;
	public static int endMinute;
	public static int endSecond;

	public static int raceMaxNum = 6; // 当前活动每天执行最大次数
	public static int raceNumForTimeControl = 0; // 当前活动每天执行次数用于时间控制
	public static int raceNum = 0; // 当前活动每天执行次数

	// public static long startActionTime = 0; // 活动开始计算时间
	// public static long endActionTime = 0; // 活动结束时间

	public static RabbitRace rabbitRace = new RabbitRace();
	public static int actionClose;

	// public static int MESSAGE;

	/**
	 * 重置时间 启动和结束的时候计算
	 */
	static public void resetTime() {
		dayStartTime = getDayDate(true);
		dayEndTime = getDayDate(false);
		raceMaxNum = (int) ((dayEndTime - dayStartTime) / MINUTE10);
	}

	/**
	 * 重置比赛控制信息
	 */
	static public void reset() {
		givePlayerPrizeWhenShutDown();
		raceNumForTimeControl = 0;
		raceNum = 0;
		raceStartTime = 0;
		resetWagerInfo();
		playerHashMap.clear();
		playerJettonHashMap.clear();
		rabbitRace.resetRabbitRace();
		state = STATE_ACTION_WAIT;
	}

	public static void reload() {
		// raceMaxNum = (int)((dayEndTime - dayStartTime)/MINUTE10);
		long now = System.currentTimeMillis();
		// resetWagerInfo();
		RabbitRaceTop.loadWinPlayerInfo();
		reloadRaceNum(now);

		if (now < dayStartTime - MINUTE5) {
			// log.info("兔子赛跑reload：活动时间未到");
			log.info("Rabbit:reloadState = STATE_ACTION_WAIT");
			state = STATE_ACTION_WAIT;
			return;
		}

		if (now >= dayEndTime) {
			// log.info("兔子赛跑reload：活动时间已过");
			log.info("Rabbit:reloadState = STATE_ACTION_END");
			state = STATE_ACTION_END;
			givePlayerPrizeWhenShutDown();
			return;
		}

		if (now >= dayStartTime - MINUTE5 && now < dayStartTime) {
			state = STATE_MESSAGE;
			// log.info("兔子赛跑reload：喊话时间段");
			log.info("Rabbit:reloadState = STATE_MESSAGE");
		} else if (now >= dayStartTime + MINUTE10 * raceNumForTimeControl
				&& now < dayStartTime + MINUTE5 + MINUTE10
						* raceNumForTimeControl) {
			// log.info("兔子赛跑reload：投票时间段");
			log.info("Rabbit:reloadState = STATE_RACE_SELECT");
			state = STATE_RACE_SELECT;
		} else if (now >= dayStartTime + MINUTE5 + MINUTE10
				* raceNumForTimeControl
				&& now < dayStartTime + MINUTE5 + rabbitRace.getRaceRunTime()
						+ MINUTE10 * raceNumForTimeControl) {
			// log.info("兔子赛跑reload：比赛时间段");
			log.info("Rabbit:reloadState = STATE_RACE_RUN");
			state = STATE_RACE_RUN;
		} else if (now >= dayStartTime + MINUTE5 + rabbitRace.getRaceRunTime()
				+ MINUTE10 * raceNumForTimeControl
				&& now < dayStartTime + MINUTE5 + MINUTE * 2
						+ rabbitRace.getRaceRunTime() + MINUTE10
						* raceNumForTimeControl) {
			// log.info("兔子赛跑reload：跑完后颁奖前时间段");
			log.info("Rabbit:reloadState = STATE_RACE_AFTER_RUN");
			state = STATE_RACE_AFTER_RUN;
		} else if (now >= dayStartTime + MINUTE5 + MINUTE * 2
				+ rabbitRace.getRaceRunTime() + MINUTE10
				* raceNumForTimeControl
				&& now < dayStartTime + MINUTE10 * (raceNumForTimeControl + 1)) {
			// log.info("兔子赛跑reload：颁奖时间段");
			log.info("Rabbit:reloadState = STATE_RACE_PRIZE");
			state = STATE_RACE_PRIZE;
			raceNumForTimeControl++;
		}
	}

	/**
	 * 用于reload时重新计算应该是第几次比赛，总共六次
	 */
	private static void reloadRaceNum(long now) {
		if (now < dayStartTime) {
			// log.info("兔子赛跑：没到比赛时间，reloadRaceNum:raceNum = 0");
			raceNumForTimeControl = 0;// 用于时间控制
			raceNum = 0;// 用于显示和倒计时
		} else if (now >= dayEndTime) {
			// log.info("兔子赛跑：超过比赛时间，reloadRaceNum:raceNum = 0");
			raceNumForTimeControl = 0;
			raceNum = 0;
		} else {
			raceNumForTimeControl = (int) ((now - dayStartTime) / MINUTE10);
			raceNum = raceNumForTimeControl + 1;

			// 打印比赛场次，raceNum从0开始
			int raceNumForReal = raceNum;
			log.info("RabbitRaceConfig:reload RaceNumber is [" + raceNumForReal
					+ "]");
		}
	}

	/**
	 * 获取开始结束时间
	 * 
	 * @param start
	 * @return
	 */
	static private long getDayDate(boolean start) {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (start) {
			cal.set(Calendar.HOUR_OF_DAY, startHour);
			cal.set(Calendar.MINUTE, startMinute);
			cal.set(Calendar.SECOND, startSecond);
		} else {
			cal.set(Calendar.HOUR_OF_DAY, endHour);
			cal.set(Calendar.MINUTE, endMinute);
			cal.set(Calendar.SECOND, endSecond);
		}
		return cal.getTime().getTime();
	}

	/**
	 * 比赛的相关进程入口
	 * 
	 * @param now
	 */
	static public void action(long now) {
		// actionAfterServiceShutDown(now);
		if (actionClose == 1) {
			return;
		}

		switch (state) {
		case STATE_ACTION_WAIT:
			if (now >= dayStartTime - MINUTE5)
				if (isRunActionNow == false) {
					isRunActionNow = true;
					state = STATE_MESSAGE;
					startNoticeMessage();
					isRunActionNow = false;
				}
			break;
		case STATE_MESSAGE:
			if (isTimeInSelectAction(now))
				if (isRunActionNow == false) {
					isRunActionNow = true;
					state = STATE_RACE_SELECT;
					selectAction();
					isRunActionNow = false;
				}
			break;
		case STATE_RACE_SELECT:
			if (isTimeInRunAction(now))
				if (isRunActionNow == false) {
					isRunActionNow = true;
					state = STATE_RACE_RUN;
					runAction(now);
					isRunActionNow = false;
				}
			break;
		case STATE_RACE_RUN:
			if (isTimeAfterRaceAction(now))
				if (isRunActionNow == false) {
					isRunActionNow = true;
					state = STATE_RACE_AFTER_RUN;
					afterRaceAction();
					isRunActionNow = false;
				}
			break;
		case STATE_RACE_AFTER_RUN:
			if (isTimeInPrizeAction(now))
				if (isRunActionNow == false) {
					isRunActionNow = true;
					state = STATE_RACE_PRIZE;
					prizeAction();
					isRunActionNow = false;
				}
			break;
		case STATE_RACE_PRIZE:
			if (now >= dayEndTime) {
				if (isRunActionNow == false) {
					isRunActionNow = true;
					state = STATE_ACTION_END;
					isRunActionNow = false;
				}
			} else if (isTimeInSelectAction(now)) {
				if (isRunActionNow == false) {
					isRunActionNow = true;
					state = STATE_RACE_SELECT;
					selectAction();
					isRunActionNow = false;
				}
			}
			break;

		default:
			break;
		}
		// if (now < dayStartTime - MINUTE5) {
		// state = STATE_ACTION_WAIT;
		// } else if (state == STATE_RACE_PRIZE && now >= dayEndTime) {
		// if (!isRunActionNow) {
		// isRunActionNow = true;
		// state = STATE_ACTION_END;
		// doAfterLastRace();
		// isRunActionNow = false;
		// }
		// } else if (state == STATE_ACTION_WAIT && now >= dayStartTime -
		// MINUTE5) {
		// startNoticeMessage();
		// } else if ((state == STATE_MESSAGE || state == STATE_RACE_PRIZE)
		// && isTimeInSelectAction(now)) {
		// selectAction();
		// } else if (state == STATE_RACE_SELECT && isTimeInRunAction(now)) {
		// runAction(now);
		// } else if (state == STATE_RACE_RUN && isTimeInPrizeAction(now)) {
		// prizeAction();
		// }

	}

	private static boolean isTimeAfterRaceAction(long now) {
		return now >= dayStartTime + MINUTE5 + rabbitRace.getRaceRunTime()
				+ MINUTE10 * raceNumForTimeControl;
	}

	private static void afterRaceAction() {
		log.info("Rabbit:State = STATE_RACE_AFTER_RACE");
		saveWinPlayerDataInTopMap();
		putWinPlayerInList();
		sendOutRabbitRaceEndInfo();
	}

	private static boolean isTimeInRunAction(long now) {
		return now >= dayStartTime + MINUTE5 + MINUTE10 * raceNumForTimeControl;
		// return now >= dayStartTime + MINUTE5 + MINUTE10 *
		// raceNumForTimeControl
		// && now < dayStartTime + MINUTE5 + rabbitRace.getRaceRunTime()
		// + MINUTE10 * raceNumForTimeControl;
	}

	// 最后一场结束后，对最后一场比赛的最后处理（没发钱的给钱、发世界聊、清数据）
	private static void doAfterLastRace() {
		sendResultToAllPlayer();
		worldMessageOfWhoWinTheMost();
		sendPrizeToPlayerWhoNotGetPrize();
		rabbitRace.resetRabbitRace();
		resetWagerInfo();
		sendGetOutMessageToAllPlayer();
		playerHashMap.clear();
		playerJettonHashMap.clear();
		setCountDown(0);// 比赛结束后，倒计时归零
		raceNum = 0;
	}

	private static boolean isTimeInPrizeAction(long now) {
		return now >= dayStartTime + MINUTE5 + MINUTE * 2
				+ rabbitRace.getRaceRunTime() + MINUTE10
				* raceNumForTimeControl;
		// return now >= dayStartTime + MINUTE5 + rabbitRace.getRaceRunTime()
		// + MINUTE10 * raceNumForTimeControl
		// && now < dayStartTime + MINUTE5 + MINUTE10
		// * (raceNumForTimeControl + 1);
	}

	/**
	 * 在比赛时间段内关闭服务器，并在比赛赛跑时间段内重启 会造成比赛没法完成进入不了下一状态，故在此把比赛置为结束状态
	 * 
	 * @param now
	 */
	// private static void actionAfterServiceShutDown(long now) {
	// boolean isOneRaceGameOver = rabbitRace.isGameOver();
	// if (!isOneRaceGameOver && state == RACERUN
	// && now >= dayStartTime + MINUTE10 * (raceNumForTimeControl + 1)) {
	// raceNumForTimeControl++;
	// state = RACEPRIZE;
	// }
	// }

	private static boolean isTimeInSelectAction(long now) {
		return now >= dayStartTime + MINUTE10 * raceNumForTimeControl;
		// return now >= dayStartTime + MINUTE10 * raceNumForTimeControl
		// && now < dayStartTime + MINUTE5 + MINUTE10
		// * raceNumForTimeControl;

	}

	/**
	 * 比赛结束后让在此系统的玩家退出
	 */
	private static void sendGetOutMessageToAllPlayer() {
		log.info("RabbitRaceConfig:sendGetOutMessageToAllPlayer()");
		Iterator<Entry<Integer, Integer>> iter = playerHashMap.entrySet()
				.iterator();
		// int playerId = 0;
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();

			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
			seg.writeShort(ClientConstants.EXTEND_RABBIT_RACE);
			seg.writeInt(6);// 标识这是协议类型6踢出协议
			connectService.writeTo(seg, playerId);

		}
	}

	/**
	 * 颁奖过程
	 */
	private static void prizeAction() {
		sendResultToAllPlayer();
		worldMessageOfWhoWinTheMost();
		sendPrizeToPlayerWhoNotGetPrize();
		raceNumForTimeControl++;
	}

	private static void saveWinPlayerDataInTopMap() {
		int winType = rabbitRace.getWinType();
		if (winType == rabbitRace.firWin) {
			putWinMapInPlayerTopMap(whoSelectWagerTypeFirst, winType,
					amendsRateOfFirWin);
		} else if (winType == rabbitRace.secWin) {
			putWinMapInPlayerTopMap(whoSelectWagerTypeSecond, winType,
					amendsRateOfSecWin);
		} else if (winType == rabbitRace.thiWin) {
			putWinMapInPlayerTopMap(whoSelectWagerTypeThird, winType,
					amendsRateOfThiWin);
		} else if (winType == rabbitRace.twoWin) {
			putWinMapInPlayerTopMap(whoSelectWagerTypeFourth, winType,
					amendsRateOfTwoWin);
		} else if (winType == rabbitRace.allWin) {
			putWinMapInPlayerTopMap(whoSelectWagerTypeFifth, winType,
					amendsRateOfAllWin);
		}
		RabbitRaceTop.saveWinPlayerData();
		RabbitRaceTop.loadWinPlayerInfo();
	}

	private static void putWinMapInPlayerTopMap(
			ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeThird2,
			int index, double amendsRate) {
		Iterator<Entry<Integer, Integer>> iter = whoSelectWagerTypeThird2
				.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			putPlayerDataInPlayerTopMap(playerId, index, amendsRate);
		}

	}

	private static void putPlayerDataInPlayerTopMap(int playerId, int index,
			double amendsRate) {
		if (RabbitRaceConfig.playerJettonHashMap.containsKey(playerId)) {
			RabbitRacePlayerData player = playerJettonHashMap.get(playerId);
			int money = (int) (player.getJettonNum(index) * moneyOfEachJetton * amendsRate);
			WinPlayerTopData winPlayerData = new WinPlayerTopData(
					player.getId(), player.getPlayerName(), money);
			RabbitRaceTop.addWinPlayerInfo(winPlayerData);
		} else {
			log.info("RabbitRaceConfig:putPlayerDataInPlayerTopMap() ID["
					+ playerId + "] in playerJettonHashMap is NULL");
		}
	}

	private static void putWinPlayerInList() {
		int winType = rabbitRace.getWinType();
		if (winType == rabbitRace.firWin) {
			putWinPlayerInList(whoSelectWagerTypeFirst, winType,
					amendsRateOfFirWin);
		} else if (winType == rabbitRace.secWin) {
			putWinPlayerInList(whoSelectWagerTypeSecond, winType,
					amendsRateOfSecWin);
		} else if (winType == rabbitRace.thiWin) {
			putWinPlayerInList(whoSelectWagerTypeThird, winType,
					amendsRateOfThiWin);
		} else if (winType == rabbitRace.twoWin) {
			putWinPlayerInList(whoSelectWagerTypeFourth, winType,
					amendsRateOfTwoWin);
		} else if (winType == rabbitRace.allWin) {
			putWinPlayerInList(whoSelectWagerTypeFifth, winType,
					amendsRateOfAllWin);
		}
	}

	private static void putWinPlayerInList(
			ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeFirst2,
			int index, double amendsRate) {
		Iterator<Entry<Integer, Integer>> iter = whoSelectWagerTypeFirst2
				.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			// log.info("RabbitRaceConfig:服务器为" + player.getPlayerName()
			// + "补发未中奖的返钱");
			setPlayerInWinMostList(playerId, index, amendsRate);
			// playerWinTheMostMoneyHashMap.put(player.getId(), money);
		}
		// return winPlayerList;
	}

	private static void setPlayerInWinMostList(int playerId, int index,
			double amendsRate) {
		RabbitRacePlayerData player = playerJettonHashMap.get(playerId);
		if (player != null) {

			int money = (int) (player.getJettonNum(index) * moneyOfEachJetton * amendsRate);

			WinPlayerData winPlayerData = new WinPlayerData(player, money);

			if (winPlayerList.isEmpty()) {
				winPlayerList.add(winPlayerData);
				return;
			}

			int listIndex = winPlayerList.size();
			while (listIndex > 0
					&& money > winPlayerList.get(listIndex - 1).getMoney()) {
				listIndex--;
			}

			winPlayerList.add(listIndex, winPlayerData);

			if (winPlayerList.size() > LIST_MAX_NUM) {
				winPlayerList.remove(winPlayerList.size() - 1);
			}
		} else {
			log.info("RabbitRaceConfig:setPlayerInWinMostList() ID[" + playerId
					+ "] in playerJettonHashMap is NULL");
		}

	}

	/**
	 * 为中途退出，但下了注的玩家颁奖 每次比赛后调用一次
	 */
	private static void sendPrizeToPlayerWhoNotGetPrize() {
		ConcurrentHashMap<Integer, Integer> wagerType = getWinPlayerMap();
		double amendsRate = getWinAmendsRate();
		int winType = rabbitRace.getWinType();
		log.info("RabbitRaceConfig:sendPrizeToPlayerWhoNotGetPrize()");
		sendPrizeToPlayerWhoNotGetPrize(wagerType, winType, amendsRate);

		sendMoneyIfMapsNotNull(whoSelectWagerTypeFirst);
		sendMoneyIfMapsNotNull(whoSelectWagerTypeSecond);
		sendMoneyIfMapsNotNull(whoSelectWagerTypeThird);
		sendMoneyIfMapsNotNull(whoSelectWagerTypeFourth);
		sendMoneyIfMapsNotNull(whoSelectWagerTypeFifth);
	}

	/**
	 * 要是还有玩家下注了，但是没返钱，就返钱
	 * 
	 * @param whoSelectWagerTypeFirst2
	 */
	private static void sendMoneyIfMapsNotNull(
			ConcurrentHashMap<Integer, Integer> whoSelectWagerTypeFirst2) {
		Iterator<Entry<Integer, Integer>> iter = whoSelectWagerTypeFirst2
				.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			// log.info("RabbitRaceConfig:服务器为" + player.getPlayerName()
			// + "补发未中奖的返钱");
			sendMoneyIfPlayerHaveJetton(playerId);
		}
	}

	/**
	 * 为中途退出，但下了注的玩家颁奖
	 * 
	 * @param wagerType
	 *            下注信息的Map
	 */
	private static void sendPrizeToPlayerWhoNotGetPrize(
			ConcurrentHashMap<Integer, Integer> wagerType, int winType,
			double amendsRate) {
		log.info("RabbitRaceConfig:WinType Map size is[" + wagerType.size()
				+ "]");
		Iterator<Entry<Integer, Integer>> iter = wagerType.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			// log.info("RabbitRaceConfig:Service send prize to" +
			// player.getPlayerName()
			// + "beacause he don't get prize.");
			sendMoneyToWinPlayer(playerId, winType, amendsRate);
			// sendMoneyToWinPlayer(wagerType, player, winType, amendsRate);
			// SendMessage
		}
	}

	/**
	 * 获得获胜的玩家下注Map
	 */
	private static ConcurrentHashMap<Integer, Integer> getWinPlayerMap() {
		int winType = rabbitRace.getWinType();
		ConcurrentHashMap<Integer, Integer> winHashMap = null;
		if (winType == rabbitRace.firWin) {
			winHashMap = whoSelectWagerTypeFirst;
		}
		if (winType == rabbitRace.secWin) {
			winHashMap = whoSelectWagerTypeSecond;
		}
		if (winType == rabbitRace.thiWin) {
			winHashMap = whoSelectWagerTypeThird;
		}
		if (winType == rabbitRace.twoWin) {
			winHashMap = whoSelectWagerTypeFourth;
		}
		if (winType == rabbitRace.allWin) {
			winHashMap = whoSelectWagerTypeFifth;
		}
		return winHashMap;
	}

	/**
	 * 获得获胜的玩家下注赔率
	 */
	private static double getWinAmendsRate() {
		int winType = rabbitRace.getWinType();
		double amendsRate = 1;
		if (winType == rabbitRace.firWin) {
			amendsRate = amendsRateOfFirWin;
		} else if (winType == rabbitRace.secWin) {
			amendsRate = amendsRateOfSecWin;
		} else if (winType == rabbitRace.thiWin) {
			amendsRate = amendsRateOfThiWin;
		} else if (winType == rabbitRace.twoWin) {
			amendsRate = amendsRateOfTwoWin;
		} else if (winType == rabbitRace.allWin) {
			amendsRate = amendsRateOfAllWin;
		}
		return amendsRate;
	}

	/**
	 * 颁奖完重置押注信息
	 */
	private static void resetWagerInfo() {
		resetAllPlayerJettonNums();
		whoSelectWagerTypeFifth.clear();
		whoSelectWagerTypeFirst.clear();
		whoSelectWagerTypeFourth.clear();
		whoSelectWagerTypeSecond.clear();
		whoSelectWagerTypeThird.clear();
	}

	/**
	 * 重置全部玩家的投注信息
	 */
	private static void resetAllPlayerJettonNums() {
		Iterator<Entry<Integer, Integer>> iter = playerHashMap.entrySet()
				.iterator();
		// RabbitRacePlayerData player = null;
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			resetPlayerJettonNums(playerId);

		}
	}

	/**
	 * 清除玩家的下注信息
	 * 
	 * @param player
	 */
	private static void resetPlayerJettonNums(int playerId) {
		RabbitRacePlayerData player = playerJettonHashMap.get(playerId);
		if (player != null) {
			player.resetJettonNumsAfterRace(); // 将玩家的下注信息清零
		} else {
			log.info("RabbitRaceConfig:resetPlayerJettonNums() ID[" + playerId
					+ "] is NULL");
		}
	}

	// /**
	// * 判断是否需要为玩家重置押注信息 确保玩家在一场比赛中掉线再上押注信息不变
	// *
	// * @param player
	// */
	// public static void isResetPlayerJettonNums(int playerId) {
	// if (!isPlayerWagerInThisRace(playerId)) {
	// resetPlayerJettonNums(playerId);
	// }
	// }

	// /**
	// * 玩家原本有下注注数，则照原下注注数恢复
	// */
	// private static void resetPlayerJettonNumsWithOldJettonNums(int playerId)
	// {
	// RabbitRacePlayerData playerData = playerJettonHashMap.get(playerId);
	// int jettonNumFir = playerData.getJettonNumFir();
	// int jettonNumSec = playerData.getJettonNumFir();
	// int jettonNumThi = playerData.getJettonNumFir();
	// int jettonNumFou = playerData.getJettonNumFir();
	// int jettonNumFif = playerData.getJettonNumFir();
	// // player.setJettonNumFir(jettonNumFir);
	// }

	/**
	 * 给玩家颁奖，把钱下发给胜利玩家 这个方法调用是在玩家一直在比赛界面才调用，针对单个玩家
	 * 若玩家中途退出，会在一场比赛结束时，对全部未发钱玩家进行结算时给钱
	 */
	public static void givePlayerPrize(int playerId) {
		int winType = rabbitRace.getWinType();
		// HashMap<Integer, PlayerData> winPlayerMap = getWinPlayerMap();
		double amendsRate = getWinAmendsRate();
		sendMoneyToWinPlayer(playerId, winType, amendsRate);
		// sendMoneyToWinPlayer(winPlayerMap, player, winType, amendsRate);
	}

	/**
	 * 把钱下发给玩家
	 * 
	 * @param player2
	 * 
	 * @param amendsRate
	 * 
	 * @param whoSelectWagerTyprFirst2
	 */
	private static void sendMoneyToWinPlayer(int playerId, int winType,
			double amendsRate) {
		if (playerJettonHashMap.containsKey(playerId)) {
			RabbitRacePlayerData player = playerJettonHashMap.get(playerId);
			// 赔率使用double表示的，需要做一下转换。用double是因为考虑到赔率不会很整，为适应可能的变换先不动，用的不是很多
			int money = (int) (player.getJettonNum(winType) * moneyOfEachJetton * amendsRate);
			log.info("RabbitRaceConfig:ID[" + player.getId() + "] Name["
					+ player.getPlayerName()
					+ "] win in Rabbit Race,so give him [" + money + "]J.");
			sendMoneyToPlayer(player, money, 0);
		} else {
			log.info("RabbitRaceConfig:playerJettonHashMap ID[" + playerId
					+ "] is NULL");
		}
	}

	// private static void sendMoneyToWinPlayer(
	// HashMap<Integer, PlayerData> playerWagerType, PlayerData player,
	// int winType, double amendsRate) {
	// if (playerWagerType.containsKey(player.getId())) {
	// // int jettonNumIndex = winType;
	// int money = (int) (player.getJettonNum(winType)
	// * moneyOfEachJetton * amendsRate);
	// sendMoneyToPlayer(player, money);
	// }
	// }

	// private static String getMailString(PlayerData player) {
	// int raceNumNow = raceNum + 1;
	// String resultStr = "您在兔兔百米大赛第" + raceNumNow + "场比赛中下注猜测：";
	// if (isHaveJettonOfFir(player)) {
	// resultStr += "1.小蓝兔胜.";
	// }
	// if (isHaveJettonOfFir(player)) {
	// resultStr += "2.小粉兔胜.";
	// }
	// if (isHaveJettonOfFir(player)) {
	// resultStr += "3.小蓝兔胜.";
	// }
	// if (isHaveJettonOfFir(player)) {
	// resultStr += "4.两只小兔子同时到达.";
	// }
	// if (isHaveJettonOfFir(player)) {
	// resultStr += "5.三只小兔子同时到达.";
	// }
	// resultStr += getWinTypeWorldMessageStr();
	// return resultStr;
	// }

	/**
	 * 把钱给玩家
	 * 
	 * @param player
	 * @param amendsRate2
	 */
	private static void sendMoneyToPlayer(RabbitRacePlayerData player,
			int money, int type) {
		// player.resetJettonNumsAfterRace();
		int winTypeNow = rabbitRace.getWinType();
		String resultStr = getWinTypeWorldMessageStr();
		if (type == 0) {
			int reduceMoney = (int)(money * 0.2);
			int resultMoney = money - reduceMoney;
			byte[] bytes = ItemUtils.money2dbAttachment(resultMoney);
			ActivityServer.server.getMailService().sendMail(
					player.getId(),
					player.getPlayerName(),
					-1,
					"系统",
					"兔兔百米大赛中奖邮件",
					resultStr + "恭喜您在兔兔百米大赛中猜中结果，获得奖金" + money + "J，扣除奥运会建造费用"
							+ reduceMoney + "J，实际获得" + resultMoney + "J",
					bytes, 0, false);

			log.info("RabbitRaceConfig:Race number[" + raceNum + "] WinType is"
					+ winTypeNow + "ID[" + player.getId() + "] Name["
					+ player.getPlayerName() + "] win, get email which have ["
					+ resultMoney + "]J money.His jettonNum[0] is["
					+ player.getJettonNum(0) + "] jettonNum[1] is["
					+ player.getJettonNum(1) + "] jettonNum[2] is["
					+ player.getJettonNum(2) + "] jettonNum[3] is["
					+ player.getJettonNum(3) + "] jettonNum[4] is["
					+ player.getJettonNum(4) + "]");
		} else {
			byte[] bytes = ItemUtils.money2dbAttachment(money);
			ActivityServer.server.getMailService().sendMail(
					player.getId(),
					player.getPlayerName(),
					-1,
					"系统",
					"兔兔百米大赛系统赠送",
					resultStr + "您在兔兔百米大赛中未猜中结果，感谢您参与兔子百米大赛。活动期间，系统赠送" + money
							+ "J ", bytes, 0, false);
			log.info("RabbitRaceConfig:Race number[" + raceNum + "] WinType is"
					+ winTypeNow + "ID[" + player.getId() + "] Name["
					+ player.getPlayerName()
					+ "] don't win, get email which have [" + money
					+ "]J money.His jettonNum[0] is[" + player.getJettonNum(0)
					+ "] jettonNum[1] is[" + player.getJettonNum(1)
					+ "] jettonNum[2] is[" + player.getJettonNum(2)
					+ "] jettonNum[3] is[" + player.getJettonNum(3)
					+ "] jettonNum[4] is[" + player.getJettonNum(4) + "]");

		}
		removePlayerFromMap(player);
		resetPlayerJettonNums(player.getId());

		// log.info("RabbitRaceConfig:服务器给" + player.getPlayerName() + "发钱，数量"
		// + money + "J");
	}

	/**
	 * 赛跑过程
	 */
	private static void runAction(long now) {
		log.info("Rabbit:state = STATE_RACE_RUN");
		RabbitRace.raceCounter = 0;
		raceStartTime = now;
		rabbitRace.sendOutRabbitInfo();
		// rabbitRace.raceStart();
	}

	/**
	 * 押注过程，5种
	 */
	private static void selectAction() {
		log.info("Rabbit:State = STATE_RACE_SELECT");
		if (raceNumForTimeControl == 0) {
			RabbitRaceTop.resetWinPlayerList();
		}
		raceNum++;
		rabbitRace.reset();
		resetWagerInfo();
		sendOutRabbitRaceStartInfo();

	}

	private static void worldMessageOfWhoWinTheMost() {
		if (winPlayerList.isEmpty()) {
			return;
		}

		boolean isOver = false;
		int maxMoney = winPlayerList.get(0).getMoney();
		Iterator<WinPlayerData> iterator = winPlayerList.iterator();

		while (iterator.hasNext() && !isOver) {
			WinPlayerData playerData = iterator.next();
			if (playerData.getMoney() < maxMoney) {
				isOver = true;
			} else {
				// PlayerData player = iterator
				int raceNumNow = raceNum;
				Server.instance.chatService.sendWorldMessage(
						-1,
						"系统",
						"兔兔百米大赛 第" + raceNumNow + "场"
								+ playerData.getPlayer().getPlayerName()
								+ "在比赛中以神鬼莫测之能大手笔押对结果夺得头筹，获得"
								+ playerData.getMoney() + "J");
			}
		}

		winPlayerList.clear();
		log.info("RabbitRaceConfig:worldMessageOfWhoWinTheMost()");
	}

	/**
	 * 将比赛结果发世界聊
	 */
	private static void sendResultToAllPlayer() {
		String resultStr = getWinTypeWorldMessageStr();
		Server.instance.chatService.sendWorldMessage(-1, "系统", "兔兔百米大赛"
				+ resultStr);
		log.info("RabbitRaceConfig:sendReaultToAllPlayer()");
	}

	private static String getWinTypeWorldMessageStr() {
		int result = rabbitRace.getWinType();
		String resultStr = null;
		int raceNumNow = raceNum;
		if (result == rabbitRace.firWin) {
			resultStr = "第" + raceNumNow + "场比赛小蓝兔获胜！";
		} else if (result == rabbitRace.secWin) {
			resultStr = "第" + raceNumNow + "场比赛小粉兔获胜！";
		} else if (result == rabbitRace.thiWin) {
			resultStr = "第" + raceNumNow + "场比赛小黄兔获胜！";
		} else if (result == rabbitRace.twoWin) {
			resultStr = "第" + raceNumNow + "场比赛有两只兔子同时到达！";
		} else if (result == rabbitRace.allWin) {
			resultStr = "第" + raceNumNow + "场比赛全部兔子同时到达！";
		}
		return resultStr;
	}

	/**
	 * 开始前的喊话
	 */
	private static void startNoticeMessage() {
		raceNumForTimeControl = 0;// 为确保安全，对此值进行初始化
		Server.instance.chatService
				.sendRoarMessage(
						-1,
						"狮子吼",
						"5分钟后开始“兔兔百米大赛”，从宠物菜单下宠物奥运会进入，下注猜测哪只会赢，压中就有大反馈哦。活动时间为每日12:00-13:00。",
						true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD,
						(short) 0);
		log.info("RabbitRaceConfig:State is WaitActionState");
	}

	/**
	 * 当前是否可以下注 此方法用在接收投注协议时判断其是否有效
	 * 
	 * @param now
	 *            当前时间
	 */
	public static boolean isCanWager() {
		long now = System.currentTimeMillis();
		boolean wager = false;

		if (now >= dayStartTime + MINUTE10 * raceNumForTimeControl
				&& now < dayStartTime + MINUTE5 + MINUTE10
						* raceNumForTimeControl && state == STATE_RACE_SELECT) {
			wager = true;
		}
		return wager;
	}

	/**
	 * 当前是否可以下发结束信息
	 * 
	 * @param now
	 *            当前时间
	 */
	public static boolean isCanGiveResult() {
		long now = System.currentTimeMillis();
		boolean wager = false;
		if (state == STATE_RACE_AFTER_RUN) {
			wager = true;
		}
		return wager;
	}

	public static void setPlayerInHashMap(WorldPlayer player) {
		if (!playerHashMap.containsKey(player.getId())) {
			playerHashMap.put(player.getId(), player.getId());
		}
		if (!playerJettonHashMap.containsKey(player.getId())) {
			RabbitRacePlayerData playerData = new RabbitRacePlayerData(
					player.getId(), player.getPlayerName());
			playerJettonHashMap.put(player.getId(), playerData);
		}
	}

	/**
	 * 将当前的状态信息发送下去
	 * 
	 * @return
	 */
	public static int getNowState() {
		int clientState = state;
		if (clientState == STATE_RACE_PRIZE) {
			clientState = STATE_RACE_AFTER_RUN;
		}
		return clientState;
	}

	/**
	 * 判断是否把此玩家信息放入hashMap中 只有以下三种状态下进入的玩家才登记
	 * 
	 * @return
	 */
	public static boolean isRegisterPalyer() {
		boolean register = false;
		if (state == STATE_RACE_SELECT || state == STATE_RACE_RUN
				|| state == STATE_RACE_AFTER_RUN || state == STATE_MESSAGE
				|| state == STATE_RACE_PRIZE) {
			register = true;
		}
		return register;
	}

	public static int[] getRabbitFirIsUseSkill() {
		return rabbitRace.rabbitFir.getIsUseSkill();
	}

	public static int[] getRabbitSecIsUseSkill() {
		return rabbitRace.rabbitSec.getIsUseSkill();
	}

	public static int[] getRabbitThiIsUseSkill() {
		return rabbitRace.rabbitThi.getIsUseSkill();
	}

	public static int getRabbitFirLoc(int counter) {
		return rabbitRace.getRabbitFirLoc(counter);
	}

	public static int getRabbitSecLoc(int counter) {
		return rabbitRace.getRabbitSecLoc(counter);
	}

	public static int getRabbitThiLoc(int counter) {
		return rabbitRace.getRabbitThiLoc(counter);
	}

	public static int getRaceCounter() {
		long clintTime = System.currentTimeMillis();
		int counter = 0;
		// if(isTimeInRunAction(now)){
		// counter = (int) ((now - (dayStartTime + MINUTE5 + MINUTE10
		// * raceNumForTimeControl)) / 1000);
		// }
		// long now = (long)(clintTime * 1000);
		long raceEndTime = dayStartTime + MINUTE5 + MINUTE10
				* raceNumForTimeControl;
		if (isTimeInRunAction(clintTime)) {
			counter = (int) ((clintTime - raceEndTime) / 1000);
		} else if (isTimeInPrizeAction(clintTime)) {
			counter = RabbitRace.locList.size() - 1;
			// log.info("返回的raceCounter:" + counter);
		}
		return counter;
		// return RabbitRace.getCounter();
	}

	// public void

	/**
	 * 遍历玩家信息，向hashMap中保存的玩家发送比赛开始信息
	 */
	public static void sendOutRabbitRaceStartInfo() {
		log.info("RabbitRaceConfig:sendOutRabbitRaceStartInfo()");
		Iterator<Entry<Integer, Integer>> iter = playerHashMap.entrySet()
				.iterator();
		// RabbitRacePlayerData player = null;
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			sendOutStartInfoToOnePlayer(connectService, playerId);
		}
	}

	/**
	 * 向单个玩家发送比赛开始信息
	 * 
	 * @param connectService
	 * @param player
	 */
	private static void sendOutStartInfoToOnePlayer(
			ConnectService connectService, int playerId) {
		if (RabbitRaceConfig.playerJettonHashMap.containsKey(playerId)) {
			RabbitRacePlayerData playerData = playerJettonHashMap.get(playerId);
			UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
			seg.writeShort(ClientConstants.EXTEND_RABBIT_RACE);
			seg.writeInt(5);// 标识这是协议类型5
			seg.writeInt(STATE_RACE_SELECT);
			seg.writeInt(raceNum - 1);
			seg.writeInts(playerData.getJettonNums());
			seg.writeInt(getCountDown());
			connectService.writeTo(seg, playerId);
			// log.info("RabbitRaceConfig:服务器向玩家：" + player.getPlayerName()
			// + "发送比赛开始信息");
		} else {
			log.info("RabbitRaceConfig:senOutStartInfoToOnePlayer() ID["
					+ playerId + "] in playerJettonHashMap is NULL");
		}
	}

	/**
	 * 遍历玩家信息，向hashMap中保存的玩家发送结束信息
	 */
	public static void sendOutRabbitRaceEndInfo() {
		log.info("RabbitRaceConfig:sendOutRabbitRaceEndInfo()");
		Iterator<Entry<Integer, Integer>> iter = playerHashMap.entrySet()
				.iterator();
		// RabbitRacePlayerData player = null;
		while (iter.hasNext()) {
			Entry<Integer, Integer> entry = iter.next();
			int playerId = entry.getValue();
			sendOutWinTypeToOnePlayer(connectService, playerId);
		}
	}

	/**
	 * 向单个玩家发送结束信息
	 * 
	 * @param connectService
	 * @param player
	 */
	public static void sendOutWinTypeToOnePlayer(ConnectService connectService,
			int playerId) {
		int winType = rabbitRace.getWinType();
		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
		seg.writeShort(ClientConstants.EXTEND_RABBIT_RACE);
		seg.writeInt(3);// 标识这是协议类型3
		seg.writeInt(winType);
		connectService.writeTo(seg, playerId);
		// log.info("RabbitRaceConfig:服务器向玩家：" + player.getPlayerName()
		// + "发送结束信息，获胜类型:" + winType);
	}

	/**
	 * 判断玩家是否押注胜利
	 * 
	 * @return
	 */
	public static boolean isPlayerWin(int playerId) {
		boolean isWin = false;
		int winType = rabbitRace.getWinType();
		// int playerId = player.getId();
		if (winType == rabbitRace.firWin
				&& whoSelectWagerTypeFirst.containsKey(playerId)) {
			isWin = true;
		}
		if (winType == rabbitRace.secWin
				&& whoSelectWagerTypeSecond.containsKey(playerId)) {
			isWin = true;
		}
		if (winType == rabbitRace.thiWin
				&& whoSelectWagerTypeThird.containsKey(playerId)) {
			isWin = true;
		}
		if (winType == rabbitRace.twoWin
				&& whoSelectWagerTypeFourth.containsKey(playerId)) {
			isWin = true;
		}
		if (winType == rabbitRace.allWin
				&& whoSelectWagerTypeFifth.containsKey(playerId)) {
			isWin = true;
		}
		return isWin;
	}

	/**
	 * 判断玩家是否在本场比赛中押注了
	 */
	public static boolean isPlayerWagerInThisRace(int playerId) {
		boolean isSelectFir = whoSelectWagerTypeFirst.containsKey(playerId);
		boolean isSelectSec = whoSelectWagerTypeSecond.containsKey(playerId);
		boolean isSelectThi = whoSelectWagerTypeThird.containsKey(playerId);
		boolean isSelectFou = whoSelectWagerTypeFourth.containsKey(playerId);
		boolean isSelectFif = whoSelectWagerTypeFifth.containsKey(playerId);
		boolean wagerInThisRace = false;
		if (isSelectFif || isSelectFir || isSelectFou || isSelectSec
				|| isSelectThi) {
			wagerInThisRace = true;
		}
		// log.info("RabbitRaceConfig:玩家" + player.getPlayerName() + "的下注情况");
		return wagerInThisRace;
	}

	private static boolean isHaveJettonOfFir(int playerId) {
		return whoSelectWagerTypeFirst.containsKey(playerId);
	}

	private static boolean isHaveJettonOfSec(int playerId) {
		return whoSelectWagerTypeSecond.containsKey(playerId);
	}

	private static boolean isHaveJettonOfThi(int playerId) {
		return whoSelectWagerTypeThird.containsKey(playerId);
	}

	private static boolean isHaveJettonOfFou(int playerId) {
		return whoSelectWagerTypeFourth.containsKey(playerId);
	}

	private static boolean isHaveJettonOfFif(int playerId) {
		return whoSelectWagerTypeFifth.containsKey(playerId);
	}

	/**
	 * 给没有胜利的玩家返还20%
	 * 
	 * @return
	 */
	public static void sendMoneyIfPlayerHaveJetton(int playerId) {
		int jettonNum = 0;
		if (playerJettonHashMap.containsKey(playerId)) {
			RabbitRacePlayerData player = playerJettonHashMap.get(playerId);
			boolean isSelectFir = isHaveJettonOfFir(playerId);
			boolean isSelectSec = isHaveJettonOfSec(playerId);
			boolean isSelectThi = isHaveJettonOfThi(playerId);
			boolean isSelectFou = isHaveJettonOfFou(playerId);
			boolean isSelectFif = isHaveJettonOfFif(playerId);

			if (isSelectFir) {
				jettonNum += player.getJettonNum(0);
			}
			if (isSelectSec) {
				jettonNum += player.getJettonNum(1);
			}
			if (isSelectThi) {
				jettonNum += player.getJettonNum(2);
			}
			if (isSelectFou) {
				jettonNum += player.getJettonNum(3);
			}
			if (isSelectFif) {
				jettonNum += player.getJettonNum(4);
			}

			if (jettonNum != 0) {
				int money = jettonNum * (moneyOfEachJetton / 5);
				int raceNumNow = raceNum;
				log.info("RabbitRaceConfig:Race number["
						+ raceNumNow
						+ "]ID["
						+ playerId
						+ "] Name["
						+ player.getPlayerName()
						+ "] have jettonNums , but not win ,so give him 20% money.The num of money is["
						+ money + "]J.");
				sendMoneyToPlayer(player, money, 1);
			}
		} else {
			log.info("RabbitRaceConfig:sendMoneyIfPlayerHaveJetton():ID["
					+ playerId + "] in playerJettonHashMap is NULL");
		}

		// log.info("RabbitRaceConfig:玩家" + player.getPlayerName() + "的总注数"
		// + jettonNum);

	}

	public static String getWinRabbitName() {
		String nameStr = null;
		int winType = rabbitRace.getWinType();
		if (winType == rabbitRace.firWin) {
			nameStr = "小蓝兔获胜";
		} else if (winType == rabbitRace.secWin) {
			nameStr = "小粉兔获胜";
		} else if (winType == rabbitRace.thiWin) {
			nameStr = "小黄兔获胜";
		} else if (winType == rabbitRace.twoWin) {
			nameStr = "两只兔子同时到达";
		} else if (winType == rabbitRace.allWin) {
			nameStr = "三只兔子同时到达";
		}
		return nameStr;

	}

	/**
	 * 将玩家从各个投注Map中清除
	 */
	public static void removePlayerFromMap(RabbitRacePlayerData player) {
		int playerId = player.getId();
		if (whoSelectWagerTypeFirst.containsKey(playerId)) {
			whoSelectWagerTypeFirst.remove(playerId);
		}
		if (whoSelectWagerTypeSecond.containsKey(playerId)) {
			whoSelectWagerTypeSecond.remove(playerId);
		}
		if (whoSelectWagerTypeThird.containsKey(playerId)) {
			whoSelectWagerTypeThird.remove(playerId);
		}
		if (whoSelectWagerTypeFourth.containsKey(playerId)) {
			whoSelectWagerTypeFourth.remove(playerId);
		}
		if (whoSelectWagerTypeFifth.containsKey(playerId)) {
			whoSelectWagerTypeFifth.remove(playerId);
		}
	}

	/**
	 * 将玩家放入相应的下注名单Map
	 * 
	 * @param wagerType
	 * @param player
	 */
	public static void putPlayerInWagerTypeMap(int wagerType, int playerId) {
		// int playerId = player.getId();
		if (wagerType == 0 && !whoSelectWagerTypeFirst.containsKey(playerId)) {
			whoSelectWagerTypeFirst.put(playerId, playerId);
		} else if (wagerType == 1
				&& !whoSelectWagerTypeSecond.containsKey(playerId)) {
			whoSelectWagerTypeSecond.put(playerId, playerId);
		} else if (wagerType == 2
				&& !whoSelectWagerTypeThird.containsKey(playerId)) {
			whoSelectWagerTypeThird.put(playerId, playerId);
		} else if (wagerType == 3
				&& !whoSelectWagerTypeFourth.containsKey(playerId)) {
			whoSelectWagerTypeFourth.put(playerId, playerId);
		} else if (wagerType == 4
				&& !whoSelectWagerTypeFifth.containsKey(playerId)) {
			whoSelectWagerTypeFifth.put(playerId, playerId);
		}
	}

	/**
	 * 获取倒计时时间
	 * 
	 * @return
	 */
	public static int getCountDown() {
		long now = System.currentTimeMillis();
		int raceNumNow = raceNum;
		if (raceNumNow > 0) {
			raceNumNow = raceNumNow - 1;
		}
		if (now < dayStartTime - MINUTE5) {
			countDown = 0;
		} else if (now >= dayEndTime) {
			// 比赛结束后，倒计时归零
			countDown = 0;
		} else if (now >= dayStartTime - MINUTE5 && now < dayStartTime) {
			// 喊完话等待开始的5分钟内
			countDown = (int) ((dayStartTime - now) / SECOND);

		} else if (now >= dayStartTime + MINUTE10 * raceNumNow
				&& now < dayStartTime + MINUTE5 + MINUTE10 * raceNumNow) {
			// 可以投票的5分内
			countDown = (int) ((dayStartTime + MINUTE5 + MINUTE10 * raceNumNow - now) / SECOND);

		} else if (now >= dayStartTime + MINUTE10 * raceNumNow + MINUTE5
				&& now < dayStartTime + MINUTE10 * (raceNumNow + 1)) {
			// 投票后的五分钟
			countDown = (int) ((dayStartTime + MINUTE10 * (raceNumNow + 1) - now) / SECOND);
		}
		return countDown;
	}

	public static void setCountDown(int countDown) {
		RabbitRaceConfig.countDown = countDown;
	}

	/**
	 * shutDown时发奖
	 */
	public static void givePlayerPrizeWhenShutDown() {
		rabbitRace.runForResultWhenNeedShutDown();
		sendPrizeToPlayerWhoNotGetPrize();
	}

	public static void sendWinPlayerTopList() {

	}

}
