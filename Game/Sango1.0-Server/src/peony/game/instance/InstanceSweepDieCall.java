package peony.game.instance;

import peony.common.ClientSessionAsyncCall;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.attendant.Attendant;
import peony.net.ClientSession;

/**
 * 副本扫荡前检测是否已击杀过boss
 * @author mfou
 *
 */

public class InstanceSweepDieCall extends ClientSessionAsyncCall{
	
	protected Unit dieUnit;
	protected Unit killUnit;
	InstanceSweepService sweepService = Server.server.getServiceRegistry().getInstanceSweepService();
	

	public InstanceSweepDieCall(ClientSession session,Unit dieUnit,Unit killUnit) {
		super(session);
		this.dieUnit = dieUnit;
		this.killUnit = killUnit;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		int instanceId = sweepService.getInstanceId(dieUnit.id);
		if(instanceId!=-1){
			Player player = null;
			if(killUnit.type == GameObject.TYPE_ATTENDANT){
				Attendant att = (Attendant)killUnit;
				if(att!=null){
					player = att.owner;
				}
			}else{
				player = (Player)killUnit;
			}
			if(player==null)
				return;
			NormalInstance ni = (NormalInstance)player.map.map.instance;
			if(ni!=null){
				for(GameObjectRef ref : ni.refs){
					Player p = ObjectAccessor.getPlayer(ref.id);
					if(p!=null){
						if(p.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(instanceId), 0)==0){
							p.pool.setInt(InstanceSweepService.getPropertyOfDayTimes(instanceId), 1);
						}
					}
				}
			}
		}
	}

}
