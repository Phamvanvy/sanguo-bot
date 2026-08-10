package canseereaditem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class testjeapi {
	public testjeapi() {
		
	}
	public static void change_value() {//将xls里的 ${GetItemCount(446)}中误翻译的变量改回来

		String[] sheets = {"2011_01_28_1","2011_01_28_2","2011_03_03_1",
							"2011_03_04_1","2011_03_04_2","2011_03_15_1","2011_03_15_2",
							"2011_03_26_1","2011_03_28_1"};		
		WritableWorkbook workbook = null;
		String error = "";
		int count = 0;
		try {
			Workbook wb = Workbook.getWorkbook(new File("D:/tmp/messages.xls"));
			workbook = Workbook.createWorkbook(new File(
					"D:/tmp/messages.xls"),wb);
			for (int j = 0; j < sheets.length; j++) {
				WritableSheet  sheet = workbook.getSheet(sheets[j]);
				int rows = sheet.getRows();
				for (int i = 0; i < rows; i++) {
					String y0 = sheet.getCell(0, i).getContents();
					error = y0;
					String y1 = sheet.getCell(1, i).getContents();
					int flag = 0;
					int startIndex = y0.indexOf("${");
					while(startIndex > 0) {
						try {
							int charLength = "${".length();
							String item = y0.substring(startIndex + charLength, y0.length());
							int endIndex = item.indexOf("}");
							String keyword = item.substring(0, endIndex);//获得${}中关键字
							
							int startout = y1.indexOf("${");
							String outitem = y1.substring(startout + charLength, y1.length());
							int endout = outitem.indexOf("}");
							String keywordy1 = outitem.substring(0, endout);//获得${}中关键字
							String tmp_str = y1.substring(0, startout) + "${" + keyword
									+ y1.substring(startout + endout + charLength);
							y1 = tmp_str;
							startIndex = y0.indexOf("${", startIndex+endIndex);
							flag = 1;
						} catch (Exception ex) {
							System.out.println("---"+sheets[j]+"--"+i+"--" + error);
							startIndex = 0;
							flag = 0;
						}
					}
					if (flag == 1) {
						Label lbl = new Label(1, i, y1);
						sheet.addCell(lbl);
					}
					if (startIndex < 0) {
						continue;
					}else{
						count++;
					}
					
				}
			}
			
			System.out.println(count);
			workbook.write();
		} catch (Exception ex) {
			System.out.println("--------" + error);
			ex.printStackTrace();
		} finally{
			if(workbook != null) {
				try {
					workbook.close();
				} catch (WriteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
  public static void main(String[] args) {
	  findString("abcde","cdefghijkl");
  }
  public static void chang_Quest_Variable() {//将xls里的所有Quest Variable的第二列都恢复
	  
		String[] sheets = {"2011_04_07_1","2011_03_14_2","2011_03_14_1",
							"2011_1_13_1","2011_01_12_3","2011_01_12_2","2011_01_12_1",
							"2011_01_10_1","2010_12_23_2","2010_12_23_1","2010_11_24_1",
							"2010_11_19_1","2010_10_28_1","2010_10_09_2","2010_10_09_1",
							"2010_09_02_2","2010_09_02_1","Locations","Skills","Titles","NPC"};		
		WritableWorkbook workbook = null;
		String error = "";
		int count = 0;
		try {
			Workbook wb = Workbook.getWorkbook(new File("D:/tmp/messages.xls"));
			workbook = Workbook.createWorkbook(new File(
					"D:/tmp/messages.xls"),wb);
			for (int j = 0; j < sheets.length; j++) {
				WritableSheet  sheet = workbook.getSheet(sheets[j]);
				int rows = sheet.getRows();
				for (int i = 0; i < rows; i++) {
					String y0 = sheet.getCell(0, i).getContents();
					error = y0;
					String y1 = sheet.getCell(1, i).getContents();
					String y2 = sheet.getCell(2, i).getContents();
					if ("Quest Variable".equalsIgnoreCase(y2)){
						Label lbl = new Label(1, i, y0);
						sheet.addCell(lbl);
						count ++;
					}
					
				}
			}
			
			System.out.println(count);
			workbook.write();
		} catch (Exception ex) {
			System.out.println("--------" + error);
			ex.printStackTrace();
		} finally{
			if(workbook != null) {
				try {
					workbook.close();
				} catch (WriteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
  public static void Delete_as() {//将xls里的所有*.as文件删掉
	  
		String[] sheets = {"2011_04_07_1","2011_03_14_2","2011_03_14_1",
							"2011_1_13_1","2011_01_12_3","2011_01_12_2","2011_01_12_1",
							"2011_01_10_1","2010_12_23_2","2010_12_23_1","2010_11_24_1",
							"2010_11_19_1","2010_10_28_1","2010_10_09_2","2010_10_09_1",
							"2010_09_02_2","2010_09_02_1","Locations","Skills","Titles","NPC"};		
		WritableWorkbook workbook = null;
		String error = "";
		int count = 0;
		try {
			Workbook wb = Workbook.getWorkbook(new File("D:/tmp/messages.xls"));
			workbook = Workbook.createWorkbook(new File(
					"D:/tmp/messages.xls"),wb);
			for (int j = 0; j < sheets.length; j++) {
				WritableSheet  sheet = workbook.getSheet(sheets[j]);
				int rows = sheet.getRows();
				for (int i = 0; i < rows; i++) {
					String y0 = sheet.getCell(0, i).getContents();
					error = y0;
					String y1 = sheet.getCell(1, i).getContents();
					String y2 = sheet.getCell(2, i).getContents();
					int startIndex = y2.indexOf(".as");
					if (startIndex>0){
						Label lbl = new Label(0, i, "");
						sheet.addCell(lbl);
						lbl = new Label(1, i, "");
						sheet.addCell(lbl);
						lbl = new Label(2, i, "");
						sheet.addCell(lbl);
						lbl = new Label(3, i, "");
						sheet.addCell(lbl);
						count ++;
						System.out.println(y2);
					}
					
				}
			}
			
			System.out.println(count);
			workbook.write();
		} catch (Exception ex) {
			System.out.println("--------" + error);
			ex.printStackTrace();
		} finally{
			if(workbook != null) {
				try {
					workbook.close();
				} catch (WriteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
  
  public static HashMap findString(String str1, String str2){
		HashMap<Character, Character> map = new HashMap<Character, Character>();
		boolean maxFlag = str1.length() > str2.length();
		int minLength = (maxFlag? str2.length(): str1.length());
		for(int i = 0; i < (maxFlag? str1.length(): str2.length()); i++){
			if(i < minLength){
				char c = str1.charAt(i);
				map.put(c, c);
				c = str2.charAt(i);
				map.put(c, c);
			}else{
				if(maxFlag){
					char c = str1.charAt(i);
					map.put(c, c);
				}else{
					char c = str2.charAt(i);
					map.put(c, c);
				}
			}
		} 
		Iterator iter = map.keySet().iterator(); 
		while (iter.hasNext()) { 
		    Object key = iter.next(); 
		    Object val = map.get(key); 
		    System.out.println("key:" + key + ",value:" + val );
		}

		return map;
	}
}
