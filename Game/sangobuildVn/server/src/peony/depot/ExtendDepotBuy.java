package peony.depot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mortbay.log.Log;

import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.item.Formula;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.TransactionBagGrid;
import peony.game.drop.GroupDrop;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.IBuyObject;
import peony.service.shop.ShopException;

public class ExtendDepotBuy implements IBuyObject {
	GameObjectRef player;
	int serial;
	int price;
	
	public ExtendDepotBuy(Player player, int serial, int price){
		this.player = player.ref();
		this.serial = serial;
		this.price = price;
	}
	
	public void commit() {
		Player p = (Player)ObjectAccessor.getGameObject(player);
		if (p != null) {
			p.depot.unlockSize();
		}
	}

	public int getCount() {
		return 1;
	}

	public int getDiscount() {
		return 100;
	}

	public List<BuyRequirement> getRequirements() {
		BuyRequirement req = new BuyRequirement();
		req.type = Shop.TYPE_IMONEY;
		req.amount = price;
		req.deduct = true;
		List<BuyRequirement> reqs = new ArrayList<BuyRequirement>();
		reqs.add(req);
		return reqs;
	}

	public void lock() throws ShopException {
	}

	public void notifyClient(boolean succ, String message) {
		Player p = (Player)ObjectAccessor.getGameObject(player);
		if (p == null) {
			return;
		}
		if (succ) {
		} else {
			ErrorHandler.sendErrorMessage(p.session, serial, OpCode.EXTEND_DEPOT_CLIENT, message);
		}
	}

	public void receive(PlayerTransaction tx, boolean supportMail)
			throws ShopException {
		Player p = (Player)ObjectAccessor.getGameObject(player);
        if (p == null) {
            throw new ShopException("Trạng thái nhân vật di thường");
        }
        TransactionBagGrid[] grids = p.depot.extendDepot(p.depot.getAddedSize() + 1, true);
        Packet pt = new Packet(OpCode.EXTEND_DEPOT_SERVER);
        pt.putInt(serial);
        pt.putInt(p.depot.getSize());
        pt.put((byte)grids.length);
        for (TransactionBagGrid grid : grids) {
        	pt.put(grid.toClientByte());
        }
		p.send(pt);
	}

	public void rollback() {
		Player p = (Player)ObjectAccessor.getGameObject(player);
		if (p != null) {
			p.depot.unlockSize();
		}
	}

	@Override
	public String toString() {
		return "TYPE[EXTENDDEPOT]";
    }
	
	/**
     * 记录购买成功日志。
     */
    public void log() {
    	LogUtil.logExtendDepot(player);
    }
    
    public String getCause() {
    	return "ETD";
    }
}

