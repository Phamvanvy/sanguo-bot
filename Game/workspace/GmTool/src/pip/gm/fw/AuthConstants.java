package pip.gm.fw;

public interface AuthConstants {
	public long shutdown       = 1L << 0; // 关闭服务器的权限
	public long addip          = 1L << 1; // 增加信任IP的权限,已经过期.由于开放IP并且增加代理,此功能不再有效
	public long reload         = 1L << 2; // 重载诸如关卡数据的权限 exec
	public long delete         = 1L << 3; // 删除 del-player recover-player
	public long modifyaccount  = 1L << 4;
	public long modify         = 1L << 5; // 修改数值 chg
	public long add            = 1L << 6; // 增加虚拟物品 gmsg中附件
	public long move           = 1L << 7; // revive,
	public long forbidaccount  = 1L << 8;
	public long forbid         = 1L << 9; // forbid
	public long kick           = 1L << 10;
	public long mute           = 1L << 11;
	public long releaseaccount = 1L << 12; // release
	public long accountinfo    = 1L << 13; // showaccount;
	public long show           = 1L << 14; // BBS op; show
	public long who            = 1L << 15; // who
	public long maxplayer      = 1L << 16;
	public long netbattle      = 1L << 17; // 
	public long overhear       = 1L << 18; // 侦听帮派,小队等聊天频道的权限
	public long chat           = 1L << 19; // 聊天的权限 m gmsg
	public long brocast        = 1L << 20; // 广播的权限
	public long root        = 1L << 21; // 根权限,创建GM帐号,修改GM权限的权限
	public long tongbattle        = 1L << 22; // 调整帮派战斗的权限
	public long loginmsg    = 1L << 23;  // 修改登录消息的权限
	public long deleteGmMail         = 1L << 24; // 是否有权限通过GM借口删除玩家呼叫GM的内容
	public long regulateExp = 1L << 25;// 调整全服打怪经验倍数的权力。expratio
	public long traceAdmin         = 1L << 26; // 是否有权限跟踪其他ＧＭ活动
	public long queryHelpByDate    = 1L << 27; // 是否有权限按日期查询求助信息
	public long addiMoney          = 1L << 28; // 是否有权限添加元宝

	public String authStrings[] = {
		"shutdown" ,
		"addip" ,
		"reload" ,
		"delete" ,
		"modifyaccount" ,
		"modify" ,
		"add" ,
		"move" ,
		"forbidaccount" ,
		"forbid" ,
		"kick" ,
		"mute" ,
		"releaseaccount" ,
		"accountinfo" ,
		"show",
		"who" ,
		"maxplayer" ,
		"netbattle",
		"overhear",
		"chat",
		"brocast",
		"root",
		"tongbattle",
		"loginmsg",
		"deleteGmMail",
		"regulateExp",
		"traceAdmin",
		"queryHelpByDate",
		"addiMoney",
	};

}
