package peony.service.welfare;

import java.util.Hashtable;
import peony.game.Player;
/**
 * 每一项福利都继承Welfare 实现自己的 初始化 判断完成 领取奖励 得到当前状态 四个方法
 * @author pmeng
 */
abstract class Welfare {
	
	/** 属性池中标记福利状态 0:未完成  1:完成  2:已领取奖励**/
	public static int NOT_FINISH 	 = 	0;
	public static int ALREADY_FINISH 	= 	1;
	public static int ALREADY_REWARD	 = 	2;
	
	public int welfareTypeId;
	public String welfareTypeName;
	public int welfareId;
	public String welfareName;
	public String welfareDec;
	public String param1;
	public String param2;
	public Hashtable<Integer,Integer> rewardItems = new Hashtable<Integer,Integer>();
	
	public Welfare(int welfareTypeId,String welfareTypeName,int welfareId,String welfareName,String welfareDec,String param1,String param2){
		this.welfareTypeId = welfareTypeId;
		this.welfareTypeName = welfareTypeName;
		this.welfareId = welfareId;
		this.welfareName = welfareName;
		this.welfareDec = welfareDec;
		this.param1 = param1;
		this.param2 = param2;
	}
	
	public void addReward(int itemId,int count){
		rewardItems.put(itemId, count);
	}
	
	public Hashtable<Integer,Integer> getReward(){
		return rewardItems;
	}
	/**下发列表是判断福利是否完成  并记录状态  0:未完  1:完成  2:领取了奖励**/
	abstract public boolean handler(Player player);
	/**过期时将完成情况置成初始状态**/
	abstract public void initWelfare(Player player);
	/**记录领取了奖励**/
	abstract public void recordReward(Player player);
	/**得到当前福利状态**/
	abstract public int getWelfareState(Player player);
	
}
