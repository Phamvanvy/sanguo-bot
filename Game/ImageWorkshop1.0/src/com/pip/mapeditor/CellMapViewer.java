package com.pip.mapeditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pip.mapeditor.data.CellMap;
import com.pip.mapeditor.data.GameMap;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display and edit cell map.
 */
public class CellMapViewer extends AbstractImageViewer {
    private CellMap map;

    private boolean isDragging;
    private Point dragStart;
    private int hoverRow;
    private int hoverCol;
    private static int[] COLORS = new int[] { 
        0xFFFFFF, 0x000000, 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0x00FFFF, 0xFF00FF,
        0x808080, 0x800080, 0x808000, 0x008080, 0x800000, 0x008000, 0x000080, 0xFF0080
    };
    
    public void setInput(Object input) {
        super.setInput(input);
        map = (CellMap)input;
        hoverRow = -1;
        hoverCol = -1;
        isDragging = false;
    }
    
    public void zoomout() {
        if (ratio > 1) {
            ratio /= 2;
            redraw();
        }
    }
    
    /**
     * Create the composite
     * @param parent
     * @param style
     */
    public CellMapViewer(Composite parent, int style) {
        super(parent, style | SWT.NO_BACKGROUND);
        this.ratio = 8.0;
        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (isInButtonArea(e.x, e.y)) {
                    return;
                }
                Point pt = new Point(e.x, e.y);
                if (e.button == 1) {
                    Point cell = pointToCell(pt);
                    if (cell.x == -1) {
                        return;
                    }
                    isDragging = true;
                    dragStart = cell;
                    map.data[cell.y][cell.x]++;
                    if (map.data[cell.y][cell.x] >= map.depth) {
                        map.data[cell.y][cell.x] = 0;
                    }
                    redraw();
                }
            }
            public void mouseUp(MouseEvent e) {
                if (isInButtonArea(e.x, e.y)) {
                    return;
                }
                if (e.button == 1) {
                    isDragging = false;
                    fireContentChanged();
                }
            }
            public void mouseDoubleClick(MouseEvent e) {
            }
        });
        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                Point p = pointToCell(new Point(e.x, e.y));
                setHover(p.y, p.x);
                if (isDragging && p.x != -1) {
                    map.data[p.y][p.x] = map.data[dragStart.y][dragStart.x];
                    redraw();
                }
            }
        });
    }
    
    protected void paintInput(GC gc) {
        if (input == null) {
            return;
        }
        Point size = getSize();
        int mapx = (int)(size.x - map.width * ratio) / 2 + paintOffset.x;
        int mapy = (int)(size.y - map.height * ratio) / 2 + paintOffset.y;
        
        // 绘制地图
        int cw = (int)ratio;
        int ch = (int)ratio;
        for (int y = 0; y < map.height; y++) {
            int cy = (int)(mapy + y * ratio);
            for (int x = 0; x < map.width; x++) {
                int cx = (int)(mapx + x * ratio);
                int color = COLORS[map.data[y][x]];
                gc.setBackground(SWTResourceManager.getColor(color >> 16, (color >> 8) & 0xFF, color & 0xFF));
                gc.fillRectangle(cx, cy, cw, ch);
                if (map.depth > 2) {
	                gc.setForeground(invert(gc.getBackground()));
	                gc.drawString(String.valueOf(map.data[y][x]), cx + cw / 2 - 3, cy + ch / 2 - 5);
                }
            }
        }
    }
    
    protected void drawInformation(GC gc) {
        super.drawInformation(gc);
        
        if (input == null) {
            return;
        }
        Point size = getSize();
        if (hoverRow != -1 && hoverCol != -1) {
            Rectangle rect = this.getCellRect(hoverRow, hoverCol);
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            gc.drawRectangle(rect.x, rect.y, rect.width - 1, rect.height - 1);
            
            // 绘制座标
            String coordStr = hoverCol + "," + hoverRow;
            Point ts = gc.textExtent(coordStr);
            gc.setForeground(invert(getBackground()));
            gc.setBackground(getBackground());
            gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
            gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
        }
    }
    
    private Point pointToCell(Point p, boolean verify) {
        if (map == null) {
            return new Point(-1, -1);
        }
        Point size = getSize();
        int offx = (int)(size.x - map.width * ratio) / 2;
        int offy = (int)(size.y - map.height * ratio) / 2;
        offx += paintOffset.x;
        offy += paintOffset.y;
        offx = (int)((p.x - offx) / ratio);
        offy = (int)((p.y - offy) / ratio);
        if (offx < 0 || offy < 0 || offx >= map.width || offy >= map.height) {
            if (verify) {
                return new Point(-1, -1);
            }
        }
        return new Point(offx, offy);
    }
    
    private Point pointToCell(Point p) {
        return pointToCell(p, true);
    }
    
    private Rectangle getCellRect(int row, int col) {
        Point size = getSize();
        int offx = (int)(size.x - map.width * ratio) / 2;
        int offy = (int)(size.y - map.height * ratio) / 2;
        offx += paintOffset.x;
        offy += paintOffset.y;
        offx += col * ratio;
        offy += row * ratio;
        return new Rectangle(offx, offy, (int)(ratio), (int)(ratio));
    }
    
    private void setHover(int row, int col) {
        if (row == hoverRow && col == hoverCol) {
            return;
        }
        hoverRow = row;
        hoverCol = col;
        redraw();
    }
}
