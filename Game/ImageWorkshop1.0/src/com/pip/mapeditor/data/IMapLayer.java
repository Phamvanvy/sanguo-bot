package com.pip.mapeditor.data;

import org.jdom.*;

/**
 * 地图层抽象接口。
 * @author lighthu
 */
public interface IMapLayer {
    /**
     * 获得层名称。
     */
    public String getName();
    
    /**
     * 获得层的npc数量
     * @return
     */
    public int getLayerCount();

    /**
     * 设置层名称。
     */
    public void setName(String name);
    
    /**
     * 从XML文档中载入
     * @param elem
     */
    public void load(Element elem);
    
    /**
     * 保存为XML文档
     * @return
     */
    public void save(Element elem);
    
    /**
     * 设置所属地图。
     */
    public void setParent(GameMap parent);
    
    /**
     * 复制。
     */
    public IMapLayer dup();
    
    /**
     * 按指定范围剪裁。
     */
    public void cut(int x, int y, int w, int h);
    
    public boolean getForceAddOrderDraw();
    
    public void setForceAddOrderDraw(boolean forceAddOrderDraw);
}
