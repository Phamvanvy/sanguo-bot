package peony.net;

import org.apache.commons.configuration.Configuration;

public class AdminClientSessionService extends DirectClientSessionService {

	
	public AdminClientSessionService(Configuration config,PacketHandler handler){
		super(config,handler);
	}
	
}
