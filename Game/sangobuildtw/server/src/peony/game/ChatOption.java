package peony.game;

public class ChatOption {
//	0 世界 1 国家 2 地区 3 同乡 4 帮派 5 队伍 6 私聊 7 系统(系统频道不可用，私聊需要加上对方Id，其他忽略)
	public static final int WORLD = 0;
	public static final int FACTION = 1;
	public static final int AREA = 2;
	public static final int NATIVE = 3;
	public static final int GUILD = 4;
	public static final int PARTY = 5;
	public static final int PRIVATE = 6;
	public static final int SYSTEM = 7;
	
	public static final int WORLD_SHOUT = 10;
	public static final int FACTION_SHOUT = 11;
	public static final int AREA_SHOUT = 12;
	public static final int NATIVE_SHOUT = 13;
	public static final int GUILD_SHOUT = 14;
	public static final int PARTY_SHOUT = 15;
	public static final int PRIVATE_SHOUT = 16;
	
	public boolean inChannel;
	public boolean notify;
	public int color;
	
	public ChatOption(boolean inChannel,boolean notify,int color){
		this.inChannel = inChannel;
		this.notify = notify;
		this.color = color;
	}
	
	@Override
	public ChatOption clone(){
		return new ChatOption(inChannel,notify,color);
	}
	
	public byte getClientByte(){
		int ret = color;
		if(inChannel)
			ret |= 1<<4;
		if(notify)
			ret |= 1<<5;
		return (byte)ret;
	}
}
