package com.pip.mapeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import javax.swing.text.html.ImageView;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pip.image.workshop.editor.AnimateEditor;
import com.pip.image.workshop.editor.ImageViewer;
import com.pip.image.workshop.editor.ViewResRefDialog;
import com.pip.mapeditor.data.BlurMapLayer;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.NPCImageInfo;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.mapeditor.data.TileSet;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImageDraw;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * 地形选择。 
 */
public class MapLandformSelector extends AbstractImageViewer implements IFileModificationListener {
    private MapEditor owner;
	private MapFile map;
	private Rectangle[] frameLayout;
	private int selectedFrame;
	private int selectingFrame;
	private long randomSeed = System.currentTimeMillis();
	private Rectangle[] buttonBounds = new Rectangle[4];
	private String[] buttonTexts = { "3x3", "5x5", "7x7", "9x9" };
	private int selectedButtonIndex = 0;

    private int hoverFrame;
	private Menu popMenu;

	// 用系统编辑器编辑中的贴图
    private HashMap<File, Integer> editingFrames;
	
	public void setInput(Object input) {
		super.setInput(input);
		map = (MapFile)input;
		hoverFrame = -1;
		selectedFrame = -1;
		frameLayout = null;
		setMenu(null);
		editingFrames = new HashMap<File, Integer>();
	}
	
	public int getBrushSize() {
	    return selectedButtonIndex * 2 + 1;
	}

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public MapLandformSelector(Composite parent, int style, MapEditor owner) {
		super(parent, style | SWT.NO_BACKGROUND);
		this.owner = owner;
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
				    for (int i = 0; i < buttonBounds.length; i++) {
				        if (buttonBounds[i].contains(e.x, e.y)) {
				            return;
				        }
				    }
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
				    for (int i = 0; i < buttonBounds.length; i++) {
                        if (buttonBounds[i].contains(e.x, e.y)) {
                            selectedButtonIndex = i;
                            redraw();
                            return;
                        }
                    }
					int newFrame = calcPointFrame(new Point(e.x, e.y));
					if (newFrame == selectingFrame && newFrame != -1) {
						setSelectedFrame(newFrame);
						fireFrameSelectionChanged(selectedFrame);
					}
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
			}
		});

		MenuManager mgr = new MenuManager();
		
        mgr.add(new Action("设为基础地形") {
            public void run() {
                onSetBasic();
            }
        });
        mgr.add(new Action("前移") {
            public void run() {
                onMoveUp();
            }
        });
        mgr.add(new Action("后移") {
            public void run() {
                onMoveDown();
            }
        });
		mgr.add(new Action("删除地形") {
		    public void run() {
		        onDeleteFrame();
		    }
		});
		mgr.add(new Action("编辑地形文件...") {
			public void run() {
				onEditFrame();
			}
		});
		mgr.add(new Action("导出地形文件"){
			public void run() {
				onExportFrame();
			}
		});
		if(owner.isLibMode()){
			mgr.add(new Action("查看文件引用"){
				public void run() {
					onShowFileRef();
				}
			});
		}
		if (popMenu != null) {
			popMenu.dispose();
		}
		popMenu = mgr.createContextMenu(this);
	}
	protected void onShowFileRef() {
		try {
			HashMap<String, Integer> refCnt = map.makeLandformRefTimes();
			ViewResRefDialog dlg = new ViewResRefDialog(getShell(), this);
			dlg.setRefMap(refCnt);
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onExportFrame(){
		String path = owner.getFilePath();
		path = path.substring(0, path.lastIndexOf("\\")+1);
		String tileInfoPath = path+selectedFrame+".lfi";
		path += selectedFrame+".ldf";
		File tempFile = new File(path );
		File lfiFile = new File(tileInfoPath);
	    try {
			map.getLandforms().get(selectedFrame).image.save(tempFile);
			map.getLandforms().get(selectedFrame).saveTileInfo(lfiFile);
			MessageDialog.openInformation(getShell(), "提示", "导出成功!文件保存在:\n"+path);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openInformation(getShell(), "错误", "导出失败:\n"+e.toString());
		}
	}
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		getBestLayout();
		int offx = (int)(size.x - frameLayout[0].width * ratio) / 2;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2;
		int count = map.getLandforms().size();
		for (int i = 0; i < count; i++) {
			Rectangle rect = zoom(frameLayout[i + 1]);
			rect.x += offx + paintOffset.x;
			rect.y += offy + paintOffset.y;
			
			// 绘制模糊地图样例
			LandformImage landform = (LandformImage)map.getLandforms().get(i).image;
            byte[][] mdtemp = BlurMapUtil.makeRectangle(6, 6);
            BlurMapUtil.drawLandform(gc, landform, new Random(randomSeed), mdtemp, rect.x, rect.y, 
                    map.getBlurTileWidth(), map.getBlurTileHeight(), ratio);
			
			// 画框
			if (i == selectedFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				gc.drawRectangle(rect);
			}
			if (i == hoverFrame) {
				gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				gc.drawRectangle(rect);
			}
		}
	}
	
	protected void drawInformation(GC gc) {
        super.drawInformation(gc);
        
        Point size = getSize();
        Point ts = gc.textExtent(buttonTexts[0]);
        int bx = 1;
        int by = size.y - ts.y - 8;
        int bw = ts.x + 7;
        int bh = ts.y + 6;
        gc.setBackground(getBackground());
        gc.setForeground(invert(getBackground()));
        for (int i = 0; i < buttonTexts.length; i++) {
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
            if (i == selectedButtonIndex) {
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
            }
            gc.drawRectangle(buttonBounds[i]);
            gc.drawText(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4);
            if (i == selectedButtonIndex) {
                gc.setForeground(invert(getBackground()));
            }
            bx += bw + 2;
        }
	}

	// 计算鼠标点到的位置的动画
	private int calcPointFrame(Point p) {
        getBestLayout();
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
	
	// 计算最佳布局
	private void getBestLayout() {
	    if (frameLayout != null) {
	        return;
	    }
	    if (input == null) {
	        return;
	    }
		int count = map.getLandforms().size();
		
        // 计算最佳布局
		Rectangle[] frameBounds = new Rectangle[count];
		for (int i = 0; i < count; i++) {
		    frameBounds[i] = new Rectangle(0, 0, map.getBlurTileWidth() * 6, map.getBlurTileHeight() * 6);
		}
		frameLayout = getBestLayout(getSize(), frameBounds);
	}
	
	public int getSelectedFrame() {
		return selectedFrame;
	}

	public void setSelectedFrame(int selectedFrame) {
		this.selectedFrame = selectedFrame;
        if (this.selectedFrame == -1) {
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
					this.selectedFrame = map.getLandforms().size() - 1;
				}
				break;
			case SWT.ARROW_DOWN:
			case SWT.ARROW_RIGHT:
				this.selectedFrame++;
				if (this.selectedFrame >= map.getLandforms().size()) {
					this.selectedFrame = 0;
					if (this.selectedFrame >= map.getLandforms().size()) {
						this.selectedFrame = -1;
					}
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

	private void onDeleteFrame() {
		int sel = selectedFrame;
		if (map == null || sel < 0) {
			return;
		}
		map.removeLandform(sel);
		onContentChanged();
	}
	
	private void onSetBasic() {
        int sel = selectedFrame;
        if (map == null || sel < 0) {
            return;
        }
        GameMap map = owner.getActiveMap();
        int activeLayer = owner.getActiveLayer();
        BlurMapLayer layer = null;
        if (map == null) {
            MessageDialog.openError(getShell(), "错误", "请先创建新场景。");
            return;
        }
        if (activeLayer == -1 || !(map.layers.get(activeLayer) instanceof BlurMapLayer)) {
            for (int i = 0; i < map.layers.size(); i++) {
                if (map.layers.get(i) instanceof BlurMapLayer) {
                    layer = (BlurMapLayer)map.layers.get(i);
                }
            }
        } else {
            layer = (BlurMapLayer)map.layers.get(activeLayer); 
        }
        if (layer == null) {
            MessageDialog.openError(getShell(), "错误", "请选择模糊地图。");
            return;
        }
        layer.setBaseLandform(sel);
        onContentChanged();
	}
	
	private void onMoveUp() {
	    int sel = selectedFrame;
	    if (map == null || sel < 1) {
	        return;
	    }
	    map.moveUpLandform(sel);
	    onContentChanged();
	}
	
	private void onMoveDown() {
        int sel = selectedFrame;
        if (map == null || sel < 0 || sel == map.getLandforms().size() - 1) {
            return;
        }
        map.moveDownLandform(sel);
        onContentChanged();
    }
	
	public void onContentChanged() {
        frameLayout = null;
        fireContentChanged();
        if (selectedFrame >= map.getLandforms().size()) {
            selectedFrame--;
            if (selectedFrame >= map.getLandforms().size()) {
                selectedFrame = -1;
            }
        }
        fireFrameSelectionChanged(selectedFrame);
        redraw();
	}
	
	// 编辑地形文件
	private void onEditFrame() {
		if (map == null || selectedFrame == -1) {
			return;
		}
		// 库模式直接编辑引用文件
		if (map.isLibMode) {
			try {
				int hashCode = map.getLandforms().get(selectedFrame).hashCode;
				String subPath = map.getProjectOwner().fileMapGet(hashCode);
				File targetFile = new File(map.getProjectPath(), subPath);
				
				// 启动一个地形编辑器来编辑文件
	            IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((targetFile.getAbsolutePath())));
	            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			    IDE.openEditorOnFileStore(page, fileStore);
			} catch (Exception e) {
				SWTUtils.showError(getShell(), "错误", e);
			}
		} else {
			try {
			    // 创建临时文件
			    File tempFile = File.createTempFile("_iws_", ".ldf");
			    map.getLandforms().get(selectedFrame).image.save(tempFile);
	            tempFile.deleteOnExit();
	    		
	            // 启动一个地形编辑器来编辑文件
	            IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((tempFile.getAbsolutePath())));
	            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			    IDE.openEditorOnFileStore(page, fileStore);
	            
	            // 监控临时文件的变化
	            FileWatcher.watch(tempFile, this);
	            editingFrames.put(tempFile, selectedFrame);
			} catch (Exception e) {
				SWTUtils.showError(getShell(), "错误", e);
			}
		}
	}

	/**
	 * 外部编辑的地形文件变化处理。读入新的地形文件。
	 */
    public void fileModified(File f) {
        if (!editingFrames.containsKey(f)) {
            return;
        }
        int frameIndex = editingFrames.get(f);
        try {
            map.getLandforms().get(frameIndex).image.load(f.getAbsolutePath());
            map.getLandforms().get(frameIndex).validate();
        } catch (Exception e) {
        }
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                onContentChanged();
            }
        });
    }

    /**
     * 关闭时取消文件监听。
     */
    public void widgetDisposed(DisposeEvent e) {
        super.widgetDisposed(e);
        if (popMenu != null) {
        	popMenu.dispose();
        }
        FileWatcher.unwatch(this);
    }
    
    public void refresh() {
        frameLayout = null;
        redraw();
    }
    @Override
	public void response(Object source, Object data) {
		boolean ret = MessageDialog.openConfirm(getShell(), "确认", "确定删除引用吗?");
		if(ret){
			((ViewResRefDialog)source).close();
			map.removeLandform(((Integer)data).intValue());
			onContentChanged();
			refresh();
		}
//		showResRef();
	}
}
