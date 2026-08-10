package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.ranking.RankingService;

public class CardExpRankingActivity implements IActivityImpl{

	private static Logger log = Logger.getLogger(CardExpRankingActivity.class);
	protected Activity activity;
	
	public CardExpRankingActivity(Activity owner){
		this.activity = owner;
	}
	
	public void clear() {
		
		
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		
		
	}

	public void save() {
		
		
	}

	public void shutdown() {
		RankingService service = Server.server.getServiceRegistry().getRankingService();
		service.rewards = new int[20];
	}

	public void startup() throws Exception {
		// 记录由GM工具输入的额度与奖励
		String config = activity.configData;
		String[] str0 = config.split(";");
		RankingService service = Server.server.getServiceRegistry().getRankingService();
		int index = 0;
		for(String str : str0){
			String[] s0 = str.split(",");
			service.rewards[index]=Integer.parseInt(s0[0]);
			service.rewards[index+1]=Integer.parseInt(s0[1]);
			index+=2;
			if(index>service.rewards.length-1)
				break;
		}
	}

}
