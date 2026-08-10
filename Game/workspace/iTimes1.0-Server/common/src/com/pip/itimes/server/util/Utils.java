package com.pip.itimes.server.util;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import com.pip.itimes.server.stage.*;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.log4j.Logger;
import java.io.*;


public class Utils{

    public static final int CLR_WHITE = 0xffffff;
    public static final int CLR_GREEN = 0x70e970;
    public static final int CLR_BLUE = 0x6fBBF9;
    public static final int CLR_PURPLE = 0xC73FFF;
    public static final int CLR_RED = 0xFF7777;
    public static final int CLR_ORANGE = 0xFFA800;
    public static final int CLR_YELLOW = 0xFFFF00;
    public static final int CLR_GRAY = 0x808080;
    
    /**
	 *狮子吼 红色
	 */
    public static final int CLR_RED_ROAR = 0xFF0000;
    /**
     * 狮子吼发送的频道
     */
    public static final String WORLD = "WORLD";
    public static final String MAP = "MAP";
    
    public static final int MALE_DARK_CAMP_FACE_ID = 30;
    public static final int MALE_BRIGHT_CAMP_FACE_ID = 28;
    public static final int MALE_WARRIORS_DEFAULT_FACE_ID = 2;
    
    public static final int FEMALE_DARK_CAMP_FACE_ID = 31;
    public static final int FEMALE_BRIGHT_CAMP_FACE_ID = 29;
    public static final int FEMALE_WARRIORS_DEFAULT_FACE_ID = 3;

    /**
     * 无阵营颜色 黄色
     */
    public static final int CAMPNO_CLR = 0xFFFF00;
    
    /**
     * 同阵营颜色 绿色
     */
    public static final int CAMPSIDE_CLR = 0x70e970;
    
    /**
     * 敌阵营颜色红色
     */
    public static final int CAMP_CLR = 0xFF7777;
    
    
    /**
     * 阵营类型  无
     */
    public static final int NO_CAMP = 0;
    /**
     * 阵营类型  光明
     */
    public static final int CAMP_BRIGHT = 2;
    
    /**
     * 阵营类型  黑暗
     */
    public static final int CAMP_DARK = 1;
    /**
     * 随机阵营
     */
    public static final int CAMP_RANDOM = 3;
    
    /**
     * 男性
     */
    public static final int SEX_BOY = 0;
    
    /**
     * 女性
     */
    public static final int SEX_GIRL = 1;
    
    /**
     * 阵营分队名称
     */
    public static final String CAMP_TEAM_BRIGHT = "元素军团";
    public static final String CAMP_TEAM_DARK = "黑龙军团";
    
    /**
     * 宝石发光需要的个数:30
     */
    public static final int gemEffectCount = 30;
    
    /**
     * 神圣宝辉需要的个数：45
     */
    public static final int gemEffectCount_Holy = 45;
    
    /**
     * 梦幻宝辉需要的个数：54
     */
    public static final int gemEffectCount_Fantasy = 54;
  
    /**
     * 宝石发光最小等级:3
     */
    public static final int gemEffectLevel = 3;
    
    /**
     * 装备可以镶嵌宝石个数:5
     */
    public static final int maxHolesEqu = 6;
    /**
     * 宠物悟性最大等级
     */
    public static final int PET_MAX_PERCEPTION_LEVEL = 8;
    /**
     * 宠物灵性最大等级
     */
    public static final int PET_MAX_SPIRITUALITY_LEVEL = 7;
    /**
     * 服务器公告限制：宠物灵性
     */
    public static final int[] PET_SPIRITUALITY_NOTICE_REQUIREMENTS = new int[] {
    								7,
    								8,
    						};
    /**
     * 服务器公告限制：宠物悟性
     */
    public static final int[] PET_PERCEPTION_NOTICE_REQUIREMENTS = new int[] {
    								6,
    								7,
    								8
    						};
    /**
     * 可以增加技能的悟性限制
     */
    public static final int[] PET_PERCEPTION_LIMITS_OF_ADD_SKILL = new int[] {
    								3,
    								6,
    								8,
    						};
    /**
     * 灵性下限
     */
    public static final int[] PET_SPIRITUALITY_LOWER_LIMITS = new int[] {
    								0,
    								1,
    								4,
    						};
    /**
     * 宠物修炼的时间单位小时
     */
    public static final long PET_PRACTICE_TIME_REWARDED = 60L * 60L * 1000L;
    
    /**
     * 宠物修炼的时间上限
     */
    public static final long PET_PRACTICE_EXHAUSTED_TIME = 5L * 60L * 60L * 1000L;
    
    /**
     * 一分钟的毫秒数
     */
    public static final long UNIT_OF_MINUTE = 60L * 1000L;
    /**
     * 一秒的毫秒数
     */
    public static final long UNIT_OF_SECOND = 1000L;
    
    /**
     * 打造熟练度上限
     */
    public static final int MAX_BUILD_PROFICIENCY = 475;
    
    /**
     * 服务器战斗回合时间限制
     */
    public static final long ROUND_TIME_LIMIT = 60L * 1000L;
    
    /**
     * 一天的毫秒数
     */
    public static final long MILLS_OF_DAY = 3600 * 24 * 1000;
    
    /**
     * VIP工资卡领取限制
     */
    public static final int VIP_GIFT_LEVEL_RESTRICTIONS = 0;
    
    /**
     * 根据VIP等级获取工资卡等级
     */
    public static final int[] GET_VIP_GIFT_COUNT = new int[] {
    	0,
    	1,
    	2,
    	4,
    	6,
    	8,
    	10
    };
    
    /**
     * 工资卡物品ID
     */
    public static final int[] VIP_WAGE_CARD_ITEMID = new int[] {
    	0,
    	201361,
    	201361,
    	201362,
    	201362,
    	201363,
    	201363
    };
    
    /**
     * 传送门级别限制
     */
    public static final int DOOR_LEVEL_RESTRICTIONS = 9;
    
    /**
     * @param playerCamp
     * @param destPlayerCamp
     * @return根据玩家的阵营和目标人物的阵营来获得该玩家的名称
     */
    public static int getCampColor(int playerCamp, int destPlayerCamp){
    	int campColor = CAMPNO_CLR;
    	if(destPlayerCamp == NO_CAMP){//玩家无阵营
    		if(playerCamp == CAMP_DARK || playerCamp == CAMP_BRIGHT){
    			campColor = CAMP_CLR;
    		}
    	}else{
    		if(playerCamp == destPlayerCamp){
    			campColor = CAMPSIDE_CLR;
    		}else{
    			if(playerCamp == NO_CAMP){
    				campColor = CAMPNO_CLR;
    			}else{
    				campColor = CAMP_CLR;
    			}
    		}
    	}
    	return campColor;
    }
    
    public static String getCampName(int playerCamp, String playerName){
    	String campName = null;
    	 if(playerCamp == NO_CAMP){
    		 campName = playerName + "自己";
         }else if(playerCamp == CAMP_BRIGHT){
        	 campName = "光明";
         }else if(playerCamp== CAMP_DARK){
        	 campName = "黑暗";
         }
    	return campName;
    }
    
  //n从零开始检测第n位为1
	public static boolean CheckIntN(int b, int n) throws Exception{   
      if(n > 31 || n < 0 )
    	  throw new Exception();   
      return ((b   &   (1   <<   n))==(1<<n));   
	}   
	
	//n从零开始设置第n位为1
	public static int  SetIntN(int  b, int n)  throws Exception {
      if(n > 31 || n < 0 )
    	  throw new Exception(); 
      int t  = b | ( 1 << n);
      return t;
	      
	}   
  
  
	
	public static String getClientItemColor(int quality){
		switch (quality) {
		case 0:
			return "<c6A5ACD>";
		case 1:
			return "<c6A5ACE>";
		case 2:
			return "<c6A5ACF>";
		case 3:
			return "<c6A5AD1>";
		case 4:
			return "<c6A5AD2>";
		default:
			return "<c6A5AD3>";
	}
		
	}
    public static final int[] CLR_EQUIP = new int[]{
                        CLR_WHITE, CLR_GREEN, CLR_BLUE, CLR_PURPLE, CLR_ORANGE, CLR_YELLOW
    };
    public static final String[] CLR_PRESCRIPTION = new String[]{
    	"ffffff", "70e970", "6fBBF9", "C73FFF", "FFA800", "FFFF00"
};
    //技能成长对应表 横为物品等级 纵为技能等级
    private static final int[][] SKILL_GROWING = new int[][]{
                    {
                                    10000, 8000, 2000, 0, 0, 0, 0, 0, 0, 0, 0
                    }, {
                                    0, 10000, 8000, 2000, 0, 0, 0, 0, 0, 0, 0
                    }, {
                                    0, 0, 10000, 8000, 2000, 0, 0, 0, 0, 0, 0
                    }, {
                                    0, 0, 0, 10000, 8000, 2000, 0, 0, 0, 0, 0
                    }, {
                                    0, 0, 0, 0, 10000, 8000, 2000, 0, 0, 0, 0
                    }, {
                                    0, 0, 0, 0, 0, 10000, 8000, 2000, 0, 0, 0
                    }, {
                                    0, 0, 0, 0, 0, 0, 10000, 8000, 2000, 0, 0
                    }, {
                                    0, 0, 0, 0, 0, 0, 0, 10000, 8000, 2000, 0
                    }, {
                                    0, 0, 0, 0, 0, 0, 0, 0, 10000, 8000, 0
                    }, {
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 10000, 0
                    }, {
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10000
                    }
    };
    private static int[] enhanceRation = new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
    	3, 3, 3, 3, 3, 3, 
    	4, 4, 4, 4, 4, 4,
    	5, 5, 5, 5, 5, 5,
    	6, 6};
    
    /**
     * @param property 1为 enhancestrength 2为enhanceintelligence 3为  enhancevitality ，4为    	 enhanceagility      
     * @return
     */
    public static int getEnhanceRation(int property,int points){
    	return enhanceRation[points];
    }
    public static String[] PET_NAME = {
                    "水晶之星", "炎笛",

                    "尼古拉",

                    "尤迪思",

                    "妙娃",

                    "卡里奥",

                    "哥拉斯",

                    "伊布",

                    "卡丘",

                    "可拉奥",

                    "烈炎",

                    "华登",

                    "丘比特",

                    "傲骨",

                    "小皮",

                    "米里奥",

                    "古特",

                    "风多莎",

                    "爱坦",

                    "糖果",

                    "神气宝贝",

                    "西斯",

                    "戈斯亚",
    };

    private static final int[] BABYPET_POINT = new int[]{
                    50, 45, 40, 35, 30, 25, 20, 15, 10
    };
    private static final int[] BABYPET_POINT_ADDED = new int[]{
                    0, 5, 10, 15, 20, 25, 30, 35, 40
    };

    private static final SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
    private static final SimpleDateFormat formatdate = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

    /*private static final int[] EXP = {10,
                                     24,
                                     62,
                                     136,
                                     258,
                                     440,
                                     694,
                                     1032,
                                     1466,
                                     2008,
                                     2136,
                                     2598,
                                     3081,
                                     3572,
                                     4055,
                                     5330,
                                     6884,
                                     8754,
                                     10981,
                                     12246,
                                     14877,
                                     18274,
                                     21908,
                                     24890,
                                     28132,
                                     31644,
                                     35437,
                                     39521,
                                     43907,
                                     48607,
                                     53631,
                                     58990,
                                     64694,
                                     70754,
                                     77182,
                                     83988,
                                     91183,
                                     98777,
                                     106781,
                                     115207,
                                     124065,
                                     133366,
                                     143120,
                                     153338,
                                     164032,
                                     175212,
                                     186889,
                                     199073,
                                     211775,
                                     225007,
                                     238779,
                                     253102,
                                     267986,
                                     283442,
                                     299482,
                                     316116,
                                     333355,
                                     351209,
                                     369689,
                                     432008,
                                     453970,
                                     517771,
                                     616745,
                                     724288,
                                     840792,
                                     966656,
                                     1102289,
                                     1248107,
                                     1404533,
                                     1572000,
                                     1750948,
                                     1941824,
                                     2145085,
                                     2361195,
                                     2590625,
                                     2833856,
                                     3091376,
                                     3363680,
                                     3651273,
                                     3954667,
                                     4274381,
                                     4610944,
                                     4964892,
                                     5336768,
                                     6299838,
                                     7363827,
                                     8535188,
                                     9820608,
                                     11227020,
                                     12761600,
                                     14431778,
                                     16245235,
                                     18209914,
                                     20334016,
                                     22626013,
                                     25094643,
                                     27748923,
                                     30598144,
                                     99999999,
                                     100000000,
    };*/
    //mengjie modify 20100827 1-3升级均为10
    private static final int[] EXP = {10,
									    10,
									    10,
									    136,
									    258,
									    440,
									    694,
									    1032,
									    1466,
									    2008,
									    2136,
									    2598,
									    3081,
									    3572,
									    4055,
									    5330,
									    6884,
									    8754,
									    10981,
									    12246,
									    13432,
									    14619,
									    17526,
									    19912,
									    22505,
									    25315,
									    28349,
									    31616,
									    35125,
									    38885,
									    42904,
									    47192,
									    51755,
									    56603,
									    61745,
									    67190,
									    72946,
									    79021,
									    85424,
									    92165,
									    99252,
									    106692,
									    114496,
									    122670,
									    131225,
									    140169,
									    149511,
									    159258,
									    169420,
									    180005,
									    191023,
									    202481,
									    214388,
									    226753,
									    239585,
									    252892,
									    266684,
									    280967,
									    295751,
									    345606,
									    363176,
									    414216,
									    493396,
									    579430,
									    672633,
									    773324,
									    881831,
									    998485,
									    1123626,
									    1257600,
									    1400758,
									    1553459,
									    1716068,
									    1888956,
									    2072500,
									    2267084,
									    2473100,
									    2690944,
									    2921018,
									    3163733,
									    3419504,
									    3688755,
									    3971913,
									    4269414,
									    5039870,
									    5891061,
									    6828150,
									    7856486,
									    8981616,
									    10209280,
									    11545422,
									    12996188,
									    14567931,
									    16267212,
									    18100810,
									    20075714,
									    22199138,
									    24478515,
									    79999999,
									    80000000
    };

    public static final int[] ENHANCE_PROBABILITY = {
        156, 140, 124, 109, 93, 62, 46, 31, 16
    };

    public static final int[] ENHANCE_PROBABILITY_ADJUST = {
            5,
            5,
            5,
            5,
            5,
            5,
            5,
            5,
            5,
            5,
            4,
            4,
            4,
            4,
            4,
            4,
            4,
            4,
            4,
            4,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            2,
            2,
            2,
            2,
            2,
            2,
            2,
            2,
            2,
            2,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -1,
            -2,
            -2,
            -2,
            -2,
            -2,
            -2,
            -2,
            -2,
            -2,
            -2,
            -3,
            -3,
            -3,
            -3,
            -3,
            -3,
            -3,
            -3,
            -3,
            -3,
            -4,
            -4,
            -4,
            -4,
            -4,
            -4,
            -4,
            -4,
            -4,
            -4,
            -5,

    };

    public static int[] Money_package = {
    	0,
    	1001,
    	1004,
    	1009,
    	1016,
    	1025,
    	1036,
    	1049,
    	1064,
    	1081,
    	1100,
    	1121,
    	1144,
    	1169,
    	1196,
    	1225,
    	1256,
    	1289,
    	1324,
    	1361,
    	1400,
    	1441,
    	1484,
    	1529,
    	1576,
    	1625,
    	1676,
    	1729,
    	1784,
    	1841,
    	1900,
    	1961,
    	2024,
    	2089,
    	2156,
    	2225,
    	2296,
    	2369,
    	2444,
    	2521,
    	2600,
    	2681,
    	2764,
    	2849,
    	2936,
    	3025,
    	3116,
    	3209,
    	3304,
    	3401,
    	3500,
    	3601,
    	3704,
    	3809,
    	3916,
    	4025,
    	4136,
    	4249,
    	4364,
    	4481,
    	4600,
    	4721,
    	4844,
    	4969,
    	5096,
    	5225,
    	5356,
    	5489,
    	5624,
    	5761,
    	5900,
    	6041,
    	6184,
    	6329,
    	6476,
    	6625,
    	6776,
    	6929,
    	7084,
    	7241,
    	7400,
    	7561,
    	7724,
    	7889,
    	8056,
    	8225,
    	8396,
    	8569,
    	8744,
    	8921,
    	9100,
    	9281,
    	9464,
    	9649,
    	9836,
    	10025,
    	10216,
    	10409,
    	10604,
    	10801,
    	11000
    };
    
    public static int[] Pet_Exp = {
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	5,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	15,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	25,
    	35,
    	35,
    	35,
    	35,
    	35,
    	35,
    	35,
    	35,
    	35,
    	35,
    	55,
    	55,
    	55,
    	55,
    	55,
    	55,
    	55,
    	55,
    	55,
    	55,
    	75,
    	75,
    	75,
    	75,
    	75,
    	75,
    	75,
    	75,
    	75,
    	75,
    	95,
    	95,
    	95,
    	95,
    	95,
    	95,
    	95,
    	95,
    	95,
    	95,
    	120,
    	120,
    	120,
    	120,
    	120,
    	120,
    	120,
    	120,
    	240,
    	500,
    	3500,
    	3500,
	};
    
    public static short getMapIdById(int id){
        return (short)((id >> 16) & 0xFFFF);
    }

    public static String getDateString(Date date){
        return format.format(date);
    }

    public static String getRemainTimeString(Date date, Date now){
        long t = date.getTime() - now.getTime();
        int hour = (int)(t / (3600 * 1000));
        if(hour == 0)
            return "一小时内";
        else
            return hour + "小时";
    }

    public static byte getType(int id){
        byte type = (byte)(((short)(id & 0xFFFF)) >> 13);
        return type;
    }

    public static short getStageId(short mapId){
        return (short)((mapId >> 4) & 0xFFFF);
    }

    public static byte getMapIndex(short mapId){
        return (byte)(mapId & 0xF);
    }

    public static int getUpLevelExp(int level){
        if(level >= EXP.length){
            return EXP[EXP.length - 1];
        }else{
            return EXP[level-1];
        }
        //return 2 * level * level * level + 8;
    }

    public static int getPetUpLevelExp(int level){
    	//mengjie add
//    	int newexp;
//    	if (level>30){
//    		newexp = 6 * level + 3;
//    	}else{
//    		newexp = (9*level+3)*level/30+1;
//    	}
        return Pet_Exp[level - 1];
//    	return newexp;
    }

    public static int getUpLevel(int oldLevel, int exp){
        int level = 0;
        int upLevelExp = getUpLevelExp(oldLevel);
        while(true){
            if(upLevelExp > exp)
                return level;
            exp -= upLevelExp;
            level++;
            upLevelExp = getUpLevelExp(oldLevel + level);
        }
    }

    public static int getUpLevelExp(int oldLevel, int newLevel){
        int ret = 0;
        for(int i = oldLevel; i < newLevel; i++){
            ret += getUpLevelExp(i);
        }
        return ret;
    }

    public static int getPetExp(Monster[] ms, Pet p,PlayerData player){
        int avgLevel = 0;
        for(int i = 0; i < ms.length; i++){
            avgLevel += ms[i].getLevel();
        }
        avgLevel /= ms.length;
        //int r = Math.abs(avgLevel - p.getLevel());
        int r = avgLevel - p.getLevel();
        //mengjie add
        int ret;
        if (p.getLevel()<30){
        	ret =  (r >= -10? 2: 0);
        }else{
        	ret =  (r >= -10? 1: 0);
        }
        Buf buf = null;
        if((buf=player.getBuf(Buf.DOUBLE_PET_EXP))!=null)
            return ret*(buf.getValue()+100)/100;
        return ret;
    }

    public static int getMaxSkillPoint(int level){
        if(level <= 10){
            return 75;
        }
        if(level <= 20){
            return 140;
        }
        if(level <= 30){
            return 195;
        }
        if(level <= 40){
            return 220;
        }
        if(level <= 50){
            return 240;
        }
        if(level <= 60){
            return 260;
        }
        if(level <= 100){
            return 275;
        }else{
            return 285;
        }
    }

    public static int getSkillLevel(int skillPoint){
        if(skillPoint >= 1 && skillPoint <= 40){
            return 1;
        }
        if(skillPoint >= 41 && skillPoint <= 75){
            return 2;
        }
        if(skillPoint >= 76 && skillPoint <= 110){
            return 3;
        }
        if(skillPoint >= 111 && skillPoint <= 140){
            return 4;
        }
        if(skillPoint >= 141 && skillPoint <= 170){
            return 5;
        }
        if(skillPoint >= 171 && skillPoint <= 195){
            return 6;
        }
        if(skillPoint >= 196 && skillPoint <= 220){
            return 7;
        }
        if(skillPoint >= 221 && skillPoint <= 240){
            return 8;
        }
        if(skillPoint >= 241 && skillPoint <= 260){
            return 9;
        }
        if(skillPoint >= 261 && skillPoint <= 275){
            return 10;
        }
        if(skillPoint >= 276 && skillPoint <= 285){
            return 11;
        }
        return 0;
    }

    public static final int getSkillGrowingChance(int itemLevel, int skillPoint){
        int level = getSkillLevel(skillPoint);
        return SKILL_GROWING[itemLevel - 1][level - 1];
    }

    public static boolean hit(Random rnd, int chance, int base){
        int r = rnd.nextInt(base);
        if(r <= chance)
            return true;
        return false;
    }

    public static Random random = new Random();
    public static boolean hit(int chance, int base){
//        Random rnd = new Random();
        return hit(random, chance, base);
    }

    public static boolean checkString(String s, boolean allowColon){
        if(s == null){
            return false;
        }
        for(int i = 0; i < s.length(); i++){
            char ch = s.charAt(i);
            boolean isValid = false;
            if(ch >= 'a' && ch <= 'z'){
                isValid = true;
            }else if(ch >= 'A' && ch <= 'Z'){
                isValid = true;
            }else if(ch >= '0' && ch <= '9'){
                isValid = true;
            }else if(ch == '_'){
                isValid = true;
            }else if(ch >= 0x4E00 && ch <= 0x9FA5){
                isValid = true;
            }else if(allowColon && ch == ':'){
                isValid = true;
            }
            if(!isValid){
                return false;
            }
        }
        return true;
    }

    public static boolean checkString(String s){
        if(s==null)
            return false;
        for(int i=0;i<s.length();i++){
            char ch = s.charAt(i);
            if (ch == 0x0D || ch == 0x0A)
                continue;
            if(ch>=0x20&&ch<=0x7e)
                continue;
            if(ch>=0x2018&&ch<=0x203B)
                continue;
            if(ch>=0x3001&&ch<=0x3002)
                continue;
            if(ch>=0x3008&&ch<=0x3011)
                continue;
            if(ch>=0x4e00&&ch<=0x9fa5)
                continue;
            if(ch>=0xf92c&&ch<=0xfa29)
                continue;
            if(ch>=0xff01&&ch<=0xffe5)
                continue;
            return false;
        }
        return true;
    }


    public static int getPetRenameMoney(Pet p){
        return p.getLevel() * p.getLevel() * 10 + 1000;
    }

    public static void initPet(Pet pet, int point, int point2,int petlevel){
        int type = pet.getPetType();
        Random rnd = new Random();
        if(!pet.getBaby()){
        	//mengjie add
            if (petlevel == 1){
            	point = point + 40;
            }else if (petlevel == 2){
            	point = point + 35;
            }else if (petlevel == 3){
            	point = point + 30;
            }else if (petlevel == 4){
            	point = point + 25;
            }else if (petlevel == 5){
            	point = point + 20;
            }else if (petlevel == 6){
            	point = point + 15;
            }else if (petlevel == 7){
            	point = point + 8;
            }else if (petlevel == 8){
            	point = point + 4;
            }
            //mengjie add end
            if(type == Pet.TYPE_0){
                int leave = point;
                int value = getCount(rnd, 1, 10) * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = getCount(rnd, 1, 10) * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                value = getCount(rnd, 10, 20) * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = getCount(rnd, 40, 60) * point / 100;
                pet.setStrength(value+8);
            }else if(type == Pet.TYPE_1){
                int leave = point;
                int value = getCount(rnd, 1, 10) * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                value = getCount(rnd, 1, 10) * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = getCount(rnd, 10, 20) * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                value = getCount(rnd, 40, 60) * point / 100;
                pet.setIntelligence(value+8);
            }else if(type == Pet.TYPE_2){
                int leave = point;
                int value = getCount(rnd, 1, 10) * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = getCount(rnd, 1, 10) * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = getCount(rnd, 10, 20) * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                value = getCount(rnd, 40, 60) * point / 100;
                pet.setVitality(value+8);
            }else if(type == Pet.TYPE_3){
                int leave = point;
                int value = getCount(rnd, 1, 10) * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = getCount(rnd, 1, 10) * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                value = getCount(rnd, 10, 20) * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                value = getCount(rnd, 40, 60) * point / 100;
                pet.setAgility(value+8);
            }else if(type == Pet.TYPE_4){
                int leave = point;
                int value = getCount(rnd, 1, 10) * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                value = getCount(rnd, 1, 10) * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = getCount(rnd, 30, 40) * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = getCount(rnd, 30, 40) * point / 100;
                pet.setVitality(value+8);
            }else if(type == Pet.TYPE_5){
                int leave = point;
                int value = getCount(rnd, 1, 10) * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = getCount(rnd, 1, 10) * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                value = getCount(rnd, 30, 40) * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = getCount(rnd, 30, 40) * point / 100;
                pet.setStrength(value+8);
            }
            pet.setPoint(point2);
            pet.setName(PET_NAME[rnd.nextInt(PET_NAME.length)]);
        }else{
            if(type == Pet.TYPE_0){
                int leave = point;
                int value = 10 * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = 10 * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                value = 20 * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                pet.setStrength(leave+8);
            }else if(type == Pet.TYPE_1){
                int leave = point;
                int value = 10 * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                value = 10 * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = 20 * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                pet.setIntelligence(leave+8);
            }else if(type == Pet.TYPE_2){
                int leave = point;
                int value = 10 * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = 10 * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = 20 * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                pet.setVitality(leave+8);
            }else if(type == Pet.TYPE_3){
                int leave = point;
                int value = 10 * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = 10 * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                value = 20 * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                pet.setAgility(leave+8);
            }else if(type == Pet.TYPE_4){
                int leave = point;
                int value = 10 * point / 100;
                pet.setStrength(value+8);
                leave -= value;
                value = 10 * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                value = 40 * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                pet.setVitality(leave+8);
            }else if(type == Pet.TYPE_5){
                int leave = point;
                int value = 10 * point / 100;
                pet.setIntelligence(value+8);
                leave -= value;
                value = 10 * point / 100;
                pet.setVitality(value+8);
                leave -= value;
                value = 40 * point / 100;
                pet.setAgility(value+8);
                leave -= value;
                pet.setStrength(leave+8);
            }
            pet.setPoint(point2);
            pet.setName(PET_NAME[rnd.nextInt(PET_NAME.length)]);
        }
    }

    public static int getCount(Random rnd, int min, int max){
        return rnd.nextInt(max - min + 1) + min;
    }

    public static int getBabyPetPoint(int level){
        int index = (level - 1) / 10 - 1;
        return BABYPET_POINT[index];
    }

    public static int getBabyPetAddedPoint(int level){
        int index = (level - 1) / 10 - 1;
        return BABYPET_POINT_ADDED[index];
    }

    public static Ability[] getPetAbilities(int type){
        Random rnd = new Random();
        IntList l = new ArrayIntList(5);
        if(type == Pet.TYPE_0){
            int[] ids = getCounts(rnd, 1001, 1005, 3);
            l.add(ids[0]);
            l.add(ids[1]);
            l.add(ids[2]);
            l.add(getCount(rnd, 1011, 1015));
            if(rnd.nextInt(2) == 0){
                l.add(getCount(rnd, 1006, 1010));
            }else{
                l.add(getCount(rnd, 1016, 1020));
            }
        }else if(type == Pet.TYPE_1){
            int[] ids = getCounts(rnd, 1006, 1010, 3);
            l.add(ids[0]);
            l.add(ids[1]);
            l.add(ids[2]);
            l.add(getCount(rnd, 1016, 1020));
            if(rnd.nextInt(2) == 0){
                l.add(getCount(rnd, 1001, 1005));
            }else{
                l.add(getCount(rnd, 1011, 1015));
            }
        }else if(type == Pet.TYPE_2){
            int[] ids = getCounts(rnd, 1016, 1020, 3);
            l.add(ids[0]);
            l.add(ids[1]);
            l.add(ids[2]);
            l.add(getCount(rnd, 1001, 1005));
            if(rnd.nextInt(2) == 0){
                l.add(getCount(rnd, 1006, 1010));
            }else{
                l.add(getCount(rnd, 1011, 1015));
            }
        }else if(type == Pet.TYPE_3){
            int[] ids = getCounts(rnd, 1011, 1015, 3);
            l.add(ids[0]);
            l.add(ids[1]);
            l.add(ids[2]);
            l.add(getCount(rnd, 1001, 1005));
            if(rnd.nextInt(2) == 0){
                l.add(getCount(rnd, 1006, 1010));
            }else{
                l.add(getCount(rnd, 1016, 1020));
            }
        }else if(type == Pet.TYPE_4){
            int[] ids = getCounts(rnd, 1006, 1010, 2);
            l.add(ids[0]);
            l.add(ids[1]);
            ids = getCounts(rnd, 1016, 1020, 2);
            l.add(ids[0]);
            l.add(ids[1]);
            if(rnd.nextInt(2) == 0){
                l.add(getCount(rnd, 1001, 1005));
            }else{
                l.add(getCount(rnd, 1011, 1015));
            }
        }else if(type == Pet.TYPE_5){
            int[] ids = getCounts(rnd, 1001, 1005, 2);
            l.add(ids[0]);
            l.add(ids[1]);
            ids = getCounts(rnd, 1011, 1015, 2);
            l.add(ids[0]);
            l.add(ids[1]);
            if(rnd.nextInt(2) == 0){
                l.add(getCount(rnd, 1006, 1010));
            }else{
                l.add(getCount(rnd, 1016, 1020));
            }
        }
        Ability[] ret = new Ability[l.size()];
        for(int i = 0; i < l.size(); i++){
            ret[i] = Ability.getAbility(l.get(i));
        }
        return ret;
    }
    
    //zxyu add
    /**
     * 随机获得指定个数的技能
     * 宠物技能从1001到1026 排除掉except里面的技能，从剩下的技能中随机获取
     * @param type
     * @param count
     * @param except
     * @return
     */
    public static Ability[] getPetAbilities(int type, int count, int[] except,int totalCount){
        Random rnd = new Random();
        IntList l = new ArrayIntList(count);
//     	int length = Integer.parseInt((totalCount+"").substring(2,4))- except.length;
        IntList abilityList = new ArrayIntList();
        for(int i=1001; i<=totalCount; i++){
        	boolean add = true;
        	for(int j=0; j<except.length; j++){
        		if(i == except[j]){
        			add = false;
        			break;
        		}
        	}
        	if(add){
        		abilityList.add(i);
        	}
        }
       
        int[] ids = getCounts(rnd, 0, abilityList.size() - 1, count);
        Ability[] ret = new Ability[count];
        for(int i = 0; i < count; i++){
            ret[i] = Ability.getAbility(abilityList.get(ids[i]));
        }
        return ret;
    }
  //zxyu add end

    public static String getPetSellDesc(Pet pet,PlayerData player,int money){
        StringBuffer buff = new StringBuffer(500);
        buff.append("宠物名称:"+pet.getName());
        buff.append(" 等级:");
        buff.append(pet.getLevel());
        if (pet.getBaby()) {
            buff.append("(宝宝)");
        }
        buff.append(" ");
        buff.append("灵性等级 ");
        buff.append(pet.getSpiritualityLevel());
        buff.append(" 悟性等级 ");
        buff.append(pet.getPerceptionLevel());
        buff.append(" ");
        buff.append("体力 ");
        buff.append(pet.getVitality());
        buff.append(" 力量 ");
        buff.append(pet.getStrength());
        buff.append(" 敏捷 ");
        buff.append(pet.getAgility());
        buff.append(" 智力 ");
        buff.append(pet.getIntelligence());
        buff.append(" ");
//        buff.append("精炼（体力");
//        buff.append(pet.getEnhancevitality());
//        buff.append("点,力量");
//        buff.append(pet.getEnhancestrength());
//        buff.append("点,敏捷");
//        buff.append(pet.getEnhanceagility());
//        buff.append("点,智力");
//        buff.append(pet.getEnhanceintelligence());
//        buff.append("点)");
        buff.append("技能:");
        Ability[] ability = pet.getAbilities();
        for(int i=0;i<ability.length;i++){
            buff.append(ability[i].getName());
            buff.append(" ");
        }
        buff.append("\n");
        buff.append("出售人:");
        buff.append(player.getPlayerName());
        buff.append(" ");
        buff.append("价格:");
        buff.append(money);
        buff.append("J\n");
        if(pet.getPerceptionLevel() >= 4 || pet.getSpiritualityLevel() >= 7){
        	buff.append("购买该宠物后，宠物与您灵魂绑定，如需再次交易需要圣水进行解锁\n");
        }
        buff.append("是否购买?\n7.是\n9.否");
        return buff.toString();
    }

    public static byte getRandomPetType(){
        Random rnd = new Random();
        return (byte)rnd.nextInt(6);
    }

    public static int[] getCounts(Random rnd, int min, int max, int count){
        IntList l = new ArrayIntList(count);
        while(l.size() < count){
            int v = getCount(rnd, min, max);
            if(!l.contains(v)){
                l.add(v);
            }
        }
        return l.toArray();
    }
    
    public static String getPlayerItemsString(PlayerData player){
        StringBuffer buff = new StringBuffer(500);
        buff.append(player.getPlayerName()+"背包内有:");
        getItemsString0(player.getBasicItems(),buff);
        getItemsString0(player.getExtendedItems(),buff);
        getItemsString0(player.getTaskItems(),buff);
        getItemsString0(player.getEquipments(),buff);
        if(buff.charAt(buff.length()-1)==','){
            buff.deleteCharAt(buff.length()-1);
        }
        return buff.toString();
    }

    public static void getItemsString0(Grid[] items,StringBuffer buff) {
        for (int i = 0; i < items.length; i++) {
            getItemString0(items[i].item, items[i].count,buff);
            buff.append(",");
        }
    }

    private static void getItemString0(IItem item, int count,StringBuffer buff) {
        byte type = item.getType();
        if (type == IItem.TYPE_BASIC) {
            BasicItem it = (BasicItem) item;
            buff.append(it.getName());
            buff.append("*");
            buff.append(count);
        } else if (type == IItem.TYPE_EXTENDED) {
            ExtendedItem it = (ExtendedItem) item;
            buff.append(it.getName());
            buff.append("*");
            buff.append(count);
        } else if (type == IItem.TYPE_TASK) {
            TaskItem it = (TaskItem) item;
            buff.append(it.getName());
            buff.append("*");
            buff.append(count);
        } else if (type == IItem.TYPE_EQU) {
            IEquipment it = (IEquipment) item;
            buff.append(it.getName());
        } else if (type == IItem.TYPE_PET) {
            Pet it = (Pet) item;
            buff.append(it.getName());
        }
    }

    /**
     * @param player
     * @return管理员展示同名角色
     */
    public static String getAdminPlayerString(PlayerData player){
    	 StringBuffer buff = new StringBuffer(2000);
    	 buff.append("Id:");
         buff.append(player.getId());
         buff.append("  名字:");
         buff.append(player.getPlayerName());
         
         buff.append("  性别:");
         buff.append(player.getSex() == 0? "男": "女");
         buff.append(" 形象:");
         buff.append(player.getFace());
         buff.append("  等级:");
         buff.append(player.getLevel());
         
         buff.append(" 阵营[");
         if(player.getCamp() == NO_CAMP){
         	buff.append("无]");
         }else if(player.getCamp() == CAMP_BRIGHT){
         	buff.append("光明]");
         }else if(player.getCamp()== CAMP_DARK){
         	buff.append("黑暗]");
         }
         
         buff.append(" 玩家最后登录的时间");
         buff.append(player.getLastLoginTime());
         buff.append(" 玩家最后下线的时间");
         buff.append(player.getLastlogoutTime());
         
         if(player.getPlayer().getValid()){
        	  buff.append(" 未删");
         }else{
        	  buff.append(" 已删");
         }
    	 return buff.toString();
    }
    public static String getPlayerString(PlayerData player){
        StringBuffer buff = new StringBuffer(2000);
        buff.append("Id:");
        buff.append(player.getId());
        buff.append("  名字:");
        buff.append(player.getPlayerName());
        buff.append("  地图:");
        buff.append(player.getMapId());
        buff.append("  坐标:");
        buff.append(player.getX());
        buff.append(",");
        buff.append(player.getY());
        buff.append("JumpMapId:");
        buff.append(player.getJumpMapId());
        buff.append(",");
        buff.append("  性别:");
        buff.append(player.getSex() == 0? "男": "女");
        buff.append(" 形象:");
        buff.append(player.getFace());
        buff.append("  转生次数:");
        buff.append(player.getReturnTimes());
        buff.append("  等级:");
        buff.append(player.getLevel());
        buff.append(" 经验:");
        buff.append(player.getExp());
        buff.append(" 升级到下一级的经验:");
        buff.append(getUpLevelExp(player.getLevel()));
        buff.append("  公会:");
        buff.append(player.getTongName());
        buff.append("  公会职务:");
        buff.append(player.getTongDuty());
        buff.append(" 金钱:");
        buff.append(player.getMoeny());
        buff.append(" 力量:");
        buff.append(player.getStrength());
        buff.append(" 敏捷:");
        buff.append(player.getAgility());
        buff.append("  体力:");
        buff.append(player.getVitality());
        buff.append("  智力:");
        buff.append(player.getIntelligence());
        buff.append("  HP:");
        buff.append(player.getHp());
        buff.append("  MP:");
        buff.append(player.getMp());
        buff.append("  荣誉:");
        buff.append(player.getCredit());
        buff.append("  剩余属性点:");
        buff.append(player.getLeavePoints());
        buff.append("  剩余技能点:");
        buff.append(player.getPoint());
        buff.append("  技能点分配:");
        buff.append(player.getAbilityPoint(0));
        buff.append(",");
        buff.append(player.getAbilityPoint(1));
        buff.append(",");
        buff.append(player.getAbilityPoint(2));
        buff.append(",");
        buff.append(player.getAbilityPoint(3));
        buff.append("  已经学会技能:");
        int[] abilities = player.getAbilitiesId();
        if(abilities.length == 0)
            buff.append("无");
        else{
            for(int i = 0; i < abilities.length; i++){
                buff.append(abilities[i]);
                buff.append(",");
            }
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("  生活技能点数:");
        for(int i = 0; i < 8; i++){
            buff.append(player.getSkillPoint(i));
            buff.append(",");
        }
        buff.deleteCharAt(buff.length() - 1);
        buff.append("  总包位:");
        buff.append(player.getAllGridSize());
        buff.append("  附加包位:");
        buff.append(player.getAddedGridSize());
        buff.append("  当前使用包位:");
        buff.append(player.getCurrentGridSize());
        buff.append("  基本物品:");
        buff.append(getItemsString(player.getBasicItems()));
        buff.append("  扩展物品:");
        buff.append(getItemsString(player.getExtendedItems()));
        buff.append("  任务物品:");
        buff.append(getItemsString(player.getTaskItems()));
        buff.append("  装备:");
        buff.append(getItemsString(player.getEquipments()));
        buff.append("  使用的装备:");
        IEquipment[] usedEquipments = player.getUsedEquipments();
        if(usedEquipments.length == 0)
            buff.append("无");
        else{
            for(int i = 0; i < usedEquipments.length; i++){
                if(usedEquipments[i] != null){
                    buff.append(getItemString(usedEquipments[i], 1));
                    buff.append(",");
                }
            }
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("  宠物栏:");
        buff.append(player.getPetSize());
        buff.append("  宠物数量:");
        buff.append(player.getPetCount());
        buff.append("   宠物:");
        Pet[] pets = player.getPets();
        if(pets.length == 0)
            buff.append("无");
        else{
            for(int i = 0; i < pets.length; i++){
                buff.append(getItemString(pets[i], 1));
                buff.append(",");
            }
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append("  跟随的宠物:");
        Pet pet = player.getPet();
        if(pet == null)
            buff.append("无");
        else{
            buff.append(getItemString(pet, 1));
        }
        buff.append(" 好友信息:");
        Friend[] friends = player.getFriends();
        if(friends.length==0){
            buff.append("无");
        }else{
            for (int i = 0; i < friends.length; i++) {
                buff.append(getFriendString(friends[i]));
                buff.append(",");
            }
            buff.deleteCharAt(buff.length() - 1);
        }
        buff.append(" 阵营[");
        if(player.getCamp() == NO_CAMP){
        	buff.append("无]");
        }else if(player.getCamp() == CAMP_BRIGHT){
        	buff.append("光明]");
        }else if(player.getCamp()== CAMP_DARK){
        	buff.append("黑暗]");
        }
        return buff.toString();
    }

    public static String getFriendString(Friend friend){
        StringBuilder sb = new StringBuilder(1000);
        sb.append("{");
        sb.append(friend.getId());
        sb.append(",");
        sb.append(friend.getName());
        sb.append(",");
        sb.append(friend.getFavorite());
        sb.append("}");
        return sb.toString();
    }

    public static String getShopString(ShopData shop){
        StringBuffer buff = new StringBuffer(1000);
        buff.append("ID:");
        buff.append(shop.getId());
        buff.append(" Name:");
        buff.append(shop.getName());
        buff.append(" Level:");
        buff.append(shop.getLevel());
        buff.append(" Size:");
        buff.append(shop.getGridSize());
        buff.append(" CurrentSize:");
        buff.append(shop.getCurrentGridSize());
        buff.append(" Money:");
        buff.append(shop.getMoney());
        Grid[] grid = shop.getItems();
        buff.append(" Items:");
        buff.append(getItemsString(grid));
        return buff.toString();
    }

    public static String getItemsString(Grid[] items){
        StringBuffer buff = new StringBuffer();
        if(items.length == 0)
            buff.append("无");
        else{
            for(int i = 0; i < items.length; i++){
                buff.append(getItemString(items[i].item, items[i].count));
            }
            buff.deleteCharAt(buff.length() - 1);
        }
        return buff.toString();
    }

    public static String getItemString(IItem item, int count){
        byte type = item.getType();
        if(type == IItem.TYPE_BASIC){
            StringBuffer buff = new StringBuffer();
            BasicItem it = (BasicItem)item;
            buff.append("{");
            buff.append(it.getItemId());
            buff.append(",");
            buff.append(it.getName());
            buff.append(",");
            buff.append(count);
            buff.append("}");
            return buff.toString();
        }else if(type == IItem.TYPE_EXTENDED){
            StringBuffer buff = new StringBuffer();
            ExtendedItem it = (ExtendedItem)item;
            buff.append("{");
            buff.append(it.getItemId());
            buff.append(",");
            buff.append(it.getName());
            buff.append(",");
            buff.append(count);
            buff.append("}");
            return buff.toString();
        }else if(type == IItem.TYPE_TASK){
            StringBuffer buff = new StringBuffer();
            TaskItem it = (TaskItem)item;
            buff.append("{");
            buff.append(it.getItemId());
            buff.append(",");
            buff.append(it.getName());
            buff.append(",");
            buff.append(count);
            buff.append("}");
            return buff.toString();
        }else if(type == IItem.TYPE_EQU){
            StringBuffer buff = new StringBuffer();
            IEquipment it = (IEquipment)item;
            buff.append("{");
            buff.append(it.getItemId());
            buff.append(",");
            buff.append(it.getName());
            buff.append(",");
            buff.append(it.getId());
            buff.append(",");
            buff.append(it.isBinded());
            buff.append("}");
            return buff.toString();
        }else if(type == IItem.TYPE_PET){
            StringBuffer buff = new StringBuffer();
            Pet it = (Pet)item;
            buff.append("{");
            buff.append(it.getItemId());
            buff.append(",");
            buff.append(it.getName());
            buff.append(",");
            buff.append(it.getId());
            buff.append("}");
            return buff.toString();
        }
        return "";
    }

    public static boolean isServerMonsterGroup(MonsterGroup mg){
        return !((mg.getType() & 0x01) == 0);
    }

   /* public static int calculateMaxHp(int vitality, int agility, int strength, int intelligence,int level){
        return vitality * 6 * ((int)sqrt(level * 100) + 30) / 40 + 50;
    }*/
    /**
     * @param vitality  体力
     * @param agility 敏捷
     * @param strength 力量
     * @param intelligence 智力
     * @param level 等级
     * @param diamondHp 宝石所加属性血值
     * 转换2后公式为 [7 * 体力 * (sqrt(等级 * 100) + 30) + 50 * 40] / 40 + 血宝石
     */
    public static int calculateMaxHp(int vitality, int agility, int strength, int intelligence,int level, int diamondHp){
        //return  8 * (vitality + strength / 6) * ((int)sqrt(level * 100) + 30) / 40 + 50  + diamondHp;
    	//return (8 * ( 6 * vitality + strength) * ((int)sqrt(level * 100) + 30) + 50 * 6 * 40) / (6 * 40) + diamondHp;
    	return (7 * vitality * ((int)sqrt(level * 100) + 30) + 50 * 40) / 40 + diamondHp;
    }
    
    /**
     * @param vitality 体力
     * @param agility 敏捷
     * @param strength 力量
     * @param intelligence 
     * @param level 等级
     * @param diamondMp 宝石所加属性蓝值
     * @return  转化后公示为[3 * (8 * 智力  + 力量) * (sqrt(等级 * 100) + 30) + 50 * 8 * 40] / (8 * 40)  +　蓝宝石
     * @return 现在公式 3 * (智力 * 8 + 力量) * (sqrt(等级 * 100)) / 320 + 50 + 蓝宝石
     */
    public static int calculateMaxMp(int vitality, int agility, int strength, int intelligence,int level, int diamondMp){
//        return 3 *(intelligence + strength /4) * ((int)sqrt(level * 100) + 30) / 40 + 50 + diamondMp;
    	return (3*(8*intelligence +strength)* ((int)sqrt(level * 100) + 30) + 50 * 8 * 40) / (8 * 40) + diamondMp;
//    	return 3 * (intelligence * 8 + strength) * ((int)sqrt(level * 100)) / 320 + 50 + diamondMp;
    }
    
    public static final long START_BIT = (~Long.MAX_VALUE) >>> 1;

    public static final long sqrt(long x) {
        if (x < 0) {
            return 0;
        }

        long y = 0;
        long b = START_BIT;

        while (b > 0) {
            if (x >= y + b) {
                x -= y + b;
                y >>= 1;
                y += b;
            } else {
                y >>= 1;
            }

            b >>= 2;
        }

        return y;
    }


    public static void log(Logger log, int id, int type, String msg){
        StringBuffer buff = new StringBuffer();
        buff.append("ID[");
        buff.append(id);
        buff.append("],");
        buff.append("TYPE[");
        buff.append(type);
        buff.append("],");
        if(msg != null){
            buff.append(msg);
        }
        log.info(buff.toString());
    }

    private static final byte[] highDigits;

    private static final byte[] lowDigits;

    // initialize lookup tables
    static{
        final byte[] digits = {
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };

        int i;
        byte[] high = new byte[256];
        byte[] low = new byte[256];
        
        for(i = 0; i < 256; i++){
            high[i] = digits[i >>> 4];
            low[i] = digits[i & 0x0F];
        }

        highDigits = high;
        lowDigits = low;
    }

    public static String getHexdump(byte[] in){
        if(in == null)
            return "null";
        if(in.length == 0){
            return "empty";
        }

        StringBuffer out = new StringBuffer((in.length * 3));

        for(int i = 0; i < in.length; i++){
            int byteValue = in[i] & 0xFF;
            out.append((char)highDigits[byteValue]);
            out.append((char)lowDigits[byteValue]);
            out.append(' ');
        }
        return out.toString();
    }

    public static String getHexString(byte[] in){
        StringBuffer out = new StringBuffer((in.length * 3));

        for(int i = 0; i < in.length; i++){
            int byteValue = in[i] & 0xFF;
            out.append((char)highDigits[byteValue]);
            out.append((char)lowDigits[byteValue]);
        }
        return out.toString();
    }

//    public static void main(String[] args) throws Exception{
//        int leave = 50;
//        int value = 10 * 50 / 100;
//        int intelligence = value+8;
////        pet.setIntelligence(value + 8);
//        leave -= value;
//        value = 10 * 50 / 100;
//        int vitality = value+8;
////        pet.setVitality(value + 8);
//        leave -= value;
//        value = 40 * 50 / 100;
//        int agility = value+8;
////        pet.setAgility(value + 8);
//        leave -= value;
//        int strength = leave+8;
////        pet.setStrength(leave + 8);
//
//        System.out.println("int:"+intelligence+" vitality:"+vitality+" agility:"+agility+" strength:"+strength);
//    }

    public static int getRefreshAbilityMoney(PlayerData player){
        int points = player.getUsedAbilityPoint();
        int money = 0;
        if (player.getAbilityTimes() > 50){
        	money = points * points * 50 + 100;
        }else{
        	money = points * points * player.getAbilityTimes() + 100;
        }
        return money;
    }

    public static int getRefreshSkillMoney(PlayerData player, int type){
        int point = player.getSkillPoint(type);
        return point * 10 + 100;
    }

    //type 0 查看 1 学习
    public static String getAbilityDesc(byte type, PlayerData player, Ability ability){
        String result = ability.getDesc();
        String tmpStr1, tmpStr2;
        int idx, tmp;

        switch(ability.getEffect()){
            case 30: //Skill.EFFECT_RESTORE_LOT_HP
                idx = result.indexOf("<Z>");
                tmpStr1 = result.substring(0, idx);
                tmpStr2 = result.substring(idx + "<Z>".length());
                tmp = player.getLevel() * ability.getValue1() / 100;
                result = tmpStr1 + tmp + tmpStr2;

                break;
        }

        return result;
    }

    public static void removePetFavor(PlayerData player, Pet pet,boolean runaway,Changed changed){
    	if(player.getVipNewLevel() > 0){	//当拥有VIP等级时,宠物的忠诚度在战斗中不改变
    		return;
    	}
        if(player.getHp() > 0 && pet.getHp() <= 0){
            player.removeFavor(pet, 5, changed, new Random());
            return;
        }
        else if(player.getHp() <= 0 && pet.getHp() <= 0){
            player.removeFavor(pet, 4, changed, new Random());
            return;
        }
        if(runaway){
            player.removeFavor(pet, 3, changed, new Random());
            return;
        }
        else if(player.getHp() <= 0 && pet.getHp() > 0){
            player.removeFavor(pet, 3, changed, new Random());
            return;
        }
    }

    public static String getMessage(String s){
        int index = s.indexOf(' ');
        if(index!=-1)
            return s.substring(index);
        return s;
    }

    public static int getAuctionFee(int shopLevel,IValuableItem item,int count){
        if(item.getType()==IItem.TYPE_EQU)
            count = 1;
        if(shopLevel==1)
            return item.getPrice()*6/100*count;
        else if(shopLevel==2)
            return item.getPrice()*5/100*count;
        else if(shopLevel==3)
            return item.getPrice()*4/100*count;
        else if(shopLevel==5)
            return item.getPrice()*3/100*count;
        return item.getPrice()*6/100*count;
    }

    public static int getAuctionFee(IValuableItem item,int count){
        if(item.getType()==IItem.TYPE_EQU)
            count = 1;
        int tmp = item.getPrice()*count*8/100;
        return tmp==0?1:tmp;////物品价格过低导致托管费为0的场合，将托管费赋为1
    }

    /**
     * @param s
     * @return根据客户端判断是否要过滤掉4.3一下的客户端
     */
    public static int getStringLength(String s){
 /*       try {
            return s.getBytes("GBK").length;
        	//return s.length();
        } catch (UnsupportedEncodingException ex) {
            return -1;
        }*/
    	
    	 try {
    		String t = s;
    		if(t.indexOf("<c") >=0){//包括物品
    			int start = t.indexOf("<c");
    			int end = t.indexOf("c>");
    			String v = t.substring(start, end + 2);
    			t = t.replaceAll(v, "");
    			//去掉/s后的所有
    			int idStart = t.indexOf("/s");
    			t = t.substring(0, idStart);
    		}
         	return t.length();
         } catch (Exception ex) {
             return -1;
         }
    }
    
    
    /**
     * @param s
     * @return过滤掉新聊天的里面的特殊协议
     */
    public static String  filterChatString(String s){
 /*       try {
            return s.getBytes("GBK").length;
        	//return s.length();
        } catch (UnsupportedEncodingException ex) {
            return -1;
        }*/
    	
    	 try {
    		String t = s;
    		if(t.indexOf("<c") >=0){//包括物品
    			int start = t.indexOf("<c");
    			String v = t.substring(start, start + 9);
    			t = t.replaceAll(v, "");
    			
    			//过滤掉 </c>
    			t = t.replaceAll("</c>", "");
    			//去掉/s后的所有
    			int idStart = t.indexOf("/s");
    			t = t.substring(0, idStart);
    		}
         	return t;
         } catch (Exception ex) {
             return "XX";
         }
    }
    /**
     * 取得新聊天中的ID
     * @param s
     * @return
     */
    public static String[]  getItemID(String s){
    	 try {
    		 String[] ret = new String[2];
    	    String t = s;
    	    if(t.indexOf("<c") >=0){//包括物品
    	    	int start = t.indexOf("<c");
    	    	String v = t.substring(start, start + 9);
    	    	t = t.replaceAll(v, "");
    	    			
    	    	//过滤掉 </c>
    	    	t = t.replaceAll("</c>", "");
    	    	//去掉/s后的所有
    	    	int idStart = t.lastIndexOf("#");
    	    	ret[0]=  t.substring(idStart-1,idStart);
    	    	t = t.substring(idStart);
    	    	int idSpace = t.indexOf(" ");
    	    	ret[1] = t.substring(idSpace);		//物品的ID
    	    }
    	    return ret;
    	 } catch (Exception ex) {
    	    return null;
    	 }
    }
    
    public static String  filterClientChatString(String s){
    	 /*       try {
    	            return s.getBytes("GBK").length;
    	        	//return s.length();
    	        } catch (UnsupportedEncodingException ex) {
    	            return -1;
    	        }*/
    	    	
    	    	 try {
    	    		String t = s;
    	    		if(t.indexOf("<c") >=0){//包括物品
    	    			int index = t.indexOf(">");
    	    			if(index - t.indexOf("<c") != 8){
    	    				throw new Exception();
    	    			}
    	    			if(t.indexOf("/s") < 0){
    	    				throw new Exception();
    	    			}
    	    			if(t.indexOf("#") < 0){
    	    				throw new Exception();
    	    			}
    	    		}
    	         	return t;
    	         } catch (Exception ex) {
    	             return "XX";
    	         }
    	    }
    
    
    

    public static int getSiderealTime(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int val = (cal.get(Calendar.MONTH)+1)*2*60+(cal.get(Calendar.DAY_OF_MONTH)*4)+4*60+36+cal.get(Calendar.HOUR_OF_DAY)*60;
        if(val>=1440)
            val -= 1440;
        return val;
    }

    public static boolean isLuckyTime(int time){
        Calendar cal = Calendar.getInstance();
        int radio = (cal.get(Calendar.MONTH)+1)+cal.get(Calendar.DAY_OF_MONTH);
        int val = radio * time;
        while(val>=1440)
            val -= 1440;
        int current = cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
        return current>=val&&current<(val+60);
    }


    public static String getLuckyTimeString(int time){
        Calendar cal = Calendar.getInstance();
        int radio = (cal.get(Calendar.MONTH)+1)+cal.get(Calendar.DAY_OF_MONTH);
        int val = radio * time;
        while(val>=1440)
            val -= 1440;
        int hour = val/60;
        int minutes = val%60;
        return (hour>=10?""+hour:"0"+hour)+":"+(minutes>10?""+minutes:"0"+minutes);
    }



    public static long getNumber(byte[] buf, int off, int len) {
        long l = 0;
        for (int i = 0; i < len; i++) {
            l <<= 8;
            l += ((int) buf[off + i]) & 0xff;
        }
        return l;
    }

    public static int getMoney_package(int level ,Random rnd){
    	int moneyint = 0;
    	moneyint = getCount(rnd,
    			Money_package[level] * 95 / 100,
    			Money_package[level] * 105 / 100);
    	return moneyint;
    }

    static Pattern phonePattern = Pattern.compile("^13[0-9]{1}[0-9]{8}|^15[8,9]{1}[0-9]{8}");


    public static boolean isValidMobilePhone(String phone){
        return phonePattern.matcher(phone).matches();
    }

    static Pattern midPattern = Pattern.compile("[\\d]{14}");

    public static boolean isValidMID(String mid){
        return midPattern.matcher(mid).matches();
    }

    public static void main(String[] args){
        String s = "asdfsj-jkldsf-dksdf";
        String[] ss = s.split("-");
        for(String e:ss){
            System.out.println(e);
        }
//        Pattern p = Pattern.compile("\\{#(\\d+)\\}");
//        Matcher matcher = p.matcher("abskdded");
//        String[] ss = {"haha","hehe"};
//        StringBuffer sb = new StringBuffer();
//        int i = 0;
//        while(matcher.find()){
//            matcher.appendReplacement(sb,ss[i]);
//            i++;
//        }
//        matcher.appendTail(sb);
//        System.out.println(sb.toString());

//        if(matcher.matches()){
//            matcher.group(1);
////            String s = matcher.replaceFirst("1");
//            StringBuffer sb = new StringBuffer();
//            matcher.appendReplacement(sb,"haha");
//            System.out.println(sb.toString());
//        }
//        System.out.println(getTimeString(3600*1000L));
//        int i = (int)Math.ceil((double)1*90/100);
//        System.out.println(i);
//        int a = 0;
//        for(int i=1;i<50;i++){
//            a += Utils.EXP[i];
//        }
//        System.out.println(a);
//        System.out.println(isValidMobilePhone("13901105162"));
    }

    public static String decodeMid(String s) {
        if(s.length()==0)
            return "";
        sun.misc.BASE64Decoder de64 = new sun.misc.BASE64Decoder();
        try {
            byte[] decData = de64.decodeBuffer(s);
            byte[] keyData1 = "pipitime".getBytes("GBK");
            byte[] realKey1 = new byte[8];
            System.arraycopy(keyData1, 0, realKey1, 0,
                             Math.min(8, keyData1.length));
            com.pip.security.DESKeyGenerator.fixUpStatic(realKey1);
            com.pip.security.DES_CBC_PKCS5 cipher1 = new com.pip.security.
                    DES_CBC_PKCS5();
            cipher1.init(com.pip.security.DES_CBC_PKCS5.DECRYPT_MODE,
                         new com.pip.security.RawSecretKey("RAW", realKey1));
            byte[] decData2 = cipher1.doFinal(decData, 0, decData.length);
            String ret = new String(decData2, "GBK");
            if(isValidMID(ret))
                return ret;
            else
                return "";
        } catch (IOException ex) {
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    public static boolean isSameDay(Date date1,Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return (cal1.get(Calendar.YEAR)==cal2.get(Calendar.YEAR))&&(cal1.get(Calendar.DAY_OF_YEAR)==cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static int getDiscountPrice(int price,int discount){
        return (int)Math.ceil((double)price*discount/100);
//        return price*discount/100+(discount%100==0?0:1);
    }



    public static String getTimeString(long time) {
        long min = time / (60 * 1000L);
        long t = time % (60 * 1000L);
        long sec = t / 1000L;
        long ms = t % 1000L;
        StringBuilder sb = new StringBuilder(100);
        if (min == 0 && sec == 0) {
            sb.append(ms);
            sb.append("毫秒");
            return sb.toString();
        }
        if (min == 0 && sec != 0) {
            sb.append(sec);
            sb.append("秒");
            sb.append(ms);
            sb.append("毫秒");
            return sb.toString();
        }
        sb.append(min);
        sb.append("分钟");
        sb.append(sec);
        sb.append("秒");
        sb.append(ms);
        sb.append("毫秒");
        return sb.toString();

    }
//精炼石数量计算mengjie modify
    public static int[] getEnhanceItemCount(IEquipment equ, Enhance enhance) {
        int[] returnint = new int[2];
    	int s = 3;
        if (equ.isArmor()) {
            s = 2;
        } else if (equ.isWeapon()) {
            s = 1;
        }
        int oldstore = s * (equ.getTimes() + 1) * enhance.getRatio();
        int newstore = Math.max(((enhance.getPercentage() * oldstore)/10000 + enhance.getAdditional()),enhance.getBottom());
        returnint[0] = oldstore;
        returnint[1] = newstore;
        return returnint;
    }
    
    /**
     * @param equ
     * @param enhance
     * @param insteadIndex  要替换的星数索引
     * @return//星数替换使用的精炼石数量计算
     */
    public static int[] getInsteadEnhanceItemCount(IEquipment equ, Enhance enhance, int insteadIndex) {
        int[] returnint = new int[2];
    	int s = 3;
        if (equ.isArmor()) {
            s = 2;
        } else if (equ.isWeapon()) {
            s = 1;
        }
        int oldstore = s * (insteadIndex) * enhance.getRatio();
        int newstore = Math.max(((enhance.getPercentage() * oldstore)/10000 + enhance.getAdditional()),enhance.getBottom());
        returnint[0] = oldstore;
        returnint[1] = newstore;
        return returnint;
    }
    public static int getEnhanceItemProbability(IEquipment equ, int level, Enhance enhance) {
        int t = -5; //如果上次是成功，减少下次成功的百分比5%
        if (!equ.getLastEnhanceStatus()) {
            t = 2; //如果上次是失败，增加成功的百分比2%
        }
        int ret = ENHANCE_PROBABILITY[equ.getTimes()] + ENHANCE_PROBABILITY_ADJUST[level] +
                  t * equ.getEnhanceStatusTimes();
        return ret;
    }
    

    /**
     * @param equ
     * @param level
     * @param enhance
     * @param insteaIndex 星数替换索引
     * @return 星数替换成功率
     */
    public static int getInsteadEnhanceItemProbability(IEquipment equ, int level, Enhance enhance, int insteaIndex) {
        int t = -5; //如果上次是成功，减少下次成功的百分比5%
        if (!equ.getLastEnhanceStatus()) {
            t = 2; //如果上次是失败，增加成功的百分比1%
        }
        int ret = ENHANCE_PROBABILITY[insteaIndex-1] + ENHANCE_PROBABILITY_ADJUST[level] +
                  t * equ.getEnhanceStatusTimes();
        return ret;
    }
    public static int getEnhancePetProbability(Pet pet) {
    	int t = 100; 
        int probability = pet.getCurrentEnchancePoint() + 1;
        if (probability <= 10 && probability >= 0) {
            t = 100; 
        }else{
        	 if (probability <= 20 && probability > 10){
        		 t = 100 - (probability - 10);
        	 }else{
        		 if(probability <= 30 && probability > 20){
        			 t = 90 -  (int)((probability - 20) * 1.5);
        		 }else{
        			 if(probability <= 40 && probability > 30){
            			 t = 75 -  (probability - 30) * 2;
        			 }else{
        				 t = 0;
        			 }
        		 }
        	 }
        }
        return t;
    }
    public static String[] getWeekBeignEnd(){
    	String[] week= new String[2];
    	
    	final long MILLS_OF_DAY = 3600 * 24 * 1000;
        Calendar cal = Calendar.getInstance();
            
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String end = formatdate.format(cal.getTime());
        Date tmp_date = new Date(cal.getTime().getTime() - MILLS_OF_DAY * 7);
        Calendar calendar=Calendar.getInstance(); 
        calendar.setTime(tmp_date);
        String begin = formatdate.format(calendar.getTime());
        week[0] = begin;
        week[1] = end;
    	return week;
    }

    public static void resetEnhanceStatus(IEquipment equ,boolean status){
        if(equ.getLastEnhanceStatus()!=status){
            equ.setLastEnhanceStatus(status);
            equ.setEnhanceStatusTimes(1);
        }else{
            equ.setEnhanceStatusTimes(equ.getEnhanceStatusTimes()+1);
        }
    }

    public static String getWholeDataPrice(String price){
        int idx = price.indexOf(',');

        if(idx >= 0){
            return price.substring(0, idx);
        }else{
            return price;
        }
    }

    public static long getTodayStart(){
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime().getTime();
    }
    
    public static Date getTodayStartDate () {
    	Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * 把一个字符串按指定分隔符分段。
     * @param s 原始字符串
     * @param ch 分隔符
     * @return 分出的段的数组
     */
    public static String[] splitString(String s, char ch){
        int startIndex = 0;
        int endIndex = 0;
        Vector vS = new Vector();

        while(true){
            endIndex = s.indexOf(ch, startIndex);

            if(endIndex == -1){
                String tmp = s.substring(startIndex);

                if(tmp.length() > 0){
                    vS.addElement(tmp);
                }

                break;
            }else{
                vS.addElement(s.substring(startIndex, endIndex));
                startIndex = endIndex + 1;
            }
        }

        String[] strs = new String[vS.size()];
        vS.copyInto(strs);

        return strs;
    }
    
    /**
     * 得到RMB价格字符串
     * @param point
     * @return
     */
    public static String getRMBPrice(int point) {
        int a1 = point / 100;
        int b1 = point % 100;
        if (b1 < 10) {
            return a1 + ".0" + b1 + "元";
        } else {
            return a1 + "." + b1 + "元";
        }
    }

//    public static void main(String[] args){
//        Calendar cal = Calendar.getInstance();
//        System.out.println("Month:"+cal.get(Calendar.MONTH));
//        System.out.println("Day:"+cal.get(Calendar.DAY_OF_MONTH));
//        System.out.println("Hour:"+cal.get(Calendar.HOUR_OF_DAY));
//        System.out.println("Minute:"+cal.get(Calendar.MINUTE));
//    }
    
    public static boolean findUnyearEquip(PlayerData player,int enhanceCount){
 		boolean findFlag = false;
 		Grid[] grids = player.getEquipments(); 
		IEquipment item;
		Grid grid ;
		Grid[] showGrids = new  Grid[grids.length];//用于最后展示列表用的
		for(int i =0; i<grids.length; i++){
			grid =  grids[i];
			item = (IEquipment) grid.item;
			if(item.canEnhance()){
				 List<Enhance> enhances  = item.getEnhances();
				 //搜索星装
				 if(enhances.size() >= enhanceCount){
					 findFlag = true;
				 }
			}else{
				continue;
			}
		}
 		return findFlag;
 	}
    
    public static boolean findEnhancePetLimited(PlayerData player,int enhanceCount){
 		boolean findFlag = false;
 		Pet[] pets = player.getPets();
 		Pet pet = null;
 		for(int i =0; i<pets.length; i++){
			pet = pets[i];
			if(pet.getCurrentEnchancePoint()>= enhanceCount){
				findFlag = true;
				break;
			}else{
				continue;
			}
		}
 		return findFlag;
 	}
    public static boolean findYearEquip(PlayerData player,int enhanceCount, int type, int yeartype ){
    	boolean findFlag = false;
    	Grid[] grids = player.getEquipments(); 
		IEquipment item;
		Grid grid ;
		Grid[] showGrids = new  Grid[grids.length];//用于最后展示列表用的
		//Map<Integer, AnniversaryEnhance> unhenceYearEquipMap = AnniversaryEnhance.getMapUnhenceYearEquip();
		for(int i =0; i<grids.length; i++){
			grid =  grids[i];
			item = (IEquipment) grid.item;
			int itemId = item.getItemId();
			AnniversaryEnhance uEnhance = AnniversaryEnhance.getUnhenceYearEquip(itemId);
			//if(unhenceYearEquipMap.containsKey(itemId)){
			if(uEnhance!=null){
				if(uEnhance.getAnniversary() == yeartype  && uEnhance.getEquipType() == type && uEnhance.getCount() >= enhanceCount){
					findFlag = true;
					break;
				}
			}
		}
    	return findFlag;
    }
    //宠物寄养园所对应的一个石头给的经验的公式
    public static final int[] PET_VIP_EXP = {
			    	7,//    	等级为0整数7 
			    	7,//    	等级为1整数7 
			    	7,//    	等级为2整数7 
			    	7,//    	等级为3整数7 
			    	7,//    	等级为4整数7 
			    	7,//    	等级为5整数7 
			    	7,//    	等级为6整数7 
			    	7,//    	等级为7整数7 
			    	7,//    	等级为8整数7 
			    	7,//    	等级为9整数7 
			    	7,//    	等级为10整数7 
			    	7,//    	等级为11整数7 
			    	7,//    	等级为12整数7 
			    	7,//    	等级为13整数7 
			    	7,//    	等级为14整数7 
			    	14,//    	等级为15整数14 
			    	14,//    	等级为16整数14 
			    	14,//    	等级为17整数14 
			    	14,//    	等级为18整数14 
			    	14,//    	等级为19整数14 
			    	14,//    	等级为20整数14 
			    	14,//    	等级为21整数14 
			    	14,//    	等级为22整数14 
			    	14,//    	等级为23整数14 
			    	14,//    	等级为24整数14 
			    	14,//    	等级为25整数14 
			    	14,//    	等级为26整数14 
			    	14,//    	等级为27整数14 
			    	14,//    	等级为28整数14 
			    	14,//    	等级为29整数14 
			    	21,//    	等级为30整数21 
			    	21,//    	等级为31整数21 
			    	21,//    	等级为32整数21 
			    	21,//    	等级为33整数21 
			    	21,//    	等级为34整数21 
			    	21,//    	等级为35整数21 
			    	21,//    	等级为36整数21 
			    	21,//    	等级为37整数21 
			    	21,//    	等级为38整数21 
			    	21,//    	等级为39整数21 
			    	21,//    	等级为40整数21 
			    	21,//    	等级为41整数21 
			    	21,//    	等级为42整数21 
			    	21,//    	等级为43整数21 
			    	21,//    	等级为44整数21 
			    	21,//    	等级为45整数21 
			    	21,//    	等级为46整数21 
			    	21,//    	等级为47整数21 
			    	21,//    	等级为48整数21 
			    	21,//    	等级为49整数21 
			    	28,//    	等级为50整数28 
			    	28,//    	等级为51整数28 
			    	28,//    	等级为52整数28 
			    	28,//    	等级为53整数28 
			    	28,//    	等级为54整数28 
			    	28,//    	等级为55整数28 
			    	28,//    	等级为56整数28 
			    	28,//    	等级为57整数28 
			    	28,//    	等级为58整数28 
			    	28,//    	等级为59整数28 
			    	35,//    	等级为60整数35 
			    	35,//    	等级为61整数35 
			    	35,//    	等级为62整数35 
			    	35,//    	等级为63整数35 
			    	35,//    	等级为64整数35 
			    	35,//    	等级为65整数35 
			    	35,//    	等级为66整数35 
			    	35,//    	等级为67整数35 
			    	35,//    	等级为68整数35 
			    	35,//    	等级为69整数35 
			    	42,//    	等级为70整数42 
			    	42,//    	等级为71整数42 
			    	42,//    	等级为72整数42 
			    	42,//    	等级为73整数42 
			    	42,//    	等级为74整数42 
			    	42,//    	等级为75整数42 
			    	42,//    	等级为76整数42 
			    	42,//    	等级为77整数42 
			    	42,//    	等级为78整数42 
			    	42,//    	等级为79整数42 
			    	49,//    	等级为80整数49 
			    	49,//    	等级为81整数49 
			    	49,//    	等级为82整数49 
			    	49,//    	等级为83整数49 
			    	49,//    	等级为84整数49 
			    	49,//    	等级为85整数49 
			    	49,//    	等级为86整数49 
			    	49,//    	等级为87整数49 
			    	49,//    	等级为88整数49 
			    	49,//    	等级为89整数49 
			    	56,//    	等级为90整数56 
			    	56,//    	等级为91整数56 
			    	56,//    	等级为92整数56 
			    	56,//    	等级为93整数56 
			    	56,//    	等级为94整数56 
			    	56,//    	等级为95整数56 
			    	56,//    	等级为96整数56 
			    	56,//    	等级为97整数56 
			    	70,//    	等级为98整数70 
			    	70,//    	等级为99整数70 
			    	70,//    	等级为100整数70 
			    	70,//    	等级为101整数70 
    };
    
    // 宠物灵性给精灵带来的奖励加成(如：（宠物自身属性 + 装备属性）* 悟性奖励比例  * SPRITE_PROPERTIES_AWARD[level] / 10000
    public static final int[] SPRITE_PROPERTIES_AWARD = {
    	   0,
    	 300,
    	 500,
    	 700,
    	1000,
    	1300,
    	2000,
    	3000,
    	6000,
    };
    
    // 宠物灵性增加成功率
    public static final int[] PET_SPIRITUALITY_UP_SUCCESS_RATE = {
    	90,
    	79,
    	68,
    	50,
    	35,
    	25,
    	11,
    	2,
    };

    // 宠物悟性给自身带来的奖励加成(如：（宠物自身力 + 装备力） * PET_PERCEPTION_AWARD[level] / 10000)
    public static final int[] PET_PERCEPTION_AWARD = {
    	10000,
    	11000,
    	12000,
    	12500,
    	13000,
    	13500,
    	14000,
    	15000,
    	16000,
    };
    
    // 获得宠物悟性等级
    public static final short getPetPerceptionLevel (int perceptionPoint) {
    	if (perceptionPoint >= 1 && perceptionPoint <= Pet_PerceptionPoint[0]) {
            return 1;
        }
        if (perceptionPoint >= Pet_PerceptionPoint[0] + 1 && perceptionPoint <= Pet_PerceptionPoint[1]) {
            return 2;
        }
        if (perceptionPoint >= Pet_PerceptionPoint[1] + 1 && perceptionPoint <= Pet_PerceptionPoint[2]) {
            return 3;
        }
        if (perceptionPoint >= Pet_PerceptionPoint[2] + 1 && perceptionPoint <= Pet_PerceptionPoint[3]) {
            return 4;
        }
        if (perceptionPoint >= Pet_PerceptionPoint[3] + 1 && perceptionPoint <= Pet_PerceptionPoint[4]) {
            return 5;
        }
        if (perceptionPoint >= Pet_PerceptionPoint[4] + 1 && perceptionPoint <= Pet_PerceptionPoint[5]) {
            return 6;
        }
        if (perceptionPoint >= Pet_PerceptionPoint[5] + 1 && perceptionPoint <= Pet_PerceptionPoint[6]) {
            return 7;
        }
        if (perceptionPoint >= Pet_PerceptionPoint[6] + 1 && perceptionPoint <= Pet_PerceptionPoint[7]) {
            return 8;
        }
        return 0;
    }
    
    // 将炼化转化为悟性
    public static final int changePracticeToPerception (int practice) {
    	if (practice >= 1 && practice <= 10) {
    		return Pet_PerceptionPoint[0];
    	}
    	if (practice >= 11 && practice <= 20) {
    		return Pet_PerceptionPoint[1];
    	}
    	if (practice >= 21 && practice <= 30) {
    		return Pet_PerceptionPoint[2];
    	}
    	if (practice >= 31 && practice <= 35) {
    		return Pet_PerceptionPoint[3];
    	}
    	if (practice >= 36 && practice <= 39) {
    		return Pet_PerceptionPoint[4];
    	}
    	if (practice >= 40) {
    		return Pet_PerceptionPoint[5];
    	}
    	return 0;
    }
    
    // 悟性经验传换成悟性等级
    public static final String getPerceptionLevelName (int perceptionLevel) {
    	switch (perceptionLevel) {
    	case 1:
    		return "(悟性1级)";
    	case 2:
    		 return "(悟性2级)";
    	case 3:
    		return "(悟性3级)";
    	case 4:
    		return "(悟性4级)";
    	case 5:
    		return "(悟性5级)";
    	case 6:
    		 return "(悟性6级)";
    	case 7:
    		return "(悟性7级)";
    	case 8:
    		return "(悟性8级)";
		default:
			return "";	
    	}
    }
    
    public static int getPetUpLevelPerceptionAllPoint(int perceptionLevel){
    	int point = 0;
    	for(int i=0; i<perceptionLevel; i++){
    		point += Pet_PerceptionPoint[i];
    	}
    	return point;
    }
    // 获得宠物悟性的升级经验
    public static int getPetUpLevelPerceptionPoint (int perceptionLevel) {
        return Pet_PerceptionPoint[perceptionLevel];
    }
    
    // 宠物悟性升级需要的点数
    public static int[] Pet_PerceptionPoint = {
    	100,
    	1000,
    	5000,
    	10000,
    	30000,
    	60000,
    	120000,
    	150000,
    	999999,
	};
    
    // 获得可以给宠物增加count个技能的ID
    public static IntList getAddCount (Random rnd, int min, int max, int count, IntList abilities) {
    	IntList canAddSkillId = new ArrayIntList (count);
    	while (canAddSkillId.size() < count) {
    		int v = getCount(rnd, min, max);
    		if (!abilities.contains(v) && !canAddSkillId.contains(v)) {
    			canAddSkillId.add(v);
    		}
    	}
    	return canAddSkillId;
    }
    // 获得给宠物随机增加的count个技能
    public static Ability[] getAddPetAbilities (IntList abilities, int count) {
    	Random rnd = new Random();
    	IntList canAddSkill = getAddCount(rnd, 1001, 1026, count, abilities);
    	Ability[] ret = new Ability[canAddSkill.size()];
        for (int i = 0; i < canAddSkill.size(); i++) {
            ret[i] = Ability.getAbility(canAddSkill.get(i));
        }
        return ret;
    }
    // 获得给宠物增加多少个技能
    public static int getAddSkillCount (int currentLevel, int lastLevel) {
    	int count = 0;
    	for (int i = lastLevel + 1; i <= currentLevel; i ++) {
    		for (int j = 0; j < PET_PERCEPTION_LIMITS_OF_ADD_SKILL.length; j ++) {
    			if (i == PET_PERCEPTION_LIMITS_OF_ADD_SKILL[j]) {
    				count ++;
    			}
    		}
    	}
    	return count;
    }
    // 判断是否是宠物灵性下限
    public static boolean getPetSpiritualityLimit (int currentLevel) {
    	for (int i = 0; i < PET_SPIRITUALITY_LOWER_LIMITS.length; i ++) {
    		if (currentLevel == PET_SPIRITUALITY_LOWER_LIMITS[i]) {
    			return true;
    		}
    	}
    	return false;
    }
    
    // 判断是否发送灵性公告,是返回灵性级别，否返回-1
    public static int judgeSpiritualitySendNotice (int currentLevel, int lastLevel) {
    	if (lastLevel >= currentLevel) {
    		return -1;
    	} else {
    		for (int i = lastLevel + 1; i <= currentLevel; i ++) {
    			for (int j = 0; j < PET_SPIRITUALITY_NOTICE_REQUIREMENTS.length; j ++) {
    				if (i == PET_SPIRITUALITY_NOTICE_REQUIREMENTS[j]) {
    					return PET_SPIRITUALITY_NOTICE_REQUIREMENTS[j];
    				}
    			}
    		}
    		return -1;
    	}
    }
    
    // 判断是否发送悟性公告,是返回悟性级别，否返回-1
    public static int judgePerceptionSendNotice (int currentLevel, int lastLevel) {
    	if (lastLevel >= currentLevel) {
    		return -1;
    	} else {
    		for (int i = lastLevel + 1; i <= currentLevel; i ++) {
    			for (int j = 0; j < PET_PERCEPTION_NOTICE_REQUIREMENTS.length; j ++) {
    				if (i == PET_PERCEPTION_NOTICE_REQUIREMENTS[j]) {
    					return PET_PERCEPTION_NOTICE_REQUIREMENTS[j];
    				}
    			}
    		}
    		return -1;
    	}
    }
    
    // 转换物品，宠物名称协议
    public static String petIdToProtocol (Pet pet) {
    	String protocol = null;
    	int length = ((Integer)pet.getId()).toString().length();
		protocol = "/s 3#" + length + " " + pet.getId();
		return protocol;
    }
    
    // 转换物品，物品ID转化物品名称
    public static String itemIdToProtocolName (int itemId) {
    	String ret = null;
    	String clr = getClientItemColor(Items.getTemplate(itemId).getQuality());
    	String itemName = Items.getTemplate(itemId).getName();
    	ret = clr + itemName + "</c>";
		return ret;
    }
    
    // 转换物品，物品名称协议
    public static String itemIdToProtocol (int itemId) {
    	String protocol = null;
    	int length = ((Integer)itemId).toString().length();
		protocol = "/s 1#" + length + " " + itemId;
		return protocol;
    }
    
    public static String equIdtoProtocol (int equId) {
    	String protocol = null;
    	int length = ((Integer)equId).toString().length();
		protocol = "/s 2#" + length + " " + equId;
		return protocol;
    }
    
    //获取从最小到最大的随机数
    public static int getRandom(Random rnd, int min, int max){
    	return Math.abs(rnd.nextInt()) % (max - min + 1) + min;
    }
    
    public static int getRandom(int min, int max){
    	return getRandom(random, min, max);
    }
    
    public static int[] getSecondGenerationPetProperties (int petType, int mainPetPerceptionLevel, int secondPetPerceptionLevel) {
    	int as[] = null;
		int min = 0;
		int max = 0;
		int value = 0;
		Random rnd = new Random();
    	switch (petType) {
		case Pet.TYPE_0:		//力量型
			min = (mainPetPerceptionLevel * mainPetPerceptionLevel + 
					secondPetPerceptionLevel * secondPetPerceptionLevel) * 2;
			max = 256;
			value = Utils.getRandom(rnd, min, max);
			as = new int[]{17, 17, 22, value};
			break;
		case Pet.TYPE_1:		//智力型
			min = (mainPetPerceptionLevel * mainPetPerceptionLevel + 
					secondPetPerceptionLevel * secondPetPerceptionLevel) * 2;
			max = 256;
			value = Utils.getRandom(rnd, min, max);
			as = new int[]{value, 22, 17, 17};
			break;
		case Pet.TYPE_2:		//体力型
			min = (mainPetPerceptionLevel * mainPetPerceptionLevel + 
					secondPetPerceptionLevel * secondPetPerceptionLevel) * 2;
			max = 256;
			value = Utils.getRandom(rnd, min, max);
			as = new int[]{17, value, 17, 22};
			break;
		case Pet.TYPE_3:		//敏捷型
			min = (mainPetPerceptionLevel * mainPetPerceptionLevel + 
					secondPetPerceptionLevel * secondPetPerceptionLevel) * 2;
			max = 256;
			value = Utils.getRandom(rnd, min, max);
			as = new int[]{17, 17, value, 22};
			break;
		case Pet.TYPE_4:		//智力体力型
			min = mainPetPerceptionLevel * mainPetPerceptionLevel + 
				secondPetPerceptionLevel * secondPetPerceptionLevel + 10;
			max = 138;
			value = Utils.getRandom(rnd, min, max);
			as = new int[]{value, value, 17, 17};
			break;
		case Pet.TYPE_5:		//力量敏捷型
			min = mainPetPerceptionLevel * mainPetPerceptionLevel + 
				secondPetPerceptionLevel * secondPetPerceptionLevel + 10;
			max = 138;
			value = Utils.getRandom(rnd, min, max);
			as = new int[]{17, 17, value, value};
			break;
		}
    	return as;
    }
    
    /**
     * 十进制数解析
     * @param s
     * @return
     * @exception NumberFormatException 参数为空,参数中有非法字符,参数超过整形范围
     */
    public static int parseInt( final String s )
    {
        if ( s == null )
            throw new NumberFormatException( "Null string" );

        int num  = 0;
        int sign = -1;
        final int len  = s.length( );
        final char ch  = s.charAt( 0 );
        if ( ch == '-' )
        {
            if ( len == 1 )
                throw new NumberFormatException( "Missing digits:  " + s );
            sign = 1;
        }
        else
        {
            final int d = ch - '0';
            if ( d < 0 || d > 9 )
                throw new NumberFormatException( "Malformed:  " + s );
            num = -d;
        }

        final int max = (sign == -1) ?
            -Integer.MAX_VALUE : Integer.MIN_VALUE;
        final int multmax = max / 10;
        int i = 1;
        while ( i < len )
        {
            int d = s.charAt(i++) - '0';
            if ( d < 0 || d > 9 )
                throw new NumberFormatException( "Malformed:  " + s );
            if ( num < multmax )
                throw new NumberFormatException( "Over/underflow:  " + s );
            num *= 10;
            if ( num < (max+d) )
                throw new NumberFormatException( "Over/underflow:  " + s );
            num -= d;
        }

        return sign * num;
    }

    /**
     * 十进制数解析，切记调用此方法应该确认参数绝对是一个合法的整形字符串
     * @param s
     * @return
     */
    public static int parseValidInt(final String s){
        int num  = 0;
        int sign = -1;
        final int len  = s.length( );
        final char ch  = s.charAt( 0 );
        if ( ch == '-' )
            sign = 1;
        else
            num = '0' - ch;

        int i = 1;
        while ( i < len )
            num = num*10 + '0' - s.charAt( i++ );

        return sign * num;

    }
    
    public static int getLoginTimeSecond(long loginTime){
    	int nowSecond = (int)(new Date().getTime() / 1000L);
    	return nowSecond - (int)(loginTime / 1000L);
    }
    
    public static int getLoginTimeSecond(long now, long loginTime){
    	return (int)(now / 1000L - loginTime / 1000L);
    }
    
    /**
     * 获得下一次可以出售佣兵的时间
     * @return
     */
    public static Date getSellNextDate(boolean pre, boolean cut4){
    	Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if(cut4 && cal.get(Calendar.HOUR_OF_DAY) < 4){
        }else{
        	cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + (pre ? -1 : 1));
        }
        cal.set(Calendar.HOUR_OF_DAY, 4);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    public static short checkCRC(byte[] byteArray, short inCRC, int start, int length){
        short tmp;
        int i;

        if(byteArray != null){
            for(i = start; i < start + length - 2; i += 2){
                tmp = (short)(((byteArray[i] & 0xFF) << 8) | (byteArray[i + 1] & 0xFF));

                inCRC ^= tmp;
            }

            if(i == start + length - 2){
                tmp = (short)(((byteArray[i] & 0xFF) << 8) | (byteArray[i + 1] & 0xFF));
            }else{
                tmp = (short)(((0 & 0xFF) << 8) | (byteArray[i] & 0xFF));
            }

            inCRC ^= tmp;
        }

        return inCRC;
    }
}
