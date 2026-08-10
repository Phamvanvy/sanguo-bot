package peony.game;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.net.ClientSession;
import peony.service.stat.Achievement;
import peony.service.stat.PvpInfo;
import peony.service.stat.StatService;

public class GetConsumnCall extends ClientSessionAsyncCall{
	
	protected Player p;
	protected int money;
	
	public GetConsumnCall(ClientSession session,Player player,int money) {
		super(session);
		this.p = player;
		this.money = money;
	}

	

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(p!=null){
			StatService service = Server.server.getServiceRegistry().getStatService();
			PvpInfo pvpInfo = service.getPvpInfo(p.id,p.faction);
//				int maxNum = Integer.parseInt(list.get(list.size()-1).param1);
				int total = 0;
				Achievement ach = service.getAchievementById(73);
				if(ach!=null){
					int maxNum = Integer.parseInt(ach.param1);
					if(pvpInfo.pool.getString(service.getPropertyOfIMoney(maxNum,true)).equals("")){
						if(pvpInfo.pool.getInt(StatService.PROPERTY_IMONEYUSE_COUNT, 0)==0){
						    total = Server.server.getServiceRegistry().getDbService().ibuyDAO.getTotalConsumeTillNow(p.id);
							pvpInfo.pool.setInt(StatService.PROPERTY_IMONEYUSE_COUNT,total);
						}else if(pvpInfo.pool.getInt(StatService.PROPERTY_IMONEYUSE_COUNT, 0) > 0){
							total = pvpInfo.pool.getInt(StatService.PROPERTY_IMONEYUSE_COUNT,0)+money;
							pvpInfo.pool.setInt(StatService.PROPERTY_IMONEYUSE_COUNT, total);
						}
						for(int i=69;i<74;i++){
							Achievement a = service.getAchievementById(i);
							if(a!=null){
								int num = Integer.parseInt(a.param1);
								if(pvpInfo.pool.getString(service.getPropertyOfIMoney(num,true)).equals("")){
									if(total >=num*3600 || pvpInfo.pool.getInt(StatService.PROPERTY_IMONEYUSE_COUNT, 0)<0){
										pvpInfo.pool.setString(service.getPropertyOfIMoney(num,true), service.getFinishTime(System.currentTimeMillis()));
										if(num==maxNum){
											service.setMessage(p,a,true,true);
										} else if(num<maxNum && pvpInfo.pool.getInt(StatService.PROPERTY_IMONEYUSE_COUNT, 0)>0){
											service.setMessage(p,a,false,true);
										}
									}
								} 
								if(pvpInfo.pool.getString(service.getPropertyOfIMoney(num,true)).equals("")){
										break;
								}
						 }
					}
				}
			}
		}
	}
}
