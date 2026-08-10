package peony.patchs;

import peony.game.Server;
import peony.net.ProxyManagingService;
import peony.net.ProxyManagingService.Proxy;

public class MaxPlayerPatch implements Runnable {

	public void run() {
	    ProxyManagingService pms = (ProxyManagingService)
        Server.server.getServiceRegistry().getService(ProxyManagingService.class);
	    for(Proxy proxy:pms.getProxys()){
	    	if(proxy.ip.startsWith("221.130")){
	    		proxy.maxPlayer = 2000;
	    		System.out.println("maxplayer ok");
	    	}
	    }
	}

}
