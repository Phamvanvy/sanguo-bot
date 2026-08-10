package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.pip.image.workshop.Settings;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pipimage.image.ImageDrawCache;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.ext.EdgeExtension;
import com.swtdesigner.SWTResourceManager;

public class EdgeEditViewer extends AbstractImageViewer implements Runnable {
	protected PipAnimateSet animateSet;
	protected EdgeExtension.Edge edge;
	
	private boolean disposed;
	private Display display;
	private int currentTime;
	private Thread animateThread;
	
	// 保存动画范围内的每一个像素是否被设置为可选区域
	private boolean[][] flagCache;
	private int flagStartX;
	private int flagStartY;
	private boolean dragging = false;
	private boolean playAnimate = true;
	private Point lastDragPoint;
	private int lastX = -1;
	private int lastY = -1;
	
	// 刷子设置
	private String[] buttonTexts = { "设置", "清除" ,"1x1", "5x5", "10x10", "20x20", "40x40", "80x80" };
    private int brushSize[] = new int[] { 1, 5, 10, 20, 40, 80 };
    // 设置按钮的位置和大小
    private Rectangle[] buttonBounds = new Rectangle[buttonTexts.length];
    // 选中的按钮
    private int selectedButtonIndex = 0;
    private int selectedBrushSize = 2;
    
    private ImageDrawCache imageCache = null;
	
	public void setInput(PipAnimateSet aset, EdgeExtension.Edge edge) {
		super.setInput(aset);
		this.animateSet = aset;
		this.edge = edge;
		redraw();
		if (aset != null) {
			loadFlags();
		}
		lastX = -1;
		lastY = -1;
	}
	
	public void setImageCache(ImageDrawCache cache) {
		imageCache = cache;
	}
	
	private int getBrushSize() {
		return brushSize[selectedBrushSize - 1];
	}
	
	// 把edge对象中的设置转换为标志数组
	private void loadFlags() {
		int startIndex = edge.beginAnimateIndex;
		if (startIndex == -1) {
			startIndex = 0;
		}
		PipAnimate animate = animateSet.getAnimate(startIndex);
		Rectangle rect = animate.getBounds();
		flagStartX = rect.x - 200;
		flagStartY = rect.y - 200;
		int w = rect.width + 400;
		int h = rect.height + 400;
		flagCache = new boolean[h][w];
		for (int y = 0; y < edge.height; y++) {
			for (int x = edge.beginX[y]; x < edge.endX[y]; x++) {
				int yy = y + edge.beginY - flagStartY;
				int xx = x - flagStartX;
				if (yy >= 0 && yy < h && xx >= 0 && xx < w) {
					flagCache[yy][xx] = true;
				}
			}
		}
	}
	
	// 把标志数组中合法的数据转换为edge对象中的表现形式
	private void saveFlags() {
		// 找出每一行第一个标记为true和最后一个标记为true的位置，顺便找出第一个不为空的行和最后一个不为空的行
		int[] startX = new int[flagCache.length];
		int[] endX = new int[flagCache.length];
		int firstY = -1;
		int lastY = -1;
		for (int i = 0; i < flagCache.length; i++) {
			int lineS = -1;  // 第一个true
			int lineE = -1;  // 最后一个true
			for (int j = 0; j < flagCache[i].length; j++) {
				if (flagCache[i][j]) {
					if (lineS == -1) {
						lineS = j;
					}
					lineE = j;
				}
			}
			startX[i] = lineS;
			endX[i] = lineE;
			if (lineS != -1) {
				if (firstY == -1) {
					firstY = i;
				}
				lastY = i;
			}
		}
		
		// 初始化edge对象中的数据
		if (firstY == -1) {
			edge.beginY = 0;
			edge.height = 0;
			edge.beginX = new int[0];
			edge.endX = new int[0];
		} else {
			edge.beginY = firstY + flagStartY;
			edge.height = lastY - firstY + 1;
			edge.beginX = new int[edge.height];
			edge.endX = new int[edge.height];
			for (int i = 0; i < edge.height; i++) {
				int ind = i + firstY;
				if (startX[ind] == -1) {
					edge.beginX[i] = 0;
					edge.endX[i] = 0;
				} else {
					edge.beginX[i] = startX[ind] + flagStartX;
					edge.endX[i] = endX[ind] + flagStartX + 1;
				}
			}
		}
	}
	
	private Point calcCoord(Point p) {
		Point size = getSize();
		int x = (int)Math.round((p.x - size.x / 2 - paintOffset.x) / ratio - 0.5);
		int y = (int)Math.round((p.y - size.y / 2 - paintOffset.y) / ratio - 0.5);
		return new Point(x, y);
	}
	
	private Point coordToPos(Point p) {
		Point size = getSize();
		int x = (int)(p.x * ratio + size.x / 2 + paintOffset.x);
		int y = (int)(p.y * ratio + size.y / 2 + paintOffset.y);
		return new Point(x, y);
	}
	
	/*
	 * 计算一条直线上的所有点，不包括起点，包括终点。
	 * @param start
	 * @param stop
	 * @return
	 */
	private Point[] getPointsOnLine(Point start, Point stop) {
		List<Point> ret = new ArrayList<Point>();
        try {
            if (start.x == stop.x) {
                // 对于垂直这种情况，无法计算tgt值，所以需要特殊处理
                int delta = 1;
                if (stop.y < start.y) {
                    delta = -1;
                }
                for (int y = start.y + 1; true; y += delta) {
                    ret.add(new Point(start.x, y));
                    if (y == stop.y) {
                        break;
                    }
                }
                Point[] arr = new Point[ret.size()];
                ret.toArray(arr);
                return arr;
            }
            
            // 计算tgt值，然后计算直线上每个点是否阻挡
            int tgt = (stop.y - start.y) * 10000 / (stop.x - start.x);
            int delta = 1;
            if (stop.x < start.x) {
                delta = -1;
            }
            int lastx = start.x;
            int lasty = start.y;
            for (int x = start.x + delta; true; x += delta) {
                int y = (x - start.x) * tgt / 10000 + start.y;
                if (x == stop.x) {
                    y = stop.y;
                }
                if (lasty > y) {
                    for (int yy = y; yy <= lasty; yy++) {
                        ret.add(new Point(lastx, yy));
                    }
                } else {
                    for (int yy = lasty; yy <= y; yy++) {
                        ret.add(new Point(lastx, yy));
                    }
                }
                if (x == stop.x) {
                    break;
                }
                lastx = x;
                lasty = y;
            }
            ret.add(new Point(stop.x, stop.y));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Point[] arr = new Point[ret.size()];
        ret.toArray(arr);
        return arr;
	}
	
	private void switchGrid(Point[] pts, int brushSize, boolean on) {
		boolean changed = false;
		int bs1 = -brushSize / 2;
		int bs2 = brushSize + bs1;
		for (Point pt : pts) {
			pt = calcCoord(pt);
			for (int bx = pt.x + bs1; bx < pt.x + bs2; bx++) {
				for (int by = pt.y + bs1; by < pt.y + bs2; by++) {
					if (bx >= flagStartX && bx < flagStartX + flagCache[0].length && by >= flagStartY && by < flagStartY + flagCache.length) {
						flagCache[by - flagStartY][bx - flagStartX] = on;
						changed = true;
					}
				}
			}
		}
		if (changed) {
			saveFlags();
			fireContentChanged();
			redraw();
		}
	}
	
	public EdgeEditViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND, GLUtils.glEnabled);

		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					// 判断是否点到左下角按钮区
					Point pt = new Point(e.x, e.y);
			        for (int i = 0; i < buttonBounds.length; i++) {
			            if (buttonBounds[i].contains(pt)) {
			                return;
			            }
			        }
			        
			        if (selectedButtonIndex == 0) {
			        	// 设置
			        	switchGrid(new Point[] { pt }, getBrushSize(), true);
			        } else {
			        	// 抹除
			        	switchGrid(new Point[] { pt }, getBrushSize(), false);
			        }
					dragging = true;
					lastDragPoint = pt;
				}
			}
			public void mouseUp(MouseEvent e) {
				dragging = false;
				lastX = e.x;
				lastY = e.y;
				
				// 检查是否点到按钮选择框
		        Point pt = new Point(e.x, e.y);
		        for (int i = 0; i < buttonBounds.length; i++) {
		            if (buttonBounds[i].contains(pt)) {
		                if (i <= 1) {
		                	selectedButtonIndex = i;
		                } else {
		                	selectedBrushSize = i - 1;
		                }
		                break;
		            }
		        }
		        
		        redraw();
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (e.button == 3) {
					playAnimate = !playAnimate;
				}
			}
		});
		
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				lastX = e.x;
				lastY = e.y;
				if (dragging) {
					Point pt = new Point(e.x, e.y);
					// 从lastDragPoint到e.x,e.y之间画一条线，不包括起始点
					if (selectedButtonIndex == 0) {
			        	// 设置
			        	switchGrid(getPointsOnLine(lastDragPoint, pt), getBrushSize(), true);
			        } else {
			        	// 抹除
			        	switchGrid(getPointsOnLine(lastDragPoint, pt), getBrushSize(), false);
			        }
					lastDragPoint = pt;
				}
				redraw();
			}
		});
		
		display = this.getDisplay();
		animateThread = new Thread(this);
		animateThread.start();
	}
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input != null) {
			int startIndex = edge.beginAnimateIndex;
			if (startIndex == -1) {
				startIndex = 0;
			}
			PipAnimate animate = animateSet.getAnimate(startIndex);
			int drawX = size.x / 2 + paintOffset.x;
			int drawY = size.y / 2 + paintOffset.y;
			animate.drawAnimateFrame(gc, currentTime, drawX, drawY, ratio, null);
		}
	}
	
	protected void paintInput(GLGraphics gc) {
		Point size = getSize();
		if (input != null) {
			int startIndex = edge.beginAnimateIndex;
			if (startIndex == -1) {
				startIndex = 0;
			}
			PipAnimate animate = animateSet.getAnimate(startIndex);
			int drawX = size.x / 2 + paintOffset.x;
			int drawY = size.y / 2 + paintOffset.y;
			animate.drawAnimateFrame(gc, currentTime, drawX, drawY, ratio, imageCache);
		}
	}
	
	protected void drawButtons(GC gc) {
    	Point size = getSize();
        gc.setBackground(getBackground());
        gc.setForeground(AbstractImageViewer.invert(getBackground()));
        int bx = 1;
        int i;
        for (i = 0; i < buttonTexts.length; i++) {
        	Point ts = gc.textExtent(buttonTexts[i]);
        	int by = size.y - ts.y - 8;
        	int bw = ts.x + 7;
        	int bh = ts.y + 6;
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
            if (i == selectedButtonIndex || i == selectedBrushSize + 1) {
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            }else{
            	gc.setForeground(AbstractImageViewer.invert(getBackground()));
            }
            gc.drawRectangle(buttonBounds[i]);
            gc.drawText(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4);
            if (i == selectedButtonIndex) {
                gc.setForeground(AbstractImageViewer.invert(getBackground()));
            }
            bx += bw + 2;
        }		
	}
	
	protected void drawButtons(GLGraphics gc) {
    	Point size = getSize();
        gc.setColor(AbstractImageViewer.invert(getBackground()));
        int bx = 1;
        int i;
        for (i = 0; i < buttonTexts.length; i++) {
        	Point ts = gc.textExtent(buttonTexts[i]);
        	int by = size.y - ts.y - 8;
        	int bw = ts.x + 7;
        	int bh = ts.y + 6;
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
            if (i == selectedButtonIndex || i == selectedBrushSize + 1) {
                gc.setColor(SWTResourceManager.getColor(SWT.COLOR_RED));
            }else{
            	gc.setColor(AbstractImageViewer.invert(getBackground()));
            }
            gc.drawRect(buttonBounds[i]);
            gc.drawText(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4);
            if (i == selectedButtonIndex) {
                gc.setColor(AbstractImageViewer.invert(getBackground()));
            }
            bx += bw + 2;
        }		
	}
	
	protected void drawMouseOverRect(GC gc) {
    	Point p = new Point(lastX, lastY);
    	p = calcCoord(p);
    	int brushSize = getBrushSize();
    	int bs1 = -brushSize / 2;
		int bs2 = brushSize + bs1;
    	Point p1 = coordToPos(new Point(p.x + bs1, p.y + bs1));
    	Point p2 = coordToPos(new Point(p.x + bs2, p.y + bs2));
    	Rectangle rect = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
    	gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
    	gc.drawRectangle(rect);
	}
	
	protected void drawMouseOverRect(GLGraphics gc) {
    	Point p = new Point(lastX, lastY);
    	p = calcCoord(p);
    	int brushSize = getBrushSize();
    	int bs1 = -brushSize / 2;
		int bs2 = brushSize + bs1;
    	Point p1 = coordToPos(new Point(p.x + bs1, p.y + bs1));
    	Point p2 = coordToPos(new Point(p.x + bs2, p.y + bs2));
    	Rectangle rect = new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
    	gc.setColor(SWTResourceManager.getColor(SWT.COLOR_RED));
    	gc.drawRect(rect);
	}
	
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		gc.setForeground(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
		gc.drawLine(0, size.y / 2 + paintOffset.y, size.x, size.y / 2 + paintOffset.y);
		gc.drawLine(size.x / 2 + paintOffset.x, 0, size.x / 2 + paintOffset.x, size.y);
		
		// 绘制选中的点，画成红色
		if (input != null) {
			gc.setAlpha(0xC0);
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
			for (int y = 0; y < flagCache.length; y++) {
				for (int x = 0; x < flagCache[y].length; x++) {
					if (flagCache[y][x]) {
						int ry = y + flagStartY;
						int rx = x + flagStartX;
						ry = (int)(ry * ratio) + size.y / 2 + paintOffset.y;
						rx = (int)(rx * ratio) + size.x / 2 + paintOffset.x;
						int rw = (int)ratio;
						int rh = (int)ratio;
						gc.fillRectangle(rx, ry, rw, rh);
					}
				}
			}
			gc.setAlpha(0xFF);
		}
		
		// 绘制选项按钮
		drawButtons(gc);
		drawMouseOverRect(gc);
	}
	
	protected void drawInformation(GLGraphics gc) {
		super.drawInformation(gc);
		
		Point size = getSize();
		gc.setColor(SWTResourceManager.getColor(0xFF, 0x99, 0x00));
		gc.drawLine(0, size.y / 2 + paintOffset.y, size.x, size.y / 2 + paintOffset.y);
		gc.drawLine(size.x / 2 + paintOffset.x, 0, size.x / 2 + paintOffset.x, size.y);
		
		// 绘制选中的点，画成红色
		if (input != null) {
			gc.setColor(0xC0FF0000);
			for (int y = 0; y < flagCache.length; y++) {
				for (int x = 0; x < flagCache[y].length; x++) {
					if (flagCache[y][x]) {
						int ry = y + flagStartY;
						int rx = x + flagStartX;
						ry = (int)(ry * ratio) + size.y / 2 + paintOffset.y;
						rx = (int)(rx * ratio) + size.x / 2 + paintOffset.x;
						int rw = (int)ratio;
						int rh = (int)ratio;
						gc.fillRect(rx, ry, rw, rh);
					}
				}
			}
			gc.setColor(0xFF000000);
		}
		
		// 绘制选项按钮
		drawButtons(gc);
		drawMouseOverRect(gc);
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
        	if (playAnimate) {
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
