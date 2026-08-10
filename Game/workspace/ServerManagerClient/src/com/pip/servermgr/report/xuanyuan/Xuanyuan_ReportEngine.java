package com.pip.servermgr.report.xuanyuan;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;

import shaft.Server;

import com.pip.servermgr.client.Settings;
import com.pip.servermgr.report.AbstractReportEngine;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class Xuanyuan_ReportEngine extends AbstractReportEngine {
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
	// 技能点消耗比例
	public static final int TYPE_SKILLPOINT = 13;
	// 职业
	public static final int TYPE_CLAZZ = 14;
	// 属性点消耗比例
	public static final int TYPE_ATTRPOINT = 15;
	// 宠物数量
	public static final int TYPE_PETCOUNT = 16;
	// 宠物最高等级
	public static final int TYPE_PETLVL = 17;
	// 宠物装备平均等级
	public static final int TYPE_PETEQULEVEL = 18;
	// 宠物宝石总等级
	public static final int TYPE_PETJEWELLEVEL = 19;
	// 宠物宝石总数
	public static final int TYPE_PETHOLECOUNT = 20;
	// 宠物装备总星级
	public static final int TYPE_PETSTARLEVEL = 21;
	// 宠物装备总资质
	public static final int TYPE_PETZIZHILEVEL = 22;
	// 打造等级
	public static final int TYPE_FORMULA = 23;
	// 好友个数
	public static final int TYPE_FRIENDCOUNT = 24;
	// 首次消费道具
	public static final int TYPE_FIRSTBUYITEM = 25;
	// 首次消费级别
	public static final int TYPE_FIRSTBUYLVL = 26;
	// 拥有坐骑数
	public static final int TYPE_HORSECOUNT = 27;
	// 完成任务数
	public static final int TYPE_FINISHQUEST = 28;
	// 持有任务数
	public static final int TYPE_CURRENTQUEST = 29;
	// 是否加入血盟
	public static final int TYPE_HASTONG = 30;
	// 绑定元宝剩余
	public static final int TYPE_BOUNDIMONEY = 31;
	// 绑定元宝消费
	public static final int TYPE_USEDBOUNDIMONEY = 32;
	// 背包格数
	public static final int TYPE_BAGSIZE = 33;
	
	protected static boolean inited;
	
	/**
	 * 取得最后一个数据类型。
	 * @return
	 */
	public int getMaxType() {
		return TYPE_BAGSIZE;
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
		case TYPE_SKILLPOINT:
			return Integer.class;
		case TYPE_CLAZZ:
			return Integer.class;
		case TYPE_ATTRPOINT:
			return Integer.class;
		case TYPE_PETCOUNT:
			return Integer.class;
		case TYPE_PETLVL:
			return Integer.class;
		case TYPE_PETEQULEVEL:
			return Integer.class;
		case TYPE_PETJEWELLEVEL:
			return Integer.class;
		case TYPE_PETHOLECOUNT:
			return Integer.class;
		case TYPE_PETSTARLEVEL:
			return Integer.class;
		case TYPE_PETZIZHILEVEL:
			return Integer.class;
		case TYPE_FORMULA:
			return Integer.class;
		case TYPE_FRIENDCOUNT:
			return Integer.class;
		case TYPE_FIRSTBUYITEM:
			return Integer.class;
		case TYPE_FIRSTBUYLVL:
			return Integer.class;
		case TYPE_HORSECOUNT:
			return Integer.class;
		case TYPE_FINISHQUEST:
			return Integer.class;
		case TYPE_CURRENTQUEST:
			return Integer.class;
		case TYPE_HASTONG:
			return Boolean.class;
		case TYPE_BOUNDIMONEY:
			return Integer.class;
		case TYPE_USEDBOUNDIMONEY:
			return Integer.class;
		case TYPE_BAGSIZE:
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
		case TYPE_SKILLPOINT:
			return "技能点使用率";
		case TYPE_CLAZZ:
			return "职业";
		case TYPE_ATTRPOINT:
			return "属性点使用率";
		case TYPE_PETCOUNT:
			return "宠物数量";
		case TYPE_PETLVL:
			return "最高宠物等级";
		case TYPE_PETEQULEVEL:
			return "宠物装备总价值";
		case TYPE_PETJEWELLEVEL:
			return "宠物宝石总等级";
		case TYPE_PETHOLECOUNT:
			return "宠物镶嵌宝石数";
		case TYPE_PETSTARLEVEL:
			return "宠物装备总星级";
		case TYPE_PETZIZHILEVEL:
			return "宠物装备总资质等级";
		case TYPE_FORMULA:
			return "打造等级";
		case TYPE_FRIENDCOUNT:
			return "好友数";
		case TYPE_FIRSTBUYITEM:
			return "首次购买物品";
		case TYPE_FIRSTBUYLVL:
			return "首次购买等级";
		case TYPE_HORSECOUNT:
			return "坐骑数";
		case TYPE_FINISHQUEST:
			return "完成任务数";
		case TYPE_CURRENTQUEST:
			return "当前任务数";
		case TYPE_HASTONG:
			return "是否有血盟";
		case TYPE_BOUNDIMONEY:
			return "绑定元宝";
		case TYPE_USEDBOUNDIMONEY:
			return "绑定元宝总消费";
		case TYPE_BAGSIZE:
			return "背包格数";
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
			return "-1 无 1 白原 2 青丘 3 赢土";
		case TYPE_EQULEVEL:
			return "全身装备价值总和（不包括宠物装备）。\n" + 
				"装备价值的计算方法是：物品等级*325*位置系数*品质系数。\n" +
				"普通蓝色装备的品质系数是1.0；\n" +
				"普通紫色装备的品质系数是1.5；\n" +
				"武器(主手+副手)的位置系数是0.25；\n" +
				"防具的位置系数总和是0.53（包括头盔、衣服、裤子、鞋子、护臂）；\n" +
				"首饰的位置系数总和是0.28（包括披风、指环、颈环、挂饰、护心镜）；\n" +
				"一点基本属性的价值是65。一套xx级紫装(xx件)的总价值是xxxx。";
		case TYPE_JEWELLEVEL:
			return "全身装备镶嵌的宝石等级总和（不包括宠物装备）。";
		case TYPE_HOLECOUNT:
			return "全身装备镶嵌的宝石总数（不包括宠物装备）。";
		case TYPE_STARLEVEL:
			return "全身装备鉴定的星级总和（不包括宠物装备）。";
		case TYPE_ZIZHILEVEL:
			return "全身装备鉴定的资质等级总和（不包括宠物装备）。";
		case TYPE_SKILLPOINT:
			return "已分配技能点占获得的技能点的百分比。";
		case TYPE_CLAZZ:
			return "";
		case TYPE_ATTRPOINT:
			return "已分配属性点占获得属性点的百分比。";
		case TYPE_PETCOUNT:
			return "拥有宠物数量。";
		case TYPE_PETLVL:
			return "级别最高的宠物的级别。";
		case TYPE_PETEQULEVEL:
			return "装备最好的宠物装备价值总和。\n" + 
			"装备价值的计算方法是：物品等级*325*位置系数*品质系数。\n" +
			"普通蓝色装备的品质系数是1.0；\n" +
			"普通紫色装备的品质系数是1.5；\n" +
			"武器(主手+副手)的位置系数是0.25；\n" +
			"防具的位置系数总和是0.53（包括头盔、衣服、裤子、鞋子、护臂）；\n" +
			"首饰的位置系数总和是0.28（包括披风、指环、颈环、挂饰、护心镜）；\n" +
			"一点基本属性的价值是65。一套xx级紫装(xx件)的总价值是xxxx。";
		case TYPE_PETJEWELLEVEL:
			return "装备最好的宠物装备上镶嵌宝石的等级总和。";
		case TYPE_PETHOLECOUNT:
			return "装备最好的宠物装备上镶嵌的宝石总数。";
		case TYPE_PETSTARLEVEL:
			return "装备最好的宠物装备鉴定的星级总和。";
		case TYPE_PETZIZHILEVEL:
			return "装备最好的宠物装备资质等级总和。";
		case TYPE_FORMULA:
			return "已学习的打造配方的最高等级。";
		case TYPE_FRIENDCOUNT:
			return "好友数量。";
		case TYPE_FIRSTBUYITEM:
			return "首次付费购买的物品ID。";
		case TYPE_FIRSTBUYLVL:
			return "首次付费购买时的角色等级。";
		case TYPE_HORSECOUNT:
			return "拥有坐骑数量。";
		case TYPE_FINISHQUEST:
			return "累计完成任务数量（循环任务只计算一次）。";
		case TYPE_CURRENTQUEST:
			return "当前任务列表中的任务数量。";
		case TYPE_HASTONG:
			return "是否已加入一个血盟。";
		case TYPE_BOUNDIMONEY:
			return "绑定元宝余额（单位是分）。";
		case TYPE_USEDBOUNDIMONEY:
			return "绑定元宝总消费金额（单位是分）";
		case TYPE_BAGSIZE:
			return "背包总格数。";
		}
		return null;
	}
	
	/**
	 * 取得数据提取线程的类名。
	 * @return
	 */
	public String getDataFetcherClass() {
		return "com.pip.servermgr.report.xuanyuan.Xuanyuan_DataFetcher";
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
		dlg.setMessage("请选择明珠轩辕数据目录");
		String oldDir = Settings.get("xuanyuan_data_dir");
		if (oldDir != null) {
			dlg.setFilterPath(oldDir);
		}
		String dir = dlg.open();
		if (dir == null) {
			return false;
		} else {
			Settings.set("xuanyuan_data_dir", dir);
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
		HashMap<Integer, Xuanyuan_Player> playerMap = new HashMap<Integer, Xuanyuan_Player>();
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
			
			// ResultRow有3种情况，player，ibuy, relation
			if (type == 0) {
				try {
					Xuanyuan_Player p = Xuanyuan_Player.parse(row);
					players.add(p);
					playerMap.put(p.id, p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (type == 1) {
				int playerid = row.getInt(1);
				int itemID = row.getInt(2);
				int imoney = row.getInt(3) / 100;
				int boundimoney = row.getInt(4) / 100;
				int level = row.getInt(5);
				if (imoney > 0) {
					Xuanyuan_Player p = playerMap.get(playerid);
					if (p != null) {
						if (p.consumeMoney == 0) {
							p.firstItemID = itemID;
							p.firstLevel = level;
						}
						p.consumeMoney += imoney;
						p.consumeBoundIMoney += boundimoney;
					}
				}
			} else if (type == 2) {
				int id = row.getInt(1);
				String text = row.getString(2);
				String[] secs = text.split("\n");
				int count = 0;
				for (int i = 0; i < secs.length; i++) {
					if (secs[i].length() > 0) {
						count++;
					}
				}
				if (playerMap.containsKey(id)) {
					playerMap.get(id).friendCount = count;
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
