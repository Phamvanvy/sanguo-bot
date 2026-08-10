package canseereaditem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import java.util.Map;

import org.dom4j.DocumentException;
import org.eclipse.swt.widgets.Text;


public class logcmcc {
	//目标文件
	public static File preFile ;
	//分析后文件
	public static File orderFile;
	public static String fillText;
	private static Text text;
	public static void main(String[] args) throws DocumentException{

		orderFile = new File("D:/1.txt");
		try {
			FileReader reader = new FileReader(orderFile);
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
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String s:items.keySet()){
			System.out.println("playerid:"+s+" count:"+items.get(s));
		}
		
	}
	public static void consolePrintln(String s){
		if(!text.isDisposed()){
			text.insert(s);
		}
	}
	public static Map<String, Integer> qqlog = new HashMap<String, Integer>();
	public static Map<String, String> items = new HashMap<String, String>();
	
	public static File qqlogfile;
	
}
