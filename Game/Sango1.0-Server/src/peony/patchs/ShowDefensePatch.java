package peony.patchs;

import peony.game.Player;

public class ShowDefensePatch implements Runnable {

	public void run() {
		
	}
	
	private void showDefense(Player p){
		System.out.println("PLAYER["+p.id+"]NAME["+p.name+"]DEFENSE["+p.defense+"]PERCENT["+p.defensePercent+"]");
	}

}
