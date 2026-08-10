package peony.game.roll;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import peony.game.Time;
import peony.service.Service;

public class RollService implements Service {
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(RollService.class);

	protected int lastUpdateTime = 0;

	protected ConcurrentHashMap<Integer,Roll> rolls = new ConcurrentHashMap<Integer,Roll>();
	
	public List<Integer> pvpRollIds = new ArrayList<Integer>();

	public void addRoll(Roll roll) {
		rolls.put(roll.id, roll);
	}

	public Roll getRoll(int id) {
		return rolls.get(id);
	}

	public void removeRoll(int id) {
		rolls.remove(id);
	}

	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public void startup() throws Exception {
		// TODO Auto-generated method stub

	}

	public void update() {
		if (Time.currTime - lastUpdateTime < 1000) {
			return;
		}
		lastUpdateTime = Time.currTime;
		Iterator<Roll> ite = rolls.values().iterator();
		if (ite.hasNext()) {
			try {
				Roll roll = ite.next();
				if (roll.state != Roll.STATE_END) {
					if ((Time.currTime - roll.startTime) >= Roll.TIMEOUT) {
						roll.timeOut();
					}
				}
				if (roll.state == Roll.STATE_END)
					ite.remove();
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}
}
