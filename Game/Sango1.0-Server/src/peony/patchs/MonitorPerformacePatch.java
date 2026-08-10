package peony.patchs;

import peony.util.TimeUtil;

public class MonitorPerformacePatch implements Runnable {
	public void run() {
		TimeUtil.monitorPerformace = !TimeUtil.monitorPerformace;
		if (TimeUtil.monitorPerformace) {
			System.out.println("Performance monitor on");
		} else {
			System.out.println("Performance monitor off");
		}
	} 
}
