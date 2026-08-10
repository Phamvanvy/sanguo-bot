package peony.service.duelmetting;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.GameItem;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.MoveCallback;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.mail.MailService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 新战场 天下第一武道会
 * @author pmeng
 */
public class DuelMettingService implements Service,VMapManager,ServiceEventListener{
	
	protected final Logger log = Logger.getLogger(DuelMettingService.class);
	
	protected DuelMettingParam param;
	
	protected Timer signTimer = new Timer(); // 报名计时器
	
	protected Timer duelTimer = new Timer();//开启副本计时器
	
	protected Timer battleTimer = new Timer();//战斗计时器
	
	protected boolean canSignUp = false;//是否可报名
	
	protected List<Integer> playerIds = new ArrayList<Integer>();//报名成功且未被淘汰的角色
	
	protected List<Integer> signUpIds = new ArrayList<Integer>();//报名成功的玩家
	
	protected List<Integer> loseIds = new ArrayList<Integer>();//淘汰的玩家
	
	protected List<DuelMettingInstance> instances = new ArrayList<DuelMettingInstance>(); // 副本集合
	
	protected IntHashMap<Integer> groups = new IntHashMap<Integer>();//分组情况     playerId --> otherPlayerId
	
	public static long ONE_WEEK = 24 * 60 * 60 * 1000 * 7L;//一周
	
	public static int MAX_PLAYER = 2;//一个副本内人数
	
	public static int ITEMID_RED = 3677;//报名成功奖励药品  包中最多有10个
	
	public static int ITEM_NUM_REWARD = 2;//胜利一场奖励药品数
	
	public static int ITEM_NUM_RED = 10;//包中令牌最多个数　
	
	public static int ITEMID_LINGPAI = 3678;//报名的消耗道具
	
//	protected static int ITEMID_REWARD = 3679;//冠军奖励
	protected static int ITEMID_REWARD = 4716;//冠军奖励
	
	public static final String DUELMETTING_WIN_TIME = "DUELMETTING_WIN_TIME";
	
	public static final int TITLE_ID = 108;
	
	protected DuelMettingDieCallBack dieCallBack;
	
	protected Random rnd = new Random();
	
	public void shutdown() {
		
	}
	
	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("duelmetting.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		dieCallBack = new DuelMettingDieCallBack(this);
		//注册地图管理器
		registerManager();
		//报名管理
		managerSignUp();
		//副本管理
		duelTimer.schedule(new TimerTask(){
			public void run() {
				 createInstances();
			}
		}, getScheduleTime(param.day, param.duelBeginHour,param.duelBeginMin), ONE_WEEK);
		
	}
	
	/** 比武大会报名 */
	public void signUp(Player p) throws DuelMettingException{
		if(p!=null){
			if(!canSignUp)
				throw new DuelMettingException(peony.Messages.STRING_00902);
			if(signUpIds.contains(p.id))
				throw new DuelMettingException(peony.Messages.STRING_00905);
			PlayerTransaction tx = p.newTransaction("DUELMETTING");
			if(p.bag.removeGameItemIngoreInstanceId(ITEMID_LINGPAI, 1, tx, true)==null){
				tx.rollback();
				throw new DuelMettingException(peony.Messages.STRING_01645);
			}
			tx.commit();
			signUpIds.add(p.id);
			playerIds.add(p.id);
			giveRedMedicinal(p,10);
			log.info("[DUELMETTING]SIGNUPSUC[" + p.id + "]PLAYERSIZE[" + playerIds.size() + "]");
		}
	}
	
	/**给补给红**/
	public void giveRedMedicinal(Player player,int redNum){
		if(redNum <= 0)
			return;
		int bagNum = player.bag.getGameItemCount(ITEMID_RED);
		int doptNum = player.depot.getGameItemCount(ITEMID_RED);
		int num = bagNum + doptNum;
		int giveNum = 0;
		if((num+redNum) > ITEM_NUM_RED){
			giveNum = ITEM_NUM_RED - num;
		}else{
			giveNum = redNum;
		}
		if(giveNum == 0)
			return;
		PlayerTransaction tx = player.newTransaction("DUELMETTING");
		GameItem red = ObjectAccessor.createGameItem(ITEMID_RED);
		try {
			player.bag.addGameItemComplete(red, giveNum, tx, true);
			tx.commit();
		} catch (NoEnoughSpaceException e) {
			tx.rollback();
			Server.server.getServiceRegistry().getMailService().sendSystemMailAsync(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_01646, "", 0, red, giveNum, "DUELMETTING");
		}
	}
	
	private void managerSignUp(){
		signTimer.schedule(new TimerTask(){
			public void run() {
				canSignUp = true;
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(
				peony.Messages.STRING_01647);
				log.info("[DUELMETTING]SIGNUP[BEGIG]");
			}
		}, getScheduleTime(param.day,param.signBeginHour,param.signBeginMin), ONE_WEEK);
		signTimer.schedule(new TimerTask(){
			public void run() {
				canSignUp = false;
				//乱序排列
				playerIds = randomIds(playerIds);
				recordGroup();
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(
				peony.Messages.STRING_01648);
				if(playerIds.size() <= 1){
					endInstances();
				}
				log.info("[DUELMETTING]SIGNUP[END]SIZE[" + signUpIds.size() + "]");
			}
		}, getScheduleTime(param.day, param.signEndHour,param.signEndMin), ONE_WEEK);
		if(inTime(param.day,param.signBeginHour,param.signBeginMin,param.signEndHour,param.signEndMin)){
			canSignUp = true;
		}
	}
	
	/** 创建副本 */
	private void createInstances(){
		int wantInstanceAccount = getWantInstanceAccount();
		if(wantInstanceAccount==0 && playerIds.size() == 1){
			return;
		}
		for(int i=0;i<wantInstanceAccount;i++){
			VMap map = VMapUtil.create(this, Server.server.getWorld(), param.duelMapId, Server.server.revision);
			DuelMettingInstance instance = new DuelMettingInstance(i+1, map, this);
			instances.add(instance);
			Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MAP_ADDED, map));
		}
		battleTimer = new Timer();
		battleTimer.schedule(new TimerTask(){
			public void run() {
				enterInstance();
				setInstanceInit();
			}
		}, 0, param.periodTime);
		if(playerIds.size() > 1){
			battleTimer.schedule(new TimerTask(){
				public void run(){
					leaveInstance();
				}
			},param.duelTime,param.periodTime);
		}
	}
	
	//角色传送完毕  设置副本状态
	public void setInstanceInit(){
		for(DuelMettingInstance instance:instances){
			instance.state = DuelMettingInstance.STATE_INIT;
		}
	}
	
	/**一轮时间到时 传出副本 重新记录playerIds**/
	private void leaveInstance(){
		for(DuelMettingInstance instance:instances){
			if(instance.players.size()==2){
				Player player1 = ObjectAccessor.getPlayer(instance.players.get(0));
				Player player2 = ObjectAccessor.getPlayer(instance.players.get(1));
				if(player1 != null&&player2 != null){
					if(player1.hp > player2.hp){
						giveRedMedicinal(player1, ITEM_NUM_REWARD);
						playerIds.add(player1.id);
						loseIds.add(player2.id);
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player1.id, peony.Messages.STRING_00483);
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player2.id, peony.Messages.STRING_00482);
					}else{
						giveRedMedicinal(player2, ITEM_NUM_REWARD);
						playerIds.add(player2.id);
						loseIds.add(player1.id);
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player2.id, peony.Messages.STRING_00483);
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player1.id, peony.Messages.STRING_00482);
					}
				}
			}else if(instance.players.size()==1){
				Player player = ObjectAccessor.getPlayer(instance.players.get(0));
				giveRedMedicinal(player, ITEM_NUM_REWARD);
				playerIds.add(player.id);
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, peony.Messages.STRING_00483);
			}
			instance.transPlayers();
			instance.map.instanceid2objects.clear();
		}
		
		//玩家全部进入地图 或 冠军产生时 停止传送
		if(playerIds.size() == 1){
			endInstances();
			return;
		}
		//记录分组
		recordGroup(); 
		Server.server.getServiceRegistry().getChatService().sendWorldMessage(
		peony.Messages.STRING_01649);
		log.info("[DUELMETTING]ENDONEROUND[LEAVEPLAYER]" + playerIds.size());
	}
	
	/** 将玩家传送进副本 */
	protected void enterInstance(){
		groups.clear();
		//如果只有一个人 直接产生冠军
		if(playerIds.size() == 0 || playerIds.size() == 1){
			endInstances();
			return;
		}
		boolean switchDoor = true;
		boolean needRecord = false;
		int lastPlayerId = 0;
		int num = playerIds.size();
		if(playerIds.size()%2==1){
			num--;
			needRecord = true;
			lastPlayerId = playerIds.get(playerIds.size() - 1);
		}
		for(int i = 0;i < num;i++){
			int id = playerIds.get(i);
			Player player = ObjectAccessor.getPlayer(id);
			if(player!=null){
				try {
					if(switchDoor){
						player.goMap(param.duelMapId,param.player1x, param.player1y);
						switchDoor = false;
					}else{
						player.goMap(param.duelMapId,param.player2x, param.player2y);
						switchDoor = true;
					}
					log.info("[DUELMETTING]ENTER[" + player.id + "]");
				} catch (VMapException e) {
					e.printStackTrace();
				}
			}
		}
		playerIds.clear();
		if(needRecord){
			playerIds.add(lastPlayerId);
		}
	}
	
	/**记录分组情况  方便查看对手**/
	private void recordGroup(){
		int size = playerIds.size();
		if(size <= 1){
			return;
		}
		if(size%2==1){
			size--;
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(playerIds.get(size), peony.Messages.STRING_01650);
		}
		for(int i = 0;i < size;i++){
			if(i%2==0){
				groups.put(playerIds.get(i), playerIds.get(i+1));
			}else{
				groups.put(playerIds.get(i), playerIds.get(i-1));
			}
		}
	}
	
	/** 根据人数确定开启副本的数量 */
	private int getWantInstanceAccount(){
		if(playerIds.size()==0)
			return 0;
		int account = playerIds.size()/MAX_PLAYER;
		int residue = playerIds.size()%MAX_PLAYER;
		if(account==0 && residue>0)
			return 0;
		if(account>0 && residue==0)
			return account;
		return account;
	}
	
	/**第一次执行时间**/
	private Date getScheduleTime(int day,int hour,int min){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.DAY_OF_WEEK, day);
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_YEAR, 7);
			return cal1.getTime();
		}
		return cal1.getTime();
	}
	
	/**是否在指定时间段内**/
	private boolean inTime(int day,int sHour, int sMin, int eHour, int eMin){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(new Date());
		cal1.set(Calendar.DAY_OF_WEEK, day);
		cal1.set(Calendar.HOUR_OF_DAY, sHour);
		cal1.set(Calendar.MINUTE, sMin);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(new Date());
		cal2.set(Calendar.DAY_OF_WEEK, day);
		cal2.set(Calendar.HOUR_OF_DAY, eHour);
		cal2.set(Calendar.MINUTE, eMin);
		return cal.after(cal1) && cal.before(cal2);
	}
	
	private void parse(Document doc){
		Element root = doc.getRootElement();
		if(root != null){
			Element duelmap = root.element("duelmap");
			int duelMapId = Integer.parseInt(duelmap.attributeValue("mapId"));
			int player1x = Integer.parseInt(duelmap.attributeValue("player1x"));
			int player1y = Integer.parseInt(duelmap.attributeValue("player1y"));
			int player2x = Integer.parseInt(duelmap.attributeValue("player2x"));
			int player2y = Integer.parseInt(duelmap.attributeValue("player2y"));
			Element outmapwei = root.element("outmapwei");
			int weiOutMapId = Integer.parseInt(outmapwei.attributeValue("outMapId"));
			int weiOutx = Integer.parseInt(outmapwei.attributeValue("outx"));
			int weiOuty = Integer.parseInt(outmapwei.attributeValue("outy"));
			Element outmapshu = root.element("outmapshu");
			int shuOutMapId = Integer.parseInt(outmapshu.attributeValue("outMapId"));
			int shuOutx = Integer.parseInt(outmapshu.attributeValue("outx"));
			int shuOuty = Integer.parseInt(outmapshu.attributeValue("outy"));
			Element outmapwu = root.element("outmapwu");
			int wuOutMapId = Integer.parseInt(outmapwu.attributeValue("outMapId"));
			int wuOutx = Integer.parseInt(outmapwu.attributeValue("outx"));
			int wuOuty = Integer.parseInt(outmapwu.attributeValue("outy"));
			Element period = root.element("period");
			int periodTime = Integer.parseInt(period.attributeValue("periodTime"));
			int duelTime = Integer.parseInt(period.attributeValue("duelTime"));
			Element sign = period.element("sign");
			int day = Integer.parseInt(sign.attributeValue("day"));
			int signBeginHour = Integer.parseInt(sign.attributeValue("signBeginHour"));
			int signBeginMin = Integer.parseInt(sign.attributeValue("signBeginMin"));
			int signEndHour = Integer.parseInt(sign.attributeValue("signEndHour"));
			int signEndMin = Integer.parseInt(sign.attributeValue("signEndMin"));
			Element duel = period.element("duel");
			int duelBeginHour = Integer.parseInt(duel.attributeValue("duelBeginHour"));
			int duelBeginMin = Integer.parseInt(duel.attributeValue("duelBeginMin"));
			param = new DuelMettingParam(day,signBeginHour,signBeginMin,signEndHour,signEndMin,duelBeginHour,
					duelBeginMin,periodTime,duelTime,duelMapId,player1x,player1y,player2x,player2y,
					weiOutMapId,weiOutx,weiOuty,shuOutMapId,shuOutx,shuOuty,wuOutMapId,
					wuOutx,wuOuty);
		}
	}
	
	/** 注册地图管理器 */
	protected void registerManager(){
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(param.duelMapId, this);
		Server.server.getEventManager().registerListener(this);
	}
	
	/**是否在报名列表中**/
	public boolean isInSign(Player player){
		if(signUpIds.contains(player.id)){
			return true;
		}
		return false;
	}
	
	/**是否在剩余选手列表中**/
	public boolean isInPlayers(Player player){
		if(playerIds.contains(player.id)){
			return true;
		}
		return false;
	}
	
	public boolean isInLosePlayer(Player player){
		if(loseIds.contains(player.id)){
			return true;
		}
		return false;
	}
	
	/**对手id**/
	public int getGroupPlayerId(Player player) throws DuelMettingException{
		if(!isInSign(player)){
			throw new DuelMettingException(peony.Messages.STRING_01651);
		}
		if(isInLosePlayer(player)){
			throw new DuelMettingException(peony.Messages.STRING_01652);
		}
		if(!isInPlayers(player)){
			throw new DuelMettingException(peony.Messages.STRING_01653);
		}
		if(groups.containsKey(player.id)){
			return groups.get(player.id);
		}else{
			throw new DuelMettingException(peony.Messages.STRING_01653);
		}
	}
	
	/**获取复活点坐标**/
	public int[] getRevivePoint(Player player){
		int[] point = null;
		if(player.faction == GameObject.FACTION_WEI){
			point = new int[]{param.weiOutMapId,param.weiOutx,param.weiOuty};
		}else if(player.faction == GameObject.FACTION_SHU){
			point = new int[]{param.shuOutMapId,param.shuOutx,param.shuOuty};
		}else if(player.faction == GameObject.FACTION_WU){
			point = new int[]{param.wuOutMapId,param.wuOutx,param.wuOuty};
		}
		return point;
	}
	
	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if(check){
			int[] point = getRevivePoint(player);
			return Server.server.getWorld().addPlayerToMap(player, point[0], point[1], point[2], true);
		}else{
			DuelMettingInstance instance = null;
			if(!signUpIds.contains(player.id))
				return null;
			instance = getUsableDuelInstance();
			if(instance!=null){
				if(player.party!=null){
					player.party.leave(player.id);
				}
				player.removeFromMap();
				instance.addPlayer(player);
				instance.map.addPlayer(player, x, y);
				log.info("______PLAYEID" + player.id + "INSTANCEID[" + instance.id + "]");
				Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_JOIN_BIWU,player));
				return instance.map;
			}
			return null;
		}
	}
	
	/**乱序排序**/
	private List<Integer> randomIds(List<Integer> playerIds){
		int size = playerIds.size();
		for(int i = 0;i < size;i++){
			int index = rnd.nextInt(size);
			int tempValue = playerIds.get(index);
			playerIds.set(index,playerIds.get(i));
			playerIds.set(i, tempValue);
		}
		return playerIds;
	}
	
	/** 获取可进入的DuelMettingInstance,按ID优先顺序 */
	private DuelMettingInstance getUsableDuelInstance(){
		for(int i=0;i<instances.size();i++){
			DuelMettingInstance ins = getInstanceById(i+1);
			if(ins.players.size()<MAX_PLAYER){
				return ins;
			}
		}
		return null;
	}
	
	/** 根据id获取Instance */
	protected DuelMettingInstance getInstanceById(int id){
		for(DuelMettingInstance instance : instances){
			if(instance.id==id){
				return instance;
			}
		}
		return null;
	}
	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return dieCallBack;
	}

	public void mapChanged(GameMapDefinition mapDef) {
		
	}

	public MoveCallback moveCallback() {
		return null;
	}

	public void outPrison(Player p) {
		
	}

	public void removeFromMap(Player player) {
		for(DuelMettingInstance instance:instances){
			instance.removePlayer(player);
		}
		
	}

	public void update(int diff) {
		/**检测是否有冠军产生**/
		for(DuelMettingInstance instance : instances){
			instance.update(diff);
		}
	}
	
	private void endInstances(){
		int winerId = 0 ;
		if(playerIds.size() == 1){
			winerId = playerIds.get(0);
			//奖励冠军
			Player player = ObjectAccessor.getPlayer(winerId);
			if(player!=null){
				player.pool.setLong(DUELMETTING_WIN_TIME, System.currentTimeMillis());
				PlayerTransaction tx = player.newTransaction("DUELMETTING");
				GameItem item = ObjectAccessor.createGameItem(ITEMID_REWARD);
				try{
					player.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
				}catch(Exception e){
					tx.rollback();
					MailService mailService = Server.server.getServiceRegistry().getMailService();
					mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_01646, "", 0, item, 1, "DUELMETTING");
				}
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(
						MessageFormat.format(peony.Messages.STRING_01654, player.name));
				log.info("[DUELMETTING]WINER[" + player.id + "]");
			}else{
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(
						peony.Messages.STRING_01655);
			}
		}else{
			Server.server.getServiceRegistry().getChatService().sendWorldMessage(
			peony.Messages.STRING_01656);
		}
		//初始化数据
		playerIds.clear();
		signUpIds.clear();
		battleTimer.cancel();
		instances.clear();
		loseIds.clear();
	}
	
	class DuelMettingParam{
		//比武报名时间
		int day;
		int signBeginHour;
		int signBeginMin;
		int signEndHour;
		int signEndMin;
		//比武开始时间
		int duelBeginHour;
		int duelBeginMin;
		//每场时间
		int periodTime;
		//战斗持续最长时间
		int duelTime;
		//比武地图信息
		int duelMapId;
		int player1x;
		int player1y;
		int player2x;
		int player2y;
		//传出地图信息
		int weiOutMapId;
		int weiOutx;
		int weiOuty;
		int shuOutMapId;
		int shuOutx;
		int shuOuty;
		int wuOutMapId;
		int wuOutx;
		int wuOuty;
		
		public DuelMettingParam(int day,int signBeginHour,int signBeginMin,int signEndHour,int signEndMin,int duelBeginHour,
				int duelBeginMin,int periodTime,int duelTime,int duelMapId,int player1x,int player1y,int player2x,int player2y,
				int weiOutMapId,int weiOutx,int weiOuty,int shuOutMapId,int shuOutx,int shuOuty,int wuOutMapId,
				int wuOutx,int wuOuty){
			this.day = day;
			this.signBeginHour = signBeginHour;
			this.signBeginMin = signBeginMin;
			this.signEndHour = signEndHour;
			this.signEndMin = signEndMin;
			this.duelBeginHour = duelBeginHour;
			this.duelBeginMin = duelBeginMin;
			this.periodTime = periodTime;
			this.duelTime = duelTime;
			this.duelMapId = duelMapId;
			this.player1x = player1x;
			this.player1y = player1y;
			this.player2x = player2x;
			this.player2y = player2y;
			this.weiOutMapId = weiOutMapId;
			this.weiOutx = weiOutx;
			this.weiOuty = weiOuty;
			this.shuOutMapId = shuOutMapId;
			this.shuOutx = shuOutx;
			this.shuOuty = shuOuty;
			this.wuOutMapId = wuOutMapId;
			this.wuOutx = wuOutx;
			this.wuOuty = wuOuty;
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LOADED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_LOADED:
			processPlayerLoad((Player)event.param1);
			break;
		}
	}
		
	private void processPlayerLoad(Player p){
		if(p!=null){
			if(p.pool.getLong(DUELMETTING_WIN_TIME, 0) != 0 && (System.currentTimeMillis() - p.pool.getLong(DUELMETTING_WIN_TIME,0)) > ONE_WEEK ){
				if(p.titles.hasTitle(TITLE_ID)){
					p.titles.removeTitle(TITLE_ID);
					MailService mailService = Server.server.getServiceRegistry().getMailService();
					mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, "\"天下第一\"称号", "虽然您的《天下第一》称号已到期，已获取的称号会在您重新登录游戏的时候删除掉，但是英雄仍乃武林至尊，何不下次继续挑战，所谓长江后浪推前浪，后浪拍死在沙滩上！", 0, 
							null, 1, null);
				}
			}
		}
	}	
}
