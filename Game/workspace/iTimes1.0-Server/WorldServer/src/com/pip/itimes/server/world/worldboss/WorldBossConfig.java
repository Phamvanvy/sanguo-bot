package com.pip.itimes.server.world.worldboss;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.server.stage.BossRush;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.ItemGroup.PetDevelopTop;
import com.pip.itimes.server.world.ItemGroup.PetDevelopTopData;
import com.pip.itimes.server.world.noahsark.NoahsarkConfig;

/**
 * @file WorldBossConfig.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-9-19
 **/
public class WorldBossConfig {
	
	private static final Logger log = Logger.getLogger(WorldBossConfig.class);
	
	public static Random rand = new Random();
	public static boolean open = false;
	public static int blessingPercent = 20;
	public static int blessingPercentMax = 100;
	public static WorldBossData worldBossData;
	private static int worldBossUpLevel = 0;		//等级的增加值 BOSS在获取等级的时候 需要加上这个值
	public static int worldBossAwardStage = 0;		//BOSS目前的奖励的阶段
	private static int worldBossInTimeIndex = -1;	//目前在哪个时间里
	
	public static ArrayList<WorldBossTime> worldBossTimes = new ArrayList<WorldBossTime>();
	public static ConcurrentHashMap<Integer, WorldBossAward> worldBossAwards = new ConcurrentHashMap<Integer, WorldBossAward>();
	public static WorldBossAwardItem roundAward[];
	public static Monster[] worldBossMonster = null;
	//当前玩家伤害表
	public static ConcurrentHashMap<Integer, WorldBossPlayer> worldBossPlayers = new ConcurrentHashMap<Integer, WorldBossPlayer>();
	//当前玩家排行榜
	public static ConcurrentHashMap<Integer, WorldBossPlayer> worldBossTops = new ConcurrentHashMap<Integer, WorldBossPlayer>();
	public static int MAX_TOP = 10;
	public static List<WorldBossPlayer> sortTop = new ArrayList<WorldBossPlayer>();
	
	public static String PATH = "WorldBoss";
	private static int luckRandom = 0;
	
	public static String strActionTime[] = {
		"活动时间在每天18:30分开始，现在还不能进行。"
	};
	
	static{
		loadfile();
		loadBoss();
	}
	
	public static String getCurrentStartMessage(){
		if(worldBossInTimeIndex < 0){
			return strActionTime[0];
		}
		return strActionTime[worldBossInTimeIndex];
	}
	
	/**
	 * 关闭世界BOSS系统
	 */
	public static void close(){
		state = STATE_NULL;
		worldBossInTimeIndex = -1;
	}
	
	public static void addPlayerRoundHurt(WorldPlayer[] players, int hurt){
		if(state == STATE_STARTING){
			synchronized (worldBossMonster) {
				for(int i=0; i<players.length; i++){
					worldBossMonster[0].setHp(worldBossMonster[0].getHp() - hurt);
					if(getWorldBossHpPercent() < 10){
						state = STATE_BOSSDIE;
						log.info("WorldBoss action starting to bossdie");
					}
					WorldBossPlayer worldBossPlayer = null;
					if(worldBossPlayers.containsKey(players[i].getId())){
						worldBossPlayer = worldBossPlayers.get(players[i].getId());
						worldBossPlayer.hurt += hurt;
					}else{
						worldBossPlayer = new WorldBossPlayer();
						worldBossPlayer.id = players[i].getId();
						worldBossPlayer.name = players[i].getPlayerName();
						worldBossPlayer.hurt = hurt;
						worldBossPlayers.put(players[i].getId(), worldBossPlayer);
					}
					addPlayerTop(worldBossPlayer);
					Changed changed = new Changed();
					int exp = 0;
					int money = 0;
					for(WorldBossAwardItem item : roundAward){
						if(item.exp > 0){
							exp += hurt * item.exp / item.dem;
						}
						if(item.money > 0){
							money += hurt * item.money / item.dem;
						}
					}
					if(exp > 0){
						players[i].addExp(exp, changed);
						worldBossPlayer.allexp += exp;
					}
					if(money > 0){
						players[i].addMoney(money, changed);
						worldBossPlayer.allmoney += money;
					}
					Server.instance.battleService.getConnectService().sendGetItem(changed, players[i].getId(), (byte) 1);
					log.info("WorldBoss playerID[" + players[i].getId() + "]add round hurt[" + hurt + "] exp[" + exp + "] money[" + money + "]");
				}
			}
		}
	}
	
	private static void sendAwardItems(WorldBossPlayer player, WorldBossAwardItem items[], int rank){
		Changed changed = new Changed();
		int exp = 0;
		int money = 0;
		String mailConnect = "你在这次抵抗怪物入侵中表现英勇，共计造成hurt点伤害，排行第rank位，共计获得exp经验，money金钱和";
		for(WorldBossAwardItem item : items){
			if(item.exp > 0){
				exp += item.exp;
			}
			if(item.money > 0){
				money += item.money;
			}
			if(item.itemcount != 0){
				IItem iit = Items.getTemplate(item.itemid).newInstance();
				int count = item.itemcount;
				mailConnect += "[" + iit.getName() + "]*" + count;
				if(iit != null){
					byte[] att = ItemUtils.item2dbAttachment(iit, count);
					Server.instance.mailService.sendMail(player.id, player.name, -1, "系统",
							"抵抗怪兽入侵奖励", "抵抗怪兽入侵 获得" + iit.getName() + "*" + count + "奖励。", att, 0, true);
					log.info("WorldBoss playerID[" + player.id + "]add award item[" + iit.getName() + "] count[" + count + "]");
				}
			}
		}
		WorldPlayer catchPlayer = null;
		if(exp > 0){
			if(catchPlayer == null){
				catchPlayer = Server.instance.playerService.getWorldPlayerAndCatch(player.id);
			}
			if(catchPlayer != null){
				catchPlayer.addExp(exp, changed);
			}
		}
		if(money > 0){
			if(catchPlayer == null){
				catchPlayer = Server.instance.playerService.getWorldPlayerAndCatch(player.id);
			}
			if(catchPlayer != null){
				catchPlayer.addMoney(money, changed);
			}
		}
		if(catchPlayer != null && catchPlayer.online()){
			Server.instance.battleService.getConnectService().sendGetItem(changed, player.id, (byte) 1);
		}
		if(catchPlayer != null){
			Server.instance.playerService.releasePlayer(catchPlayer);
		}
		
		//幸运大奖
		if(rank < 3){
			if(rank == 1){
				luckRandom = Utils.getRandom(1, 100);
			}
			boolean good = false;
			switch(rank){
			case 1:
				good = luckRandom <= 50;
				break;
			case 2:
				good = luckRandom > 50 && luckRandom <= 80;
				break;
			case 3:
				good = luckRandom > 80;
				break;
			}
			if(good){
				int addMoney = (getWorldBossLevel() - 1) * 100000;
				if(addMoney > 0){
					byte[] att = ItemUtils.money2dbAttachment(addMoney);
					Server.instance.mailService.sendMail(player.id, player.name, -1, "系统", "抵抗怪兽入侵幸运大奖", "恭喜你在本次抗击邪火凤凰的战斗中贡献卓越，并获得了幸运大奖：" + addMoney + "金币。", att, 0, true);
					log.info("WorldBoss playerID[" + player.id + "]luck money[" + addMoney + "] rank[" + rank + "]");
				}
			}
		}
		
		mailConnect = mailConnect.replaceAll("exp", "" + player.allexp);
		mailConnect = mailConnect.replaceAll("money", "" + player.allmoney);
		mailConnect = mailConnect.replaceAll("hurt", "" + player.hurt);
		mailConnect = mailConnect.replaceAll("rank", "" + rank);
		Server.instance.mailService.sendMail(player.id, player.name, -1, "系统", "抵抗怪兽入侵", mailConnect, null, 0, true);
		log.info("WorldBoss playerID[" + player.id + "]add award rank[" + rank + "] allexp[" + player.allexp + "] allmoney[" + player.allmoney + "]" + " allhurt[" + player.hurt + "]");		
	}
	
	/**
	 * 玩家创建一个新的战斗的时候,将玩家战斗的时候修改一下
	 * @param player
	 */
	public static void addPlayer2Battle(WorldPlayer player){
		WorldBossPlayer worldBossPlayer = null;
		if(worldBossPlayers.containsKey(player.getId())){
			worldBossPlayer = worldBossPlayers.get(player.getId());
			worldBossPlayer.battletime = System.currentTimeMillis();
		}else{
			worldBossPlayer = new WorldBossPlayer();
			worldBossPlayer.id = player.getId();
			worldBossPlayer.name = player.getPlayerName();
			worldBossPlayer.battletime = System.currentTimeMillis();
			worldBossPlayers.put(player.getId(), worldBossPlayer);
		}
		log.info("WorldBoss playerID[" + player.getId() + "]add battle");
	}
	
	/**
	 * 检测是否可以进入战斗,返回0可以战斗,大于0则是需要再等待的秒数,-1则表示在硬性的时间内
	 * @param player
	 * @return
	 */
	public static int canAdd2Battle(WorldPlayer player, boolean gettime){
		WorldBossPlayer worldBossPlayer = null;
		if(worldBossPlayers.containsKey(player.getId())){
			worldBossPlayer = worldBossPlayers.get(player.getId());
			long usedsecond = (System.currentTimeMillis() - worldBossPlayer.battletime) / 1000;
			if(usedsecond < worldBossData.getRoundHardSecond()){
				if(gettime){
					return (int)(worldBossData.getRoundSecond() - usedsecond);
				}
				return -1;
			}
			if(usedsecond < worldBossData.getRoundSecond()){
				return (int)(worldBossData.getRoundSecond() - usedsecond);
			}
		}
		return 0;
	}
	
	/**
	 * 获取世界BOSS的详细信息
	 * @return
	 */
	public static String getWorldBossInfo(){
		if(worldBossMonster == null){
			return "";
		}else{
			StringBuilder sb = new StringBuilder();
			sb.append(worldBossMonster[0].getName());
			sb.append(",");
			sb.append(getWorldBossLevel());
			sb.append(",");
			sb.append(getWorldBossHpPercent());
			sb.append(",");
			sb.append(int2str(worldBossMonster[0].getHp()));	//当前血量
			sb.append(",");
			sb.append(int2str(worldBossData.getHp()));	//血量上限
			return sb.toString();
		}
	}
	
	public static int getWorldBossHpPercent(){
		if(worldBossMonster != null){
			if(worldBossData.getHp() > 1000000){
				return worldBossMonster[0].getHp() / (worldBossData.getHp() / 100);
			}
			return worldBossMonster[0].getHp() * 100 / worldBossData.getHp();
		}
		return 0;
	}
	
	public static int getPlayerAPPercent(WorldPlayer player){
		WorldBossPlayer worldBossPlayer = null;
		if(worldBossPlayers.containsKey(player.getId())){
			worldBossPlayer = worldBossPlayers.get(player.getId());
			return worldBossPlayer.addPAPercent;
		}
		return 0;
	}
	
	public static String int2str(int value){
		if(value < 10000){
			return "" + value;
		}
		if(value < 100000000){
			return (value / 10000) + "万";
		}
		return (value / 100000000) + "亿";
	}
	
	/**
	 * 祝福功能 这里添加攻击百分比 不能超过100%
	 * @param player
	 * @param percent
	 * @return
	 */
	public static boolean addPlayerAPPercent(WorldPlayer player){
		synchronized (worldBossMonster) {
			WorldBossPlayer worldBossPlayer = null;
			if(worldBossPlayers.containsKey(player.getId())){
				worldBossPlayer = worldBossPlayers.get(player.getId());
				if(worldBossPlayer.addPAPercent >= blessingPercentMax){
					return false;
				}
				worldBossPlayer.addPAPercent += blessingPercent;
			}else{
				worldBossPlayer = new WorldBossPlayer();
				worldBossPlayer.id = player.getId();
				worldBossPlayer.name = player.getPlayerName();
				worldBossPlayer.addPAPercent = blessingPercent;
				worldBossPlayers.put(player.getId(), worldBossPlayer);
			}
			if(worldBossPlayer.addPAPercent > blessingPercentMax){
				worldBossPlayer.addPAPercent = blessingPercentMax;
			}
			return true;
		}
	}
	
	/**
	 * 将Player尝试加入排行榜
	 * @param player
	 * @param hurt
	 */
	public static void addPlayerTop(WorldBossPlayer player){
		synchronized (worldBossTops) {
			if(worldBossTops.containsKey(player.id)){
				WorldBossPlayer p = worldBossTops.get(player.id);
				p.hurt = player.hurt;
				sortTop = sortplayer(worldBossTops);
				saveplayerData();
			}else{
				if(worldBossTops.size() >= MAX_TOP){
					WorldBossPlayer tempplayer = getPlayerMin(worldBossTops);
					if(tempplayer != null){
						if(player.hurt < tempplayer.hurt){
							return;
						}
						worldBossTops.remove(tempplayer.id);
					}
				}
				WorldBossPlayer wbp = new WorldBossPlayer();
				wbp.id = player.id;
				wbp.name = player.name;
				wbp.hurt = player.hurt;
				worldBossTops.put(player.id, wbp);
				sortTop = sortplayer(worldBossTops);
				saveplayerData();
			}
		}
	}
	
	/**
	 * 获得排行榜中最小值的玩家
	 * @param map
	 * @return
	 */
	public static WorldBossPlayer getPlayerMin(ConcurrentHashMap<Integer, WorldBossPlayer> map){
		Iterator<WorldBossPlayer> iter = map.values().iterator();
		WorldBossPlayer tempplayer = null;
		while(iter.hasNext()){	
			WorldBossPlayer currentplayer = iter.next();
			if(tempplayer == null){
				tempplayer = currentplayer;
			}else{
				if(currentplayer.hurt < tempplayer.hurt){
					tempplayer = currentplayer;
				}
			}
		}
		return tempplayer;
	}
	
	public static int getWorldBossMgId(){
		return worldBossData.getMgId();
	}
	
	public static Monster[] createWorldBoss(StageService service){
		MonsterGroup mg = service.getMonsterGroup(worldBossData.getMgId());
		byte[] mIds = mg.getMonstersId();
        short[] pros = mg.getProbabilities();
        List l = new ArrayList(3);
        Monster[] monsters = service.getMonsters(mg.getId());
        for (int i = 0; i < mIds.length; i++) {
            if (hit100(rand, pros[i])) {
                l.add(monsters[i]);
            }
        }
        Monster[] ms = new Monster[l.size()];
        l.toArray(ms);
        int level = getWorldBossLevel();
        int pa = level * 1000;
        int def = level * level;
        int hp = level * level * level * (level - 30) / 10 * 12;
        int mp = Integer.MAX_VALUE;
        worldBossData.setHp(hp);
        worldBossData.setMp(mp);
        worldBossData.setPa(pa);
        worldBossData.setDef(def);
        for(int i=0;i<ms.length;i++){
        	ms[i].setLevel((short)level);
        	ms[i].setSpecialHP(hp);
        	ms[i].setSpecialMP(mp);
        	ms[i].setHp(hp);
        	ms[i].setMp(mp);
        	ms[i].setMaxHp(hp);
        	ms[i].setMaxMp(mp);
        	ms[i].setPDef(def);
        	ms[i].setMDef(def);
        }
		return ms;
	}
	
	public static int getBossMaxHp(){
		return worldBossData.getHp();
	}
	
	public static int getBossMaxMp(){
		return worldBossData.getMp();
	}
	
	public static int getBossHp(){
		return worldBossMonster[0].getHp();
	}
	
	public static int getBossMp(){
		return worldBossMonster[0].getMp();
	}
	
    private static boolean hit100(Random rnd, int chance) {
        int r = rnd.nextInt(100);
        if (r <= chance)
            return true;
        return false;
    }
	
    /**
     * 提升世界BOSS的等级 但等级不会超过限制的等级
     * @param level
     */
	public static boolean worldBossLevelUp(int level){
		boolean flag = false;
		//不在时间段内 不能进行升级
		if(worldBossInTimeIndex == -1){
			return flag;
		}
		WorldBossTime time = worldBossTimes.get(worldBossInTimeIndex);
		Calendar cal = Calendar.getInstance();
		//在能够升级的时间可以进行升级
		if(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE) <= time.getStartTime() + worldBossData.getLeveluptime()){
			worldBossUpLevel += level;
			if(getWorldBossLevel() > worldBossData.getMaxLevel()){
				worldBossUpLevel = worldBossData.getMaxLevel() - worldBossData.getLevel();
			}
			flag = true;
		}
		createWorldBoss(Server.instance.stageService);
		saveBossData();
		return flag;
	}
	
	/**
	 * 判断是否是在某个活动的时间段内
	 * @param offsetMinute 可以设置为提前的分钟数 这个主要用于狮子吼
	 * @return
	 */
	public static WorldBossTime isActivityTime(int offsetMinute){
		Calendar cal = Calendar.getInstance();
		int time = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		for(WorldBossTime worldBossTime : worldBossTimes){
			if(time >= worldBossTime.getStartTime() + offsetMinute && time <= worldBossTime.getEndTime() + offsetMinute){
				return worldBossTime;
			}
		}
		return null;
	}

	
	public static void changeAwards(int level){
		for(WorldBossAward award : worldBossAwards.values()){
			if(level >= award.startLevel && level <= award.endLevel){
				worldBossAwardStage = award.stage;
				return;
			}
		}
		worldBossAwardStage = worldBossAwards.size() - 1;
		if(worldBossAwardStage < 0){
			worldBossAwardStage = 0;
		}
	}
	
	public static int getWorldBossLevel(){
		if(worldBossData != null){
			return worldBossData.getLevel() + worldBossUpLevel;
		}
		return 0;
	}
	
	public final static byte STATE_NULL = 0;		//无状态
	public final static byte STATE_AG = 1;			//公告状态
	public final static byte STATE_STARTING = 2;	//开始状态
	public final static byte STATE_TIMEOUT = 3;		//时间结束
	public final static byte STATE_BOSSDIE = 4;		//BOSS被打退 可进行升级状态
	public final static byte STATE_WAITOUT = 5;		//等待时间结束
	public final static byte STATE_TEMP = 6;		//临时状态
	public static byte state = STATE_NULL;
	
	public static void action(long now){
		if(!open) return;
		if(worldBossInTimeIndex >= 0){
			switch(state){
			case STATE_AG:
				WorldBossTime time = worldBossTimes.get(worldBossInTimeIndex);
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(now);
				if(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE) >= time.getStartTime()){
					state = STATE_STARTING;
					Server.instance.chatService.sendRoarMessage( -1, "狮子吼", "邪火凤凰已经入侵幻想大陆，大家快来我这（瓦伊特）共同抵御啊！", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
					log.info("WorldBoss action starting");
				}
				break;
			case STATE_STARTING:
			case STATE_WAITOUT:
				time = worldBossTimes.get(worldBossInTimeIndex);
				cal = Calendar.getInstance();
				cal.setTimeInMillis(now);
				if(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE) >= time.getEndTime()){
					if(state == STATE_STARTING){
						state = STATE_TEMP;
						sendPlayerAward(worldBossAwardStage);
						Server.instance.chatService.sendRoarMessage( -1, "狮子吼", "勇士们没有在指定时间内击退邪火凤凰，幻想大陆的财产遭受重大打击，下次一定要誓死守护啊。", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
						log.info("WorldBoss action starting to timeout");
					}
					state = STATE_TIMEOUT;
				}
				break;
			case STATE_TIMEOUT:
				state = STATE_NULL;
				worldBossInTimeIndex = -1;
				log.info("WorldBoss action reset null");
				break;
			case STATE_BOSSDIE:
				state = STATE_TEMP;
				boolean flag = worldBossLevelUp(1);
				sendPlayerAward(worldBossAwardStage);
				changeAwards(getWorldBossLevel());
				if(flag){
					Server.instance.chatService.sendRoarMessage( -1, "狮子吼", "勇士们在15分钟内击退了邪火凤凰，明天它将更加强大，同时也会携带更多的宝物！", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
				}else{
					Server.instance.chatService.sendRoarMessage( -1, "狮子吼", "勇士们同心协力击退了邪火凤凰，保护了幻想大陆，明天它还会再来的！", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
				}
				log.info("WorldBoss action bossdie to waitout");
				state = STATE_WAITOUT;
				break;
			}
		}else{
			WorldBossTime time = isActivityTime(-3);
			if(time != null){
				//清除上次参与的人数
				worldBossPlayers.clear();
				sortTop.clear();
				worldBossTops.clear();
				state = STATE_AG;
				createWorldBoss(Server.instance.stageService);
				worldBossInTimeIndex = time.getIndex();
		    	Server.instance.chatService.sendRoarMessage( -1, "狮子吼", "3分钟后邪火凤凰将要入侵幻想大陆，大家快来我这（瓦伊特）共同抵御啊！", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
		    	log.info("WorldBoss action ag");
			}
		}
	}
	
	public static void sendPlayerAward(int stage){
		synchronized (worldBossPlayers) {
			WorldBossAward award = worldBossAwards.get(stage);
			if(award != null){
				List<WorldBossPlayer> sortAllPlayer = sortplayer(worldBossPlayers);
				if(sortAllPlayer != null && sortAllPlayer.size() > 0){
					//2013年1月15日11:49:45 只有官服才会产出奖励 by zxyu
					if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
						NoahsarkConfig.setWorldTopPrizes();
					}
					StringBuilder sb = new StringBuilder();
					for(int i=0; i<sortAllPlayer.size(); i++){
						WorldBossPlayer player = sortAllPlayer.get(i);
						WorldBossAwardItem items[] = award.otherItems;
						if(i == 0){
							items = award.firstItems;
						}else if(i == 1){
							items = award.secondItems;
						}else if(i == 2){
							items = award.thirdItems;
						}else if(i < 10){
							items = award.top10Items;
						}else if(i < 20){
							items = award.top20Items;
						}else if(i < 50){
							items = award.top50Items;
						}
						sendAwardItems(player, items, i + 1);
						if(i < 3){
							sb.append("<" + player.name + ">");				
						}
					}
					sb.append("在这次邪火凤凰入侵的保卫战当前取得了傲人的成绩，他们是英雄，祝贺他们吧！");
					Server.instance.chatService.sendRoarMessage( -1, "狮子吼", sb.toString(), true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
				}
			}else{
				log.info("WorldBoss stage award[" + stage + "] is null");
			}
		}
	}
	
	//将表中数据排序
	public static List<WorldBossPlayer> sortplayer(ConcurrentHashMap<Integer, WorldBossPlayer> mpp){
		Iterator<WorldBossPlayer> iter = mpp.values().iterator();
		while(iter.hasNext()){
			WorldBossPlayer playervalue = iter.next();
			if(playervalue != null){
				List<WorldBossPlayer> sortTopData = new ArrayList<WorldBossPlayer>();
				WorldBossPlayer p = new WorldBossPlayer();
				p.id = playervalue.id;
				p.hurt = playervalue.hurt;
				p.name = playervalue.name;
				p.allexp = playervalue.allexp;
				p.allmoney = playervalue.allmoney;
				sortTopData.add(p);
				while(iter.hasNext()){
					WorldBossPlayer playerData = iter.next();
					if(playerData != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i = 0;i<size;i++){
							WorldBossPlayer temp = sortTopData.get(i);
							if(playerData.hurt > temp.hurt){
								p = new WorldBossPlayer();
								p.id = playerData.id;
								p.hurt = playerData.hurt;
								p.name = playerData.name;
								p.allexp = playerData.allexp;
								p.allmoney = playerData.allmoney;
								sortTopData.add(i, p);
								insert = true;
								break;
							}
						}
						if(!insert){
							p = new WorldBossPlayer();
							p.id = playerData.id;
							p.hurt = playerData.hurt;
							p.name = playerData.name;
							p.allexp = playerData.allexp;
							p.allmoney = playerData.allmoney;
							sortTopData.add(p);
						}
					}
				}
				return sortTopData;
			}
		}
		return null;
	}
	
	//保存排行榜信息
	public static void saveplayerData(){
		try {
			synchronized (worldBossTops) {
				Document doc = DocumentHelper.createDocument();
				Element root = doc.addElement(PATH);
				Element attrElement = root.addElement("players");
				for(WorldBossPlayer bbp : worldBossTops.values()){
					Element elItemTopData = attrElement.addElement("Data");
					elItemTopData.addAttribute("playerid", "" + bbp.id);
					elItemTopData.addAttribute("playername", "" + bbp.name);
					elItemTopData.addAttribute("hurt", "" + bbp.hurt);
				};
				try {
		        	String path = System.getProperty("user.dir") + "/" + PATH;
		        	File dir = new File(path);
		        	if(!dir.exists()){
		        		dir.mkdir();
		        	}
		        	File file = new File(PATH + "/" + PATH + ".xml");
		        	file.createNewFile();
					saveDocument(doc, new FileWriter(file));
					log.info("Save WorldBoss Top ok");
				} catch (IOException e) {
					log.error(e, e);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	public static void saveBossData(){
		try {
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement(PATH);
			Element bossElement = root.addElement("Boss");
			bossElement.addAttribute("level", "" + worldBossUpLevel);
			try {
				String path = System.getProperty("user.dir") + "/" + PATH;
				File dir = new File(path);
				if(!dir.exists()){
					dir.mkdir();
				}
				File file = new File(PATH + "/bossData.xml");
				file.createNewFile();
				saveDocument(doc, new FileWriter(file));
				log.info("Save WorldBoss boss ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	public static void saveDocument(Document doc, Writer w){
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        XMLWriter writer = new XMLWriter(w, format);
        try {
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			 try {
				writer.close();
			} catch (IOException e) {
			}
		}
    }
	
	//读取文件
	public static void loadfile(){
		synchronized (worldBossTops) {
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/" + PATH + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
		    		worldBossTops.clear();
					Element attrRoot = root.element("players");
	    			for(Iterator data = attrRoot.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("playerid"));
						String playername = elData.attributeValue("playername");
						int hurt = Integer.parseInt(elData.attributeValue("hurt"));
						WorldBossPlayer player = new WorldBossPlayer();
						player.id = playerid;
						player.name = playername;
						player.hurt = hurt;
						worldBossTops.put(playerid, player);
					}
	    			if(worldBossTops.size() > MAX_TOP){
	    				int removeCount = worldBossTops.size() - MAX_TOP;
	    				while(removeCount > 0){
	    					WorldBossPlayer tp = getPlayerMin(worldBossTops);
	    					worldBossTops.remove(tp.id);
	    					removeCount --;
	    				}
	    			}
	    			sortTop = sortplayer(worldBossTops);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
			
		}
		
	}
	
	public static void loadBoss(){
		File file = new File(System.getProperty("user.dir") + "/" + PATH + "/bossData.xml");
		if(file.exists()){
	    	try {
	    		SAXReader reader = new SAXReader();
	    		Document doc = reader.read(file);
	    		Element root = doc.getRootElement();
				Element attrRoot = root.element("Boss");
    			worldBossUpLevel = Integer.parseInt(attrRoot.attributeValue("level"));
	    	} catch (Exception e) {
	    		log.error(e, e);
	    	}
		}
	}
	
	public static List<WorldBossPlayer> getTop(){
		return sortTop;
	}  
}
