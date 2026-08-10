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
	public static final String PREFIXX_HORSE_1 = "horse_1";
	public static final String PREFIXX_HORSE_2 = "horse_2";
	public static final String PREFIXX_HORSE_3 = "horse_3";
	public static final String PREFIX_JINGLIANXILIANG = "jingliangxiliang";
	public static final String PREFIX_PAOXIAO = "paoxiao";
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
			{ 1877, 1878, 1879, 1880 },
			{ 1007839, 1007840, 1007841, 1007842, 1007843, 1007844, 1007845,
					1007846, 1007847, 1007848, 1007849, 1007850, 1007851,
					1007852, 1007853, 1007854, 1007855, 1007856, 1007857,
					1007858, 1007859, 1007860, 1007861, 1007862, 1007863,
					1007864, 1007865, 1007866 },
			{ 1007811, 1007812, 1007813, 1007814, 1007815, 1007816, 1007817,
					1007818, 1007819, 1007820, 1007821, 1007822, 1007823,
					1007824, 1007825, 1007826, 1007827, 1007828, 1007829,
					1007830, 1007831, 1007832, 1007833, 1007834, 1007835,
					1007836, 1007837, 1007838 }, 
			{2412},
			{
					1007888,//精炼西凉振军使 护腕     
					1007889,//精炼西凉振军使 护符      
					1007890,//精炼西凉振军使 玉佩    
					1007891,//精炼西凉勇骑使 护腕
					1007892,//精炼西凉勇骑使 护符
					1007893,//精炼西凉勇骑使 玉佩
					1007894,//精炼西凉灭阵使 护腕
					1007895,//精炼西凉灭阵使 护符
					1007896,//精炼西凉灭阵使 玉佩
					1007897,//精炼西凉安民使 护腕
					1007898,//精炼西凉安民使 护符
					1007899//精炼西凉安民使 玉佩
			},{
				1007928,//精炼咆哮神威营面具
				1007935,//精炼咆哮神机营面具
				1007942,//精炼咆哮神策营面具
				1007949,//精炼咆哮神佑营面具
				1007929,//精炼咆哮神威营颈甲
				1007936,//精炼咆哮神机营颈甲
				1007943,//精炼咆哮神策营颈甲
				1007950,//精炼咆哮神佑营颈甲
				1007930,//精炼咆哮神威营胸甲
				1007937,//精炼咆哮神机营胸甲
				1007944,//精炼咆哮神策营胸甲
				1007951,//精炼咆哮神佑营胸甲
				1007931,//精炼咆哮神威营臀甲
				1007938,//精炼咆哮神机营臀甲
				1007945,//精炼咆哮神策营臀甲
				1007952,//精炼咆哮神佑营臀甲
				1007932,//精炼咆哮神威营马鞍
				1007939,//精炼咆哮神机营马鞍
				1007946,//精炼咆哮神策营马鞍
				1007953,//精炼咆哮神佑营马鞍
				1007933,//精炼咆哮神威营蹄掌
				1007940,//精炼咆哮神机营蹄掌
				1007947,//精炼咆哮神策营蹄掌
				1007954,//精炼咆哮神佑营蹄掌
				1007934,//精炼咆哮神威营脚蹬
				1007941,//精炼咆哮神机营脚蹬
				1007948,//精炼咆哮神策营脚蹬
				1007955//精炼咆哮神佑营脚蹬
		},
	};

	private static String[] pres = { PREFIXX_NANHAI, PREFIXX_XINGCHEN_1,
			PREFIXX_XINGCHEN_2_1, PREFIXX_XINGCHEN_2_2, PREFIXX_XINGCHEN_2_3,
			PREFIXX_XINGCHEN_2_4, PREFIXX_XINGCHEN_3, PREFIXX_HORSE_1, PREFIXX_HORSE_2, PREFIXX_HORSE_3,
			PREFIX_JINGLIANXILIANG, PREFIX_PAOXIAO};

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
