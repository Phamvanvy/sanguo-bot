package peony.service.enhance;

import java.util.Random;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Time;
import peony.game.itemenhance.ItemEnhance;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;

public class EnhanceService implements Service {

	public static final String PROPERTY_ENHANCE_TIMES = "enhancetiems";

	public static final String PROPERTY_ENHANCE_DATE = "enhancedate";

	protected static final int[] ENHANCE_MAX = { 150, 100, 150, 60 };
	
	protected static final int MONEY = 200; 

	protected static final String[] enhanceType = { "base", "natural", "star",
			"jewel" };

	protected static final String[] enhanceName = { "\n基础强化:\nCường hóa cơ sở:", "Cường hóa cấp sao:", "Cường hóa tư chất: ",
			"Cường hóa bảo thạch: " };

	protected Random rnd = new Random();

	public void shutdown() {

	}

	public void startup() throws Exception {

	}

	public void processEnhance(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if (p != null) {
			int serial = packet.getInt();
			int itemId = packet.getInt();
			int instanceId = packet.getInt();
			Object[] os = ItemUtil.findPlayerEquipment(p, itemId, instanceId);
			if (os != null) {
				GameItem gameItem = (GameItem) os[0];
				if (gameItem == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, "Không tìm thấy vật phẩm");
					return;
				}
				if (!gameItem.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, "Không phải trang bị");
					return;
				}
				int enhanceTimes = getEnhanceTimes(p);
				PlayerTransaction tx = p.newTransaction("ENHANCEEQU");
				try {
//					int money = (int) (100 * Math.pow(2, enhanceTimes + 1));
					p.decMoney(MONEY, tx, true);
					tx.commit();
					ItemEnhance ie = (ItemEnhance) gameItem.object;
					if (ie == null) {
						ie = new ItemEnhance();
						gameItem.object = ie;
					}
					int[] oldArr = ie.getEnhanceData(); // 已有的强化效果
					int[] newArr = enhanceData(gameItem); // 新的强化效果
					// 强化成功后当天次数增加
					p.pool.setInt(PROPERTY_ENHANCE_TIMES, enhanceTimes + 1);
					if (enhanceTimes == 0) {
						// 记录最后一次强化日期
						p.pool.setInt(PROPERTY_ENHANCE_DATE, Time.day);
					}
					Packet pt = new Packet(OpCode.ENHANCE_EQUIP_SERVER);
					pt.putInt(serial);
					pt.putInt(ENHANCE_MAX.length);
					for (int i = 0; i < ENHANCE_MAX.length; i++) {
						String color = getColor(newArr[i],ENHANCE_MAX[i]);
						pt.putUTF(enhanceName[i] + color +String.valueOf(newArr[i])+"</c>" + "Giai đoạn");
						pt.put(getType(newArr[i], oldArr[i]));
					}
					ie.setEnhanceData(newArr);
					Object owner = os[1];
					if (owner instanceof Player) {
						p.refreshProperties(false);
					} else if (owner instanceof Horse) {
						Horse h = (Horse) owner;
						h.refreshProperties(false, p);
						if (h == p.horse) {
							p.refreshProperties(false);
						}
					}
					if(gameItem.bindInstance==-1){
					    gameItem.bindInstance = 0;
					}
					pt.put(gameItem.toClientBytes());
					p.send(pt);
				} catch (NoEnoughValueException ex) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, "Số dư vàng của bạn không đủ, không thể cường hóa trang bị");
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ENHANCE_EQUIP_CLIENT, "没找到指定装备");
			}
		}
	}
	
	/**
	 * 确定颜色
	 */
	public String getColor(int num,int maxNum){
		int percent = Math.round((num*100)/maxNum);
		String retColor = "<c0>";
		if(percent >= 90)
			//橙色
			retColor = "<cFF7F00>";
		else if(percent >= 80)
			//紫色
			retColor = "<cCD00CD>";
		else if(percent >= 70)
			//蓝色
			retColor = "<c0000FF>";
		else if(percent >= 60)
			//绿色
			retColor = "<c006633>";
        return retColor;
	}

	/**
	 * 强化数据大随机限定
	 * @param gameItem
	 * @return
	 */
	public int[] enhanceData(GameItem gameItem) {
		ItemEnhance ie = (ItemEnhance) gameItem.object;
		int[] ret = new int[4];
		for (int i = 0; i < ENHANCE_MAX.length; i++) {
			ret[i] = rnd.nextInt(ENHANCE_MAX[i] - 1) + 1;
		}
		if (ret[0] >= 90 && ret[0] < 105) {
			ret[2] = rnd.nextInt(ENHANCE_MAX[2] - 50) + 50;
		} else if (ret[0] >= 105 && ret[0] < 120) {
			if (ie != null && ie.getStar() > 0) {
				ret[2] = rnd.nextInt(ENHANCE_MAX[2] - 50) + 50;
			} else {
				ret[1] = rnd.nextInt(ENHANCE_MAX[1] - 30) + 30;
			}
		} else if (ret[0] >= 120 && ret[0] < 135) {
			if (ie != null && ie.getJewelCount() > 0) {
				if (ie != null && ie.getStar() > 0) {
					ret[2] = rnd.nextInt(ENHANCE_MAX[2] - 50) + 50;
				} else {
					ret[1] = rnd.nextInt(ENHANCE_MAX[1] - 30) + 30;
				}
			} else {
				ret[3] = rnd.nextInt(ENHANCE_MAX[3] - 10) + 10;
			}
		}
		return ret;
	}

	public byte getType(int da, int lastDa) {
		if (da > lastDa) {
			return 2;
		} else if (da < lastDa) {
			return 1;
		} else {
			return 0;
		}
	}

	public int getEnhanceTimes(Player p) {
		int enhanceDate = p.pool.getInt(PROPERTY_ENHANCE_DATE, 0);
		if (enhanceDate == Time.day) {
			int enhanceTimes = p.pool.getInt(PROPERTY_ENHANCE_TIMES, 0);
			return enhanceTimes;
		}
		return 0;
	}
}
