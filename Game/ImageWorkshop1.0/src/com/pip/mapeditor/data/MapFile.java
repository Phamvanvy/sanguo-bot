package com.pip.mapeditor.data;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.jdom.*;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.RepeatAddException;
import com.pip.util.AnimateSetOperator;
import com.pip.util.EFSUtil;
import com.pip.util.Point;
import com.pip.util.Utils;
import com.pipimage.image.ColorsExceedException;
import com.pipimage.image.JPEGMergeOption;
import com.pipimage.image.LandformImage;
import com.pipimage.image.MergeAreaTest;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * 地图文件。地图文件采用XML格式，内容包括：地图信息、地图NPC图片、关联Tile图片、关联动画。
 * @author lighthu
 */
public class MapFile {
    private ProjectOwner projectOwner;
	/** 坐标格点大小 */
    private int cellSize = 8;
 
    /** 精确地图贴图宽度 */
    private int tileWidth = 16;
    /** 精确地图贴图高度 */
    private int tileHeight = 8;
    /** 模糊地图贴图宽度 */
    private int blurTileWidth = 16;
    /** 模糊地图贴图高度 */
    private int blurTileHeight = 16;
    
	/** 地图 */
	private ArrayList<GameMap> maps;
	/** 模糊地图地形贴图 */
	private ArrayList<TileSet> landforms;
	/** 精确地图贴图 */
	private TileSet tileImage;
	/** 动画集合（包括地图NPC也作为动画实现） <br/>
	 * 为了统一,库模式下,所有动画都会被拷贝进去
	 * */
	private PipAnimateSet animates;
	/** NPC图片扩展描述信息.<br/>
	 * KEY,默认是NPC图片在动画集合中的顺序号，VALUE是NPC扩展信息<br/>
	 * 在编辑器改造后使用库模式时,key是animateRef<<32|index<br/>
	 * animateRef->引用的动画的hashCode值; index,该动画中的下标
	 *  */
	private HashMap<Long, NPCImageInfo> npcInfo = new HashMap<Long, NPCImageInfo>();
	/**
	 * 库模式动画集合（包括地图NPC也作为动画实现）
	 */
	public List<PipAnimateSet> animateList;
	/**
	 * 参考调色板信息。
	 */
	public List<int[]> refPalettes;
	
	/**
	 * 创建一个缺省大小的地图文件。
	 */
	public MapFile() {
		maps = new ArrayList<GameMap>();
		landforms = new ArrayList<TileSet>();
		tileImage = new TileSet(false);
		animates = new PipAnimateSet();
		npcInfo = new HashMap<Long, NPCImageInfo>();
		refPalettes = new ArrayList<int[]>();
	}
	
	/**
	 * Used to trace project data directory path, ends with File.separator
	 */
	private String prjDataPath;
	private String sourceFilePath;
	
	public ProjectOwner getProjectOwner() {
		return projectOwner;
	}
	
	public String getProjectPath() {
		return prjDataPath;
	}
	
	public String getSourceFilePath() {
		return sourceFilePath;
	}
	
	public void load(File file) throws Exception{
		load(file, true);
	}
	/**
	 * 从文件中载入。
	 */
	public void load(File file, boolean loadAniSet) throws Exception {
		sourceFilePath = file.getAbsolutePath();
		try {
			prjDataPath = ProjectOwner.getProjectRootPath(file.getAbsolutePath()).getAbsolutePath() + File.separator;
			projectOwner = ProjectOwner.find(prjDataPath, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		file = new File(sourceFilePath.replace("\\", "/"));
	    Document doc = Utils.loadDOM(file);
	    parseXML(doc, loadAniSet);
	}

	/**
	 * 从输入流中载入。
	 */
	public void load(InputStream is) throws Exception {
	    Document doc = Utils.loadDOM(is);
	    parseXML(doc, true);
	}
	
	// 解析XML格式的地图文件
	private void parseXML(Document doc, boolean loadAniSet) throws Exception {
        maps = new ArrayList<GameMap>();
        landforms = new ArrayList<TileSet>();
        tileImage = new TileSet(false);
        animates = new PipAnimateSet();
        npcInfo = new HashMap<Long, NPCImageInfo>();

        Element root = doc.getRootElement();
	    
	    // 基本信息
        isLibMode = root.getAttribute("libmode")!=null;
        if(isLibMode){
        	curGroup = root.getAttributeValue("curGroup") ; 
        	if(curGroup==null){
        		curGroup = "";
        	}
        }
        try {
            cellSize = Integer.parseInt(root.getAttributeValue("cellsize"));
        } catch (Exception e) {
        }
	    tileWidth = Integer.parseInt(root.getAttributeValue("tilewidth"));
        tileHeight = Integer.parseInt(root.getAttributeValue("tileheight"));
        blurTileWidth = Integer.parseInt(root.getAttributeValue("blurtilewidth"));
        blurTileHeight = Integer.parseInt(root.getAttributeValue("blurtileheight"));
	    
	    // 载入地图
	    List mapList = root.getChildren("map");
	    for (int i = 0; i < mapList.size(); i++) {
	        Element elem = (Element)mapList.get(i);
	        GameMap newMap = new GameMap(this, 0, 0);
	        newMap.load(elem);
	        maps.add(newMap);
	    }
	    
	    // 载入地形文件
	    List landformList = root.getChildren("landform");
	    int size = landformList.size();
	    for (int i = 0; i < size; i++) {
	    	Element elem = (Element)landformList.get(i);
	    	if(isLibMode){
	    		if(loadAniSet){
		    		int hashCode = Integer.parseInt(elem.getAttributeValue("hashCode"));
					loadLandFormByHashCode(hashCode);
	    		}
	    	}else{
		        TileSet newLandform = new TileSet(true);
		        newLandform.load(elem);
		        landforms.add(newLandform);
	    	}
	    }
	    
	    // 载入贴图文件
	    Element tileImageElem = root.getChild("tileimage");
	    tileImage = new TileSet(false);
	    tileImage.load(tileImageElem);
	    
	    // 载入动画文件
	    Element animateElem = root.getChild("animates");
	    List animateImages = animateElem.getChildren("image");
	    if(isLibMode){
	    	if(loadAniSet){
	    		loadLibModeAnimates(root);
	    	}
	    }else{
		    animates = new PipAnimateSet();
		    for (int i = 0; i < animateImages.size(); i++) {
		        Element elem = (Element)animateImages.get(i);
		        String name = elem.getAttributeValue("filename");
		        PipImage image = new PipImage();
		        byte[] data = Base64.decode(elem.getTextTrim());
		        image.load(new ByteArrayInputStream(data));
		        animates.addSourceFile(name, image);
		    }
		    Element ctsElem = animateElem.getChild("cts");
		    byte[] data = Base64.decode(ctsElem.getTextTrim());
		    animates.loadCTSFile(new DataInputStream(new ByteArrayInputStream(data)));
	    }
	    
	    // 载入NPC扩展描述信息
        Element npcInfoElem;
        if(isLibMode){
        	//loadLibModeAnimates do load npc info from each file
        }else{
        	npcInfoElem = root.getChild("npcinfo");
        	loadNpcInfo(npcInfoElem, 0);
        }
        
        // 载入参考调色板信息
        refPalettes.clear();
        List paletteList = root.getChildren("palette");
	    for (int i = 0; i < paletteList.size(); i++) {
	    	Element elem = (Element)paletteList.get(i);
	    	String[] secs = elem.getTextTrim().split(" ");
	    	int[] pal = new int[secs.length / 4];
	    	for (int j = 0; j < pal.length; j++) {
	    		int i1 = Integer.parseInt(secs[j * 4], 16);
	    		int i2 = Integer.parseInt(secs[j * 4 + 1], 16);
	    		int i3 = Integer.parseInt(secs[j * 4 + 2], 16);
	    		int i4 = Integer.parseInt(secs[j * 4 + 3], 16);
	    		pal[j] = (i1 << 24) | (i2 << 16) | (i3 << 8) | i4;
	    	}
	    	refPalettes.add(pal);
	    }
        
	}
	private void loadNpcInfo(Element npcInfoElem, int libHashCode){
		List npcList = npcInfoElem.getChildren("npc");
        for (int i = 0; i < npcList.size(); i++) {
            Element elem = (Element)npcList.get(i);
            int index = Integer.parseInt(elem.getAttributeValue("index"));
            NPCImageInfo info = new NPCImageInfo();
            long key = index;
            if(isLibMode){
            	info.animateRef = libHashCode;
            	key = info.animateRef;
            	key <<= 32;
            	key |= index;
            }
            info.cx = Utils.stringToIntArray(elem.getAttributeValue("cx"), ',');
            info.cy = Utils.stringToIntArray(elem.getAttributeValue("cy"), ',');
            info.cw = Utils.stringToIntArray(elem.getAttributeValue("cw"), ',');
            info.ch = Utils.stringToIntArray(elem.getAttributeValue("ch"), ',');
            npcInfo.put(key, info);
        }
	}
	private void loadLandFormByHashCode(int hashCode) throws Exception {
		PipImage lfImage = new LandformImage();
		String refFile = projectOwner.fileMapGet(hashCode);
		if(refFile==null){
			throw new Exception("引用的文件映射不存在");
		}
		String fileName = prjDataPath+projectOwner.fileMapGet(hashCode);
		lfImage.load(fileName );
		TileSet newLandform = new TileSet(lfImage);
		newLandform.hashCode = hashCode;
		fileName = fileName.replaceAll("\\.ldf$", "\\.lfi");
		File lfiFile = new File(fileName);
		if(lfiFile.exists()){
			Element tileInfoDocRoot = Utils.loadDOM(lfiFile).getRootElement();
			newLandform.loadTileInfo(tileInfoDocRoot);
		}
		landforms.add(newLandform);		
	}
	
	/**
	 * 查找一个地图文件引用的所有其他文件。
	 * @param srcFile
	 * @return
	 * @throws Exception
	 */
	public static List<String> getRefFiles(ProjectOwner project, File srcFile) throws Exception {
		Document doc = Utils.loadDOM(srcFile);
		Element root = doc.getRootElement();
		boolean isLibMode = root.getAttribute("libmode") != null;
		List<String> retList = new ArrayList<String>();
		if (!isLibMode) {
			return retList;
		}
		List landformList = root.getChildren("landform");
	    int size = landformList.size();
	    for (int i = 0; i < size; i++) {
	    	Element elem = (Element)landformList.get(i);
	    	int hashCode = Integer.parseInt(elem.getAttributeValue("hashCode"));
	    	String refFile = project.fileMapGet(hashCode);
	    	if (refFile == null) {
	    		throw new IOException("引用文件不存在：" + elem.getAttributeValue("file"));
	    	}
	    	retList.add(refFile);
	    }
	    Element aniElem = root.getChild("animates");
	    List ctsList = aniElem.getChildren("cts");
	    size = ctsList.size();
	    for (int i = 0; i < size; i++) {
	    	Element elem = (Element)ctsList.get(i);
	    	int hashCode = Integer.parseInt(elem.getAttributeValue("hashCode"));
	    	String refFile = project.fileMapGet(hashCode);
	    	if (refFile == null) {
	    		throw new IOException("引用文件不存在：" + elem.getAttributeValue("file"));
	    	}
	    	retList.add(refFile);
	    }
	    return retList;
	}
	
	/**
	 * 注意使用了LinkedHashMap来保证entry的顺序,因为在做删除时用的是传回来的索引
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Integer> makeLandformRefTimes() throws Exception{
		HashMap<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
		BlurMapLayer blurLayer;
		for(TileSet ts:landforms){
			map.put(ts.hashCode, 0);
		}
		for(GameMap gm:maps){
			for(IMapLayer iml:gm.layers){
				if(iml instanceof BlurMapLayer){
					blurLayer = (BlurMapLayer) iml;
					int baseIdx = blurLayer.getBaseLandform();
					int baseHashCode = 0;
					if(baseIdx>=0){
						baseHashCode = landforms.get(baseIdx).hashCode; 
					}
					for(byte[] line:blurLayer.getLayerData()){
						for(byte bIdx:line){
							if(bIdx == -1){
								if(baseIdx>=0){
									map.put(baseHashCode, map.get(baseHashCode)+1);
								}
							}else{
								int hashCode = landforms.get(bIdx).hashCode;
								map.put(hashCode, map.get(hashCode)+1);
							}
						}
					}
				}
			}
		}
		HashMap<String, Integer> ret = new LinkedHashMap<String, Integer>();
		for(Integer hashCode:map.keySet()){
			ret.put(projectOwner.fileMapGet(hashCode), map.get(hashCode));
		}
		return ret;
	}
	
	/**
	 * 注意使用了LinkedHashMap来保证entry的顺序,因为在做删除时用的是传回来的索引
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Integer> makeResRefTimes() throws Exception{
		HashMap<Integer, Integer> refCounter = new HashMap<Integer, Integer>();
		for(PipAnimateSet pas:animateList){
			refCounter.put(pas.hashCode, 0);
		}
		for(GameMap gm:maps){
			for(IMapLayer iml:gm.layers){
				if(iml instanceof MapNPCLayer){
					MapNPCLayer mnl = (MapNPCLayer)iml;
					for (MapNPC npc : mnl.getNpcs()) {
						if(npc instanceof MultiAnimNPC){
							MultiAnimNPC mnpc = (MultiAnimNPC) npc;
							for(MapNPC ncc:mnpc.getChildren()){
								refCounter.put(ncc.animateSetRef,	refCounter.get(ncc.animateSetRef).intValue()+1);
							}
						}else{
							refCounter.put(npc.animateSetRef,	refCounter.get(npc.animateSetRef).intValue()+1);
						}
					}
				}
			}
		}
		HashMap<String, Integer> ret = new LinkedHashMap<String, Integer>();
		for(PipAnimateSet pas:animateList){
			String resName = projectOwner.fileMapGet(pas.hashCode); 
			if(resName.endsWith(".ctn")){
				resName = resName.replace(".ctn",".cts");
			}
			ret.put(resName, refCounter.get(pas.hashCode));
		}
		return ret;
	}
	
	public boolean isLibMode = false;
	/**
	 * 当前地图所用的分组,libMode下有效。XML文件中的根节点的curGroup属性保存该值.<p>
	 * 加载地图时加载，如果XML无此属性，则赋值为空字符串
	 */
	String curGroup = "";
	
	private void loadLibModeAnimates(Element root) throws Exception {
		animateList = new ArrayList<PipAnimateSet>();
		Element animateElem = root.getChild("animates");
	    List ctsList = animateElem.getChildren("cts");
	    int size = ctsList.size();
	    for (int i = 0; i < size; i++) {
	        Element elem = (Element)ctsList.get(i);
	        int hashCode = elem.getAttribute("hashCode").getIntValue();
	        String ctsFilePath = null;
	        try{
	        	ctsFilePath = prjDataPath+projectOwner.fileMapGet(hashCode).replaceAll("\\.ctn$", "\\.cts");
	        }catch(Exception e){
	        	String fileRefName = elem.getAttributeValue("file");
				throw new Exception("Miss file:"+fileRefName+"\nCause:"+e.getMessage(), e);
	        }
	        // load collision data
//	        String colName = ctsFilePath.replaceAll("\\.cts$", "\\.col");
//    		File colFile = new File(colName);
//    		if(colFile.exists()){
//    			Document doc = Utils.loadDOM(colFile);
//    			loadNpcInfo(doc.getRootElement(), hashCode);
//    		}
    		//load collision data end
	        PipAnimateSet animate = new PipAnimateSet();
	        animate.hashCode = hashCode;
	        File file = new File(ctsFilePath);
			animate.load(file);
			animateList.add(animate);
			// init animates
			if(animates==null){
				animates = animate;
				continue;
			}
			//copy all animate to animates
			int count = animate.getAnimateCount();
			for(int j=0; j<count; j++){
				animates.addAnimate(animate.getAnimate(j));
			}
	    }
	    //mock code
//	    animates = animateList.get(0); 
	}
	/**
	 * 取得当前选中的npc对应的animate的hashCode和实际下标
	 * @param selIdx
	 * @return [hashCode, realIdx]
	 */
	public int[] getNpcAnimateRef(int selIdx){
		int curIdx = 0;
		for(PipAnimateSet pa:animateList){
			curIdx += pa.getAnimateCount();
			if(curIdx>selIdx){
				int realIdx = selIdx - (curIdx - pa.getAnimateCount());
				int hashCode = pa.hashCode;
				return new int[]{hashCode, realIdx};
			}
		}
		return null;
	}
	/**
	 * 
	 * @param setRefIdx PipAnimateSet's hash code(recorded at xml element, not java object's hashcode)
	 * @param animIdx
	 * @return
	 */
	public PipAnimate getAnimate(int setRefIdx, int animIdx){
		if(animateList!=null)
		for(PipAnimateSet pa:animateList){
			if(pa.hashCode == setRefIdx){
				if( animIdx >= pa.getAnimateCount()){
					animIdx = 0;
				}
				return pa.getAnimate(animIdx);
			}
		}
		return null;
	}
	/**
	 * 根据hashCode获得对应的PipAnimateSet
	 * @param hashCode
	 * @return
	 */
	public PipAnimateSet getAnimateSet(int hashCode){
		for(PipAnimateSet pa:animateList){
			if(pa.hashCode == hashCode){
				return pa;
			}
		}
		return null;
	}
	/**
	 * 保存到文件中。
	 */
	public void save(File f) throws Exception {
	    // 限制：不能超过7种地形，每种地形不能超过31个TILE
	    if (this.landforms.size() > 7) {
	        throw new Exception("这个版本不支持一个地图文件超过7种地形。");
	    }
	    for (TileSet ts : landforms) {
	        if (ts.tileInfo.size() > 31) {
	            throw new Exception("这个版本不支持超过31个TILE的地形。");
	        }
	    }
	    
	    Element root = new Element("mapfile");
	    saveXML(root,true);
	    Document doc = new Document(root);
	    File tmpFile = File.createTempFile("med", ".map");
	    Utils.saveDOM(doc, tmpFile);
	    EFSUtil.copyFile(tmpFile, f);
	    tmpFile.delete();
	}
	
	/**
	 * 保存到输出流中。
	 */
	public void save(OutputStream os) throws Exception {
		Element root = new Element("mapfile");
		saveXML(root,false);
        Document doc = new Document(root);
        Utils.saveDOM(doc, os);
	}
	
	/**
	 * 保存为byte数组。
	 * @return
	 * @throws Exception
	 */
	public byte[] toByteArray() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        save(bos);
        return bos.toByteArray();
    }


	// 把地图文件保存到XML文档中。
    private void saveXML(Element root,boolean toFile) throws Exception {
        // 基本信息
        root.addAttribute("cellsize", String.valueOf(cellSize));
        root.addAttribute("tilewidth", String.valueOf(tileWidth));
        root.addAttribute("tileheight", String.valueOf(tileHeight));
        root.addAttribute("blurtilewidth", String.valueOf(blurTileWidth));
        root.addAttribute("blurtileheight", String.valueOf(blurTileHeight));
        if(isLibMode){
        	root.addAttribute("libmode","true");
        	root.addAttribute("curGroup", curGroup);
        	if(toFile){
        		fixMissMath();
        	}
        }

        // 保存地图
        for (GameMap map : maps) {
            root.getMixedContent().add(map.save());
        }
        
        // 保存地形文件
        for (TileSet landform : landforms) {
            Element elem = new Element("landform");
            if(isLibMode){
            	elem.addAttribute("hashCode", landform.hashCode+"");
            	elem.addAttribute("file", projectOwner.fileMapGet(landform.hashCode));
            	if(toFile){
	            	String fileName = prjDataPath+projectOwner.fileMapGet(landform.hashCode);
	        		fileName = fileName.replaceAll("\\.ldf$", "\\.lfi");
	        		landform.saveTileInfo(new File(fileName));
            	}
            }else{
	            landform.save(elem);
            }
            root.getMixedContent().add(elem);
        }
        
        // 保存贴图文件
        Element elem = new Element("tileimage");
        tileImage.save(elem);
        root.getMixedContent().add(elem);
        
        // 保存动画文件
        Element animateElem = new Element("animates");
        root.getMixedContent().add(animateElem);
        if(isLibMode){
        	makeLibModeAnimates(animateElem);
        }else{
	        for (int i = 0; i < animates.getFileCount(); i++) {
	            String fileName = animates.getFileName(i);
	            PipImage fileImage = animates.getSourceImage(i);
	            elem = new Element("image");
	            elem.addAttribute("filename", fileName);
	            elem.setText(imageToText(fileImage));
	            animateElem.getMixedContent().add(elem);
	        }
	        Element ctsElem = new Element("cts");
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        animates.save(bos, true);
	        ctsElem.setText(Base64.encode(bos.toByteArray()));
	        animateElem.getMixedContent().add(ctsElem);
        }
        
        // 保存NPC扩展描述信息
        Element npcInfoElem = new Element("npcinfo");
//        HashMap<Integer, List<Element>> hashCode2NpcInfos = null;
        if(isLibMode && toFile){//非库模式时加入npcinfo节点。库模式下，将此节点分离到独立文件
        	//地图碰撞不再使用NPC碰撞信息
//        	hashCode2NpcInfos = new HashMap<Integer, List<Element>>();
        }else{
        	root.getMixedContent().add(npcInfoElem);
	        Iterator<Long> itor = npcInfo.keySet().iterator();
	        while (itor.hasNext()) {
	            long index = itor.next();
	            NPCImageInfo info = npcInfo.get(index);
	            elem = new Element("npc");
	            elem.addAttribute("index", String.valueOf((int)(index&0xFFFFFFFF)));
	            elem.addAttribute("cx", Utils.intArrayToString(info.cx, ','));
	            elem.addAttribute("cy", Utils.intArrayToString(info.cy, ','));
	            elem.addAttribute("cw", Utils.intArrayToString(info.cw, ','));
	            elem.addAttribute("ch", Utils.intArrayToString(info.ch, ','));
//            if(isLibMode && toFile){
//            	if(hashCode2NpcInfos.containsKey(info.animateRef)==false){
//            		hashCode2NpcInfos.put(info.animateRef, new ArrayList<Element>());
//            	}
//            	//将animateRef相同的NPCImageInfo放在同一个List 里，后面保存至文件
//            	hashCode2NpcInfos.get(info.animateRef).add(elem);
//            }else{
	            npcInfoElem.getMixedContent().add(elem);
//            }
	        }
        }
//        if(isLibMode && toFile){
//        	for(Integer hashCode:hashCode2NpcInfos.keySet()){
//        		List<Element> list = hashCode2NpcInfos.get(hashCode);
//        		Element docRoot = new Element("npcinfo");
//        		for(Element el:list){
//        			docRoot.getMixedContent().add(el);
//        		}
//        		Document doc = new Document(docRoot);
//        		String ctnName = projectOwner.fileMapGet(hashCode);
//        		String colName = projectOwner.getPrjDataPath()+ctnName.replaceAll("\\.ctn$", "\\.col");
//        		File colFile = new File(colName);
//        		Utils.saveDOM(doc, colFile);
//        	}
//        }
        
        // 保存参考调色板信息
        for (int[] pal : refPalettes) {
        	Element palElem = new Element("palette");
        	StringBuilder sb = new StringBuilder();
        	for (int i = 0; i < pal.length; i++) {
        		if (i > 0) {
        			sb.append(" ");
        		}
        		int i1 = (pal[i] >> 24) & 0xFF;
        		int i2 = (pal[i] >> 16) & 0xFF;
        		int i3 = (pal[i] >> 8) & 0xFF;
        		int i4 = pal[i] & 0xFF;
        		sb.append(hex(i1) + " " + hex(i2) + " " + hex(i3) + " " + hex(i4));
        	}
        	palElem.setText(sb.toString());
        	root.getMixedContent().add(palElem);
        }
    }
    
    private String hex(int i) {
    	String s = Integer.toHexString(i);
    	if (s.length() == 1) {
    		s = "0" + s;
    	}
    	return s;
    }
    
	/** 自动修正由于删除动画文件中的动画,导致NPC动画索引越界的隐患(越界时置为0) **/
    private void fixMissMath() {
		for(GameMap gm:maps){
			for(IMapLayer layer:gm.layers){
				if(layer instanceof MapNPCLayer){
					for(MapNPC npc:((MapNPCLayer)layer).getNpcs()){
						if(npc instanceof MultiAnimNPC){
							for(MapNPC cc:((MultiAnimNPC)npc).getChildren()){
								if(cc.animate>=getAnimateSet(cc.animateSetRef).getAnimateCount()){
									System.out.println("fix "+cc.animateSetRef+":"+cc.animate);
									cc.animate = 0;
								}
							}
						}else{
							if(npc.animate>=getAnimateSet(npc.animateSetRef).getAnimateCount()){
								System.out.println("fix "+npc.animateSetRef+":"+npc.animate);
								npc.animate = 0;
							}
						}
					}
				}
			}
		}
	}

	private void makeLibModeAnimates(Element animateElem) {
    	if(animateList!=null)
    	for(PipAnimateSet pas:animateList){
    		Element ctsEl = new Element("cts");
    		ctsEl.addAttribute("hashCode", pas.hashCode+"");
    		try {
				ctsEl.addAttribute("file", projectOwner.fileMapGet(pas.hashCode));
			} catch (Exception e) {
				e.printStackTrace();
			}
    		animateElem.getMixedContent().add(ctsEl);
    	}
	}

	// 保存一个PipImage为文本格式
    private String imageToText(PipImage image) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        return Base64.encode(bos.toByteArray());
    }
    
	public ArrayList<GameMap> getMaps() {
		return maps;
	}
	
	public ArrayList<TileSet> getLandforms() {
	    return landforms;
	}
	
	public TileSet getTileImage() {
	    return tileImage;
	}
	
	public PipAnimateSet getAnimates() {
	    return animates;
	}
	/**
	 * KEY,默认是NPC图片在动画集合中的顺序号，VALUE是NPC扩展信息<br/>
	 * 在编辑器改造后使用库模式时,key是animateRef<<32|index<br/>
	 * animateRef->引用的动画的hashCode值; index,该动画中的下标
	 * @return See MapFile.npcInfo
	 */
	public HashMap<Long, NPCImageInfo> getNPCs() {
	    return npcInfo;
	}
	
	public int getCellSize() {
	    return cellSize;
	}
	
	public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getBlurTileWidth() {
        return blurTileWidth;
    }

    public int getBlurTileHeight() {
        return blurTileHeight;
    }
    
    public void setBlurTileWidth(int w) {
    	blurTileWidth = w;
    }
    
    public void setBlurTileHeight(int h) {
    	blurTileHeight = h;
    }
    
    /**
     * 库模式下不适用.在MapNpcSelector的onDumFrame中有检测.
     * @param index
     */
    public void onAnimateInsert(int index) {
	    for (GameMap map : maps) {
	        for (IMapLayer layer : map.layers) {
	            if (layer instanceof MapNPCLayer) {
	                ((MapNPCLayer)layer).onAnimateInsert(index);
	            }
	        }
		}

	    // 调整NPC阻挡信息里的索引
        Iterator<Long> itor = npcInfo.keySet().iterator();
        HashMap<Long, NPCImageInfo> newInfo = new HashMap<Long, NPCImageInfo>();
        while (itor.hasNext()) {
            Long key = itor.next();
            if (key < index) {
                newInfo.put(key, npcInfo.get(key));
            } else {
                newInfo.put(key + 1, npcInfo.get(key));
            }
        }
        npcInfo = newInfo;
    }

    /**
	 * 删除一个动画序列。引用这一动画序列的所有NPC都会被删除。<br/>
	 * 库模式下不适用.在MapNpcSelector的onDeleteFrame中有检测
	 */
	public void removeAnimate(int index) {
	    for (GameMap map : maps) {
	        for (IMapLayer layer : map.layers) {
	            if (layer instanceof MapNPCLayer) {
	                ((MapNPCLayer)layer).onAnimateRemoved(index);
	            }
	        }
		}
	    animates.removeAnimate(index);
	    
	    // 调整NPC阻挡信息里的索引
	    Iterator<Long> itor = npcInfo.keySet().iterator();
	    HashMap<Long, NPCImageInfo> newInfo = new HashMap<Long, NPCImageInfo>();
	    while (itor.hasNext()) {
	        long key = itor.next();
	        if (key < index) {
	            newInfo.put(key, npcInfo.get(key));
	        } else if (key > index) {
	            newInfo.put(key - 1, npcInfo.get(key));
	        }
	    }
	    npcInfo = newInfo;
	    
	    // 优化动画文件，删除没有用到的帧和图块
	    List<Integer> unusedFrames = animates.findUnusedFrames();
	    for (int i = unusedFrames.size() - 1; i >= 0; i--) {
	        animates.removeFrame(unusedFrames.get(i));
	    }
	    for (int i = animates.getFileCount() - 1; i >= 0; i--) {
	        List<Integer> unusedPieces = animates.findUnusedPiece(i);
            PipImage image = animates.getSourceImage(i);
	        for (int j = unusedPieces.size() - 1; j >= 0; j--) {
	            int fcount = image.getImgCount();
                int pcount = image.getImagePalettes().size();
                int pieceToDelete = unusedPieces.get(j);
                pieceToDelete %= image.getImgCount();
                HashMap<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
                for (int k = 0; k < fcount; k++) {
                    if (k < pieceToDelete) {
                        for (int l = 0; l < pcount; l++) {
                            frameMap.put(l * fcount + k, l * (fcount - 1) + k);
                        }
                    } else if (k > pieceToDelete) {
                        for (int l = 0; l < pcount; l++) {
                            frameMap.put(l * fcount + k, l * (fcount - 1) + k - 1);
                        }
                    }
                }
                image.getImageDatas().remove(pieceToDelete);
                animates.adjustSourceFrame(i, frameMap);
	        }
	        if (image.getImgCount() == 0) {
	            animates.removeSourceFile(i);
	        }
	    }
	}
	
	/**
     * 前移一个动画序列，同时更新引用。<br/>
     * 库模式下不适用.在MapNpcSelector的onMoveUpFrame方法中有检测.
     */
    public void moveUpAnimate(int index) {
    	long idx = index;
        for (GameMap map : maps) {
            for (IMapLayer layer : map.layers) {
                if (layer instanceof MapNPCLayer) {
                    ((MapNPCLayer)layer).onAnimateMoveUp(index);
                }
            }
        }
        animates.swapAnimates(index, index - 1);
        NPCImageInfo info1 = npcInfo.remove(idx);
        NPCImageInfo info2 = npcInfo.remove(idx - 1);
        if (info1 != null) {
            npcInfo.put(idx - 1, info1);
        }
        if (info2 != null) {
            npcInfo.put(idx, info2);
        }
    }
	
	/**
	 * 删除一个贴图帧。如果这一帧指向的实际贴图图片没有引用了，则图片也被删除。所有引用这一贴图的地图图块都将被删除。
	 */
	public void removeTile(int frame) {
	    // 计算引用这一图片帧的所有Tile数量
	    TileInfo tinfo = tileImage.tileInfo.get(frame);
		int count = 0;
		for (TileInfo tt : tileImage.tileInfo) {
			if (tt.frameID == tinfo.frameID) {
				count++;
			}
		}
		tileImage.tileInfo.remove(frame);
		
		// 如果没有引用了，删除这一图片，所有Tile中大于这一图片索引的，索引值减1
		if (count == 1) {
			tileImage.image.getImageDatas().remove(tinfo.frameID);
			if (tileImage.image.getImgCount() == 0) {
				tileImage.image.getImagePalettes().clear();
			}
			for (TileInfo tt : tileImage.tileInfo) {
			    if (tt.frameID >= tinfo.frameID) {
			        tt.frameID--;
			    }
			}
		}
		
		// 更新所有地图
		for (GameMap map : maps) {
		    for (IMapLayer layer : map.layers) {
                if (layer instanceof AccurateMapLayer) {
                    ((AccurateMapLayer)layer).onTileRemoved(frame);
                }
            }
		}
	}
	
	/**
	 * 删除一个地形。
	 */
	public void removeLandform(int index) {
	    if(isLibMode){
	    	removeLandformRef(landforms.get(index).hashCode);
	    }
		landforms.remove(index);
	    
	    // 更新所有地图
	    for (GameMap map : maps) {
            for (IMapLayer layer : map.layers) {
                if (layer instanceof BlurMapLayer) {
                    ((BlurMapLayer)layer).onLandformRemoved(index);
                }
            }
        }
	}
	
	private void removeLandformRef(int hashCode) {
		try {
			projectOwner.removeAnimateRef(hashCode, sourceFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 把一个地形前移（越靠前的地形在地图中越靠近底层）
	 */
	public void moveUpLandform(int index) {
	    if (index < 1 || index >= landforms.size()) {
	        return;
	    }
	    
	    TileSet ts = landforms.get(index);
	    landforms.set(index, landforms.get(index - 1));
	    landforms.set(index - 1, ts);

	    // 更新所有地图
        for (GameMap map : maps) {
            for (IMapLayer layer : map.layers) {
                if (layer instanceof BlurMapLayer) {
                    ((BlurMapLayer)layer).onLandformSwap(index - 1, index);
                }
            }
        }
	}

    /**
     * 把一个地形后移（越靠后的地形在地图中越靠近顶层）
     */
    public void moveDownLandform(int index) {
        if (index >= landforms.size() - 1) {
            return;
        }
        
        TileSet ts = landforms.get(index);
        landforms.set(index, landforms.get(index + 1));
        landforms.set(index + 1, ts);

        // 更新所有地图
        for (GameMap map : maps) {
            for (IMapLayer layer : map.layers) {
                if (layer instanceof BlurMapLayer) {
                    ((BlurMapLayer)layer).onLandformSwap(index, index + 1);
                }
            }
        }
    }

    /**
     * 查找一帧贴图是否已存在，如果存在，返回索引；如果不存在，加入并返回索引。
     */
    public int findOrAddTile(int frame, int trans) {
        for (int i = tileImage.tileInfo.size() - 1; i >= 0; i--) {
            TileInfo info = tileImage.tileInfo.get(i);
            if (info.frameID == frame && info.transit == trans) {
                return i;
            }
        }
        TileInfo newFrame = new TileInfo();
        newFrame.frameID = frame;
        newFrame.transit = trans;
        newFrame.thumbColor = 0;
        newFrame.unpassable = false;
        tileImage.tileInfo.add(newFrame);
        return tileImage.tileInfo.size() - 1;
    }
    
    private String getEnlargeName(String rawName) {
    	int dotPos = rawName.lastIndexOf('.');
    	if (dotPos == -1) {
    		if (rawName.endsWith("_缩小")) {
    			return rawName.substring(0, rawName.length() - 3);
    		} else {
    			return rawName + "_放大";
    		}
    	} else {
    		String subName = rawName.substring(0, dotPos);
    		if (subName.endsWith("_缩小")) {
    			return subName.substring(0, subName.length() - 3) + rawName.substring(dotPos);
    		} else {
    			return subName + "_放大" + rawName.substring(dotPos);
    		}
    	}
    }
    
    private String getSmallerName(String rawName) {
    	int dotPos = rawName.lastIndexOf('.');
    	if (dotPos == -1) {
    		if (rawName.endsWith("_放大")) {
    			return rawName.substring(0, rawName.length() - 3);
    		} else {
    			return rawName + "_缩小";
    		}
    	} else {
    		String subName = rawName.substring(0, dotPos);
    		if (subName.endsWith("_放大")) {
    			return subName.substring(0, subName.length() - 3) + rawName.substring(dotPos);
    		} else {
    			return subName + "_缩小" + rawName.substring(dotPos);
    		}
    	}
    }
    
    private void replaceNPCAnimateSetRef(int oldHash, int newHash) {
    	for (GameMap map : maps) {
    		for (IMapLayer layer : map.layers) {
    			if (layer instanceof MapNPCLayer) {
    				MapNPCLayer npcLayer = (MapNPCLayer)layer;
    				for (MapNPC npc : npcLayer.getNpcs()) {
    					if (npc instanceof MultiAnimNPC) {
    						MultiAnimNPC mnpc = (MultiAnimNPC)npc;
    						for (MapNPC child : mnpc.getChildren()) {
    							if (child.animateSetRef == oldHash) {
    								child.animateSetRef = newHash;
    	    					}
    						}
    					} else {
	    					if (npc.animateSetRef == oldHash) {
	    						npc.animateSetRef = newHash;
	    					}
    					}
    				}
    			}
    		}
    	}
    }
    
    /**
     * 所有地图放大一倍。地图NPC和地图tile放大一倍。
     */
    public void enlarge() throws Exception {
        cellSize *= 2;
        tileWidth *= 2;
        tileHeight *= 2;
        blurTileWidth *= 2;
        blurTileHeight *= 2;
        for (GameMap map : maps) {
            map.enlarge();
        }
        if (landforms != null) {
            for (TileSet ts : landforms) {
                ts.image.enlarge();
                if (isLibMode) {
                	// 库模式下，不直接修改当前文件的内容，而是把文件改名后保存
                	String path = projectOwner.fileMapGet(ts.hashCode);
                	path = getEnlargeName(path);
                	ts.hashCode = projectOwner.regFile(prjDataPath + path);
                	
                	// 如果目标文件已经存在，不覆盖，而是从目标文件中载入
                	if (new File(prjDataPath, path).exists()) {
						PipImage lfImage = new LandformImage();
						String fileName = new File(prjDataPath, path).getAbsolutePath();
						lfImage.load(fileName);
						TileSet newLandform = new TileSet(lfImage);
						fileName = fileName.replaceAll("\\.ldf$", "\\.lfi");
						File lfiFile = new File(fileName);
						if (lfiFile.exists()) {
							Element tileInfoDocRoot = Utils.loadDOM(lfiFile).getRootElement();
							newLandform.loadTileInfo(tileInfoDocRoot);
						}
						ts.image = newLandform.image;
						ts.tileInfo = newLandform.tileInfo;
                	}
                }
            }
        }
        if (tileImage != null) {
            tileImage.image.enlarge();
        }
        if (isLibMode) {
        	// 库模式下，animates里实际上只包含了所有引用的AnimateSet里动画序列引用，所以直接
        	// 把引用动画都放大就可以了
        	// 注意，这里不直接修改当前文件的内容，而是把文件改名后保存。
        	for (int i = 0; i < animateList.size(); i++) {
        		PipAnimateSet aset = animateList.get(i);
        		aset.enlarge(true);
        		String path = projectOwner.fileMapGet(aset.hashCode);
        		path = getEnlargeName(path);
        		int oldHashCode = aset.hashCode;
        		aset.hashCode = projectOwner.regFile(prjDataPath + path);
        		aset.setOriginalFile(new File(getEnlargeName(aset.getOriginalFile().getAbsolutePath())));
        		for (int j = 0; j < aset.getFileCount(); j++) {
        			String fname = aset.getFileName(j);
        			fname = getEnlargeName(fname);
        			aset.setFileName(j, fname);
        		}
        		replaceNPCAnimateSetRef(oldHashCode, aset.hashCode);
        		
        		// 如果目标文件已经存在，不覆盖，而是从目标文件中载入
        		if (aset.getOriginalFile().exists()) {
        			PipAnimateSet newset = new PipAnimateSet();
        			newset.load(aset.getOriginalFile());
        			newset.hashCode = aset.hashCode;
        			if (aset.getAnimateCount() != newset.getAnimateCount()) {
        				throw new Exception("错误：放大动画文件已存在，但和源文件拥有动画数量不一致！\n" + aset.getOriginalFile());
        			}
        			animateList.set(i, newset);
        		}
        	}
        } else {
        	animates.enlarge(true);
        }
        for (NPCImageInfo nif : npcInfo.values()) {
            nif.enlarge();
        }
    }
    
    /**
     * 所有地图缩小一倍。地图NPC和地图tile缩小一倍。
     */
    public void smaller() throws Exception {
        cellSize /= 2;
        tileWidth /= 2;
        tileHeight /= 2;
        blurTileWidth /= 2;
        blurTileHeight /= 2;
        for (GameMap map : maps) {
            map.smaller();
        }
        if (landforms != null) {
            for (TileSet ts : landforms) {
                ts.image.smaller();
                if (isLibMode) {
                	// 库模式下，不直接修改当前文件的内容，而是把文件改名后保存
                	String path = projectOwner.fileMapGet(ts.hashCode);
                	path = getSmallerName(path);
                	ts.hashCode = projectOwner.regFile(prjDataPath + path);
                	
                	// 如果目标文件已经存在，不覆盖，而是从目标文件中载入
                	if (new File(prjDataPath, path).exists()) {
						PipImage lfImage = new LandformImage();
						String fileName = new File(prjDataPath, path).getAbsolutePath();
						lfImage.load(fileName);
						TileSet newLandform = new TileSet(lfImage);
						fileName = fileName.replaceAll("\\.ldf$", "\\.lfi");
						File lfiFile = new File(fileName);
						if (lfiFile.exists()) {
							Element tileInfoDocRoot = Utils.loadDOM(lfiFile).getRootElement();
							newLandform.loadTileInfo(tileInfoDocRoot);
						}
						ts.image = newLandform.image;
						ts.tileInfo = newLandform.tileInfo;
                	}
                }
            }
        }
        if (tileImage != null) {
            tileImage.image.smaller();
        }
        if (isLibMode) {
        	// 库模式下，animates里实际上只包含了所有引用的AnimateSet里动画序列引用，所以直接
        	// 把引用动画都放大就可以了
        	// 注意，这里不直接修改当前文件的内容，而是把文件改名后保存。
        	for (int i = 0; i < animateList.size(); i++) {
        		PipAnimateSet aset = animateList.get(i);
        		aset.smaller(true);
        		String path = projectOwner.fileMapGet(aset.hashCode);
        		path = getSmallerName(path);
        		int oldHashCode = aset.hashCode;
        		aset.hashCode = projectOwner.regFile(prjDataPath + path);
        		aset.setOriginalFile(new File(getSmallerName(aset.getOriginalFile().getAbsolutePath())));
        		for (int j = 0; j < aset.getFileCount(); j++) {
        			String fname = aset.getFileName(j);
        			fname = getSmallerName(fname);
        			aset.setFileName(j, fname);
        		}
        		replaceNPCAnimateSetRef(oldHashCode, aset.hashCode);
        		
        		// 如果目标文件已经存在，不覆盖，而是从目标文件中载入
        		if (aset.getOriginalFile().exists()) {
        			PipAnimateSet newset = new PipAnimateSet();
        			newset.load(aset.getOriginalFile());
        			newset.hashCode = aset.hashCode;
        			if (aset.getAnimateCount() != newset.getAnimateCount()) {
        				throw new Exception("错误：放大动画文件已存在，但和源文件拥有动画数量不一致！\n" + aset.getOriginalFile());
        			}
        			animateList.set(i, newset);
        		}
        	}
        } else {
        	animates.smaller(true);
        }
        for (NPCImageInfo nif : npcInfo.values()) {
            nif.smaller();
        }
    }

    /**
     * 删除库模式下地图对index指定的动画集合(animateSet)的引用
     * @param index animateList 中的下标
     */
	public void removeAniamteRef(int index) {
		PipAnimateSet pas = animateList.get(index);
		removeNpcRef(pas);
		animateList.remove(index);
		try {
			projectOwner.removeAnimateRef(pas.hashCode, sourceFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void removeNpcRef(PipAnimateSet pasRef){
		int refResHashCode = pasRef.hashCode;
		for (GameMap map : maps) {
	        for (IMapLayer layer : map.layers) {
	            if (layer instanceof MapNPCLayer) {
	                ((MapNPCLayer)layer).onAnimateRefRemoved(refResHashCode);
	            }
	        }
		}
//	    animates.removeAnimate(index);
	    PipAnimateSet newpas = new PipAnimateSet();
	    for(PipAnimateSet pas:animateList){
	    	if(pas.hashCode != pasRef.hashCode){
	    		for(int i=0; i<pas.getAnimateCount(); i++){
	    			newpas.addAnimate(pas.getAnimate(i));
	    		}
	    	}
	    }
	    animates = newpas;
	    
	    // 调整NPC阻挡信息里的索引
	    Iterator<Long> itor = npcInfo.keySet().iterator();
	    HashMap<Long, NPCImageInfo> newInfo = new HashMap<Long, NPCImageInfo>();
	    while (itor.hasNext()) {
	    	Long key = itor.next();
	        if ( (int)(key>>32) != refResHashCode) {
	            newInfo.put(key, npcInfo.get(key));
	        }
	    }
	    npcInfo = newInfo;
	    
	    // 优化动画文件，删除没有用到的帧和图块
//	    List<Integer> unusedFrames = animates.findUnusedFrames();
//	    for (int i = unusedFrames.size() - 1; i >= 0; i--) {
//	        animates.removeFrame(unusedFrames.get(i));
//	    }
//	    for (int i = animates.getFileCount() - 1; i >= 0; i--) {
//	        List<Integer> unusedPieces = animates.findUnusedPiece(i);
//            PipImage image = animates.getSourceImage(i);
//	        for (int j = unusedPieces.size() - 1; j >= 0; j--) {
//	            int fcount = image.getImgCount();
//                int pcount = image.getImagePalettes().size();
//                int pieceToDelete = unusedPieces.get(j);
//                pieceToDelete %= image.getImgCount();
//                HashMap<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
//                for (int k = 0; k < fcount; k++) {
//                    if (k < pieceToDelete) {
//                        for (int l = 0; l < pcount; l++) {
//                            frameMap.put(l * fcount + k, l * (fcount - 1) + k);
//                        }
//                    } else if (k > pieceToDelete) {
//                        for (int l = 0; l < pcount; l++) {
//                            frameMap.put(l * fcount + k, l * (fcount - 1) + k - 1);
//                        }
//                    }
//                }
//                image.getImageDatas().remove(pieceToDelete);
//                animates.adjustSourceFrame(i, frameMap);
//	        }
//	        if (image.getImgCount() == 0) {
//	            animates.removeSourceFile(i);
//	        }
//	    }		
	}
	/**
	 * 检测将要添加的资源和当前地图使用的组是否一致
	 * @param hashCode
	 * @return
	 * @throws Exception 
	 */
	private boolean checkGroupConstraints(int hashCode) throws Exception{
		String subResPath = projectOwner.fileMapGet(hashCode);
		subResPath = subResPath.substring(subResPath.indexOf(File.separator)+1);
		subResPath = subResPath.substring(0, subResPath.indexOf(File.separator));
		List<String> grpDirNames = projectOwner.getGroupDirNames();
		if(grpDirNames!=null && grpDirNames.contains(subResPath)){
			if(curGroup.equals("")==false){
				//如果是组资源，且当前地图有所属组，则必须与当前的组一致
				if(subResPath.startsWith(curGroup)==false){
					return false;
				}
			}else{
				//是组资源，且当前地图没有所属组，则设置
				curGroup = subResPath;
				projectOwner.addGroupCache(sourceFilePath, curGroup);
			}
		}
		return true;
	}
	/**
	 * 添加动画引用. hashCode是动画资源的唯一性标识符.
	 * @param hashCode
	 * @throws Exception
	 */
	public void addAnimateRef(int hashCode) throws Exception {
		if(getAnimate(hashCode, 0)!=null){
			throw new RepeatAddException("这个资源已经被添加过了.");
		}
		String ctsFilePath = prjDataPath+projectOwner.fileMapGet(hashCode);
		ctsFilePath = ctsFilePath.substring(0, ctsFilePath.length()-1)+"s";
        PipAnimateSet animate = new PipAnimateSet();
        animate.hashCode = hashCode;
        if(checkGroupConstraints(hashCode)==false){
        	throw new Exception("当前地图已经使用了分组 "+curGroup+" 里的资源。\n不能再使用其他组的资源。");
		}
        File file = new File(ctsFilePath);
		animate.load(file);
		animateList.add(animate);				
		for(int j=0; j<animate.getAnimateCount(); j++){
			animates.addAnimate(animate.getAnimate(j));
		}
	}
	
	/**
	 * 从库模式构建非库模式下的animateSet
	 * @return obj[] obj[0]->PipAnimateSet, obj[1]->HashMap<Point(animateSetRef, animate), Integer(new index in new animate set)> mapping
	 * @throws ColorsExceedException 
	 */
	public Object[] buildPackAni(int borderSize, int maxHeight) throws ColorsExceedException{
		AnimateSetOperator animatesOper = new AnimateSetOperator(borderSize, maxHeight);
		HashMap<Point, Integer> mapping = new HashMap<Point, Integer>();
		for(GameMap map:maps){
			for(IMapLayer layer : map.layers){
				if (layer instanceof MapNPCLayer == false) {
					continue;
				}
				MapNPCLayer nlayer = (MapNPCLayer) layer;
				List<MapNPC> npcs = nlayer.getNpcs();
				for (MapNPC npc : npcs) {
					Point key = new Point(npc.animateSetRef, npc.animate);
					if(mapping.containsKey(key)){
						continue;
					}
					if(npc instanceof MultiAnimNPC){
                        ArrayList<MapNPC> children = ((MultiAnimNPC)npc).getChildren();
                        for(MapNPC cNPC: children){
                        	key = new Point(cNPC.animateSetRef, cNPC.animate);
        					if(mapping.containsKey(key)){
        						continue;
        					}
        					animatesOper.addAnimate(getAnimateSet(cNPC.animateSetRef), cNPC.animate);
        					mapping.put(key, animatesOper.getAnimateCount() - 1);
                        }
                    }else{
//                    	try {
                    	animatesOper.addAnimate(getAnimateSet(npc.animateSetRef), npc.animate);
//                    	}catch(Exception e) {
//                    		System.out.println("count=" + animates.getAnimateCount() + ",npc.animateSetRef=" + npc.animateSetRef);
//                    		
//                    	}
                    	mapping.put(key, animatesOper.getAnimateCount() - 1);
                    }
				}
			}
		}
		Object[] ret = new Object[]{animatesOper, mapping}; 
		return ret;
	}
	
	/**
	 * 添加landForm引用
	 * @param hashCode
	 * @throws Exception
	 */
	public void addLandFormRef(int hashCode) throws Exception {
		for(TileSet ts:landforms){
			if(ts.hashCode == hashCode){
				throw new RepeatAddException("这个资源已经添加过了");
			}
		}
		if(checkGroupConstraints(hashCode)==false){
			throw new Exception("当前地图已经使用了分组 "+curGroup+" 里的资源。\n不能再使用其他组的资源。");
		}
		loadLandFormByHashCode(hashCode);
	}

	public long getNPCKey(MapNPC npc) {
		if (isLibMode) {
			return ((((long)npc.animateSetRef) & 0xFFFFFFFF) << 32) | npc.animate;
		} else {
			return npc.animate;
		}
	}
	public String getRefRealPath(int hashCode){
		try {
			return prjDataPath+projectOwner.fileMapGet(hashCode);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 保存所有引用的文件。这个方法只被用在地图整体放大的缩小操作上。如果对库模式的地图进行放大和缩小操作，
	 * 地图中所引用的库中的资源也会被放大或缩小，这些修改会在地图文件保存时一起保存。
	 */
	public void saveLibFiles() throws Exception {
		if (!isLibMode) {
			return;
		}
		
		// 保存修改后的地形文件
        for (TileSet landform : landforms) {
        	String sourceName = prjDataPath + projectOwner.fileMapGet(landform.hashCode);
        	landform.image.save(new File(sourceName));
        	ProjectParser.addFileRef(sourceName, this.sourceFilePath);
        }
        
        // 保存修改后的动画文件，以及里面的pip文件
        for (PipAnimateSet aset : animateList) {
        	File ctsFile = aset.getOriginalFile();
        	aset.save(ctsFile, true);
        	ProjectParser.addFileRef(ctsFile.getAbsolutePath(), this.sourceFilePath);
        	String ctnFile = ctsFile.getAbsolutePath().replaceAll("\\.cts$", "\\.ctn");
        	aset.save(new File(ctnFile), false);
        }
	}

	public void exportAsPkg() throws Exception{
		File dir = new File(sourceFilePath.replaceAll(".map$", ""));
		if(dir.exists()){
			for(File f:dir.listFiles()){
				f.delete();
			}
		}else{
			dir.mkdir();
		}
		Object obj[];
		obj = buildPackAni(0, 2048);
		PipAnimateSet mergedAni4libMap = (PipAnimateSet) obj[0];
		int cnt = mergedAni4libMap.getAnimateCount();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		mergedAni4libMap.save(os, false);
		Utils.saveFileData(new File(dir, dir.getName()+".ctn"), os.toByteArray());
		cnt = mergedAni4libMap.getFileCount();
		for(int i=0; i<cnt; i++){
			byte[] img = PipImage.makeImageFile(mergedAni4libMap.getSourceImage(i),false);
			Utils.saveFileData(new File(dir, mergedAni4libMap.getFileName(i)), img);	
		}		
	}
	
	// 测试导出成JPEG模式，然后再载进来
	public int testJPEG(JPEGMergeOption option) throws Exception {
		// 查找所有用到的图片
		List<PipImage> pips = new ArrayList<PipImage>();
		for (PipAnimateSet as : animateList) {
			for (int i = 0; i < as.getFileCount(); i++) {
				pips.add(as.getSourceImage(i));
			}
		}
		
		// 所有图块加到一个真彩色PIP里面
		PipImage mergeImg = new PipImage();
		mergeImg.setSupportColorOp(false);
    	mergeImg.setSupportMoreColors(false);
        mergeImg.setMergeMode(false);
        mergeImg.setTrueColor(true);
        mergeImg.setJPEGOption(option);
        for (PipImage pimg : pips) {
        	for (int i = 0; i < pimg.getImgCount(); i++) {
        		int[] frameRGB = pimg.getImageData(i).make(pimg.isTrueColor() ? null : pimg.getImagePalettes().get(0));
        		PipImageData newData = new PipImageData();
        		newData.width = pimg.getImageData(i).width;
        		newData.height = pimg.getImageData(i).height;
        		newData.data = frameRGB;
        		mergeImg.getImageDatas().add(newData);
        	}
        }

		// PIP文件存盘
        File pipFile = File.createTempFile("_iws_", ".pip");
        mergeImg.save(pipFile);
        long length = pipFile.length();
        
        // 重新载入
        mergeImg = new PipImage();
        mergeImg.load(pipFile.getAbsolutePath());
        pipFile.delete();
        
        // 更新所有内存中的PIP内容
        int find = 0;
		for (PipImage pimg : pips) {
			pimg.setTrueColor(true);
			for (int i = 0; i < pimg.getImgCount(); i++) {
				pimg.getImageData(i).data = mergeImg.getImageData(find).data;
				if (pimg.getImageData(i).width != mergeImg.getImageData(find).width) {
					throw new IllegalArgumentException();
				}
				if (pimg.getImageData(i).height != mergeImg.getImageData(find).height) {
					throw new IllegalArgumentException();
				}
				find++;
			}
		}
		
		return (int)length;
	}
	
	// 给某个区域内的图片加2像素的边
	private void addBorder(int[][] imgData, int x, int y, int width, int height) {
		// 查找每一行非透明内容的起始坐标和结束坐标，起始坐标-1表示本行空
		int[] xstart = new int[height];
		int[] xend = new int[height];
		for (int yy = y; yy < y + height; yy++) {
			int minx = -1;
			int maxx = -1;
			for (int xx = x; xx < x + width; xx++) {
				int p = imgData[yy][xx];
				if ((p & 0xFF000000) != 0) {
					if (minx == -1) {
						minx = xx;
					}
					maxx = xx;
				}
			}
			xstart[yy - y] = minx;
			xend[yy - y] = maxx;
		}
		
		// 水平方向，所有非空行，2边各填充2像素
		for (int yy = y; yy < y + height; yy++) {
			int minx = xstart[yy - y];
			int maxx = xend[yy - y];
			if (minx == -1) {
				continue;
			}
			imgData[yy][minx - 4] = imgData[yy][minx];
			imgData[yy][minx - 3] = imgData[yy][minx];
			imgData[yy][minx - 2] = imgData[yy][minx];
			imgData[yy][minx - 1] = imgData[yy][minx];
			imgData[yy][maxx + 1] = imgData[yy][maxx];
			imgData[yy][maxx + 2] = imgData[yy][maxx];
			imgData[yy][maxx + 3] = imgData[yy][maxx];
			imgData[yy][maxx + 4] = imgData[yy][maxx];
		}
		
		// 垂直方向，所有空行，从上边或者下边复制
		for (int yy = y; yy < y + height; yy++) {
			if (xstart[yy - y] != -1) {
				continue;
			}
			if (yy < y + height - 1 && xstart[yy + 1 - y] != -1) {
				int minx = xstart[yy + 1 - y];
				int maxx = xend[yy + 1 - y];
				System.arraycopy(imgData[yy + 1], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
			if (yy < y + height - 2 && xstart[yy + 2 - y] != -1) {
				int minx = xstart[yy + 2 - y];
				int maxx = xend[yy + 2 - y];
				System.arraycopy(imgData[yy + 2], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
			if (yy < y + height - 3 && xstart[yy + 3 - y] != -1) {
				int minx = xstart[yy + 3 - y];
				int maxx = xend[yy + 3 - y];
				System.arraycopy(imgData[yy + 3], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
			if (yy < y + height - 4 && xstart[yy + 4 - y] != -1) {
				int minx = xstart[yy + 4 - y];
				int maxx = xend[yy + 4 - y];
				System.arraycopy(imgData[yy + 4], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
			if (yy > y && xstart[yy - 1 - y] != -1) {
				int minx = xstart[yy - 1 - y];
				int maxx = xend[yy - 1 - y];
				System.arraycopy(imgData[yy - 1], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
			if (yy > y + 1 && xstart[yy - 2 - y] != -1) {
				int minx = xstart[yy - 2 - y];
				int maxx = xend[yy - 2 - y];
				System.arraycopy(imgData[yy - 2], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
			if (yy > y + 2 && xstart[yy - 3 - y] != -1) {
				int minx = xstart[yy - 3 - y];
				int maxx = xend[yy - 3 - y];
				System.arraycopy(imgData[yy - 3], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
			if (yy > y + 3 && xstart[yy - 4 - y] != -1) {
				int minx = xstart[yy - 4 - y];
				int maxx = xend[yy - 4 - y];
				System.arraycopy(imgData[yy - 4], minx, imgData[yy], minx, maxx - minx + 1);
				continue;
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		tileImage = null;
		animates = null;
		
		this.animateList.clear();
		this.maps.clear();
		this.landforms.clear();
		this.npcInfo .clear();
		this.refPalettes .clear();
		this.animateList = null;
		this.maps = null;
		this.npcInfo = null;
		this.refPalettes = null;
		
		super.finalize();
	}
}
