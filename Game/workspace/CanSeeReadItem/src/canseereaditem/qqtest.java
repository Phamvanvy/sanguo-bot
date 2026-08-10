package canseereaditem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class qqtest {
	//目标文件
	public static File preFile ;
	//分析后文件
	public static File orderFile;
	public static String fillText;
	private static Text text;
	public static void main(String[] args) throws DocumentException{

		orderFile = new File("D:/workspace/CanSeeReadItem/equ.xml");
		try {
			FileReader reader = new FileReader(orderFile);
			BufferedReader br = new BufferedReader(reader);
			String s = null;
			String tmps = "";
			try {
				while ((s = br.readLine()) != null) {
					// tmpes = tmpes.append(s);
					tmps = s;
					tmps = tmps.trim();
					int startIndex = s.lastIndexOf("itemLevel=\"");
					if (startIndex < 0){
						continue;
					}
					int charLength = "itemLevel=\"".length();
					String item = s.substring(startIndex + charLength, s.length());
					int endIndex = item.indexOf("\" requireLevel");
					if (endIndex < 0){
						continue;
					}
					String itemLevel = item.substring(0, endIndex);
					
					startIndex = s.lastIndexOf("requireLevel=\"");
					charLength = "requireLevel=\"".length();
					item = s.substring(startIndex + charLength, s.length());
					endIndex = item.indexOf("\" createType");
					String requireLevel = item.substring(0, endIndex);
					
					startIndex = s.lastIndexOf("title=\"");
					charLength = "title=\"".length();
					item = s.substring(startIndex + charLength, s.length());
					endIndex = item.indexOf("\" itemLevel");
					String title = item.substring(0, endIndex);
					
					if (!itemLevel.equalsIgnoreCase(requireLevel)){
						if (items.containsKey(title)){
							String count = items.get(title);
							items.remove(title);
							items.put(title, requireLevel);
						}else{
							items.put(title, requireLevel);
						}
					}
					
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
			System.out.println("itemtitle:"+s+" requireLevel:"+items.get(s));
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
