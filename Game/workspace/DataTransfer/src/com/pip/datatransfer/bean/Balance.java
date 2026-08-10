package com.pip.datatransfer.bean;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.pip.datatransfer.BalanceException;
import com.pip.datatransfer.Errors;

/**
 * i币分A类以及B类，A类i币属于未确认部分(比如手机钱包的充值)，B类i币属于确认部分
 * i币的消费也分为A类以及B类
 * B类i币必须是>=0的，而A类i币可以是任意值
 * A类消费需要先扣除A类i币，如果A类i币不够那么就扣除B类i币(如果A类i币当前值>0，那么需要先扣到0，然后剩下的用B类i币扣)，如果A+B类i币都不够那么返回失败
 * B类消费需要先扣除B类i币，如果B类i币不够那么直接返回失败
 * 充值时一律先冲成A类i币，然后在适当时候转成B类i币。这样如果A类i币在小于零的情况下，那么充值的钱就会被扣除一部分(或者全部)
 * 当A类i币转成B类i币是，如果A类i币的数值不够，那么就将A类i币正的部分转掉
 * 如果需要倒扣A类i币，那么需要先扣除A类i币，如果发现A类i币不够扣(如果开始是正数，那么需要先扣成0)，那么就扣B类i币，如果再扣不够，那么就将A类i币扣到负数
 * 如果需要倒扣B类i币，那么需要先扣除B类i币，如果发现B类i币不够扣(需要先扣到0)，剩余部分用A类i币扣，再不够扣就将A类i币扣成负数
 * 
 * @author Jeffrey
 *
 */
@Embeddable
public final class Balance {
	
	//bBalance必须>=0
	@Column(name="abalance",nullable=false)
	private int aBalance;
	//aBalance可以为任意数
	@Column(name="bbalance",nullable=false)
	private int bBalance;

	public Balance(int aBalance,int bBalance){
		this.aBalance = aBalance;
		this.bBalance = bBalance;
	}
	
	public Balance(){
		
	}
	
	public int getBBalance() {
		return bBalance;
	}

	public int getABalance() {
		return aBalance;
	}
	
	public Balance clone(){
		return new Balance(aBalance,bBalance);
	}

	/**
	 * 扣除A类i币，如果不够扣就扣B类i币，如果再不够扣就抛出BalanceException异常
	 * @param value
	 * @return
	 * @throws BalanceException
	 */
	public Balance decABalance(int value) throws BalanceException{
		if(value<0)
			throw new IllegalArgumentException("value can not be "+value);
		if(aBalance>0){
			if(aBalance>=value){
				return new Balance(aBalance-value,bBalance);
			}else{
				int m = value - aBalance;
				if(bBalance>=m){ //先将A类i币扣到0，然后扣B类i币
					return new Balance(0,bBalance-m);
				}else{
					throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
				}
			}
		}else{  //如果A类i币<=0，那么只有扣B类i币
			if(bBalance<value)
				throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
			else{
				return new Balance(aBalance,bBalance-value);
			}			
		}
	}
	
	
	/**
	 * 扣除B类i币，如果不够扣就扣A类i币，如果在不够扣就抛出BalanceException
	 * @param value
	 * @return
	 * @throws BalanceException
	 */
	public Balance decBBalance(int value) throws BalanceException{
		if(value<0)
			throw new IllegalArgumentException("value can not be "+value);
		if(bBalance>0){
			if(bBalance>=value){
				return new Balance(aBalance,bBalance-value);
			}else{
				int m = value - bBalance;
				if(aBalance>=m)
					return new Balance(aBalance-m,0);
				else
					throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
			}
		}else{
			if(aBalance<value)
				throw new BalanceException(Errors.NOT_ENOUGH_BALANCE);
			else{
				return new Balance(aBalance-value,bBalance);
			}
		}
	}
	
	/**
	 * 先扣A类i币，如果扣不够扣B类i币，如果再不够就将A类i币扣成负数
	 * @param value
	 * @return
	 */
	public Balance forceDecABalance(int value){
		if(value<0)
			throw new IllegalArgumentException("value can not be "+value);
		if(aBalance>0){
			if(aBalance>=value){
				return new Balance(aBalance-value,bBalance);
			}else{
				int m = value - aBalance;
				if(bBalance>=m){ //先将A类i币扣到0，然后扣B类i币
					return new Balance(0,bBalance-m);
				}else{
					int n = bBalance - m;
					return new Balance(n,0);
				}
			}
		}else{  //如果A类i币<=0，那么只有扣B类i币
			if(bBalance<value){
				int m = value - bBalance;
				return new Balance(aBalance-m,0);
			}else{
				return new Balance(aBalance,bBalance-value);
			}			
		}		
	}
	
	public Balance addABalance(int value){
		if(value<0)
			throw new IllegalArgumentException("value can not be "+value);
		return new Balance(aBalance+value,bBalance);
	}
	
	public Balance addBBalance(int value){
		if(value<0)
			throw new IllegalArgumentException("value can not be "+value);
		return new Balance(aBalance,bBalance+value);
	}
	
	
	
	public int getValue(){
		if(bBalance>=0&&aBalance>=0)
			return bBalance + aBalance;
		return bBalance>=0?bBalance:aBalance;
	}
	
	public boolean equals(Balance b){
		return aBalance==b.aBalance&&bBalance==b.bBalance;
	}
	
	public boolean equals(Object b){
		return equals((Balance)b);
	}
	public String toString() {
		return aBalance + "/" + bBalance;
	}
}
