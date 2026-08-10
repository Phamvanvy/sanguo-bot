package com.pip.mapeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.text.html.ImageView;

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
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pip.image.workshop.editor.ImageViewer;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.TileInfo;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.SWTUtils;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImageDraw;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display tiles in a map. 
 */
public class MapTileSelector extends AbstractImageViewer implements SelectionListener, IFileModificationListener {
	private MapFile map;
    private Rectangle[] frameLayout;
    private Rectangle switchButtonBounds;

	private int selectedFrame;
	private int selectingFrame;
	private int hoverFrame;
	private Menu popMenu;
	private Menu colorMenu;
	
	private Point mouseDownPos;
	
    public static final int[] thumbColors = new int[] {
        0x000000, 0x808080, 0xC0C0C0, 0xFFFFFF,
        0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
        0x0000FF, 0xFF00FF, 0xFFFF80, 0x00FF80,
        0x80FFFF, 0x8080FF, 0xFF0080, 0xFF8040
    };
    public static final String[] thumbColorNames = new String[] {
    	"黑色", "灰色", "浅灰色", "白色",
    	"红色", "黄色", "绿色", "青色",
    	"蓝色", "洋红色", "浅黄色", "青绿色",
    	"天蓝色", "紫色", "深粉色", "橙色"
    };
    public static Image[] colorMenuImages = new Image[thumbColors.length];
    
    static {
    	for (int i = 0; i < thumbColors.length; i++) {
    		Image img = new Image(Display.getCurrent(), 64, 16);
    		GC gc = new GC(img);
    		int r = (thumbColors[i] >> 16) & 0xFF;
    		int g = (thumbColors[i] >> 8) & 0xFF;
    		int b = thumbColors[i] & 0xFF;
    		gc.setBackground(SWTResourceManager.getColor(r, g, b));
    		gc.fillRectangle(0, 0, 64, 16);
    		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    		gc.drawRectangle(0, 0, 63, 15);
    		gc.dispose();
    		colorMenuImages[i] = img;
    	}
    }
    private MenuItem[] colorMenuItems;
    private int chooseColorIndex;
    private boolean showExtendedInfo;

	// 用系统编辑器编辑中的图块
    private HashMap<File, Integer> editingFrames;
	
	public void setInput(Object input) {
		super.setInput(input);
		map = (MapFile)input;
		hoverFrame = -1;
		selectedFrame = 0;
		setMenu(null);
		editingFrames = new HashMap<File, Integer>();
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public MapTileSelector(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		ratio = 2.0;
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				hoverFrame = calcPointFrame(new Point(e.x, e.y));
				redraw();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
				    if (switchButtonBounds.contains(e.x, e.y)) {
				        showExtendedInfo = !showExtendedInfo;
				        redraw();
				        return;
				    }
					mouseDownPos = new Point(e.x, e.y);
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
				    if (switchButtonBounds.contains(e.x, e.y)) {
                        return;
                    }
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectingFrame && newFrame != -1) {
						setSelectedFrame(newFrame);
						fireFrameSelectionChanged(selectedFrame);
					}
					checkTileButton(mouseDownPos, new Point(e.x, e.y));
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
			}
		});

		MenuManager mgr = new MenuManager();
		
		mgr.add(new Action("复制") {
			public void run() {
				onDuplicateFrame();
			}
		});
		mgr.add(new Action("复制图片") {
			public void run() {
				onDuplicateImage();
			}
		});
		mgr.add(new Action("水平翻转") {
			public void run() {
				onHflipFrame();
			}
		});
		mgr.add(new Action("垂直翻转") {
			public void run() {
				onVflipFrame();
			}
		});
		mgr.add(new Action("删除") {
			public void run() {
				onDeleteFrame();
			}
		});
		mgr.add(new Separator());
		mgr.add(new Action("用系统编辑器编辑") {
			public void run() {
				onEditFrame();
			}
		});
		popMenu = mgr.createContextMenu(this);
		
		colorMenu = new Menu(this);
		colorMenuItems = new MenuItem[thumbColors.length];
		for (int i = 0; i < thumbColors.length; i++) {
			MenuItem mi = new MenuItem(colorMenu, SWT.PUSH);
			mi.setImage(colorMenuImages[i]);
			mi.setText(thumbColorNames[i]);
			mi.addSelectionListener(this);
			colorMenuItems[i] = mi;
		}
	}
	
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		for (int i = 0; i < colorMenuItems.length; i++) {
			if (colorMenuItems[i] == e.getSource()) {
			    TileInfo tinfo = map.getTileImage().tileInfo.get(chooseColorIndex);
			    tinfo.thumbColor = thumbColors[i];
				fireContentChanged();
				redraw();
				return;
			}
		}
	}

	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
        Point size = getSize();
        String text;
        if (showExtendedInfo) {
            text = "隐藏详细信息";
        } else {
            text = "显示详细信息";
        }
        Point ts = gc.textExtent(text);
        gc.setForeground(invert(getBackground()));
        gc.setBackground(getBackground());
        gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(text, 5, size.y - ts.y - 4);
        
        switchButtonBounds = new Rectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
	}
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		int tw = map.getTileWidth();
		int th = map.getTileHeight();
		
		frameLayout = getBestLayout();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2;
		int count = map.getTileImage().tileInfo.size();
		for (int i = -1; i < count; i++) {
			Rectangle rect = zoom(frameLayout[i + 2]);
			rect.x += offx + paintOffset.x;
			rect.y += offy + paintOffset.y;
			if (i == -1) {
				MapViewer.paintTransparentBackground(gc, rect.x, rect.y, rect.width, rect.height);
			} else {
			    TileInfo tileInfo = map.getTileImage().tileInfo.get(i);
				Image frameImg = map.getTileImage().image.getImageDraw(tileInfo.frameID).createSWTImage(getDisplay(), tileInfo.transit);
				gc.drawImage(frameImg, 0, 0, tw, th, rect.x, rect.y, rect.width, (int)(th * ratio));
				frameImg.dispose();
				
				if (showExtendedInfo) {
    				int tc = tileInfo.thumbColor;
    				int imgh = (int)(th * ratio);
    				gc.setBackground(SWTResourceManager.getColor((tc >> 16) & 0xFF, (tc >> 8) & 0xFF, tc & 0xFF));
    				gc.fillRectangle(rect.x, rect.y + imgh, rect.width / 2, rect.height - imgh);
    				
    				String str = String.valueOf(i);
    				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    				gc.drawText(str, rect.x + 2, rect.y + imgh);
    				
    				if (tileInfo.unpassable) {
    					gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
    				} else {
    					gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    				}
    				gc.fillRectangle(rect.x + rect.width / 2, rect.y + imgh, rect.width / 2, rect.height - imgh);
    				
    				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    				gc.drawLine(rect.x + rect.width / 2, rect.y + imgh, rect.x + rect.width / 2, rect.y + rect.width);
				}
			}
			rect.width--;
			rect.height--;
			if (i + 1 == selectedFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_MAGENTA));
				gc.drawRectangle(rect);
				gc.drawRectangle(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
			}
			if (i + 1 == hoverFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				gc.drawRectangle(rect);
			}
		}
	}
	
	// 检查是否点中了一个Tile的设置区
	private void checkTileButton(Point p1, Point p2) {
		if (frameLayout == null) {
			return;
		}
		Point size = getSize();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2 + paintOffset.x;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2 + paintOffset.y;
		for (int i = 2; i < frameLayout.length; i++) {
			int dx = (int)(frameLayout[i].x * ratio) + offx;
			int dy = (int)(frameLayout[i].y * ratio) + offy;
			int dw = (int)(frameLayout[i].width * ratio);
			int dh = (int)(frameLayout[i].height * ratio);
			TileInfo tileInfo = map.getTileImage().tileInfo.get(i - 2);
			int imgh = (int)(map.getTileImage().image.getImageData(tileInfo.frameID).getHeight() * ratio);
			Rectangle rect1 = new Rectangle(dx, dy + imgh, dw / 2, dh - imgh);
			if (rect1.contains(p1) && rect1.contains(p1)) {
				chooseTileColor(p2, i - 2);
				return;
			}
			Rectangle rect2 = new Rectangle(dx + dw / 2, dy + imgh, dw / 2, dh - imgh);
			if (rect2.contains(p1) && rect2.contains(p1)) {
				switchPassable(i - 2);
				return;
			}
		}
	}
	
	private void chooseTileColor(Point p, int frame) {
		chooseColorIndex = frame;
		p = getDisplay().map(this, null, p);
		colorMenu.setLocation(p);
		colorMenu.setVisible(true);
	}
	
	private void switchPassable(int frame) {
	    TileInfo tileInfo = map.getTileImage().tileInfo.get(frame);
	    tileInfo.unpassable = !tileInfo.unpassable;
		fireContentChanged();
		redraw();
	}
	
	private int calcPointFrame(Point p) {
		if (frameLayout == null) {
			return -1;
		}
		Point size = getSize();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2 + paintOffset.x;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2 + paintOffset.y;
		for (int i = 1; i < frameLayout.length; i++) {
			int dx = (int)(frameLayout[i].x * ratio) + offx;
			int dy = (int)(frameLayout[i].y * ratio) + offy;
			int dw = (int)(frameLayout[i].width * ratio);
			int dh = (int)(frameLayout[i].height * ratio);
			if (new Rectangle(dx, dy, dw, dh).contains(p)) {
				return i - 1;
			}
		}
		return -1;
	}
	
	private Rectangle[] getBestLayout() {
		Point size = getSize();
		int tw = map.getTileWidth();
		int th = map.getTileHeight();
		if (showExtendedInfo) {
		    th *= 2;
		}
		int totalCount = map.getTileImage().tileInfo.size() + 1;
		double rratio = (double)size.y / size.x;
		double bestWidth = Math.sqrt(tw * totalCount * th / rratio);
		int bestcw = (int)Math.ceil(bestWidth / tw);
		
		Rectangle[] ret = new Rectangle[totalCount + 1];
		int rows = (totalCount + bestcw - 1) / bestcw;
		ret[0] = new Rectangle(0, 0, bestcw * tw, rows * th);
		for (int i = 0; i < totalCount; i++) {
			int cx = i % bestcw;
			int cy = i / bestcw;
			ret[i + 1] = new Rectangle(cx * tw, cy * th, tw, th);
		}
		return ret;
	}
	
	public int getSelectedFrame() {
		return selectedFrame;
	}

	public void setSelectedFrame(int selectedFrame) {
		this.selectedFrame = selectedFrame;
		if (this.selectedFrame == -1 || this.selectedFrame == 0) {
			setMenu(null);
		} else {
			setMenu(popMenu);
		}
		redraw();
	}

	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (input != null) {
			switch (keyCode) {
			case SWT.ARROW_UP:
			case SWT.ARROW_LEFT:
				this.selectedFrame--;
				if (this.selectedFrame < 0) {
					this.selectedFrame = map.getTileImage().tileInfo.size();
				}
				break;
			case SWT.ARROW_DOWN:
			case SWT.ARROW_RIGHT:
				this.selectedFrame++;
				if (this.selectedFrame > map.getTileImage().tileInfo.size()) {
					this.selectedFrame = 0;
				}
				break;
			case SWT.DEL:
				onDeleteFrame();
				break;
			default:
				break;
			}
			redraw();
		}
	}

	private void onDuplicateFrame() {
		int sel = selectedFrame - 1;
		if (map == null || sel < 0) {
			return;
		}
		TileInfo tinfo = map.getTileImage().tileInfo.get(sel);
		TileInfo ninfo = new TileInfo();
		ninfo.copyFrom(tinfo);
		map.getTileImage().tileInfo.add(ninfo);
		fireContentChanged();
		selectedFrame = map.getTileImage().tileInfo.size();
		fireFrameSelectionChanged(selectedFrame);
		redraw();
	}
	
	private void onDuplicateImage() {
		int sel = selectedFrame - 1;
		if (map == null || sel < 0) {
			return;
		}
		TileInfo tinfo = map.getTileImage().tileInfo.get(sel);
		PipImageData idata = map.getTileImage().image.getImageData(tinfo.frameID).duplicate();
		map.getTileImage().image.getImageDatas().add(idata);
		TileInfo ninfo = new TileInfo();
		ninfo.copyFrom(tinfo);
		ninfo.frameID = map.getTileImage().image.getImgCount() - 1;
		map.getTileImage().tileInfo.add(ninfo);
		fireContentChanged();
		selectedFrame = map.getTileImage().tileInfo.size();
		fireFrameSelectionChanged(selectedFrame);
		redraw();
	}
	
	private void onHflipFrame() {
		int sel = selectedFrame - 1;
		if (map == null || sel < 0) {
			return;
		}
		TileInfo tinfo = map.getTileImage().tileInfo.get(sel);
		tinfo.transit = PipImage.hflip(tinfo.transit);
		fireContentChanged();
		redraw();
	}
	
	private void onVflipFrame() {
		int sel = selectedFrame - 1;
		if (map == null || sel < 0) {
			return;
		}
		TileInfo tinfo = map.getTileImage().tileInfo.get(sel);
        tinfo.transit = PipImage.vflip(tinfo.transit);
        fireContentChanged();
        redraw();
	}
	
	private void onDeleteFrame() {
		int sel = selectedFrame - 1;
		if (map == null || sel < 0) {
			return;
		}
		map.removeTile(sel);
		fireContentChanged();
		if (selectedFrame > map.getTileImage().tileInfo.size()) {
			selectedFrame--;
		}
		fireFrameSelectionChanged(selectedFrame);
		redraw();
	}
	
	private void onEditFrame() {
		int sel = selectedFrame - 1;
		if (map == null || sel < 0) {
			return;
		}
		TileInfo tinfo = map.getTileImage().tileInfo.get(sel);
		Image img = map.getTileImage().image.getImageDraw(tinfo.frameID).createSWTImage(getDisplay(), 0);
		PngEncoder enc = new PngEncoder(img);
		try {
			File tmpFile = File.createTempFile("_iws_", ".png");
			FileOutputStream fos = new FileOutputStream(tmpFile);
			enc.encode32(fos, false);
			fos.close();
			tmpFile.deleteOnExit();
			Runtime.getRuntime().exec(new String[] {
				Settings.imageEditor,
				java.text.MessageFormat.format(Settings.imageEditorArg, tmpFile.getAbsolutePath())
			});
			
			FileWatcher.watch(tmpFile, this);
			editingFrames.put(tmpFile, tinfo.frameID);
		} catch (Exception e) {
			SWTUtils.showError(getShell(), "错误", e);
		}
		img.dispose();
	}

	class EditingFrameChanged implements Runnable {
	    private File file;
	    private int frame;
		
		public EditingFrameChanged(File file, int frame) {
			this.file = file;
			this.frame = frame;
		}
		
		public void run() {
			PipImage allImage = map.getTileImage().image;
			if (frame >= allImage.getImgCount()) {
				return;
			}
			try {
				Image newImg = new Image(getDisplay(), file.getAbsolutePath());
				int[][] newData = ImageViewer.getImageData(newImg, newImg.getBounds());
				newImg.dispose();
				allImage.addFrame(newData);
				PipImageData newFrame = allImage.getImageDatas().get(allImage.getImgCount() - 1);
				allImage.getImageDatas().set(frame, newFrame);
				allImage.getImageDatas().remove(allImage.getImgCount() - 1);
				fireContentChanged();
				redraw();
			} catch (Exception e) {
				SWTUtils.showError(getShell(), "错误", e);
			}
		}
	}

	public void fileModified(File f) {
        Integer frame = editingFrames.get(f);
        if (frame != null) {
            getDisplay().asyncExec(new EditingFrameChanged(f, frame.intValue()));
        }
    }
	
	public void widgetDisposed(DisposeEvent e) {
	    super.widgetDisposed(e);
	    FileWatcher.unwatch(this);
	    colorMenu.dispose();
	    if (popMenu != null) {
	    	popMenu.dispose();
	    }
	}
}
