package com.pip.mapeditor.data;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.util.Utils;

public class ProjectParser {
//	private static HashMap<Element, Element> prj2indexDocRoot = new HashMap<Element, Element>();
//	public static boolean parsed(Element prjEl){
//		if(prj2indexDocRoot.containsKey(prjEl)){
//			return true;
//		}
//		return false;
//	}
	
//	public static void linkMap(Element el, String mapFilePaht) {
//		Element refEl = new Element("ref");
//		refEl.addAttribute("file", mapFilePaht);
//		refEl.addAttribute("name", mapFilePaht);
//		el.addContent(refEl);
//	}
	
	public static int getFileHashCode(File file) {
		String subPath = file.getAbsolutePath();
		subPath = subPath.substring(subPath.indexOf("data\\")+5);
		if(subPath.endsWith(".cts")){
			subPath = subPath.replaceAll("\\.cts$", "\\.ctn");
		}
		return subPath.hashCode();
	}
	public static String[] getFileRefList(String srcFileName) throws Exception{
		File f = new File(srcFileName);
		File refXml = new File(f.getParent(), f.getName()+".ref");
		if(refXml.exists()==false){
			return new String[0];
		}
		Document doc = Utils.loadDOM(refXml);
		List<Element> list = doc.getRootElement().getChildren();
		String names[] = new String[list.size()];
		int i = 0;
		for(Element el:list	){
			names[i] = el.getAttributeValue("file");
			i++;
		}
		return names;
	}
	/**
	 * 为refFile添加被引用的记录.<br/>
	 * @param refFile
	 * @throws Exception 
	 */
	public static void addFileRef(String refToFile, String refFromFile) throws Exception {
		File refToF = new File(refToFile);
		File refXml = new File(refToF.getParent(), refToF.getName()+".ref");
		Document doc;
		if(refXml.exists()){
			doc = Utils.loadDOM(refXml);
		}else{
			doc = new Document(new Element("ref"));
		}
		String subPath = refFromFile.substring(refFromFile.indexOf("data\\")+5);
		//check added
		for(Element el:(List<Element>)doc.getRootElement().getChildren("item")){
			if(subPath.equals(el.getAttributeValue("file"))){
				return;
			}
		}
		Element item = new Element("item");
		item.addAttribute("file", subPath );
		doc.getRootElement().getMixedContent().add(item);
		Utils.saveDOM(doc, refXml);
	}
		
	public static boolean isPipLibDir(File f){
		return f.getName().equals("pipLib") && f.isDirectory();
	}
	public static String parsePrjDataPath(String absolutePath){
		String subPath = absolutePath;
		subPath = subPath.substring(0,subPath.indexOf("data")+4)+File.separator;
		return subPath;
	}

}
