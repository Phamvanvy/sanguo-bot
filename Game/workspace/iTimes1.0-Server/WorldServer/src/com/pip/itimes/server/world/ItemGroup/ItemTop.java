package com.pip.itimes.server.world.ItemGroup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.server.util.Utils;

public class ItemTop {
	private static final Logger log = Logger.getLogger(ItemTop.class);
	
	public static final String SAVE_FILE_NAME = "ItemTopData";
	
	public static final int MAX_WEEK_DAY = 14;
	public static final int MAX_TOP = 10;
	
	private static Map<Long, Map<Integer, ItemTopData>> playerDayConsumeData = new ConcurrentHashMap<Long, Map<Integer,ItemTopData>>();;
	private static List<ItemTopData> sortDayTopData; //天排行榜的数据
	private static long sortDayTopTime = 0;		//天排行榜的日期
	
	private static List<ItemTopData> sortWeekTopData;	//周排行榜的数据
	private static long sortWeekTopTime = 0;	//周排行榜的日期
	
	/**
	 * 添加每天玩家的消费点数
	 * @param playerid
	 * @param consume
	 */
	public static void addConsume(int playerid, int consume){
		synchronized (playerDayConsumeData) {
			long today = Utils.getTodayStart();
			ConcurrentHashMap<Integer, ItemTopData> dayData = null;
			boolean todayPut = false;
			if(playerDayConsumeData.containsKey(today)){
				dayData = (ConcurrentHashMap<Integer, ItemTopData>)playerDayConsumeData.get(new Long(today));
			}else{
				dayData = new ConcurrentHashMap<Integer, ItemTopData>(MAX_TOP);
				todayPut = true;
			}
			if(dayData.containsKey(playerid)){
				ItemTopData itemTopData = dayData.get(playerid);
				itemTopData.setConsume(consume);
//				log.debug("modify playerid[" + playerid + "] consume[" + consume + "]");
			}else{
				dayData.put(playerid, new ItemTopData(playerid, consume));
//				log.debug("add playerid[" + playerid + "] consume[" + consume + "]");
			}
			if(todayPut){
				playerDayConsumeData.put(today, dayData);
			}
		}
	}
	
	/**
	 * 获得日期排行榜 返回空表示没有记录排行榜
	 * @return
	 */
	public static List<ItemTopData> getTop2Day(){
		long oldDay = Utils.getTodayStart() - Utils.MILLS_OF_DAY;
		if(playerDayConsumeData != null && playerDayConsumeData.containsKey(oldDay)){
			//过期需要重排
			if(sortDayTopTime != oldDay){
				Map<Integer, ItemTopData> consumeMap = playerDayConsumeData.get(oldDay);
				if(consumeMap == null) return null;
				if(sortDayTopData != null){
					sortDayTopData.clear();
				}
				sortDayTopData = sortTopData(consumeMap);
				sortDayTopTime = oldDay;
			}
			return sortDayTopData;
		}
		return null;
	}
	
	/**
	 * 将消费数据排序放进sortDayTopData中
	 * @param consumeMap
	 */
	private static List<ItemTopData> sortTopData(Map<Integer, ItemTopData> consumeMap){
		Iterator<ItemTopData> iter = consumeMap.values().iterator();
		ItemTopData minValue = iter.next();
		if(minValue != null){
			List<ItemTopData> sortTopData = new ArrayList<ItemTopData>(MAX_TOP + 1);
			sortTopData.add(new ItemTopData(minValue.getPlayerID(), minValue.getConsume()));
			while(iter.hasNext()){
				ItemTopData itemTopData = iter.next();
				if(itemTopData != null){
					int size = sortTopData.size();
					boolean insert = false;
					for(int i=0; i<size; i++){
						ItemTopData itemTopDataTemp = sortTopData.get(i);
						if(itemTopData.getConsume() > itemTopDataTemp.getConsume()){
							sortTopData.add(i, new ItemTopData(itemTopData.getPlayerID(), itemTopData.getConsume()));
							insert = true;
							break;
						}
					}
					if(!insert){
						sortTopData.add(new ItemTopData(itemTopData.getPlayerID(), itemTopData.getConsume()));
					}
					if(sortTopData.size() > MAX_TOP){
						sortTopData.remove(sortTopData.size() - 1);
					}
				}
			}
			return sortTopData;
		}
		return null;
	}
	
	/**
	 * 获得周排行榜 返回空表示没有记录排行榜
	 * @return
	 */
	public static List<ItemTopData> getTop2Week(){
		if(playerDayConsumeData == null || playerDayConsumeData.size() == 0){
			return null;
		}
		Calendar cal = Calendar.getInstance();
		int week = cal.get(Calendar.DAY_OF_WEEK);
		if(week < 3){
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - (week - 3) - 14);
		}else{
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - (week - 3) - 7);
		}
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long startDay = cal.getTimeInMillis();
		//记录小于7天 不显示周排行
		if(System.currentTimeMillis() - startDay < 7 * Utils.MILLS_OF_DAY){
			return null;
		}
		
		if(sortWeekTopTime == startDay && sortWeekTopData != null){
			return sortWeekTopData;
		}
		
		List<Map<Integer, ItemTopData>> listWeekMap = new ArrayList<Map<Integer, ItemTopData>>();
		long tempDay = startDay;
		for(int i=0; i<7; i++){
			if(playerDayConsumeData.containsKey(tempDay)){
				listWeekMap.add(playerDayConsumeData.get(tempDay));
			}
			tempDay += Utils.MILLS_OF_DAY;
		}
		if(listWeekMap.size() == 0) return null;
		Map<Integer, ItemTopData> weekData = new HashMap<Integer, ItemTopData>();
		for(Map<Integer, ItemTopData> map : listWeekMap){
			Iterator<ItemTopData> iter = map.values().iterator();
			while(iter.hasNext()){
				ItemTopData itemTopData = iter.next();
				if(itemTopData != null){
					if(weekData.containsKey(itemTopData.getPlayerID())){
						ItemTopData itemTopDataTemp = weekData.get(itemTopData.getPlayerID());
						itemTopDataTemp.setConsume(itemTopDataTemp.getConsume() + itemTopData.getConsume());
					}else{
						weekData.put(itemTopData.getPlayerID(), new ItemTopData(itemTopData.getPlayerID(), itemTopData.getConsume()));
					}
				}
			}
		}
		sortWeekTopData = sortTopData(weekData);
		sortWeekTopTime = startDay;
		return sortWeekTopData;
	}
	
	/**
	 * 保存2天的数据
	 */
	public static void save2File(){
		synchronized (playerDayConsumeData) {
			long todayTime = Utils.getTodayStart();
			saveDay(todayTime);
			saveDay(todayTime - Utils.MILLS_OF_DAY);
		}
    }
	
	public static void saveDay(long dayTime){
		try{
			if(playerDayConsumeData.containsKey(dayTime)){
				Long key = dayTime;
		    	Document doc = DocumentHelper.createDocument();
		        Element root = doc.addElement("ItemTopData");
	            Element elem = root.addElement("Day");
	            elem.addAttribute("id", key.toString());
	            Map<Integer, ItemTopData> map = playerDayConsumeData.get(key);
	        	for(ItemTopData itemTopData : map.values()){
	        		Element elItemTopData = elem.addElement("Data");
	        		elItemTopData.addAttribute("playerid", "" + itemTopData.getPlayerID());
	        		elItemTopData.addAttribute("consume", "" + itemTopData.getConsume());
	        	};
		        try {
		        	String path = System.getProperty("user.dir") + "/" + SAVE_FILE_NAME;
		        	File dir = new File(path);
		        	if(!dir.exists()){
		        		dir.mkdir();
		        	}
		        	File file = new File(path + "/" + SAVE_FILE_NAME + dayTime + ".xml");
		        	file.createNewFile();
					saveDocument(doc, new FileWriter(file));
					log.info("Save Consume ok");
				} catch (IOException e) {
					log.error(e, e);
				}
			}
		}catch(Exception e){
			log.error(e, e);
		}
	}
	
    /**
     * 从文件中加载购买数据 需要先加载配置文件中的Group信息
     */
    public static void load4File(){
    	synchronized (playerDayConsumeData) {
    		Long todayTime = Utils.getTodayStart() - Utils.MILLS_OF_DAY * (MAX_WEEK_DAY - 1);
    		for(int i=0; i<MAX_WEEK_DAY; i++){
		    	File file = new File(System.getProperty("user.dir") + "/" + SAVE_FILE_NAME + "/" + SAVE_FILE_NAME + todayTime + ".xml");
		    	if(file.exists()){
			    	try {
			    		SAXReader reader = new SAXReader();
			    		Document doc = reader.read(file);
			    		Element root = doc.getRootElement();
						for (Iterator day = root.elementIterator("Day"); day.hasNext();) {
							Element el = (Element)day.next();
							long id = Long.parseLong(el.attributeValue("id"));
							Map<Integer, ItemTopData> itemTopDataMap = new ConcurrentHashMap<Integer, ItemTopData>();
							for(Iterator data = el.elementIterator("Data"); data.hasNext();){
								Element elData = (Element)data.next();
								int playerid = Integer.parseInt(elData.attributeValue("playerid"));
								int consume = Integer.parseInt(elData.attributeValue("consume"));
								itemTopDataMap.put(playerid, new ItemTopData(playerid, consume));
							}
							if(playerDayConsumeData.containsKey(id)){
								ConcurrentHashMap<Integer, ItemTopData> dayData = (ConcurrentHashMap<Integer, ItemTopData>)playerDayConsumeData.get(id);
								if(dayData == null || dayData.size() == 0){
									playerDayConsumeData.put(id, itemTopDataMap);
								}
							}else{
								playerDayConsumeData.put(id, itemTopDataMap);
							}
						}
			    	} catch (Exception e) {
			    		log.error(e, e);
			    	}
		    	}
		    	todayTime += Utils.MILLS_OF_DAY;
    		}
    		getTop2Day();
    		getTop2Week();
    	}
    }
    
    public static void saveDocument(Document doc, Writer w){
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        XMLWriter writer = new XMLWriter(w, format);
        try {
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			 try {
				writer.close();
			} catch (IOException e) {
			}
		}
    }
}
