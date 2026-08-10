package peony.service.activity;

import org.apache.log4j.Logger;

import peony.game.itemenhance.JewelService;

public class ChangeAddHoleSucRateActivity implements IActivityImpl{

	private static Logger log = Logger.getLogger(ChangeAddHoleSucRateActivity.class);
	protected Activity activity;
	protected int[] sourceRate = {10000,9000,3000,1000,400,300,200};
	protected int[] addHoleSucRate = {10000,9000,3000,1000,400,300,200};
	
	public ChangeAddHoleSucRateActivity(Activity owner){
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
			String[] str = config.split(",");
			for(int i=0;i<str.length;i++){
				addHoleSucRate[i] = Integer.parseInt(str[i]);
			}
		} catch (Exception e) {
			for(int i=0;i<sourceRate.length;i++){
				addHoleSucRate[i] = sourceRate[i];
			}
		}
	}
	
	public void clear() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		for(int i=0;i<sourceRate.length;i++){
			JewelService.addHoleSucRate[i] = sourceRate[i];
		}
	}

	public void startup() throws Exception {
		for(int i=0;i<addHoleSucRate.length;i++){
			JewelService.addHoleSucRate[i] = addHoleSucRate[i];
		}
	}
}
