package patchs;

import java.lang.reflect.Field;

import com.pip.itimes.server.world.MailService;

public class CopyOfMailPatch20100205_01 implements Runnable {
	public void run() {
		Thread[] ts = new Thread[256];
		int count = Thread.enumerate(ts);
		for(int i=0;i<count;i++){
			Class cls = Thread.class;
			try {
				Field fld = cls.getDeclaredField("target");
				fld.setAccessible(true);
				Object obj = fld.get(ts[i]);
				if (obj != null && obj instanceof MailService) {
					ts[i].stop();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
