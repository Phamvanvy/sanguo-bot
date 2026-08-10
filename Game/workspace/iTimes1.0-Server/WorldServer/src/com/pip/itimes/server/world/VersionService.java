package com.pip.itimes.server.world;

import org.dom4j.Element;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import java.util.Iterator;

public class VersionService {

    private Map id2version = new HashMap();

    public VersionService() throws Exception{
        load();
    }

    private void load() throws Exception{
        File f = new File(System.getProperty("user.dir") +
                          "/version.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(f);

        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("version");i.hasNext();){
            Element node = (Element)i.next();
            Version version = new Version();
            String id = node.attributeValue("id");
            version.setId(id);
            String status = node.attributeValue("status");
            if("current".equals(status)){
                version.setStatus(Version.STATUS_CURRENT);
            }else if("obsolete".equals(status)){
                version.setStatus(Version.STATUS_OBSOLETE);
            }else if("canceled".equals(status)){
                version.setStatus(Version.STATUS_CANCELED);
            }else{
                version.setStatus(Version.STATUS_CANCELED);
            }
            String charge = node.attributeValue("charge");
//            boolean charge = Boolean.getBoolean(node.attributeValue("charge"));
            String[] charges = charge.split(",");
            version.setCharge(charges);
            String feeplan = node.attributeValue("feeplan");
//            int feeplan = Integer.parseInt(node.attributeValue("feeplan"));
            version.setFeeplan(feeplan);
            String regString = node.attributeValue("allowreg");
            if("true".equals(regString)){
                version.setCanReg(true);
            }else{
                version.setCanReg(false);
            }
            try {
            	version.setDataVersion(Integer.parseInt(node.attributeValue("dataversion")));
            } catch (Exception e) {
            }
            int maxLevel = Integer.parseInt(node.attributeValue("maxlevel"));
            version.setMaxLevel(maxLevel);
            String description = node.attributeValue("description");
            version.setDescription(description);
            String message = node.getText();
            version.setMessage(message);
            id2version.put(version.getId(),version);
        }
    }

    public Version getVersion(String id){
        return (Version)id2version.get(id);
    }

    public void reload(){
        id2version.clear();
        try {
            load();
        } catch (Exception ex) {
        }
    }
}
