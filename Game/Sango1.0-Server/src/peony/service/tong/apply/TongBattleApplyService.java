package peony.service.tong.apply;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameObject;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.chat.ChatService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.tong.Tong;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.battle.TongBattleVMapManager;
import peony.util.TimeUtil;

public class TongBattleApplyService implements Service, ServiceEventListener {

	private static final Logger log = Logger.getLogger(TongBattleApplyService.class);
	
	/** 申请城战的军团集合 key:城市ID value:军团集合 */
	public Map<Integer, List<TongBattleApply>> applys = new HashMap<Integer, List<TongBattleApply>>();
	
	/** 各个城市城战对决双方 */
	public Map<Integer, TongBattleApply[]> battles = new HashMap<Integer, TongBattleApply[]>();
	
	/** 各个城市的防守方 */
	public Map<Integer, TongBattleApply> owners = new HashMap<Integer, TongBattleApply>();
	
	public Map<Integer, Boolean> canApply = new HashMap<Integer, Boolean>();
	public Map<Integer, TimeController> controllers = new HashMap<Integer, TimeController>();
	
	public static long ONEWEEK = 7 * 24 * 3600 * 1000L;
	public static int MIN_BATTLEAPPLY_MONEY = 300000;
	
	public static int currentCity; //当前被争夺的城市ID,城战结束后置为0
	
	private static String[] WEEKNAME = {peony.Messages.STRING_00634,peony.Messages.STRING_00635,peony.Messages.STRING_00636,peony.Messages.STRING_00637,peony.Messages.STRING_00638,peony.Messages.STRING_00639,peony.Messages.STRING_00640};
	
	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
			.findFile("tongbattleapply.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Calendar cal = Calendar.getInstance();
		for (final int mapId : controllers.keySet()) {
			TimeController controller = controllers.get(mapId);
			if (controller != null) {
				int weekDay = controller.weekDay;
				int beginTime = controller.beginTime;
				int endTime = controller.endTime;
				if (in(cal, weekDay, beginTime, endTime)) {
					canApply.put(mapId, true);
				}
				Server.server.scheduExec.scheduleAtFixedRate(new Runnable() {
					public void run() {
						canApply.put(mapId, true);
					}
				}, TimeUtil.getScheduleTimeMills(new Date(), weekDay, beginTime, 0), ONEWEEK, TimeUnit.MILLISECONDS);
				Server.server.scheduExec.scheduleAtFixedRate(new Runnable() {
					public void run() {
						canApply.put(mapId, false);
						currentCity = mapId;
						//最后定夺
						decide();
					}
				}, TimeUtil.getScheduleTimeMills(new Date(), weekDay, endTime, 0), ONEWEEK, TimeUnit.MILLISECONDS);
			}
		}
		loadApplys();
		processOldMapApplys();
		Server.server.getEventManager().registerListener(this);
	}
	
	@SuppressWarnings("unchecked")
	protected void loadApplys(){
		TongBattleApplyDao dao = Server.server.getServiceRegistry().getDbService().tongBattleApplyDao;
		List<TongBattleApply> list = dao.list("from TongBattleApply");
		
		Iterator<TongBattleApply> it = list.iterator();
		while(it.hasNext()){
			TongBattleApply a = (TongBattleApply)it.next();
			if(a.state==0 && !canApply(a.mapId)){
				dao.makeTransient(a);
				it.remove();
			}
		}
		
		for(TongBattleApply apply : list){
			if(apply.state==0){
				List<TongBattleApply> l = applys.get(apply.mapId);
				if(l==null){
					l = new ArrayList<TongBattleApply>();
					applys.put(apply.mapId, l);
				}
				l.add(apply);
			}else{
				owners.put(apply.mapId, apply);
			}
			if(apply.getTong()==null){
				int tongId = apply.tongId;
				log.info("[LOADTONG]loadApplys");
				Server.server.getServiceRegistry().getTongService().loadTong(tongId);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		if(list!=null && list.size()>0){
			for(Element element : list){
				int mapId = Integer.parseInt(element.attributeValue("id"));
				int beginTime = Integer.parseInt(element.attributeValue("beginTime"));
				int endTime = Integer.parseInt(element.attributeValue("endTime"));
				int weekDay = Integer.parseInt(element.attributeValue("weekDay"));
				String name = element.attributeValue("name");
				TimeController controller = new TimeController(beginTime, endTime, weekDay, name);
				controllers.put(mapId, controller);
			}
		}
	}
	
	protected void processOldMapApplys(){
		applys.remove(864);
		battles.remove(864);
		owners.remove(864);
	}
	
	public void tagTongBattle(Player p, int playerId) throws TongBattleException{
		TongService tongService = Server.server.getServiceRegistry().getTongService();
		Tong tong = tongService.getPlayerTong(p.id,false);
		TongMember tm2 = tongService.getPlayerInfo(p.id);
		if(tong!=null){
			TongMember tm = tongService.getPlayerInfo(playerId);
			if(tm==null){
				for(TongMember t : tong.members){
					if(t.id == playerId){
						tm = t;
					}
				}
			}
			if(tm2!=null&&tm2.duty<=TongService.NORMAL){
				throw new TongBattleException(peony.Messages.STRING_00143);
			}
			if(tm.actor!=null && tm.actor.level<TongBattleVMapManager.MINLEVEL)
				throw new TongBattleException(MessageFormat.format(peony.Messages.STRING_00641, TongBattleVMapManager.MINLEVEL));
			tm.battleTag = 1;
			for(TongMember t : tong.members){
				if(t.id == playerId){
					t.battleTag = 1;
				}
			}
			Server.server.getServiceRegistry().getDbService().tongMemberDAO.updateEntity(tm);
		}
	}
	
	public boolean isPreBattleSide(int tongId){
		for(List<TongBattleApply> applyL : applys.values()){
			for(TongBattleApply apply : applyL){
				if(apply.tongId==tongId){
					return true;
				}
			}
		}
		TongBattleVMapManager manager = Server.server.getServiceRegistry().getTongBattleVMapManager();
		Calendar cal = Calendar.getInstance();
		if(getApplyByTongId(tongId)!=null && manager.in(cal, 0, 0, 20, 0)){
			return true;
		}
		return false;
	}
	
	public int getApplysAccount(int mapId){
		if(applys.get(mapId)==null)
			return 0;
		return applys.get(mapId).size();
	}
	
	public boolean isWinner(int tongId){
		for(TongBattleApply apply : owners.values()){
			if(apply.tongId==tongId){
				return true;
			}
		}
		return false;
	}
	
	public String getWinner(int mapId){
		TongBattleApply apply = owners.get(mapId);
		if(apply==null){
			return "";
		}else{
			Tong tong = Server.server.getServiceRegistry().getTongService().getTong(apply.tongId);
			String factionName = "";
			try {
				int faction = 0;
				for(int i=0;i<tong.members.size();i++){
					faction = tong.members.get(i).actor.faction;
					if(faction>0)
						break;
				}
				factionName = GameObject.getFactionName(faction);
			} catch (Exception e) {
			}
			return apply.tongName + (factionName=="" ? "" : ("<c00ff00>(" + factionName + ")</c>"));
		}
	}
	
	public int getWinnerMapId(int tongId){
		for(TongBattleApply apply : owners.values()){
			if(apply.tongId==tongId){
				return apply.mapId;
			}
		}
		return -1;
	}
	
	public Tong getWinnerTong(int mapId){
		TongBattleApply apply = owners.get(mapId);
		if(apply==null){
			return null;
		}else{
			return apply.getTong();
		}
	}
	
	public void abandon(Player p, int tongId) throws TongBattleException{
		if(isWinner(tongId)){
			if(p.map.getId()!=getWinnerMapId(tongId)){
				throw new TongBattleException(peony.Messages.STRING_00144);
			}
			int mapId = 0;
			for(int id : owners.keySet()){
				if(tongId==owners.get(id).tongId){
					mapId = id;
				}
			}
			if(isPreBattleSide(tongId)){
				throw new TongBattleException(peony.Messages.STRING_00642);
			}
			owners.remove(mapId);
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getTong(tongId);
			Server.server.getServiceRegistry().getChatService().sendWorldMessage(
					MessageFormat.format(peony.Messages.STRING_00643, tong.name, getMapName(mapId)));
			Server.server.getServiceRegistry().getChatService().sendGuildSystemMessage(
					MessageFormat.format(peony.Messages.STRING_00644, p.name, getMapName(mapId)), tongId);
		}else{
			throw new TongBattleException(peony.Messages.STRING_00645);
		}
	}
	
	public String getCanApplyTime(int mapId){
		TimeController controller = controllers.get(mapId);
		if(controller==null)
			return "";
		return MessageFormat.format(peony.Messages.STRING_00646, controller.name,
				WEEKNAME[(controller.weekDay-1)]+" <cff0000>"
				+controller.beginTime+":00-"+controller.endTime+":00</c>",controller.name,
				WEEKNAME[(controller.weekDay-1)]+" <cff0000>"+"19:00</c>");
	}

	public void apply(Player p, int mapId, int money) throws TongBattleApplyException {
		if (p != null) {
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id,false);
			if (tong == null) {
				throw new TongBattleApplyException(peony.Messages.STRING_00647);
			} else if (tongService.getPlayerInfo(p.id).duty != TongService.CHAIRMAN) {
				throw new TongBattleApplyException(peony.Messages.STRING_00648);
			}
			if (hasApplyed(tong.id)) {
				throw new TongBattleApplyException(peony.Messages.STRING_00649);
			}
			if(isWinner(tong.id)){
				throw new TongBattleApplyException(peony.Messages.STRING_00650);
			}
			if (canApply.get(mapId)==null || !canApply.get(mapId)) {
				throw new TongBattleApplyException(peony.Messages.STRING_00651);
			}
			if(money<MIN_BATTLEAPPLY_MONEY){
				throw new TongBattleApplyException(peony.Messages.STRING_00652);
			}
			TongBattleApply battleApply = new TongBattleApply(tong.id, mapId, p.faction);
			try {
				tong.decMoney(money);
				battleApply.addMoney(money);
			} catch (NoEnoughValueException e) {
				throw new TongBattleApplyException(peony.Messages.STRING_00653);
			}
			addTongBattleApply(mapId, battleApply);
			LogUtil.logTongBattleApply(p, tong, money);
		}
	}
	
	public void tongBattleBid(Player p, int mapId, int money) throws TongBattleApplyException{
		if(p!=null){
			if(money<=0){
				throw new TongBattleApplyException(peony.Messages.STRING_00654);
			}
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id,false);
			if (tong == null) {
				throw new TongBattleApplyException(peony.Messages.STRING_00647);
			} else if (tongService.getPlayerInfo(p.id).duty != TongService.CHAIRMAN) {
				throw new TongBattleApplyException(peony.Messages.STRING_00648);
			}
			TongBattleApply battleApply = getTongBattleApply(mapId,tong.id);
			if(battleApply==null){
				throw new TongBattleApplyException(peony.Messages.STRING_00655);
			}
			boolean can = canApply.get(mapId);
			if (!can) {
				throw new TongBattleApplyException(peony.Messages.STRING_00656);
			}
			try {
				tong.decMoney(money);
				battleApply.addMoney(money);
			} catch (NoEnoughValueException e) {
				throw new TongBattleApplyException(peony.Messages.STRING_00653);
			}
			LogUtil.logTongBattleBid(p, tong, money);
		}
	}
	
	public void addTongBattleApply(int mapId, TongBattleApply apply){
		List<TongBattleApply> list = applys.get(mapId);
		if(list==null){
			list = new ArrayList<TongBattleApply>();
			applys.put(mapId, list);
		}
		list.add(apply);
	}
	
	public TongBattleApply getTongBattleApply(int mapId, int tongId){
		List<TongBattleApply> list = applys.get(mapId);
		if(list==null)
			return null;
		for(TongBattleApply apply : list){
			if(apply.tongId==tongId){
				return apply;
			}
		}
		return null;
	}

	public boolean hasApplyed(int tongId) {
		for (int mapId : applys.keySet()) {
			List<TongBattleApply> applyList = applys.get(mapId);
			for (TongBattleApply apply : applyList) {
				if (apply.tongId == tongId)
					return true;
			}
		}
		return false;
	}
	
	public TongBattleApply getApplyByTongId(int tongId){
		for (TongBattleApply[] applyArr : battles.values()) {
			if(applyArr!=null){
				for(TongBattleApply apply : applyArr){
					if (apply.tongId == tongId)
						return apply;
				}
			}
		}
		for (TongBattleApply apply : owners.values()) {
			if (apply.tongId == tongId)
				return apply;
		}
		return null;
	}

	public boolean canApply(int mapId) {
		return canApply.get(mapId) == null ? false : canApply.get(mapId);
	}

	public List<TongBattleApply> applyList(int mapId) {
		bubbleApplys();
		return applys.get(mapId);
	}

	private void bubbleApplys() {
		for (int mapId : applys.keySet()) {
			List<TongBattleApply> list = applys.get(mapId);
			if (list != null && list.size() > 0) {
				TongBattleApply[] applyList = new TongBattleApply[list.size()];
				list.toArray(applyList);
				for (int i = 0; i < applyList.length; i++) {
					for (int j = i + 1; j < applyList.length; j++) {
						if (applyList[i].money < applyList[j].money) {
							TongBattleApply temp;
							temp = applyList[i];
							applyList[i] = applyList[j];
							applyList[j] = temp;
						}
					}
				}
				list.clear();
				for(TongBattleApply apply : applyList){
					list.add(apply);
				}
			}
		}
	}

	private void decide() {
		bubbleApplys();
		for(int mapId : applys.keySet()){
			List<TongBattleApply> list = applys.get(mapId);
			if(list!=null && list.size()>0){
				TongBattleApply[] battles = null;
				if(list.size()==1){
					if(owners.get(mapId)==null){
						//占领城池
						preWin(mapId, list.get(0),0);
						return;
					}else{
						battles = new TongBattleApply[1];
						battles[0] = list.get(0);
						this.battles.put(mapId, battles);
						notifyBattles(battles,owners.get(mapId));
					}
				}else{
					battles = new TongBattleApply[2];
					battles[0] = list.get(0);
					battles[1] = list.get(1);
					this.battles.put(mapId, battles);
					notifyBattles(battles,owners.get(mapId));
				}
				refreshApplys(mapId, battles);
				for(TongBattleApply apply : battles){
					notifyBattle(apply);
				}
				LogUtil.logTongBattleApplyDecide(battles, mapId);
			}
		}
	}
	
	protected void notifyBattles(TongBattleApply[] apps, TongBattleApply owner){
		ChatService service = Server.server.getServiceRegistry().getChatService();
		for(TongBattleApply apply : apps){
			int tongId = apply.tongId;
			service.sendGuildSystemMessage(MessageFormat.format(
					peony.Messages.STRING_00657, apply.tongName, getMapName(apply.mapId)), tongId);
			service.sendFactionSystemMessage(apply.faction
					, MessageFormat.format(peony.Messages.STRING_00657, apply.tongName, getMapName(apply.mapId)));
		}
		if(owner!=null){
			if(apps.length==2){
				service.sendGuildSystemMessage(MessageFormat.format(
						peony.Messages.STRING_00658, apps[0].tongName, apps[1].tongName), owner.tongId);
			}else if(apps.length==1){
				service.sendGuildSystemMessage(MessageFormat.format(
						peony.Messages.STRING_00659, apps[0].tongName), owner.tongId);
			}
		}
	}
	
	/** 申请攻城军团直接占领城市 */
	public void preWin(int mapId, TongBattleApply apply, int type){
		apply.state = 1;
		owners.put(apply.mapId, apply);
		apply.getTong().pool.setInt(Tong.PROPERTY_TONGBATTLE_WIN, apply.getTong().pool.getInt(Tong.PROPERTY_TONGBATTLE_WIN,0)+1);
		apply.getTong().modify = true;
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		if(type==0){
			chatService.sendGuildSystemMessage(MessageFormat.format(
					peony.Messages.STRING_00660, apply.tongName,getMapName(mapId)), apply.tongId);
			chatService.sendFactionSystemMessage(apply.faction, MessageFormat.format(
					peony.Messages.STRING_00660, apply.tongName,getMapName(mapId)));
		}else if(type==1){
			chatService.sendGuildSystemMessage(MessageFormat.format(
					peony.Messages.STRING_00661, apply.tongName,getMapName(mapId)), apply.tongId);
			chatService.sendFactionSystemMessage(apply.faction, MessageFormat.format(
					peony.Messages.STRING_00661, apply.tongName,getMapName(mapId)));
		}
		if(applys.get(mapId)!=null){
			applys.get(mapId).clear();
		}
		apply.getTong().taxRate = 0.05f;
		apply.getTong().modify = true;
	}
	
	private void notifyBattle(final TongBattleApply apply){
		Server.server.scheduExec.schedule(new Runnable(){
			public void run() {
				Server.server.getServiceRegistry().getChatService().sendGuildSystemMessage(MessageFormat.format(
						peony.Messages.STRING_00662, apply.tongName,getMapName(apply.mapId)), apply.tongId);
			}
		}, 55*60*1000l, TimeUnit.MILLISECONDS);
		final Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run() {
				Server.server.getServiceRegistry().getChatService().sendGuildSystemMessage(
						MessageFormat.format(peony.Messages.STRING_00663, getMapName(apply.mapId)), apply.tongId);
			}
		}, 56*60*1000L, 2*60*1000L);
		new Timer().schedule(new TimerTask(){
			public void run() {
				timer.cancel();
			}
		}, 60*60*1000L);
	}
	
	public String getMapName(int mapId){
		TimeController controller = controllers.get(mapId);
		return controller.name;
	}
	
	public void refreshApplys(int mapId, TongBattleApply[] battles){
		clearApplys(battles);
		List<TongBattleApply> list = applys.get(mapId);
		if(list!=null){
			for(TongBattleApply apply : battles){
				list.add(apply);
			}
		}
	}
	
	public void clearApplys(TongBattleApply[] battles){
		for(int mapId : applys.keySet()){
			List<TongBattleApply> list = applys.get(mapId);
			Iterator<TongBattleApply> it = list.iterator();
			while(it.hasNext()){
				TongBattleApply a = (TongBattleApply)it.next();
				if(!has(battles, a)){
					// 返还金钱,扣除10%的手续费
					int money = a.money;
					money = (int) (a.money * 0.9f);
					Tong tong = a.getTong();
					tong.addMoney(money);
					ChatService chatService = Server.server.getServiceRegistry().getChatService();
					chatService.sendGuildSystemMessage(peony.Messages.STRING_00664, a.tongId);
					LogUtil.logTongBattleReturnMoney(a, money);
				}
				it.remove();
			}
//			if(list!=null)
//				list.clear();
		}
	}
	
	protected boolean has(TongBattleApply[] battles, TongBattleApply a){
		for(TongBattleApply app : battles){
			if(a.tongId==app.tongId){
				return true;
			}
		}
		return false;
	}
	
	public void clearApplys(int signMapId){
		if(applys.get(signMapId)!=null){
			applys.get(signMapId).clear();
		}
	}
	
	// 用于测试
	@SuppressWarnings("unused")
	private Date getScheduleTime(Calendar cal, int weekDay, int hour, int min) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.DAY_OF_WEEK, ((weekDay+1)==8 ? 1 : (weekDay+1)));
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.WEEK_OF_MONTH, 1);
			return cal1.getTime();
		}
		return cal1.getTime();
	}

	private boolean in(Calendar cal, int weekDay, int beginTime, int endTime) {
		if (cal.get(Calendar.DAY_OF_WEEK) == ((weekDay+1)==8 ? 1 : (weekDay+1))) {
			return (cal.get(Calendar.HOUR_OF_DAY) >= beginTime)
					&& (cal.get(Calendar.HOUR_OF_DAY) < endTime);
		}
		return false;
	}

	public void shutdown() {
		TongBattleApplyDao dao = Server.server.getServiceRegistry().getDbService().tongBattleApplyDao;
		dao.delete("delete from TongBattleApply");
		for(int mapId : applys.keySet()){
			List<TongBattleApply> applyList = applys.get(mapId);
			for(TongBattleApply app : applyList){
				dao.newEntity(app);
			}
		}
		for(TongBattleApply apply : owners.values()){
			dao.newEntity(apply);
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_MAP_PLAYER_ADDED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
				notifyEnterTongOccupy((VMap)event.param1, (Player)event.param2);
				break;
		}
	}
	
	protected void notifyEnterTongOccupy(VMap map, Player player){
		Tong winner = getWinnerTong(map.getId());
		if(winner!=null){
			String factionName = "";
			try {
				int faction = 0;
				for(int i=0;i<winner.members.size();i++){
					faction = winner.members.get(i).actor.faction;
					if(faction>0)
						break;
				}
				factionName = GameObject.getFactionName(faction);
			} catch (Exception e) {
			}
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			chatService.sendPrivateShout(player.id, 0xff4700, 11000, player.faction, MessageFormat.format(
					peony.Messages.STRING_00665, winner.name, factionName=="" ? "" : ("("+factionName+")")));
		}
	}

}

class TimeController {

	public int weekDay;
	public int beginTime;
	public int endTime;
	public String name;

	public TimeController(int beginTime, int endTime, int weekDay, String name) {
		super();
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.weekDay = weekDay;
		this.name = name;
	}

}
