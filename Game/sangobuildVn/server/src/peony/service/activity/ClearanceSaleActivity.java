package peony.service.activity;

import peony.game.Server;
import peony.service.ClearanceSaleService;

/**
 * 团购活动
 */
public class ClearanceSaleActivity implements IActivityImpl {

	private Activity activity;
	private int decItem; //团购报名扣除物品
	private int decItemCount; //团购报名扣除物品数量
	private int minPlayers; //团购最低人数
	private int maxPlayers; //团购人数限制
	private int rewardItem; //团购物品
	private int costPrice; //原价
	private int price; //现价
	private String nextItem; //下期商品
	
	public ClearanceSaleActivity(Activity act) {
		this.activity = act;
	}
	
	public void clear() {
		
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		String config = activity.configData;
		String[] str1 = config.split(",");
		for(String str2 : str1){
			String[] str3 = str2.split(":");
			String varName = str3[0];
			String value = str3[1];
			if(varName.equals("decitem")){
				this.decItem = Integer.parseInt(value);
			}else if(varName.equals("decitemcount")){
				this.decItemCount = Integer.parseInt(value);
			}else if(varName.equals("minplayers")){
				this.minPlayers = Integer.parseInt(value);
			}else if(varName.equals("maxplayers")){
				this.maxPlayers = Integer.parseInt(value);
			}else if(varName.equals("rewarditem")){
				this.rewardItem = Integer.parseInt(value);
			}else if(varName.equals("price")){
				this.price = Integer.parseInt(value);
			}else if(varName.equals("nextitem")){
				this.nextItem = value;
			}else if(varName.equals("costprice")){
				this.costPrice = Integer.parseInt(value);
			}
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		ClearanceSaleService service = Server.server.getServiceRegistry().getClearanceSaleService();
		service.end();
	}

	public void startup() throws Exception {
		ClearanceSaleService service = Server.server.getServiceRegistry().getClearanceSaleService();
		long endTime = activity.schedule.stopTime.getTime();
		service.begin(decItem, decItemCount, minPlayers, maxPlayers, rewardItem, endTime, costPrice, price, nextItem);
	}

}
