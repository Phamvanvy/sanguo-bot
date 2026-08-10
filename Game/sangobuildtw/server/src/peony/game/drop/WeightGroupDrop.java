package peony.game.drop;

import java.util.Random;

import peony.game.Gain;

public class WeightGroupDrop implements Drop {
    protected int start;
	protected int weight;
	protected Drop drop;
	
	public WeightGroupDrop(int weight,Drop drop){
		this.weight = weight;
		this.drop = drop;
	}
	
	public void calc(Random rnd, Gain gain) {
		drop.calc(rnd, gain);
	}

}
