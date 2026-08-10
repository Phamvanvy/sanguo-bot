package peony.service.account;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import peony.game.Server;
import peony.service.Service;

public class RecordChargeService implements Service {
	
	List<Charge> chargeList = new ArrayList<Charge>();

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		ChargeDao dao = Server.server.getServiceRegistry().getDbService().chargeDao;
		chargeList = dao.getAllCharges();
	}
	
	public void addCharge(Charge charge){
		synchronized (this) {
			chargeList.add(charge);
			ChargeDao dao = Server.server.getServiceRegistry().getDbService().chargeDao;
			dao.newEntity(charge);
		}
	}
	
	public List<Charge> getChargesAfter(Date date){
		List<Charge> list = new ArrayList<Charge>();
		Iterator<Charge> it = chargeList.iterator();
		while(it.hasNext()){
			Charge charge =it.next();
			if(charge.chargeTime.after(date)){
				list.add(charge);
			}
		}
		return list;
	}
	
	public int getChargeAfter(int accountId, Date date){
		synchronized (this) {
			List<Charge> list = getChargesAfter(date);
			int money = 0;
			Iterator<Charge> it = list.iterator();
			while(it.hasNext()){
				Charge charge = it.next();
				if(charge.accountId==accountId){
					money += charge.money;
				}
			}
			return money;
		}
	}
	
	public int getChargeByDay(int accountId, Date date){
		synchronized (this) {
			List<Charge> list = new ArrayList<Charge>();
			Iterator<Charge> it = chargeList.iterator();
			while(it.hasNext()){
				Charge charge =it.next();
				Calendar cal = Calendar.getInstance();
				cal.setTime(charge.chargeTime);
				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(date);
				if(charge.accountId==accountId && cal.get(Calendar.DAY_OF_YEAR)==cal1.get(Calendar.DAY_OF_YEAR)){
					list.add(charge);
				}
			}
			int totalMoney = 0;
			for(Charge ch : list){
				totalMoney += ch.money;
			}
			return totalMoney;
		}
	}

}
