package peony.game.nation;

import peony.game.Creature;
import peony.game.CreatureDieCallback;
import peony.game.GameObject;
import peony.game.Player;
import peony.game.Unit;

public class SneakCreatureDieCallback implements CreatureDieCallback {

	public void die(Creature c, Unit source) {
		if(source.type==GameObject.TYPE_PLAYER){
			int type = 0;
			if(c.name.equals("军械都尉")){
				type = SneakFlag.TYPE_WEAPON;
			}else if(c.name.equals("粮草都尉")){
				type = SneakFlag.TYPE_FOOD;
			}else{
				return;
			}
			Player p = (Player)source;
			if(p.isAlive()){
				SneakFlag f = (SneakFlag)p.flag;
				if(f==null){
					f = new SneakFlag((NationSneakBattleFieldInstance)source.getVMap().instance,type);
					p.setFlag(f);
				}else{
					f.type |= type;
				}
			}
		}
	}

}
