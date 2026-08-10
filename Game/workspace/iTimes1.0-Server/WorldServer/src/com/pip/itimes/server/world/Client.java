package com.pip.itimes.server.world;

public class Client {

    public int sessionId = -1;
    public int accountId = -1;
    public int playerId = -1;
    public Version version;
    public String channel = "";
    public String name;
    public String key;
    public String password;
    public String phone;
    public int modifyPasswordTimes;
    public long iMoney;
    public long bBalance;
    public boolean isMonth;
    public boolean isSubscribe;
    public String model;
    public STATUS status = STATUS.INIT;
    public int loginErrorTime;
    public Integer fromip;
    
    // 卓望平台用户ID（PIP版本也有）
    public String cmccUserId;
    // 卓望平台用户Key（PIP版本也有）
    public String cmccKey;
    // 客户端版本号，格式为：2.2-CCCCCPiP-xxxxx
    public String rawVersion;
    // 上次向认证服务器发送在线保持请求的时间
    public long lastReportTime;
    // 客户端取到的手机号
    public String realPhone = "";
    // 客户端下载地址，如果不为null，则用户下次进入i币商店时需要模拟下载卓望版本客户端
    public String cmccDownloadUrl;

    public enum STATUS{INIT,LOGIN,PLAYERLOGIN,LOGOUT};

    //mengjie add 手机号对应所在城市
    public String cityname = "";

    //快速注册或者快速进入的角色名
    public String roleName = "";
    
    //新快速注册标志
    public byte fastRegFlag = 0;
    
    public Client(int sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isLogin(){
        return status==STATUS.LOGIN||status==STATUS.PLAYERLOGIN;
    }

    public boolean isPlayerLogin(){
        return status==STATUS.PLAYERLOGIN;
    }
    
    public void setFromIp(int fromip){
        this.fromip = new Integer(fromip);
    }
    
    public String getFromIp(){
        String result = "";
        
        if(fromip != null){
            int ip = fromip.intValue();
            
            result += ((ip >> 24) & 0xff) + ".";
            result += ((ip >> 16) & 0xff) + ".";
            result += ((ip >> 8) & 0xff) + ".";
            result += (ip & 0xff);
        }
        
        return result;
    }
    
    /**
     * 取得此版本客户端对应的数据版本号。
     */
    public int getDataVersion() {
    	if (version == null) {
    		return 0;
    	}
    	return version.getDataVersion();
    }
 
    /**
     * 取得客户端JVMCODE。
     */
    public String getJvmCode() {
    	if (model == null) {
    		return "Midp2";
    	}
        int pos = model.indexOf('/');
        if (pos == -1) {
        	return "Midp2";
        } else {
        	return model.substring(pos + 1);
        }
    }
}
