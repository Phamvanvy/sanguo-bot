package peony.game.instance;

import java.util.List;

import peony.game.GameObjectRef;
import peony.game.Server;
import peony.game.Time;

public class InstanceCheck implements Runnable{
	public void run(){
		NormalVMapManager manager = (NormalVMapManager)Server.server.getWorld().getVMapManager(432);
		List<NormalInstance> l = manager.playerid2instances.get(13008);
		for(NormalInstance instance:l){
			System.out.println("CURRENTTIME:"+Time.currTime);
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE[").append(instance.createTime).append("]LASTLEAVE["+instance.lastLeaveTime).append("]COUNT[").append(instance.playerCount).append("]TIMEOUT["+instance.timeOut+"]");
			int i=0;
			for(GameObjectRef ref:instance.refs){
				sb.append("ref"+(i++)).append(ref.id);
			}
			System.out.println(sb.toString());
		}
	}
}
