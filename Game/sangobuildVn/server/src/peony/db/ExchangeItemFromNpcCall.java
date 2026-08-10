package peony.db;

import java.text.MessageFormat;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.itemenhance.ItemEnhance;
import peony.net.ClientSession;
import peony.net.Packet;

public class ExchangeItemFromNpcCall extends ClientSessionAsyncCall {

	/** 用5星装备兑换南海装备 */
	public static final String CONDITION_5STARS = "5stars";
	/** 扣除6个董卓令 */
	public static final String CONDITION_6ITEM = "6item";
	/** 扣除1个董卓令 */
	public static final String CONDITION_1ITEM = "1item";
	/** 扣除5个珍珠 */
	public static final String CONDITION_5ITEM = "5item";

	/** 条件通过 */
	public static final int ERROR_TYPE_NONE = -1;
	/** 装备不够5星 */
	public static final int ERROR_TYPE_STARS_NOT_ENOUPH = 0;
	/** 装备已经镶嵌宝石 */
	public static final int ERROR_TYPE_EMBEDED_JEWELS = 1;
	/** 此装备不能用来兑换 */
	public static final int ERROR_TYPE_ERROR_EQUIPMENT = 2;
	/** 未找到装备 */
	public static final int ERROR_TYPE_EQUIP_NOTFOUND = 3;
	/** 不是装备装备 */
	public static final int ERROR_TYPE_NOT_AN_EQUIPMENT = 4;
	/** 没有物品 */
	public static final int ERROR_TYPE_6ITEM = 5;
	/** 没有物品 */
	public static final int ERROR_TYPE_1ITEM = 6;
	/** 没有物品 */
	public static final int ERROR_TYPE_5ITEM = 7;

	private int serial;
	private int requestId;
	private int itemId;
	private int instanceId;
	private String condition;
	private Player player;
	private static int MINSTAR = 5;
	/**
	 * 可以换的旧装备
	 */
	private static int[] oldEquips = { 1006747,// 南海镇守使 冠
			1006748,// 南海巡阅使 冠
			1006749,// 南海按察使 冠
			1006750,// 南海安抚使 冠
			1006751,// 南海镇守使 袍
			1006752,// 南海巡阅使 袍
			1006753,// 南海按察使 袍
			1006754,// 南海安抚使 袍
			1006755,// 南海镇守使 护腿
			1006756,// 南海巡阅使 护腿
			1006757,// 南海按察使 护腿
			1006758,// 南海安抚使 护腿
			1006759,// 南海镇守使 靴
			1006760,// 南海巡阅使 靴
			1006761,// 南海按察使 靴
			1006762,// 南海安抚使 靴
			1006763,// 南海镇守使 长杆刀
			1006764,// 南海巡阅使 弓
			1006765,// 南海按察使 扇
			1006766,// 南海按察使 剑
			1007313,// 南海布政使 冠
			1007314,// 南海布政使 袍
			1007315,// 南海布政使 护腿
			1007316,// 南海布政使 扇
			1007319,// 南海布政使 靴
			1006943,// 南海按察使 披风
			1006944,// 南海按察使 盾
			1006945,// 南海镇守使 披风
			1006946,// 南海镇守使 盾
			1006947,// 南海巡阅使 披风
			1006948,// 南海巡阅使 盾
			1006949,// 南海安抚使 披风
			1006950,// 南海安抚使 盾
			1007317,// 南海布政使 披风
			1007318 // 南海布政使 盾
	};

	/**
	 * 得到的新装备
	 */
	private static int[] newEquips = { 1007635,// 精炼 南海镇守使 冠
			1007636,// 精炼 南海巡阅使 冠
			1007637,// 精炼 南海按察使 冠
			1007638,// 精炼 南海安抚使 冠
			1007639,// 精炼 南海镇守使 袍
			1007640,// 精炼 南海巡阅使 袍
			1007641,// 精炼 南海按察使 袍
			1007642,// 精炼 南海安抚使 袍
			1007643,// 精炼 南海镇守使 护腿
			1007644,// 精炼 南海巡阅使 护腿
			1007645,// 精炼 南海按察使 护腿
			1007646,// 精炼 南海安抚使 护腿
			1007647,// 精炼 南海镇守使 靴
			1007648,// 精炼 南海巡阅使 靴
			1007649,// 精炼 南海按察使 靴
			1007650,// 精炼 南海安抚使 靴
			1007651,// 精炼 南海镇守使 长杆刀
			1007652,// 精炼 南海巡阅使 弓
			1007653,// 精炼 南海按察使 扇
			1007670,// 精炼 南海按察使 剑
			1007655,// 精炼 南海布政使 冠
			1007656,// 精炼 南海布政使 袍
			1007657,// 精炼 南海布政使 护腿
			1007658,// 精炼 南海布政使 扇
			1007659,// 精炼 南海布政使 靴
			1007660,// 精炼 南海按察使 披风
			1007661,// 精炼 南海按察使 盾
			1007662,// 精炼 南海镇守使 披风
			1007663,// 精炼 南海镇守使 盾
			1007664,// 精炼 南海巡阅使 披风
			1007665,// 精炼 南海巡阅使 盾
			1007666,// 精炼 南海安抚使 披风
			1007667,// 精炼 南海安抚使 盾
			1007668,// 精炼 南海布政使 披风
			1007669 // 精炼 南海布政使 盾
	};

	protected int[] PREFIXX_XINGCHEN_1_OLD = { 1007671, 1007672, 1007673,
			1007674, 1007675, 1007676, 1007677, 1007678, 1007679, 1007680,
			1007681, 1007682, 1007683, 1007684, 1007685, 1007686, 1007687,
			1007688, 1007689, 1007690, 1007691, 1007692, 1007693, 1007694,
			1007695, 1007696, 1007697, 1007698, 1007699, 1007700, 1007701,
			1007702, 1007703, 1007704, 1007705, 1007706, 1007707, 1007708,
			1007709, 1007710, 1007711, 1007712, 1007713, 1007714, 1007715,
			1007716, 1007717, 1007718, 1007719, 1007720, 1007721, 1007722,
			1007723, 1007724, 1007725, 1007726 };

	protected int[] PREFIXX_XINGCHEN_1_NEW = { 1007727, 1007728, 1007729,
			1007730, 1007731, 1007732, 1007733, 1007734, 1007735, 1007736,
			1007737, 1007738, 1007739, 1007740, 1007741, 1007742, 1007743,
			1007744, 1007745, 1007746, 1007747, 1007748, 1007749, 1007750,
			1007751, 1007752, 1007753, 1007754, 1007755, 1007756, 1007757,
			1007758, 1007759, 1007760, 1007761, 1007762, 1007763, 1007764,
			1007765, 1007767, 1007768, 1007769, 1007770, 1007771, 1007772,
			1007773, 1007774, 1007775, 1007776, 1007777, 1007778, 1007779,
			1007780, 1007781, 1007782, 1007783 };

	protected int[] PREFIXX_XINGCHEN_2_1_OLD = { 1007671, 1007672, 1007673,
			1007674, 1007675, 1007676, 1007677, 1007699, 1007700, 1007701,
			1007702, 1007703, 1007704, 1007705 };

	protected int[] PREFIXX_XINGCHEN_2_2_OLD = { 1007678, 1007679, 1007680,
			1007681, 1007682, 1007683, 1007684, 1007706, 1007707, 1007708,
			1007709, 1007710, 1007711, 1007712 };

	protected int[] PREFIXX_XINGCHEN_2_3_OLD = { 1007685, 1007686, 1007687,
			1007688, 1007689, 1007690, 1007691, 1007713, 1007714, 1007715,
			1007716, 1007717, 1007718, 1007719 };

	protected int[] PREFIXX_XINGCHEN_2_4_OLD = { 1007692, 1007693, 1007694,
			1007695, 1007696, 1007697, 1007698, 1007720, 1007721, 1007722,
			1007723, 1007724, 1007725, 1007726 };

	protected int[] PREFIXX_XINGCHEN_3_OLD = { 1007727, 1007728, 1007729,
			1007730, 1007731, 1007732, 1007733, 1007755, 1007756, 1007757,
			1007758, 1007759, 1007760, 1007761, 1007734, 1007735, 1007736,
			1007737, 1007738, 1007739, 1007740, 1007762, 1007763, 1007764,
			1007765, 1007767, 1007768, 1007769, 1007741, 1007742, 1007743,
			1007744, 1007745, 1007746, 1007747, 1007770, 1007771, 1007772,
			1007773, 1007774, 1007775, 1007776, 1007748, 1007749, 1007750,
			1007751, 1007752, 1007753, 1007754, 1007777, 1007778, 1007779,
			1007780, 1007781, 1007782, 1007783 };

	protected int[] PREFIXX_XINGCHEN_3_NEW = { 1877, 1878, 1879, 1880 };

	public ExchangeItemFromNpcCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player) session.getClient();
		this.serial = packet.getInt();
		this.requestId = packet.getInt();
		this.itemId = packet.getInt();
		this.instanceId = packet.getInt();
		this.condition = packet.getString();
	}

	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.NPC_EXCHANGE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.NPC_EXCHANGE_CLIENT, errorMessage);
		}
	}

	public void run() {
		if (player != null) {
			PlayerTransaction tx = player.newTransaction("NPCEXCHANGE");
			GameItem item = player.bag.removeGameItem(itemId, instanceId, 1,
					tx, true);
			int result = checkCondition(player, item, condition);
			if (result == ERROR_TYPE_NONE) {
				try {
					GameItem reward = ObjectAccessor.createGameItem(requestId);
					player.bag.addGameItemComplete(reward, 1, tx, true);
					tx.commit();
				} catch (Exception e) {
					error("没有要兑换的物品或者背包已满");
					tx.rollback();
				}
			} else {
				switch (result) {
				case ERROR_TYPE_STARS_NOT_ENOUPH:
					error(MessageFormat.format("请选择一件星级大于{0}的南海装备", MINSTAR));
					break;
				case ERROR_TYPE_EMBEDED_JEWELS:
					error(MessageFormat.format("该装备已经镶嵌宝石,请摘除所有宝石后再行兑换",
							MINSTAR));
					break;
				case ERROR_TYPE_EQUIP_NOTFOUND:
					error("无效的装备");
					break;
				case ERROR_TYPE_ERROR_EQUIPMENT:
					error("该装备不能用于兑换");
					break;
				case ERROR_TYPE_NOT_AN_EQUIPMENT:
					error("请选择一件装备");
					break;
				case ERROR_TYPE_6ITEM:
					error("需要6个董卓令");
					break;
				case ERROR_TYPE_1ITEM:
					error("需要1个董卓令");
					break;
				case ERROR_TYPE_5ITEM:
					error("需要5个珍珠");
					break;
				default:
					error("不能兑换");
					break;
				}

				tx.rollback();
			}
		}
		addToClientSession();
	}

	/** 是否匹配 */
	private boolean match(int oldId, int newId, int[] oldE, int[] newE) {
		boolean r = false;
		for (int i = 0; i < oldE.length; i++) {
			if (oldE[i] == oldId) {
				if (newE[i] == newId) {
					r = true;
				}
				break;
			}
		}
		return r;
	}

	protected int checkCondition(Player player, GameItem item, String condition) {
		int ret = ERROR_TYPE_NONE;
		if (item == null) {
			return ERROR_TYPE_EQUIP_NOTFOUND;
		}
		if (!item.template.isEquipment()) {
			return ERROR_TYPE_NOT_AN_EQUIPMENT;
		}
		if(condition.equalsIgnoreCase(CONDITION_5STARS)){
			if(!(isConditionEquip(item.template.id, oldEquips) && isConditionEquip(requestId, newEquips)))
				return ERROR_TYPE_ERROR_EQUIPMENT;
			if (item.object != null && item.object instanceof ItemEnhance) {
				ItemEnhance enhance = (ItemEnhance) item.object;
				if (enhance.getStar() < MINSTAR)
					return ERROR_TYPE_STARS_NOT_ENOUPH;
				if (enhance.getJewelCount() > 0)
					return ERROR_TYPE_EMBEDED_JEWELS;
			} else {
				return ERROR_TYPE_STARS_NOT_ENOUPH;
			}
		}else if(condition.equalsIgnoreCase(CONDITION_6ITEM)){
			if(hasJewels(item))
				return ERROR_TYPE_EMBEDED_JEWELS;
			if(match(item.template.id, requestId, PREFIXX_XINGCHEN_1_OLD, PREFIXX_XINGCHEN_1_NEW)){
				PlayerTransaction tx = player.newTransaction("6ITEM");
				if(player.bag.removeGameItem(1662, -1, 6, tx, true)==null){
					tx.rollback();
					return ERROR_TYPE_6ITEM;
				}
				tx.commit();
			}else{
				return ERROR_TYPE_ERROR_EQUIPMENT;
			}
		}else if(condition.equalsIgnoreCase(CONDITION_1ITEM)){
			if(hasJewels(item))
				return ERROR_TYPE_EMBEDED_JEWELS;
			if((isConditionEquip(item.template.id, PREFIXX_XINGCHEN_2_1_OLD) && isConditionEquip(requestId, PREFIXX_XINGCHEN_2_1_OLD)) || 
					(isConditionEquip(item.template.id, PREFIXX_XINGCHEN_2_2_OLD) && isConditionEquip(requestId, PREFIXX_XINGCHEN_2_2_OLD)) || 
							(isConditionEquip(item.template.id, PREFIXX_XINGCHEN_2_3_OLD) && isConditionEquip(requestId, PREFIXX_XINGCHEN_2_3_OLD)) || 
									(isConditionEquip(item.template.id, PREFIXX_XINGCHEN_2_4_OLD) && isConditionEquip(requestId, PREFIXX_XINGCHEN_2_4_OLD))){
				PlayerTransaction tx = player.newTransaction("1ITEM");
				if(player.bag.removeGameItem(1662, -1, 1, tx, true)==null){
					tx.rollback();
					return ERROR_TYPE_1ITEM;
				}
				tx.commit();
			}else{
				return ERROR_TYPE_ERROR_EQUIPMENT;
			}
		}else if(condition.equalsIgnoreCase(CONDITION_5ITEM)){
			if(hasJewels(item))
				return ERROR_TYPE_EMBEDED_JEWELS;
			if(isConditionEquip(requestId, PREFIXX_XINGCHEN_3_NEW)){
				if(isConditionEquip(item.template.id, PREFIXX_XINGCHEN_3_OLD)){
					PlayerTransaction tx = player.newTransaction("5ITEM");
					if(player.bag.removeGameItem(1311, -1, 5, tx, true)==null){
						tx.rollback();
						return ERROR_TYPE_5ITEM;
					}
					tx.commit();
				}else{
					return ERROR_TYPE_ERROR_EQUIPMENT;
				}
			}else{
				return ERROR_TYPE_ERROR_EQUIPMENT;
			}
		}
		return ret;
	}

	protected boolean isConditionEquip(int id, int[] newE) {
		for(int id1 : newE){
			if(id==id1)
				return true;
		}
		return false;
	}
	
	protected boolean hasJewels(GameItem item){
		if (item.object != null && item.object instanceof ItemEnhance) {
			ItemEnhance enhance = (ItemEnhance) item.object;
			if (enhance.getJewelCount() > 0)
				return true;
		}
		return false;
	}

}
