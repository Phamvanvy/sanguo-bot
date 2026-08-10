package com.pip.itimes.server.world.toplist;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.ArenaTeam;
import com.pip.itimes.server.bean.ArenaTeamTotal;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.dao.ArenaTeamDao;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.IbuyDao;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.stage.Command;
import com.pip.itimes.server.stage.EquipmentHelper;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Instanceadd;
import com.pip.itimes.server.stage.PlayerDataException;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;

public class PlayerTopList extends TopList {
	private static final Logger log = Logger.getLogger(PlayerTopList.class);

	private PlayerService playerService;
	private PlayerDao playerDao;
	private IbuyDao ibuyDao;
	private ArenaTeamDao arenaTeamDao;
	protected MailService mailService;
	boolean onlyClear = false;
	private Date lastTopListTime;
	private List<Player> playerKillsCache = new Vector<Player>();
	public static List<Player> playerSneaksCache = new Vector<Player>();
	private Map<Integer, Player> sneaker = new Hashtable<Integer, Player>();
	public static List<Player> playerIbuyCache = new Vector<Player>();

	private List<ArenaTeam> arenaLevelCache = new Vector<ArenaTeam>();
	private List<ArenaTeam> arenaLevel2Cache = new Vector<ArenaTeam>();
	private List<ArenaTeam> arenaLevel3Cache = new Vector<ArenaTeam>();
	private List<Player> playerarenaLevelCache = new Vector<Player>();
	private List<ArenaTeamTotal> allserverarenaLevelCache = new Vector<ArenaTeamTotal>();
	private List<ArenaTeamTotal> allserverarenaLevel2Cache = new Vector<ArenaTeamTotal>();
	private List<ArenaTeamTotal> allserverarenaLevel3Cache = new Vector<ArenaTeamTotal>();
	private List<ArenaTeamTotal> allserverarenaLevelWorldWarCache = new Vector<ArenaTeamTotal>();
	private long lastArenaTopListTime;
	private Map<Integer, IEquipment[]> playEquipCache = new Hashtable<Integer, IEquipment[]>();// 装备储存
																								// ；
	private static final long PLAYSER_MAKE_TOP_LIST_TIME = (long) 2 * 3600 * 1000;
	private static final long PLAYSER_TOP_LIST_PERIOD = (long) 24 * 3600 * 1000;
	private static final long PLAYSER_TOP_LIST_SPACE = (long) 1 * 3600 * 1000;

	public PlayerTopList() {
		lastTopListTime = new Date(getTodayStart() + getMakeTime());
		playerDao = new PlayerDao();
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void setPlayerServcie(PlayerService playerService) {
		this.playerService = playerService;
	}

	protected long getLastMakeTime() {
		return lastTopListTime.getTime();
	}

	protected long getMakeTime() {
		return PLAYSER_MAKE_TOP_LIST_TIME;

	}

	protected long getPeriod() {
		return PLAYSER_TOP_LIST_PERIOD;
	}

	protected long getSpace() {
		return PLAYSER_TOP_LIST_SPACE;
	}

	public void processTopList() {
		if (testTopListTime()) {
			makeTopList();
		}
		
		if(testTopListEquipTime()){
			//用于装备内存清空
			clearEquipList();
		}
		if (testTopListArenaTime()){
			makeArenaTeamTopList();
		}
		 //CmccJILIN
		if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
			Date data_tmp = new Date();
			if ((Server.cmcc_jilin_lasttime.getDay()<data_tmp.getDay()) ||
					((Server.cmcc_jilin_lasttime.getDay()>data_tmp.getDay()) && (Server.cmcc_jilin_lasttime.getTime()<data_tmp.getTime()))){
				Server.cmcc_jilin_playerid = new HashMap<String,Integer>();
				Server.cmcc_jilin_count = 0;
				Server.cmcc_jilin_lasttime = data_tmp;
			}
		}
	}

	protected final long getWeekStart() {
		/*
		 * Calendar cal = Calendar.getInstance(); final long MILLS_OF_DAY = 3600
		 * 24 1000; SimpleDateFormat sf = new
		 * SimpleDateFormat("yyyy-MM-dd 00:00:00");
		 * cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); Date tmp_date = new
		 * Date(cal.getTime().getTime() - MILLS_OF_DAY 7); Calendar
		 * calendar=Calendar.getInstance(); calendar.setTime(tmp_date); return
		 * calendar.getTime().getTime();*/
		 
		Calendar cal = Calendar.getInstance();
		final long MILLS_OF_DAY = 3600 * 24 * 1000;
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){//星期日
			cal.setTime(new Date(cal.getTime().getTime() - MILLS_OF_DAY));//取昨天，再取得周一的日期
		}
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime().getTime();
	}

	
	protected final boolean testTopListEquipTime(){
		boolean flag = false;
		 long currentTime = System.currentTimeMillis(); //时间上进行清除\
		 if(currentTime >( getWeekStart() + getMakeTime()+ getSpace())&& currentTime < ( getWeekStart() +getMakeTime()+ getSpace()*2)){ 
			 if(!onlyClear){ 
				 onlyClear = true ; 
				 flag = true; 
			 } 
		 } //每周一自动维护状态进行清除
		 if( currentTime > (getWeekStart() + getMakeTime() + getSpace()*2)){ 
			 onlyClear = false ; 
			 flag = false; } 
		 return  flag;
	}
	protected final boolean testTopListArenaTime(){
		long currentTime = System.currentTimeMillis(); //时间
		if( currentTime > lastArenaTopListTime + PLAYSER_TOP_LIST_SPACE){ 
			return  true;
		} 
		 return  false;
	}
	private void clearEquipList(){
		if(playEquipCache == null && 0 == playEquipCache.size()){
			return; 
		}else{	
			playEquipCache.clear(); 
	 } 
	}
	private void makeTopList() {
		playerDao.killsAndSneaksDayEnd();

		WorldPlayer[] players = playerService.getPlayers();

		for (int i = 0; i < players.length; i++) {
			synchronized (players[i]) {
				players[i].killDayEnd();
				players[i].sneakDayEnd();
				playerService.savePlayer(players[i]);
			}
		}

		lastTopListTime = new Date(System.currentTimeMillis());

		List<Player> killsTmp = new Vector<Player>();
		List<Player> sneaksTmp = new Vector<Player>();
		List<Player> ibuyTmp = new Vector<Player>();
		List<ArenaTeam> arenaLeveltmp = new Vector<ArenaTeam>();
		List<Player> playerarenaLevelTmp = new Vector<Player>();
		//Map<Integer, IEquipment[]> tempPlayEquipCache = new Hashtable<Integer, IEquipment[]>();// 装备储存
		
		playerKillsCache = killsTmp;
		playerSneaksCache = sneaksTmp;
		playerIbuyCache = ibuyTmp;
		arenaLevelCache = arenaLeveltmp;
		arenaLevel2Cache = arenaLeveltmp;
		arenaLevel3Cache = arenaLeveltmp;
		playerarenaLevelCache = playerarenaLevelTmp;
		//playEquipCache = tempPlayEquipCache;
		sneaker.clear();

		log.info("Make Yesterday Playser Top List Kills and Sneaks OK at "
				+ lastTopListTime);
	}
	private void makeArenaTeamTopList() {
		lastArenaTopListTime = new Date(System.currentTimeMillis()).getTime();

		List<ArenaTeamTotal> arenaLeveltmp = new Vector<ArenaTeamTotal>();

		allserverarenaLevelCache = arenaLeveltmp;
		allserverarenaLevel2Cache = arenaLeveltmp;
		allserverarenaLevel3Cache = arenaLeveltmp;
		
		allserverarenaLevelWorldWarCache = arenaLeveltmp;
		try {
			Server.instance.arenaSession.getAllserverArenaTeam(ArenaConstants.ARENA_TYPE_ONE,10);
			log.info("Make Allserver ArenaTeam Top List OK at "
					+ new Date(System.currentTimeMillis()));
		} catch (Exception e) {
			log.info("Make Allserver ArenaTeam Top List ERROR at "
					+ new Date(System.currentTimeMillis()));
		}
	}
	public boolean isSneaker(int playerId) {
		return sneaker.get(playerId) != null;
	}

	public List<String> getPlayerTopListKills(WorldPlayer player, int num) {
		List<String> result = new Vector<String>();

		try {
			Player[] players;

			if (playerKillsCache.size() >= num) {
				players = new Player[playerKillsCache.size()];
				playerKillsCache.toArray(players);
			} else {
				players = playerDao.getPlayerLastKillsOrder(num);

				List<Player> tmp = new Vector<Player>();

				for (int i = 0; i < players.length; i++) {
					tmp.add(players[i]);
				}

				playerKillsCache = tmp;
			}

			if (players.length > 0) {
				int playerId = player.getId();
				boolean flag = true;

				for (int i = 0; i < players.length; i++) {
					if (players[i].getLastKills() == 0) {
						break;
					}

					String tmp = "" + (i + 1) + ". "
							+ players[i].getPlayerName() + " "
							+ players[i].getLastKills() + "只";
					result.add(tmp);

					if (players[i].getId() == playerId) {
						flag = false;
					}
				}

				if (flag) {
					if (player.getPlayer().getLastKills() > 0) {
						int index = playerDao.getLastKillsOrder(player
								.getPlayer());
						String tmp = "" + (index + 1) + ". "
								+ player.getPlayerName() + " "
								+ player.getPlayer().getLastKills() + "只";
						result.add(tmp);
					}
				}
			}
		} catch (DataAccessException e) {
			log.error(e, e);
		}

		return result;
	}

	public List<String> getArenaTopListLevel(ArenaTeam arenateam ,WorldPlayer player, int num,int type) {
		List<String> result = new Vector<String>();

		try {
			ArenaTeam[] arenateamlist = null;
			switch(type){
				case ArenaConstants.ARENA_TYPE_ONE: { //1v1排行榜查看
					if (arenaLevelCache.size() >= num) {
						arenateamlist = new ArenaTeam[arenaLevelCache.size()];
						arenaLevelCache.toArray(arenateamlist);
					} else {
						arenaTeamDao = new ArenaTeamDao();
						arenateamlist = arenaTeamDao.getArenaTeamLevelOrder(num,type);
						List<ArenaTeam> tmp = new Vector<ArenaTeam>();
						for (int i = 0; i < arenateamlist.length; i++) {
							tmp.add(arenateamlist[i]);
						}
						arenaLevelCache = tmp;
					}
				}
				break;
    			case ArenaConstants.ARENA_TYPE_TWO: { //2v2排行榜查看
    				if (arenaLevel2Cache.size() >= num) {
    					arenateamlist = new ArenaTeam[arenaLevel2Cache.size()];
    					arenaLevel2Cache.toArray(arenateamlist);
    				} else {
    					arenaTeamDao = new ArenaTeamDao();
    					arenateamlist = arenaTeamDao.getArenaTeamLevelOrder(num,type);
    					List<ArenaTeam> tmp = new Vector<ArenaTeam>();
    					for (int i = 0; i < arenateamlist.length; i++) {
    						tmp.add(arenateamlist[i]);
    					}
    					arenaLevel2Cache = tmp;
    				}
    			}
    			break;
				case ArenaConstants.ARENA_TYPE_THREE: { //3v3排行榜查看
					if (arenaLevel3Cache.size() >= num) {
    					arenateamlist = new ArenaTeam[arenaLevel3Cache.size()];
    					arenaLevel3Cache.toArray(arenateamlist);
    				} else {
    					arenaTeamDao = new ArenaTeamDao();
    					arenateamlist = arenaTeamDao.getArenaTeamLevelOrder(num,type);
    					List<ArenaTeam> tmp = new Vector<ArenaTeam>();
    					for (int i = 0; i < arenateamlist.length; i++) {
    						tmp.add(arenateamlist[i]);
    					}
    					arenaLevel3Cache = tmp;
    				}
				}
			}
			

			if (arenateamlist.length > 0) {
				boolean flag = true;
				if (arenateam == null){
					for (int i = 0; i < arenateamlist.length; i++) {
						if (arenateamlist[i].getArenalevel() == 0) {
							break;
						}

						String tmp = "" + (i + 1) + ". "
								+ arenateamlist[i].getArenaname() + " 战队等级:"
								+ arenateamlist[i].getArenalevel();
						result.add(tmp);
					}
					flag = false;
				}else{
					int arenateamId = arenateam.getId();
					for (int i = 0; i < arenateamlist.length; i++) {
						if (arenateamlist[i].getArenalevel() == 0) {
							break;
						}

						String tmp = "" + (i + 1) + ". "
								+ arenateamlist[i].getArenaname() + " 战队等级:"
								+ arenateamlist[i].getArenalevel();
						result.add(tmp);

						if (arenateamlist[i].getId() == arenateamId) {
							flag = false;
						}
					}
				}
				if (flag) {
					if (arenateam.getArenalevel() > 0) {
						int index = arenaTeamDao.getArenaTeamLevelowner(arenateam.getId(),arenateam.getArenalevel(),type);
						String tmp = "" + (index + 1) + ". "
								+ arenateam.getArenaname() + " 战队等级:"
								+ arenateam.getArenalevel();
						result.add(tmp);
					}
				}
			}
		} catch (DataAccessException e) {
			log.error(e, e);
		}

		return result;
	}
	public List<String> getPlayerarenaLevelTopList(WorldPlayer player, int num,int type) {
		List<String> result = new Vector<String>();

		try {
			Player[] players = null;

			if (playerarenaLevelCache.size() >= num) {
				players = new Player[playerarenaLevelCache.size()];
				playerarenaLevelCache.toArray(players);
			} else {
				switch(type){
					case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队
						players = playerDao.getPlayerarenaLevel1(num);
                	}
					break;
//					case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队
//						return null;
//                	}
//					case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队
//						return null;
//                	}
				}
				List<Player> tmp = new Vector<Player>();

				for (int i = 0; i < players.length; i++) {
					tmp.add(players[i]);
				}
				playerarenaLevelCache = tmp;
			}

			if (players.length > 0) {
				int playerId = player.getId();
				boolean flag = true;

				for (int i = 0; i < players.length; i++) {
					if (players[i].getArenaLevel() == 0) {
						break;
					}

					String tmp = "" + (i + 1) + ". "
							+ players[i].getPlayerName() + " 个人竞技场等级:"
							+ players[i].getArenaLevel();
					result.add(tmp);

					if (players[i].getId() == playerId) {
						flag = false;
					}
				}

				if (flag) {
					if (player.getPlayer().getArenaLevel() > 0) {
						int index = playerDao.getPlayerarenaLevelone1(player.getPlayer().getId(),player.getPlayer().getArenaLevel());
						String tmp = "" + (index + 1) + ". "
								+ player.getPlayer().getPlayerName() + " 个人竞技场等级:"
								+ player.getPlayer().getArenaLevel();
						result.add(tmp);
					}
				}
			}
		} catch (DataAccessException e) {
			log.error(e, e);
		}

		return result;
	}
	
	public List<String> getAllServerArenaLevelTopList(WorldPlayer player, int num,int type) {
		List<String> result = new Vector<String>();

		ArenaTeamTotal[] arenateamlist = null;
		switch(type){
			case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队
				if (allserverarenaLevelCache.size() >= num) {
					arenateamlist = new ArenaTeamTotal[allserverarenaLevelCache.size()];
					allserverarenaLevelCache.toArray(arenateamlist);
				} else {
					Server.instance.arenaSession.getAllserverArenaTeam(ArenaConstants.ARENA_TYPE_ONE,num);
					return null;
				}
			}
			break;
			case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队
				if (allserverarenaLevel2Cache.size() >= num) {
					arenateamlist = new ArenaTeamTotal[allserverarenaLevel2Cache.size()];
					allserverarenaLevel2Cache.toArray(arenateamlist);
				} else {
					Server.instance.arenaSession.getAllserverArenaTeam(ArenaConstants.ARENA_TYPE_TWO,num);
					return null;
				}
			}
			break;
			case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队
				if (allserverarenaLevel3Cache.size() >= num) {
					arenateamlist = new ArenaTeamTotal[allserverarenaLevel3Cache.size()];
					allserverarenaLevel3Cache.toArray(arenateamlist);
				} else {
					Server.instance.arenaSession.getAllserverArenaTeam(ArenaConstants.ARENA_TYPE_THREE,num);
					return null;
				}
			}
			break;
		}
		
		if (arenateamlist.length > 0) {
			for (int i = 0; i < arenateamlist.length; i++) {
				if (arenateamlist[i].getArenalevel() == 0) {
					break;
				}

				String tmp = "" + (i + 1) + ". "
						+ arenateamlist[i].getArenaname() + "("+
						arenateamlist[i].getServername()+") 战队等级:"
						+ arenateamlist[i].getArenalevel();
				result.add(tmp);
			}
		}

		return result;
	}
	public List<String> getAllServerArenaLevelTopListWorldWar(WorldPlayer player, int num,int type) {
		List<String> result = new Vector<String>();

		ArenaTeamTotal[] arenateamlist = null;
		switch(type){
			case ArenaConstants.ARENA_TYPE_ONE: { //1v1战队
				if (allserverarenaLevelWorldWarCache.size() >= num) {
					arenateamlist = new ArenaTeamTotal[allserverarenaLevelWorldWarCache.size()];
					allserverarenaLevelWorldWarCache.toArray(arenateamlist);
				} else {
					Server.instance.arenaSession.getAllserverArenaTeamWorldWar(ArenaConstants.ARENA_TYPE_ONE,num);
					return null;
				}
			}
			break;
			case ArenaConstants.ARENA_TYPE_TWO: { //2v2战队
//				if (allserverarenaLevel2Cache.size() >= num) {
//					arenateamlist = new ArenaTeamTotal[allserverarenaLevel2Cache.size()];
//					allserverarenaLevel2Cache.toArray(arenateamlist);
//				} else {
//					Server.instance.arenaSession.getAllserverArenaTeam(ArenaConstants.ARENA_TYPE_TWO,num);
//					return null;
//				}
			}
			break;
			case ArenaConstants.ARENA_TYPE_THREE: { //3v3战队
//				if (allserverarenaLevel3Cache.size() >= num) {
//					arenateamlist = new ArenaTeamTotal[allserverarenaLevel3Cache.size()];
//					allserverarenaLevel3Cache.toArray(arenateamlist);
//				} else {
//					Server.instance.arenaSession.getAllserverArenaTeam(ArenaConstants.ARENA_TYPE_THREE,num);
//					return null;
//				}
			}
			break;
		}
		
		if (arenateamlist.length > 0) {
			for (int i = 0; i < arenateamlist.length; i++) {
				if (arenateamlist[i].getArenalevel() == 0) {
					break;
				}

				String tmp = "" + (i + 1) + ". "
						+ arenateamlist[i].getArenaname() + "("+
						arenateamlist[i].getServername()+") 战队等级:"
						+ arenateamlist[i].getArenalevel();
				result.add(tmp);
			}
		}

		return result;
	}
	public void setAllServerArenaLevelTopList(ArenaTeamTotal[] arenateamlist,int type) {
		if (arenateamlist.length>0) {
			List<ArenaTeamTotal> arenaLeveltmp = new Vector<ArenaTeamTotal>();
			switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:{ //1v1竞技场报名
            	allserverarenaLevelCache = arenaLeveltmp;
    			for (int i = 0; i < arenateamlist.length; i++) {
    				allserverarenaLevelCache.add(arenateamlist[i]);
    			}
            }
            break;
            case ArenaConstants.ARENA_TYPE_TWO:{ //2v2竞技场报名
            	allserverarenaLevel2Cache = arenaLeveltmp;
    			for (int i = 0; i < arenateamlist.length; i++) {
    				allserverarenaLevel2Cache.add(arenateamlist[i]);
    			}
            }
            break;
            case ArenaConstants.ARENA_TYPE_THREE:{ //3v3竞技场报名
            	
            	allserverarenaLevel3Cache = arenaLeveltmp;
    			for (int i = 0; i < arenateamlist.length; i++) {
    				allserverarenaLevel3Cache.add(arenateamlist[i]);
    			}
            }
            break;
        }
			
		}
	}
	public void setAllServerArenaLevelTopListWorldWar(ArenaTeamTotal[] arenateamlist,int type) {
		if (arenateamlist.length>0) {
			List<ArenaTeamTotal> arenaLeveltmp = new Vector<ArenaTeamTotal>();
			switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:{ //1v1竞技场报名
            	allserverarenaLevelWorldWarCache = arenaLeveltmp;
    			for (int i = 0; i < arenateamlist.length; i++) {
    				allserverarenaLevelWorldWarCache.add(arenateamlist[i]);
    			}
            }
            break;
            case ArenaConstants.ARENA_TYPE_TWO:{ //2v2竞技场报名
//            	allserverarenaLevel2Cache = arenaLeveltmp;
//    			for (int i = 0; i < arenateamlist.length; i++) {
//    				allserverarenaLevel2Cache.add(arenateamlist[i]);
//    			}
            }
            break;
            case ArenaConstants.ARENA_TYPE_THREE:{ //3v3竞技场报名
            	
//            	allserverarenaLevel3Cache = arenaLeveltmp;
//    			for (int i = 0; i < arenateamlist.length; i++) {
//    				allserverarenaLevel3Cache.add(arenateamlist[i]);
//    			}
            }
            break;
			}
			
		}
	}
	public List<String> getPlayerTopListSneaks(WorldPlayer player, int num) {
		List<String> result = new Vector<String>();

		try {
			Player[] players;

			if (playerSneaksCache.size() >= num) {
				players = new Player[playerSneaksCache.size()];
				playerSneaksCache.toArray(players);
			} else {
				players = playerDao.getPlayerLastSneaksOrder(num);

				List<Player> tmp = new Vector<Player>();

				for (int i = 0; i < players.length; i++) {
					tmp.add(players[i]);
					sneaker.put(player.getId(), player.getPlayer());
				}

				playerSneaksCache = tmp;
			}

			if (players.length > 0) {
				int playerId = player.getId();
				boolean flag = true;

				for (int i = 0; i < players.length; i++) {
					if (players[i].getLastSneaks() == 0) {
						break;
					}

					String tmp = "" + (i + 1) + ". "
							+ players[i].getPlayerName() + " "
							+ players[i].getLastSneaks() + "人";
					result.add(tmp);

					if (players[i].getId() == playerId) {
						flag = false;
					}
				}

				if (flag) {
					if (player.getPlayer().getLastSneaks() > 0) {
						int index = playerDao.getLastSneaksOrder(player
								.getPlayer());
						String tmp = "" + (index + 1) + ". "
								+ player.getPlayerName() + " "
								+ player.getPlayer().getLastSneaks() + "人";
						result.add(tmp);
					}
				}
			}
		} catch (DataAccessException e) {
			log.error(e, e);
		}

		return result;
	}

	// mengjie add ibuytop10
	public List<String> getPlayerTopListIbuy(WorldPlayer player, int num,
			String begin, String end) throws Exception {
		List<String> result = new Vector<String>();

		try {
			List<Object[]> tmplist;
			Player[] players = null;
			if (playerIbuyCache.size() >= num) {
				players = new Player[playerIbuyCache.size()];
				playerIbuyCache.toArray(players);
			} else {
				ibuyDao = new IbuyDao();
				tmplist = ibuyDao.getPlayerTop10(num, begin, end);
				List<Player> tmp = new Vector<Player>();
				for (int i = 0; i < tmplist.size(); i++) {
					Object[] tmpplayerid = tmplist.get(i);
					if (tmpplayerid != null) {
						int playerid = ((Integer) tmpplayerid[0]).intValue();
						String sum = tmpplayerid[1].toString();

						WorldPlayer worldplayer = playerService
								.getWorldPlayer(playerid);
						if (worldplayer == null) {
							Player p = playerService.loadPlayerById(playerid);
							if (p != null) {
								worldplayer = new WorldPlayer(p);
							}
						}
						if (worldplayer != null) {
							tmp.add(worldplayer.getPlayer());
//							mailService.sendMail(
//											worldplayer.getId(),
//											"",
//											-1,
//											"系统",
//											"名人通知",
//											"恭喜你已经成为了幻想名人，请到位于瓦伊特森林的名人堂里领取属于你的超级奖励吧！每天都可以领一次哦。",
//											null, 0, false);
						}

					}

				}

				playerIbuyCache = tmp;
				players = new Player[playerIbuyCache.size()];
				playerIbuyCache.toArray(players);
			}

			if (players.length > 0) {
				for (int i = 0; i < players.length; i++) {
					String tmp = "" + (i + 1) + ". "
							+ players[i].getPlayerName() + ".";
					result.add(tmp);
				}
			}
		} catch (DataAccessException e) {
			log.error(e, e);
		}

		return result;
	}

	public boolean getPlayerIbuyTop(WorldPlayer player, int num, String begin,
			String end) throws Exception {
		boolean flag = false;
		try {
			List<Object[]> tmplist;
			Player[] players = null;
			if (playerIbuyCache.size() >= num) {
				players = new Player[playerIbuyCache.size()];
				playerIbuyCache.toArray(players);
			} else {
				ibuyDao = new IbuyDao();
				tmplist = ibuyDao.getPlayerTop10(num, begin, end);
				List<Player> tmp = new Vector<Player>();
				for (int i = 0; i < tmplist.size(); i++) {
					Object[] tmpplayerid = tmplist.get(i);
					if (tmpplayerid != null) {
						int playerid = ((Integer) tmpplayerid[0]).intValue();
						String sum = tmpplayerid[1].toString();

						WorldPlayer worldplayer = playerService
								.getWorldPlayer(playerid);
						if (worldplayer == null) {
							Player p = playerService.loadPlayerById(playerid);
							if (p != null) {
								worldplayer = new WorldPlayer(p);
							}
						}
						if (worldplayer != null) {
							tmp.add(worldplayer.getPlayer());
//							mailService.sendMail(
//											worldplayer.getId(),
//											"",
//											-1,
//											"系统",
//											"名人通知",
//											"恭喜你已经成为了幻想名人，请到位于瓦伊特森林的名人堂里领取属于你的超级奖励吧！每天都可以领一次哦。",
//											null, 0, false);
						}
					}
				}
				playerIbuyCache = tmp;
				players = new Player[playerIbuyCache.size()];
				playerIbuyCache.toArray(players);
			}
			if (players.length > 0) {
				for (int i = 0; i < players.length; i++) {
					if (players[i].getId() == player.getId()) {
						flag = true;
					}
					Instanceadd.setNpcnameby(players[i].getPlayerName(), i);
					Instanceadd.setNpcsexby(players[i].getSex(), i);
				}
			}
		} catch (DataAccessException e) {
			log.error(e, e);
		}

		return flag;
	}

	public String[] getPlayerIbuyTopplayer(int num, String begin, String end,
			int top) throws Exception {
		String[] result = new String[2];
		try {
			List<Object[]> tmplist;
			Player[] players = null;
			if (playerIbuyCache.size() >= num) {
				players = new Player[playerIbuyCache.size()];
				playerIbuyCache.toArray(players);
			} else {
				ibuyDao = new IbuyDao();
				tmplist = ibuyDao.getPlayerTop10(num, begin, end);
				List<Player> tmp = new Vector<Player>();
				for (int i = 0; i < tmplist.size(); i++) {
					Object[] tmpplayerid = tmplist.get(i);
					if (tmpplayerid != null) {
						int playerid = ((Integer) tmpplayerid[0]).intValue();
						String sum = tmpplayerid[1].toString();

						WorldPlayer worldplayer = playerService
								.getWorldPlayer(playerid);
						if (worldplayer == null) {
							Player p = playerService.loadPlayerById(playerid);
							if (p != null) {
								worldplayer = new WorldPlayer(p);
							}
						}
						if (worldplayer != null) {
							tmp.add(worldplayer.getPlayer());
//							mailService.sendMail(
//											worldplayer.getId(),
//											"",
//											-1,
//											"系统",
//											"名人通知",
//											"恭喜你已经成为了幻想名人，请到位于瓦伊特森林的名人堂里领取属于你的超级奖励吧！每天都可以领一次哦。",
//											null, 0, false);
						}

					}

				}

				playerIbuyCache = tmp;
				players = new Player[playerIbuyCache.size()];
				playerIbuyCache.toArray(players);
			}
			if (players.length > 0 && players.length > top) {
				result[0] = String.valueOf(players[top].getId());
				result[1] = players[top].getPlayerName();
			} else {
				return null;
			}
		} catch (DataAccessException e) {
			log.error(e, e);
		}

		return result;
	}
	//mengjie add arena top
//	public String[] getPlayerIbuyTopplayer(int num, String begin, String end,
//			int top) throws Exception {
//		String[] result = new String[2];
//		try {
//			List<Object[]> tmplist;
//			Player[] players = null;
//			if (playerIbuyCache.size() >= num) {
//				players = new Player[playerIbuyCache.size()];
//				playerIbuyCache.toArray(players);
//			} else {
//				ibuyDao = new IbuyDao();
//				tmplist = ibuyDao.getPlayerTop10(num, begin, end);
//				List<Player> tmp = new Vector<Player>();
//				for (int i = 0; i < tmplist.size(); i++) {
//					Object[] tmpplayerid = tmplist.get(i);
//					if (tmpplayerid != null) {
//						int playerid = ((Integer) tmpplayerid[0]).intValue();
//						String sum = tmpplayerid[1].toString();
//
//						WorldPlayer worldplayer = playerService
//								.getWorldPlayer(playerid);
//						if (worldplayer == null) {
//							Player p = playerService.loadPlayerById(playerid);
//							if (p != null) {
//								worldplayer = new WorldPlayer(p);
//							}
//						}
//						if (worldplayer != null) {
//							tmp.add(worldplayer.getPlayer());
//							mailService
//									.sendMail(
//											worldplayer.getId(),
//											"",
//											-1,
//											"系统",
//											"名人通知",
//											"恭喜你已经成为了幻想名人，请到位于瓦伊特森林的名人堂里领取属于你的超级奖励吧！每天都可以领一次哦。",
//											null, 0, false);
//						}
//
//					}
//
//				}
//
//				playerIbuyCache = tmp;
//				players = new Player[playerIbuyCache.size()];
//				playerIbuyCache.toArray(players);
//			}
//			if (players.length > 0) {
//				result[0] = String.valueOf(players[top].getId());
//				result[1] = players[top].getPlayerName();
//			}
//		} catch (DataAccessException e) {
//			log.error(e, e);
//		}
//
//		return result;
//	}
	public IEquipment[] getPlayerIbuyTopPlayerEquip(int id) throws Exception {
		IEquipment[] euqips = null;
		if (playerIbuyCache == null || playerIbuyCache.size() == 0) {// 检查名人堂
			// 执行添加名人操作并返回
			// 名人验证并返回
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			long MILLS_OF_DAY = 3600 * 24 * 1000;
			List<String> list = null;
			Calendar cal = Calendar.getInstance();
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){//星期日
				cal.setTime(new Date(cal.getTime().getTime() - MILLS_OF_DAY));//取昨天，再取得周一的日期
			}
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			String end = sf.format(cal.getTime());
			// cal.roll(Calendar.WEEK_OF_MONTH, -1);
			// cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			Date tmp_date = new Date(cal.getTime().getTime() - MILLS_OF_DAY * 7);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(tmp_date);
			String begin = sf.format(calendar.getTime());
			try {
				ibuyDao = new IbuyDao();
				List<Object[]> tmplist = ibuyDao.getPlayerTop10(10, begin, end);
				Player[] players = null;
				List<Player> tmp = new Vector<Player>();
				for (int i = 0; i < tmplist.size(); i++) {
					Object[] tmpplayerid = tmplist.get(i);
					if (tmpplayerid != null) {
						int playerid = ((Integer) tmpplayerid[0]).intValue();
						String sum = tmpplayerid[1].toString();

						WorldPlayer worldplayer = playerService
								.getWorldPlayer(playerid);
						if (worldplayer == null) {
							Player p = playerService.loadPlayerById(playerid);
							if (p != null) {
								worldplayer = new WorldPlayer(p);
							}
						}
						if (worldplayer != null) {
							tmp.add(worldplayer.getPlayer());
						}
					}
				}
				playerIbuyCache = tmp;
			} catch (Exception ex) {
				log.error("读取名人堂" + Server.iMoneyStoreString + "记录错误。", ex);
			}
		}
		if (playEquipCache == null || playEquipCache.size() == 0) {// 检查并添加装备数据
			Player player;
			byte[] bytes;
			
			for (int k = 0; k < playerIbuyCache.size(); k++) {
				IEquipment[] usedEquipments = new IEquipment[9];
				player = playerIbuyCache.get(k);
				bytes = player.getUsedEquipments();
				if (bytes != null && bytes.length > 2) {
					ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
					DataInputStream dis = new DataInputStream(bis);
					byte version = dis.readByte();
					short size = dis.readShort();
					for (int i = 0; i < size; i++) {
						IEquipment equ = EquipmentHelper.createFromDbBytes(
								version, dis);
						if (equ == null)
							throw new PlayerDataException("数据错误");
						usedEquipments[equ.getPart()] = equ;
					}
					// 放入临时缓存储存
 					playEquipCache.put(player.getId(), usedEquipments);
				}
			}
		}
		// 下发装备数据
		if (id > playerIbuyCache.size() && id > playEquipCache.size()) {
			return euqips;
		}
		Player player = playerIbuyCache.get(id);
		int playerId = player.getId();
		if (playEquipCache.containsKey(playerId)) {
			euqips = playEquipCache.get(playerId);
		}
		return euqips;
	}

	public boolean setPlayerIbuyTopPlayerEquip(int playerId,
			IEquipment[] usedEquipments) throws Exception {
		boolean flag = false;
		if (playerIbuyCache == null || playerIbuyCache.size() == 0) {// 检查名人堂，
																		// 如果刚好维护
																		// ，
																		// 则自动的生成名人堂
			// 名人验证并返回
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			long MILLS_OF_DAY = 3600 * 24 * 1000;
			List<String> list = null;
			Calendar cal = Calendar.getInstance();
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){//星期日
				cal.setTime(new Date(cal.getTime().getTime() - MILLS_OF_DAY));//取昨天，再取得周一的日期
			}
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			String end = sf.format(cal.getTime());
			// cal.roll(Calendar.WEEK_OF_MONTH, -1);
			// cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			Date tmp_date = new Date(cal.getTime().getTime() - MILLS_OF_DAY * 7);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(tmp_date);
			String begin = sf.format(calendar.getTime());
			try {
				ibuyDao = new IbuyDao();
				List<Object[]> tmplist = ibuyDao.getPlayerTop10(10, begin, end);
				Player[] players = null;
				List<Player> tmp = new Vector<Player>();
				for (int i = 0; i < tmplist.size(); i++) {
					Object[] tmpplayerid = tmplist.get(i);
					if (tmpplayerid != null) {
						int playerid = ((Integer) tmpplayerid[0]).intValue();
						String sum = tmpplayerid[1].toString();

						WorldPlayer worldplayer = playerService
								.getWorldPlayer(playerid);
						if (worldplayer == null) {
							Player p = playerService.loadPlayerById(playerid);
							if (p != null) {
								worldplayer = new WorldPlayer(p);
							}
						}
						if (worldplayer != null) {
							tmp.add(worldplayer.getPlayer());
						}
					}
				}
				playerIbuyCache = tmp;
			} catch (Exception ex) {
				log.error("读取名人堂" + Server.iMoneyStoreString + "记录错误。", ex);
			}
		}
		
		if (playEquipCache == null || playEquipCache.size() == 0) {// 自己把自己加到缓存中
			Player player;
			byte[] bytes;	
			for (int k = 0; k < playerIbuyCache.size(); k++) {
				IEquipment[] tempUsedEquipments = new IEquipment[9];
				player = playerIbuyCache.get(k);
				bytes = player.getUsedEquipments();
				if (bytes != null && bytes.length > 2) {
					ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
					DataInputStream dis = new DataInputStream(bis);
					byte version = dis.readByte();
					short size = dis.readShort();
					for (int i = 0; i < size; i++) {
						IEquipment equ = EquipmentHelper.createFromDbBytes(
								version, dis);
						if (equ == null)
							throw new PlayerDataException("数据错误");
						tempUsedEquipments[equ.getPart()] = equ;
					}
					// 放入临时缓存储存
					playEquipCache.put(player.getId(), tempUsedEquipments);
				}
			}
		}
		if (!playEquipCache.containsKey(playerId)) {
			flag = false;
		} else {
			playEquipCache.remove(playerId);
			playEquipCache.put(playerId, usedEquipments);
			flag = true;
		}

		return flag;
	}
	public String getPlayerName(int id){
		Player player = playerIbuyCache.get(id);
		String string = player.getPlayerName();
		return string;
	}
	public boolean isFamuous(int playerId){
		boolean flag = false;
		if (playerIbuyCache == null || playerIbuyCache.size() == 0) {// 检查名人堂，
			// 如果刚好维护
			// ，
			// 则自动的生成名人堂
			// 名人验证并返回
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			long MILLS_OF_DAY = 3600 * 24 * 1000;
			List<String> list = null;
			Calendar cal = Calendar.getInstance();
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){//星期日
				cal.setTime(new Date(cal.getTime().getTime() - MILLS_OF_DAY));//取昨天，再取得周一的日期
			}
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			String end = sf.format(cal.getTime());
			// cal.roll(Calendar.WEEK_OF_MONTH, -1);
			// cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			Date tmp_date = new Date(cal.getTime().getTime() - MILLS_OF_DAY * 7);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(tmp_date);
			String begin = sf.format(calendar.getTime());
			try {
				ibuyDao = new IbuyDao();
				List<Object[]> tmplist = ibuyDao.getPlayerTop10(10, begin, end);
				Player[] players = null;
				List<Player> tmp = new Vector<Player>();
				for (int i = 0; i < tmplist.size(); i++) {
					Object[] tmpplayerid = tmplist.get(i);
					if (tmpplayerid != null) {
						int playerid = ((Integer) tmpplayerid[0]).intValue();
						String sum = tmpplayerid[1].toString();

						WorldPlayer worldplayer = playerService
								.getWorldPlayer(playerid);
						if (worldplayer == null) {
							Player p = playerService.loadPlayerById(playerid);
							if (p != null) {
								worldplayer = new WorldPlayer(p);
							}
						}
						if (worldplayer != null) {
							tmp.add(worldplayer.getPlayer());
						}
					}
				}
				playerIbuyCache = tmp;
			} catch (Exception ex) {
				log.error("读取名人堂" + Server.iMoneyStoreString + "记录错误。", ex);
			}
		}
		Player player;
		for(int k = 0; k < playerIbuyCache.size(); k++){
			player = playerIbuyCache.get(k);
			if(playerId == player.getId()){
				flag = true;
				break;
			}
		}
		return flag;
	}
}
