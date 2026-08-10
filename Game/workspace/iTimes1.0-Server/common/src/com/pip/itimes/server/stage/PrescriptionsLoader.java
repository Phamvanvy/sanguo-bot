package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;

import com.pip.itimes.server.util.Utils;

import java.util.Iterator;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PrescriptionsLoader {
    public PrescriptionsLoader(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadPrescriptions(doc);
    }

    private void loadPrescriptions(Document doc){
        Element root = doc.getRootElement();
         for(Iterator i=root.elementIterator("Recipe");i.hasNext();){
             Element node = (Element)i.next();
             Prescription prescription = new Prescription();
             int id = Integer.parseInt(node.attributeValue("id"));
             byte type = Byte.parseByte(node.attributeValue("type"));
             String name = node.attributeValue("name");
             short level = Short.parseShort(node.attributeValue("level"));
             short skillLevel = Short.parseShort(node.attributeValue("skillLevel"));
             boolean playGame = Byte.parseByte(node.attributeValue("playgame"))==0?false:true;
             int productivity = Integer.parseInt(node.attributeValue("productivity"));
             int money = Integer.parseInt(node.attributeValue("money"));
             int maxPoint = Integer.parseInt(node.attributeValue("maxPoint"));
             int equType = Integer.parseInt(node.attributeValue("equType"));
             int color = Integer.parseInt(node.attributeValue("color"));
             int itemId = Integer.parseInt(node.attributeValue("itemId"));
             int costTime = Integer.parseInt(node.attributeValue("costTime"));
             prescription.setId(id);
             prescription.setType(type);
             prescription.setName(name);
             prescription.setLevel(level);
             prescription.setSkillLevel(skillLevel);
             prescription.setPlayeGame(playGame);
             prescription.setProductivity(productivity);
             prescription.setMoney(money);
             prescription.setMaxPoint(maxPoint);
             prescription.setEquType(equType);
             prescription.setColor(color);
             prescription.setItemId(itemId);
             prescription.setCostTime(costTime);
             for(Iterator j=node.elementIterator("Resource");j.hasNext();){
                 Element el = (Element)j.next();
                 int Id = Integer.parseInt(el.attributeValue("id"));
                 byte typeResource = Byte.parseByte(el.attributeValue("type"));
                 IItemTemplate template = Items.getTemplate(Id);
                 byte count = Byte.parseByte(el.attributeValue("count"));
                 byte consumeMode = Byte.parseByte(el.attributeValue("consumeMode"));
                 prescription.addResource(template,count,consumeMode);
             }
             String desc = null;
             String productColor = null;
             for(Iterator j=node.elementIterator("Product");j.hasNext();){
                 Element el = (Element)j.next();
                 byte pType = Byte.parseByte(el.attributeValue("type"));
                 int pId = Integer.parseInt(el.attributeValue("id"));
                 byte count = Byte.parseByte(el.attributeValue("count"));
                 int productType = Integer.parseInt(el.attributeValue("color"));
                 IItemTemplate template = Items.getTemplate(pId);
                 productColor = Utils.CLR_PRESCRIPTION[template.getQuality()];
                 prescription.addProduct(template,count);
             }
             desc = node.elementTextTrim("Desc");
        	 int index = desc.indexOf("：");		//非英文状态
        	 String subStr1 = desc.substring(0, index + 1);		//截取生成:
        	 String subStr2 = desc.substring(index + 1);
        	 int index2 = desc.indexOf("：", index + 1);
        	 String subStr3 = desc.substring(index + 1,index2 - 2);		//截取生成物品明细
        	 String subStr4 = desc.substring(index2 - 2,index2 + 1);	//截取原料：
        	 String subStr5 = desc.substring(index2 + 1);				//截取原料明细
        	 IItemTemplate item2 = Items.getTemplate(itemId);
        	 if (item2 != null) {
        		 String getDesc = "";
        		 prescription.setDesc(getDesc + subStr1 + "\n"+ "<c" + productColor + ">" + subStr3  + "</c>" + "\n\n" + subStr4 + "\n" + subStr5);
        		 if (item2.getDesc() != null) {
        			 getDesc = item2.getDesc();
        		 }
        		 if(item2 instanceof ExtendedItemTemplate){            	 
        			 ExtendedItemTemplate exItem2 = (ExtendedItemTemplate)item2;
        			 exItem2.setDesc(item2.getDesc() + "\n" + subStr1 + "\n"+ "<c" + productColor + ">" + subStr3  + "</c>" + "\n\n" + subStr4 + "\n" + subStr5);
        		 }
        	 }
             PrescriptionsAll.addPrescription(prescription);
         }
    }
}
