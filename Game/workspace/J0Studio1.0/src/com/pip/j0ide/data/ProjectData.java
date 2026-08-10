package com.pip.j0ide.data;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.pip.util.Utils;

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
	
	public static final String TYPE_GLOBAL = "全局变量";
	public static final String TYPE_MODEL = "机型";
	public static final String TYPE_SCRIPT = "游戏脚本";
	public static final String TYPE_REVISION = "目标";
	public static String[] TYPE_NAMES = { TYPE_GLOBAL, TYPE_MODEL, TYPE_REVISION,TYPE_SCRIPT };
	
	/**
	 * 构造一个空项目对象。
	 */
	public ProjectData() {
	    variables = new ArrayList<Variable>();
		models = new ArrayList<Model>();
		targets = new ArrayList<Variable>();
	}
	
	/**
	 * 取得项目根目录。
	 */
	public File getBaseDir() {
		return baseDir;
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
		variables.clear();
		targets.clear();
		Object[] arr = doc.getRootElement().getChildren("variable").toArray();
        for (int j = 0; j < arr.length; j++) {
            String varname = ((Element)arr[j]).getAttributeValue("name");
            String varvalue = ((Element)arr[j]).getAttributeValue("value");
            Variable var = new Variable();
            var.name = varname;
            var.value = varvalue;
            variables.add(var);
        }
		Element elem = doc.getRootElement().getChild("models");
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
	    Element root = new Element("downloadsetting");
	    Document doc = new Document(root);
	    for (int j = 0; j < variables.size(); j++) {
            Element elem2 = new Element("variable");
            elem2.setAttribute("name", variables.get(j).name);
            elem2.setAttribute("value", variables.get(j).value);
            root.addContent(elem2);
        }
	    Element elem = new Element("models");
	    saveModels(elem);
	    root.addContent(elem);
	    for (int j = 0; j < targets.size(); j++) {
            Element elem2 = new Element("target");
            elem2.setAttribute("revision", targets.get(j).name);
            elem2.setAttribute("output", targets.get(j).value);
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
}
