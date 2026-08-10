package com.pip.server.billing.security;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_bindrequest")
public class BindRequest {

	public static final String TYPE_PHONE = "phone";
	public static final String TYPE_MAIL = "mail";
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id",nullable=false)
	protected int id;
	
	@Column(name="type",nullable=false)
	protected String type;
	
	@Column(name="accountid",nullable=false)
	protected int accountid;
	
	@Column(name="randomstring",nullable=false)
	protected String randomString;
	
	@Column(name="content",nullable=false)
	protected String content;
	
	@Column(name="createtime",nullable=false)
	protected Date createTime;
	
	@Column(name="used",nullable=false)
	protected boolean used;

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getAccountid() {
		return accountid;
	}

	public void setAccountid(int accountid) {
		this.accountid = accountid;
	}

	public String getRandomString() {
		return randomString;
	}

	public void setRandomString(String randomString) {
		this.randomString = randomString;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean isMailRequest(){
		return TYPE_MAIL.equals(type);
	}
	
	public boolean isPhoneRequest(){
		return TYPE_PHONE.equals(type);
	}
}
