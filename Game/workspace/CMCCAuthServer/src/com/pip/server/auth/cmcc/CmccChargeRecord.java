package com.pip.server.auth.cmcc;

/**
 * 平台充值记录表。
 */
public class CmccChargeRecord {
    /*
     * 成功/失败
     */
    private int result;
    /*
     * 平台用户ID
     */
    private String userId;
    /*
     * 起始记录号
     */
    private int startSequence;
    /*
     * 返回记录
     */
    private CmccChargeItem[] items;

    public CmccChargeRecord(int result, String userId, int startSequence, CmccChargeItem[] items) {
        this.result = result;
        this.userId = userId;
        this.startSequence = startSequence;
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public int getResult() {
        return result;
    }

    public CmccChargeItem[] getItems() {
        return items;
    }

    public int getStartSequence() {
        return startSequence;
    }
}
