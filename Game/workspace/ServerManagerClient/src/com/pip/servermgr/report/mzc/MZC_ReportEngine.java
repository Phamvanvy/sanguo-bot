package com.pip.servermgr.report.mzc;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;

import com.pip.mzcity.world.launch.Server;
import com.pip.servermgr.client.Settings;
import com.pip.servermgr.report.AbstractReportEngine;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class MZC_ReportEngine extends AbstractReportEngine {
	// 是否存活
	public static final int TYPE_ISALIVE = 1;
	// 存活时间
	public static final int TYPE_LIVETIME = 2;
	// 等级
	public static final int TYPE_LEVEL = 3;
	// 是否充值
	public static final int TYPE_ISPAY = 4;
	// 充值金额
	public static final int TYPE_PAY = 5;
	// VIP等级
	public static final int TYPE_VIPLEVEL = 6;
	// 金币
	public static final int TYPE_MONEY = 7;
	// 木材
	public static final int TYPE_WOOD = 8;
	// 石头
	public static final int TYPE_STONE = 9;
	// 粮食
	public static final int TYPE_FOOD = 10;
	// 水晶
	public static final int TYPE_CRYSTAL = 11;
	// 人口
	public static final int TYPE_IDLE = 12;
	// 英雄血
	public static final int TYPE_HEROBLOOD = 13;
	// 战功
	public static final int TYPE_EXPLOITS = 14;
	// 金色英雄数量
	public static final int TYPE_GOLDHEROCOUNT = 15;
	// 紫色或金色英雄数量
	public static final int TYPE_PURPLEHEROCOUNT = 16;
	// 紫色或金色或蓝色英雄数量
	public static final int TYPE_BLUEHEROCOUNT = 17;
	// 最高等级5个英雄的平均等级
	public static final int TYPE_AVGHEROLEVEL = 18;
	// 平均建筑等级
	public static final int TYPE_AVGBUILDLEVEL = 19;
	// 平均符文等级
	public static final int TYPE_AVGJEWELLEVEL = 20;
	// 平均强化等级
	public static final int TYPE_AVGENHLEVEL = 21;
	// 平均英雄培养等级
	public static final int TYPE_AVGHEROENHLEVEL = 22;
	// 平均英雄训练属性品质
	public static final int TYPE_AVGHEROTRAINLEVEL = 23;
	// 平均英雄刀盾数
	public static final int TYPE_AVGHEROSTAR = 24;
	// 好友个数
	public static final int TYPE_FRIENDCOUNT = 25;
	// 最高兵种进阶等级
	public static final int TYPE_MAXARMYLEVEL = 26;
	// 最高兵种强化等级
	public static final int TYPE_MAXARMYENHLEVEL = 27;
	
	protected static boolean inited;
	
	/**
	 * 取得最后一个数据类型。
	 * @return
	 */
	public int getMaxType() {
		return TYPE_MAXARMYENHLEVEL;
	}
	
	/**
	 * 取得某个统计项数据类型。
	 * @param type 参见DataFilter里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Class getDataType(int type) { 
		switch (type) {
		case TYPE_ISALIVE:
			return Boolean.class;
		case TYPE_LIVETIME:
			return Integer.class;
		case TYPE_LEVEL:
			return Integer.class;
		case TYPE_ISPAY:
			return Boolean.class;
		case TYPE_PAY:
			return Integer.class;
		case TYPE_VIPLEVEL:
			return Integer.class;
		case TYPE_MONEY:
			return Integer.class;
		case TYPE_WOOD:
			return Integer.class;
		case TYPE_STONE:
			return Integer.class;
		case TYPE_FOOD:
			return Integer.class;
		case TYPE_CRYSTAL:
			return Integer.class;
		case TYPE_IDLE:
			return Integer.class;
		case TYPE_HEROBLOOD:
			return Integer.class;
		case TYPE_EXPLOITS:
			return Integer.class;
		case TYPE_GOLDHEROCOUNT:
			return Integer.class;
		case TYPE_PURPLEHEROCOUNT:
			return Integer.class;
		case TYPE_BLUEHEROCOUNT:
			return Integer.class;
		case TYPE_AVGHEROLEVEL:
			return Float.class;
		case TYPE_AVGBUILDLEVEL:
			return Float.class;
		case TYPE_AVGJEWELLEVEL:
			return Float.class;
		case TYPE_AVGENHLEVEL:
			return Float.class;
		case TYPE_AVGHEROENHLEVEL:
			return Float.class;
		case TYPE_AVGHEROTRAINLEVEL:
			return Float.class;
		case TYPE_AVGHEROSTAR:
			return Float.class;
		case TYPE_FRIENDCOUNT:
			return Integer.class;
		case TYPE_MAXARMYLEVEL:
			return Integer.class;
		case TYPE_MAXARMYENHLEVEL:
			return Integer.class;
		}
		return null;
	}
	
	/**
	 * 取得某个统计项名称。
	 * @param type 参见DataFilter里的常量
	 * @return 
	 */
	public String getTypeName(int type) { 
		switch (type) {
		case TYPE_ISALIVE:
			return "是否存活";
		case TYPE_LIVETIME:
			return "存活天数";
		case TYPE_LEVEL:
			return "等级";
		case TYPE_ISPAY:
			return "是否付费";
		case TYPE_PAY:
			return "付费金额";
		case TYPE_VIPLEVEL:
			return "VIP等级";
		case TYPE_MONEY:
			return "金币";
		case TYPE_WOOD:
			return "木材";
		case TYPE_STONE:
			return "石头";
		case TYPE_FOOD:
			return "粮食";
		case TYPE_CRYSTAL:
			return "水晶";
		case TYPE_IDLE:
			return "人口";
		case TYPE_HEROBLOOD:
			return "英雄血";
		case TYPE_EXPLOITS:
			return "战功";
		case TYPE_GOLDHEROCOUNT:
			return "金色英雄数量";
		case TYPE_PURPLEHEROCOUNT:
			return "紫色英雄数量";
		case TYPE_BLUEHEROCOUNT:
			return "蓝色英雄数量";
		case TYPE_AVGHEROLEVEL:
			return "平均英雄等级";
		case TYPE_AVGBUILDLEVEL:
			return "平均建筑等级";
		case TYPE_AVGJEWELLEVEL:
			return "平均符文等级";
		case TYPE_AVGENHLEVEL:
			return "平均强化等级";
		case TYPE_AVGHEROENHLEVEL:
			return "平均英雄培养等级";
		case TYPE_AVGHEROTRAINLEVEL:
			return "平均英雄训练等级";
		case TYPE_AVGHEROSTAR:
			return "平均英雄刀盾数";
		case TYPE_FRIENDCOUNT:
			return "好友个数";
		case TYPE_MAXARMYLEVEL:
			return "最高士兵等级";
		case TYPE_MAXARMYENHLEVEL:
			return "最高士兵强化等级";
		}
		return null;
	}
	
	/**
	 * 取得某个统计项的描述。
	 * @param type 参见DataFilter里的常量
	 * @return 
	 */
	public String getTypeComments(int type) { 
		switch (type) {
		case TYPE_ISALIVE:
			return "7天内有登陆行为的，算作存活。";
		case TYPE_LIVETIME:
			return "从注册开始，到用户最后一次上线之间的天数。";
		case TYPE_LEVEL:
			return "角色当前等级。";
		case TYPE_ISPAY:
			return "是否充值过。";
		case TYPE_PAY:
			return "累计充值金额（元宝）。";
		case TYPE_VIPLEVEL:
			return "VIP等级";
		case TYPE_MONEY:
			return "角色当前持有金币。";
		case TYPE_WOOD:
			return "角色当前持有木材。";
		case TYPE_STONE:
			return "角色当前持有石头。";
		case TYPE_FOOD:
			return "角色当前持有粮食。";
		case TYPE_CRYSTAL:
			return "角色当前持有水晶。";
		case TYPE_IDLE:
			return "角色当前剩余人口数量。";
		case TYPE_HEROBLOOD:
			return "角色当前持有英雄血。";
		case TYPE_EXPLOITS:
			return "角色当前持有战功。";
		case TYPE_GOLDHEROCOUNT:
			return "角色拥有的金色英雄数量。";
		case TYPE_PURPLEHEROCOUNT:
			return "角色拥有的紫色或金色英雄数量。";
		case TYPE_BLUEHEROCOUNT:
			return "角色拥有的蓝色、紫色或金色英雄数量。";
		case TYPE_AVGHEROLEVEL:
			return "角色最高等级的5个英雄的平均等级。";
		case TYPE_AVGBUILDLEVEL:
			return "角色所有建筑的平均等级。";
		case TYPE_AVGJEWELLEVEL:
			return "角色装备符文等级最高的5个英雄的平均符文等级（符文总等级除以60）。";
		case TYPE_AVGENHLEVEL:
			return "角色强化等级最高的最多10件装备的平均强化等级。";
		case TYPE_AVGHEROENHLEVEL:
			return "角色培养等级最高的5个英雄的5个属性平均培养等级。";
		case TYPE_AVGHEROTRAINLEVEL:
			return "角色训练等级最高的5个英雄的平均训练等级（训练等级=avg(训练属性*100/训练属性最大值)。";
		case TYPE_AVGHEROSTAR:
			return "角色最高等级的5个英雄的平均刀盾数。";
		case TYPE_FRIENDCOUNT:
			return "好友个数。";
		case TYPE_MAXARMYLEVEL:
			return "最高士兵进阶等级。";
		case TYPE_MAXARMYENHLEVEL:
			return "最高士兵强化等级。";
		}
		return null;
	}
	
	/**
	 * 取得数据提取线程的类名。
	 * @return
	 */
	public String getDataFetcherClass() {
		return "com.pip.servermgr.report.mzc.MZC_DataFetcher";
	}
	
	/**
	 * 准备数据。
	 * @return 是否准备成功
	 */
	public boolean init() {
		if (inited) {
			return true;
		}
		
		// 选择数据目录
		DirectoryDialog dlg = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dlg.setMessage("请选择圣域之战数据目录");
		String oldDir = Settings.get("mzc_data_dir");
		if (oldDir != null) {
			dlg.setFilterPath(oldDir);
		}
		String dir = dlg.open();
		if (dir == null) {
			return false;
		} else {
			Settings.set("mzc_data_dir", dir);
			try {
				Server.initForStat(new File(dir));
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "错误", e.toString());
				return false;
			}
		}
		inited = true;
		return true;
	}
	
	/**
	 * 从文件中解析出玩家数据。
	 */
	public List<IPlayer> parseFile(DataInputStream dis) throws Exception {
		List<IPlayer> players = new ArrayList<IPlayer>();
		HashMap<Integer, MZC_Player> playerMap = new HashMap<Integer, MZC_Player>();
		while (true) {
			int type = dis.read();
			if (type == -1 || type == 255) {
				break;
			}
			int len = dis.readInt();
			byte[] data = new byte[len];
			dis.readFully(data);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			ResultRow row = (ResultRow)ois.readObject();
			
			// ResultRow只能是1种情况，player
			if (type == 0) {
				try {
					MZC_Player p = MZC_Player.parse(row);
					players.add(p);
					playerMap.put(p.id, p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return players;
	}
	
	/**
	 * 取得玩家表的名称。
	 */
	public String getPlayerTable() {
		return "player";
	}
}
