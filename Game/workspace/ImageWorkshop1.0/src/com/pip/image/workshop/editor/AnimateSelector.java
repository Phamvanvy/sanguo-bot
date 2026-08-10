package com.pip.image.workshop.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mapeditor.BlurMapUtil;
import com.pipimage.image.*;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to choose an animate sequence from an animate set.
 */
public class AnimateSelector extends AbstractImageViewer implements Runnable {
	private Rectangle[] frameLayout;
	private Rectangle[] frameBounds;
	private int selectedFrame;
	private int selectingFrame;

    private int hoverFrame;
	private int currentTime;
	private boolean disposed = false;
	private Display display;
	
	public void setInput(Object input) {
		super.setInput(input);
		hoverFrame = -1;
		selectedFrame = -1;
		currentTime = 0;
		frameLayout = null;
		frameBounds = null;
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateSelector(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				hoverFrame = calcPointFrame(new Point(e.x, e.y));
				redraw();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectingFrame && newFrame != -1) {
						setSelectedIndex(newFrame);
						fireFrameSelectionChanged(selectedFrame);
					}
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
			}
		});
		display = getDisplay();
        new Thread(this).start();
	}
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		getBestLayout();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2;
		
		PipAnimateSet animateSet = (PipAnimateSet)input;
		for (int i = 0; i < animateSet.getAnimateCount(); i++) {
			Rectangle rect = zoom(frameLayout[i + 1]);
			rect.x += offx + paintOffset.x;
			rect.y += offy + paintOffset.y;
			
			// 绘制动画
			int animateX = rect.x - (int)(frameBounds[i].x * ratio);
			int animateY = rect.y - (int)(frameBounds[i].y * ratio);
			PipAnimate animate = animateSet.getAnimate(i);
			int animateFrame = animate.getFrameAtTime(currentTime);
			animate.drawFrame(gc, animateFrame, animateX, animateY, ratio, null);
			
			// 画框
			if (i == selectedFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				gc.drawRectangle(rect);
			}
			if (i == hoverFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				gc.drawRectangle(rect);
			}
		}
	}
	
	// 计算鼠标点到的位置的动画
	private int calcPointFrame(Point p) {
        getBestLayout();
		if (frameLayout == null) {
			return -1;
		}
		Point size = getSize();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2 + paintOffset.x;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2 + paintOffset.y;
		for (int i = 1; i < frameLayout.length; i++) {
			int dx = (int)(frameLayout[i].x * ratio) + offx;
			int dy = (int)(frameLayout[i].y * ratio) + offy;
			int dw = (int)(frameLayout[i].width * ratio);
			int dh = (int)(frameLayout[i].height * ratio);
			if (new Rectangle(dx, dy, dw, dh).contains(p)) {
				return i - 1;
			}
		}
		return -1;
	}
	
	// 计算最佳布局
	private void getBestLayout() {
	    if (frameLayout != null) {
	        return;
	    }
	    if (input == null) {
	        return;
	    }
	    PipAnimateSet animateSet = (PipAnimateSet)input;
		int count = animateSet.getAnimateCount();
		
		// 计算所有动画占用的大小
		frameBounds = new Rectangle[count];
		for (int i = 0; i < count; i++) {
	        frameBounds[i] = animateSet.getAnimate(i).getBounds();
		}
		
		// 计算最佳布局
		frameLayout = getBestLayout(getSize(), frameBounds);
	}
	
	public int getSelectedIndex() {
		return selectedFrame;
	}

	public void setSelectedIndex(int index) {
	    this.selectedFrame = index;
	    redraw();
	}
	
	public int getFrameCount() {
		if (input == null) {
			return 0;
		}
		return ((PipAnimateSet)input).getAnimateCount();
	}
	
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (input != null) {
			switch (keyCode) {
			case SWT.ARROW_UP:
			case SWT.ARROW_LEFT:
				this.selectedFrame--;
				if (this.selectedFrame < 0) {
					this.selectedFrame = getFrameCount() - 1;
				}
				break;
			case SWT.ARROW_DOWN:
			case SWT.ARROW_RIGHT:
				this.selectedFrame++;
				if (this.selectedFrame >= getFrameCount()) {
					this.selectedFrame = 0;
					if (this.selectedFrame >= getFrameCount()) {
						this.selectedFrame = -1;
					}
				}
				break;
			default:
				break;
			}
			redraw();
		}
	}

	/**
	 * 动画播放到下一帧。
	 */
	public void step() {
	    currentTime++;
	}

    public void widgetDisposed(DisposeEvent e) {
        super.widgetDisposed(e);
        this.disposed = true;
    }
    
    public void run() {
        while (!disposed) {
            long preTime = System.currentTimeMillis();
            step();
            if (disposed) {
                break;
            }
            display.asyncExec(new Runnable() {
                public void run() {
                    redraw();
                }
            });
            try {
            	long processTime = System.currentTimeMillis() - preTime;
            	if (processTime < Settings.animateFrameDelay) {
            		Thread.sleep(Settings.animateFrameDelay - processTime);
            	}
			} catch (Exception e) {
			}
        }
    }
}
