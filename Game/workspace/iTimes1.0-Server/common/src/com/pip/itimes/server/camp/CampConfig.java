package com.pip.itimes.server.camp;

import java.util.TreeMap;

public class CampConfig{
    public static CampAuctionConfig campAuctionConfig = new CampAuctionConfig();
    public static CampVoteConfig campVoteConfig = new CampVoteConfig();
    public static TreeMap<Integer, CampSkill> campSkills = new TreeMap<Integer, CampSkill>();
    
    public static final long SILENCE_TIME = 2000L * 60L * 60L;	//领袖禁言时间更改为2小时
    public static final long SILENCE_TIME_OFFICIAL = 1000L * 60L * 30L;
    public static final long SILENCE_OFFICIAL_COUNT = 10;		//官员禁言的次数

    public static int taxDefault = 5;
    public static int taxMin = 5;
    public static int taxMax = 20;
    public static int taxNoCamp = 20;
    public static int qualificationLevel = 50;
    public static int voteLevel = 50;
    public static int amerceLimit = 10;
    public static int amerceLimitOfficial = 5;			//财政司官员罚款的次数
    public static int amercePlayerLimit = 10;
}
