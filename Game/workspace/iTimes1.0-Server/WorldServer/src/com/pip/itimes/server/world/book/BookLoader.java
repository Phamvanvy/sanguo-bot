package com.pip.itimes.server.world.book;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BookLoader {
	public BookLoader(File bookFile) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(bookFile);
        Element root = doc.getRootElement();
        loadNotice(root);
        ArrayList<BookAction> tmpActions = null;
        tmpActions = loadAction(root, "Actions");
        if(BookConfig.accounts != null){
	        synchronized (BookConfig.actions) {
				BookConfig.actions.clear();
				BookConfig.actions = tmpActions;
			}
        }else{
        	BookConfig.actions = tmpActions;
        }
        loadResource(root);
        loadAccount(root);
        loadGrowup(root);
        
        tmpActions = loadAction(root, "Gifts");
        if(BookConfig.gifts != null){
	        synchronized (BookConfig.gifts) {
				BookConfig.gifts.clear();
				BookConfig.gifts = tmpActions;
			}
        }else{
        	BookConfig.gifts = tmpActions;
        }
        tmpActions = loadAction(root, "Citys");
        if(BookConfig.citys != null){
        	synchronized (BookConfig.citys) {
        		BookConfig.citys.clear();
        		BookConfig.citys = tmpActions;
        	}
        }else{
        	BookConfig.citys = tmpActions;
        }
        tmpActions = loadAction(root, "Instances");
        if(BookConfig.instances != null){
        	synchronized (BookConfig.instances) {
        		BookConfig.instances.clear();
        		BookConfig.instances = tmpActions;
        	}
        }else{
        	BookConfig.instances = tmpActions;
        }
	}
	
	public void loadNotice(Element root){
		ArrayList<BookNotice> bookNotices = new ArrayList<BookNotice>();
		Element notices = root.element("Notices");
		for (Iterator<Element> messages = notices.elementIterator("Message"); messages.hasNext();) {
			Element el = (Element)messages.next();
			BookNotice notice = new BookNotice();
			notice.setTilte(el.attributeValue("Title"));
			notice.setContext(el.attributeValue("Context"));
			Element time = el.element("StartTime");
			int year = Integer.parseInt(time.attributeValue("Year"));
			int month = Integer.parseInt(time.attributeValue("Month"));
			int day = Integer.parseInt(time.attributeValue("Day"));
			int hour = Integer.parseInt(time.attributeValue("Hour"));
			int minute = Integer.parseInt(time.attributeValue("Minute"));
			int second = Integer.parseInt(time.attributeValue("Second"));
			notice.setStartTime(getTime(year, month, day, hour, minute, second));
			time = el.element("EndTime");
			year = Integer.parseInt(time.attributeValue("Year"));
			month = Integer.parseInt(time.attributeValue("Month"));
			day = Integer.parseInt(time.attributeValue("Day"));
			hour = Integer.parseInt(time.attributeValue("Hour"));
			minute = Integer.parseInt(time.attributeValue("Minute"));
			second = Integer.parseInt(time.attributeValue("Second"));
			notice.setEndTime(getTime(year, month, day, hour, minute, second));
			//只有活动在有效期时才加入
			if(notice.isActioning()){
				bookNotices.add(notice);
			}
		}
		if(BookConfig.notices != null){
			synchronized (BookConfig.notices) {
				BookConfig.notices.clear();
				BookConfig.notices = bookNotices;
			}
		}else{
			BookConfig.notices = bookNotices;
		}
	}
	
	public void loadResource(Element root){
		ArrayList<BookResource> bookResources = new ArrayList<BookResource>();
		Element notices = root.element("Resources");
		for (Iterator<Element> messages = notices.elementIterator("Message"); messages.hasNext();) {
			Element el = (Element)messages.next();
			BookResource resource = new BookResource();
			resource.setTilte(el.attributeValue("Title"));
			resource.setContext(el.attributeValue("Context"));
			bookResources.add(resource);
		}
		if(BookConfig.resources != null){
			synchronized (BookConfig.resources) {
				BookConfig.resources.clear();
				BookConfig.resources = bookResources;
			}
		}else{
			BookConfig.resources = bookResources;
		}
	}
	
	public void loadAccount(Element root){
		ArrayList<BookResource> bookAccount = new ArrayList<BookResource>();
		Element notices = root.element("Accounts");
		for (Iterator<Element> messages = notices.elementIterator("Message"); messages.hasNext();) {
			Element el = (Element)messages.next();
			BookResource account = new BookResource();
			account.setTilte(el.attributeValue("Title"));
			account.setContext(el.attributeValue("Context"));
			bookAccount.add(account);
		}
		if(BookConfig.accounts != null){
			synchronized (BookConfig.accounts) {
				BookConfig.accounts.clear();
				BookConfig.accounts = bookAccount;
			}
		}else{
			BookConfig.accounts = bookAccount;
		}
	}
	
	public void loadGrowup(Element root){
		ArrayList<BookResource> bookGrowups = new ArrayList<BookResource>();
		Element growups = root.element("Growups");
		for (Iterator<Element> messages = growups.elementIterator("Message"); messages.hasNext();) {
			Element el = (Element)messages.next();
			BookResource growup = new BookResource();
			growup.setTilte(el.attributeValue("Title"));
			growup.setContext(el.attributeValue("Context"));
			bookGrowups.add(growup);
		}
		if(BookConfig.growups != null){
			synchronized (BookConfig.growups) {
				BookConfig.growups.clear();
				BookConfig.growups = bookGrowups;
			}
		}else{
			BookConfig.growups = bookGrowups;
		}
	}
	
	public ArrayList<BookAction> loadAction(Element root, String title){
		ArrayList<BookAction> bookActions = new ArrayList<BookAction>();
		Element actions = root.element(title);
		for (Iterator<Element> messages = actions.elementIterator("Message"); messages.hasNext();) {
			Element el = (Element)messages.next();
			BookAction action = new BookAction();
			action.setTilte(el.attributeValue("Title"));
			action.setContext(el.attributeValue("Context"));
			action.setMapID(Short.parseShort(el.attributeValue("MapID")));
			action.setPostion(Short.parseShort(el.attributeValue("Row")), Short.parseShort(el.attributeValue("Col")));
			action.setLevel(Integer.parseInt(el.attributeValue("LevelMin")), Integer.parseInt(el.attributeValue("LevelMax")));
			Element time = el.element("StartTime");
			int year = Integer.parseInt(time.attributeValue("Year"));
			int month = Integer.parseInt(time.attributeValue("Month"));
			int day = Integer.parseInt(time.attributeValue("Day"));
			int hour = Integer.parseInt(time.attributeValue("Hour"));
			int minute = Integer.parseInt(time.attributeValue("Minute"));
			int second = Integer.parseInt(time.attributeValue("Second"));
			action.setStartTime(getTime(year, month, day, hour, minute, second));
			time = el.element("EndTime");
			year = Integer.parseInt(time.attributeValue("Year"));
			month = Integer.parseInt(time.attributeValue("Month"));
			day = Integer.parseInt(time.attributeValue("Day"));
			hour = Integer.parseInt(time.attributeValue("Hour"));
			minute = Integer.parseInt(time.attributeValue("Minute"));
			second = Integer.parseInt(time.attributeValue("Second"));
			action.setEndTime(getTime(year, month, day, hour, minute, second));
			//只有活动在有效期时才加入
			if(action.isActioning()){
				bookActions.add(action);
			}
		}
		return bookActions;
	}
	
	public long getTime(int year, int month, int day, int hour, int minute, int second){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		return cal.getTime().getTime();
	}
}
