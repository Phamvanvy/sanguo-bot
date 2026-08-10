package peony.game;

import java.util.Calendar;
import peony.net.Packet;

/**
 * 玩家上班打卡信息。
 * 打卡从周一计时开始，七天后清除所有打卡记录信息。七天周期内按累计打卡天数进行奖励。
 * @author dchen
 */
public class CardPunch {
	
	protected Player player;
	protected static int[] rewards = {3901, 3901, 3902, 3901, 3903, 3901, 3904}; //打卡奖励
	protected static boolean hasInitReward;
	protected static String[] rewardDes = new String[7]; //奖励描述
	protected static final String cardPunchPoolProperty = "CARD_PUNCH";
	protected static final String cardPunchWeekDayPoolProperty = "CARD_PUNCH_WEEKDAY";
	protected static final String cardPunchDayPoolProperty = "CARD_PUNCH_DAY";
	public static Calendar cal = Calendar.getInstance();
	public static boolean newMethod = true;
	
	
	public CardPunch(Player player){
		this.player = player;
		if(!hasInitReward){
			initCardPunchDes();
			hasInitReward = true;
		}
	}
	
	protected static void initCardPunchDes(){
		for(int i=0;i<rewards.length;i++){
			int itemId = rewards[i];
			GameItem item = null;
			try {item = ObjectAccessor.createGameItem(itemId);} catch (Exception e) {}
			rewardDes[i] = item==null ? "" : item.getDesc();
		}
	}
	
	/** 打卡 */
	public void punchCard(int serial) throws Exception{
		if(hasPunch())
			throw new Exception(peony.Messages.STRING_00977);
		int punchs = getPunchs();
		punchs++;
		if(punchs>=8)
			punchs = 1;
		setPunchs(punchs);
		setPunchWeekDay(getWeekDay());
		setPunchDay(getDay());
		int rewardItemId = getRewardItemId();
		PlayerTransaction tx = player.newTransaction("CARDPUNCH");
		GameItem item = ObjectAccessor.createGameItem(rewardItemId);
		if(item!=null){
			try {
				player.bag.addGameItemComplete(item, 1, tx, false);
				tx.commit();
				player.message(-1, peony.Messages.STRING_00978, -1, -1);
			} catch (Exception e) {
				tx.rollback();
				Server.server.getServiceRegistry().getMailService().
					sendSystemMailAsync(player.id, peony.Messages.STRING_00004, peony.Messages.STRING_00979, "", 0, item, 1, "CARDPUNCH");
				player.message(-1, peony.Messages.STRING_00980, -1, -1);
			}
		}else{
			tx.rollback();
		}
		Packet pt = new Packet(OpCode.CARD_PUNCH_SERVER);
		pt.putInt(serial);
		pt.put(getPlayerCurrentStar());
		player.send(pt);
	}
	
	public void updatePunch(){
		int punchWeekDay = player.cardPunch.getPunchWeekDay();
		int punchDay = player.cardPunch.getPunchDay();
		int currentWeekDay = CardPunch.getWeekDay();
		int currentDay = CardPunch.getDay();
		if(currentDay<punchDay)
			punchDay = 0;
		if(punchWeekDay>currentWeekDay){
			setPunchs(0);
		}else if(currentDay-7>=punchDay){
			setPunchs(0);
		}
	}
	
	public boolean hasPunch(){
		int punchWeekDay = getPunchWeekDay();
		int punchDay = getPunchDay();
		int currentWeekDay = getWeekDay();
		int currentDay = getDay();
		if(punchWeekDay==currentWeekDay && punchDay==currentDay)
			return true;
		return false;
	}
	
	public int getPunchs(){
		return player.pool.getInt(cardPunchPoolProperty, 0);
	}
	
	public void setPunchs(int punchs){
		player.pool.setInt(cardPunchPoolProperty, punchs);
	}
	
	public int getPunchWeekDay(){
		return player.pool.getInt(cardPunchWeekDayPoolProperty, 0);
	}
	
	public void setPunchWeekDay(int weekDay){
		player.pool.setInt(cardPunchWeekDayPoolProperty, weekDay);
	}
	
	public int getPunchDay(){
		return player.pool.getInt(cardPunchDayPoolProperty, 0);
	}
	
	public void setPunchDay(int day){
		player.pool.setInt(cardPunchDayPoolProperty, day);
	}
	
	public int getRewardItemId(){
		return rewards[player.pool.getInt(cardPunchPoolProperty, 0)-1];
	}
	
	public byte getPlayerCurrentStar(){
		int punchs = getPunchs();
		switch(punchs){
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
	
	public static String[] getCardPunchDes(){
		return rewardDes;
	}
	
	public static int getWeekDay(){
		int weekDay = 0;
		if(!newMethod){
			Calendar cal = Calendar.getInstance();
			weekDay = cal.get(Calendar.DAY_OF_WEEK);
		}else{
			weekDay = Time.currentWeekDay;
		}
		weekDay--;
		weekDay = weekDay==0 ? 7 : weekDay;
		return weekDay;
	}
	
	public static int getDay(){
		int day = 0;
		if(!newMethod){
			Calendar cal = Calendar.getInstance();
			day = cal.get(Calendar.DAY_OF_YEAR);
		}else{
			day = Time.currentDayOfYear;
		}
		return day;
	}
}
