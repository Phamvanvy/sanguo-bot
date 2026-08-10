package peony.service.welfare;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.util.Utils;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.itemeffect.ActivityItemEffect;
import peony.game.party.PartyMember;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.duel.DuelService;
import peony.service.fame.FameService;
/**
 * 每日福利
 * @author pmeng
 */
public class WelfareService implements Service,ServiceEventListener{

	protected Map<Integer,List<Welfare>> welfares = new HashMap<Integer,List<Welfare>>();//typeId -->List<Welfare>
	
	protected Map<Integer,Welfare> welfares2 = new HashMap<Integer,Welfare>();//id --> welfare
	
	protected Map<Integer,String> welfaresTypes = new HashMap<Integer,String>();//typeId --> typeName
	
	public static String WELFARE_KAY = "welfare_key";//是否对福利列表进行了初始化   1:以初始化
	public static String PLAYER_WELFARE_BEGIN_TIME = "welfare_begin_time";//当前有效时段开始时间
	public static String PLAYER_WELFARE_END_TIME = "welfare_end_time";//当前有效时段结束时间

	public static String WELFARE_ONLINE_ONEHORE = "online_one_hour"; //在线一小时
	public static String WELFARE_ONLINE_BEGIN_TIME = "online_begin_time";//玩家在线开始计算的时间（针对凌晨一点不下线的玩家）
	public static String PLAYER_ONLINE_TIME_CURRETDAY = "online_time";//玩家在线时长
	
	public static String WELFARE_PAY_ONEGLOD = "pay_one_glod";//支付一元宝
	
	public static String WELFARE_EVERYDAY_QUEST = "every_day_quest";//5个每日任务
	
	public static String WELFARE_LOOP_QUEST = "welfare_loop_quest";//跑环任务
	
	public static String WELFARE_EASY_INSTANCE = "welfare_easy_instance";//普通难度副本
	public static String WELFARE_HARD_INSTANCE = "welfare_HARD_instance";//普通难度副本
	public static String WELFARE_KILL_BEN = "welfare_kill_ben";//普通难度副本
	
	public static String WELFARE_FUQI_QUEST = "welfare_fuqi_quest";//夫妻每日
	public static String WELFARE_JIEYI_QUEST = "welfare_jieyi_quest";//结义每日
	public static String WELFARE_YIXING_QUEST = "welfare_yixing_quest";//异性每日
	public static String WELFARE_TAOYUAN_QUEST = "welfare_taoyuan_quest";//仙桃源
	
	public static String WELFARE_CHAOTING_QUEST = "welfare_chaoting_quest";//朝廷任务
	public static String WELFARE_SHICHA_QUEST = "welfare_shicha_quest";//视察任务
	
	public static String WELFARE_KILLENEMY_THREE = "welfare_killenemy_three";//杀3名敌国玩家
	public static String WELFARE_KILLENEMY_TWENTY = "welfare_killenemy_twenty";//杀20名敌国玩家
	public static String WELFARE_BEKILL_TEN = "welfare_bekill_ten";//被杀10次
	public static String PLAYER_KILL_ENEMY = "welfare_kill_enemy";//玩家杀敌人数
	public static String PLAYER_BEKILL_ENEMY = "welfare_bekill_enemy";//玩家被杀数

	public static String WELFARE_KILL_CREATURE_FIFTY = "welfare_kill_creature_fifty";//杀50只怪
	public static String PLAYER_KILL_CREATURE = "player_kill_creature_fifty";//玩家杀怪数
	
	public static String WELFARE_WIN_BATTLE_FIELD = "welfare_win_battle";//战场胜利
	
	protected static long ONEDAY = 24 * 3600 * 1000L;
	protected static long ONEHORE = 3600 * 1000L;
	
	protected WelfareOnlineOneHour welfareOnlineOneHour = null;
	protected PayOneGlod payOneGlod = null;
	protected EveryDayQuest everyDayQuest = null;
	protected LoopQuest loopQuest = null;
	protected EasyInstance easyInstance = null;
	protected HeroInstance heroInstance = null;
	protected KillBen killBen = null;
	protected FinishFuqiQuest fuqiQuest = null;
	protected FinishJieYiQuest jieyiQuest = null;
	protected FinishYiXingQuest yixingQuest = null;
	protected FinishTaoYuanQuest taoyuanQuest = null;
	protected ChaoTingQuest chaoTing = null;
	protected KingLookQuest shiCha = null;
	protected KillThreeEnemy killThreeEnemy = null;
	protected KillTwentyEnemy killTwentyEnemy = null;
	protected BeKilledTen beKillTen = null;
	protected KillFiftyCreature killFiftyCreature = null;
	protected KingLetter kingLetter = null;
	protected JoinDouZhen joinDouZhen = null;
	protected JoinSiLi joinSili = null;
	protected JoinBiWu joinBiWu = null;
	protected KillOneInBattleField killOneInBattle = null; 
	protected WinBattlefieldOne winBattlefield = null;
	
	
	/**根据类型构造福利对象**/
	private Welfare getWelfareById(int welfareTypeId,String welfareTypeName,int welfareId,String welfareName,String welfareDec,String param1,String param2){
		Welfare welfare = null;
		switch(welfareId){
			case 0:
				welfare = new WelfareOnlineOneHour(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				welfareOnlineOneHour = (WelfareOnlineOneHour)welfare;
				break;
			case 1:
				welfare = new PayOneGlod(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				payOneGlod = (PayOneGlod)welfare;
				break;
			case 2:
				welfare = new EveryDayQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				everyDayQuest = (EveryDayQuest)welfare;
				break;
			case 4:
				welfare = new LoopQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				loopQuest = (LoopQuest)welfare;
				break;
			case 5:
				welfare = new EasyInstance(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				easyInstance = (EasyInstance)welfare;
				break;
			case 6:
				welfare = new HeroInstance(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				heroInstance = (HeroInstance)welfare;
				break;
			case 7:
				welfare = new KillBen(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				killBen = (KillBen)welfare;
				break;
			case 8:
				welfare = new ChaoTingQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				chaoTing = (ChaoTingQuest)welfare;
				break;
			case 9:
				welfare = new KingLookQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				shiCha = (KingLookQuest)welfare;
				break;
			case 10:
				welfare = new KingLetter(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				kingLetter = (KingLetter)welfare;
				break;
			case 11:
				welfare = new JoinDouZhen(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				joinDouZhen = (JoinDouZhen)welfare;
				break;
			case 12:
				welfare = new JoinSiLi(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				joinSili = (JoinSiLi)welfare;
				break;
			case 13:
				welfare = new JoinBiWu(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				joinBiWu = (JoinBiWu)welfare;
				break;
			case 14:
				welfare = new FinishFuqiQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				fuqiQuest = (FinishFuqiQuest)welfare;
				break;
			case 15:
				welfare = new FinishJieYiQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				jieyiQuest = (FinishJieYiQuest)welfare;
				break;
			case 16:
				welfare = new FinishYiXingQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				yixingQuest = (FinishYiXingQuest)welfare;
				break;
			case 17:
				welfare = new FinishTaoYuanQuest(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				taoyuanQuest = (FinishTaoYuanQuest)welfare;
				break;
			case 18:
				welfare = new KillThreeEnemy(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				killThreeEnemy = (KillThreeEnemy)welfare;
				break;
			case 19:
				welfare = new KillTwentyEnemy(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				killTwentyEnemy = (KillTwentyEnemy)welfare;
				break;
			case 20:
				welfare = new BeKilledTen(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				beKillTen = (BeKilledTen)welfare;
				break;
			case 21:
				welfare = new KillFiftyCreature(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				killFiftyCreature = (KillFiftyCreature)welfare;
				break;
			case 22:
				welfare = new KillOneInBattleField(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				killOneInBattle = (KillOneInBattleField)welfare;
				break;
			case 23:
				welfare = new WinBattlefieldOne(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
				winBattlefield = (WinBattlefieldOne)welfare;
				break;
			default:
				welfare = new DefaultWelfare(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec, param1, param2); 
		}
		return welfare;
	}
	
	public void shutdown() {
		
	}
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("welfare.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				initPlayerWelfareInfo();
			}
		}, getInitTime(), ONEDAY, TimeUnit.MILLISECONDS);
		
	}
	
	/**距离第一次初始化所需时间**/
	private long getInitTime(){
		Date currentDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		if(cal.get(Calendar.HOUR_OF_DAY) == 0){
			cal.set(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis() - currentDate.getTime();
		}else{
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 1);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis() - currentDate.getTime();
		}
	}
	
	/**
	 * 凌晨一点初始化所有在线玩家的福利表
	 */
	private void initPlayerWelfareInfo(){
		for(Player p:ObjectAccessor.players.values()){
			initEffectiveTime(p);
			for(List<Welfare> welsets:welfares.values()){
				for(Welfare welfare:welsets){
					welfare.initWelfare(p);
				}
			}
		}
	}
	
	/**角色登录时初始化福利表**/
	private void initPlayerWelfareInfo(Player player){
		if(isNeedInit(player) || player.pool.getString(WelfareService.WELFARE_KAY).equals("")){
			player.pool.setString(WelfareService.WELFARE_KAY, "1");
			for(List<Welfare> welsets:welfares.values()){
				for(Welfare welfare:welsets){
					welfare.initWelfare(player);
				}
			}
		}
	}
	/**凌晨一点 初始化角色的有效时段**/
	private void initEffectiveTime(Player player){
		Calendar begin = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		begin.setTime(new Date());
		begin.set(Calendar.HOUR_OF_DAY, 1);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		begin.set(Calendar.MILLISECOND, 0);
		end.setTime(new Date());
		end.add(Calendar.DAY_OF_MONTH, 1);
		end.set(Calendar.HOUR_OF_DAY, 1);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		end.set(Calendar.MILLISECOND, 0);
		player.pool.setLong(PLAYER_WELFARE_BEGIN_TIME,begin.getTimeInMillis());
		player.pool.setLong(PLAYER_WELFARE_END_TIME, end.getTimeInMillis());
	}
	
	
	/**最后一次退出时间是否是当前有效时段   凌晨1点--下一天凌晨1点**/
	private boolean isNeedInit(Player player){
		Calendar cal = Calendar.getInstance();
		cal.setTime(player.lastLogoutTime);
		Calendar begin = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 0){//现在是0--1 有效时段从前一天的凌晨一点开始
			begin.setTime(new Date());
			begin.add(Calendar.DAY_OF_MONTH, -1);
			begin.set(Calendar.HOUR_OF_DAY, 1);
			begin.set(Calendar.MINUTE, 0);
			begin.set(Calendar.SECOND, 0);
			begin.set(Calendar.MILLISECOND, 0);
			end.setTime(new Date());
			end.set(Calendar.HOUR_OF_DAY, 1);
			end.set(Calendar.MINUTE, 0);
			end.set(Calendar.SECOND, 0);
			end.set(Calendar.MILLISECOND, 0);
		}else{//1--0 有效时段到下一天的1点
			begin.setTime(new Date());
			begin.set(Calendar.HOUR_OF_DAY, 1);
			begin.set(Calendar.MINUTE, 0);
			begin.set(Calendar.SECOND, 0);
			begin.set(Calendar.MILLISECOND, 0);
			end.setTime(new Date());
			end.add(Calendar.DAY_OF_MONTH, 1);
			end.set(Calendar.HOUR_OF_DAY, 1);
			end.set(Calendar.MINUTE, 0);
			end.set(Calendar.SECOND, 0);
			end.set(Calendar.MILLISECOND, 0);
		}
			
		if(cal.getTimeInMillis() > begin.getTimeInMillis() && cal.getTimeInMillis() <= end.getTimeInMillis()){//已经初始化过
			if(player.pool.getString(WelfareService.WELFARE_KAY).equals("")){
				player.pool.setLong(PLAYER_WELFARE_BEGIN_TIME,begin.getTimeInMillis());
				player.pool.setLong(PLAYER_WELFARE_END_TIME, end.getTimeInMillis());
			}
			return false;
		}else{
			player.pool.setLong(PLAYER_WELFARE_BEGIN_TIME,begin.getTimeInMillis());
			player.pool.setLong(PLAYER_WELFARE_END_TIME, end.getTimeInMillis());
			return true;
		}
	}
	
	/**判断一个时间是否在有效时段内**/
	public boolean isEffectiveTime(Player player,long time){
		long begin = player.pool.getLong(PLAYER_WELFARE_BEGIN_TIME,0);
		long end = player.pool.getLong(PLAYER_WELFARE_END_TIME,0);
		if(time > begin && time < end){
			return true;
		}else{
			return false;
		}
	}
	
	/** 玩家上线时检测是否有未领取的每日福利奖励 */
	public boolean hasWelfareReady(Player player){
		boolean ret = false;
		int welfareTypeCount = welfaresTypes.size();
		for(int i = 0;i < welfareTypeCount;i ++){
			List<Welfare> wels = welfares.get(i);
			for(int j = 0;j < wels.size();j++){
				Welfare tempWelfare = wels.get(j);
				int state = welfares2.get(tempWelfare.welfareId).getWelfareState(player);
				if(state == Welfare.ALREADY_FINISH){
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		if (root != null) {
			List welfareTypes = root.elements("welfareType");
			for (int i = 0; i < welfareTypes.size(); i++) {
				int typeId = Integer.parseInt(((Element) welfareTypes.get(i))
						.attributeValue("type"));
				String typeName = ((Element)welfareTypes.get(i)).attributeValue("name");
				welfaresTypes.put(typeId, typeName);
				List<Welfare> welfares = ((Element)welfareTypes.get(i)).elements("welfare");
				List<Welfare> tempWelfares = new ArrayList<Welfare>();
				for(int j = 0;j < welfares.size();j++){
					int welfareId = Integer.parseInt(((Element) welfares.get(j)).attributeValue("welfareId"));
					String welfareName = ((Element)welfares.get(j)).attributeValue("welfareName");
					String welfareDec = ((Element)welfares.get(j)).attributeValue("welfareDec");
					String param1  = ((Element)welfares.get(j)).attributeValue("param1");
					String param2  = ((Element)welfares.get(j)).attributeValue("param2");
					Welfare welfare = getWelfareById(typeId,typeName,welfareId,welfareName,welfareDec,param1,param2);
					tempWelfares.add(welfare);
					String[] reward = Utils.stringToStringArray(((Element)welfares.get(j)).attributeValue("reward"), ';');
					for(String str:reward){
						int rewardId = Utils.stringToIntArray(str,',')[0];
						int count = Utils.stringToIntArray(str,',')[1];
						welfare.rewardItems.put(rewardId,count);
					}
					welfares2.put(welfareId, welfare);
				}
				this.welfares.put(typeId, tempWelfares);
			}
	    }
	}
	
	/**
	 * 每日福利返回
	 * seral						int
	 * size         				short   福利分类数
	 * 循环size次
	 * 		welfareTypeName     	String  福利分类名称
	 * 		welfareSize         	short   包含具体福利数 
	 * 		finishNum 				short   本分类完成的福利数
	 * 			循环welfareSize次   
	 * 				welfareId		short   福利id
	 * 				welfareName     String  福利名称
	 * 				welfareDec      String  福利描述
	 * 				isFinished      byte    是否完成（0:未完成    1：完成）
	 * 				rewardSize      short    奖励的种类数
	 * 					循环rewardSize次
	 * 						itemId  int     奖励物品id
	 * 						count   int     物品的数量
	 */
	public synchronized void getWelfareList(Packet packet,ClientSession session){
		Player player = (Player) session.getClient();
		int serial = packet.getInt();
		int playerID = packet.getInt();
		playerID = Math.abs(playerID);
		Player owner = ObjectAccessor.getPlayer(playerID);
		FameService fs = Server.server.getServiceRegistry().getFameService();
		DuelService ds = Server.server.getServiceRegistry().getDuelService();
		if(owner == null && fs != null){
			owner = fs.getStatue(playerID);
		}
		if(owner == null && ds != null){
			owner = ds.getStatue(playerID);
		}
		if(player!=null && owner != null){
			Packet pt = new Packet(OpCode.WELFARE_LIST_SERVER);
			pt.putInt(serial);
			int welfareTypeCount = welfaresTypes.size();
			pt.putShort(welfareTypeCount);
			for(int i = 0;i < welfareTypeCount;i ++){
				List<Welfare> wels = welfares.get(i);
				pt.putString(welfaresTypes.get(i));
				pt.putShort(wels.size());
				pt.putShort(getAccomplishWelfareByType(owner,wels));
				for(int j = 0;j < wels.size();j++){
					Welfare tempWelfare = wels.get(j);
					pt.putShort(tempWelfare.welfareId);
					pt.putString(tempWelfare.welfareName);
					pt.putString(tempWelfare.welfareDec);
					pt.put(tempWelfare.getWelfareState(owner));
					if(tempWelfare.getWelfareState(owner)==Welfare.ALREADY_REWARD){
						pt.putShort(0);
					}else{
						pt.putShort(tempWelfare.rewardItems.size());
						for(int k = 0;k < tempWelfare.rewardItems.size();k++){
							Set<Integer> key = tempWelfare.rewardItems.keySet();
							for(int itemid:key){
								pt.putInt(itemid);
								pt.putInt(tempWelfare.rewardItems.get(itemid));
								ItemTemplate it = ObjectAccessor.getItemTemplate(itemid);
								if(it != null){
									pt.putInt(it.showType);
								}else{
									pt.putInt(0);
								}
							}
						}
					}
				}
			}
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.WELFARE_LIST_CLIENT, peony.Messages.STRING_00088);
		}
	}
	
	/**
	 * 领取奖励
	 */
	public synchronized int getRewared(int welfareId,Player player){
		Welfare welfare = welfares2.get(welfareId);
		if(welfare != null && welfare.getWelfareState(player)== Welfare.ALREADY_FINISH){
			Set<Integer> itemIds = welfare.getReward().keySet();
			for(int id:itemIds){
				int count = welfare.getReward().get(id);
				GameItem item = ObjectAccessor.createGameItem(id);
				if(item != null){
					PlayerTransaction tx = player.newTransaction("WELFAREREWARD");
					try {
						player.bag.addGameItemComplete(item, count, tx, true);
						tx.commit();
					} catch (Exception e) {
						tx.rollback();
						String content = MessageFormat.format(
								peony.Messages.STRING_00778, welfare.welfareName,count,
								item.template.name);
						Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_00779, content, 0,
								item, count, "WELFAREREWARD");
					}
				}
			}
			welfare.recordReward(player);
		}
		return welfare.getWelfareState(player);
	}
	
	private short getAccomplishWelfareByType(Player player,List<Welfare> welfares){
		short count = 0;
		for(int i = 0;i < welfares.size();i++){
			Welfare tempWel = welfares.get(i);
			if(tempWel.handler(player)){
				count++;
			}
		}
		return count;
	}

	/**实时判断福利的完成**/
	public void proccessWelfareFinish(Player p){
		for(int i = 0;i < welfares2.size();i++){
			Welfare tempWel = welfares2.get(i);
			if(tempWel != null){
				tempWel.handler(p);
			}
		}
	}
	
	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOADED,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
				ServiceEvent.EVENT_IBUY,
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_BATTLE_WIN
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_PLAYER_LOADED:
				initPlayerWelfareInfo((Player)event.param1);
				break;
			case ServiceEvent.EVENT_PLAYER_LOGOUTED:
				welfareOnlineOneHour.recordOnlineTime((Player)event.param1);
				break;
			case ServiceEvent.EVENT_IBUY:
				payOneGlod.processIbuy((Integer)event.param1, (Integer)event.param2);
				break;
			case ServiceEvent.EVENT_UNIT_DIE:
				easyInstance.unitDie((Unit)event.param1,(Unit)event.param2);
				heroInstance.unitDie((Unit)event.param1,(Unit)event.param2);
				killBen.unitDie((Unit)event.param1,(Unit)event.param2);
				killThreeEnemy.unitDie((Unit)event.param1,(Unit)event.param2);
				killOneInBattle.unitDie((Unit)event.param1,(Unit)event.param2);
				break;
			case ServiceEvent.EVENT_BATTLE_WIN:
				winBattlefield.processBattleWin((Integer)event.param1,(Integer) event.param2);
				break;
		}
	}

	
	/**每个福利对应一个具体实现类--在线一小时**/
	class WelfareOnlineOneHour extends Welfare {
		
		public WelfareOnlineOneHour(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		public void recordOnlineTime(Player player){
			long time = player.pool.getLong(PLAYER_ONLINE_TIME_CURRETDAY, 0) + (System.currentTimeMillis() - player.pool.getLong(WELFARE_ONLINE_BEGIN_TIME, 0L));
			player.pool.setLong(PLAYER_ONLINE_TIME_CURRETDAY, time);
		}
		public void initBeginTime(Player player){
			if(isEffectiveTime(player, player.loginTime)){//1点后登录
				player.pool.setLong(WELFARE_ONLINE_BEGIN_TIME, player.loginTime);
			}else{//0--1点登录
				player.pool.setLong(WELFARE_ONLINE_BEGIN_TIME, player.pool.getLong(PLAYER_WELFARE_BEGIN_TIME,0L));
			}
		}
		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_ONLINE_ONEHORE,NOT_FINISH);
			player.pool.setLong(PLAYER_ONLINE_TIME_CURRETDAY, 0);
			initBeginTime(player);
		}

		public boolean handler(Player player) {
			initBeginTime(player);
			long onlineTime = player.pool.getLong(PLAYER_ONLINE_TIME_CURRETDAY, 0) + (System.currentTimeMillis() - player.pool.getLong(WELFARE_ONLINE_BEGIN_TIME, 0L));
			if(onlineTime > ONEHORE){
				if(player.pool.getInt(WELFARE_ONLINE_ONEHORE,NOT_FINISH) == NOT_FINISH){
					player.pool.setInt(WELFARE_ONLINE_ONEHORE,ALREADY_FINISH);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
				}
				return true;
			}
			return false;
		}
		
		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_ONLINE_ONEHORE,ALREADY_REWARD);
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_ONLINE_ONEHORE);
		}
		
	}
	
	/**消费一元宝**/
	class PayOneGlod extends Welfare {

		public PayOneGlod(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		protected void processIbuy(int playerId, int imoney){
			Player p = ObjectAccessor.getPlayer(playerId);
			if(p!=null){
				if(payOneGlod.getWelfareState(p) == Welfare.NOT_FINISH){
					if(imoney >= 36){
						p.pool.setInt(WELFARE_PAY_ONEGLOD,Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,p));
					}
				}
				ActivityItemEffect.addCount(p);//监听打折卡问题
			}
		}
		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_PAY_ONEGLOD,Welfare.NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_PAY_ONEGLOD,Welfare.ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(payOneGlod.getWelfareState(player) == Welfare.NOT_FINISH){
				return false;
			}
			return true;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_PAY_ONEGLOD,0);
		}
	}
	
	/**5个每日任务**/
	class EveryDayQuest extends Welfare {
		
		public EveryDayQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_EVERYDAY_QUEST, Welfare.NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_EVERYDAY_QUEST, Welfare.ALREADY_REWARD);
		}
		
		public boolean handler(Player player) {
			if(player.asmVm.getEveryDayQuest()>=5){
				if(player.pool.getInt(WELFARE_EVERYDAY_QUEST) == NOT_FINISH){
					player.pool.setInt(WELFARE_EVERYDAY_QUEST, Welfare.ALREADY_FINISH);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
				}
				return true;
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_EVERYDAY_QUEST, Welfare.NOT_FINISH);
		}
	}


	/**一系列跑环任务**/
	class LoopQuest extends Welfare {
		
		int[] questId = new int[]{
				1409,
				1419,
				1429,
				1439,
				1299,
				1259,
				1269,
				1359,
				1369,
				1379,
				1389,
				1399,
				1279,
				1289,
				1309,
				1319,
				1329,
				1339,
				1349,
				1239,
				1249};//环任务的最后一个任务
		
		public LoopQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_LOOP_QUEST, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_LOOP_QUEST, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			for(int id:questId){
				if(isEffectiveTime(player,player.asmVm.getFinishTime(id))){
					if(player.pool.getInt(WELFARE_LOOP_QUEST) == NOT_FINISH){
						player.pool.setInt(WELFARE_LOOP_QUEST, Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
					}
					return true;
				}
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_LOOP_QUEST,Welfare.NOT_FINISH);
		}
	}
	
	/**普通难度副本**/
	class EasyInstance extends Welfare {
		
		int[] bossIds = new int[]{
			4001795,
			3162115,
			1769474,
			5509157
		};
		
		public EasyInstance(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		public void unitDie(Unit u1,Unit u2){
			for(int id:bossIds){
				if(u1.id == id){
					Player p = ObjectAccessor.getPlayer(u2.id);
					if(p!=null){
						if(p.party != null){
							for(PartyMember player:p.party.members){
								if(player.player.pool.getInt(WELFARE_EASY_INSTANCE) == NOT_FINISH){
									player.player.pool.setInt(WELFARE_EASY_INSTANCE, ALREADY_FINISH);
									Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player.player));
								}
							}
						}else{
							if(p.pool.getInt(WELFARE_EASY_INSTANCE) == NOT_FINISH){
								p.pool.setInt(WELFARE_EASY_INSTANCE, ALREADY_FINISH);
								Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,p));
							}
						}
					}
				}
			}
		}

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_EASY_INSTANCE, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_EASY_INSTANCE, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(WELFARE_EASY_INSTANCE, NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}
		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_EASY_INSTANCE,NOT_FINISH);
		}
	}
	
	/**英雄难度副本**/
	class HeroInstance extends Welfare {
		
		int[] bossIds = new int[]{
				4005891,
				4669443,
				1773570,
				5513228
		};
		
		public HeroInstance(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}

		public void unitDie(Unit u1,Unit u2){
			for(int id:bossIds){
				if(u1.id == id){
					Player p = ObjectAccessor.getPlayer(u2.id);
					if(p!=null){
						if(p.party != null){
							for(PartyMember player:p.party.members){
								if(player.player.pool.getInt(WELFARE_HARD_INSTANCE) == NOT_FINISH){
									player.player.pool.setInt(WELFARE_HARD_INSTANCE, ALREADY_FINISH);
									Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player.player));
								}
							}
						}else{
							if(p.pool.getInt(WELFARE_HARD_INSTANCE) == NOT_FINISH){
								p.pool.setInt(WELFARE_HARD_INSTANCE, ALREADY_FINISH);
								Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,p));
							}
						}
					}
				}
			}
		}
		
		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_HARD_INSTANCE, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_HARD_INSTANCE, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(WELFARE_HARD_INSTANCE, NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}
		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_HARD_INSTANCE,NOT_FINISH);
		}
	}
	
	/**击杀本董卓**/
	class KillBen extends Welfare {
		int[] bossIds = new int[]{
				6422532
		};
		public KillBen(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}

		public void unitDie(Unit u1,Unit u2){
			for(int id:bossIds){
				if(u1.id == id){
					Player p = ObjectAccessor.getPlayer(u2.id);
					if(p!=null){
						if(p.party != null){
							for(PartyMember player:p.party.members){
								if(player.player.pool.getInt(WELFARE_KILL_BEN) == NOT_FINISH){
									player.player.pool.setInt(WELFARE_KILL_BEN, ALREADY_FINISH);
									Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player.player));
								}
							}
						}else{
							if(p.pool.getInt(WELFARE_KILL_BEN) == NOT_FINISH){
								p.pool.setInt(WELFARE_KILL_BEN, ALREADY_FINISH);
								Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,p));
							}
						}
					}
				}
			}
		}
		
		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_KILL_BEN, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_KILL_BEN, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(WELFARE_KILL_BEN, NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}
		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_KILL_BEN,NOT_FINISH);
		}
	}
	
	/**完成1朝廷任务**/
	class ChaoTingQuest extends Welfare {
		
		int[] questId  = new int[]{
				567,
				568,
				569,
				570,
				571,
				572,
				573,
				574,
				575,
				576,
				577,
				578,
				579,
				580,
				581
		};
		
		public ChaoTingQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_CHAOTING_QUEST, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_CHAOTING_QUEST, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			for(int id:questId){
				if(isEffectiveTime(player,player.asmVm.getFinishTime(id))){
					if(player.pool.getInt(WELFARE_CHAOTING_QUEST) == NOT_FINISH){
						player.pool.setInt(WELFARE_CHAOTING_QUEST, Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
					}
					return true;
				}
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_CHAOTING_QUEST,Welfare.NOT_FINISH);
		}
	}
	
	/**完成1国公视察任务**/
	class KingLookQuest extends Welfare {
		
		int[] questId = new int[]{
				2273,2274,2275,2276,2277,2278,2279,2280,2281,2282,2283,2284,2285,2286,2287
		};
		
		public KingLookQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_SHICHA_QUEST, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_SHICHA_QUEST, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			for(int id:questId){
				if(isEffectiveTime(player,player.asmVm.getFinishTime(id))){
					if(player.pool.getInt(WELFARE_SHICHA_QUEST) == NOT_FINISH){
						player.pool.setInt(WELFARE_SHICHA_QUEST, Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
					}
					return true;
				}
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_SHICHA_QUEST,Welfare.NOT_FINISH);
		}
	}
	
	/**向国君呈上密函**/
	class KingLetter extends Welfare {
		
		public KingLetter(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(Player.WELFARE_SUBMIT_LETTER,NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(Player.WELFARE_SUBMIT_LETTER,ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(Player.WELFARE_SUBMIT_LETTER,NOT_FINISH)==NOT_FINISH){
				return false;
			}
			return true;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(Player.WELFARE_SUBMIT_LETTER,NOT_FINISH);
		}
	}
	
	/**参加一次斗阵**/
	class JoinDouZhen extends Welfare {
		
		public JoinDouZhen(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(Player.WELFARE_JOIN_TOWER, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(Player.WELFARE_JOIN_TOWER, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(Player.WELFARE_JOIN_TOWER,NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}
		public int getWelfareState(Player player) {
			return player.pool.getInt(Player.WELFARE_JOIN_TOWER,NOT_FINISH);
		}
	}
	
	/**参加一次司隶战役**/
	class JoinSiLi extends Welfare {
		
		public JoinSiLi(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		public void initWelfare(Player player){
			player.pool.setInt(Player.WELFARE_JOIN_SILI, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(Player.WELFARE_JOIN_SILI, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(Player.WELFARE_JOIN_SILI,NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(Player.WELFARE_JOIN_SILI,NOT_FINISH);
		}
	}
	
	/**参加一次比武招亲**/
	class JoinBiWu extends Welfare {
		
		public JoinBiWu(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		public void initWelfare(Player player){
			player.pool.setInt(Player.WELFARE_JOIN_BIWU,NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(Player.WELFARE_JOIN_BIWU,ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			int a = player.pool.getInt(Player.WELFARE_JOIN_BIWU,NOT_FINISH);
			if(player.pool.getInt(Player.WELFARE_JOIN_BIWU,NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(Player.WELFARE_JOIN_BIWU,NOT_FINISH);
		}
	}
	
	/**完成一次夫妻每日任务**/
	class FinishFuqiQuest extends Welfare {
		
		int[] questId = new int[]{
				597,
				598,
				599,
				1446,
				1447,
				1448,
				2387,
				2388,
				2389,
				2390,
				2391,
				2392,
				2396,
				2397,
				2398,
				2399,
				2400,
				2401,
				2402,
				2403,
				2404,
				2405,
				2406,
				2407,
				2408,
				2409,
				2410,
				2411,
				2412,
				2413,
				2414,
				2415,
				2416,
				2417,
				2418,
				2419,
				2420,
				2421,
				2422,
				2423,
				2424,
				2425,
				2426,
				2427,
				2428,
				2429,
				2430,
				2431,
				2432,
				2433,
				2434
		};

		public FinishFuqiQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_FUQI_QUEST, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_FUQI_QUEST, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			for(int id:questId){
				if(isEffectiveTime(player,player.asmVm.getFinishTime(id))){
					if(player.pool.getInt(WELFARE_FUQI_QUEST) == NOT_FINISH){
						player.pool.setInt(WELFARE_FUQI_QUEST, Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
					}
					return true;
				}
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_FUQI_QUEST,Welfare.NOT_FINISH);
		}
	}
	
	/**完成一次结义每日任务**/
	class FinishJieYiQuest extends Welfare {
		
		int[] questId = new int[]{
				2138,
				2139,
				2146
		};
		
		public FinishJieYiQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_JIEYI_QUEST, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_JIEYI_QUEST, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			for(int id:questId){
				if(isEffectiveTime(player,player.asmVm.getFinishTime(id))){
					if(player.pool.getInt(WELFARE_JIEYI_QUEST) == NOT_FINISH){
						player.pool.setInt(WELFARE_JIEYI_QUEST, Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
					}
					return true;
				}
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_JIEYI_QUEST,Welfare.NOT_FINISH);
		}
	}
	
	/**完成一次异性任务**/
	class FinishYiXingQuest extends Welfare {
		
		int[] questId = new int[]{
				841,
				842,
				843,
				844,
				845,
				846,
				847,
				848,
				849,
				850,
				851,
				852,
				853,
				854,
				855,
				856,
				857,
				858,
				859,
				860,
				861,
				862,
				863,
				864,
				865,
				866,
				867,
				868,
				869,
				870,
				871,
				872,
				873,
				874,
				875,
				876,
				877,
				878,
				881,
				882,
				883,
				884,
				885,
				886,
				887,
				888,
				889,
				890,
				891,
				892,
				893,
				894,
				895,
				896,
				897,
				898,
				899
		};
		
		public FinishYiXingQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_YIXING_QUEST, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_YIXING_QUEST, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			for(int id:questId){
				if(isEffectiveTime(player,player.asmVm.getFinishTime(id))){
					if(player.pool.getInt(WELFARE_YIXING_QUEST) == NOT_FINISH){
						player.pool.setInt(WELFARE_YIXING_QUEST, Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
					}
					return true;
				}
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_YIXING_QUEST,Welfare.NOT_FINISH);
		}
	}
	
	/**闯荡一次仙桃源**/
	class FinishTaoYuanQuest extends Welfare {
		
		int[] questId = new int[]{
				898,
				899,
				877,
				878,
				858,
				859
		};
		
		public FinishTaoYuanQuest(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_TAOYUAN_QUEST, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_TAOYUAN_QUEST, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			for(int id:questId){
				if(isEffectiveTime(player,player.asmVm.getFinishTime(id))){
					if(player.pool.getInt(WELFARE_TAOYUAN_QUEST) == NOT_FINISH){
						player.pool.setInt(WELFARE_TAOYUAN_QUEST, Welfare.ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
					}
					return true;
				}
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_TAOYUAN_QUEST,Welfare.NOT_FINISH);
		}
	}
	
	/**击杀3名敌国玩家**/
	class KillThreeEnemy extends Welfare {
		
		public KillThreeEnemy(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}

		public void initWelfare(Player player){
			player.pool.setInt(PLAYER_KILL_ENEMY, 0);
			player.pool.setInt(WELFARE_KILLENEMY_THREE, NOT_FINISH);
		}
		
		protected void unitDie(Unit u1,Unit u2){
			//杀人
			if(u1.type==GameObject.TYPE_PLAYER&&u2.type==GameObject.TYPE_PLAYER){
				if(u1.faction!=u2.faction){
					int num = ((Player)u1).pool.getInt(PLAYER_BEKILL_ENEMY);
					num++;
					((Player)u1).pool.setInt(PLAYER_BEKILL_ENEMY, num);
					int num2 = ((Player)u2).pool.getInt(PLAYER_KILL_ENEMY);
					num2++;
					((Player)u2).pool.setInt(PLAYER_KILL_ENEMY, num2);
				}
			}
			//杀怪
			if(u1.type == GameObject.TYPE_CREATURE&&u2.type==GameObject.TYPE_PLAYER){
				int num3 = ((Player)u2).pool.getInt(PLAYER_KILL_CREATURE);
				num3++;
				((Player)u2).pool.setInt(PLAYER_KILL_CREATURE, num3);
			}
			
		}
		
		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_KILLENEMY_THREE, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(PLAYER_KILL_ENEMY) >= 3){
				if(player.pool.getInt(WELFARE_KILLENEMY_THREE) == NOT_FINISH){
					player.pool.setInt(WELFARE_KILLENEMY_THREE, ALREADY_FINISH);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
				}
				return true;
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_KILLENEMY_THREE,NOT_FINISH);
		}
	}
	
	/**击杀20名敌国玩家**/
	class KillTwentyEnemy extends Welfare {
		
		public KillTwentyEnemy(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		

		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_KILLENEMY_TWENTY, NOT_FINISH);
		}
		
		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_KILLENEMY_TWENTY, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(PLAYER_KILL_ENEMY) >= 20){
				if(player.pool.getInt(WELFARE_KILLENEMY_TWENTY) == NOT_FINISH){
					player.pool.setInt(WELFARE_KILLENEMY_TWENTY, ALREADY_FINISH);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
				}
				return true;
			}
			return false;
		}
		
		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_KILLENEMY_TWENTY,NOT_FINISH);
		}
	}
	
	/**被敌国玩家击杀10次**/
	class BeKilledTen extends Welfare {
		
		public BeKilledTen(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		public void initWelfare(Player player){
			player.pool.setInt(PLAYER_BEKILL_ENEMY, 0);
			player.pool.setInt(WELFARE_BEKILL_TEN, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_BEKILL_TEN,ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(PLAYER_BEKILL_ENEMY)>=10){
				if(player.pool.getInt(WELFARE_BEKILL_TEN) == NOT_FINISH){
					player.pool.setInt(WELFARE_BEKILL_TEN,ALREADY_FINISH);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
				}
				return true;
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_BEKILL_TEN,NOT_FINISH);
		}
	}
	
	/**击杀50只怪物**/
	class KillFiftyCreature extends Welfare {
		
		public KillFiftyCreature(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}

		public void initWelfare(Player player){
			player.pool.setInt(PLAYER_KILL_CREATURE, 0);
			player.pool.setInt(WELFARE_KILL_CREATURE_FIFTY, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_KILL_CREATURE_FIFTY, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(PLAYER_KILL_CREATURE) >= 50){
				if(player.pool.getInt(WELFARE_KILL_CREATURE_FIFTY) == NOT_FINISH){
					player.pool.setInt(WELFARE_KILL_CREATURE_FIFTY, ALREADY_FINISH);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,player));
				}
				return true;
			}
			return false;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_KILL_CREATURE_FIFTY,NOT_FINISH);
		}
	}
	
	/**在战场内击杀一个玩家**/
	class KillOneInBattleField extends Welfare {
		
		int[] mapId = new int[]{
				752,
				912,
				928
		};
		
		public KillOneInBattleField(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}

		public void initWelfare(Player player){
			player.pool.setInt(Player.WELFARE_KILLONE_INDUEL, NOT_FINISH);
		}
		
		public boolean isInArray(int id){
			for(int a:mapId){
				if(a == id){
					return true;
				}
			}
			return false;
		}
		
		public void unitDie(Unit u1,Unit u2){
			if(u1.type==GameObject.TYPE_PLAYER&&u2.type==GameObject.TYPE_PLAYER){
				if(isInArray(u1.map.id) && u1.map.id == u2.map.id && u1.faction!=u2.faction){
					if(((Player)u2).pool.getInt(Player.WELFARE_KILLONE_INDUEL, NOT_FINISH) == NOT_FINISH){
						((Player)u2).pool.setInt(Player.WELFARE_KILLONE_INDUEL, ALREADY_FINISH);
						Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,(Player)u2));
					}
				}
			}
		}
		
		public void recordReward(Player player) {
			player.pool.setInt(Player.WELFARE_KILLONE_INDUEL, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(Player.WELFARE_KILLONE_INDUEL, NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(Player.WELFARE_KILLONE_INDUEL, NOT_FINISH);
		}
	}
	
	/**获胜一次战场**/
	class WinBattlefieldOne extends Welfare {
		
		public WinBattlefieldOne(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		public void processBattleWin(int playerId,int typeId){
			Player p = ObjectAccessor.getPlayer(playerId);
			if(p != null && typeId == 1){
				if(p.pool.getInt(WELFARE_WIN_BATTLE_FIELD,NOT_FINISH) == NOT_FINISH){
					p.pool.setInt(WELFARE_WIN_BATTLE_FIELD, ALREADY_FINISH);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,p));
				}
			}
		}
		public void initWelfare(Player player){
			player.pool.setInt(WELFARE_WIN_BATTLE_FIELD, NOT_FINISH);
		}

		public void recordReward(Player player) {
			player.pool.setInt(WELFARE_WIN_BATTLE_FIELD, ALREADY_REWARD);
		}

		public boolean handler(Player player) {
			if(player.pool.getInt(WELFARE_WIN_BATTLE_FIELD, NOT_FINISH) == NOT_FINISH){
				return false;
			}
			return true;
		}

		public int getWelfareState(Player player) {
			return player.pool.getInt(WELFARE_WIN_BATTLE_FIELD, NOT_FINISH);
		}
	}
	
	/**缺省实现**/
	class DefaultWelfare extends Welfare {
		
		public DefaultWelfare(int welfareTypeId, String welfareTypeName,
				int welfareId, String welfareName, String welfareDec,
				String param1, String param2) {
			super(welfareTypeId, welfareTypeName, welfareId, welfareName, welfareDec,
					param1, param2);
		}
		
		public void initWelfare(Player player){
		}

		public void recordReward(Player player) {
		}

		public boolean handler(Player player) {
				return false;
		}

		public int getWelfareState(Player player) {
			return  NOT_FINISH;
		}
	}
	
	
}
