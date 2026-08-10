package peony.game.drop;

import java.util.Random;

import peony.game.CommonUtil;
import peony.game.Gain;

public class CreditDrop  extends SimpleDrop {
	
	public CreditDrop(int questId,int min,int max){
		super(questId,min,max);
	}
	
	public void calc(Random rnd, Gain gain) {
		int e = CommonUtil.getCount(rnd, min, max);
		if(e>0){
			gain.addCredit(e);
		}
	}

	public int calc(Random rnd){
		return CommonUtil.getCount(rnd, min, max);
	}
}
