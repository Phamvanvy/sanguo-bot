package com.pip.gameaccount;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_gameaccount")
public class GameAccount {
	
	@Id 
//	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")	
	private int id;
	
	@Column(name="name",nullable=false)
	private String name;
	
	@Column(name="createtime",nullable=false)
	private Date createTime;
	
	@Column(name="issubscribe",nullable=false)
	private boolean isSubscribe;
	
	@Column(name="subscribetime")
	private Date subscribeTime;
	
	@Column(name="monthfee",nullable=false)
	private int monthFee;
	
	@Column(name="lastfeetime")
	private Date lastFeeTime;
	
	@Column(name="lastpaytime")
	private Date lastPayTime;
	
	@Column(name="monthpay",nullable=false)
	private int monthPay;
	
	@Column(name="lastmonthpay",nullable=false)
	private int lastmonthpay;
	
	@Column(name="session")
	private String key;
	
	@Column(name="serverid")
	private String serverId;
	
	@Column(name="logintime")
	private Date loginTime;
	
	public Date getLastPayTime() {
		return lastPayTime;
	}
	public void setLastPayTime(Date lastPayTime) {
		this.lastPayTime = lastPayTime;
	}
	public int getMonthPay() {
		return monthPay;
	}
	public void setMonthPay(int monthPay) {
		this.monthPay = monthPay;
	}
	public int getLastmonthpay() {
		return lastmonthpay;
	}
	public void setLastmonthpay(int lastmonthpay) {
		this.lastmonthpay = lastmonthpay;
	}

	public boolean isSubscribe() {
		return isSubscribe;
	}
	public void setSubscribe(boolean isSubscribe) {
		this.isSubscribe = isSubscribe;
	}
	public Date getSubscribeTime() {
		return subscribeTime;
	}
	public void setSubscribeTime(Date subscribeTime) {
		this.subscribeTime = subscribeTime;
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
	public int getMonthFee() {
		return monthFee;
	}
	public void setMonthFee(int monthFee) {
		this.monthFee = monthFee;
	}
	public Date getLastFeeTime() {
		return lastFeeTime;
	}
	public void setLastFeeTime(Date lastFeeTime) {
		this.lastFeeTime = lastFeeTime;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getServerId() {
		return serverId;
	}
	public void setLastServerId(String serverId) {
		this.serverId = serverId;
	}
	public Date getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
}
