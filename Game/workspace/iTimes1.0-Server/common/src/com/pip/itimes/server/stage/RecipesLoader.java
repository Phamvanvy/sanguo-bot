package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;
import java.util.Iterator;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class RecipesLoader {
    public RecipesLoader(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadRecipes(doc);
    }

    private void loadRecipes(Document doc){
        Element root = doc.getRootElement();
         for(Iterator i=root.elementIterator("Recipe");i.hasNext();){
             Element node = (Element)i.next();
             Recipe recipe = new Recipe();
             int id = Integer.parseInt(node.attributeValue("id"));
             byte type = Byte.parseByte(node.attributeValue("type"));
             String name = node.attributeValue("name");
             short level = Short.parseShort(node.attributeValue("level"));
             short skillLevel = Short.parseShort(node.attributeValue("skillLevel"));
             boolean playGame = Byte.parseByte(node.attributeValue("playgame"))==0?false:true;
             int productivity = Integer.parseInt(node.attributeValue("productivity"));
             int money = Integer.parseInt(node.attributeValue("money"));
             recipe.setId(id);
             recipe.setType(type);
             recipe.setName(name);
             recipe.setLevel(level);
             recipe.setSkillLevel(skillLevel);
             recipe.setPlayeGame(playGame);
             recipe.setProducitivity(productivity);
             recipe.setMoney(money);
             for(Iterator j=node.elementIterator("Resource");j.hasNext();){
                 Element el = (Element)j.next();
                 int rId = Integer.parseInt(el.attributeValue("id"));
                 IItemTemplate template = Items.getTemplate(rId);
                 byte count = Byte.parseByte(el.attributeValue("count"));
                 recipe.addResource(template,count);
             }
             for(Iterator j=node.elementIterator("Product");j.hasNext();){
                 Element el = (Element)j.next();
                 byte pType = Byte.parseByte(el.attributeValue("type"));
                 int pId = Integer.parseInt(el.attributeValue("id"));
                 byte count = Byte.parseByte(el.attributeValue("count"));
                 IItemTemplate template = Items.getTemplate(pId);
                 recipe.addProduct(template,count);
             }
             String desc = node.elementTextTrim("Desc");
             recipe.setDesc(desc);
             Recipes.addRecipe(recipe);
             ExtendedItemTemplate item = new ExtendedItemTemplate();
             item.setBindType((byte)0);
             item.setCanUse(false);
             item.setAutoUse(false);
             item.setAutoUseMessage("");
             item.setEffects(new Effect[0]);
             item.setItemId(recipe.getId()+4000000);
             item.setName(recipe.getName()+"链接");
             item.setPrice(0);
             item.setDesc("可以用来生成"+recipe.getName());
             Items.addTemplate(item);
         }
    }
}
