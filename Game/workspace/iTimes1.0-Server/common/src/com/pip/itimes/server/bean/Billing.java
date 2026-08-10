package com.pip.itimes.server.bean;


import java.util.Date;

public class Billing{


	private int id;

	private String msisdn;

	private int balance;

	private int feeplan;

	private Date createtime;

	private Date lastbillingtime;

	private int credit;

	public Billing() {
	}



	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsisdn() {
		return this.msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public int getBalance() {
		return this.balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getFeeplan() {
		return this.feeplan;
	}

	public void setFeeplan(int feeplan) {
		this.feeplan = feeplan;
	}

	public Date getCreatetime() {
		return this.createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public Date getLastbillingtime() {
		return this.lastbillingtime;
	}

	public void setLastbillingtime(Date lastbillingtime) {
		this.lastbillingtime = lastbillingtime;
	}

	public int getCredit() {
		return this.credit;
	}

	public void setCredit(int credit) {
		this.credit = credit;
	}

}
