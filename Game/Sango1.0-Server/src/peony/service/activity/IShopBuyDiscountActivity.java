package peony.service.activity;

import peony.game.Server;
import peony.service.shop.ShopService;

/**
 * 商店整体打折
 * @author dchen
 */
public class IShopBuyDiscountActivity implements IActivityImpl {

	protected Activity activity;
	
	protected int shopId;
	protected int buyObjectType;
	protected int discount;
	
	public IShopBuyDiscountActivity(Activity activity){
		this.activity = activity;
	}
	
	public void clear() {
		
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		String data = activity.configData;
		String[] data0 = data.split(";");
		for(String data1 : data0){
			String[] data2 = data1.split(":");
			if(data2[0].equals("shop"))
				shopId = Integer.parseInt(data2[1]);
			else if(data2[0].equals("type"))
				buyObjectType = Integer.parseInt(data2[1]);
			else if(data2[0].equals("discount"))
				discount = Integer.parseInt(data2[1]);
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		ShopService service = Server.server.getServiceRegistry().getShopService();
		service.removeShopDiscount(shopId, buyObjectType);
	}
	
	public void startup() throws Exception {
		ShopService service = Server.server.getServiceRegistry().getShopService();
		service.addShopDiscount(shopId, buyObjectType, discount);
	}

}
