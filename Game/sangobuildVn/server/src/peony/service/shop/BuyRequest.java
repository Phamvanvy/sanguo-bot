package peony.service.shop;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import peony.game.DataService;
import peony.game.GameObjectRef;
import peony.game.ItemTemplate;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.service.account.Account;
import peony.service.account.AccountService;

import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;

/**
 * 购买商店中物品的请求。
 */
public class BuyRequest {
	/*
	 * 服务对象
	 */
	ShopService shopService;
	/*
	 * 购买ID
	 */
	int buyID;
	/*
	 * 创建时间
	 */
	int createTime;
	/*
	 * 购买玩家
	 */
	GameObjectRef player;
	/*
	 * 购买目标
	 */
	IBuyObject buyObject;
	/*
	 * 本次购买需要扣除但还没有扣除的东西
	 */
	List<Shop.BuyRequirement> pendingReqs;
	/*
	 * 本次购买已经扣除成功的东西
	 */
	List<Shop.BuyRequirement> succReqs;
	/*
	 * 玩家操作事务
	 */
	PlayerTransaction tx = null;
	
	/**
	 * 创建一个购买请求。
	 * @param service 商店服务对象
	 * @param id 请求ID
	 * @param player 购买玩家
	 * @param buyObject 购买的项目（可能是物品或其他东西）
	 * @throws ShopException 如果参数错误，抛出异常
	 */
	public BuyRequest(ShopService service, int id, Player player, IBuyObject buyObject) throws ShopException {
		this.shopService = service;
		buyID = id;
		createTime = Time.currTime;
		this.player = player.ref();
		this.tx = player.newTransaction(buyObject.getCause());
		this.buyObject = buyObject;
		this.pendingReqs = new ArrayList<Shop.BuyRequirement>();
		this.pendingReqs.addAll(buyObject.getRequirements());
	}
	
	/**
	 * 锁定商店物品数量，把本次购买的数量从剩余数量中扣除。
	 * @throws ShopException 如果剩余数量不足，或者已经超过购买限制或者不满足一些基
	 * 本购买条件，抛出异常
	 */
	public void lock() throws ShopException {
		Player p = (Player)ObjectAccessor.getGameObject(player);
		if (p == null) {
			throw new ShopException("Trạng thái nhân vật di thường");
		}
		
		// 检查基本购买条件（不需要扣除物品的条件）
		Iterator<Shop.BuyRequirement> reqItor = pendingReqs.iterator();
		while (reqItor.hasNext()) {
			Shop.BuyRequirement req = reqItor.next();
			if (req.deduct) {
				continue;
			}
			switch (req.type) {
			case Shop.TYPE_HONOR:
				if (p.getCredit() < req.amount) {
					throw new ShopException(MessageFormat.format("Cần phải có {0} chiến công", req.amount));
				}
				break;
			case Shop.TYPE_IMONEY:
			{
				AccountService as = Server.server.getServiceRegistry().getAccountService();
				Account account = as.getAccount(p.accountId);
				if (account == null || account.getIMoney() / 100 < req.amount) {
					throw new ShopException(MessageFormat.format("{0}{1} Yêu cầu trong tài khoản có ít nhất {0}{1}", req.amount,Server.iMoneyString));
				}
				break;
			}
			case Shop.TYPE_ITEM:
			{
				ItemTemplate it = ObjectAccessor.getItemTemplate(req.item.id);
				if (it == null) {
					throw new ShopException("商品配置错误");
				}
				PlayerTransaction tx1 = p.newTransaction(buyObject.getCause());
				if (p.bag.removeGameItem(req.item.id, -1, req.amount, tx1, true) == null) {
					tx1.rollback();
					if (req.amount == 1) {
						throw new ShopException(MessageFormat.format("要求拥有{0}", it.name));
					} else {
						throw new ShopException(MessageFormat.format("Yêu cầu có {0}cái{1}", req.amount,it.name));
					}
				}
				tx1.rollback();
				break;
			}
			case Shop.TYPE_MONEY:
			{
			    if (p.getMoney() < req.amount) {
			        throw new ShopException(MessageFormat.format("Cần có {0} vàng", req.amount));
			    }
				break;
			}
			case Shop.TYPE_RANK:
			{
				DataService ds = Server.server.getServiceRegistry().getDataService();
				Rank rank = (Rank)ds.data.findDictObject(Rank.class, req.amount);
				if (rank == null) {
					throw new ShopException("商品配置错误");
				}
				if (p.getRank() < rank.id) {
					throw new ShopException(MessageFormat.format("Yêu cầu có {0} hoặc  quân hàm trở lên mới có thể mua được", rank.title));	
				}
				break;
			}
			case Shop.TYPE_VARIABLE:
			{
			    int value = p.asmVm.getGlobalValue(req.varName);
			    if (value < req.amount) {
			        throw new ShopException(MessageFormat.format("Điều kiện không đủ: {0}", req.varDesc));
			    }
			    break;
			}
			case Shop.TYPE_LEVEL:
			{
			    if (p.level < req.amount) {
			        throw new ShopException(MessageFormat.format("需要达到{0}级", req.amount));
			    }
			    break;
			}
			case Shop.TYPE_CONSUMECODE:
			{
				continue;
			}
			}
			reqItor.remove();
		}
		
		// 锁定购买目标
		buyObject.lock();
	}
	
	/**
	 * 为本次购买进行支付，尝试扣除所有需要扣除的东西。
	 * @return 如果支付成功，返回true；如果需要支付i币，不能立刻扣除，返回false
	 * @throws ShopException 如果支付过程中遇到不够的，抛出异常
	 */
	public boolean pay() throws ShopException {
		Player p = (Player)ObjectAccessor.getGameObject(player);
		if (p == null) {
			throw new ShopException("Trạng thái nhân vật di thường");
		}
		succReqs = new ArrayList<Shop.BuyRequirement>();
		Iterator<Shop.BuyRequirement> reqItor = pendingReqs.iterator();
		int imoney = 0;
		int count = buyObject.getCount();
		int discount = buyObject.getDiscount();
		String consumeCode = null;
		int money = 0;
		while (reqItor.hasNext()) {
			Shop.BuyRequirement req = reqItor.next();
			if (req.type == Shop.TYPE_CONSUMECODE) {
				consumeCode = req.varName;
				continue;
			}
			if (!req.deduct) {
				continue;
			}
			switch (req.type) {
			case Shop.TYPE_HONOR:
			{
				long value = (long)req.amount * count * discount / 100;
				if (value > 0x7FFFFFFF) {
                    // 越界了
                    throw new ShopException("Chiến công không đủ");
                }
				try {
					p.decCredit((int)value, tx, false);
				} catch (NoEnoughValueException e) {
					throw new ShopException("Chiến công không đủ");
				}
				break;
			}
			case Shop.TYPE_IMONEY:
			{
			    long value = (long)req.amount * count * discount;
			    if (value > 0x7FFFFFFF) {
			        // 越界了
			        throw new ShopException("Số dư không đủ");
			    }
				imoney += (int)value;
				break;
			}
			case Shop.TYPE_ITEM:
			{
				ItemTemplate it = ObjectAccessor.getItemTemplate(req.item.id);
				if (it == null) {
					throw new ShopException("商品配置错误");
				}
				if(it.isEquipment()){
					if (p.bag.removeGameItemIngoreInstanceId(req.item.id, req.amount*count, tx, true) == null) {
						if (req.amount * count == 1) {
							throw new ShopException(MessageFormat.format("Cần {0}",  it.name));
						} else {
							throw new ShopException(MessageFormat.format("Cần 0}cái{1}", (req.amount * count),it.name));
						}
					}
				}else{
					if (p.bag.removeGameItem(req.item.id, -1, req.amount*count, tx, true) == null) {
						if (req.amount * count == 1) {
							throw new ShopException(MessageFormat.format("Cần {0}",  it.name));
						} else {
							throw new ShopException(MessageFormat.format("Cần 0}cái{1}", (req.amount * count),it.name));
						}
					}
				}
				break;
			}
			case Shop.TYPE_MONEY: {
				long value = (long)req.amount * count * discount / 100;
				if (value > 0x7FFFFFFF) {
                    // 越界了
                    throw new ShopException("Số dư không đủ");
                }
				money += (int)value;
				try {
					p.decMoney((int)value, tx, false);
				} catch (NoEnoughValueException e) {
					throw new ShopException("Số dư không đủ");
				}	
				break;
			}
			case Shop.TYPE_RANK:
				// 不可能走到这一步
				break;
			case Shop.TYPE_VARIABLE:
                // 不可能走到这一步
                break;
            case Shop.TYPE_LEVEL:
                // 不可能走到这一步
                break;
			}
			reqItor.remove();
			succReqs.add(req);
		}
		if (imoney > 0) {
			if("CMCC".equals(Server.server.revision)){
				if(count>1){
					throw new ShopException("Không thể mua một lần nhiều vật phẩm này");
				}
			}
			if ("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)) {
				if (consumeCode == null) {
					throw new ShopException("Thương phẩm này tạm thời không cho mua");
				}
			}
			// 向认证服务器发起扣费请求，并把自己添加到ShopService的回调请求中
			shopService.asyncPay(p.accountId, imoney, consumeCode, this, p.level, p.faction);
			return false;
		}
		
		// 统计
		if (money > 0) {
			Server.server.getServiceRegistry().getRealtimeStatService().moneyUseCounter += money;
		}
		return true;
	}
	
	/**
	 * 收货，把要购买的物品添加到用户背包中。
	 * @param supportMail 是否支持在背包满时使用邮件发货
	 * @throws ShopException 如果背包满，抛出异常
	 */
	public void receive(boolean supportMail) throws ShopException {
		buyObject.receive(tx, supportMail);
	}
	
	/**
	 * 提交事务，实际扣除所有需要扣除的东西。
	 */
	public void commit() {
	    buyObject.commit();
		tx.commit();
	}
	
	/**
	 * 回滚事务，把所有先前扣除成功的东西全部恢复。
	 */
	public void rollback() {
		if (tx != null) {
			tx.rollback();
		}
		buyObject.rollback();
	}
}
