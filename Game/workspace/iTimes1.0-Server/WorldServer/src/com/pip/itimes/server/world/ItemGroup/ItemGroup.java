package com.pip.itimes.server.world.ItemGroup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ItemGroup {
	private static final Logger log = Logger.getLogger(ItemGroup.class);
	
	/**
	 * 存储当前物品数据和角色购买数据
	 */
	private static Map<Integer, ItemData> groupsData = new ConcurrentHashMap<Integer, ItemData>();
	/**
	 * 存储物品源数据
	 */
	private static Map<Integer, ItemGroup> groups = new ConcurrentHashMap<Integer, ItemGroup>();
	
	/**
	 * 
	 */
	private static Map<Integer, ArrayList<Integer>> groupsType = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	
	public static void init(){
		groupsData = new ConcurrentHashMap<Integer, ItemData>();
		groups = new ConcurrentHashMap<Integer, ItemGroup>();
		groupsType = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	}
	
    public static void addGroup(ItemGroup group){
    	ItemData itemData = new ItemData();
    	long now = System.currentTimeMillis();
    	for(Iterator<ItemInfo> iter = group.getItemInfo().values().iterator(); iter.hasNext();){
    		ItemInfo itemInfo = (ItemInfo)iter.next();
    		itemInfo.setTimer(now);
    		switch(itemInfo.getCountType()){
    		case ItemConstants.COUNTTYPE_AllCOUNT:
    			itemData.addAllItem(itemInfo.copy());
    			break;
    		case ItemConstants.COUNTTYPE_ONECOUNT:
    			itemData.addOneItem(itemInfo.copy());
    			break;
    		}
    	}
    	groupsData.put(new Integer(group.getID()), itemData);
    	groups.put(new Integer(group.getID()), group);
    	if(groupsType.containsKey(new Integer(group.getType()))){
    		ArrayList<Integer> idArray = groupsType.get(new Integer(group.getType()));
    		idArray.add(group.getID());
    	}else{
    		ArrayList<Integer> idArray = new ArrayList<Integer>();
    		idArray.add(group.getID());
    		groupsType.put(new Integer(group.getType()), idArray);
    	}
    }
    
    public static void refreshGroup(byte refreshType, long now){
    	Iterator iter = groupsData.entrySet().iterator();
    	while(iter.hasNext()){
    		Entry entry = (Entry)iter.next();
    		int id = (Integer)entry.getKey();
    		ItemData itemData = (ItemData)entry.getValue();
    		if(itemData != null){
    			itemData.refresh(refreshType, now, groups.get(id));
    		}
    	}
    }
    
    public static void save2File(){
    	Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("ItemGroups");
        for (Integer key : groupsData.keySet()) {
            Element elem = root.addElement("Group");
            elem.addAttribute("id", key.toString());
        	ItemData itemData = (ItemData)groupsData.get(key);
        	if(itemData != null){
        		Element elmentItem = elem.addElement("ItemData");
        		itemData.save(elmentItem);
        	};
        }
        try {
        	String path = System.getProperty("user.dir") + "/" + ItemConstants.SAVE_FILE_NAME;
        	File dir = new File(path);
        	if(!dir.exists()){
        		dir.mkdir();
        	}
        	File file = new File(path + "/" + ItemConstants.SAVE_FILE_NAME + ".xml");
        	file.createNewFile();
			saveDocument(doc, new FileWriter(file));
		} catch (IOException e) {
			log.error(e, e);
		}
    }
    
    /**
     * 从文件中加载购买数据 需要先加载配置文件中的Group信息
     */
    public static void load2File(){
    	File file = new File(System.getProperty("user.dir") + "/" + ItemConstants.SAVE_FILE_NAME + "/" + ItemConstants.SAVE_FILE_NAME + ".xml");
    	if(file.exists()){
	    	try {
	    		SAXReader reader = new SAXReader();
	    		Document doc = reader.read(file);
	    		Element root = doc.getRootElement();
				for (Iterator group = root.elementIterator("Group"); group.hasNext();) {
					Element el = (Element)group.next();
					int id = Integer.parseInt(el.attributeValue("id"));
					ItemData itemData = groupsData.get(id);
					if(itemData != null){
						Element elementItem = el.element("ItemData");
						itemData.load(elementItem);
					}
				}
	    	} catch (Exception e) {
	    		log.error(e, e);
	    	}
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
    
    /**
     * 获取指定角色在指定的商店上的物品信息列表
     * 获得的有限制个数的物品时，为获取时的个数，购买时会重新获取
     * @param playerid
     * @param groupid
     * @return
     */
    public static HashMap<Integer, ItemInfo> getItemInfo(int playerid, int groupid){
    	ItemData itemData = groupsData.get(new Integer(groupid));
    	if(itemData != null){
    		return itemData.getPlayerItem(playerid, groups.get(new Integer(groupid)));
    	}
    	return null;
    }
    
    public static byte addItemBuyInfo(int playerid, int groupid, int itemid, int count){
    	ItemData itemData = groupsData.get(new Integer(groupid));
    	if(itemData != null){
    		ItemBuyInfo itemBuyInfo = new ItemBuyInfo();
    		itemBuyInfo.setCount(count);
    		itemBuyInfo.setItemID(itemid);
    		long now = System.currentTimeMillis();
    		itemBuyInfo.setStartTimer(now);
    		itemBuyInfo.setLastTimer(now);
    		return itemData.addBuyInfo(playerid, itemBuyInfo);
    	}
    	return 0;
    }
    
    /**
     * 获得指定类型的所有物品列表
     * @param playerid
     * @param groupType
     * @return
     */
    public static List<ItemGroup> getItemInfoForType(int playerid, int groupType){
    	if(groupsType.containsKey(groupType)){
    		List<ItemGroup> list = new ArrayList<ItemGroup>();
    		ArrayList<Integer> idArray = groupsType.get(new Integer(groupType));
    		for(Integer id : idArray){
    			ItemGroup itemGroup = groups.get(new Integer(id)).copyMicro();
    			itemGroup.setItemInfo(getItemInfo(playerid, id));
    			list.add(itemGroup);
    		}
    		return list;
    	}
    	return null;
    }
    
	private int id;
	private byte type;
	private String desc;
	private HashMap<Integer, ItemInfo> itemInfo;
	
	/**
	 * 小型复制ItemGroup 不包括物品表
	 * @return
	 */
	public ItemGroup copyMicro(){
		ItemGroup itemGroup = new ItemGroup();
		itemGroup.setID(id);
		itemGroup.setDesc(desc);
		itemGroup.setType(type);
		return itemGroup;
	}
	
	public void setID(int id){
		this.id = id;
	}
	
	public int getID(){
		return id;
	}
	
	public void setType(byte type){
		this.type = type;
	}
	
	public byte getType(){
		return type;
	}
	
	public void setDesc(String desc){
		this.desc = desc;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public void setItemInfo(HashMap<Integer, ItemInfo> itemInfo){
		this.itemInfo = itemInfo;
	}
	
	public HashMap<Integer, ItemInfo> getItemInfo(){
		return itemInfo;
	}
	
}
