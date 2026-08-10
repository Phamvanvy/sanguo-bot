package peony.patchs;

import peony.game.Server;

public class AntiCheatFlagPatch implements Runnable {
	public void run() {
		Server.server.antiCheat = !Server.server.antiCheat;
		System.out.println("Current anti_cheat = " + Server.server.antiCheat);
	}
}
