package peony.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.Server;



public class TorchService implements Service{
	
	public Map<String, Integer> money = new HashMap<String, Integer>();//玩家获得的话费
	
	public Map<String, Integer> addCount = new HashMap<String, Integer>();//玩家获得话费的次数
	
	protected static final long ONEMONTH = 30*24*60*60*1000L;
	
	private File xml = null;

	public void shutdown() {
		saveCmccFUJIANxml();
	}

	public void startup() throws Exception { 
		xml = new File(Server.server.getServiceRegistry().getDataService().data.baseDir.getPath() + File.separatorChar + "cmccXML.xml");
		if(!xml.exists()){
			//初始化doc
			xml.createNewFile();
			saveCmccFUJIANxml();
		}
		FileInputStream fis = new FileInputStream(xml);
		Document doc = CommonUtil.getDocument(fis);
        parse(doc);
        fis.close();
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.syncRunner.add(new Runnable(){
					public void run() {
						synchronized (this) {
							saveCmccFUJIANxml();
						}
					}
				});
			}
		}, 10*60*1000L, 10*60*1000L, TimeUnit.MILLISECONDS);
		
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Server.server.syncRunner.add(new Runnable(){
					public void run() {
						synchronized (this) {
							money.clear();
							addCount.clear();
						}
					}
				});
			}
		}, getScheduleTime(Calendar.getInstance()), ONEMONTH, TimeUnit.MILLISECONDS);
		
	}
	
	public void saveCmccFUJIANxml(){
    	Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("city");
        for (String s : money.keySet()) {
            Element elem = root.addElement("fujian");
            elem.addAttribute("userid", s);
            elem.addAttribute("money", money.get(s).toString());
            elem.addAttribute("count", addCount.get(s).toString());
        }
        int total = getSum();
        Element elem = root.addElement("totalcount");
        elem.addAttribute("total", String.valueOf(total));
        if(xml != null){
        	try {
				FileWriter fw = new FileWriter(xml);
				CommonUtil.saveDocument(doc, fw);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
	
	@SuppressWarnings("unchecked")
	public void parse(Document doc){
		Element root = doc.getRootElement();
		for(Iterator<Element> i= root.elementIterator("fujian");i.hasNext();){
			Element ele = i.next();
			String userid = ele.attributeValue("userid");
			int mo = Integer.parseInt(ele.attributeValue("money"));
			int count = Integer.parseInt(ele.attributeValue("count"));
			if(!money.containsKey(userid)){
				money.put(userid, mo);
			}
			if(!addCount.containsKey(userid)){
				addCount.put(userid, count);
			}
		}
	}
	
	public int getSum() {
		Set<String> keys = money.keySet();
		int count = 0;
		if (keys != null && keys.size() != 0) {
			for (String i : keys) {
				count += money.get(i);
			}
			return count;
		}
		return 0;
	}
	
	public long getScheduleTime(Calendar cal) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, 23);
		cal1.set(Calendar.MINUTE, 59);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal1.set(Calendar.MONTH,Calendar.OCTOBER);
		cal1.set(Calendar.DAY_OF_MONTH, 31);
		if (cal1.before(cal)) {
			cal1.add(Calendar.MONTH,1);
			return cal1.getTime().getTime()-System.currentTimeMillis();
		} else {
			return cal1.getTime().getTime()-System.currentTimeMillis();
		}
	}

}
