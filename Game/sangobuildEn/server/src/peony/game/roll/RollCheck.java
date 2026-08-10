package peony.game.roll;

import peony.game.Server;
import ch.javasoft.util.intcoll.IntHashMap;

public class RollCheck implements Runnable{
	public void run(){
		RollService rollService = Server.server.getServiceRegistry().getRollService();
		IntHashMap<Roll> rolls = new IntHashMap<Roll>(rollService.rolls);
		System.out.println("ROLLSIZE:"+rolls.size());
		for(Roll r:rolls.values()){
			StringBuilder sb = new StringBuilder();
			sb.append("ROLL[").append(r.id).append("]SIZE[").append(r.infos.length).append("]");
			for(RollInfo info:r.infos){
				if(info!=null){
					sb.append("info-");
					sb.append(info.state);
				}
			}
			System.out.println(sb.toString());
		}
	}
}
