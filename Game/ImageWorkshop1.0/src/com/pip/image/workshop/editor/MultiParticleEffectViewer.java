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
 * C版本粒子效果播放器，可同时播放多个粒子效果。
 */
public class MultiParticleEffectViewer extends AbstractImageViewer implements Runnable {
	private Point lastPoint;
	
	private ParticleEffectPlayer[] particleEffects;
	private Image backgroundImage;
	protected boolean playing = true;
	protected int playTimer = 0;
	
	private Rectangle[] effectBounds;
	private int displayWidth, displayHeight;
	private int selectedFrame = -1;
	private int hoverFrame = -1;
	
	private Display display;
	
	public void setBackgroundImage(Image img) {
		backgroundImage = img;
		redraw();
	}

	public void setInput(ParticleEffectPlayer[] input) {
		super.setInput(input);
		particleEffects = input;
		playTimer = 0;
		selectedFrame = -1;
		hoverFrame = -1;
		
		// 计算最佳的播放位置布局。每一个粒子效果占用320x240的区域
		if (particleEffects == null || particleEffects.length == 0) {
			effectBounds = new Rectangle[0];
			displayWidth = 0;
			displayHeight = 0;
			return;
		}
		int rows = (int)Math.sqrt(particleEffects.length);
		int cols = (particleEffects.length + rows - 1) / rows;
		displayWidth = cols * 320;
		displayHeight = rows * 240;
		effectBounds = new Rectangle[particleEffects.length];
		for (int i = 0; i < particleEffects.length; i++) {
			int row = i / cols;
			int col = i % cols;
			effectBounds[i] = new Rectangle(col * 320 - displayWidth / 2, row * 240 - displayHeight / 2, 320, 240);
		}
	}
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public MultiParticleEffectViewer(Composite parent, int style) {
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
		display = getDisplay();
		new Thread(this).start();
	}
	
	public void run() {
		while (playing) {
			playTimer++;
            try {
        		Thread.sleep(Settings.animateFrameDelay);
			} catch (Exception e) {
			}
            display.asyncExec(new Runnable() {
                public void run() {
                    try {
                        redraw();
                    } catch (Exception e) {
                    }
                }
            });
		}
	}
	
	private Point calcCoord(Point p) {
		Point size = getSize();
		int x = (int)((p.x - size.x / 2 - paintOffset.x) / ratio);
		int y = (int)((p.y - size.y / 2 - paintOffset.y) / ratio);
		return new Point(x, y);
	}
	
	private int calcSelection(Point p) {
		for (int i = 0; effectBounds != null && i < effectBounds.length; i++) {
			if (effectBounds[i].contains(p)) {
				return i;
			}
		}
		return -1;
	}

	protected void onMouseMove(MouseEvent e) {
		lastPoint = calcCoord(new Point(e.x, e.y));
		hoverFrame = calcSelection(lastPoint);
		redraw();
	}

	protected void onMouseUp(MouseEvent e) {
		if (isInButtonArea(e.x, e.y)) {
			return;
		}
		if (e.button == 1) {
			selectedFrame = hoverFrame;
			redraw();
		}		
	}

	protected void onMouseDown(MouseEvent e) {
		if (isInButtonArea(e.x, e.y)) {
			return;
		}
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
		if (input == null) {
			return;
		}
		gc.setZ(10000.0f);
		int basex = (int)((size.x / 2 + paintOffset.x) / ratio);
		int basey = (int)((size.y / 2 + paintOffset.y) / ratio);
		gc.setScale((float)ratio);
		for (int i = 0; i < particleEffects.length; i++) {
			int effx = effectBounds[i].x + effectBounds[i].width / 2;
			int effy = effectBounds[i].y + effectBounds[i].height / 2;
			synchronized (particleEffects[i].manager) {
				particleEffects[i].setPosition(basex + effx, basey + effy);
				particleEffects[i].draw(gc.getHandle(), 0, 0);
			}
		}
		gc.setScale(1.0f);
		
		drawSelection(gc);
	}
	
	protected void drawSelection(GLGraphics gc) {
		Point size = getSize();
		int basex = size.x / 2 + paintOffset.x;
		int basey = size.y / 2 + paintOffset.y;
		if (selectedFrame != -1) {
			Rectangle rect = zoom(effectBounds[selectedFrame]);
			rect.x += basex;
			rect.y += basey;
			gc.setColor(0x0000FF);
			gc.drawRect(rect);
		}
		if (hoverFrame != -1) {
			Rectangle rect = zoom(effectBounds[hoverFrame]);
			rect.x += basex;
			rect.y += basey;
			gc.setColor(0xFF0000);
			gc.drawRect(rect);
		}
	}
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    playing = false;
	    input = null;
	}
	
	public int getSelectedFrame() {
		return selectedFrame;
	}
}
