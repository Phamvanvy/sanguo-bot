package com.pip.itimes.server.stage;

public class KeysEffect extends Effect {

    private int count;
    private int group1;
    private int group2;
    private int boxId;

    public KeysEffect(int group1,int group2,int boxId,int count) {
        this.group1 = group1;
        this.group2 = group2;
        this.boxId = boxId;
        this.count = count;
    }

    public int getGroup1(){
        return group1;
    }

    public int getGroup2(){
        return group2;
    }

    public int getBoxId(){
        return boxId;
    }

    public int getCount(){
        return count;
    }

    public byte getType() {
        return 44;
    }
}
