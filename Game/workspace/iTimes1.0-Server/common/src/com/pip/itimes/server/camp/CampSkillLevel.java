package com.pip.itimes.server.camp;

public class CampSkillLevel{
    private int level;
    private int parm1;
    private int parm2;
    private int upgrade;
    private int maint;
    private int destroy;

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getParm1(){
        return parm1;
    }

    public void setParm1(int parm1){
        this.parm1 = parm1;
    }

    public int getParm2(){
        return parm2;
    }

    public void setParm2(int parm2){
        this.parm2 = parm2;
    }

    public int getUpgrade(){
        return upgrade;
    }

    public void setUpgrade(int upgrade){
        this.upgrade = upgrade;
    }

    public int getMaint(){
        return maint;
    }

    public void setMaint(int maint){
        this.maint = maint;
    }

    public int getDestroy(){
        return destroy;
    }

    public void setDestroy(int destroy){
        this.destroy = destroy;
    }
}