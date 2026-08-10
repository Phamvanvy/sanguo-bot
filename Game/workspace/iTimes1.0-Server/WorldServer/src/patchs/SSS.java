package patchs;

import com.pip.itimes.server.world.sports.SportsTimer;

public class SSS implements Runnable {

	public void run() {
		SportsTimer.cancel();
	}

}
