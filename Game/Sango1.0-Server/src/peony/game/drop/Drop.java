package peony.game.drop;

import java.util.Random;

import peony.game.Gain;

public interface Drop {
	
	public void calc(Random rnd,Gain gain);
}
