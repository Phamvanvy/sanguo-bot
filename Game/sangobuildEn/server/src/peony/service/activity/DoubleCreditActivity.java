package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;

/**
 * 购物1.5倍战功活动。
 * @author mfou
 */

public class DoubleCreditActivity implements IActivityImpl{
	private static Logger log = Logger.getLogger(DoubleCreditActivity.class);
	protected Activity activity;
	
	public DoubleCreditActivity(Activity owner){
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		Server.server.creditRatio = 1.5f;
		
	}
	

	public void clear() {
		Server.server.creditRatio = 1.0f;
	}
	

}
