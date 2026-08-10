package peony.service.levellimit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import peony.game.Server;
import peony.service.Service;

public class LevelLimitService implements Service {
	
	protected Set<Integer> ids = new HashSet<Integer>();

	public void shutdown() {
	}

	public void startup() throws Exception {
		loadIds();
	}

	public void loadIds() throws Exception{
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("Items/IDS.txt");
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
			String s = null;
			while ((s = r.readLine()) != null) {
					int id = Integer.parseInt(s);
					ids.add(id);
			}
			r.close();
		} catch (NumberFormatException e) {
			throw new Exception(e);
		}
	}

	public boolean check(int id) {
		return ids.contains(id);
	}
}
