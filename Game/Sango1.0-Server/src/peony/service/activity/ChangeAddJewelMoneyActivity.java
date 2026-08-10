package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.itemenhance.JewelService;

public class ChangeAddJewelMoneyActivity implements IActivityImpl{

	private static Logger log = Logger.getLogger(ChangeAddJewelMoneyActivity.class);
	protected Activity activity;
	protected float rate = 1;
	
	public ChangeAddJewelMoneyActivity(Activity owner){
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		try{
			String config = activity.configData;
			rate = Float.parseFloat(config);
		} catch(Exception e){
			rate = 1.0f;
		}
	}
	
	public void clear() {
		
	}

	public void save() {
	
		
	}

	public void shutdown() {
		JewelService.addJewelRate = 1.0f;
	}

	public void startup() throws Exception {
		JewelService.addJewelRate = rate;
	}
}
