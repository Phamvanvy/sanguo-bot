package peony.produce;

import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import peony.game.DataService;
import peony.game.EquipmentTemplate;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;

public class ProduceService implements Service{
	private static Logger log = Logger.getLogger(ProduceService.class);
	public static Random random = new Random();
	public ProjectData projectData = Server.server.getServiceRegistry().getDataService().data;
//	public Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
	protected static int[][] enhanceRatio = {{100,40,10,0,0,0,0,0,0,0},{0,100,40,10,0,0,0,0,0,0},
			{0,0,100,40,10,0,0,0,0,0},{0,0,0,100,40,10,0,0,0,0},{0,0,0,0,100,40,10,0,0,0},{0,0,0,0,0,100,40,10,0,0},
			{0,0,0,0,0,0,100,40,10,0},{0,0,0,0,0,0,0,100,40,0},{0,0,0,0,0,0,0,0,100,40},{0,0,0,0,0,0,0,0,0,100}};
	/**
	 * 获取玩家的当前熟练度级别
	 */
	public static int getPracticeLevel(int playerLevel, int practice) {
		if(practice == 0){
			return 1;
		}
		if (playerLevel >= 1 && playerLevel <= 10) {
			if (getLevel(practice) > 2) {
				return 2;
			}
		} else if (playerLevel >= 11 && playerLevel <= 20) {
			if (getLevel(practice) > 4) {
				return 4;
			}
		} else if (playerLevel >= 21 && playerLevel <= 25) {
			if (getLevel(practice) > 6) {
				return 6;
			}
		} else if (playerLevel >= 26 && playerLevel <= 30) {
			if (getLevel(practice) > 7) {
				return 7;
			}
		} else if (playerLevel >= 31 && playerLevel <= 35) {
			if (getLevel(practice) > 8) {
				return 8;
			}
		} else if (playerLevel >= 36 && playerLevel <= 40) {
			if (getLevel(practice) > 9) {
				return 9;
			}
		}
		return getLevel(practice);
	}

	/**
	 * 根据熟练度获取级别
	 */
	public static int getLevel(int level) {
		if (level >= 1 && level <= 40) {
			return 1;
		} else if (level >= 41 && level <= 75) {
			return 2;
		} else if (level >= 76 && level <= 110) {
			return 3;
		} else if (level >= 111 && level <= 140) {
			return 4;
		} else if (level >= 141 && level <= 170) {
			return 5;
		} else if (level >= 171 && level <= 195) {
			return 6;
		} else if (level >= 196 && level <= 220) {
			return 7;
		} else if (level >= 221 && level <= 240) {
			return 8;
		} else if (level >= 241 && level <= 260) {
			return 9;
		} else if (level >= 261 && level <= 275) {
			return 10;
		}
		return 0;
	}

	/**
	 * 根据配方书级别获得最低熟练度点数
	 */
	public static int getPracticeByLevel(int level){
		switch(level){
			case 1:
				return 1;
			case 2:
				return 41;
			case 3:
				return 76;
			case 4:
				return 111;
			case 5:
				return 141;
			case 6:
				return 171;
			case 7:
				return 196;
			case 8:
				return 221;
			case 9:
				return 241;
			case 10:
				return 260;
		}
		return 1;
	}
	
	/**
	 * 根据级别获取对应的熟练度级别名称
	 */
	public static String getLevelToString(int level) {
		if (level == 1) {
			return "Cấp 1";
		} else if (level == 2) {
			return "Cấp 2";
		} else if (level == 3) {
			return "cấp 3";
		} else if (level == 4) {
			return "Cấp 4";
		} else if (level == 5) {
			return "Cấp 5";
		} else if (level == 6) {
			return "Cấp 6";
		} else if (level == 7) {
			return "Cấp 7";
		} else if (level == 8) {
			return "专业";
		} else if (level == 9) {
			return "Tinh thông";
		} else if (level == 10) {
			return "Chuyên gia";
		}
		return "";
	}

	/**
	 * 根据产出最小量和最大量随机产生产出数量
	 */
	public static int getRandomAmount(int minAmount, int maxAmount){
		if(maxAmount > minAmount){
			int randomNum = random.nextInt((maxAmount-minAmount)+1);
			return (randomNum+minAmount);
		}else if(maxAmount == minAmount){
			return minAmount;
		}
		return 0;
	}
	
	/**
	 * 增加熟练度
	 */
//	public static int enhancePractice(int formulaOrItemLevel, int produceOrgatherLevel) {
//		if(formulaOrItemLevel == 0){
//			return 0;
//		}
//		formulaOrItemLevel = formulaOrItemLevel - 1;
//		produceOrgatherLevel = produceOrgatherLevel - 1;
//		int ratio = enhanceRatio[formulaOrItemLevel][produceOrgatherLevel];
//		int num = random.nextInt(100);
//		if(num < ratio){
//			return 1;
//		}
//		return 0;
//	}
	public static int enhancePractice(Player p, int formulaOrItemLevel, int produceOrgatherLevel, int currentPractice, int playerLevel) {
		int maxPractice = getMaxPractice(produceOrgatherLevel);
		if(formulaOrItemLevel == 0){
			return 0;
		}
		formulaOrItemLevel = formulaOrItemLevel - 1;
		produceOrgatherLevel = produceOrgatherLevel - 1;
		int ratio = enhanceRatio[formulaOrItemLevel][produceOrgatherLevel];
		int num = random.nextInt(100);
		if(num < ratio && currentPractice < maxPractice){
			return 1;
		}else if(num < ratio && currentPractice == maxPractice){
			if(getPracticeLevel(playerLevel, (currentPractice+1)) > (produceOrgatherLevel+1)){
				return 1;
			}else{
				Packet pt = new Packet(OpCode.MESSAGE_SERVER);
				pt.putInt(0);
				pt.putString("Đã không thể tăng độ thành Thục, nếu muốn tăng độ thành Thục lên cao, xin chọn tăng cấp độ nhân vật");
				pt.putInt(0);
				pt.putInt(0);
				if(p != null){
					p.send(pt);
				}
			}
		}
		return 0;
	}

	/**
	 * 根据级别获取本级别能够达到的最大熟练度
	 */
	public static int getMaxPractice(int produceOrgatherLevel){
		switch(produceOrgatherLevel){
			case 1:
				return 40;
			case 2:
				return 75;
			case 3:
				return 110;
			case 4:
				return 140;
			case 5:
				return 170;
			case 6:
				return 195;
			case 7:
				return 220;
			case 8:
				return 240;
			case 9:
				return 260;
			case 10:
				return 275;
		}
		return 0;
	}
	
	/**
	 * 配方详细信息
	 */
	public void formulaIndex(Packet packet, ClientSession session) {
		Player p = (Player) session.getClient();
		if(p != null){
			int formulaId = packet.getInt();
			Formula formula = (Formula) projectData.findObject(Formula.class, formulaId);
				//ObjectAccessor.formulas.get(formulaId); 
			if (formula != null) {
				Packet packet1 = new Packet(OpCode.FORMULA_INFO_SERVER);
				packet1.putInt(formulaId);
				packet1.putShort(ProduceService.getPracticeByLevel(formula.level));
				List<BuyRequirement> list = formula.requirements;
				packet1.putShort(list.size());
				for (BuyRequirement buyRequirement : list) {
					packet1.put(buyRequirement.type);
					int amount = buyRequirement.amount;
					if (buyRequirement.type == Shop.TYPE_ITEM) {
						if (buyRequirement.item != null) {
							try {
								packet1.put(GameItem.toClientBytes(ObjectAccessor.getItemTemplate(buyRequirement.item.id)));
							} catch (Exception e) {
								log.error(e, e);
							}
						}
					}
					packet1.putInt(amount);
					packet1.put((buyRequirement.deduct == true ? 1 : 0));
					if(buyRequirement.type == Shop.TYPE_RANK){
						DataService ds = Server.server.getServiceRegistry().getDataService();
						Rank rank = (Rank)ds.data.findDictObject(Rank.class, amount);
						packet1.putString(rank.title);
					}else{
						packet1.putString("");
					}
				}
				packet1.putShort(formula.movePoint);
				packet1.putString(formula.description);
				packet1.put(formula.productType);
				packet1.put(formula.minAmount);
				packet1.put(formula.maxAmount);
				if (ObjectAccessor.getItemTemplate(formula.itemID) != null) {
					packet1.put(GameItem.toClientBytes(ObjectAccessor.getItemTemplate(formula.itemID)));
				}
				p.send(packet1);
			}
		}
	}

	/**
	 * 打造
	 */
	public void produce(int formulaId, Player p, ClientSession session, int serial) {
		LogUtil.logProduceTry(p, formulaId);
		
		if(!p.formulaList.contains(formulaId)){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT, "Không học được cách phối chế chỉ định");
			return;
		}
		Formula formula = (Formula) projectData.findObject(Formula.class, formulaId);
			//ObjectAccessor.formulas.get(formulaId); 
		int practice = p.pool.getInt(Player.PROPERTY_PRODUCE_ABILITY, 1); // 打造技能熟练度
		int level1 = formula.level;// 配方书级别
		List<BuyRequirement> requirements = formula.requirements;// 制造需求
		int movePoint = formula.movePoint; // 消耗行动力
		int productType = formula.productType; // 产出类型
		int groupId = formula.groupID; // 掉落组ID
		int itemId = formula.itemID; // 物品ID
		if (ProduceService.getPracticeLevel(p.level, practice) < level1) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT, "Kỹ năng chế tạo chưa thành thạo");
			return;
		}
		ShopService shopService = Server.server.getServiceRegistry().getShopService();
		try {
			Packet packet1 = new Packet(OpCode.PRODUCE_SERVER);
			packet1.putInt(serial);
			shopService.buy(p, new ProduceIbuy(requirements,p,productType,groupId,session,serial,packet1,itemId,formula,practice,movePoint));
		} catch (ShopException e) {
//			ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT, e.getMessage());
		}
	}

	public void shutdown() {
		
	}

	public void startup() throws Exception {
	}

	/**
	 * 删除配方
	 */
	public static void deleteFormula(Packet packet, ClientSession session) {
		Player p = (Player)session.getClient();
		int serial = packet.getInt();
		int formulaId = packet.getInt();
		if(p != null){
			try{
				p.formulaList.ids.remove(formulaId);
				Packet pt = new Packet(OpCode.FORMULA_DELETE_SERVER);
				p.send(pt);
			}catch(Exception e){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.FORMULA_DELETE_CLIENT, "Xóa không thành công ");
			}
		}
	}

	/**
	 * 获取不能学习配方书的原因
	 */
	public String getLevelCause(int playerLevel, int producePractice, int formulaLevel) {
		int practiceLevel = getPracticeLevel(playerLevel, producePractice);
		if(practiceLevel < formulaLevel){
			if(getLevel(producePractice) < formulaLevel && getMaxLevelByPlayerLevel(playerLevel) >= formulaLevel){
				return "Độ chế tạo thông thạo";
			}else if(getLevel(producePractice) >= formulaLevel && getMaxLevelByPlayerLevel(playerLevel) < formulaLevel){
				return "Nhân vật đẳng cấp";
			}else if(getLevel(producePractice) < formulaLevel && getMaxLevelByPlayerLevel(playerLevel) < formulaLevel){
				return "Đẳng cấp nhân vật và độ chế tạo thành thạo";
			}
		}
		return "Cấp bậc chế tạo chức năng";
	}

	/**
	 * 根据人物级别获取他所能达到的最高熟练度级别
	 */
	private int getMaxLevelByPlayerLevel(int playerLevel) {
		if (playerLevel >= 1 && playerLevel <= 10) {
			return 2;
		} else if (playerLevel >= 11 && playerLevel <= 20) {
			return 4;
		} else if (playerLevel >= 21 && playerLevel <= 25) {
			return 6;
		} else if (playerLevel >= 26 && playerLevel <= 30) {
			return 7;
		} else if (playerLevel >= 31 && playerLevel <= 35) {
			return 8;
		} else if (playerLevel >= 36 && playerLevel <= 40) {
			return 9;
		}
		return 10;
	}
	
	public int getTypeOfFormula(int itemId){
		GameItem item = ObjectAccessor.createGameItem(itemId);
		if(item!=null){
			if(item.template.isEquipment()){
				if(item.template.equipment.type == EquipmentTemplate.TYPE_WEAPON){
					return 0;
				} else if(item.template.equipment.type == EquipmentTemplate.TYPE_ARMOR){
					return 1;
				} else if(item.template.equipment.type == EquipmentTemplate.TYPE_TRINKET){
					return 2;
				}
			} else if(item.template.itemType == Item.TYPE_RENEW){
				return 3;
			} else {
				return 4;
			}
		}
		return -1;
	}
	
	
	public void binaryAdd(List<Formula> list,Formula fml){
		int start = -1;
		int end = list.size();
		int mid = end;
		while(end - start > 1){
			mid = (start + end) / 2;
			Formula g = list.get(mid);
			if(fml.level > g.level){
				end = mid;
			} else if(fml.level < g.level){
				start = mid;
			} else {
				int quality1 = ObjectAccessor.getItemTemplate(fml.itemID).quality;
				int quality2 = ObjectAccessor.getItemTemplate(g.itemID).quality;
				if(quality1 > quality2){
					end = mid;
				} else {
					start = mid;
				} 
			}
		}
		list.add(end, fml);
	}
}
