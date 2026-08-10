package com.pip.mapeditor.tool;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Menu;

/**
 * 地图编辑组件使用的编辑工具的接口。通过这个接口把作用于地图上不同类型的操作抽象出来。一个地图编辑器上同时
 * 只能使用一种工具。
 * @author lighthu
 */
public interface IMapEditTool {
    /**
     * 鼠标按下事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask);

    /**
     * 鼠标抬起事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask);
    
    /**
     * 鼠标移动事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y);

    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc);

    /**
     * 键按下事件。
     */
    public void onKeyDown(int keyCode);
    
    /**
     * 键松开事件。
     */
    public void onKeyUp(int keyCode);
    
    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu();
}
