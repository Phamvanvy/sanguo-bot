package peony.service.account;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.game.itemeffect.AddItemEffect;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.activity.NewServerAreaActivity;
import peony.service.sleepycat.SleepyCatService;

import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

public class ChargeActivityService implements Service {

	public static String PROPERTY_FIRSTCHARGE_CREATEED = "firstcharge";// 首充额度
	public static String PROPERTY_FIRSTCHARGE_CHARGEANDREWARD = "firstchargegetreward";// 首充是否领取奖励标识
	public static String PROPERTY_FIRSTCHARGE_CREATETIME = "firstchargecreatetime";// 首充完成时间，开始记录累充时间
	public static String PROPERTY_ACCUMULATECHARGE_CREATED = "accumulatechargecreated";// 累充已领取奖励额度
	public static String PROPERTY_ACCUMULATECHARGE_CHARGEANDREWARD = "accumulatechargegetreward";// 累充是否领取奖励标识
	public static String PROPERTY_ACCUMULATECHARGE_LASTTIME = "accumulatelasttime";// 累充剩余时间
	
	public static String PROPERTY_CHARGE_TOTAL = "chargetotal";		//一共充值额度
	
	public static final int FIRST_STATE = 1;	//显示首冲奖励
	public static final int MUL_STATE = 1<<1;	//显示累充奖励
	public static final int NEW_STATE = 1<<2;	//显示新区活动
	
	public static final int REWARD_JEWEL_CNT = 7;	//新区 活动奖励宝石数量
	
	public static final int ITEM_ID_FIRSTCHARGE = 1615;	//首冲四级宝石奖励
	
	
	public static Map<Integer, FirstCharge> firstChargeList = new HashMap<Integer, FirstCharge>();
	public static long FIFTEEN_DAY = 15 * 24 * 60 * 60 * 1000L;
	public Map<String,ChargeConfig> revision2Config = new HashMap<String,ChargeConfig>();
	
	public static boolean newAreaActEnd = false;	//新区活动是否结束(true的时候可以领取新区奖励)
	
	/** 获取首充奖励 */
	public void getFirstChargeReward(FirstCharge firstCharge, Player player, int ammount) {
		ChargeConfig config = getConfigByRevision();
		if(config!=null){
			float itemCnt = ammount/12.5f;
			int itemCount = (int)itemCnt;
			if(itemCnt > itemCount){
				itemCount++;
			}
			
			GameItem item = null;
			int[] FIRSTCHARGE_REWARD = config.firstchargeReward;
			if(!Server.server.revision.equals(Server.REVISION_TYPE_TW)){
				ammount *= 10;
			}
			if(ammount >= config.firstchargeValue){
				item = ObjectAccessor.createGameItem(FIRSTCHARGE_REWARD[1]);
			}else{
				item = ObjectAccessor.createGameItem(FIRSTCHARGE_REWARD[0]);
			}
			if (item != null) {
				PlayerTransaction tx = player.newTransaction("FIRSTCHARGE");
				try{
					player.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
				}catch(NoEnoughSpaceException e){
					tx.rollback();
			    	MailService mailService = Server.server.getServiceRegistry().getMailService();
			    	mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "首充奖励", "恭喜您充值成功，大奖在此，快速速收下吧，祝您游戏愉快。", 0, 
			    			item, 1, "FIRSTCHARGE");
			    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您的背包已满，首充奖励已通过飞鸽发送");
				}
				
		    	GameItem firstItem = ObjectAccessor.createGameItem(ITEM_ID_FIRSTCHARGE);
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				int pages = itemCount/100;
				for(int i=0; i<pages; i++){
					mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "首次充值送宝石活动奖励", "感谢您的参与，请查收附件，更多精彩敬请关注游戏内线上公告", 0, 
			    			firstItem, 100, "FIRSTCHARGEJEWEL");
				}
				int lastCnt = itemCount%100;
				if(lastCnt > 0){
					mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "首次充值送宝石活动奖励", "感谢您的参与，请查收附件，更多精彩敬请关注游戏内线上公告", 0, 
			    			firstItem, lastCnt, "FIRSTCHARGEJEWEL");
				}
				
				String msg = MessageFormat.format("{0}从首次充值活动中获得{1}!/-6", player.name, item.template.name);
			    Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
				
				firstCharge.pool.setInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 1);
				long leftTime = System.currentTimeMillis()- firstCharge.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME,0);
				int newDay = -1;
				if(leftTime<ChargeActivityService.FIFTEEN_DAY){
					newDay = (int) ((ChargeActivityService.FIFTEEN_DAY - leftTime) / (24 * 60 * 60 * 1000));
					if (newDay < 15)
						newDay ++;
				}
				player.addIntPropertyChangedItem(ChangedItem.ACTIVITY_STATE, getActivityState(player), false, true);
				addFirstCharge(player.accountId, firstCharge);
				player.message(-1, "领取奖励成功！", -1, -1);
			}
		}
	}

	/** 获取累充奖励 */
	public void getAccumulateChargeReward(FirstCharge firstCharge, Player player) {
		int getReward = 0;
		int hasGetMoney = firstCharge.pool.getInt(PROPERTY_ACCUMULATECHARGE_CREATED, 0);
		ChargeConfig config = getConfigByRevision();
		
		int ammount = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_CHARGE_TOTAL, 0);
		if(!Server.server.revision.equals(Server.REVISION_TYPE_TW)){
			ammount *= 10;
		}
		if(config!=null){
			int[] ACCUMULATE_MONEY = config.chargeValue;
			int[] ACCUMULATE_REWARD = config.chargeReward;
			if(getMulChargeMax(player.accountId) >= ACCUMULATE_MONEY.length){
				ErrorHandler.sendErrorMessage(player.session, -1, OpCode.CHARGEACTIVITY_GETREWARD_CLIENT, "您已经领取累计充值奖励");
				return;
			}
			
			for (int i = 0; i < ACCUMULATE_MONEY.length; i++) {
				GameItem item = null;
				if (ammount < ACCUMULATE_MONEY[i])
					break;
				if (getMulChargeMax(player.accountId) > i)
					continue;
				item = ObjectAccessor.createGameItem(ACCUMULATE_REWARD[i]);
				if (item != null) {
					PlayerTransaction tx = player.newTransaction("ACCUMULATECHARGE");
					try{
						player.bag.addGameItemComplete(item, 1, tx, true);
						tx.commit();
					}catch(NoEnoughSpaceException e){
						tx.rollback();
				    	MailService mailService = Server.server.getServiceRegistry().getMailService();
				    	mailService.sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "累计充值奖励", "恭喜您充值成功，大奖在此，快速速收下吧，祝您游戏愉快。", 0, 
				    			item, 1, "ACCUMULATECHARGE");
				    	Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, "您的背包已满，累计充值奖励已通过飞鸽发送");
					}
					hasGetMoney = ACCUMULATE_MONEY[i];
					firstCharge.pool.setInt(PROPERTY_ACCUMULATECHARGE_CREATED, hasGetMoney);
					getReward++;
					String msg = MessageFormat.format("{0}从累计充值活动中获得{1}!/-6", player.name, item.template.name);
				    Server.server.getServiceRegistry().getChatService().sendWorldMessage(msg);
				}
			}
			if (getReward > 0) {
				player.message(-1, "领取奖励成功！", -1, -1);
				if (hasGetMoney >= ACCUMULATE_MONEY[ACCUMULATE_MONEY.length - 1]) {
					//firstCharge.pool.remove(PROPERTY_FIRSTCHARGE_CREATEED);
					player.addIntPropertyChangedItem(ChangedItem.ACTIVITY_STATE, getActivityState(player), false, true);
				}
				addFirstCharge(player.accountId, firstCharge);
			}else{
				ErrorHandler.sendErrorMessage(player.session, -1, OpCode.CHARGEACTIVITY_GETREWARD_CLIENT, "充值元宝数不足，不能领取奖励");
			}
		}
	}
	
	
	public void shutdown() {

	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("fistcharge.xml");
        Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
        parse(doc);
        loadNewArea();
	}
	
    public void parse(Document doc){
    	Element root = doc.getRootElement();
		if (root != null) {
			List charges = root.elements("type");
			for (int i = 0; i < charges.size(); i++) {
				String revision = ((Element) charges.get(i))
				.attributeValue("revision");
				ChargeConfig config = new ChargeConfig(revision);
				List firstcharge = ((Element) charges.get(i)).elements("firstcharge");
				int chargeValue = Integer.parseInt(((Element) firstcharge.get(0))
						.attributeValue("chargevalue"));
				String firstchargeReward = ((Element) firstcharge.get(0))
						.attributeValue("reward");
				String[] str0 = firstchargeReward.split(",");
				config.firstchargeReward = new int[str0.length];
				for(int j=0;j<str0.length;j++){
					config.firstchargeReward[j] = Integer.parseInt(str0[j]);
				}
				config.firstchargeValue = chargeValue;
				List accumulateCharge = ((Element) charges.get(i)).elements("accumulatecharge");
				String accuChargeValue = ((Element) accumulateCharge.get(0))
						.attributeValue("chargevalue");
				String[] str = accuChargeValue.split(",");
				config.chargeValue = new int[str.length];
				for(int j=0;j<str.length;j++){
					config.chargeValue[j] = Integer.parseInt(str[j]);
				}
				String rewards = ((Element) accumulateCharge.get(0)).attributeValue("rewards");
				String[] str2 = rewards.split(",");
				config.chargeReward = new int[str2.length];
				for(int j=0;j<str2.length;j++){
					config.chargeReward[j] = Integer.parseInt(str2[j]);
				}
				revision2Config.put(revision, config);
			}
		}
    }
    
    public void loadConfigs(){
    	byte[] bytes = Server.server.getServiceRegistry().getDataService().data
		.findFile("fistcharge.xml");
        Document doc;
		try {
			doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
        	Element root = doc.getRootElement();
    		if (root != null) {
    			List charges = root.elements("type");
    			for (int i = 0; i < charges.size(); i++) {
    				String revision = ((Element) charges.get(i))
    				.attributeValue("revision");
    				ChargeConfig config = new ChargeConfig(revision);
    				List firstcharge = ((Element) charges.get(i)).elements("firstcharge");
    				int chargeValue = Integer.parseInt(((Element) firstcharge.get(0))
    						.attributeValue("chargevalue"));
    				String firstchargeReward = ((Element) firstcharge.get(0))
    						.attributeValue("reward");
    				String[] str0 = firstchargeReward.split(",");
    				config.firstchargeReward = new int[str0.length];
    				for(int j=0;j<str0.length;j++){
    					config.firstchargeReward[j] = Integer.parseInt(str0[j]);
    				}
    				config.firstchargeValue = chargeValue;
    				List accumulateCharge = ((Element) charges.get(i)).elements("accumulatecharge");
    				String accuChargeValue = ((Element) accumulateCharge.get(0))
    						.attributeValue("chargevalue");
    				String[] str = accuChargeValue.split(",");
    				config.chargeValue = new int[str.length];
    				for(int j=0;j<str.length;j++){
    					config.chargeValue[j] = Integer.parseInt(str[j]);
    				}
    				String rewards = ((Element) accumulateCharge.get(0)).attributeValue("rewards");
    				String[] str2 = rewards.split(",");
    				config.chargeReward = new int[str2.length];
    				for(int j=0;j<str2.length;j++){
    					config.chargeReward[j] = Integer.parseInt(str2[j]);
    				}
    				revision2Config.put(revision, config);
    			}
    		}
		} catch (Exception e) {
			
		}
    }
    
    public ChargeConfig getConfigByRevision(){
    	ChargeConfig config = null;
    	if(Server.isAppSection){
			config = revision2Config.get("APP");
		}else{
			config = revision2Config.get(Server.server.revision);
			if(config==null){
				config = revision2Config.get("PIP");
			}
		}
    	return config;
    }

	public void addFirstCharge(int accountId, FirstCharge firstCharge) {
		firstChargeList.put(accountId, firstCharge);
	}

	/** 获取首充实例 */
	public FirstCharge getFirstCharge(int accountId, boolean loadDb) {
		if (firstChargeList.containsKey(accountId)) {
			return firstChargeList.get(accountId);
		} else {
			if (loadDb) {
				FirstChargeDao firstChargeDao = Server.server
						.getServiceRegistry().getDbService().firstChargeDao;
				List<FirstCharge> charges = firstChargeDao.getFirstChargeByAccountId(accountId);
				if(charges!=null){
					if(charges.size()==1){
						FirstCharge firstCharge = charges.get(0);
						if (firstCharge != null) {
							addFirstCharge(accountId, firstCharge);
							return firstCharge;
						}
					}else if(charges.size()>1){
						//处理合服首充逻辑
						FirstCharge firstCharge1 = charges.get(0);
						FirstCharge firstCharge2 = charges.get(1);
						FirstCharge firstChargeMain = firstCharge1.id<firstCharge2.id ? firstCharge1 : firstCharge2;
						FirstCharge firstChargeOther = firstCharge1.id>firstCharge2.id ? firstCharge1 : firstCharge2;
						//首充逻辑
						boolean hasFirstChargeMain = firstChargeMain.pool.getLong(PROPERTY_FIRSTCHARGE_CREATETIME, 0) > 0;
						boolean	hasFirstChargeOther = firstChargeOther.pool.getLong(PROPERTY_FIRSTCHARGE_CREATETIME, 0) > 0;
						if(!hasFirstChargeOther){
							firstChargeDao.makeTransient(firstChargeOther);
							addFirstCharge(accountId, firstChargeMain);
							return firstChargeMain;
						}else if(!hasFirstChargeMain){
							firstChargeDao.makeTransient(firstChargeMain);
							addFirstCharge(accountId, firstChargeOther);
							return firstChargeOther;
						}else{
							//主副区都有首充记录
							boolean hasOverTimeMain = false;
							boolean hasOverTimeOther = false;
							long leftTime = System.currentTimeMillis() - firstChargeMain.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, 0);
					    	if(leftTime>ChargeActivityService.FIFTEEN_DAY){
					    		hasOverTimeMain = true;
					    	}
					    	leftTime = System.currentTimeMillis() - firstChargeOther.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, 0);
					    	if(leftTime>ChargeActivityService.FIFTEEN_DAY){
					    		hasOverTimeOther = true;
					    	}
					    	int hasGetMoneyMain = firstChargeMain.pool.getInt(
				    				PROPERTY_ACCUMULATECHARGE_CREATED, 0);
				    		int hasGetMoneyOhter = firstChargeOther.pool.getInt(
				    				PROPERTY_ACCUMULATECHARGE_CREATED, 0);
				    		int maxMoney = hasGetMoneyMain>hasGetMoneyOhter?hasGetMoneyMain:hasGetMoneyOhter;
				    		Date startTime = new Date();
				    		Date endTime = new Date();
				    		long firstChargeTimeMain = firstChargeMain.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME,0);
				    		long firstChargeTimeOther = firstChargeOther.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME,0);
					    	if(hasOverTimeMain && hasOverTimeOther){
					    		if(firstChargeOther.pool.getInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 0)!=0){
					    			firstChargeMain.pool.setInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 1);
					    		}
					    		firstChargeDao.makeTransient(firstChargeOther);
					    		addFirstCharge(accountId, firstChargeMain);
					    		return firstChargeMain;
					    	}else if(hasOverTimeMain && !hasOverTimeOther){
					    		firstChargeOther.pool.setInt(PROPERTY_ACCUMULATECHARGE_CREATED, maxMoney);
					    		startTime.setTime(firstChargeTimeMain);
				    			endTime.setTime(firstChargeTimeOther);
				    			int firstchargeMoney = Server.server.getServiceRegistry().getDbService().chargeDao.getAccumulateCharge(accountId, startTime, endTime);
				    			firstchargeMoney += firstChargeMain.pool.getInt(PROPERTY_FIRSTCHARGE_CREATEED, 0);
				    			firstChargeOther.pool.setInt(PROPERTY_FIRSTCHARGE_CREATEED, firstchargeMoney);
				    			if(firstChargeMain.pool.getInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 0)!=0){
				    				firstChargeOther.pool.setInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 1);
					    		}
					    		firstChargeDao.makeTransient(firstChargeMain);
					    		addFirstCharge(accountId, firstChargeOther);
					    		return firstChargeOther;
					    	}else if(!hasOverTimeMain && hasOverTimeOther){
					    		firstChargeMain.pool.setInt(PROPERTY_ACCUMULATECHARGE_CREATED, maxMoney);
					    		startTime.setTime(firstChargeTimeOther);
				    			endTime.setTime(firstChargeTimeMain);
				    			int firstchargeMoney = Server.server.getServiceRegistry().getDbService().chargeDao.getAccumulateCharge(accountId, startTime, endTime);
				    			firstchargeMoney += firstChargeOther.pool.getInt(PROPERTY_FIRSTCHARGE_CREATEED, 0);
				    			firstChargeMain.pool.setInt(PROPERTY_FIRSTCHARGE_CREATEED, firstchargeMoney);
				    			if(firstChargeOther.pool.getInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 0)!=0){
				    				firstChargeMain.pool.setInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 1);
					    		}
					    		firstChargeDao.makeTransient(firstChargeOther);
					    		addFirstCharge(accountId, firstChargeMain);
					    		return firstChargeMain;
					    	}else{
					    		long firstChargeTime = firstChargeTimeMain<firstChargeTimeOther?firstChargeTimeMain:firstChargeTimeOther;
					    		firstChargeMain.pool.setInt(PROPERTY_ACCUMULATECHARGE_CREATED, maxMoney);
					    		if(firstChargeTime == firstChargeTimeOther){
					    			startTime.setTime(firstChargeTimeOther);
					    			endTime.setTime(firstChargeTimeMain);
					    			int firstchargeMoney = Server.server.getServiceRegistry().getDbService().chargeDao.getAccumulateCharge(accountId, startTime, endTime);
					    			firstchargeMoney += firstChargeOther.pool.getInt(PROPERTY_FIRSTCHARGE_CREATEED, 0);
					    			firstChargeMain.pool.setInt(PROPERTY_FIRSTCHARGE_CREATEED, firstchargeMoney);
					    		}else{
					    			startTime.setTime(firstChargeTimeMain);
					    			endTime.setTime(firstChargeTimeOther);
					    			int firstchargeMoney = Server.server.getServiceRegistry().getDbService().chargeDao.getAccumulateCharge(accountId, startTime, endTime);
					    			firstchargeMoney += firstChargeMain.pool.getInt(PROPERTY_FIRSTCHARGE_CREATEED, 0);
					    			firstChargeMain.pool.setInt(PROPERTY_FIRSTCHARGE_CREATEED, firstchargeMoney);
					    			firstChargeMain.pool.setLong(PROPERTY_FIRSTCHARGE_CREATETIME, firstChargeTimeOther);
					    		}
					    		if(firstChargeOther.pool.getInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 0)!=0){
				    				firstChargeMain.pool.setInt(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD, 1);
					    		}
					    		firstChargeDao.makeTransient(firstChargeOther);
					    		firstChargeDao.updateEntity(firstChargeMain);
					    		addFirstCharge(accountId, firstChargeMain);
					    		return firstChargeMain;
					    	}
						}
					}
				}
			}
		}
		return null;
	}

	/** 首充或累充界面显示 */
	public void chargeActivityUI(ClientSession session, Packet packet) {
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player) session.getClient();
		if (player != null) {
			ChargeConfig config = getConfigByRevision();
			if(config!=null){
				FirstCharge firstCharge = getFirstCharge(player.accountId, false);
				Packet pt = new Packet(OpCode.CHARGEACTIVITY_UIINFO_SERVER);
				pt.putInt(serial);
				int chargeMoney = 0;
				int ammount = 0;	//首充金额
				if(firstCharge != null){
					chargeMoney = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_CHARGE_TOTAL, 0);
					ammount = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED,0);
					if(!Server.server.revision.equals(Server.REVISION_TYPE_TW)){
						ammount *= 10;
						chargeMoney *= 10;
					}
				}
				pt.putInt(chargeMoney);
				ActivityService actService = Server.server.getServiceRegistry().getActivityService();
				Activity act = actService.getActivityByImpClass("NewServerAreaActivity");
				if(act==null || !act.isActive() || !act.isEnabled()){
					pt.put(0);
				}else{
					NewServerAreaActivity nyaAct = (NewServerAreaActivity)act.getImpl();
					pt.put(nyaAct.lastDays);
				}
				if (type == 1) {
					int[] FIRSTCHARGE_REWARD = config.firstchargeReward;
					pt.putInt(FIRSTCHARGE_REWARD.length);
					for (int i = 0; i < FIRSTCHARGE_REWARD.length; i++) {
						GameItem item = ObjectAccessor.createGameItem(FIRSTCHARGE_REWARD[i]);
						ItemEffect effect = item.template.useType.effect;
						if (effect instanceof AddItemEffect) {
							AddItemEffect itemEffect = (AddItemEffect) effect;
							int[] itemIds = itemEffect.getItemIds();
							int[] itemCount = itemEffect.getItemCount();
							if(i==0){
								pt.putInt(0);
							}else{
								pt.putInt(config.firstchargeValue);
							}
							
							int state = 0;
							
							if(firstCharge != null){
								if(firstCharge.hasGetFirstGift(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD)){
									if(ammount >= config.firstchargeValue){
										if(i==1){
											state = 2;
										}
									}else{
										if(i==0){
											state = 2;
										}
									}
								}else if(ammount > 0){
									if(ammount >= config.firstchargeValue){
										if(i==1){
											state = 1;
										}
									}else{
										if(i==0){
											state = 1;
										}
									}
								}else{
									
								}
							}
							
							pt.put(state);
							pt.putInt(itemIds.length);
							for (int j = 0; j < itemIds.length; j++) {
								GameItem gameItem = ObjectAccessor
										.createGameItem(itemIds[j]);
								pt.putInt(itemIds[j]);
								pt.putInt(gameItem.template.showType);
								pt.putInt(gameItem.template.showImage);
								pt.putInt(itemCount[j]);
								pt.put(gameItem.template.quality);
								pt.putString(gameItem.template.name);
							}
						}
					}
				} else if (type == 2) {
					int[] rewardStr;
					int[] moneyStr;
					int[] ACCUMULATE_REWARD = config.chargeReward;
					int[] ACCUMULATE_MONEY = config.chargeValue;
					rewardStr = new int[ACCUMULATE_REWARD.length];
					moneyStr = new int[ACCUMULATE_REWARD.length];
					for(int i=0;i<ACCUMULATE_REWARD.length;i++){
						rewardStr[i] = ACCUMULATE_REWARD[i];
						moneyStr[i] = ACCUMULATE_MONEY[i];
					}
					pt.putInt(rewardStr.length);
					for (int i = 0; i < rewardStr.length; i++) {
						GameItem item = ObjectAccessor
								.createGameItem(rewardStr[i]);
						ItemEffect effect = item.template.useType.effect;
						if (effect instanceof AddItemEffect) {
							AddItemEffect itemEffect = (AddItemEffect) effect;
							int[] itemIds = itemEffect.getItemIds();
							int[] itemCount = itemEffect.getItemCount();
							pt.putInt(config.chargeValue[i]);
							
							int state = 0;
							//是否领取了这一个奖励
							if(getMulChargeMax(player.accountId) > i){
								state = 2;
							}else{
								if(chargeMoney>=moneyStr[i]){
									state = 1;
								}
							}
							pt.put(state);
							
							pt.putInt(itemIds.length);
							for (int j = 0; j < itemIds.length; j++) {
								GameItem gameItem = ObjectAccessor
										.createGameItem(itemIds[j]);
								pt.putInt(itemIds[j]);
								pt.putInt(gameItem.template.showType);
								pt.putInt(gameItem.template.showImage);
								pt.putInt(itemCount[j]);
								pt.put(gameItem.template.quality);
	//							pt.putString(gameItem.getDesc());
								pt.putString(gameItem.template.name);
							}
						}
					}
				}else if(type == 0) {	//新区活动
					pt.putInt(1);
					pt.putInt(0);
					pt.put(0);
					pt.putInt(0);
				}
				player.send(pt);
			}
		}
	}
	
	/** 领取充值活动奖励 */
	public void getChargeActReward(ClientSession session, Packet packet) {
		int serial = packet.getInt();
		int type = packet.get();
		Player player = (Player) session.getClient();
		if (player != null) {
			ChargeConfig config = getConfigByRevision();
			if(config != null){
				FirstCharge firstCharge = getFirstCharge(player.accountId, false);
				if(type == 1){	//领取首冲奖励
					if(firstCharge != null){
						if(!firstCharge.hasGetFirstGift(ChargeActivityService.PROPERTY_FIRSTCHARGE_CHARGEANDREWARD)){
							//玩家手动领取
							int ammount = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED,0);
							if(ammount>0){
						       getFirstChargeReward(firstCharge, player, ammount);
							}else{
								ErrorHandler.sendErrorMessage(session, serial, OpCode.CHARGEACTIVITY_GETREWARD_CLIENT, "您还未充值不能领取奖励");
								return;
							}
						}else{
							ErrorHandler.sendErrorMessage(session, serial, OpCode.CHARGEACTIVITY_GETREWARD_CLIENT, "您已经领取首充奖励");
							return;
						}
					}else{
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CHARGEACTIVITY_GETREWARD_CLIENT, "您还未充值不能领取奖励");
						return;
					}
				}else if(type == 2){
					if(firstCharge != null){
						getAccumulateChargeReward(firstCharge, player);
					}else{
						ErrorHandler.sendErrorMessage(session, serial, OpCode.CHARGEACTIVITY_GETREWARD_CLIENT, "您还未充值不能领取奖励");
						return;
					}
				}
				int chargeMoney = 0;
				int ammount = 0;	//首充金额
				if(firstCharge != null){
					chargeMoney = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_CHARGE_TOTAL, 0);
					ammount = firstCharge.pool.getInt(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATEED,0);
					if(!Server.server.revision.equals(Server.REVISION_TYPE_TW)){
						ammount *= 10;
						chargeMoney *= 10;
					}
				}
				Packet pt = new Packet(OpCode.CHARGEACTIVITY_GETREWARD_SERVER);
				pt.putInt(serial);
				if(type == 1){
					int[] FIRSTCHARGE_REWARD = config.firstchargeReward;
					pt.put(FIRSTCHARGE_REWARD.length);
					for (int i = 0; i < FIRSTCHARGE_REWARD.length; i++) {
						int state = 0;
						if(firstCharge != null){
							if(firstCharge.hasGetFirstGift(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD)){
								if(ammount >= config.firstchargeValue){
									if(i==1){
										state = 2;
									}
								}else{
									if(i==0){
										state = 2;
									}
								}
							}else if(ammount > 0){
								if(ammount >= config.firstchargeValue){
									if(i==1){
										state = 1;
									}
								}else{
									if(i==0){
										state = 1;
									}
								}
							}
						}
						pt.put(state);
					}
				}else if(type == 2){
					int[] rewardStr;
					int[] moneyStr;
					int[] ACCUMULATE_REWARD = config.chargeReward;
					int[] ACCUMULATE_MONEY = config.chargeValue;
					rewardStr = new int[ACCUMULATE_REWARD.length];
					moneyStr = new int[ACCUMULATE_REWARD.length];
					for(int i=0;i<ACCUMULATE_REWARD.length;i++){
						rewardStr[i] = ACCUMULATE_REWARD[i];
						moneyStr[i] = ACCUMULATE_MONEY[i];
					}
					pt.put(rewardStr.length);
					for (int i = 0; i < rewardStr.length; i++) {
						int state = 0;
						//是否领取了这一个奖励
						if(getMulChargeMax(player.accountId) > i){
							state = 2;
						}else{
							if(chargeMoney>=moneyStr[i]){
								state = 1;
							}
						}
						pt.put(state);
					}
				}else{
					pt.put(0);
				}
				player.send(pt);
			}else{
				Packet pt = new Packet(OpCode.CHARGEACTIVITY_GETREWARD_SERVER);
				pt.putInt(serial);
				pt.put(0);
				player.send(pt);
			}
			player.addIntPropertyChangedItem(ChangedItem.ACTIVITY_STATE, getActivityState(player), false, true);
		}
	}
	
	/** 获取活动状态 */
	public int getActivityState(Player p) {
		int actState = 0;
		FirstCharge firstCharge = getFirstCharge(p.accountId, false);
		if (firstCharge != null) {
			//首冲奖励
			if(!firstCharge.hasGetFirstGift(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD)) {
				actState |= FIRST_STATE;
			}
			//累充奖励
			if(!hasGetMulGift(p.accountId)) {
				actState |= MUL_STATE;
			}
		}else{
			actState |= FIRST_STATE;
			actState |= MUL_STATE;
		}
		
		ActivityService actService = Server.server.getServiceRegistry().getActivityService();
		Activity act = actService.getActivityByImpClass("NewServerAreaActivity");
		if((act==null || !act.isActive() || !act.isEnabled())){
			
		}else{
			actState |= NEW_STATE;
		}
		
		return actState;
	}
	
	//累充剩余天数
	public int getAccumulActLast(Player p){
		FirstCharge firstCharge = getFirstCharge(p.accountId, false);
		if (firstCharge != null) {
			if (firstCharge.hasGetFirstGift(PROPERTY_FIRSTCHARGE_CHARGEANDREWARD) && hasGetMulGift(p.accountId)) {
				return -1;
			} else {
				//是否完成首冲
				if (firstCharge.hasGetFirstGift(PROPERTY_FIRSTCHARGE_CREATEED)) {
					long leftTime = System.currentTimeMillis()
							- firstCharge.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, 0);
					if (leftTime < ChargeActivityService.FIFTEEN_DAY) {
						int newDay = (int) ((ChargeActivityService.FIFTEEN_DAY - leftTime) / (24 * 60 * 60 * 1000));
						if (newDay < 15)
							return ++newDay;
						else
							return newDay;
					} else {
						return -1;
					}
				}
			}
		}
		return -1;
	}
	
	public String getDBName() {
		return "NEWSERVERAREAACTIVITY";
	}
	
	//读取新区活动信息
	public void loadNewArea(){
		SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
		Database db = null;
		try {
			db = dbservice.openDatabase(getDBName());
			Cursor cursor = null;
			DatabaseEntry keyEntry = new DatabaseEntry();
			DatabaseEntry dataEntry = new DatabaseEntry();
			try {
				cursor = db.openCursor(null, new CursorConfig());
				if(cursor.getNext(keyEntry, dataEntry, null) != OperationStatus.NOTFOUND){
					String key = StringBinding.entryToString(keyEntry);
					newAreaActEnd = Boolean.parseBoolean(StringBinding.entryToString(dataEntry));
				}
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
		} finally {
			if (db != null) {
				try {
					db.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	public void setNewAreaEnd(boolean isEnd){
		newAreaActEnd = isEnd;
	}
	
	/**
	 * 领取了几个奖励
	 */
	public int getMulChargeMax(int accountId){
		ChargeConfig config = getConfigByRevision();
		if(config != null){
			FirstCharge firstCharge = getFirstCharge(accountId, true);
			if(firstCharge != null){
				long firstChargeTime = firstCharge.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, 0);
				if(firstChargeTime<=0 ){	//还没有首冲
					return 0;
				}
				int hasGetMoney = firstCharge.pool.getInt(PROPERTY_ACCUMULATECHARGE_CREATED, 0);//已经领取的额度
				int[] ACCUMULATE_REWARD = config.chargeReward;
				int[] ACCUMULATE_MONEY = config.chargeValue;
				//已领取奖励
				if(firstCharge.pool.getInt(PROPERTY_ACCUMULATECHARGE_CHARGEANDREWARD, 0) > 0){
					return ACCUMULATE_REWARD.length;
				}
				
				for(int i=0; i<ACCUMULATE_REWARD.length; i++){
		    		if(hasGetMoney < ACCUMULATE_MONEY[i]){
						return i;
					}
				}
				return ACCUMULATE_REWARD.length;
			}
		}
		return 0;
	}
	
	/**
	 * 是否全部领取了累充活动奖励
	 */
	public boolean hasGetMulGift(int accountId){
		ChargeConfig config = getConfigByRevision();
		if(config != null){
			FirstCharge firstCharge = getFirstCharge(accountId, true);
			if(firstCharge != null){
				long firstChargeTime = firstCharge.pool.getLong(ChargeActivityService.PROPERTY_FIRSTCHARGE_CREATETIME, 0);
				if(firstChargeTime<=0 ){	//还没有首冲
					return false;
				}
				long leftTime = System.currentTimeMillis() - firstChargeTime;
				int hasGetMoney = firstCharge.pool.getInt(PROPERTY_ACCUMULATECHARGE_CREATED, 0);//已经领取的额度
				int chargeMoney = firstCharge.pool.getInt(PROPERTY_CHARGE_TOTAL, 0);	//累充额度
				if(!Server.server.revision.equals(Server.REVISION_TYPE_TW)){
					chargeMoney *= 10;
				}
				int[] ACCUMULATE_REWARD = config.chargeReward;
				int[] ACCUMULATE_MONEY = config.chargeValue;
				
				//已领取奖励
				if(firstCharge.pool.getInt(PROPERTY_ACCUMULATECHARGE_CHARGEANDREWARD, 0) > 0){
					return true;
				}
				
				for(int i=0; i<ACCUMULATE_REWARD.length; i++){
			    	if(leftTime>=ChargeActivityService.FIFTEEN_DAY){	//累充活动结束
			    		if(chargeMoney < ACCUMULATE_MONEY[i]){
							return true;
						}
			    	}
		    		if(hasGetMoney < ACCUMULATE_MONEY[i]){
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

}
class ChargeConfig{
	String revision;
	int firstchargeValue;
	int[] firstchargeReward;
	int[] chargeValue;
	int[] chargeReward;
	public ChargeConfig(String revision){
		this.revision = revision;
	}
}
