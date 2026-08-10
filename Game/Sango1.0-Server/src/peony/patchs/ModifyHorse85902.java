package peony.patchs;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.TransactionBag;
import peony.game.TransactionBagGrid;

public class ModifyHorse85902 implements Runnable{
	public void run(){
		Player p = ObjectAccessor.getPlayer(85902);
		boolean dup = false;
		Set<Integer> set = new TreeSet<Integer>();
		for(Horse h:p.horseBag.horses){
			if(set.contains(h.instanceId)){
				dup = true;
				break;
			}else{
				set.add(h.instanceId);
			}
		}
		if(dup){
			System.out.println("12731dup");
			for(Horse h:p.horseBag.horses){
				h.instanceId = Server.server.getServiceRegistry().getSleepyCatService().generatorHorseId();
				for(GameItem item:h.equs.equs){
					if(item!=null&&item.bindInstance!=-1){
						item.bindInstance = 0;
						System.out.println("Item["+item.template.name+"]");
					}
				}
			}
			try {
				Field field = TransactionBag.class.getDeclaredField("grids");
				field.setAccessible(true);
				List<TransactionBagGrid> grids = (List<TransactionBagGrid>)field.get(p.bag);
				for(TransactionBagGrid grid:grids){
					if(grid!=null&&grid.getItem()!=null){
						GameItem item = grid.getItem();
						if(item.bindInstance!=-1&&item.bindInstance!=0){
							item.bindInstance = 0;
							System.out.println("BagItem["+item.template.name+"]");
						}
						if(item.object!=null&&item.object instanceof Horse){
							Horse h = (Horse)item.object;
							h.instanceId = Server.server.getServiceRegistry().getSleepyCatService().generatorHorseId();
							System.out.println("Item["+item.template.name+"]");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
