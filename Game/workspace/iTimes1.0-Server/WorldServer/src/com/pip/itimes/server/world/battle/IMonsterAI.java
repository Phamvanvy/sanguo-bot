package com.pip.itimes.server.world.battle;

import java.util.Random;
import java.util.Vector;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IMonsterAI {

    public static final Random rnd = new Random();

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                       BattleSprite[] them, BattleSprite[] ourPet,
                       BattleSprite[] themPet,Vector battleMovie,
                       BattleDataProcess battleDataProcess, int round);
    public int getSpecialHp();
    public int getSpecialMp();
}
