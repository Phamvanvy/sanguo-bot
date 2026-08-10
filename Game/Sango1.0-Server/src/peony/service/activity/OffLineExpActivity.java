package peony.service.activity;

import peony.game.Server;
import peony.game.exp.ExpService;

public class OffLineExpActivity implements IActivityImpl{
	
	protected Activity activity;
	
	public OffLineExpActivity(Activity owner) {
		this.activity = owner;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public void startup() throws Exception {
		ExpService expService = Server.server.getServiceRegistry().getExpService();
		String config = activity.getConfigData();
		if(config!=null){
			expService.offLineRatio = Float.parseFloat(config);
		}
	}
	
	public void shutdown() {
		ExpService expService = Server.server.getServiceRegistry().getExpService();
		expService.offLineRatio = 1.0f;
	}

	public void clear() {
		
	}

	public void load() {
		
	}

	public void save() {
			
	}
}
