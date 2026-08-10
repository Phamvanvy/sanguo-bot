package peony.game.gift;

public class ChannelGiftRule {
	
	public static final int TYPE_EVERYACCOUNT = 0; //每个账号只给一次礼物
	public static final int TYPE_EVERYACTOR = 1; //每个角色给一次礼物
	
	public String channel;
	public int itemId;
	public int count;
	public int type;
	public String message;
	
	public ChannelGiftRule(String channel,int itemId,int count,int type,String message){
		this.channel = channel;
		this.itemId = itemId;
		this.count = count;
		this.type = type;
		this.message = message;
	}
	
	public boolean inChannel(String channel){
		return this.channel.equals(channel);
	}
	
	
}
