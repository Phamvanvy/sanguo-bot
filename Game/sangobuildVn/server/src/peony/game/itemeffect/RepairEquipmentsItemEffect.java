package peony.game.itemeffect;

import java.util.ArrayList;
import java.util.List;

import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.net.Packet;

public class RepairEquipmentsItemEffect implements ItemEffect {

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx_ext)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player) source;
		int totalMoney = 0;
		List<Horse> horses = new ArrayList<Horse>();
		for (int i = 0; i < p.equipments.equs.length; i++) {
			GameItem equ = p.equipments.equs[i];
			if (equ != null) {
				totalMoney += equ.getRepairMoney();
			}
		}
		if (p.horse != null) {
			totalMoney += p.horse.getRepairMoney();
			horses.add(p.horse);
		}
		if (totalMoney == 0) {
			throw new UseItemException("没有需要修理的装备");
		}

		PlayerTransaction tx = p.newTransaction("REP");
		try {
			p.decMoney(totalMoney, tx, true);
			tx.commit();
			p.equipments.repair();
			if(p.horse!=null){
				p.horse.repair(p);
			}
			Packet pt = new Packet(OpCode.REPAIR_SERVER);
			pt.putInt(-1);
			pt.putInt(totalMoney);
			p.send(pt);
		} catch (NoEnoughValueException e) {
			tx.rollback();
			throw new UseItemException("没有足够的金钱");
		}
	}

}
