package com.pip.datatransfer.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;


@Entity
@Table(name = "tbl_account")
public class Account implements Serializable {
	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	@Column(name="name",nullable=false)
	private String name;
	@Column(name="password",nullable=false)
	private String passWord;
	@Column(name="guardpass")
	private String guardPass;
	@Embedded
	private Balance balance;
	@Column(name="createtime",nullable=false)
	private Date createTime;
	@Column(name="gamecode",nullable=false)
	private String createGameCode; //建立帐号的游戏码
	@Column(name="lastloginTime")
	private Date lastLoginTime;
	@Column(name="status",nullable=false)
	private int status;
	@Column(name="phone")
	private String phone;
	@Column(name="recommend")
	private String recommend; //推荐人
	@Column(name="comment")
	private String comment;
	@Column(name="serviceversion", nullable=false)
	private String serviceVersion;
	@Column(name="lastpaytime")
	private Date lastPayTime;
	@Column(name="monthfee")
	private int monthFee;
	@Column(name="lastmonthfee")
	private int lastMonthFee;
	@Column(name="modifypasswordtimes")
	private int modifyPasswordTimes;

	public Date getLastPayTime() {
		return lastPayTime;
	}

	public void setLastPayTime(Date lastPayTime) {
		this.lastPayTime = lastPayTime;
	}

	public int getMonthFee() {
		return monthFee;
	}

	public void setMonthFee(int monthFee) {
		this.monthFee = monthFee;
	}

	public int getLastMonthFee() {
		return lastMonthFee;
	}

	public void setLastMonthFee(int lastMonthFee) {
		this.lastMonthFee = lastMonthFee;
	}

	public Account() {

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

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getGuardPass() {
		return guardPass;
	}

	public void setGuardPass(String guardPass) {
		this.guardPass = guardPass;
	}

	public Balance getBalance(){
		return balance;
	}
	
	public void setBalance(Balance balance){
		this.balance = balance;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateGameCode() {
		return createGameCode;
	}

	public void setCreateGameCode(String createGameCode) {
		this.createGameCode = createGameCode;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRecommend() {
		return recommend;
	}

	public void setRecommend(String recommend) {
		this.recommend = recommend;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public int getModifyPasswordTimes() {
		return modifyPasswordTimes;
	}

	public void setModifyPasswordTimes(int modifyPasswordTimes) {
		this.modifyPasswordTimes = modifyPasswordTimes;
	}
}
