package com.pip.itimes.server.world;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.world.battle.BattleSprite;

public class MercenaryConstants {
	public static boolean open = false;
	
	/**
	 * 系统佣兵数据
	 * KEY:profession
	 * VALUE:佣兵数据
	 */
	private static HashMap<Integer, MercenaryData> mapMercenaryData = new HashMap<Integer, MercenaryData>();
	
	/**
	 * 系统佣兵商店列表
	 */
	private static ArrayList<MercenaryShop> listSystemShop = new ArrayList<MercenaryShop>();
	
	/**
	 * 排除使用的技能
	 */
	private static HashMap<Integer, Integer> exceptSkill = new HashMap<Integer, Integer>();
	
	public static void loadMercenaryConstants(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        load(root);
	}
	
	public static ArrayList<MercenaryShop> getSystemShop(){
		return listSystemShop;
	}
	
	public static MercenaryShop getMercenaryShop(int index){
		if(index < 0 || index >= listSystemShop.size()){
			return null;
		}
		return listSystemShop.get(index);
	}
	
	private static void load(Element root){
		Element setup = root.element("Setup");
		open = Integer.parseInt(setup.attributeValue("open")) == 0 ? false : true;
//		if(open){
			synchronized (mapMercenaryData) {
				mapMercenaryData.clear();
				for (Iterator<Element> mercenarys = root.elementIterator("Mercenary"); mercenarys.hasNext();) {
					Element mercenary = mercenarys.next();
					boolean valid = mercenary.attributeValue("valid").equals("true") ? true : false;
					int id = Integer.parseInt(mercenary.attributeValue("id"));
					byte profession = Byte.parseByte(mercenary.attributeValue("profession"));
					int attr_str = Integer.parseInt(mercenary.attributeValue("attr_str"));
					int attr_agi = Integer.parseInt(mercenary.attributeValue("attr_agi"));
					int attr_vit = Integer.parseInt(mercenary.attributeValue("attr_vit"));
					int attr_int = Integer.parseInt(mercenary.attributeValue("attr_int"));
					int attr_pdef = Integer.parseInt(mercenary.attributeValue("attr_pdef"));
					ArrayList<Short> listSkill = new ArrayList<Short>();
					Element skills = mercenary.element("Skills");
					for (Iterator<Element> skill = skills.elementIterator("Skill"); skill.hasNext();) {
						Element eskill = skill.next();
						if(!eskill.attributeValue("use").equals("0")){
							listSkill.add(new Short(eskill.attributeValue("id")));
						}
					}
					
					BattleSprite bs = new BattleSprite();
					bs.initBattleData(BattleSprite.TYPE_PLAYER, 100, attr_vit, attr_str, attr_int, attr_agi, 0, 0, 0, 0, 0, 0, null,null,null,null,null);
					bs.initEquipData(new IEquipment[9]);
					
					MercenaryData md = new MercenaryData();
					md.setId(id);
					md.setProfession(profession);
					md.setAttrStr(attr_str);
					md.setAttrAgi(attr_agi);
					md.setAttrVit(attr_vit);
					md.setAttrInt(attr_int);
					md.setHP(bs.attributes[BattleSprite.ATTR_HPMAX]);
					md.setMP(bs.attributes[BattleSprite.ATTR_MPMAX]);
					md.setPMin(bs.attributes[BattleSprite.ATTR_PMIN]);
					md.setPMax(bs.attributes[BattleSprite.ATTR_PMAX]);
					md.setMMin(bs.attributes[BattleSprite.ATTR_MMIN]);
					md.setMMax(bs.attributes[BattleSprite.ATTR_MMAX]);
					md.setPDef(bs.getDefence());
					md.setMDef(bs.getMagicDefence());
					md.setPHit(bs.getShowAttribute(BattleSprite.ATTR_PHIT));
					md.setMHit(bs.getShowAttribute(BattleSprite.ATTR_MHIT));
					md.setFlee(bs.attributes[BattleSprite.ATTR_FLEE]);
					md.setPCri(bs.attributes[BattleSprite.ATTR_PCRI]);
					md.setMCri(bs.attributes[BattleSprite.ATTR_MCRI]);
					md.setNoCri(bs.attributes[BattleSprite.ATTR_NOCRI]);
					short[] skillid = new short[listSkill.size()];
					for(int i=0; i<listSkill.size(); i++){
						skillid[i] = listSkill.get(i);
					}
					md.setSkillID(skillid);
					mapMercenaryData.put(md.getId(), md);
				}
			}
			synchronized (listSystemShop) {
				listSystemShop.clear();
				Element eShopList = root.element("ShopList");
				for (Iterator<Element> shoplist = eShopList.elementIterator("Mercenary"); shoplist.hasNext();) {
					Element eshop = shoplist.next();
					MercenaryShop ms = new MercenaryShop();
					ms.setId(Integer.parseInt(eshop.attributeValue("id")));
					ms.setName(eshop.attributeValue("name"));
					ms.setPrice(Integer.parseInt(eshop.attributeValue("price")));
					ms.setProfession(Integer.parseInt(eshop.attributeValue("profession")));
					ms.setSex(Byte.parseByte(eshop.attributeValue("sex")));
					ms.setFace(Integer.parseInt(eshop.attributeValue("face")));
					ms.setFire(calcFire(ms.getId()));
					listSystemShop.add(ms);
				}
			}
			synchronized (exceptSkill) {
				exceptSkill.clear();
				Element eSkillExcept = root.element("SkillExcept");
				for (Iterator<Element> skillList = eSkillExcept.elementIterator("Skill"); skillList.hasNext();) {
					Element skill = skillList.next();
					exceptSkill.put(Integer.parseInt(skill.attributeValue("id")), null);
				}
			}
//		}
	}
	
	public static int calcFire(int id){
		if(mapMercenaryData.containsKey(id)){
			MercenaryData md = mapMercenaryData.get(id);
			if(md != null){
				return calcFire(md.getHP(), md.getMP(), md.getPMax(),
						md.getMMax(), md.getMHit(), md.getFlee(),
						md.getPCri(), md.getMCri(), md.getNoCri(), md.getPDef());
			}
		}
		return 0;
	}
	
	public static MercenaryData getMercenaryData(int id){
		if(mapMercenaryData.containsKey(id)){
			MercenaryData md = mapMercenaryData.get(id);
			return md;
		}
		return null;
	}
	
	public static int calcFire(int hp, int mp, int pmax, int mmax, int mhit, 
			int flee, int pcri, int mcri, int nocri, int pdef){
		int value = 0;
		value += hp * 3 / 10;
		value += mp * 2;
		value += pmax * 2;
		value += mmax * 8 / 10;
		value += mhit * 5;
		value += flee * 5;
		value += pcri * 5;
		value += mcri * 8 / 10;
		value += nocri * 3;
		value += pdef * 10;
		return value / 100;
	}
	
	/**
	 * 获取技能列表
	 * @param bytes
	 * @param isExcept
	 * @return
	 */
	public static short[] getSkillList(byte[] bytes, boolean isExcept){
		try{
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            DataInputStream dis = new DataInputStream(bis);
            short size = dis.readShort();
            
            HashMap<Integer, Ability> skills = new HashMap<Integer, Ability>();
            for (int i = 0; i < size; i++) {
                int abilityId = dis.readShort();
                if(isExcept && exceptSkill.containsKey(abilityId)){
                	continue;
                }
                Ability ability = Ability.getAbility(abilityId);
                if(skills.containsKey(ability.getEffect())){
                	Ability tmpAbility = skills.get(ability.getEffect());
                	if(ability.getLevel() > tmpAbility.getLevel()){
                		skills.put(ability.getEffect(), ability);
                	}
                }else{
                	skills.put(ability.getEffect(), ability);
                }
            }
            Iterator<Ability> iter = skills.values().iterator();
            short[] skillList = new short[skills.size()];
            int index = 0;
            while(iter.hasNext()){
            	Ability ability = iter.next();
            	skillList[index++] = (short)ability.getId();
            }
            return skillList;
		}catch(Exception e){
		}
		return new short[0];
	}
}
