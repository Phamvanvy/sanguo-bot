package com.pip.itimes.server.world.ItemGroup;

import com.pip.itimes.server.util.Utils;

public class ItemConstants {
	
	public static final int GROUP_TYPE_KILL = 1;			//杀戮点数商店
	
	public static final long WEEK_MILLS = Utils.MILLS_OF_DAY * 6;			//星期的毫秒数 大于该值算一星期

	public static final long MONTH_MILLS = Utils.MILLS_OF_DAY * 6 * 30;		//月的毫秒数 大于该值算一个月
	
	public static final String SAVE_FILE_NAME = "ItemGroupData";
	
	public static final byte COUNTTYPE_INFINITY = 0;	//默认的个数类型为无穷 不使用count值
	public static final byte COUNTTYPE_AllCOUNT = 1;	//所有人共享个数count
	public static final byte COUNTTYPE_ONECOUNT = 2;	//个人独享count
	
	public static final byte REFRESHTYPE_DAY = 0;		//默认刷新类型为天
	public static final byte REFRESHTYPE_HOUR = 1;		//目前不支持该类型
	public static final byte REFRESHTYPE_WEEK = 2;
	public static final byte REFRESHTYPE_MONTH = 3;
	
	public static final byte BUY_TYPE_OK = -1;			//购买记录成功
	public static final byte BUY_TYPE_NULL = 0;			//无效
	public static final byte BUY_TYPE_ALLCOUNT_ZERO = 1;		//全局物品个数为0
	public static final byte BUY_TYPE_ALLCOUNT_NOTENOUGH = 2;	//全局物品个数不足
	public static final byte BUY_TYPE_ONECOUNT_ZERO = 3;		//独享物品个数为0
	public static final byte BUY_TYPE_ONECOUNT_NOTENOUGH = 4;	//独享物品个数不足
	
}
