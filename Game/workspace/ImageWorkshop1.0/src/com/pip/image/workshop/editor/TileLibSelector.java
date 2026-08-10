package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
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
import com.pip.mapeditor.BlurMapUtil;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
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
	protected ArrayList<Integer> selectedFrames;
	
	// 预览动画时，允许只显示部分动画序列
	protected boolean[] animateVisibleFlag;
	// 预览动画时，允许把一个图片的一个图块替换为另外一个图块
	protected int replaceImageID = -1;
	protected int replaceSourceFrame;
	protected int replaceTargetFrame;
	protected int replaceOffsetX;
	protected int replaceOffsetY;
	
	// 预览PipAnimateSet的时候，把每一帧都缓存起来
	protected Map<Integer, Image> frameCache;
	
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
		clearCache();
	}
	
	public void setAnimateVisibleFlag(boolean[] flag) {
		animateVisibleFlag = flag;
	}
	
	public void setReplaceImage(int imageID, int sourceFrame, int targetFrame, int offsetX, int offsetY) {
		replaceImageID = imageID;
		replaceSourceFrame = sourceFrame;
		replaceTargetFrame = targetFrame;
		replaceOffsetX = offsetX;
		replaceOffsetY = offsetY;
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
        if (getMenu() != null) {
        	getMenu().dispose();
        }
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
		    int tileSize = img.getFrameCount() == 0 ? 16 : img.getImageData(0).width;
		    frameLayout = new Rectangle[] { new Rectangle(0, 0, tileSize * 6, tileSize * 6), new Rectangle(0, 0, tileSize * 6, tileSize * 6) };
            int offx = (int)(size.x - tileSize * 6 * ratio) / 2;
            int offy = (int)(size.y - tileSize * 6 * ratio) / 2;
            byte[][] mdtemp = BlurMapUtil.makeRectangle(6, 6);
            BlurMapUtil.drawLandform(gc, img, new Random(randomSeed), mdtemp, offx, offy, tileSize, tileSize, ratio);
		} else if (input instanceof LandformImage[]) {
		    LandformImage[] imgs = (LandformImage[])input;
		    Rectangle[] sizes = new Rectangle[imgs.length];
		    for (int i = 0; i < sizes.length; i++) {
		    	int tileSize = imgs[i].getFrameCount() == 0 ? 16 : imgs[i].getImageData(0).width;
		    	sizes[i] = new Rectangle(0, 0, tileSize * 6, tileSize * 6);
		    }
		    frameLayout = getBestLayout(size, sizes);
		    
		    int offx = (int)(size.x - frameLayout[0].width * ratio) / 2;
            int offy = (int)(size.y - frameLayout[0].height * ratio) / 2;
		    for (int i = 0; i < imgs.length; i++) {
		    	Rectangle rect = zoom(frameLayout[i + 1]);
				rect.x += offx + paintOffset.x;
				rect.y += offy + paintOffset.y;
				int tileSize = imgs[i].getFrameCount() == 0 ? 16 : imgs[i].getImageData(0).width;
				
				// 地形图片缓存以提高速度
				if (frameCache == null) {
                	frameCache = new HashMap<Integer, Image>();
                }
				Image cachedImg = frameCache.get(i);
				if (cachedImg == null) {
					cachedImg = new Image(getDisplay(), tileSize * 6, tileSize * 6);
					GC tmpGC = new GC(cachedImg);
					byte[][] mdtemp = BlurMapUtil.makeRectangle(6, 6);
					BlurMapUtil.drawLandform(tmpGC, imgs[i], new Random(randomSeed), mdtemp, 0, 0, tileSize, tileSize, 1.0);
					tmpGC.dispose();
					frameCache.put(i, cachedImg);
				}
				gc.drawImage(cachedImg, 0, 0, tileSize * 6, tileSize * 6, 
						rect.x, rect.y, (int)(tileSize * 6 * ratio), (int)(tileSize * 6 * ratio));
		    }
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
		    	if (animateVisibleFlag != null && !animateVisibleFlag[i]) {
		    		continue;
		    	}
                int posIndex = i + 1;
                Rectangle rect = zoom(pos[posIndex]);
                rect.x += offx + paintOffset.x;
                rect.y += offy + paintOffset.y;
                PipAnimate animate = ani.getAnimate(i);
                int frame = animate.getFrameAtTime(currentTime);
                if (frame == -1) {
                	continue;
                }
                PipAnimateFrameRef frameRef = animate.getFrame(frame);
                int rframe = frameRef.getFrame();
                Rectangle abounds;
                Rectangle bounds;
                if (replaceImageID == -1) {
                	abounds = animate.getBounds();
                	bounds = ani.getFrame(rframe).getBounds();
                } else {
                	abounds = animate.getBounds(replaceImageID, replaceSourceFrame,
                			replaceTargetFrame, replaceOffsetX, replaceOffsetY);
                	bounds = ani.getFrame(rframe).getBounds(replaceImageID, replaceSourceFrame,
                			replaceTargetFrame, replaceOffsetX, replaceOffsetY);
                }
                
                // 帧缓存起来
                if (frameCache == null) {
                	frameCache = new HashMap<Integer, Image>();
                }
                Image cachedImg = frameCache.get(rframe);
                if (cachedImg != null && (cachedImg.getBounds().width != bounds.width || cachedImg.getBounds().height != bounds.height)) {
                	cachedImg.dispose();
                	frameCache.remove(rframe);
                	cachedImg = null;
                }
                if (cachedImg == null) {
                	// 把帧画到缓存上
                	cachedImg = new Image(getDisplay(), bounds.width, bounds.height);
                	frameCache.put(rframe, cachedImg);
                	GC fgc = new GC(cachedImg);
                	fgc.setBackground(getBackground());
                	fgc.fillRectangle(0, 0, bounds.width, bounds.height);
                	if (replaceImageID == -1) {
                		ani.getFrame(rframe).draw(fgc, -bounds.x, -bounds.y, 1.0);
                	} else {
                		ani.getFrame(rframe).draw(fgc, -bounds.x, -bounds.y, 1.0,
                				replaceImageID, replaceSourceFrame,
                    			replaceTargetFrame, replaceOffsetX, replaceOffsetY);
                	}
                	fgc.dispose();
                }
                int xoff = (int)((-abounds.x + bounds.x + frameRef.getDx()) * ratio);
                int yoff = (int)((-abounds.y + bounds.y + frameRef.getDy()) * ratio);
                gc.drawImage(cachedImg, 0, 0, bounds.width, bounds.height, rect.x + xoff, rect.y + yoff, (int)(bounds.width * ratio), (int)(bounds.height * ratio));
		    }
		} else if (input instanceof PipAnimateSet[]) {
		    PipAnimateSet[] anis = (PipAnimateSet[])input;
            Rectangle[] pos = getBestLayout(anis);
            frameLayout = pos;
            if (pos == null) {
                return;
            }
            int offx = (int)(size.x - pos[0].width * ratio) / 2;
            int offy = (int)(size.y - pos[0].height * ratio) / 2;
            int posIndex = 0;
            for (PipAnimateSet ani : anis) {
			    for (int i = 0; i < ani.getAnimateCount(); i++) {
			    	posIndex++;
	                Rectangle rect = zoom(pos[posIndex]);
	                rect.x += offx + paintOffset.x;
	                rect.y += offy + paintOffset.y;
	                PipAnimate animate = ani.getAnimate(i);
	                int frame = animate.getFrameAtTime(currentTime);
	                if (frame == -1) {
	                	continue;
	                }
	                PipAnimateFrameRef frameRef = animate.getFrame(frame);
	                int rframe = frameRef.getFrame();
	                Rectangle abounds;
	                Rectangle bounds;
                	abounds = animate.getBounds();
                	bounds = ani.getFrame(rframe).getBounds();
                	
                	// 帧缓存起来
                    if (frameCache == null) {
                    	frameCache = new HashMap<Integer, Image>();
                    }
                    Image cachedImg = frameCache.get(posIndex);
                    if (cachedImg != null && (cachedImg.getBounds().width != bounds.width || cachedImg.getBounds().height != bounds.height)) {
                    	cachedImg.dispose();
                    	frameCache.remove(posIndex);
                    	cachedImg = null;
                    }
                    if (cachedImg == null) {
                    	// 把帧画到缓存上
                    	cachedImg = new Image(getDisplay(), bounds.width, bounds.height);
                    	frameCache.put(posIndex, cachedImg);
                    	GC fgc = new GC(cachedImg);
                    	fgc.setBackground(getBackground());
                    	fgc.fillRectangle(0, 0, bounds.width, bounds.height);
                		ani.getFrame(rframe).draw(fgc, -bounds.x, -bounds.y, 1.0);
                    	fgc.dispose();
                    }
                    int xoff = (int)((-abounds.x + bounds.x + frameRef.getDx()) * ratio);
                    int yoff = (int)((-abounds.y + bounds.y + frameRef.getDy()) * ratio);
                    gc.drawImage(cachedImg, 0, 0, bounds.width, bounds.height, rect.x + xoff, rect.y + yoff, (int)(bounds.width * ratio), (int)(bounds.height * ratio));
			    }
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
			} else if (input instanceof PipImage || input instanceof PipAnimateSet || input instanceof PipAnimateSet[] || input instanceof LandformImage[]) {
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
				
				// 选中索引
				if (selectedFrames.size() > 0) {
					String str = "索引：";
					for (int i = 0; i < selectedFrames.size(); i++) {
						if (i > 0) {
							str += ",";
						}
						str += selectedFrames.get(i);
					}
					Point ts = gc.textExtent(str);
					gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
					gc.drawText(str, 4, size.y - ts.y - 5);
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
		} else {
			if (frameLayout == null) {
				return;
			}
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
		fireFrameSelectionChanged(selectedFrames.size() == 0 ? -1 : selectedFrames.get(0));
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
        	if (animateVisibleFlag != null && !animateVisibleFlag[i]) {
        		frameBounds[i] = new Rectangle(0, 0, 0, 0);
        	} else {
	            PipAnimate animate = animateSet.getAnimate(i);
	            if (replaceImageID == -1) {
	            	frameBounds[i] = animate.getBounds();
	            } else {
	            	frameBounds[i] = animate.getBounds(replaceImageID, replaceSourceFrame,
            			replaceTargetFrame, replaceOffsetX, replaceOffsetY);
	            }
        	}
        }
        return getBestLayout(getSize(), frameBounds);
    }
    
    private Rectangle[] getBestLayout(PipAnimateSet[] animates) {
    	List<Rectangle> rects = new ArrayList<Rectangle>();
    	for (PipAnimateSet animateSet : animates) {
	        for (int i = 0; i < animateSet.getAnimateCount(); i++) {
	            PipAnimate animate = animateSet.getAnimate(i);
            	rects.add(animate.getBounds());
	        }
    	}
        Rectangle[] frameBounds = new Rectangle[rects.size()];
        rects.toArray(frameBounds);
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
        	long preTime = System.currentTimeMillis();
            currentTime++;
            try {
                if (input != null && (input instanceof PipAnimateSet || input instanceof PipAnimateSet[])) {
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
            	long processTime = System.currentTimeMillis() - preTime;
            	if (processTime < Settings.animateFrameDelay) {
            		Thread.sleep(Settings.animateFrameDelay - processTime);
            	}
			} catch (Exception e) {
			}
        }
    }
    
    public void clearCache() {
    	if (frameCache != null) {
    		for (Image img : frameCache.values()) {
    			img.dispose();
    		}
    		frameCache = null;
    	}
    }

	@Override
	public void widgetDisposed(DisposeEvent e) {
		super.widgetDisposed(e);
		clearCache();
	}
}
