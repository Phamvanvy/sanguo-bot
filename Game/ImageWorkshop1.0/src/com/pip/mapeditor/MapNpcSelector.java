package com.pip.mapeditor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.text.html.ImageView;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pip.image.workshop.editor.AnimateEditor;
import com.pip.image.workshop.editor.ImageViewer;
import com.pip.image.workshop.editor.ViewResRefDialog;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.IMapLayer;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.MapNPC;
import com.pip.mapeditor.data.MapNPCLayer;
import com.pip.mapeditor.data.MultiAnimNPC;
import com.pip.mapeditor.data.NPCImageInfo;
import com.pip.mapeditor.data.ProjectOwner;
import com.pip.mapeditor.data.ProjectParser;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.ColorsExceedException;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.pipimage.image.PipImageDraw;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * A widget to display npcs in a map. 
 */
public class MapNpcSelector extends AbstractImageViewer implements IFileModificationListener {
	private MapFile map;
	private Rectangle[] frameLayout;
	private Rectangle[] frameBounds;
	private int selectedFrame;
	private int selectingFrame;
	
	private Image mapBuffer;

    private int hoverFrame;
	private Menu popMenu;
	private int currentTime;
	private File tempDir, tempFile;
	private Action showResSummaryAction;
	
	public void setInput(Object input) {
		super.setInput(input);
		map = (MapFile)input;
		hoverFrame = -1;
		selectedFrame = -1;
		currentTime = 0;
		frameLayout = null;
		frameBounds = null;
		if(map.isLibMode){
			makeResRefMenu();
		}
		setMenu(popMenu);
	}
	
	private Action showResRefAction;
	private void makeResRefMenu() {
		if(showResRefAction!=null){
			return;
		}
		showResRefAction = new Action("查看资源引用"){
			public void run(){
				try {
					showResRef();
				} catch (Exception e) {
					SWTUtils.showError(getShell(), "错误", "解析引用资源时出现错误。", e);
				}
			}
		};
		menuMgr.add(showResRefAction);
		menuMgr.add(new Action("显示动画文件名称"){
			public void run(){
				int[] realRef = map.getNpcAnimateRef(selectedFrame);
				inEditAnimateSetHashCode = realRef[0];
				String ctsName = map.getRefRealPath(inEditAnimateSetHashCode);
				ctsName = ctsName.replace(".ctn", ".cts");
				InputDialog dlg = new InputDialog(getShell(),"当前选中的NPC所在的文件","", ctsName, null);
				dlg.open();
			}
		});
		showResSummaryAction = new Action("统计资源"){
			public void run(){
				try {
					Object obj[];
					obj = map.buildPackAni(0, 2048);
					PipAnimateSet mergedAni4libMap = (PipAnimateSet) obj[0];
					int cnt = mergedAni4libMap.getAnimateCount();
					int[] aniAreaAndCnt = sumNPCAreaAndCount();
					cnt = map.getMaps().size();
					int mapArea = 0;
					for(int i=0; i<cnt; i++){
						GameMap m = map.getMaps().get(i);
						mapArea += m.width*m.height;
					}
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					mergedAni4libMap.save(os, false);
					int anpSize = os.toByteArray().length;
					cnt = mergedAni4libMap.getFileCount();
					for(int i=0; i<cnt; i++){
						anpSize += PipImage.makeImageFile(mergedAni4libMap.getSourceImage(i),false).length;
					}
					String message = "整理后的动画(ctn+pip)大小:" + anpSize/1024+"K ("+anpSize+"字节)\n"+
							"动画面积:" + aniAreaAndCnt[0] + "像素\n" +
							"动画个数:" + aniAreaAndCnt[1] + "\n" +
							"地图面积(各个地图之和):" + mapArea + "像素\n" +
							"覆盖率(动画面积/地图面积):" + (aniAreaAndCnt[0]*100/mapArea) + "%";
					MessageDialog.openInformation(getShell(), "资源统计", message);
				} catch (Exception e) {
					SWTUtils.showError(getShell(), "错误", "统计出现错误。", e);
				}
			}
		};
		menuMgr.add(showResSummaryAction);
		menuMgr.add(new Action("导出为pip+ctn"){
			public void run(){
				try{
					map.exportAsPkg();
					MessageDialog.openInformation(getShell(), "导出为压缩模式", "已导出至同名文件夹(map文件所在目录)");
				}catch(Exception e){
					SWTUtils.showError(getShell(), "错误", "导出为压缩模式出现错误。", e);
				}
			}
		});
		if (popMenu != null) {
			popMenu.dispose();
		}
		popMenu = menuMgr.createContextMenu(this);
	}
	/**
	 * 统计地图NPC个数和面积
	 * @return int[]{area, npcCnt}
	 */
	public int[] sumNPCAreaAndCount(){
		int ret = 0;
		int npcCnt = 0;
		for(GameMap gm:map.getMaps()){
			for(IMapLayer iml:gm.layers){
				if(iml instanceof MapNPCLayer){
					MapNPCLayer mnl = (MapNPCLayer)iml;
					for (MapNPC npc : mnl.getNpcs()) {
						if(npc instanceof MultiAnimNPC){
							MultiAnimNPC mnpc = (MultiAnimNPC) npc;
							for(MapNPC ncc:mnpc.getChildren()){
								Rectangle bounds = map.getAnimate(ncc.animateSetRef, ncc.animate).getBounds();
								ret += bounds.width*bounds.height;
								npcCnt ++;
							}
						}else{
							Rectangle bounds = map.getAnimate(npc.animateSetRef, npc.animate).getBounds();
							ret += bounds.width*bounds.height;
							npcCnt ++;
						}
					}
				}
			}
		}
		return new int[]{ret, npcCnt};
	}
	protected void showResRef() throws Exception {
		HashMap<String, Integer> refCnt = map.makeResRefTimes();
		ViewResRefDialog dlg = new ViewResRefDialog(getShell(),this);
		dlg.setRefMap(refCnt);
		dlg.open();
	}
	@Override
	public void response(Object source, Object data) {
		boolean ret = MessageDialog.openConfirm(getShell(), "确认", "确定删除引用吗?");
		if(ret){
			((ViewResRefDialog)source).close();
			map.removeAniamteRef(((Integer)data).intValue());
			MapEditor.imageCache.remove(map.getAnimates());
			onContentChanged();
			refresh();
		}
//		showResRef();
	}

	public void zoomin() {
        if (ratio < 64) {
            ratio *= 2;
            refresh();
        }
    }
    
    public void zoomout() {
        if (ratio > 0.125) {
            ratio /= 2;
            refresh();
        }
    }

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public MapNpcSelector(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
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
					selectingFrame = calcPointFrame(new Point(e.x, e.y));
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y)) {
					return;
				}
				if (e.button == 1) {
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
		
		mgr.add(new Action("删除动画") {
		    public void run() {
		        onDeleteFrame();
		    }
		});
        mgr.add(new Action("复制动画") {
            public void run() {
                onDupFrame();
            }
        });
        mgr.add(new Action("前移") {
            public void run() {
                onMoveUpFrame();
            }
        });
        mgr.add(new Action("设置碰撞区域...") {
            public void run() {
                onSetupCollision();
            }
        });
        mgr.add(new Action("调整参考点...") {
            public void run() {
                onAdjustRefPos();
            }
        });
        mgr.add(new Separator());
		mgr.add(new Action("编辑动画文件...") {
			public void run() {
				onEditAnimateSet();
			}
		});
		menuMgr = mgr;
		popMenu = mgr.createContextMenu(this);
	}
	private MenuManager menuMgr;
	protected void createMapBuffer() {
        getBestLayout();
        int mapw = (int)(frameLayout[0].width * ratio);
        int maph = (int)(frameLayout[0].height * ratio);
        if(mapw==0 || maph==0){
        	return;
        }
        mapBuffer = new Image(getDisplay(), mapw, maph);
        GC gc = new GC(mapBuffer);
        
        gc.setBackground(this.getBackground());
        gc.fillRectangle(0, 0, mapw, maph);
        
        int count = map.getAnimates().getAnimateCount();
        for (int i = 0; i < count; i++) {
            Rectangle rect = zoom(frameLayout[i + 1]);
            
            // 绘制动画
            int animateX = rect.x - (int)(frameBounds[i].x * ratio);
            int animateY = rect.y - (int)(frameBounds[i].y * ratio);
            PipAnimate animate = map.getAnimates().getAnimate(i);
            animate.drawAnimateFrame(gc, currentTime, animateX, animateY, ratio, MapEditor.imageCache);
        }
        gc.dispose();
    }
	
	protected void paintInput(GC gc) {
		Point size = getSize();
		if (input == null) {
			return;
		}
		if (mapBuffer == null) {
            createMapBuffer();
        }
        if (mapBuffer == null) {
            return;
        }
        int offx = (int)(size.x - frameLayout[0].width * ratio) / 2;
		int offy = (int)(size.y - frameLayout[0].height * ratio) / 2;
		gc.drawImage(mapBuffer, offx + paintOffset.x, offy + paintOffset.y);
		
		int count = map.getAnimates().getAnimateCount();
		for (int i = 0; i < count; i++) {
			Rectangle rect = zoom(frameLayout[i + 1]);
			rect.x += offx + paintOffset.x;
			rect.y += offy + paintOffset.y;
			
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
		if(map.isLibMode){
			getMergedLayout();
			return;
		}
	    if (frameLayout != null) {
	        return;
	    }
	    if (input == null) {
	        return;
	    }
		int count = map.getAnimates().getAnimateCount();
		
		// 计算所有动画占用的大小
		frameBounds = new Rectangle[count];
		for (int i = 0; i < count; i++) {
		    frameBounds[i] = map.getAnimates().getAnimate(i).getBounds();
		}
		
		// 计算最佳布局
		frameLayout = getBestLayout(getSize(), frameBounds);
	}
	
	private void getMergedLayout() {
		if (frameLayout != null) {
	        return;
	    }
	    if (input == null) {
	        return;
	    }
	    
	    List<Rectangle> animList = new ArrayList<Rectangle>();
		for(int i=0; i<map.animateList.size();i++){
			PipAnimateSet pa = map.animateList.get(i);
			for(int j=0; j<pa.getAnimateCount(); j++){
				animList.add(pa.getAnimate(j).getBounds());
			}
		}
		// 计算所有动画占用的大小
		frameBounds =  animList.toArray(new Rectangle[0]);
		
		
		// 计算最佳布局
		frameLayout = getBestLayout(getSize(), frameBounds);		
	}

	public int getSelectedFrame() {
		return selectedFrame;
	}

	public void setSelectedFrame(int selectedFrame) {
		this.selectedFrame = selectedFrame;
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
					this.selectedFrame = map.getAnimates().getAnimateCount() - 1;
				}
				break;
			case SWT.ARROW_DOWN:
			case SWT.ARROW_RIGHT:
				this.selectedFrame++;
				if (this.selectedFrame >= map.getAnimates().getAnimateCount()) {
					this.selectedFrame = 0;
					if (this.selectedFrame >= map.getAnimates().getAnimateCount()) {
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
		if(map.isLibMode){
			MessageDialog.openInformation(getShell(), "提示", "此方法在库模式下不可使用.");
			return;
		}
		int sel = selectedFrame;
		if (map == null || sel < 0) {
			return;
		}
		map.removeAnimate(sel);
		MapEditor.imageCache.remove(map.getAnimates());
		onContentChanged();
	}
	
	private void onMoveUpFrame() {
		if(map.isLibMode){
			MessageDialog.openInformation(getShell(), "提示", "此方法在库模式下不可使用.");
			return;
		}
        int sel = selectedFrame;
        if (map == null || sel <= 0) {
            return;
        }
        map.moveUpAnimate(sel);
        onContentChanged();
    }

	
    private void onDupFrame() {
    	if(map.isLibMode){
			MessageDialog.openInformation(getShell(), "提示", "此方法在库模式下不可使用.");
			return;
		}
        int sel = selectedFrame;
        if (map == null || sel < 0) {
            return;
        }
        map.getAnimates().dupAnimate(sel, false);
        map.onAnimateInsert(sel + 1);
        onContentChanged();
    }
	
	public void onContentChanged() {
        frameLayout = null;
        frameBounds = null;
        if (mapBuffer != null) {
            mapBuffer.dispose();
        }
        mapBuffer = null;
        fireContentChanged();
        if (selectedFrame >= map.getAnimates().getAnimateCount()) {
            selectedFrame--;
            if (selectedFrame >= map.getAnimates().getAnimateCount()) {
                selectedFrame = -1;
            }
        }
        fireFrameSelectionChanged(selectedFrame);
        redraw();
	}
	
	// 设置碰撞区域，启动一个对话框来完成这个工作
	private void onSetupCollision() {
	    int sel = selectedFrame;
        if (map == null || sel < 0) {
            return;
        }
        long key = sel;
        if(map.isLibMode){
        	int ref[] = map.getNpcAnimateRef(sel);
        	key = ref[0];
        	key<<=32;
        	key |= ref[1];
        }
        NPCImageInfo info = map.getNPCs().get(key);
        PipAnimate animate = map.getAnimates().getAnimate(sel);
        SetupCollisionDialog dlg = new SetupCollisionDialog(getShell(), animate, info);
        if (dlg.open() == SetupCollisionDialog.OK) {
            Rectangle[] rects = dlg.getSelectedArea();
            if (rects == null || rects.length == 0) {
                map.getNPCs().remove(key);
            } else {
                NPCImageInfo newInfo = new NPCImageInfo();
                newInfo.animateRef = (int)(key>>32);
                newInfo.cx = new int[rects.length];
                newInfo.cy = new int[rects.length];
                newInfo.cw = new int[rects.length];
                newInfo.ch = new int[rects.length];
                for (int i = 0; i < rects.length; i++) {
                    newInfo.cx[i] = rects[i].x;
                    newInfo.cy[i] = rects[i].y;
                    newInfo.cw[i] = rects[i].width;
                    newInfo.ch[i] = rects[i].height;
                }
                map.getNPCs().put(key, newInfo);
            }
            onContentChanged();
        } else {
        	if (info == null || info.cx.length == 0) {
        		map.getNPCs().remove(key);
        	}
        }
	}
	
	// 调整参考点，启动一个对话框来完成这个工作
    private void onAdjustRefPos() {
        int sel = selectedFrame;
        if (map == null || sel < 0) {
            return;
        }
        String[] refs = checkMultiMapRef();
        if (refs != null) {
        	String msg = "有超过一个地图引用此动画：\n";
        	for (int i = 0; i < refs.length; i++) {
        		if (i > 0) {
        			msg += "\n";
        		}
        		msg += refs[i];
        	}
        	msg += "\n是否继续修改参考点？所有这些地图中引用此动画的NPC都将被调整，如果这些文件已经被打开，将会被自动关闭。";
        	if (!MessageDialog.openConfirm(getShell(), "提示", msg)) {
        		return;
        	}
        	
        	// 关闭所有引用的其他文件
        	String thisFile = ProjectOwner.getProjectRelatePath(map.getSourceFilePath());
        	int[] refInfo = map.getNpcAnimateRef(sel);
        	for (String ref : refs) {
        		if (ref.equals(thisFile)) {
        			continue;
        		}
        		File mapf = new File(map.getProjectPath(), ref);
        		IEditorPart editor = null;
                IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(mapf.getAbsolutePath()));
                FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
                editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(input);
                if (editor != null) {
                	if (!PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, true)) {
                		return;
                	}
                }
        	}
        }
        PipAnimate animate = map.getAnimates().getAnimate(sel);
        AdjustRefPointDialog dlg = new AdjustRefPointDialog(getShell(), animate);
        if (dlg.open() == AdjustRefPointDialog.OK) {
            Point offset = dlg.getOffset();
            
            // 调整这个动画序列中所有帧的位置，反向调整所用使用这个动画序列的NPC的位置
            for (int i = 0; i < animate.getFrameCount(); i++) {
                PipAnimateFrameRef frame = animate.getFrame(i);
                frame.setDx(frame.getDx() + offset.x);
                frame.setDy(frame.getDy() + offset.y);
            }
            for (GameMap gm : map.getMaps()) {
                for (IMapLayer layer : gm.layers) {
                    if (layer instanceof MapNPCLayer) {
                        MapNPCLayer npcl = (MapNPCLayer)layer;
                        for (MapNPC npc : npcl.getNpcs()) {
                        	if(map.isLibMode){
                        		updateNPCPosition(npc, offset, sel);
                        	}else{
	                            if (npc.animate == sel) {
	                                npc.x -= offset.x;
	                                npc.y -= offset.y;
	                            }
                        	}
                        }
                    }
                }
            }
            
            // 如果有其他引用此动画的文件，所有这些文件里的引用位置都要被调整
            if (refs != null) {
            	String thisFile = ProjectOwner.getProjectRelatePath(map.getSourceFilePath());
            	int[] refInfo = map.getNpcAnimateRef(sel);
            	for (String ref : refs) {
            		if (ref.equals(thisFile)) {
            			continue;
            		}
            		File mapf = new File(map.getProjectPath(), ref);
            		MapFile mf = new MapFile();
            		try {
            			mf.load(mapf, true);
            		} catch (Exception e) {
            			SWTUtils.showError(getShell(), "错误", "载入地图出错：" + ref, e);
            			continue;
            		}
            		for (GameMap gm : mf.getMaps()) {
                        for (IMapLayer layer : gm.layers) {
                            if (layer instanceof MapNPCLayer) {
                                MapNPCLayer npcl = (MapNPCLayer)layer;
                                for (MapNPC npc : npcl.getNpcs()) {
                            		updateNPCPosition(npc, offset, refInfo);
                                }
                            }
                        }
                    }
            		try {
            			mf.save(mapf);
            		} catch (Exception e) {
            			SWTUtils.showError(getShell(), "错误", "保存地图出错：" + ref, e);
            			continue;
            		}
            	}
            }
            
            // 调整此NPC已经设置的碰撞区域
            long key = sel;
            if(map.isLibMode){
            	updateAnimFile(animate, sel);
            	int ref[] = map.getNpcAnimateRef(sel);
            	key = ref[0];
            	key<<=32;
            	key |= ref[1];
            }
            NPCImageInfo info = map.getNPCs().get(key);
            if (info != null) {
                for (int i = 0; i < info.cx.length; i++) {
                    info.cx[i] += offset.x;
                    info.cy[i] += offset.y;
                }
            }
        
            onContentChanged();
        }
    }
	
	private void updateAnimFile(PipAnimate animate, int sel) {
		PipAnimateSet pas = animate.getParent();
		String srcFileName = map.getRefRealPath(pas.hashCode).replace(".ctn", ".cts");
		try {
			File animateFile = new File(srcFileName);
			pas.save(animateFile, true);
			String path = animateFile.getAbsolutePath();
			path = path.substring(0, path.length() - 1) + "n";
			pas.save(new File(path), false);
		} catch (IOException e) {
			SWTUtils.showError(getShell(), "错误", "保存参考点至动画文件时出现错误，这可能造成已调整的参考点在重新打开地图时失效。", e);
		}
	}

	private void updateNPCPosition(MapNPC npc, Point offset, int slctIdx) {
		updateNPCPosition(npc, offset, map.getNpcAnimateRef(slctIdx));
	}
	
	private void updateNPCPosition(MapNPC npc, Point offset, int[] ref) {
		ArrayList<MapNPC> npcs = new ArrayList<MapNPC>();
		if(npc instanceof MultiAnimNPC){
			npcs.addAll(((MultiAnimNPC)npc).getChildren());
		}else{
			npcs.add(npc);
		}
		for (MapNPC ncc : npcs) {
			if (ncc.animateSetRef == ref[0] && ncc.animate == ref[1]) {
				ncc.x -= offset.x;
				ncc.y -= offset.y;
			}
		}
		//组合NPC的总坐标和最顶层的那个一致
		//if(npc instanceof MultiAnimNPC){ 
			//((MultiAnimNPC)npc).update();
		//}
	}

	private String[] checkMultiMapRef() {
		if(map.isLibMode){
			int ref[] = map.getNpcAnimateRef(selectedFrame);
			String srcFileName = map.getRefRealPath(ref[0]).replaceAll(".ctn$", ".cts");
			String[] refs = null;
			try {
				refs = ProjectParser.getFileRefList(srcFileName);
			} catch (Exception e) {
				SWTUtils.showError(getShell(), "错误", "检查地图引用时出现错误。", e);
				return null;
			}
			if(refs.length>1){
				return refs;
			}
		}
		return null;
	}

	// 编辑动画序列文件，这里把地图文件中的动画文件保存为临时文件，启动一个动画编辑器来编辑。
	private void onEditAnimateSet() {
		if (map == null) {
			return;
		}
		if(map.isLibMode){
			onEditLibAnimateSet();
			return;
		}
		try {
		    // 创建临时目录
            if (tempFile != null) {
                FileWatcher.unwatch(tempFile, this);
            }
    		if (tempDir == null) {
    		    tempDir = File.createTempFile("_iws_", ".man");
    		    tempDir.delete();
    		    tempDir.mkdirs();
    		    tempFile = new File(tempDir, "temp.cts");
    		}
    		
    		// 关闭已打开的编辑器
    		IEditorPart editor = null;
    		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((tempFile.getAbsolutePath())));
    		FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
    		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            editor = page.findEditor(input);
            if (editor != null) {
                page.closeEditor(editor, false);
            }
            
            // 删除过去的文件
            Utils.deleteDir(tempDir);
            tempDir.mkdirs();
            
            // 把当前动画文件保存到文件中，启动一个新的编辑器来编辑
            map.getAnimates().forceSave(tempFile);
            page.openEditor(input, AnimateEditor.ID);
            
            // 监控临时文件的变化
            FileWatcher.watch(tempFile, this);
		} catch (Exception e) {
			SWTUtils.showError(getShell(), "错误", e);
		}
	}
	private int inEditAnimateSetHashCode;
	
	private void onEditLibAnimateSet() {
//		File tempFile;
		int[] realRef = map.getNpcAnimateRef(selectedFrame);
		inEditAnimateSetHashCode = realRef[0];
		String ctsName = map.getRefRealPath(inEditAnimateSetHashCode);
		if(ctsName == null){
			MessageDialog.openError(getShell(), "错误", "查找对应的动画文件时出错.");
			return;
		}
		System.out.println("MapNpcSelector.onEditAnimateSet():"+ctsName);
		if (tempFile != null) {
            FileWatcher.unwatch(tempFile, this);
        }
		tempFile = new File(ctsName.replace(".ctn", ".cts"));
		// 关闭已打开的编辑器
		IEditorPart editor = null;
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((tempFile.getAbsolutePath())));
		FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        editor = page.findEditor(input);
        if (editor != null) {
            page.closeEditor(editor, false);
        }

        try {
			page.openEditor(input, AnimateEditor.ID);
		} catch (PartInitException e) {
			SWTUtils.showError(getShell(), "错误", "打开对应的动画文件时出错。", e);
		}
        
        // 监控临时文件的变化
        FileWatcher.watch(tempFile, this);
		return;		
	}

	/**
	 * 动画播放到下一帧。
	 */
	public void step() {
	    currentTime++;
	}

	/**
	 * 外部编辑的动画文件变化处理。读入新的动画文件。
	 */
    public void fileModified(File f) {
        if (!f.equals(tempFile)) {
            return;
        }
        try {
        	if(map.isLibMode){
        		map.getAnimateSet(inEditAnimateSetHashCode).load(tempFile);
        		int cnt = map.getAnimates().getAnimateCount();
        		for(int i=0; i<cnt; i++){
        			map.getAnimates().removeAnimate(0);
        		}
        		for(PipAnimateSet pas:map.animateList){
        			cnt = pas.getAnimateCount();
        			for(int i=0; i<cnt; i++){
        				map.getAnimates().addAnimate(pas.getAnimate(i));
        			}
        		}
//        		refresh();
        		redraw();
        		return;
        	}else{
        		map.getAnimates().load(tempFile);
        	}
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
        if (tempFile != null) {
            FileWatcher.unwatch(tempFile, this);
            Utils.deleteDir(tempDir);
        }
        if (mapBuffer != null) {
            mapBuffer.dispose();
        }
        if (popMenu != null) {
        	popMenu.dispose();
        }
    }
    
    public void refresh() {
        frameLayout = null;
        if (mapBuffer != null) {
            mapBuffer.dispose();
        }
        mapBuffer = null;
        redraw();
    }
}
