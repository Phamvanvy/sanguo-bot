package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

/**
 * 获得坐骑经验。
 * @author lighthu
 */
public class GetHorseExpEffect implements ItemEffect {
    protected float amount;
    protected int[] valueTable;
	
	public GetHorseExpEffect(float amount, int[] valueTable) {
	    this.amount = amount;
	    this.valueTable = valueTable;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)source;
		if (p.horse == null) {
		    throw new UseItemException("你现在没有骑马");
		}
		if(p.horse.level >= p.level){
			throw new UseItemException("当您升到更高等级的时候才能对这匹马使用此道具");
		}
		int addValue;
		if (valueTable != null) {
            addValue = valueTable[p.horse.level - 1];
        } else if (amount >= 0) {
		    addValue = (int)amount;
		} else {
		    addValue = (int)(-(amount * p.horse.level));
		}
		p.horse.setExp(p.horse.exp + addValue, p, "ITE");
	}
	
	public boolean isAsync(){
		return false;
	}
}
