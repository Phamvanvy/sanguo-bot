package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.ranking.RankingService;

public class RockCardCountActivity implements IActivityImpl {
	
	private static Logger log = Logger.getLogger(RockCardCountActivity.class);
	
	protected Activity activity;
	
	public RockCardCountActivity(Activity owner) {
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
		RankingService rankingService = Server.server.getServiceRegistry().getRankingService();
		rankingService.BASE_ROCKCOUNT = 0;
		rankingService.ROCKCOUNT_REWARD = null;
		
	}
	
	///newact "摇卡次数奖励" "标题" "活动说明" "starttime=2012-1-21 09:00:00;stoptime=2012-12-31 23:59:00" "RockCardCountActivity" "count:10,20,30;reward:1311,10,1311,9,1311,8" 1
	///enableact "摇卡次数奖励" 1

	public void startup() throws Exception {
		RankingService rankingService = Server.server.getServiceRegistry().getRankingService();
		String config = activity.configData;
		String[] str0 = config.split(";");
		for(String str1 : str0){
			String[] str = str1.split(":");
			if(str[0].equals("count")){
				rankingService.BASE_ROCKCOUNT = Integer.parseInt(str[1]);
			}else if(str[0].equals("reward")){
				String[] temp = str[1].split(",");
				rankingService.ROCKCOUNT_REWARD = new int[temp.length];
				for(int i=0;i<temp.length;i++){
					rankingService.ROCKCOUNT_REWARD[i] = Integer.parseInt(temp[i]);
				}
			}
		}
	}
}
