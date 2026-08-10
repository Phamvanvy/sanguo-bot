package com.pip.mapeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImageDraw;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * 碰撞区域编辑器。
 */
public class CollisionAreaEditor extends AbstractImageViewer implements Runnable {
	private Point startPoint, endPoint;
    private Point lastPoint;
	private boolean isSelecting;
	private ArrayList<Rectangle> selectedAreas = new ArrayList<Rectangle>();
	private boolean isDragging;
    private int focusIndex;
	private int dragAnchor;
	private Point dragStartPoint;
    private Rectangle dragStartBounds;
	private int currentTime;
	private boolean disposed;
	private Display display;
	
	private PipAnimate animate;
	private Thread animateThread;
	/**
	 * 设置区域的个数,默认0，表示不限制
	 */
	private int areaCnt = 0;
	private int forceWidth;
	private int forceHeight;
	
	public boolean allowOutRange = false;

	public void setInput(Object input) {
		super.setInput(input);
		animate = (PipAnimate)input;
		selectedAreas = new ArrayList<Rectangle>();
		isSelecting = false;
		isDragging = false;
		focusIndex = -1;
		setMenu(null);
		setCursor(ImageViewer.cursorCross);
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public CollisionAreaEditor(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (input == null) {
					return;
				}
				Point pt = new Point(e.x, e.y);
				pt = calcCoord(pt);
				if (!isDragging) {
					if (isSelecting) {
						setCursor(ImageViewer.cursorCross);
					} else {
					    int[] anchor = getDragAnchor(e.x, e.y);
					    if (anchor == null) {
					        setCursor(ImageViewer.getCursorOfAnchor(-1));
					    } else {
                            setCursor(ImageViewer.getCursorOfAnchor(anchor[1]));
					    }
					}
				}
				lastPoint = pt;
				if (isSelecting) {
					endPoint = new Point(e.x, e.y);
				} else if (isDragging) {
					Point dragNow = new Point(e.x, e.y);
					dragSelection(dragNow);
				}
				redraw();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (input == null) {
					return;
				}
				if (e.button == 1) {
					Point pt = new Point(e.x, e.y);
					int[] anchor = getDragAnchor(e.x, e.y);
					if (anchor != null) {
					    isDragging = true;
					    focusIndex = anchor[0];
						dragAnchor = anchor[1];
						Rectangle rect = selectedAreas.get(focusIndex);
						dragStartBounds = new Rectangle(rect.x, rect.y, rect.width, rect.height);
						dragStartPoint = new Point(e.x, e.y);
						return;
					}
					if(areaCnt>0 && selectedAreas.size() == areaCnt){
						return;
					}
					isSelecting = true;
					startPoint = pt;
					endPoint = startPoint;
					isSelecting = true;
					redraw();
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (input == null) {
					return;
				}
				if (e.button == 1) {
					if (isSelecting) {
						endPoint = new Point(e.x, e.y);
						Rectangle rect = calcSelection();
						if (rect != null) {
    						normalize(rect);
    						if (!rect.isEmpty()) {
    						    selectedAreas.add(rect);
    						    focusIndex = selectedAreas.size() - 1;
    						    fireContentChanged();
    						}
						}
						isSelecting = false;
					} else if (isDragging) {
					    Point dragNow = new Point(e.x, e.y);
	                    dragSelection(dragNow);
	                    Rectangle rect = selectedAreas.get(focusIndex);
                        normalize(rect);
                        if (rect.isEmpty()) {
                            selectedAreas.remove(focusIndex);
                            focusIndex = -1;
                        }
                        fireContentChanged();
	                    isDragging = false;
					}
					redraw();
				}
			}
			public void mouseDoubleClick(MouseEvent e) {}
		});

		display = this.getDisplay();
		animateThread = new Thread(this);
		animateThread.start();
	}
	
	// Compute the drag anchor. The return value may be one of the following:
	// null : no anchor
	// not null: the first element is index of area, the second element is:
	//  0 : center
	//  1 : west edge
	//  2 : north edge;
	//  3 : east edge
	//  4 : south edge
	//  5 : north-west corner
	//  6 : north-east corner
	//  7 : south-east corner
	//  8 : south-west corner
	private int[] getDragAnchor(int x, int y) {
	    if (isSelecting) {
			return null;
		}
		Rectangle imgSize = zoom(animate.getBounds());
		Point size = getSize();
		for (int i = 0; i < selectedAreas.size(); i++) {
    		Rectangle area = zoom(selectedAreas.get(i));
    		area.x += (size.x - imgSize.width) / 2 + paintOffset.x;
    		area.y += (size.y - imgSize.height) / 2 + paintOffset.y;
    		
    		// check north west corner
    		if (new Rectangle(area.x - 3, area.y - 3, 6, 6).contains(x, y)) {
    			return new int[] { i, 5 };
    		}
    		// check north east corner
    		if (new Rectangle(area.x + area.width - 3, area.y - 3, 6, 6).contains(x, y)) {
    			return new int[] { i, 6 };
    		}
    		// check south east corner
    		if (new Rectangle(area.x + area.width - 3, area.y + area.height - 3, 6, 6).contains(x, y)) {
    		    return new int[] { i, 7 };
    		}
    		// check south west corner
    		if (new Rectangle(area.x - 3, area.y + area.height - 3, 6, 6).contains(x, y)) {
    		    return new int[] { i, 8 };
    		}
    		// check west edge
    		if (new Rectangle(area.x - 3, area.y, 6, area.height).contains(x, y)) {
    		    return new int[] { i, 1 };
    		}
    		// check north edge
    		if (new Rectangle(area.x, area.y - 3, area.width, 6).contains(x, y)) {
    		    return new int[] { i, 2 };
    		}
    		// check east edge
    		if (new Rectangle(area.x + area.width - 3, area.y, 6, area.height).contains(x, y)) {
    		    return new int[] { i, 3 };
    		}
    		// check south edge
    		if (new Rectangle(area.x, area.y + area.height - 3, area.width, 6).contains(x, y)) {
    		    return new int[] { i, 4 };
    		}
    		// check center
    		if (area.contains(x, y)) {
    			return new int[] { i, 0 };
    		}
		}
		return null;
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
			drawSelection(gc);
		}

		gc.setBackground(getBackground());
		gc.setForeground(invert(getBackground()));
        if (lastPoint != null) {
            String coordStr = lastPoint.x + "," + lastPoint.y;
            Point ts = gc.textExtent(coordStr);
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
			drawX = drawX - zoomSize.x + paintOffset.x;
			drawY = drawY - zoomSize.y + paintOffset.y;
			if(areaCnt != 1){
				animate.drawFrame(gc, frame, drawX, drawY, ratio);
			}else{// setup head icon area code
				PipAnimateFrame fr = animate.getFrame(animate.getFrameAtTime(currentTime)).realize();
				Rectangle frBds = animate.getBounds();
				fr.draw(gc, drawX, drawY, ratio);
				if(selectedAreas.size()>0){
					focusIndex = 0;
				}
				if(focusIndex >= 0){
					Rectangle focusRect = selectedAreas.get(focusIndex);
					int diffX = (int)(drawX + (frBds.x - frBds.width - 4) * ratio);
					int diffY = (int)(drawY + frBds.y * ratio);
					Rectangle newClip = new Rectangle(diffX, diffY, (int)(focusRect.width * ratio), (int)(focusRect.height * ratio));
					gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					
					Rectangle clip = gc.getClipping();
					gc.setClipping(newClip);
					fr.draw(gc, (int)(drawX - (frBds.width + 4 + focusRect.x) * ratio), (int)(drawY - focusRect.y * ratio), ratio);
					gc.drawRectangle(newClip.x, newClip.y, newClip.width-1, newClip.height-1);
					gc.setClipping(clip);
				}
			}
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
	
	private void normalize(Rectangle rect) {
		if (rect.width < 0) {
			rect.x = rect.x + rect.width;
			rect.width = -rect.width;
		}
		if (rect.height < 0) {
			rect.y = rect.y + rect.height;
			rect.height = -rect.height;
		}
		if (!allowOutRange) {
			if (rect.x < 0) {
			    rect.width += rect.x;
			    rect.x = 0;
			}
			if (rect.y < 0) {
			    rect.height += rect.y;
			    rect.y = 0;
			}
		}
		Rectangle bounds = animate.getBounds();
		if (!allowOutRange) {
			if (rect.x + rect.width > bounds.width) {
			    rect.width = bounds.width - rect.x;
			}
			if (rect.y + rect.height > bounds.height) {
			    rect.height = bounds.height - rect.y;
			}
		}
		if(areaCnt == 1 && forceWidth > 0){
			rect.width = forceWidth;
			rect.height = forceHeight;
		}
	}
	
	//  1 : west edge
	//  2 : north edge;
	//  3 : east edge
	//  4 : south edge
	//  5 : north-west corner
	//  6 : north-east corner
	//  7 : south-east corner
	//  8 : south-west corner
	private void dragSelection(Point end) {
		int offx = end.x - dragStartPoint.x;
		int offy = end.y - dragStartPoint.y;
		offx = (int)(fitGrid(offx) / ratio);
		offy = (int)(fitGrid(offy) / ratio);
		Rectangle focusRect = selectedAreas.get(focusIndex);
		switch (dragAnchor) {
		case 0:
		    focusRect.x = dragStartBounds.x + offx;
		    focusRect.y = dragStartBounds.y + offy;
		    break;
		case 1:
		    focusRect.x = dragStartBounds.x + offx;
		    focusRect.width = dragStartBounds.width - offx;
		    break;
		case 2:
		    focusRect.y = dragStartBounds.y + offy;
		    focusRect.height = dragStartBounds.height - offy;
		    break;
		case 3:
		    focusRect.width = dragStartBounds.width + offx;
		    break;
		case 4:
		    focusRect.height = dragStartBounds.height + offy;
		    break;
		case 5:
		    focusRect.x = dragStartBounds.x + offx;
		    focusRect.y = dragStartBounds.y + offy;
		    focusRect.width = dragStartBounds.width - offx;
		    focusRect.height = dragStartBounds.height - offy;
		    break;
		case 6:
		    focusRect.y = dragStartBounds.y + offy;
		    focusRect.width = dragStartBounds.width + offx;
		    focusRect.height = dragStartBounds.height - offy;
		    break;
		case 7:
		    focusRect.width = dragStartBounds.width + offx;
		    focusRect.height = dragStartBounds.height + offy;
		    break;
		case 8:
		    focusRect.x = dragStartBounds.x + offx;
            focusRect.width = dragStartBounds.width - offx;
            focusRect.height = dragStartBounds.height + offy;
            break;
		}
	}
	
	private Rectangle calcSelection() {
		Rectangle imgSize = animate.getBounds();
		Rectangle zoomSize = zoom(imgSize);
		Point size = getSize();
		zoomSize.x = (size.x - zoomSize.width) / 2 + paintOffset.x;
		zoomSize.y = (size.y - zoomSize.height) / 2 + paintOffset.y;
		Rectangle selection = new Rectangle(Math.min(startPoint.x, endPoint.x), 
				Math.min(startPoint.y, endPoint.y), Math.abs(startPoint.x - endPoint.x),
				Math.abs((startPoint.y - endPoint.y)));
		selection.intersect(zoomSize);
		int x = (int)(fitGrid(selection.x - zoomSize.x) / ratio);
		int y = (int)(fitGrid(selection.y - zoomSize.y) / ratio);
		int x2 = (int)(fitGrid(selection.x - zoomSize.x + selection.width) / ratio);
		int y2 = (int)(fitGrid(selection.y - zoomSize.y + selection.height) / ratio);
		if (x2 == x && y2 == y) {
			return null;
		}
		return new Rectangle(x, y, x2 - x, y2 - y);
	}
	
	private void drawSelection(GC gc) {
	    for (int i = 0; i < selectedAreas.size(); i++) {
	        Rectangle rect = selectedAreas.get(i);
            Rectangle imgSize = animate.getBounds();
            Rectangle zoomSize = zoom(imgSize);
            Point size = getSize();
            Rectangle zoomArea = zoom(rect);
            zoomArea.x += (size.x - zoomSize.width) / 2;
            zoomArea.y += (size.y - zoomSize.height) / 2;
            gc.setBackground(getDisplay().getSystemColor(i == focusIndex ? SWT.COLOR_RED : SWT.COLOR_BLACK));
            zoomArea.x += paintOffset.x;
            zoomArea.y += paintOffset.y;
            gc.setAlpha(0x80);
            gc.fillRectangle(zoomArea);
            gc.setAlpha(0xFF);
        }
		if (isSelecting) {
			Rectangle selection = new Rectangle(Math.min(startPoint.x, endPoint.x), 
					Math.min(startPoint.y, endPoint.y), Math.abs(startPoint.x - endPoint.x),
					Math.abs((startPoint.y - endPoint.y)));
			gc.setXORMode(true);
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.drawRectangle(selection);
			gc.setXORMode(false);
		}
	}
	
	public Rectangle[] getSelectedArea() {
	    if (selectedAreas.size() == 0) {
	        return null;
	    }
	    
	    // 去掉互相覆盖的区域
	    List<Rectangle> retList = new ArrayList<Rectangle>();
	    for (int i = 0; i < selectedAreas.size(); i++) {
	        Rectangle rect = selectedAreas.get(i);
	        boolean covered = false;
	        for (Rectangle r : retList) {
	            if (r.union(rect).equals(r)) {
	                covered = true;
	                break;
	            }
	        }
	        if (covered) {
	            continue;
	        }
	        for (int j = i + 1; j < selectedAreas.size(); j++) {
	            Rectangle r = selectedAreas.get(j);
	            if (r.union(rect).equals(r)) {
	                covered = true;
	                break;
	            }
	        }
	        if (covered) {
	            continue;
	        }
	        retList.add(rect);
	    }
	    
	    Rectangle[] ret = new Rectangle[retList.size()];
	    retList.toArray(ret);
	    return ret;
	}
	
	public void setSelectedArea(Rectangle[] rects) {
	    selectedAreas.clear();
	    if(rects!=null)
	    for (Rectangle r : rects) {
	        selectedAreas.add(r);
	    }
	}

	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (isSelecting || isDragging || focusIndex < 0 || focusIndex >= selectedAreas.size()) {
		    return;
		}
		Rectangle selArea = selectedAreas.get(focusIndex);
		switch (keyCode) {
		case SWT.DEL:
		    selectedAreas.remove(focusIndex);
		    focusIndex = -1;
		    redraw();
		    break;
		case SWT.ARROW_UP:
			selArea.y--;
			break;
		case SWT.ARROW_DOWN:
		    selArea.y++;
			break;
		case SWT.ARROW_LEFT:
		    selArea.x--;
			break;
		case SWT.ARROW_RIGHT:
		    selArea.x++;
			break;
		default:
			return;
		}
		normalize(selArea);
		if (selArea.isEmpty()) {
		    selectedAreas.remove(focusIndex);
		    focusIndex = -1;
		    fireContentChanged();
		}
        redraw();
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
                    	if(!isDisposed())
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

	public int getAreaCnt() {
		return areaCnt;
	}

	public void setAreaCnt(int areaCnt) {
		this.areaCnt = areaCnt;
	}

	/**
	 * 强制头像区域大小为高宽i, areaCnt == 1 时有效
	 * @param i
	 */
	public void setAreaSize(int w, int h) {
		forceWidth = w;
		forceHeight = h;
	}
}
