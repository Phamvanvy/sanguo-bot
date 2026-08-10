package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;
import peony.service.stat.StatService;

public class TrackPlayerLogActivity implements IActivityImpl {
	private static Logger log = Logger.getLogger(TrackPlayerLogActivity.class);
	
	protected Activity activity;
	
	public TrackPlayerLogActivity(Activity owner) {
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
		StatService.trackLogPlayer.clear();
	}

	public void startup() throws Exception {
		String config = activity.getConfigData();
		if(config!=null){
			String[] str = config.split(",");
			for(String s : str){
				StatService.trackLogPlayer.add(Integer.parseInt(s));
			}
		}
		
	}
}