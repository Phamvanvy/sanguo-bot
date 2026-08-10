package com.pip.itimes.server.stage;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import java.io.File;
import org.dom4j.Element;
import java.util.Iterator;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TransferLoader {
    public TransferLoader(File file) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        loadDoors(document);
    }

    public void loadDoors(Document doc){
        Element root = doc.getRootElement();
       for(Iterator i=root.elementIterator("door");i.hasNext();){
           Element node = (Element)i.next();
           String name = node.attributeValue("name");
           int mapId = Integer.parseInt(node.attributeValue("mapid"));
           int x = Integer.parseInt(node.attributeValue("x"));
           int y = Integer.parseInt(node.attributeValue("y"));
           
           int newMap = Integer.parseInt(node.attributeValue("newMap"));
           int newX = Integer.parseInt(node.attributeValue("newX"));
           int newY = Integer.parseInt(node.attributeValue("newY"));
           int level = Integer.parseInt(node.attributeValue("level"));
           
           TransferDoor door = new TransferDoor();
           door.setName(name);
           door.setX(x);
           door.setY(y);
           door.setMapId(mapId);
           door.setNewMap(newMap);
           door.setNewX(newX);
           door.setNewY(newY);
           door.setLevel(level);
           TransferDoor.addTransferDoor(door);
       }
       for(Iterator i=root.elementIterator("nodoor");i.hasNext();){
           Element node = (Element)i.next();
           String message = node.attributeValue("message");
           short mapId = Short.parseShort(node.attributeValue("mapid"));
           NoDoor noDoor = new NoDoor(mapId,message);
           NoDoor.addNoDoor(noDoor);
       }
       
       for(Iterator i=root.elementIterator("notransfer");i.hasNext();){
           Element node = (Element)i.next();
           String message = node.attributeValue("message");
           short mapId = Short.parseShort(node.attributeValue("mapid"));
           NoDoor noDoor = new NoDoor(mapId,message);
           NoDoor.addNoTransfer(noDoor);
       }
    }
}
