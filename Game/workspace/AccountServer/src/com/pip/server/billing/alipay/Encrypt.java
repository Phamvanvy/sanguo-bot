package com.pip.server.billing.alipay;

/**
 * 加密接口类 
 */
public interface Encrypt {

    /**
     * 加密
     * 
     * @param content 需要加密的内容
     * @param key 加密的key
     * @return 返回加密后的字符串
     * @throws Exception 加密失败
     */
    public String encrypt(String content, String key) throws Exception;

    /**
     * 解密
     * 
     * @param content 需要解密的内容
     * @param key 解密的key
     * @return 返回解密后端字符串
     * @throws Exception 解密失败
     */
    public String decrypt(String content, String key) throws Exception;

}
