package peony.service.activity;

import org.apache.log4j.Logger;
import peony.game.Horse;
import peony.service.cards.CardRockCall;

/**
 * 周年庆活动
 * @author mfou
 */

public class AnniversaryActivity implements IActivityImpl{
	
	private static Logger log = Logger.getLogger(AnniversaryActivity.class);
	
    private Activity activity;
	
	public AnniversaryActivity(Activity owner){
		this.activity = owner;
	}

	public void clear() {
		
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		try{
			String config = activity.configData;
			String[] str = config.split(",");
			for(String str1 : str){
				String[] str2 = str1.split(":");
				if(str2[0].equalsIgnoreCase("CER"))
					CardRockCall.GAINCARDEXPRATIO = Float.parseFloat(str2[1]);
			}
		} catch(Exception e){
			CardRockCall.GAINCARDEXPRATIO = 1.0f;
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		CardRockCall.GAINCARDEXPRATIO = 1.0f;
	}

	public void startup() throws Exception {
		
	}

}
