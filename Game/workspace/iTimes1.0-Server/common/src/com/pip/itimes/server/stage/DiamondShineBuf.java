package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DiamondShineBuf {
    public final static byte PHYSIC_ATTC = -2;
    public final static byte MAGIC_ATTC = -3;
    public final static byte NOCRI = -4;
    public final static byte PHYSIC_CRI = -5;
    public final static byte MAGIC_CRI = -6;
    
    public final static byte SERVER_PHYSIC_CRI = -7;
    public final static byte SERVER_MAGIC_CRI = -8;
    public final static byte AGI = 1;
    public final static byte STR = 2;
    public final static byte INT = 3;
    public final static byte ADD_MPMAX = 4;
    public final static byte ADD_HPMAX = 5;

    public final static byte STR_VALUE = 6; //增加力量点数
    public final static byte AGI_VALUE = 7; //增加敏捷点数
    public static final byte VIT_VALUE = 8; //增加体力点数
    public static final byte INT_VALUE = 9; //增加智力点数
    
    public final static byte UNIT_TIMES = 0;
    public final static byte UNIT_SECOND = 1;
    public final static byte UNIT_CAMP = 2;
    public final static byte UNIT_DIAMONDSHINE = 3;


    public final static int[] BUF_PRO ={1, 2, 3, 4};
    public final static String[] BUF_STRING = {
                                              "物理攻击力#",
                                              "魔法攻击力#",
                                              "物理爆击率#%",
                                              "魔法爆击率#%",
                                              "免爆击率#%",
                                              "敏捷率#%",
                                              "力量#",
                                              "智力#",
                                              "生命#",
                                              "魔法#",
    };

    public final static Map<Integer,String> BUFS =  new HashMap<Integer,String>();
    static{
        for(int i=0;i<BUF_PRO.length;i++){
            BUFS.put(BUF_PRO[i],BUF_STRING[i]);
        }
    }
    
    private int id;
    private byte pro;
    private int value;
    private int time;
    private byte unit;
    private long timestamp;
    
    public DiamondShineBuf(int id,byte pro,int value,int time,byte unit) {
        this.id = id;
        this.pro = pro;		//added by Jeremy:见BUF_STRING
        this.value = value;
        this.time = time;
        this.unit = unit;		//added by Jeremy:计算次数的buff还是计算时间的buff
    }

    public int getId(){
        return id;
    }

    public byte getProperty(){
        return pro;
    }

    public int getValue(){
        return value;
    }
    
    public void setValue(int value){
    	this.value = value;
    }

    public int getTime(){
        return time;
    }

    public void descTime(){
        time--;
    }

    public byte getUnit(){
        return unit;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
}
