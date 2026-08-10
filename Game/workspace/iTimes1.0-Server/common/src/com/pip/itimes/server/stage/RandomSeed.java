package com.pip.itimes.server.stage;

import java.util.Random;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class RandomSeed {

    public static final Random rnd = new Random();

    public static int getSeed(){
        return rnd.nextInt(10000000)+1;
    }
}
