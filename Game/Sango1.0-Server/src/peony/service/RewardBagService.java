package peony.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.itemenhance.ItemEnhance;

public class RewardBagService implements Service {

	protected static final Logger log = Logger.getLogger(RewardBagService.class);
	protected Map<Integer, List<Config>> configs = new HashMap<Integer, List<Config>>();
	protected static Random random = new Random();
	
	class Config {
		
		int equipTemplateId;
		int star;
		int attType; //资质属性
		int attType1; //资质属性
		int attValue; //资质属性值
		int attValue1; //资质属性值
		int hole; //增加孔数
		int[] jewels; //宝石ID数组

		public Config(String equipTemplateId, String star, String natual, String hole, String jewels) {
			try {
				this.equipTemplateId = Integer.parseInt(equipTemplateId);
				this.star = Integer.parseInt(star);
				try {
					this.attType = Integer.parseInt(natual.split(",")[0]);
					this.attValue = Integer.parseInt(natual.split(",")[1]);
					this.attType1 = Integer.parseInt(natual.split(",")[2]);
					this.attValue1 = Integer.parseInt(natual.split(",")[3]);
				} catch (Exception e) {
					this.attType = -1;
					this.attValue = -1;
					this.attType1 = -1;
					this.attValue1 = -1;
				}
				this.hole = Integer.parseInt(hole);
				int jewelLength = jewels.split(",").length;
				if (jewelLength > 1) {
					this.jewels = new int[jewelLength];
					for (int i = 0; i < jewelLength; i++) {
						this.jewels[i] = Integer.parseInt(jewels.split(",")[i]);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	public void startup() throws Exception {
		try {
			loadConfig();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void loadConfig() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("rewardbag.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		if (list != null && list.size() > 0) {
			for (Element element : list) {
				int itemId = Integer.parseInt(element.attributeValue("id"));
				List<Element> subList = element.elements("rewardItem");
				List<Config> configList = new ArrayList<Config>();
				for(Element ele : subList){
					String rewardItemId = ele.attributeValue("id");
					String star = ele.attributeValue("star");
					String natualLevel = ele.attributeValue("natualLevel");
					String hole = ele.attributeValue("addHole");
					String jewel = ele.attributeValue("jewel");
					Config config = new Config(rewardItemId, star, natualLevel, hole, jewel);
					configList.add(config);
				}
				configs.put(itemId, configList);
			}
		}
	}

	/** 任务特殊奖励 */
	public boolean isSpecialItem(int sourceId,int itemId) {
		List<Config> configList = configs.get(sourceId);
		if(configList!=null&&configList.size()>0){
			for(Config config : configList){
				if(config.equipTemplateId == itemId){
					return true;
				}
			}
		}
		return false;
	}

	/** 生成任务特殊奖励物品 */
	public GameItem getGameItem(int sourceId,int itemId) {
		ItemTemplate template = ObjectAccessor.getItemTemplate(itemId);
		if (template != null) {
			List<Config> cfs = configs.get(sourceId);
			for(Config config : cfs){
				if(config.equipTemplateId == itemId){
					if (template.newInstance || template.isEquipment()) {
						GameItem item = ObjectAccessor.createGameItem(template, -1);
						return dressGameItem(item, config);
					} else {
						GameItem item = ObjectAccessor.createGameItem(template, -1);
						return dressGameItem(item, config);
					}
				}
			}
		}
		return null;
	}
	
	protected GameItem dressGameItem(GameItem item, Config config){
		if(config.star>0)
			ItemUtil.star(item, config.star);
		if(config.hole>0)
			ItemUtil.hole(item, config.hole);
		if(config.attType>=0 && config.attType1>=0)
			ItemUtil.natualEnhance(item, config.attType, config.attValue, config.attType1, config.attValue1);
		ItemEnhance ie = (ItemEnhance)item.object;
		if(config.jewels!=null && config.jewels.length>0)
			for(int i=0;i<config.jewels.length;i++){
				ie.addJewel(i, config.jewels[i]);
			}
		return item;
	}


	public void shutdown() {
		
	}

}

