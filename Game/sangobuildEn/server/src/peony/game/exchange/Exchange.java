package peony.game.exchange;

import java.util.concurrent.atomic.AtomicInteger;

import peony.game.Gain;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Time;
import peony.net.Packet;

public class Exchange {
	private static final AtomicInteger ids = new AtomicInteger(0);
	private static final int MAX_GRID = 8;
	public int serial;
	public ExchangeService service;
	public int id;

	public GameObjectRef sourceRef;
	public int sourceMoney;
	public int sourceStatus;
	public ExchangeBag sourceBag;

	public GameObjectRef targetRef;
	public int targetMoney;
	public int targetStatus;
	public ExchangeBag targetBag;

	public int state;
	
	public int exchangeId; //交易流水号,每次变更交易内容都会递增，客户端在确认交易是上传的流水号必须跟服务器端得相同才会进行交易

	public static final int STATE_INIT = 0;
	public static final int STATE_STARTED = 1;
	public static final int STATE_COMPLETED = 2;

	public int createTime;

	public Exchange(ExchangeService service, int serial,
			GameObjectRef sourceRef, GameObjectRef targetRef) {
		this.service = service;
		this.serial = serial;
		id = ids.incrementAndGet();
		this.sourceRef = sourceRef;
		this.targetRef = targetRef;
		this.sourceBag = new ExchangeBag(MAX_GRID);
		this.targetBag = new ExchangeBag(MAX_GRID);
		this.state = STATE_INIT;
		this.createTime = Time.currTime;
		service.exchangeCreated(this);
	}

	public boolean contains(int playerId) {
		return sourceRef.id == playerId || targetRef.id == playerId;
	}

	public boolean addMoney(int playerId, int count) {
		if (playerId == sourceRef.id) {
			sourceMoney += count;
			changeAllStatus();
			return true;
		} else if (playerId == targetRef.id) {
			targetMoney += count;
			changeAllStatus();
			return true;
		}
		return false;
	}

	public boolean accept(int playerId, int exchangeId) {
		if(this.exchangeId != exchangeId)
			return false;
		if (playerId == sourceRef.id) {
			sourceStatus = 1;
		} else if (playerId == targetRef.id) {
			targetStatus = 1;
		}
		if (checkComplete()) {
			return true;
		} else {
			Packet pt = getInfoPacket();
			broadcast(pt);
			return false;
		}
	}

	protected boolean checkComplete() {
		return sourceStatus == 1 && targetStatus == 1;
	}

	public void changeAllStatus() {
		sourceStatus = 0;
		targetStatus = 0;
		exchangeId ++;
		Packet pt = getInfoPacket();
		broadcast(pt);
	}

	public boolean decMoney(int playerId, int count) {
		if (playerId == sourceRef.id) {
			if (sourceMoney >= count) {
				sourceMoney -= count;
				changeAllStatus();
				return true;
			}
			return false;
		} else if (playerId == targetRef.id) {
			if (targetMoney >= count) {
				targetMoney -= count;
				changeAllStatus();
				return true;
			}
			return false;
		}
		return false;
	}

	public ExchangeGrid remove(int playerId, int gridId) {
		if (gridId < 0 || gridId >= MAX_GRID)
			return null;
		ExchangeBag bag = sourceBag;
		if (targetRef.id == playerId) {
			bag = targetBag;
		}
		ExchangeGrid grid = bag.removeGrid(gridId);
		if (grid != null && !grid.isEmpty()) {
			if(grid.item!=null){
				ObjectAccessor.removeGameItemFromCache(grid.item);
			}
			changeAllStatus();
		}
		return grid;
	}

	public boolean addGameItem(int playerId, GameItem item, int count) {
		ExchangeBag bag = sourceBag;
		if (targetRef.id == playerId) {
			bag = targetBag;
		}
		boolean added = bag.addItem(item, count);
		ObjectAccessor.addGameItemToCached(item);
		if (added) {
			changeAllStatus();
		}
		return added;
	}

	public Packet getInfoPacket() {
		Packet pt = new Packet(OpCode.EXCHANGE_INFO_SERVER);
		pt.putInt(id);
		pt.putInt(sourceRef.id);
		pt.put(sourceBag.getSize());
		for (ExchangeGrid g : sourceBag.grids) {
			pt.putShort(g.count);
			if (g.item != null)
				pt.put(g.item.toClientBytes());
		}
		pt.putInt(sourceMoney);
		pt.put(sourceStatus);
		pt.putInt(targetRef.id);
		pt.put(targetBag.getSize());
		for (ExchangeGrid g : targetBag.grids) {
			pt.putShort(g.count);
			if (g.item != null)
				pt.put(g.item.toClientBytes());
		}
		pt.putInt(targetMoney);
		pt.put(targetStatus);
		pt.putInt(exchangeId);
		return pt;
	}

	public void complete(Player opPlayer, boolean ok) {
		if(state == STATE_INIT){
			this.state = STATE_COMPLETED;
			service.removeExchange(id);
			return;
		}
		Packet pt = new Packet(OpCode.EXCHANGE_COMPLETE_SERVER);
		pt.putInt(id);
		if (ok)
			pt.put(0);
		else
			pt.put(1);
		Player p1 = ObjectAccessor.getPlayer(sourceRef.id);
		if (p1 != null) {
			if (p1.exchange == this) {
				p1.exchange = null;
			}
			p1.send(pt);
		}
		Player p2 = ObjectAccessor.getPlayer(targetRef.id);
		if (p2 != null) {
			if (p2.exchange == this) {
				p2.exchange = null;
			}
			p2.send(pt);
		}
		this.state = STATE_COMPLETED;
		service.removeExchange(id);
		for(ExchangeGrid grid:sourceBag.grids){
			if(grid.item!=null){
				ObjectAccessor.removeGameItemFromCache(grid.item);
			}
		}
		for(ExchangeGrid grid:targetBag.grids){
			if(grid.item!=null){
				ObjectAccessor.removeGameItemFromCache(grid.item);
			}
		}
	}

	public void broadcast(Packet pt) {
		Player p1 = ObjectAccessor.getPlayer(sourceRef.id);
		Player p2 = ObjectAccessor.getPlayer(targetRef.id);
		if (p1 != null)
			p1.send(pt);
		if (p2 != null)
			p2.send(pt);
	}

	public void restoreToGain(int playerId, Gain gain) {
		ExchangeBag bag = sourceBag;
		int money = sourceMoney;
		if (targetRef.id == playerId) {
			bag = targetBag;
			money = targetMoney;
		}
		gain.addMoney(money);
		bag.restoreToGain(gain);
	}

	public int getOtherId(int playerId) {
		if (sourceRef.id == playerId)
			return targetRef.id;
		else
			return sourceRef.id;
	}
}
