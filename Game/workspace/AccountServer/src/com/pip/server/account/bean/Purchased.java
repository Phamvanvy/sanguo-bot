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
@Table(name = "tbl_purchased")
public class Purchased implements Serializable{

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="accountid",nullable=false)
	private int accountId;
	
	@Column(name="code",nullable=false)
	private int code;
	
	@Column(name="status",nullable=false)
	private int status;
	
	@Column(name="createtime",nullable=false)
	private Date createTime;
	
	@Column(name="feeid",nullable=false)
	private int feeId;
	
	@Column(name="phone")
	private String phone;

	public int getFeeId() {
		return feeId;
	}

	public String getPhone() {
		return phone;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setFeeId(int feeId) {
		this.feeId = feeId;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	
	public String toString(){
		return "ID["+id+"]AcountId["+accountId+"]Code["+code+"]Status["+status+"]";
	}
}
