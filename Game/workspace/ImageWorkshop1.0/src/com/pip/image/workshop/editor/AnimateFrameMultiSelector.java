package com.pip.image.workshop.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.pipimage.image.PipAnimateFrame;

/**
 * 同时显示多个动画帧，并支持多选。
 */
public class AnimateFrameMultiSelector extends AbstractImageViewer {
	// 多帧模式的变量
	private Point[] frameLayout;
	private boolean[] selectFlags;
	private PipAnimateFrame[] frames;
	private int selectingFrame;
	private int rightSelectFrame;
	private Image mapBuffer;
	
	public void setInput(Object input) {
		super.setInput(input);
		frames = (PipAnimateFrame[])input;
		selectFlags = new boolean[frames.length];
	}

    public void zoomin() {
        if (ratio < 64) {
            ratio *= 2;
            refresh();
        }
    }
    
    public void zoomout() {
        if (ratio > 0.125) {
            ratio /= 2;
            refresh();
        }
    }

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AnimateFrameMultiSelector(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				} else if (e.button == 3 && isFrameSelectionAllowed()) {
					rightSelectFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1 && isFrameSelectionAllowed()) {
					int index = calcPointFrame(new Point(e.x, e.y));
					if (index >= 0 && index < frames.length && index == selectingFrame) {
						selectFlags[index] = !selectFlags[index];
						fireFrameSelectionChanged(index);
						redraw();
					}
				} else if (e.button == 3 && isFrameSelectionAllowed()) {
					int index = calcPointFrame(new Point(e.x, e.y));
					if (index >= 0 && index < frames.length && index == rightSelectFrame) {
						fireFrameDoubleClicked(index);
					}
				}
			}
		});
	}

    protected void createMapBuffer() {
        Point[] pos = getBestLayout();
        frameLayout = pos;
        if (pos == null) {
            return;
        }
        int mapw = (int)(pos[0].x * ratio);
        int maph = (int)(pos[0].y * ratio);
        mapBuffer = new Image(getDisplay(), mapw, maph);
        GC gc = new GC(mapBuffer);
        
        gc.setBackground(this.getBackground());
        gc.fillRectangle(0, 0, mapw, maph);
        
        for (int i = 0; i < frames.length; i++) {
            PipAnimateFrame fr = frames[i];
            int dx = (int)(pos[i + 1].x * ratio);
            int dy = (int)(pos[i + 1].y * ratio);
            Rectangle frameRec = zoom(fr.getBounds());
            fr.draw(gc, dx - frameRec.x, dy - frameRec.y, ratio);
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
            gc.drawRectangle(dx, dy, frameRec.width, frameRec.height);
        }
        gc.dispose();
    }
   
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
		} else {
		    if (mapBuffer == null) {
		        createMapBuffer();
		    }
		    if (mapBuffer == null) {
		        return;
		    }
			int offx = (size.x - mapBuffer.getBounds().width) / 2;
			int offy = (size.y - mapBuffer.getBounds().height) / 2;
		    gc.drawImage(mapBuffer, offx + paintOffset.x, offy + paintOffset.y);
		}
	}
	
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		if (input == null) {
		} else {
		    Point[] pos = frameLayout;
			if (pos != null) {
				int offx = (int)(size.x - pos[0].x * ratio) / 2;
				int offy = (int)(size.y - pos[0].y * ratio) / 2;
				for (int i = 0; i < frames.length; i++) {
					int dx = (int)(pos[i + 1].x * ratio) + offx;
					int dy = (int)(pos[i + 1].y * ratio) + offy;
					Rectangle frameRec = zoom(frames[i].getBounds());
					if (selectFlags[i]) {
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
						gc.drawRectangle(dx + paintOffset.x, dy + paintOffset.y, frameRec.width, frameRec.height);
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
					}
				}
			}
		}
	}
	
	private int calcPointFrame(Point p) {
		if (frameLayout == null) {
			return -1;
		}
		Point size = getSize();
		int offx = (int)(size.x - frameLayout[0].x * ratio) / 2;
		int offy = (int)(size.y - frameLayout[0].y * ratio) / 2;
		for (int i = 1; i < frameLayout.length; i++) {
			int dx = (int)(frameLayout[i].x * ratio) + offx;
			int dy = (int)(frameLayout[i].y * ratio) + offy;
			PipAnimateFrame fr = frames[i - 1];
			Rectangle frameRec = zoom(fr.getBounds());
			frameRec.x = dx + paintOffset.x;
			frameRec.y = dy + paintOffset.y;
			if (frameRec.contains(p)) {
				return i - 1;
			}
		}
		return -1;
	}
	
	private Point[] getBestLayout() {
		int w = 0, h = 0;
		for (int i = 0; i < frames.length; i++) {
			Rectangle rect = frames[i].getBounds();
			w += rect.width + 2;
			if (rect.height + 2 > h) {
				h = rect.height + 2;
			}
		}
		if (w / h > 3) {
			w = (int)(w / Math.sqrt(w / (h * 3)));
		}
		Point[] ret = new Point[frames.length + 1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new Point(0, 0);
		}
		int rw = 0, lh = 0, dx = 0, dy = 0;
		for (int i = 0; i < frames.length; i++) {
			Rectangle rect = frames[i].getBounds();
			if (dx != 0 && dx + rect.width + 2 > w) {
				dx = 0;
				dy += lh;
				lh = 0;
				i--;
				continue;
			} else {
				ret[i + 1].x = dx;
				ret[i + 1].y = dy;
				dx += rect.width + 2;
				if (lh < rect.height + 2) {
					lh = rect.height + 2;
				}
				if (dx > rw) {
					rw = dx;
				}
			}
		}
		ret[0].x = rw;
		ret[0].y = dy + lh;
		return ret;
	}
	
	private boolean isFrameSelectionAllowed() {
		return input != null;
	}

	public boolean isFrameSelected(int index) {
		return selectFlags[index];
	}
	
	public void setFrameSelected(int index, boolean flag) {
		selectFlags[index] = flag;
	}

	public void refresh() {
	    if (mapBuffer != null) {
	        mapBuffer.dispose();
	    }
        mapBuffer = null;
        redraw();
    }
	
	public void widgetDisposed(DisposeEvent evt) {
	    super.widgetDisposed(evt);
	    if (mapBuffer != null) {
            mapBuffer.dispose();
        }
	}
}
