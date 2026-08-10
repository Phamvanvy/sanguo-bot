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
@Table(name = "tbl_recommendrequest")
public class RecommendRequest implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private int id;
    @Column(name="account")
    private int account;                // 推荐人帐号ID
	@Column(name="rectime")
	private Date recTime;               // 推荐时间
	@Column(name="gamecode")
	private String gameCode;            // 推荐服务器代码
	@Column(name="targetphone")
	private String targetPhone;         // 推荐手机号
	@Column(name="validtime")
	private Date validTime;             // 有效期
	
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getAccount() {
        return account;
    }
    public void setAccount(int account) {
        this.account = account;
    }
    public Date getRecTime() {
        return recTime;
    }
    public void setRecTime(Date recTime) {
        this.recTime = recTime;
    }
    public String getGameCode() {
        return gameCode;
    }
    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }
    public String getTargetPhone() {
        return targetPhone;
    }
    public void setTargetPhone(String targetPhone) {
        this.targetPhone = targetPhone;
    }
    public Date getValidTime() {
        return validTime;
    }
    public void setValidTime(Date validTime) {
        this.validTime = validTime;
    }
}
