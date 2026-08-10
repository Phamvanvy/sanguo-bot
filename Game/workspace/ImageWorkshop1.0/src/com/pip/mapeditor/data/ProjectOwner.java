package com.pip.mapeditor.data;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.util.Utils;
import com.pipimage.image.PipAnimateSet;

/**
 * 此类用于管理项目属性,和文件映射
 * @author jhkang
 *
 */
public class ProjectOwner {
	/**
	 * Format :***\data\ 
	 */
	private String prjDataPath;
	/**
	 * @param absolutePath
	 * @return path to "data" directory, ends with "data\"
	 */
	public String getPrjDataPath() {
		return prjDataPath;
	}
	public void setPrjDataPath(String prjDataPath) {
		if(prjDataPath.endsWith(File.separator)==false){
			prjDataPath+=File.separator;
		}
		this.prjDataPath = prjDataPath;
	}
	/**
	 * map hashCode to file path<br/>
	 * Value string is file path relative to project data directory.<br/>
	 * eg. 668376643:pipLib\landform\0.ldf.<br/>
	 */
	private HashMap<Integer,String> fileMap = new HashMap<Integer, String>();
	
	private ProjectOwner(String prjDataPath){
		if(prjDataPath.endsWith(File.separator)==false){
			prjDataPath+=File.separator;
		}
		this.prjDataPath = prjDataPath;
	}
	
	private String libDirName = "pipLib";
	
	public String getLibDirName() {
		return libDirName;
	}
	public String fileMapGet(int hashCode) throws Exception{
		if (new File(prjDataPath).getName().equals("data") == false) {
			throw new Exception("当前项目路径不符合库模式的要求.不能进行地图相关的操作.");
		}
		if(fileMap.size()==0){
			refreshFileMap(this.prjDataPath+libDirName);
		}
		String s = fileMap.get(hashCode);
		if(s==null){
			String msg = "没有找到hashCode:"+hashCode+"对应的文件路径.\n" +
					"可能该文件不处于当前地图所在的项目的data目录(或其子目录)下.\n" +
					"或者文件映射没有刷新,请刷新项目浏览器的项目节点.";
			throw new Exception(msg);
		}
		return s;
	}
	public void refreshFileMap(String absolutePath, boolean clear) {
		if(clear){
			fileMap.clear();
		}
		refreshFileMap(absolutePath);
	}
	
	/**
	 * refresh without clear
	 * @param path
	 */
	public void refreshFileMap(String path){
		System.out.println("ProjectOwner.walkDirectory() refresh file map:");
		System.out.println("At:"+path);
		walkDirectory(path);
	}
	
	public void refreshRefs(String path) {
		clearRefs(path);
		rebuildRefs(path);
	}
	
	public static int getHashCode(String path) {
		path = path.replace('/', '\\');
		return path.hashCode();
	}
	
	/**
	 * 根据一个绝对路径，取得相对于项目的路径（如果是在项目里的话）。这里有一点问题，就是我们只能认定一个名为data的父目录
	 * 就是项目根目录，所以项目中的目录绝对不能取名叫做data。
	 * @param fullPath
	 * @return
	 */
	public static String getProjectRelatePath(String fullPath) {
		File f = new File(fullPath);
		String ret = "";
		while (f != null && !f.getName().equals("data")) {
			if (ret.length() == 0) {
				ret = f.getName();
			} else {
				ret = f.getName() + "\\" + ret;
			}
			f = f.getParentFile();
		}
		if (f == null) {
			return fullPath;
		} else {
			return ret;
		}
	}
	
	/**
	 * 根据一个绝对路径，取得项目的路径（如果是在项目里的话）。这里有一点问题，就是我们只能认定一个名为data的父目录
	 * 就是项目根目录，所以项目中的目录绝对不能取名叫做data。
	 * @param fullPath
	 * @return
	 */
	public static File getProjectRootPath(String fullPath) {
		File f = new File(fullPath);
		while (f != null && !f.getName().equals("data")) {
			f = f.getParentFile();
		}
		return f;
	}
	
	private void walkDirectory(String path){
		File f = new File(path);
		if(f.isFile()){
			String subPath = getProjectRelatePath(f.getAbsolutePath());
			fileMap.put(getHashCode(subPath), subPath);
		}else if(f.isDirectory()){
			String fileNames[] = f.list( new FilenameFilter(){
				public boolean accept(File dir, String name) {
					if(name.endsWith(".ctn") || name.endsWith("ldf") || new File(dir,name).isDirectory()){
						return true;
					}
					return false;
				}
				
			});
			for(int i=0; i<fileNames.length; i++){
				walkDirectory(f.getAbsolutePath()+"\\"+fileNames[i]);
			}
		}
	}
	
	/*
	 * 删除所有ref文件。
	 * @param path
	 */
	private void clearRefs(String path){
		File f = new File(path);
		if (f.isFile()) {
			if (f.getName().endsWith(".ref")) {
				f.delete();
			}
		}else if(f.isDirectory()){
			String fileNames[] = f.list( new FilenameFilter(){
				public boolean accept(File dir, String name) {
					if(name.endsWith(".ref") || new File(dir,name).isDirectory()){
						return true;
					}
					return false;
				}
				
			});
			for(int i=0; i<fileNames.length; i++){
				clearRefs(f.getAbsolutePath()+"\\"+fileNames[i]);
			}
		}
	}
	
	/*
	 * 遍历所有map文件，重建ref表。
	 * @param path
	 */
	private void rebuildRefs(String path){
		File f = new File(path);
		if (f.isFile()) {
			if (f.getName().endsWith(".map")) {
				try {
					List<String> refFiles = MapFile.getRefFiles(this, f);
					for (String refFile : refFiles) {
						refFile = refFile.replaceAll("\\.ctn$", "\\.cts");
						ProjectParser.addFileRef(new File(prjDataPath, refFile).getAbsolutePath(), f.getAbsolutePath());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else if(f.isDirectory()){
			String fileNames[] = f.list( new FilenameFilter(){
				public boolean accept(File dir, String name) {
					if(name.endsWith(".map") || new File(dir,name).isDirectory()){
						return true;
					}
					return false;
				}
				
			});
			for(int i=0; i<fileNames.length; i++){
				rebuildRefs(f.getAbsolutePath()+"\\"+fileNames[i]);
			}
		}
	}
	
	/**
	 * 美术资源的分组映射;File为pipLib文件夹<p>
	 * ArrayList里的String是对应于pipLib的文件夹名称
	 */
	private HashMap<File, ArrayList<String>> prjGroupMap = new HashMap<File, ArrayList<String>>();
	/**
	 * 当前使用的组名称;默认为"",表示没有使用任何一个组
	 */
	private String curGroupName = "";
	public void setCurGroupName(String curGroupName) {
		this.curGroupName = curGroupName;
		File groupDef = new File(prjDataPath+File.separator+libDirName, "groups.xml");
		Document doc = null;
		if(groupDef.exists()){
			try {
				doc =  Utils.loadDOM(groupDef);
				Element root = doc.getRootElement();
				root.getAttribute("curGroup").setValue(curGroupName);
				Utils.saveDOM(doc, groupDef);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}
	/**
	 * 检查一个文件夹是否为美术资源的组文件夹
	 * @param f
	 * @return
	 * @throws Exception 
	 */
	public boolean checkGroupDir(File f){
		return checkGroupDir(f, false);
	}
	/**
	 * 
	 * @param f
	 * @param checkMain 是否是检查当前使用的组和f一致
	 * @return
	 */
	public boolean checkGroupDir(File f,boolean checkMain){
		if(prjGroupMap.containsKey(f.getParentFile())){
			if(checkMain){
				return this.curGroupName.equals(f.getName());
			}
			return prjGroupMap.get(f.getParentFile()).contains(f.getName());
		}else if(f.getParentFile().getName().equals("pipLib")){
			try {
				loadGroupDef(f.getParentFile());
				if(checkMain){
					return this.curGroupName.equals(f.getName());
				}
				return prjGroupMap.get(f.getParentFile()).contains(f.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * 加载组文件信息,如果没有分组文件.则新建一个
	 * @param parentFile -> pipLib directory
	 * @throws Exception 
	 */
	private void loadGroupDef(File parentFile) throws Exception {
		File groupDef = new File(parentFile, "groups.xml");
		Document doc = null;
		if(groupDef.exists()){
			doc =  Utils.loadDOM(groupDef);
		}else{
			doc = new Document(new Element("groups"));
			doc.getRootElement().addAttribute("curGroup", "");
			Utils.saveDOM(doc, groupDef);
		}
		Element root = doc.getRootElement();
		curGroupName = root.getAttributeValue("curGroup");
		ArrayList<String> dirNames = new ArrayList<String>();
		for(Element entry:(List<Element>)root.getChildren()){
			dirNames.add(entry.getAttributeValue("dirName"));
		}
		prjGroupMap.put(parentFile, dirNames);
	}
	/**
	 * 
	 * @param curDir 被设置为组文件夹的file
	 * @throws Exception 
	 */
	public void addGroupDir(File curDir) throws Exception {
		if(prjGroupMap.containsKey(curDir.getParentFile())==false){
			loadGroupDef(curDir.getParentFile());
		}
		prjGroupMap.get(curDir.getParentFile()).add(curDir.getName());
		saveGroupDef(curDir.getParentFile(), prjGroupMap.get(curDir.getParentFile()));
	}
	public void removeGroupDir(File f) throws Exception {
		prjGroupMap.get(f.getParentFile()).remove(f.getName());
		saveGroupDef(f.getParentFile(), prjGroupMap.get(f.getParentFile()));
	}
	/**
	 * 
	 * @param libFile
	 * @param arrayList
	 * @throws Exception 
	 */
	private void saveGroupDef(File libFile, ArrayList<String> arrayList) throws Exception {
		File groupDef = new File(libFile, "groups.xml");
		Document doc = null;
		if(groupDef.exists()){
			try {
				doc =  Utils.loadDOM(groupDef);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("解析组文件时发生错误",e);
			}
			Element newRoot = (Element) doc.getRootElement().clone();
			newRoot.getMixedContent().clear();
			for(String dirName:arrayList){
				Element entry = new Element("group");
				entry.addAttribute("dirName", dirName);
				newRoot.getMixedContent().add(entry);
			}
			doc.setRootElement(newRoot);
			try {
				Utils.saveDOM(doc, groupDef);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("保存组文件时发生错误",e);
			}
		}else{
			throw new Exception("没有找到分组信息文件\n"+groupDef.getAbsolutePath());
		}		
	}
	
	public int regFile(String absolutePath) {
		String subPath = getProjectRelatePath(absolutePath);
		if (subPath.toLowerCase().endsWith(".cts")) {
			subPath = Utils.replaceSuffix(subPath, "ctn");
		}
		int ret = getHashCode(subPath);
		if (fileMap.size() != 0) {
			// size为0表示还没有刷新的项目，不要加入导致不能刷新
			fileMap.put(ret, subPath);
		}
		return ret;
	}
	
	/**
	 * 移除文件引用
	 * @param hashCode, 被引用文件标识
	 * @param sourceFilePath 引用文件绝对路径
	 * @throws Exception 
	 */
	public void removeAnimateRef(int hashCode, String sourceFilePath) throws Exception {
		String prjDir = this.prjDataPath;
		String subPath = fileMapGet(hashCode);
		if(subPath.endsWith(".ctn")){
			subPath = subPath.replace(".ctn",".cts");
		}
		String refFilePath = prjDir + subPath;
		File refToF = new File(refFilePath);
		File refXml = new File(refToF.getParent(), refToF.getName()+".ref");
		Document doc;
		if(refXml.exists()){
			doc = Utils.loadDOM(refXml);
		}else{
			doc = new Document(new Element("ref"));
		}
		sourceFilePath = sourceFilePath.replace(prjDir, "");
		List mixedContent = new ArrayList();
		for(Element item:(List<Element>)doc.getRootElement().getChildren()){
			if(item.getAttributeValue("file").equals(sourceFilePath)){
				doc.getRootElement().getMixedContent().remove(item);
			}else{
				mixedContent.add(item);
			}
		}
		doc.getRootElement().setMixedContent(mixedContent);
		Utils.saveDOM(doc, refXml);
	}
	
	/**
	 * key : prjDataPath, ends with File.separator
	 */
	private static HashMap<String, ProjectOwner> pool = new HashMap<String, ProjectOwner>();
	
	/**
	 * 
	 * @param prjDataPath2, ends with File.separator
	 * @return
	 */
	public static ProjectOwner find(String prjDataPath2, boolean create) {
		if(!prjDataPath2.endsWith(File.separator)){
			prjDataPath2 += File.separator;
		}
		if(pool.containsKey(prjDataPath2)){
			return pool.get(prjDataPath2);
		}
		if (create) {
			ProjectOwner p = new ProjectOwner(prjDataPath2);
			pool.put(prjDataPath2, p);
			return p;
		}
		return null;
	}
	
	public List<String> getGroupDirNames() {
		//由于后来改造成一个项目一个map了.所以可以直接拿第一个
		if(prjGroupMap.size()>0){
			return prjGroupMap.values().iterator().next();
		}else{
			return null;
		}
	}
	/**
	 * 获取可切换的分组名称。
	 * @param libFile pipLib 文件夹
	 * @param filter 是否过滤项目当前使用的分组
	 * @return
	 * @throws Exception
	 */
	public List<String> getSwitchGroupNames(File libFile, boolean filter) throws Exception {
		if(prjGroupMap.containsKey(libFile)==false){
			loadGroupDef(libFile);
		}
		ArrayList<String> dirNames = prjGroupMap.get(libFile);
		ArrayList<String> ret = (ArrayList<String>) dirNames.clone();
		if(filter)
		ret.remove(curGroupName);
		return ret;
	}
	public String getCurGroupName() {
		return curGroupName;
	}
	//how to roll back if encountered exception?-->preverify?
	public void replaceHashCodeRef(File srcFile, File destFile) throws Exception {
		replaceHashCodeRef(srcFile, destFile, true);
		replaceHashCodeRef(srcFile, destFile, false);
	}
	private void refreshOpenedEditor(File srcFile, boolean needOpen) throws Exception{
//		// 检查是否已经打开编辑器，如果已经打开则激活，否则打开
//        IEditorPart editor = null;
//		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((srcFile .getAbsolutePath())));
//        FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
//        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//        editor = page.findEditor(input);
//        if (editor != null) {
//        	page.closeEditor(editor, false);
//        	if(needOpen){
//        		page.openEditor(input, MapEditor.ID);
//        	}
//        } 
	}
	/**
	 * 根据资源文件切换地图分组。 考虑另外一种模式:指定地图然后进行切换.<p>
	 * 切换地图对分组的引用。原理：遍历当前使用的分组，查找CTS和LDF文件，<p>
	 * 根据他们的被引用记录,找到引用他们的地图,将地图中对应的hashCode值替换掉.<p>
	 * 文件路径在组文件夹那一层做替换,其余部分完全一样.
	 * @param srcFile
	 * @param destFile
	 * @param isVerify 是否是在做验证.验证时只检查当前组文件夹下的文件是否在要切换到的组文件夹下有对应的文件.
	 * @throws Exception
	 */
	private void replaceHashCodeRef(File srcFile, File destFile, boolean isVerify) throws Exception {
		if(srcFile.isFile()){
			if(isVerify){
				if(destFile.exists()){
					return;
				}else{
					throw new Exception("缺失组文件\n"+destFile.getAbsolutePath().replace(prjDataPath, ""));
				}
			}
			String[] mapNames = ProjectParser.getFileRefList(srcFile.getAbsolutePath());
			int srcHashCode = getHashCode(srcFile.getAbsolutePath().replace(prjDataPath, ""));
			int destHashCode = getHashCode(destFile.getAbsolutePath().replace(prjDataPath, ""));
			for(String name:mapNames){
				MapFile mapFile = new MapFile();
				File mf = new File(prjDataPath,name);
				//将已经打开的此文件的编辑器关闭
				refreshOpenedEditor(mf,false);
				mapFile.load(mf);
				if(mapFile.isLibMode==false){
					continue;
				}
				for(TileSet ts:mapFile.getLandforms()){
					if(ts.hashCode==srcHashCode){
						ts.hashCode = destHashCode;
						ProjectParser.addFileRef(destFile.getAbsolutePath(), mf.getAbsolutePath());
					}
				}
				if(mapFile.animateList!=null){
					for(PipAnimateSet pas:mapFile.animateList){
						if(pas.hashCode==srcHashCode){
							pas.hashCode = destHashCode;
							ProjectParser.addFileRef(destFile.getAbsolutePath(), mf.getAbsolutePath());
						}
					}
					for(GameMap gm:mapFile.getMaps()){
						for(IMapLayer iml:gm.layers){
							if(iml instanceof MapNPCLayer){
								MapNPCLayer mnl = (MapNPCLayer)iml;
								for (MapNPC npc : mnl.getNpcs()) {
									if(npc.animateSetRef==srcHashCode){
										npc.animateSetRef = destHashCode;
									}
								}
							}
						}
					}
					for(NPCImageInfo info:mapFile.getNPCs().values()){
						if(info.animateRef==srcHashCode){
							info.animateRef = destHashCode;
						}
					}
				}
				mapFile.save(mf);
			}
		}else{
			//list cts , landform only?
			File children[] = srcFile.listFiles();
			for(File child:children){
				String name = child.getName().toLowerCase();
				if(child.isDirectory() || name.endsWith("cts") || name.endsWith("ldf")){
					File childDest = new File(destFile, child.getName());
					replaceHashCodeRef(child, childDest, isVerify);
				}
			}
		}
	}
	public static void main(String[] args){
		File f = new File("E:\\workspace\\Sanguo-Editor1.0\\data\\Areas");
		checkNPCImageInfoIndex(f);
	}
	public static void checkNPCImageInfoIndex(File srcFile){
		if(srcFile.isFile()){
			MapFile mapFile = new MapFile();
			try{
				mapFile.load(srcFile);
			}catch(Exception e){
				System.out.println(e.getMessage()+":"+srcFile.getAbsolutePath());
			}
		}else{
			File children[] = srcFile.listFiles();
			for(File child:children){
				String name = child.getName().toLowerCase();
				if(child.isDirectory() || name.endsWith(".map")){
					checkNPCImageInfoIndex(child);
				}
			}
		}
	}
	/**
	 * 单个地图进行组切换的工具
	 * @param curHashCode
	 * @param curGroupTag
	 * @param targetGroupTag
	 * @return
	 * @throws Exception 
	 */
	private int checkAndFindReplaceHashCode(int curHashCode, String curGroupTag, String targetGroupTag) throws Exception{
		String fileName;
		fileName = fileMapGet(curHashCode);
		int ret = 0;
		if(fileName.startsWith(curGroupTag)){
			fileName = fileName.replace(curGroupTag, targetGroupTag);
			ret = getHashCode(fileName);
			if(fileMap.containsKey(ret)==false){
				throw new Exception("组文件缺失：\n"+fileName);
			}
		}
		return ret;
	}
	/**
	 * 切换单个地图
	 * @param selectedMapFile
	 * @throws Exception 
	 */
	public void replaceHashCodeRef(File selectedMapFile,File curGroupDir,File toGroupDir) throws Exception {
		MapFile mapFile = new MapFile();
		mapFile.load(selectedMapFile);
		String curGroup = mapGroupCache.get(selectedMapFile.getAbsolutePath());
		if(mapFile.isLibMode==false || curGroup==null || curGroup.equals("")){
			return;
		}
		String curGroupTag = this.libDirName+File.separator+curGroup;
		String targetGroupTag = this.libDirName+File.separator+toGroupDir.getName();
		int targetHashCode;
		for(TileSet ts:mapFile.getLandforms()){
			targetHashCode = checkAndFindReplaceHashCode(ts.hashCode, curGroupTag, targetGroupTag);
			if(targetHashCode!=0){		
				ts.hashCode = targetHashCode;
			}
		}
		if(mapFile.animateList!=null){
			for(PipAnimateSet pas:mapFile.animateList){
				targetHashCode = checkAndFindReplaceHashCode(pas.hashCode, curGroupTag, targetGroupTag);
				if(targetHashCode!=0){		
					pas.hashCode = targetHashCode;
				}
			}
			for(GameMap gm:mapFile.getMaps()){
				for(IMapLayer iml:gm.layers){
					if(iml instanceof MapNPCLayer){
						MapNPCLayer mnl = (MapNPCLayer)iml;
						for (MapNPC npc : mnl.getNpcs()) {
							targetHashCode = checkAndFindReplaceHashCode(npc.animateSetRef, curGroupTag, targetGroupTag);
							if(targetHashCode!=0){		
								npc.animateSetRef = targetHashCode;
							}
						}
					}
				}
			}
			for(NPCImageInfo info:mapFile.getNPCs().values()){
				targetHashCode = checkAndFindReplaceHashCode(info.animateRef, curGroupTag, targetGroupTag);
				if(targetHashCode!=0){		
					info.animateRef = targetHashCode;
				}
			}
		}
		mapFile.curGroup = toGroupDir.getName();
		mapFile.save(selectedMapFile);
		//更新缓存
		mapGroupCache.put(selectedMapFile.getAbsolutePath(), toGroupDir.getName());
		refreshOpenedEditor(selectedMapFile, true);
	}
	
	/**
	 * 缓存地图文件名和该地图当前分组名称
	 */
	private HashMap<String, String> mapGroupCache;
	
	public String getMapCurGroup(File selectedFile) throws Exception {
		if(mapGroupCache == null){
			mapGroupCache = new HashMap<String, String>();
		}
		if(mapGroupCache.containsKey(selectedFile.getAbsolutePath())){
			return mapGroupCache.get(selectedFile.getAbsolutePath());
		}else{
			Document doc = null;
			try {
				doc = Utils.loadDOM(selectedFile);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("获取地图当前分组时出现错误:\n"+e);
			}
			String ret = doc.getRootElement().getAttributeValue("curGroup") ;
			mapGroupCache.put(selectedFile.getAbsolutePath(), ret);
			return ret;
		}
	}
	public void addGroupCache(String sourceFilePath, String curGroup) {
		mapGroupCache.put(sourceFilePath, curGroup);
	}
	public static void disposeAll() {
		for(ProjectOwner owner:pool.values()){
			owner.dispose();
		}
		pool.clear();
	}
	private void dispose() {
		if(mapGroupCache!=null)
		this.mapGroupCache.clear();
		if(prjGroupMap!=null)
		this.prjGroupMap.clear();
		if(fileMap!=null)
		this.fileMap.clear();
	}
}

