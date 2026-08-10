package com.pip.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pip.mapeditor.data.ProjectOwner;
import com.pip.mapeditor.data.ProjectParser;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipParticleEffectSet;

/**
 * 文件移动工具，可以移动多个文件，或者移动目录到一个指定目录里去。指定的目录不能是任何一个选中的文件所在的目录，也不能是任何一个
 * 选中的目录或者其子目录。
 * 如果要移动的文件引用的文件不在被移动范围内（cts引用pip，pef，hk和eqp引用cts。eqp引用hk和map引用ldf或cts不在禁止范围内），
 * 则不能移动。
 * 如果要移动的文件被不在移动范围内的文件引用（cts引用pip，pef，hk和eqp引用cts。eqp引用hk和map引用ldf或cts不在禁止范围内），
 * 则不能移动。
 * 如果移动的目标目录中有同名文件或目录，则不允许移动。
 * ldf和cts不能被移动到新的项目中，除非在本项目所有引用它的map文件都在移动列表中。
 * 如果ldf和cts文件被移动，所有引用它的map文件要被更新。
 * 如果hk文件在一个项目中，那么它不能被移动到其他项目里去，除非项目中关联它的所有eqp文件都在移动范围里。
 * 如果项目中的hk文件被移动，那么所有项目中引用它的eqp文件都必须更新。
 * 如果不在项目中的hk文件被移动，那么所有它所在子目录中的eqp文件都必须更新。
 * 如果eqp文件被移动，那么需要修改它引用的hk文件路径。
 * 如果map文件在一个项目中，那么它不能被移动到其他项目里去，除非项目中它关联的所有文件都在移动范围里。
 * 如果map文件被移动，所有它引用的ldf文件和cts文件的ref列表要更新。
 * 被移动的文件的所有附属文件也一起加入移动范围：
 *     cts：ctn
 *     ldf：lfi
 *     hk：hkc
 *     eqp：eqpc
 * @author lighthu
 */
public class FileMover {
	/**
	 * 移动一组文件或目录到另外一个目录。
	 * @param sources 源文件或目录
	 * @param targetDir 目标目录
	 */
	public static void move(String[] sources, File targetDir) throws Exception {
		// 先找出所有需要移动的文件和目录，记录在一个对应表里
		Map<String, String> fileMap = new HashMap<String, String>();
		for (String source : sources) {
			if (fileMap.containsKey(source)) {
				continue;
			}
			File f = new File(source);
			// 不能把文件移动到自己原来所在目录中
			if (f.getParentFile().equals(targetDir)) {
				continue;
			}
			if (f.isDirectory()) {
				// 不能把目录移动到自己或者子目录中去
				if (f.equals(targetDir) || Utils.isAncestorDir(f, targetDir)) {
					throw new Exception("不能把目录移动到自己或子目录中去。");
				}
				File tdir = new File(targetDir, f.getName());
				if (tdir.exists()) {
					throw new Exception("不支持目录覆盖，请先删除目标目录再试。");
				}
				
				// 记录目录对应关系
				fileMap.put(source, tdir.getAbsolutePath());
			} else {
				File tfile = new File(targetDir, f.getName());
				if (tfile.exists()) {
					throw new Exception("不支持文件覆盖，请先删除目标文件再试。");
				}
				
				// 记录文件对应关系
				fileMap.put(source, tfile.getAbsolutePath());
				
				// 附属文件一起移动：cts->ctn+cts.ref，ldf->lfi+ldf.ref，hk->hkc，eqp->eqpc
				if (source.toLowerCase().endsWith(".cts")) {
					fileMap.put(Utils.replaceSuffix(source, "ctn"), Utils.replaceSuffix(tfile.getAbsolutePath(), "ctn"));
					fileMap.put(Utils.replaceSuffix(source, "cts.ref"), Utils.replaceSuffix(tfile.getAbsolutePath(), "cts.ref"));
				} else if (source.toLowerCase().endsWith(".ldf")) {
					fileMap.put(Utils.replaceSuffix(source, "lfi"), Utils.replaceSuffix(tfile.getAbsolutePath(), "lfi"));
					fileMap.put(Utils.replaceSuffix(source, "ldf.ref"), Utils.replaceSuffix(tfile.getAbsolutePath(), "ldf.ref"));
				} else if (source.toLowerCase().endsWith(".hk")) {
					fileMap.put(Utils.replaceSuffix(source, "hkc"), Utils.replaceSuffix(tfile.getAbsolutePath(), "hkc"));
				} else if (source.toLowerCase().endsWith(".eqp")) {
					fileMap.put(Utils.replaceSuffix(source, "eqpc"), Utils.replaceSuffix(tfile.getAbsolutePath(), "eqpc"));
				}
			}
		}
		
		// 剔除不存在的文件
		Object[] paths = fileMap.keySet().toArray();
		for (int i = 0; i < paths.length; i++) {
			if (!(new File((String)paths[i]).exists())) {
				fileMap.remove(paths[i]);
			}
		}
		
		// 检查所有需要移动的文件是否是一个可移动的整体
		checkConsistency(fileMap);
		
		// 移动所有关联表里存在的文件
		paths = fileMap.keySet().toArray();
		for (int i = 0; i < paths.length; i++) {
			String srcPath = (String)paths[i];
			File srcFile = new File(srcPath);
			String destPath = fileMap.get(srcPath);
			File destFile = new File(destPath);
			if (srcFile.isDirectory()) {
				// 所有子目录中的文件加入映射表
				Set<String> subFiles = new HashSet<String>();
				Utils.findFilesInDir(srcFile, null, subFiles);
				for (String subPath : subFiles) {
					String rpath = Utils.getRelatePath(subPath, srcPath);
					String tarPath = new File(destFile, rpath).getAbsolutePath();
					fileMap.put(subPath, tarPath);
				}
			}
			EFSUtil.moveFile(srcFile, destFile);
		}
		
		// 处理ldf、cts、hk、eqp文件移动后的更新引用
		for (String sourcePath : fileMap.keySet()) {
			File srcFile = new File(sourcePath);
			if (srcFile.isDirectory()) {
				continue;
			}
			File destFile = new File(fileMap.get(sourcePath));
			String lname = srcFile.getName().toLowerCase();
			if (lname.endsWith(".ldf") || lname.endsWith(".cts")) {
				// 如果ldf和cts文件被移动，所有引用它的map文件要被更新。但是如果这个map文件也被移动了，则不处理，等后面处理map时再一起处理。
				processMapResourceMove(srcFile, destFile, fileMap);
				
				// 如果新文件在项目中，在项目里注册
				File projRoot = ProjectOwner.getProjectRootPath(destFile.getAbsolutePath());
				if (projRoot != null) {
					ProjectOwner po = ProjectOwner.find(projRoot.getAbsolutePath(), false);
					if (po != null) {
						po.regFile(destFile.getAbsolutePath());
					}
				}
			} else if (lname.endsWith(".hk")) {
				// 如果项目中的hk文件被移动，那么所有项目中引用它的eqp文件都必须更新。
				// 如果不在项目中的hk文件被移动，那么所有它所在子目录中的eqp文件都必须更新。
				processHkMove(srcFile, destFile, fileMap);
			} else if (lname.endsWith(".eqp") || lname.endsWith(".eqpc")) {
				// 如果eqp文件被移动，那么需要修改它引用的hk文件路径。
				try {
					EquipHookMap.updateHkPath(fileMap.get(sourcePath), sourcePath, fileMap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// 最后处理map文件移动后的更新引用
		for (String sourcePath : fileMap.keySet()) {
			File srcFile = new File(sourcePath);
			if (srcFile.isDirectory()) {
				continue;
			}
			File destFile = new File(fileMap.get(sourcePath));
			String lname = srcFile.getName().toLowerCase();
			if (lname.endsWith(".map")) {
				// 如果map文件被移动，所有它引用的ldf文件和cts文件的ref列表要更新。
				// 如果map文件引用的ldf文件或cts文件也移动了，则map文件本身也要调整以适应。
				updateMapRefs(srcFile, destFile, fileMap);
			}
		}
	}
	
	/*
	 * 检查要移动的所有文件是不是一个可移动的整体。规则包括：
	 * 如果要移动的文件引用的文件不在被移动范围内（cts引用pip，pef，hk和eqp引用cts。eqp引用hk和map引用ldf或cts不在禁止范围内），
	 * 则不能移动。
     * 如果要移动的文件被不在移动范围内的文件引用（cts引用pip，pef，hk和eqp引用cts。eqp引用hk和map引用ldf或cts不在禁止范围内），
     * 则不能移动。
     * ldf和cts不能被移动到新的项目中，除非在本项目所有引用它的map文件都在移动列表中。
     * 如果hk文件在一个项目中，那么它不能被移动到其他项目里去，除非项目中关联它的所有eqp文件都在移动范围里。
     * 如果map文件在一个项目中，那么它不能被移动到其他项目里去，除非项目中它关联的所有文件都在移动范围里。
	 */
	private static void checkConsistency(Map<String, String> fileMap) throws Exception {
		for (String sourcePath : fileMap.keySet()) {
			File f = new File(sourcePath);
			if (f.isDirectory()) {
				continue;
			}
			
			// 检查是否此文件本目录引用的文件都在移动范围内
			if (!fileMap.containsKey(f.getParentFile().getAbsolutePath())) {
				String[] refFiles = findRelateFiles(sourcePath);
				for (String refFile : refFiles) {
					if (!fileMap.containsKey(refFile)) {
						throw new Exception(sourcePath + "引用的文件" + refFile + "没有被选中，不能执行移动操作。");
					}
				}
			}
			
			// 检查是否所有本目录引用此文件的文件都在移动范围内
			checkLocalRef(sourcePath, fileMap.keySet());
			
			// ldf和cts不能被移动到新的项目中，除非在本项目所有引用它的map文件都在移动列表中。
			if (sourcePath.toLowerCase().endsWith(".ldf") || sourcePath.toLowerCase().endsWith(".cts")) {
				String targetPath = fileMap.get(sourcePath);
				File srcProj = ProjectOwner.getProjectRootPath(sourcePath);
				if (srcProj != null && !srcProj.equals(ProjectOwner.getProjectRootPath(targetPath))) {
					String[] refMaps = ProjectParser.getFileRefList(sourcePath);
					for (String refMap : refMaps) {
						String fullMapPath = new File(srcProj, refMap).getAbsolutePath();
						if (!fileMap.containsKey(fullMapPath)) {
							throw new Exception(sourcePath + "被" + refMap + "引用，不能移动到其他项目中。");
						}
					}
				}
			}
			
			// 如果hk文件在一个项目中，那么它不能被移动到其他项目里去，除非项目中关联它的所有eqp文件都在移动范围里。
			if (sourcePath.toLowerCase().endsWith(".hk")) {
				String targetPath = fileMap.get(sourcePath);
				File srcProj = ProjectOwner.getProjectRootPath(sourcePath);
				if (srcProj != null && !srcProj.equals(ProjectOwner.getProjectRootPath(targetPath))) {
					String[] refEqps = findEqpRefHk(sourcePath);
					for (String refEqp : refEqps) {
						String fullPath = new File(srcProj, refEqp).getAbsolutePath();
						if (!fileMap.containsKey(fullPath)) {
							throw new Exception(sourcePath + "被" + refEqp + "引用，不能移动到其他项目中。");
						}
					}
				}
			}
			
			// 如果map文件在一个项目中，那么它不能被移动到其他项目里去，除非项目中它关联的所有文件都在移动范围里。
			if (sourcePath.toLowerCase().endsWith(".map")) {
				String targetPath = fileMap.get(sourcePath);
				File srcProj = ProjectOwner.getProjectRootPath(sourcePath);
				if (srcProj != null && !srcProj.equals(ProjectOwner.getProjectRootPath(targetPath))) {
					String[] refFiles = FileRenamer.getMapRef(new File(sourcePath));
					for (String refFile : refFiles) {
						String fullPath = new File(srcProj, refFile).getAbsolutePath();
						if (!fileMap.containsKey(fullPath)) {
							throw new Exception(sourcePath + "应用的资源" + refFile + "没有被选中，不能移动到其他项目中。");
						}
					}
				}
			}
		}
	}
	
	/*
	 * 找出引用一个hk文件的所有eqp文件（在项目目录或者hk所在目录中）。
	 */
	private static String[] findEqpRefHk(String hkFile) {
		File f = new File(hkFile);
		File p = f.getParentFile();
		File projDir = ProjectOwner.getProjectRootPath(hkFile);
		if (projDir != null) {
			p = projDir;
		}
		
		// 找出子目录中所有的eqp
		Set<String> childFiles = new HashSet<String>();
		Utils.findFilesInDir(p, ".eqp", childFiles);
		List<String> retList = new ArrayList<String>();
		for (String reff : childFiles) {
			// 读取eqp文件，检查是否引用了指定的hk文件
			String relatePath = Utils.getRelatePath(hkFile, reff);
			try {
				if (EquipHookMap.checkHkRef(new File(reff), relatePath)) {
					retList.add(reff);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String[] ret = new String[retList.size()];
		retList.toArray(ret);
		return ret;
	}
	
	/*
	 * 检查一个文件是否被不在移动范围内的文件引用，只考虑cts引用pip，pef，hk和eqp引用cts的情况。
	 */
	private static void checkLocalRef(String path, Set<String> excludeFiles) throws Exception {
		if (path.toLowerCase().endsWith(".pip")) {
			// pip文件可能被本目录下的cts文件引用
			File f = new File(path);
			File p = f.getParentFile();
			if (excludeFiles.contains(p.getAbsolutePath())) {
				// 如果本目录都在移动列表，不用检查了
				return;
			}
			
			// 找出所有引用此pip的cts文件
			File[] ctsFiles = p.listFiles(new FileExtensionFilter(new String[] { "cts" }, false));
			for (File ctsf : ctsFiles) {
				if (excludeFiles.contains(ctsf.getAbsolutePath())) {
					continue;
				}
				PipAnimateSet pas = null;
				try {
					pas = new PipAnimateSet();
					pas.load(ctsf);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				int cnt = pas.getFileCount();
				for (int i = 0; i < cnt; i++) {
					String name = pas.getFileName(i);
					if (name.equals(f.getName())) {
						throw new Exception(path + "被" + ctsf.getName() + "引用，不能移动。");
					}
				}
			}
		} else if (path.toLowerCase().endsWith(".cts")) {
			// cts文件可能被本目录下的pef，hk或eqp文件引用
			File f = new File(path);
			File p = f.getParentFile();
			if (excludeFiles.contains(p.getAbsolutePath())) {
				// 如果本目录都在删除列表，不用检查了
				return;
			}
			
			// 找出所有引用此cts的pef,hk或eqp文件
			File[] refFiles = p.listFiles(new FileExtensionFilter(new String[] { "pef", "hk", "eqp" }, false));
			for (File reff : refFiles) {
				if (excludeFiles.contains(reff.getAbsolutePath())) {
					continue;
				}
				
				// 检查是否引用
				if (reff.getName().toLowerCase().endsWith(".pef")) {
					PipParticleEffectSet pes = new PipParticleEffectSet();
					try {
						pes.load(reff);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					if (pes.getAnimateFile().equals(f)) {
						throw new Exception(path + "被" + reff.getName() + "引用，不能移动。");
					}
				} else {
					String ctsName = null;
					try {
						byte[] fdata = Utils.loadFileData(reff);
						ctsName = new DataInputStream(new ByteArrayInputStream(fdata)).readUTF();
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					if (ctsName.equals(f.getName())) {
						throw new Exception(path + "被" + reff.getName() + "引用，不能移动。");
					}
				}
			}
		}
	}
	
	/*
	 * 找出一个文件在本目录下引用的其他文件。
	 * pef，hk，eqp引用cts，cts引用pip
	 */
	private static String[] findRelateFiles(String srcFile) throws Exception {
		List<String> retList = new ArrayList<String>();
		List<File> stack = new ArrayList<File>();
		stack.add(new File(srcFile));
		while (stack.size() > 0) {
			File srcF = stack.remove(0);
			File srcP = srcF.getParentFile();
			try {
				if (srcF.getName().toLowerCase().endsWith(".pef")) {
					PipParticleEffectSet pes = new PipParticleEffectSet();
					pes.load(srcF);
					retList.add(pes.getAnimateFile().getAbsolutePath());
					stack.add(pes.getAnimateFile());
				} else if (srcF.getName().toLowerCase().endsWith(".hk") || srcFile.toLowerCase().endsWith(".eqp")) {
					byte[] fdata = Utils.loadFileData(srcF);
					String ctsName = new DataInputStream(new ByteArrayInputStream(fdata)).readUTF();
					retList.add(new File(srcP, ctsName).getAbsolutePath());
					stack.add(new File(srcP, ctsName));
				} else if (srcF.getName().toLowerCase().endsWith(".cts")) {
					PipAnimateSet pas = new PipAnimateSet();
					pas.load(srcF);
					for (int i = 0; i < pas.getFileCount(); i++) {
						retList.add(pas.getSourceFile(i).getAbsolutePath());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String[] ret = new String[retList.size()];
		retList.toArray(ret);
		return ret;
	}
	
	/*
	 * 如果ldf和cts文件被移动，所有引用它的map文件要被更新。但是如果这个map文件也被移动了，则不处理，等后面处理map时再一起处理。
	 */
	private static void processMapResourceMove(File srcFile, File destFile, Map<String, String> fileMap) throws Exception {
		// 从目标位置读取出引用这个文件的map文件的列表
		String[] refMaps = ProjectParser.getFileRefList(destFile.getAbsolutePath());
		if (refMaps == null || refMaps.length == 0) {
			return;
		}
		File oldProjDir = ProjectOwner.getProjectRootPath(srcFile.getAbsolutePath());
		File newProjDir = ProjectOwner.getProjectRootPath(destFile.getAbsolutePath());
		if (oldProjDir == null || newProjDir == null) {
			return;
		}
		for (String refMap : refMaps) {
			File mapF = new File(oldProjDir, refMap);
			if (fileMap.containsKey(mapF.getAbsolutePath())) {
				// 此map文件也被移动了，等更新那个map时再来刷新引用关系，这里暂时把这个引用关系断开
				ProjectParser.removeFileRef(destFile.getAbsolutePath(), mapF.getAbsolutePath());
				continue;
			}
	
			// 计算新旧路径和hashcode
			String oldPath = ProjectOwner.getProjectRelatePath(srcFile.getAbsolutePath());
			String newPath = ProjectOwner.getProjectRelatePath(destFile.getAbsolutePath());
			if (oldPath.toLowerCase().endsWith(".cts")) {
				oldPath = Utils.replaceSuffix(oldPath, "ctn");
				newPath = Utils.replaceSuffix(newPath, "ctn");
			}
			int oldHash = ProjectOwner.getHashCode(oldPath);
			int newHash = ProjectOwner.getHashCode(newPath);
			try {
				FileRenamer.replaceMapRef(mapF, oldHash, oldPath, newHash, newPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 如果项目中的hk文件被移动，那么所有项目中引用它的eqp文件都必须更新。
	 * 如果不在项目中的hk文件被移动，那么所有它所在子目录中的eqp文件都必须更新。
	 */
	private static void processHkMove(File srcFile, File destFile, Map<String, String> fileMap) throws Exception {
		File searchDir = ProjectOwner.getProjectRootPath(srcFile.getAbsolutePath());
		if (searchDir == null) {
			searchDir = srcFile.getParentFile();
		}
		if (searchDir.exists()) {
			Set<String> fileSet = new HashSet<String>();
			Utils.findFilesInDir(searchDir, ".eqp", fileSet);
			Utils.findFilesInDir(searchDir, ".eqpc", fileSet);
			for (String fpath : fileSet) {
				try {
					EquipHookMap.updateHkPath(fpath, fpath, fileMap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * 如果map文件被移动，所有它引用的ldf文件和cts文件的ref列表要更新。
	 * 如果map文件引用的ldf文件或cts文件也移动了，则map文件本身也要调整以适应。
	 */
	private static void updateMapRefs(File srcFile, File destFile, Map<String, String> fileMap) throws Exception {
		try {
			File oldProj = ProjectOwner.getProjectRootPath(srcFile.getAbsolutePath());
			String[] refFiles = FileRenamer.getMapRef(destFile);
			for (String refFile : refFiles) {
				if (refFile.toLowerCase().endsWith(".ctn")) {
					refFile = Utils.replaceSuffix(refFile, "cts");
				}
				String oldRefPath = new File(oldProj, refFile).getAbsolutePath();
				if (fileMap.containsKey(oldRefPath)) {
					// 对于map和ldf一起移动的情况，需要更新map文件，并重新设置引用关系
					String newRefPath = ProjectOwner.getProjectRelatePath(fileMap.get(oldRefPath));
					int oldHash = ProjectOwner.getHashCode(refFile);
					int newHash = ProjectOwner.getHashCode(newRefPath);
					FileRenamer.replaceMapRef(destFile, oldHash, refFile, newHash, newRefPath);
					ProjectParser.addFileRef(fileMap.get(oldRefPath), destFile.getAbsolutePath());
				} else {
					// 对于ldf没有一起移动的情况，需要断开旧的引用关系，并设置新的引用关系
					ProjectParser.removeFileRef(oldRefPath, srcFile.getAbsolutePath());
					ProjectParser.addFileRef(oldRefPath, destFile.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
