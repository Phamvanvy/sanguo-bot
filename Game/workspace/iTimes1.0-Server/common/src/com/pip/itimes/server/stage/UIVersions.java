package com.pip.itimes.server.stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.pip.itimes.net.UWAPSegment;


/**
 * 用于加载和输出脚本协议
 * @author wpjiang
 *
 */
public class UIVersions {
	private static Logger log = Logger.getLogger(UIVersions.class);
	//版本号
	public static Map<String, UIVersion> versions = new HashMap<String, UIVersion>();
	
	public static void removeVersion(){
		versions.clear();
	}
	//由于脚本数据的加载 防止etf，png, etd文件
	public static Map<String, byte[]> etfs = new HashMap<String, byte[]>();
	public static Map<String, byte[]> res = new HashMap<String, byte[]>();
	public static Map<String, byte[]> etds = new HashMap<String, byte[]>();
	
	public static short getUIVersion(String name){
		short version = 0;
		if(versions.containsKey(name)){
			version = versions.get(name).version;
		}
		return version;
	}
	/**
	 * 用于加载ui数据和图片
	 * @param taskDir
	 */
	public static void loadTasks(File taskDir) {
		etfs.clear();
		res.clear();
		etds.clear();
		Map<String, byte[]> newEfts = new HashMap<String, byte[]>();
		Map<String, byte[]> newRes = new HashMap<String, byte[]>();
		Map<String, byte[]> newEtds = new HashMap<String, byte[]>();

		File[] files = taskDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.getName().equals("CVS")) {
				continue;
			}
			if (file.isDirectory()) {
				File[] fi = file.listFiles();
				for (File f : fi) {
					loadData(f, newEfts, newRes, newEtds);
				}
			} else {
				loadData(file, newEfts, newRes, newEtds);
			}
		}
		etfs = newEfts;
		res = newRes;
		etds = newEtds;
	}
	/**
	 * 读取etf.gz,etd,pip,png文件,文件名为 filename_model.etf.gz filename_model.etd
	 * filename_model.pip filename_model.png四种
	 * 
	 * @param file
	 * @param newEfts
	 * @param newRes
	 * @param newEtds
	 */
	private static void loadData(File file, Map newEfts, Map newRes, Map newEtds) {
		String fileName = file.getName();
		if (file.isFile() && "gz".equals(FilenameUtils.getExtension(fileName))) {
			FileInputStream is = null;
			try {
				is = new FileInputStream(file);
				log.info("load task ui:" + fileName);
				int length = is.available();
				byte[] etf = new byte[length];
				is.read(etf);
				newEfts.put(fileName, etf);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException ex1) {
					}
			}
		} else if (file.isFile()
				&& "etd".equals(FilenameUtils.getExtension(fileName))) {
			FileInputStream is = null;
			try {
				is = new FileInputStream(file);
				log.info("load task ui etd:" + fileName);
				int length = is.available();
				byte[] etd = new byte[length];
				is.read(etd);
				newEtds.put(fileName, etd);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException ex1) {
					}
			}
		} 
	}
	public static void addUIVersion(String name, UIVersion uiversion){
		versions.put(name, uiversion);
	}
	
	/**
	 * 向客户端输出ui版本信息
	 * @param seg
	 */
	public static void getUIVersions(UWAPSegment seg){
		if (seg != null) {
			byte size = 0;
			Set<Entry<String, UIVersion>> v = versions.entrySet();
			Iterator it = v.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				UIVersion uv = (UIVersion) entry.getValue();
				if (uv.version > 0) {
					size++;
				}
			}
			seg.write(size);
			it = v.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				UIVersion uv = (UIVersion) entry.getValue();
				if (uv.version > 0) {
					seg.writeShort(uv.id);
					seg.writeString(uv.name + ".etf.gz");
					seg.writeShort(uv.version);
				}
			}
		}
		
	}
    /**
     *根据名称查找客户端需要的脚本和图片资源
     * @param uiName
     * @return 资源字节
     */
    public static byte[] gettaskUIBytes(String uiName) {
    	if(!uiName.endsWith(".png") && !uiName.endsWith(".pip")){
    		if(uiName.endsWith(".etd")){
        		byte[] data = findEtd(uiName);
        		if(data != null) {
        			return data;
        		}
    		}else{
		        byte[] etfFile = findETF(uiName);
		        if(etfFile != null){
		        	return etfFile;
		        }
    		} 
    	}else {
    		byte[] data = findRes(FilenameUtils.getBaseName(uiName));
    		if(data != null) {
    			return data;
    		}
    	}
        return null;
    }
    /**
     * 查找gz为尾部的etf文件
     * @param uiName
     * @return
     */
    public static byte[] findETF(String uiName) {
		return (byte[]) etfs.get(uiName);
	}

	/**
	 * 查找图片资源
	 * @param resName
	 * @return
	 */
	public static byte[] findRes(String resName) {
		return (byte[]) res.get(resName);
	}

	/**
	 * 查找etd文件
	 * @param name
	 * @return
	 */
	public static byte[] findEtd(String name) {
		return (byte[]) etds.get(name);
	}
}
