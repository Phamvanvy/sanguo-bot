package peony.patchs;

import peony.game.GameItem;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.itemenhance.ItemEnhance;

public class KingEquipPatch implements Runnable {

	public static int playerId = 554038;
	public static int equipId = 1007869;
	
	public void run() {
		Player player = ObjectAccessor.getPlayer(playerId);
		if(player!=null){
			for(GameItem item : player.equipments.equs){
				if(item!=null && item.template!=null && item.template.isEquipment() && item.template.id==equipId){
					if(item.object!=null && item.object instanceof ItemEnhance){
						ItemEnhance ie = (ItemEnhance)item.object;
						int jewelId = ie.getJewel(0);
						ie.removeJewel(0);
						PlayerTransaction tx = player.newTransaction("JEWELPATCH");
						try {
							player.bag.addGameItemComplete(ObjectAccessor.createGameItem(jewelId), 1, tx, false);
							tx.commit();
						} catch (NoEnoughSpaceException e) {
							tx.rollback();
						}
					}
				}
			}
		}
	}

}
