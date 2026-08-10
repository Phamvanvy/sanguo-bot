package peony.game.beautyparade;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.Actor;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TitleUtil;
import peony.game.mail.MailService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.friend.RelationList;
import peony.util.StringUtil;

public class BeautyParadeService implements Service, ServiceEventListener {
	
	public Set<Beauty> beautys = new HashSet<Beauty>(); // 参选人集合
	public Set<VotePlayer> votePlayers = new HashSet<VotePlayer>(); // 投票人集合
	public Set<VoteType> voteTypes = new HashSet<VoteType>(); // 所有的投票方式
	private Condition signCondition = new Condition(); // 报名条件
	public boolean canSignUp; // 报名标志
	public boolean canVote;  // 投票标志
	private Time signTime; // 报名时间信息
	private Time voteTime; // 投票时间信息
	private Time endTime; // 选举结束时间信息
	private Timer timer = new Timer();
	private long ONEWEEK = 7 * 24 * 3600 * 1000L; // 一周
	private static int[] titles = {865,866,867,868,868,868,868,868,868,868,869}; // 称号物品
	private static int[] titleIds = {56,57,58,59,59,59,59,59,59,59,60};

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
			.findFile("beautyparade.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.getEventManager().registerListener(this);
		loadBeautys();
		loadVotePlayers();
		processNotify();
		processTimer();
	}
	
	private void loadBeautys(){
		BeautySignDao dao = Server.server.getServiceRegistry().getDbService().beautySignDao;
		List<Beauty> list = dao.getBeautys();
		for(Beauty bt : list){
			this.beautys.add(bt);
		}
	}
	
	private void loadVotePlayers(){
		BeautyVoteDao dao = Server.server.getServiceRegistry().getDbService().beautyVoteDao;
		List<VotePlayer> list = dao.getVotePlayers();
		for(VotePlayer vp : list){
			this.votePlayers.add(vp);
		}
	}
	
	private void processNotify(){
		timer.schedule(new TimerTask(){
			public void run() {
				Server.server.getServiceRegistry().getChatService().sendWorldMessage("每周一次的三國選美大賽就要開始了,請大家到各國主城選美使者處報名參加.");
			}
		}, getScheduleTime(signTime.startWeek, signTime.startHour, signTime.startMin), ONEWEEK);
	}
	
	private void processTimer(){
		timer.schedule(new TimerTask(){
			public void run() {
				canSignUp = true;
				clear();
			}
		}, getScheduleTime(signTime.startWeek, signTime.startHour, signTime.startMin), ONEWEEK);
		timer.schedule(new TimerTask(){
			public void run() {
				canSignUp = false;
			}
		}, getScheduleTime(signTime.endWeek, signTime.endHour, signTime.endMin), ONEWEEK);
		if(inTime(signTime.startWeek, signTime.startHour, signTime.startMin, 
				signTime.endWeek, signTime.endHour, signTime.endMin)){
			canSignUp = true;
		}
		timer.schedule(new TimerTask(){
			public void run() {
				canVote = true;
			}
		}, getScheduleTime(voteTime.startWeek, voteTime.startHour, voteTime.startMin), ONEWEEK);
		timer.schedule(new TimerTask(){
			public void run() {
				canVote = false;
			}
		}, getScheduleTime(voteTime.endWeek, voteTime.endHour, voteTime.endMin), ONEWEEK);
		if(inTime(voteTime.startWeek, voteTime.startHour, voteTime.startMin, 
				voteTime.endWeek, voteTime.endHour, voteTime.endMin)){
			canVote = true;
		}
		timer.schedule(new TimerTask(){
			public void run() {
				endParade();
			}
		}, getScheduleTime(endTime.endWeek, endTime.endHour, endTime.endMin), ONEWEEK);
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		
		Element signDate = root.element("sign");
		int startWeek = Integer.parseInt(signDate.attributeValue("startWeek"));
		int endWeek = Integer.parseInt(signDate.attributeValue("endWeek"));
		String start = signDate.attributeValue("startTime");
		String end = signDate.attributeValue("endTime");
		int startHour = Integer.parseInt(start.split(":")[0]);
		int startMin = Integer.parseInt(start.split(":")[1]);
		int endHour = Integer.parseInt(end.split(":")[0]);
		int endMin = Integer.parseInt(end.split(":")[1]);
		signTime = new Time(startWeek, startHour, startMin, endWeek, endHour, endMin);
		
		String itemStr = signDate.attributeValue("item");
		String[] items = itemStr.split(";");
		for(String item : items){
			int itemId = Integer.parseInt(item.split(",")[0]);
			int count = Integer.parseInt(item.split(",")[1]);
			signCondition.addItem(itemId, count);
		}
		
		Element voteDate = root.element("vote");
		int startWeek1 = Integer.parseInt(signDate.attributeValue("startWeek"));
		int endWeek1 = Integer.parseInt(signDate.attributeValue("endWeek"));
		String start1 = voteDate.attributeValue("startTime");
		String end1 = voteDate.attributeValue("endTime");
		int startHour1 = Integer.parseInt(start1.split(":")[0]);
		int startMin1 = Integer.parseInt(start1.split(":")[1]);
		int endHour1 = Integer.parseInt(end1.split(":")[0]);
		int endMin1 = Integer.parseInt(end1.split(":")[1]);
		voteTime = new Time(startWeek1, startHour1, startMin1, endWeek1, endHour1, endMin1);
		
		List<Element> voteTypeList = root.elements("votetype");
		for(Element ele : voteTypeList){
			int id = Integer.parseInt(ele.attributeValue("id"));
			String typeName = ele.attributeValue("typeName");
			int itemId = Integer.parseInt(ele.attributeValue("itemId"));
			int value = Integer.parseInt(ele.attributeValue("value"));
			VoteType voteType = new VoteType(id, typeName, itemId, value);
			this.voteTypes.add(voteType);
		}
		
		Element endTimeEle = root.element("end");
		int week = Integer.parseInt(endTimeEle.attributeValue("week"));
		String endTimeStr = endTimeEle.attributeValue("time");
		int endH = Integer.parseInt(endTimeStr.split(":")[0]);
		int endM = Integer.parseInt(endTimeStr.split(":")[1]);
		endTime = new Time(0, 0, 0, week, endH, endM);
	}
	
	/** 选美报名 */
	public void signUp(Player p, String slogan) throws BeautyParadeException{
		synchronized (this) {
			if(p!=null){
				if(canSignUp){
					if(getBeauty(p.id)!=null){
						throw new BeautyParadeException("您已經報名!");
					}
					List<Integer> itemIds = signCondition.itemIds;
					PlayerTransaction tx = p.newTransaction("BEAUTYSIGNUP");
					for(int i=0;i<itemIds.size();i++){
						int itemId = itemIds.get(i);
						int count = signCondition.itemCounts.get(i);
						GameItem item = p.bag.removeGameItemIngoreInstanceId(itemId, count, tx, true);
						if(item==null){
							tx.rollback();
							throw new BeautyParadeException(MessageFormat.format("您的{0}不足", 
									ObjectAccessor.getItemTemplate(itemId).name));
						}
					}
					int money = signCondition.money;
					try {
						p.decMoney(money, tx, true);
					} catch (NoEnoughValueException e) {
						tx.rollback();
						throw new BeautyParadeException("您的金錢不足");
					}
					tx.commit();
					slogan = StringUtil.filterBadWords(slogan);
					Beauty beauty = new Beauty();
					beauty.playerId = p.id;
					beauty.name = p.name;
					beauty.sex = p.sex;
					beauty.signUpDate = new Date();
					beauty.slogan = slogan;
					beauty.faction = p.faction;
					beautys.add(beauty);
					Server.server.getServiceRegistry().getDbService().beautySignDao.newEntity(beauty);
					LogUtil.logBeautySignUp(p);
				}else{
					throw new BeautyParadeException("現在不是報名時間");
				}
			}
		}
	}
	
	/** 选美投票 */
	public void vote(Player p, int signPlayerId, int type, int count) throws BeautyParadeException{
		synchronized (this) {
			if(p!=null){
				if(!canVote)
					throw new BeautyParadeException("現在不是投票時間");
				VoteType voteType = getVoteType(type);
				if(voteType!=null){
					Beauty beauty = getBeauty(signPlayerId);
					if(beauty==null)
						throw new BeautyParadeException("您所投票的玩家沒有報名參選");
					if(count<=0)
						throw new BeautyParadeException("輸入錯誤");
					int itemId = voteType.itemId;
					PlayerTransaction tx = p.newTransaction("BEAUTYVOTE");
					GameItem item = p.bag.removeGameItemIngoreInstanceId(itemId, count, tx, true);
					if(item==null){
						tx.rollback();
						throw new BeautyParadeException("您的道具不足");
					}
					tx.commit();
					int value = voteType.value * count;
					beauty.votes += value;
					Server.server.getServiceRegistry().getDbService().beautySignDao.updateEntity(beauty);
					VotePlayer vp = getVotePlayer(p.id);
					if(vp==null){
						vp = new VotePlayer();
						vp.playerId = p.id;
						vp.name = p.name;
						vp.faction = p.faction;
						this.votePlayers.add(vp);
					}
					vp.votes += value;
					LogUtil.logBeautyVote(p, signPlayerId, type, count);
				}else{
					throw new BeautyParadeException("錯誤的投票方式");
				}
			}
		}
	}
	
	/** 获取报名者信息 */
	public Beauty getBeauty(int playerId){
		Iterator<Beauty> it = beautys.iterator();
		while(it.hasNext()){
			Beauty beauty = it.next();
			if(beauty.playerId==playerId)
				return beauty;
		}
		return null;
	}
	
	/** 获取投票者信息 */
	public VotePlayer getVotePlayer(int playerId){
		Iterator<VotePlayer> it = votePlayers.iterator();
		while(it.hasNext()){
			VotePlayer vp = it.next();
			if(vp.playerId==playerId){
				return vp;
			}
		}
		return null;
	}
	
	/** 获取投票最多的玩家信息 */
	public VotePlayer getMaxVotePlayer(){
		VotePlayer voteP = null;
		Iterator<VotePlayer> it = votePlayers.iterator();
		while(it.hasNext()){
			VotePlayer vp = it.next();
			if(voteP==null){
				voteP = vp;
			}else{
				if(vp.votes>voteP.votes){
					voteP = vp;
				}
			}
		}
		return voteP;
	}
	
	/** 选举结束 */
	public void endParade(){
		synchronized (this) {
			// 获取前十名佳丽
			Object[] arr = bubbleBeautys();
			List<Beauty> lastBeautys = new ArrayList<Beauty>();
			int maxBeauty = 10;
			maxBeauty = Math.min(maxBeauty, arr.length);
			for(int i=0;i<maxBeauty;i++){
				if(arr[i]!=null){
					Beauty b = (Beauty)arr[i];
					lastBeautys.add(b);
					MailService mailService = Server.server.getServiceRegistry().getMailService();
					mailService.sendSystemMailAsync(b.playerId, "系統", "選美稱號", "", 0, 
							ObjectAccessor.createGameItem(titles[i]), 1, "BEAUTY");
					LogUtil.logBeautyEnd(b.playerId, b.sex, b.votes);
					Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.
							format("恭喜{0}獲得{1}稱號", b.name, TitleUtil.getTitle(titleIds[i]).name));
				}
			}
			// 删除十名之外的佳丽信息
			Iterator<Beauty> it = this.beautys.iterator();
			while(it.hasNext()){
				Beauty b = it.next();
				if(!in(b, lastBeautys)){
					it.remove();
					Server.server.getServiceRegistry().getDbService().beautySignDao.makeTransient(b);
				}
			}
			// 删除除投票最多一位的其他所有投票者信息
			VotePlayer mVotePlayer = getMaxVotePlayer();
			if(mVotePlayer!=null){
				LogUtil.logBeautyMaxVotes(mVotePlayer.playerId, mVotePlayer.votes);
				Iterator<VotePlayer> iterator = this.votePlayers.iterator();
				while(iterator.hasNext()){
					VotePlayer vp = iterator.next();
					if(vp.playerId!=mVotePlayer.playerId){
						iterator.remove();
					}else{
						MailService mailService = Server.server.getServiceRegistry().getMailService();
						mailService.sendSystemMailAsync(vp.playerId, "系統", "選美稱號", "", 0, 
								ObjectAccessor.createGameItem(titles[10]), 1, "BEAUTY");
						Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat.
								format("恭喜{0}獲得{1}稱號", vp.name, TitleUtil.getTitle(titleIds[10]).name));
					}
				}
			}
		}
	}
	
	/** 清除上轮选美的数据 */
	private void clear(){
		synchronized (this) {
			Iterator<Beauty> it = this.beautys.iterator();
			while(it.hasNext()){
				Beauty b = it.next();
				Server.server.getServiceRegistry().getDbService().beautySignDao.makeTransient(b);
				it.remove();
			}
			Iterator<VotePlayer> it1 = this.votePlayers.iterator();
			while(it1.hasNext()){
				it1.next();
				it1.remove();
			}
			Server.server.getServiceRegistry().getDbService().beautyVoteDao.delete("delete from VotePlayer");
		}
	}
	
	/** 选美候选人列表 */
	public Object[] beautyList() throws BeautyParadeException{
		synchronized (this) {
			if(beautys.size()==0)
				throw new BeautyParadeException("沒有候選人");
			Object[] beautyArr = new Object[beautys.size()+1];
			Object[] bubbles = bubbleBeautys();
			for(int i=0;i<bubbles.length;i++){
				beautyArr[i] = bubbles[i];
			}
			VotePlayer vp = getMaxVotePlayer();
			beautyArr[beautyArr.length-1] = vp; 
			return beautyArr;
		}
	}
	
	/** 好友在排行榜中的列表 */
	public List<Beauty> getBeautysInFriend(Player p) throws BeautyParadeException{
		synchronized (this) {
			if(p!=null){
				List<Beauty> list = new ArrayList<Beauty>();
				Object[] os = bubbleBeautys();
				RelationList reList = p.relations.friends;
				if(reList.players==null || reList.players.size()==0)
					throw new BeautyParadeException("沒有好友");
				for(Object o : os){
					Beauty b = (Beauty)o;
					for(Actor a : reList.players){
						if(b.playerId==a.id){
							list.add(b);
						}
					}
				}
				return list;
			}
			return null;
		}
	}
	
	private Object[] bubbleBeautys(){
		Object[] beautyArr = new Beauty[beautys.size()];
		int index = 0;
		for(Beauty b : this.beautys){
			beautyArr[index] = b;
			index++;
		}
		Beauty temp = null;
		for(int i=0;i<beautyArr.length;i++){
			for(int j=i+1;j<beautyArr.length;j++){
				if(((Beauty)beautyArr[i]).votes<((Beauty)beautyArr[j]).votes || 
						(((Beauty)beautyArr[i]).votes==((Beauty)beautyArr[j]).votes && 
								((Beauty)beautyArr[i]).signUpDate.after(((Beauty)beautyArr[j]).signUpDate))){
					temp = (Beauty)beautyArr[i];
					beautyArr[i] = beautyArr[j];
					beautyArr[j] = temp;
				}
			}
		}
		return beautyArr;
	}
	
	private VoteType getVoteType(int id){
		for(VoteType type : voteTypes){
			if(type.id==id)
				return type;
		}
		return null;
	}
	
	private boolean in(Beauty b, List<Beauty> list){
		for(Beauty bt : list){
			if(b.playerId==bt.playerId)
				return true;
		}
		return false;
	}
	
	public Date getScheduleTime(int weekDay, int hour, int min){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.DAY_OF_WEEK, ((weekDay+1)==8 ? 1 : (weekDay+1)));
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		if (cal1.before(cal)) {
			cal1.add(Calendar.WEEK_OF_MONTH, 1);
			return cal1.getTime();
		}
		return cal1.getTime();
	}
	
	private boolean inTime(int sWeekDay, int sHour, int sMin, int eWeekDay, int eHour, int eMin){
		Calendar cal = Calendar.getInstance();
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(new Date());
		cal1.set(Calendar.DAY_OF_WEEK, (sWeekDay==7 ? 1 : sWeekDay+1));
		cal1.set(Calendar.HOUR_OF_DAY, sHour);
		cal1.set(Calendar.MINUTE, sMin);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(new Date());
		cal2.set(Calendar.DAY_OF_WEEK, (eWeekDay==7 ? 1 : eWeekDay+1));
		cal2.set(Calendar.HOUR_OF_DAY, eHour);
		cal2.set(Calendar.MINUTE, eMin);
		return cal.after(cal1) && cal.before(cal2);
	}
	
	public void shutdown() {
		BeautyVoteDao dao = Server.server.getServiceRegistry().getDbService().beautyVoteDao;
		dao.delete("delete from VotePlayer");
		VotePlayer vp = getMaxVotePlayer();
		if(vp!=null){
			dao.newEntity(vp);
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
			for(int i=0;i<titleIds.length;i++){
				if(p.titles.hasTitle(titleIds[i]) && !isBeauty(p.id) && !isVotePlayer(p.id)){
					p.titles.removeTitle(titleIds[i]);
				}
//				if(p.titles.hasTitle(titleIds[i]) && isBeauty(p.id) && 
//						getPositionInBeautys(p.id)!=i){
//					p.titles.removeTitle(titleIds[i]);
//				}
//				if(p.titles.hasTitle(titleIds[i]) && isVotePlayer(p.id) && 
//						(getMaxVotePlayer()==null || getMaxVotePlayer().playerId!=p.id)){
//					p.titles.removeTitle(titleIds[i]);
//				}
			}
		}
	}
	
	public boolean isBeauty(int playerId){
		synchronized (this) {
			for(Beauty b : beautys){
				if(b.playerId==playerId)
					return true;
			}
			return false;
		}
	}
	
	public boolean isVotePlayer(int playerId){
		synchronized (this) {
			for(VotePlayer vp : votePlayers){
				if(vp.playerId==playerId)
					return true;
			}
			return false;
		}
	}
	
	public int getPositionInBeautys(int playerId){
		Object[] list = bubbleBeautys(); 
		for(int i=0;i<list.length;i++){
			Beauty b = (Beauty)list[i];
			if(b.playerId==playerId)
				return i;
		}
		return -1;
	}

}

class Time{
	
	public int startWeek;
	public int startHour;
	public int startMin;
	public int endWeek;
	public int endHour;
	public int endMin;
	
	public Time(int week, int startHour, int startMin, int endWeek, int endHour, int endMin){
		this.startWeek = week;
		this.startHour = startHour;
		this.startMin = startMin;
		this.endWeek = endWeek;
		this.endHour = endHour;
		this.endMin = endMin;
	}
	
}

class VoteType{
	
	public int id;
	public String typeName;
	public int itemId;
	public int value;
	
	public VoteType(int id, String typeName, int itemId, int value){
		this.id = id;
		this.typeName = typeName;
		this.itemId = itemId;
		this.value = value;
	}
	
}

class Condition{
	
	public List<Integer> itemIds = new ArrayList<Integer>();
	public List<Integer> itemCounts = new ArrayList<Integer>();
	public int money;
	
	public void addItem(int itemId, int count){
		itemIds.add(itemId);
		itemCounts.add(count);
	}
	
}
