package com.pip.mapeditor.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 地图层设置工具
 * @author jhkang
 */
public class TileConfigTool implements IMapEditTool {
	/**
     * @see com.pip.xiyou.GameSprite.flyEffectDY
     */
    public int flyEffectDY = 60;
    // 附着的编辑器
    protected MapViewer viewer;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;
    // 当前是否拖动状态
    private boolean isDragging;
    // 设置按钮的文本
    private String[] buttonTexts = { "设置", "清除" ,"1x1", "3x3", "5x5", "7x7", "30x30"};
    private int brushSizeShift[] = new int[]{0, 1, 2, 3, 4};
    private int brushSize[] = new int[]{1, 3, 5, 7, 30};
    // 设置按钮的位置和大小
    private Rectangle[] buttonBounds = new Rectangle[buttonTexts.length];
    // 选中的按钮
    private int selectedButtonIndex = 0;
    
    private int selectedBrushSize = 1;
    
    public static final byte CONFIG_MASK_SIGHT = 1;
    
    public static final byte CONFIG_MASK_GROUND = 2;
    
    public static final byte CONFIG_MASK_SKY= 4;
    
    public static final byte CONFIG_MASK_SAFE_AREA = 8;
    
    private byte configMask = CONFIG_MASK_SKY;
    private boolean reverse;  // 如果为true，则显示时1才画黑框
    private String hintMessage;
    
    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param mask 
     * @param tv 贴图查看器
     */
    public TileConfigTool(MapViewer viewer, byte mask, boolean reverse, String  hint) {
        this.viewer = viewer;
        this.reverse = reverse;
        lastX = -1;
        lastY = -1;
        this.configMask = mask;
        this.hintMessage = hint;
    }

    // 根据坐标计算此坐标对应的格点坐标
    protected void map2cell(Point pt) {
        pt.x /= viewer.getMap().parent.getCellSize();
        pt.y /= viewer.getMap().parent.getCellSize();
    }

    // 判断一个坐标是否合法格点位置坐标
    protected boolean isValidCell(Point pt) {
        if (pt.x < 0 || pt.x >= viewer.getMap().width) {
            return false;
        }
        if (pt.y < 0 || pt.y >= viewer.getMap().height) {
            return false;
        }
        return true;
    }

    /**
     * 鼠标按下事件。开始拖动绘制。
     * 贴图查看器中当前选中贴图。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
        // 检查是否点到按钮选择框
        Point pt = new Point(x, y);
        viewer.map2screen(pt);
        for (int i = 0; i < buttonBounds.length; i++) {
            if (buttonBounds[i].contains(pt)) {
                return;
            }
        }
        response(x, y);
        viewer.fireContentChanged();
        isDragging = true;
        lastX = x;
        lastY = y;
    }

    /**
     * 鼠标抬起事件。拖动绘制结束，把拖动过的点都设置为选中的Tile。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        if (isDragging) {
            isDragging = false;
        }

        // 检查是否点到按钮选择框
        Point pt = new Point(x, y);
        viewer.map2screen(pt);
        for (int i = 0; i < buttonBounds.length; i++) {
            if (buttonBounds[i].contains(pt)) {
                if(i<=1){
                	this.selectedButtonIndex = i;
                }else{
                	this.selectedBrushSize = i - 1;
                }
                break;
//                return;
            }
        }
        viewer.redraw();
    }
    
    /**
     * 鼠标移动事件。拖动刷子。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
        if (isDragging) {
            if(Math.abs(x-lastX)>viewer.getMap().parent.getCellSize() || Math.abs(y - lastY)>viewer.getMap().parent.getCellSize()){
            	fillLine(x, y);
            }else{
            	response(x,y);
            }
            viewer.fireContentChanged();
        }
        lastX = x;
        lastY = y;
        viewer.redraw();
    }
    
    protected void fillLine(int x, int y){
    	int baseX = Math.min(lastX, x);
    	int baseY = Math.min(lastY, y);
    	int endX = Math.max(lastX, x);
    	int endY = Math.max(lastY, y);
    	int step = viewer.getMap().parent.getCellSize();
    	int[] lineParam = makeLineParam(lastX, lastY, x, y);
    	int a = lineParam[0], b = lineParam[1], c = lineParam[2];
    	int xx = baseX, yy = baseY;
    	while(true){
    		if(b!=0){
	    		xx += step;
	    		if(xx>=endX){
	    			break;
	    		}
	    		yy = Math.round(-(a*xx + c)/b);
    		}else{
    			yy += step;
    			if(yy>=endY){
    				break;
    			}
    		}
    		response(xx, yy);
    	}
    }
    /**
     * 获得两点确定的直线的标准式参数Ax+By+C = 0; ret[]{A,B,C}
     * @param x
     * @param y
     * @param x2
     * @param y2
     * @return
     */
    public static int[] makeLineParam(int x, int y, int x2, int y2){
    	int a = y - y2;
    	int b = x2 - x;
    	int c = x*y2 - x2*y;
    	return new int[]{a,b,c};
    }
    
    protected void response(int x, int y){
    	int cellSize = viewer.getMap().parent.getCellSize();
    	x -= brushSizeShift[selectedBrushSize - 1]*cellSize;
    	y -= brushSizeShift[selectedBrushSize - 1]*cellSize;
    	Point pt = new Point(x, y);
        byte[][] data = viewer.getMap().tileInfo;
        int len = brushSize[selectedBrushSize - 1];
        for(int i=0; i<len;i++){
        	for(int j=0; j<len; j++){
        		pt.y = y+i*cellSize;
        		pt.x = x + j*cellSize;
		        if (isValidCell(pt)) {
		            map2cell(pt);
		            if (reverse) {
			        	if (selectedButtonIndex == 0) { //设置
			        		data[pt.y][pt.x] |= configMask; 
			        	} else {
			        		data[pt.y][pt.x] &= ~configMask;
			        	}
		            } else {
		            	if (selectedButtonIndex == 0) { //设置
			        		data[pt.y][pt.x] &= ~configMask; 
			        	} else {
			        		data[pt.y][pt.x] |= configMask;
			        	}
		            }
		        }
        	}
        }
    }
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
    	
    	drawCellMask(gc);

        // 绘制按钮
        drawButtons(gc);
        
        // 绘制座标
        drawCoordinate(gc);
        
        drawMouseOverRect(gc);
        if(configMask ==  CONFIG_MASK_SKY){
        	drawFlyFoot(gc);
        }
        
        // 绘制提示信息
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        Point size = viewer.getSize();
		Point ts = gc.textExtent(hintMessage);
		gc.drawText(hintMessage, size.x / 2 - ts.x / 2, size.y - ts.y - 5);
    }
    
    /** 绘制格点属性<br/>
     * 不可通过绘制黑色框,可通过不绘制<br/>
     * 可飞行可行走绘制黄色点框.<br/>
     * 视野遮挡时不绘制表示遮挡 */
    protected void drawCellMask(GC gc) {
    	GameMap map = viewer.getMap();
        int cellSize = map.parent.getCellSize();
        int i = 0;
        for (; i < map.tileInfo.length; i++) {
            for (int j = 0; j < map.tileInfo[i].length; j++) {
            	gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            	gc.setLineStyle(SWT.LINE_SOLID);
                byte value = map.tileInfo[i][j];
                if ((value & configMask) == 0) {
                	if (reverse) {
                		continue;
                	}
                } else {
                	if (!reverse) {
                		continue;
                	}
                }
                Rectangle rect = new Rectangle(j * cellSize, i * cellSize, cellSize, cellSize);
                viewer.map2screen(rect);
                gc.drawRectangle(rect);
            }
        }		
	}

	protected void drawCoordinate(GC gc) {
    	int cellSize = viewer.getMap().parent.getCellSize();
    	Point size = viewer.getSize();
        String coordStr = lastX + "," + lastY + "," + (lastX / cellSize) + "," + (lastY / cellSize);
        Point ts = gc.textExtent(coordStr);
        gc.setForeground(MapViewer.invert(viewer.getBackground()));
        gc.setBackground(viewer.getBackground());
        gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);		
	}

	protected void drawButtons(GC gc) {
    	Point size = viewer.getSize();
        gc.setBackground(viewer.getBackground());
        gc.setForeground(MapViewer.invert(viewer.getBackground()));
        int bx = 1;
        int i;
        for (i = 0; i < buttonTexts.length; i++) {
        	Point ts = gc.textExtent(buttonTexts[i]);
        	int by = size.y - ts.y - 8;
        	int bw = ts.x + 7;
        	int bh = ts.y + 6;
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
            if (i == selectedButtonIndex || i == selectedBrushSize + 1) {
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            }else{
            	gc.setForeground(MapViewer.invert(viewer.getBackground()));
            }
            gc.drawRectangle(buttonBounds[i]);
            gc.drawText(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4);
            if (i == selectedButtonIndex) {
                gc.setForeground(MapViewer.invert(viewer.getBackground()));
            }
            bx += bw + 2;
        }		
	}

	protected void drawMouseOverRect(GC gc) {
    	Point p = new Point(lastX, lastY);
    	map2cell(p);
    	int cellSize = viewer.getMap().parent.getCellSize();
    	int len = brushSize[selectedBrushSize - 1]*cellSize;
    	p.x -= brushSizeShift[selectedBrushSize - 1];
    	p.y -= brushSizeShift[selectedBrushSize - 1];
    	p.x *= cellSize;
    	p.y *= cellSize;
    	Rectangle rect = new Rectangle(p.x,p.y, len, len);
    	viewer.map2screen(rect);
    	gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
    	gc.drawRectangle(rect);
	}

	protected void drawFlyFoot(GC gc){
    	gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	Point p = new Point(lastX, lastY);
    	viewer.map2screen(p);
    	p.y -= flyEffectDY;
    	gc.fillArc(p.x - 15, p.y - 8, 30, 16, 0, 360);
    	gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
    	gc.fillArc(p.x - 13, p.y - 6, 26, 12, 0, 360);
    }
    /**
     * 键按下事件。
     */
    public void onKeyDown(int keyCode) {
    	switch(keyCode){
    	case SWT.F1:
    		for(byte[] b :	viewer.getMap().tileInfo){
    			Arrays.fill(b, (byte)0);
    		}
    		break;
    	}
    }
    
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
