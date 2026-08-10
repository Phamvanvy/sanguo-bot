package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class AbilitiesLoader {
    private Logger log = Logger.getLogger(AbilitiesLoader.class);

    public AbilitiesLoader(File file) throws Exception{
        log.info("loading abilities from"+file.getName());
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        readAbilitites(document);
        log.info("load abilities complete");
    }

    private void readAbilitites(Document document){
       Element root = document.getRootElement();
       for(Iterator i=root.elementIterator("skill");i.hasNext();){
           Element node = (Element)i.next();
           Ability ability = new Ability();
           Attribute attribute = node.attribute("id");
           int id = Integer.parseInt(attribute.getValue());
           ability.setId(id);
           attribute = node.attribute("name");
           String name = attribute.getStringValue();
           ability.setName(name);
           attribute = node.attribute("skillType");
           byte type = Byte.parseByte(attribute.getValue());
           ability.setType(type);
           attribute = node.attribute("effect");
           int effect = Integer.parseInt(attribute.getValue());
           ability.setEffect(effect);
           attribute = node.attribute("state");
           int status = Integer.parseInt(attribute.getValue());
           ability.setStatus(status);
           attribute = node.attribute("position");
           int position = Integer.parseInt(attribute.getValue());
           ability.setPosition(position);
           attribute = node.attribute("coolDown");
           int cdTime = Integer.parseInt(attribute.getValue());
           ability.setCDTime(cdTime);
           attribute = node.attribute("coolDownID");
           int cd = Integer.parseInt(attribute.getValue());
           ability.setCD(cd);
           //增加技能的等级读取
           attribute = node.attribute("maxlevel");
           int maxLevel = Integer.parseInt(attribute.getValue());
           ability.setMaxLevel(maxLevel);
           
           attribute = node.attribute("level");
           int level = Integer.parseInt(attribute.getValue());
           ability.setLevel(level);
           attribute = node.attribute("skillValue1");
           int value1 = Integer.parseInt(attribute.getValue());
           ability.setValue1(value1);
           attribute = node.attribute("skillValue2");
           int value2 = Integer.parseInt(attribute.getValue());
           ability.setValue2(value2);
           attribute = node.attribute("hitRate");
           int hit = Integer.parseInt(attribute.getValue());
           ability.setHit(hit);
           attribute = node.attribute("effectTurn");
           int effectTime = Integer.parseInt(attribute.getValue());
           ability.setEffectTime(effectTime);
           attribute = node.attribute("needMp");
           int mana = Integer.parseInt(attribute.getValue());
           ability.setMana(mana);
           attribute = node.attribute("needSp");
           int requiredLevel = Integer.parseInt(attribute.getValue());
           ability.setRequiredLevel(requiredLevel);
           attribute = node.attribute("price");
           int price = Integer.parseInt(attribute.getValue());
           int enmity = Integer.parseInt(node.attributeValue("enmity"));
           int adjust = Integer.parseInt(node.attributeValue("adjust"));
           int enmityType = Integer.parseInt(node.attributeValue("enmityType"));
           int arithmetic = Integer.parseInt(node.attributeValue("arithmetic"));
           ability.setEnmity(enmity);
           ability.setAdjust(adjust);
           ability.setEnmityType(enmityType);
           ability.setPrice(price);
           ability.setDesc(node.getText());
           ability.setArithmetic(arithmetic);
           Ability.addAbility(ability);
       }
   }

}
