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
public class RecipesNewLoader {
    public RecipesNewLoader(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadRecipesNew(doc);
    }

    private void loadRecipesNew(Document doc){
        Element root = doc.getRootElement();
         for(Iterator i=root.elementIterator("Recipe");i.hasNext();){
             Element node = (Element)i.next();
             RecipeNew recipeNew = new RecipeNew();
             int id = Integer.parseInt(node.attributeValue("id"));
             byte type = Byte.parseByte(node.attributeValue("type"));
             String name = node.attributeValue("name");
             short level = Short.parseShort(node.attributeValue("level"));
             short skillLevel = Short.parseShort(node.attributeValue("skillLevel"));
             boolean playGame = Byte.parseByte(node.attributeValue("playgame"))==0?false:true;
             int productivity = Integer.parseInt(node.attributeValue("productivity"));
             int money = Integer.parseInt(node.attributeValue("money"));
             int equType = Integer.parseInt(node.attributeValue("equType"));
             int color = Integer.parseInt(node.attributeValue("color"));
             int itemId = Integer.parseInt(node.attributeValue("itemId"));
             recipeNew.setId(id);
             recipeNew.setType(type);
             recipeNew.setName(name);
             recipeNew.setLevel(level);
             recipeNew.setSkillLevel(skillLevel);
             recipeNew.setPlayeGame(playGame);
             recipeNew.setProductivity(productivity);
             recipeNew.setMoney(money);
             recipeNew.setEquType(equType);
             recipeNew.setColor(color);
             recipeNew.setItemId(itemId);
             for(Iterator j=node.elementIterator("Resource");j.hasNext();){
                 Element el = (Element)j.next();
                 int Id = Integer.parseInt(el.attributeValue("id"));
                 byte typeResource = Byte.parseByte(el.attributeValue("type"));
                 IItemTemplate template = Items.getTemplate(Id);
                 byte count = Byte.parseByte(el.attributeValue("count"));
                 byte consumeMode = Byte.parseByte(el.attributeValue("consumeMode"));
                 recipeNew.addResource(template,count,consumeMode);
             }
             String desc = null;
             int productColor = 0;
             for(Iterator j=node.elementIterator("Product");j.hasNext();){
                 Element el = (Element)j.next();
                 byte pType = Byte.parseByte(el.attributeValue("type"));
                 int pId = Integer.parseInt(el.attributeValue("id"));
                 byte count = Byte.parseByte(el.attributeValue("count"));
                 int productType = Integer.parseInt(el.attributeValue("color"));
                 productColor = Utils.CLR_EQUIP[productType];
                 IItemTemplate template = Items.getTemplate(pId);
                 recipeNew.addProduct(template,count);
             }
             desc = node.elementTextTrim("Desc");
        	 int index = desc.indexOf("：");		//非英文状态
        	 String subStr1 = desc.substring(0, index + 1);		//截取生成:
        	 String subStr2 = desc.substring(index + 1);
        	 int index2 = desc.indexOf("：", index + 1);
        	 String subStr3 = desc.substring(index + 1,index2 - 2);		//截取生成物品明细
        	 String subStr4 = desc.substring(index2 - 2,index2 + 1);	//截取原料：
        	 String subStr5 = desc.substring(index2 + 1);				//截取原料明细
             recipeNew.setDesc(subStr1 + "\n"+ subStr3 + "\n\n" + subStr4 + "\n" + "<c" + productColor + ">" + subStr5 + "</c>");
             IItemTemplate item2 = Items.getTemplate(itemId);
             if(item2 instanceof ExtendedItemTemplate){            	 
            	 ExtendedItemTemplate exItem2 = (ExtendedItemTemplate)item2;
            	 exItem2.setDesc(subStr1 + "\n"+ subStr3 + "\n\n" + subStr4 + "\n" + "<c" + productColor + ">" + subStr5 + "</c>");
             }
             RecipesNew.addRecipeNew(recipeNew);
             ExtendedItemTemplate item = new ExtendedItemTemplate();
             item.setBindType((byte)0);
             item.setCanUse(false);
             item.setAutoUse(false);
             item.setAutoUseMessage("");
             item.setEffects(new Effect[0]);
             item.setItemId(recipeNew.getId()+4000000);
             item.setName(recipeNew.getName()+"链接");
             item.setPrice(0);
             item.setDesc("可以用来生成"+recipeNew.getName());
             Items.addTemplate(item);
         }
    }
}