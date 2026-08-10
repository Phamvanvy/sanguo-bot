package com.pip.mapeditor.data;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.util.Utils;

public class ProjectParser {
	public static int getFileHashCode(File file) {
		String subPath = ProjectOwner.getProjectRelatePath(file.getAbsolutePath());
		if (subPath.toLowerCase().endsWith(".cts")) {
			subPath = Utils.replaceSuffix(subPath, "ctn");
		}
		return ProjectOwner.getHashCode(subPath);
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
	 * @param refToFile 被引用文件，ldf或者ctn
	 * @param refFromFile 引用文件，map
	 * @throws Exception 
	 */
	public static void addFileRef(String refToFile, String refFromFile) throws Exception {
		File refToF = new File(refToFile);
		File refXml = new File(refToF.getParent(), refToF.getName()+".ref");
		Document doc;
		boolean newFile;
		if (refXml.exists()) {
			doc = Utils.loadDOM(refXml);
			newFile = false;
		}else{
			doc = new Document(new Element("ref"));
			newFile = true;
		}
		String subPath = ProjectOwner.getProjectRelatePath(refFromFile);
		for (Element el:(List<Element>)doc.getRootElement().getChildren("item")) {
			if (subPath.equals(el.getAttributeValue("file"))) {
				return;
			}
		}
		Element item = new Element("item");
		item.addAttribute("file", subPath );
		doc.getRootElement().getMixedContent().add(item);
		Utils.saveDOM(doc, refXml, newFile);
	}
	
	/**
	 * 为refFile删除被引用的记录.<br/>
	 * @param refToFile 被引用文件，ldf或者ctn
	 * @param refFromFile 引用文件，map
	 * @throws Exception 
	 */
	public static void removeFileRef(String refToFile, String refFromFile) throws Exception {
		File refToF = new File(refToFile);
		File refXml = new File(refToF.getParent(), refToF.getName()+".ref");
		Document doc;
		if (refXml.exists()) {
			doc = Utils.loadDOM(refXml);
		} else {
			return;
		}
		String subPath = ProjectOwner.getProjectRelatePath(refFromFile);
		boolean found = false;
		for (Element el:(List<Element>)doc.getRootElement().getChildren("item")) {
			if (subPath.equals(el.getAttributeValue("file"))) {
				doc.getRootElement().getMixedContent().remove(el);
				found = true;
				break;
			}
		}
		if (found) {
			Utils.saveDOM(doc, refXml, false);
		}
	}
		
	public static boolean isPipLibDir(File f){
		return f.getName().equals("pipLib") && f.isDirectory();
	}
}
