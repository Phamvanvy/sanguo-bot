package peony.game.exp;

import java.text.MessageFormat;
import java.util.List;
import org.apache.log4j.Logger;
import peony.game.ChatOption;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.HorseUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.PlayerUtil;
import peony.game.Server;
import peony.game.Time;
import peony.game.UseItemException;
import peony.game.VMap;
import peony.game.buff.Buff;
import peony.game.changed.ChangedItem;
import peony.game.chat.ChatMessage;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;

public class ExpService implements Service, ServiceEventListener {
	
	private static final Logger log = Logger.getLogger(ExpService.class);
	public static final int TYPE_PLAYER = 1;
	public static final int TYPE_HORSE = 0;
	public static final String PLAYER_LEAVINGEXP = "LEAVINGEXP";
	public float offLineRatio = 1.0f;
	public float vipOffLineRatio = 1.0f;
	protected int[] mapId = {-1, 272, 240, 352};
	public static int[] onlineExps = {
		0,
		303,
		307,
		312,
		316,
		324,
		333,
		345,
		358,
		373,
		394,
		418,
		444,
		478,
		516,
		556,
		601,
		657,
		717,
		780,
		856,
		939,
		1026,
		1117,
		1225,
		1339,
		1459,
		1596,
		1740,
		1891,
		2049,
		2226,
		2412,
		2605,
		2821,
		3045,
		3277,
		3519,
		3784,
		4060,
		4345,
		4657,
		4980,
		5313,
		5655,
		6028,
		6411,
		6805,
		7233,
		7671,
		8121,
		8584,
		9081,
		9589,
		10113,
		10671,
		11244,
		11830,
		12430,
		13069,
		13723,
		14391,
		15100,
		15825,
		16564,
		17320,
		18120,
		18936,
		19767,
		20644,
		37470,
		39066,
		40692,
		42403,
		44145,
		45919,
		47781,
		49677,
		51604,
		53566,
		55620,
		57709,
		59832,
		62052,
		64308,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
		66600,
};

	public static int[] horseOnlineExps = {
		0,
		120,
		120,
		122,
		122,
		122,
		122,
		126,
		128,
		128,
		132,
		134,
		138,
		144,
		146,
		152,
		158,
		168,
		174,
		182,
		194,
		204,
		218,
		230,
		246,
		260,
		278,
		296,
		314,
		338,
		360,
		384,
		410,
		438,
		464,
		498,
		530,
		566,
		602,
		642,
		684,
		726,
		770,
		818,
		866,
		918,
		974,
		1032,
		1088,
		1152,
		1218,
		1284,
		1352,
		1424,
		1500,
		1578,
		1658,
		1746,
		1830,
		1920,
		2012,
		2108,
		2208,
		2312,
		2420,
		2528,
		2640,
		2756,
		2876,
		3000,
		6260,
		6524,
		6792,
		7064,
		7352,
		7646,
		7944,
		8258,
		8576,
		8900,
		9174,
		9475,
		9777,
		10078,
		10380,
		10681,
		10983,
		11285,
		11586,
		11888,
		12189,
		12491,
		12792,
		13094,
		13396,
		13697,
		13999,
		14300,
		14602,
		14903,
		15205,
		15205,
		15205,
		15205,
		15205,
		15205,
		15205,
		15205,
		15205,
		15205,
		15205,
	};
	
	public static int[] doubaohorseexp ={
			0,
			2,
			2,
			2,
			3,
			4,
			4,
			4,
			4,
			5,
			5,
			5,
			5,
			6,
			7,
			8,
			9,
			17,
			20,
			24,
			28,
			28,
			33,
			38,
			44,
			50,
			58,
			66,
			74,
			84,
			66,
			74,
			83,
			93,
			103,
			114,
			126,
			139,
			153,
			168,
			200,
			230,
			246,
			263,
			281,
			323,
			344,
			366,
			389,
			413,
			500,
			529,
			560,
			592,
			625,
			741,
			781,
			822,
			865,
			909,
			1166,
			1446,
			1750,
			2078,
			2431,
			3747,
			4199,
			5119,
			6109,
			7172,
			12983,
			19741,
			32041,
			50730,
			81108,
			129230,
			206233,
			329009,
			525085,
			838106,
			909868,
			981630,
			1091261,
			1200893,
			1310524,
			1420155,
			1529786,
			1639417,
			1749049,
			1858680,
			1968311,
			2079472,
			2187573,
			2297204,
			2406836,
			2516467,
			2626098,
			2735729,
			2845360,
			2954992,
			3064263,
			3064263,
			3064263,
			3064263,
			3064263,
			3064263,
			3064263,
			3064263,
			3064263,
			3064263,
			3064263
	};
	public int[] xunshoulinghorseexp;
	public static long onlineDis = 20*60*1000L;
	public static long notonlineDis = 15*60*1000L;
	public static int checkDay = 60;
	public static int checkLevel = 20;
	public static int maxExp = 99990000;
	
	/**
	 * 玩家上线处理
	 */
	private void checkOnlineExp(Player p){
		if(p!=null){
			if(p.level>=Player.MAX_LEVEL)
				return;
			handleNotOnlineExp(p);
			List<Horse> list = getAgentHoeses(p);
			if(list!=null && list.size()==1){
				Horse horse = null;
				for(Horse h : list){
					horse = h;
				}
				horse.notOnlineExpTime = (int) (Time.currTime + notonlineDis);
			}
//			if(p.map.getId()==mapId[p.faction]){
				p.onlineExpTime = (int) (Time.currTime + onlineDis);
//			}else{
//				p.onlineExpTime = 0;
//			}
		}
	}

	/**
	 * 玩家进入场景处理
	 */
	private void checkenterMapExp(VMap map, Player p) {
		if(p!=null){
			if(p.level>=Player.MAX_LEVEL)
				return;
//			if(map!=null && map.getId()==mapId[p.faction]){
//				p.onlineExpTime = (int) (Time.currTime + onlineDis);
//			}else{
//				p.onlineExpTime = 0;
//			}
		}
	}
	
	
	/**
	 * 代理饲养马匹
	 * @param p 
	 * @param horseId 代理饲养马的ID 
	 */
	public void addAgentHorse(Player p, int horseId) throws ExpException{
		if(p!=null){
			Horse horse = p.horseBag.getHorse(horseId);
			if(horse == null){
				throw new ExpException(peony.Messages.STRING_01198);
			}
			if(horse.agentHorse==1){
				throw new ExpException(peony.Messages.STRING_01199);
			}
			if(p.horseBag.getAgentHorses().size()>=1){
				throw new ExpException(peony.Messages.STRING_01200);
			}
			if(horse.level>p.level){
				throw new ExpException(peony.Messages.STRING_01201);
			}
			horse.agentHorse = 1;
			horse.agentTime = System.currentTimeMillis();
			horse.notOnlineExpTime = (int) (Time.currTime + notonlineDis);
			horse.addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_AGENT, (1<<7)|horse.lockSkillId, false);
			LogUtil.logAgentHorse(p, horse);
		}
	}
	
	/**
	 * 获取代理饲养的马ID
	 * @param p
	 * @return
	 */
	public List<Horse> getAgentHoeses(Player p) {
		if(p!=null){
			List<Horse> list = p.horseBag.getAgentHorses();
			return list;
		}
		return null;
	}
	
	/**
	 * 判断是否是代理饲养的马
	 * @param p
	 * @param horseInstanceId
	 * @return
	 */
	public boolean isAgentHorse(Player p, int horseInstanceId){
		if(p!=null){
			List<Horse> horses = getAgentHoeses(p);
			for(Horse h : horses){
				if(horseInstanceId==h.instanceId)
					return true;
			}
		}
		return false;
	}

	/**
	 * 解除代理饲养马
	 * @param p
	 * @param horesInstanceId
	 */
	public void removeAgentHorse(Player p, int horseInstanceId) throws ExpException{
		if(p!=null){
			List<Horse> list = p.horseBag.getAgentHorses();
			for(Horse horse : list){
				if(horse.instanceId==horseInstanceId){
					horse.agentHorse = 0;
					horse.agentTime = 0;
					horse.notOnlineExpTime = 0;
					horse.leavingExp = 0;
					horse.addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_AGENT, horse.lockSkillId, false);
					LogUtil.logRemoveAgentHorse(p, horse);
				}
			}
		}
	}
	
	/**
	 * 在线获取经验值
	 */
	public void handleOnlineExp(Player p){
		if(p!=null){
			//  一盒酥主城不再自动消耗
//			PlayerTransaction tx = p.newTransaction("EXCEXP");
//				GameItem item = p.bag.removeGameItem(1183, -1, 1, tx, false);
//				if(item!=null && p.map.getId()==mapId[p.faction]){
//					int getExp = getExp(p.level, onlineDis, ExpService.TYPE_PLAYER);
//					p.addExp(getExp, tx, true);
//					Server.server.getServiceRegistry().getChatService().addChatMessage(
//							new ChatMessage(ChatOption.PRIVATE, -1, -1, "系统",
//									p.id, MessageFormat.format("阁下获{0}经验，消耗1盒{1}", getExp,ObjectAccessor.getItemTemplate(1183).name), null));
//					tx.commit();
//					LogUtil.logExchangeOnlineExp(p, getExp);
//				}else{
//					tx.rollback();
					long leavingExp = p.pool.getLong(ExpService.PLAYER_LEAVINGEXP, 0L);
					leavingExp += getExp(p.level, onlineDis, ExpService.TYPE_PLAYER);
					leavingExp = leavingExp>maxExp ? maxExp : leavingExp;
					p.pool.setLong(ExpService.PLAYER_LEAVINGEXP, leavingExp);
//					if(p.map.getId()==mapId[p.faction]){
//						Server.server.getServiceRegistry().getChatService().addChatMessage(
//							new ChatMessage(ChatOption.PRIVATE, -1, -1, "系统",
//									p.id, MessageFormat.format("您的{0}不足，请进入充值商店购买。", ObjectAccessor.getItemTemplate(1183).name), null));
//					}
//				}
		}
	}

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOGINED, // 角色登录事件
				ServiceEvent.EVENT_MAP_PLAYER_ADDED, // 进入场景事件
				ServiceEvent.EVENT_PLAYER_FIRSTLOAD, //角色在登录以后第一次发送load信息
				};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_LOGINED:
				checkOnlineExp((Player)event.param1);
				break;
			case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
				checkenterMapExp((VMap) event.param1, (Player) event.param2);
				break;
			case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
				sendNotify((Player)event.param1);
				break;
		}
	}
	
	protected void sendNotify(Player player){
		if(player==null)
			return;
//		if(player.level>=30 && player.pool.getLong(ExpService.PLAYER_LEAVINGEXP, 0)>0){
//			Server.server.getServiceRegistry().getChatService()
//			.sendPrivateMessage(player.id,MessageFormat.format("阁下有{0}离线经验可兑换，请在人物信息的经验兑换中兑换", getNotOnineExps(player)));
//		}
		handleHorseExp(player);
	}

	/**
	 * 获取增加的经验值
	 */
	public int getExp(int level, long distance, int type) {
		int onlinedis = (int) (distance/(onlineDis)); // 20分钟
		int notOnlineDis = (int) (distance/(notonlineDis)); // 30分钟
		if(type==ExpService.TYPE_PLAYER){
			return (int) (Server.server.onlineExpRatio * onlineExps[level] * onlinedis);
		}else if(type==ExpService.TYPE_HORSE){
			return (int) (Server.server.agentHorseExp * horseOnlineExps[level] * notOnlineDis);
		}
		return 0;
	}
	
	/**
	 * 玩家上线后处理已经积累的离线经验 
	 */
	private void handleNotOnlineExp(Player p) {
		if(p!=null){
			long exp = p.pool.getLong(ExpService.PLAYER_LEAVINGEXP, 0L);
			if(exp <0 || exp > maxExp){
				p.pool.setLong(ExpService.PLAYER_LEAVINGEXP, maxExp);
			}
			int count = (int)(p.getLastLogoutElapseTime() / (30 * 60 * 1000L));
			if(count > 0){
				long leavingExp = p.pool.getLong(ExpService.PLAYER_LEAVINGEXP, 0L);
				AccountProperty ap = Server.server.getServiceRegistry().getVipPrivilegeService().getAccountProperty(p.accountId);
				float vipOffLineRatio = VipPrivilegeService.VIP_OFFLINEEXP[ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, 0)];
				leavingExp += offLineRatio * count * onlineExps[p.level] * vipOffLineRatio;
				leavingExp = leavingExp>maxExp ? maxExp : leavingExp;
				// 对于20及以上超过20天没有上线的玩家上线后直接增加累计离线经验的30%
//				if(p.level>=checkLevel && p.getLastLogoutElapseTime()>=getTimeDis(checkDay)){
//					int giftExp = (int) (leavingExp*0.3);
//					p.setExp(p.exp+giftExp, false);
//					log.info("[GIFTEXP]"+LogUtil.getPlayerLogString(p)+"GIFTEXP["+giftExp+"]");
//					leavingExp = leavingExp - giftExp;
//				}
				p.pool.setLong(ExpService.PLAYER_LEAVINGEXP, leavingExp);
			}
		}
	}
	
	/**
	 * 得到玩家积累的离线经验
	 * @param p
	 * @return
	 */
	public int getNotOnineExps(Player p){
		if(p!=null){
			return (int) p.pool.getLong(ExpService.PLAYER_LEAVINGEXP, 0);
		}
		return 0;
	}
	
	/**
	 * 计算兑换的经验
	 * @param p 	Player
	 * @param count 一合酥数量
	 * @return
	 */
	public int[] calculateOfflineExp(Player p,int count){
		int exp = 0;
		int restOfflineExp = getNotOnineExps(p);
		int getExp = getExp(p.level, 30*60*1000L, ExpService.TYPE_PLAYER);
		int level = p.level;
		int expNow = p.exp;
		int count1 = 0;
		while(restOfflineExp >= getExp){
			getExp = getExp(level, 30*60*1000L, ExpService.TYPE_PLAYER);
			if(restOfflineExp - getExp < 0){
				break;
			}
			
			count1++;
			if(count1 <= count){
				exp += getExp;
			} else {
				break;
			}
			
			restOfflineExp -= getExp;
			
			expNow += getExp;
			
			int upLevel = PlayerUtil.getUpLevel(level, expNow);
			if(upLevel > 0){
//				expNow -= PlayerUtil.LEVELUP_EXP[level];
				for(int lv = level;lv<level+upLevel;lv++){
				    expNow -= PlayerUtil.LEVELUP_EXP[lv];
				}
				level += upLevel;	
			}
			
			
		}
		
		return new int[]{exp,count1};
	}
	
	/**
	 * 兑换离线经验
	 * @param p
	 * @return 剩余离线经验
	 */
	public void exchaneExp(Player p, Packet packet, ClientSession session, int serial,int limitcount){
		if(p!=null){
			int expLock = p.pool.getInt(Player.PROPERTY_LOCK_EXP, Player.EXP_UNLOCK);  //如果玩家锁住经验，物品不能使用
			if(expLock == Player.EXP_LOCK){
				p.message(-1, "你已锁定经验，无法再获得经验，如想获得经验请去主城官职管理员处解锁。", -1, -1);
				return;
			}
			int leavingExp = getNotOnineExps(p);
			if(leavingExp==0){
				p.message(-1, peony.Messages.STRING_01202, -1, -1);
				return;
			}
			int count = p.bag.getGameItemCount(1183);
			if(count > limitcount){
				count = limitcount;
			}
			if(count==0){
				p.message(-1, MessageFormat.format(peony.Messages.STRING_01203, ObjectAccessor.getItemTemplate(1183).name),
						-1, -1);
				return;
			}else{
				int exp = 0;
//				int count1 = 0;
//				while(leavingExp>0){
//					int getExp = getExp(p.level, 30*60*1000L, ExpService.TYPE_PLAYER);
//					if(leavingExp<getExp){
//						if(exp==0){
//							p.message(-1, peony.Messages.STRING_01204, -1, -1);
//						}
//						break;
//					}
//					if(p.level>=Player.MAX_LEVEL)
//						break;
//					PlayerTransaction tx = p.newTransaction("EXCEXP");
//					GameItem item = p.bag.removeGameItem(1183, -1, 1, tx, false);
//					if(item!=null){
//						tx.commit();
//						leavingExp -= getExp;
//						exp += getExp;
//						count1 ++;
//					}else{
//						tx.rollback();
//						break;
//					}
//					if(count1 == count){
//						break;
//					}
//				}
				int[] result = calculateOfflineExp(p, count);
				exp = result[0];
				int count1 = result[1];
				result = null;
				if(exp > 0){
					PlayerTransaction tx = p.newTransaction("EXCEXP");
					GameItem item = p.bag.removeGameItem(1183, -1, count, tx, false);
					if(item!=null){
						tx.commit();
					}else{
						tx.rollback();
					}
					
					PlayerTransaction tx2 = p.newTransaction("EXCEXP");
					p.addExp(exp, tx2, true);
					tx2.commit();
					leavingExp -= exp;
					p.pool.setLong(ExpService.PLAYER_LEAVINGEXP,leavingExp );
					int getExp = getExp(p.level, 30*60*1000L, ExpService.TYPE_PLAYER);
					String msg;
					if(leavingExp < getExp){
						msg = MessageFormat.format(peony.Messages.STRING_01205,
								String.valueOf(exp),String.valueOf(count),ObjectAccessor.getItemTemplate(1183).name,String.valueOf(leavingExp));
					} else {
						msg = MessageFormat.format(peony.Messages.STRING_01206,
								String.valueOf(exp),String.valueOf(count),ObjectAccessor.getItemTemplate(1183).name,String.valueOf(leavingExp));
					}
					Server.server.getServiceRegistry().getChatService()
					.sendPrivateMessage(p.id, msg);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_USEITEM,p,1183,count));
					LogUtil.logExchangeOfflineExp(p, exp, leavingExp, count);
				}
				
			}
		}
	}
	
	/** 全部使用豆包或驯兽铃获取坐骑经验请求 */
	public void askToGetHorseExp(Packet packet,ClientSession session){
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		int itemId = packet.getInt();
		if(p != null){
			if(itemId != 797 && itemId != 984){
				p.message(-1, peony.Messages.STRING_01207, -1, -1);
				return;
			}
			if(p.horse == null){
				p.message(-1, peony.Messages.STRING_01208, -1, -1);
				return;
			}
			if(p.horse.level>=p.level){
				p.message(-1, peony.Messages.STRING_00528, -1, -1);
				return;
			}
			int itemCount = p.bag.getGameItemCount(itemId);
			if(itemCount <= 0){
				p.message(-1, peony.Messages.STRING_01209, -1, -1);
				return;
			}
			int[] horseexpadd = null ;
			if(itemId == 797){
				horseexpadd = doubaohorseexp;
			} else if(itemId == 984){
				horseexpadd = xunshoulinghorseexp;
			}
			int expAdd = 0;
			int cnt = 0;
			int tempExp = p.horse.exp;
			int horseUpLevel = p.horse.level;
			int oriExp = 0;
			while(itemCount > 0){
				int oldLevel = horseUpLevel;
				oriExp = tempExp;
				tempExp += horseexpadd[oldLevel-1];
				int upLevel = HorseUtil.getUpLevel(oldLevel, tempExp);
				if(upLevel>0){
					if(horseUpLevel+upLevel <= p.level){
						expAdd += horseexpadd[oldLevel-1] ;
						cnt++;
						for(int i=0;i<upLevel;i++){
							oldLevel = horseUpLevel;
							horseUpLevel++;
							tempExp -= HorseUtil.getUpLevelExp(oldLevel, horseUpLevel);
						}
					} else {
						for(int i=1;i<=upLevel;i++){
							oldLevel = horseUpLevel;
							if(horseUpLevel>p.level-1){
								oldLevel --;
								break;
							}
							horseUpLevel++;
							expAdd += HorseUtil.getUpLevelExp(oldLevel, horseUpLevel);
							tempExp -= HorseUtil.getUpLevelExp(oldLevel, horseUpLevel);
						}
						if(horseUpLevel>oldLevel){
							cnt++;
							expAdd -= oriExp;
						}
					}
				} else {
					expAdd += horseexpadd[oldLevel-1];
					cnt++;
				}
				if(horseUpLevel>=p.level){
					break;
				}
				 itemCount--;
			}
			if(cnt <= 0){
				p.message(-1, peony.Messages.STRING_01210, -1, -1);
				return;
			}
			String msg = MessageFormat.format(peony.Messages.STRING_01211,
					cnt,ObjectAccessor.getItemTemplate(itemId).name,p.horse.name,expAdd);
			Packet pt = new Packet(OpCode.ASK_TOGET_HORSEEXP_SERVER);
			pt.putInt(serial);
			pt.putInt(itemId);
			pt.putString(msg);
			p.send(pt);
		}
	}
	
	/** 全部使用豆包或驯兽铃获取坐骑经验 */
	public void useToGetHorseExp(Player p,int itemId){
		if(p!=null){
			int itemCount = p.bag.getGameItemCount(itemId);
			if(itemCount == 0){
				p.message(-1, peony.Messages.STRING_01209, -1, -1);
				return;
			}
			int[] horseexpadd = null ;
			if(itemId == 797){
				horseexpadd = doubaohorseexp;
			} else if(itemId == 984){
				horseexpadd = xunshoulinghorseexp;
			}
			int expAdd = 0;
			int cnt = 0;
			int tempExp = p.horse.exp;
			int horseUpLevel = p.horse.level;
			int oriExp = 0;
			while(itemCount > 0){
				int oldLevel = horseUpLevel;
				oriExp = tempExp;
				tempExp += horseexpadd[oldLevel-1];
				int upLevel = HorseUtil.getUpLevel(oldLevel, tempExp);
				if(upLevel>0){
					if(horseUpLevel+upLevel <= p.level){
						expAdd += horseexpadd[oldLevel-1] ;
						cnt++;
						for(int i=0;i<upLevel;i++){
							oldLevel = horseUpLevel;
							horseUpLevel++;
							tempExp -= HorseUtil.getUpLevelExp(oldLevel, horseUpLevel);
						}
					} else {
						for(int i=1;i<=upLevel;i++){
							oldLevel = horseUpLevel;
							if(horseUpLevel>p.level-1){
								oldLevel --;
								break;
							}
							horseUpLevel++;
							expAdd += HorseUtil.getUpLevelExp(oldLevel, horseUpLevel);
							tempExp -= HorseUtil.getUpLevelExp(oldLevel, horseUpLevel);
						}
						if(horseUpLevel>oldLevel){
							cnt++;
							expAdd -= oriExp;
						}
					}
				} else {
					expAdd += horseexpadd[oldLevel-1];
					cnt++;
				}
				if(horseUpLevel>=p.level){
					break;
				}
				 itemCount--;
			}
			if(cnt>0){
				PlayerTransaction tx = p.newTransaction("WHOLEUSE");
				GameItem item = p.bag.removeGameItem(itemId, -1, cnt, tx, false);
				if(item!=null){
					tx.commit();
				}else{
					tx.rollback();
				}
				p.horse.setExp(expAdd+p.horse.exp, p, "ITE");
				String msg = MessageFormat.format(peony.Messages.STRING_01212,
						p.horse.name,expAdd,cnt,ObjectAccessor.getItemTemplate(itemId).name,horseUpLevel);
				Server.server.getServiceRegistry().getChatService()
				.sendPrivateMessage(p.id, msg);
			}
		}
	}
	
	/**
	 * 兑换马的离线经验
	 * @param session
	 * @param p
	 * @param serial
	 * @param horseInstanceId
	 */
	public void handleHorseExp(Player p){
		long currentTime = System.currentTimeMillis();
		if(p!=null){
			int signCount = p.bag.getGameItemCount(1184);
			if(signCount==0 && p.horseBag.getAgentHorses().size()>0){
				Server.server.getServiceRegistry().getChatService()
				.sendPrivateMessage(p.id, MessageFormat.format(peony.Messages.STRING_01213, ObjectAccessor.getItemTemplate(1184).name));
			}
			if(signCount>0){
				List<Horse> horses = p.horseBag.getAgentHorses();
				if(horses.size()>0){
					Horse horse = null;
					for(Horse h : horses){
						horse = h;
					}
					long agentTime = horse.agentTime;
					int leavingExp = horse.leavingExp;
					long dis = currentTime - agentTime;
					int notOnlineExps = getExp(horse.level, dis, ExpService.TYPE_HORSE);
					leavingExp = notOnlineExps + leavingExp;
					horse.agentTime = currentTime;
					int exp = 0;
					int count = 0;
					while(leavingExp>0){
						int getExp = getExp(horse.level, notonlineDis, ExpService.TYPE_HORSE);
						if(leavingExp<getExp){
							if(exp==0){
								Server.server.getServiceRegistry().getChatService()
								.sendPrivateMessage(p.id, MessageFormat.format(peony.Messages.STRING_01214, leavingExp));
							}
							break;
						}
						int horseUpLevel = HorseUtil.getUpLevel(horse.level, getExp+horse.exp) + horse.level;
						if(horseUpLevel>Player.MAX_LEVEL||horseUpLevel>p.level){
							Server.server.getServiceRegistry().getChatService()
							.sendPrivateMessage(p.id, peony.Messages.STRING_01215);
							break;
						}
						PlayerTransaction tx = p.newTransaction("EXCHORSEEXP");
						GameItem item = p.bag.removeGameItem(1184, -1, 1, tx, false);
						if(item!=null){
							tx.commit();
							horse.setExp(horse.exp + getExp, p, "EXCHORSEEXP");
							leavingExp -= getExp;
							exp += getExp;
							count ++;
						}else{
							tx.rollback();
							break;
						}
					}
					if(exp > 0){
						horse.leavingExp = leavingExp;
						if(leavingExp>0){
							Server.server.getServiceRegistry().getChatService()
							.sendPrivateMessage(p.id, MessageFormat.format(peony.Messages.STRING_01216, 
									exp,ObjectAccessor.getItemTemplate(1184).name,count,leavingExp,ObjectAccessor.getItemTemplate(1184).name));
						}else{
							Server.server.getServiceRegistry().getChatService()
							.sendPrivateMessage(p.id, MessageFormat.format(peony.Messages.STRING_01217, 
									exp,ObjectAccessor.getItemTemplate(1184).name,count));
						}
						LogUtil.logAgentHorseExp(p, horse, exp, leavingExp, count);
					}
				}
			}
		}
	}
	
	
	
	/**
	 * 获取days天的毫秒数
	 * @param days
	 * @return
	 */
	public long getTimeDis(int days){
		return days * 24 * 60 * 60 * 1000L;
	}
	
}
