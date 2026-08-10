package com.pip.sanguo.data;

import java.io.File;
import java.util.*;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.util.Utils;

public class CmccConfig {
    /*
     * 幻想武林所有消费代码的配置。
     */
    private String[][] CONSUME_CODES;
    /*
     * 幻想武林所有消费代码及其价格，key是消费代码，value是点数价格
     */
    private Map<String, Integer> ALL_CONSUME_CODES = new HashMap<String, Integer>();

    public CmccConfig(File configFile) {
        if (!configFile.exists()) {
            return;
        }
        try {
            loadConfig(Utils.loadDOM(configFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*
     * 从配置文件中载入消费代码。
     */
    private void loadConfig(Document doc) throws Exception {
        List<String[]> items = new ArrayList<String[]>();
        Iterator itor = doc.getRootElement().getChildren("item").iterator();
        while (itor.hasNext()) {
            Element elem = (Element)itor.next();
            String code = elem.getAttributeValue("consumecode");
            String name = elem.getAttributeValue("name");
            String price = elem.getAttributeValue("price");
            items.add(new String[] { code, name, price });
        }
        CONSUME_CODES = new String[items.size()][];
        items.toArray(CONSUME_CODES);
        
        ALL_CONSUME_CODES.clear();
        ALL_CONSUME_CODES.put("", 0);
        for (int i = 0; i < CONSUME_CODES.length; i++) {
            ALL_CONSUME_CODES.put(CONSUME_CODES[i][0], Integer.parseInt(CONSUME_CODES[i][2]));
        }
    }
    
    /**
     * 查询一个消费代码对应的点数价格。
     * @param consumeCode
     * @return 如果消费代码不存在，返回0.
     */
    public int getPrice(String consumeCode) {
        try {
            return ALL_CONSUME_CODES.get(consumeCode);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 查询一个消费代码对应的道具名称。
     * @param consumeCode
     */
    public String getItemName(String consumeCode) {
        for (int i = 0; i < CONSUME_CODES.length; i++) {
            if (consumeCode.equals(CONSUME_CODES[i][0])) {
                String ret = CONSUME_CODES[i][1];
                if (ret.endsWith("(折)")) {
                    ret = ret.substring(0, ret.length() - 3);
                }
                return ret;
            }
        }
        return null;
    }
}
