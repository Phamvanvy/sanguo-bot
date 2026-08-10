package com.pip.mapeditor.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.jdom.*;

import com.pip.util.Utils;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipImage;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * 一组贴图集合。
 * @author lighthu
 */
public class TileSet {
	/**
	 * 
	 */
	public int hashCode;
    /** 图片文件 */
    public PipImage image;
    /** 贴图描述信息 */
    public ArrayList<TileInfo> tileInfo = new ArrayList<TileInfo>();
    
    /**
     * 缺省构造方法。
     * @param isLandform 是否地形文件
     */
    public TileSet(boolean isLandform) {
        if (isLandform) {
            image = new LandformImage();
        } else {
            image = new PipImage();
        }
    }
    /**
     * libMode
     * @param img
     */
    public TileSet(PipImage img){
    	image = img;
    }
    /**
     * 从XML文件中载入
     * @param elem
     * @throws Exception
     */
    public void load(Element elem) throws Exception {
        Element imageElem = elem.getChild("imagedata");
        byte[] data = Base64.decode(imageElem.getTextTrim());
        image.load(new ByteArrayInputStream(data));
        loadTileInfo(elem);
    }
    /**
     * 
     * @param elem should be land form node
     */
    public void loadTileInfo(Element elem){
    	// 载入贴图扩展描述信息
    	List tileList = elem.getChildren("tile");
    	for (int i = 0; i < tileList.size(); i++) {
    		Element elem1 = (Element)tileList.get(i);
    		TileInfo info = new TileInfo();
    		info.frameID = Integer.parseInt(elem1.getAttributeValue("frame"));
    		info.transit = Integer.parseInt(elem1.getAttributeValue("transit"));
    		info.thumbColor = Integer.parseInt(elem1.getAttributeValue("thumbcolor"));
    		info.unpassable = "false".equals(elem1.getAttributeValue("passable"));
    		tileInfo.add(info);
    	}
    	validate();
    }
    /**
     * 保存到XML文档。
     * @param elem
     */
    public void save(Element elem) throws Exception {
    	validate();
        Element imageElem = new Element("imagedata");
        imageElem.setText(imageToText(image));
        elem.getMixedContent().add(imageElem);
        saveTileInfo(elem);
    }
    // 保存贴图扩展描述信息
    private void saveTileInfo(Element elem){
    	for (TileInfo info : tileInfo) {
    		Element elem1 = new Element("tile");
    		elem1.addAttribute("frame", String.valueOf(info.frameID));
    		elem1.addAttribute("transit", String.valueOf(info.transit));
    		elem1.addAttribute("thumbcolor", String.valueOf(info.thumbColor));
    		elem1.addAttribute("passable", info.unpassable ? "false" : "true");
    		elem.getMixedContent().add(elem1);
    	}
    }
    // 保存一个PipImage为文本格式
    public static String imageToText(PipImage image) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        return Base64.encode(bos.toByteArray());
    }
 	
 	/*
     * 对于模糊地图地形，确保每个tile都有tileinfo。
     */
    public void validate() {
    	if (image instanceof LandformImage) {
    		while (tileInfo.size() > image.getImgCount()) {
    			tileInfo.remove(tileInfo.size() - 1);
    		}
    		TileInfo ref = null;
    		if (tileInfo.size() > 0) {
    			ref = tileInfo.get(tileInfo.size() - 1);
    		}
    		for (int i = tileInfo.size(); i < image.getImgCount(); i++) {
    			TileInfo tinfo = new TileInfo();
    			tinfo.frameID = i;
    			tinfo.transit = 0;
    			tinfo.thumbColor = ref == null ? 0 : ref.thumbColor;
    			tinfo.unpassable = ref == null ? false : ref.unpassable;
    			tileInfo.add(tinfo);
    		}
    	}
    }
	public void saveTileInfo(File lfiFile) throws Exception {
		Document doc = new Document(new Element("landformInfo"));
		for(TileInfo info:tileInfo){
			Element elem1 = new Element("tile");
            elem1.addAttribute("frame", String.valueOf(info.frameID));
            elem1.addAttribute("transit", String.valueOf(info.transit));
            elem1.addAttribute("thumbcolor", String.valueOf(info.thumbColor));
            elem1.addAttribute("passable", info.unpassable ? "false" : "true");
            doc.getRootElement().getMixedContent().add(elem1);
		}
		Utils.saveDOM(doc, lfiFile);
	}
}
