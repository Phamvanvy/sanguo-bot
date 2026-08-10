package peony.service.activity;

import peony.game.Player;
import peony.service.player.PlayerService;

public class AntiBotActivity implements IActivityImpl {

	protected Activity act;
	protected long baseTime;
	
	public AntiBotActivity(Activity act){
		this.act = act;
	}
	
	public void clear() {
		
	}

	public Activity getActivity() {
		return act;
	}

	public void load() {
		String data = act.configData;
		String[] str = data.split(";");
		for(String str1 : str){
			String[] str2 = str1.split(":");
			if(str2[0].equals("basetime")){
				baseTime = Long.parseLong(str2[1]);
			}
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		PlayerService.baseMuteAccountTime = 0;
		Player.antiBotModel = Player.ANTIPLUG_MODEL_LOG;
	}

	public void startup() throws Exception {
		PlayerService.baseMuteAccountTime = baseTime;
		Player.antiBotModel = Player.ANTIPLUG_MODEL_NONBENEFIT;
	}

}
