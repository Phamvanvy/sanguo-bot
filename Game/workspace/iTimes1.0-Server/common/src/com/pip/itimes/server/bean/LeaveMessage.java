package com.pip.itimes.server.bean;

import java.util.Date;

public class LeaveMessage {

    private int id;
    private int sourceId;
    private String sourceName;
    private int ownerId;
    private String content;
    private String title;
    private Date createTime;

    public LeaveMessage() {
    }

    public String getSourceName() {
        return sourceName;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }
}
