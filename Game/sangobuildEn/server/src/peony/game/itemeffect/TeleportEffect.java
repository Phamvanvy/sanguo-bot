package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.VMapException;
import peony.game.battlefield.FlagBattleFieldInstance;

/**
 * 物品使用传送效果。
 * @author lighthu
 */
public class TeleportEffect implements ItemEffect {
    protected int[] weiLocation;
    protected int[] shuLocation;
    protected int[] wuLocation;
	
	public TeleportEffect(int[] w, int[] s, int[] wu) {
	    weiLocation = w;
	    shuLocation = s;
	    wuLocation = wu;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player)source;
		if(p.getVMap().instance instanceof FlagBattleFieldInstance){
			throw new UseItemException("此地不能使用此道具");
		}
		int[] t;
		if (p.faction == GameObject.FACTION_WEI) {
		    t = weiLocation;
		} else if (p.faction == GameObject.FACTION_SHU) {
		    t = shuLocation;
		} else {
		    t = wuLocation;
		}
		try {
			((Player)source).goMap(t[0], t[1], t[2]);
		} catch (VMapException e) {
			throw new UseItemException(e.getMessage());
		}
	}
	
	public boolean isAsync(){
		return false;
	}
}
