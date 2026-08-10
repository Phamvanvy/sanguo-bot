package com.pip.j0ide.data;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.pip.gtl.codegen.GTLProgGenerator;
import com.pip.gtl.preprocess.GTLPreProcessor;
import com.pip.j0ide.Settings;

/**
 * 本类管理乐园平台中的机型、数据包以及游戏定义等数据。
 * @author lighthu
 */
public class ProjectData {
    /** 全局变量 */
    public ArrayList<Variable> variables;
	/** 预定义机型 */
	protected ArrayList<Model> models;
	/** 数据根目录 */
	protected File baseDir;
	
	/**
	 *  不同Revision的编译目标（相对dataui的路径）
	 */
	public ArrayList<Variable> targets;
	/**
	 * 不同Revision是否限制只编译含有该Revision特殊代码的脚本
	 */
	public ArrayList<Variable> targetsLimit;
	/** 数据link目录 */
	public File linkDir;
	/**
	 * GTL文件编码。
	 */
	public String sourceEncoding = "GBK";
	/**
	 * 是否支持短路计算。
	 */
	public boolean shortCircuit = false;
	/**
	 * 是否支持强制类型检查。
	 */
	public boolean typeCheck = false;
	/*
	 * 是否支持不可到达代码检查
	 */
	public boolean unreachCheck = false;
	/*
	 * 是否支持多分枝return 一致检查
	 */
	public boolean multiReturnCheck = false;
	
	public static final String TYPE_PROJ_CONFIG = "项目设置";
	public static final String TYPE_GLOBAL = "全局变量";
	public static final String TYPE_MODEL = "机型";
	public static final String TYPE_SCRIPT = "游戏脚本";
	public static final String TYPE_REVISION = "目标";
	public static String[] TYPE_NAMES = {TYPE_PROJ_CONFIG, TYPE_GLOBAL, TYPE_MODEL, TYPE_REVISION,TYPE_SCRIPT };
	
	/**
	 * 构造一个空项目对象。
	 */
	public ProjectData() {
	    variables = new ArrayList<Variable>();
		models = new ArrayList<Model>();
		targets = new ArrayList<Variable>();
		targetsLimit = new ArrayList<Variable>();
	}
	
	/**
	 * 取得项目根目录。
	 */
	public File getBaseDir() {
		return baseDir;
	}
	
	/**
	 * 取得项目link目录。
	 */
	public File getLinkDir() {
		return linkDir;
	}
	
	/**
	 * 设置项目link目录。
	 */
	public void setLinkDir(String dir) {
		if(dir == null || "".equals(dir.trim())) {
			linkDir = null;
		} else {
			linkDir = new File(dir);			
		}		
		
		GTLPreProcessor.setFindDir(baseDir, linkDir);
	}
	
	/**
	 * 载入指定目录中的项目。项目信息存放在项目根目录里的index.xml中。
	 * @param dir 项目根目录
	 * @throws Exception
	 */
	public void load(File dir) throws Exception {
		baseDir = dir;
		models.clear();
		Document doc = loadDOM(new File(baseDir, "index.xml"));
		
		Element elem = doc.getRootElement().getChild("link");
		if(elem != null) {
			String link = elem.getAttributeValue("linkDir");
			if(link != null && new File(link).exists()) {
				linkDir = new File(link);
			}
		} else {
			linkDir = null;
		}
		elem = doc.getRootElement().getChild("source_encoding");
		if (elem != null) {
			sourceEncoding = elem.getTextTrim();
		} else {
			sourceEncoding = "GBK";
		}
		elem = doc.getRootElement().getChild("short_circuit");
		if (elem != null) {
			shortCircuit = "true".equals(elem.getTextTrim());
		} else {
			shortCircuit = false;
		}
		elem = doc.getRootElement().getChild("type_check");
		if (elem != null) {
			typeCheck = "true".equals(elem.getTextTrim());
		} else {
			typeCheck = false;
		}
		elem = doc.getRootElement().getChild("unreach_check");
		if (elem != null) {
			unreachCheck = "true".equals(elem.getTextTrim());
		} else {
			unreachCheck = false;
		}
		elem = doc.getRootElement().getChild("multiReturn_check");
		if (elem != null) {
			multiReturnCheck = "true".equals(elem.getTextTrim());
		} else {
			multiReturnCheck = false;
		}
		
		variables.clear();
		targets.clear();
		targetsLimit.clear();
		Object[] arr = doc.getRootElement().getChildren("variable").toArray();
        for (int j = 0; j < arr.length; j++) {
            String varname = ((Element)arr[j]).getAttributeValue("name");
            String varvalue = ((Element)arr[j]).getAttributeValue("value");
            Variable var = new Variable();
            var.name = varname;
            var.value = varvalue;
            variables.add(var);
        }
        elem = doc.getRootElement().getChild("models");
		if (elem != null) {
			loadModels(elem.getChildren("model").toArray());
		}
		
		arr = doc.getRootElement().getChildren("target").toArray();
        for (int j = 0; j < arr.length; j++) {
            String varname = ((Element)arr[j]).getAttributeValue("revision");
            String varvalue = ((Element)arr[j]).getAttributeValue("output");
            Variable var = new Variable();
            var.name = varname;
            var.value = varvalue;
            targets.add(var);
        }
        
        arr = doc.getRootElement().getChildren("limit").toArray();
        for (int j = 0; j < arr.length; j++) {
            String varname = ((Element)arr[j]).getAttributeValue("revision");
            String varvalue = ((Element)arr[j]).getAttributeValue("value");
            Variable var = new Variable();
            var.name = varname;
            var.value = varvalue;
            targetsLimit.add(var);
        }
        
        GTLPreProcessor.setFindDir(baseDir, linkDir);
        GTLPreProcessor.sourceEncoding = sourceEncoding;
        GTLProgGenerator.supportShortCircuit = shortCircuit;
        GTLProgGenerator.supportTypeCheck_G = typeCheck;
        GTLProgGenerator.supportUnreachCheck = unreachCheck;
        GTLProgGenerator.supportMultiReturnCheck = multiReturnCheck;
	}
	
	/**
	 * 载入一个XML文件并转换为DOM。
	 * @throws Exception
	 */
	public static Document loadDOM(File file) throws Exception{
        SAXBuilder sb = new SAXBuilder();
        sb.setValidation(false);
        Document doc = sb.build(file);
        return doc;
    }
	
	/**
	 * 从字节流中载入DOM。
	 */
	public static Document loadDOM(byte[] data) throws Exception {
		SAXBuilder sb = new SAXBuilder();
		sb.setValidation(false);
		Document doc = sb.build(new ByteArrayInputStream(data));
		return doc;
	}

	/**
	 * 从指定的节点中载入机型定义。
	 */
	protected void loadModels(Object[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			Element elem = (Element)nodes[i];
			Model model = new Model();
			model.title = elem.getAttributeValue("title");
			model.device = elem.getAttributeValue("device");
			model.id = elem.getAttributeValue("id");
			Element elem2 = elem.getChild("comments");
			if (elem2 != null) {
				model.comments = elem2.getText();
			} else {
				model.comments = "";
			}
			model.variables = new ArrayList<Variable>();
			Object[] arr = elem.getChildren("variable").toArray();
			for (int j = 0; j < arr.length; j++) {
				String varname = ((Element)arr[j]).getAttributeValue("name");
				String varvalue = ((Element)arr[j]).getAttributeValue("value");
				Variable var = new Variable();
				var.name = varname;
				var.value = varvalue;
				model.variables.add(var);
			}
			models.add(model);
		}
	}
	
	/**
	 * 根据名字查找机型。
	 * @param name 机型名字
	 * @return 找到的机型对象，如果找不到返回null。
	 */
	public Model findModel(String name) {
		for (int i = 0; i < models.size(); i++) {
			Model m = models.get(i);
			if (m.id.equals(name)) {
				return m;
			}
		}
		return null;
	}

	/**
	 * 保存项目。
	 */
	public void save() throws Exception {
		Settings.saveSetting();
		
	    Element root = new Element("downloadsetting");
	    Document doc = new Document(root);
	    
	    if(linkDir != null) {
	    	Element elem = new Element("link");
	    	elem.setAttribute("linkDir", linkDir.getAbsolutePath());
	    	root.addContent(elem);	    	
	    }
    	Element elem = new Element("source_encoding");
    	elem.setText(sourceEncoding);
    	root.addContent(elem);	  
    	
    	elem = new Element("short_circuit");
    	elem.setText(shortCircuit ? "true" : "false");
    	root.addContent(elem);
    	
    	elem = new Element("type_check");
    	elem.setText(typeCheck ? "true" : "false");
    	root.addContent(elem);
    	
    	elem = new Element("unreach_check");
    	elem.setText(unreachCheck ? "true" : "false");
    	root.addContent(elem);
    	
    	elem = new Element("multiReturn_check");
    	elem.setText(multiReturnCheck? "true" : "false");
    	root.addContent(elem);
    	
	    for (int j = 0; j < variables.size(); j++) {
            Element elem2 = new Element("variable");
            elem2.setAttribute("name", variables.get(j).name);
            elem2.setAttribute("value", variables.get(j).value);
            root.addContent(elem2);
        }
	    elem = new Element("models");
	    saveModels(elem);
	    root.addContent(elem);
	    for (int j = 0; j < targets.size(); j++) {
            Element elem2 = new Element("target");
            elem2.setAttribute("revision", targets.get(j).name);
            elem2.setAttribute("output", targets.get(j).value);
            root.addContent(elem2);
        }
	    for (int j = 0; j < targetsLimit.size(); j++) {
            Element elem2 = new Element("limit");
            elem2.setAttribute("revision", targetsLimit.get(j).name);
            elem2.setAttribute("value", targetsLimit.get(j).value);
            root.addContent(elem2);
        }
        saveDOM(doc, new File(baseDir, "index.xml"));
	}
	
	/**
	 * 把DOM树保存到XML文件中。
	 * @throws Exception
	 */
    public static void saveDOM(Document doc, File file) throws Exception {
		FileOutputStream fos = null;
		try {
			XMLOutputter out = new XMLOutputter("    ", true, "GBK");
			fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			out.output(doc, bos);
			bos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}
    
    /**
     * 保存机型定义。
     */
    protected void saveModels(Element parent) {
    	for (int i = 0; i < models.size(); i++) {
    		Model m = models.get(i);
    		Element elem = new Element("model");
    		elem.setAttribute("title", m.title);
    		elem.setAttribute("device", m.device);
    		elem.setAttribute("id", m.id);
    		Element elem2 = new Element("comments");
    		elem2.setText(m.comments);
    		elem.addContent(elem2);
    		for (int j = 0; j < m.variables.size(); j++) {
    			elem2 = new Element("variable");
    			elem2.setAttribute("name", m.variables.get(j).name);
    			elem2.setAttribute("value", m.variables.get(j).value);
    			elem.addContent(elem2);
    		}
    		parent.addContent(elem);
    	}
    }

    /**
     * 得到所有的机型定义。
     */
    public Model[] getModels() {
    	Model[] ret = new Model[models.size()];
    	models.toArray(ret);
    	return ret;
    }

    /**
     * 新建一个机型。
     * @return 新创建的机型对象
     */
    public Model newModel() {
    	Model model = new Model();
    	int seq = 1;
    	while (findModel("Model" + seq) != null) {
    		seq++;
    	}
    	model.title = "机型" + seq;
    	model.id = "Model" + seq;
    	model.device = "General/midp2";
    	model.comments = "";
    	model.variables = new ArrayList<Variable>();
    	models.add(model);
    	return model;
    }
    
    /**
     * 删除指定的机型定义。
     */
    public void deleteModel(Model m) {
    	for (int i = 0; i < models.size(); i++) {
    		if (models.get(i) == m) {
    			models.remove(i);
    			break;
    		}
    	}
    }
    
    /**
     * 得到GTL文件根目录。
     */
    public File getGTLDir() {
    	return new File(baseDir, "gtl");
    }
    
    public String getGTLVersion() {
    	for(int i = 0; i < variables.size(); i++) {
    		Variable var = variables.get(i);
    		if(var.name.equals("GTLVersion")) {
    			return var.value;
    		}
    	}
    	return null;
    }
}
