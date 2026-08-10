package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

public class Effect_AntiDamage extends EffectConfig {

    private int type;
    private int[] timeDis = new int[0];
    private int[] ratio = new int[0];
    
    public Effect_AntiDamage(int type){
        this.type = type;
    }
    
    @Override
    public Object getParam(int index) {
        switch (index) {
        case 0:
            return timeDis;
        case 1:
            return ratio;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Class getParamClass(int index) {
        switch (index) {
        case 0:
            return Integer.class;
        case 1:
            return Integer.class;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getParamCount() {
        return 2;
    }

    @Override
    public String getParamName(int index) {
        switch (index) {
        case 0:
            return "º‰∏Ù(√Î)";
        case 1:
           return "∏≈¬ (%)";
        }
        throw new IllegalArgumentException();
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setLevelCount(int max) {
        timeDis = Utils.realloc(timeDis, max);
        ratio = Utils.realloc(ratio, max);
    }

}
