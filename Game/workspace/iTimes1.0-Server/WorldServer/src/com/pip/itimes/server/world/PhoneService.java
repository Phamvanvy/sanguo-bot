package com.pip.itimes.server.world;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import java.io.File;
import org.dom4j.Element;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PhoneService {

    private Map name2phone = new HashMap();

    public PhoneService() throws Exception{
        load();
    }

    public void load() throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new File(System.getProperty("user.dir")+"/phones.xml"));
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("phone-type");i.hasNext();){
            Element node = (Element)i.next();
            PhoneType phone = new PhoneType();
            phone.setId(node.attributeValue("name"));
            int interval = Integer.parseInt(node.attributeValue("interval"));
            phone.setInterval(interval);
            String pkgType = node.attributeValue("filetype");
            if("pkg".equals(pkgType)){
                phone.setFiltType(PhoneType.PKG);
            }
            else if("pkgem".equals(pkgType)){
                phone.setFiltType(PhoneType.PKGEM);
            }else if("pkgs".equals(pkgType)){
                phone.setFiltType(PhoneType.PKGS);
            }
            name2phone.put(phone.getId(),phone);
        }
    }

    public void reload(){
        synchronized(this){
            name2phone.clear();
            try {
                load();
            } catch (Exception ex) {
            }
        }
    }

    public PhoneType getPhoneType(String id){
        synchronized(this){
            return (PhoneType) name2phone.get(id);
        }
    }
}
