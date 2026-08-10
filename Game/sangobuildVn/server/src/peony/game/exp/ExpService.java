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
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.buff.Buff;
import peony.game.changed.ChangedItem;
import peony.game.chat.ChatMessage;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class ExpService implements Service, ServiceEventListener {
	
	private static final Logger log = Logger.getLogger(ExpService.class);
	public static final int TYPE_PLAYER = 1;
	public static final int TYPE_HORSE = 0;
	public static final String PLAYER_LEAVINGEXP = "LEAVINGEXP";
	public float offLineRatio = 1.0f;
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
		68928,
		71358,
		73824,
		76329,
		78937,
		81586,
		84273,
		87000,
		89835,
		92712,
		95628,
		98658,
		101730,
		104844,
		108000,
};

	protected static int[] horseOnlineExps = {
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
	};
	
	public static long onlineDis = 20*60*1000L;
	public static long notonlineDis = 15*60*1000L;
	public static int checkDay = 60;
	public static int checkLevel = 20;
	public static int maxExp = 25000000;
	
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
				throw new ExpException("没有对应的马。");
			}
			if(horse.agentHorse==1){
				throw new ExpException("不可重复代理饲养");
			}
			if(p.horseBag.getAgentHorses().size()>=1){
				throw new ExpException("不可代理饲养更多的马");
			}
			if(horse.level>p.level){
				throw new ExpException("不能代理饲养超过人等级的马");
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
			PlayerTransaction tx = p.newTransaction("BUY");
				GameItem item = p.bag.removeGameItem(1183, -1, 1, tx, false);
				if(item!=null && p.map.getId()==mapId[p.faction]){
					int getExp = getExp(p.level, onlineDis, ExpService.TYPE_PLAYER);
					p.addExp(getExp, tx, true);
					Server.server.getServiceRegistry().getChatService().addChatMessage(
							new ChatMessage(ChatOption.PRIVATE, -1, -1, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>",
									p.id, MessageFormat.format("阁下获{0}经验，消耗1盒{1}", getExp,ObjectAccessor.getItemTemplate(1183).name), null));
					tx.commit();
					LogUtil.logExchangeOnlineExp(p, getExp);
				}else{
					tx.rollback();
					long leavingExp = p.pool.getLong(ExpService.PLAYER_LEAVINGEXP, 0L);
					leavingExp += getExp(p.level, onlineDis, ExpService.TYPE_PLAYER);
					leavingExp = leavingExp>maxExp ? maxExp : leavingExp;
					p.pool.setLong(ExpService.PLAYER_LEAVINGEXP, leavingExp);
					if(p.map.getId()==mapId[p.faction]){
						Server.server.getServiceRegistry().getChatService().addChatMessage(
							new ChatMessage(ChatOption.PRIVATE, -1, -1, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>",
									p.id, MessageFormat.format("您的{0}不足，请进入充值商店购买。", ObjectAccessor.getItemTemplate(1183).name), null));
					}
				}
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
				leavingExp += offLineRatio * count * onlineExps[p.level];
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
	 * 兑换离线经验
	 * @param p
	 * @return 剩余离线经验
	 */
	public void exchaneExp(Player p, Packet packet, ClientSession session, int serial){
		if(p!=null){
			int leavingExp = getNotOnineExps(p);
			if(leavingExp==0){
				p.message(-1, "暂时没有可兑换的离线经验", -1, -1);
				return;
			}
			int count = p.bag.getGameItemCount(1183);
			if(count==0){
				p.message(-1, MessageFormat.format("您的{0}不足，请进入充值商店购买", ObjectAccessor.getItemTemplate(1183).name),
						-1, -1);
				return;
			}else{
				int exp = 0;
				int count1 = 0;
				while(leavingExp>0){
					int getExp = getExp(p.level, 30*60*1000L, ExpService.TYPE_PLAYER);
					if(leavingExp<getExp){
						if(exp==0){
							p.message(-1, "当前离线经验不够兑换", -1, -1);
						}
						break;
					}
					if(p.level>=Player.MAX_LEVEL)
						break;
					PlayerTransaction tx = p.newTransaction("BUY");
					GameItem item = p.bag.removeGameItem(1183, -1, 1, tx, false);
					if(item!=null){
						p.addExp(getExp, tx, true);
						tx.commit();
						leavingExp -= getExp;
						exp += getExp;
						count1 ++;
					}else{
						tx.rollback();
						break;
					}
				}
				if(exp > 0){
					p.pool.setLong(ExpService.PLAYER_LEAVINGEXP, leavingExp);
					int getExp = getExp(p.level, 30*60*1000L, ExpService.TYPE_PLAYER);
					String msg;
					if(leavingExp < getExp){
						msg = MessageFormat.format("恭喜您获得经验{0},消耗{1}盒{2},剩余{3}经验值不够兑换",
								exp,count1,ObjectAccessor.getItemTemplate(1183).name,leavingExp);
					} else {
						msg = MessageFormat.format("恭喜您获得经验{0},消耗{1}盒{2},还剩余{3}经验值可兑换，快去充值商店购买吧",
								exp,count1,ObjectAccessor.getItemTemplate(1183).name,leavingExp);
					}
					Server.server.getServiceRegistry().getChatService()
					.sendPrivateMessage(p.id, msg);
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_USEITEM,p,1183,count1));
					LogUtil.logExchangeOfflineExp(p, exp, leavingExp, count1);
				}
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
				.sendPrivateMessage(p.id, MessageFormat.format("您的{0}不足，请进入充值商店购买。", ObjectAccessor.getItemTemplate(1184).name));
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
								.sendPrivateMessage(p.id, MessageFormat.format("剩余经验{0}不足兑换", leavingExp));
							}
							break;
						}
						int horseUpLevel = HorseUtil.getUpLevel(horse.level, getExp+horse.exp) + horse.level;
						if(horseUpLevel>Player.MAX_LEVEL||horseUpLevel>p.level){
							Server.server.getServiceRegistry().getChatService()
							.sendPrivateMessage(p.id, "坐骑等级不能超过人物等级，离线代理喂养停止，" +
									"加油练级才能驾驭更高级的坐骑。");
							break;
						}
						PlayerTransaction tx = p.newTransaction("BUY");
						GameItem item = p.bag.removeGameItem(1184, -1, 1, tx, false);
						if(item!=null){
							tx.commit();
							horse.setExp(horse.exp + getExp, p, "BUY");
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
							.sendPrivateMessage(p.id, MessageFormat.format("代理饲养的马获取经验值{0},消耗{1}{2}剩余{3}经验,快去充值商店购买{4}吧", 
									exp,ObjectAccessor.getItemTemplate(1184).name,count,leavingExp,ObjectAccessor.getItemTemplate(1184).name));
						}else{
							Server.server.getServiceRegistry().getChatService()
							.sendPrivateMessage(p.id, MessageFormat.format("代理饲养的马获取经验值{0},消耗{1}{2}剩余{3}经验", 
									exp,ObjectAccessor.getItemTemplate(1184).name,count,leavingExp));
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
