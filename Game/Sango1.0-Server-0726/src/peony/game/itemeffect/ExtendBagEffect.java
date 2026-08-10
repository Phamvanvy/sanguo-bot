package peony.game.itemeffect;

import peony.game.Action;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

/**
 * 扩展背包。
 * @author lighthu
 */
public class ExtendBagEffect implements ItemEffect {
    protected int count;
	
	public ExtendBagEffect(int count) {
	    this.count = count;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)source;
		if(p.bag.getAddedSize()>=count)
			throw new UseItemException("不能再用此物品扩展包格了");
		p.bag.extend(count, true);
		
		// 记录玩家动作
		p.addAction(Action.EXTEND_BAG);
	}
	
	public boolean isAsync(){
		return false;
	}
}
