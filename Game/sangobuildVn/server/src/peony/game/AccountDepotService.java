package peony.game;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.pip.sanguo.data.item.Item;
import peony.db.AccountDepotDAO;
import peony.depot.AccountDepotCall;
import peony.depot.DepotException;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;

public class AccountDepotService implements Service{
	
	private static final int OPEN_ITEMID = 2396;
	private ConcurrentHashMap<Integer,AccountDepot> accountDepots = new ConcurrentHashMap<Integer,AccountDepot>();	
	/**
	 * 判断是否拥有账号仓库
	 */
	public void hasAccountDepot(Player p, int serial,short id){
		if (p != null) {
			if(id == 0){
				AccountDepot accountDepot = accountDepots.get(p.accountId);
				Packet pt = new Packet(OpCode.ACCOUNTDEPOT_CHECK_SERVER);
				pt.putInt(serial);
				if(accountDepot!=null && accountDepot.depot.getGrids().size()>0){
					pt.put(accountDepot.depot.getSize());
					for (TransactionBagGrid grid : accountDepot.depot.getGrids()) {
						pt.put(grid.toClientByte());
					}
				} else{
					pt.put(0);
				}
				p.send(pt);
			}
		}
	}
	
	/**
	 * 账户仓库开启
	 */
	public void turnOn(Player p, int serial,short id) throws Exception {
		if (p != null) {
			if(id == 0){
				AccountDepot accountDepot = accountDepots.get(p.accountId);
				if (accountDepot!=null && accountDepot.depot != null && accountDepot.depot.getGrids().size() > 0) {
					throw new Exception("您已经开启珍宝阁了!");
				}
				PlayerTransaction tx = p.newTransaction("OPENACCOUNTDEPOT");
			    GameItem item = p.bag.removeGameItemIngoreInstanceId(OPEN_ITEMID, 1, tx, false);
			    if(item == null){
					tx.rollback();
					throw new Exception("您的物品不足!");
			    }
				tx.commit();
				accountDepot = new AccountDepot();
				accountDepot.accountId = p.accountId;
				accountDepot.depot = new TransactionBag(p, 15, 0);
//				AccountDepotDAO accountDepotDAO= Server.server.getServiceRegistry().getDbService().accountDepotDAO;
//				accountDepotDAO.newEntity(accountDepot);
				accountDepots.put(p.accountId, accountDepot);
				Server.server.getServiceRegistry().getDbService().schedule(new AccountDepotCall(p.session, accountDepot));
				LogUtil.logAccountDepotOpen(p);
				Packet pt = new Packet(OpCode.ACCOUNTDEPOT_REQUEST_SERVER);
				pt.putInt(serial);
				pt.put(accountDepot.depot.getSize());
				for (TransactionBagGrid grid : accountDepot.depot.getGrids()) {
					pt.put(grid.toClientByte());
				}
				p.send(pt);
			}
		}
	}
	
	/**
	 * 从背包中取物品放入仓库
	 */
	public void getItemFromBagToDepot(Player p, ClientSession session, int serial, 
			int gridId, int itemId, int instanceId, int count) throws Exception {
		if (p != null) {
			if (count <= 0) {
				throw new Exception("Invalid item count");
			}
			AccountDepot accountDepot = accountDepots.get(p.accountId);
			if(accountDepot == null || accountDepot.depot==null || accountDepot.depot.getSize()==0){
				throw new Exception("您还没有开启珍宝阁!");
			}
			int currentCount = p.bag.getGameItemCount(itemId);
			if(currentCount <= 0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_GETFROMBAG_CLIENT, "没有该物品!");
				return;
			}
			if (count > currentCount) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_GETFROMBAG_CLIENT, "Số lượng vật phẩm không đủ!");
				return;
			}
			GameItem item = ObjectAccessor.createGameItem(itemId);
			if(item == null || item.template.itemType != Item.TYPE_JEWEL || item.template.useLevel < 4){
				throw new Exception("珍宝阁只能存放4级(含)以上宝石");
			}
			PlayerTransaction tx = p.newTransaction("ASTO");
			TransactionBagGrid bagGrid = p.bag.removeGridGameItem(gridId, itemId, instanceId, count, tx, false);
			if (bagGrid == null) {
				tx.rollback();
				throw new Exception("Invalid bag item");
			}
			// 记录日志
			LogUtil.logAccountDepotPutTry(p, bagGrid.getItem(), count);
			List<TransactionBagGrid> depotGrids;
			try {
				depotGrids = accountDepot.depot.addDepotGameItemComplete(bagGrid.getItem(), count, tx, false);
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
				throw new Exception("您的珍宝阁已满!");
			}
			// 记录日志
			LogUtil.logAccountDepotPutOK(p, bagGrid.getItem(), count);
			tx.commit();
			Packet pt = new Packet(OpCode.ACCOUNTDEPOT_GETFROMBAG_SERVER);
			pt.putInt(serial);
			pt.put(depotGrids.size());
			for(TransactionBagGrid depotGrid : depotGrids){
				pt.put(depotGrid.toClientByte());
			}
			accountDepots.put(p.accountId, accountDepot);
			p.send(pt);
		}
	}
	
	
	/**
	 * 从仓库中取出物品放回背包
	 */
	public void getItemFromDepotToBag(Player p, ClientSession session, int serial,  
			int gridId, int itemId, int instanceId, int count) throws Exception {
		if (p != null) {
			if (count <= 0) {
				throw new Exception("Invalid item count");
			}
			AccountDepot accountDepot = accountDepots.get(p.accountId);
			if(accountDepot == null || accountDepot.depot==null || accountDepot.depot.getSize()==0){
				throw new DepotException("你还没有开启珍宝阁!");
			}
			int currentCount = accountDepot.depot.getGameItemCount(itemId);
			if(currentCount <= 0){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_GETFROMBAG_CLIENT, "没有该物品!");
				return;
		    }
			if (count > currentCount) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEPOT_GETFROMDEPOT_CIENT, "Số lượng vật phẩm không đủ!");
				return;
			}
			PlayerTransaction tx = p.newTransaction("AUST");
			TransactionBagGrid depootGrid = accountDepot.depot.removeDepotGridGameItem(gridId, itemId, instanceId, count, tx, false);
			if (depootGrid == null) {
				tx.rollback();
				throw new Exception("Invalid depot item");
			}
			// 记录日志
			LogUtil.logAccountDepotGetTry(p, depootGrid.getItem(), count);
			try {
				p.bag.addGameItemComplete(depootGrid.getItem(), count, tx, false);
			} catch (NoEnoughSpaceException e) {
				tx.rollback();
				throw new Exception("您的背包已满\nHành trang của bạn đã đầy");
			}
			// 记录日志
			LogUtil.logAccountDepotGetOK(p, depootGrid.getItem(), count);
			tx.commit();
			Packet pt = new Packet(OpCode.ACCOUNTDEPOT_GETFROMBAG_SERVER);
			pt.putInt(serial);
			pt.put(1);
			pt.put(depootGrid.toClientByte());
			accountDepots.put(p.accountId, accountDepot);
			p.send(pt);
		}
	}
	
	/**
	 * 账号仓库整理
	 */
	protected void accountDepotArrange(Packet packet, ClientSession session) {
		Player player = (Player) session.getClient();
		if (player != null) {
			int serial = packet.getInt();
			AccountDepot accDepot = accountDepots.get(player.accountId);
			if(accDepot != null){
				if (accDepot.depot.arrange()) {
					Packet pt = new Packet(OpCode.ACCOUNTDEPOT_ARRANGE_SERVER);
					pt.putInt(serial);
					pt.put(accDepot.depot.getSize());
					for (TransactionBagGrid grid : accDepot.depot.grids) {
						pt.put(grid.toClientByte());
					}
					player.send(pt);
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ACCOUNTDEPOT_ARRANGE_SERVER, "现在暂时不能整理珍宝阁，请稍后再试");
				}
			}
		}
	}
	
//	/**
//	 * 交换仓库里的两个包格。
//	 */
//	public void exchangeAccountDepotGrid(Player p, ClientSession session, int serial, int grid1, int grid2) throws Exception {
//		if (p != null) {
//			AccountDepot accDepot = accountDepots.get(p.accountId);
//			if(accDepot != null){
//				if (accDepot.depot == null || accDepot.depot.getSize() == 0){
//					throw new DepotException("你还没有开启账号仓库!");
//				}
//				boolean ret = accDepot.depot.exchange(grid1, grid2, false);
//				if (ret) {
//					Packet pt = new Packet(OpCode.ACCOUNTDEPOT_EXCHANGE_SERVER);
//					pt.putInt(serial);
//					pt.put(2);
//					pt.put(accDepot.depot.getGrids().get(grid1).toClientByte());
//					pt.put(accDepot.depot.getGrids().get(grid2).toClientByte());
//					p.send(pt);
//				} else {
//					throw new DepotException("移动物品失败");
//				}
//			}
//		}
//	}

	public void shutdown() {
		if(accountDepots!=null && accountDepots.size()>0){
			for(int accountId:accountDepots.keySet()){
				saveAccountDepot(accountId);
			}
		}
	}

	public void loadAccountDepot(int accountId){
		AccountDepotDAO accountDepotDAO= Server.server.getServiceRegistry().getDbService().accountDepotDAO;
		AccountDepot depot = accountDepotDAO.getAccountDepot(accountId);
		if(depot!=null){
			accountDepots.put(accountId, depot);
		}
	}
	
	public void saveAccountDepot(int accountId){
		if(accountDepots!=null && accountDepots.size()>0){
			AccountDepotDAO accountDepotDAO= Server.server.getServiceRegistry().getDbService().accountDepotDAO;
			AccountDepot depot = accountDepots.get(accountId);
			if(depot != null){
				accountDepotDAO.updateEntity(depot);
			}
			accountDepots.remove(accountId);
		}
	}

	public void startup() throws Exception {
		
	}
	
	

}
