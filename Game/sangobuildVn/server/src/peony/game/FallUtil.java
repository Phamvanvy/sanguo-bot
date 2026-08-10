package peony.game;

import java.util.Random;

public class FallUtil {
	
	
    public static int getCount(Random rnd, int min, int max){
        return rnd.nextInt(max - min + 1) + min;
    }
    
    public static boolean hit(Random rnd, int chance, int base){
        int r = rnd.nextInt(base);
        if(r <= chance)
            return true;
        return false;
    }
    
    public static boolean hitFall(Random rnd,int chance){
    	return hit(rnd,chance,10000);
    }
}
