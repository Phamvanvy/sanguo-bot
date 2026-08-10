package com.pip.itimes.server.world.awardbox;

import java.io.File;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.stage.DropGroup;
import com.pip.itimes.server.stage.DropGroups;
import com.pip.itimes.server.stage.DropItem;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.WorldPlayer;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class AwardBoxConfig {
	private static Logger log = Logger.getLogger(AwardBoxConfig.class);

	// 随机数生成器
	private static Random rnd = new Random();

	private static int moneyType;// 游戏币类型，当前为i币（0），将添加J币（1）
	private static int price;// 价格
	private static int dropGroupId;// 价格
	private static int isSystemOpen;
	private static int itemsCount;
	private static int[] elemIndex;
	private static int[] elemBackIndex;

	public static int[] getElemIndex() {
		return elemIndex;
	}

	public static void setElemIndex(int[] elemIndex) {
		AwardBoxConfig.elemIndex = elemIndex;
	}

	public static int[] getElemBackIndex() {
		return elemBackIndex;
	}

	public static void setElemBackIndex(int[] elemBackIndex) {
		AwardBoxConfig.elemBackIndex = elemBackIndex;
	}

	public AwardBoxConfig(File file) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Element root = doc.getRootElement();
		load(root);
		// log.info("花钱开宝箱:loadSuccess moneyType[" + getMoneyType() + "] price["
		// + getPrice() + "] itemsCount[" + getItemsCount() + "]");
		log.info("AwardBox:loadSuccess moneyType[" + getMoneyType()
				+ "] price[" + getPrice() + "] dropgroupid[" + getDropGroupId()
				+ "]itemsCount[" + getItemsCount() + "]");

	}

	/**
	 * 为玩家生成本次的奖项索引
	 * 
	 * @param player
	 * @return
	 */
	public static int getBoxResultIndex(WorldPlayer player) {
		int resultIndex = 0;
		DropGroup group = DropGroups.getDropGroup(dropGroupId,
				player.getLevel());
		if (group != null) {
			if (group.getDropItems().size() != itemsCount) {
				log.info("AwardBox:ID[" + player.getId() + "] DropItems size["
						+ group.getDropItems().size() + "]");
			}

			int randomNum = Utils.getRandom(rnd, 1, 1000);

			DropItem item = group.calcDropItem(randomNum);

			resultIndex = item.getId();
			int itemId = item.getItem().getItemId();

			// 把开奖的itemId保存在otherPool中
			player.setAwardBoxItemId(itemId);
			log.info("AwardBox:ID[" + player.getId() + "] name["
					+ player.getPlayerName() + "] getBoxResultIndex["
					+ resultIndex + "] give player itemId[" + itemId + "]");
		} else {
			log.info("AwardBox:ID[" + player.getId()
					+ "] AwardBoxDropGroup is null. failed!");
		}

		return resultIndex;
	}

	/**
	 * 生成十个的索引数组
	 * 
	 * @param player
	 * @return
	 */
	public static int[] getBoxResultIndexArray(WorldPlayer player) {
		int[] resultIndex = new int[10];
		int[] itemsIndex = new int[10];
		DropGroup group = DropGroups.getDropGroup(dropGroupId,
				player.getLevel());
		String logStr = new String();
		if (group != null) {
			if (group.getDropItems().size() != itemsCount) {
				log.info("AwardBox:ID[" + player.getId() + "] DropItems size["
						+ group.getDropItems().size() + "] in buy ten");
			}
			for (int index = 0; index < 10; index++) {
				int randomNum = Utils.getRandom(rnd, 1, 1000);

				DropItem item = group.calcDropItem(randomNum);

				resultIndex[index] = item.getId();
				itemsIndex[index] = item.getItem().getItemId();
				int itemId = item.getItem().getItemId();
				logStr += "itemId" + index + "[" + itemId + "]";
			}

			// 把开奖的itemId保存在otherPool中
			player.setAwardBoxItemIdArray(itemsIndex);

			//此log不通顺也别改了，因为跟上面有一句重复了，一改分析工具会报错
			log.info("AwardBox:ID[" + player.getId() + "] name["
					+ player.getPlayerName() + "] give player itemIdArray " + logStr);
		} else {
			log.info("AwardBox:ID[" + player.getId()
					+ "] AwardBoxDropGroup in buy ten is null. failed!");
		}

		return resultIndex;
	}

	public static int getBoxItemId(WorldPlayer player) {
		// int resultIndex = 0;
		int itemId = 0;
		DropGroup group = DropGroups.getDropGroup(dropGroupId,
				player.getLevel());
		if (group != null) {
			if (group.getDropItems().size() != itemsCount) {
				log.info("AwardBox:ID[" + player.getId() + "] DropItems size["
						+ group.getDropItems().size() + "] in getBoxItemId()");
			}

			int randomNum = Utils.getRandom(rnd, 1, 1000);

			DropItem item = group.calcDropItem(randomNum);

			// resultIndex = item.getId();
			itemId = item.getItem().getItemId();

		} else {
			log.info("AwardBox:ID[" + player.getId()
					+ "] AwardBoxDropGroup is null. failed! in getBoxItemId()");
		}

		return itemId;
	}

	public static int getMoneyType() {
		return moneyType;
	}

	public void setMoneyType(int moneyType) {
		AwardBoxConfig.moneyType = moneyType;
	}

	public static int getPrice() {
		return price;
	}

	public void setPrice(int moneyCount) {
		AwardBoxConfig.price = moneyCount;
	}

	public static int getIsSystemOpen() {
		return isSystemOpen;
	}

	public void setIsActionOpen(int isActionOpen) {
		AwardBoxConfig.isSystemOpen = isActionOpen;
	}

	public static int getItemsCount() {
		return itemsCount;
	}

	public void setItemsCount(int itemsCount) {
		AwardBoxConfig.itemsCount = itemsCount;
	}

	public static String[] getDescribes(WorldPlayer player) {
		DropGroup group = DropGroups.getDropGroup(dropGroupId,
				player.getLevel());
		String[] describes = new String[itemsCount];
		if (group != null) {
			if (group.getDropItems().size() != itemsCount) {
				log.info("AwardBox:ID[" + player.getId() + "] DropItems size["
						+ group.getDropItems().size() + "]");
			}

			for (int index = 0; index < itemsCount; index++) {

				// describes[index] = group.getDropItems().get(index);
				DropItem dropItem = (DropItem) group.getDropItems().get(index);
				if (dropItem == null) {
					log.info("AwardBox:getDescribes() dropItem index[" + index
							+ "] is null");
					describes[index] = "数据错误";
				} else {
					describes[index] = dropItem.getItem().getName();
				}

			}
		} else {
			for (int index = 0; index < itemsCount; index++) {
				describes[index] = "数据错误";
			}
			log.info("AwardBox:ID[" + player.getId()
					+ "] AwardBoxDropGroup is null. failed!");
		}

		return describes;
	}

	private void load(Element root) {
		// Element box = root.element("Box");
		Element config = root.element("Config");
		setMoneyType(Integer.parseInt(config.attributeValue("moneytype")));
		setPrice(Integer.parseInt(config.attributeValue("price")));
		setDropGroupId(Integer.parseInt(config.attributeValue("dropgroupid")));
		setItemsCount(Integer.parseInt(config.attributeValue("itemcount")));
		setIsActionOpen(Integer.parseInt(config.attributeValue("systemopen")));

		elemIndex = new int[itemsCount];
		elemBackIndex = new int[itemsCount];

		Element elemPic = root.element("Elems");
		int index = 0;
		for (Iterator<Element> elem = elemPic.elementIterator("Elem"); elem
				.hasNext();) {
			Element el = (Element) elem.next();
			elemIndex[index] = translateElemIndex(el.attributeValue("elemindex"));
			elemBackIndex[index] = translateBackIndex(el
					.attributeValue("backindex"));
			index++;
		}
	}

	public static int getDropGroupId() {
		return dropGroupId;
	}

	public static void setDropGroupId(int dropGroupId) {
		AwardBoxConfig.dropGroupId = dropGroupId;
	}

	public static int translateElemIndex(String name) {
		int result = 0;
		if (name.equals("loricae")) {
			result = 0;
		} else if (name.equals("diamond")) {
			result = 1;
		} else if (name.equals("pet")) {
			result = 2;
		} else if (name.equals("smallstar")) {
			result = 3;
		} else if (name.equals("ball")) {
			result = 4;
		} else if (name.equals("bigstar")) {
			result = 5;
		} else if (name.equals("face")) {
			result = 6;
		} else if (name.equals("box")) {
			result = 7;
		}
		return result;
	}
	
	public static int translateBackIndex(String name) {
		int result = 9;
		if (name.equals("blue")) {
			result = 9;
		} else if (name.equals("yellow")) {
			result = 10;
		} else if (name.equals("green")) {
			result = 11;
		} else if (name.equals("red")) {
			result = 12;
		}
		return result;
	}
}
