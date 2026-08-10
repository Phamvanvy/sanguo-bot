package com.pipimage.image;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.util.SWTUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.jdom.Element;

import com.swtdesigner.SWTResourceManager;

public class PipAni4AniFramePiece extends PipAnimateFramePiece {
	/**
	 * 在静态代码段里初始化
	 */
	public static Color[] paintColors;
	/**
	 * 占位示意图宽度
	 */
	private int width = 6;
	
	private int paintColorIndex;
	
	/**
	 * 是否绘制挂接点的准星,在装备编辑器界面用到;
	 */
	private boolean paintAnchor;
	/**
	 * 对于没有挂接的部分，是否绘制挂接点。
	 */
	private boolean paintEmptyAnchor = true;
	
	private Rectangle anchorRect = new Rectangle(0,0,0,0);
	
	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * 占位示意图高度
	 */
	private int height = 6;
	public String name = "新挂接点";
	private PipAnimate animate;
	private boolean paintBounds;
	private boolean paintAnchorBounds;

	public PipAni4AniFramePiece(PipAnimateFrame parent) {
		super(parent);
		frame = 0xFFFF;
	}
	
	public void setPaintEmptyAnchor(boolean value) {
		paintEmptyAnchor = value;
	}
	
	public String toString(){
		return getImageID()+":"+name;
	}
	public void copyFrom(PipAnimateFramePiece hookBase) {
		this.name = "name missing";
		this.setImageID(hookBase.getImageID());
		this.dx = hookBase.dx;
		this.dy = hookBase.dy;
		this.frame = hookBase.getFrame();
	}

	public PipAni4AniFramePiece(PipAnimateFrame parent, int iid, int ff) {
		super(parent, iid, ff);
	}

	@Override
	public Object clone() {
		PipAni4AniFramePiece newPiece = new PipAni4AniFramePiece(this.parent);
		newPiece.copyFrom(this);
		newPiece.name = this.name;
		return newPiece;
	}

	@Override
	public void draw(GC g, int x, int y, double ratio, ImageDrawCache cache) {
		int xx = (int) (dx * ratio + x);
		int yy = (int) (dy * ratio + y);
		if(animate == null){
			if (paintEmptyAnchor) {
				paintAnchor(g, ratio, xx, yy);
			}
		}else{
			Long frameByTime = System.currentTimeMillis()/100;
			animate.drawAnimateFrame(g, frameByTime.intValue() & Integer.MAX_VALUE, xx, yy, ratio, cache);
			if(paintBounds){
				Rectangle rect = animate.getBounds();
				rect.x *= ratio;
				rect.y *= ratio;
				rect.x += xx;
				rect.y += yy;
				rect.width *= ratio;
				rect.height *= ratio;
				g.setForeground(SWTResourceManager.getColor(0));
				g.drawRectangle(rect);
			}
			if(paintAnchor){
				paintAnchor(g, ratio, xx, yy);
			}
		}
	}
	
	@Override
	public void draw(GLGraphics g, int x, int y, double ratio, ImageDrawCache cache, int rotate2, int scalex2, int scaley2, int color) {
		int xx, yy;
		if (rotate2 != 0) {
			// 旋转
			double[][] pos = { { dx * ratio * scalex2 / 100.0, dy * ratio * scaley2 / 100.0 } };
			double angle = ((rotate2 % 360) + 360) % 360;
			angle = angle * Math.PI / 180;
			double sin = Math.sin(angle);
			double cos = Math.cos(angle);
			pos = GLUtils.mul(pos, new double[][] {
					{ cos, -sin, 0 },
					{ sin, cos, 0 },
					{ 0, 0, 1}
			});
			xx = (int)(pos[0][0] + x);
			yy = (int)(pos[0][1] + y);
		} else {
			xx = (int) (dx * ratio * scalex2 / 100.0 + x);
			yy = (int) (dy * ratio * scaley2 / 100.0 + y);
		}
		if(animate == null){
			if (paintEmptyAnchor) {
				paintAnchor(g, ratio, xx, yy);
			}
		}else{
			Long frameByTime = System.currentTimeMillis()/100;
			animate.drawAnimateFrame(g, frameByTime.intValue() & Integer.MAX_VALUE, xx, yy, ratio, cache);
			if(paintBounds){
				Rectangle rect = animate.getBounds();
				rect.x *= ratio;
				rect.y *= ratio;
				rect.x += xx;
				rect.y += yy;
				rect.width *= ratio;
				rect.height *= ratio;
				g.setColor(SWTResourceManager.getColor(0));
				g.drawRect(rect);
			}
			if(paintAnchor){
				paintAnchor(g, ratio, xx, yy);
			}
		}
	}
	
	private void paintAnchor(GC g, double ratio, int xx, int yy) {
		g.setBackground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		int w = (int)(width*ratio);
		int h = (int)(height*ratio);
		boolean adv = g.getAdvanced();
		anchorRect.x = xx - w/2;
		anchorRect.y = yy - h/2;
		anchorRect.width = w;
		anchorRect.height = h;
		int size = (int) (2*ratio);
		if(paintAnchorBounds){
			g.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			g.drawRoundRectangle(anchorRect.x, anchorRect.y, w, h, size, size);
		}else{
			g.setAlpha(150);
			g.fillRoundRectangle(anchorRect.x, anchorRect.y, w, h, size, size);
		}
		
		g.setForeground(paintColors[paintColorIndex]);
		g.drawLine(xx, yy - h/2, xx, yy+h/2);//竖线
		g.drawLine(xx - w/2, yy, xx + w/2, yy);//横线
		g.setAdvanced(adv);		
	}
	
	private void paintAnchor(GLGraphics g, double ratio, int xx, int yy) {
		g.setColor(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		int w = (int)(width*ratio);
		int h = (int)(height*ratio);
		//boolean adv = g.getAdvanced();
		anchorRect.x = xx - w/2;
		anchorRect.y = yy - h/2;
		anchorRect.width = w;
		anchorRect.height = h;
		int size = (int) (2*ratio);
		if(paintAnchorBounds){
			g.setColor(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			g.drawRoundRect(anchorRect.x, anchorRect.y, w, h, size, size);
		}else{
			g.setColor(0x90808080);
			g.fillRoundRect(anchorRect.x, anchorRect.y, w, h, size, size);
		}
		
		g.setColor(paintColors[paintColorIndex]);
		g.drawLine(xx, yy - h/2, xx, yy+h/2);//竖线
		g.drawLine(xx - w/2, yy, xx + w/2, yy);//横线
		//g.setAdvanced(adv);		
	}
	
	static{
		initPaintColors();
	}
	private static void initPaintColors() {
		String[] colorString = new String[]{
				"ff00ff","370101",
				"ff0000","000000",
				"001eff","535353",
				"00e4ff","00ff9c",
				"00ff0c","fff600",
				"ff6c00"};
		paintColors = new Color[colorString.length];
		for(int i=0; i<paintColors.length; i++){
			String s = colorString[i];
			paintColors[i] = new Color(Display.getCurrent(), SWTUtils.getRGB(Integer.parseInt(s, 16)));
		}
	}

	/**
	 * 使绘制的示例挂接图像<b>中心点</b>在实际点上(默认是图块的<b>左上角</b>在实际点上)
	 */
	@Override
	public int getDx() {
		if (animate == null) {
			return dx - this.width/2;
		} else {
			return dx + animate.getBounds().x;
		}
	}
	
	public int getRealDx() {
		return dx;
	}

	/**
	 * @see {@link #getDx()}
	 */
	@Override
	public int getDy() {
		if (animate == null) {
			return dy - this.height / 2;
		} else {
			return dy + animate.getBounds().y;
		}
	}
	
	public int getRealDy() {
		return dy;
	}

	@Override
	public int getHeight() {
		if (animate == null) {
			return height ;
		} else {
			return animate.getBounds().height;
		}
	}

	@Override
	public int getWidth() {
		if (animate == null) {
			return width ;
		} else {
			return animate.getBounds().width;
		}
	}

	@Override
	public void setDx(int dx) {
		if (animate == null) {
			this.dx = dx + this.width/2;
		} else {
			this.dx = dx - animate.getBounds().x;
		}
	}

	@Override
	public void setDy(int dy) {
		if (animate == null) {
			this.dy = dy + this.height/2;
		} else {
			this.dy = dy - animate.getBounds().y;
		}
	}

	@Override
	public void setImageID(int imageID) {
		super.setImageID(imageID);
		setPaintColorIndex(imageID);
	}

	synchronized public void bindAnimate(PipAnimate pa) {
		this.animate = pa;
	}

	synchronized public PipAnimate getBindAnimate(){
		return this.animate;
	}
	synchronized public boolean binded(){
		return this.animate != null;
	}
	synchronized public void unbind() {
		this.animate = null;
	}

	public void setRealDx(int v) {
		dx = v;
	}
	
	public void setRealDy(int v){
		dy = v;
	}

	public int getPaintColorIndex() {
		return paintColorIndex;
	}

	public void setPaintColorIndex(int paintColorIndex) {
		this.paintColorIndex = paintColorIndex%paintColors.length;
	}

	public void setPaintBounds(boolean b) {
		paintBounds = b;
	}

	public void setPaintAnchor(boolean paintAnchor) {
		this.paintAnchor = paintAnchor;
	}

	public Rectangle getAnchorRect() {
		return anchorRect;
	}

	public void setPaintAnchorBounds(boolean b) {
		paintAnchorBounds = b;
	}

	/**
	 * 取得缩放旋转后的4个顶点位置（相对于基准点）。
	 * @return
	 */
	public double[][] getBounds() {
		if (animate == null) {
			return new double[][] {
				{ dx - 8, dy - 8, 1 },
				{ dx + 8, dy - 8, 1 },
				{ dx + 8, dy + 8, 1 },
				{ dx - 8, dy + 8, 1 }
			};
		} else {
			Rectangle rect = animate.getBounds();
			return new double[][] {
					{ rect.x + dx, rect.y + dy, 1 },
					{ rect.x + rect.width + dx, rect.y + dy, 1},
					{ rect.x + rect.width + dx, rect.y + rect.height + dy, 1 },
					{ rect.x + dx, rect.y + rect.height + dy, 1 }
			};
		}
	}
}
