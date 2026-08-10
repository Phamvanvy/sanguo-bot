package peony.game.drop;

import java.util.Random;

import peony.game.CommonUtil;
import peony.game.Gain;

/**
 * ÉùÍûµôÂä¡£
 * @author lighthu
 */
public class HonorDrop extends SimpleDrop {
	public HonorDrop(int questId,int min,int max){
		super(questId,min,max);
	}
	
	public void calc(Random rnd, Gain gain) {
		int m = CommonUtil.getCount(rnd, min, max);
		if(m>0){
			gain.addHonor(m);
		}
	}
	
	public int calc(Random rnd){
		return CommonUtil.getCount(rnd, min, max);
	}

}
