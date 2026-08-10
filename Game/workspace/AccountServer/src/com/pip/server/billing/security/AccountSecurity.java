package com.pip.server.billing.security;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_accountsecurity")
public class AccountSecurity {
	
	private static final long WEEK = 7*24*3600*1000L;
	
	@Id
	@Column(name="id",nullable=false)
	protected int id;
	
	@Column(name="name",nullable=false)
	protected String name;
	
	@Column(name="phone")
	protected String phone;
	
	@Column(name="newphone")
	protected String newPhone;
	
	@Column(name="bindtime")
	protected Date bindTime;
	
	@Column(name="idcard")
	protected String idcard;
	
	@Column(name="question")
	protected String question;
	
	@Column(name="answer")
	protected String answer;
	
	@Column(name="mail")
	protected String mail;
	
	@Column(name="createtime",nullable=false)
	protected Date createTime;

	@Column(name="bindidcardtime")
	protected Date bindIDCardTime;

	@Column(name="bindphonetime")
	protected Date bindPhoneTime;

	@Column(name="bindmailtime")
	protected Date bindMailTime;

	@Column(name="bindquestiontime")
	protected Date bindQuestionTime;
	
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

	public String getPhone() {
		return phone;
	}
	
	public String getBindPhone(){
		if(newPhone==null)
			return phone;
		if(System.currentTimeMillis()-bindTime.getTime()>WEEK){
			return newPhone;
		}else{
			return phone;
		}
	}
	
	
	/*
	 * 绑定手机号码会在一个星期以后生效
	 */
	public void setBindPhone(String bindPhone){
		if(phone==null){
			phone = bindPhone;
			return;
		}
		if(newPhone==null){
			newPhone = bindPhone;
			bindTime = new Date();
		}else{
			if(System.currentTimeMillis()-bindTime.getTime()>WEEK){ //如果绑定已经生效
				phone = bindPhone;
				bindTime = new Date();
			}else{
				newPhone = bindPhone;
				bindTime = new Date();
			}
		}
	}

	public String getNewPhone() {
		return newPhone;
	}

	public void setNewPhone(String newPhone) {
		this.newPhone = newPhone;
	}

	public Date getBindTime() {
		return bindTime;
	}

	public void setBindTime(Date bindTime) {
		this.bindTime = bindTime;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public boolean isPhoneBound(){
		return phone!=null;
	}
	
	public boolean isQnaBound(){
		return question!=null&&answer!=null;
	}
	
	public boolean isMailBound(){
		return mail!=null;
	}
	
	public boolean isIdcardBound(){
		return idcard!=null;
	}

	public Date getBindIDCardTime() {
		return bindIDCardTime;
	}

	public void setBindIDCardTime(Date bindIDCardTime) {
		this.bindIDCardTime = bindIDCardTime;
	}

	public Date getBindPhoneTime() {
		return bindPhoneTime;
	}

	public void setBindPhoneTime(Date bindPhoneTime) {
		this.bindPhoneTime = bindPhoneTime;
	}

	public Date getBindMailTime() {
		return bindMailTime;
	}

	public void setBindMailTime(Date bindMailTime) {
		this.bindMailTime = bindMailTime;
	}

	public Date getBindQuestionTime() {
		return bindQuestionTime;
	}

	public void setBindQuestionTime(Date bindQuestionTime) {
		this.bindQuestionTime = bindQuestionTime;
	}
}
