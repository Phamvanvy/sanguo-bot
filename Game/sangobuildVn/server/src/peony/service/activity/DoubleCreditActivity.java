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
	protected float ratio = 1;
	
	public DoubleCreditActivity(Activity owner){
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		String config = activity.configData;
		parseConfig(config);
	}
	
	protected void parseConfig(String config){
		try {
			String[] str = config.split(":");
			if(str[0].equals("ratio")){
				this.ratio = new Float(str[1]);
			}
		} catch (Exception e) {
			ratio = 1f;
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		Server.server.creditRatio = 1.0f;
	}

	public void startup() throws Exception {
		Server.server.creditRatio = ratio;
	}
	

	public void clear() {
	}
	

}
