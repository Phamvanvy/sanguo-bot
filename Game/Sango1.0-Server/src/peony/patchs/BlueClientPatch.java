package peony.patchs;

import peony.game.Server;
import peony.service.player.PlayerService;

public class BlueClientPatch implements Runnable {

	public void run() {
		if(!Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW)){
			PlayerService.oldAndroidMods = new String[]{"AndroidNew","AndroidLargeNew","iOSNewUI",
					"iOSNewUILarge","Nokia5800New"};
			System.out.println("_______________BlueClientPatch load OK");
		}else{
			PlayerService.oldAndroidMods = new String[]{};
			System.out.println("_______________TAIWAN BlueClientPatch load OK");
		}
	}

}
