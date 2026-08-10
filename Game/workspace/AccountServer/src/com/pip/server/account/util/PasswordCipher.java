package com.pip.server.account.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 用于密码字段的加/解密。
 * @author lighthu
 */
public class PasswordCipher {
	static final String ENC_KEY = "b5a144f5";
	static SecretKey desKey;
	
	static {
		byte[] staticKey = ENC_KEY.getBytes();
		try {
			SecretKeyFactory keyfact = SecretKeyFactory.getInstance("DES");
	        DESKeySpec dks = new DESKeySpec(staticKey); 
	        desKey = keyfact.generateSecret(dks);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对一个未加密的密码进行加密，返回加密后的密码字符串。
	 * @param src
	 * @return 如果加密失败，返回null
	 */
	public static String encode(String src) {
		String str = "";
		try {
		   Cipher c1 = Cipher.getInstance("DES/ECB/PKCS5Padding");
		   c1.init(Cipher.ENCRYPT_MODE, desKey);
		   byte[] encoded = c1.doFinal(src.getBytes("UTF-8"));
		   for (int i = 0; i < encoded.length; i++) {
	           str += byteToHex(encoded[i]);
	       }
	       return str;	   
		  
		} catch (Exception e) {
		    e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 对一个加密的密码进行解密，返回解密后的密码字符串。
	 * @param dst
	 * @return 如果解密失败，返回null
	 */
	public static String decode(String dst) {
		try {
		   Cipher c1 = Cipher.getInstance("DES/ECB/PKCS5Padding");
		   c1.init(Cipher.DECRYPT_MODE, desKey);
		   byte[] ByteRequest = hexStringToByte(dst);
		   byte[] decoded = c1.doFinal(ByteRequest);
		   return new String(decoded, "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}  
	 
	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static String byteToHex(byte b) {
		char Digest[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		char[] ch = new char[2];
		ch[0] = Digest[(b >>> 4 & 0X0F)];
		ch[1] = Digest[b & 0X0F];
		return new String(ch);
	}
	
	public static void main(String[] args) {
		System.out.println(decode("2EB863787A418C32"));
	}
}
