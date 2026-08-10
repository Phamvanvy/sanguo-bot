package com.pip.itimes.server.bean;

import java.util.Date;


/**
 * 
 */

public class Bbs{


    // Fields    

     private int id;
     private int bbsId;
     private int playerId;
     private String playerName;
     private String title;
     private String content;
     private Date postTime;
     private int priority;


    public Bbs() {
    }


    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getBbsId(){
    	return this.bbsId;
    }
    
    public void setBbsId(int bbsId){
    	this.bbsId = bbsId;
    }
    
    public int getPlayerId() {
        return this.playerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return this.playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }

    public Date getPostTime() {
        return this.postTime;
    }
    
    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public int getPriority() {
        return this.priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }

}