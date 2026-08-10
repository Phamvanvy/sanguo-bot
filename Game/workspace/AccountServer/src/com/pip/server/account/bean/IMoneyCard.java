package com.pip.server.account.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "tbl_imoneycard")
public class IMoneyCard implements Serializable {
    /** ID */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private int id;
    /** 卡号 */
    @Column(name="cardno",nullable=false)
    private String cardno;
    /** 卡密 */
    @Column(name="password",nullable=false)
    private String password;
    /** 游戏区代码 */
    @Column(name="gamecode",nullable=false)
    private String gameCode;
    /** 金额（1/100i币为单位） */
    @Column(name="amount",nullable=false)
    private int amount;
    /** 生成时间 */
    @Column(name="createtime",nullable=false)
    private java.util.Date createTime;
    /** 生成帐号ID */
    @Column(name="accountid",nullable=false)
    private int accountID;
    /** 是否已兑换 */
    @Column(name="used",nullable=false)
    private boolean used;
    /** 兑换时间 */
    @Column(name="usetime",nullable=true)
    private java.util.Date useTime;
    /** 兑换帐号ID */
    @Column(name="useaccount",nullable=false)
    private int useAccount = -1;
    /** 兑换游戏区代码 */
    @Column(name="usegamecode",nullable=true)
    private String useGameCode;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getGameCode() {
		return gameCode;
	}
	public void setGameCode(String gameCode) {
		this.gameCode = gameCode;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public java.util.Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}
	public int getAccountID() {
		return accountID;
	}
	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}
	public boolean isUsed() {
		return used;
	}
	public void setUsed(boolean used) {
		this.used = used;
	}
	public java.util.Date getUseTime() {
		return useTime;
	}
	public void setUseTime(java.util.Date useTime) {
		this.useTime = useTime;
	}
	public int getUseAccount() {
		return useAccount;
	}
	public void setUseAccount(int useAccount) {
		this.useAccount = useAccount;
	}
	public String getUseGameCode() {
		return useGameCode;
	}
	public void setUseGameCode(String useGameCode) {
		this.useGameCode = useGameCode;
	}
}
