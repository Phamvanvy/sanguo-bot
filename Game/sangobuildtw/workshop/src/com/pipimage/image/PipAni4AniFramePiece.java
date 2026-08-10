package com.pipimage.image;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
		frame = 0xFF;
	}
	
	public String toString(){
		return getImageID()+":"+name;
	}
	public void copyFrom(PipAnimateFramePiece hookBase) {
		this.name = "name missing";
		this.setImageID(hookBase.getImageID());
		this.dx = hookBase.getDx();
		this.dy = hookBase.getDy();
		this.frame = hookBase.getFrame();
	}
//	public void saveToXMLNode(Element parent){
//		Element hookEntry = new Element("hook");
//		hookEntry.addAttribute("name", name);
//		hookEntry.addAttribute("hookId", imageID+"");
//		parent.addContent(hookEntry);
//	}

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
			paintAnchor(g, ratio, xx, yy);
		}else{
			Long frameByTime = System.currentTimeMillis()/100;
			int aniFrame = animate.getFrameAtTime(frameByTime.intValue() & Integer.MAX_VALUE);
			animate.drawFrame(g, aniFrame, xx, yy, ratio);
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

	@Override
	public void enlarge() {
		// TODO Auto-generated method stub
		super.enlarge();
	}

	/**
	 * 使绘制的示例挂接图像<b>中心点</b>在实际点上(默认是图块的<b>左上角</b>在实际点上)
	 */
	@Override
	public int getDx() {
		return super.getDx() - this.width/2;
	}

	/**
	 * @see {@link #getDx()}
	 */
	@Override
	public int getDy() {
		return super.getDy() - this.height/2;
	}

	/**
	 * @see {@link #getDx()}
	 * @return
	 */
	public int getRealDx(){
		return super.getDx();
	}
	/**
	 * @see {@link #getDx()}
	 * @return
	 */
	public int getRealDy(){
		return super.getDy();
	}
	
	@Override
	public int getFrame() {
		// TODO Auto-generated method stub
		return super.getFrame();
	}

	@Override
	public int getHeight() {
		return height ;
	}

	@Override
	public PipImageData getImageData() {
		// TODO Auto-generated method stub
		return super.getImageData();
	}

	@Override
	public int getImageID() {
		// TODO Auto-generated method stub
		return super.getImageID();
	}

	@Override
	public PipAnimateFrame getParent() {
		// TODO Auto-generated method stub
		return super.getParent();
	}

	@Override
	public int getTransition() {
		// TODO Auto-generated method stub
		return super.getTransition();
	}

	@Override
	public int getWidth() {
		return width ;
	}

	@Override
	public void load(DataInputStream dis) throws IOException {
		// TODO Auto-generated method stub
		super.load(dis);
	}

//	@Override
//	public void save(DataOutputStream dos) throws IOException {
//		super.save(dos);
//		dos.writeUTF(name);
//	}

	@Override
	public void setDx(int dx) {
		this.dx = dx + this.width/2;
	}

	@Override
	public void setDy(int dy) {
		this.dy = dy + this.height/2;
	}

	@Override
	public void setFrame(int frame) {
		// TODO Auto-generated method stub
		super.setFrame(frame);
	}

	@Override
	public void setImageID(int imageID) {
		super.setImageID(imageID);
		setPaintColorIndex(imageID);
	}

	@Override
	public void setParent(PipAnimateFrame parent) {
		// TODO Auto-generated method stub
		super.setParent(parent);
	}

	@Override
	public void setTransition(int transition) {
		// TODO Auto-generated method stub
		super.setTransition(transition);
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

}
