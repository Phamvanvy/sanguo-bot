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
public class ChatFavoriteLoader {
    public ChatFavoriteLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadFavorites(doc);;
    }

    private void loadFavorites(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("ChatFavorite");i.hasNext();){
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            String name = node.attributeValue("name");
            String desc = node.attributeValue("desc");
            ChatFavorite chatFavorite = new ChatFavorite();
            chatFavorite.id = id;
            chatFavorite.name = name;
            chatFavorite.desc = desc;
            ChatFavorites.addFavorite(chatFavorite);
        }
    }
}
