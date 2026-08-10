package com.pip.image.workshop.font;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.pip.image.workshop.editor.AbstractImageViewer;

public class FontViewer extends AbstractImageViewer {
	private Image fontBuffer;
	private int showCols;
	private int[] chars;
	
	public void setInput(Object input) {
		super.setInput(input);
		if (fontBuffer != null) {
			fontBuffer.dispose();
			fontBuffer = null;
		}
		redraw();
	}
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public FontViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
					if (fontBuffer == null) {
						return;
					}
					Point size = getSize();
					int mapx = (int)(size.x - fontBuffer.getBounds().width * ratio) / 2 + paintOffset.x;
					int mapy = (int)(size.y - fontBuffer.getBounds().height * ratio) / 2 + paintOffset.y;
					int xx = (int)((e.x - mapx) / ratio);
					int yy = (int)((e.y - mapy) / ratio);
					FontData font = (FontData)input;
					int col = xx / (font.width + 2);
					int row = yy / (font.height + 2);
					int index = row * showCols + col;
					int ch = chars[index];
					beginEdit(ch, col, row);
				}
			}
		});
	}
	
	private void createFontBuffer() {
		FontData font = (FontData)input;
		int cols = 800 / font.width;
		int rows = (font.charPixels.size() + cols - 1) / cols;
		int bwidth = (font.width + 2) * cols;
		int bheight = (font.height + 2) * rows;
		
		fontBuffer = new Image(getDisplay(), bwidth, bheight);
        GC gc = new GC(fontBuffer);
        
        gc.setBackground(this.getBackground());
        gc.fillRectangle(0, 0, bwidth, bheight);
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        
        chars = new int[font.charPixels.size()];
        int index = 0;
        for (int ch = 0; ch < 65536; ch++) {
        	if (!font.charPixels.containsKey(ch)) {
        		continue;
        	}
        	chars[index] = ch;
        	int chx = (index % cols) * (font.width + 2) + 1;
        	int chy = (index / cols) * (font.height + 2) + 1;
        	byte[] pdata = font.charPixels.get(ch);
        	for (int y = 0; y < font.height; y++) {
        		for (int x = 0; x < font.width; x++) {
        			if (pdata[y * font.width + x] != 0) {
        				gc.drawPoint(x + chx, y + chy);
        			}
        		}
        	}
        	index++;
        	if ((index % cols) == 0) {
        		int liney = (index / cols) * (font.height + 2) - 1;
        		gc.drawLine(0, liney, bwidth, liney);
        	}
        }
        gc.dispose();
        showCols = cols;
	}
	
	private void updateFontBuffer(int ch, int col, int row) {
		if (fontBuffer == null) {
			return;
		}
		FontData font = (FontData)input;
		
        GC gc = new GC(fontBuffer);
        
        gc.setBackground(this.getBackground());
        gc.fillRectangle(0, 0, font.width + 2, font.height + 1);
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

    	int chx = (col) * (font.width + 2) + 1;
    	int chy = (row) * (font.height + 2) + 1;
    	byte[] pdata = font.charPixels.get(ch);
    	for (int y = 0; y < font.height; y++) {
    		for (int x = 0; x < font.width; x++) {
    			if (pdata[y * font.width + x] != 0) {
    				gc.drawPoint(x + chx, y + chy);
    			}
    		}
    	}
        gc.dispose();
	}
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		if (fontBuffer == null) {
			createFontBuffer();
		}
		int mapx = (int)(size.x - fontBuffer.getBounds().width * ratio) / 2 + paintOffset.x;
		int mapy = (int)(size.y - fontBuffer.getBounds().height * ratio) / 2 + paintOffset.y;
		gc.drawImage(fontBuffer, 0, 0, fontBuffer.getBounds().width, fontBuffer.getBounds().height, 
				mapx, mapy, (int)(fontBuffer.getBounds().width * ratio), 
				(int)(fontBuffer.getBounds().height * ratio));
	}

    public void widgetDisposed(DisposeEvent e) {
        super.widgetDisposed(e);
        if (fontBuffer != null) {
        	fontBuffer.dispose();
        	fontBuffer = null;
        }
    }
    
    /*
     * ±à¼­Ò»¸ö×Ö·û¡£
     */
    private void beginEdit(int ch, int col, int row) {
    	EditCharDialog dlg = new EditCharDialog(getShell(), (FontData)input, ch);
    	if (dlg.open() == Dialog.OK) {
    		((FontData)input).charPixels.put(ch, dlg.getCharData());
    		updateFontBuffer(ch, col, row);
    		redraw();
    	}
    }
}
