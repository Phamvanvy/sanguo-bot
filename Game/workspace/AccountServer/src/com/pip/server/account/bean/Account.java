package com.pip.server.account.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.pip.server.account.util.PasswordCipher;

@Entity
@Table(name = "tbl_account")
public class Account implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "password", nullable = false)
	private String password;
	@Column(name = "guardpass")
	private String guardPass;
	@Embedded
	private Balance balance;
	@Column(name = "createtime", nullable = false)
	private Date createTime;
	@Column(name = "gamecode", nullable = false)
	private String createGameCode; // 建立帐号的游戏码
	@Column(name = "lastloginTime")
	private Date lastLoginTime;
	@Column(name = "status", nullable = false)
	private int status;
	@Column(name = "phone")
	private String phone;
	@Column(name = "recommend")
	private String recommend; // 推荐人
	@Column(name = "comment")
	private String comment;
	@Column(name = "serviceversion")
	private String serviceVersion;
	@Column(name = "lastpaytime")
	private Date lastPayTime;
	@Column(name = "monthfee")
	private long monthFee;
	@Column(name = "lastmonthfee")
	private long lastMonthFee;
	@Column(name = "modifypasswordtimes")
	private int modifyPasswordTimes;
	@Column(name = "model")
	private String model;
	@Column(name = "versionpatch")
	private String versionPatch;
	@Column(name = "cbalance")
	private int cbalance;
	@Column(name = "activephone")
	private String activePhone;
	@Column(name = "regphone")
	private String regPhone;

	public int getCbalance() {
		return cbalance;
	}

	public void setCbalance(int cbalance) {
		this.cbalance = cbalance;
	}

	public int getRegType() {
		return regType;
	}

	public void setRegType(int regType) {
		this.regType = regType;
	}

	@Column(name = "regtype")
	private int regType;

	public String getVersionPatch() {
		return versionPatch;
	}

	public void setVersionPatch(String versionPatch) {
		this.versionPatch = versionPatch;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Date getLastPayTime() {
		return lastPayTime;
	}

	public void setLastPayTime(Date lastPayTime) {
		this.lastPayTime = lastPayTime;
	}

	public long getMonthFee() {
		return monthFee;
	}

	public void setMonthFee(long monthFee) {
		this.monthFee = monthFee;
	}

	public long getLastMonthFee() {
		return lastMonthFee;
	}

	public void setLastMonthFee(long lastMonthFee) {
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

	public String getPassword() {
		return password;
	}

	/**
	 * 获取解密后的密码。
	 * 
	 * @return
	 */
	public String getPasswordDec() {
		String ret;
		if (password.startsWith("$e$")) {
			ret = PasswordCipher.decode(password.substring(3));
			if (ret == null) {
				ret = password;
			}
		} else {
			ret = password;
			setPasswordEnc(password);
		}
		return ret;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 设置加密前的密码，本方法对密码进行加密。
	 */
	public void setPasswordEnc(String str) {
		password = "$e$" + PasswordCipher.encode(str);
	}

	public String getGuardPass() {
		return guardPass;
	}

	public void setGuardPass(String guardPass) {
		this.guardPass = guardPass;
	}

	public Balance getBalance() {
		return balance;
	}

	public void setBalance(Balance balance) {
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

	public String getActivePhone() {
		return activePhone;
	}

	public void setActivePhone(String activePhone) {
		this.activePhone = activePhone;
	}

	public String getRegPhone() {
		return regPhone;
	}

	public void setRegPhone(String regPhone) {
		this.regPhone = regPhone;
	}
}
