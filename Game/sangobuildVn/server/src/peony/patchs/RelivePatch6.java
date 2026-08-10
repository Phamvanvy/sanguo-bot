package peony.patchs;

import peony.game.ObjectAccessor;
import peony.game.Player;

public class RelivePatch6 implements Runnable{
	public void run(){
		Player p = ObjectAccessor.getPlayer(26403);
		if(p!=null&&!p.isAlive()){
			p.relive(p.maxmp/2,	 p.maxhp/2);
			System.out.println("26403ALIVE");
		}
//		p = ObjectAccessor.getPlayer(4110);
//		if(p!=null&&!p.isAlive()){
//			p.relive(p.maxmp/2,	 p.maxhp/2);
//			System.out.println("4110ALIVE");
//		}
	}
}
