package com.pip.server.billing.alipay;

/**
 * 安全管理类
 * 
 */
public interface SecurityManager {

    /**
     * 对原始数据进行签名
     * 
     * @param content 原始数据
     * @param algoType 算法类型
     * @param key 私钥
     * @return 签名
     * @throws Exception
     */
    public String sign(String algoType, String content, String key) throws Exception;

    /**
     * 验证签名
     * 
     * @param content 原始数据
     * @param algoType 算法类型
     * @param sign 签名
     * @param key 公钥
     * @return 签名验证通过 False 签名验证失败
     * @throws Exception
     */
    public boolean verify(String algoType, String content, String sign, String key) throws Exception;

    /**
     * 加密
     * 
     * @param content 需要加密的内容
     * @param algoType 算法类型
     * @param key 加密的key
     * @return 返回加密后的字符串
     * @throws Exception 加密失败
     */
    public String encrypt(String algoType, String content, String key) throws Exception;

    /**
     * 解密
     * 
     * @param content 需要解密的内容
     * @param algoType 算法类型
     * @param key 解密的key
     * @return 返回解密后端字符串
     * @throws Exception 解密失败
     */
    public String decrypt(String algoType, String content, String key) throws Exception;

}
