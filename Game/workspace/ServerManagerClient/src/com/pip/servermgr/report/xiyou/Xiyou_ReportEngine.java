package com.pip.servermgr.report.xiyou;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import optimus.gamemain.XiyouServer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.PlatformUI;

import com.pip.servermgr.client.Settings;
import com.pip.servermgr.report.AbstractReportEngine;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class Xiyou_ReportEngine extends AbstractReportEngine {
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
	// 宠物最高等级
	public static final int TYPE_PETLVL = 16;
	// 宠物最高资质
	public static final int TYPE_PETQUALITY = 17;
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
	// 持有法宝数
	public static final int TYPE_MAGICCOUNT = 27;
	// 法宝等级
	public static final int TYPE_MAGICLEVEL = 28;
	// 完成任务数
	public static final int TYPE_FINISHQUEST = 29;
	// 持有任务数
	public static final int TYPE_CURRENTQUEST = 30;
	// 是否加入洞府
	public static final int TYPE_HASTONG = 31;
	// 荣誉
	public static final int TYPE_HONOR = 32;
	// 声望
	public static final int TYPE_REPUTATION = 33;
	// 绑定元宝剩余
	public static final int TYPE_BOUNDIMONEY = 34;
	// 绑定元宝消费
	public static final int TYPE_USEDBOUNDIMONEY = 35;
	// 是否飞行
	public static final int TYPE_FLY = 36;
	// 装备栏强化等级
	public static final int TYPE_EQUIPBAR = 37;
	// 背包格数
	public static final int TYPE_BAGSIZE = 38;
	//元婴等级
	public static final int TYPE_YUANYING_LEVEL = 39;
	//有尚武之王
	public static final int TYPE_HAS_SW = 40;
	
	protected static boolean inited;
	
	private static int handleCount;
	
	/**
	 * 取得最后一个数据类型。
	 * @return
	 */
	public int getMaxType() {
		return TYPE_HAS_SW;
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
		case TYPE_PETLVL:
			return Integer.class;
		case TYPE_PETQUALITY:
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
		case TYPE_MAGICCOUNT:
			return Integer.class;
		case TYPE_MAGICLEVEL:
			return Integer.class;
		case TYPE_FINISHQUEST:
			return Integer.class;
		case TYPE_CURRENTQUEST:
			return Integer.class;
		case TYPE_HASTONG:
			return Boolean.class;
		case TYPE_HONOR:
			return Integer.class;
		case TYPE_REPUTATION:
			return Integer.class;
		case TYPE_BOUNDIMONEY:
			return Integer.class;
		case TYPE_USEDBOUNDIMONEY:
			return Integer.class;
		case TYPE_FLY:
			return Boolean.class;
		case TYPE_EQUIPBAR:
			return Integer.class;
		case TYPE_BAGSIZE:
			return Integer.class;
		case TYPE_YUANYING_LEVEL:
			return Integer.class;
		case TYPE_HAS_SW:
			return Boolean.class;
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
		case TYPE_PETLVL:
			return "最高宠物等级";
		case TYPE_PETQUALITY:
			return "最高宠物价值";
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
		case TYPE_MAGICCOUNT:
			return "法宝数";
		case TYPE_MAGICLEVEL:
			return "法宝等级";
		case TYPE_FINISHQUEST:
			return "完成任务数";
		case TYPE_CURRENTQUEST:
			return "当前任务数";
		case TYPE_HASTONG:
			return "是否有洞府";
		case TYPE_HONOR:
			return "荣誉";
		case TYPE_REPUTATION:
			return "声望";
		case TYPE_BOUNDIMONEY:
			return "绑定元宝";
		case TYPE_USEDBOUNDIMONEY:
			return "绑定元宝总消费";
		case TYPE_FLY:
			return "是否飞行";
		case TYPE_EQUIPBAR:
			return "装备栏强化总等级";
		case TYPE_BAGSIZE:
			return "背包格数";
		case TYPE_YUANYING_LEVEL:
			return "元婴等级";
		case TYPE_HAS_SW:
			return "是否有尚武之王";
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
			return "1 - 氏人  2 - 幻妖  3 - 玄仙    4 - 灵怪";
		case TYPE_EQULEVEL:
			return "全身装备价值总和（不包括宠物装备）。\n" + 
				"装备价值的计算方法是：物品等级*120*位置系数*品质系数。\n" +
				"普通蓝色装备的品质系数是1.4；\n" +
				"普通紫色装备的品质系数是1.8；\n" +
				"武器的位置系数是0.25；\n" +
				"防具的位置系数总和是0.56（包括头盔、衣服、裤子、鞋子、腰带）；\n" +
				"首饰的位置系数总和是0.19（包括护腕、项链、护符、披风）；\n" +
				"坐骑装备的位置系数总和是0.385（包括头部、盔甲、项圈、护符）；\n" +
				"一点基本属性的价值是20。一套70级打造紫装（10件）的总价值是18357。";
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
			return "1修罗，2灵璧，3魂缚，4食脱，5仙剑，6霸体，7灵羁，8杏林";
		case TYPE_ATTRPOINT:
			return "已分配属性点占获得属性点的百分比。";
		case TYPE_PETLVL:
			return "级别最高的宠物的级别。";
		case TYPE_PETQUALITY:
			return "最有价值的宠物的价值。";
		case TYPE_PETEQULEVEL:
			return "装备最好的坐骑的装备价值总和。\n" + 
			"装备价值的计算方法是：物品等级*120*位置系数*品质系数。\n" +
			"普通蓝色装备的品质系数是1.4；\n" +
			"普通紫色装备的品质系数是1.8；\n" +
			"武器的位置系数是0.25；\n" +
			"防具的位置系数总和是0.56（包括头盔、衣服、裤子、鞋子、腰带）；\n" +
			"首饰的位置系数总和是0.19（包括护腕、项链、护符、披风）；\n" +
			"坐骑装备的位置系数总和是0.385（包括头部、盔甲、项圈、护符）；\n" +
			"一点基本属性的价值是20。一套70级打造宠物紫装（4件）的总价值是6237。";
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
		case TYPE_MAGICCOUNT:
			return "拥有法宝数量。";
		case TYPE_MAGICLEVEL:
			return "最高法宝等级。";
		case TYPE_FINISHQUEST:
			return "累计完成任务数量（循环任务只计算一次）。";
		case TYPE_CURRENTQUEST:
			return "当前任务列表中的任务数量。";
		case TYPE_HASTONG:
			return "是否已加入一个洞府。";
		case TYPE_HONOR:
			return "荣誉。";
		case TYPE_REPUTATION:
			return "声望。";
		case TYPE_BOUNDIMONEY:
			return "绑定元宝余额（单位是分）。";
		case TYPE_USEDBOUNDIMONEY:
			return "绑定元宝总消费金额（单位是分）";
		case TYPE_FLY:
			return "当前是否在飞行中。";
		case TYPE_EQUIPBAR:
			return "所有装备栏强化等级之和。";
		case TYPE_BAGSIZE:
			return "背包总格数。";
		case TYPE_YUANYING_LEVEL:
			return "元婴等级1-9";
		case TYPE_HAS_SW:
			return "是否有尚武之王装备";
		}
		return null;
	}
	
	/**
	 * 取得数据提取线程的类名。
	 * @return
	 */
	public String getDataFetcherClass() {
		return "com.pip.servermgr.report.xiyou.Xiyou_DataFetcher";
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
		dlg.setMessage("请选择明珠西游数据目录");
		String oldDir = Settings.get("xiyou_data_dir");
		if (oldDir != null) {
			dlg.setFilterPath(oldDir);
		}
		String dir = dlg.open();
		if (dir == null) {
			return false;
		} else {
			Settings.set("xiyou_data_dir", dir);
			try {
				new XiyouServer(dir);
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
		handleCount = 0;
		List<IPlayer> players = new ArrayList<IPlayer>();
		HashMap<Integer, Xiyou_Player> playerMap = new HashMap<Integer, Xiyou_Player>();
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
					Xiyou_Player p = Xiyou_Player.parse(row);
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
					Xiyou_Player p = playerMap.get(playerid);
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
			
			handleCount ++;
			System.out.println("处理了" + handleCount + "," + playerMap.size() + ",type=" + type);
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
