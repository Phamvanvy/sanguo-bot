package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.ranking.RankingService;

public class PrayCountRewardActivity implements IActivityImpl {
	
	private static Logger log = Logger.getLogger(PrayCountRewardActivity.class);
	
	protected Activity activity;
	
	public PrayCountRewardActivity(Activity owner) {
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
		rankingService.BASE_PRAYCOUNT = 0;
		rankingService.PRAYCOUNT_REWARD = null;
		
	}

	public void startup() throws Exception {
		RankingService rankingService = Server.server.getServiceRegistry().getRankingService();
		String config = activity.configData;
		String[] str0 = config.split(";");
		for(String str1 : str0){
			String[] str = str1.split(":");
			if(str[0].equals("count")){
				rankingService.BASE_PRAYCOUNT = Integer.parseInt(str[1]);
			}else if(str[0].equals("reward")){
				String[] temp = str[1].split(",");
				rankingService.PRAYCOUNT_REWARD = new int[temp.length];
				for(int i=0;i<temp.length;i++){
					rankingService.PRAYCOUNT_REWARD[i] = Integer.parseInt(temp[i]);
				}
			}
		}
		
	}
}
