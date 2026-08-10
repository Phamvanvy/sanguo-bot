package com.pip.image.workshop.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mango.jni.GLWindow;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display animation.
 */
abstract public class AbstractImageViewer extends Canvas implements PaintListener, DisposeListener {
	protected double ratio = 1.0;
	protected ToolBar toolBar;
	protected Image bufferImg;
	protected Object input;
	protected ImageViewerListener listener;
	protected ImageViewerListenerEx listenerEx;
	protected Image zoominImg, zoomoutImg, choosecolorImg, gridImg, centerImg;
	protected boolean showGrid;
	protected Point paintOffset;
	protected boolean draggingOffset;
	protected Point previousPoint;
	protected Menu oldMenu;
	protected int keyEventMask;
	
	protected boolean glMode = false;
	protected GLWindow glWin;
	protected GLGraphics glgc;
	
	private static RGB defaultBackgroundColor = new RGB(0xEE, 0xF2, 0xFB);

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AbstractImageViewer(Composite parent, int style) {
		this(parent, style, false);
	}
	
	public AbstractImageViewer(Composite parent, int style, boolean glMode) {
		super(parent, style | SWT.NO_BACKGROUND);
		this.glMode = glMode;
		addPaintListener(this);
		setBackground(SWTResourceManager.getColor(defaultBackgroundColor));
		paintOffset = new Point(0, 0);

		zoominImg = ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/zoomin.gif");
		zoomoutImg = ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/zoomout.gif");
		choosecolorImg = ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/choosecolor.gif");
		gridImg = ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/grid.gif");
		centerImg = ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/center.png");
		
		addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                redraw();
            }

            public void focusLost(FocusEvent e) {
                redraw();
            }
        });
        addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_TAB_NEXT
                        || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    e.doit = true;
                }
            };
        });
        addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
            	keyEventMask = event.stateMask;
            	onKeyDown(event.keyCode);
            }
        });
        addListener(SWT.KeyUp, new Listener() {
            public void handleEvent(Event event) {
            	keyEventMask = event.stateMask;
                onKeyUp(event.keyCode);
            }
        });
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (draggingOffset) {
					if (oldMenu == null) {
						oldMenu = AbstractImageViewer.this.getMenu();
						AbstractImageViewer.this.setMenu(null);
					}
					paintOffset.x += e.x - previousPoint.x;
					paintOffset.y += e.y - previousPoint.y;
					previousPoint = new Point(e.x, e.y);
					redraw();
				}
			}
		});
        addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					int btn = getButtonAt(e.x, e.y);
					switch (btn) {
					case 0:
						zoomin();
						break;
					case 1:
						zoomout();
						break;
					case 2:
						chooseColor();
						break;
					case 3:
						switchShowGrid();
						break;
					case 4:
						center();
						break;
					}
				}
				if (e.button == 3) {
					if (draggingOffset == false) {
						draggingOffset = true;
						previousPoint = new Point(e.x, e.y);
					} else {
						draggingOffset = false;
					}
				}
			}
			
			public void mouseUp(MouseEvent e) {
				if (e.button == 3 && draggingOffset) {
					draggingOffset = false;
					if (oldMenu != null) {
						AbstractImageViewer.this.setMenu(oldMenu);
						oldMenu = null;
					}
				}
			}
		});
        this.addMouseWheelListener(new MouseWheelListener() {
            public void mouseScrolled(MouseEvent e) {
                if (e.count > 0) {
                    zoomin();
                } else {
                    zoomout();
                }
            }
        });
        addDisposeListener(this);
        if (glMode) {
        	glWin = new GLWindow(this.handle);
        	glgc = new GLGraphics();
        }
	}
	
	public boolean isOpenGLMode() {
		return glMode;
	}

	protected void center() {
		paintOffset.x = paintOffset.y = 0;
//		System.out.println("reset");
		redraw();
	}

	protected boolean isInButtonArea(int x, int y) {
		return x >= 0 && x <= 80 && y >= 0 && y <= 22;
	}
	
	protected int getButtonAt(int x, int y) {
		Rectangle rect = new Rectangle(4, 4, 16, 16);
		for (int i = 0; i < 5; i++) {
			if (rect.contains(x, y)) {
				return i;
			}
			rect.x += 18;
		}
		return -1;
	}
	
	public void zoomin() {
		if (ratio < 64) {
			ratio *= 2;
			paintOffset.x *= 2;
			paintOffset.y *= 2;
			redraw();
		}
	}
	
	public void zoomout() {
		if (ratio > 0.125) {
			ratio /= 2;
			paintOffset.x /= 2;
			paintOffset.y /= 2;
			redraw();
		}
	}
	
	protected void chooseColor() {
		ColorDialog dlg = new ColorDialog(getShell());
		RGB newColor = dlg.open();
		if (newColor != null) {
			defaultBackgroundColor = newColor;
			setBackground(SWTResourceManager.getColor(newColor));
			if(toolBar != null){
				toolBar.setBackground(SWTResourceManager.getColor(newColor));
			}
			redraw();
		}
	}
	
	protected void switchShowGrid() {
		showGrid = !showGrid;
		redraw();
	}

	public void widgetDisposed(DisposeEvent e) {
		removePaintListener(this);
		if (bufferImg != null) {
			bufferImg.dispose();
		}
		if (glWin != null) {
			glWin.dispose();
		}
		if (glgc != null) {
			glgc.dispose();
		}
	}
	
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected void drawGrid(GC gc) {
		if (showGrid) {
			Point size = getSize();
			int gridsize = 6;
			if (ratio > gridsize) {
				gridsize = (int)ratio;
			}
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
			
			// Draw horizontal lines
			int y = size.y / 2;
			while (y > 0) {
				gc.drawLine(0, y, size.x, y);
				y -= gridsize;
			}
			y = size.y / 2;
			while (y < size.y) {
				gc.drawLine(0, y, size.x, y);
				y += gridsize;
			}
			
			// Draw vertical lines
			int x = size.x / 2;
			while (x > 0) {
				gc.drawLine(x, 0, x, size.y);
				x -= gridsize;
			}
			x = size.x / 2;
			while (x < size.x) {
				gc.drawLine(x, 0, x, size.y);
				x += gridsize;
			}
		}
	}
	
	protected void drawBackground(GC gc) {
		gc.setBackground(getBackground());
		Point size = getSize();
		gc.fillRectangle(0, 0, size.x, size.y);
	}
	
	protected void drawInformation(GC gc) {
		Point size = getSize();
		String ratioStr = String.valueOf(ratio * 100) + "%";
		Point ts = gc.textExtent(ratioStr);
		gc.setForeground(invert(getBackground()));
		gc.setBackground(getBackground());
		gc.drawRectangle(size.x - ts.x - 9, 1, ts.x + 7, ts.y + 6);
		gc.drawText(ratioStr, size.x - ts.x - 5, 4);
		
		String info1 = "W-放大 S-缩小 按住鼠标右键可拖动显示区域";
        ts = gc.textExtent(info1);
        gc.drawText(info1, size.x / 2 - ts.x / 2, 5);
	}
	
	private void drawButtons(GC gc) {
		if(zoominImg == null){
			return;
		}
		gc.drawImage(zoominImg, 4, 4);
		gc.drawImage(zoomoutImg, 22, 4);
		gc.drawImage(choosecolorImg, 40, 4);
		gc.drawImage(gridImg, 58, 4);
		//center
		gc.drawImage(centerImg, 76, 4);
//		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
//		gc.drawRectangle(76, 4, 16, 16);
	}
	private boolean hasError = false;
	public void paintControl(PaintEvent e) {
		if(hasError){
			return;
		}
		if (!glMode) {
		    GC bufferGC = null;
		    try {
	    		Point size = getSize();
	    		if (bufferImg != null && (bufferImg.getBounds().width != size.x || bufferImg.getBounds().height != size.y)) {
	    			bufferImg.dispose();
	    			bufferImg = null;
	    		}
	    		if (bufferImg == null) {
	    			bufferImg = new Image(getDisplay(), size.x, size.y);
	    		}
	   			bufferGC = new GC(bufferImg);
	    		bufferGC.setClipping(0, 0, size.x, size.y);
			    drawBackground(bufferGC);
		        paintInput(bufferGC);
	    		drawGrid(bufferGC);
	    		drawButtons(bufferGC);
	   		    drawInformation(bufferGC);
	    		if (isFocusControl()) {
	    			drawFocus(bufferGC);
	    		}
	    		e.gc.drawImage(bufferImg, 0, 0);
		    } catch (Throwable e1) {
		        e1.printStackTrace();
	//	        MessageDialog.openError(getShell(), "Error 3", e1.toString());
		        hasError = true;
		    } finally {
		        if (bufferGC != null) {
		            bufferGC.dispose();
		        }
		    }
		} else {
			try {
				glgc.setZ(-99999.0f);
				drawBackground(glgc);
				paintInput(glgc);
				glgc.setZ(0.0f);
				drawGrid(glgc);
				drawButtons(glgc);
				drawInformation(glgc);
				if (isFocusControl()) {
					drawFocus(glgc);
				}
				glgc.setColor(0xFF0000);
				Point size = getSize();
				glgc.drawString("OpenGL", 2, size.y - 40, GLGraphics.TOP | GLGraphics.LEFT);
				glWin.draw(glgc);
				glgc.clear();
			} catch (Throwable e1) {
				e1.printStackTrace();
				hasError = true;
			}
		}
	}
	
	protected void drawBackground(GLGraphics gc) {
		gc.setColor(0xFF, getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue());
		Point size = getSize();
		gc.fillRect(0, 0, size.x, size.y);
	}
	
	protected void paintInput(GLGraphics gc) {
	}
	
	protected void drawGrid(GLGraphics gc) {
		if (showGrid) {
			Point size = getSize();
			int gridsize = 6;
			if (ratio > gridsize) {
				gridsize = (int)ratio;
			}
			Color c = getDisplay().getSystemColor(SWT.COLOR_GRAY);
			gc.setColor(0xFF, c.getRed(), c.getGreen(), c.getBlue());
			
			// Draw horizontal lines
			int y = size.y / 2;
			while (y > 0) {
				gc.drawLine(0, y, size.x, y);
				y -= gridsize;
			}
			y = size.y / 2;
			while (y < size.y) {
				gc.drawLine(0, y, size.x, y);
				y += gridsize;
			}
			
			// Draw vertical lines
			int x = size.x / 2;
			while (x > 0) {
				gc.drawLine(x, 0, x, size.y);
				x -= gridsize;
			}
			x = size.x / 2;
			while (x < size.x) {
				gc.drawLine(x, 0, x, size.y);
				x += gridsize;
			}
		}
	}
	
	private void drawButtons(GLGraphics gc) {
		if(zoominImg == null){
			return;
		}
		gc.drawTexture(GLUtils.loadImage(zoominImg), 0, 0, 4, 4);
		gc.drawTexture(GLUtils.loadImage(zoomoutImg), 0, 0, 22, 4);
		gc.drawTexture(GLUtils.loadImage(choosecolorImg), 0, 0, 40, 4);
		gc.drawTexture(GLUtils.loadImage(gridImg), 0, 0, 58, 4);
		gc.drawTexture(GLUtils.loadImage(centerImg), 0, 0, 76, 4);
	}
	
	protected void drawInformation(GLGraphics gc) {
		Point size = getSize();
		String ratioStr = String.valueOf(ratio * 100) + "%";
		int tw = gc.stringWidth(ratioStr);
		int th = gc.getFontHeight();
		Color c = invert(getBackground());
		gc.setColor(0xFF, c.getRed(), c.getGreen(), c.getBlue());
		gc.drawRect(size.x - tw - 9, 1, tw + 7, th + 6);
		gc.drawString(ratioStr, size.x - tw - 5, 4, GLGraphics.TOP | GLGraphics.LEFT);
		
		String info1 = "W-放大 S-缩小 按住鼠标右键可拖动显示区域";
        tw = gc.stringWidth(info1);
        gc.drawString(info1, size.x / 2 - tw / 2, 5, GLGraphics.TOP | GLGraphics.LEFT);
	}
	
	protected void drawFocus(GLGraphics gc) {
		Color c = getDisplay().getSystemColor(SWT.COLOR_BLUE);
		gc.setColor(0xFF, c.getRed(), c.getGreen(), c.getBlue());
		gc.drawRect(0, 0, getSize().x - 1, getSize().y - 1);
	}
	
	protected void drawFocus(GC gc) {
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		gc.drawRectangle(0, 0, getSize().x - 1, getSize().y - 1);
	}
	
	abstract protected void paintInput(GC gc);
	
	public static Color invert(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		if (r > 112 && r < 144) {
			r = 255 - r + ((r < 128) ? 16 : -16);
		} else {
			r = 255 - r;
		}
		if (g > 112 && g < 144) {
			g = 255 - g + ((g < 128) ? 16 : -16);
		} else {
			g = 255 - g;
		}
		if (b > 112 && b < 144) {
			b = 255 - b + ((b < 128) ? 16 : -16);
		} else {
			b = 255 - b;
		}
		return SWTResourceManager.getColor(r, g, b);
	}
	
	protected int fitGrid(int value) {
		double d = Math.round(value / ratio);
		return (int)(d * ratio);
	}
	
	protected Rectangle zoom(Rectangle r) {
		return new Rectangle((int)(r.x * ratio), (int)(r.y * ratio), (int)(r.width * ratio), (int)(r.height * ratio));
	}
	
	public void setInput(Object input) {
		this.input = input;
		this.paintOffset = new Point(0, 0);
	}
	
	public void setImageViewerListener(ImageViewerListener l) {
		listener = l;
	}
	
	public void setImageViewerListenerEx(ImageViewerListenerEx l) {
		listenerEx = l;
	}
	
	protected void fireAreaSelected() {
		if (listener != null) {
			listener.areaSelected(this);
		}
	}
	
	protected void fireFrameSelectionChanged(int newFrame) {
		if (listener != null) {
			listener.frameSelectionChanged(this, newFrame);
		}
	}
	
	protected void fireFrameDoubleClicked(int frame) {
		if (listener != null) {
			listener.frameDoubleClicked(this, frame);
		}
	}
	
	public void fireContentChanged() {
		if (listener != null) {
			listener.contentChanged(this);
		}
	}
	
	public void fireTransferFrame() {
		if (listenerEx != null) {
			listenerEx.onTransferFrame(this);
		}
	}
	
	protected void onKeyDown(int keyCode) {
		if (keyCode == 'w') {
			zoomin();
		} else if (keyCode == 's') {
			zoomout();
		} else if (keyCode == 'g') {
			switchShowGrid();
		}
	}
	
	protected void onKeyUp(int keyCode) {
	}
	
	public double getRatio() {
	    return ratio;
	}

    /**
     * 在指定大小的窗口中计算一组图片的最佳布局。
     * @param windowSize 窗口大小 
     * @param frameBounds 图片的尺寸
     * @return
     */
    public static Rectangle[] getBestLayout(Point windowSize, Rectangle[] frameBounds) {
        // 计算所有帧占用的大小
        int w = 0, h = 0;
        int count = frameBounds.length;
        for (int i = 0; i < count; i++) {
            w += frameBounds[i].width + 2;
            if (frameBounds[i].height + 2 > h) {
                h = frameBounds[i].height + 2;
            }
        }

        // 计算一个最合适布局比例
        double rratio = (double)windowSize.y / windowSize.x;
        double bestWidth = Math.sqrt(w * h / rratio);
        w = (int)Math.ceil(bestWidth);
        Rectangle[] ret = new Rectangle[count + 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Rectangle(0, 0, 0, 0);
        }
        
        // 布局
        int rw = 0, lh = 0, dx = 0, dy = 0;
        for (int i = 0; i < count; i++) {
            int pw = frameBounds[i].width;
            int ph = frameBounds[i].height;
            if (dx != 0 && dx + pw + 2 > w && dx > 0) {
                dx = 0;
                dy += lh;
                lh = 0;
                i--;
                continue;
            } else {
                ret[i + 1].x = dx;
                ret[i + 1].y = dy;
                ret[i + 1].width = pw;
                ret[i + 1].height = ph;
                dx += pw + 2;
                if (lh < ph + 2) {
                    lh = ph + 2;
                }
                if (dx > rw) {
                    rw = dx;
                }
            }
        }
        ret[0].width = rw;
        ret[0].height = dy + lh;
        return ret;
    }
    public void response(Object source, Object data){
    	
    }
}
