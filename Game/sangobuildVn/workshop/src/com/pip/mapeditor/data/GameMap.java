package com.pip.mapeditor.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.jdom.*;

public class GameMap {
	/** 所属地图文件 */
	public MapFile parent;
    /** 地图宽度 */
    public int width;
    /** 地图高度 */
    public int height;
	/** 地图层 */
	public ArrayList<IMapLayer> layers;
	/** 地面层 */
	public IMapLayer groundLayer;
	/** 天空地面层 */
	public IMapLayer skyLayer;
	/** 视野遮挡定义 */
	public boolean[][] eyesightBlock;
	
	/**
	 * 地图阻挡信息 值解释:第0位(视线遮挡 1表示遮挡) 1位(地面层阻挡 1表示可通过) 2位(天空层阻挡 1表示可通过) 3位(安全区 0表示安全区)
	 */
	public byte[][] tileInfo;
    	
    /**
     * 缺省构造方法。
     * @param parent 所属地图文件。
     */
    public GameMap(MapFile parent, int mw, int mh) {
        this.parent = parent;
        width = mw;
        height = mh;
        layers = new ArrayList<IMapLayer>();
        eyesightBlock = new boolean[height / parent.getCellSize()][width / parent.getCellSize()];
        tileInfo = new byte[height / parent.getCellSize()][width / parent.getCellSize()];
    }
    
    /**
     * 从XML文件中读取。
     */
    public void load(Element elem) {    		
        width = Integer.parseInt(elem.getAttributeValue("width"));
        height = Integer.parseInt(elem.getAttributeValue("height"));
        eyesightBlock = new boolean[height / parent.getCellSize()][width / parent.getCellSize()];
        tileInfo = new byte[height / parent.getCellSize()][width / parent.getCellSize()];
        List layerList = elem.getChildren("layer");
        int size = layerList.size();
        for (int i = 0; i < size; i++) {
            Element elem1 = (Element)layerList.get(i);
            String type = elem1.getAttributeValue("type");
            IMapLayer newLayer;
            if ("accurate".equals(type)) {
                newLayer = new AccurateMapLayer(this);
            } else if ("blur".equals(type)) {
                newLayer = new BlurMapLayer(this);
            } else if ("npc".equals(type)) {
                newLayer = new MapNPCLayer(this);
            } else {
                throw new IllegalArgumentException("Unknown layer type.");
            }
            if ("true".equals(elem1.getAttributeValue("isground"))) {
                groundLayer = newLayer;
            }
            if ("true".equals(elem1.getAttributeValue("issky"))) {
            	skyLayer = newLayer;
            }
                        
        	String forceAddOrderDrawStr  = elem1.getAttributeValue("forceAddOrderDraw");
        	if(forceAddOrderDrawStr != null && "".equals(forceAddOrderDrawStr) == false) {
        		newLayer.setForceAddOrderDraw("1".equals(forceAddOrderDrawStr));
        	}
        	
            newLayer.load(elem1);
            layers.add(newLayer);
        }
        
        // 载入视线遮挡设置（可选）
        Element elem2 = elem.getChild("eyesightblock");
        if (elem2 != null) {
            List lineList = elem2.getChildren("line");
            size = lineList.size();
            for (int i = 0; i < size; i++) {
                Element lineElem = (Element)lineList.get(i);
                String[] secs = lineElem.getTextTrim().split(" ");
                for (int j = 0; j < eyesightBlock[i].length; j++) {
                    eyesightBlock[i][j] = "1".equals(secs[j]);
                }
            }
        }
        Element linesEl = elem.getChild("tileBlock");
        if(linesEl!=null){
        	List lineList = linesEl.getChildren("line");
        	size = lineList.size();
            for (int i = 0; i < size && i < tileInfo.length; i++) {
                Element lineElem = (Element)lineList.get(i);
                String[] secs = lineElem.getTextTrim().split(" ");
                for (int j = 0; j < secs.length && j < tileInfo[i].length; j++) {
                	tileInfo[i][j] = Byte.parseByte(secs[j]);
                }
            }
        }
    }
    
    /**
     * 保存到XML文档中。
     */
    public Element save() {
        Element elem = new Element("map");                
        elem.addAttribute("width", String.valueOf(width));
        elem.addAttribute("height", String.valueOf(height));
        int i = 0;
        for (IMapLayer layer : layers) {        	
            Element elem1 = new Element("layer");
            if (layer instanceof AccurateMapLayer) {
                elem1.addAttribute("type", "accurate");
            } else if (layer instanceof BlurMapLayer) {
                elem1.addAttribute("type", "blur");
            } else if (layer instanceof MapNPCLayer) {
                elem1.addAttribute("type", "npc");
            } else {
                throw new IllegalArgumentException("Unknown layer type.");
            }
            if (layer == groundLayer) {
                elem1.addAttribute("isground", "true");
            }
            if (layer == skyLayer) {
            	elem1.addAttribute("issky", "true");
            }
            
            elem1.addAttribute("forceAddOrderDraw", layer.getForceAddOrderDraw() ? "1" : "0");
            
            layer.save(elem1);
            elem.getMixedContent().add(elem1);
            i ++;
        }
        
        // 如果有视线遮挡设置，保存之
        Element elem2 = new Element("eyesightblock");
        boolean hasBlock = false;
        for (i = 0; i < eyesightBlock.length; i++) {
            Element lineElem = new Element("line");
            StringBuffer buf = new StringBuffer(200);
            for (int j = 0; j < eyesightBlock[i].length; j++) {
                if (eyesightBlock[i][j]) {
                    buf.append("1 ");
                    hasBlock = true;
                } else {
                    buf.append("0 ");
                }
            }
            lineElem.setText(buf.toString());
            elem2.getMixedContent().add(lineElem);
        }
        if (hasBlock) {
            elem.getMixedContent().add(elem2);
        }
        if(this.parent.isLibMode){
	        Element tileBlock = new Element("tileBlock");
	        for (i = 0; i < tileInfo.length; i++) {
	        	Element lineElem = new Element("line");
	            StringBuffer buf = new StringBuffer(200);
	            for (int j = 0; j < tileInfo[i].length; j++) {
	                    buf.append(tileInfo[i][j]+" ");
	            }
	            lineElem.setText(buf.toString());
	            tileBlock.getMixedContent().add(lineElem);
	        }
	        elem.getMixedContent().add(tileBlock);
        }
        return elem;
    }
    
    /**
     * 整体放大一倍。地图数据不变，所有tile放大；NPC层所有位置放大一倍。
     */
    public void enlarge() {
        width *= 2;
        height *= 2;
        for (IMapLayer layer : layers) {
            if (layer instanceof MapNPCLayer) {
                ((MapNPCLayer)layer).enlarge();
            }
        }
    }
    
    /**
     * 整体缩小一倍。地图数据不变，所有tile缩小；NPC层所有位置缩小一倍。
     */
    public void smaller() {
        width /= 2;
        height /= 2;
        for (IMapLayer layer : layers) {
            if (layer instanceof MapNPCLayer) {
                ((MapNPCLayer)layer).smaller();
            }
        }
    }
    
    /**
     * 复制成一个新的地图。
     */
    public GameMap dup() {
    	GameMap ret = new GameMap(parent, width, height);
    	for (IMapLayer layer : layers) {
    		IMapLayer newLayer = layer.dup();
    		newLayer.setParent(ret);
			ret.layers.add(newLayer);
    		if (layer == groundLayer) {
    			ret.groundLayer = newLayer;
    		}
    		if (layer == skyLayer) {
    			ret.skyLayer = newLayer;
    		}
    	}
    	ret.eyesightBlock = new boolean[eyesightBlock.length][eyesightBlock[0].length];
    	for (int i = 0; i < eyesightBlock.length; i++) {
    		System.arraycopy(eyesightBlock[i], 0, ret.eyesightBlock[i], 0, eyesightBlock[i].length);
    	}
    	ret.tileInfo = new byte[tileInfo.length][tileInfo[0].length];
    	for (int i = 0; i < tileInfo.length; i++) {
    		System.arraycopy(tileInfo[i], 0, ret.tileInfo[i], 0, tileInfo[i].length);
    	}
    	return ret;
    }
    
    /**
     * 保留地图的一部分，删除多余的部分。
     */
    public void cut(int x, int y, int w, int h) {
    	for (IMapLayer layer : layers) {
    		layer.cut(x, y, w, h);
    	}
    	int cx = x / parent.getCellSize();
    	int cy = y / parent.getCellSize();
    	int cw = w / parent.getCellSize();
    	int ch = h / parent.getCellSize();
    	
    	boolean[][] neweb = new boolean[ch][cw];
		int startX = Math.max(0, cx);
		int copyW = Math.min(eyesightBlock[0].length, cx + cw) - startX;
    	for (int i = 0; i < ch; i++) {
    		if (i + cy < 0) {
    			continue;
    		}
    		if (i + cy >= eyesightBlock.length) {
    			break;
    		}
    		System.arraycopy(eyesightBlock[i + cy], startX, neweb[i], startX - cx, copyW);
    	}
    	eyesightBlock = neweb;
    	
    	byte[][] newti = new byte[ch][cw];
    	startX = Math.max(0, cx);
		copyW = Math.min(tileInfo[0].length, cx + cw) - startX;
    	for (int i = 0; i < ch; i++) {
    		if (i + cy < 0) {
    			continue;
    		}
    		if (i + cy >= tileInfo.length) {
    			break;
    		}
    		System.arraycopy(tileInfo[i + cy], startX, newti[i], startX - cx, copyW);
    	}
    	tileInfo = newti;

    	width = w;
    	height = h;
    }
}
