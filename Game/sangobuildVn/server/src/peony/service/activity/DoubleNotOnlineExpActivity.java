package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;

/**
 * 一盒酥1.5倍经验
 * @author dchen
 */
public class DoubleNotOnlineExpActivity implements IActivityImpl {

	private static Logger log = Logger.getLogger(DoubleNotOnlineExpActivity.class);
	protected Activity activity;
	protected float ratio = 1;
	
	public DoubleNotOnlineExpActivity(Activity owner){
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
		Server.server.onlineExpRatio = 1.0f;
	}

	public void startup() throws Exception {
		Server.server.onlineExpRatio = ratio;
		
	}

	public void clear() {
		
	}

}
