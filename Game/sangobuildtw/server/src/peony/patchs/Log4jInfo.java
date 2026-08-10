package peony.patchs;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log4jInfo implements Runnable {

	public void run() {
		Logger.getRootLogger().setLevel(Level.INFO);
	}

}
