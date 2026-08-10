package peony.patchs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import peony.game.Server;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.asyncbattle.AsyncNormalBoard;

public class ABoardPatch implements Runnable {
	public void run() {
		AsyncBattleService service = Server.server.getServiceRegistry()
				.getAsyncBattleService();
		synchronized (service) {
			List<Map.Entry<Integer, AsyncNormalBoard>> infoIds = new ArrayList<Map.Entry<Integer, AsyncNormalBoard>>(
					service.getId2boards().entrySet());
			// ≈≈–Ú
			Collections.sort(infoIds,
					new Comparator<Map.Entry<Integer, AsyncNormalBoard>>() {
						public int compare(
								Map.Entry<Integer, AsyncNormalBoard> o1,
								Map.Entry<Integer, AsyncNormalBoard> o2) {
							return (o1.getValue().rank - o2.getValue().rank);
						}
					});
			for (int i = 0; i < infoIds.size(); i++) {
				Entry<Integer, AsyncNormalBoard> ent = infoIds.get(i);
				ent.getValue().rank = i + 1;
				System.out.println(ent.getKey() + "=" + ent.getValue().rank);
			}
			service.getRank2boards().clear();
			for (AsyncNormalBoard board : service.getId2boards().values()) {
				service.getRank2boards().put(board.rank, board);
			}
			for (AsyncNormalBoard board : service.getRank2boards().values()) {
				System.out.println("-------------:" + board.name + " "
						+ board.rank);
			}
			System.out.println("-------------------------ABoardOK");
		}
	}
}
