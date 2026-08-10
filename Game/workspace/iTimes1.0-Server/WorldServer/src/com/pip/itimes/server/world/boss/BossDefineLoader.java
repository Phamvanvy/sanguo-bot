package com.pip.itimes.server.world.boss;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import com.pip.itimes.server.stage.BossTips;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.question.Question;

/**
 * @author wpjiang
 *	世界boss加载器
 */
public class BossDefineLoader{
	
	/**
	 * boss无装备刷新
	 */
	public final static int bossFinalEquId = 0;
	public static HashMap<Integer, BossDefine> bossEquMap = new HashMap<Integer, BossDefine>();
	
	public static HashMap<Integer, BossDefine> bossDefineMap = new HashMap<Integer, BossDefine>();
	
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	public static final int period = 86400000;
	
	public static long lastMakeTime; //默认要这天的起始时间 最好一次更新
	
	public BossDefineLoader(File pkgDir) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(pkgDir);
        loadWorldBoss(doc);
	}
	
	public void loadWorldBoss(Document doc){
		bossEquMap.clear();
		bossDefineMap.clear();
		BossTips.worldBossTipClear();
		BossTips.bossTipClear();
		BossTips.clearBossPreTip();
		Element root = doc.getRootElement();
		int id;
		long begin = 0;
		long end = 0;
		Iterator it = root.elementIterator("boss");
		Date date = new Date();
		while(it.hasNext()) {
			Element elem = (Element)it.next();
		/*	int id = Integer.parseInt(e.attributeValue("mgid"),16);
            localmessage[0] = e.attributeValue("mapid");*/
            
			id = Integer.parseInt(elem.attributeValue("groupid"),16);
			String b = elem.attributeValue("starttime");
			String e = elem.attributeValue("endtime");
			
			String dataday = format1.format(date);
			try {
				String twelve = dataday + " " + "12:00:00";
				Date te = format.parse(twelve);
				long twelveClock = te.getTime();
				long twelveSecond = 12 * 60 * 60;//12小时秒
				
				//起始时间
				b = dataday + " " + b;
				Date d = format.parse(b);
				begin = d.getTime();
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				if(begin < twelveClock){
					begin = cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR) * 60 * 60;
				}else{
					begin = cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR) * 60 * 60 + twelveSecond;
				}
				
				//结束时间
				e = dataday + " " + e;
				Date de = format.parse(e);
				end = de.getTime();
				cal.setTime(de);
				if(end < twelveClock){
					end = cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR) * 60 * 60;
				}else{
					end = cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR) * 60 * 60 + twelveSecond;
				}
				
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			long refreshTime = Long.parseLong(elem.attributeValue("refreshtime")) / 1000;
			int hpMax = Integer.parseInt(elem.attributeValue("hpmax"));
			int mpMax = Integer.parseInt(elem.attributeValue("mpmax"));
			
			
			String message = elem.attributeValue("message");
	        BossTips.addWorldBossTip(id,message);
	        
	        String privateMessage = elem.attributeValue("privatemassage");
	        BossTips.addBossTip(id, privateMessage);
	        
	        String preChatMessage = elem.attributeValue("premessage");
	        BossTips.addBossPreTip(id, preChatMessage);
	        
	        long pretime = Long.parseLong(elem.attributeValue("pretime"));
			//int rect = Integer.parseInt(elem.attributeValue("rect"));
			BossDefine bossDefine = new BossDefine(id, hpMax, mpMax, refreshTime, begin, end, pretime);
			for(Iterator j = elem.elementIterator("map");j.hasNext();){
				Element map = (Element)j.next();
	            int mapId= Integer.parseInt(map.attributeValue("mapid"));
	            int x = Integer.parseInt(map.attributeValue("x"));
	            int y = Integer.parseInt(map.attributeValue("y"));
	            int rect = Integer.parseInt(map.attributeValue("rect"));
	            bossDefine.addBossDefine(mapId, x, y, rect);
	        }
			
			int equId = Integer.parseInt(elem.attributeValue("equId"));
			bossDefine.setBossEquid(equId);
			bossDefineMap.put(id, bossDefine);
			if(equId != bossFinalEquId){
				bossEquMap.put(equId, bossDefine);
			}
		}
		
		lastMakeTime = Utils.getTodayStart();
	}
		
	/**
	 * 已经是第二天了，需要重置
	 */
	public static void reSet(){
		lastMakeTime = Utils.getTodayStart();
		for(Map.Entry<Integer, BossDefine> boss: bossDefineMap.entrySet()){
    		BossDefine bossDefine = boss.getValue();
    		bossDefine.resetCurrentRefreshTime();
		}
	}
}
