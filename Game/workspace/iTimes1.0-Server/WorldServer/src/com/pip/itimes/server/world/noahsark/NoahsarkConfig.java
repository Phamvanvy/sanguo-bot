package com.pip.itimes.server.world.noahsark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.worldboss.WorldBossConfig;
import com.pip.itimes.server.world.worldboss.WorldBossPlayer;


public class NoahsarkConfig {
	private static final Logger log = Logger.getLogger(NoahsarkConfig.class);

	public static Random rand = new Random();
	public static int year = 0;
	public static int month = 0;
	public static int day = 0;
	public static int endYear = 0;
	public static int endMonth = 0;
	public static int endDay = 0;
	public static StringBuffer title;
	public static NoahsarkDonate donate;
	public static NoahsarkScoreTop scoreTop;
	public static NoahsarkDonateMaterial[] bossPrize;
	public static int ticketCounts = 0;
	
	public static ConcurrentHashMap<Integer,NoahsarkPlayer> noahsarkTop = new ConcurrentHashMap<Integer,NoahsarkPlayer>();
	public static ConcurrentHashMap<Integer,NoahsarkPlayer> noahsarkPlayer = new ConcurrentHashMap<Integer,NoahsarkPlayer>();
	public static ConcurrentHashMap<String,TimesCount> timesCountsMap = new ConcurrentHashMap<String,TimesCount>();
	public static int MAX_TOP = 10;
	public static String PATH = "noahsak";
	private static List<NoahsarkPlayer> sortTop = new ArrayList<NoahsarkPlayer>();
	private static List<NoahsarkPlayer> playerList = new ArrayList<NoahsarkPlayer>();
	private static List<TimesCount> timesCountsList = new ArrayList<TimesCount>();

	public static final int STAGE_NOT_STARTED = -1;
	public static final int STAGE_DONATE_NOT_STARTED = -2;
	public static final int STAGE_DONATE_STARTED = 1;
	public static final int STAGE_DONATE_END = 2;
	public static final int STAGE_TOP_STARTED = 3;
	public static final int STAGE_TOP_END = 4;
	/**
	 * 积分清零和领取礼物时的状态
	 */
	public static int stage ;
	protected static MailService mailService;
	protected static ChatService chatService;
	protected PlayerService playerService;
	protected WorldBossConfig worldBossConfig;
	
	
	static {
		loadFile();
		loadItemFile();
	}

   	public static void setStage (int stage) {
	    NoahsarkConfig.stage = stage;
	}
	public static int getStage() {
    	return stage;
    }
	public static void setNoahsarkStage(){
		Date date = new Date();
		if(date.getTime() >= getEndDate().getTime()){
			setStage(STAGE_DONATE_END);
		}else if(date.getTime() < getStartDate().getTime()){
			setStage(STAGE_NOT_STARTED);
		}else if(date.getTime() >= getStartDate().getTime()
				&& date.getTime() < getDonateStartDate().getTime()){
			setStage(STAGE_DONATE_NOT_STARTED);
		}else{
			setStage(STAGE_DONATE_STARTED);
		}
		/*if(date.getTime() >= getEndDate().getTime()){
			setStage(STAGE_DONATE_END);
		}else if(date.getTime() < getStartDate().getTime()){
			setStage(STAGE_NOT_STARTED);
		}else if(date.getTime() >= getStartDate().getTime()
				&& date.getTime() < getDonateStartDate().getTime()){
			setStage(STAGE_TOP_STARTED);
		}else{
			setStage(STAGE_TOP_END);
		}*/
	}
	public static Date getStartDate(){
		return getConfigDate(year,month,day);
	}
	public static Date getEndDate(){
		return getConfigDate(endYear,endMonth,endDay);
	}
	public static Date getDonateStartDate(){
		return getConfigDate(donate.getYear(),donate.getMonth(),donate.getDay());
	} 
	public static Date getConfigDate (int year,int month,int day){
		Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month-1);
			cal.set(Calendar.DAY_OF_MONTH, day);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	public static int setDonateNumber(int id, String name, int score,int count){
		NoahsarkPlayer player = null;
		if(noahsarkPlayer.containsKey(id)){
			player = noahsarkPlayer.get(id);
			if(player.getTotalCount() < 20){
				player.setTotalCount(count);
				player.setTotalScore(score);
				Calendar c =  Calendar.getInstance();
				c.setTimeInMillis(System.currentTimeMillis());
				player.setDonateDate(c.getTimeInMillis());
			}
		}else{
			Calendar c1 =  Calendar.getInstance();
			c1.setTimeInMillis(System.currentTimeMillis());
			player = new NoahsarkPlayer(id, name,score,count,c1.getTimeInMillis());
			noahsarkPlayer.put(id, player);
		}
		playerList = sortPlayer(noahsarkPlayer);
		savePlayerData();
		return player.getTotalCount();
	}
	/**
	 * 购买成功，把剩余个数计入timesCountsMap中
	 * @param itemId
	 * @param timesId
	 * @param counts
	 */
	public static void addTimesCounts(int itemId,int timesId,int counts,long period){
		TimesCount timesCount = null;
		if(timesCountsMap.containsKey(itemId+" "+timesId)){
			timesCount = timesCountsMap.get(itemId+" "+timesId);
			timesCount.setCounts(counts);
			timesCount.setPeriod(period);
		}else{
			timesCount = new TimesCount(itemId, timesId,counts,period);
			timesCountsMap.put(itemId+" "+timesId, timesCount);
		}
		setTimesCountsList(timesCountsMap);
		saveData();//每次都存一次，所以map，list中会有无数条，所以又list中去重
	}
	/**
	 * 把map中的内容加到timesCountsList中，得去重
	 * @param mpp
	 */
	public static void setTimesCountsList(ConcurrentHashMap<String, TimesCount> mpp){
		Iterator<TimesCount> iter = mpp.values().iterator();
		while(iter.hasNext()){
			TimesCount timesCounts = iter.next();
			timesCountsList.add(timesCounts);
		}
		if(timesCountsList.size()>1){
			Set set = new HashSet();
			List newList = new ArrayList();
			for (Iterator<TimesCount> it = timesCountsList.iterator(); it.hasNext();) {
				TimesCount element = it.next();
		         if (set.add(element)){
		        	 newList.add(element);
		        }
			}
			timesCountsList =  newList ;      
		}

	}
	/**
	 * 获得timesCountsList的内容
	 * @return
	 */
	public static List<TimesCount> getTimesCountsList(){
		return timesCountsList;
	}
	/**
	 * 把timesCountsList的记录保存到xml文件中
	 */
	public static void saveData(){
		try{
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("NoahsarkItem");
			for(TimesCount itemsCounts :timesCountsList){
				Element itemData = root.addElement("data");
				itemData.addAttribute("itemId",""+itemsCounts.getItemId());
				itemData.addAttribute("timesId", ""+itemsCounts.getTimesId());
				itemData.addAttribute("counts", ""+itemsCounts.getCounts());
				itemData.addAttribute("period", ""+itemsCounts.getPeriod());
								
			}
			try{
				String path = System.getProperty("user.dir")+"/"+ PATH;
				File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + PATH + "Item.xml");
	        	file.createNewFile();
	        	saveDocument(doc, new FileWriter(file));
				log.info("Save noahsarkItemData ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}	
	}
	/**
	 * 启动时加载xml文件，并把它再次读入map以及list中
	 */
	public static void loadItemFile(){
		synchronized(timesCountsMap){
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/" + PATH + "Item.xml" );
			if(file.exists()){
				try{
					SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
		    		for(Iterator data = root.elementIterator("data");data.hasNext();){
		    			Element itemData = (Element)data.next();
		    			int itemId = Integer.parseInt(itemData.attributeValue("itemId"));
		    			int timesId = Integer.parseInt(itemData.attributeValue("timesId"));
		    			int counts = Integer.parseInt(itemData.attributeValue("counts"));
		    			long period = Long.parseLong(itemData.attributeValue("period"));
						TimesCount p = new TimesCount(itemId,timesId,counts,period);
						timesCountsMap.put(itemId+" "+timesId, p);
					}
	    			 setTimesCountsList(timesCountsMap);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
		}
	}
	public static long getDateLong(Date date){
		Calendar c = Calendar.getInstance();
        c.setTime(date);
        long dateLong = c.get(Calendar.YEAR)*10000+ (c.get(Calendar.MONTH)+1)*100+c.get(Calendar.DAY_OF_MONTH);
        return dateLong;
	}
    public void setMailService (MailService mailService) {
	      this.mailService = mailService;
		}
		 
	public void setChatService (ChatService chatService) {
		this.chatService = chatService;
	}
	
	public void setPlayerService (PlayerService playerService) {
		this.playerService = playerService;
	}
	public WorldBossConfig getWorldBossConfig() {
		return worldBossConfig;
	}
	public void setWorldBossConfig(WorldBossConfig worldBossConfig) {
		this.worldBossConfig = worldBossConfig;
	}
	public int checkActivityState(){
		Date now = new Date();
		long time = now.getTime();
		if (time < getStartDate().getTime() || time >getEndDate().getTime()) {
			return STAGE_NOT_STARTED;
		} else if(time<getDonateStartDate().getTime()){
			return STAGE_DONATE_NOT_STARTED;
		}
		return STAGE_DONATE_STARTED;
	}
	public static List<NoahsarkPlayer> getSortTop(){
		return sortTop;
	}
	public static void setSortTop(){
		synchronized (noahsarkTop){
			if(noahsarkPlayer.size()<= MAX_TOP){
				Iterator iter = noahsarkPlayer.values().iterator();
				while(iter.hasNext()){
					NoahsarkPlayer p = (NoahsarkPlayer)iter.next();
					noahsarkTop.put(p.getId(), p);
				}
			}else{
				List<NoahsarkPlayer> sortList = sortPlayer(noahsarkPlayer);
				//NoahsarkPlayer[] players = (NoahsarkPlayer[])noahsarkPlayer.values().toArray();
				for(int i=0;i<10;i++){
					noahsarkTop.put(sortList.get(i).getId(), sortList.get(i));
				}
			}
			
			sortTop = sortPlayer(noahsarkTop);
		}
	}
	public static NoahsarkPlayer getPlayerMin(ConcurrentHashMap<Integer, NoahsarkPlayer> map){
		NoahsarkPlayer player = null;
		Iterator<NoahsarkPlayer> iter = map.values().iterator();
		while(iter.hasNext()){	
			NoahsarkPlayer currentplayer = iter.next();
			if(player == null){
				player = currentplayer;
			}else{
				if(currentplayer.getTotalScore() < player.getTotalScore()){
					player = currentplayer;
				}
			}
		}
		return player;
	}

	public static List<NoahsarkPlayer> sortPlayer(ConcurrentHashMap<Integer, NoahsarkPlayer> mpp){
		Iterator<NoahsarkPlayer> iter = mpp.values().iterator();
		List<NoahsarkPlayer> sortTopData = null;
		while(iter.hasNext()){
			NoahsarkPlayer player = iter.next();
			if(player != null){
				sortTopData = new ArrayList<NoahsarkPlayer>();
				NoahsarkPlayer p = new NoahsarkPlayer();
				p.setNoahsarkFirstDay(player.getNoahsarkFirstDay());
				p.setId(player.getId());
				p.setName(player.getName());
				p.setTotalScore(player.getTotalScore());
				p.setDonateDate(player.getDonateDate());
				p.setTotalCount(player.getTotalCount());

				sortTopData.add(p);
				while(iter.hasNext()){
					NoahsarkPlayer player1 = iter.next();
					if(player1 != null){
						int size = sortTopData.size();
						boolean insert = false;
						for(int i = 0;i<size;i++){
							NoahsarkPlayer temp = sortTopData.get(i);
							if(player1.getTotalScore()> temp.getTotalScore()){
								p = new NoahsarkPlayer();
								p.setId(player1.getId());
								p.setName(player1.getName());
								p.setTotalScore(player1.getTotalScore());
								p.setTotalCount(player1.getTotalCount());
								p.setDonateDate(player1.getDonateDate());
								sortTopData.add(i, p);
								insert = true;
								break;
							}
						}
						if(!insert){
							p = new NoahsarkPlayer();
							p.setId(player1.getId());
							p.setName(player1.getName());
							p.setTotalScore(player1.getTotalScore());
							p.setTotalCount(player1.getTotalCount());
							p.setDonateDate(player1.getDonateDate());
							sortTopData.add(p);
						}
					}
				}
				
			}
		}
		return sortTopData;
	}
	
	public static void savePlayerData(){
		try{
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("NoahsarkPlayers");
			for(NoahsarkPlayer player :playerList){
				Element playerData = root.addElement("Player");
				playerData.addAttribute("id",""+player.getId());
				playerData.addAttribute("name", ""+player.getName());
				playerData.addAttribute("totalScore", ""+player.getTotalScore());
				playerData.addAttribute("totalCount", ""+player.getTotalCount());
				playerData.addAttribute("donateDate", ""+player.getDonateDate());
				
			}
			try{
				String path = System.getProperty("user.dir")+"/"+ PATH;
				File dir = new File(path);
	        	if(!dir.exists()){
	        		dir.mkdir();
	        	}
	        	File file = new File(path + "/" + PATH + ".xml");
	        	file.createNewFile();
	        	saveDocument(doc, new FileWriter(file));
				log.info("Save noahsarkPlayers ok");
			} catch (IOException e) {
				log.error(e, e);
			}
		} catch (Exception e) {
			log.error(e, e);
		}	
	}

	public  static void saveDocument(Document doc, Writer w){
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
				e.printStackTrace();
			}
		}
	}
	
	public static void loadFile(){
		synchronized(noahsarkPlayer){
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/" + PATH + ".xml" );
			if(file.exists()){
				try{
					SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
		    		for(Iterator playerData = root.elementIterator("Player");playerData.hasNext();){
		    			Element player = (Element)playerData.next();
		    			int id = Integer.parseInt(player.attributeValue("id"));
						String name = player.attributeValue("name");
						int totalScore = Integer.parseInt(player.attributeValue("totalScore"));
						int totalCount = Integer.parseInt(player.attributeValue("totalCount"));
						long donateDate = Long.parseLong(player.attributeValue("donateDate"));
						NoahsarkPlayer p = new NoahsarkPlayer(id,name,totalScore,totalCount,donateDate );
						noahsarkPlayer.put(id, p);
					}
	    			playerList = sortPlayer(noahsarkPlayer);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}
		}
	}
	public static void resetDonateCount(){
		Iterator<NoahsarkPlayer> iter = noahsarkPlayer.values().iterator();
		while(iter.hasNext()){
			NoahsarkPlayer player = iter.next();
			player.resetCount();
			playerList = sortPlayer(noahsarkPlayer);
			savePlayerData();
		}
	}
	public static Date setDate(){
		Calendar calendar=Calendar.getInstance();   //创建一个日历对象        
		calendar.setTime(new Date());             //用当前时间初始化日历时间     
		int year = calendar.get(Calendar.YEAR);         
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String week = String.valueOf(calendar.get(Calendar.DAY_OF_WEEK)-1);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);        
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		calendar.set(year,month,day,hour,minute,second);
		return calendar.getTime();
	}
	public static  void prizesAccordingScore() throws Exception{
		if(getDonateStartDate().getTime()>System.currentTimeMillis()&&System.currentTimeMillis()>getEndDate().getTime()){
			return;
		}else{
			Iterator iter = noahsarkPlayer.values().iterator();
			while(iter.hasNext()){
				NoahsarkPlayer p = (NoahsarkPlayer)iter.next();
				if(p.getTotalScore()>= 30){
				int giftId = NoahsarkConfig.scoreTop.getBase().getItemId();
				int giftCount = NoahsarkConfig.scoreTop.getBase().getItemCount();
				IItem iit = Items.getTemplate(giftId).newInstance();
				byte[] att = ItemUtils.item2dbAttachment(iit, giftCount);
					mailService.sendMail(p.getId(), p.getName(), -1, "系统",
							iit.getName() + "*" + giftCount, "恭喜您获得了我们赠送的" + giftCount 
							+ "张" + iit.getName() + "，祝您愉快。（捐献积分超过30分，就会获得五张火车票）",att,0, true);
					log.info(p.getId() + "" + p.getName() + " scores of donate greater than 30 and get " + giftId + "goods" + 
							giftCount + " counts" );
				}
			}
			setSortTop();
			for(int i=0;i<Math.min(sortTop.size(),3);i++){
				NoahsarkPlayer pp = (NoahsarkPlayer)sortTop.get(i);
				if(i == 0 ){
					int firstGiftId = NoahsarkConfig.scoreTop.getFirst().getItemId();
					int firstGiftCount = NoahsarkConfig.scoreTop.getFirst().getItemCount();
					IItem iit = Items.getTemplate(firstGiftId).newInstance();
					byte[] att = ItemUtils.item2dbAttachment(iit, firstGiftCount);
						mailService.sendMail(pp.getId(), pp.getName(), -1, "系统",
								iit.getName() + "*" + firstGiftCount, "您在这次火车票预订活动捐献排行榜中获得了第一名" +
										"，获得了我们赠送的" + firstGiftCount 
								+ "张" + iit.getName() + "，祝您愉快。",att,0, true);
						log.info(pp.getId() + "" + pp.getName() + "  donate first and get " + firstGiftId + "goods" + 
								firstGiftCount + " counts" );
				}
				if(i == 1 ){
					int secondGiftId = NoahsarkConfig.scoreTop.getSecond().getItemId();
					int secondGiftCount = NoahsarkConfig.scoreTop.getSecond().getItemCount();
					IItem iit = Items.getTemplate(secondGiftId).newInstance();
					byte[] att = ItemUtils.item2dbAttachment(iit, secondGiftCount);
						mailService.sendMail(pp.getId(), pp.getName(), -1, "系统",
								iit.getName() + "*" + secondGiftCount, "您在这次火车票预订活动捐献排行榜中获得了第二名" +
										"，获得了我们赠送的" + secondGiftCount 
								+ "张" + iit.getName() + "，祝您愉快。",att,0, true);
						log.info(pp.getId() + "" + pp.getName() + "  donate second and get " + secondGiftId + "goods" + 
								secondGiftCount + " counts" );
				}
				if(i == 2 ){
					int thirdGiftId = NoahsarkConfig.scoreTop.getThird().getItemId();
					int thirdGiftCount = NoahsarkConfig.scoreTop.getThird().getItemCount();
					IItem iit = Items.getTemplate(thirdGiftId).newInstance();
					byte[] att = ItemUtils.item2dbAttachment(iit, thirdGiftCount);
						mailService.sendMail(pp.getId(), pp.getName(), -1, "系统",
								iit.getName() + "*" + thirdGiftCount, "您在这次火车票预订活动捐献排行榜中获得了第三名" +
										"，获得了我们赠送的" + thirdGiftCount 
								+ "张" + iit.getName() + "，祝您愉快。",att,0, true);
						log.info(pp.getId() + "" + pp.getName() + "  donate third and get " + thirdGiftId + "goods" + 
								thirdGiftCount + " counts" );
				}
			}
			
		}
	}
	public static void resetTotalScore(){
		Iterator<NoahsarkPlayer> iter = noahsarkPlayer.values().iterator();
		while(iter.hasNext()){
			NoahsarkPlayer player = iter.next();
			player.resetScore();
			playerList = sortPlayer(noahsarkPlayer);
			savePlayerData();
		}
	}
	public static void reset(long now){
		long day = 1000*24*60*60;
		resetDonateCount();
		setNoahsarkStage();
		if(((now-getDonateStartDate().getTime())/day)%7 == 0){
			try {
				prizesAccordingScore();
			} catch (Exception e) {
				e.printStackTrace();
			}
			resetTotalScore();
		}
	}
	public static void resetiShopLion(long now){ 
		long day = 1000*24*60*60;
		long hour = 1000*60*60;
		long minute = 1000*60;
		long second = 1000;
		if(getStage()!= STAGE_NOT_STARTED && getStage()!= STAGE_DONATE_END){
			if(((now-getStartDate().getTime())%day)/hour== 9
					&& ((now-getStartDate().getTime())%hour)/minute == 55
					&& ((now-getStartDate().getTime())%minute)/second < 5){
				iShopLion(1);
			}else if(((now-getStartDate().getTime())%day)/hour== 13
					&& ((now-getStartDate().getTime())%hour)/minute == 55
					&& ((now-getStartDate().getTime())%minute)/second < 5){
				iShopLion(2);
			}else if(((now-getStartDate().getTime())%day)/hour== 18
					&& ((now-getStartDate().getTime())%hour)/minute == 55
					&& ((now-getStartDate().getTime())%minute)/second < 5){
				iShopLion(3);
			}
		}
		
	}
	public static void iShopLion(int times){
		switch(times){
		case 1:
			String	msg = "活动期间每日上午10点-12点开始在ISHOP限量出售第一批一等火车票，还有5分钟抢购活动就开始了，快来抢购吧！";
			chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
			break;
		case 2:
			String	msg2 = "活动期间每日上午14点-16点开始在ISHOP限量出售第二批一等火车票，还有5分钟抢购活动就开始了，快来抢购吧！";
			chatService.sendRoarMessage( -1, "狮子吼", msg2, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
			break;
		case 3:
			String	msg3 = "活动期间每日上午19点-20点开始在ISHOP限量出售第三批一等火车票，还有5分钟抢购活动就开始了，快来抢购吧！";
			chatService.sendRoarMessage( -1, "狮子吼", msg3, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
			break;
		}
			
		
	}
	public static void  setWorldTopPrizes(){
		if(getStartDate().getTime()>System.currentTimeMillis()&&System.currentTimeMillis()>getEndDate().getTime()){
			return;
		}else{
			if(WorldBossConfig.sortTop.size()> 0 ){
				List<WorldBossPlayer> list = WorldBossConfig.sortTop;
				for(int i = 0 ;i<3;i++){
					WorldBossPlayer player = list.get(i);
					switch(i){
					case 0:
						int firstGiftId = NoahsarkConfig.bossPrize[0].getItemId();
						int firstGiftCount = NoahsarkConfig.bossPrize[0].getItemCount();
						int percent = Utils.getRandom(0, 100);
						IItem iit;
						if(percent < 10){
							iit = Items.getTemplate(firstGiftId).newInstance(); 
							byte[] att = ItemUtils.item2dbAttachment(iit, firstGiftCount);
								mailService.sendMail(player.id, player.name, -1, "系统",
										iit.getName() + "*" + firstGiftCount, "您在这次世界boss排行榜中获得了第一名" +
												"，获得了我们赠送的" + firstGiftCount 
										+ "张" + iit.getName() + "，祝您愉快。",att,0, true);	
								log.info(player.id + "" + player.name + "win the first prize of worldBoss and get " + firstGiftId + "goods" + 
										firstGiftCount + " counts" );
					    }
						break;
					case 1:
						int secondGiftId = NoahsarkConfig.bossPrize[1].getItemId();
						int secondGiftCount = NoahsarkConfig.bossPrize[1].getItemCount();
						int percent1 = Utils.getRandom(0, 100);
						IItem iit1;
						if(percent1 < 10){
							iit1 = Items.getTemplate(secondGiftId).newInstance(); 
							byte[] att1 = ItemUtils.item2dbAttachment(iit1, secondGiftCount);
								mailService.sendMail(player.id, player.name, -1, "系统",
										iit1.getName() + "*" + secondGiftCount, "您在这次世界boss排行榜中获得了第二名" +
												"，获得了我们赠送的" + secondGiftCount 
										+ "张" + iit1.getName() + "，祝您愉快。",att1,0, true);	
								log.info(player.id + "" + player.name + "win the second prize of worldBoss and get " + secondGiftId + "goods" + 
										secondGiftCount + " counts" );
					    }
						break;
					case 2:
						int thirdGiftId = NoahsarkConfig.bossPrize[2].getItemId();
						int thirdGiftCount = NoahsarkConfig.bossPrize[2].getItemCount();
						int percent2 = Utils.getRandom(0, 100);
						IItem iit2;
						if(percent2 < 10){
							iit2= Items.getTemplate(thirdGiftId).newInstance(); 
							byte[] att2 = ItemUtils.item2dbAttachment(iit2, thirdGiftCount);
								mailService.sendMail(player.id, player.name, -1, "系统",
										iit2.getName() + "*" + thirdGiftCount, "您在这次世界boss排行榜中获得了第三名" +
												"，获得了我们赠送的" + thirdGiftCount 
										+ "张" + iit2.getName() + "，祝您愉快。",att2,0, true);
								log.info(player.id + "" + player.name + "win the third prize of worldBoss and get " + thirdGiftId + "goods" + 
										thirdGiftCount + " counts" );
					    }
						break;
				   }
				}
			}
		}
	}
	/*public static void resetTicketCounts(){
		IStoreGroup inTimeGroup = IStoreGroups.getGroup("限时抢购专区");
		IStoreItem[] items = inTimeGroup.getItems();
		for(int m = 0;m < items[0].times.size();m++){
			String start = items[0].times.get(m).getStart();
			Calendar calendarStart = IShopTimeItem.getDate(start);
			if(System.currentTimeMillis() == calendarStart.getTimeInMillis()){
				ticketCounts=0;
			}
		}
			
	}*/
	public int getTicketCounts(){
    	return ticketCounts;
    }
	
}
	/**/
	/**
	 * 捐赠物品排行
	 * @param player
	 */
	/*public void playerAddItem(NoahsarkPlayer player){
		synchronized (noahsarkTop){
			if(noahsarkTop.containsKey(player.getId())){
				NoahsarkPlayer player1 = noahsarkTop.get(player.getId());
				player1.setTotalScore(getDonateScore());
				sortTop = sortplayer(noahsarkTop);
				saveplayerData();
			}else{
				if(noahsarkTop.size() >= MAX_TOP){
					NoahsarkPlayer tempplayer = getPlayerMin(noahsarkTop);
					if(tempplayer != null){
						if(player.getTotalScore() >tempplayer.getTotalScore()){
							return;
						}
						noahsarkTop.remove(tempplayer.getId());
					}
				}
				NoahsarkPlayer p = new NoahsarkPlayer();
				p.setId(player.getId());
				p.setName(player.getName());
				p.setTotalScore(player.getTotalScore());
				noahsarkTop.put(player.getId(), p);
				sortTop = sortplayer(noahsarkTop);
				saveplayerData();
			}
		}
	}*/
    //保存排行榜信息
	/*public static void saveplayerData(){
		try {
			synchronized (noahsarkTop) {
				Document doc = DocumentHelper.createDocument();
				Element root = doc.addElement(PATH);
				Element attrElement = root.addElement("players");
				for(NoahsarkPlayer bbp : noahsarkTop.values()){
					Element elItemTopData = attrElement.addElement("Data");
					elItemTopData.addAttribute("id", "" + bbp.getId());
					elItemTopData.addAttribute("name", "" + bbp.getName());
					elItemTopData.addAttribute("totalScore", "" + bbp.getTotalScore());
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
					log.info("Save Noahsark Top ok");
				} catch (IOException e) {
					log.error(e, e);
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}*/
	
	/*public static void saveDocument(Document doc, Writer w){
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
    }*/
	//读取文件
	/*public static void loadFile(){
		synchronized (noahsarkTop) {
			File file = new File(System.getProperty("user.dir") + "/" + PATH + "/" + PATH + ".xml");
			if(file.exists()){
		    	try {
		    		SAXReader reader = new SAXReader();
		    		Document doc = reader.read(file);
		    		Element root = doc.getRootElement();
		    		noahsarkTop.clear();
					Element attrRoot = root.element("players");
	    			for(Iterator data = attrRoot.elementIterator("Data"); data.hasNext();){
						Element elData = (Element)data.next();
						int playerid = Integer.parseInt(elData.attributeValue("id"));
						String playername = elData.attributeValue("name");
						int totalScore = Integer.parseInt(elData.attributeValue("totalScore"));
						NoahsarkPlayer player = new NoahsarkPlayer();
						player.setId(playerid);
						player.setName(playername);
						player.setTotalScore(totalScore);
						noahsarkTop.put(playerid, player);
					}
	    			if(noahsarkTop.size() > MAX_TOP){
	    				int removeCount = noahsarkTop.size() - MAX_TOP;
	    				while(removeCount > 0){
	    					NoahsarkPlayer tp = getPlayerMin(noahsarkTop);
	    					noahsarkTop.remove(tp.getId());
	    					removeCount --;
	    				}
	    			}
	    			sortTop = sortplayer(noahsarkTop);
		    	} catch (Exception e) {
		    		log.error(e, e);
		    	}
			}			
		}	
	}	*/
