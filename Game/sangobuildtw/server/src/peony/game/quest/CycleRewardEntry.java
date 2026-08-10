package peony.game.quest;

import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;

public class CycleRewardEntry implements QuestRewardEntry {

	public static int[] CYCLE_ITEMS = {1673,1674,1675,1676,1677,1678,1679};
	
	public void gain(Gain gain) {
		Player p = gain.getPlayer();
		int cycle = p.asmVm.getFinishedCycle();
		int id = CYCLE_ITEMS[cycle];
		GameItem item = ObjectAccessor.createGameItem(id);
		gain.addGainItem(item, 1);
	}

}
