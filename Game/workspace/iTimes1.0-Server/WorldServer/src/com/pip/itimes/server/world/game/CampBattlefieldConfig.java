package com.pip.itimes.server.world.game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.CampBattlefieldService;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * 阵营战场配置
 * @author hchen
 *
 */
public class CampBattlefieldConfig {
	/**
	 * 艾克斯资源争夺战
	 * 常量定义：战场类型
	 */
	public static final String CAMP_BATTLEFIELD_TYPE_RESOURCES = "resources";
	/**
	 * 艾克斯资源争夺战
	 * 常量定义：战场名称
	 */
	public static final String CAMP_BATTLEFIELD_NAME_RESOURCES = "黑龙元素军团战场（阵营分边）";
	
	public static final String CAMP_BATTLEFIELD_NAME_RESOURCES2 = "黑龙元素军团战场（随机分边）";
	
	/**
	 * 死灰峡谷复兴战
	 * 常量定义：战场类型
	 */
	public static final String CAMP_BATTLEFIELD_TYPE_RESUSCITATION = "resuscitation";
	/**
	 * 死灰峡谷复兴战
	 * 常量定义：战场名称
	 */
	public static final String CAMP_BATTLEFIELD_NAME_RESUSCITATION = "死灰峡谷复兴战";
	
	/**
	 * 闪金沙漠生存战
	 * 常量定义：战场类型
	 */
	public static final String CAMP_BATTLEFIELD_TYPE_SURVIVAL = "survival";
	/**
	 * 闪金沙漠生存战
	 * 常量定义：战场名称
	 */
	public static final String CAMP_BATTLEFIELD_NAME_SURVIVAL = "闪金沙漠生存战";
	
	/**
	 * 毒雾沼泽王者之战
	 * 常量定义：战场类型
	 */
	public static final String CAMP_BATTLEFIELD_TYPE_KING = "king";
	/**
	 * 毒雾沼泽王者之战
	 * 常量定义：战场名称
	 */
	public static final String CAMP_BATTLEFIELD_NAME_KING = "毒雾沼泽王者之战";
	
	/**
	 * 高炎溶洞阵地战
	 * 常量定义：战场类型
	 */
	public static final String CAMP_BATTLEFIELD_TYPE_POSITION = "position";
	/**
	 * 高炎溶洞阵地战
	 * 常量定义：战场名称
	 */
	public static final String CAMP_BATTLEFIELD_NAME_POSITION = "高炎溶洞阵地战";
	
	/**
	 * 默认杀戮点数
	 */
	public static final int DEFAULT_POINTS = 5;
	/**
	 * 世界地图ID
	 */
	public static final short WORLD_MAP = 177;
	/**
	 * 阵营战场：随机进入时除了rate外增加的基数
	 */
	public static final int AWARD_BASE = 1;
	/**
     * 阵营战场：每一分钟查看是否有未开启的战场
     */
    public static final long TIME_OPEN_BATTLEFIELD = 1000L * 60;
	/**
	 * 阵营战场：等待玩家进入战场的时间
	 */
	public static final long TIME_LIMIT_START = 1000L * 75;
	/**
	 * 阵营战场：发奖的时间限制
	 */
	public static final long TIME_LIMIT_PRIZES = 1000L * 60 * 5;
	/**
	 * 阵营战场：战场结束前停止拽入战场的时间
	 */
	public static final long TIME_LIMIT_JOIN = 1000L * 60 * 3;
	/**
	 * 阵营战场：进入限制
	 */
	public static final int INTO_LIMIT_LEVEL = 19;
	
	/**
	 * 阵营战场错误：战场排队在CD中
	 */
	public static final int ERROR_IS_COOLING = -10;
	/**
	 * 阵营战场错误：没有此类战场
	 */
	public static final int ERROR_WITHOUT_BATTLEFIELD = -11;
	/**
	 * 阵营战场错误：已在战场队列中
	 */
	public static final int ERROR_READY_IN_QUEUE = -12;
	/**
	 * 阵营战场错误：已在其他副本中
	 */
	public static final int ERROR_READY_IN_INSTANCE = -13;
	/**
	 * 阵营战场错误：没有阵营
	 */
	public static final int ERROR_NO_CAMP = -14;
	/**
	 * 阵营战场错误：等级太低
	 */
	public static final int ERROR_LEVEL_TOO_LOW = -15;
	/**
	 * 阵营战场错误：世界地图无法加入战场
	 */
	public static final int ERROR_ON_WORLD_MAP = -17;
	/**
	 * 阵营战场错误:在其它战场列队中
	 */
	public static final int ERROR_INOTHER_BATTLEFIELD_QUEUE = -18;
	
	/**
	 * 成功加入阵营战场队列中
	 */
	public static final int SUCCESS_QUEUED = -1;
	/**
	 * 可以加入阵营战场队列
	 */
	public static final long CAN_JOIN_QUEUE = -2;
	/**
	 * 0以上为战场类型，以下为战场阵营错误
	 */
	public static final int BATTLEFIELD_TYPE = 0;	
	
	/**
	 * 阵营战场操作：请求战场名字
	 */
	public static final byte ACTION_REQUEST_NAME = 1;
	/**
	 * 阵营战场操作：请求战场详情
	 */
	public static final byte ACTION_REQUEST_DESC = 2;
	/**
	 * 阵营战场操作：请求加入战场
	 */
	public static final byte ACTION_REQUEST_JOIN = 3;
	/**
	 * 阵营战场操作：查看商店
	 */
	public static final byte ACTION_VIEW_SHOP = 4;
	/**
	 * 阵营战场操作：缴纳全部资源
	 */
	public static final byte ACTION_CONTRIBUTE_RESOURCES = 5;
	/**
	 * 阵营战场操作：查看战果
	 */
	public static final byte ACTION_VIEW_VICTORIES = 6;
	/**
	 * 阵营战场操作：退出战场
	 */
	public static final byte ACTION_EXIT_BATTLEFIELD = 7;
	/**
	 * 阵营战场操作：查看规则
	 */
	public static final byte ACTION_VIEW_RULES = 8;
	/**
	 * 阵营战场操作：商店物品
	 */
	public static final byte ACTION_SHOP_ITEM = 9;
	/**
	 * 阵营战场操作：购买物品
	 */
	public static final byte ACTION_BUY_ITEM = 10;
	/**
	 * 阵营战场操作：放弃战场
	 */
	public static final byte ACTION_GIVEUP_BATTLEFIELD = 124;
	/**
	 * 阵营战场操作：战场中的宣战
	 */
	public static final byte ACTION_BATTLEFIELD_WAR = 125;
	/**
	 * 阵营战场操作：奔赴战场
	 */
	public static final byte ACTION_GOTO_BATTLEFIELD = 126;
	/**
	 * 阵营战场操作：离开队列
	 */
	public static final byte ACTION_LEAVE_QUEUE = 127;
	/**
	 * 阵营战场：发送广播时间点，每5分钟发一次
	 */
	public static final long SEND_BROADCAST_TIME = 1000L * 60 * 5;
	/**
	 * 所有阵营战场的名字：必须是唯一的
	 */
	public static ArrayList<String> battlefieldName = new ArrayList<String>();
	/**
	 * 阵营战场
	 * KEY：战场名字
	 * VALUES：战场对象
	 */
	public static ConcurrentHashMap<String, CampBattlefield> battlefields = new ConcurrentHashMap<String, CampBattlefield>();
	/**
	 * 格式化时间
	 */
	protected static SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
	
	/**
	 * 创建战场详情
	 * @param type
	 * @param player
	 * @param campBattlefield
	 * @param campBattlefieldService
	 * @return
	 */
	public static String getSendDetails (String type, WorldPlayer player, CampBattlefield campBattlefield, CampBattlefieldService campBattlefieldService) {
		String sendDetails = "";
		int playerLevel = player.getLevel();
		String name = campBattlefield.getName();
		String desc = campBattlefield.getDesc();
		String targer = campBattlefield.getTarget();
		int levelType = CampBattlefieldService.setLevelTypeByBattleName(type, playerLevel);
		CampBattlefieldAward award = campBattlefield.getCampBattlefieldTypeAward(levelType);
		if (award != null) {
//		    int summonRate = award.getSummonRate();
//		    int rate = award.getRate();
			int winnerExpRate = award.getWinnerExpRate();
		    int loserExpRate = award.getLoserExpRate();
		    int winnerPoint = award.getWinnerPoint();
		    int loserPoint = award.getLoserPoint();
//		    int summonWinnerExpRate = award.getSummonWinnerExpRate();
//		    int summonLoserExpRate = award.getSummonLoserExpRate();
//		    int summonWinnerPoint = award.getWinnerPoint();
//		    int summonLoserPoint = award.getLoserPoint();
		    int loseExp = 0;
		    int winnerExp = 0;
		    if (playerLevel != 100) {
		    	winnerExp = Utils.getUpLevelExp(playerLevel) * winnerExpRate / 100;
			    loseExp = Utils.getUpLevelExp(playerLevel) * loserExpRate / 100;
		    }
		    sendDetails += name + "\n\n    " + desc + "\n" + targer + "\n"
		    			+ "获胜奖励：" + "\n" + (player.getLevel() >= 100 ? "" :"  经验：" +  winnerExp) + "  获得杀戮点数：" + winnerPoint 
		    			 + (player.getLevel() >= 100 ? "" : "\n" + "失败奖励：" + "\n" + "  经验：" +  loseExp) ;//+ "  扣除杀戮点数：" + loserPoint;
		} else {
			sendDetails += name + "\n\n    " + desc + "\n" + targer + "\n"
						+ "奖励：" + "\n" + "  您的等级太低，无法加入战场并获得奖励。";
		}
		return sendDetails;
	}
}
