package com.pip.server.auth.bean;

import java.util.Date;

/**
 * 推荐成功记录。
 * @author lighthu
 */
public class RecommendRecord implements java.io.Serializable {
    private int id;
    private String source;
    private String sourceRegion;
    private String target;
    private String targetRegion;
    private Date finishTime;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getSourceRegion() {
        return sourceRegion;
    }
    public void setSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getTargetRegion() {
        return targetRegion;
    }
    public void setTargetRegion(String targetRegion) {
        this.targetRegion = targetRegion;
    }
    public Date getFinishTime() {
        return finishTime;
    }
    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }
}
