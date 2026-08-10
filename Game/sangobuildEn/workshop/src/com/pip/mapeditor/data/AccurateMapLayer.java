package com.pip.mapeditor.data;

import java.io.*;
import java.util.*;
import org.jdom.*;

/**
 * 精确贴图层。
 * @author lighthu
 */
public class AccurateMapLayer implements IMapLayer {
	/** 所属地图 */
	public GameMap parent;
	/** 地图层名称 */
	private String name;
	/** 地图层贴图数据：每个字节对应地图上的一块，存储对应的贴图序号。-1表示空。*/
	private short[][] layerData;
	
	private boolean forceAddOrderDraw;
	
    /**
	 * 缺省构造方法。
	 * @param parent 所属地图，地图的基本信息必须已经确定。
	 */
	public AccurateMapLayer(GameMap parent) {
		this.parent = parent;
		int cols = parent.width / parent.parent.getTileWidth();
		int rows = parent.height / parent.parent.getTileHeight();
		layerData = new short[rows][cols];
		for (int i = 0; i < rows; i++) {
			Arrays.fill(layerData[i], (short)-1);
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
    	AccurateMapLayer ret = new AccurateMapLayer(parent);
    	ret.name = name;
    	ret.layerData = new short[layerData.length][layerData[0].length];
    	for (int i = 0; i < layerData.length; i++) {
    		System.arraycopy(layerData[i], 0, ret.layerData[i], 0, layerData[i].length);
    	}
    	return ret;
    }
    
    /**
     * 按指定范围剪裁。
     */
    public void cut(int x, int y, int w, int h) {
    	int cx = x / parent.parent.getTileWidth();
    	int cy = y / parent.parent.getTileHeight();
    	int cw = w / parent.parent.getTileWidth();
    	int ch = h / parent.parent.getTileHeight();
    	short[][] newdata = new short[ch][cw];
    	for (int i = 0; i < ch; i++) {
    		System.arraycopy(layerData[i + cy], cx, newdata[i], 0, cw);
    	}
    	layerData = newdata;
    }
    
	/**
	 * 设置一个1x1的临时空间。
	 */
	public void setSingle() {
	    layerData = new short[1][1];
	}

    /**
     * 从XML文档中载入
     * @param elem
     */
    public void load(Element elem) {
        name = elem.getAttributeValue("name");
        ArrayList<short[]> lines = new ArrayList<short[]>();
        List lineList = elem.getChildren("line");
        for (int i = 0; i < lineList.size(); i++) {
            Element lineElem = (Element)lineList.get(i);
            short[] lineData = textToBytes(lineElem.getText());
            lines.add(lineData);
        }
        layerData = new short[lines.size()][];
        lines.toArray(layerData);
    }
    /**
     * 保存为XML文档
     * @return
     */
    public void save(Element elem) {
        elem.addAttribute("name", name);
        for (int i = 0; i < layerData.length; i++) {
            Element elem1 = new Element("line");
            elem1.setText(bytesToText(layerData[i]));
            elem.getMixedContent().add(elem1);
        }
    }
    
    // 字节数据转换为文本显示
    private String bytesToText(short[] data) {
        StringBuffer buf = new StringBuffer(data.length * 3);
        int count = data.length;
        for (int i = 0; i < count; i++) {
            if (data[i] >= 255) {
                throw new IllegalArgumentException("最多只能使用255个贴图块。");
            }
            String str = Integer.toHexString(data[i] & 0xFF);
            if (str.length() == 1) {
                buf.append('0');
            }
            buf.append(str);
            buf.append(' ');
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    // 文本转换为字节数据
    private short[] textToBytes(String str) {
        String[] secs = str.trim().split(" ");
        int count = secs.length;
        short[] ret = new short[count];
        for (int i = 0; i < count; i++) {
            ret[i] = (short)Integer.parseInt(secs[i], 16);
            if (ret[i] == 0xFF) {
                ret[i] = -1;
            }
        }
        return ret;
    }

    /**
     * 获取地图格点数据。
     * @return
     */
    public short[][] getLayerData() {
        return layerData;
    }
    
    /**
     * 处理贴图被删除事件。
     * @param frame 被删除贴图的ID
     */
    public void onTileRemoved(int frame) {
        for (int i = 0; i < layerData.length; i++) {
            for (int j = 0; j < layerData[i].length; j++) {
                int index = layerData[i][j];
                if (index == frame) {
                    layerData[i][j] = -1;
                } else if (index > frame) {
                    layerData[i][j] = (short)(index - 1);
                }
            }
        }
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

	public boolean getForceAddOrderDraw() {
		return forceAddOrderDraw;
	}

	public void setForceAddOrderDraw(boolean forceAddOrderDraw) {
		this.forceAddOrderDraw = forceAddOrderDraw;
	}
}
