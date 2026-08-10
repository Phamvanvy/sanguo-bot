package peony.game.admin;

import java.text.MessageFormat;
import java.util.*;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.drop.GroupDrop;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 *  掉落组测试接口。
 *  请求： Admin客户端  》 世界服务器
 *  参数：
 *  1：int 序列号
 *  2：int 掉落组id
 *  3：int 虚拟玩家级别
 *  4：int 测试次数
 *  
 *  返回：Admin客户端  《 世界服务器
 *  参数：
 *  1：int 序列号
 *  2：short 掉落格子数量
 *  	 3 掉落物品详情，重复上述次数
 *       3.1 int 物品id
 *       3.2 String 物品名称
 *       3.2 int 数量
 */
public class DropTestCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int dropGroupId;
	protected int playerLevel;
	protected int times;
	
	public static Random rnd = new Random(System.currentTimeMillis());
	public DropTestCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.dropGroupId = pt.getInt();
		this.playerLevel = pt.getInt();
		this.times = pt.getInt();
	}
	HashMap<Integer,GainItem> map = new HashMap<Integer,GainItem> ();
	public void callFinish() throws Exception {
		if (map.size() > Short.MAX_VALUE) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_GM_DROP_TEST, MessageFormat.format("Vật phẩm rơi quá nhiều, vượt quá phạm vi thỏa thuận ：{0}", map.size()));
			return;
		}
		 Packet pt = new Packet(OpCode.ADMIN_GM_DROP_TEST);
		 pt.putInt(serial);
		 pt.putShort(map.size() + qualityCount.length);
		 for (GainItem fall : map.values()) {
			 GameItem item = fall.getItem();
			 pt.putInt(item.template.id);
			 pt.putString(item.template.name);
			 pt.putInt(fall.getCount());
		 }
		 for (int i = 0; i < qualityCount.length; i++) {
			 pt.putInt(i);
			 pt.putString(qualityName[i]);
			 pt.putInt(qualityCount[i]);
		 }
		 session.send(pt);
	}
	public static String qualityName[] = {"Trang bị màu trắng","Trang bị màu xanh","Trang bị màu lam","紫色装备","黄色装备"};
	public int qualityCount[] = new int[qualityName.length];
	public void run() {
		GroupDrop d = ObjectAccessor.getGroupDrop(dropGroupId);
		if (d == null) {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_GM_DROP_TEST, MessageFormat.format("không có tổ bị rớt lại tương ứng:{0}", dropGroupId));
			return;
		}
		Player player = new Player();
		player.setLevel(playerLevel, false);
		Gain gain = new Gain(player); 
		for (int i = times; i-->0; ) {
			d.calc(rnd, gain);
		}
		
		GainItem[] drops = gain.getGainItems();
		for (GainItem fall : drops) {
			GainItem itm = map.get(fall.getItem().template.id);
			if (fall.getItem().template.isEquipment()) {
				qualityCount[fall.getItem().template.quality] += fall.getCount();
			}
			if (itm != null) {
				itm.add(fall.getCount());
			} else {
				map.put(fall.getItem().template.id, fall);
			}
		}
		addToClientSession();
	}

}
