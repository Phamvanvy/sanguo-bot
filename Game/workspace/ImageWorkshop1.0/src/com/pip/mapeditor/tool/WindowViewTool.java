package com.pip.mapeditor.tool;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;

import com.pip.image.workshop.Settings;
import com.pip.mapeditor.MapViewer;
import com.swtdesigner.SWTResourceManager;

/**
 * 通过小窗口查看地图的工具。
 * @author lighthu
 */
public class WindowViewTool implements IMapEditTool {
    // 附着的编辑器
    private MapViewer viewer;
    // 最近一次检测到的鼠标位置
    private int lastX = -1, lastY = -1;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public WindowViewTool(MapViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * 鼠标按下事件。
     * 贴图查看器中当前选中贴图。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
    }

    /**
     * 鼠标抬起事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
    }

    /**
     * 鼠标移动事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
        lastX = x;
        lastY = y;
        viewer.redraw();
    }

    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
    	int windowW = Settings.screenWidth;
    	int windowH = Settings.screenHeight;
    	
    	// 计算露出区域
    	Rectangle rect = new Rectangle(lastX - windowW / 2, lastY - windowH / 2, windowW, windowH);
    	viewer.map2screen(rect);
    	Point size = viewer.getSize();
    	if (rect.x < 0) {
    		rect.width += rect.x;
    		rect.x = 0;
    	}
    	if (rect.y < 0) {
    		rect.height += rect.y;
    		rect.y = 0;
    	}
    	if (rect.x + rect.width > size.x) {
    		rect.width = size.x - rect.x;
    	}
    	if (rect.y + rect.height > size.y) {
    		rect.height = size.y - rect.y;
    	}
    	
    	// 绘制蒙版
    	gc.setAlpha(0xE0);
    	gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		gc.fillRectangle(0, 0, size.x, rect.y);
		gc.fillRectangle(0, rect.y, rect.x, rect.height);
		gc.fillRectangle(rect.x + rect.width, rect.y, size.x - rect.x - rect.width, rect.height);
		gc.fillRectangle(0, rect.y + rect.height, size.x, size.y - rect.y - rect.height);
        gc.setAlpha(0xFF);
    }

    /**
     * 键按下事件。
     */
    public void onKeyDown(int keyCode) {}

    /**
     * 键松开事件。
     */
    public void onKeyUp(int keyCode) {}

    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu() {
        return null;
    }
}
