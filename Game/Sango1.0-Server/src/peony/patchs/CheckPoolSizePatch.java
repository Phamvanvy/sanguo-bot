package peony.patchs;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.ehcache.Element;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PropertyPool;
import peony.game.Server;
import peony.service.player.PlayerService;

public class CheckPoolSizePatch implements Runnable {

	public void run() {
		try {
			Object[] arr = ObjectAccessor.players.values().toArray();
			Field poolField = PropertyPool.class.getDeclaredField("properties");
			poolField.setAccessible(true);
			HashSet<Integer> passed = new HashSet<Integer>();
			int total = 0;
			int totalPlayers = 0;
			for (Object o : arr) {
				Player p = (Player)o;
				passed.add(p.id);
				ConcurrentHashMap<String, String> pp = (ConcurrentHashMap<String, String>)poolField.get(p.pool);
				total += pp.size();
				totalPlayers++;
			}
			
			PlayerService ps = Server.server.getServiceRegistry().getPlayerService();
			List list = ps.cache.getKeys();
			for (Object k : list) {
				Element elem = ps.cache.get(k);
				Player p = (Player)elem.getObjectValue();
				if (passed.contains(p.id)) {
					continue;
				}
				passed.add(p.id);
				ConcurrentHashMap<String, String> pp = (ConcurrentHashMap<String, String>)poolField.get(p.pool);
				total += pp.size();
				totalPlayers++;
			}
			System.out.println("total player: " + totalPlayers);
			System.out.println("total pool item: " + total);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
