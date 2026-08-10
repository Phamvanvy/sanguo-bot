package com.pip.wulin2;

import java.io.*;

import com.pip.util.DBConfig;
import com.pip.util.DBUtils;

public class Send1 {
	public static void main(String[] args) throws Exception {
		DBConfig.init(new java.io.File("config.properties"));
		String sql = "select id from tbl_userdata where playername = ?";
		DBUtils.queryInt("itimes_6", sql, new Object[] { "Áè¶ù" });
		/*FileReader fr = new FileReader("c:/a.txt");
		BufferedReader bf = new BufferedReader(fr);
		String line;
		while ((line = bf.readLine()) != null) {
			String[] secs = line.trim().split("\\s+");
		}*/
	}
}
