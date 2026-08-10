package peony.service.expansionbattle;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.Server;

/**
 * 存放各种buff、npc信息
 * @author dchen
 */
public class ExpansionConfig {

	public int mapId;
	public List<ExpansionPeriod> periods = new ArrayList<ExpansionPeriod>();
	public Map<Integer, List<ExpansionNpcTemplate>> npcTemplates = new HashMap<Integer, List<ExpansionNpcTemplate>>();
	public List<ExpansionNpcTemplate> publicTemplates = new ArrayList<ExpansionNpcTemplate>();
	
	/** 玩家用无敌BUFF */
	public static int BUFF_WUDI = 256;
	
	/** 箭塔用无敌BUFF */
	public static int BUFF_WUDI1 = 257;
	
	/** 讨贼檄文BUFF */
	public static int BUFF_ITEM = 261;
	
	/** 讨贼檄文物品ID */
	public static int ITEM = 1909;
	
	public void initConfig(){
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.
			findFile("expansionbattle.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ExpansionNpcTemplate getNpcTemplate(int type, int faction){
		List<ExpansionNpcTemplate> templates = npcTemplates.get(faction);
		if(templates!=null){
			for(ExpansionNpcTemplate temp : templates){
				if(temp.type==type){
					return temp;
				}
			}
		}
		return null;
	}
	
	public List<ExpansionNpcTemplate> getNpcTemplate(int type){
		List<ExpansionNpcTemplate> list = new ArrayList<ExpansionNpcTemplate>();
		for(ExpansionNpcTemplate temp : publicTemplates){
			if(temp.type==type){
				list.add(temp);
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) throws Exception {
		Element root = doc.getRootElement();
		List<Element> battles = root.elements("battle");
		for(Element battle : battles){
			mapId = Integer.parseInt(battle.attributeValue("mapId"));
			Element pdEl = battle.element("period");
			int duration = Integer.parseInt(pdEl.attributeValue("duration"));
			List<Element> pds = pdEl.elements("pd");
			for(Element pd : pds){
				int startHour = Integer.parseInt(pd.attributeValue("startHour"));
				int startMin = Integer.parseInt(pd.attributeValue("startMin"));
				int endHour = Integer.parseInt(pd.attributeValue("endHour"));
				int endMin = Integer.parseInt(pd.attributeValue("endMin"));
				ExpansionPeriod p = new ExpansionPeriod(startHour,startMin,endHour,endMin,duration);
				periods.add(p);
			}
			
			Element wei = battle.element("wei");
			List<Element> weiNpcs = wei.elements("npc");
			List<ExpansionNpcTemplate> weiList = new ArrayList<ExpansionNpcTemplate>();
			for(Element n : weiNpcs){
				int id = Integer.parseInt(n.attributeValue("id"));
				int type = Integer.parseInt(n.attributeValue("type"));
				int x = Integer.parseInt(n.attributeValue("x"));
				int y = Integer.parseInt(n.attributeValue("y"));
				ExpansionNpcTemplate temp = new ExpansionNpcTemplate(type,id,x,y);
				weiList.add(temp);
			}
			npcTemplates.put(1, weiList);
			
			Element shu = battle.element("shu");
			List<Element> shuNpcs = shu.elements("npc");
			List<ExpansionNpcTemplate> shuList = new ArrayList<ExpansionNpcTemplate>();
			for(Element n : shuNpcs){
				int id = Integer.parseInt(n.attributeValue("id"));
				int type = Integer.parseInt(n.attributeValue("type"));
				int x = Integer.parseInt(n.attributeValue("x"));
				int y = Integer.parseInt(n.attributeValue("y"));
				ExpansionNpcTemplate temp = new ExpansionNpcTemplate(type,id,x,y);
				shuList.add(temp);
			}
			npcTemplates.put(2, shuList);
			
			Element wu = battle.element("wu");
			List<Element> wuNpcs = wu.elements("npc");
			List<ExpansionNpcTemplate> wuList = new ArrayList<ExpansionNpcTemplate>();
			for(Element n : wuNpcs){
				int id = Integer.parseInt(n.attributeValue("id"));
				int type = Integer.parseInt(n.attributeValue("type"));
				int x = Integer.parseInt(n.attributeValue("x"));
				int y = Integer.parseInt(n.attributeValue("y"));
				ExpansionNpcTemplate temp = new ExpansionNpcTemplate(type,id,x,y);
				wuList.add(temp);
			}
			npcTemplates.put(3, wuList);
			
			Element npc = battle.element("npcs");
			List<Element> npcs = npc.elements("npc");
			for(Element n : npcs){
				int id = Integer.parseInt(n.attributeValue("id"));
				int type = Integer.parseInt(n.attributeValue("type"));
				int x = Integer.parseInt(n.attributeValue("x"));
				int y = Integer.parseInt(n.attributeValue("y"));
				ExpansionNpcTemplate temp = new ExpansionNpcTemplate(type,id,x,y);
				publicTemplates.add(temp);
			}
		}
	}
	
}
