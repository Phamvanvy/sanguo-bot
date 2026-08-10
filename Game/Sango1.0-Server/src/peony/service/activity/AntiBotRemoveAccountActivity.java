package peony.service.activity;

import java.util.ArrayList;
import java.util.List;
import peony.game.Server;
import peony.service.player.PlayerService;
import peony.service.sleepycat.SleepyCatService;

public class AntiBotRemoveAccountActivity implements IActivityImpl {

	protected Activity act;
	protected List<Integer> accounts = new ArrayList<Integer>();
	protected boolean isRemoveAll = false;
	
	public AntiBotRemoveAccountActivity(Activity act){
		this.act = act;
	}
	
	public void clear() {
		
	}

	public Activity getActivity() {
		return act;
	}

	public void load() {
		String data = act.configData;
		if(!data.equals("all")){
			String[] str = data.split(",");
			for(String str1 : str){
				accounts.add(Integer.parseInt(str1));
			}
		}else{
			isRemoveAll = true;
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		PlayerService playerService = Server.server.getServiceRegistry().getPlayerService();
		for(int accountId : accounts){
			playerService.removeAccountMute(accountId);
		}
		if(isRemoveAll){
			SleepyCatService dbservice = Server.server.getServiceRegistry().getSleepyCatService();
			dbservice.removeDatabase("kickaccountdb");
			dbservice.kickAccountDB = dbservice.openDatabase("kickaccountdb");
		}
	}

}
