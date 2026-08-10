package peony.game.roll;

import peony.game.Server;
import peony.game.Time;
import ch.javasoft.util.intcoll.IntHashMap;

public class RollCheck3 {
	public void run() {
		RollService rollService = Server.server.getServiceRegistry()
				.getRollService();
		IntHashMap<Roll> rolls = new IntHashMap<Roll>(rollService.rolls);
		System.out
				.println("ROLLSIZE:" + rolls.size() + "TIME:" + Time.currTime);
		for (Roll r : rolls.values()) {
			StringBuilder sb = new StringBuilder();
			sb.append("ROLL[").append(r.id).append("]SIZE[").append(
					r.infos.length)
					.append("]TIME[" + r.startTime + "]TIMEOUT[").append(
							Time.currTime - r.startTime >= Roll.TIMEOUT)
					.append("]STATE["+r.state+"]");
			for (RollInfo info : r.infos) {
				if (info != null) {
					sb.append("info-");
					sb.append(info.state);
				}
			}
			System.out.println(sb.toString());
		}
	}
}
