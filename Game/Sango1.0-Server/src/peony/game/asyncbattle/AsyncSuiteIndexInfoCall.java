package peony.game.asyncbattle;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.net.ClientSession;

public class AsyncSuiteIndexInfoCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int itemId;
	protected int itemInstanceId;
	protected int type;
	protected int instanceId;
	protected int horseInstanceId;
	
	protected Player player;
	
	public AsyncSuiteIndexInfoCall(ClientSession session, int serial,
			int itemId, int itemInstanceId, int type, int instanceId,
			int horseInstanceId) {
		super(session);
		this.serial = serial;
		this.itemId = itemId;
		this.itemInstanceId = itemInstanceId;
		this.type = type;
		this.instanceId = instanceId;
		this.horseInstanceId = horseInstanceId;
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(player!=null){
			player.suiteIndex(serial, itemId, itemInstanceId, type, instanceId, horseInstanceId);
		}
	}

}
