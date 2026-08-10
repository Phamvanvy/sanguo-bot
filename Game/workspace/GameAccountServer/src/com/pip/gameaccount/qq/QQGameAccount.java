package com.pip.gameaccount.qq;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_gameaccount")
public class QQGameAccount {
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")	
	private int id;
	
	@Column(name="name",nullable=false)
	private String name;
	
	@Column(name="createtime",nullable=false)
	private Date createTime;
	
	@Column(name="balance",nullable=false)
	private int balance;
	
	@Column(name="status",nullable=false)
	private int status;
	
	@Column(name="session")
	private String key;
	
	@Column(name="serverid")
	private String serverId;
	
	@Column(name="logintime")
	private Date loginTime;
	 
	@Column(name="activephone")
	private String activePhone;
	
	@Column(name="regphone")
	private String regPhone;

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

    public String getActivePhone() {
        return activePhone;
    }

    public void setActivePhone(String activePhone) {
        this.activePhone = activePhone;
    }

    public String getRegPhone() {
        return regPhone;
    }

    public void setRegPhone(String regPhone) {
        this.regPhone = regPhone;
    }
}
