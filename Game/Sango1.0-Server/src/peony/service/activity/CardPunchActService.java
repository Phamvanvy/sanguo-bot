package peony.service.activity;


import java.util.Date;
import ch.javasoft.util.intcoll.IntHashMap;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.net.Packet;
import peony.service.Service;

public class CardPunchActService implements Service{

//	protected static int[] showRewardId = {4590,4598,4592,4590,4593,4598,4594}; //打卡奖励包
//	protected static int[][] rewardId={{4599,4590},{4598,4599},{4592,0},{4590,4599},{4593,0},{4598,4599},{4594,0}};
	
	//新年狂欢签到奖励包
	protected static int[] showRewardId = {4634,4634,4635,4634,4636,4634,4637}; 
	protected static int[][] rewardId={{4634,2750},{4634,0},{4635,0},{4634,0},{4636,0},{4634,0},{4637,0}};
	protected static String[] rewardDes = new String[7]; //奖励描述
	
	/** 每个账号的领取奖励信息: int[0],领取的数量 int[1],最后一次领取的星期,int[2],最后一次领取的day */
	protected IntHashMap<int[]> accountGifts = new IntHashMap<int[]>();
	protected IntHashMap<int[]> player2CardPunch = new IntHashMap<int[]>();
	protected int lastSaveTime;
	
	public void shutdown() {
		
		
	}

	public void startup() throws Exception {
		initCardPunchGiftDes();
	}
	
	protected static void initCardPunchGiftDes(){
		for(int i=0;i<showRewardId.length;i++){
			int itemId = showRewardId[i];
			GameItem item = null;
			try {item = ObjectAccessor.createGameItem(itemId);} catch (Exception e) {}
			rewardDes[i] = item==null ? "" : item.getDesc();
		}
	}
	
	public static String[] getGiftsDes(){
		return rewardDes;
	}

	
	/** 签到领取奖励 */
	public void getGift(int serial, Player player) throws Exception{
		if(hasGift(player))
			throw new Exception("同一账号每天只能领取一次");
		if(isEnd()){
//			throw new Exception("活动大使正在准备礼物，请您等到今天9:00以后再来签到并领取奖励。");
			throw new Exception("新年活动圆满结束，让我们明年再相见！");
			
		}
		int gifts = getGifts(player);
		gifts++;
//		if(gifts>=8)
//			gifts = 1;
		setGifts(player, gifts);
		setGiftWeekDay(player, getWeekDay());
		setGiftDay(player, getDay());
		int tempGifts = gifts%7;
		int index = 6;
		if(tempGifts!=0){
			index =tempGifts-1;
		}
		int[] itemArr = rewardId[index];
		for(int i=0;i<itemArr.length;i++){
			if(itemArr[i]==0)
				continue;
		    GameItem item = ObjectAccessor.createGameItem(itemArr[i]);
		    PlayerTransaction tx = player.newTransaction("CARDPUNCHACT");
			if(item!=null){
				try {
					player.bag.addGameItemComplete(item, 1, tx, false);
					tx.commit();
					player.message(-1, "恭喜您签到成功，今天的签到礼包已发放到背包，请查收。", -1, -1);
				} catch (Exception e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService().
						sendSystemMailAsync(player.id, peony.Messages.STRING_00004, "签到奖励", "", 0, item, 1, "CARDPUNCHACT");
					player.message(-1, "恭喜您签到成功，因背包已满，奖励已通过飞鸽发放，请查收。", -1, -1);
				}
			}else{
				tx.rollback();
			}
		}
		
		Packet pt = new Packet(OpCode.CARD_PUNCH_SERVER);
		pt.putInt(serial);
		pt.put(getPlayerCurrentStar(player));
		player.send(pt);
		// 每10分钟保存一次记录
//		if (Time.currTime > lastSaveTime + 600000) {
////			saveAccountGiftsData();
//		}	
	}
	
	/** 周期为周二到下个周二,每周二清零 */
	public void updateAnniversaryData(Player player){
		int giftWeekDay = getGiftWeekDay(player);
		int giftDay = getGiftDay(player);
		int currentWeekDay = getWeekDay();
		int currentDay = getDay();
		if(currentDay<giftDay)
			giftDay = 0;
		if(currentDay-7>=giftDay){
			//连续7天未打卡清零
			setGifts(player, 0);
		}else if(currentWeekDay==2 && giftWeekDay!=2){
			setGifts(player, 0);
		}else if(giftWeekDay==1 && currentWeekDay!=1){
			setGifts(player, 0);
		}else if(currentWeekDay!=1 && giftWeekDay>currentWeekDay){
			setGifts(player, 0);
		}
	}
	
	public boolean isEnd(){
		ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
		Activity activity = activityService.getActivityByImpClass(CardPunchActivity.class.getSimpleName());
		Date dateNow = new Date();
		try{
			if(dateNow.before(activity.getSchedule().stopTime)){
				return false;
			}
		}catch(Exception e){
			return true;
		}
		return true;
		
	}

	public byte getPlayerCurrentStar(Player player){
		int gifts = getGifts(player);
		int gs = gifts%7;
		if(gs == 0 && gifts>0){
			gs = 7;
		}
		if(gifts == 0){
			gs = 0;
		}
		switch(gs){
		case 0:
			return 0;
		case 1:
			return 64;
		case 2:
			return 96;
		case 3:
			return 112;
		case 4:
			return 120;
		case 5:
			return 124;
		case 6:
			return 126;
		case 7:
			return 127;
		}
		return 0;
	}
	
	
	protected int getGiftWeekDay(Player player){
		int accountId = player.accountId;
		try {
			return accountGifts.get(accountId)[1];
		} catch (Exception e) {
			return 0;
		}
	}
	
	protected int getGiftDay(Player player){
		int accountId = player.accountId;
		try {
			return accountGifts.get(accountId)[2];
		} catch (Exception e) {
			return 0;
		}
	}
	
	public boolean hasGift(Player player){
		int giftWeekDay = getGiftWeekDay(player);
		int giftDay = getGiftDay(player);
		int currentWeekDay = getWeekDay();
		int currentDay = getDay();
		if(giftWeekDay==currentWeekDay && giftDay==currentDay)
			return true;
		return false;
	}
	
	public int getGifts(Player player){
		int accountId = player.accountId;
		try {
			return accountGifts.get(accountId)[0];
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static int getWeekDay(){
		int weekDay = 0;
		weekDay = Time.currentWeekDay;
		weekDay--;
		weekDay = weekDay==0 ? 7 : weekDay;
		return weekDay;
	}
	
	public static int getDay(){
		int day = 0;
		day = Time.currentDayOfYear;
		return day;
	}
	
	public void setGifts(Player player, int gifts){
		int[] arr = accountGifts.get(player.accountId);
		if(arr==null)
			arr = new int[3];
		arr[0] = gifts;
		accountGifts.put(player.accountId, arr);
	}
	
	public void setGiftWeekDay(Player player, int weekDay){
		int[] arr = accountGifts.get(player.accountId);
		if(arr==null)
			arr = new int[3];
		arr[1] = weekDay;
		accountGifts.put(player.accountId, arr);
	}
	
	public void setGiftDay(Player player, int day){
		int[] arr = accountGifts.get(player.accountId);
		if(arr==null)
			arr = new int[3];
		arr[2] = day;
		accountGifts.put(player.accountId, arr);
	}
	
	public void addAccountGifts(int accountId,int[] arr){
		accountGifts.put(accountId, arr);
	}

}
