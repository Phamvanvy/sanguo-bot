package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IPet extends IItem{
    public int getPetType();
    public int getLevel();
    public int getCurrentPoint();
    public int getPoint();
    public int getFavor();
    public int getAgility();
    public int getStrength();
    public int getVitality();
    public int getIntelligence();
    public int getHp();
    public int getMp();
    public boolean getBaby();
    public int getExp();
    public Ability[] getAbilities();
//    public int getRenameTimes();
}
