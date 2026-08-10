package peony.patchs;

import java.lang.reflect.Field;
import java.util.List;

import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.itemenhance.JewelService;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.Item;

public class JewelPatch implements Runnable {

	public void run() {
        ProjectData data = Server.server.getServiceRegistry().getDataService().data;
        
        // 找出所有的宝石物品
        ItemTemplate[][] jewelItems = new ItemTemplate[JewelService.MAX_JEWEL_TYPES][JewelService.JEWEL_LEVELS];
        List<DataObject> items = data.getDataListByType(Item.class);
        for (DataObject dobj : items) {
            Item item = (Item)dobj;
            if (item.type == Item.TYPE_JEWEL && !item.isFlaw) {
                jewelItems[item.jewelAttrType][item.playerLevel - 1] = ObjectAccessor.getItemTemplate(item.id);
            }
        }
        
        JewelService s = Server.server.getServiceRegistry().getJewelService();
        try {
			Field f = JewelService.class.getDeclaredField("jewels");
			f.setAccessible(true);
			f.set(s, jewelItems);
			System.out.println("jewel patch ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
