package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;
import java.util.Iterator;

/**
 * @author Jeffery
 * @version 1.0
 */
public class TaskDefinitionLoader {

    public TaskDefinitionLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadDefinitions(doc);
    }

    private void loadDefinitions(Document doc){
        Element root = doc.getRootElement();
        for (Iterator i = root.elementIterator("task"); i.hasNext(); ) {
            Element node = (Element) i.next();
            String s = node.attributeValue("tasksID");
            short[] tasksId = getShorts(s);
            short id = Short.parseShort(node.attributeValue("id"));
            String desc = node.getStringValue();
            TaskDefinitions.addTaskDefinition(id, tasksId, desc);
        }
    }

    private static short[] getShorts(String s) {
        if (s.length() == 0)
            return null;
        String[] ss = s.split(",");
        short[] ret = new short[ss.length];
        for (int i = 0; i < ss.length; i++) {
        	ss[i] = ss[i].trim();
            ret[i] = Short.parseShort(ss[i]);
        }
        return ret;
    }

}
