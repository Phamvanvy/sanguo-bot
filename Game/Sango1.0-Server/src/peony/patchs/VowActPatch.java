package peony.patchs;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;

import peony.game.CommonUtil;
import peony.game.Server;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.activity.VowActivity;

public class VowActPatch implements Runnable {

	public void run() {
		ActivityService service = Server.server.getServiceRegistry().getActivityService();
		Activity act = service.getActivityByImpClass("VowActivity");
		VowActivity vow = (VowActivity)act.getImpl();
		vow.noticeItems.clear();
		try{
			byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("Items/vownoticeitems.xml");
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			Element root = doc.getRootElement();
			Iterator ite = root.elementIterator("item");
			while (ite.hasNext()) {
				Element el = (Element)ite.next();
				int id = Integer.parseInt(el.attributeValue("id"));
				int shout = Integer.parseInt(el.attributeValue("shout"));
				int count = Integer.parseInt(el.attributeValue("count"));
				vow.noticeItems.put(id, count);
			}
			System.out.println("______________load vow success!!!!");
		}catch(Exception e){}
	}

}
