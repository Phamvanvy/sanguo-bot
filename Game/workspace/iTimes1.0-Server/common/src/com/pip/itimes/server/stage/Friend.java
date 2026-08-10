package com.pip.itimes.server.stage;

public class Friend implements Comparable<Friend>{

    private int id;
    private String name;
    private int favorite;
    private long loginTime;

    public Friend(int id,String name,int favorite, long loginTime) {
        this.id = id;
        this.name = name;
        this.favorite = favorite;
        this.loginTime = loginTime;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFavorite() {
        return favorite;
    }
    
    public void setLoginTime(long loginTime){
    	this.loginTime = loginTime;
    }
    
    public long getLoginTime(){
    	return loginTime;
    }

    public int compareTo(Friend f){
        int ret = favorite - f.favorite;
        if(ret==0){
            return id - f.id;
        }
        return ret;
    }
}
