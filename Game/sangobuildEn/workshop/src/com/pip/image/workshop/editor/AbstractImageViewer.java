package com.pip.image.workshop.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.pip.image.workshop.WorkshopPlugin;
import com.pipimage.image.*;
import com.pipimage.png.PngEncoder;
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
	protected Image zoominImg, zoomoutImg, choosecolorImg, gridImg, centerImg;
	protected boolean showGrid;
	protected Point paintOffset;
	protected boolean draggingOffset;
	protected Point previousPoint;
	protected Menu oldMenu;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public AbstractImageViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addPaintListener(this);
		setBackground(SWTResourceManager.getColor(0xEE, 0xF2, 0xFB));
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
            	onKeyDown(event.keyCode);
            }
        });
        addListener(SWT.KeyUp, new Listener() {
            public void handleEvent(Event event) {
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
			redraw();
		}
	}
	
	public void zoomout() {
		if (ratio > 0.125) {
			ratio /= 2;
			redraw();
		}
	}
	
	protected void chooseColor() {
		ColorDialog dlg = new ColorDialog(getShell());
		RGB newColor = dlg.open();
		if (newColor != null) {
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
		if (bufferImg != null) {
			bufferImg.dispose();
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
