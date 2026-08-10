package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.activity.Activity;
import peony.service.activity.IActivityImpl;
import peony.service.ranking.RankingService;


public class PrayActivity implements IActivityImpl {
	
	private static Logger log = Logger.getLogger(PrayActivity.class);
	
	protected Activity activity;
	
	public PrayActivity(Activity owner) {
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}

	public void clear() {
		
		
	}

	public void load() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		RankingService service = Server.server.getServiceRegistry().getRankingService();
		service.prayRewards = new int[20];
	}

	///newact "祈福奖励" "标题" "活动说明" "starttime=2012-1-21 09:00:00;stoptime=2012-12-31 23:59:00" "PrayActivity" "1311,10;1311,9;1311,8" 1
	///enableact "祈福奖励" 1
	public void startup() throws Exception {
		// 记录由GM工具输入的额度与奖励
		String config = activity.getConfigData();
		String[] str0 = config.split(";");
		RankingService service = Server.server.getServiceRegistry().getRankingService();
		int index = 0;
		for(String str : str0){
			String[] s0 = str.split(",");
			service.prayRewards[index]=Integer.parseInt(s0[0]);
			service.prayRewards[index+1]=Integer.parseInt(s0[1]);
			index+=2;
			if(index>service.prayRewards.length-1)
				break;
		}
	}

	
	
}
