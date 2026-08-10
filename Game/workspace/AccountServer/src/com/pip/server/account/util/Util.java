package com.pip.server.account.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

public class Util {
	 private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f' }; 
	
    public static boolean verifyMD5(String s,String md5String){
    	try {
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			byte[] bytes = s.getBytes();
			MD5.update(bytes,0,bytes.length);
			String digest = getHexString(MD5.digest());
			if(digest.equalsIgnoreCase(md5String)){
			    return true;
			}else{
			    return false;
			}
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
    }
    
    /**
     * 验证带md5的密码，有部分密码是通过md5存储的，这部分密码的开始是字符#
     * @param p1 原始密码
     * @param p2 用户密码
     * @return 如果正确返回
     */
    public static boolean verifyPassword(String p1,String p2){
    	if(p1.equals(p2))
    		return true;
    	if(verifyMD5(p2,p1.substring(1)))
    		return true;
    	return false;
    }
    
	public static String getHexString(byte[] bytes) {
		char str[] = new char[bytes.length*2];
		int k = 0;
		for (int i = 0; i < bytes.length; i++) {
			byte byte0 = bytes[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}
	
    public static boolean inLaterMonth(Date checkDate, Date now) {
        if (checkDate == null) {
            return false;
        }
        Calendar current = Calendar.getInstance();
        Calendar last = Calendar.getInstance();

        current.setTime(now);
        last.setTime(checkDate);
        int m1 = last.get(Calendar.YEAR) * 100 + last.get(Calendar.MONTH);
        int m2 = current.get(Calendar.YEAR) * 100 + current.get(Calendar.MONTH);
        return m1 >= m2;
    }
    
    public static String objectToString(Object obj) {
        try {
            if (obj == null) {
                return "";
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            String ret = new sun.misc.BASE64Encoder().encode(bos.toByteArray());
            ret = ret.replace("\n", "");
            ret = ret.replace("\r", "");
            return ret;
        } catch (Exception e) {
            return "";
        }
    }
    
    /** 把String转换为对象。 */
    public static Object stringToObject(String str) {
        try {
            if (str == null || str.length() == 0) {
                return null;
            }
            byte[] data = new sun.misc.BASE64Decoder().decodeBuffer(str);
            return new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
        }catch (Exception e) {
            return null;
        }
    }    
}
