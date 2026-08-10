package com.pip.server.billing.umpay;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_umpaydata")
public class UMPayData {
	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="name",nullable=false)
	private String userName;
	
	@Column(name="phones",nullable=false)
	private String phones;
	
	@Column(name="lastmodifytime")
	private java.util.Date lastModifyTime;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public java.util.Date getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(java.util.Date d) {
		this.lastModifyTime = d;
	}

	public String getPhones() {
		return phones;
	}

	public void setPhones(String p) {
		this.phones = p;
	}
	
	public boolean isPhoneBounded(String phone) {
	    String[] phoneList = phones.split(",");
	    if (phone.length() > 11) {
	        phone = phone.substring(phone.length() - 11);
	    }
	    for (int i = 0; i < phoneList.length; i++) {
	        String comp = phoneList[i];
	        if (comp.length() > 11) {
	            comp = comp.substring(comp.length() - 11);
	        }
	        if (comp.length() != phone.length()) {
	            continue;
	        }
	        int match = 0;
	        for (int j = 0; j < comp.length(); j++) {
	            if (comp.charAt(j) == phone.charAt(j)) {
	                match++;
	            }
	        }
	        if (match >= 10) {
	            return true;
	        }
	    }
	    return false;
	}
}
