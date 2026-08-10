package patchs;

public class StopThreadPatch implements Runnable {

	public void run() {
		Thread[] ts = new Thread[256];
		int count = Thread.enumerate(ts);
		for(int i=0;i<count;i++){
			if(ts[i].getName().equals("Sports")){
				ts[i].stop();
				System.out.println("sports stop");
			}
		}
	}

}
