package peony.game.drop;

import java.util.Random;

import peony.game.CommonUtil;
import peony.game.Gain;

public class ExpDrop extends SimpleDrop {
	
	public ExpDrop(int questId,int min,int max){
		super(questId,min,max);
	}
	
	public void calc(Random rnd, Gain gain) {
		int e = CommonUtil.getCount(rnd, min, max);
		if(e>0){
			gain.addExp(e);
		}
	}

	public int calc(Random rnd){
		return CommonUtil.getCount(rnd, min, max);
	}
}
