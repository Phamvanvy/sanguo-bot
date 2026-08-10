package com.pip.server.auth.cmcc;

/**
 * 平台充值记录条目。
 */
public class CmccChargeItem {
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

    public CmccChargeItem(String date, String type, int point) {
        this.date = date;
        this.type = type;
        this.point = point;
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
}
