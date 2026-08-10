package com.pip.itimes.server.suit;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.stage.EquipmentTemplate;

public class PointSuitLoader{
    public PointSuitLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadSuits(doc);
    }

    private void loadSuits(Document doc){
        Element root = doc.getRootElement();
        
        if(root == null){
            return;
        }
        
        for(Iterator i=root.elementIterator("enhancesuits");i.hasNext();){
            Element suitNode = (Element)i.next();
            Suit suit = new Suit();
            
            Attribute attrSuit = suitNode.attribute("suitId");
            suit.setId(Integer.parseInt(attrSuit.getValue()));
            
            attrSuit = suitNode.attribute("name");
            suit.setName(attrSuit.getValue());
            
            attrSuit = suitNode.attribute("color");
            suit.setColor(Integer.parseInt(attrSuit.getValue()));
            
            Element effects = suitNode.element("effects");
            
            if(effects != null){
                for(Iterator j = effects.elementIterator("effect"); j.hasNext();){
                    Element effectNode = (Element)j.next();
                    SuitEffect effect = new SuitEffect();
                    
                    effect.setSuit(suit);
                    
                    Attribute attrEffect = effectNode.attribute("count");
                    effect.setCount(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("desc");
                    effect.setDesc(attrEffect.getValue());
                    
                    attrEffect = effectNode.attribute("type");
                    effect.setType(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("value");
                    effect.setValue(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("way");
                    effect.setWay(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("bout");
                    effect.setBout(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("percent");
                    effect.setPercent(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("skillParm1");
                    effect.setSkillParm1(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("skillParm2");
                    effect.setSkillParm2(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("skillPercent");
                    effect.setSkillPercent(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("skillBout");
                    effect.setSkillBout(Integer.parseInt(attrEffect.getValue()));
                    
                    attrEffect = effectNode.attribute("skillMpUse");
                    effect.setSkillMpUse(Integer.parseInt(attrEffect.getValue()));
                    
                    Element effectSkills = effectNode.element("skills");
                    
                    if(effectSkills != null){
                        for(Iterator k = effectSkills.elementIterator("skill"); k.hasNext();){
                            Element effectSkillNode = (Element)k.next();
                            
                            Attribute attrEffectSkill = effectSkillNode.attribute("id");
                            effect.addSkill(Integer.parseInt(attrEffectSkill.getValue()));
                        }
                    }
                    
                    suit.addEffect(effect);
                }
            }
            
            Suits.addSuit(suit);
        }
    }
}
