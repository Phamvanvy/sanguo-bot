package peony.game.drop;

import java.util.Random;

import peony.game.CommonUtil;
import peony.game.Gain;

public class MoneyDrop extends SimpleDrop {

	public MoneyDrop(int questId,int min,int max){
		super(questId,min,max);
	}
	
	public void calc(Random rnd, Gain gain) {
		int m = CommonUtil.getCount(rnd, min, max);
		m *= gain.getPlayer().tirePercent;
		if(m>0){
			gain.addMoney(m);
		}
	}
	
	public int calc(Random rnd){
		return CommonUtil.getCount(rnd, min, max);
	}

}
