package com.pip.image.workshop;

import java.io.*;
import java.util.*;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import com.pip.util.Utils;

public class Settings {
	public static File workingDir = new File(".");
	public static ArrayList<File> bookmarks = new ArrayList<File>();
	public static String imageEditor = "mspaint.exe";
	public static String imageEditorArg = "\"{0}\"";
	public static File tileLibDir = new File(".");
	public static Element projects;
	
	/**
	 * 日志目录,位于user.home/美术编辑器日志
	 */
	public static String logDir;
	
	public static void loadSetting() {
		String home = System.getProperty("user.home");
		initLog(home);
		FileInputStream fis = null;
		try {
			loadLibModeSetting();
			Properties props = new Properties();
			fis = new FileInputStream(new File(home, "imageworkshop.properties"));
			props.load(fis);
			// props.loadFromXML(fis);
			workingDir = new File(props.getProperty("working_dir", "."));
			tileLibDir = new File(props.getProperty("tile_lib_dir", "."));
			Enumeration names = props.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String)names.nextElement();
				if (!name.startsWith("bookmark")) {
					continue;
				}
				File bookmarkDir = new File(props.getProperty(name));
				if (bookmarkDir.exists() && bookmarkDir.isDirectory()) {
					bookmarks.add(bookmarkDir);
				}
			}
			imageEditor = props.getProperty("image_editor", "mspaint.exe");
			imageEditorArg = props.getProperty("image_editor_arg", "\"{0}\"");
		} catch (IOException e) {
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
	private static void initLog(String home){
		logDir = home+File.separator+"美术编辑器日志";
	}
	private static void saveLibModeSetting() {
		try {
			Utils.saveDOM(projects.getDocument(), new File(System.getProperty("user.home"),"imageWorkShopConfig.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void loadLibModeSetting() {
		File f = new File(System.getProperty("user.home"),"imageWorkShopConfig.xml");
		if(f.exists()==false){
			createDefaultLibModeConfig();
			return;
		}
		f = new File(System.getProperty("user.home"),"imageWorkShopConfig.xml");
		try {
			Document config = Utils.loadDOM(f);
			projects = config.getRootElement();
//			List<Element> list = projects.getChildren();
//			for(Element el:list){
//				ProjectParser.walkDirectory(el.getAttributeValue("dir"));//+"\\pipLib");
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createDefaultLibModeConfig() {
		Element root = new Element("projects");
		Element prj = new Element("project");
		prj.addAttribute("name", "示例项目");
		prj.addAttribute("dir", "");
		
		root.addContent( prj);
		Document doc = new Document(root);
		projects = doc.getRootElement();
		saveLibModeSetting();
	}

	public static void saveSetting() {
		String home = System.getProperty("user.home");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(home, "imageworkshop.properties"));
			Properties props = new Properties();
			props.setProperty("working_dir", workingDir.getAbsolutePath());
			props.setProperty("tile_lib_dir", tileLibDir.getAbsolutePath());
			for (int i = 0; i < bookmarks.size(); i++) {
				props.setProperty("bookmark" + i, bookmarks.get(i).getAbsolutePath());
			}
			props.setProperty("image_editor", imageEditor);
			props.setProperty("image_editor_arg", imageEditorArg);
			props.store(fos, "ImageWorkshop Configuration");
			//props.storeToXML(fos, "ImageWorkshop Configuration", "GBK");
			saveLibModeSetting();
		} catch (IOException e) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
