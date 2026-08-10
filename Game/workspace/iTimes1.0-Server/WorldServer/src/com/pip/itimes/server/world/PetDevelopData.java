package com.pip.itimes.server.world;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @file PetDevelopConstants.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-9-5
 **/
public class PetDevelopData {
	public byte type;
	public String title;
	public ArrayList<PetDevelopMoneyData> listMoney = new ArrayList<PetDevelopMoneyData>();
	public ArrayList<PetDevelopMoneyData> listiMoney = new ArrayList<PetDevelopMoneyData>();
	
	public static ArrayList<PetDevelopData> listPetDevelop = new ArrayList<PetDevelopData>();
	
	
	public PetDevelopMoneyData getMoney(int index){
		if(listMoney.size() == 0){
			return null; 
		}
		if(index >= listMoney.size()){
			return listMoney.get(listMoney.size() - 1);
		}
		return listMoney.get(index);
	}
	
	public PetDevelopMoneyData getiMoney(int index){
		if(listiMoney.size() == 0){
			return null; 
		}
		if(index >= listiMoney.size()){
			return listiMoney.get(listiMoney.size() - 1);
		}
		return listiMoney.get(index);
	}
	
	public static void loadData(File file) throws Exception{
		SAXReader reader = new SAXReader();
        Document doc = reader.read(file);
        Element root = doc.getRootElement();
        load(root);
	}
	
	private static void load(Element root){
		synchronized (listPetDevelop) {
			listPetDevelop.clear();
			for (Iterator<Element> develops = root.elementIterator("develop"); develops.hasNext();) {
				Element develop = develops.next();
				PetDevelopData pd = new PetDevelopData();
				pd.type = Byte.parseByte(develop.attributeValue("type"));
				pd.title = develop.attributeValue("title");
				for (Iterator<Element> moneys = develop.elementIterator("money"); moneys.hasNext();) {
					Element money = moneys.next();
					PetDevelopMoneyData pdmd = new PetDevelopMoneyData();
					pdmd.money = Integer.parseInt(money.attributeValue("money"));
					pdmd.addtitle = money.attributeValue("addtitle");
					pd.listMoney.add(pdmd);
				}
				for (Iterator<Element> moneys = develop.elementIterator("imoney"); moneys.hasNext();) {
					Element money = moneys.next();
					PetDevelopMoneyData pdmd = new PetDevelopMoneyData();
					pdmd.money = Integer.parseInt(money.attributeValue("imoney"));
					pdmd.itemid = Integer.parseInt(money.attributeValue("itemid"));
					pdmd.itemcount = Integer.parseInt(money.attributeValue("itemcount"));
					pdmd.addtitle = money.attributeValue("addtitle");
					pd.listiMoney.add(pdmd);
				}
				listPetDevelop.add(pd);
			}
		}
	}
}
