package com.pip.server.account;

import java.util.Date;

import com.pip.server.account.bean.Fee;
import com.pip.server.account.dao.FeeDAO;

public class FeeService {
	
	protected FeeDAO dao;
	protected AccountService accountService;
	
	public FeeService(FeeDAO dao,AccountService accountService){
		this.dao = dao;
		this.accountService = accountService;
	}
	
	public Fee addCompletedFee(int accountId,int value,String channel){
		Fee fee = new Fee();
		fee.setAccountId(accountId);
		fee.setAmount(value);
		fee.setChannel(channel);
		fee.setCharged(true);
		Date current = new Date();
		fee.setCreateTime(current);
		fee.setFinishTime(current);
		dao.add(fee);
		return fee;
	}
	
	public Fee findFee(int id){
		return dao.getFee(id);
	}
	
	public Fee findLatestFee(String channel){
		return dao.getLatestFee(channel);
	}
	
	 public Fee newFee(String accountName, int amount, String channel) {
		 AccountEntity ae = accountService.getAccountEntity(accountName);
		 if(ae!=null){
			 Fee fee = new Fee();
			 fee.setAccountId(ae.getId());
			 fee.setCharged(false);
			 fee.setCreateTime(new Date());
			 fee.setAmount(amount);
			 fee.setChannel(channel);
			 dao.add(fee);
			 return fee;
		 }
		 return null;
	 }
	 
	 public int getMonthPayment(String channel){
		 return dao.getMonthSum(channel);
	 }
	 
	 public int getChinarunCharge(int accountID){
         return dao.getChinarunCharge(accountID);
     }

	 public boolean purchaseProduct(int feeID, int productCode, String phone) {
		Fee fee = findFee(feeID);
		if (fee == null) {
			return false;
		}
		if (!accountService.purchaseProduct(fee, productCode, phone)) {
			return false;
		}
		fee.setCharged(true);
		fee.setFinishTime(new Date());
		dao.update(fee);
		return true;
	}

	public boolean fulfillOrder(int feeID) {
		Fee fee = findFee(feeID);
		if (fee == null) {
			return false;
		}
		if (!accountService.fulfillOrder(fee)) {
			return false;
		}
		fee.setCharged(true);
		fee.setFinishTime(new Date());
		dao.update(fee);
		return true;
	}
	
	
	public boolean fulfillOrder2(int feeID) {
		Fee fee = findFee(feeID);
		if (fee == null) {
			return false;
		}
		if (!accountService.fulfillOrder2(fee)) {
			return false;
		}
		fee.setCharged(true);
		fee.setFinishTime(new Date());
		dao.update(fee);
		return true;
	}

	public String resetPassword(int feeID) {
		Fee fee = findFee(feeID);
		if (fee == null) {
			return null;
		}
		String ret = accountService.resetPassword(fee);
		if (ret == null) {
			return null;
		}
		fee.setCharged(true);
		fee.setFinishTime(new Date());
		dao.update(fee);
		return ret;
	}
	     
	public boolean changePhone(int feeID, String newPhone) {
		Fee fee = findFee(feeID);
		if (fee == null) {
			return false;
		}
		if (!accountService.changePhone(fee, newPhone)) {
			return false;
		}
		fee.setCharged(true);
		fee.setFinishTime(new Date());
		dao.update(fee);
		return true;
	}
}
