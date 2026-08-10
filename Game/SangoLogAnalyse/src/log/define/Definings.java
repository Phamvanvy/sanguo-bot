package log.define;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import log.define.processor.LogProcessor;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.dom4j.Document;
import org.dom4j.Element;

public class Definings {

	private static final ConcurrentHashMap<String, LogDefine> id2logDefine = new ConcurrentHashMap<String, LogDefine>();
	public static Map<String, String> itemNames = new HashMap<String, String>();
	public static Map<String,String> equipmentNames = new HashMap<String,String>();
	public static Map<String,String> horseType = new HashMap<String,String>();
	public static Map<String,String> skills = new HashMap<String,String>();
	public static Map<String,String> quests = new HashMap<String,String>();
	

	@SuppressWarnings("unchecked")
	public static final void loadDefine() {
		try {
			XMLConfiguration config = new XMLConfiguration("define.xml");
			List<SubnodeConfiguration> list = config.configurationsAt("define");
			for (SubnodeConfiguration node : list) {
				LogDefine define = new LogDefine(node);
				if (id2logDefine.containsKey(define.getId())) {
					throw new Exception("LogDefine duplicated:"
							+ define.getId());
				}
				id2logDefine.put(define.getId(), define);
				List<SubnodeConfiguration> list1 = node
						.configurationsAt("processor");
				for (SubnodeConfiguration node1 : list1) {
					LogProcessor processor = LogProcessor.loadProcessor(define,
							node1);
					define.addLogProcessor(processor.getId(), processor);
				}

				// ‘ÿ»ÎLogItem
				List<SubnodeConfiguration> list2 = node
						.configurationsAt("item");
				for (SubnodeConfiguration node2 : list2) {
					LogItem item = LogItem.loadItem(define, node2);
					define.addLogItem(item.getId(), item);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			XMLConfiguration config = new XMLConfiguration("logs.xml");
			List<SubnodeConfiguration> list2 = config
					.configurationsAt("gamelog");
			for (SubnodeConfiguration node3 : list2) {
				String id = node3.getString("id");
				LogDefine define = id2logDefine.get(id);
				List<SubnodeConfiguration> list3 = node3
						.configurationsAt("log");
				for (SubnodeConfiguration node4 : list3) {
					LogType type = LogType.loadType(define, node4);
					define.addLogType(type.getId(), type);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			File file = new File("item.xml");
			FileInputStream fis = new FileInputStream(file);
			Document doc = CommonUtil.getDocument(fis);
			Element root = doc.getRootElement();
			if (root != null) {
				List list = root.elements("item");
				for (int i = 0; i < list.size(); i++) {
					String id = ((Element) list.get(i)).attributeValue("id");
					String itemName = ((Element) list.get(i))
							.attributeValue("title");
					itemNames.put(id, itemName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try{
			File file = new File("equipment.xml");
			FileInputStream fis2 = new FileInputStream(file);
			Document doc2 = CommonUtil.getDocument(fis2);
			Element root2 = doc2.getRootElement();
			if(root2!=null){
				List list = root2.elements("equipment");
				for(int i=0;i<list.size();i++){
					String id = ((Element)list.get(i)).attributeValue("id");
					String name = ((Element)list.get(i)).attributeValue("title");
					equipmentNames.put(id, name);
				}
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		try{
			File file = new File("horsetype.xml");
			FileInputStream fis3 = new FileInputStream(file);
			Document doc3 = CommonUtil.getDocument(fis3);
			Element root3 = doc3.getRootElement();
			if(root3!=null){
				List list = root3.elements("horsetype");
				for(int i=0;i<list.size();i++){
					String id = ((Element)list.get(i)).attributeValue("id");
					String name = ((Element)list.get(i)).attributeValue("title");
					horseType.put(id, name);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			File file = new File("skills.xml");
			FileInputStream fis4 = new FileInputStream(file);
			Document doc4 = CommonUtil.getDocument(fis4);
			Element root4 = doc4.getRootElement();
			if(root4!=null){
				List list = root4.elements("skill");
				for(int i=0;i<list.size();i++){
					String id = ((Element)list.get(i)).attributeValue("id");
					String name = ((Element)list.get(i)).attributeValue("title");
					skills.put(id, name);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			File file = new File("index.xml");
			FileInputStream fis5 = new FileInputStream(file);
			Document doc5 = CommonUtil.getDocument(fis5);
			Element root5 = doc5.getRootElement();
			if(root5!=null);{
				List list = root5.elements("quest");
				for(int i=0;i<list.size();i++){
					String id = ((Element)list.get(i)).attributeValue("id");
					String name = ((Element)list.get(i)).attributeValue("title");
					quests.put(id, name);
				}
			}
			
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public static final LogDefine getLogDefine(String id) {
		return id2logDefine.get(id);
	}

	public static final String getItemName(String id) {
		return itemNames.get(id);
	}
	
	public static final String getEquipmentName(String id){
		return equipmentNames.get(id);
	}
	
	public static final String getHorseName(String id){
		return horseType.get(id);
	}
	
	public static final String getSkillName(String id){
		return skills.get(id);
	}
	
	public static final String getQuestName(String id){
		return quests.get(id);
	}
}
