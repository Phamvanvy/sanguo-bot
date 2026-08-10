package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Title;
import peony.game.TitleUtil;
import peony.game.Unit;
import peony.game.UseItemException;

public class AddTitleItemEffect implements ItemEffect {

	protected int titleId;
	
	public AddTitleItemEffect(int titleId){
		this.titleId = titleId;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player)target;
		Title t = TitleUtil.getTitle(titleId);
		if(t!=null){
			if(!p.addTitle(t)){
				throw new UseItemException("此稱號已經存在");
			}
		}else{
			throw new UseItemException("沒找到指定的稱號");
		}
	}

	public boolean isAsync(){
		return false;
	}
}
