package com.pip.servermgr.report.sanguo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;

import peony.game.Server;

import com.pip.servermgr.client.Settings;
import com.pip.servermgr.report.AbstractReportEngine;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class Sanguo_ReportEngine extends AbstractReportEngine {
	// 是否存活
	public static final int TYPE_ISALIVE = 1;
	// 存活时间
	public static final int TYPE_LIVETIME = 2;
	// 等级
	public static final int TYPE_LEVEL = 3;
	// 是否消费
	public static final int TYPE_ISPAY = 4;
	// 消费金额
	public static final int TYPE_PAY = 5;
	// 携带游戏币
	public static final int TYPE_MONEY = 6;
	// 阵营
	public static final int TYPE_FACTION = 7;
	// 装备平均等级
	public static final int TYPE_EQULEVEL = 8;
	// 总宝石等级
	public static final int TYPE_JEWELLEVEL = 9;
	// 宝石个数
	public static final int TYPE_HOLECOUNT = 10;
	// 总星级
	public static final int TYPE_STARLEVEL = 11;
	// 总装备资质
	public static final int TYPE_ZIZHILEVEL = 12;
	// 总装备强化等级
	public static final int TYPE_ENHLEVEL = 13;
	// 技能点消耗比例
	public static final int TYPE_SKILLPOINT = 14;
	// 职业
	public static final int TYPE_CLAZZ = 15;
	// 属性点消耗比例
	public static final int TYPE_ATTRPOINT = 16;
	// 坐骑最高等级
	public static final int TYPE_HORSELVL = 17;
	// 坐骑装备平均等级
	public static final int TYPE_HORSEEQULEVEL = 18;
	// 坐骑宝石总等级
	public static final int TYPE_HORSEJEWELLEVEL = 19;
	// 坐骑宝石总数
	public static final int TYPE_HORSEHOLECOUNT = 20;
	// 坐骑装备总星级
	public static final int TYPE_HORSESTARLEVEL = 21;
	// 坐骑装备总资质
	public static final int TYPE_HORSEZIZHILEVEL = 22;
	// 坐骑装备总强化等级
	public static final int TYPE_HORSEENHLEVEL = 23;
	// 打造等级
	public static final int TYPE_FORMULA = 24;
	// 杀人数
	public static final int TYPE_KILLCOUNT = 25;
	// 被杀数
	public static final int TYPE_DIECOUNT = 26;
	// 胜率
	public static final int TYPE_WINLEVEL = 27;
	// 好友个数
	public static final int TYPE_FRIENDCOUNT = 28;
	// 第一个任务ID
	public static final int TYPE_FIRSTQUEST = 29;
	// 最后一个完成的任务ID
	public static final int TYPE_LASTQUEST = 30;
	
	
	protected static boolean inited;
	
	/**
	 * 取得最后一个数据类型。
	 * @return
	 */
	public int getMaxType() {
		return TYPE_LASTQUEST;
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
		case TYPE_MONEY:
			return Integer.class;
		case TYPE_FACTION:
			return Integer.class;
		case TYPE_EQULEVEL:
			return Integer.class;
		case TYPE_JEWELLEVEL:
			return Integer.class;
		case TYPE_HOLECOUNT:
			return Integer.class;
		case TYPE_STARLEVEL:
			return Integer.class;
		case TYPE_ZIZHILEVEL:
			return Integer.class;
		case TYPE_ENHLEVEL:
			return Integer.class;
		case TYPE_SKILLPOINT:
			return Integer.class;
		case TYPE_CLAZZ:
			return Integer.class;
		case TYPE_ATTRPOINT:
			return Integer.class;
		case TYPE_HORSELVL:
			return Integer.class;
		case TYPE_HORSEEQULEVEL:
			return Integer.class;
		case TYPE_HORSEJEWELLEVEL:
			return Integer.class;
		case TYPE_HORSEHOLECOUNT:
			return Integer.class;
		case TYPE_HORSESTARLEVEL:
			return Integer.class;
		case TYPE_HORSEZIZHILEVEL:
			return Integer.class;
		case TYPE_HORSEENHLEVEL:
			return Integer.class;
		case TYPE_FORMULA:
			return Integer.class;
		case TYPE_KILLCOUNT:
			return Integer.class;
		case TYPE_DIECOUNT:
			return Integer.class;
		case TYPE_WINLEVEL:
			return Integer.class;
		case TYPE_FRIENDCOUNT:
			return Integer.class;
		case TYPE_FIRSTQUEST:
			return Integer.class;
		case TYPE_LASTQUEST:
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
			return "消费金额";
		case TYPE_MONEY:
			return "金钱";
		case TYPE_FACTION:
			return "阵营";
		case TYPE_EQULEVEL:
			return "装备总价值";
		case TYPE_JEWELLEVEL:
			return "宝石总等级";
		case TYPE_HOLECOUNT:
			return "镶嵌宝石数";
		case TYPE_STARLEVEL:
			return "总星级";
		case TYPE_ZIZHILEVEL:
			return "总资质等级";
		case TYPE_ENHLEVEL:
			return "总强化等级";
		case TYPE_SKILLPOINT:
			return "技能点使用率";
		case TYPE_CLAZZ:
			return "职业";
		case TYPE_ATTRPOINT:
			return "属性点使用率";
		case TYPE_HORSELVL:
			return "最高坐骑等级";
		case TYPE_HORSEEQULEVEL:
			return "坐骑装备总价值";
		case TYPE_HORSEJEWELLEVEL:
			return "坐骑宝石总等级";
		case TYPE_HORSEHOLECOUNT:
			return "坐骑镶嵌宝石数";
		case TYPE_HORSESTARLEVEL:
			return "坐骑装备总星级";
		case TYPE_HORSEZIZHILEVEL:
			return "坐骑装备总资质等级";
		case TYPE_HORSEENHLEVEL:
			return "坐骑装备总强化等级";
		case TYPE_FORMULA:
			return "打造等级";
		case TYPE_KILLCOUNT:
			return "杀人数";
		case TYPE_DIECOUNT:
			return "被杀数";
		case TYPE_WINLEVEL:
			return "胜率";
		case TYPE_FRIENDCOUNT:
			return "好友数";
		case TYPE_FIRSTQUEST:
			return "第一个任务ID";
		case TYPE_LASTQUEST:
			return "最后完成的任务ID";
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
			return "是否购买过收费道具（使用元宝卡生成元宝购买的也算）。";
		case TYPE_PAY:
			return "购买收费道具用掉的钱（单位是分）。";
		case TYPE_MONEY:
			return "角色持有游戏币。";
		case TYPE_FACTION:
			return "1 - 魏国\n2 - 蜀国\n3 - 吴国";
		case TYPE_EQULEVEL:
			return "全身装备价值总和（不包括坐骑装备）。\n" + 
				"装备价值的计算方法是：物品等级*720*位置系数*品质系数。\n" +
				"普通蓝色装备的品质系数是0.4；\n" +
				"普通紫色装备的品质系数是0.5；\n" +
				"武器的位置系数是0.25；\n" +
				"防具的位置系数总和是0.59（包括头盔、衣服、裤子、鞋子、副手）；\n" +
				"首饰的位置系数总和是0.25（包括护腕、玉佩、护肤、披风）；\n" +
				"坐骑装备的位置系数总和是0.35（包括面具、颈甲、胸甲、臀甲、鞍、蹄掌、脚蹬）；\n" +
				"一套普通南海（头盔、衣服、裤子、鞋子、副手、武器、披风）的总价值是27116。";
		case TYPE_JEWELLEVEL:
			return "全身装备镶嵌的宝石等级总和（不包括坐骑装备）。";
		case TYPE_HOLECOUNT:
			return "全身装备镶嵌的宝石总数（不包括坐骑装备）。";
		case TYPE_STARLEVEL:
			return "全身装备鉴定的星级总和（不包括坐骑装备）。";
		case TYPE_ZIZHILEVEL:
			return "全身装备鉴定的资质等级总和（不包括坐骑装备）。";
		case TYPE_ENHLEVEL:
			return "全身装备强化等级总和（不包括坐骑装备）。一个装备强化有4项数值，这里取的是它们的总和。";
		case TYPE_SKILLPOINT:
			return "已分配技能点占获得的技能点的百分比。";
		case TYPE_CLAZZ:
			return "0 - 武将\n1 - 刺客\n2 - 谋士\n3 - 方士";
		case TYPE_ATTRPOINT:
			return "已分配属性点占获得属性点的百分比。";
		case TYPE_HORSELVL:
			return "级别最高的坐骑的级别。";
		case TYPE_HORSEEQULEVEL:
			return "装备最好的坐骑的装备价值总和。\n" + 
				"装备价值的计算方法是：物品等级*720*位置系数*品质系数。\n" +
				"普通蓝色装备的品质系数是0.4；\n" +
				"普通紫色装备的品质系数是0.5；\n" +
				"武器的位置系数是0.25；\n" +
				"防具的位置系数总和是0.59（包括头盔、衣服、裤子、鞋子、副手）；\n" +
				"首饰的位置系数总和是0.25（包括护腕、玉佩、护肤、披风）；\n" +
				"坐骑装备的位置系数总和是0.35（包括面具、颈甲、胸甲、臀甲、鞍、蹄掌、脚蹬）；\n" +
				"一套70级打造马装（蓝色6件）的总价值是7344。";
		case TYPE_HORSEJEWELLEVEL:
			return "装备最好的坐骑装备上镶嵌宝石的等级总和。";
		case TYPE_HORSEHOLECOUNT:
			return "装备最好的坐骑装备上镶嵌的宝石总数。";
		case TYPE_HORSESTARLEVEL:
			return "装备最好的坐骑装备鉴定的星级总和。";
		case TYPE_HORSEZIZHILEVEL:
			return "装备最好的坐骑装备鉴定的资质等级总和。";
		case TYPE_HORSEENHLEVEL:
			return "装备最好的坐骑装备强化等级总和。";
		case TYPE_FORMULA:
			return "已学习的打造配方的最高等级。";
		case TYPE_KILLCOUNT:
			return "累计杀人次数。";
		case TYPE_DIECOUNT:
			return "累计被杀次数。";
		case TYPE_WINLEVEL:
			return "累计杀人次数*100/(累计杀人次数+累计被杀次数)。50以上表示杀人比被杀多。";
		case TYPE_FRIENDCOUNT:
			return "好友数量。";
		case TYPE_FIRSTQUEST:
			return "第一个任务ID。";
		case TYPE_LASTQUEST:
			return "最后完成的任务ID。";
		}
		return null;
	}
	
	/**
	 * 取得数据提取线程的类名。
	 * @return
	 */
	public String getDataFetcherClass() {
		return "com.pip.servermgr.report.sanguo.Sanguo_DataFetcher";
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
		dlg.setMessage("请选择明珠三国数据目录");
		String oldDir = Settings.get("sanguo_data_dir");
		if (oldDir != null) {
			dlg.setFilterPath(oldDir);
		}
		String dir = dlg.open();
		if (dir == null) {
			return false;
		} else {
			Settings.set("sanguo_data_dir", dir);
			try {
				new Server(dir);
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
		HashMap<Integer, Sanguo_Player> playerMap = new HashMap<Integer, Sanguo_Player>();
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
			
			// ResultRow有4种情况，player，pvpinfo，ibuy, relation
			if (type == 0) {
				Sanguo_Player p = Sanguo_Player.parse(row);
				players.add(p);
				playerMap.put(p.id, p);
			} else if (type == 1) {
				int id = row.getInt(1);
				int dc = row.getInt(2);
				int kc = row.getInt(3);
				playerMap.get(id).dieCount = dc;
				playerMap.get(id).killCount = kc;
			} else if (type == 2) {
				int id = row.getInt(1);
				int amount;
				if (row.getObject(2) instanceof Long) {
					amount = (int)(((Long)row.getObject(2)).longValue() / 360);
				} else if (row.getObject(2) instanceof byte[]){
					amount = (int)(Long.parseLong(new String((byte[])row.getObject(2))) / 360);
				} else if (row.getObject(2) instanceof String) {
					amount = (int)(Long.parseLong((String)row.getObject(2)) / 360);
				} else {
					throw new IllegalArgumentException(row.getObject(2).getClass().toString());
				}
				playerMap.get(id).consumeMoney = amount;
			} else if (type == 3) {
				int id = row.getInt(1);
				String text = row.getString(2);
				String[] secs = text.split("\n");
				int count = 0;
				for (int i = 0; i < secs.length; i++) {
					if (secs[i].length() > 0) {
						count++;
					}
				}
				playerMap.get(id).friendCount = count;
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
