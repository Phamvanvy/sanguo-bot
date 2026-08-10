package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
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
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mapeditor.BlurMapUtil;
import com.pipimage.image.*;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display a tile lib.
 */
public class TileLibSelector extends AbstractImageViewer implements Runnable {
	private int tileWidth;
	private int tileHeight;
	private int rows;
	private int cols;
	private int hoverFrame = -1;
	private Rectangle[] frameLayout;
	private int currentTime;
    private boolean disposed;
    private Display display;
    private Thread animateThread;
    private long randomSeed = System.currentTimeMillis();

	private boolean isDragging;
	private Point dragStart, dragNow;
	private ArrayList<Integer> selectedFrames;

	
	public void setInput(Object input, int tw, int th) {
		super.setInput(input);
		tileWidth = tw;
		tileHeight = th;
		if (input instanceof Image) {
			rows = ((Image)input).getBounds().height / tileHeight;
			cols = ((Image)input).getBounds().width / tileWidth;
		}
		hoverFrame = -1;
		isDragging = false;
		selectedFrames = new ArrayList<Integer>();
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public TileLibSelector(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					isDragging = true;
					dragStart = new Point(e.x, e.y);
					dragNow = dragStart;
					recalcSelectedFrames(e.x, e.y, 1, 1);
					redraw();
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					isDragging = false;
					redraw();
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				int oldHover = hoverFrame;
				hoverFrame = calcPointFrame(new Point(e.x, e.y));
				if (hoverFrame != oldHover) {
					redraw();
				}
				
				if (isDragging) {
					int w = e.x - dragStart.x;
					int h = e.y - dragStart.y;
					if (w == 0) {
						w = 1;
					}
					if (h == 0) {
						h = 1;
					}
					recalcSelectedFrames(dragStart.x, dragStart.y, w, h);
					dragNow = new Point(e.x, e.y);
					redraw();
				}
			}
		});

		prepareMenu();
		
        display = this.getDisplay();
        animateThread = new Thread(this);
        animateThread.start();
	}
	
	protected void prepareMenu() {
		MenuManager mgr = new MenuManager();
        
        mgr.add(new Action("添加到当前地图") {
            public void run() {
                if (selectedFrames.size() > 0) {
                    fireFrameDoubleClicked(0);
                }
            }
        });
        setMenu(mgr.createContextMenu(this));
	}

	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
		} else if (input instanceof Image) {
			Image img = (Image)input;
			Rectangle imgSize = img.getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2;
			int drawY = (size.y - zoomSize.height) / 2;
			gc.drawImage(img, 0, 0, imgSize.width, imgSize.height, drawX + paintOffset.x, drawY + paintOffset.y, zoomSize.width, zoomSize.height);
		} else if (input instanceof LandformImage) {
		    LandformImage img = (LandformImage)input;
		    frameLayout = new Rectangle[] { new Rectangle(0, 0, 96, 96), new Rectangle(0, 0, 96, 96) };
            int offx = (int)(size.x - 96 * ratio) / 2;
            int offy = (int)(size.y - 96 * ratio) / 2;
            byte[][] mdtemp = BlurMapUtil.makeRectangle(6, 6);
            BlurMapUtil.drawLandform(gc, img, new Random(randomSeed), mdtemp, offx, offy, 16, 16, ratio);
		} else if (input instanceof PipImage) {
			PipImage img = (PipImage)input;
			Rectangle[] pos = getBestLayout(img);
			frameLayout = pos;
			if (pos == null) {
				return;
			}
			int offx = (int)(size.x - pos[0].width * ratio) / 2;
			int offy = (int)(size.y - pos[0].height * ratio) / 2;
			for (int i = 0; i < img.getImgCount(); i++) {
				int posIndex = i + 1;
				Rectangle rect = zoom(pos[posIndex]);
				rect.x += offx + paintOffset.x;
				rect.y += offy + paintOffset.y;
				Image frameImg = img.getImageDraw(i).createSWTImage(getDisplay(), 0);
				gc.drawImage(frameImg, 0, 0, frameImg.getBounds().width, frameImg.getBounds().height, 
				        rect.x, rect.y, rect.width, rect.height);
				frameImg.dispose();
			}
		} else if (input instanceof PipAnimateSet) {
		    PipAnimateSet ani = (PipAnimateSet)input;
            Rectangle[] pos = getBestLayout(ani);
            frameLayout = pos;
            if (pos == null) {
                return;
            }
            int offx = (int)(size.x - pos[0].width * ratio) / 2;
            int offy = (int)(size.y - pos[0].height * ratio) / 2;
		    for (int i = 0; i < ani.getAnimateCount(); i++) {
                int posIndex = i + 1;
                Rectangle rect = zoom(pos[posIndex]);
                rect.x += offx + paintOffset.x;
                rect.y += offy + paintOffset.y;
                PipAnimate animate = ani.getAnimate(i);
                Rectangle bounds = animate.getBounds();
                bounds = zoom(bounds);
                int frame = animate.getFrameAtTime(currentTime);
            	animate.drawFrame(gc, frame, rect.x - bounds.x, rect.y - bounds.y, ratio);
		    }
		}
	}
	
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		if (input != null) {
			gc.setForeground(invert(getBackground()));
			if (input instanceof Image) {
				Rectangle imgSize = ((Image)input).getBounds();
				Rectangle zoomSize = zoom(imgSize);
				int drawX = (size.x - zoomSize.width) / 2 + paintOffset.x;
				int drawY = (size.y - zoomSize.height) / 2 + paintOffset.y;
				
				if (selectedFrames.size() > 0) {
					gc.setForeground(invert(getBackground()));
					for (int i = 0; i < rows * cols; i++) {
						if (!selectedFrames.contains(i)) {
							continue;
						}
						int cellx = tileWidth * (i % cols);
						int celly = tileHeight * (i / cols);
						Rectangle rect = new Rectangle(cellx, celly, tileWidth, tileHeight);
						rect = zoom(rect);
						rect.x += drawX;
						rect.y += drawY;
//						rect.width -= 1;
//						rect.height -= 1;
						gc.drawRectangle(rect);
					}
				}

				if (hoverFrame != -1) {
					gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					int cellx = tileWidth * (hoverFrame % cols);
					int celly = tileHeight * (hoverFrame / cols);
					Rectangle rect = new Rectangle(cellx, celly, tileWidth, tileHeight);
					String cellStr = cellx + "," + celly + "," + tileWidth + "," + tileHeight;
					rect = zoom(rect);
					rect.x += drawX;
					rect.y += drawY;
					rect.x -= 1;
					rect.y -= 1;
					rect.width += 1;
					rect.height += 1;
					gc.drawRectangle(rect);
					
					gc.setForeground(invert(getBackground()));
					Point ts = gc.textExtent(cellStr);
					gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
					gc.drawText(cellStr, 4, size.y - ts.y - 5);
				}
			} else if (input instanceof PipImage || input instanceof PipAnimateSet) {
				Rectangle[] pos = frameLayout;
				int offx = (int)(size.x - pos[0].width * ratio) / 2;
				int offy = (int)(size.y - pos[0].height * ratio) / 2;
				for (int i = 0; i < pos.length - 1; i++) {
					int posIndex = i + 1;
					Rectangle rect = zoom(pos[posIndex]);
					rect.x += offx + paintOffset.x;
					rect.y += offy + paintOffset.y;
					if (i == hoverFrame) {
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
						gc.drawRectangle(rect);
						gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
					} else if (selectedFrames.contains(posIndex - 1)) {
						gc.drawRectangle(rect);
					}
				}
			}
		}
		
		gc.setForeground(invert(getBackground()));
		if (input != null && input instanceof Image) {
			Rectangle imgSize = ((Image)input).getBounds();
			String str = imgSize.width + "," + imgSize.height;
			Point ts = gc.textExtent(str);
			gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
			gc.drawText(str, size.x - ts.x - 5, size.y - ts.y - 5);
		}
		
		if (isDragging) {
			gc.setXORMode(true);
			gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			int x = dragStart.x;
			int y = dragStart.y;
			int w = dragNow.x - dragStart.x;
			int h = dragNow.y - dragStart.y;
			if (w < 0) {
				x = x + w;
				w = -w;
			}
			if (h < 0) {
				y = y + h;
				h = -h;
			}
			gc.drawRectangle(x, y, w, h);
			gc.setXORMode(false);
		}
	}
	
	private int calcPointFrame(Point p) {
		if (input == null) {
			return -1;
		}
		if (input instanceof Image) {
			if (rows == 0 || cols == 0) {
				return -1;
			}
			Point size = getSize();
			Rectangle imgSize = ((Image)input).getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2 + paintOffset.x;
			int drawY = (size.y - zoomSize.height) / 2 + paintOffset.y;
			int col = (int)((p.x - drawX) / ratio) / tileWidth;
			int row = (int)((p.y - drawY) / ratio) / tileHeight;
			if (col < 0 || col >= cols || row < 0 || row >= rows) {
				return -1;
			}
			return row * cols + col;
		} else {
			if (frameLayout == null) {
				return -1;
			}
			Point size = getSize();
			int offx = (int)(size.x - frameLayout[0].width * ratio) / 2 + paintOffset.x;
			int offy = (int)(size.y - frameLayout[0].height * ratio) / 2 + paintOffset.y;
			for (int i = 1; i < frameLayout.length; i++) {
			    Rectangle rect = zoom(frameLayout[i]);
			    rect.x += offx;
			    rect.y += offy;
				if (rect.contains(p)) {
					return i - 1;
				}
			}
			return -1;
		}
	}
	
	private void recalcSelectedFrames(int x, int y, int w, int h) {
		if (input == null) {
			return;
		}
		selectedFrames.clear();
		if (w < 0) {
			x += w;
			w = -w;
		}
		if (h < 0) {
			y += h;
			h = -h;
		}
		Point size = getSize();
		Rectangle selRect = new Rectangle(x, y, w, h);
		if (input instanceof Image) {
			Rectangle imgSize = ((Image)input).getBounds();
			Rectangle zoomSize = zoom(imgSize);
			int drawX = (size.x - zoomSize.width) / 2 + paintOffset.x;
			int drawY = (size.y - zoomSize.height) / 2 + paintOffset.y;
			for (int i = 0; i < rows * cols; i++) {
				int cellx = tileWidth * (i % cols);
				int celly = tileHeight * (i / cols);
				Rectangle rect = new Rectangle(cellx, celly, tileWidth, tileHeight);
				rect = zoom(rect);
				rect.x += drawX;
				rect.y += drawY;
				if (selRect.intersects(rect)) {
					selectedFrames.add(i);
				}
			}
		} else if (input instanceof PipImage || input instanceof PipAnimateSet) {
			Rectangle[] pos = frameLayout;
			int offx = (int)(size.x - pos[0].width * ratio) / 2 + paintOffset.x;
			int offy = (int)(size.y - pos[0].height * ratio) / 2 + paintOffset.y;
			for (int i = 0; i < pos.length - 1; i++) {
				Rectangle rect = zoom(pos[i + 1]);
				rect.x += offx;
				rect.y += offy;
				if (selRect.intersects(rect)) {
					selectedFrames.add(i);
				}
			}
		}
	}

	private Rectangle[] getBestLayout(PipImage img) {
		Rectangle[] frameBounds = new Rectangle[img.getImgCount()];
		for (int i = 0; i <img.getImgCount(); i++) {
		    PipImageData data = img.getImageData(i);
		    frameBounds[i] = new Rectangle(0, 0, data.getWidth(), data.getHeight());
		}
		return getBestLayout(getSize(), frameBounds);
	}

    private Rectangle[] getBestLayout(PipAnimateSet animateSet) {
        Rectangle[] frameBounds = new Rectangle[animateSet.getAnimateCount()];
        for (int i = 0; i < animateSet.getAnimateCount(); i++) {
            PipAnimate animate = animateSet.getAnimate(i);
            frameBounds[i] = animate.getBounds();
        }
        return getBestLayout(getSize(), frameBounds);
    }
	
	public int[] getSelectedFrames() {
		int[] ret = new int[selectedFrames.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = selectedFrames.get(i);
		}
		return ret;
	}
	
	public void setSelectedFrame(int sel) {
		selectedFrames.clear();
		selectedFrames.add(sel);
		redraw();
	}

    // 驱动动画
    public void run() {
        while (!disposed) {
            currentTime++;
            try {
                if (input != null && input instanceof PipAnimateSet) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            try {
                            	if(!isDisposed())
                                redraw();
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }catch(SWTException we){
            	System.err.println(we);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }
}
