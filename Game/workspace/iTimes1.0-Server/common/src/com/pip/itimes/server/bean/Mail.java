package com.pip.itimes.server.bean;

import java.util.Date;



public class Mail{

     private int id;
     private int sourceId;
     private String sourceName;
     private int destId;
     private String destName;
     private String title;
     private String content;
     private byte[] attachment;
     private int price;
     private Date postTime;
     private boolean readed;
     private Date validTime;

    public Mail() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return this.sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public int getDestId() {
        return this.destId;
    }

    public void setDestId(int destId) {
        this.destId = destId;
    }

    public void setDestName(String name){
        this.destName = name;
    }

    public String getDestName(){
        return destName;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if(title != null && title.length() > 255){
            this.title = title.substring(0, 255);
        }else{
            this.title = title;
        }
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getAttachment() {
        return this.attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Date getPostTime() {
        return this.postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public boolean getReaded() {
        return this.readed;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }


    public Date getValidTime(){
        return validTime;
    }

    public void setValidTime(Date validTime){
        this.validTime = validTime;
    }


}
