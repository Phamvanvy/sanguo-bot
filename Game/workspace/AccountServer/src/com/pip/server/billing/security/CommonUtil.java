package com.pip.server.billing.security;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.pip.server.account.IStringValidator;
import com.pip.server.account.PatternStringValidator;

import edu.emory.mathcs.backport.java.util.Arrays;

public class CommonUtil {
	
	
	protected static IStringValidator phoneValidator = new PatternStringValidator("^13[0-9]{1}[0-9]{8}|^15[0-9]{1}[0-9]{8}|^18[0-9]{1}[0-9]{8}");
	protected static IStringValidator idcardValidator = new PatternStringValidator("\\d{15}|\\d{17}[\\dXx]");;
	protected static IStringValidator mailValidator = new PatternStringValidator("^([a-z0-9A-Z]+[_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
	public static final Random seed = new Random();
	
	public static boolean validPhone(String phone){
		return phoneValidator.valid(phone) == IStringValidator.OK;
	}
	
	public static boolean validEmail(String mail){
		return mailValidator.valid(mail) == IStringValidator.OK;
	}
	
	public static boolean validIdcard(String idcard){
		return idcardValidator.valid(idcard) == IStringValidator.OK;
	}
	
//	public static String randomString(){
//		String s = UUID.randomUUID().toString();
//		String[] ss = s.split("-");
//		return ss[0]+ss[1]+ss[2]+ss[3]+ss[4];
//	}
	
	public static String randomString(){
		String s = dateFormat.format(new Date());
		int i = seed.nextInt(1000);
		return String.format(s+"%03d", i);
	}
	
	public static String blurString(String s,int start,int end){
		if(s==null)
			throw new IllegalArgumentException();
		if(start<0||start>=s.length()||end<0||end>=s.length()||end<start)
			throw new IllegalArgumentException();
		char[] cs = s.toCharArray();
		char[] blurs = new char[end-start+1];
		Arrays.fill(blurs, '*');
		System.arraycopy(blurs, 0, cs, start, blurs.length);
		return new String(cs);
	}

//	public static String getHexString(byte[] bytes) {
//		char str[] = new char[bytes.length*2];
//		int k = 0;
//		for (int i = 0; i < bytes.length; i++) {
//			byte byte0 = bytes[i];
//			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
//			str[k++] = hexDigits[byte0 & 0xf];
//		}
//		return new String(str);
//	}
	
//	 private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
//			'b', 'c', 'd', 'e', 'f' }; 
	
	public static void main(String[] args){
		char c1 = ' ';
		char c2 = ' ';
		System.out.print(c1==c2);
	}
	
	
}
