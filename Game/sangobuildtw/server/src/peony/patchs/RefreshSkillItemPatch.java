package peony.patchs;

import java.util.Iterator;
import java.util.List;

import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.UseType;

import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.ShopItem;

public class RefreshSkillItemPatch implements Runnable{
	public void run(){
		ItemTemplate template = ObjectAccessor.getItemTemplate(1243);
		template.useType = UseType.NOUSETYPE;
		Shop shop = Server.server.getServiceRegistry().getShopService().findShop(21);
		List<ShopItem> items = shop.items;
		Iterator<ShopItem> ite = items.iterator();
		while(ite.hasNext()){
			ShopItem item = ite.next();
			if(item.item.id==1243){
				ite.remove();
				break;
			}
		}
		System.out.println("RefreshSkillItemPatchOk");
	}
}
