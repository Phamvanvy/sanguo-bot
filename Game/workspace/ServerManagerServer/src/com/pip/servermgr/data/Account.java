package com.pip.servermgr.data;

import java.util.HashSet;

import com.pip.security.*;
import com.pip.servermgr.Utils;

public class Account {
	public String name;
	public String password;
	public HashSet<String> roles = new HashSet<String>();
	public boolean allowModify;
	public int[][] allowIPs = new int[][] { { 0, 0 } };
	
	public boolean checkPassword(String pass) {
		return convert(pass).equals(password); 
	}
	
	private String convert(String pass) {
		if (pass.length() == 0) {
			return "";
		}
		try {
			MD5 md5 = new MD5();
			byte[] pdata = pass.getBytes("UTF-8");
			md5.update(pdata, 0, pdata.length);
			byte[] digest = md5.digest();
			return new String(Base64.encode(digest));
		} catch (Exception e) {
			return "";
		}
	}
	
	public void setPassword(String newpass) {
		password = convert(newpass);
	}
	
	public boolean hasRole(String role) {
		return roles.contains(role);
	}
	
	public boolean checkIP(String ipStr) {
		int ip = Utils.str2ip(ipStr);
		for (int i = 0; i < allowIPs.length; i++) {
			if (allowIPs[i][0] == (ip & allowIPs[i][1])) {
				return true;
			}
		}
		return false;
	}
}
