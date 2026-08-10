package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.attendant.Attendant;


public class AttendantAddHpItemEffect implements ItemEffect {

	protected int addHp;
	protected int addMp;
	
	public AttendantAddHpItemEffect(int addHp, int addMp){
		this.addHp = addHp;
		this.addMp = addMp;
	}
	
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if (!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player player = (Player) target;
		if (player != null) {
			Attendant attendant = player.attendant;
			if(attendant!=null){
				if(attendant.hp>=attendant.maxhp && attendant.mp>=attendant.maxmp){
					throw new UseItemException(peony.Messages.STRING_00531);
				}
				int currentHp = attendant.hp;
				int maxHp = attendant.maxhp;
				currentHp += addHp;
				currentHp = Math.min(currentHp, maxHp);
				attendant.setHp(currentHp, false);
				
				int currentMp = attendant.mp;
				int maxMp = attendant.maxmp;
				currentMp += addMp;
				currentMp = Math.min(currentMp, maxMp);
				attendant.setMp(currentMp, false);
			}else{
				throw new UseItemException(peony.Messages.STRING_00532);
			}
		}
	}
	
	public boolean isAsync() {
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}

}
