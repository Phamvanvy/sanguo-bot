package peony.patchs;

import java.lang.reflect.Field;
import java.util.List;
import peony.game.Server;
import peony.service.expansionbattle.ExpansionInstance;
import peony.service.expansionbattle.ExpansionService;

public class ExpansionNationPatch implements Runnable {

	public void run() {

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					processStat();
				}
			}
		});

	}

	protected void processStat() {
		ExpansionService service = Server.server.getServiceRegistry().getExpansionService();
		try {
			Field instances = ExpansionService.class.getDeclaredField("instances");
			instances.setAccessible(true);
			List<ExpansionInstance> list = (List<ExpansionInstance>) instances.get(service);
			for (ExpansionInstance in : list) {
				if(in!=null){
					Field stat = ExpansionInstance.class.getDeclaredField("state");
					stat.setAccessible(true);
					int v = stat.getInt(in);
					if (v == ExpansionInstance.STATE_END) {
						if (in.wei.door_buff != 0)
							in.wei.door_buff = 0;
						if (in.shu.door_buff != 0)
							in.shu.door_buff = 0;
						if (in.wu.door_buff != 0)
							in.wu.door_buff = 0;
					}
				}
			}
			System.out.println("load ExpansionNation ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
