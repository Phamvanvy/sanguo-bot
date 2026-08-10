package peony.patchs;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Time;

public class PvPFactionCheck implements Runnable{

	public void run(){
		for(Player p:ObjectAccessor.players.values()){
			System.out.println("TIME["+Time.currTime+"]");
			if(p.level>Player.MAX_PVE_LEVEL&&(!p.isPvpFaction()||p.pvpFactionTime!=0)){
				System.out.println("ID["+p.id+"]LEVEL["+p.level+"]PVPFACTION["+p.isPvpFaction()+"]TIME["+p.pvpFactionTime+"]");
			}
		}
	}
}
