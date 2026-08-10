package peony.game.admin;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Server;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminSendMail2Call extends ClientSessionAsyncCall {

	int serial;
	int playerId;
	String title,content;
	String itemStringDesc;

	public AdminSendMail2Call(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
		this.title = packet.getString();
		this.content = packet.getString();
		this.itemStringDesc = packet.getString();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		try {
			Actor actor = Server.server.getServiceRegistry()
					.getActorCacheService().find(playerId);
			if (actor != null) {
				EquDesc equDesc = parseEquLog(itemStringDesc);
				ItemTemplate template = ObjectAccessor
						.getItemTemplate(equDesc.itemId);
				if (template != null) {
					GameItem item = ObjectAccessor.createGameItem(template,
							equDesc.instanceId);
					if (equDesc.hole > 0 || equDesc.jewels != null
							|| equDesc.naturals != null || equDesc.star > 0
							|| equDesc.maxHole > 0) {
						ItemEnhance enhance = new ItemEnhance();
						enhance.setAddHole(equDesc.hole);
						enhance.setAddMaxHole(equDesc.maxHole);
						enhance.setStar(equDesc.star);
						if (equDesc.jewels != null) {
							for (int i = 0; i < equDesc.jewels.length; i++) {
								enhance.addJewel(i, equDesc.jewels[i]);
							}
						}
						if (equDesc.naturals != null) {
							NaturalEnhance[] naturals = new NaturalEnhance[equDesc.naturals.length / 2];
							for (int i = 0; i < equDesc.naturals.length; i += 2) {
								naturals[i / 2] = ItemUtil
										.createNaturalEnhance(item,
												equDesc.naturals[i],
												equDesc.naturals[i + 1]);
							}
							enhance.setNaturals(naturals);
						}
						item.object = enhance;
					}
					item.bindInstance = 0;
					Server.server.getServiceRegistry().getMailService()
					.sendSystemMail(playerId, "系統", title, content,
							0, item, 1, "GM");
					Packet pt = new Packet(OpCode.ADMIN_SENDMAIL2_SERVER);
					pt.putInt(serial);
					pt.putString(item.template.name);
					session.send(pt);
				}
			} else {
				ErrorHandler.sendAdminErrorMessage(session, serial,
						OpCode.ADMIN_SENDMAIL2_CLIENT, "裝備Id錯誤");
			}
		} catch (ParseLogException e) {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_SENDMAIL2_CLIENT, e.getMessage());
		}
	}

	// EQU(1006605,9011905,S=6,H=3,JEW=1396+1368+1431+1375,NR=4/31+10/11)
	public static EquDesc parseEquLog(String s) throws ParseLogException {
		if (!s.startsWith("EQU(")) {
			throw new ParseLogException("日志格式錯誤,比如以EQU開頭");
		}
		String tempString = s.substring(4, s.length() - 1);
		String[] ss = tempString.split(",");
		int itemId = Integer.parseInt(ss[0]);
		int instanceId = Integer.parseInt(ss[1]);
		EquDesc desc = new EquDesc();
		desc.itemId = itemId;
		desc.instanceId = instanceId;
		if (ss.length > 2) {
			for (int i = 2; i < ss.length; i++) {
				String se = ss[i];
				if (se.startsWith("S=")) {
					desc.star = Integer.parseInt(se.substring(2));
				} else if (se.startsWith("H=")) {
					desc.hole = Integer.parseInt(se.substring(2));
				} else if (se.startsWith("MH=")) {
					desc.maxHole = Integer.parseInt(se.substring(3));
				} else if (se.startsWith("JEW=")) {
					String[] jewStrings = se.substring(4).split("\\+");
					desc.jewels = new int[jewStrings.length];
					for (int j = 0; j < jewStrings.length; j++) {
						desc.jewels[j] = Integer.parseInt(jewStrings[j]);
					}
				} else if (se.startsWith("NR=")) {
					String[] naturalsString = se.substring(3).split("\\+");
					desc.naturals = new int[naturalsString.length * 2];
					for (int j = 0; j < naturalsString.length; j++) {
						String[] nString = naturalsString[j].split("/");
						desc.naturals[2 * j] = Integer.parseInt(nString[0]);
						desc.naturals[2 * j + 1] = Integer.parseInt(nString[1]);
					}
				}
			}
		}
		return desc;
	}

	public static void main(String[] args) {
		ItemDesc desc = null;
		try {
			desc = parseEquLog("EQU(1006605,9011905,S=6,H=3,JEW=1396+1368+1431+1375,NR=4/31+10/11)");
		} catch (ParseLogException e) {
			e.printStackTrace();
		}
		System.out.println(desc);
	}
}

class ItemDesc {
	public int itemId;
	public int instanceId;
}

class EquDesc extends ItemDesc {
	public int star;
	public int hole;
	public int maxHole;
	public int[] jewels;
	public int[] naturals;

}