package com.pip.itimes.server.bean;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Admin {

    private int id;
    private String name;
    private String password;
    private String auth;
    private long authMask;
    
    public Admin() {
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPassword(){
        return password;
    }

    public String getAuth() {
        return auth;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public int hashCode(){
        return id;
    }
    public void setAuth(String auth) {
        this.auth = auth;
        if (auth.startsWith("0x") || auth.startsWith("0X")) {
        	try {
				authMask = Long.parseLong(auth.substring(2), 16);
			} catch (NumberFormatException e) {
				authMask = 0;
				e.printStackTrace();
			}
        } else {
	        authMask = 0;
	        if (auth != null) {
	        	int n = 0;
	        	while (n < auth.length()) {
	        		int k = auth.indexOf('|', n);
	        		String s = null;
	        		if (k > 0) {
	        			s = auth.substring(n, k);
	        			n = k+1;
	        		} else {
	        			s = auth.substring(n);
	        			n = auth.length();
	        		}
	        		for (int i = 0; i < AdminAuth.authStrings.length; i++) {
	        			if (s.equals(AdminAuth.authStrings[i])) {
	        				authMask |= 1 << i;
	        			}
        			}
        		}
        	}
        }
    }

    public boolean hasAuth(String a){
    	for (int i = 0; i < AdminAuth.authStrings.length; i++) {
    		if (a.equals(AdminAuth.authStrings[i])) {
    			return (authMask & (1L << i)) != 0;
    		}
    	}
    	return false;
    }

    public boolean hasAuth(long authes){
    	return (authMask & authes) == authes;
    }

    public static interface AdminAuth {
		public static final String authStrings[] = {
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
		};
		public long shutdown       = 1L << 0; // 关闭服务器的权限
		public long addip          = 1L << 1; // 增加信任IP的权限,已经过期.由于开放IP并且增加代理,此功能不再有效
		public long reload         = 1L << 2; // 重载诸如关卡数据的权限
		public long delete         = 1L << 3; // 删除玩家数据的权限
		public long modifyaccount  = 1L << 4; // 修改帐号信息的权限
		public long modify         = 1L << 5; // 修改角色信息的权限
		public long add            = 1L << 6;  // 是否有权限发放虚拟物品(这些物品是当下生成的)
		public long move           = 1L << 7; // 移动玩家的权限
		public long forbidaccount  = 1L << 8; 
		public long forbid         = 1L << 9;
		public long kick           = 1L << 10;
		public long mute           = 1L << 11;
		public long releaseaccount = 1L << 12;
		public long accountinfo    = 1L << 13;
		public long show           = 1L << 14;
		public long who            = 1L << 15;
		public long maxplayer      = 1L << 16;
		public long netbattle      = 1L << 17; // 
		public long overhear       = 1L << 18; // 侦听帮派,小队等聊天频道的权限
		public long chat           = 1L << 19; // 聊天的权限
		public long brocast        = 1L << 20; // 广播的权限
		public long root        = 1L << 21; // 根权限,创建GM帐号,修改GM权限的权限
		public long tongbattle        = 1L << 22; // 调整帮派战斗的权限
		public long loginmsg    = 1L << 23;  // 修改登录消息的权限
		public long deleteGmMail         = 1L << 24; // 是否有权限通过GM接口删除玩家呼叫GM的内容
		public long regulateExp = 1L << 25;// 调整全服打怪经验倍数的权力。（幻想中暂时没实现）
		public long traceAdmin         = 1L << 26; // 是否有权限跟踪其他ＧＭ活动
    }

}
