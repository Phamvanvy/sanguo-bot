package com.pip.server.billing.alipay;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_alipayorder")
public class Order_Alipay {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
	private int id;
	
    @Column(name="accountid",nullable=false)
    private int accountID;

    @Column(name="username",nullable=false)
	private String userName;
	
	@Column(name="createtime",nullable=false)
	private java.util.Date createTime;

	@Column(name="finishtime")
    private java.util.Date finishTime;
    
	@Column(name="money",nullable=false)
	private int money;

	@Column(name="status",nullable=false)
    private int status;
	
    @Column(name="gamecode",nullable=false)
    private int gameCode;

    @Column(name="imoney",nullable=false)
    private int imoney;
    
    @Column(name="payseq")
    private String paySeq;

    @Column(name="tradeno")
    private String tradeNo;
    
    public String getPaySeq() {
        return paySeq;
    }

    public void setPaySeq(String paySeq) {
        this.paySeq = paySeq;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public java.util.Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }

    public java.util.Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(java.util.Date finishTime) {
        this.finishTime = finishTime;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getGameCode() {
        return gameCode;
    }

    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }

    public int getImoney() {
        return imoney;
    }

    public void setImoney(int imoney) {
        this.imoney = imoney;
    }

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
}