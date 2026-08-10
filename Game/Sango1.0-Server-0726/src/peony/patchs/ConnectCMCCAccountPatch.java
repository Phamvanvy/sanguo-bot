package peony.patchs;

import peony.game.Server;
import peony.service.account.cmcc.CmccAccountService;


public class ConnectCMCCAccountPatch implements Runnable {

	public void run() {
		CmccAccountService slaveAccountService = new CmccAccountService(
				Server.server.getConfig().configurationAt("slaveaccount"));
		try {
			slaveAccountService.startup();
			Server.server.getServiceRegistry().setSlaveAccountService(slaveAccountService);
			System.out.println("ConnectCMCCAccount OK!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ConnectCMCCAccount ERROR");
		}
		
	}

}
