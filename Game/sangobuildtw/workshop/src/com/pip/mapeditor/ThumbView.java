package com.pip.mapeditor;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pipimage.image.*;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;
import com.pip.mapeditor.data.*;

/**
 * A widget to display map.
 */
public class ThumbView extends AbstractImageViewer {
    public final static int CELL_SIZE = 8;
	private GameMap map;
	
	public void setInput(Object input) {
		super.setInput(input);
		map = (GameMap)input;
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
	public ThumbView(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
	}
	
	protected void paintInput(GC gc) {
		if (input == null) {
			return;
		}
		Point size = getSize();
		
		// 把地图数据转换为格点数据，每个格点占8x8的位置。
		int cols = map.width / CELL_SIZE;
		int rows = map.height / CELL_SIZE;
		int[][] data = new int[rows][cols];
		ArrayList<TileInfo> accTileInfo = map.parent.getTileImage().tileInfo;
		for (IMapLayer layer : map.layers) {
		    if (layer instanceof AccurateMapLayer) {
		        AccurateMapLayer al = (AccurateMapLayer)layer;
		        short[][] mapData = al.getLayerData();
		        int w = mapData[0].length;
		        int h = mapData.length;
		        int cellw = map.parent.getTileWidth() / CELL_SIZE;
		        int cellh = map.parent.getTileHeight() / CELL_SIZE;
		        for (int i = 0; i < h ; i++) {
		            for (int j = 0; j < w; j++) {
		                short md = mapData[i][j];
		                if (md == -1) {
		                    continue;
		                }
		                TileInfo tinfo = accTileInfo.get(md);
		                int tc = tinfo.thumbColor;
		                int startrow = i * cellh;
		                int startcol = j * cellw;
		                for (int k = 0; k < cellh; k++) {
		                    Arrays.fill(data[startrow + k], startcol, startcol + cellw, tc);
		                }
		            }
		        }
		    } else if (layer instanceof BlurMapLayer) {
		        BlurMapLayer al = (BlurMapLayer)layer;
		        int[][][] mapData = al.getMapData();
                int w = mapData[0].length;
                int h = mapData.length;
                int cellw = map.parent.getBlurTileWidth() / CELL_SIZE;
                int cellh = map.parent.getBlurTileHeight() / CELL_SIZE;
                for (int i = 0; i < h ; i++) {
                    for (int j = 0; j < w; j++) {
                        int[] md = mapData[i][j];
                        for (int k = md.length - 1; k >= 0; k--) {
                            if (md[k] != -1) {
                                int startrow = i * cellh;
                                int startcol = j * cellw;
                                int lfid = md[k] >> 16;
                                int tid = md[k] & 0xFF;
                                int tc = map.parent.getLandforms().get(lfid).tileInfo.get(tid).thumbColor;
                                for (int m = 0; m < cellh; m++) {
                                    Arrays.fill(data[startrow + m], startcol, startcol + cellw, tc);
                                }
                                break;
                            }
                        }
                    }
                }
		    }
		}

        int aw = (int)(cols * ratio);
        int ah = (int)(rows * ratio);
		int offx = ((int)(size.x - aw) / 2) + paintOffset.x;
		int offy = ((int)(size.y - ah) / 2) + paintOffset.y;

		// 绘制底层透明背景
		MapViewer.paintTransparentBackground(gc, offx, offy, aw, ah);

		// 绘制点阵
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (data[i][j] != -1) {
					int cx = offx + (int)(j * ratio);
					int cy = offy + (int)(i * ratio);
					int tc = data[i][j];
					gc.setBackground(SWTResourceManager.getColor((tc >> 16) & 0xFF, (tc >> 8) & 0xFF, tc & 0xFF));
					gc.fillRectangle(cx, cy, (int)(ratio), (int)(ratio));
				}
			}
		}
	}
}
