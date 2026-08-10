package peony.patchs;

import java.lang.reflect.Field;

import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.itemeffect.HorseItemEffect;

public class HorseImage1147 implements Runnable {
	
	public static final int[] ITEM_IDS = {
		117,305,306,307,308,818,820,822,823,824,825,826,827,828,829,795,
		
	};
	
	
	
	public void run() {
		Player p = ObjectAccessor.getPlayer(1147);
		if (p != null) {
//			for (Horse h : p.horseBag.horses) {
//				System.out.println("id:" + h.template.id);
//				System.out.println("name:" + h.name);
//				System.out.println("iconid:" + h.iconId);
//				System.out.println("initlevel:" + h.initLevel);
//				System.out.println("itemid:" + h.itemId);
//				System.out.println("imageid:" + h.imageId);
//			}
			for(Horse h: p.horseBag.horses){
				if(h.iconId==0||h.imageId==0){
					try {
						repaire(h);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void repaire(Horse h) throws Exception{
		for(int id:ITEM_IDS){
			ItemTemplate it = ObjectAccessor.getItemTemplate(id);
			if(it.useType.effect instanceof HorseItemEffect){
				HorseItemEffect effect = (HorseItemEffect)it.useType.effect;
				Field field = HorseItemEffect.class.getDeclaredField("horseTypes");
				field.setAccessible(true);
				int[] types = (int[])field.get(effect);
				for(int type:types){
					if(type==h.template.id){
						Field imageField = HorseItemEffect.class.getDeclaredField("imageId");
						imageField.setAccessible(true);
						int imageId = imageField.getInt(effect);
						h.imageId = imageId;
						h.itemId = id;
						h.iconId = it.showType;
						System.out.println(h.name+"OK");
						return;
					}
				}
			}
		}
	}
}
