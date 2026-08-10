package com.pip.server.billing.paypal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_paypalorder")
public class Order_Paypal {
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

	@Column(name="currencycode",nullable=false)
	private String currencyCode;
	
	@Column(name="rmbmoney",nullable=false)
	private int rmbmoney;
	
	@Column(name="status",nullable=false)
    private int status;
	
    @Column(name="gamecode",nullable=false)
    private int gameCode;

    @Column(name="imoney",nullable=false)
    private int imoney;
    
    @Column(name="payseq")
    private String paySeq;

    @Column(name="feeid")
    private int feeId;
    
    @Column(name="paypalid")
    private String paypalID;
    
    @Column(name="fee")
	private int feeamount;
    
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

	public int getFeeId() {
		return feeId;
	}

	public void setFeeId(int feeId) {
		this.feeId = feeId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public int getRmbmoney() {
		return rmbmoney;
	}

	public void setRmbmoney(int rmbmoney) {
		this.rmbmoney = rmbmoney;
	}

	public String getPaypalID() {
		return paypalID;
	}

	public void setPaypalID(String paypalID) {
		this.paypalID = paypalID;
	}

	public int getFeeamount() {
		return feeamount;
	}

	public void setFeeamount(int feeamount) {
		this.feeamount = feeamount;
	}
}