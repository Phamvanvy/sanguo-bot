package patchs;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;

public class ClearBattlePatch implements Runnable {

	public void run() {
		try {
			Field field1 = BattleService2.class.getDeclaredField("playerid2battles");
			Field field2 = BattleService2.class.getDeclaredField("id2battles");
			field1.setAccessible(true);
			field2.setAccessible(true);
			final Map<Integer,Battle2> playerid2battles = (Map<Integer,Battle2>)field1.get(Server.instance.battleService);
			final ConcurrentHashMap<Integer,Battle2> id2battles = (ConcurrentHashMap<Integer,Battle2>)field2.get(Server.instance.battleService);
			new Thread(new Runnable(){
				public void run(){
					while(true){
						Iterator<Battle2> ite = playerid2battles.values().iterator();
						while(ite.hasNext()){
							Battle2 battle = ite.next();
							if(!id2battles.containsKey(battle.getId())){
								ite.remove();
							}
						}
						try {
							Thread.sleep(5 * 60 * 1000);
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
