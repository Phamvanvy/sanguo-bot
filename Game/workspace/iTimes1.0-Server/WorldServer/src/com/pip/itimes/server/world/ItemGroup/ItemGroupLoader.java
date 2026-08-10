package com.pip.itimes.server.world.ItemGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Items;

public class ItemGroupLoader {
	public ItemGroupLoader(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        ItemGroup.init();
        load(root);
	}
	
	public void load(Element root){
		for(Iterator<Element> group = root.elementIterator("Group"); group.hasNext();){
			Element node = (Element)group.next();
			ItemGroup itemGroup = new ItemGroup();
			itemGroup.setID(getInt(node.attributeValue("id")));
			itemGroup.setType(getByte(node.attributeValue("type")));
			itemGroup.setDesc(node.attributeValue("desc"));
			HashMap<Integer, ItemInfo> itemMap = new HashMap<Integer, ItemInfo>();
			for (Iterator<Element> item = node.elementIterator("Item"); item.hasNext();) {
				Element el = (Element)item.next();
				ItemInfo itemInfo = new ItemInfo();
				itemInfo.setItemID(getInt(el.attributeValue("itemid")));
				itemInfo.setPoint(getInt(el.attributeValue("point")));
				itemInfo.setCountType(getByte(el.attributeValue("counttype")));
				itemInfo.setCount(getInt(el.attributeValue("count")));
				itemInfo.setRefreshType(getByte(el.attributeValue("refreshtype")));
				itemInfo.setRefresh(getInt(el.attributeValue("refresh")));
				itemInfo.setDesc(el.attributeValue("desc"));
				itemInfo.setLevel(getInt(el.attributeValue("level")));
				IItemTemplate iitem = Items.getTemplate(itemInfo.getItemID());
				itemInfo.setItem(iitem);
				itemMap.put(new Integer(itemInfo.getItemID()), itemInfo);
			}
			itemGroup.setItemInfo(itemMap);
			ItemGroup.addGroup(itemGroup);
		}
		//读取数据之后加载存储在文件中的角色购买信息和商店信息
		ItemGroup.load2File();
	}
	
	public int getInt(String str){
		if(str == null){
			return 0;
		}
		return Integer.parseInt(str);
	}
	
	public byte getByte(String str){
		if(str == null){
			return 0;
		}
		return Byte.parseByte(str);
	}
}
