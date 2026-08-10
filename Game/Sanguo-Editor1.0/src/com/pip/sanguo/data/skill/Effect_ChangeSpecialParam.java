package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

public class Effect_ChangeSpecialParam extends EffectConfig{
    private int type;
    private int[][] value = new int[4][0];
    private float[][] percent = new float[4][0];
    private String[] name = {"沉默","恐惧","麻痹","定身"};
    
    public Effect_ChangeSpecialParam(int t){
        type = t;
    }
    @Override
    public Object getParam(int index) {
        int group = index / 2;
        index = index % 2;
        switch (index) {
        case 0:
            return value[group];
        case 1:
            return percent[group];
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Class getParamClass(int index) {
        index = index % 2;
        switch (index) {
        case 0:
            return Integer.class;
        case 1:
            return Float.class;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getParamCount() {
        return 8;
    }

    @Override
    public String getParamName(int index) {
        int group = index / 2;
        index = index % 2;
        switch (index) {
        case 0:
            return name[group]+"数额" ;
        case 1:
            return name[group]+"百分比";
        }
        throw new IllegalArgumentException();
    }
           

    @Override
    public int getType() { 
        return type;
    }

    @Override
    public void setLevelCount(int max) {
        for (int i = 0; i < 4; i++) {
            value[i] = Utils.realloc(value[i], max);
            percent[i] = Utils.realloc(percent[i], max);
        }
    }

}
