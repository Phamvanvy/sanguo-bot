package peony.game.touchaction;

import peony.game.Creature;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.gift.GiftService;
import peony.net.Packet;

import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.GiftGroup.GiftDef;

public class GiftGetTouchAction implements TouchAction {

	private static final String GIFT_GET_SCRIPT = "ui_get_gift";

	protected int[] groupIds;

	public GiftGetTouchAction(String[] args) {
		groupIds = new int[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			groupIds[i - 1] = Integer.parseInt(args[i]);
		}
	}

	public void touch(Player player, Creature npc) {
		GiftGroup[] gs = new GiftGroup[groupIds.length];
		GiftService service = Server.server.getServiceRegistry()
				.getGiftService();
		for (int i = 0; i < groupIds.length; i++) {
			gs[i] = service.getGiftGroup(groupIds[i]);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("###");
		boolean hasGroup = false;
		for (int i = 0; i < gs.length; i++) {
			GiftDef[] gds = gs[i].findGifts(player.level);
			if (gds != null && gds.length != 0) {
				hasGroup = true;
				sb.append(gs[i].getID());
				sb.append(",");
				sb
						.append(gs[i].translateText(gs[i].groupMessage, gds[0],
								0, 0));
				sb.append(",");
			}
		}
		if (hasGroup) {
			Packet pt = new Packet(OpCode.OPENUI_SERVER);
			pt.putString(GIFT_GET_SCRIPT);
			pt.putString(sb.substring(0, sb.length()-1));
			player.send(pt);
		}
	}

}
