package com.pip.server.auth.bean;

import java.util.Date;

/**
 * 移动用户对应的实际手机号。
 * @author lighthu
 */
public class UserPhone implements java.io.Serializable {
    private String userID;
    private String phone;
    private Date downloadDate;
    
    public Date getDownloadDate() {
		return downloadDate;
	}
	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}
	public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
