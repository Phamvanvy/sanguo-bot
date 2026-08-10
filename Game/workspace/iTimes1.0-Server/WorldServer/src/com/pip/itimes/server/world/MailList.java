package com.pip.itimes.server.world;

import java.util.List;

public class MailList {
    private int count;
    private List list;
    public MailList(int count,List list){
        this.count = count;
        this.list = list;
    }

    public int getCount(){
        return count;
    }

    public List getList(){
        return list;
    }
}
