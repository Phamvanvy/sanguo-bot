package com.pip.servermgr.report.wulin;

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

import com.pip.servermgr.client.Settings;
import com.pip.servermgr.report.AbstractReportEngine;
import com.pip.servermgr.report.IPlayer;
import com.pip.util.ResultRow;
import com.pip.wulin2.server.world.Server;

public class Wulin_ReportEngine extends AbstractReportEngine {
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
	// 门派
	public static final int TYPE_UNION = 7;
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
	// 坐骑数量
	public static final int TYPE_HORSECOUNT = 28;
	// 是否进行过坐骑强化
	public static final int TYPE_ISHORSEENH = 29;
	// 完成任务个数
	public static final int TYPE_FINISHQUESTCOUNT = 30;
	// 现存任务个数
	public static final int TYPE_QUESTCOUNT = 31;
	// 是否有工会
	public static final int TYPE_HASTONG = 32;
	// 传功等级
	public static final int TYPE_TRANSFERLEVEL = 33;
	//拥有宠物的个数
	public static final int TYPE_HAVE_PET_COUNT = 34;
	//宠物中最高的强化等级
	public static final int TYPE_PET_MIX_MAXLEVEL = 35;
	//宠物最高的强化所有累计经验
	public static final int TYPE_PET_MIX_MAXEXP = 36;
	
	public static final int TYPE_KMONEY = 37;
	
	public static final int TYPE_CREDIT = 38;
	
	public static final int TYPE_ITEMCOUNT = 39;
	
	public static final int TYPE_BABLECREDIT = 40;
	
	// 宝石数最多的宠物的宝石总数
	public static final int TYPE_PETHOLECOUNTMAX = 41;
	
	//玩家所有宝石分类统计数
	public static final int TYPE_ALL_STONE_BY_LEVEL = 42;
	
	
	//消费积分
	public static final int TYPE_USED_IMONEY_POINT = 43;
	//
	//VIP类型
	public static final int TYPE_VIP_TYPE = 44;
	//心法总和
	public static final int TYPE_XINFA_ALL = 45;
	//攻击心法
	public static final int TYPE_XINFA_ATK = 46;
	//防御心法
	public static final int TYPE_XINFA_DEF = 47;
	
	//参加升级返利活动
	public static final int TYPE_SHENGJIFANLI = 48;
	
	
	
	protected static boolean inited;
	
	/**
	 * 取得最后一个数据类型。
	 * @return
	 */
	public int getMaxType() {
		return TYPE_SHENGJIFANLI;
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
		case TYPE_UNION:
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
		case TYPE_TRANSFERLEVEL:
			return Integer.class;
		case TYPE_PET_MIX_MAXLEVEL:
			return Integer.class;
		case TYPE_HAVE_PET_COUNT:
			return Integer.class;
		case TYPE_PET_MIX_MAXEXP:
			return Integer.class;
		case TYPE_KMONEY:
			return Integer.class;
		case TYPE_CREDIT:
			return Integer.class;
		case TYPE_ITEMCOUNT:
			return Integer.class;
		case TYPE_BABLECREDIT:
			return Integer.class;
		case TYPE_PETHOLECOUNTMAX:
			return Integer.class;
		case TYPE_ALL_STONE_BY_LEVEL:
			return Integer.class;
		case TYPE_USED_IMONEY_POINT:
			return Integer.class;
		case TYPE_VIP_TYPE:
			return Integer.class;
		case TYPE_XINFA_ALL:
			return Integer.class;
		case TYPE_XINFA_ATK:
			return Integer.class;
		case TYPE_XINFA_DEF:
			return Integer.class;
		case TYPE_SHENGJIFANLI:
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
		case TYPE_UNION:
			return "门派";
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
		case TYPE_TRANSFERLEVEL:
			return "传功等级";
		case TYPE_PET_MIX_MAXLEVEL:
			return "宠物最高强化等级";
		case TYPE_HAVE_PET_COUNT:
			return "拥有宠物数量";
		case TYPE_PET_MIX_MAXEXP:
			return "宠物最高的强化累计经验";
		case TYPE_KMONEY:
			return "金券";
		case TYPE_CREDIT:
			return "声望";
		case TYPE_ITEMCOUNT:
			return "物品数量";
		case TYPE_BABLECREDIT:
			return "通天阁积分";
		case TYPE_PETHOLECOUNTMAX:
			return "宠物中单只最多宝石";
		case TYPE_ALL_STONE_BY_LEVEL:
			return "玩家所有宝石分类统计数";
		case TYPE_USED_IMONEY_POINT:
			return "玩家的消费积分";
		case TYPE_VIP_TYPE:
			return "玩家的vip类型";
		case TYPE_XINFA_ALL:
			return "玩家的心法总和";
		case TYPE_XINFA_ATK:
			return "玩家的攻击心法总和";
		case TYPE_XINFA_DEF:
			return "玩家的防御心法总和";
		case TYPE_SHENGJIFANLI:
			return "是否参加了升级返利活动";
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
		case TYPE_UNION:
			return "5 - 华山\n6 - 日月\n7 - 逍遥";
		case TYPE_EQULEVEL:
			return "全身装备价值总和（不包括宠物装备）。\n" + 
				"装备价值的计算方法是：物品等级*720*位置系数*品质系数。\n" +
				"普通蓝色装备的品质系数是0.4；\n" +
				"普通紫色装备的品质系数是0.5；\n" +
				"武器的位置系数是0.25；\n" +
				"防具的位置系数总和是0.59（包括头盔、衣服、裤子、鞋子、副手）；\n" +
				"首饰的位置系数总和是0.25（包括护腕、玉佩、护肤、披风）；\n" +
				"坐骑装备的位置系数总和是0.35（包括面具、颈甲、胸甲、臀甲、鞍、蹄掌、脚蹬）；\n" +
				"一套普通南海（头盔、衣服、裤子、鞋子、副手、武器、披风）的总价值是27116。";
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
				"装备价值的计算方法是：物品等级*720*位置系数*品质系数。\n" +
				"普通蓝色装备的品质系数是0.4；\n" +
				"普通紫色装备的品质系数是0.5；\n" +
				"武器的位置系数是0.25；\n" +
				"防具的位置系数总和是0.59（包括头盔、衣服、裤子、鞋子、副手）；\n" +
				"首饰的位置系数总和是0.25（包括护腕、玉佩、护肤、披风）；\n" +
				"坐骑装备的位置系数总和是0.35（包括面具、颈甲、胸甲、臀甲、鞍、蹄掌、脚蹬）；\n" +
				"一套70级打造马装（蓝色6件）的总价值是7344。";
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
		case TYPE_TRANSFERLEVEL:
			return "传功等级";
		case TYPE_HAVE_PET_COUNT:
			return "拥有宠物数量";
		case TYPE_PET_MIX_MAXLEVEL:
			return "宠物最高强化等级";
		case TYPE_PET_MIX_MAXEXP:
			return "宠物最高的强化所有累计经验";
		case TYPE_KMONEY:
			return "玩家的金券数量";
		case TYPE_CREDIT:
			return "玩家的声望";
		case TYPE_ITEMCOUNT:
			return "玩家某件物品的数量";
		case TYPE_BABLECREDIT:
			return "玩家通天阁积分";
		case TYPE_PETHOLECOUNTMAX:
			return "宠物中单只最多宝石";
		case TYPE_ALL_STONE_BY_LEVEL:
			return "玩家所有宝石数分类统计数";
		case TYPE_USED_IMONEY_POINT:
			return "玩家的消费积分";
		case TYPE_VIP_TYPE:
			return "玩家的vip类型";
		case TYPE_XINFA_ALL:
			return "玩家的心法总和";
		case TYPE_XINFA_ATK:
			return "玩家的攻击心法总和";
		case TYPE_XINFA_DEF:
			return "玩家的防御心法总和";
		case TYPE_SHENGJIFANLI:
			return "是否参加了升级返利活动";
		}
		return null;
	}
	
	/**
	 * 取得数据提取线程的类名。
	 * @return
	 */
	public String getDataFetcherClass() {
		return "com.pip.servermgr.report.wulin.Wulin_DataFetcher";
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
		dlg.setMessage("请选择武林OL数据目录");
		String oldDir = Settings.get("wulin_data_dir");
		if (oldDir != null) {
			dlg.setFilterPath(oldDir);
		}
		String dir = dlg.open();
		if (dir == null) {
			return false;
		} else {
			Settings.set("wulin_data_dir", dir);
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
		HashMap<Integer, Wulin_Player> playerMap = new HashMap<Integer, Wulin_Player>();
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
			
			// ResultRow有3种情况，tbl_userdata, tbl_task, tbl_ibuy //2012-02-22 增加 tbl_xinfa
			if (type == 0) {
				Wulin_Player p = Wulin_Player.parse(row);
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
				if (imoney > 0) {
					Wulin_Player p = playerMap.get(playerid);
					if (p.consumeMoney == 0) {
						p.firstItemID = itemID;
					}
					p.consumeMoney += imoney;
				}
			}else if (type == 3) {
				int id = row.getInt(1);
				int hanatk = row.getInt(2);
				int yanatk = row.getInt(3);
				int shaatk = row.getInt(4);
				int handef = row.getInt(5);
				int yandef = row.getInt(6);
				int shadef = row.getInt(7);
				int main = row.getInt(8);
				int last = row.getInt(9);
				int[] xinfa = new int[]{id,hanatk,yanatk,shaatk,handef,yandef,shadef,main,last};
				playerMap.get(id).xinfa = xinfa;
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
