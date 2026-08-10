package com.pip.server.auth.cmcc;

/**
 * 平台消费记录项。
 */
public class CmccConsumeItem {
    /*
     * 日期时间
     */
    private String date;
    /*
     * 类型
     */
    private String type;
    /*
     * 金额
     */
    private int point;
    /*
     * 厂商代码
     */
    private String cpId;
    /*
     * 服务代码
     */
    private String cpServiceId;
    /*
     * 业务名称
     */
    private String serviceName;

    public CmccConsumeItem(String date, String type, int point, String cpId, String cpServiceId, String serviceName) {
        this.date = date;
        this.type = type;
        this.point = point;
        this.cpId = cpId;
        this.cpServiceId = cpServiceId;
        this.serviceName = serviceName;
    }

    public String getType() {
        return type;
    }

    public int getPoint() {
        return point;
    }

    public String getDate() {
        return date;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getCPID() {
        return cpId;
    }
    
    public String getCPServiceID() {
        return cpServiceId;
    }
    
    public String getDisplayText() {
        if (serviceName == null) {
            return date;
        } else {
            return date + " " + serviceName;
        }
    }
}
