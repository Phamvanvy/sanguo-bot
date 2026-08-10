package canseereaditem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentException;



public class test {
	public static void main() throws Exception{

		try {
			FileReader reader = new FileReader( new File("D:/1.txt"));
			BufferedReader br = new BufferedReader(reader);
			String s = null;
			String tmps = "1";
			Map<String,Integer> userid = new HashMap<String,Integer>();
			try {
				while ((s = br.readLine()) != null) {
					// tmpes = tmpes.append(s);
					tmps = s;
					tmps = tmps.trim();
					int startIndex = s.lastIndexOf("MID[");
					if (startIndex < 0){
						continue;
					}
					int charLength = "MID[".length();
					String item = s.substring(startIndex + charLength, s.length());
					int endIndex = item.indexOf("]SRC[");
					if (endIndex < 0){
						continue;
					}
					String user_id = item.substring(0, endIndex);
					if (!userid.containsKey(user_id)){
						userid.put(user_id, 1);
					}else{
						int count = userid.get(user_id)+1;
						userid.remove(user_id);
						userid.put(user_id, count);
					}
					
					
				}
				System.out.println("count:"+userid.size());
				for (String str : userid.keySet()) {
					if (userid.get(str) > 2)
					System.out.println(str+":"+userid.get(str));
		        }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		for(String s:items.keySet()){
//			System.out.println("playerid:"+s+" count:"+items.get(s));
//		}
	}
	private static boolean checkString(String s, boolean allowColon) {
		if (s == null) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			boolean isValid = false;
			if (ch >= 'a' && ch <= 'z') {
				isValid = true;
			} else if (ch >= 'A' && ch <= 'Z') {
				isValid = true;
			} else if (ch >= '0' && ch <= '9') {
				isValid = true;
			} else if (ch == '_') {
				isValid = true;
			} else if (ch >= 0x4E00 && ch <= 0x9FA5) {
				isValid = true;
			} else if (true){
				System.out.print("name = " + ch);
				if (ch >= 0x00C0 && ch <= 0x1EF9) {
					isValid = true;
				}
			} else if (allowColon && ch == ':') {
				isValid = true;
			}
			if (!isValid) {
				return false;
			}
		}
		return true;
	}
	public static void rename(File dir,String oldPrefix,String newPrefix){
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if(fs[i].getName().startsWith(oldPrefix)){
				String path = fs[i].getParent();
				String name = fs[i].getName();
				String newName = newPrefix.concat(name.substring(oldPrefix.length()));
				int tmp = 99999 / i;
				while(tmp / 10 > 0){
					newName += "0";
					tmp /= 10;
				}
				newName += i;
				fs[i].renameTo(new File(path,newName));
			}
		}
	}

	
}
