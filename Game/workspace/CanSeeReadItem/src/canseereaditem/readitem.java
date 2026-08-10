package canseereaditem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * @author wpjiang 用于原始最简单的保存文件导出
 */
public class readitem implements Const{
	
	/**
	 * 用于读取循环的次数
	 */
	public static int cycle = 3;
	
	/**
	 * 载入任务，拓展，基本物品
	 * 
	 * @throws DocumentException
	 */
	public static boolean showFlag = false;
	public static int curMenuType=0;
	public static String readitemBack = "";
	public static int spMark;
	public static StringBuilder sBuilder = new StringBuilder();
	public static HashMap<Integer,MenuType> parseMenuMap =new HashMap<Integer,MenuType>();
	
	public static void loadItem() throws DocumentException {
		MenuType.items.clear();
		File file = new File("item.xml");//"D:\\data\\Items\\item.xml"
		// writeTextln(file.getAbsolutePath());
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator("item"); i.hasNext();) {
			Element node = (Element) i.next();
			Attribute att = node.attribute("itemID");
			int itemId = Integer.parseInt(att.getValue());
			att = node.attribute("title");
			String itemName = att.getValue();
			MenuType.items.put(itemId, itemName);
		}
	}
	
	public static void loadPetColor() throws DocumentException {
		MenuType.petColor.clear();
		File file = new File("petcolor.xml");
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator("pet"); i.hasNext();) {
			Element element = (Element)i.next();
			byte type = Byte.parseByte(element.attributeValue("type"));
			for(Iterator j=element.elementIterator("generation");j.hasNext();){
				Element tmpElement = (Element)j.next();
				int bindType = Integer.parseInt(tmpElement.attributeValue("bindtype"))-1;
	            String[] colors = (tmpElement.attributeValue("color")).split(",");
	            String key = type + "_" + bindType;
	            MenuType.petColor.put(key, colors);
			}
		}
	}

	/**
	 * 加载装备
	 */
	public static void loadEqus() throws DocumentException {
		MenuType.equs.clear();
		File file = new File("equ.xml");//"D:\\data\\Items\\equ.xml"
		// writeTextln(file.getAbsolutePath());
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator("item"); i.hasNext();) {
			Element node = (Element) i.next();
			Attribute att = node.attribute("itemID");
			int equId = Integer.parseInt(att.getValue());
			att = node.attribute("title");
			String equName = att.getValue();
			MenuType.equs.put(equId, equName);
		}

	}
	//加载技能
	public static void loadSkill() throws DocumentException {
		MenuType.Skills.clear();
		File file = new File("index.xml");// "D:\\data\\Skill\\index.xml"
		// writeTextln(file.getAbsolutePath());
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator("skill"); i.hasNext();) {
			Element node = (Element) i.next();
			Attribute attribute = node.attribute("id");
			int id = Integer.parseInt(attribute.getValue());
			attribute = node.attribute("name");
			String name = attribute.getStringValue();
			MenuType.Skills.put(id, name);
		}

	}
	//加载打造配方
	public static void loadRecipesNew() throws DocumentException{
		MenuType.recipesNew.clear();
		File file = new File("RecipesNew.xml");
		// writeTextln(file.getAbsolutePath());
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Element root = doc.getRootElement();
		for (Iterator i = root.elementIterator("Recipe"); i.hasNext();) {
			Element node = (Element) i.next();
			Attribute att = node.attribute("id");
			int itemId = Integer.parseInt(att.getValue());
			att = node.attribute("name");
			String itemName = att.getValue();
			MenuType.recipesNew.put(itemId, itemName);
		}
	}
	
	public static void initParseMenu(){
		parseMenuMap.clear();
		parseMenuMap.put(MENU_NORMAL, new NormalMenu());
		parseMenuMap.put(MENU_PET, new PetMenu());
		parseMenuMap.put(MENU_EQUIP, new EquipMenu());
		parseMenuMap.put(MENU_MERCENARY, new MercenaryMenu());
		parseMenuMap.put(MENU_FARM, new FarmMenu());
	}
	
	/**
	 * @param s
	 * @return查找符合条件的日志行
	 */
	public static int findMessage(String s, int k,int type) {
		MenuType tmp = parseMenuMap.get(type);
		if(tmp!=null){
			return tmp.checkMessage(s, k);
		}else{
			return 0;
		}
	}

	/**
	 * @param s
	 * @param index
	 * @return按照查找的返回结果去确定要分离的字符串
	 */
	public static String splitMessage(String s, int index, int i,int type) {
		String string = "";
		String strStart="";
		String strEnd="]";
		if(-1 == index && i == -1){//获得ID[]内的playerid
			strStart="ID[";
		}else{
			MenuType tmp = parseMenuMap.get(type);
			if(tmp!=null){
				strStart=tmp.splitMessage(s, index, i);
			}
		}
		if(strStart.length()>0){
			string=splitMessageSP(s,strStart,strEnd);
		}else{
			string=s;
		}
		return string;
	};
	
	/**
	 * 根据传入的开头和结尾来截取字符串
	 * @param s
	 * @param start
	 * @param end
	 * @return 截取好的字符串
	 */
	public static String splitMessageSP(String s,String start,String end){
		if(s==null || start==null ){
			return "";
		}
		String str="";
		if(end==null){
			end =defaultEnd;
		}
		int startIndex = s.lastIndexOf(start);
		if(startIndex <0){
			return "";
		}
		int charLength = start.length();
		String temString = s.substring(startIndex + charLength, s.length());
		int endIndex = temString.indexOf(end);
		str = temString.substring(0, endIndex);
		return str;
	}
	/**
	 * 返回ID
	 * @param s
	 * @return
	 */
	public static String splitMessageID(String s){
		if(s==null)
			return "";
		String str="";
		String strStart="";
		String strEnd="]";
		if(s.contains("SystemMail") && s.contains("DestID[")){
			strStart="DestID[";
		}else if(s.contains("Produce playerID[")){
			strStart="Produce playerID[";
		}else if(s.contains("PlayerID[")){
			strStart="PlayerID[";
		}else if((s.contains("ID["))){
			strStart="ID[";
		}
		if(strStart.length()>0){
			int charLength=strStart.length();
			int startIndex=s.indexOf(strStart) + charLength;
			int endIndex=s.indexOf(strEnd, startIndex);
			if(startIndex>=0 && endIndex <= s.length()){
				str=s.substring(startIndex, endIndex);
				try {
					int tmp = Integer.parseInt(str);
				} catch (Exception e) {
					return "";
				}
				str="玩家ID["+str+"]";
			}else{
				str="";
			}
		}
		return str;
	}
	/**
	 * @param s
	 * @return 获得当前时间值
	 */
	public static String spliteTime(String s) {
		String time = s.substring(0, 19);
		return time;

	}

	/**
	 * @param s
	 * @return将字符串转换为字节
	 */
	public static byte[] getdata(String s) {
		// byte[] usedBytes = null ;
		s = s.trim();
		int j = 0;
		while ((j = s.indexOf(" ")) != -1) {
			s = s.substring(0, j).concat(s.substring(j + 1));
		}
		byte[] tmp = s.getBytes();
		byte[] high = new byte[256];
		byte[] low = new byte[256];

		for (int i = 0; i < 256; i++) {
			high[i] = digits[i >>> 4];
			low[i] = digits[i & 0x0F];
		}

		byte[] usedBytes = new byte[tmp.length / 2];
		for (int i = 0; i < tmp.length - 1; i = i + 2) {
			int t = 0;
			for (int z = 0; z < high.length; z++) {
				if (high[z] == (char) tmp[i]) {
					if (low[z] == (char) tmp[i + 1]) {
						usedBytes[i / 2] = (byte) z;
					}
				}
			}
		}
		return usedBytes;
	}

	/**
	 * @param 写下当前标题
	 */
	public static void writeCause(int type,int index) {
		String str=getSelectTitle(type,index);
		writeTextln(str+"：",false);
//		if(showFlag){
//			AnalyzeLog.consolePrintln("\n");
//		}
	}
	
	
	/**
	 * @param type
	 * @param index
	 * @return获取要选择的标题
	 */
	public static String getSelectTitle(int type,int index){
		String str="";
		MenuType tmp = parseMenuMap.get(type);
		if(tmp!=null){
			int i=index-1;
			if(i<tmp.titleName.length)
				str = tmp.titleName[i];
			}
		return str;
	}
	
	/**
	 * @param type
	 * @param s
	 * @return获取要生成的标题索引
	 */
	public static int getSelectIndex(int type,String s){
		MenuType tmp = parseMenuMap.get(type);
		if(tmp!=null){
			for(int i=0;i<tmp.titleName.length;i++){
				if(s.equals(tmp.titleName[i])){
					return i+1;
				}
			}
		}
		return 0;
	}
	
	public static int getTitleNameLength(int type){
		MenuType tmp = parseMenuMap.get(type);
		if(tmp!=null){
			return tmp.titleName.length;
		}
		return 0;
	}
	/**
	 * 获取装备星数
	 */
	public static String getEquCount(String s) {
		int startIndex = s.lastIndexOf("count[");
		String string = null;
		if (startIndex != -1) {
			int charLength = "count[".length();
			String temString = s.substring(startIndex + charLength, s.length());
			int endIndex = temString.indexOf("]");
			string = temString.substring(0, endIndex);
		}
		return string;

	}


	public static void readSelect(int type, int k, boolean export)
			throws IOException, DocumentException {
		if (AnalyzeLog.preFile == null)
			return;
		// loadEqus();
		// loadItem();
		// loadSkill();

		String tmps = "";
		// File AnalyzeLog.orderFile = new File("d://log.txt");
		if (export) {
			if (AnalyzeLog.orderFile.exists()) {
				// writeText("文件存在");
				AnalyzeLog.orderFile.delete();
				AnalyzeLog.orderFile.createNewFile();
			} else {
				// writeText("文件不存在");
				AnalyzeLog.orderFile.createNewFile();// 不存在则创建
			}
		}

		try {
			String s = null;
			int len = export ? cycle : type + 1;
			while (type < len) {
				int subLen = getTitleNameLength(type) + 1;
				int i = export ? 1 : getTitleNameLength(type);
				k = export ? i : k;
				for (; i < subLen; i++, k++) {
					if(sBuilder.length()>0){
						sBuilder.setLength(0);
					}
					FileReader reader = new FileReader(AnalyzeLog.preFile);
					BufferedReader br = new BufferedReader(reader);
					writeCause(type, k);

					while ((s = br.readLine()) != null) {
						// tmpes = tmpes.append(s);
						tmps = s;
						tmps = tmps.trim();
						if(spMark > 0){
							readitemBack = s;
							spMark = 0;
						}
						// 进行文字匹配
						int index = findMessage(tmps, k, type);
						boolean finsucess = false;
						if (0 != index) {
							finsucess = true;
						}
						if (finsucess) {

							tmps = splitMessage(tmps, index, k, type);
							String strID = splitMessageID(s);
							MenuType tmpMenu = parseMenuMap.get(type);
							if (tmpMenu != null && tmpMenu.parseMessage(s, tmps, strID,index)) {
								writeTextln("   时间为" + spliteTime(s), false);
//								if (!export) {
//									AnalyzeLog.consolePrintln("\n");
//								}
							}
						}
					}
					if (export) {
						//writeTextln("", false);
						writeFile();
					}else {
						AnalyzeLog.writeText(sBuilder.toString());
					}
					sBuilder.setLength(0);
					br.close();
					reader.close();
				}
				type++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param tmp
	 *            写下邮件里的物品
	 * @throws IOException
	 */
	public static void getMailItem(byte[] tmp) throws IOException {
		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		DataInputStream dos = new DataInputStream(biss);
		byte b = dos.readByte();
//		if (b == 2 ) {// 金钱或者装备
			byte t = dos.readByte();
			if (t == 8 && tmp.length == 6) {// 金钱
				writeText("金钱数为[" + dos.readInt() + "]", false);
			} else if(t == 3 ) {// 装备
				int equId = dos.readInt();
				writeText("装备物品ID为[" + equId + "]", false);
				if (MenuType.equs.containsKey(equId)) {
					writeText("  装备物品名称为" + MenuType.equs.get(equId), false);
					readitem.writeEQU(dos);
				}
			}
//		} else if (b == 3 ) {
			// 基本物品，拓展物品，任务物品
			else{
				int itemId = dos.readInt();
				if (0 == t) {// 基本物品
					writeText("基本物品ID为[" + itemId + "]", false);
					if (MenuType.items.containsKey(itemId)) {
						writeText("  基本物品名称为" + MenuType.items.get(itemId), false);
					}
				} else if (1 == t) {// 任务物品
					writeText("任务物品ID为[" + itemId + "]", false);
					if (MenuType.items.containsKey(itemId)) {
						writeText("  任务物品名称为" + MenuType.items.get(itemId), false);
					}
				} else if (2 == t) {// 拓展物品
					writeText("拓展物品ID为[" + itemId + "]", false);
					if (MenuType.items.containsKey(itemId)) {
						writeText("  拓展物品名称为" + MenuType.items.get(itemId), false);
					}
				}
				short count = dos.readShort();
				writeText("  物品个数为" + count, false);
			}
//		}
	}
	/**
	 * @param tmp
	 *            写下当前与商店贸易
	 * @throws IOException
	 */
	public static void getSellItem(byte[] tmp) throws IOException {
		/*
		 * ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		 * DataInputStream dos = new DataInputStream(biss); byte b =
		 * dos.readByte(); if (b == 2) {// 金钱或者装备 byte t = dos.readByte(); if (t
		 * == 8 && tmp.length == 6) {// 金钱 writeText("卖到商店的金钱数为[" +
		 * dos.readInt() + "]"); } else {// 装备 int equId = dos.readInt();
		 * writeText("卖到商店的装备物品id为[" + equId + "]"); if
		 * (equs.containsKey(equId)) { writeText("  卖到商店的装备物品名称为" +
		 * equs.get(equId)); } } } else if (b == 3) { // 基本物品，拓展物品，任务物品 byte t =
		 * dos.readByte(); int itemId = dos.readInt(); if (0 == t) {// 基本物品
		 * writeText("卖到商店的基本物品id为[" + itemId + "]"); if
		 * (items.containsKey(itemId)) { writeText("  卖到商店的基本物品名称为" +
		 * items.get(itemId)); }
		 * 
		 * } else if (1 == t) {// 任务物品 writeText("卖到商店的任务物品id为[" + itemId +
		 * "]"); if (items.containsKey(itemId)) { writeText("  卖到商店的任务物品名称为" +
		 * items.get(itemId)); } } else if (2 == t) {// 拓展物品
		 * writeText("卖到商店的拓展物品id为[" + itemId + "]"); if
		 * (items.containsKey(itemId)) { writeText("  卖到商店的拓展物品名称为" +
		 * items.get(itemId)); } } }
		 */
		ByteArrayInputStream biss = new ByteArrayInputStream(tmp);
		DataInputStream dos = new DataInputStream(biss);

		int itemId = dos.readInt();
		if (MenuType.equs.containsKey(itemId)) {
			writeText("普通商店卖掉装备物品ID为[" + itemId + "]", false);
			writeText("           普通商店卖掉装备物品名称为" + MenuType.equs.get(itemId), false);
		}

		if (MenuType.items.containsKey(itemId)) {
			writeText("普通商店卖掉物品ID为[" + itemId + "]", false);
			writeText("           普通商店卖掉物品名称为" + MenuType.items.get(itemId), false);
		}

	}

	public static void writeText(String s, boolean flag) {
//		if (!showFlag) {
//			try {
//				BufferedWriter output = new BufferedWriter(new FileWriter(
//						AnalyzeLog.orderFile, true));
//				output.write(s);
//				output.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//		} else {
//			AnalyzeLog.consolePrintln(s);
//		}
		if(s.length()>0)
			sBuilder.append(s);
	}

	public static void writeTextln(String s, boolean flag) {
//		if (!showFlag) {
//			try {
//				BufferedWriter output = new BufferedWriter(new FileWriter(
//						AnalyzeLog.orderFile, true));
//				output.write(s);
//				output.write("\n");
//				output.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			AnalyzeLog.consolePrintln(s);
//		}
		if(s.length()>0){
			writeText(s,false);
		}
		sBuilder.append("\n");
	}
	
	public static void writeFile(){
		if(sBuilder==null || sBuilder.length()== 0 )
			return;
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(
					AnalyzeLog.orderFile, true));
			output.write(sBuilder.toString());
			output.write("\n");
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void writeEQU(DataInputStream dos)throws IOException{
		int CAN_LETTERING = 0; //可以进行刻字
		int LETTERINGED = 1;  //已经刻字了
		
		Integer id = dos.readInt();// id
		writeText("  实例ID[" + id + "] ", false);
		
		dos.readInt(); //0
		boolean isbind = dos.readBoolean(); //是否绑定
		short s = dos.readShort();
		writeText("  当前物品的耐久度：" + s, false);
		// 精炼的属性
		int size = dos.read();
		int[] enhances = new int[size];
		if(size != 0){
			StringBuffer str = new StringBuffer();
			str.append(" 装备的精炼属性： ");
			for(int i = 0; i < size; i ++){
				int enhance = dos.read();
				if(enhance == 1){
					str.append("[体力]");	
				}
				if(enhance == 2){
					str.append("[智力]");	
				}
				if(enhance == 3){
					str.append("[力量]");	
				}
				if(enhance == 4){
					str.append("[敏捷]");	
				}
				if(enhance == 5){
					str.append("[物攻]");	
				}
				if(enhance == 6){
					str.append("[魔攻]");	
				}
				if(enhance == 7){
					str.append("[物防]");	
				}
				if(enhance == 8){
					str.append("[魔防]");	
				}
				if(enhance == 9){
					str.append("[命中等级]");	
				}
				if(enhance == 10){
					str.append("[闪避等级]");	
				}
				if(enhance == 11){
					str.append("[物爆等级]");	
				}
				if(enhance == 12){
					str.append("[魔爆等级]");	
				}
			}
			writeText(str.toString(), false);
		}
		dos.read();
		dos.read();
		dos.readLong();
		try {
			byte diamond = dos.readByte();
			writeText("  装备的星级钻数：" + diamond, false);
			int lettter_Flag = dos.readInt();
		
			if(CheckIntN(lettter_Flag, CAN_LETTERING) && CheckIntN(lettter_Flag, LETTERINGED)){
				String letter = dos.readUTF();
				writeText("  装备上刻的字为：" + letter, false);
			}

			byte diamondLen = dos.readByte();
			byte[] diamondMoasiacRoleInfo = new byte[diamondLen];
			dos.read(diamondMoasiacRoleInfo);
			
			//String diamondContent = String.valueOf(dos.read());
			//diamondContent = diamondContent.substring(diamondContent.length()-(int)diamondLen);
			
			byte sizeDi = dos.readByte();		// 镶嵌宝石的个数
			HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
			for(int i = 0 ; i < sizeDi; i++){
				byte index = dos.readByte();		//孔位
				int diamondID = dos.readInt();		//宝石ID
				map.put((int)index, diamondID);			
			}
			
			writeText("  装备上孔位信息：", false);
			for(int i = 0;i < diamondLen;i++)
			{	
				Byte roleInfo = diamondMoasiacRoleInfo[i];
				if(roleInfo >= 1){
					if(roleInfo == 1){
						writeText("  孔位", false);
						Integer temp = i;
						writeText(temp.toString(), false);
						writeText("已经打孔", false);
					}else{
						if(map.containsKey(i)){
							int itemId = map.get(i);
							writeText("第"+i+"孔，已经打孔，并且镶嵌了宝石为"+MenuType.items.get(itemId), false);
						}
					}
				}
			
			/*String temp = diamondContent.substring(0+i*2, 2+i*2);
			if(Integer.parseInt(temp) == 1){
				writeText("第"+i+"孔，已经打孔，但没有镶嵌", false);
			}
			if(Integer.parseInt(temp) == 0){
				writeText("第"+i+"孔，还没有打孔", false);
			}
			if(Integer.parseInt(temp) == 2){
				int diamondID = (int)map.get(i);
				writeText("第"+i+"孔，已经打孔，并且镶嵌了宝石为"+items.get(diamondID), false);
			}*/
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//n从零开始检测第n位为1
	public static boolean CheckIntN(int b, int n) throws Exception{   
      if(n > 31 || n < 0 )
    	  throw new Exception();   
      return ((b   &   (1   <<   n))==(1<<n));   
	}   
	
	//n从零开始设置第n位为1
	public static int  SetIntN(int  b, int n)  throws Exception {
      if(n > 31 || n < 0 )
    	  throw new Exception(); 
      int t  = b | ( 1 << n);
      return t;
	      
	}   
	
	
}
