package peony.game.drop;

import java.util.Random;

import peony.game.CommonUtil;
import peony.game.Gain;

public class RateDrop extends RangeDrop{
	protected int rate;
	protected Drop drop;
	
	public RateDrop(int rate,Drop drop){
		super(1, 1);
		this.rate = rate;
		this.drop = drop;
	}
	
	public RateDrop(int rate,Drop drop,int min,int max){
		super(min, max);
		this.rate = rate;
		this.drop = drop;
	}
	
	public Drop getDrop(){
		return drop;
	}
	
	public void calc(Random rnd, Gain gain) {
		if(CommonUtil.hit(rnd, rate, 1000000)){
			int count;
			if (min == max) {
				count = min;
			} else {
				count = CommonUtil.getCount(rnd, min, max);
			}
			for (int i = 0; i < count; i++) {
				drop.calc(rnd, gain);
			}
		}
	}
	
	public boolean hit(Random rnd) {
	    return CommonUtil.hit(rnd, rate, 1000000);
	}
}
