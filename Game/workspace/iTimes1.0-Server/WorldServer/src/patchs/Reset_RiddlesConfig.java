package patchs;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.riddles.RiddlesConfig2;

public class Reset_RiddlesConfig implements Runnable{

	/**
	 * 每天检测一次是否需要重置 	第一次启动时会直接重置	每次重置后设置sleep到次日此时再次重置
	 */
	public void run() {
		try {
			new Thread(new Runnable(){
				public void run(){
					long startTime = Utils.getTodayStart();
					long sleepTime = 1 * 60 * 60 * 1000;
					while(true){
						try {
							long now = System.currentTimeMillis();
							if(now >= startTime){
								RiddlesConfig2.resetTime();
								RiddlesConfig2.reload();
								startTime += 24 * 60 * 60 * 1000;
								sleepTime = startTime - now; 
								System.out.println("RiddlesConfig-------reset");
							}
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
