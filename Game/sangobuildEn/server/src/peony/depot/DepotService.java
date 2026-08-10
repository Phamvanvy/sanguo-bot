package peony.depot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import peony.game.Action;
import peony.game.ErrorHandler;
import peony.game.ExtendBagBuy;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionBag;
import peony.game.TransactionBagGrid;
import peony.game.changed.BagChangedItem;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;

public class DepotService implements Service {
	private static final Logger log = Logger.getLogger(DepotService.class);
	private ConcurrentHashMap<Integer, Boolean> map = new ConcurrentHashMap<Integer, Boolean>();
	
	/**
	 * 仓库开启
	 */
	public void turnOn(Player p, int serial) throws DepotException {
		if (p != null) {
			if (p.depot != null && p.depot.getGrids().size() > 0) {
				throw new DepotException("您已經開啟過倉庫了!");
			}
			int playerLevel = p.level;
			if (playerLevel < 30) {
				throw new DepotException("級別達到30級才能擁有自己的倉庫哦!");
			}
			p.depot = new TransactionBag(p, 15, 0);
			Packet pt = new Packet(OpCode.DEPOT_REQUEST_SERVER);
			pt.putInt(serial);
			pt.put(p.depot.getSize());
			for (TransactionBagGrid grid : p.depot.getGrids()) {
				pt.put(grid.toClientByte());
			}
			p.send(pt);
			map.put(p.id, false);
			
			// 记录玩家动作
			p.addAction(Action.OPEN_DEPOT);
		}
	}

	/**
	 * 从仓库中取出物品放回背包
	 */
	public void getItemFromDepotToBag(Player p, ClientSession session, int serial,  
			int gridId, int itemId, int instanceId, int count) throws DepotException {
		if (p != null) {
			if(p.depot==null || p.depot.getSize()==0){
				throw new DepotException("你還沒有開啟倉庫!");
			}
			int currentCount = p.depot.getGameItemCount(itemId);
			if(currentCount <= 0)
				return;
			if (count > currentCount) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_GETFROMDEPOT_CIENT, "物品數量不足!");
				return;
			}
			PlayerTransaction tx = p.newTransaction("UST");
			TransactionBagGrid depootGrid = p.depot.removeDepotGridGameItem(gridId, itemId, instanceId, count, tx, false);
			// 记录日志
			LogUtil.logDepotGetTry(p, depootGrid.getItem(), count);
			try {
				p.bag.addGameItemComplete(depootGrid.getItem(), count, tx, false);
				p.decMoney(100, tx, false);
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
				throw new DepotException("您的背包已滿!");
			} catch (NoEnoughValueException e){
				tx.rollback();
				throw new DepotException("需要收取100金錢費用,您的金錢不足!");
			}
			// 记录日志
			LogUtil.logDepotGetOK(p, depootGrid.getItem(), count);
			tx.commit();
			Packet pt = new Packet(OpCode.DEPOT_GETFROMBAG_SERVER);
			pt.putInt(serial);
			pt.put(1);
			pt.put(depootGrid.toClientByte());
			p.send(pt);
		}
	}

	/**
	 * 从背包中取物品放入仓库
	 */
	public void getItemFromBagToDepot(Player p, ClientSession session, int serial, 
			int gridId, int itemId, int instanceId, int count) throws DepotException {
		if (p != null) {
			if(p.depot==null || p.depot.getSize()==0){
				throw new DepotException("你還沒有開啟倉庫!");
			}
			int currentCount = p.bag.getGameItemCount(itemId);
			if(currentCount <= 0){
				return;
			}
			if (count > currentCount) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_GETFROMBAG_CLIENT, "物品數量不足!");
				return;
			}
			PlayerTransaction tx = p.newTransaction("STO");
			TransactionBagGrid bagGrid = p.bag.removeGridGameItem(gridId, itemId, instanceId, count, tx, false);
			// 记录日志
			LogUtil.logDepotPutTry(p, bagGrid.getItem(), count);
			List<TransactionBagGrid> depotGrids;
			try {
				depotGrids = p.depot.addDepotGameItemComplete(bagGrid.getItem(), count, tx, false);
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
				throw new DepotException("您的倉庫已滿!您可以到元寶商店購買并使用更多格位的倉庫擴展符.");
			}
			// 记录日志
			LogUtil.logDepotPutOK(p, bagGrid.getItem(), count);
			tx.commit();
			if(map.get(p.id) != null && !map.get(p.id)){
				Packet pt = new Packet(OpCode.MESSAGE_SERVER);
				pt.putInt(0);
				pt.putString("每次從倉庫取出一次物品,需要收取100金錢費用");
				pt.putInt(0);
				pt.putInt(0);
				p.send(pt);
				map.remove(p.id);
				map.put(p.id, true);
			}
			Packet pt = new Packet(OpCode.DEPOT_GETFROMBAG_SERVER);
			pt.putInt(serial);
			pt.put(depotGrids.size());
			for(TransactionBagGrid depotGrid : depotGrids){
				pt.put(depotGrid.toClientByte());
			}
			p.send(pt);
		}
	}
	
	/**
	 * 交换仓库里的两个包格。
	 */
	public void exchangeGrid(Player p, ClientSession session, int serial, int grid1, int grid2) throws DepotException {
		if (p != null) {
			if (p.depot == null || p.depot.getSize() == 0){
				throw new DepotException("你還沒有開啟倉庫!");
			}
			boolean ret = p.depot.exchange(grid1, grid2, false);
			if (ret) {
				Packet pt = new Packet(OpCode.DEPOT_EXCHANGE_SERVER);
				pt.putInt(serial);
				pt.put(2);
				pt.put(p.depot.getGrids().get(grid1).toClientByte());
				pt.put(p.depot.getGrids().get(grid2).toClientByte());
				p.send(pt);
			} else {
				throw new DepotException("移動物品失敗");
			}
		}
	}
	
	/**
	 * 拆分仓库里的一个物品。
	 */
	public void splitItem(Player p, ClientSession session, int serial, int gridId, int itemId, int count) throws DepotException {
		if (p.depot == null || p.depot.getSize() == 0){
			throw new DepotException("你還沒有開啟倉庫!");
		}
		try {
			ChangedItem[] changes = p.depot.splitGridGameItem(gridId, itemId, count);
			Packet pt = new Packet(OpCode.DEPOT_SPLITITEM_SERVER);
			pt.putInt(serial);
			pt.put(changes.length);
			for (ChangedItem citem : changes) {
				BagChangedItem bcitem = (BagChangedItem)citem;
				pt.put(bcitem.getGrid().toClientByte());
			}
			p.send(pt);
		} catch (NoEnoughSpaceException ne) {
			throw new DepotException("倉庫已滿,不能拆分");
		} catch (Exception e) {
			throw new DepotException("此物品不能拆分");
		}
	}
	
	/*
	 * 取得玩家扩展仓库包格需要的价格。
	 */
	private int getExtendDepotPrice(Player p) {
		int addSize = p.depot.getAddedSize();
		return (addSize + 1) * 100;
	}
	
	/**
	 * 取扩展仓库的价格。
	 */
	public void getExtendDepotPrice(Player player, ClientSession session, int serial) {
		if (player.depot == null || player.depot.getGrids().size() == 0) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.EXTEND_DEPOT_PRICE_CLIENT, "你還沒有開啟倉庫!");
			return;
		}
		Packet pt = new Packet(OpCode.EXTEND_DEPOT_PRICE_SERVER);
		pt.putInt(serial);
		pt.putInt(getExtendDepotPrice(player));
		session.send(pt);
	}
	
	/**
	 * 扩展仓库。
	 */
	public void extendDepot(Player player, ClientSession session, int serial) {
		if (player.depot == null || player.depot.getGrids().size() == 0) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.EXTEND_DEPOT_CLIENT, "你還沒有開啟倉庫!");
			return;
		}
		if (player.depot.isSizeLocked()) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.EXTEND_DEPOT_CLIENT, "操作正在進行中");
			return;
		}
		
		// 通过ShopService完成购买
		int price = getExtendDepotPrice(player);
		ShopService shopService = Server.server.getServiceRegistry().getShopService();
		try {
			player.depot.lockSize();
			shopService.buy(player, new ExtendDepotBuy(player, serial, price));
		} catch (ShopException e) {
		}
	}

	public void shutdown() {
		
	}

	public void startup() throws Exception {
		
	}

	public void flushDepot(Player p) {
		if(p != null){
			map.remove(p.id);
			map.put(p.id, false);
		}
	}

}
