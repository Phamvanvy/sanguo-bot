package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Iterator;

/**
 * @author Jeffery
 * @version 1.0
 */
public class MaterialTypeLoader {

    public MaterialTypeLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        load(doc);
    }

    private void load(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("MaterialType");i.hasNext();){
            Element element = (Element)i.next();
            String name = element.attributeValue("name");
            byte type = Byte.parseByte(element.attributeValue("id"));
            loadItems(name,type,element);
        }
        
        //进行采集资源和锻造倍数修正
        RecipeAlter.clearGatherAlter();
        for(Iterator i=root.elementIterator("gather");i.hasNext();){
            Element element = (Element)i.next();
            int type  = Integer.parseInt(element.attributeValue("type"));
            int id = Integer.parseInt(element.attributeValue("id"));
            int count = Integer.parseInt(element.attributeValue("count"));
            RecipeAlter.addGatherAlter(type, id, count);
        }
        
        RecipeAlter.clearProductMap();
        for(Iterator i=root.elementIterator("product");i.hasNext();){
            Element element = (Element)i.next();
            int id = Integer.parseInt(element.attributeValue("recipeId"));
            int count = Integer.parseInt(element.attributeValue("count"));
            RecipeAlter.addProduct(id, count);
        }
        
    }

    private void loadItems(String name,byte type,Element node){
        for(Iterator i=node.elementIterator("Item");i.hasNext();){
            Element element = (Element)i.next();
            int id = Integer.parseInt(element.attributeValue("id"));
            byte level = Byte.parseByte(element.attributeValue("level"));
            MaterialType materialType = new MaterialType(name,type,id,level);
            MaterialTypes.addMaterialType(materialType);
        }
    }
}
