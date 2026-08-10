package com.pip.itimes.server.bean;

import java.util.Date;

public class Blog {

    private int id;
    private int playerId;
    private String playerName;
    private String content;
    private String title;
    private Date createTime;
    private int readedTimes;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setReadedTimes(int readedTimes) {
        this.readedTimes = readedTimes;
    }



    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }



    public String getTitle() {
        return title;
    }

    public int getReadedTimes() {
        return readedTimes;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getId() {
        return id;
    }

}
