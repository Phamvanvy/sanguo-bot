package peony.game.touchaction;

import peony.game.Creature;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.Packet;

public class GuildTouchAction implements TouchAction {
	
	private static final String GUILD_CREATE_SCRIPT = "ui_tong_create";
	
	public void touch(Player player, Creature npc) {
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(GUILD_CREATE_SCRIPT);
		pt.putString(String.format("%d,%d", npc.type,npc.id));
		player.send(pt);
	}

}
