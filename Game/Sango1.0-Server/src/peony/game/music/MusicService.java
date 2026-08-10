package peony.game.music;

import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

import com.pip.sanguo.data.Sound;

public class MusicService implements Service, ServiceEventListener {

	public void shutdown() {

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_MAP_PLAYER_LOADED, // 角色在进入地图以后发送load信息
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_MAP_PLAYER_LOADED:
			playerLoaded((Player) event.param2);
			break;
		}
	}
	
	protected void playerLoaded(Player p) {
		VMap map = p.getVMap();
		if (map != null) {
			int musicId = map.mapDef.mapInfo.backgroundMusic;
			Sound sound = (Sound) Server.server.getServiceRegistry()
					.getDataService().data.findObject(Sound.class, musicId);
			if(sound != null){
				Packet pt = new Packet(OpCode.MUSIC_SERVER);
				pt.putString(sound.source.getName());
				p.send(pt);
			}
		}
	}
}
