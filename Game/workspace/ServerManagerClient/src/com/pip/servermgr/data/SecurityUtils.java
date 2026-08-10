package com.pip.servermgr.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.pip.security.*;

public class SecurityUtils {
	private static String SESSION_KEY = "laoutqqhd9272l;javnzy220";
	private static long serverTime;
	private static long localTime;
	
	/**
	 * 设置服务器参照时间，这个时间用于估算服务器准确时间。
	 */
	public static void updateServerTime(long t) {
		serverTime = t;
		localTime = System.currentTimeMillis();
	}
	
	/**
	 * 取得当前估算的服务器时间。
	 */
	public static long getServerTime() {
		return System.currentTimeMillis() - localTime + serverTime;
	}
	
	/**
	 * 对密钥数据进行奇偶校验处理，返回结果。
	 * @param key
	 * @return
	 */
    public static byte[] fixUpKey(byte[] key) {
        int b;
        for (int i = 0; i < key.length; i++) {
            b = key[i];
            key[i] = (byte)((b & 0xFE) |
                              (((b >> 1) ^
                                (b >> 2) ^
                                (b >> 3) ^
                                (b >> 4) ^
                                (b >> 5) ^
                                (b >> 6) ^
                                (b >> 7)) & 0x01));
        }
        return key;
    }
    
    /**
     * 把一个long型数据转换为byte数组。
     * @return
     */
    private static byte[] longToBytes(long value) {
    	byte[] ret = new byte[8];
    	ret[0] = (byte)(value >> 56);
    	ret[1] = (byte)(value >> 48);
    	ret[2] = (byte)(value >> 40);
    	ret[3] = (byte)(value >> 32);
    	ret[4] = (byte)(value >> 24);
    	ret[5] = (byte)(value >> 16);
    	ret[6] = (byte)(value >> 8);
    	ret[7] = (byte)value;
    	return ret;
    }
    
    /**
     * 把一个byte数组转换为long
     * @param data
     * @return
     */
    private static long bytesToLong(byte[] data) {
    	try {
    		return new DataInputStream(new ByteArrayInputStream(data)).readLong();
    	} catch (Exception e) {
    		return 0;
    	}
    }

	/**
	 * 用内置的密钥对密码进行加密处理。
	 * @param data
	 * @return
	 */
	public static String encryptPassword(String pass) throws Exception {
        // 组装数据
		byte[] passData = pass.getBytes("UTF-8");
        byte[] timeData = longToBytes(getServerTime());
        MD5 md5 = new MD5();
        md5.update(passData, 0, passData.length);
        md5.update(timeData, 0, timeData.length);
        byte[] digest = md5.digest();
        byte[] rawData = new byte[passData.length + timeData.length + digest.length];
        System.arraycopy(passData, 0, rawData, 0, passData.length);
        System.arraycopy(timeData, 0, rawData, passData.length, timeData.length);
        System.arraycopy(digest, 0, rawData, passData.length + timeData.length, digest.length);

        // 加密
        byte[] sessionKeyData = SESSION_KEY.getBytes();
        RawSecretKey sessionKey = new RawSecretKey("RAW", fixUpKey(sessionKeyData));
        TripleDES_CBC_PKCS5 cipher = new TripleDES_CBC_PKCS5();
        cipher.init(TripleDES_CBC_PKCS5.ENCRYPT_MODE, sessionKey);
        byte[] encData = cipher.doFinal(rawData, 0, rawData.length);
        
        // 结果转换为BASE64
        return new String(Base64.encode(encData));
	}
	
	/**
	 * 用内置的密钥对密码进行解密处理。
	 */
	public static String decryptPassword(String token) throws Exception {
		// BASE64解码
		byte[] encData = Base64.decode(token);
		
		// 解密
        byte[] sessionKeyData = SESSION_KEY.getBytes();
        RawSecretKey sessionKey = new RawSecretKey("RAW", fixUpKey(sessionKeyData));
        TripleDES_CBC_PKCS5 cipher = new TripleDES_CBC_PKCS5();
        cipher.init(TripleDES_CBC_PKCS5.DECRYPT_MODE, sessionKey);
        byte[] rawData = cipher.doFinal(encData, 0, encData.length);
        
        // 验证MD5
        if (rawData.length < 24) {
        	throw new Exception();
        }
        MD5 md5 = new MD5();
        md5.update(rawData, 0, rawData.length - 16);
        byte[] digest = md5.digest();
        for (int i = 0; i < 16; i++) {
        	if (digest[i] != rawData[rawData.length - 16 + i]) {
        		throw new Exception();
        	}
        }
        
        // 验证timestamp
        byte[] timeData = new byte[8];
        System.arraycopy(rawData, rawData.length - 24, timeData, 0, 8);
        long timestamp = bytesToLong(timeData);
        long gap = timestamp - System.currentTimeMillis();
        if (gap < -60000 && gap > 60000) {
        	// 正负一分钟算合法
        	throw new Exception();
        }
        
        // 取出密码
        return new String(rawData, 0, rawData.length - 24, "UTF-8");
	}
}
