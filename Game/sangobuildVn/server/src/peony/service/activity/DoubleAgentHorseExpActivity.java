package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.Server;

public class DoubleAgentHorseExpActivity implements IActivityImpl {

	private static Logger log = Logger.getLogger(DoubleAgentHorseExpActivity.class);
	protected Activity activity;
	
	public DoubleAgentHorseExpActivity(Activity owner){
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
		Server.server.agentHorseExp = 1.5f;
	}

	public void clear() {
		Server.server.agentHorseExp = 1.0f;
	}

}
