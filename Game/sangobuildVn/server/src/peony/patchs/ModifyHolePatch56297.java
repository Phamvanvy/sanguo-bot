package peony.patchs;

import java.util.List;

import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.TransactionBagGrid;
import peony.game.itemenhance.ItemEnhance;

public class ModifyHolePatch56297 implements Runnable {

	public void run() {
		Player p = ObjectAccessor.getPlayer(56297);
		if(p!=null){
	        for(GameItem item:p.equipments.equs){
	        	if(item!=null){
	        		checkAndRepaire(item);
	        	}
	        }
	        List<TransactionBagGrid> l = p.bag.getGrids();
	        for(TransactionBagGrid g:l){
	        	if(g.getItem()!=null){
	        		checkAndRepaire(g.getItem());
	        	}
	        }
	        l = p.depot.getGrids();
	        for(TransactionBagGrid g:l){
	        	if(g.getItem()!=null){
	        		checkAndRepaire(g.getItem());
	        	}
	        }
		}else{
			System.out.println("not found");
		}
	}
	
	protected void checkAndRepaire(GameItem item){
		if(item.object!=null&&item.object instanceof ItemEnhance){
			ItemEnhance en = (ItemEnhance)item.object;
			if((en.getAddHole()+item.template.equipment.initHole)<en.getJewelCount()){
				en.setAddHole(en.getJewelCount()-(en.getAddHole()+item.template.equipment.initHole));
//				en.setAddHole(2);
				System.out.println(item.template.name+"addHole");
			}
		}
	}

}
