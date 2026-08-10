package com.pip.mapeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pip.image.workshop.editor.ImageViewer;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImageDraw;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * 参考点编辑器。
 */
public class RefPointEditor extends AbstractImageViewer implements Runnable {
    private Point currentPos;
	private Point lastPoint;
	private int currentTime;
	private boolean disposed;
	private Display display;
	
	private PipAnimate animate;
    private Rectangle animateBounds;
	private Thread animateThread;

	public void setInput(Object input) {
		super.setInput(input);
		animate = (PipAnimate)input;
		animateBounds = animate.getBounds();
		currentPos = new Point(-animateBounds.x, -animateBounds.y);
		setMenu(null);
		setCursor(ImageViewer.cursorCross);
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public RefPointEditor(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				Point pt = new Point(e.x, e.y);
				lastPoint = calcCoord(pt);
				redraw();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					Point pt = new Point(e.x, e.y);
					currentPos = calcCoord(pt);
					redraw();
				}
			}
		});

		display = this.getDisplay();
		animateThread = new Thread(this);
		animateThread.start();
	}

	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		
		if (input != null) {
		    Rectangle imgSize = animate.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawRectangle(drawX - 1 + paintOffset.x, drawY - 1 + paintOffset.y, zoomSize.width + 1, zoomSize.height + 1);

		    gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
		    int px = (int)(currentPos.x * ratio);
		    int py = (int)(currentPos.y * ratio);
		    px = drawX + px + paintOffset.x;
		    py = drawY + py + paintOffset.y;
		    gc.drawRectangle(px - 4, py - 4, 8, 8);
            gc.drawLine(px - 5, py, px + 5, py);
			gc.drawLine(px, py - 5, px, py + 5);
		}

		gc.setBackground(getBackground());
		gc.setForeground(invert(getBackground()));
        String selStr = currentPos.x + "," + currentPos.y;
        Point ts = gc.textExtent(selStr);
        gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(selStr, 4, size.y - ts.y - 5);
        if (lastPoint != null) {
            String coordStr = lastPoint.x + "," + lastPoint.y;
            ts = gc.textExtent(coordStr);
            gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
            gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
        }
	}
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input != null) {
			Rectangle imgSize = animate.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			int frame = animate.getFrameAtTime(currentTime);
			animate.drawFrame(gc, frame, drawX - zoomSize.x + paintOffset.x, drawY - zoomSize.y + paintOffset.y, ratio);
		}
	}
	
	private Point calcCoord(Point p) {
		Rectangle imgSize = animate.getBounds();
		Rectangle zoomSize = zoom(imgSize);
		Point size = getSize();
		zoomSize.x = (size.x - zoomSize.width) / 2 + paintOffset.x;
		zoomSize.y = (size.y - zoomSize.height) / 2 + paintOffset.y;
		int x = (int)(fitGrid(p.x - zoomSize.x) / ratio);
		int y = (int)(fitGrid(p.y - zoomSize.y) / ratio);
		return new Point(x, y);
	}
	
	public Point getOffset() {
	    return new Point(-animateBounds.x - currentPos.x, -animateBounds.y - currentPos.y);
	}
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    disposed = true;
	    try {
	        this.animateThread.join();
	    } catch (Exception e1) {
	    }
	}

	// 驱动动画
	public void run() {
        while (!disposed) {
        	long preTime = System.currentTimeMillis();
            currentTime++;
            try {
                display.asyncExec(new Runnable() {
                    public void run() {
                        redraw();
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
}
