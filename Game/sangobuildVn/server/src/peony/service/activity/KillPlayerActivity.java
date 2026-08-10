package peony.service.activity;

import peony.game.Server;
import peony.service.player.KillPlayerService;

public class KillPlayerActivity implements IActivityImpl {

	private Activity activity;

	public KillPlayerActivity(Activity owner) {
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	public void startup() throws Exception {
		KillPlayerService service = Server.server.getServiceRegistry()
				.getKillPlayerService();
		String config = activity.getConfigData();
		if (config != null) {
			String[] str = config.split(",");
			for (int i = 0; i < str.length; i++) {
				service.groupId = Integer.parseInt(str[0]);
				service.ratio = Integer.parseInt(str[1]);
			}
		}
	}

	public void shutdown() {
		KillPlayerService service = Server.server.getServiceRegistry()
				.getKillPlayerService();
		service.ratio = 0;
		service.groupId = -1;
	}

	public void clear() {

	}

	public void load() {

	}

	public void save() {

	}
}
