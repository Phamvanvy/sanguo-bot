package peony.depot;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class ExtendDepotEffect implements ItemEffect {

	protected int count;
	
	public ExtendDepotEffect(int count){
		this.count = count;
	}
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)source;
		if(p != null && p.depot.getGrids().size() == 0)
			throw new UseItemException("你还没有开启仓库不能使用此物品");
		if(p.depot.getAddedSize()>=count)
			throw new UseItemException("不能再用此物品扩展包格了");
		p.depot.extendDepot(count, true);
	}

}
