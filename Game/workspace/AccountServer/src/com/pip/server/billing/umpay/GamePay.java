package com.pip.server.billing.umpay;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_game_pay")
public class GamePay {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
	private int id;
	
    @Column(name="mobile",nullable=false)
    private String mobile;

    @Column(name="msg",nullable=false)
	private String msg;
    
    @Column(name="gamecode",nullable=false)
    private String gameCode;
    
    @Column(name="paytype")
    private String payType;
    
    @Column(name="spnumber")
    private String spNumber;
	
    @Column(name="createtime")
    private java.util.Date createTime;

	public java.util.Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}

	public String getGameCode() {
		return gameCode;
	}

	public void setGameCode(String gameCode) {
		this.gameCode = gameCode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getSpNumber() {
		return spNumber;
	}

	public void setSpNumber(String spNumber) {
		this.spNumber = spNumber;
	}
    
}
