package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.decimoney.DecImoneyEffect;
import peony.depot.ExtendDepotEffect;
import peony.game.attendant.Attendant;
import peony.game.buff.BuffUtil;
import peony.game.itemeffect.ActivePowerEffect;
import peony.game.itemeffect.AddFormulaEffect;
import peony.game.itemeffect.AddHpItemEffect;
import peony.game.itemeffect.AddItemEffect;
import peony.game.itemeffect.AddKingHpItemEffect;
import peony.game.itemeffect.AddMpItemEffect;
import peony.game.itemeffect.AddTitleItemEffect;
import peony.game.itemeffect.AttendantAddHpItemEffect;
import peony.game.itemeffect.AttendantBookSkillEffect;
import peony.game.itemeffect.AttendantItemEffect;
import peony.game.itemeffect.BookSkillItemEffect;
import peony.game.itemeffect.ComboItemEffect;
import peony.game.itemeffect.DivorceEffect;
import peony.game.itemeffect.DropItemEffect;
import peony.game.itemeffect.EmptyItemEffect;
import peony.game.itemeffect.ExtHorseMaxItemEffect;
import peony.game.itemeffect.ExtendAttendantBagEffect;
import peony.game.itemeffect.ExtendBagEffect;
import peony.game.itemeffect.GetClickExpEffect;
import peony.game.itemeffect.GetClickMoneyEffect;
import peony.game.itemeffect.GetCreditEffect;
import peony.game.itemeffect.GetExpEffect;
import peony.game.itemeffect.GetHonorEffect;
import peony.game.itemeffect.GetHorseExpEffect;
import peony.game.itemeffect.GetMoneyEffect;
import peony.game.itemeffect.HorseFeedItemEffect;
import peony.game.itemeffect.HorseItemEffect;
import peony.game.itemeffect.IMoneyCardEffect;
import peony.game.itemeffect.MarriageTeleportEffect;
import peony.game.itemeffect.QuestItemEffect;
import peony.game.itemeffect.RefAttendantSkillItemEffect;
import peony.game.itemeffect.RefreshPropertyPointsItemEffect;
import peony.game.itemeffect.RefreshSkillPointItemEffect;
import peony.game.itemeffect.RepairEquipmentsItemEffect;
import peony.game.itemeffect.RiddleItemEffect;
import peony.game.itemeffect.RideItemEffect;
import peony.game.itemeffect.ScriptEffect;
import peony.game.itemeffect.SkillItemEffect;
import peony.game.itemeffect.TDToTowerEffect;
import peony.game.itemeffect.TeleportEffect;
import peony.game.itemeffect.TongBattleCarEffect;
import peony.game.itemeffect.TorchEffect;
import peony.game.itemeffect.UnSignupItemEffect;
import peony.game.itemeffect.UseDecItemEffect;
import peony.game.itemenhance.ItemEnhance;
import peony.game.itemenhance.NaturalEnhance;
import peony.game.nation.Nation;
import peony.game.nation.NationService;
import peony.game.nation.NationSkill8;
import peony.game.suite.SuiteEffect;
import peony.game.suite.SuiteEffects;
import peony.produce.ProduceService;
import peony.service.fame.Fame;
import peony.service.fame.FameService;
import ch.javasoft.util.IntArray;
import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.SuiteConfig;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.item.Item;

public class ItemUtil implements DataChangeHandler {
	
	private static final Logger log = Logger.getLogger(ItemUtil.class);
	
	public static final byte VERSION = 2;
	
	private static final Random rnd = new Random();
	
	public static final int ITEM_WORLD_CHAT = 677;//世界喊话符
	public static final int ITEM_FACTION_CHAT = 676;//国家喊话符
	public static final int ITEM_ACTIVATION = 1225;//激活码对应物品
	public static final int ITEM_ACTIVATION1 = 1237;
	public static final int ITEM_ACTIVATION2 = 1238; //论坛500分奖品
	public static final int ITEM_RELIVE = 670;//返魂香包
	public static final int ITEM_HORSEFOOD_ADDBUFF = 1242;//马粮加buff
	public static final int ITEM_HORSEFOOD = 819; //马粮 
	public static final int ITEM_STAR_ENHANCE_LEVEL1 = 1579; //低级星级鉴定符
	public static final int ITEM_STAR_ENHANCE_LEVEL2 = 1580; //高级星级鉴定符
	public static final int ITEM_STAR_ENHANCE_LEVEL3 = 1581; //顶级星级鉴定符
	public static final int ITEM_NATURAL_ENHANCE = 1582; //资质鉴定符
	public static final int ITEM_CHINAJOY = 1236; //金翎奖短信礼包
	public static final int ITEM_KILLKING = 1174; //英勇勋章，击杀国王以后的奖励
	public static final int ITEM_NATION_QUEST = 1182; //国家任务道具
	public static final int ITME_NATIONBATTLE_WIN = 1311; //国战战场胜利奖励
	public static final int ITEM_CHANGE_NAME = 1255; //改名符
	public static final int ITEM_CHANGE_TONG_NAME = 1256; //军团改名符
	public static final int ITEM_IMONEY_CARD_10 = 1163;
	public static final int ITEM_ONLINEEXP_CLICK = 1643;  //点击获得累计在线时间经验
	public static final int ITEM_TONG_QUEST = 1104; // 军团任务道具
	public static final int ITEM_ONLINT_GETMONEY = 1943; //财神散财礼包
	public static final int ITEM_ASSOCIATION = 2167; //结义令
	public static final int ASSOCIATION_RENAME_ITEM = 2277;//血盟改名符
	public static final int ATTENDANT_LIGHTSKILL_ITEM = 3301; //随从技能位点化符
	public static final int ATTENDANT_RENAME_ITEM = 3302; //随从改名符
	public static final int ATTENDANT_ADDLOYAL_ITEM = 3303; //随从激活忠诚度物品
	public static final int HORSE_UP_SKILLLEVEL_ITEM = 2413; //坐骑升级需求物品
	
	//	31%	35%	20%	10%	4%
	//	5~7％随机	8~13％随机	14~20％随机	21-28％随机	29-45％随机
	public static final int[][] NATURAL_ENHANCE_PERCENT = {
		{4,29,45},
		{4,29,45},
		{4,29,45},
		{4,29,45},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{3,21,28},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{2,14,20},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{1,8,13},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
		{0,5,7},
	};
//	无星级鉴定符	48.59%	32%	12%	4%	2%	1%	0.4%	0.01%	0%
//	低级星级鉴定符	0%	0%	30.5%	38%	18%	10%	3%	0.5%	0%
//	高级星级鉴定符	0%	0%	0%	0%	58%	29%	10%	3%	0%
//	顶级星级鉴定符	0%	0%	0%	0%	0%	0%	0%	0%	100%

	public static final int[][] START_ENHANCE = {
		{4859,3200,1200, 400, 200, 100,  40,   1,   0},
		{   0,   0,3050,3800,1800,1000, 300,  50,   0},
		{   0,   0,   0,   0,5800,2900,1000, 300,   0},
		{   0,   0,   0,   0,   0,   0,   0,   0,10000},
	};
	
	public static Map<Integer, Boolean> noticeItems = new HashMap<Integer, Boolean>();
	
	/**
	 * 判断一个物品获得时是否需要发送系统广播。
	 * @param itemID
	 * @return 0 不需要广播  1 普通广播 2 狮子吼
	 */
	public static int getNoticeType(int itemID) {
		Boolean b = noticeItems.get(itemID);
		if (b == null) {
			return 0;
		} else {
			return b.booleanValue() ? 2 : 1;
		}
	}
	
    /**
     * 在玩家的背包、身上、坐骑上查找某件装备。
     * @param p
     * @param itemID
     * @param instanceID
     * @return 如果找不到，返回null；如果找到，返回的数组中，第一个元素是GameItem，第二个元素
     *     是找到的位置，可能是Player, TransactionBag或Horse。
     */
    public static Object[] findPlayerEquipment(Player p, int itemID, int instanceID) {
        GameItem gi = p.equipments.find(itemID, instanceID);
        if (gi != null) {
            return new Object[] { gi, p };
        }
        gi = p.bag.getGameItem(-1, itemID, instanceID);
        if (gi != null) {
            return new Object[] { gi, p.bag };
        }
        for (Horse horse : p.horseBag.horses) {
            gi = horse.equs.find(itemID, instanceID);
            if (gi != null) {
                return new Object[] { gi, horse };
            }
        }
        for(Attendant attendant : p.attendantBag.attendants){
        	for(GameItem gameItem : attendant.equs){
        		if(gameItem!=null && gameItem.template.id==itemID && gameItem.instanceId==instanceID)
        			return new Object[] {gameItem, attendant};
        	}
        }
        return null;
    }
	
	public static void load() throws Exception{
		DataService dataService = Server.server.getServiceRegistry().getDataService();
		loadItemTemplates(dataService.data);
		loadEquipmentTemplates(dataService.data);
		modifyDescriptions(dataService.data);
		loadNoticeItems();
	}
	
	public static void load(DataService dataService) throws Exception{
		loadItemTemplates(dataService.data);
		loadEquipmentTemplates(dataService.data);
		modifyDescriptions(dataService.data);
	}
	
	/**
	 * 载入需要通知的物品。
	 */
	@SuppressWarnings("unchecked")
	public static void loadNoticeItems() throws Exception {
		noticeItems.clear();
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Items/noticeitems.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		Element root = doc.getRootElement();
		Iterator ite = root.elementIterator("item");
		while (ite.hasNext()) {
			Element el = (Element)ite.next();
			int id = Integer.parseInt(el.attributeValue("id"));
			int shout = Integer.parseInt(el.attributeValue("shout"));
			noticeItems.put(id, shout == 1);
		}
	}
	
	/**
	 * 按使用星级鉴定符的类型给装备加上星级
	 * @param item
	 * @param type 
	 * @return
	 */
	public static int startEnhance(GameItem item,int type,Player player){
		int c = rnd.nextInt(10000);
		int[] p = START_ENHANCE[type];
		if(player!=null){
			NationService nationService = Server.server.getServiceRegistry().getNationService();
			Nation nation = nationService.getNationByFaction(player.faction);
			NationSkill8 nationSkill8 = (NationSkill8) nation.skills.get(8);
			if(nationSkill8!=null){
				p = nationSkill8.getStarEnhanceRatio(type);
			}
		}
		int v = 0;
		int star = 0;
		for(int i=p.length-1;i>=0;i--){
			v += p[i];
			if(c<v){
				star = i+1;
				break;
			}
		}
		ItemEnhance ie = (ItemEnhance) item.object;
		if(ie==null){
			ie = new ItemEnhance();
			item.object = ie;
		}
		ie.setStar(star);
		if(ie.getNaturals()!=null){
			for(NaturalEnhance h : ie.getNaturals()){
				h.value = item.getNatureEnhanceAttribute(h.attType
						, h.percent);
				if(h.value==0)
					h.value = 1;
			}
		}
		return star;
	}
	
	
	/**
	 * 给装备装备加上指定星级
	 * @return
	 */
	public static void star(GameItem item,int star){
		ItemEnhance ie = (ItemEnhance) item.object;
		if(ie == null){
			ie = new ItemEnhance();
			item.object = ie;
		}
		ie.setStar(star);
	}
	
	public static void hole(GameItem item,int hole){
		ItemEnhance ie = (ItemEnhance) item.object;
		if(ie == null){
			ie = new ItemEnhance();
			item.object = ie;
		}
		ie.setAddHole(hole);
	}
	
	public static final void main(String[] args) {
		System.out.println(naturalEnhanceValue(AttributeCalculator.ATTRIBUTE_STR, 40, 70, 5, 3, 0.1f));
	}
	
	/**
	 * 资质鉴定，
	 * @param item
	 */
	public static void naturalEnhance(GameItem item){
//		34%	24%	23%	15%	4%
//		2~6％随机	7~11％随机	12~16％随机	17-21％随机	22-45％随机
		int[] pros = new int[2];
		Arrays.fill(pros, -1);
//		boolean enhanced = false;
		ItemEnhance ie = null;
		if (item.object != null) {
			ie = (ItemEnhance) item.object;
		}
//		if(!enhanced){
		int[] ps = item.template.equipment.getNaturalEnhanceAtts();
//		int[] ps = item.template.equipment.getNaturalEnhanceAtts0();
		if(ps==null)
			return;
		if(ps.length==1){
			pros[0] = ps[0];
		}
		else if(ps.length==2){
			pros[0] = ps[0];
			pros[1] = ps[1];
		}
		else{
			int index1 = rnd.nextInt(ps.length);
			int index2 = index1;
			while(index2==index1){
				index2 = rnd.nextInt(ps.length);
			}
			pros[0] = ps[index1];
			pros[1] = ps[index2];
		}
//		}
		List<NaturalEnhance> l = new ArrayList<NaturalEnhance>(2);
		for(int i=0;i<pros.length;i++){
			if(pros[i]!=-1){
				int index = rnd.nextInt(100);
				int[] v = NATURAL_ENHANCE_PERCENT[index];
				int c = CommonUtil.getCount(rnd, v[1], v[2]);
//				NaturalEnhance ne = new NaturalEnhance(v[0],pros[i],c,item.template.equipment.equ.getAttribute(pros[i])*c/100);
//				float attributeValue = AttributeCalculator.ATTRIBUTES[pros[i]].value;
				int value = item.getNatureEnhanceAttribute(pros[i],c);
				NaturalEnhance ne = new NaturalEnhance(v[0],pros[i],c,(value==0 ? 1 : value));
//				else{
//					ne.setLevel(v[0]);
//					ne.setPercent(c);
//					ne.addValue(item.getNatureEnhanceAttribute(c,attributeValue));
//				}
				l.add(ne);
			}
		}
		if(ie==null){
			ie = new ItemEnhance();
			item.object = ie;
		}
		NaturalEnhance[] nes = new NaturalEnhance[l.size()];
		l.toArray(nes);
		ie.setNaturals(nes);
	}
	
	public static NaturalEnhance createNaturalEnhance(GameItem item, int attType,int percent){
		int level = 0;
		for(int i=0;i<NATURAL_ENHANCE_PERCENT.length;i++){
			if(percent>=NATURAL_ENHANCE_PERCENT[i][1]&&percent<=NATURAL_ENHANCE_PERCENT[i][2]){
				level = NATURAL_ENHANCE_PERCENT[i][0];
				break;
			}
		}
		int value = item.getNatureEnhanceAttribute(attType, percent);
		NaturalEnhance ne = new NaturalEnhance(level,attType,percent,(value==0 ? 1 : value));
		return ne;
	}
	
	public static void natualEnhance(GameItem item, int attType, int percent, int attType1, int percent1){
		NaturalEnhance[] es = new NaturalEnhance[2];
		NaturalEnhance enhance = createNaturalEnhance(item, attType, percent);
		NaturalEnhance enhance1 = createNaturalEnhance(item, attType1, percent1);
		es[0] = enhance;
		es[1] = enhance1;
		if(item.object instanceof ItemEnhance){
			ItemEnhance ie = (ItemEnhance) item.object;
			ie.setNaturals(es);
		}
	}
	
//	装备价值=（该装备可佩带等级+该装备星级*3）*720*0.1*（1+品质系数）
//	若装备可佩带等级=1，则取该装备物品等级计算
//	资质鉴定额外价值=装备价值*资质鉴定提高比例
//	属性A：资质鉴定额外价值*20%/属性A单位价值
	public static int naturalEnhanceValue(int pro, int ratio, int level, int star,
			int quality,float extraQuality) {
		return (int) (((level + star * 3) * 720 * .1f * (1.0f + AttributeCalculator.QUALITY_ADDITION[quality] + extraQuality))
				* (ratio / 100.0f) * .2f / AttributeCalculator.ATTRIBUTES[pro].value);
	}
	
//	public static int naturalEnhanceValue(GameItem item,int pro,int ratio){
//		EquipmentTemplate template = item.template.equipment;
//		int star = 0;
//		if()
//		return naturalEnhancevalue(pro,ratio,template.useLevel==1?template.level:template.useLevel,)
//	}
	
	public static String buyRequirementToString(BuyRequirement br){
		switch (br.type) {
        case Shop.TYPE_MONEY:
            if (br.deduct) {
                return "金钱:" + br.amount;
            } else {
                return "金钱达到:" + br.amount;
            }
        case Shop.TYPE_HONOR:
            if (br.deduct) {
                return "荣誉:" + br.amount;
            } else {
                return "荣誉达到:" + br.amount;
            }
        case Shop.TYPE_IMONEY:
            if (br.deduct) {
                return "i币:" + br.amount;
            } else {
                return "i币达到:" + br.amount;
            }
        case Shop.TYPE_RANK:
        	DataService ds = Server.server.getServiceRegistry().getDataService();
            Rank rank = (Rank)ds.data.findDictObject(Rank.class, br.amount);
            return "军衔达到:" + rank.title;
        case Shop.TYPE_VARIABLE:
            return br.varName + "达到:" + br.amount;
        case Shop.TYPE_LEVEL:
            return "级别达到:" + br.amount;
        case Shop.TYPE_CONSUMECODE:
            return "消费代码:" + br.varName;
        case Shop.TYPE_EXP:
            if (br.deduct) {
                return "经验:" + br.amount;
            } else {
                return "经验达到:" + br.amount;
            }
        default: // TYPE_ITEM
            if (br.deduct) {
                return br.item.title + " x" + br.amount;
            } else {
                return "拥有:" + br.item.title + " x" + br.amount;
            }
        }
	}
	
	protected static void modifyDescriptions(ProjectData data) {
		for (ItemTemplate template : ObjectAccessor.itemTemplates.values()) {
			if (template.useType.effect instanceof AddFormulaEffect) {
				AddFormulaEffect e = (AddFormulaEffect) template.useType.effect;
				Formula f = (Formula)data.findObject(Formula.class, e.formulaID);
				StringBuilder sb = new StringBuilder(500);
				sb.append(template.desc);
				sb.append("\n");
				sb.append("所需熟练度:").append(ProduceService.getPracticeByLevel(f.level)).append("\n");
				sb.append("级别:").append(ProduceService.getLevelToString(f.level)).append("\n");
				sb.append("消耗行动力:").append(f.movePoint).append("\n");
				for(BuyRequirement br:f.requirements){
					sb.append(buyRequirementToString(br)).append("\n");
				}
				if(f.productType==Formula.PRODUCT_DROPGROUP){
					sb.append("产出:").append(ObjectAccessor.getItemTemplate(f.itemID).name).append("(随机属性)");
				}
				else if(f.productType==Formula.PRODUCT_ITEM){
					sb.append("产出:").append(ObjectAccessor.getItemTemplate(f.itemID).name).append("");
				}
				if (f.minAmount != 1 || f.maxAmount != 1) {
					sb.append("\n数量:").append(f.minAmount).append("~").append(f.maxAmount);
				}
				template.desc = sb.toString();
//				  配方书描述信息字符串格式:
//					  习得配方 配方名称
//					  配方描述信息
//					  所需熟练度:N
//					  级别:级别名称
//					  消耗行动力:N
//					  扣除(需要)原材料名称 X N
//					  扣除(需要)原材料名称 X N
//					  ...
//					  if(fml.produceType == PRODUCE_TYPE_GROUP){
//					  	产出类型:掉落组
//					  	示例物品:示例物品名称
//					  }else{
//					  	产出:物品名称
//					  }
//					  数量范围:fml.minCnt+"~"+fml.maxCnt
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static void loadItemTemplates(ProjectData data) throws Exception{
		List templates = data.getDataListByType(Item.class);
		for(Object o:templates){
			Item item = (Item)o;
			ItemTemplate template = ItemUtil.translateItemTemplate(item);
			ObjectAccessor.addItemTemplate(template);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void loadEquipmentTemplates(ProjectData data) throws Exception{
		List templates = data.getDataListByType(Equipment.class);
		for(Object o:templates){
			Equipment equ = (Equipment)o;
			ItemTemplate template = ItemUtil.translateItemTemplate(equ);
			ObjectAccessor.addItemTemplate(template);
		}
		List suiteConfigs = data.getDataListByType(SuiteConfig.class);
		for(Object o:suiteConfigs) {
			SuiteConfig config = (SuiteConfig)o;
			SuiteEffects effects = translateSuiteConfig(config);
			ObjectAccessor.suites.put(effects.getID(), effects);
		}

	}
	
//	@SuppressWarnings("unchecked")
//	public static void loadFormulas(ProjectData data) throws Exception{
//		List formulas = data.getDataListByType(Formula.class);
//		for(Object o:formulas){
//			Formula formula = (Formula)o; // 得到打造配方
//			ObjectAccessor.formulas.put(formula.id, formula);
//		}
//	}
	
	public static ItemTemplate translateItemTemplate(Equipment equ){
		ItemTemplate template = translateItemTemplate((Item)equ);
		EquipmentTemplate equTemplate = new EquipmentTemplate();
		template.equipment = equTemplate;
		equTemplate.equ = equ;
		equTemplate.useLevel = equ.playerLevel;
		equTemplate.agilityLimit = equ.astrictAgility<=0?0:equ.astrictAgility;
		equTemplate.strengthLimit = equ.astrictPower<=0?0:equ.astrictPower;
		equTemplate.intelligentLimit = equ.astrictInteligence<=0?0:equ.astrictInteligence;
		equTemplate.staminaLimit = equ.astrictStamina<=0?0:equ.astrictStamina;
		equTemplate.duration = equ.durability;
		equTemplate.showRandom = equ.showRandom;
		equTemplate.initHole = equ.holeCount;
		equTemplate.maxHole = equ.maxHoleCount;
		equTemplate.canJudgeStar = equ.canJudgeStar;
		equTemplate.canJudgePotential = equ.canJudgePotential;
		equTemplate.canCopy = equ.canCopy;
		equTemplate.markCharCount = equ.markCharCount;
		if(equ.equipmentType==Equipment.EQUI_TYPE_WEAPON){
			equTemplate.type = EquipmentTemplate.TYPE_WEAPON;
		}
		else if(equ.equipmentType==Equipment.EQUI_TYPE_PROTECTOR){
			equTemplate.type = EquipmentTemplate.TYPE_ARMOR;
		}
		else if(equ.equipmentType==Equipment.EQUI_TYPE_JEWELRY){
			equTemplate.type = EquipmentTemplate.TYPE_TRINKET;
		}
		if(equ.place==Equipment.WEAPON_AXE){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_AXE;
		}
		else if(equ.place==Equipment.WEAPON_BOW){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_BOW;
		}
		else if(equ.place==Equipment.WEAPON_FALCHION){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_POLEARM;
		}
		else if(equ.place==Equipment.WEAPON_FAN){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_FAN;
		}
		else if(equ.place==Equipment.WEAPON_KNIFE){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_KNIFE;
		}
		else if(equ.place==Equipment.WEAPON_SPEAR){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_SPEAR;
		}
		else if(equ.place==Equipment.WEAPON_SWORD){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_SWORD;
		}
		else if(equ.place==Equipment.PROTECTOR_CLOTHES){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_CHEST;
		}
		else if(equ.place==Equipment.PROTECTOR_HELMETS){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_HEAD;
		}
		else if(equ.place==Equipment.PROTECTOR_SHOES){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_FEET;
		}
		else if(equ.place==Equipment.PROTECTOR_TROUSERS){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_LEG;
		}
        else if(equ.place==Equipment.PROTECTOR_SHIELD){
            equTemplate.minorType = EquipmentTemplate.MINORTYPE_SHIELD;
        }
		else if(equ.place==Equipment.JEWELRY_CUFF){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_WRIST;
		}
		else if(equ.place==Equipment.JEWELRY_HUFU){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_HUFU;
		}
		else if(equ.place==Equipment.JEWELRY_YUPEI){
			equTemplate.minorType = EquipmentTemplate.MINORTYPE_YUPEI;
		}
        else if(equ.place==Equipment.JEWELRY_PIFENG){
            equTemplate.minorType = EquipmentTemplate.MINORTYPE_PIFENG;
        }
        else if(equ.place==Equipment.HORSE_HELMET){
        	equTemplate.minorType = EquipmentTemplate.MINORTYPE_HORSE_HEAD;
        }
        else if(equ.place==Equipment.HORSE_NECK){
        	equTemplate.minorType = EquipmentTemplate.MINORTYPE_HORSE_NECK;
        }
        else if(equ.place==Equipment.HORSE_BREAST){
        	equTemplate.minorType = EquipmentTemplate.MINORTYPE_HORSE_CHEST;
        }
        else if(equ.place==Equipment.HORSE_ASS){
        	equTemplate.minorType = EquipmentTemplate.MINORTYPE_HORSE_ASS;
        }
        else if(equ.place==Equipment.HORSE_SADDLE){
        	equTemplate.minorType = EquipmentTemplate.MINORTYPE_HORSE_BACK;
        }
        else if(equ.place==Equipment.HORSE_HOOF){
        	equTemplate.minorType = EquipmentTemplate.MINORTYPE_HORSE_LEG;
        } else if (equ.place == Equipment.HORSE_PEDAL) {
            equTemplate.minorType = EquipmentTemplate.MINORTYPE_HORSE_PEDAL;
        }
		equTemplate.clazz = equ.job;
		equTemplate.mask();
		equTemplate.level = equ.level;
		equTemplate.value = equ.getValue();
		try {
		    equTemplate.specialEffect = BuffUtil.createSuiteBuff(equ.buffID, equ.buffLevel);
		} catch (Exception e) {
		    log.warn("Invalid equipment effect: " + equ.id, e);
		}
		return template;
	}
	
	
	/**
	 * 将地图编辑器的Item转换成游戏程序使用的ItemTemplate
	 * @param item
	 * @return
	 */
	public static ItemTemplate translateItemTemplate(Item item){
		ItemTemplate template = new ItemTemplate();
		template.id = item.id;
		template.name = item.title;
		template.itemType = item.type;
		template.maxCount = item.addition;
		template.showType = item.iconIndex;
		template.isTaskItem = item.taskFlag;
		template.isTaskCopy = item.taskMuti;
		template.level = item.level;
		template.useLevel = item.playerLevel;
		template.quality = item.quality;
		template.newInstance = item.instance;
		template.produceArea = item.produceArea;
		if (item.sale) {
		    template.price = item.price;
		} else {
		    template.price = -1;
		}
		template.canSale = item.sale;
		template.desc = item.description;
		if (item.type == Item.TYPE_JEWEL) {
		    template.jewelAttrType = item.jewelAttrType;
		    template.jewelType = item.jewelType;
		    template.jewelAttrValue = item.calcJewelAttr();
		    template.isHorseJewel = item.isHorseJewel;
		    template.isFlaw = item.isFlaw;
		}
		if(item.bind==Item.BIND_NO){
			template.bindType = ItemTemplate.BIND_NO;
		}
		else if(item.bind == Item.BIND_EQUIPMENT){
			template.bindType = ItemTemplate.BIND_USED;
		}
		else if(item.bind == Item.BIND_PICK_UP){
			template.bindType = ItemTemplate.BIND_REWARD;
		}
		if(item.available==Item.AVAILABLE_NO){
			template.useType = UseType.NOUSETYPE;
		}else{
			UseType useType = new UseType();
			if(item.available==Item.AVAILABLE_UN_BATTLE)
				useType.occasion = UseType.OCCASION_NOBATTLE;
			else if(item.available==Item.AVAILABLE_BATTLE){
				useType.occasion = UseType.OCCASION_BATTLE;
			}
			else if(item.available==Item.AVAILABLE_EVER){
				useType.occasion = UseType.OCCASION_ALL;
			}
			if(item.area==Item.AREA_UN_DEFINE){
				useType.targetType=UseType.TARGET_NOTARGET;
			}
			else if(item.area==Item.AREA_SELF){
				useType.targetType=UseType.TARGET_SELF;
			}
			else if(item.area==Item.AREA_TEAM){
				useType.targetType=UseType.TARGET_PARTY;
			}
			else if(item.area==Item.AREA_ENEMY){
				useType.targetType=UseType.TARGET_ENEMY;
			}
			else if(item.area==Item.AREA_ALL){
				useType.targetType=UseType.TARGET_ALL;
			}
			useType.useClazz = item.useClazz;
			useType.useConfirm = item.useConfirm;
			useType.consume = item.waste;
			useType.useCount = item.count;
			useType.coolDownId = item.coldDownGroup;
			useType.coolDownTime = item.coldDownTime;
			useType.spellTime = item.schedule;
			useType.distance = item.distance;
			if(useType.distance!=-1){
				useType.distance = item.distance * 8;
			}
			template.useType = useType;
			ItemEffect[] effects = translateEffects(item.effects);
			for (ItemEffect eff : effects) {
			    if (eff instanceof QuestItemEffect) {
			        template.triggerQuest = (QuestItemEffect)eff;
			    }
			}
			if(effects.length==0){
				template.useType.effect = new EmptyItemEffect();
			}
			else if(effects.length==1){
				template.useType.effect = effects[0];
			}else{
				template.useType.effect = new ComboItemEffect(effects);
			}
		}
		if(item.timeType==Item.TIME_TYPE_ABSOLUTELY){
			template.itemValid = new ItemValid(ItemValid.TYPE_ABSOLUTELY,item.time);
		}
		else if(item.timeType==Item.TIME_TYPE_RELATIVELY){
			template.itemValid = new ItemValid(ItemValid.TYPE_RELATIVELY,item.time);
		}

		return template;
	}
	
	public static byte[] getEquipmentsDBBytes(GameItem[] equs){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(500);
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(VERSION);
			dos.write(equs.length);
			for(GameItem item:equs){
				if(item==null){
					dos.write(0);
				}else{
					dos.write(1);
					dos.write(getGameItemDBBytes(item));
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static byte[] getEquipmentsDBBytes(Equipments equs){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(500);
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(VERSION);
			dos.write(equs.equs.length);
			for(GameItem item:equs.equs){
				if(item==null){
					dos.write(0);
				}else{
					dos.write(1);
					dos.write(getGameItemDBBytes(item));
//					dos.writeInt(item.template.id);
//					dos.write(item.leaveUseCount);
//					dos.writeInt(item.validTime);
//					dos.writeInt(item.bindInstance);
//					dos.writeShort(item.duration);
//					dos.writeInt(item.instanceId);
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static byte[] getHorseEquipmentsDBBytes(HorseEquipments equs){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(500);
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.write(VERSION);
			dos.write(equs.equs.length);
			for(GameItem item:equs.equs){
				if(item==null){
					dos.write(0);
				}else{
					dos.write(1);
					dos.write(getGameItemDBBytes(item));
				}
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		return baos.toByteArray();		
	}
	
	/**
	 * 获得背包的数据库流
	 * @param bag
	 * @return
	 */
	public static byte[] getBagDBBytes(Bag bag) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(VERSION); //版本号
			dos.writeInt(bag.size);
			for (BagGrid grid : bag.grids) {
				dos.write(grid.id);
				dos.write(grid.count);
				if (grid.item != null) {
					dos.write(getGameItemDBBytes(grid.item));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static byte[] getBagDBBytes(TransactionBag bag){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(3); //版本号
			dos.writeInt(bag.size);
			dos.writeInt(bag.addedSize);
			for (TransactionBagGrid grid : bag.grids) {
				dos.write(grid.id);
				dos.write(grid.count);
				if (grid.item != null&&grid.count != 0) {
					dos.write(getGameItemDBBytes(grid.item));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static byte[] getGameItemDBBytes(GameItem item){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(item.template.id);
			dos.write(item.leaveUseCount);
			dos.writeInt(item.validTime);
			dos.writeInt(item.bindInstance);
			if(item.template.isEquipment())
				dos.writeShort(item.duration);
			dos.writeInt(item.instanceId);
			if(item.object==null){
				dos.writeInt(0);
			}else{
				Serializer s = PersistenceManager.serializer(item.object.serializerClass());
				dos.writeInt(s.getId());
				dos.write(s.serialize(item.object));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e){
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static GameItem getGameItemFromDB(DataInputStream dis,int version){
		GameItem item;
		try {
			int itemId = dis.readInt();
			ItemTemplate template = ObjectAccessor.getItemTemplate(itemId);
			if (template == null){
				log.error("[ILLEGALITEM]ITEMID[" + itemId + "]");
				throw new IllegalArgumentException();
			}
			int leaveUseCount = dis.read();
			int validTime = dis.readInt();
			int bindInstance = 0;
			if (version == 1) {
				boolean bind = dis.readBoolean();
				bindInstance = bind ? 0 : -1;
			}
			else if(version>=2){
				bindInstance = dis.readInt();
			}
			int duration = 0;
			if(template.isEquipment()){
				duration = dis.readShort();
			}
			int instanceId = dis.readInt();
			if(template.newInstance&&instanceId==-1){
				instanceId = Server.server.getServiceRegistry().getSleepyCatService().generatorItemId();
			}
			item = new GameItem(template,instanceId);
			item.instanceId = instanceId;
			item.bindInstance = bindInstance;
			item.leaveUseCount = leaveUseCount;
			item.duration = duration;
			item.validTime = validTime;
			if (version >= 2) {
				int marshallerId = dis.readInt();
				Marshaller m = PersistenceManager.marshaller(marshallerId);
				if (m != null) {
					item.object = m.marshaller(dis,item);
				}
			}
			return item;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static GameItem getGameItemFromDB(byte[] bytes,int version){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		return getGameItemFromDB(dis,version);
	}
	
	public static Equipments getEquipmentsFromDB(byte[] bytes, Player owner){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		Equipments equs = new Equipments(owner);
		try{
			int version = dis.readByte(); //version;
			int count = dis.readByte();
			for(int i=0;i<count;i++){
				int c = dis.readByte();
				if(c==1){
					GameItem item = getGameItemFromDB(dis,version);
					equs.equs[i] = item;
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return equs;
	}
	
	public static HorseEquipments getHorseEquipmentsFromDB(DataInputStream dis, Horse owner){
		HorseEquipments equs = new HorseEquipments(owner);
		try{
			int version = dis.readByte(); //version;
			int count = dis.readByte();
			for(int i=0;i<count;i++){
				int c = dis.readByte();
				if(c==1){
					GameItem item = getGameItemFromDB(dis,version);
					equs.equs[i] = item;
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return equs;
	}
	
	public static GameItem[] getAttendantEquipmentsFromDB(DataInputStream dis, Attendant owner){
		GameItem[] equs = new GameItem[10];
		try{
			int version = dis.readByte(); //version;
			int count = dis.readByte();
			for(int i=0;i<count;i++){
				int c = dis.readByte();
				if(c==1){
					GameItem item = getGameItemFromDB(dis,version);
					equs[i] = item;
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return equs;
	}
	
	/**
	 * 从数据库流生成背包
	 * @param bytes
	 * @param owner
	 * @return
	 */
	public static Bag getBagFromDB(byte[] bytes, Player owner) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		Bag bag = null;
		try {
			int version = dis.readByte(); // version
			int size = dis.readInt();
			bag = new Bag(owner, size);
			for (int i = 0; i < size; i++) {
				int id = dis.readByte();
				int count = dis.readByte();
				if (count > 0) {
					GameItem item = getGameItemFromDB(dis,version);
					BagGrid grid = bag.grids.get(id);
					grid.item = item;
					grid.count = count;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bag;
	}
	
	public static TransactionBag getTransactionBagFromDB(byte[] bytes, Player owner){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		TransactionBag bag = null;
		try {
			int version = dis.readByte(); // version
			int size = dis.readInt();
			int addedSize = 0;
			if(version>=3){
				addedSize = dis.readInt();
			}
			bag = new TransactionBag(owner, size, addedSize);
start:
			for (int i = 0; i < bag.getSize(); i++) {
				int id = dis.readByte();
				int count = dis.readByte();
				if (count > 0) {
					GameItem item = getGameItemFromDB(dis,version);
					TransactionBagGrid grid = bag.grids.get(id);
					if (item.instanceId != GameItem.GENERAL_INSTANCEID) {
						Set<Integer> indexes = bag
								.getIndexSet(item.template.id);
						if (indexes != null) {
							for (int index : indexes) {
								TransactionBagGrid other = bag.getGrid(index);
								if (other.item != null
										&& other.item.instanceId == item.instanceId) {
									log
											.info("[BUILDBAGINSTANCEERROR]"
													+ LogUtil
															.getPlayerLogString(owner)
													+ LogUtil
															.getGameItemString(
																	item, 1));
									bag.addIndex(-1, id);
									continue start;
								}
							}
						}
					}
					grid.item = item;
					grid.count = count;
					bag.removeIndex(-1, grid.id);
					bag.addIndex(grid.item.template.id, grid.id);
				}else{
					bag.addIndex(-1, id);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bag;
	}
	
	public static TransactionBag getTransactionBagFromDB0(byte[] bytes){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		TransactionBag bag = null;
		try {
			int version = dis.readByte(); // version
			int size = dis.readInt();
			int addedSize = 0;
			if(version>=3){
				addedSize = dis.readInt();
			}
			bag = new TransactionBag(size, addedSize);
start:
			for (int i = 0; i < bag.getSize(); i++) {
				int id = dis.readByte();
				int count = dis.readByte();
				if (count > 0) {
					GameItem item = getGameItemFromDB(dis,version);
					TransactionBagGrid grid = bag.grids.get(id);
					if (item.instanceId != GameItem.GENERAL_INSTANCEID) {
						Set<Integer> indexes = bag
								.getIndexSet(item.template.id);
						if (indexes != null) {
							for (int index : indexes) {
								TransactionBagGrid other = bag.getGrid(index);
								if (other.item != null
										&& other.item.instanceId == item.instanceId) {
									bag.addIndex(-1, id);
									continue start;
								}
							}
						}
					}
					grid.item = item;
					grid.count = count;
					bag.removeIndex(-1, grid.id);
					bag.addIndex(grid.item.template.id, grid.id);
				}else{
					bag.addIndex(-1, id);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bag;
	}
	
	public static TransactionBag getTransactionBagFromDB(byte[] bytes, Fame owner){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		TransactionBag bag = null;
		try {
			int version = dis.readByte(); // version
			int size = dis.readInt();
			int addedSize = 0;
			if(version>=3){
				addedSize = dis.readInt();
			}
			bag = new TransactionBag(owner, size, addedSize);
start:
			for (int i = 0; i < bag.getSize(); i++) {
				int id = dis.readByte();
				int count = dis.readByte();
				if (count > 0) {
					GameItem item = getGameItemFromDB(dis,version);
					TransactionBagGrid grid = bag.grids.get(id);
					if (item.instanceId != GameItem.GENERAL_INSTANCEID) {
						Set<Integer> indexes = bag
								.getIndexSet(item.template.id);
						if (indexes != null) {
							for (int index : indexes) {
								TransactionBagGrid other = bag.getGrid(index);
								if (other.item != null
										&& other.item.instanceId == item.instanceId) {
									Player p = FameService.statuePlayer.get(owner.playerId);
									log
											.info("[BUILDBAGINSTANCEERROR]"
													+ LogUtil
															.getPlayerLogString(p)
													+ LogUtil
															.getGameItemString(
																	item, 1));
									bag.addIndex(-1, id);
									continue start;
								}
							}
						}
					}
					grid.item = item;
					grid.count = count;
					bag.removeIndex(-1, grid.id);
					bag.addIndex(grid.item.template.id, grid.id);
				}else{
					bag.addIndex(-1, id);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bag;
	}
	
	public static boolean checkUseTarget(Unit source,GameItem item,GameObject target){
		int targetType = item.template.useType.targetType;
		if(targetType==UseType.TARGET_ALL)
			return true;
		if(targetType==UseType.TARGET_NOTARGET)
			return true;
		if(targetType==UseType.TARGET_SELF&&source==target)
			return true;
		if(targetType==UseType.TARGET_ENEMY&&source.isEnemy(target))
			return true;
		if(targetType==UseType.TARGET_PARTY&&!source.isEnemy(target))
			return true;
		return false;
	}
	
	public static ItemEffect[] translateEffects(List<com.pip.sanguo.data.item.ItemEffect> effects){
		List<ItemEffect> ret = new ArrayList<ItemEffect>(effects.size());
		IntArray items = new IntArray();
		IntArray counts = new IntArray();
		for(com.pip.sanguo.data.item.ItemEffect effect:effects){
			if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_ADDHP){
				AddHpItemEffect e = new AddHpItemEffect(effect.getIntParam("amount"));
				ret.add(e);
			}
			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_ADDMP){
				AddMpItemEffect e = new AddMpItemEffect(effect.getIntParam("amount"));
				ret.add(e);
			}
			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_GETITEM){
				items.add(effect.getIntParam("item"));
				counts.add(effect.getIntParam("count"));
			}
			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_HORSE){
				RideItemEffect e = new RideItemEffect();
				ret.add(e);
			}
			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_QUEST){
				QuestItemEffect e = new QuestItemEffect(effect.getIntParam("quest"), effect.getIntParam("questshu"), effect.getIntParam("questwu"));
				ret.add(e);
			}
//			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_RELIVE){
//				ReliveItemEffect e = new ReliveItemEffect(effect.getIntParam("percent"));
//				ret.add(e);
//			}
			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_SKILL){
				SkillItemEffect e = new SkillItemEffect(effect.getIntParam("skill"),effect.getIntParam("level"),effect.getIntParam("usebuffs")==0?false:true);
				ret.add(e);
			}
			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_LEARNSKILL){
				BookSkillItemEffect e = new BookSkillItemEffect(effect.getIntParam("skill"),effect.getIntParam("level"));
				ret.add(e);
			}
			else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_TITLE){
				AddTitleItemEffect e = new AddTitleItemEffect(effect.getIntParam("title"));
				ret.add(e);
			}
			else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_GETHORSE) {
				String s = effect.param.get("horsetype");
				String[] ss = s.split(",");
				int[] types = new int[ss.length];
				for(int i=0;i<ss.length;i++){
					types[i] = Integer.parseInt(ss[i]);
				}
				HorseItemEffect e = new HorseItemEffect(effect
						.getIntParam("imageid"), types, effect.getIntParam("level"),effect.param.get("name"));
				ret.add(e);
			} 
//			else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_GAIN_CARD) {
////				int id = effect.getIntParam("card");
////				DataService ds = Server.server.getServiceRegistry().getDataService();
////				Card cd = ds.data.findCard(id);
////				if(cd != null){
////					CardEffect e = new CardEffect(cd);
////					ret.add(e);
////				}
//			} 
			else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_DROPGROUP) {
			    int dg1 = effect.getIntParam("dropgroup1");
			    int dg2 = effect.getIntParam("dropgroup2");
			    String var = effect.param.get("controlvar");
			    int sc = effect.getIntParam("switchcount");
			    int mail = effect.getIntParam("mail");
			    int ui = effect.getIntParam("useitem");
			    int uic = effect.getIntParam("useitemcount");
			    DropItemEffect e = new DropItemEffect(dg1, dg2, var, sc, mail == 1, ui, uic);
			    ret.add(e);
			}
			else if (effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_FEEDHORSE){
				HorseFeedItemEffect e = new HorseFeedItemEffect();
				ret.add(e);
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_TELEPORT) {
			    int[] weiLocation = new int[] { 
			            effect.getIntParam("weimapid"), effect.getIntParam("weix"), effect.getIntParam("weiy")
			    };
                int[] shuLocation = new int[] { 
                        effect.getIntParam("shumapid"), effect.getIntParam("shux"), effect.getIntParam("shuy")
                };
                int[] wuLocation = new int[] { 
                        effect.getIntParam("wumapid"), effect.getIntParam("wux"), effect.getIntParam("wuy")
                };
			    TeleportEffect e = new TeleportEffect(weiLocation, shuLocation, wuLocation);
			    ret.add(e);
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_FORMULA) {
                int fid = effect.getIntParam("formula");
                ret.add(new AddFormulaEffect(fid));
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_MARRIAGETELEPORT) {
                ret.add(new MarriageTeleportEffect());
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_DIVORCE) {
                ret.add(new DivorceEffect());
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_EXTENDBAG) {
                int count = effect.getIntParam("count");
                ret.add(new ExtendBagEffect(count));
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_REFRESHSKILLPOINTS){
				ret.add(new RefreshSkillPointItemEffect());
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_EXTENDSTORAGE){
				int count = effect.getIntParam("count");
				ret.add(new ExtendDepotEffect(count));
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_EXP) {
			    float amount = effect.getFloatParam("amount");
			    String table = effect.param.get("table");
			    ret.add(new GetExpEffect(amount, parseValueTable(table)));
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_HORSEEXP) {
                float amount = effect.getFloatParam("amount");
                String table = effect.param.get("table");
                ret.add(new GetHorseExpEffect(amount, parseValueTable(table)));
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_HONOR) {
                float amount = effect.getFloatParam("amount");
                String table = effect.param.get("table");
                ret.add(new GetHonorEffect(amount, parseValueTable(table)));
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_CREDIT) {
                float amount = effect.getFloatParam("amount");
                String table = effect.param.get("table");
                ret.add(new GetCreditEffect(amount, parseValueTable(table)));
            } else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_MONEY) {
                float amount = effect.getFloatParam("amount");
                String table = effect.param.get("table");
                ret.add(new GetMoneyEffect(amount, parseValueTable(table)));
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_REFRESH_PROPERTY){
				ret.add(new RefreshPropertyPointsItemEffect());
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_REPAIR_EQUIPMENTS){
				ret.add(new RepairEquipmentsItemEffect());
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_EXTEND){
				String s = effect.param.get("script");
				String[] ss = s.split("\\s+");
				if(ss.length>2)
					throw new IllegalArgumentException("");
				String args = "";
				String script = ss[0];
				if(ss.length==2)
					args = ss[1];
				ret.add(new ScriptEffect(script,args));
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_IMONEY_CARD){
				ret.add(new IMoneyCardEffect());
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_RIDDLE){
				ret.add(new RiddleItemEffect());
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_UNSIGNUP){
				ret.add(new UnSignupItemEffect());
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_CLICKEXP){
			    ret.add(new GetClickExpEffect());
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_TONGBATTLECAR){
			    ret.add(new TongBattleCarEffect());
			} else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_ADDKINGHP){
				AddKingHpItemEffect e = new AddKingHpItemEffect(effect.getIntParam("rate"));
				ret.add(e);
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_ACTIVEPOWER) {
				ret.add(new ActivePowerEffect(effect.getIntParam("value")));
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_CLICKGETMONEY){
				ret.add(new GetClickMoneyEffect(effect.getIntParam("money")));
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_USEDECITEM){
				ret.add(new UseDecItemEffect(effect.getIntParam("itemid")));
			} else if(effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_ACTIVITY){
			    TorchEffect e = new TorchEffect(effect.getIntParam("type"));
                ret.add(e);
			} else if (effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_TOWERDEFEND){
			    ret.add(new TDToTowerEffect());
			} else if(effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_DECIMONEY){
				DecImoneyEffect e = new DecImoneyEffect(effect.getIntParam("itemid"));
				ret.add(e);
			} else if(effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_EXTEND_HORSE_BAG_MAX){
				ExtHorseMaxItemEffect e = new ExtHorseMaxItemEffect(effect.getIntParam("value"));
				ret.add(e);
			} else if(effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_ATTENDANT){
				ret.add(new AttendantItemEffect(effect.getIntParam("attendant")));
			} else if(effect.effectType == com.pip.sanguo.data.item.ItemEffect.TYPE_EXTENDATTENDANBAG){
				ret.add(new ExtendAttendantBagEffect(effect.getIntParam("attendantbag")));
			} else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_ATTENDANTLEARNSKILL){
				ret.add(new AttendantBookSkillEffect(effect.getIntParam("skill"), effect.getIntParam("level")));
            } else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_ATTENDANT_ADDHP){
                ret.add(new AttendantAddHpItemEffect(effect.getIntParam("attendanthp"),effect.getIntParam("attendantmp")));
            } else if(effect.effectType==com.pip.sanguo.data.item.ItemEffect.TYPE_USESKILL_REFATT){
                ret.add(new RefAttendantSkillItemEffect(effect.getIntParam("skill0"),
                        effect.getIntParam("level0"),effect.getIntParam("skill1"),
                        effect.getIntParam("level1"),effect.getIntParam("skill2"),
                        effect.getIntParam("level2"),effect.getIntParam("usebuffs")==0?false:true));
            }
		}
		if(!items.isEmpty()){
			AddItemEffect e = new AddItemEffect(items.toArray(),counts.toArray());
			ret.add(0, e);
		}
		ItemEffect[] ra = new ItemEffect[ret.size()];
		ret.toArray(ra);
		return ra;
	}
	
	/*
	 * 解析级别和数值的对应表。表的格式为x,x,x,x,x,x。第一个值对应1级。
	 */
	private static int[] parseValueTable(String str) {
        if (str == null || str.length() == 0) {
             return null;
        }
        String[] secs = str.split(",");
        int[] ret = new int[100];
        for (int i = 0; i < ret.length && i < str.length(); i++) {
            try {
                ret[i] = Integer.parseInt(secs[i]);
            } catch (Exception e) {
            }
        }
        return ret;
	}
	
	private static SuiteEffects translateSuiteConfig(SuiteConfig config) {
	    SuiteEffect[] se = new SuiteEffect[config.effects.size()];
        int i = 0;
        for (com.pip.sanguo.data.equipment.SuiteConfig.SuiteEffect effect : config.effects) {
            SuiteEffect effect2 = new SuiteEffect();
            effect2.setBuff(BuffUtil.createSuiteBuff(effect.buffID, effect.buffLevel));
            effect2.setCount(effect.count);
            se[i] = effect2;
            i++;
        }
        SuiteEffects effects = new SuiteEffects(config.id, config.title, se);
        for (com.pip.sanguo.data.equipment.Equipment equ : config.equipments) {
            ItemTemplate template = ObjectAccessor.getItemTemplate(equ.id);
            template.equipment.suiteEffects = effects;
            effects.addEquip(equ.id);
        }
        return effects;
	}

    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof Equipment) {
            // 添加装备
            Equipment equ = (Equipment)obj;
            ItemTemplate template = ItemUtil.translateItemTemplate(equ);
            ObjectAccessor.addItemTemplate(template);
        } else if (obj instanceof Item) {
            // 添加物品
            Item item = (Item)obj;
            ItemTemplate template = ItemUtil.translateItemTemplate(item);
            ObjectAccessor.addItemTemplate(template);
        } else if (obj instanceof SuiteConfig) {
            // 添加套装
            SuiteConfig config = (SuiteConfig)obj;
            SuiteEffects effects = translateSuiteConfig(config);
            ObjectAccessor.suites.put(effects.getID(), effects);
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof Item) {
            // 删除装备/物品
            ObjectAccessor.itemTemplates.remove(obj.id);
        } else if (obj instanceof SuiteConfig) {
            // 删除套装
            SuiteEffects eff = ObjectAccessor.suites.remove(obj.id);
            if (eff != null) {
                for (int equID : eff.getEquips()) {
                    ItemTemplate it = ObjectAccessor.getItemTemplate(equID);
                    if (it != null && it.equipment != null) {
                        it.equipment.suiteEffects = null;
                    }
                }
            }
        }
    }
    
    /**
     * 对象即将被修改通知。
     * @param obj 修改前的对象
     */
    public void dataObjectChanging(DataObject obj) {
    }
    
    /**
     * 对象被修改通知。
     * @param newobj 修改后的新对象
     */
    public void dataObjectChanged(DataObject newobj) {
        if (newobj instanceof Equipment) {
            // 装备属性修改
            Equipment equ = (Equipment)newobj;
            ItemTemplate template = ItemUtil.translateItemTemplate(equ);
            ItemTemplate oldTemplate = ObjectAccessor.getItemTemplate(equ.id);
            if (oldTemplate != null) {
                oldTemplate.copyFrom(template);
            }
        } else if (newobj instanceof Item) {
            // 物品属性修改
            Item item = (Item)newobj;
            ItemTemplate template = ItemUtil.translateItemTemplate(item);
            ItemTemplate oldTemplate = ObjectAccessor.getItemTemplate(item.id);
            if (oldTemplate != null) {
                oldTemplate.copyFrom(template);
            }
        } else if (newobj instanceof SuiteConfig) {
            // 套装属性修改
            SuiteEffects se = ObjectAccessor.suites.get(newobj.id);
            if (se != null) {
                // 删除旧的
                for (int equID : se.getEquips()) {
                    ItemTemplate it = ObjectAccessor.getItemTemplate(equID);
                    if (it != null && it.equipment != null) {
                        it.equipment.suiteEffects = null;
                    }
                }
                
                // 替换新的
                SuiteEffects effects = translateSuiteConfig((SuiteConfig)newobj);
                ObjectAccessor.suites.put(effects.getID(), effects);
            }
        }
    }
    
    public static String parseUseConfirm(String useConfirm){
		try {
			if(useConfirm.contains("$(")){
				int beginIndex = useConfirm.indexOf("$");
				int endIndex = useConfirm.indexOf(")");
				String valueStr = useConfirm.substring(beginIndex+2, endIndex);
				int itemId = Integer.parseInt(valueStr);
				float price = Server.server.getServiceRegistry().getShopService().getItemPrice(itemId)/36f;
				String value = useConfirm.substring(0, beginIndex)+price+useConfirm.substring(endIndex+1);
				if(value.contains("&[")){
					int beginIndex0 = value.indexOf("&");
					int endIndex0 = value.indexOf("*");
					int beginIndex1 = value.indexOf("*");
					int endIndex1 = value.indexOf("]");
					String valueStr0 = value.substring(beginIndex0+2, endIndex0);
					String valueStr1 = value.substring(beginIndex1+1, endIndex1);
					int itemId0 = Integer.parseInt(valueStr0);
					float price0 = Server.server.getServiceRegistry().getShopService().getItemPrice(itemId0)/36f;
					float ratio = new Float(valueStr1);
					float v = price0 * ratio;
					String value0 = value.substring(0, beginIndex0)+v+value.substring(endIndex1+1);
					return value0;
				}
				return value;
			}
		} catch (Exception e) {
			return useConfirm;
		}
		return useConfirm;
	}
    
}


