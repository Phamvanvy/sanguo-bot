package com.pip.image.workshop.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.pip.image.workshop.Settings;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mango.jni.ParticleEffectPlayer;
import com.pip.mapeditor.MapEditor;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipParticle;
import com.swtdesigner.SWTResourceManager;

/**
 * C版本粒子效果播放器，一次只能播放一个粒子效果。
 */
public class ParticleEffectViewer2 extends AbstractImageViewer implements Runnable {
	private Point refPoint = new Point(0, 0);
	private boolean isDraggingRefPoint;
	private boolean mouseInRefPointArea;
	protected Point dragStartPoint;
	private Point dragStartPos;
	private Point lastPoint;
	private ParticleEffectPlayer particleEffect;
	private Image backgroundImage;
	protected PipAnimate refAnimate;
	protected boolean playing = true;
	protected int playTimer = 0;
	
	public void setBackgroundImage(Image img) {
		backgroundImage = img;
		redraw();
	}

	public void setRefAnimate(PipAnimate input) {
		this.refAnimate = input;
	}
	
	public void setInput(ParticleEffectPlayer input) {
		super.setInput(input);
		particleEffect = input;
		playTimer = 0;
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
	public ParticleEffectViewer2(Composite parent, int style) {
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
		new Thread(this).start();
	}
	
	public void run() {
		while (playing) {
			playTimer++;
            try {
        		Thread.sleep(Settings.animateFrameDelay);
			} catch (Exception e) {
			}
		}
	}

	protected void onMouseMove(MouseEvent e) {
		mouseInRefPointArea = inRefPointAnchor(e.x, e.y);
		if (isDraggingRefPoint) {
            int offx = (int)((e.x - dragStartPoint.x) / ratio);
            int offy = (int)((e.y - dragStartPoint.y) / ratio);
            offx += dragStartPos.x;
            offy += dragStartPos.y;
            refPoint.x = offx;
            refPoint.y = offy;
        }
		lastPoint = calcCoord(new Point(e.x, e.y));
		redraw();		
	}

	protected void onMouseUp(MouseEvent e) {
		if (isInButtonArea(e.x, e.y)) {
			return;
		}
		if (e.button == 1) {
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
		if (e.button == 1) {
		    if (inRefPointAnchor(e.x, e.y)) {
                isDraggingRefPoint = true;
                dragStartPoint = new Point(e.x, e.y);
                dragStartPos = new Point(refPoint.x, refPoint.y);
                redraw();
                return;
            }
		}		
	}

	protected void drawInformation(GC gc) {
		super.drawInformation(gc);

		Point size = getSize();
		gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
        int refX = (int)(refPoint.x * ratio);
        int refY = (int)(refPoint.y * ratio);
        gc.drawLine(0, size.y / 2 + refY + paintOffset.y, size.x, size.y / 2 + refY + paintOffset.y);
        gc.drawLine(size.x / 2 + refX + paintOffset.x, 0, size.x / 2 + refX + paintOffset.x, size.y);
        if (isDraggingRefPoint || mouseInRefPointArea) {
            gc.drawRectangle(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
        }

		drawScreenFrame(gc);
		
		gc.setForeground(invert(getBackground()));
		
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
		gc.setColor(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
        int refX = (int)(refPoint.x * ratio);
        int refY = (int)(refPoint.y * ratio);
        gc.drawLine(0, size.y / 2 + refY + paintOffset.y, size.x, size.y / 2 + refY + paintOffset.y);
        gc.drawLine(size.x / 2 + refX + paintOffset.x, 0, size.x / 2 + refX + paintOffset.x, size.y);
        if (isDraggingRefPoint || mouseInRefPointArea) {
            gc.drawRect(size.x / 2 + refX + paintOffset.x - 4, size.y / 2 + refY + paintOffset.y - 4, 8 , 8);
        }

		drawScreenFrame(gc);
		
		gc.setColor(invert(getBackground()));
		
		String coordStr;
        if (isDraggingRefPoint) {
            coordStr = refPoint.x + "," + refPoint.y;
        } else {
            return;
        }
        Point ts = gc.textExtent(coordStr);
        gc.drawRect(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
	}

	protected void paintInput(GC gc) {
	}
	
	protected void paintInput(GLGraphics gc) {
		Point size = getSize();
		if (backgroundImage != null) {
			int iw = backgroundImage.getBounds().width;
			int ih = backgroundImage.getBounds().height;
			gc.drawTexture(GLUtils.loadImage(backgroundImage), 0, 0, (int)(size.x / 2 - iw / 2 * ratio), 
					(int)(size.y / 2 - ih / 2 * ratio), 
					(int)(iw * ratio), (int)(ih * ratio));
		}
		if (refAnimate != null) {
			refAnimate.drawAnimateFrame(gc, playTimer, size.x / 2 + paintOffset.x , size.y / 2 + paintOffset.y, ratio, MapEditor.imageCache);
		}
		if (input == null) {
			return;
		}
		
		int dx = size.x / 2 + paintOffset.x;
		int dy = size.y / 2 + paintOffset.y;
		synchronized (particleEffect.manager) {
			gc.setZ(10000.0f);
			gc.setScale((float)ratio);
			particleEffect.setPosition((int)(dx / ratio), (int)(dy / ratio));
			particleEffect.draw(gc.getHandle(), 0, 0);
			gc.setScale(1.0f);
		}
	}
	
	private void drawScreenFrame(GC gc) {
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
	
	private void drawScreenFrame(GLGraphics gc) {
		Point size = getSize();
		int sw = Settings.screenWidth;
		int sh = Settings.screenHeight;
		Rectangle screen = new Rectangle(-sw / 2, -sh / 2, sw, sh);
		screen = zoom(screen);
		screen.x += size.x / 2 + paintOffset.x;
		screen.y += size.y / 2 + paintOffset.y;
		gc.setColor(invert(getBackground()));
		gc.drawRect(screen);
	}
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    playing = false;
	    input = null;
	}
	
    public Point getRefPoint() {
        return refPoint;
    }
}
