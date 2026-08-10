package com.pip.mapeditor.data;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import org.jdom.*;

import com.pip.mapeditor.BlurMapUtil;
import com.pipimage.image.LandformImage;

/**
 * 模糊贴图层。
 * @author lighthu
 */
public class BlurMapLayer implements IMapLayer {
	/** 所属地图 */
	public GameMap parent;
    /** 地图层名称 */
    private String name;
	/** 随机数种子 */
	private int randomSeed;
	/** 基础地形 */
	private int baseLandform;
    /** 地形块列表 */
	private byte[][] layerData;
	/** 
	 * 生成的地形图缓存：地形图中每个格点的数组中，从低到高4个int分别表示4层叠加数据。每一层的数据中，
	 * -1表示空；4个字节从高到低依次是：保留、地形ID、翻转值、Tile索引。
	 * 两个字节，-1表示空；高字节高4位表示地形ID、低3位表示翻转值，低字节表示Tile索引。
	 */
	private int[][][] mapDataBuffer;
	
	private boolean forceAddOrderDraw;
	
	/**
	 * 缺省构造方法。
	 * @param parent 所属地图，地图的基本信息必须已经确定。
	 */
	public BlurMapLayer(GameMap parent) {
		this.parent = parent;
		randomSeed = (int)System.currentTimeMillis();
		baseLandform = -1;
		int cols = parent.width / parent.parent.getBlurTileWidth();
        int rows = parent.height / parent.parent.getBlurTileHeight();
        layerData = new byte[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(layerData[i], (byte)-1);
        }
	}
	
	/**
     * 设置所属地图。
     */
    public void setParent(GameMap parent) {
    	this.parent = parent;
    }
    
    /**
     * 复制。
     */
    public IMapLayer dup() {
    	BlurMapLayer ret = new BlurMapLayer(parent);
    	ret.name = name;
    	ret.randomSeed = randomSeed;
    	ret.baseLandform = baseLandform;
    	ret.layerData = new byte[layerData.length][layerData[0].length];
    	for (int i = 0; i < layerData.length; i++) {
    		System.arraycopy(layerData[i], 0, ret.layerData[i], 0, layerData[i].length);
    	}
    	return ret;
    }
    
    /**
     * 按指定范围剪裁。
     */
    public void cut(int x, int y, int w, int h) {
    	int cx = x / parent.parent.getBlurTileWidth();
    	int cy = y / parent.parent.getBlurTileHeight();
    	int cw = w / parent.parent.getBlurTileWidth();
    	int ch = h / parent.parent.getBlurTileHeight();
    	byte[][] newdata = new byte[ch][cw];
    	int startX = Math.max(0, cx);
		int copyW = Math.min(layerData[0].length, cx + cw) - startX;
    	for (int i = 0; i < ch; i++) {
    		if (i + cy < 0) {
    			continue;
    		}
    		if (i + cy >= layerData.length) {
    			break;
    		}
    		System.arraycopy(layerData[i + cy], startX, newdata[i], startX - cx, copyW);
    	}
    	layerData = newdata;
    	mapDataBuffer = null;
    }

    /**
     * 从XML文档中载入
     * @param elem
     */
    public void load(Element elem) {
        name = elem.getAttributeValue("name");
        randomSeed = Integer.parseInt(elem.getAttributeValue("seed"));
        baseLandform = Integer.parseInt(elem.getAttributeValue("baselandform"));
        
        ArrayList<byte[]> lines = new ArrayList<byte[]>();
        List lineList = elem.getChildren("line");
        int size = lineList.size();
        for (int i = 0; i < size; i++) {
            Element lineElem = (Element)lineList.get(i);
            byte[] lineData = textToBytes(lineElem.getText());
            lines.add(lineData);
        }
//        layerData = new byte[lines.size()][];
//        lines.toArray(layerData);
        
        // remove adundunt data
        int cols = parent.width / parent.parent.getBlurTileWidth();
        int rows = parent.height / parent.parent.getBlurTileHeight();
        layerData = new byte[rows][cols];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(layerData[i], (byte)-1);
            if (i < size) {
            	byte[] fd = lines.get(i);
            	if (fd.length > layerData[i].length) {
            		System.arraycopy(fd, 0, layerData[i], 0, layerData[i].length);
            	} else {
            		System.arraycopy(fd, 0, layerData[i], 0, fd.length);
            	}
            }
        }
    }
    
    /**
     * 保存为XML文档
     * @return
     */
    public void save(Element elem) {
        elem.addAttribute("name", name);
        elem.addAttribute("seed", String.valueOf(randomSeed));
        elem.addAttribute("baselandform", String.valueOf(baseLandform));
        for (int i = 0; i < layerData.length; i++) {
            Element elem1 = new Element("line");
            elem1.setText(bytesToText(layerData[i]));
            elem.getMixedContent().add(elem1);
        }
    }

    // 字节数据转换为文本显示
    private String bytesToText(byte[] data) {
        StringBuffer buf = new StringBuffer(data.length * 3);
        int count = data.length;
        for (int i = 0; i < count; i++) {
            buf.append(data[i]);
            buf.append(' ');
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    // 文本转换为字节数据
    private byte[] textToBytes(String str) {
        String[] secs = str.trim().split(" ");
        int count = secs.length;
        byte[] ret = new byte[count];
        for (int i = 0; i < count; i++) {
            ret[i] = (byte)Integer.parseInt(secs[i]);
        }
        return ret;
    }
    
    /**
     * 取得当前计算地图用的随机数种子。
     */
    public int getRandomSeed() {
        return randomSeed;
    }

    /**
     * 设置新的随机数种子。
     * @param randomSeed
     */
    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
    }
    
    /**
     * 获得基本地形。
     */
    public int getBaseLandform() {
        return baseLandform;
    }

    /**
     * 设置基本地形。
     */
    public void setBaseLandform(int baseLandform) {
        this.baseLandform = baseLandform;
        clearBuffer();
    }

    /**
     * 获取地图格点数据。
     * @return
     */
    public byte[][] getLayerData() {
        return layerData;
    }

    /**
     * 处理地形被删除事件。
     */
    public void onLandformRemoved(int index) {
        if (baseLandform == index) {
            baseLandform = -1;
        }
        for (int i = 0; i < layerData.length; i++) {
            for (int j = 0; j < layerData[i].length; j++) {
                if (layerData[i][j] == index) {
                    layerData[i][j] = -1;
                }
            }
        }
        clearBuffer();
    }
    
    /**
     * 处理地形ID互换事件。
     */
    public void onLandformSwap(int index1, int index2) {
        if (baseLandform == index1) {
            baseLandform = index2;
        } else if (baseLandform == index2) {
            baseLandform = index1;
        }
        for (int i = 0; i < layerData.length; i++) {
            for (int j = 0; j < layerData[i].length; j++) {
                if (layerData[i][j] == index1) {
                    layerData[i][j] = (byte)index2;
                } else if (layerData[i][j] == index2) {
                    layerData[i][j] = (byte)index1;
                }
            }
        }
        clearBuffer();
    }

    /**
     * 获得层名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 设置层名称。
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获得层的NPC数量
     */
    public int getLayerCount(){
        return 0;
    }
    
    /**
     * 清除缓存的地图数据。
     */
    public void clearBuffer() {
        mapDataBuffer = null;
    }
    
    /**
     * 取得实际计算出来的地图数据。
     * @return
     */
    public int[][][] getMapData() {
        if (mapDataBuffer == null) {
            createBuffer();
        }
        return mapDataBuffer;
    }
    
    // 生成地图数据。
    private void createBuffer() {
        int rows = layerData.length;
        int cols = layerData[0].length;
        
        // 地图数据初始化为全透明（-1）
        mapDataBuffer = new int[rows][cols][3];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Arrays.fill(mapDataBuffer[i][j], -1);
            }
        }
        
        // 如果有基础地形，则铺满基础地形的100%块(1111)
        if (baseLandform != -1) {
            Random rand = new Random(randomSeed);
            LandformImage image = (LandformImage)parent.parent.getLandforms().get(baseLandform).image;
            image.generateSearchTable();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int[] tile = image.getTile(rand, 0x0F);
                    if (tile[0] != -1) {
                        mapDataBuffer[i][j] = new int[] { makeLayerBits(baseLandform, tile[0], tile[1]), -1, -1 };
                    }
                }
            }
        }
        
        // 按照地形的优先顺序开始铺地图
        for (int lf = 0; lf < parent.parent.getLandforms().size(); lf++) {
            if (lf == baseLandform) {
                continue;
            }
            Random rand = new Random(randomSeed);
            byte[][] tmpData = BlurMapUtil.makeLayer(layerData, lf);
            LandformImage image = (LandformImage)parent.parent.getLandforms().get(lf).image;
            image.generateSearchTable();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int[] tile = image.getTile(rand, tmpData[i + 1][j + 1]);
                    if (tile[0] != -1) {
                        if (tmpData[i + 1][j + 1] == 0x0F) {
                            // 如果计算出来这个Tile已经是占100%了，则覆盖以前的所有Tile
                            mapDataBuffer[i][j] = new int[] { makeLayerBits(lf, tile[0], tile[1]), -1, -1 };
                        } else {
                            // 如果不占满，则合并
                            mergeGridData(mapDataBuffer[i][j], makeLayerBits(lf, tile[0], tile[1]));
                        }
                    }
                }
            }
        }
    }
    
    // 生成最终地图数据中一个格点一个地形层的数据
    private int makeLayerBits(int lfid, int tileid, int transit) {
        return (lfid << 16) | (transit << 8) | tileid;
    }
    
    // 把一层地形格点数据合并到最终地图数据中
    private void mergeGridData(int[] cell, int newLayer) {
        for (int i = 0; i < cell.length; i++) {
            if (cell[i] == -1) {
                cell[i] = newLayer;
                break;
            }
        }
    }
    
	public boolean getForceAddOrderDraw() {
		return forceAddOrderDraw;
	}

	public void setForceAddOrderDraw(boolean forceAddOrderDraw) {
		this.forceAddOrderDraw = forceAddOrderDraw;
	}
}
