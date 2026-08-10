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
 * 文件复制工具，可以复制多个文件，或者复制目录到一个指定目录里去。如果指定的目录就是源文件的父目录，复制工具
 * 会为新文件指定一个新名字。
 * 如果要复制的文件拥有引用文件，而且不是本目录复制，那么我们尝试把引用的文件也复制过去或者链接过去。
 * 需要处理的引用包括：
 *     cts引用pip
 *     pef引用cts
 *     hk引用cts
 *     eqp引用cts和hk（hk通过链接引用）
 *     map引用ldf和cts（通过链接引用）
 * 还有一些附属文件也需要复制：
 *     cts：ctn
 *     ldf：lfi
 *     hk：hkc
 *     eqp：eqpc
 * @author lighthu
 */
public class FileCopier {
	/**
	 * 复制一组文件或目录。
	 * @param target 文件
	 */
	public static void copy(String[] sources, File targetDir) throws Exception {
		// 先找出所有需要拷贝的文件和目录，记录在一个对应表里
		Map<String, String> fileMap = new HashMap<String, String>();
		for (String source : sources) {
			if (fileMap.containsKey(source)) {
				continue;
			}
			File f = new File(source);
			if (f.isDirectory()) {
				// 不能把目录拷贝到自己或者子目录中去
				if (f.equals(targetDir) || Utils.isAncestorDir(f, targetDir)) {
					throw new Exception("不能把目录复制到自己或子目录中去。");
				}
				
				File tdir = new File(targetDir, f.getName());
				if (tdir.equals(f)) {
					// 本目录拷贝，重命名
					int id = 1;
					do {
						tdir = new File(targetDir, f.getName() + "_" + id);
						if (!tdir.exists()) {
							break;
						}
						id++;
					} while (true);
				} else if (tdir.exists()) {
					throw new Exception("不支持目录覆盖，请先删除目标目录再试。");
				}
				
				// 记录目录拷贝关系
				fileMap.put(source, tdir.getAbsolutePath());
			} else {
				File tfile = new File(targetDir, f.getName());
				if (tfile.equals(f)) {
					// 本目录拷贝，重命名
					String fname = f.getName();
					int pos = fname.lastIndexOf('.');
					String suffix = "";
					if (pos != -1) {
						suffix = fname.substring(pos);
						fname = fname.substring(0, pos);
					}
					int id = 1;
					do {
						tfile = new File(targetDir, fname + "_" + id + suffix);
						if (!tfile.exists()) {
							break;
						}
						id++;
					} while (true);
				} else if (tfile.exists()) {
					throw new Exception("不支持文件覆盖，请先删除目标文件再试。");
				} else {
					// 拷贝关联文件：pef，hk，eqp引用的cts，cts引用的pip
					String[] relateFiles = findRelateFiles(source);
					for (String relateFile : relateFiles) {
						if (fileMap.containsKey(relateFile)) {
							continue;
						}
						String rname = new File(relateFile).getName();
						if (new File(targetDir, rname).exists()) {
							throw new Exception("不支持文件覆盖，请先删除目标文件再试。");
						}
						String tpath = new File(targetDir, rname).getAbsolutePath();
						fileMap.put(relateFile, tpath);
						
						// 拷贝附属文件：cts->ctn，ldf->lfi，hk->hkc，eqp->eqpc
						if (relateFile.toLowerCase().endsWith(".cts")) {
							fileMap.put(Utils.replaceSuffix(relateFile, "ctn"), Utils.replaceSuffix(tpath, "ctn"));
						} else if (relateFile.toLowerCase().endsWith(".ldf")) {
							fileMap.put(Utils.replaceSuffix(relateFile, "lfi"), Utils.replaceSuffix(tpath, "lfi"));
						} else if (relateFile.toLowerCase().endsWith(".hk")) {
							fileMap.put(Utils.replaceSuffix(relateFile, "hkc"), Utils.replaceSuffix(tpath, "hkc"));
						} else if (relateFile.toLowerCase().endsWith(".eqp")) {
							fileMap.put(Utils.replaceSuffix(relateFile, "eqpc"), Utils.replaceSuffix(tpath, "eqpc"));
						}
					}
				}
				
				// 记录文件拷贝关系
				fileMap.put(source, tfile.getAbsolutePath());
				
				// 拷贝附属文件：cts->ctn，ldf->lfi，hk->hkc，eqp->eqpc
				if (source.toLowerCase().endsWith(".cts")) {
					fileMap.put(Utils.replaceSuffix(source, "ctn"), Utils.replaceSuffix(tfile.getAbsolutePath(), "ctn"));
				} else if (source.toLowerCase().endsWith(".ldf")) {
					fileMap.put(Utils.replaceSuffix(source, "lfi"), Utils.replaceSuffix(tfile.getAbsolutePath(), "lfi"));
				} else if (source.toLowerCase().endsWith(".hk")) {
					fileMap.put(Utils.replaceSuffix(source, "hkc"), Utils.replaceSuffix(tfile.getAbsolutePath(), "hkc"));
				} else if (source.toLowerCase().endsWith(".eqp")) {
					fileMap.put(Utils.replaceSuffix(source, "eqpc"), Utils.replaceSuffix(tfile.getAbsolutePath(), "eqpc"));
				}
			}
		}
		
		// 拷贝所有关联表里存在的文件，不存在的文件从列表中删除
		Object[] paths = fileMap.keySet().toArray();
		for (int i = 0; i < paths.length; i++) {
			String srcPath = (String)paths[i];
			File srcFile = new File(srcPath);
			if (!srcFile.exists()) {
				fileMap.remove(srcPath);
			} else {
				String destPath = fileMap.get(srcPath);
				File destFile = new File(destPath);
				if (srcFile.isDirectory()) {
					EFSUtil.copyDir(srcFile, destFile);
					
					// 所有拷贝的文件加入映射表
					Set<String> subFiles = new HashSet<String>();
					Utils.findFilesInDir(srcFile, null, subFiles);
					for (String subPath : subFiles) {
						String rpath = Utils.getRelatePath(subPath, srcPath);
						String tarPath = new File(destFile, rpath).getAbsolutePath();
						fileMap.put(subPath, tarPath);
					}
				} else {
					EFSUtil.copyFile(srcFile, destFile);
				}
			}
		}
		
		// 所有新创建的eqp文件和eqpc文件，更新其中的hk文件链接
		for (String sourcePath : fileMap.keySet()) {
			if (sourcePath.toLowerCase().endsWith(".eqp") || sourcePath.toLowerCase().endsWith(".eqpc")) {
				String targetPath = fileMap.get(sourcePath);
				try {
					EquipHookMap.updateHkPath(targetPath, sourcePath, fileMap);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// 所有新创建的额map文件，更新其中的map文件链接
		for (String sourcePath : fileMap.keySet()) {
			if (sourcePath.toLowerCase().endsWith(".map")) {
				String targetPath = fileMap.get(sourcePath);
				addMapRef(targetPath);
			}
		}
		
		// 所有新创建的ldf和cts文件，在项目中注册（如果在项目中）
		for (String sourcePath : fileMap.keySet()) {
			if (sourcePath.toLowerCase().endsWith(".ldf") || sourcePath.toLowerCase().endsWith(".cts")) {
				String targetPath = fileMap.get(sourcePath);
				File projRoot = ProjectOwner.getProjectRootPath(targetPath);
				if (projRoot != null) {
					ProjectOwner po = ProjectOwner.find(projRoot.getAbsolutePath(), false);
					if (po != null) {
						po.regFile(targetPath);
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
	 * 添加一个map文件所引用的所有cts和ldf文件对此map文件的引用关系。
	 */
	private static void addMapRef(String path) throws Exception {
		// 如果map文件在项目下，才能做此操作
		File projRoot = ProjectOwner.getProjectRootPath(path);
		if (projRoot == null) {
			return;
		}
		
		// 找出所有此MAP文件引用的文件
		try {
			String[] refFiles = FileRenamer.getMapRef(new File(path));
			for (String refFile : refFiles) {
				if (refFile.toLowerCase().endsWith(".ctn")) {
					refFile = Utils.replaceSuffix(refFile, "cts");
				}
				File refF = new File(projRoot, refFile);
				ProjectParser.addFileRef(refF.getAbsolutePath(), path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
