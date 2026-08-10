package com.pip.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

/**
 * 一个附加于Text输入框的小工具类，使Text输入框获得输入焦点时自动选中全部文本。
 * @author lighthu
 */
public class AutoSelectAll implements FocusListener {
    public static AutoSelectAll instance = new AutoSelectAll();
    public void focusGained(FocusEvent e) {
        if (e.getSource() instanceof Text) {
            ((Text)e.getSource()).selectAll();
        }
    }
    public void focusLost(FocusEvent e) {}
    
    public static void main(String[] args) throws Exception {
    	String src = "c:/2.log";
    	FileReader fr = new FileReader(src);
    	BufferedReader br = new BufferedReader(fr);
    	String line;
    	
    	while ((line = br.readLine()) != null) {
    		if (line.trim().length() == 0) {
    			continue;
    		}
    		String[] sec = line.split("\t");
    		System.out.print(sec[0] + "\t" + sec[1] + "\t");
    		System.out.println(convertOperator(sec[2]));
    	}
    	fr.close();
    }
    
    public static String convertOperator(String op) {
    	if (op.contains("移动")) {
    		return "移动";
    	} else if (op.contains("网通") || op.contains("联通")) {
    		return "联通";
    	} else if (op.contains("电信")) {
    		return "电信";
    	} else if (op.contains("铁通")) {
    		return "铁通";
    	} else if (op.contains("未知")) {
    		return "未知";
    	} else if (op.contains("美国") || op.contains("加拿大") || op.contains("欧洲") || op.contains("德国") ||
    			op.contains("英国") || op.contains("IANA") || op.contains("法国") || op.contains("南非") || 
    			op.contains("巴西 ") || op.contains("俄罗斯") || op.contains("黑山") || op.contains("巴拿马") ||
    			op.contains("澳大利亚") || op.contains("意大利") || op.contains("委内瑞拉") || op.contains("荷兰") || 
    			op.contains("斯洛伐克") || op.contains("阿根廷")) {
    		return "欧美";
    	} else if (op.contains("印度") || op.contains("香港") || op.contains("韩国") || op.contains("日本") ||
    			op.contains("马来西亚") || op.contains("哈萨克斯坦") || op.contains("新加坡") || op.contains("越南") ||
    			op.contains("澳门")) {
    		return "亚洲";
    	} else {
    		return "国内其他网络";
    	}
    }
    
//    public static void main(String[] args) throws Exception {
//    	String src = "c:/ip.log";
//    	FileReader fr = new FileReader(src);
//    	BufferedReader br = new BufferedReader(fr);
//    	String line;
//    	
//    	HashMap<String, Integer> countMap = new HashMap<String, Integer>();
//    	while ((line = br.readLine()) != null) {
//    		if (line.trim().length() == 0) {
//    			continue;
//    		}
//    		int pos1 = line.indexOf("/") + 1;
//    		if (pos1 == 0) {
//    			continue;
//    		}
//    		int pos2 = line.length();
//    		// pos2 = line.lastIndexOf(".", pos2 - 1);
//    		String ip = line.substring(pos1, pos2);
//    		if (countMap.containsKey(ip)) {
//    			countMap.put(ip, countMap.get(ip) + 1);
//    		} else {
//    			countMap.put(ip, 1);
//    		}
//    	}
//    	fr.close();
//    	for (String ip : countMap.keySet()) {
//    		System.out.println(ip + "\t" + countMap.get(ip) + "\t" + getIPSource(ip));
//    		try {
//    			Thread.sleep(300);
//    		} catch (Exception e) {
//    		}
//    	}
//    }
//    
//    public static String getIPSource(String ip) throws Exception {
//    	HttpURLConnection conn = null;
//    	try {
//    		conn = (HttpURLConnection)(new URL("http://www.ip138.cn/index.php?ip=" + ip).openConnection());
//    		int code = conn.getResponseCode();
//    		InputStream is = conn.getInputStream();
//    		byte[] buf = new byte[20480];
//    		int len = 0;
//    		while (true) {
//    			int thisLen = is.read(buf, len, 20480 - len);
//    			if (thisLen <= 0) {
//    				break;
//    			} else {
//    				len += thisLen;
//    			}
//    		}
//    		is.close();
//    		String str = new String(buf, 0, len, "GBK");
//    		int pos = str.indexOf("查 询 结 果<br><br>");
//    		if (pos == -1) {
//    			return "查询错误";
//    		}
//    		pos += "查 询 结 果<br><br>".length();
//    		pos = str.indexOf("<br>", pos) + 4;
//    		int pos2 = str.indexOf("<br>", pos);
//    		return str.substring(pos, pos2);
//    	} finally {
//    		if (conn != null) {
//    			conn.disconnect();
//    		}
//    	}
//    }
    
    /*(public static void main(String[] args) throws Exception {
    	File src = new File("c:/Users/Lighthu/Desktop/temp.log");
    	FileReader fr = new FileReader(src);
    	BufferedReader br = new BufferedReader(fr);
    	String line;
    	
    	HashMap<Integer, Integer> balanceFinal = new HashMap<Integer, Integer>();
    	HashMap<Integer, Integer> tbalFinal = new HashMap<Integer, Integer>();
    	
    	while ((line = br.readLine()) != null) {
    		line = line.trim();
    		if (line.length() == 0) {
    			continue;
    		}
    		if (!line.contains("MODIFY_TSCORE")) {
    			continue;
    		}
    		int id = Integer.parseInt(getParam(line, "]ID"));
    		int add = Integer.parseInt(getParam(line, "]ADD"));
    		int old = Integer.parseInt(getParam(line, "]OLD"));
    		int newv = Integer.parseInt(getParam(line, "]NEW"));
    		System.out.println(id + "\t" + add + "\t" + old + "\t" + newv);
    	}
    	fr.close();
    }
    */
    /*public static void main(String[] args) throws Exception {
    	File src = new File("c:/Users/Lighthu/Desktop/temp.log");
    	FileReader fr = new FileReader(src);
    	BufferedReader br = new BufferedReader(fr);
    	String line;
    	
    	HashMap<Integer, Integer> balanceFinal = new HashMap<Integer, Integer>();
    	HashMap<Integer, Integer> tbalFinal = new HashMap<Integer, Integer>();
    	
    	while ((line = br.readLine()) != null) {
    		line = line.trim();
    		if (line.length() == 0) {
    			continue;
    		}
    		String[] secs = line.split(",");
    		int id = Integer.parseInt(secs[0]);
    		int bal = Integer.parseInt(secs[1]);
    		int tbal = Integer.parseInt(secs[2]);
    		balanceFinal.put(id, bal);
    		tbalFinal.put(id, tbal);
    	}
    	fr.close();
    	
    	src = new File("c:/Users/Lighthu/Desktop/addscore.log");
    	fr = new FileReader(src);
    	br = new BufferedReader(fr);
    	
    	HashMap<Integer, Integer> balanceMap = new HashMap<Integer, Integer>();
    	HashMap<Integer, Integer> tbalMap = new HashMap<Integer, Integer>();
    	HashSet<Integer> errorIDs = new HashSet<Integer>();
    	
    	while ((line = br.readLine()) != null) {
    		line = line.trim();
    		if (line.length() == 0) {
    			continue;
    		}
    		if (line.contains("ADD_SCORE")) {
	    		int id = Integer.parseInt(getParam(line, "]ID"));
	    		int balance = Integer.parseInt(getParam(line, "]BALANCE"));
	    		int tbal = Integer.parseInt(getParam(line, "]TBAL"));
	    		int value = Integer.parseInt(getParam(line, "]VALUE"));
	    		
	    		// 校验本条之前的余额和上一次记录的余额是否一致
	    		if (balanceMap.containsKey(id) && balanceMap.get(id) != balance - value) {
	    			// 不一致，记录回档金额日志
//	    			System.out.println(id + ", " + (balanceMap.get(id) - (balance - value)) + ", ");
//	    			System.out.println("回档ID[" + id + "]BALANCE[" + (balanceMap.get(id) - (balance - value)) + "]");
	    			errorIDs.add(id);
	    		}
	    		if (tbalMap.containsKey(id) && tbalMap.get(id) != tbal - value) {
//	    			System.out.println(id + ", " + (tbalMap.get(id) - (tbal - value)) + ", ");
	    			// System.out.println("回档ID[" + id + "]TBAL[" + (tbalMap.get(id) - (tbal - value)) + "]");
	    			errorIDs.add(id);
	    		}
	    		balanceMap.put(id, balance);
	    		tbalMap.put(id, tbal);
    		} else if (line.contains("DEC_SCORE")) {
    			int id = Integer.parseInt(getParam(line, "]ID"));
	    		int balance = Integer.parseInt(getParam(line, "]BALANCE"));
	    		int value = Integer.parseInt(getParam(line, "]VALUE"));
	    		
	    		// 校验本条之前的余额和上一次记录的余额是否一致
	    		if (balanceMap.containsKey(id) && balanceMap.get(id) != balance + value) {
	    			// 不一致，记录回档金额日志
//	    			System.out.println(id + ", " + (balanceMap.get(id) - (balance - value)) + ", ");
//	    			System.out.println("回档ID[" + id + "]BALANCE[" + (balanceMap.get(id) - (balance + value)) + "]");
	    			errorIDs.add(id);
	    		}
	    		balanceMap.put(id, balance);
    		}
    	}
    	fr.close();
    	
    	// 最后再查找一次所有和结果不匹配的
    	for (int id : balanceMap.keySet()) {
    		int balance = balanceMap.get(id);
    		int tbal = tbalMap.containsKey(id) ? tbalMap.get(id) : tbalFinal.get(id);
    		int rbalance = balanceFinal.containsKey(id) ? balanceFinal.get(id) : 0;
    		int rtbal = tbalFinal.containsKey(id) ? tbalFinal.get(id) : 0;
//    		if (balance != rbalance) {
//    			if (errorIDs.contains(id)) {
//    				System.out.println("FATAL ERROR " + id);
//    			}
//    			System.out.println(id + ", " + (balance - rbalance) + ", " );
//    		}
    		if (tbal != rtbal) {
    			if (errorIDs.contains(id)) {
    				System.out.println("FATAL ERROR " + id);
    			}
    			System.out.println(id + ", " + (tbal - rtbal) + ", " );
    		}
    	}
    }*/
    
    /*public static void main(String[] args) throws Exception {
    	File src = new File("c:/Users/Lighthu/Desktop/中秋佳节包.txt");
    	File dest = new File("c:/Users/Lighthu/Desktop/中秋佳节包-out.txt");
    	FileReader fr = new FileReader(src);
    	BufferedReader br = new BufferedReader(fr);
    	String line;
    	PrintWriter out = new PrintWriter(new FileWriter(dest));
    	while ((line = br.readLine()) != null) {
    		line = line.trim();
    		if (line.length() == 0) {
    			continue;
    		}
    		out.print("insert into tbl_card (cardno, gamecode, cardtype, validtime, used, accountid, maptype) values ('");
    		out.print(line);
    		out.println("', 6, '2010083012', '2010/12/30 00:00:00', 0, 0, 2083);");
    	}
    	out.close();
    	fr.close();
    }*/
    
//    public static void main(String[] args) throws Exception {
//    	PipImage img = new PipImage();
//    	String path = "D:\\workspace\\flash\\Sanguo-Flash-Data1.0\\data\\client_res\\weapon.pip"; 
//    	img.load(path);
//    	for (int i = 0; i < img.getImgCount(); i++) {
//    		PipImageData id = img.getImageData(i);
//    		int[] newdata = new int[(id.width + 40) * (id.height + 40)];
//    		Arrays.fill(newdata, id.data[0]);
//    		for (int j = 0; j < id.height; j++) {
//    			for (int k = 0; k < id.width; k++) {
//    				newdata[(j + 20) * (id.width + 40) + k + 20] = id.data[j * id.width + k];
//    			}
//    		}
//    		id.data = newdata;
//    		id.width += 40;
//    		id.height += 40;
//    	}
//    	img.save(new File(path));
//    }
    
/*    public static void main(String[] args) throws Exception {
    	//File dir = new File("D:\\workspace\\flash\\Sanguo-Flash-Data1.0\\data\\client_res");
    	File dir = new File("D:\\workspace\\flash\\Sanguo-Flash-Data1.0\\data\\Animations\\2x");
    	File[] fs = dir.listFiles();
    	for (File f : fs) {
    		if (f.getName().endsWith(".cts")) {
    			PipAnimateSet pas = new PipAnimateSet();
    			pas.load(f);
    			int weaponIndex = -1;
    			for (int i = 0; i < pas.getFileCount(); i++) {
    				if ("weapon.pip".equals(pas.getFileName(i))) {
    					weaponIndex = i;
    					break;
    				}
    			}
    			if (weaponIndex != -1) {
    				// 所有用到weapon的帧，都向左上移动20像素
    				for (int i = 0; i < pas.getFrameCount(); i++) {
    					PipAnimateFrame frame = pas.getFrame(i);
    					for (int j = 0; j < frame.getPieceCount(); j++) {
    						PipAnimateFramePiece p = frame.getPiece(j);
    						if (p.getImageID() == weaponIndex) {
    							p.setDx(p.getDx() - 20);
    							p.setDy(p.getDy() - 20);
    						}
    					}
    				}
    				
    				pas.save(f, true);
    				String path = f.getAbsolutePath();
    				path = path.substring(0, path.length() - 1) + "n";
    				pas.save(new File(path), false);
    			}
    		}
    	}
	}    */
    
    public static void addMap(HashMap<Integer, Integer> map, int id, int count) {
    	Integer old = map.get(id);
    	if (old == null) {
    		map.put(id, count);
    	} else {
    		map.put(id, old.intValue() + count);
    	}
    }
    
    public static int getID(String line, String prefix) {
    	int pos = line.indexOf(prefix) + prefix.length();
    	int pos2 = line.indexOf(',', pos);
    	return Integer.parseInt(line.substring(pos, pos2));
    }
    
    public static int getCount(String line, String prefix) {
    	int pos = line.indexOf(prefix);
    	int pos2 = line.indexOf(')', pos + 1);
    	pos = line.lastIndexOf(',', pos2) + 1;
    	return Integer.parseInt(line.substring(pos, pos2));
    }
    
    public static String getParam(String line, String prefix) {
    	int pos = line.indexOf(prefix + "[") + prefix.length() + 1;
    	int pos2 = line.indexOf(']', pos);
    	return line.substring(pos, pos2);
    }
}
