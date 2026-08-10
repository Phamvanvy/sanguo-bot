package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.Element;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TaskAwardLoader {
    public TaskAwardLoader(File file) throws Exception{
        SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        loadTaskAwards(doc);
    }

    private void loadTaskAwards(Document doc){
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("Task");i.hasNext();){
            Element node = (Element)i.next();
            short id = Short.parseShort(node.attributeValue("id"));
            TaskAward award = new TaskAward();
            award.setTaskId(id);
            Element comm = node.element("Common");
            if(comm!=null)
                loadCommons(comm,award);
            Element sub = node.element("TaskItem");
            if(sub!=null){
                SubTaskAward[] subs = loadSubs(sub);
                for(int j=0;j<subs.length;j++){
                    award.addAward(subs[j]);
                }
            }
            TaskAwards.addAward(award);
        }
    }

    private SubTaskAward[] loadSubs(Element element) {
        List l = new ArrayList();
        for (Iterator i = element.elementIterator("Step"); i.hasNext(); ) {
            Element el = (Element) i.next();
            int subId = Integer.parseInt(el.attributeValue("id"));
            SubTaskAward sub = new SubTaskAward();
            sub.setSubId(subId);
            for (Iterator j = el.elementIterator("item"); j.hasNext(); ) {
                Element node = (Element) j.next();
                byte id = Byte.parseByte(node.attributeValue("id"));
                byte type = Byte.parseByte(node.attributeValue("type"));
                int itemId = Integer.parseInt(node.attributeValue("itemID"));
                int count = Integer.parseInt(node.attributeValue("amount"));
                if (type == 0 || type == 1 || type == 2 || type == 3) {
                    IItemTemplate template = Items.getTemplate(itemId);
                    if (template != null) {
                        TemplateGrid grid = new TemplateGrid(template,count);
                        sub.addItem(grid);
                    }
                } else if (type == 5) {
                    sub.addMoney(count);
                } else if (type == 7) {
                    sub.addExp(count);
                } else if (type == 9) {
                    sub.addCredit(count);
                }
            }
            l.add(sub);
        }
        SubTaskAward[] ret = new SubTaskAward[l.size()];
        l.toArray(ret);
        return ret;
    }


    private void loadCommons(Element comm,TaskAward award){
        SubTaskAward sub = new SubTaskAward();
        sub.setSubId((byte)-1);
        for(Iterator i=comm.elementIterator("item");i.hasNext();){
            Element node = (Element) i.next();
            byte type = Byte.parseByte(node.attributeValue("type"));
            int itemId = Integer.parseInt(node.attributeValue("itemID"));
            int count = Integer.parseInt(node.attributeValue("amount"));
            if(type==0||type==1||type==2){
                IItemTemplate item = Items.getTemplate(itemId);
                if(item!=null){
                    TemplateGrid grid = new TemplateGrid(item,count);
                    sub.addItem(grid);
                }
            }
            else if(type==3){
                IItemTemplate item = Items.getTemplate(itemId);
                if(item!=null){
                    TemplateGrid grid = new TemplateGrid(item,1);
                    sub.addItem(grid);
                }
            }
            else if(type==5){
                sub.addMoney(count);
            }
            else if(type==7){
                sub.addExp(count);
            }
        }
        award.addCommonAward(sub);
    }
}
