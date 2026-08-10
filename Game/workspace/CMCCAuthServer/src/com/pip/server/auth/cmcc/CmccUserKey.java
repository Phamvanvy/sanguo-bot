package com.pip.server.auth.cmcc;

/**
 * 卓望平台用户信息。
 */
public class CmccUserKey {
    /*
     * 用户ID
     */
    private String userId;
    /*
     * 会话Key
     */
    private String key;
    /*
     * 最后活跃时间
     */
    private long activeTime;
    /*
     * 所属地区
     */
    private String region = "";

    public CmccUserKey(String userId, String key, long activeTime, String reg) {
        this.userId = userId;
        this.key = key;
        this.activeTime = activeTime;
        this.region = reg;
    }
    
    public CmccUserKey(String userId, String data) {
        this.userId = userId;
        String[] lines = data.split("\n");
        key = lines[0];
        if (lines.length > 1) {
            try {
                activeTime = Long.parseLong(lines[1]);
            } catch (Exception e) {
            }
        }
        if (lines.length > 2) {
            region = lines[2];
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getKey() {
        return key;
    }
    
    public String getRegion() {
        return region;
    }
    
    public String getSaveKey() {
        return key + "\n" + activeTime + "\n" + region;
    }
    
    public void refreshActiveTime() {
        activeTime = System.currentTimeMillis();
    }
    
    public long getActiveTime() {
        return activeTime;
    }
}
