package com.pip.image.workshop.editor;

import java.awt.Polygon;
import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopApplication;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mapeditor.MapEditor;
import com.pip.util.SWTUtils;
import com.pipimage.image.*;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display animation. The widget has two modes: editing and playing.
 * In editing mode, user can drag current frame; in playing mode, the frames is played
 * in sequence and user can't drag them.
 */
public class AnimateViewer extends AbstractImageViewer implements Runnable, PieceColorDialog.ColorChangeListener {
	protected int currentFrame;
	protected boolean isDragging;
	protected Point dragStartPoint;
	private Point dragStartPos;
	private Point lastPoint;
	private boolean playing;
	private Thread playThread;
	private int playTimer;
	private int[] visibleFrames;
	private Point refPoint = new Point(0, 0);
	private boolean isDraggingRefPoint;
	private boolean mouseInRefPointArea;
	protected PipAnimate refAnimate;
	protected Rectangle visibleArea;    // 新功能，可以截取动画中的一部分区域显示
	protected boolean showRefLines = true;
	protected boolean allowModify = true;
	private Menu popMenu;
	
	private ImageDrawCache cache = MapEditor.imageCache;
	private Image anchorButtonImage, colorButtonImage, rotateButtonImage, scaleButtonImage, scalexButtonImage, scaleyButtonImage;
	// 当前选中图块的操作按钮位置
	private Rectangle[] pieceButtonPos;
	// 是否正在拖动当前选中图块的操作按钮
	private boolean isDraggingPieceButton;
	// 拖动的图块操作按钮的索引
	private int draggingButtonIndex;
	// 开始拖动时的图块操作按钮位置
	private Rectangle[] draggingStartPieceButtonPos;
	// 开始拖动时的图块参数
	private int[] draggingStartPieceParam;
	
	public void setInput(PipAnimate input) {
		super.setInput(input);
		currentFrame = -1;
		isDragging = false;
		isDraggingPieceButton = false;
		this.stop();
		visibleFrames = new int[0];
		setMenu(popMenu);
	}
	
	public void setImageCache(ImageDrawCache cache) {
		this.cache = cache;
	}
	
	public void setAllowModify(boolean value) {
		this.allowModify = value;
	}
	
	public void setShowRefLines(boolean value) {
		showRefLines = value;
	}
	
	public void setVisibleArea(Rectangle rect) {
		visibleArea = rect;
	}
	
	public void setRefAnimate(PipAnimate input) {
		this.refAnimate = input;
	}
	
	public void setVisibleFrames(int[] arr) {
		visibleFrames = arr;
	}
	
	public void setCurrentFrame(int frame) {
		currentFrame = frame;
		isDragging = false;
	}
	
	public void play() {
		if (input == null || ((PipAnimate)input).getFrameCount() == 0) {
			return;
		}
		setInput(input);
		playing = true;
		playTimer = 0;
		playThread = new Thread(this);
		playThread.start();
	}
	
	public void stop() {
		playing = false;
		try {
			playThread.join();
		} catch (Exception e) {
		}
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public void run() {
		while (playing && input != null && playThread == Thread.currentThread()) {
			long preTime = System.currentTimeMillis();
			playTimer++;
			try {
			    Display display = getDisplay();
                display.asyncExec(new Runnable() {
                    public void run() {
                        try {
                        	if(!isDisposed())
                            redraw();
                        } catch (Exception e) {
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
            	long processTime = System.currentTimeMillis() - preTime;
            	if (processTime < Settings.animateFrameDelay) {
            		Thread.sleep(Settings.animateFrameDelay - processTime);
            	}
			} catch (Exception e) {
			}
		}
	}
	
	protected PipAnimateFrameRef getFrameData() {
		return ((PipAnimate)input).getFrame(currentFrame);
	}
	
	private Point calcCoord(Point p) {
		Point size = getSize();
		int x = (int)(fitGrid(p.x - size.x / 2 - paintOffset.x) / ratio);
		int y = (int)(fitGrid(p.y - size.y / 2 - paintOffset.y) / ratio);
		return new Point(x, y);
	}
	
    private boolean inRefPointAnchor(int x, int y) {
        Point size = getSize();
        int refX = (int)(refPoint.x * ratio) + size.x / 2 + paintOffset.x;
        int refY = (int)(refPoint.y * ratio) + size.y / 2 + paintOffset.y;
        if (x >= refX - 3 && x <= refX + 3 && y >= refY - 3 && y <= refY + 3) {
            return true;
        }
        return false;
    }	
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND, GLUtils.glEnabled);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
			    onMouseMove(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				onMouseDown(e);
			}
			public void mouseUp(MouseEvent e) {
				onMouseUp(e);
			}
		});
		
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("导出动画序列...") {
			public void run() {
				exportAnimate();
			}
		});
		if (popMenu != null) {
			popMenu.dispose();
		}
		popMenu = mgr.createContextMenu(this);
		anchorButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/anchor.gif");
		colorButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/color.gif");
		rotateButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/rotate.gif");
		scaleButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/scale.gif");
		scalexButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/scalex.gif");
		scaleyButtonImage = SWTResourceManager.getImage(AnimateFrameEditor.class, "/com/pip/image/workshop/editor/scaley.gif");
	}

	protected void onMouseMove(MouseEvent e) {
		if (playing || !allowModify) {
			return;
		}
		mouseInRefPointArea = inRefPointAnchor(e.x, e.y);
		if (isDragging && input != null && currentFrame != -1) {
			int offx = (int)((e.x - dragStartPoint.x) / ratio);
			int offy = (int)((e.y - dragStartPoint.y) / ratio);
			offx += dragStartPos.x;
			offy += dragStartPos.y;
			getFrameData().setDx(offx);
			getFrameData().setDy(offy);
		} else if (isDraggingRefPoint) {
            int offx = (int)((e.x - dragStartPoint.x) / ratio);
            int offy = (int)((e.y - dragStartPoint.y) / ratio);
            offx += dragStartPos.x;
            offy += dragStartPos.y;
            refPoint.x = offx;
            refPoint.y = offy;
        } else if (isDraggingPieceButton) {
        	PipAnimateFrameRef frameRef = getFrameData();
				
            // 拖动的点位置保存在draggingButtonIndex：0 - 基准点、1 - 旋转点、2 - 缩放、3 - x方向缩放、4 - y方向缩放
            // 开始拖动时图块的参数保存在draggingStartPieceParam;
            // 开始拖动时图块各按钮点的位置保存在draggingStartPieceButtonPos
            if (draggingButtonIndex == 1) {
            	// 旋转
            	double offx = e.x - dragStartPoint.x;
            	double offy = e.y - dragStartPoint.y;
            	double anglex = draggingStartPieceButtonPos[1].x + offx - draggingStartPieceButtonPos[0].x;
            	double angley = draggingStartPieceButtonPos[1].y + offy - draggingStartPieceButtonPos[0].y;
            	angley = -angley;
            	double dist = Math.sqrt(anglex * anglex + angley * angley);
            	if (dist > 0.00000001) {
            		// 计算新的角度0-360
                	double sin = angley / dist;
                	double angle = Math.asin(sin);
                	if (anglex < 0) {
                		angle = Math.PI - angle;
                	}
                	angle += Math.PI / 2;
                	int newAngle = (int)(angle * 180 / Math.PI);
                	newAngle = (newAngle + 720) % 360;
                	
                	// 计算老的角度0-360
                	int oldAngle = ((frameRef.rotate % 360) + 360) % 360;
                	
                	// 计算拖动偏移角度
                	int angleDist = (((newAngle - oldAngle) % 360) + 360) % 360;
                	
                	// 如果偏移角度<180，表示逆时针旋转，否则表示顺时针旋转
                	if (angleDist < 180) {
                		frameRef.rotate += angleDist;
                    	if ((e.stateMask & SWT.SHIFT) != 0) {
                    		frameRef.rotate -= frameRef.rotate % 10;
                    	}
                	} else {
                		frameRef.rotate -= 360 - angleDist;
                    	if ((e.stateMask & SWT.SHIFT) != 0) {
                    		frameRef.rotate -= frameRef.rotate % 10;
                    	}
                	}
            	}
            } else if (draggingButtonIndex == 2) {
            	// 水平和垂直方向同时缩放
            	double oldoffx = draggingStartPieceButtonPos[2].x - draggingStartPieceButtonPos[0].x;
            	double oldoffy = draggingStartPieceButtonPos[2].y - draggingStartPieceButtonPos[0].y;
            	double newoffx = e.x - draggingStartPieceButtonPos[0].x;
            	double newoffy = e.y - draggingStartPieceButtonPos[0].y;
            	double olddist = Math.sqrt(oldoffx * oldoffx + oldoffy * oldoffy);
            	double newdist = Math.sqrt(newoffx * newoffx + newoffy * newoffy);
            	if (olddist < 0.01) {
            		olddist = 0.01;
            	}
            	if (newdist < 0.01) {
            		newdist = 0.01;
            	}
            	frameRef.scalex = (int)(draggingStartPieceParam[1] * newdist / olddist);
            	if ((e.stateMask & SWT.SHIFT) != 0) {
            		frameRef.scalex -= frameRef.scalex % 10;
            	}
            	if (frameRef.scalex < 1) {
            		frameRef.scalex = 1;
            	}
            	frameRef.scaley = (int)(draggingStartPieceParam[2] * newdist / olddist);
            	if ((e.stateMask & SWT.SHIFT) != 0) {
            		frameRef.scaley -= frameRef.scaley % 10;
            	}
            	if (frameRef.scaley < 1) {
            		frameRef.scaley = 1;
            	}
            } else if (draggingButtonIndex == 3) {
            	// 水平方向缩放
            	double oldoffx = draggingStartPieceButtonPos[3].x - draggingStartPieceButtonPos[0].x;
            	double oldoffy = draggingStartPieceButtonPos[3].y - draggingStartPieceButtonPos[0].y;
            	double newoffx = e.x - draggingStartPieceButtonPos[0].x;
            	double newoffy = e.y - draggingStartPieceButtonPos[0].y;
            	double olddist = Math.sqrt(oldoffx * oldoffx + oldoffy * oldoffy);
            	double newdist = Math.sqrt(newoffx * newoffx + newoffy * newoffy);
            	if (olddist < 0.01) {
            		olddist = 0.01;
            	}
            	if (newdist < 0.01) {
            		newdist = 0.01;
            	}
            	frameRef.scalex = (int)(draggingStartPieceParam[1] * newdist / olddist);
            	if ((e.stateMask & SWT.SHIFT) != 0) {
            		frameRef.scalex -= frameRef.scalex % 10;
            	}
            	if (frameRef.scalex < 1) {
            		frameRef.scalex = 1;
            	}
            } else if (draggingButtonIndex == 4) {
            	// 垂直方向缩放
            	double oldoffx = draggingStartPieceButtonPos[4].x - draggingStartPieceButtonPos[0].x;
            	double oldoffy = draggingStartPieceButtonPos[4].y - draggingStartPieceButtonPos[0].y;
            	double newoffx = e.x - draggingStartPieceButtonPos[0].x;
            	double newoffy = e.y - draggingStartPieceButtonPos[0].y;
            	double olddist = Math.sqrt(oldoffx * oldoffx + oldoffy * oldoffy);
            	double newdist = Math.sqrt(newoffx * newoffx + newoffy * newoffy);
            	if (olddist < 0.01) {
            		olddist = 0.01;
            	}
            	if (newdist < 0.01) {
            		newdist = 0.01;
            	}
            	frameRef.scaley = (int)(draggingStartPieceParam[2] * newdist / olddist);
            	if ((e.stateMask & SWT.SHIFT) != 0) {
            		frameRef.scaley -= frameRef.scaley % 10;
            	}
            	if (frameRef.scaley < 1) {
            		frameRef.scaley = 1;
            	}
            }
		}
		lastPoint = calcCoord(new Point(e.x, e.y));
		redraw();		
	}

	protected void onMouseUp(MouseEvent e) {
		if (isInButtonArea(e.x, e.y)) {
			return;
		}
		if (playing || !allowModify) {
			return;
		}
		if (e.button == 1) {
			if (isDragging && input != null && currentFrame != -1) {
				isDragging = false;
				redraw();
				int offx = (int)((e.x - dragStartPoint.x) / ratio);
				int offy = (int)((e.y - dragStartPoint.y) / ratio);
				if (offx != 0 || offy != 0) {
					fireContentChanged();
				}
			} else if (isDraggingPieceButton) {
				isDraggingPieceButton = false;
				redraw();
				int offx = (int)((e.x - dragStartPoint.x) / ratio);
				int offy = (int)((e.y - dragStartPoint.y) / ratio);
				if (offx != 0 || offy != 0) {
					fireContentChanged();
				}
			}
			if (isDraggingRefPoint) {
                isDraggingRefPoint = false;
                redraw();
            }
		}		
	}

	protected void onMouseDown(MouseEvent e) {
		if (isInButtonArea(e.x, e.y)) {
			return;
		}
		if (playing || !allowModify) {
			return;
		}
		
		if (e.button == 1) {
		    if (inRefPointAnchor(e.x, e.y)) {
                isDraggingRefPoint = true;
                dragStartPoint = new Point(e.x, e.y);
                dragStartPos = new Point(refPoint.x, refPoint.y);
                redraw();
                return;
            }
		    if (pieceButtonPos != null) {
		    	for (int i = 1; i < pieceButtonPos.length; i++) {
		    		if (pieceButtonPos[i].contains(e.x, e.y)) {
		    			if (i == 5) {
		    				// 这是颜色按钮
		    				PipAnimateFrameRef frameRef = getFrameData();
		    				int initColor = frameRef.color;
		    				int clr = PieceColorDialog.choose(initColor, AnimateViewer.this);
		    				frameRef.color = clr;
		    				redraw();
		    				if (clr != initColor) {
		    					fireContentChanged();
		    				}
		    				return;
		    			}
		    			isDraggingPieceButton = true;
		    			dragStartPoint = new Point(e.x, e.y);
		    			draggingStartPieceButtonPos = pieceButtonPos;
		    			PipAnimateFrameRef frameRef = getFrameData();
		    			draggingStartPieceParam = new int[] { frameRef.rotate, frameRef.scalex, frameRef.scaley };
		    			draggingButtonIndex = i;
		    			redraw();
		    			return;
		    		}
		    	}
		    }
		    if (input != null && currentFrame != -1) {
				isDragging = true;
				dragStartPoint = new Point(e.x, e.y);
				dragStartPos = new Point(getFrameData().getDx(), getFrameData().getDy()); 
				redraw();
		    }
		}		
	}
	
	protected void drawRefLine(GC gc) {
		if (!showRefLines) {
			return;
		}
		Point size = getSize();
		gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
        int refX = (int)(refPoint.x * ratio);
        int refY = (int)(refPoint.y * ratio);
        gc.drawLine(0, size.y / 2 + refY + paintOffset.y, size.x, size.y / 2 + refY + paintOffset.y);
        gc.drawLine(size.x / 2 + refX + paintOffset.x, 0, size.x / 2 + refX + paintOffset.x, size.y);
        if (isDraggingRefPoint || (mouseInRefPointArea && !isDragging && !playing && allowModify)) {
            gc.drawRectangle(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
        }
	}

	protected void drawRefLine(GLGraphics gc) {
		if (!showRefLines) {
			return;
		}
		Point size = getSize();
		gc.setColor(0xFF, 0xFF, 0x99, 0x00);
        int refX = (int)(refPoint.x * ratio);
        int refY = (int)(refPoint.y * ratio);
        gc.drawLine(0, size.y / 2 + refY + paintOffset.y, size.x, size.y / 2 + refY + paintOffset.y);
        gc.drawLine(size.x / 2 + refX + paintOffset.x, 0, size.x / 2 + refX + paintOffset.x, size.y);
        if (isDraggingRefPoint || (mouseInRefPointArea && !isDragging && !playing && allowModify)) {
            gc.drawRect(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
        }
	}
	
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
        drawRefLine(gc);
        
		if (input == null || currentFrame == -1) {
			drawScreenFrame(gc);
			return;
		} else {
			if (!playing && allowModify) {
				drawSelection(gc);
				drawScreenFrame(gc);
			} else {
				drawScreenFrame(gc);
				return;
			}
		}
		
		gc.setForeground(invert(getBackground()));
		PipAnimateFrameRef frameRef = getFrameData();
		if (frameRef != null) {
			PipAnimateFrame frame = frameRef.realize();
			if (frame == null) {
				return;
			}
			Rectangle fb = frame.getBounds();
			fb.x += frameRef.getDx();
			fb.y += frameRef.getDy();
			String coordStr = fb.x + "," + fb.y + "," + fb.width + "," + fb.height;
			Point ts = gc.textExtent(coordStr);
			gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
            gc.drawText(coordStr, 4, size.y - ts.y - 5);
		}
		
		String coordStr;
        if (isDraggingRefPoint) {
            coordStr = refPoint.x + "," + refPoint.y;
        } else {
            return;
        }
        Point ts = gc.textExtent(coordStr);
        gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
	}
	
	protected void drawInformation(GLGraphics gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
        drawRefLine(gc);
        
		if (input == null || currentFrame == -1) {
			drawScreenFrame(gc);
			return;
		} else {
			if (!playing && allowModify) {
				drawSelection(gc);
				drawScreenFrame(gc);
			} else {
				drawScreenFrame(gc);
				return;
			}
		}
		
		Color clr = invert(getBackground());
		gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
		PipAnimateFrameRef frameRef = getFrameData();
		if (frameRef != null) {
			PipAnimateFrame frame = frameRef.realize();
			if (frame == null) {
				return;
			}
			Rectangle fb = frame.getBounds();
			fb.x += frameRef.getDx();
			fb.y += frameRef.getDy();
			String coordStr = fb.x + "," + fb.y + "," + fb.width + "," + fb.height;
			int tw = gc.stringWidth(coordStr);
			int th = gc.getFontHeight();
			gc.drawRect(1, size.y - th - 8, tw + 7, th + 6);
            gc.drawString(coordStr, 4, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
		}
		
		String coordStr;
        if (isDraggingRefPoint) {
            coordStr = refPoint.x + "," + refPoint.y;
        } else {
            return;
        }
        int tw = gc.stringWidth(coordStr);
		int th = gc.getFontHeight();
        gc.drawRect(size.x - tw - 9, size.y - th - 8, tw + 7, th + 6);
        gc.drawString(coordStr, size.x - tw - 5, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
	}

	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		
		// 如果指定了剪切区域，把剪切区域调整到画面中央
		int offx2 = 0;
		int offy2 = 0;
		if (visibleArea != null) {
			int cx = visibleArea.x + visibleArea.width / 2;
			int cy = visibleArea.y + visibleArea.height / 2;
			gc.setClipping((int)(size.x / 2 - (visibleArea.width / 2) * ratio + paintOffset.x),
					(int)(size.y / 2 - (visibleArea.height / 2) * ratio + paintOffset.y),
					(int)(visibleArea.width * ratio), (int)(visibleArea.height * ratio));
			offx2 = (int)(-cx * ratio);
			offy2 = (int)(-cy * ratio);
		}
		
		// 先画背景帧
		int dx, dy;
		PipAnimateFrameRef frameRef;
		PipAnimateFrame frame;
		if (!isPlaying()) {
			for (int i = 0; i < visibleFrames.length; i++) {
				PipAnimate ani = (PipAnimate)input;
				if (visibleFrames[i] >= ani.getFrameCount()) {
					continue;
				}
				
				if (refAnimate != null) {
					int t = ani.getTimeOfFrame(visibleFrames[i]);
					refAnimate.drawAnimateFrame(gc, t, size.x / 2 + paintOffset.x + offx2,
							size.y / 2 + paintOffset.y + offy2, ratio, cache);
				}
				
				frameRef = ani.getFrame(visibleFrames[i]);
				frameRef.draw(gc, size.x / 2 + paintOffset.x + offx2, size.y / 2 + paintOffset.y + offy2, ratio, cache);
			}
		}
		if (playing) {
			if (refAnimate != null && currentFrame != -1) {
				int t = ((PipAnimate)input).getTimeOfFrame(currentFrame);
				refAnimate.drawAnimateFrame(gc, t, size.x / 2 + paintOffset.x + offx2,
						size.y / 2 + paintOffset.y + offy2, ratio, cache);
			}
			((PipAnimate)input).drawAnimateFrame(gc, playTimer, size.x / 2 + paintOffset.x + offx2,
					size.y / 2 + paintOffset.y + offy2, ratio, cache);
		} else if (currentFrame != -1) {
			// 最后画选中帧
			
			if (refAnimate != null) {
				int t = ((PipAnimate)input).getTimeOfFrame(currentFrame);
				refAnimate.drawAnimateFrame(gc, t, size.x / 2 + paintOffset.x + offx2,
						size.y / 2 + paintOffset.y + offy2, ratio, cache);
			}
			
			frameRef = getFrameData();
			frame = frameRef.realize();
			frameRef.draw(gc, size.x / 2 + paintOffset.x + offx2, size.y / 2 + paintOffset.y + offy2, ratio, cache);
		}
		if (visibleArea != null) {
			gc.setClipping(0, 0, size.x, size.y);
		}
	}
	
	protected void paintInput(GLGraphics gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		
		gc.setZ(0);
		
		// 如果指定了剪切区域，把剪切区域调整到画面中央
		int offx2 = 0;
		int offy2 = 0;
		if (visibleArea != null) {
			int cx = visibleArea.x + visibleArea.width / 2;
			int cy = visibleArea.y + visibleArea.height / 2;
			gc.setClip((int)(size.x / 2 - (visibleArea.width / 2) * ratio + paintOffset.x),
					(int)(size.y / 2 - (visibleArea.height / 2) * ratio + paintOffset.y),
					(int)(visibleArea.width * ratio), (int)(visibleArea.height * ratio));
			offx2 = (int)(-cx * ratio);
			offy2 = (int)(-cy * ratio);
		}
		
		// 先画背景帧
		int dx, dy;
		PipAnimateFrameRef frameRef;
		PipAnimateFrame frame;
		if (!isPlaying()) {
			for (int i = 0; i < visibleFrames.length; i++) {
				PipAnimate ani = (PipAnimate)input;
				if (visibleFrames[i] >= ani.getFrameCount()) {
					continue;
				}
				
				if (refAnimate != null) {
					int t = ani.getTimeOfFrame(visibleFrames[i]);
					refAnimate.drawAnimateFrame(gc, t, size.x / 2 + paintOffset.x + offx2,
							size.y / 2 + paintOffset.y + offy2, ratio, cache);
				}
				
				frameRef = ani.getFrame(visibleFrames[i]);
				frame = frameRef.realize();
				frameRef.draw(gc, size.x / 2 + paintOffset.x + offx2, size.y / 2 + paintOffset.y + offy2, ratio, cache);
			}
		}
		if (playing) {
			if (refAnimate != null && currentFrame != -1) {
				int t = ((PipAnimate)input).getTimeOfFrame(currentFrame);
				refAnimate.drawAnimateFrame(gc, t, size.x / 2 + paintOffset.x + offx2,
						size.y / 2 + paintOffset.y + offy2, ratio, cache);
			}
			((PipAnimate)input).drawAnimateFrame(gc, playTimer, size.x / 2 + paintOffset.x + offx2,
					size.y / 2 + paintOffset.y + offy2, ratio, cache);
		} else if (currentFrame != -1) {
			// 最后画选中帧
			
			if (refAnimate != null) {
				int t = ((PipAnimate)input).getTimeOfFrame(currentFrame);
				refAnimate.drawAnimateFrame(gc, t, size.x / 2 + paintOffset.x + offx2,
						size.y / 2 + paintOffset.y + offy2, ratio, cache);
			}
		
			frameRef = getFrameData();
			frame = frameRef.realize();
			frameRef.draw(gc, size.x / 2 + paintOffset.x + offx2, size.y / 2 + paintOffset.y + offy2, ratio, cache);
		}
		if (visibleArea != null) {
			gc.setClip(0, 0, size.x, size.y);
		}
	}
	
	protected void drawScreenFrame(GC gc) {
		if (!showRefLines) {
			return;
		}
		Point size = getSize();
		int sw = Settings.screenWidth;
		int sh = Settings.screenHeight;
		Rectangle screen = new Rectangle(-sw / 2, -sh / 2, sw, sh);
		screen = zoom(screen);
		screen.x += size.x / 2 + paintOffset.x;
		screen.y += size.y / 2 + paintOffset.y;
		gc.setForeground(invert(gc.getBackground()));
		gc.drawRectangle(screen);
	}
	
	protected void drawScreenFrame(GLGraphics gc) {
		if (!showRefLines) {
			return;
		}
		Point size = getSize();
		int sw = Settings.screenWidth;
		int sh = Settings.screenHeight;
		Rectangle screen = new Rectangle(-sw / 2, -sh / 2, sw, sh);
		screen = zoom(screen);
		screen.x += size.x / 2 + paintOffset.x;
		screen.y += size.y / 2 + paintOffset.y;
		Color clr = invert(getBackground());
		gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
		gc.drawRect(screen.x, screen.y, screen.width, screen.height);
	}
	
	protected void drawSelection(GC gc) {
		Point size = getSize();
		PipAnimateFrameRef frameRef = getFrameData();
		PipAnimateFrame frame = frameRef.realize();
		int dx = size.x / 2;
		int dy = size.y / 2;
		dx += frameRef.getDx() * ratio;
		dy += frameRef.getDy() * ratio;
		Rectangle frameRec = zoom(frame.getBounds());
		gc.setForeground(getDisplay().getSystemColor(isDragging ? SWT.COLOR_RED : SWT.COLOR_BLUE));
		gc.drawRectangle(frameRec.x + dx + paintOffset.x, frameRec.y + dy + paintOffset.y, frameRec.width, frameRec.height);
	}
	
	protected void drawSelection(GLGraphics gc) {
		PipAnimateFrameRef frameRef = getFrameData();
		PipAnimateFrame frame = frameRef.realize();
		pieceButtonPos = null;

		Point size = this.getSize();
		int dx = size.x / 2;
		int dy = size.y / 2;
		dx += frameRef.getDx() * ratio;
		dy += frameRef.getDy() * ratio;
		Color clr = getDisplay().getSystemColor(isDragging ? SWT.COLOR_RED : SWT.COLOR_BLUE);
		gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
		Rectangle frameRec = zoom(frame.getBounds());
		double[][] points = new double[][] {
				{ frameRec.x, frameRec.y, 1 },
				{ frameRec.x + frameRec.width, frameRec.y, 1 },
				{ frameRec.x + frameRec.width, frameRec.y + frameRec.height, 1 },
				{ frameRec.x, frameRec.y + frameRec.height, 1 },
				{ 0, 0, 1 },
				{ 0, 50, 1 },
				{ frameRec.x + frameRec.width, frameRec.y + frameRec.height, 1 },
				{ frameRec.x + frameRec.width, frameRec.y + frameRec.height / 2, 1 },
				{ frameRec.x + frameRec.width / 2, frameRec.y + frameRec.height, 1 },
		};
		points = GLUtils.mul(points, new double[][] {
				{ frameRef.scalex / 100.0, 0, 0 },
				{ 0, frameRef.scaley / 100.0, 0 },
				{ 0, 0, 1 }
		});
		points = GLUtils.rotate(points, frameRef.rotate);
		points = GLUtils.mul(points, new double[][] {
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ dx + paintOffset.x, dy + paintOffset.y, 1 }
		});
		gc.drawLine((int)points[0][0], (int)points[0][1], (int)points[1][0], (int)points[1][1]);
		gc.drawLine((int)points[1][0], (int)points[1][1], (int)points[2][0], (int)points[2][1]);
		gc.drawLine((int)points[2][0], (int)points[2][1], (int)points[3][0], (int)points[3][1]);
		gc.drawLine((int)points[3][0], (int)points[3][1], (int)points[0][0], (int)points[0][1]);

		// 绘制当前帧的参考点、旋转、缩放、颜色操作控件
		pieceButtonPos = new Rectangle[6];
		pieceButtonPos[0] = new Rectangle((int)points[4][0] - 8, (int)points[4][1] - 8, 16, 16);
		pieceButtonPos[1] = new Rectangle((int)points[5][0] - 8, (int)points[5][1] - 8, 16, 16);
		pieceButtonPos[2] = new Rectangle((int)points[6][0] - 8, (int)points[6][1] - 8, 16, 16);
		pieceButtonPos[3] = new Rectangle((int)points[7][0] - 8, (int)points[7][1] - 8, 16, 16);
		pieceButtonPos[4] = new Rectangle((int)points[8][0] - 8, (int)points[8][1] - 8, 16, 16);
		pieceButtonPos[5] = new Rectangle((int)points[4][0] - 8, (int)points[4][1] - 58, 16, 16);
		gc.drawTexture(GLUtils.loadImage(anchorButtonImage), 0, 0, pieceButtonPos[0].x, pieceButtonPos[0].y);
		gc.drawTexture(GLUtils.loadImage(rotateButtonImage), 0, 0, pieceButtonPos[1].x, pieceButtonPos[1].y);
		gc.drawTexture(GLUtils.loadImage(scaleButtonImage), 0, 0, pieceButtonPos[2].x, pieceButtonPos[2].y);
		gc.drawTexture(GLUtils.loadImage(scalexButtonImage), 0, 0, pieceButtonPos[3].x, pieceButtonPos[3].y);
		gc.drawTexture(GLUtils.loadImage(scaleyButtonImage), 0, 0, pieceButtonPos[4].x, pieceButtonPos[4].y);
		gc.drawTexture(GLUtils.loadImage(colorButtonImage), 0, 0, pieceButtonPos[5].x, pieceButtonPos[5].y);
		
		// 绘制当前拖动点的参数
		if (isDraggingPieceButton && draggingButtonIndex == 1) {
			String text = String.valueOf(frameRef.rotate);
			Point ts = gc.textExtent(text);
			gc.setColor(0xFF000000);
			gc.drawRect(pieceButtonPos[1].x - ts.x / 2 - 2, pieceButtonPos[1].y - 24, ts.x + 6, ts.y + 6);
			gc.drawText(text, pieceButtonPos[1].x - ts.x / 2 + 2, pieceButtonPos[1].y - 21);
		}
		if (isDraggingPieceButton && draggingButtonIndex == 2) {
			String text = String.valueOf(frameRef.scalex + "%/" + frameRef.scaley + "%");
			Point ts = gc.textExtent(text);
			gc.setColor(0xFF000000);
			gc.drawRect(pieceButtonPos[2].x - ts.x / 2 - 2, pieceButtonPos[2].y - 24, ts.x + 6, ts.y + 6);
			gc.drawText(text, pieceButtonPos[2].x - ts.x / 2 + 2, pieceButtonPos[2].y - 21);
		}
		if (isDraggingPieceButton && draggingButtonIndex == 3) {
			String text = String.valueOf(frameRef.scalex + "%");
			Point ts = gc.textExtent(text);
			gc.setColor(0xFF000000);
			gc.drawRect(pieceButtonPos[3].x - ts.x / 2 - 2, pieceButtonPos[3].y - 24, ts.x + 6, ts.y + 6);
			gc.drawText(text, pieceButtonPos[3].x - ts.x / 2 + 2, pieceButtonPos[3].y - 21);
		}
		if (isDraggingPieceButton && draggingButtonIndex == 4) {
			String text = String.valueOf(frameRef.scaley + "%");
			Point ts = gc.textExtent(text);
			gc.setColor(0xFF000000);
			gc.drawRect(pieceButtonPos[4].x - ts.x / 2 - 2, pieceButtonPos[4].y - 24, ts.x + 6, ts.y + 6);
			gc.drawText(text, pieceButtonPos[4].x - ts.x / 2 + 2, pieceButtonPos[4].y - 21);
		}
	}
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    if (playing) {
	        stop();
	    }
	    input = null;
	    if (popMenu != null) {
	    	popMenu.dispose();
	    }
	}
	
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (keyCode == 'l') {
			this.setShowRefLines(!showRefLines);
			return;
		}
		if (isDragging || input == null || currentFrame == -1) {
			return;
		}
		PipAnimateFrameRef ref = ((PipAnimate)input).getFrame(currentFrame);
		switch (keyCode) {
		case SWT.ARROW_UP:
			ref.setDy(ref.getDy() - 1);
			break;
		case SWT.ARROW_DOWN:
			ref.setDy(ref.getDy() + 1);
			break;
		case SWT.ARROW_LEFT:
			ref.setDx(ref.getDx() - 1);
			break;
		case SWT.ARROW_RIGHT:
			ref.setDx(ref.getDx() + 1);
			break;
		default:
			return;
		}
		redraw();
		fireContentChanged();
	}

    public Point getRefPoint() {
        return refPoint;
    }
    
    private void exportOneFrame(PipAnimateFrame frame, File f) throws Exception {
		Rectangle size = frame.getBounds();
		Image img = new Image(getShell().getDisplay(), size.width, size.height);
		GC gc = new GC(img);
		frame.draw(gc, -size.x, -size.y, 1.0f);
		gc.dispose();
		
		// 保存文件
		try {
	        PngEncoder enc = new PngEncoder(img);
	        FileOutputStream fos = new FileOutputStream(f);
	        enc.encode32(fos, false);
	        fos.close();
		} catch (Exception e) {
			throw e;
		} finally {
			img.dispose();
		}
	}
    
    public void exportAnimate() {
    	PipAnimate ani = (PipAnimate)input;
    	if (ani == null) {
    		return;
    	}
    	
		// 选择文件
		DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.SAVE);
		String path = dlg.open();
		if (path == null) {
			return;
		}
		
		// 创建内存图片
		try {
			for (int i = 0; i < ani.getFrameCount(); i++) {
				PipAnimateFrame af = ani.getFrame(i).realize();
				exportOneFrame(af, new File(path, "frame" + i + ".png"));
			}
	        MessageDialog.openInformation(getShell(), "导出", "导出成功！");
		} catch (Exception e) {
			SWTUtils.showError(getShell(), "错误", e);
		}
	}
    
    public void colorChanged(int newClr) {
		getFrameData().color = newClr;
		redraw();
	}
}
