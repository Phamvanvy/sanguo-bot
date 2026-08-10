package com.pip.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.mapeditor.data.ProjectOwner;
import com.pip.mapeditor.data.ProjectParser;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipParticleEffectSet;

/**
 * 文件改名工具，会扫描所有引用此文件的其他文件，并做引用更改。
 * pip改名，需要修改cts。
 * ldf改名，需要修改map,lfi,ldf.ref
 * cts改名，需要修改pef，map，hk，eqp, ctn, cts.ref。
 * hk改名，需要修改eqp和hkc。
 * eqp改名，需要修改eqpc。
 * map改名，修改所有此map文件引用的文件对应的ref文件。
 * @author lighthu
 */
public class FileRenamer {
	/**
	 * 重命名一个文件。
	 * @param fromFullPath 源文件全路径
	 * @param newName 目标文件名
	 * @return 修改成功返回true。
	 */
	public static boolean rename(String fromFullPath, String newName) {
		try {
			renameSafe(fromFullPath, newName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static void renameSafe(String fromFullPath, String newName) throws Exception {
		File fromF = new File(fromFullPath);
		File toF = new File(fromF.getParentFile(), newName);
		
		// 测试文件改名
		if (!fromF.renameTo(toF)) {
			throw new IOException();
		}
		toF.renameTo(fromF);
		
		File dir = fromF.getParentFile();
		String fname = fromF.getName();
		String tname = toF.getName();
		String lname = fname.toLowerCase();
		if (lname.endsWith(".pip")) {
			// pip改名后，需要修改同目录下引用这个pip的cts文件里的引用文件名
			handleFileRenamed(dir, "cts", new FixPipInCts(), fname, tname, false);
		} else if (lname.endsWith(".ldf")) {
			// ldf改名后，需要修改同目录下对应的lfi文件名和ldf.ref文件名，另外还需要读取ldf.ref，修改里面记录的所有引用此
			// ldf文件的map文件中的引用名称和hashcode
			File lfiF = Utils.replaceSuffix(fromF, "lfi");
			File lfiT = Utils.replaceSuffix(toF, "lfi");
			lfiF.renameTo(lfiT);
			File refF = Utils.replaceSuffix(fromF, "ldf.ref");
			File refT = Utils.replaceSuffix(toF, "ldf.ref");
			refF.renameTo(refT);
			
			updateRefedMap(fromF, toF);
		} else if (lname.endsWith(".cts")) {
			// cts改名后，需要修改同目录下引用这个cts文件的pef、hk、eqp文件里的引用文件名，修改同目录下ctn和cts.ref文件名。
			// 另外还需要读取cts.ref，修改里面记录的所有引用此cts文件的map文件中的引用名称和hashcode。
			handleFileRenamed(dir, "pef", new FixCtsInPef(), fname, tname, false);
			handleFileRenamed(dir, "hk", new FixCtsInHkOrEqp(), fname, tname, false);
			handleFileRenamed(dir, "eqp", new FixCtsInHkOrEqp(), fname, tname, false);
			
			File ctnF = Utils.replaceSuffix(fromF, "ctn");
			File ctnT = Utils.replaceSuffix(toF, "ctn");
			ctnF.renameTo(ctnT);
			File refF = Utils.replaceSuffix(fromF, "cts.ref");
			File refT = Utils.replaceSuffix(toF, "cts.ref");
			refF.renameTo(refT);
			
			updateRefedMap(fromF, toF);
		} else if (lname.endsWith(".hk")) {
			// hk文件改名，需要搜索所有子目录，找到引用这个hk文件的eqp文件，修改里面的引用文件名。另外还需要修改同目录下的hkc
			// 文件名。
			File rootDir;
			File projDir = ProjectOwner.getProjectRootPath(dir.getAbsolutePath());
			if (projDir == null) {
				rootDir = dir;
			} else {
				rootDir = projDir;
			}
			Set<String> eqpFiles = new HashSet<String>();
			Utils.findFilesInDir(rootDir, "eqp", eqpFiles);
			Utils.findFilesInDir(rootDir, "eqpc", eqpFiles);
			for (String eqpFile : eqpFiles) {
				String fromRelate = Utils.getRelatePath(fromF.getAbsolutePath(), eqpFile);
				String toRelate = Utils.getRelatePath(toF.getAbsolutePath(), eqpFile);
				EquipHookMap.changeHkName(new File(eqpFile), fromRelate, toRelate);
			}
			
			File hkcF = Utils.replaceSuffix(fromF, "hkc");
			File hkcT = Utils.replaceSuffix(toF, "hkc");
			hkcF.renameTo(hkcT);
		} else if (lname.endsWith(".eqp")) {
			// eqp文件改名，需要同目录下的eqpc文件名。
			File eqpcF = Utils.replaceSuffix(fromF, "eqpc");
			File eqpcT = Utils.replaceSuffix(toF, "eqpc");
			eqpcF.renameTo(eqpcT);
		} else if (lname.endsWith(".map")) {
			// map文件改名，修改所有此map文件引用的文件对应的ref文件。
			updateMapRef(fromF, toF);
		} else {
			// 其他类型文件，直接改名即可，不需要做额外处理。
		}
		
		// 实际改名
		fromF.renameTo(toF);
	}
	
	/*
	 * 当一个文件改名后，修改同目录下通过名字引用这个文件的其他文件中的引用文件名，以保证文件格式正确。
	 * @param dir 要搜索的目录
	 * @param suffix 要处理的文件的扩展名
	 * @param processor 处理器接口
	 * @param renameFrom 文件原名（相对于dir的路径）
	 * @param renameTo 文件新名（相对于dir的路径）
	 * @param includeSubDir 是否搜索子目录
	 */
	private static void handleFileRenamed(File dir, String suffix, IFixFileRef processor, String renameFrom, String renameTo, boolean includeSubDir) {
		File[] files = dir.listFiles(new FileExtensionFilter(new String[] { suffix }, includeSubDir));
		for (File f : files) {
			if (f.isDirectory()) {
				handleFileRenamed(f, suffix, processor, "../" + renameFrom, "../" + renameTo, includeSubDir);
			} else {
				try {
					processor.fix(f, renameFrom, renameTo);
				} catch (Exception e) {
					System.err.println("process " + f + " error:");
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * 修改一个LDF文件或者CTS文件名以后，修改所有引用此文件的地图的引用。注意MAP文件引用CTS时是用CTN文件名引用的。
	 */
	private static void updateRefedMap(File fromF, File toF) throws Exception {
		// 如果两个文件都在同一个项目下，才能做此操作
		File projRoot = ProjectOwner.getProjectRootPath(fromF.getAbsolutePath());
		if (projRoot == null || !projRoot.equals(ProjectOwner.getProjectRootPath(toF.getAbsolutePath()))) {
			return;
		}
		
		// 找出所有引用此文件的MAP文件
		String[] mapList = null;
		try {
			mapList = ProjectParser.getFileRefList(toF.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mapList == null || mapList.length == 0) {
			return;
		}
		
		// 计算新旧路径和hashcode
		String oldPath = ProjectOwner.getProjectRelatePath(fromF.getAbsolutePath());
		String newPath = ProjectOwner.getProjectRelatePath(toF.getAbsolutePath());
		if (oldPath.toLowerCase().endsWith(".cts")) {
			oldPath = Utils.replaceSuffix(oldPath, "ctn");
			newPath = Utils.replaceSuffix(newPath, "ctn");
		}
		int oldHash = ProjectOwner.getHashCode(oldPath);
		int newHash = ProjectOwner.getHashCode(newPath);
		for (String mapPath : mapList) {
			try {
				replaceMapRef(new File(projRoot, mapPath), oldHash, oldPath, newHash, newPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 在项目中注册新文件
		ProjectOwner po = ProjectOwner.find(projRoot.getAbsolutePath(), false);
		if (po != null) {
			po.regFile(new File(projRoot, newPath).getAbsolutePath());
		}
	}
	
	/*
	 * 打开一个MAP文件，把其中一个文件引用（CTN或者LDF）修改为新的路径，HASHCODE也相应修改。
	 * 为了提交处理效率，这个方法直接从XML层面上处理，而不把MAP文件全部载入。
	 * 具体处理规则是：
	 *     <ROOT> -> animates -> cts元素，如果hashCode字段和oldHash匹配，则替换hashCode属性和file属性
	 *     <ROOT> -> landform元素，如果hashCode字段和oldHash匹配，则替换hashCode属性和file属性
	 *     <ROOT> -> map -> layer -> npc元素，如果animateRef和oldHash匹配，则替换animateRef属性，如果此npc元素还有子npc元素，同样处理
	 */
	public static void replaceMapRef(File mapFile, int oldHash, String oldPath, int newHash, String newPath) throws Exception {
		Document doc = Utils.loadDOM(mapFile);
		Element root = doc.getRootElement();
		if (!"true".equals(root.getAttributeValue("libmode"))) {
			return;
		}
		String oldHashStr = String.valueOf(oldHash);
		String newHashStr = String.valueOf(newHash);
		boolean changed = false;
		
		// 处理所有<ROOT> -> animates -> cts元素
		List list = root.getChild("animates").getChildren("cts");
		for (int i = 0; i < list.size(); i++) {
			Element child = (Element)list.get(i);
			if (child.getAttributeValue("hashCode").equals(oldHashStr)) {
				child.getAttribute("hashCode").setValue(newHashStr);
				child.getAttribute("file").setValue(newPath);
				changed = true;
			}
		}
		
		// 处理所有<ROOT> -> landform元素
		list = root.getChildren("landform");
		for (int i = 0; i < list.size(); i++) {
			Element child = (Element)list.get(i);
			if (child.getAttributeValue("hashCode").equals(oldHashStr)) {
				child.getAttribute("hashCode").setValue(newHashStr);
				child.getAttribute("file").setValue(newPath);
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
				if (child.getAttributeValue("animateRef").equals(oldHashStr)) {
					child.getAttribute("animateRef").setValue(newHashStr);
					changed = true;
				}
			}
			stack.addAll(list);
		}
		
		// 保存文件
		if (changed) {
			Utils.saveDOM(doc, mapFile, false);
			System.out.println("file changed: " + mapFile);
		}
	}
	
	/*
	 * 修改一个MAP文件名后，修改此MAP文件所引用的所有ldf文件和cts文件对应的ref文件。
	 */
	private static void updateMapRef(File fromF, File toF) throws Exception {
		// 如果两个文件都在同一个项目下，才能做此操作
		File projRoot = ProjectOwner.getProjectRootPath(fromF.getAbsolutePath());
		if (projRoot == null || !projRoot.equals(ProjectOwner.getProjectRootPath(toF.getAbsolutePath()))) {
			return;
		}
		
		// 找出所有此MAP文件引用的文件
		try {
			String[] refFiles = getMapRef(fromF);
			for (String refFile : refFiles) {
				if (refFile.toLowerCase().endsWith(".ctn")) {
					refFile = Utils.replaceSuffix(refFile, "cts");
				}
				File refF = new File(projRoot, refFile);
				ProjectParser.removeFileRef(refF.getAbsolutePath(), fromF.getAbsolutePath());
				ProjectParser.addFileRef(refF.getAbsolutePath(), toF.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 分析一个map文件所引用的所有ldf文件和cts文件。
	 * @return 返回文件相对路径数组，注意cts文件的引用路径里写的是ctn
	 */
	public static String[] getMapRef(File mapFile) throws Exception {
		Document doc = Utils.loadDOM(mapFile);
		Element root = doc.getRootElement();
		if (!"true".equals(root.getAttributeValue("libmode"))) {
			return new String[0];
		}
		List<String> retList = new ArrayList<String>();
		
		// 处理所有<ROOT> -> animates -> cts元素
		List list = root.getChild("animates").getChildren("cts");
		for (int i = 0; i < list.size(); i++) {
			Element child = (Element)list.get(i);
			retList.add(child.getAttributeValue("file"));
		}
		
		// 处理所有<ROOT> -> landform元素
		list = root.getChildren("landform");
		for (int i = 0; i < list.size(); i++) {
			Element child = (Element)list.get(i);
			retList.add(child.getAttributeValue("file"));
		}
		
		String[] ret = new String[retList.size()];
		retList.toArray(ret);
		return ret;
	}
	
	/*
	 * 文件引用改名处理器接口。如果文件A引用文件B，那么文件B改名时，需要对文件A调用此接口的fix方法，把引用名称
	 * 也修改成新的。
	 */
	static interface IFixFileRef {
		public void fix(File resFile, String renameFrom, String renameTo) throws Exception;
	}
	
	/*
	 * pip文件名修改后，修改cts文件中引用的pip文件名。
	 */
	static class FixPipInCts implements IFixFileRef {
		public void fix(File resFile, String renameFrom, String renameTo) throws Exception {
			try {
				PipAnimateSet pas = new PipAnimateSet();
				pas.load(resFile);
				boolean hit = false;
				int cnt = pas.getFileCount();
				for (int i = 0; i < cnt; i++) {
					String name = pas.getFileName(i);
					if (name.equals(renameFrom)) {
						pas.setFileName(i, renameTo);
						hit = true;
					}
				}
				if (hit) {
					pas.save(resFile, true);
					pas.save(Utils.replaceSuffix(resFile, "ctn"), false);
					System.out.println("file changed: " + resFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * cts文件名修改后，修改pef文件中引用的cts文件名。
	 */
	static class FixCtsInPef implements IFixFileRef {
		public void fix(File resFile, String renameFrom, String renameTo) throws Exception {
			PipParticleEffectSet pef = new PipParticleEffectSet();
			pef.load(resFile);
			if (pef.getAnimateFile().getName().equals(renameFrom)) {
				pef.setAnimateFileName(new File(resFile.getParentFile(), renameTo));
				pef.save(resFile);
				System.out.println("file changed: " + resFile);
			}
		}
	}
	
	/*
	 * cts文件名修改后，修改hk文件中引用的cts文件名。
	 */
	static class FixCtsInHkOrEqp implements IFixFileRef {
		public void fix(File resFile, String renameFrom, String renameTo) throws Exception {
			byte[] content = Utils.loadFileData(resFile);
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
			String ctsName = dis.readUTF();
			if (ctsName.equals(renameFrom)) {
				byte[] def = new byte[dis.available()];
				dis.readFully(def);
				dis.close();
				ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(byteArrayOut);
				dos.writeUTF(renameTo);
				dos.write(def);
				dos.close();
				Utils.saveFileData(resFile, byteArrayOut.toByteArray());
				System.out.println("file changed: " + resFile);
			}
		}
	}
}
