package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.net.ClientSession;
import peony.net.Packet;

public class GetAppointItemDescCall extends ClientSessionAsyncCall {

	public static final String PREFIXX_NANHAI = "nanhai";
	public static final String PREFIXX_XINGCHEN_1 = "xingchen_1";
	public static final String PREFIXX_XINGCHEN_2_1 = "xingchen_2_1";
	public static final String PREFIXX_XINGCHEN_2_2 = "xingchen_2_2";
	public static final String PREFIXX_XINGCHEN_2_3 = "xingchen_2_3";
	public static final String PREFIXX_XINGCHEN_2_4 = "xingchen_2_4";
	public static final String PREFIXX_XINGCHEN_3 = "xingchen_3";
	private int serial;
	private String value;
	private Player player;
	private static int[][] equips = {
			// 南海改
			{ 1007635, 1007636, 1007637, 1007638, 1007639, 1007640, 1007641,
					1007642, 1007643, 1007644, 1007645, 1007646, 1007647,
					1007648, 1007649, 1007650, 1007651, 1007652, 1007653,
					1007670, 1007655, 1007656, 1007657, 1007658, 1007659,
					1007662, 1007663, 1007664, 1007665, 1007660, 1007661,
					1007666, 1007667, 1007668, 1007669 },
			{ 1007727, 1007728, 1007729, 1007730, 1007731, 1007732, 1007733,
					1007734, 1007735, 1007736, 1007737, 1007738, 1007739,
					1007740, 1007741, 1007742, 1007743, 1007744, 1007745,
					1007746, 1007747, 1007748, 1007749, 1007750, 1007751,
					1007752, 1007753, 1007754, 1007755, 1007756, 1007757,
					1007758, 1007759, 1007760, 1007761, 1007762, 1007763,
					1007764, 1007765, 1007767, 1007768, 1007769, 1007770,
					1007771, 1007772, 1007773, 1007774, 1007775, 1007776,
					1007777, 1007778, 1007779, 1007780, 1007781, 1007782,
					1007783 },
			{ 1007671, 1007672, 1007673, 1007674, 1007675, 1007676, 1007677,
					1007699, 1007700, 1007701, 1007702, 1007703, 1007704,
					1007705 },
			{ 1007678, 1007679, 1007680, 1007681, 1007682, 1007683, 1007684,
					1007706, 1007707, 1007708, 1007709, 1007710, 1007711,
					1007712 },
			{ 1007685, 1007686, 1007687, 1007688, 1007689, 1007690, 1007691,
					1007713, 1007714, 1007715, 1007716, 1007717, 1007718,
					1007719 },
			{ 1007692, 1007693, 1007694, 1007695, 1007696, 1007697, 1007698,
					1007720, 1007721, 1007722, 1007723, 1007724, 1007725,
					1007726 }, 
			{ 1877, 1878, 1879, 1880 } 
	};

	private static String[] pres = { PREFIXX_NANHAI, PREFIXX_XINGCHEN_1,
			PREFIXX_XINGCHEN_2_1, PREFIXX_XINGCHEN_2_2, PREFIXX_XINGCHEN_2_3, PREFIXX_XINGCHEN_2_4, PREFIXX_XINGCHEN_3 };

	public GetAppointItemDescCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.value = packet.getString();
		this.player = (Player) session.getClient();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		if (player != null) {
			int index = getIndexByPre(value);
			if (index != -1) {
				int[] eps = equips[index];
				Packet pt = new Packet(OpCode.APPOINITEM_DESC_SERVER);
				pt.putInt(serial);
				pt.putInt(eps.length);
				for (int e : eps) {
					ItemTemplate item = ObjectAccessor.getItemTemplate(e);
					pt.putInt(item.id);
					pt.putInt(item.showType);
					pt.putString(item.name);
					pt.putString(item.desc);
					pt.put(item.quality);
				}
				session.send(pt);
			}
		}
	}

	private int getIndexByPre(String pre) {
		for (int i = 0; i < pres.length; i++) {
			if (pres[i].equals(pre)) {
				return i;
			}
		}
		return -1;
	}

}
