package com.pip.server.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pip.server.account.bean.Account;
import com.pip.server.account.bean.Balance;
import com.pip.server.account.bean.LoginInfo;
import com.pip.server.account.bean.Purchased;

public class AccountEntity {
	
	private Account account;
	private Map<String,LoginInfo> loginInfos = new HashMap<String,LoginInfo>();
	private Map<Integer,Purchased> purchaseds = new HashMap<Integer,Purchased>();
	private List<Long> loginFailRecords = new ArrayList<Long>();
	
	public AccountEntity(Account account){
		this.account = account;
	}
	
	/**
	 * 记录用户登录失败一次。
	 */
	public void recordLoginFail() {
		loginFailRecords.add(System.currentTimeMillis());
	}
	
	/**
	 * 检查用户是否在短时间内重复登陆失败多次，以防止密码穷举攻击。2分钟内如果连续输错5次密码，则在随后的2分钟内禁止登录。
	 * @return 如果存在可能的攻击，返回false。
	 */
	public boolean checkLoginAttack() {
		if (loginFailRecords.size() >= 5) {
			int size = loginFailRecords.size();
			long time5 = loginFailRecords.get(size - 1);
			long time1 = loginFailRecords.get(size - 5);
			long now = System.currentTimeMillis();
			if (now - time5 > 120000L) {
				// 如果已经过了2分钟，清除记录
				loginFailRecords.clear();
				return true;
			}
			if (time5 - time1 < 120000L) {
				// 2分钟内5次输入错误，并且还在2分钟内，禁止登录
				return false;
			} else {
				// 删除前面的登录错误记录，只保留最后4条
				loginFailRecords = loginFailRecords.subList(size - 4, size);
			}
		}
		return true;
	}
	
	public LoginInfo[] getLoginInfos(){
		Collection<LoginInfo> s = loginInfos.values();
		LoginInfo[] ret = new LoginInfo[s.size()];
		s.toArray(ret);
		return ret;
	}

	public Purchased[] getPurchaseds(){
		Collection<Purchased> s = purchaseds.values();
		Purchased[] ret = new Purchased[s.size()];
		s.toArray(ret);
		return ret;
	}
	
	public Account getAccount(){
		return account;
	}
	
	public void setName(String name){
		account.setName(name);
	}
	
	public void addLoginInfo(LoginInfo loginInfo){
		loginInfos.put(loginInfo.getServiceId(),loginInfo);
	}
	
	public LoginInfo getLoginInfo(String serviceId){
		return loginInfos.get(serviceId);
	}
	
	public void addPurchased(Purchased purchased){
		purchaseds.put(purchased.getCode(), purchased);
	}
	
	public Purchased getPurchased(int code){
		return purchaseds.get(code);
	}
	
	public Purchased removePurchased(int code){
		return purchaseds.remove(code);
	}
	
	public int[] getPurchasedCodes(){
		Set<Integer> s = purchaseds.keySet();
		int[] ret = new int[s.size()];
		int i = 0;
		for(int code:s){
			ret[i] = code;
			i++;
		}
		return ret;
	}
	
	public int getId(){
		return account.getId();
	}
	
	public String getName(){
		return account.getName();
	}
	
	public String getPassWord(){
		return account.getPasswordDec();
	}
	
	public int getStatus(){
		return account.getStatus();
	}
	
	public void setBalance(int aBalance,int bBalance){
		account.setBalance(new Balance(aBalance,bBalance));
	}
	
	public void setBalance(Balance balance){
		account.setBalance(balance);
	}
	
	public Balance getBalance(){
		return account.getBalance();
	}
	
	
	public Date getCreateTime(){
		return account.getCreateTime();
	}
	
	public void setStatus(int status){
		account.setStatus(status);
	}
	
	public void setComment(String comment){
		String oComment = account.getComment();
		account.setComment((oComment==null?"":(oComment+";"))+comment);
	}
	
	public void setLastLoginTime(Date date){
		account.setLastLoginTime(date);
	}
	
	public String getPhone(){
		return account.getPhone();
	}
	
	public void setPhone(String phone){
		account.setPhone(phone);
	}
	
	public boolean isLogon(){
		Iterator<LoginInfo> ite = loginInfos.values().iterator();
		while(ite.hasNext()){
			LoginInfo li = ite.next();
			if(li.isValid())
				return true;
		}
		return false;
	}
	
	public Date getLastPayTime(){
		return account.getLastPayTime();
	}
	
	public void setLastPayTime(Date time){
		account.setLastPayTime(time);
	}
	
	public long getMonthFee(){
		return account.getMonthFee();
	}
	
	public void setMonthFee(long monthFee){
		account.setMonthFee(monthFee);
	}
	
	public long getLastMonthFee(){
		return account.getLastMonthFee();
	}
	
	public void setLastMonthFee(long lastMonthFee){
		account.setLastMonthFee(lastMonthFee);
	}
	
	public void setPassword(String password){
		account.setPasswordEnc(password);
	}
	
	public int getCbalance(){
		return account.getCbalance();
	}
	
	public void setCbalance(int cbalance){
		account.setCbalance(cbalance);
	}
	
	public int getRegType(){
		return account.getRegType();
	}
}
