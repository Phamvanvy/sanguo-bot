package com.pip.mapeditor.tool;

import java.awt.event.KeyEvent;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import com.pip.mapeditor.MapTileSelector;
import com.pip.mapeditor.MapViewer;
import com.swtdesigner.SWTResourceManager;

public class RealPlayTool extends SkyPassableTool {

	public RealPlayTool(MapViewer viewer, MapTileSelector tv) {
		super(viewer, tv);
	}
	/** 地图坐标,不是屏幕坐标 */
	int roleX = -1, roleY = -1;
	@Override
	public void mouseDown(int x, int y, int mask) {
	}

	@Override
	public void mouseMove(int x, int y) {
	}

	@Override
	protected void drawFlyFoot(GC gc) {
		if(roleX<0){
			return;
		}
		Point p = new Point(roleX, roleY);
		viewer.map2screen(p);
		
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	gc.fillArc(p.x - 15, p.y - 8, 30, 16, 0, 360);
    	gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
    	gc.fillArc(p.x - 13, p.y - 6, 26, 12, 0, 360);
		
		p.y -= flyEffectDY;
		
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	gc.fillArc(p.x - 15, p.y - 8, 30, 16, 0, 360);
    	gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
    	gc.fillArc(p.x - 13, p.y - 6, 26, 12, 0, 360);
	}
	/**
     * 鼠标抬起事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
	@Override
	public void mouseUp(int x, int y, int mask) {
		roleX = x;
		roleY = y;
		viewer.redraw();
	}

	@Override
	public void onKeyDown(int keyCode) {
		int delta = 10;
		boolean hit = false;
		int remX = roleX;
		int remY = roleY;
		switch(keyCode){
		case SWT.ARROW_LEFT:
			roleX -= delta;
			hit = true;
			break;
		case SWT.ARROW_RIGHT:
			roleX += delta;
			hit = true;
			break;
		case SWT.ARROW_UP:
			roleY -= delta;
			hit = true;
			break;
		case SWT.ARROW_DOWN:
			roleY += delta;
			hit = true;
			break;
		}
		if(hit){
			int[] checkPoint = checkFlyBlock(remX, remY, roleX, roleY); 
			roleX = checkPoint[0];
			roleY = checkPoint[1];
			viewer.redraw();
		}
	}

	/**
	 * 检查角色移动的两点确定的线段和地图的飞行阻挡线段是否有交点<p>
	 * 有则返回交点坐标remX,remY; 无则返回x, y
	 * @param remX
	 * @param remY
	 * @param x
	 * @param y
	 * @return
	 */
	private int[] checkFlyBlock(int remX, int remY, int x, int y) {
		int[] ret = new int[]{x, y};
		int lineA[] = makeLineParam(remX, remY, x, y);
		int a1 = lineA[0], b1 = lineA[1], c1 = lineA[2];
		int len = Math.max(Math.abs(x-remX)+1, Math.abs(y-remY)+1);
		int [] yv = new int[len];
		int [] xv = new int[len];
		if(a1==0){//水平线
			Arrays.fill(yv, -c1);
			int baseX = Math.min(remX, x);
			for(int i=0; i<len; i++){
				xv[i] = baseX + i;
			}
		}else if(b1==0){//垂直线
			int baseY = Math.min(remY, y);
			Arrays.fill(xv, -c1);
			for(int i=0; i<len; i++){
				yv[i] = baseY + i;
			}
		}else{//斜线
			int baseX = Math.min(remX, x);
			int dx = Math.abs(remX - x);
			float k = -b1/a1;
			for(int i=0; i<len; i++	){
				xv[i] = baseX + Math.round(dx*i/len);
				yv[i] = Math.round(k*xv[i] - c1);
			}
		}
		return ret;
	}
	
	public boolean pointInRect(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
		boolean ret = true;
		int minX = Math.min(x1, x2);
//		if(x>Math.max(x1,x2) || x<Math.min(x1, x2) || y>Math.max(y1, y2) || y<Math.min(y2, y1)){
//			ret = false;
//		}
		return ret;
	}
	

}
