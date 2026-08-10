package com.pip.server.account.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tbl_fee")
public class Fee implements Serializable{
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
    private int id;
	@Column(name="accountId",nullable=false)
    private int accountId;
	@Column(name="createtime",nullable=false)
    private Date createTime;
	@Column(name="finishtime")
    private Date finishTime;
	@Column(name="charged",nullable=false)
    private boolean charged;
	@Column(name="amount",nullable=false)
    private int amount;
	@Column(name="channel",nullable=false)
    private String channel;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}
	public boolean isCharged() {
		return charged;
	}
	public void setCharged(boolean charged) {
		this.charged = charged;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
  
	
    
}
