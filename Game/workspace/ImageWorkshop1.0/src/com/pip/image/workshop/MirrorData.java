package com.pip.image.workshop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.mapeditor.data.ProjectOwner;
import com.pip.util.EFSUtil;
import com.pip.util.FileCopier;
import com.pip.util.Utils;

/**
 * 本类是一个镜像目录的描述数据，包括源目录名；每个文件对应源文件的版本。
 * @author lighthu
 */
public class MirrorData {
	public File projectRoot;	// 项目根目录
	public String sourceDir;    // 源目录，相对于项目根目录
	public String mirrorDir;	// 目标目录
	public Map<String, Integer> sourceFileVersions;   // 源文件版本号，key是文件路径（相对于项目根目录）
	
	/**
	 * 创建/载入一个镜像目录的设置。
	 * @param srcDir 源目录（只有在创建时需要传入此参数）
	 * @param dir 镜像目录
	 */
	public MirrorData(File srcDir, File dir) throws Exception {
		projectRoot = ProjectOwner.getProjectRootPath(dir.getAbsolutePath());
		mirrorDir = ProjectOwner.getProjectRelatePath(dir.getAbsolutePath());
		sourceFileVersions = new HashMap<String, Integer>();
		File xmlFile = new File(dir, "mirror.xml");
		if (!xmlFile.exists()) {
			sourceDir = ProjectOwner.getProjectRelatePath(srcDir.getAbsolutePath());
		} else {
			Document doc = Utils.loadDOM(xmlFile);
			sourceDir = doc.getRootElement().getChildText("source");
			List list = doc.getRootElement().getChildren("file");
			for (int i = 0; i < list.size(); i++) {
				Element fileElem = (Element)list.get(i);
				String fname = fileElem.getAttributeValue("name");
				int fversion = Integer.parseInt(fileElem.getAttributeValue("version"));
				sourceFileVersions.put(fname, fversion);
			}
		}
	}
	
	/**
	 * 保存到XML文件。
	 */
	public void save() throws Exception {
		File xmlFile = new File(projectRoot, mirrorDir + "/mirror.xml");
		Element root = new Element("mirror");
	    Element srcElem = new Element("source");
	    srcElem.setText(sourceDir);
	    root.getMixedContent().add(srcElem);
	    Iterator<String> itor = sourceFileVersions.keySet().iterator();
	    while (itor.hasNext()) {
	    	String fname = itor.next();
	    	int version = sourceFileVersions.get(fname);
	    	Element fileElem = new Element("file");
	    	fileElem.addAttribute("name", fname);
	    	fileElem.addAttribute("version", String.valueOf(version));
	    	root.getMixedContent().add(fileElem);
	    }
		Document doc = new Document(root);
	    Utils.saveDOM(doc, xmlFile);
	}
	
	/**
     * 取得文件的当前版本号。
     * 版本号编码规则：
     * 4字节整数，最高2位为type，前3个字节22位表示文件大小，最后一个字节表示文件CRC（字节异或算法）。
     */
    public static int getFileCRCVersion(File file) {
        byte[] content = null;
        try {
            content = Utils.loadFileData(file);
        } catch (Exception e) {
            return 0;
        }
        return (content.length << 8) | (crc8(content) & 0xFF);
    }
	
	/**
     * 字节流CRC值 
     */
    private static byte crc8(byte[] data) {
        byte ret = 0;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            ret ^= data[i];
        }
        return ret;
    }
    
    public File getSourceDir() {
    	return new File(projectRoot, sourceDir);
    }
    
    public File getMirrorDir() {
    	return new File(projectRoot, mirrorDir);
    }
    
    /**
     * 使镜像目录和源目录同步。同步时，所有源目录中有而镜像目录中不存在的文件都会被复制过来，但镜像目录中存在但源目录中不存在
     * 的文件会被保留。同步过程中会记录所有拷贝的文件的源文件版本号，以便将来判断文件是否被修改过。
     * @throws Exception
     */
    public void sync() throws Exception {
    	// 拷贝不存在的文件
    	syncDir(getSourceDir(), getMirrorDir(), "");
    	
    	// 扫描镜像目录，更新所有map文件和ref文件中的引用文件路径。如果引用的是旧目录下的文件，要改成引用新目录中的对应文件。
    	Set<String> fileSet = new HashSet<String>();
    	Utils.findFilesInDir(getMirrorDir(), ".ref", fileSet);
    	for (String path : fileSet) {
    		processRefFile(path);
    	}
    	fileSet.clear();
    	Utils.findFilesInDir(getMirrorDir(), ".map", fileSet);
    	for (String path : fileSet) {
    		processMapFile(path);
    		FileCopier.addMapRef(path);
    	}
    	
    	// 保存mirror.xml
    	save();
    }
    
    /*
     * 拷贝一个目录中的所有文件到另外一个目录中。如果目标文件存在且源文件没有被修改过，则跳过。
     */
    protected void syncDir(File src, File dest, String relate) throws Exception {
    	ProjectOwner po = ProjectOwner.find(projectRoot.getAbsolutePath(), false);
    	File[] children = src.listFiles();
    	for (File child : children) {
    		if (child.isDirectory()) {
    			if (child.getName().equals("CVS") || child.getName().equals(".svn")) {
    				continue;
    			}
    			File target = new File(dest, child.getName());
    			if (!target.exists()) {
    				target.mkdirs();
    			}
    			if (!target.isDirectory()) {
    				throw new IOException("目录结构不一致，无法继续同步。");
    			}
    			syncDir(child, target, relate + child.getName() + "/");
    		} else {
    			File target = new File(dest, child.getName());
    			if (target.exists() && target.isDirectory()) {
					throw new IOException("目录结构不一致，无法继续同步。");
    			}
    			String srcName = relate + child.getName();
    			int version = getFileCRCVersion(child);
    			if (target.exists() && sourceFileVersions.containsKey(srcName) && sourceFileVersions.get(srcName) == version) {
    				continue;
    			}
    			EFSUtil.copyFile(child, target);
    			sourceFileVersions.put(srcName, version);
    			if (po != null) {
    				po.regFile(target.getAbsolutePath());
    			}
    		}
    	}
    }
    
    /*
     * 处理一个map文件，把里面的引用源目录中的文件都改成引用镜像目录中的文件。
     */
    protected void processMapFile(String filePath) {
    	try {
    		Document doc = Utils.loadDOM(new File(filePath));
    		Element root = doc.getRootElement();
    		if (!"true".equals(root.getAttributeValue("libmode"))) {
    			return;
    		}
    		boolean changed = false;
    		Map<String, String> changedHashCodes = new HashMap<String, String>();
    		String checkHead = sourceDir + "\\";
    		
    		// 处理所有<ROOT> -> animates -> cts元素
    		List list = root.getChild("animates").getChildren("cts");
    		for (int i = 0; i < list.size(); i++) {
    			Element child = (Element)list.get(i);
    			String refFile = child.getAttributeValue("file");
    			if (refFile.startsWith(checkHead)) {
    				String newFile = mirrorDir + "\\" + refFile.substring(checkHead.length());
    				int newHash = ProjectOwner.getHashCode(newFile);
    				changedHashCodes.put(child.getAttributeValue("hashCode"), String.valueOf(newHash));
    				child.getAttribute("hashCode").setValue(String.valueOf(newHash));
    				child.getAttribute("file").setValue(newFile);
    				changed = true;
    			}
    		}
    		
    		// 处理所有<ROOT> -> landform元素
    		list = root.getChildren("landform");
    		for (int i = 0; i < list.size(); i++) {
    			Element child = (Element)list.get(i);
    			String refFile = child.getAttributeValue("file");
    			if (refFile.startsWith(checkHead)) {
    				String newFile = mirrorDir + "\\" + refFile.substring(checkHead.length());
    				int newHash = ProjectOwner.getHashCode(newFile);
    				changedHashCodes.put(child.getAttributeValue("hashCode"), String.valueOf(newHash));
    				child.getAttribute("hashCode").setValue(String.valueOf(newHash));
    				child.getAttribute("file").setValue(newFile);
    				changed = true;
    			}
    		}
    		
    		// 处理所有<ROOT> -> map -> layer -> npc元素
    		ArrayList stack = new ArrayList();
    		list = root.getChildren("map");
    		for (int i = 0; i < list.size(); i++) {
    			Element child = (Element)list.get(i);
    			List list2 = child.getChildren("layer");
    			stack.addAll(list2);
    		}
    		while (stack.size() > 0) {
    			Element parent = (Element)stack.remove(0);
    			list = parent.getChildren("npc");
    			for (int i = 0; i < list.size(); i++) {
    				Element child = (Element)list.get(i);
    				String refCode = child.getAttributeValue("animateRef");
    				if (changedHashCodes.containsKey(refCode)) {
    					child.getAttribute("animateRef").setValue(changedHashCodes.get(refCode));
    					changed = true;
    				}
    			}
    			stack.addAll(list);
    		}
    		
    		// 保存文件
    		if (changed) {
    			Utils.saveDOM(doc, new File(filePath), false);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /*
     * 处理一个ref文件，把里面的引用源目录中的文件都改成引用镜像目录中的文件。
     */
    protected void processRefFile(String filePath) {
    	try {
			Document doc = Utils.loadDOM(new File(filePath));
			String checkHead = sourceDir + "\\";
			boolean changed = false;
			for (Element el:(List<Element>)doc.getRootElement().getChildren("item")) {
				String fpath = el.getAttributeValue("file");
				if (fpath.startsWith(checkHead)) {
					String newFile = mirrorDir + "\\" + fpath.substring(checkHead.length());
					el.getAttribute("file").setValue(newFile);
					changed = true;
				}
			}
			if (changed) {
				Utils.saveDOM(doc, new File(filePath), false);
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

	/**
	 * 判断一个目录是否是镜像目录。
	 * @param dir
	 * @return
	 */
	public static boolean isMirrorDir(File dir) {
		return new File(dir, "mirror.xml").exists();
	}
	
	/**
	 * 判断一个文件在上次镜像时的版本号。
	 * @param path 相对路径
	 * @return
	 */
	public int getFileVersion(String path) {
		if (sourceFileVersions.containsKey(path)) {
			return sourceFileVersions.get(path);
		}
		return 0;
	}
}
