package com.pip.sanguo.data;

import java.io.*;
import java.util.*;

import org.jdom.*;

import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.util.Utils;

/**
 * 一个关卡的详细描述信息，这些信息被保存在关卡目录中的info.xml文件里。
 * @author lighthu
 */
public class GameAreaInfo {
    public GameArea owner;
    public List<GameMapInfo> maps = new ArrayList<GameMapInfo>();
    
    public GameAreaInfo(GameArea owner) {
        this.owner = owner;
    }
    
    public void load() throws Exception {
        Document doc = Utils.loadDOM(new File(owner.source, "info.xml"));
        loadFromXML(doc);
    }
    
    public void save() throws Exception {
        Utils.saveDOM(saveToXML(), new File(owner.source, "info.xml"));
    }
    
    public byte[] toByteArray() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Utils.saveDOM(saveToXML(), bos);
        return bos.toByteArray();
    }
    
    public Document saveToXML() throws Exception {
        Element root = new Element("areainfo");
        for (GameMapInfo map : maps) {
            root.getMixedContent().add(map.save());
        }
        return new Document(root);
    }
    
    public void loadFromXML(Document doc) throws Exception {
        List list = doc.getRootElement().getChildren("map");
        maps.clear();
        for (Object obj : list) {
            Element elem = (Element)obj;
            GameMapInfo gmi = new GameMapInfo(owner);
            gmi.load(elem);
            
            // 有序插入
            int insertPos = 0;
            for (int i = 0; i < maps.size(); i++) {
                if (maps.get(i).id >= gmi.id) {
                    break;
                }
                insertPos++;
            }
            if (insertPos == -1) {
                maps.add(gmi);
            } else {
                maps.add(insertPos, gmi);
            }
        }
    }
}
