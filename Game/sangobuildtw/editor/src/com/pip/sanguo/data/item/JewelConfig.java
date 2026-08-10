package com.pip.sanguo.data.item;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.sanguo.data.ProjectData;
import com.pip.util.Utils;

/**
 * ±¶ Ø≈‰÷√Œƒº˛jewel_config.xml
 * @author lighthu
 */
public class JewelConfig {
    public static class JewelAttr {
        public int attrType;
        public float attrRatio;
    }
    
    protected ProjectData owner;
    public Map<Integer, JewelAttr[]> jewelAttrs = new HashMap<Integer, JewelAttr[]>();
    
    public JewelConfig(ProjectData owner) throws Exception {
        this.owner = owner;
        load(new File(owner.baseDir, "Items/jewel_config.xml"));
    }
    
    private void load(File f) throws Exception {
        Document doc = Utils.loadDOM(f);
        List l = doc.getRootElement().getChildren("jewel");
        for (int i = 0; i < l.size(); i++) {
            Element elem = (Element)l.get(i);
            int type = Integer.parseInt(elem.getAttributeValue("type"));
            List l2 = elem.getChildren("attr");
            JewelAttr[] attrs = new JewelAttr[l2.size()];
            for (int j = 0; j < l2.size(); j++) {
                Element elem2 = (Element)l2.get(j);
                attrs[j] = new JewelAttr();
                attrs[j].attrType = Integer.parseInt(elem2.getAttributeValue("type"));
                attrs[j].attrRatio = Float.parseFloat(elem2.getAttributeValue("ratio"));
            }
            jewelAttrs.put(type, attrs);
        }
    }
}
