package com.pip.server.billing.card;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 道具兑换卡。
 * @author lighthu
 */
@Entity
@Table(name = "tbl_card")
public class Card {
    /** ID */
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private int id;
    /** 卡号 */
    @Column(name="cardno",nullable=false)
    private String cardno;
    /** 游戏代码 */
    @Column(name="gamecode",nullable=false)
    private int gameCode;
    /** 兑换类型 */
    @Column(name="cardtype",nullable=false)
    private int cardType;
    /** 映射兑换类型（对应于游戏中的礼包） */
    @Column(name="maptype",nullable=false)
    private int mapType;
    /** 有效期限 */
    @Column(name="validtime",nullable=false)
    private java.util.Date validTime;
    /** 是否已兑换 */
    @Column(name="used",nullable=false)
    private boolean used;
    /** 兑换帐号ID */
    @Column(name="accountid",nullable=false)
    private int accountID;
    
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
    public int getGameCode() {
        return gameCode;
    }
    public void setGameCode(int gameCode) {
        this.gameCode = gameCode;
    }
    public int getCardType() {
        return cardType;
    }
    public void setCardType(int cardType) {
        this.cardType = cardType;
    }
    public int getMapType() {
        return mapType;
    }
    public void setMapType(int mapType) {
        this.mapType = mapType;
    }
    public java.util.Date getValidTime() {
        return validTime;
    }
    public void setValidTime(java.util.Date validTime) {
        this.validTime = validTime;
    }
    public boolean isUsed() {
        return used;
    }
    public void setUsed(boolean used) {
        this.used = used;
    }
    public int isAccountID() {
        return accountID;
    }
    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }
}
