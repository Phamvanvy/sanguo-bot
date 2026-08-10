package com.pip.itimes.server.world;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.TwelfthLunarConfig;
import com.pip.itimes.server.stage.TwelfthLunarShowInfo;
import com.pip.itimes.server.util.Utils;

public class TwelfthLunarService {
	private static final Logger log = Logger.getLogger(ShoutService.class);
	/**
	 * 腊八活动停止
	 */
	public static final int STAGE_NOT_STARTED = -1;
	/**
	 * 腊八活动已开始：玩家可以捐助腊八豆
	 */
	public static final int STAGE_DONATE_STARTED = 1;
	/**
	 * 腊八捐助施粥结束，但排行榜不关闭
	 */
	public static final int STAGE_DONATE_END = 2;
	/**
	 * 排行榜开启
	 */
	public static final int STAGE_TOP_STARTED = 3;
	/**
	 * 排行榜关闭
	 */
	public static final int STAGE_TOP_END = 4;
	
	/**
	 * 当前活动状态(主要用于每天活动的开始和关闭)
	 */
	public static int currentSegment = -1;
	/**
	 * 本次食神活动的状态（主要用于活动结束后自动发奖）
	 */
	public static int stage = -1;
	/**
	 * 本次食神活动排行榜的状态（主要排行榜）
	 */
	public static int topStage = -1;
	/**
	 * 参加活动领取礼物ID
	 */
	private int activityGiftId;
	/**
	 * 每次参加活动领取礼物个数
	 */
	private int activityGiftCount;
	/**
	 * 允许参加活动的等级
	 */
	private int level;
	
	protected MailService mailService;
	protected ChatService chatService;
	protected PlayerService playerService;
	
	public TwelfthLunarService () {
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
	
	/**
	 * 设置参加活动领取礼物ID
	 * @param campGiftId
	 */
	public void setActivityGiftId (int activityGiftId) {
		this.activityGiftId = activityGiftId;
	}
	
	/**
	 * 获得参加活动领取礼物ID
	 * @return
	 */
	public int getActivityGiftId () {
		return activityGiftId;
	}
	
	/**
	 * 设置每次参加活动领取礼物个数
	 * @return
	 */
	public void setActivityGiftCount (int activityGiftCount) {
		this.activityGiftCount = activityGiftCount;
	}
	
	/**
	 * 获得每次参加活动领取礼物个数
	 * @return
	 */
	public int getActivityGiftCount () {
		return activityGiftCount;
	}
	
	/**
	 * 设置参加活动等级
	 * @param level
	 */
	public void setActivityLevel (int level) {
		this.level = level;
	}
	
	/**
	 * 获得参加活动等级
	 * @return
	 */
	public int getActivityLevel () {
		return level;
	}
	
	/**
	 * 设置腊八粥的个数
	 */
	public void setGruelCount (int gruelCount) {
		TwelfthLunarConfig.gruelCount = gruelCount;
	}
	
	/**
	 * 获得腊八粥的个数
	 * @return
	 */
	public int getGruelCount () {
		return TwelfthLunarConfig.gruelCount;
	}
	
	/**
	 * 检查施粥活动是否开始，没有开始返回-1,开始返回当天的第几次活动
	 * @return 
	 */
	public int checkEffectivePeriod () {
		Date now = new Date();
		long time = now.getTime();
		if (time < TwelfthLunarConfig.startDate.getTime() || time > TwelfthLunarConfig.endDate.getTime()) {
			return STAGE_NOT_STARTED;
		} else {
			for (int i = 0; i < TwelfthLunarConfig.segment; i++) {
				if (time >= TwelfthLunarConfig.twelfthLunarActivityConfig[i].getActivityStartTime().getTime()
						&& time < TwelfthLunarConfig.twelfthLunarActivityConfig[i].getActivityEndTime().getTime()) {
					if (currentSegment == STAGE_NOT_STARTED) {
						setResetData(i);
						sendActivityChat(true, i);
						sendTopChat();
					}
					return i;
				}
			}
			if (currentSegment > STAGE_NOT_STARTED) {
				sendActivityChat(false, currentSegment);
				sendTopChat();
			}
		}
		return STAGE_NOT_STARTED;
	}
	
	/**
	 * 检查腊八活动时间：排行榜是否开启
	 * @return
	 */
	public boolean checkTopEffectivePeriod () {
		Date now = new Date();
		long time = now.getTime();
		if (time < TwelfthLunarConfig.topStartDate.getTime() || time >= TwelfthLunarConfig.topEndDate.getTime()) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 检查腊八活动时间：是否可以得到腊八豆、可否捐献
	 * @return
	 */
	public boolean checkDonateEffectivePeriod () {
		Date now = new Date();
		long time = now.getTime();
		if (time < TwelfthLunarConfig.startDate.getTime() || time >= TwelfthLunarConfig.endDate.getTime()) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 重置数据
	 * @param index
	 */
	public void setResetData (int index) {
		setActivityGiftId(TwelfthLunarConfig.twelfthLunarActivityConfig[index].getTwelfthLunar().getGiftId());
		setActivityGiftCount(TwelfthLunarConfig.twelfthLunarActivityConfig[index].getTwelfthLunar().getCount());
		setActivityLevel(TwelfthLunarConfig.twelfthLunarActivityConfig[index].getTwelfthLunar().getLevel());
	}
	
	/**
	 * 活动通知
	 * @param start
	 * @param segment
	 */
	public void sendActivityChat (boolean start, int segment) {
		if(chatService != null) {
			Date todayStart = Utils.getTodayStartDate();
			Date endTime = TwelfthLunarConfig.getConfigDate(false, 0, 0, 0, TwelfthLunarConfig.endYear, TwelfthLunarConfig.endMonth, TwelfthLunarConfig.endDay, 0, 0);
			if (!start && todayStart.equals(endTime) && (segment + 1) == TwelfthLunarConfig.segment) {
				String	msg = "时光荏苒，到了该说再见的时候了，让我们记住这次捐豆的英雄们吧！咱们明年再见。";
				chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
			} else {
				String msg = getActivityMessage(start, segment);
				if (msg != null) {
					chatService.sendRoarMessage( -1, "狮子吼", msg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
				}
			}
		}
	}
	/**
	 * 通报表扬
	 * @param start
	 * @param segment
	 */
	public void sendTopChat () {
		setTopList();
		List<TwelfthLunarShowInfo> topList = getTopList();
		String topMsg = "捐豆排行榜实时通报：";
		String[] name = new String[TwelfthLunarConfig.topPraise];
		if (topList != null) {
			for (int i = 0; i < topList.size() && i < TwelfthLunarConfig.topPraise; i ++) {
				Map.Entry<Integer, TwelfthLunarShowInfo> infoMap = (Entry<Integer, TwelfthLunarShowInfo>) topList.get(i);
				TwelfthLunarShowInfo showInfo = infoMap.getValue();
				name[i] = showInfo.getPlayerName();
			}
		}
		if (name[0] != null && !name[0].equals("")) {
			topMsg += name[0] + "勇夺第一";
		} else {
			topMsg = null;
		}
		if (topMsg != null) {
			if (name[1] != null && !name[1].equals("")) {
				topMsg += "，" + name[1] + "名列第二";
				if (name[2] != null && !name[2].equals("")) {
					topMsg += "，" + name[2] + "紧随其后。让我们为他们的善行敬礼！";
				} else {
					topMsg += "，让我们为他们的善行敬礼！";
				}
			} else {
				topMsg += "，让我们为他的善行敬礼！";
			}
		}
		if (topMsg != null) {
			chatService.sendRoarMessage( -1, "狮子吼", topMsg, true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
		} else {
			chatService.sendRoarMessage( -1, "狮子吼", "没有人捐豆就无法熬出美味的腊八粥了，世间自有真情在，让我们为自己也为别人捐献自己的一份力量。", true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0);
		}
	}
	
	/**
	 * 获得活动的通知
	 * @param start
	 * @param segment
	 * @return
	 */
	public String getActivityMessage (boolean start, int segment) {
		if (segment >= 0 && segment < TwelfthLunarConfig.twelfthLunarActivityConfig.length) {
			if (start) {
				return TwelfthLunarConfig.twelfthLunarActivityConfig[segment].getStartMessage();
			} else {
				return TwelfthLunarConfig.twelfthLunarActivityConfig[segment].getEndMessage();
			}
		} else {
			return null;
		}
	}
	
	/**
	 * 设置当前活动次数 -1无活动
	 * @param segment
	 */
	public void setCurrentSegment (int segment) {
		currentSegment = segment;
	}
	
    /**
     * 检查活动是否开始
     */
    public boolean checkStarted () {
    	if (currentSegment > STAGE_NOT_STARTED) {
    		return true;
    	}
		return false;
    }
    
    /**
     * 检查玩家消费是否达到要求
     * @param player
     * @param consumer
     */
    public void checkPlayerConsumer (WorldPlayer player, int consumer, ConnectSession connectSession) {
    	player.setTwelfthLunarConsumer(player.getTwelfthLunarConsumer() + consumer);
    	int count = 0;
    	while (player.getTwelfthLunarConsumer() >= TwelfthLunarConfig.donateConsumer * Server.consumerType) {
    		player.setTwelfthLunarConsumer(player.getTwelfthLunarConsumer() - TwelfthLunarConfig.donateConsumer * Server.consumerType);
			sendDonateItem(player);
			count ++;
    	}
//    	if (count > 0) {
//    		int money = TwelfthLunarConfig.donateConsumer * count / TwelfthLunarConfig.donateBeanCount;
//    		connectSession.sendMessage(player.getId(),"恭喜您累积消费了" + money + "元，获得了" + TwelfthLunarConfig.donateBeanCount * count + "个腊八豆，物品已发至邮箱，请注意查收！");
//    	}
    }
    
    /**
     * 消费满RMB50以邮件方式发送腊八豆
     * @param player
     */
    public void sendDonateItem (WorldPlayer player) {
		IItem iit = Items.getTemplate(TwelfthLunarConfig.donateItemId).newInstance();
		if (iit != null) {
			byte[] att = ItemUtils.item2dbAttachment(iit, TwelfthLunarConfig.donateBeanCount);
			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
					iit.getName() + "*" + TwelfthLunarConfig.donateBeanCount, "恭喜您获得了我们赠送的" + TwelfthLunarConfig.donateBeanCount + "个腊八豆，祝您消费愉快。（每消费满" + TwelfthLunarConfig.donateConsumer + "元送" + TwelfthLunarConfig.donateBeanCount + "个腊八豆）", att, 0, true);
			log.info("TwelfthLunarService sendDonateItem to playerID[" + player.getId()
					+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
					iit.getItemId() + "] ItemName[" + iit.getName() + "] ItemCount[" + TwelfthLunarConfig.donateBeanCount
					+ "] Remain PlayerTwelfthLunarConsumer[" + player.getTwelfthLunarConsumer() + "] iMoneyType[" + Server.consumerType + "]");
		} else {
			log.info("TwelfthLunarService sendDonateItem failure: Error! Item is " + TwelfthLunarConfig.donateItemId);
		}
    }
    
    /**
     * 腊八消费活动
     */
    public void addTwelfthLunarActivity (int cost, WorldPlayer player, StoreService.Request request, ConnectSession connectSession) {
    	if (checkDonateEffectivePeriod()) {
        	if (cost > 0) {
        		if (player != null) {
        			synchronized (player) {
        				checkPlayerConsumer(player, request.price / 100, connectSession);
        			}
        		}
        	}
        }
    }
    
    /**
     * 食神活动的状态
     * @param stage
     */
    public void setStage (int stage) {
    	TwelfthLunarService.stage = stage;
    }
    /**
     * 食神活动排行榜状态
     * @param topStage
     */
    public void setTopStage (int topStage) {
    	TwelfthLunarService.topStage = topStage;
    }
    
    /**
     * 设置食神活动状态，判断是否可以自动发奖
     */
    public void setIronChefStage () {
		Date date_tmp = new Date();
		if (date_tmp.getTime() < TwelfthLunarConfig.startDate.getTime()) {
			setStage(STAGE_NOT_STARTED);
		} else if (date_tmp.getTime() >= TwelfthLunarConfig.startDate.getTime()
				&& date_tmp.getTime() < TwelfthLunarConfig.endDate.getTime()) {
			setStage(STAGE_DONATE_STARTED);
		}
		
		if (date_tmp.getTime() < TwelfthLunarConfig.topStartDate.getTime()) {
			setTopStage(STAGE_NOT_STARTED);
		} else if (date_tmp.getTime() >= TwelfthLunarConfig.topStartDate.getTime()
				&& date_tmp.getTime() < TwelfthLunarConfig.topEndDate.getTime()) {
			setTopStage(STAGE_TOP_STARTED);
		} else {
			setTopStage(STAGE_TOP_END);
		}
	}
    
    public void setAllStage () {
    	Date date_tmp = new Date();
    	if (date_tmp.getTime() >= TwelfthLunarConfig.endDate.getTime()) {
    		setStage(STAGE_DONATE_END);
    	}
    	setIronChefStage();
    }
    
    /**
	 * 获得食神活动的状态（主要用于整个活动结束后自动发奖）
	 */
    public int getStage() {
    	return stage;
    }
    /**
	 * 获得食神活动排行榜的状态（主要排行榜）
	 */
    public int getTopStage () {
    	return topStage;
    }
    
    /**
	 * 捐赠物品
	 * @param player
	 * @param count
	 */
	public void playerDonateItem (WorldPlayer player, int count) {
		synchronized (TwelfthLunarConfig.playerDonateMap){
			if (TwelfthLunarConfig.playerDonateMap.containsKey(player.getId())) {
				TwelfthLunarShowInfo tsi = (TwelfthLunarShowInfo) TwelfthLunarConfig.playerDonateMap.get(player.getId());
				tsi.setCount(tsi.getCount() + count);
				TwelfthLunarConfig.playerDonateMap.put(player.getId(), tsi);
			} else {
				TwelfthLunarShowInfo tsi = new TwelfthLunarShowInfo (player.getId(), player.getLevel(), player.getPlayerName(), count);
				TwelfthLunarConfig.playerDonateMap.put(player.getId(), tsi);
			}
		}
	}
	
	/**
	 * 设置食神活动排行榜
	 */
	public void setTopList () {
		TwelfthLunarConfig.topList = sort();
	}
	
	/**
	 * 获得所有捐献腊八豆的玩家信息
	 * @return
	 */
	public Map<Integer, TwelfthLunarShowInfo> getDonateItemPlayer () {
		return TwelfthLunarConfig.playerDonateMap;
	}
	
	/**
	 * 获得特定捐献腊八豆的玩家信息
	 * @return
	 */
	public int getPlyaerDonateCount (int playerId) {
		int ret = 0;
		if (TwelfthLunarConfig.playerDonateMap.get(playerId) != null) {
			ret = TwelfthLunarConfig.playerDonateMap.get(playerId).getCount();
		}
		return ret;
	}
	
	/**
	 * 排序
	 * @param type
	 * @return List
	 */
	public List sort () {
		Map<Integer,TwelfthLunarShowInfo> map_Data = null;
		map_Data = getDonateItemPlayer();
		List<Map.Entry<Integer, TwelfthLunarShowInfo>> list_Data = new ArrayList<Map.Entry<Integer, TwelfthLunarShowInfo>>(map_Data.entrySet());
		Collections.sort(list_Data, new Comparator<Map.Entry<Integer, TwelfthLunarShowInfo>> () {
			public int compare(Map.Entry<Integer, TwelfthLunarShowInfo> o1, Map.Entry<Integer, TwelfthLunarShowInfo> o2){
				return (o2.getValue().getCount() - o1.getValue().getCount());
			}
		});
		return list_Data;
	}
    
	/**
	 * 获得排行榜
	 * @param type
	 * @return List
	 */
	public List<TwelfthLunarShowInfo> getTopList () {
		return TwelfthLunarConfig.topList;
	}
	
    /**
     * 自动发奖
     * @throws Exception 
     */
    public void prizesAfterEvent () throws Exception {
    	setIronChefStage();
		if (getStage() == STAGE_DONATE_STARTED) {
			Date date_tmp = new Date();
			if (date_tmp.getTime() >= TwelfthLunarConfig.endDate.getTime()) {
				setStage(STAGE_DONATE_END);
				setTopList();
				List<TwelfthLunarShowInfo> topList = getTopList();
				if (topList != null) {
					for (int i = 0; i < topList.size() && i < TwelfthLunarConfig.topPraise; i ++) {
						boolean mark = false;
						Map.Entry<Integer, TwelfthLunarShowInfo> infoMap = (Entry<Integer, TwelfthLunarShowInfo>) topList.get(i);
						TwelfthLunarShowInfo tsi = infoMap.getValue();
//						WorldPlayer player = playerService.getWorldPlayer(tsi.getId());
//						if (player == null) {
//							player = playerService.loadWorldPlayer(tsi.getId());
//							mark = true;
//						}
						WorldPlayer player = playerService.getWorldPlayerAndCatch(tsi.getId());
						if (player != null) {
							log.info("TwelfthLunarService TopList playerID[" + tsi.getId()
									+ "] playerName[" + player.getPlayerName() + "] DonateItemCount["
									+ tsi.getCount() + "]");
							int itemId = TwelfthLunarConfig.twelfthLunarConfigMap.get(i + 1);
							IItem iit = Items.getTemplate(itemId).newInstance();
							byte[] att = ItemUtils.item2dbAttachment(iit, 1);
							String message = null;
							switch(i){
							case 0:
								message = "您在这次腊八活动中大发善心在捐献榜获得了第一名，赢得了“全球最杰出好人”的称号";
								break;
							case 1:
								message = "您在这次腊八活动中大发善心在捐献榜获得了第二名，赢得了“全国最杰出好人”的称号";
								break;
							case 2:
								message = "您在这次腊八活动中大发善心在捐献榜获得了第三名，赢得了“你真是个好人”的称号";
								break;
							default:
								message = "您在这次腊八活动中大发善心在捐献榜有排名，这是奖励";
							}
							mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
									iit.getName() + "*" + 1, message, att, 0, true);
							log.info("TwelfthLunarService topListSendMail to playerID[" + player.getId()
									+ "] playerName[" + player.getPlayerName() + "] get ItemId[" +
									iit.getItemId() + "] ItemName [" + iit.getName() + "] ItemCount[" + 1 + "]");
//							if (mark) {
//								playerService.savePlayer(player);
//							}
						}
						playerService.releasePlayer(player);
					}
				}
			}
		}
    }
    
    public static void saveIronChefActivityXml (Map<Integer, TwelfthLunarShowInfo> playerDonateMap) {
    	Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("IronChefActivities");
        for (Integer s : playerDonateMap.keySet()) {
            Element elem = root.addElement("IronChefActivity");
            elem.addAttribute("id", s.toString());
            elem.addAttribute("level", String.valueOf(playerDonateMap.get(s).getLevel()));
            elem.addAttribute("playerName", String.valueOf(playerDonateMap.get(s).getPlayerName()));
            elem.addAttribute("count", String.valueOf(playerDonateMap.get(s).getCount()));
        }
        Element elem = root.addElement("Gruel");
        elem.addAttribute("GruelCount", String.valueOf(TwelfthLunarConfig.gruelCount));
        try {
			saveDocument(doc, new FileWriter("IronChefActivity.xml"));
		} catch (IOException e) {
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
}
