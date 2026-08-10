package com.pip.server.billing.ruyifu;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class EncDec {
	SecretKey desKey;
	
	public void setKey(String key){
		byte[] staticKey = key.getBytes();
		try {
			SecretKeyFactory keyfact = SecretKeyFactory.getInstance("DES");
	        DESKeySpec dks = new DESKeySpec(staticKey); 
	        desKey = keyfact.generateSecret(dks);
		}catch(Exception e){
			e.printStackTrace();
		}		   
	}
	
	public String encode(String src) {
		String str = "";
		try{ 
		   Cipher c1 = Cipher.getInstance("DES/ECB/PKCS5Padding");
		   c1.init(Cipher.ENCRYPT_MODE, desKey);
		   byte[] encoded = c1.doFinal(src.getBytes());
		   for(int i=0;i<encoded.length;i++)
	       {
	           str += byteToHex(encoded[i]);
	       }
	       return str;	   
		  
		}catch(Exception e){
		    e.printStackTrace();
			return null;

		}
		
	}
	
	public String decode(String dst) {
		try{ 
		   
		   Cipher c1 = Cipher.getInstance("DES/ECB/PKCS5Padding");
		   c1.init(Cipher.DECRYPT_MODE, desKey);
		   byte[] ByteRequest = hexStringToByte(dst);
		   byte[] decoded = c1.doFinal(ByteRequest);
		   return new String(decoded);
		  
		}catch(Exception e){
			e.printStackTrace();
			return null;

		}
		
	}
	
	public String md5enc(String src){
		try{ 
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] dest = md5.digest(src.getBytes());
			String enc = "";
			for(int i=0;i<dest.length;i++){
				enc += byteToHex(dest[i]);
			}
			return enc.toLowerCase();
			
		}catch(Exception e){
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
	
	public static String byteToHex(byte b)
	   {
	       char Digest[] = { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	       char[]ch = new char[2];
	       ch[0] = Digest[(b>>>4&0X0F)];
	       ch[1] = Digest[b&0X0F];
	       return new String(ch);
	   }
}
