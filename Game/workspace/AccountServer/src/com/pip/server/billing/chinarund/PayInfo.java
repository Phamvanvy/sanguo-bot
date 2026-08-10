package com.pip.server.billing.chinarund;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "payInfo")
public class PayInfo {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="payid",nullable=false)
	private String payId;
	
	@Column(name="accountid",nullable=false)
	private int accountId;
	
	@Column(name="username",nullable=false)
	private String userName;
	
	@Column(name="paytime",nullable=false)
	private String payTime;
	
	@Column(name="money",nullable=false)
	private String money;
	
	@Column(name="valid",nullable=false)
	private boolean valid;
	
	@Column(name="kindOfGame",nullable=false)
	private int game;
	
	@Column(name="ifaddISuccess")
	private boolean addIFail;
	
	@Column(name="i_sum")
	private int i_sum;
	
	@Column(name="channel")
	private String channel;
	
	@Column(name="notifytime")
	private Date notifyTime;
	
	@Column(name="feeid")
	private int feeid;
	
	@Column(name="cardtype")
	private String cardType;
	
	@Column(name="cardno")
	private String cardno;
	
	@Column(name="cardpass")
	private String cardpass;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPayTime() {
		return payTime;
	}

	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public int getGame() {
		return game;
	}

	public void setGame(int game) {
		this.game = game;
	}

	public boolean isAddIFail() {
		return addIFail;
	}

	public void setAddIFail(boolean addIFail) {
		this.addIFail = addIFail;
	}

	public int getI_sum() {
		return i_sum;
	}

	public void setI_sum(int i_sum) {
		this.i_sum = i_sum;
	}
	
	public String getChannel() {
	    return channel;
	}
	
	public void setChannel(String c) {
	    channel = c;
	}

    public Date getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(Date notifyTime) {
        this.notifyTime = notifyTime;
    }

    public int getFeeid() {
        return feeid;
    }

    public void setFeeid(int feeid) {
        this.feeid = feeid;
    }

    public String getCardno() {
        return cardno;
    }

    public void setCardno(String cardno) {
        this.cardno = cardno;
    }

    public String getCardpass() {
        return cardpass;
    }

    public void setCardpass(String cardpass) {
        this.cardpass = cardpass;
    }

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
}
