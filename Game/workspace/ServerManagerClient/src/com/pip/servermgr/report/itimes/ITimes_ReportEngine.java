package com.pip.servermgr.report.itimes;

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

import com.pip.itimes.server.world.Server;
import com.pip.servermgr.client.Settings;
import com.pip.servermgr.report.AbstractReportEngine;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;

public class ITimes_ReportEngine extends AbstractReportEngine {
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
	// 装备总价值
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
	// 宠物数量
	public static final int TYPE_PETCOUNT = 17;
	// 宠物装备总价值
	public static final int TYPE_PETEQULEVEL = 18;
	// 宠物宝石总等级
	public static final int TYPE_PETJEWELLEVEL = 19;
	// 宠物宝石总数
	public static final int TYPE_PETHOLECOUNT = 20;
	// 宠物装备总星级
	public static final int TYPE_PETSTARLEVEL = 21;
	// 宠物装备总资质
	public static final int TYPE_PETZIZHILEVEL = 22;
	// 打造技能等级
	public static final int TYPE_MANULEVEL = 23;
	// 好友个数
	public static final int TYPE_FRIENDCOUNT = 24;
	// 持有法宝数量
	public static final int TYPE_FABAOCOUNT = 25;
	// 最高法宝等级
	public static final int TYPE_FABAOLEVEL = 26;
	// 首次消费物品ID
	public static final int TYPE_FIRSTBUYITEM = 27;
	// 首次消费等级
	public static final int TYPE_FIRSTBUYLEVEL = 28;
	// 坐骑数量
	public static final int TYPE_HORSECOUNT = 29;
	// 是否进行过坐骑强化
	public static final int TYPE_ISHORSEENH = 30;
	// 完成任务个数
	public static final int TYPE_FINISHQUESTCOUNT = 31;
	// 现存任务个数
	public static final int TYPE_QUESTCOUNT = 32;
	// 是否有工会
	public static final int TYPE_HASTONG = 33;
	// 房屋等级
	public static final int TYPE_HOUSELEVEL = 34;
	// 形象
	public static final int TYPE_FACE = 35;
	// 杀人数
	public static final int TYPE_KILLS = 36;
	// 偷袭数
	public static final int TYPE_SNEAKS = 37;
	// 竞技场等级
	public static final int TYPE_ARENALEVEL = 38;
	// 竞技场点数
	public static final int TYPE_ARENAPOINT = 39;
	// 竞技场等级2
	public static final int TYPE_ARENALEVEL2 = 40;
	// 竞技场等级3
	public static final int TYPE_ARENALEVEL3 = 41;
	// 阵营杀人数
	public static final int TYPE_CAMPWIN = 42;
	// 阵营被杀数
	public static final int TYPE_CAMPLOST = 43;
	// 阵营声望
	public static final int TYPE_CAMPCREDIT = 44;
	
	protected static boolean inited;
	
	/**
	 * 取得最后一个数据类型。
	 * @return
	 */
	public int getMaxType() {
		return TYPE_CAMPCREDIT;
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
		case TYPE_PETCOUNT:
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
		case TYPE_MANULEVEL:
			return Integer.class;
		case TYPE_FRIENDCOUNT:
			return Integer.class;
		case TYPE_FABAOCOUNT:
			return Integer.class;
		case TYPE_FABAOLEVEL:
			return Integer.class;
		case TYPE_FIRSTBUYITEM:
			return Integer.class;
		case TYPE_FIRSTBUYLEVEL:
			return Integer.class;
		case TYPE_HORSECOUNT:
			return Integer.class;
		case TYPE_ISHORSEENH:
			return Boolean.class;
		case TYPE_FINISHQUESTCOUNT:
			return Integer.class;
		case TYPE_QUESTCOUNT:
			return Integer.class;
		case TYPE_HASTONG:
			return Boolean.class;
		case TYPE_HOUSELEVEL:
			return Integer.class;
		case TYPE_FACE:
			return Integer.class;
		case TYPE_KILLS:
			return Integer.class;
		case TYPE_SNEAKS:
			return Integer.class;
		case TYPE_ARENALEVEL:
			return Integer.class;
		case TYPE_ARENAPOINT:
			return Integer.class;
		case TYPE_ARENALEVEL2:
			return Integer.class;
		case TYPE_ARENALEVEL3:
			return Integer.class;
		case TYPE_CAMPWIN:
			return Integer.class;
		case TYPE_CAMPLOST:
			return Integer.class;
		case TYPE_CAMPCREDIT:
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
		case TYPE_PETLVL:
			return "最高宠物等级";
		case TYPE_PETCOUNT:
			return "宠物数量";
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
		case TYPE_MANULEVEL:
			return "打造等级";
		case TYPE_FRIENDCOUNT:
			return "好友数";
		case TYPE_FABAOCOUNT:
			return "法宝数";
		case TYPE_FABAOLEVEL:
			return "最高法宝等级";
		case TYPE_FIRSTBUYITEM:
			return "首次购买物品";
		case TYPE_FIRSTBUYLEVEL:
			return "首次购买等级";
		case TYPE_HORSECOUNT:
			return "坐骑数";
		case TYPE_ISHORSEENH:
			return "是否强化过坐骑";
		case TYPE_FINISHQUESTCOUNT:
			return "完成任务数";
		case TYPE_QUESTCOUNT:
			return "当前任务数";
		case TYPE_HASTONG:
			return "是否有公会";
		case TYPE_HOUSELEVEL:
			return "房屋等级";
		case TYPE_FACE:
			return "形象";
		case TYPE_KILLS:
			return "杀人数";
		case TYPE_SNEAKS:
			return "偷袭数";
		case TYPE_ARENALEVEL:
			return "竞技场等级";
		case TYPE_ARENAPOINT:
			return "竞技场积分";
		case TYPE_ARENALEVEL2:
			return "竞技场等级2";
		case TYPE_ARENALEVEL3:
			return "竞技场等级3";
		case TYPE_CAMPWIN:
			return "阵营杀人数";
		case TYPE_CAMPLOST:
			return "阵营被杀数";
		case TYPE_CAMPCREDIT:
			return "阵营声望";
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
			return "0 - 光明\n1 - 黑暗";
		case TYPE_EQULEVEL:
			return "全身装备价值总和（不包括宠物装备）。\n" + 
				"TODO 填入幻想装备价值计算方法说明。";
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
			return "0 - 侠客\n1 - 杀手\n2 - 术士";
		case TYPE_ATTRPOINT:
			return "已分配属性点占获得属性点的百分比。";
		case TYPE_PETLVL:
			return "级别最高的宠物的级别。";
		case TYPE_PETCOUNT:
			return "拥有宠物数量。";
		case TYPE_PETEQULEVEL:
			return "装备最好的宠物的装备价值总和。\n" + 
				"TODO 填入幻想装备价值计算方法说明。";
		case TYPE_PETJEWELLEVEL:
			return "装备最好的宠物装备上镶嵌宝石的等级总和。";
		case TYPE_PETHOLECOUNT:
			return "装备最好的宠物装备上镶嵌的宝石总数。";
		case TYPE_PETSTARLEVEL:
			return "装备最好的宠物装备鉴定的星级总和。";
		case TYPE_PETZIZHILEVEL:
			return "装备最好的坐骑装备强化等级总和。";
		case TYPE_MANULEVEL:
			return "打造技能等级。";
		case TYPE_FRIENDCOUNT:
			return "好友数量。";
		case TYPE_FABAOCOUNT:
			return "拥有法宝数量。";
		case TYPE_FABAOLEVEL:
			return "最高法宝等级。";
		case TYPE_FIRSTBUYITEM:
			return "首次付费购买的物品ID。";
		case TYPE_FIRSTBUYLEVEL:
			return "首次付费购买时的等级。";
		case TYPE_HORSECOUNT:
			return "拥有坐骑数量。";
		case TYPE_ISHORSEENH:
			return "是否强化过坐骑。";
		case TYPE_FINISHQUESTCOUNT:
			return "从创建角色开始总共完成的任务数量（循环任务只计算一次）。";
		case TYPE_QUESTCOUNT:
			return "当前任务列表中存在的任务数量。";
		case TYPE_HASTONG:
			return "是否已加入公会。";
		case TYPE_HOUSELEVEL:
			return "房屋等级。";
		case TYPE_FACE:
			return "形象ID。";
		case TYPE_KILLS:
			return "杀人数。";
		case TYPE_SNEAKS:
			return "偷袭数。";
		case TYPE_ARENALEVEL:
			return "竞技场等级。";
		case TYPE_ARENAPOINT:
			return "竞技场积分。";
		case TYPE_ARENALEVEL2:
			return "竞技场等级2。";
		case TYPE_ARENALEVEL3:
			return "竞技场等级3。";
		case TYPE_CAMPWIN:
			return "阵营杀人数。";
		case TYPE_CAMPLOST:
			return "阵营被杀数。";
		case TYPE_CAMPCREDIT:
			return "阵营声望。";
		}
		return null;
	}
	
	/**
	 * 取得数据提取线程的类名。
	 * @return
	 */
	public String getDataFetcherClass() {
		return "com.pip.servermgr.report.itimes.ITimes_DataFetcher";
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
		dlg.setMessage("请选择明珠幻想数据目录");
		String oldDir = Settings.get("itimes_data_dir");
		if (oldDir != null) {
			dlg.setFilterPath(oldDir);
		}
		String dir = dlg.open();
		if (dir == null) {
			return false;
		} else {
			Settings.set("itimes_data_dir", dir);
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
		HashMap<Integer, ITimes_Player> playerMap = new HashMap<Integer, ITimes_Player>();
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
			
			// ResultRow有3种情况，tbl_userdata, tbl_task, tbl_ibuy
			if (type == 0) {
				ITimes_Player p = ITimes_Player.parse(row);
				players.add(p);
				playerMap.put(p.innerPlayer.getId(), p);
			} else if (type == 1) {
				int id = row.getInt(1);
				byte[] current = (byte[])row.getObject(2);
				byte[] finish = (byte[])row.getObject(3);
				playerMap.get(id).currenttask = current;
				playerMap.get(id).finishedtask = finish;
			} else if (type == 2) {
				int playerid = row.getInt(1);
				int itemID = row.getInt(2);
				int payType = row.getInt(3);
				int imoney = row.getInt(4);
				int giftflag = row.getInt(5);
				int level = row.getInt(6);
				if (imoney > 0) {
					ITimes_Player p = playerMap.get(playerid);
					if (p.consumeMoney == 0) {
						p.firstItemID = itemID;
						p.firstLevel = level;
					}
					p.consumeMoney += imoney;
				}
			}
		}
		return players;
	}
	
	/**
	 * 取得玩家表的名称。
	 */
	public String getPlayerTable() {
		return "tbl_userdata";
	}
}
