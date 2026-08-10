package com.pip.server.account.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "tbl_accountcredit")
public class AccountCredit implements Serializable {
	@Id
	@Column(name="id")
	private int id;                     // 帐号ID
    @Column(name="credit")
    private int credit;                 // 积分余额
	@Column(name="logouttime")
	private Date logoutTime;            // 上次登出时间
	@Column(name="dayonline")
	private int dayOnline;              // 上次登出时累计当日在线时长(秒)
	@Column(name="daycredit")
	private int dayCredit;              // 上次登出时累计当日奖励积分
	
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getCredit() {
        return credit;
    }
    public void setCredit(int credit) {
        this.credit = credit;
    }
    public Date getLogoutTime() {
        return logoutTime;
    }
    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }
    public int getDayCredit() {
        return dayCredit;
    }
    public void setDayCredit(int dayCredit) {
        this.dayCredit = dayCredit;
    }
    public int getDayOnline() {
        return dayOnline;
    }
    public void setDayOnline(int dayOnline) {
        this.dayOnline = dayOnline;
    }
}
