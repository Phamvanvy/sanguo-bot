package com.pip.server.auth.cmcc;

/**
 * 平台消费记录表。
 */
public class CmccConsumeRecord {
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
     * 消费记录
     */
    private CmccConsumeItem[] items;

    public CmccConsumeRecord(int result,String userId,int startSequence,CmccConsumeItem[] items) {
        this.result = result;
        this.userId = userId;
        this.startSequence = startSequence;
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public int getStartSequence() {
        return startSequence;
    }

    public int getResult() {
        return result;
    }

    public CmccConsumeItem[] getItems() {
        return items;
    }
}
