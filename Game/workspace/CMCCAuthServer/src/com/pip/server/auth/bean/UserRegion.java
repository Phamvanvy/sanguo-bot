package com.pip.server.auth.bean;

import java.util.Date;

/**
 * 移动用户所在地域的信息。
 * @author lighthu
 */
public class UserRegion implements java.io.Serializable {
    private String userID;
    private String region;
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
}
