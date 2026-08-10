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
 * A widget to edit palette.
 */
public class PaletteEditor extends Canvas implements PaintListener, DisposeListener {
	protected Image bufferImg;
	protected PipImagePalette input;
	protected ImageViewerListener listener;
	protected int hoverIndex;
	protected static final int CELL_WIDTH = 18;
	protected static final int CELL_HEIGHT = 12;
	protected static ColorDialog colorDlg;
	protected Image transImg;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public PaletteEditor(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addPaintListener(this);
		setBackground(SWTResourceManager.getColor(0xEE, 0xF2, 0xFB));
		transImg = ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/transparent.gif");

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
            }
        });
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				int oldHover = hoverIndex;
				hoverIndex = getIndexAt(e.x, e.y);
				if (oldHover != hoverIndex) {
					redraw();
				}
			}
		});
        addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
			}
			
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					int index = getIndexAt(e.x, e.y);
					if (index != -1) {
						chooseColor(index);
					}
				}
				if (e.button == 3) {
					int index = getIndexAt(e.x, e.y);
					if (index != -1) {
						switchTransparent(index);
					}
				}
			}
		});
        addDisposeListener(this);
	}
	
	protected int getIndexAt(int x, int y) {
		if (input == null) {
			return -1;
		}
		Point size = this.getSize();
		int columns = (size.x - 2) / CELL_WIDTH;
		int col = (x - 1) / CELL_WIDTH;
		int row = (y - 1) / CELL_HEIGHT;
		int index = row * columns + col;
		if (col >= columns) {
			return -1;
		}
		if (index >= 0 && index < input.getPalette().length) {
			return index;
		}
		return -1;
	}
	
	protected void chooseColor(int index) {
		if (input == null) {
			return;
		}
		if (colorDlg == null) {
			colorDlg = new ColorDialog(getShell());
		}
		RGB newColor = colorDlg.open();
		if (newColor != null) {
			int clr = 0xFF000000 | (newColor.red << 16) | (newColor.green << 8) | newColor.blue;
			input.getPalette()[index] = clr;
			this.fireContentChanged();
			redraw();
		}
	}
	
	protected void switchTransparent(int index) {
		if (input == null) {
			return;
		}
		int clr = input.getPalette()[index];
		if ((clr & 0xFF000000) != 0) {
			clr &= 0x00FFFFFF;
		} else {
			clr |= 0xFF000000;
		}
		input.getPalette()[index] = clr;
		this.fireContentChanged();
		redraw();
	}

	public void widgetDisposed(DisposeEvent e) {
		if (bufferImg != null) {
			bufferImg.dispose();
		}
		removePaintListener(this);
	}
	
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	protected void drawBackground(GC gc) {
		gc.setBackground(getBackground());
		Point size = getSize();
		gc.fillRectangle(0, 0, size.x, size.y);
	}
	
	public void paintControl(PaintEvent e) {
		Point size = getSize();
		if (bufferImg != null && (bufferImg.getBounds().width != size.x || bufferImg.getBounds().height != size.y)) {
			bufferImg.dispose();
			bufferImg = null;
		}
		if (bufferImg == null) {
			bufferImg = new Image(getDisplay(), size.x, size.y);
		}
		GC bufferGC = new GC(bufferImg);
		drawBackground(bufferGC);
		paintInput(bufferGC);
		if (isFocusControl()) {
			drawFocus(bufferGC);
		}
		bufferGC.dispose();
		e.gc.drawImage(bufferImg, 0, 0);
	}
	
	protected void drawFocus(GC gc) {
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		gc.drawRectangle(0, 0, getSize().x - 1, getSize().y - 1);
	}
	
	protected void paintInput(GC gc) {
		if (input == null) {
			return;
		}
		Point size = this.getSize();
		int columns = (size.x - 2) / CELL_WIDTH;
		Rectangle hoverRect = null;
		for (int i = 0; i < input.getPalette().length; i++) {
			int clr = input.getPalette()[i];
			int x = 1 + (i % columns) * CELL_WIDTH;
			int y = 1 + (i / columns) * CELL_HEIGHT;
			int alpha = (clr >> 24) & 0xFF;
			if (alpha == 0) {
				gc.setClipping(x, y, CELL_WIDTH, CELL_HEIGHT);
				gc.drawImage(transImg, x, y);
				gc.setClipping(0, 0, size.x, size.y);
			} else {
				gc.setAlpha((clr >> 24) & 0xFF);
				Color c = new Color(gc.getDevice(), (clr >> 16) & 0xFF, (clr >> 8) & 0xFF, clr & 0xFF);
				gc.setBackground(c);
				gc.fillRectangle(x, y, CELL_WIDTH, CELL_HEIGHT);
				gc.setBackground(getBackground());
				gc.setAlpha(0xFF);
				c.dispose();
			}
			if (i == this.hoverIndex) {
				hoverRect = new Rectangle(x, y, CELL_WIDTH, CELL_HEIGHT);
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
			} else {
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			}
			gc.drawRectangle(x, y, CELL_WIDTH, CELL_HEIGHT);
		}
		if (hoverRect != null) {
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
			gc.drawRectangle(hoverRect);
		}
	}
	
	protected Color invert(Color c) {
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
	
	public void setInput(PipImagePalette input) {
		this.input = input;
		hoverIndex = -1;
		redraw();
	}
	
	public void setImageViewerListener(ImageViewerListener l) {
		listener = l;
	}
	
	protected void fireContentChanged() {
		if (listener != null) {
			listener.contentChanged(this);
		}
	}
}
