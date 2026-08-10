package peony.patchs;

import peony.game.Server;

public class HorseIdCheck implements Runnable{
	public void run(){
		for(int i=0;i<5;i++)
			System.out.println(Server.server.getServiceRegistry().getSleepyCatService().generatorHorseId());
	}
}
