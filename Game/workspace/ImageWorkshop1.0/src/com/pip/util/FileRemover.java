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
 * 文件删除工具，可以删除多个文件，或者删除目录。
 * 如果删除的是文件，需要检查这个文件是否被其他文件引用，如果有，则不能删除。
 * 如果删除的是map文件，需要删除此map文件所有引用文件的关系。
 * 可能的文件引用关系包括：
 *     cts引用pip
 *     map引用ldf
 *     pef,map,hk,eqp引用cts
 *     eqp引用hk
 * 如果文件可以被删除，下面的文件还需要删除附属文件：
 *     cts：ctn，cts.ref
 *     ldf：lfi，ldf.ref
 *     hk：hkc
 *     eqp：eqpc
 * @author lighthu
 */
public class FileRemover {
	/**
	 * 删除一组文件或目录。
	 * @param target 文件
	 */
	public static void remove(String[] target) throws Exception {
		// 找出所有受影响需要删除的文件（递归子目录）
		Set<String> affectFiles = new HashSet<String>();
		for (String t : target) {
			File f = new File(t);
			if (f.isFile()) {
				affectFiles.add(t);
			} else if (f.isDirectory()) {
				Utils.findFilesInDir(f, null, affectFiles);
			}
		}
		
		// 检查是否有任何一个要被删除的文件被不在删除列表中的文件引用了
		for (String path : affectFiles) {
			checkRef(path, affectFiles);
		}
		
		// 找出删除列表中所有map文件，删除其引用关系
		for (String path : affectFiles) {
			if (path.toLowerCase().endsWith(".map")) {
				releaseMapRef(path);
			}
		}
		
		// 删除所有选中的目录和文件（目录整个删除，文件删除的同时需要删除附加文件，包括：ctn, cts.ref, lfi, ldf.ref, hkc, eqpc
		for (String t : target) {
			File f = new File(t);
			if (f.isFile()) {
				f.delete();
				if (t.toLowerCase().endsWith(".cts")) {
					Utils.replaceSuffix(f, "ctn").delete();
					Utils.replaceSuffix(f, "cts.ref").delete();
				} else if (t.toLowerCase().endsWith(".ldf")) {
					Utils.replaceSuffix(f, "lfi").delete();
					Utils.replaceSuffix(f, "ldf.ref").delete();
				} else if (t.toLowerCase().endsWith(".hk")) {
					Utils.replaceSuffix(f, "hkc").delete();
				} else if (t.toLowerCase().endsWith(".eqp")) {
					Utils.replaceSuffix(f, "eqpc").delete();
				}
			} else if (f.isDirectory()) {
				Utils.deleteDir(f);
			}
		}
	}
	
	/*
	 * 检查一个文件是否还被其他不在删除列表中的文件引用。
	 */
	private static void checkRef(String path, Set<String> excludeFiles) throws Exception {
		if (path.toLowerCase().endsWith(".pip")) {
			// pip文件可能被本目录下的cts文件引用
			File f = new File(path);
			File p = f.getParentFile();
			if (excludeFiles.contains(p.getAbsolutePath())) {
				// 如果本目录都在删除列表，不用检查了
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
						throw new Exception(path + "被" + f.getName() + "引用，不能删除。");
					}
				}
			}
		} else if (path.toLowerCase().endsWith("ldf")) {
			// 通过ref文件查找引用ldf的map
			String[] refMaps = ProjectParser.getFileRefList(path);
			File projDir = ProjectOwner.getProjectRootPath(path);
			if (projDir != null) {
				for (String refMap : refMaps) {
					File refMapFile = new File(projDir, refMap);
					if (!excludeFiles.contains(refMapFile.getAbsolutePath())) {
						throw new Exception(path + "被" + refMapFile.getName() + "引用，不能删除。");
					}
				}
			}
		} else if (path.toLowerCase().endsWith(".cts")) {
			// 通过ref文件查找引用cts的map
			String[] refMaps = ProjectParser.getFileRefList(path);
			File projDir = ProjectOwner.getProjectRootPath(path);
			if (projDir != null) {
				for (String refMap : refMaps) {
					File refMapFile = new File(projDir, refMap);
					if (!excludeFiles.contains(refMapFile.getAbsolutePath())) {
						throw new Exception(path + "被" + refMapFile.getName() + "引用，不能删除。");
					}
				}
			}
			
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
					PipParticleEffectSet pes = null;
					try {
						pes = new PipParticleEffectSet();
						pes.load(reff);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					if (pes.getAnimateFile().equals(f)) {
						throw new Exception(path + "被" + reff.getName() + "引用，不能删除。");
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
						throw new Exception(path + "被" + reff.getName() + "引用，不能删除。");
					}
				}
			}
		} else if (path.toLowerCase().endsWith(".hk")) {
			// hk文件可能被项目目录下的eqp文件引用（包括子目录）
			File f = new File(path);
			File p = f.getParentFile();
			File projDir = ProjectOwner.getProjectRootPath(path);
			if (projDir != null) {
				p = projDir;
			}
			if (excludeFiles.contains(p.getAbsolutePath())) {
				// 如果本目录都在删除列表，不用检查了
				return;
			}
			
			// 找出子目录中所有的eqp
			Set<String> childFiles = new HashSet<String>();
			Utils.findFilesInDir(p, ".eqp", childFiles);
			for (String reff : childFiles) {
				if (excludeFiles.contains(reff)) {
					continue;
				}
				
				// 读取eqp文件，检查是否引用了指定的hk文件
				String relatePath = Utils.getRelatePath(path, reff);
				try {
					if (EquipHookMap.checkHkRef(new File(reff), relatePath)) {
						throw new Exception(path + "被" + new File(reff).getName() + "引用，不能删除。");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * 删除一个map文件所引用的所有cts和ldf文件对此map文件的引用关系。
	 */
	private static void releaseMapRef(String path) throws Exception {
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
				ProjectParser.removeFileRef(refF.getAbsolutePath(), path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
