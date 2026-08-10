package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.mortbay.log.Log;

/**
 * @author wpjiang 里面存放了该文件夹下的所有动画所需要的图片和文件夹
 */

public class Animate {
	
	
	public static int CTN_TYPE_NETPLAYER = 1;           //人物形象请求下载动画ctn
	public static int CTN_TYPE_BATTLE    = 2;           //请求的战斗动画ctn

	public static int CACHE_IMAGE_NO     = 1;           //请求的形象存储但放入2级形象缓存， 而且不通知更新形象
	public static int CACHE_INAGE_YES    = 2;           //请求的形象图片进行 缓存，并通知更新形象
	
	/**
	 * 用于存储改每个形象用的行走动画和战斗动画名称
	 */
	public static HashMap<Integer, String> animateRoleImageMap = new HashMap<Integer, String>();
	public static void addAnimateRoleImage(int id, String animteName){
		animateRoleImageMap.put(id, animteName);
	}
	
	
	/**
	 * 用于动画所需要的图片
	 */
	Map<String, PngResourceData> pipImageMap = new HashMap<String, PngResourceData>();
	/**
	 * 用于动画所需要的ctn文件
	 */
	Map<String, byte[]> ctnMap = new HashMap<String, byte[]>();

	private File pngDir;
	private Map files = new HashMap();

	public Animate(File pngDir) throws Exception {
		this.pngDir = pngDir;
		load();
	}

	private void load() throws IOException {
		// TODO Auto-generated method stub
		pipImageMap.clear();
		ctnMap.clear();
		File[] resources = pngDir.listFiles();
		for (int i = 0; i < resources.length; i++) {
			if(resources[i].isDirectory()){//进入子目录
				File[] fi = resources[i].listFiles();
				for(int k = 0; k < fi.length; k++){
					loadAnimateFile(fi[k]);
				}
			}else{
				loadAnimateFile(resources[i]);
			}
		}
	}
	public void loadAnimateFile(File file) throws IOException{
		String fileName = file.getName();
		String baseName = FilenameUtils.getBaseName(fileName);
		String ext = FilenameUtils.getExtension(fileName);

		if ("pip".equals(ext)) {
			PngResourceData resource = (PngResourceData) files
					.get(baseName);
			if (resource == null) {
				resource = new PngResourceData();
				resource.name = baseName;
				pipImageMap.put(baseName, resource);
			}
			FileInputStream fs = new FileInputStream(file);

			fs = new FileInputStream(file);
			byte[] pipImg = IOUtils.toByteArray(fs);
			fs.close();
			resource.pipImg = pipImg;
			Log.info("加载" + baseName+ ".pip");
		} else if("ctn".equals(ext)) {
			byte[] bytes = ctnMap.get(baseName);
			if (bytes == null) {
				FileInputStream fs = new FileInputStream(file);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				fs = new FileInputStream(file);
				byte[] pipImgCtn = IOUtils.toByteArray(fs);
				fs.close();
				dos.close();
				ctnMap.put(baseName, pipImgCtn);
				Log.info("加载" + baseName+ ".ctn");
			}
		}
	}
	public byte[] getCtn(String name){
		String baseName = FilenameUtils.getBaseName(name);
		return ctnMap.get(baseName);
	}
	public byte[] getPip(String name){
		String baseName = FilenameUtils.getBaseName(name);
		return pipImageMap.get(baseName).pipImg;
	}
}
