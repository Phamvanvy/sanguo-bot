package com.pip.itimes.utils.award;

import com.pip.itimes.utils.IAward;

/**
 * 获得的属性变化
 * @author Jeffrey
 * @version 1.0
 */
public class PropertyAward implements IAward{

    public static final byte SEX = 1;
    public static final byte FACE = 2;
    public static final byte RETURNTIMES = 3;
    public static final byte LEVEL = 4;
    public static final byte EXP = 5;
    public static final byte MONEY = 6;
    public static final byte CREDIT = 7;
    public static final byte STRENGTH = 8;
    public static final byte AGILITY = 9;
    public static final byte VITALITY = 10;
    public static final byte INTELLIGENCE = 11;
    public static final byte LUCK = 12;
    public static final byte HP = 13;
    public static final byte MP = 14;
    public static final byte LEAVEPOINTS = 15;
    public static final byte PATTACK = 16;
    public static final byte PDEFENSE = 17;
    public static final byte MATTACK = 18;
    public static final byte MDEFENSE = 19;
    public static final byte HIT = 20;
    public static final byte PARRY = 21;
    public static final byte PCRITICAL = 22;
    public static final byte MCRITICAL = 23;
    public static final byte ARMOR = 24;
    public static final byte GAINEXP = 25;
    public static final byte UPLEVELEXP = 26;
    public static final byte GRIDSIZE = 27;
    public static final byte POINT = 28;


    private int property;
    private int value;

    public PropertyAward(int property,int value) {
        this.property = property;
        this.value = value;
    }

    public int getProperty(){
        return property;
    }

    public int getValue(){
        return value;
    }

    public String toString(){
        return "{Property["+property+","+value+"]}";
    }
}
