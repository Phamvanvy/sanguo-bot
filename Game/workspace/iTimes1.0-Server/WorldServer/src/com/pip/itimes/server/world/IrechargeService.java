package com.pip.itimes.server.world;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.pip.itimes.server.bean.Irecharge;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.IrechargeDao;
import com.pip.itimes.server.world.activityService.activity.Service;

public class IrechargeService implements Service {
	private IrechargeDao dao;
	List<Irecharge> chargeList = new ArrayList<Irecharge>();
    
    public IrechargeService (IrechargeDao dao) {
        this.dao = dao;
    }

    public void addIrecharge (Irecharge iRecharge) throws BuyException {
    	synchronized (this) {
    		try {
    			chargeList.add(iRecharge);
    			dao.makePersistent(iRecharge);
    		} catch (DataAccessException ex) {
    			throw new BuyException("Ìí¼ÓI±Ò³äÖµ¼ÇÂ¼´íÎó");
    		}
		}
    }

	public void startup() {
		try {
			chargeList = dao.getAllRecharge();
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		
	}

	public void process(long time) throws Exception {
		
	}
	
	public int getChargeByDay (int accountId, Date date) {
		synchronized (this) {
			List<Irecharge> list = new ArrayList<Irecharge>();
			Iterator<Irecharge> it = chargeList.iterator();
			while (it.hasNext()) {
				Irecharge recharge = it.next();
				Calendar cal = Calendar.getInstance();
				cal.setTime(recharge.getChargetime());
				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(date);
				if (recharge.getAccountid() == accountId && cal.get(Calendar.DAY_OF_YEAR) == cal1.get(Calendar.DAY_OF_YEAR)){
					list.add(recharge);
				}
			}
			int totalMoney = 0;
			for(Irecharge ch : list){
				totalMoney += ch.getMoney();
			}
			return totalMoney;
		}
	}
}
