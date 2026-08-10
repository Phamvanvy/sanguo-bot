package com.pip.server.billing.ruyifu;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_ruyifuorder")
public class Order_RuYiFu {
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
    
    @Column(name="orderid")
    private String orderID;
    
    @Column(name="channel")
    private String channel;
    
    @Column(name="cardno")
    private String cardNo;
    
    @Column(name="cardpass")
    private String cardPass;
    
    @Column(name="feeid")
    private int feeID;
    
    @Column(name="cardcorp")
    private int cardCorp;

    public int getCardCorp() {
		return cardCorp;
	}

	public void setCardCorp(int cardCorp) {
		this.cardCorp = cardCorp;
	}

	public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardPass() {
        return cardPass;
    }

    public void setCardPass(String cardPass) {
        this.cardPass = cardPass;
    }

    public int getFeeID() {
        return feeID;
    }

    public void setFeeID(int feeID) {
        this.feeID = feeID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String oid) {
        this.orderID = oid;
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

}
