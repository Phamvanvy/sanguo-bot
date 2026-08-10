package com.pip.itimes.server.stage;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 下载文件管理器。本类处理文件路径映射（根据机型、版本号、UIMODEL等数据），计算文件版本号，并对下载文件内容进行缓存。
 * @author lighthu
 */
public class DownloadManager {
	/**
	 * Singleton
	 */
	public static DownloadManager instance;
	
	/*
	 * 搜索路径。
	 */
	static class SearchPath {
		// 相对项目路径，可带有变量（用{}括起来）
		public String path;
		// 是否搜索子目录
		public boolean includeSubDir;
		// 适配机型，null表示通用
		public String model;
	}
	
	/*
	 * 文件类型配置。
	 */
	static class FileType {
		// 文件名模式
		public Pattern pattern;
		// 此文件类型的搜索路径
		public SearchPath[] pathes;
	}
	
	// 根目录
	private File root;
	// 文件类型
	private FileType[] fileTypes;
	// 客户端内置文件模式表
	private Pattern[] builtInFilePatterns;
	
	// 文件名对应表，key是拼起来的一个字符串（version/model/uimodel/filename），value是实际文件的相对路径
	private ConcurrentHashMap<String, String> fileNameMap;
	// 客户端内置文件检测表，key是文件的相对路径，value表示是否内置文件
	private ConcurrentHashMap<String, Boolean> builtInFileCache;
	// 文件内容缓存，key是文件的相对路径，value是文件内容
	private ConcurrentHashMap<String, byte[]> fileDataCache;
	// 文件版本号缓存，key是文件的相对路径，value是文件版本号
	private ConcurrentHashMap<String, Integer> fileVersionCache;
	
	
	/**
	 * 初始化一个新的管理器
	 * @param root 根目录
	 * @throws IOException
	 */
	public DownloadManager(File root) throws Exception {
		this.root = root;
		init();
		instance = this;
	}
	
	/*
	 * 从download_settings.xml里读取配置。
	 */
	private void init() throws Exception {
		SAXReader reader = new SAXReader();
    	Document doc = reader.read(new File(root, "download_settings.xml"));
    	Element root = doc.getRootElement();
    	
    	List list = root.elements("filetype");
    	fileTypes = new FileType[list.size()];
    	for (int i = 0; i < fileTypes.length; i++) {
    		Element elem = (Element)list.get(i);
    		fileTypes[i] = new FileType();
    		fileTypes[i].pattern = Pattern.compile(elem.attributeValue("pattern"));
    		List list2 = elem.elements("path");
    		fileTypes[i].pathes = new SearchPath[list2.size()];
    		for (int j = 0; j < fileTypes[i].pathes.length; j++) {
    			fileTypes[i].pathes[j] = new SearchPath();
    			Element elem2 = (Element)list2.get(j);
    			fileTypes[i].pathes[j].path = elem2.getText();
    			fileTypes[i].pathes[j].includeSubDir = "true".equals(elem2.attributeValue("include_sub_dir"));
    			fileTypes[i].pathes[j].model = elem2.attributeValue("model");
    		}
    	}
    	
    	list = root.elements("built_in_file");
    	builtInFilePatterns = new Pattern[list.size()];
    	for (int i = 0; i < builtInFilePatterns.length; i++) {
    		Element elem = (Element)list.get(i);
    		builtInFilePatterns[i] = Pattern.compile(elem.attributeValue("pattern"));
    	}
    	
    	fileNameMap = new ConcurrentHashMap<String, String>();
    	builtInFileCache = new ConcurrentHashMap<String, Boolean>();
    	fileDataCache = new ConcurrentHashMap<String, byte[]>();
    	fileVersionCache = new ConcurrentHashMap<String, Integer>();
	}
	
	/*
	 * 替换在字符串中出现的变量（{}括起来的部分），可用的变量有：
	 *  Version - 客户端版本号
	 *  Model - 客户端机型
	 *  UIModel - 客户端UI机型
	 *  Revision - 大版本
	 */
	private String replaceVar(String str, String version, String model, String uimodel, String revision) {
		int len = str.length();
		StringBuffer buf = new StringBuffer(len);
		int state = 0;
		int startPos = 0;
		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);
			if (state == 0) {
				if (ch == '{') {
					state = 1;
					startPos = i;
				} else {
					buf.append(ch);
				}
			} else if (state == 1) {
				if (ch == '}') {
					state = 0;
					String key = str.substring(startPos + 1, i);
					if ("Version".equals(key)) {
						buf.append(version);
					} else if ("Model".equals(key)) {
						buf.append(model);
					} else if ("UIModel".equals(key)) {
						buf.append(uimodel);
					} else if ("Revision".equals(key)) {
						buf.append(revision);
					} else {
						buf.append("{" + key + "}");
					}
				}
			}
		}
		if (state == 1) {
			buf.append(str.substring(startPos));
		}
		return buf.toString();
	}
	
	/*
	 * 在指定子目录中查找一个文件，如果找到，返回找到的文件的相对路径。
	 */
	private String searchInDir(String path, String name, boolean includeSubDir) {
		// "mi/"开头，".ps"扩展名的文件需要特殊转换
		if (name.startsWith("mi/")) {
			name = name.substring(3);
		}
		if (name.endsWith(".ps")) {
			name = name.substring(0, name.length() - 1);
		}
		
		// 在此目录中查找
		File f = new File(root, path + "/" + name);
		if (f.exists()) {
			return path + "/" + name;
		}
		
		// 在子目录中查找
		if (includeSubDir) {
			File[] children = new File(root, path).listFiles();
			for (File child : children) {
				if (!child.isDirectory()) {
					continue;
				}
				String tmp = searchInDir(path + "/" + child.getName(), name, true);
				if (tmp != null) {
					return tmp;
				}
			}
		}
		return null;
	}
	
	/*
	 * 搜索指定文件对应的实际文件相对路径。
	 */
	private String searchFile(String version, String model, String uimodel, String fileName, String revision) {
		String key = version + "/" + model + "/" + uimodel + "/" + fileName;
		String ret = fileNameMap.get(key);
		if (ret == null) {
			// 查找此文件对应的文件类型
			FileType type = null;
			for (FileType t : fileTypes) {
				if (t.pattern.matcher(fileName).matches()) {
					type = t;
					break;
				}
			}
			
			// 依次在每个搜索路径中搜索此文件
			for (SearchPath sp : type.pathes) {
				if (sp.model != null && !model.equals(sp.model)) {
					continue;
				}
				String path = replaceVar(sp.path, version, model, uimodel, revision);
				ret = searchInDir(path, fileName, sp.includeSubDir);
				if (ret != null) {
					fileNameMap.put(key, ret);
					break;
				}
			}
		}
		return ret;
	}
	
	/**
	 * 判断一个文件是否是客户端内置文件。
	 */
	public boolean isBuiltIn(String version, String model, String uimodel, String fileName, String revision) {
		String realPath = searchFile(version, model, uimodel, fileName, revision);
		if (realPath == null) {
			return false;
		}
		if (builtInFileCache.containsKey(realPath)) {
			return builtInFileCache.get(realPath);
		}
		boolean result = false;
		for (Pattern p : builtInFilePatterns) {
			if (p.matcher(realPath).matches()) {
				result = true;
				break;
			}
		}
		builtInFileCache.put(realPath, result);
		return result;
	}
	
	/*
	 * 计算一个数据流的8位crc。
	 */
	private byte crc8(byte[] data) {
		byte ret = 0;
		int len = data.length;
		for (int i = 0; i < len; i++) {
			ret ^= data[i];
		}
		return ret;
	}
	
	/**
	 * 取得文件的当前版本号。
	 * 版本号编码规则：
     * 4字节整数，最高位是表示此文件是否优先保留在缓存中（内置），前3个字节其他23位表示文件大小，最后一个字
     * 节表示文件CRC（字节异或算法）。
	 */
	public int getFileVersion(String version, String model, String uimodel, String fileName, String revision) throws IOException {
		String realPath = searchFile(version, model, uimodel, fileName, revision);
		if (realPath == null) {
			return 0;
		}
		if (fileVersionCache.containsKey(realPath)) {
			return fileVersionCache.get(realPath);
		}
		byte[] content = getFileContent(version, model, uimodel, fileName, revision);
		boolean bflag = isBuiltIn(version, model, uimodel, fileName, revision);
		return (content.length << 8) | (crc8(content) & 0xFF) | (bflag ? 0x80000000 : 0);
	}
	
	/**
	 * 取得文件的内容。
	 */
	public byte[] getFileContent(String version, String model, String uimodel, String fileName, String revision) throws IOException {
		String realPath = searchFile(version, model, uimodel, fileName, revision);
		if (realPath == null) {
			return null;
		}
		if (fileDataCache.containsKey(realPath)) {
			return fileDataCache.get(realPath);
		}
		
		// ".ps"结尾的文件需要特殊处理，带上".s"
		byte[] ret;
		if (fileName.endsWith(".ps")) {
			byte[] pdata = loadFileData(new File(root, realPath));
			byte[] sdata = loadFileData(new File(root, realPath.substring(0, realPath.length() - 2) + ".s"));
			ret = new byte[pdata.length + sdata.length + 4];
			ret[0] = (byte)((pdata.length >> 24) & 0xFF);
			ret[1] = (byte)((pdata.length >> 16) & 0xFF);
			ret[2] = (byte)((pdata.length >> 8) & 0xFF);
			ret[3] = (byte)((pdata.length >> 0) & 0xFF);
			System.arraycopy(pdata, 0, ret, 4, pdata.length);
			System.arraycopy(sdata, 0, ret, pdata.length + 4, sdata.length);
		} else {
			ret = loadFileData(new File(root, realPath));
		}
		fileDataCache.put(realPath, ret);
		return ret;
	}

    /**
     * 载入文件内容到字符数组。
     */
    public static byte[] loadFileData(File src) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(src);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ByteArrayOutputStream bos = new ByteArrayOutputStream((int)src.length());
			byte[] data = new byte[256];
			int len;
			while ((len = bis.read(data)) >= 0) {
				if (len == 0) {
					continue;
				}
				bos.write(data, 0, len);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
