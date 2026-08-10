/**
 * 
 */
package canseereaditem;

import java.io.IOException;

/**
 * @author senli
 * 
 */
public class FarmMenu extends MenuType {

	public FarmMenu() {
		type = MENU_FARM;
		titleName = new String[] { "缴获战利品", "损失的道具", "土地升级", "土地开启", "偷取物品" };
	}

	@Override
	public int checkMessage(String s, int k) {
		int result = 0;
		if (k == TYPE_FARM_DROPITEM && s.contains("FarmDropItem")) {
			result = TYPE_FARM_DROPITEM; // 缴获战利品
		} else if (k == TYPE_FARM_LOSTITEM && s.contains("FarmFruit")) {
			result = TYPE_FARM_LOSTITEM;// 损失的道具
		} else if (k == TYPE_FARM_LANDLEVELUP && s.contains("FarmLandLevel")) {
			result = TYPE_FARM_LANDLEVELUP;// 土地升级
		} else if (k == TYPE_FARM_LANDOPEN && s.contains("FarmLandOpen")) {
			result = TYPE_FARM_LANDOPEN;// 土地开启
		} else if (k == TYPE_FARM_STEAL && s.contains("FarmLandSteal")) {
			result = TYPE_FARM_STEAL;// 偷取物品
		}
		return result;
	}

	@Override
	public boolean parseMessage(String s, String tmps, String strID, int index)
			throws IOException {
		if (strID != null) {
			writeText(strID + " ", false);
		}
		switch (index) {
		case TYPE_FARM_DROPITEM:
		case TYPE_FARM_LOSTITEM:
			String itemID = readitem.splitMessageSP(s, "itemID[", defaultEnd);
			String count = readitem.splitMessageSP(s, "count[", defaultEnd);
			if (index == TYPE_FARM_DROPITEM) {
				writeText("缴获战利品  ， 给玩家发送邮件。  ", false);
			} else {
				writeText("损失物品  ， ", false);
			}
			writeText(" 物品ID[" + itemID + "]  物品名称[" + tmps + "]  数量[" + count
					+ "] ", false);
			break;
		case TYPE_FARM_LANDLEVELUP:
			String landLevel = readitem.splitMessageSP(s, "LandLevel[",
					defaultEnd);
			writeText("土地升级成功！   土地等级[" + landLevel + "] 土地索引[" + tmps + "] ",
					false);
			break;
		case TYPE_FARM_LANDOPEN:
			String landCount = readitem.splitMessageSP(s, "LandCount[",
					defaultEnd);
			writeText("土地开启成功！  土地索引[" + tmps + "] 土地数量[" + landCount + "] ",
					false);
			break;
		case TYPE_FARM_STEAL:
			String stealPlayerID = "未记录";
			String stealItemID = "未记录";
			String stealItemName = "未记录";
			if (s.contains("stealPlayerID")) {
				stealPlayerID = readitem.splitMessageSP(s, "stealPlayerID[",
						defaultEnd);
			}
			if (s.contains("itemID[")) {
				stealItemID = readitem.splitMessageSP(s, "itemID[", defaultEnd);
			}
			if (s.contains("itemName[")) {
				stealItemName = readitem.splitMessageSP(s, "itemName[",
						defaultEnd);
			}
			String stealCount = readitem.splitMessageSP(s, "StealCount[",
					defaultEnd);
			String playerID = readitem.splitMessageSP(s, "playerID[",
					defaultEnd);

			writeText("偷取物品  庄园主人ID[" + playerID + "]  偷取物品的玩家ID["
					+ stealPlayerID + "]  偷取的个数[" + stealCount + "]  偷取的物品ID["
					+ stealItemID + "]  偷取的物品名称[" + stealItemName + "]  土地索引["
					+ tmps + "] ", false);
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public String splitMessage(String s, int index, int i) {
		String strStart = "";
		if (index == TYPE_FARM_DROPITEM && i == TYPE_FARM_DROPITEM) {
			strStart = "itemName[";
		} else if (index == TYPE_FARM_LOSTITEM && i == TYPE_FARM_LOSTITEM) {
			strStart = "itemName[";
		} else if (index == TYPE_FARM_LANDLEVELUP && i == TYPE_FARM_LANDLEVELUP) {
			strStart = "LandIndex[";
		} else if (index == TYPE_FARM_LANDOPEN && i == TYPE_FARM_LANDOPEN) {
			strStart = "LandIndex[";
		} else if (index == TYPE_FARM_STEAL && i == TYPE_FARM_STEAL) {
			strStart = "LandIndex[";
		}
		return strStart;
	}

}
