package com.pip.server.auth.cmcc;

/**
 * 短信购买道具请求。
 */
public class CmccSmsBuyReq {
    /*
     * 购买token（16位）
     */
    public String token;
    /*
     * 世界服务器请求ID
     */
    public int requestId;
    /*
     * 用户ID
     */
    public String userId;
    /*
     * 游戏服务器ID
     */
    public String serverId;
    /*
     * 帐号ID
     */
    public int accountId;
    /*
     * 角色ID
     */
    public int playerId;
    /*
     * 消费代码
     */
    public String consumeCode;
}
