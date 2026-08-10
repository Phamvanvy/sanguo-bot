package peony.patchs;

import org.apache.log4j.Logger;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;
import peony.service.activity.TenthAnniversaryService;

public class TenActPatch implements Runnable, ServiceEventListener {
	private static final Logger log = Logger.getLogger(TenActPatch.class);
	public void run() {
//		Server.server.getEventManager().registerListener(this);
//		TenthAnniversaryService service=Server.server.getServiceRegistry().getTenthAnniversaryService();
//		String flag3=service.getWeekSalary(TenthAnniversaryService.DAY_SALARY_3);
//		String flag30=service.getWeekSalary(TenthAnniversaryService.DAY_SALARY_30);
//		String flag70=service.getWeekSalary(TenthAnniversaryService.DAY_SALARY_70);
//		VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
//		for(Player online : ObjectAccessor.players.values()){
//			if(online!=null){
//				AccountProperty ap=vipService.getAccountProperty(online.accountId);
//				int currentDaySalary=ap.pool.getInt(TenthAnniversaryService.DAYSALARY, 0);//今天完成的工资数
//				if(ap.pool.getInt("TENACTPATCH", 0)==0){
//					log.info("[TENACTPATCHONLINEPLAYER_PRE]ACC["+online.accountId+"]ID["+online.id
//							+"]WEEKSALARY_3["+ap.pool.getInt(flag3, 0)
//							+"]WEEKSALARY_30["+ap.pool.getInt(flag30, 0)
//							+"]WEEKSALARY_70["+ap.pool.getInt(flag70, 0)+"]"
//					);
//					ap.pool.setInt("TENACTPATCH", 1);
//					if(ap.pool.getInt(flag3, 0)==-1){
//						int count=(currentDaySalary>=3?1:0);
//						ap.pool.setInt(flag3, 2+count);
//					}
//					if(ap.pool.getInt(flag30, 0)==-1){
//						int count=(currentDaySalary>=30?1:0);
//						ap.pool.setInt(flag30, 2+count);
//					}
//					if(ap.pool.getInt(flag70, 0)==-1){
//						int count=(currentDaySalary>=70?1:0);
//						ap.pool.setInt(flag70, 2+count);
//					}
//					log.info("[TENACTPATCHONLINEPLAYER_AFTER]ACC["+online.accountId+"]ID["+online.id
//							+"]WEEKSALARY_3["+ap.pool.getInt(flag3, 0)
//							+"]WEEKSALARY_30["+ap.pool.getInt(flag30, 0)
//							+"]WEEKSALARY_70["+ap.pool.getInt(flag70, 0)+"]"
//							);
//				}
//			}
//		}
//		System.out.println("-----------------TENACTPATCH_OK!!");
	}

	public int[] getEventTypes() {
		return new int[]{ServiceEvent.EVENT_PLAYER_FIRSTLOAD};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_PLAYER_FIRSTLOAD:
//			TenthAnniversaryService service=Server.server.getServiceRegistry().getTenthAnniversaryService();
//			String flag3=service.getWeekSalary(TenthAnniversaryService.DAY_SALARY_3);
//			String flag30=service.getWeekSalary(TenthAnniversaryService.DAY_SALARY_30);
//			String flag70=service.getWeekSalary(TenthAnniversaryService.DAY_SALARY_70);
//			VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
//			Player p=(Player)event.param1;
//			if(p!=null){
//				AccountProperty ap=vipService.getAccountProperty(p.accountId);
//				int currentDaySalary=ap.pool.getInt(TenthAnniversaryService.DAYSALARY, 0);//今天完成的工资数
//				if(ap.pool.getInt(TenthAnniversaryService.WEEKFLAG,0)==service.weekFlag){
//					if(ap.pool.getInt("TENACTPATCH", 0)==0){
//						ap.pool.setInt("TENACTPATCH", 1);
//						log.info("[TENACTPATCHOFFLINEPLAYER_PRE]ACC["+p.accountId+"]ID["+p.id
//								+"]WEEKSALARY_3["+ap.pool.getInt(flag3, 0)
//								+"]WEEKSALARY_30["+ap.pool.getInt(flag30, 0)
//								+"]WEEKSALARY_70["+ap.pool.getInt(flag70, 0)+"]"
//						);
//						if(ap.pool.getInt(flag3, 0)==-1){
//							int count=(currentDaySalary>=3?1:0);
//							ap.pool.setInt(flag3, 2+count);
//						}
//						if(ap.pool.getInt(flag30, 0)==-1){
//							int count=(currentDaySalary>=30?1:0);
//							ap.pool.setInt(flag30, 2+count);
//						}
//						if(ap.pool.getInt(flag70, 0)==-1){
//							int count=(currentDaySalary>=70?1:0);
//							ap.pool.setInt(flag70, 2+count);
//						}
//						log.info("[TENACTPATCHOFFLINEPLAYER_AFTER]ACC["+p.accountId+"]ID["+p.id
//								+"]WEEKSALARY_3["+ap.pool.getInt(flag3, 0)
//								+"]WEEKSALARY_30["+ap.pool.getInt(flag30, 0)
//								+"]WEEKSALARY_70["+ap.pool.getInt(flag70, 0)+"]"
//						);
//					}
//				}
//			}
			break;
		}
	}

}
