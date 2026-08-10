package com.pip.mapeditor.tool;

import java.awt.event.KeyEvent;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mango.jni.GLGraphics;
import com.pip.mapeditor.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 
 * @author jhkang
 *
 */
public class SkyPassableTool implements IMapEditTool {
    // 附着的编辑器
    protected MapViewer viewer;
    // 贴图选择影响的贴图查看器对象。当选中某个贴图块时会在贴图查看器中也选中。
    // 最近一次检测到的鼠标位置
    private int lastX = -1, lastY = -1;
    
    private Vector<Point[]> blockLines;
    /**
     * 有了起点坐标后,鼠标移动时的位置
     */
    private int curX = -1, curY = -1;
    /**
     * 任意时候鼠标的移动位置
     */
    private int mouseX, mouseY;
    private int underMouseLineIdx = -1;
    
    /**
     * @see com.pip.xiyou.GameSprite.flyEffectDY
     */
    public int flyEffectDY = 60;
    
    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public SkyPassableTool(MapViewer viewer, MapTileSelector tv) {
        this.viewer = viewer;
        blockLines = new Vector<Point[]>();
    }

    /**
     * 鼠标按下事件。
     * 贴图查看器中当前选中贴图。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
    	lastX = x;
    	lastY = y;
    }

    /**
     * 鼠标抬起事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        // 从顶层到底层检查点中的Tile
//        GameMap map = viewer.getMap();
    	if(lastX<0 || Math.sqrt(Math.pow((x-lastX),2)+Math.pow((y-lastY),2))<10){
    		lastX = -1;
    		viewer.redraw();
    		return;
    	}
    	Point start = new Point(lastX, lastY);
    	Point end = new Point(x, y);
    	blockLines.add(new Point[]{start, end});
    	viewer.fireContentChanged();
    	viewer.redraw();
    	lastX = -1;
    }

    /**
     * 鼠标移动事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
    	mouseX = x;
    	mouseY = y;
    	if(lastX >= 0){
	    	curX = x; curY = y;
//	    	viewer.redraw();
    	}else{
    		double nearest = Integer.MAX_VALUE;
    		int idx = -1;
    		for(int i=0; i<blockLines.size(); i++){
    			Point start = blockLines.get(i)[0];
    			Point end = blockLines.get(i)[1];
    			int[] param = makeLineParam(start.x, start.y, end.x, end.y);
    			int a = param[0];
    			int b = param[1];
    			int c = param[2];
    			double delta =Math.abs( (a*x+b*y+c) / Math.sqrt(x^2+y^2) );
    			if(delta<nearest){
    				nearest = delta;
    				idx = i;
    			}
    		}
    		if(idx>=0 && idx!=underMouseLineIdx){
    			underMouseLineIdx = idx;
//    			viewer.redraw();
    		}
    	}
    	viewer.redraw();
    }
    /**
     * 获得两点确定的直线的标准式参数Ax+By+C = 0; ret[]{A,B,C}
     * @param x
     * @param y
     * @param x2
     * @param y2
     * @return
     */
    public int[] makeLineParam(int x, int y, int x2, int y2){
    	int a = y - y2;
    	int b = x2 - x;
    	int c = x*y2 - x2*y;
    	return new int[]{a,b,c};
    }
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
    	int idx = 0;
    	int focusColor = SWT.COLOR_BLUE;
    	int normalShowColor = SWT.COLOR_RED;
    	gc.setLineWidth(5);
    	for(Point[] pos:blockLines){
    		gc.setForeground(SWTResourceManager.getColor(normalShowColor));
    		if(idx == underMouseLineIdx	){
    			gc.setForeground(SWTResourceManager.getColor(focusColor));
    		}
    		Point start = new Point(pos[0].x, pos[0].y);
    		Point end = new Point(pos[1].x, pos[1].y);
    		viewer.map2screen(start);
    		viewer.map2screen(end);
    		gc.drawLine(start.x, start.y, end.x, end.y);
    		idx ++;
    	}
    	gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	if(lastX >= 0){
	    	Point start = new Point(lastX, lastY);
			Point end = new Point(curX, curY); 
			viewer.map2screen(start);
			viewer.map2screen(end);
			gc.drawLine(start.x, start.y, end.x, end.y);
    	}
    	drawFlyFoot(gc);
    	gc.setLineWidth(1);
    }
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GLGraphics gc) {
    	int idx = 0;
    	int focusColor = SWT.COLOR_BLUE;
    	int normalShowColor = SWT.COLOR_RED;
    	//gc.setLineWidth(5);
    	for(Point[] pos:blockLines){
    		gc.setColor(SWTResourceManager.getColor(normalShowColor));
    		if(idx == underMouseLineIdx	){
    			gc.setColor(SWTResourceManager.getColor(focusColor));
    		}
    		Point start = new Point(pos[0].x, pos[0].y);
    		Point end = new Point(pos[1].x, pos[1].y);
    		viewer.map2screen(start);
    		viewer.map2screen(end);
    		gc.drawLine(start.x, start.y, end.x, end.y);
    		idx ++;
    	}
    	gc.setColor(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	if(lastX >= 0){
	    	Point start = new Point(lastX, lastY);
			Point end = new Point(curX, curY); 
			viewer.map2screen(start);
			viewer.map2screen(end);
			gc.drawLine(start.x, start.y, end.x, end.y);
    	}
    	drawFlyFoot(gc);
    	//gc.setLineWidth(1);
    }
    protected void drawFlyFoot(GC gc){
    	gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	Point p = new Point(mouseX, mouseY);
    	viewer.map2screen(p);
    	p.y -= flyEffectDY;
    	gc.fillArc(p.x - 15, p.y - 8, 30, 16, 0, 360);
    	gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
    	gc.fillArc(p.x - 13, p.y - 6, 26, 12, 0, 360);
    }
    
    protected void drawFlyFoot(GLGraphics gc){
    	gc.setColor(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	Point p = new Point(mouseX, mouseY);
    	viewer.map2screen(p);
    	p.y -= flyEffectDY;
    	gc.fillArc(p.x - 15, p.y - 8, 30, 16, 0, 360);
    	gc.setColor(SWTResourceManager.getColor(SWT.COLOR_GRAY));
    	gc.fillArc(p.x - 13, p.y - 6, 26, 12, 0, 360);
    }
    
    /**
     * 键按下事件。
     */
    public void onKeyDown(int keyCode) {}

    /**
     * 键松开事件。
     */
    public void onKeyUp(int keyCode) {
    	switch(keyCode){
    	case KeyEvent.VK_DELETE:
    		if(underMouseLineIdx>=0){
    			blockLines.removeElementAt(underMouseLineIdx);
    			viewer.fireContentChanged();
    			viewer.redraw();
    		}
    		break;
    	}
    }

    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu() {
        return null;
    }
}