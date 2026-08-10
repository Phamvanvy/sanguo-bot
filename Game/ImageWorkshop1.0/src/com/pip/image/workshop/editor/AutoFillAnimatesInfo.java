package com.pip.image.workshop.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pip.util.Utils;

/**
 * 此类从一个目录中读取用于自动创建动画序列的图片的信息。
 * 每个部位2个子目录，一个存储此部位的所有帧图片，一个存储每个图片对应的挂接点信息。
 * 存储帧图片的目录名为：b-<部位名称>，例如b-arms
 * 存储挂接点信息的目录名为：s-<部位名称>，例如s-arms
 * 帧图片的文件名为：b-<动作序号>_<动作名称>_<部位名称>_<帧号>.png，例如b-1_待机_arms_20.png
 * 挂接点信息文件名为：s-<动作序号>_<动作名称>_<部位名称>_<帧号>.png，例如s-1_待机_arms_20.png 
 * @author light.hu
 */
public class AutoFillAnimatesInfo {
	// 部位的名称
	public String[] partNames;
	// 每个动画的动作序号
	private int[] animateIDs;
	// 动画序列的名称
	public String[] animateNames;
	// 每个动画序列的帧数
	public int[] animateFrameCounts;
	// 每个部位每个动画序列的图片文件，按帧号从小到大排序。key的格式是 <部位名称>_<动作名称>
	private HashMap<String, File[]> partImageFiles;
	// 每个部位每个动画序列的参考点图片文件，按帧号从小到大排序。key的格式是 <部位名称>_<动作名称>
	private HashMap<String, File[]> partAnchorFiles;
	
	public AutoFillAnimatesInfo(File rootDir) throws Exception {
		// 首先读取目录名，检查部位名称是否配对
		readPartNames(rootDir);
		
		// 从任意一个部位图片目录中读取动画序列的名称
		readAnimateNames(new File(rootDir, "b-" + partNames[0]));
		
		// 从第一个部位目录中读取每个动画序列的帧数
		animateFrameCounts = new int[animateNames.length];
		File dir = new File(rootDir, "b-" + partNames[0]);
		for (int i = 0; i < animateNames.length; i++) {
			animateFrameCounts[i] = getImageFiles(dir, "b-", animateIDs[i], animateNames[i]).length;
			if (animateFrameCounts[i] == 0) {
				throw new Exception("在目录" + dir + "中，没有找到动作" + animateNames[i] + "的图片。");
			}
		}
		
		// 从每一个部位目录中读取每个动画序列的帧数进行验证，并把结果放到partImageFiles和partAnchorFiles表中
		partImageFiles = new HashMap<String, File[]>();
		partAnchorFiles = new HashMap<String, File[]>();
		for (int i = 0; i < partNames.length; i++) {
			for (int j = 0; j < animateNames.length; j++) {
				dir = new File(rootDir, "b-" + partNames[i]);
				String[] fileNames = getImageFiles(dir, "b-", animateIDs[j], animateNames[j]);
				if (fileNames.length != animateFrameCounts[j]) {
					throw new Exception("在目录" + dir + "中，包含" + fileNames.length + "个动作" + animateNames[j] + "的图片，需要" + animateFrameCounts[j] + "个。");
				}
				partImageFiles.put(partNames[i] + "_" + animateNames[j], getFiles(dir, fileNames));
				
				dir = new File(rootDir, "s-" + partNames[i]);
				fileNames = getImageFiles(dir, "s-", animateIDs[j], animateNames[j]);
				if (fileNames.length != animateFrameCounts[j]) {
					throw new Exception("在目录" + dir + "中，包含" + fileNames.length + "个动作" + animateNames[j] + "的图片，需要" + animateFrameCounts[j] + "个。");
				}
				partAnchorFiles.put(partNames[i] + "_" + animateNames[j], getFiles(dir, fileNames));
			}
		}
	}
	
	public int getTotalFrames() {
		int ret = 0;
		for (int v : animateFrameCounts) {
			ret += v;
		}
		return ret;
	}
	
	public File[] getImageFiles(int partIndex, int animateIndex) {
		return partImageFiles.get(partNames[partIndex] + "_" + animateNames[animateIndex]);
	}
	
	public File[] getAnchorFiles(int partIndex, int animateIndex) {
		return partAnchorFiles.get(partNames[partIndex] + "_" + animateNames[animateIndex]);
	}
	
	private File[] getFiles(File baseDir, String[] names) {
		File[] ret = new File[names.length];
		for (int i = 0; i < names.length; i++) {
			ret[i] = new File(baseDir, names[i]);
		}
		return ret;
	}
	
	private String[] getImageFiles(File dir, String prefix, int animateID, String animateName) throws Exception {
		String[] names = Utils.listFile(dir, "png");
		List<String> retList = new ArrayList<String>();
		String fullPrefix = prefix + animateID + "_" + animateName + "_";
		for (String n : names) {
			if (n.startsWith(fullPrefix)) {
				retList.add(n);
			}
		}
		for (int i = 0; i < retList.size() - 1; i++) {
			for (int j = i + 1; j < retList.size(); j++) {
				String n1 = retList.get(i);
				String n2 = retList.get(j);
				int id1 = getFrameIndex(n1);
				int id2 = getFrameIndex(n2);
				if (id1 > id2) {
					retList.set(i, n2);
					retList.set(j, n1);
				}
			}
		}
		String[] ret = new String[retList.size()];
		retList.toArray(ret);
		return ret;
	}
	
	private int getFrameIndex(String name) throws Exception {
		try {
			int pos = name.lastIndexOf('.');
			name = name.substring(0, pos);
			pos = name.lastIndexOf('_');
			return Integer.parseInt(name.substring(pos + 1));
		} catch (Exception e) {
			throw new Exception("文件名" + name + "不符合命名规范，格式必须为b-<动作序号>_<动作名称>_<部位名称>_<帧号>.png或s-<动作序号>_<动作名称>_<部位名称>_<帧号>.png。");
		}
	}
	
	private void readAnimateNames(File dir) throws Exception {
		String[] names = Utils.listFile(dir, "png");
		Map<Integer, String> nameMap = new HashMap<Integer, String>();
		Set<String> usedNames = new HashSet<String>();
		for (String n : names) {
			if (!n.startsWith("b-") && !n.startsWith("s-")) {
				continue;
			}
			int pos1 = n.indexOf('_');
			if (pos1 == -1) {
				throw new Exception("图片文件" + dir + "\\" + n + "命名不规范，格式必须为：b-<动作序号>_<动作名称>_<部位名称>_<帧号>.png");
			}
			int pos2 = n.indexOf('_', pos1 + 1);
			if (pos2 == -1) {
				throw new Exception("图片文件" + dir + "\\" + n + "命名不规范，格式必须为：b-<动作序号>_<动作名称>_<部位名称>_<帧号>.png");
			}
			String indStr = n.substring(2, pos1);
			String nstr = n.substring(pos1 + 1, pos2);
			int id;
			try {
				id = Integer.parseInt(indStr);
			} catch (Exception e) {
				throw new Exception("图片文件" + dir + "\\" + n + "命名不规范，格式必须为：b-<动作序号>_<动作名称>_<部位名称>_<帧号>.png");
			}
			if (nstr.length() == 0) {
				throw new Exception("图片文件" + dir + "\\" + n + "命名不规范，格式必须为：b-<动作序号>_<动作名称>_<部位名称>_<帧号>.png");
			}
			if (nameMap.containsKey(id)) {
				if (!nstr.equals(nameMap.get(id))) {
					throw new Exception("图片文件" + dir + "\\" + n + "指定了动作序号" + id + "的名称为" + nstr + "，和其他文件存在冲突。");
				}
			} else {
				if(usedNames.contains(nstr)) {
					throw new Exception("动作名称不允许重复：" + nstr);
				}
				nameMap.put(id, nstr);
				usedNames.add(nstr);
			}
		}
		if (nameMap.isEmpty()) {
			throw new Exception("在目录" + dir + "中没有找到可用的图片文件。");
		}
		Integer[] ids = new Integer[nameMap.size()];
		nameMap.keySet().toArray(ids);
		Arrays.sort(ids);
		animateIDs = new int[ids.length];
		animateNames = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			animateIDs[i] = ids[i].intValue();
			animateNames[i] = nameMap.get(ids[i]);
		}
	}
	
	private void readPartNames(File rootDir) throws Exception {
		File[] fs = rootDir.listFiles();
		List<String> nameList = new ArrayList<String>();
		for (File f : fs) {
			if (!f.isDirectory()) {
				continue;
			}
			String n = f.getName();
			if (!n.startsWith("b-")) {
				continue;
			}
			String partName = n.substring(2);
			boolean found = false;
			for (File ff : fs) {
				if (ff.getName().equals("s-" + partName)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new Exception("部位 " + partName + " 没有找到参考点图片目录。");
			}
			nameList.add(partName);
		}
		if (nameList.size() == 0) {
			throw new Exception("没有找到部位图片目录。");
		}
		partNames = new String[nameList.size()];
		nameList.toArray(partNames);
	}
}
