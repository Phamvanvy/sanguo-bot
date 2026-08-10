package peony.game.nation;

import peony.game.Flag;
import peony.game.Player;

public class SneakFlag implements Flag {

	public static final int TYPE_FOOD = 1;
	public static final int TYPE_WEAPON = 2;
	public static final int FOOD_SCORE = 15;
	public static final int WEAPON_SCORE = 30;
	
	public NationSneakBattleFieldInstance instance;
	public int type;
	
	public SneakFlag(NationSneakBattleFieldInstance instance,int type){
		this.instance = instance;
		this.type = type;
	}
	
	public void bind(Player p) {
		
	}

	public void unbind(Player p) {
		
	}
	
	public int getScore(){
		int ret = 0;
		if((type&TYPE_FOOD)!=0){
			ret += FOOD_SCORE;
		}
		if((type&TYPE_WEAPON)!=0){
			ret += WEAPON_SCORE;
		}
		return ret;
	}

}
