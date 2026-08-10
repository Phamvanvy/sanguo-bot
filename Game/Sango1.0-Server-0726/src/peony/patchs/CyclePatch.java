package peony.patchs;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.vm.ASMGameVM;

public class CyclePatch implements Runnable {

	public void run() {
		Player p = ObjectAccessor.getPlayer(429);
		ASMGameVM vm = p.asmVm;
		System.out.println("ID[429]CurrentCycle["+vm.getCurrentCycle()+"]CycleIndex["+vm.getCurrentCycleIndex()+"]CycleState["+vm.getCurrentCycleState()+"]Finished["+vm.getFinishedCycle());
	}

}
