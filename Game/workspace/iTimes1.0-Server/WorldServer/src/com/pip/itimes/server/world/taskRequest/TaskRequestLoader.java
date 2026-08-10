package com.pip.itimes.server.world.taskRequest;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.util.Utils;

public class TaskRequestLoader {
	public TaskRequestLoader(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        loadTask(root);
	}
	
	public void loadTask(Element root) throws Exception{
		TaskRequestData.taskRequests.clear();
		for (Iterator<Element> task = root.elementIterator("Task"); task.hasNext();) {
			Element el = (Element)task.next();
			TaskRequest request = new TaskRequest();
			request.setID((short)Utils.parseInt(el.attributeValue("id")));
			request.setType(TaskRequest.getType(el.attributeValue("type")));
			switch(request.getType()){
			case TaskRequest.TYPE_MAILITEM:
				Element send = el.element("send");
				request.setMailTitle(send.attributeValue("title"));
				request.setMailContext(send.attributeValue("context"));
				request.setMailItemID(Utils.parseInt(send.attributeValue("itemid")));
				request.setMailItemCount(Utils.parseInt(send.attributeValue("itemcount")));
				request.setMailNew(send.attributeValue("newMail").equals("true"));
				request.setMailNewText(send.attributeValue("newText"));
				request.setMailOpenUI(send.attributeValue("openUI").equals("true"));
				break;
			}
			if(TaskRequestData.taskRequests.containsKey(new Integer(request.getID()))){
				throw new Exception("TaskRequestLoader Same Key[" + request.getID() + "]");
			}else{
				TaskRequestData.taskRequests.put(new Integer(request.getID()), request);
			}
		}
	}
}
