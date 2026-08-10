package com.pip.mapeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import com.pip.util.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.TileView;
import com.pip.image.workshop.WorkshopPlugin;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.image.workshop.editor.ImageViewerListener;
import com.pip.image.workshop.editor.StateManager;
import com.pip.mapeditor.data.*;
import com.pip.mapeditor.tool.EmulateWalkTool;
import com.pip.mapeditor.tool.EyesightTool;
import com.pip.mapeditor.tool.LandformTool;
import com.pip.mapeditor.tool.MapNPCTool;
import com.pip.mapeditor.tool.MultiAnimNPCTool;
import com.pip.mapeditor.tool.PassableTool;
import com.pip.mapeditor.tool.PickupTool;
import com.pip.mapeditor.tool.RealPlayTool;
import com.pip.mapeditor.tool.ThumbTool;
import com.pip.mapeditor.tool.TileConfigTool;
import com.pip.mapeditor.tool.TileTool;
import com.pip.mapeditor.tool.WindowViewTool;
import com.pip.util.Utils;
import com.pipimage.image.ImageDrawCache;
import com.pipimage.image.LandformImage;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipAnimateSet.SimilarImageResult;
import com.pipimage.png.PngEncoder;
import com.pipimage.png.PngFile;
import com.swtdesigner.ResourceManager;

public class MapEditor extends EditorPart implements ImageViewerListener, SelectionListener, Runnable {
    /**
     * 用于地图编辑器的系统图片缓存，加快绘图速度。
     */
    public static ImageDrawCache imageCache = new ImageDrawCache(1000);
    
	class LayerContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
		    GameMap map = (GameMap)inputElement;
		    if (map == null) {
		        return new Object[0];
		    } else {
		        return map.layers.toArray();
		    }
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	class LayerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
		    GameMap map = getActiveMap();
		    IMapLayer layer = (IMapLayer)element;
		    String ret = "error";
		    
		    if (element == map.groundLayer || element == map.skyLayer) {
		        if(layer.getLayerCount() > 0){
		        	ret = layer.getName() + " (" + layer.getLayerCount() + ") " + "(*)";
		        }else{
		        	ret = layer.getName() + "(*)";
		        }
		    } else {
		        if(layer.getLayerCount() > 0){
		        	ret = layer.getName() + " (" + layer.getLayerCount() + ")";
		        }else{
		        	ret = layer.getName();
		        }
		    }
		    
		    if(layer.getForceAddOrderDraw()) {
		    	ret += "(---)";
		    }
		    
		    return ret;
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	public static final int PROPERTY_CANUNDO = 1;
	public static final int PROPERTY_CANREDO = 2;

	public static final String ID = "com.pip.mapeditor.MapEditor"; //$NON-NLS-1$

	private File sourceFile;
	private boolean dirty = false;
	private MapFile mapFile;

	private StateManager stateMgr;
	
	private MapViewer mapView;
	private MapNpcSelector npcSelector;
	private MapTileSelector tileSelector;
	private MapLandformSelector landformSelector;
	private ThumbView thumbView;
	private CheckboxTableViewer layerViewer;
	private Table layerTable;
	private ToolBar pageToolBar;
	private ArrayList<ToolItem> pageItems;
	private CTabFolder materialTabs;
	private CTabItem tileTabItem, npcTabItem, landformTabItem;
	private ToolItem materialWinButton;
	private SashForm mainSashForm;
	
	private boolean playAnimate;
	private boolean disposed;
	private Thread animateThread;
	private Display display;
	private boolean libChanged = false;			// 是否已修改地图中引用的库文件，在放大和缩小操作的时候会导致库文件改变 
	
	private Action addLayerAction, deleteLayerAction, moveUpAction, moveDownAction, setGroundAction, setSkyAction, forceAddOrderAction;
	
	private ToolItem comNPCItem, realPlayItem, safeAreaTool, tileConfigTool, pickupItem, tileItem, npcItem, passableItem, thumbItem, landformItem, eyesightItem;
	private ToolItem windowItem;
	private ToolItem emulateItem;
	private ToolItem playItem;
	private ToolBar paletteToolBar;
	private ArrayList<ToolItem> paletteItems;
	
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		mainSashForm = new SashForm(container, SWT.VERTICAL);

		final Composite composite = new Composite(mainSashForm, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.verticalSpacing = 0;
		gridLayout_1.marginWidth = 0;
		gridLayout_1.marginHeight = 0;
		gridLayout_1.horizontalSpacing = 0;
		gridLayout_1.numColumns = 4;
		composite.setLayout(gridLayout_1);

		final ToolBar toolBar = new ToolBar(composite, SWT.VERTICAL);
		toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		final ToolBar toolBar_1 = new ToolBar(composite, SWT.VERTICAL);
		toolBar_1.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		playItem = new ToolItem(toolBar_1, SWT.CHECK);
		playItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        playAnimate = playItem.getSelection();
		    }
		});
		playItem.setToolTipText("播放动画");
		playItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/play.gif"));

		final ToolItem newItemToolItem_1 = new ToolItem(toolBar_1, SWT.PUSH);
		newItemToolItem_1.setDisabledImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/sep.gif"));
		newItemToolItem_1.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/sep.gif"));
		newItemToolItem_1.setEnabled(false);

		final ToolItem importItem = new ToolItem(toolBar_1, SWT.PUSH);
		importItem.setToolTipText("导入图片文件");
		importItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onImport();
			}
		});
		importItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/import.gif"));

		final ToolItem exportItem = new ToolItem(toolBar_1, SWT.PUSH);
		exportItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onExport();
			}
		});
		exportItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/export.gif"));
		exportItem.setToolTipText("导出成图片");

		final ToolItem mergeItem = new ToolItem(toolBar_1, SWT.PUSH);
		mergeItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onMerge();
			}
		});
		mergeItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/merge.gif"));
		mergeItem.setToolTipText("合并地图文件");

		final ToolItem enlargeItem = new ToolItem(toolBar_1, SWT.PUSH);
		enlargeItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        onEnlarge();
		    }
		});
		enlargeItem.setToolTipText("放大一倍");
		enlargeItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/enlarge.gif"));

		final ToolItem smallerItem = new ToolItem(toolBar_1, SWT.PUSH);
		smallerItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		    	try {
		    		onSmaller();
		    	} catch (Throwable e1) {
		    		e1.printStackTrace();
		    	}
		    }
		});
		smallerItem.setToolTipText("缩小一倍");
		smallerItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/smaller.gif"));

		final ToolItem dupItem = new ToolItem(toolBar_1, SWT.PUSH);
		dupItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onDup();
			}
		});
		dupItem.setToolTipText("复制地图");
		dupItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/joy.gif"));
		
		if (mapFile.isLibMode){
			final ToolItem propertyItem = new ToolItem(toolBar_1, SWT.NONE);
			propertyItem.addSelectionListener(new SelectionAdapter() {
			});
			propertyItem.setToolTipText("库模式");
			propertyItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/lib.png"));
		}

		final SashForm sashForm_2 = new SashForm(composite, SWT.NONE);
		sashForm_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		pageToolBar = new ToolBar(composite, SWT.NONE);
		pageToolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Composite mapViewContainer = new Composite(sashForm_2, SWT.NONE);
		mapViewContainer.setLayout(new FillLayout());

		final SashForm sashForm_3 = new SashForm(sashForm_2, SWT.VERTICAL);
		sashForm_3.setLayout(new FillLayout());
		
		layerViewer = CheckboxTableViewer.newCheckList(sashForm_3, SWT.BORDER);
		layerTable = layerViewer.getTable();
		layerTable.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
			    GameMap map = getActiveMap();
		        if (map == null) {
		            return;
		        }
		        int sel = layerTable.getSelectionIndex();
		        if (sel == -1) {
		            return;
		        }
				IMapLayer selMapLayer = map.layers.get(sel); 
		        if(selMapLayer.getForceAddOrderDraw()) {
		        	forceAddOrderAction.setText("取消按添加顺序绘制");       	
		        } else {
		        	forceAddOrderAction.setText("按添加顺序绘制");     	
		        }
			}
			
		});

		final TableColumn layerTitleColumn = new TableColumn(layerTable, SWT.NONE);
		layerTitleColumn.setWidth(150);
		layerTitleColumn.setText("层号");
		layerViewer.setContentProvider(new LayerContentProvider());
		layerViewer.setLabelProvider(new LayerLabelProvider());

		final Composite thumbViewContainer = new Composite(sashForm_3, SWT.NONE);
		thumbViewContainer.setLayout(new FillLayout());
		
		sashForm_2.setWeights(new int[] {4, 1 });
		sashForm_3.setWeights(new int[] {1, 1});

		pickupItem = new ToolItem(toolBar, SWT.RADIO);
		pickupItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    updateTool();
			}
		});
		pickupItem.setToolTipText("选择工具");
		pickupItem.setSelection(true);
		pickupItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/pickup.gif"));

		tileItem = new ToolItem(toolBar, SWT.RADIO);
		tileItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    materialTabs.setSelection(0);
			    updateTool();
			}
		});
		tileItem.setToolTipText("贴图工具");
		tileItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/tile.gif"));

		landformItem = new ToolItem(toolBar, SWT.RADIO);
		landformItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        materialTabs.setSelection(2);
		        updateTool();
		    }
		});
		landformItem.setToolTipText("地形工具");
		landformItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/landform.gif"));

		npcItem = new ToolItem(toolBar, SWT.RADIO);
		npcItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    materialTabs.setSelection(1);
			    updateTool();
			}
		});
		npcItem.setToolTipText("地图NPC工具");
		npcItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/npc.gif"));

		passableItem = new ToolItem(toolBar, SWT.RADIO);
		passableItem.setToolTipText("通过性工具");
		passableItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			    materialTabs.setSelection(0);
			    updateTool();
			}
		});
		if(mapFile.isLibMode){
			passableItem.setToolTipText("地面通过性工具");
		}
		passableItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/passable.gif"));

		if(mapFile.isLibMode){
			tileConfigTool = new ToolItem(toolBar, SWT.RADIO);
			tileConfigTool.setToolTipText("天空通过性工具");
			tileConfigTool.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
				    materialTabs.setSelection(0);
				    updateTool();
				}
			});
			tileConfigTool.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/skyPassabel.gif"));
			
//			realPlayItem = new ToolItem(toolBar, SWT.RADIO);
//			realPlayItem.setToolTipText("角色行走工具");
//			realPlayItem.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//				    materialTabs.setSelection(0);
//				    updateTool();
//				}
//			});
//			realPlayItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/joy.gif"));
			
			comNPCItem = new ToolItem(toolBar, SWT.RADIO);
			comNPCItem.setToolTipText("组合NPC工具");
			comNPCItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					materialTabs.setSelection(0);
					updateTool();
				}
			});
			comNPCItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/ComNPC.png"));
			
			safeAreaTool = new ToolItem(toolBar, SWT.RADIO);
			safeAreaTool.setToolTipText("安全区工具");
			safeAreaTool.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					materialTabs.setSelection(0);
					updateTool();
				}
			});
			safeAreaTool.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/safeArea.gif"));
		}
		
		thumbItem = new ToolItem(toolBar, SWT.RADIO);
		thumbItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        materialTabs.setSelection(0);
		        updateTool();
		    }
		});
		thumbItem.setToolTipText("缩略图工具");
		thumbItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/thumb.gif"));

		eyesightItem = new ToolItem(toolBar, SWT.RADIO);
		eyesightItem.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        updateTool();
		    }
		});
		eyesightItem.setToolTipText("视线遮挡工具");
		eyesightItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/eyesight.gif"));

		windowItem = new ToolItem(toolBar, SWT.RADIO);
		windowItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				updateTool();
			}
		});
		windowItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/window.gif"));
		windowItem.setToolTipText("模拟手机屏幕查看");

		emulateItem = new ToolItem(toolBar, SWT.RADIO);
		emulateItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				updateTool();
			}
		});
		emulateItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/character.gif"));
		emulateItem.setToolTipText("模拟人物行走");

		createPageButtons();

		paletteToolBar = new ToolBar(composite, SWT.NONE);
		paletteToolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		createPaletteButtons();

		materialTabs = new CTabFolder(mainSashForm, SWT.BOTTOM);
		final ToolBar toolBar2 = new ToolBar(materialTabs, SWT.HORIZONTAL);
		materialTabs.setTopRight(toolBar2);

		materialWinButton = new ToolItem(toolBar2, SWT.PUSH);
		materialWinButton.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        int totalHeight = mainSashForm.getSize().y;
		        int bottomHeight = materialTabs.getSize().y;
		        if (bottomHeight <= 20) {
                    mainSashForm.setWeights(new int[] { 2, 1 });
		        } else {
                    mainSashForm.setWeights(new int[] { totalHeight / 20, 1 });
		        }
		    }
		});
		materialWinButton.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/down.gif"));

		tileTabItem = new CTabItem(materialTabs, SWT.NONE);
		tileTabItem.setText("贴图");

		final Composite composite_1 = new Composite(materialTabs, SWT.NONE);
		composite_1.setLayout(new FillLayout());
		tileTabItem.setControl(composite_1);
		
		tileSelector = new MapTileSelector(composite_1, SWT.NONE);
		tileSelector.setImageViewerListener(this);
		tileSelector.setInput(mapFile);

		npcTabItem = new CTabItem(materialTabs, SWT.NONE);
		npcTabItem.setText("NPC");

		final Composite composite_2 = new Composite(materialTabs, SWT.NONE);
		composite_2.setLayout(new FillLayout());
		npcTabItem.setControl(composite_2);
		
		npcSelector = new MapNpcSelector(composite_2, SWT.NONE);
		npcSelector.setImageViewerListener(this);
		npcSelector.setInput(mapFile);
        
		landformTabItem = new CTabItem(materialTabs, SWT.NONE);
        landformTabItem.setText("地形");
        
        final Composite composite_3 = new Composite(materialTabs, SWT.NONE);
        composite_3.setLayout(new FillLayout());
        landformTabItem.setControl(composite_3);

        landformSelector = new MapLandformSelector(composite_3, SWT.NONE, this);
        landformSelector.setImageViewerListener(this);
        landformSelector.setInput(mapFile);

        materialTabs.setSelection(1);
		
        materialTabs.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				ToolItem[] tool = toolBar.getItems();
				if(e.item == landformTabItem){
					
					for(int i = 0; i < tool.length; i++){
						if(tool[i] != landformItem){
							tool[i].setSelection(false);
						}
					}
					landformItem.setSelection(true);
					
				}else if(e.item == npcTabItem){
					
					for(int i = 0; i < tool.length; i++){
						if(tool[i] != npcItem){
							tool[i].setSelection(false);
						}
					}
					npcItem.setSelection(true);
				}else if(e.item == tileTabItem){
					for(int i = 0; i < tool.length; i++){
						if(tool[i] != tileItem){
							tool[i].setSelection(false);
						}
					}
					tileItem.setSelection(true);
				}
				updateTool();
			}
        	}
        );

        
		mainSashForm.setWeights(new int[] {2, 1 });
		MenuManager mgr = new MenuManager();
		mgr.add(addLayerAction);
		mgr.add(moveUpAction);
		mgr.add(moveDownAction);
		mgr.add(deleteLayerAction);
		mgr.add(setGroundAction);
		mgr.add(setSkyAction);
		mgr.add(forceAddOrderAction);

		mapView = new MapViewer(mapViewContainer, SWT.NONE);
		mapView.setImageViewerListener(this);
		
		thumbView = new ThumbView(thumbViewContainer, SWT.NONE);
				
		activePageChanged();
		
		layerViewer.setAllChecked(true);
		layerTable.setSelection(0);
		Menu menu = mgr.createContextMenu(layerTable);
		layerTable.setMenu(menu);
		
		layerViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
		        layerConfigChanged();
			}
		});
		layerViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
                if (!Arrays.equals(mapView.getShowLayers(), getShowLayers())) {
                    layerConfigChanged();
                } else {
                    mapView.setActiveLayer(getActiveLayer());
                }
			}
		});
		
		this.setPartName(sourceFile.getName());
		
        display = getSite().getShell().getDisplay();
		animateThread = new Thread(this);
		animateThread.start();
	}
	public String getFilePath(){
		return sourceFile.getAbsolutePath();
	}
	public boolean isLibMode(){
		return mapFile.isLibMode;
	}
    @Override
    public void dispose() {
        super.dispose();
        disposed = true;
        try {
            animateThread.join();
        } catch (Exception e) {
        }
        imageCache.setPalette(null);
    }

    // 创建地图页签
	private void createPageButtons() {
		ToolItem addItem = new ToolItem(pageToolBar, SWT.PUSH);
		addItem.setToolTipText("创建新场景");
		addItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/add.gif"));
		addItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onAddPage();
			}
		});
		
		ToolItem delItem = new ToolItem(pageToolBar, SWT.PUSH);
		delItem.setToolTipText("删除当前场景");
		delItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/delete.gif"));
		delItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onDelPage();
			}
		});
		
		pageItems = new ArrayList<ToolItem>();
		for (int i = 0; i < mapFile.getMaps().size(); i++) {
			ToolItem item = new ToolItem(pageToolBar, SWT.RADIO);
			item.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/" + (i + 1) + ".gif"));
			if (i == 0) {
				item.setSelection(true);
			}
			item.addSelectionListener(this);
			pageItems.add(item);
		}
	}
	
	// 创建调色板页签
	private void createPaletteButtons() {
		ToolItem addItem = new ToolItem(paletteToolBar, SWT.PUSH);
		addItem.setToolTipText("添加新调色板");
		addItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/add.gif"));
		addItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onAddPalette();
			}
		});
		
		ToolItem delItem = new ToolItem(paletteToolBar, SWT.PUSH);
		delItem.setToolTipText("删除当前调色板");
		delItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/delete.gif"));
		delItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onDelPalette();
			}
		});
		
		paletteItems = new ArrayList<ToolItem>();
		for (int i = 0; i < mapFile.refPalettes.size() + 1; i++) {
			ToolItem item = new ToolItem(paletteToolBar, SWT.RADIO);
			item.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/" + i + ".gif"));
			item.addSelectionListener(this);
			paletteItems.add(item);
		}
		paletteItems.get(0).setSelection(true);
	}
	
	// 刷新地图页签
	private void refreshPageButtons() {
		while (pageItems.size() > mapFile.getMaps().size()) {
			int index = pageItems.size() - 1;
			pageItems.get(index).dispose();
			pageItems.remove(index);
		}
		while (pageItems.size() < mapFile.getMaps().size()) {
			int index = pageItems.size();
			ToolItem item = new ToolItem(pageToolBar, SWT.RADIO);
			item.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/" + (index + 1) + ".gif"));
			item.addSelectionListener(this);
			pageItems.add(item);
		}
	}
	
	// 刷新调色板页签
	private void refreshPaletteButtons() {
		while (paletteItems.size() > mapFile.refPalettes.size() + 1) {
			int index = paletteItems.size() - 1;
			paletteItems.get(index).dispose();
			paletteItems.remove(index);
		}
		while (paletteItems.size() < mapFile.refPalettes.size() + 1) {
			int index = paletteItems.size();
			ToolItem item = new ToolItem(paletteToolBar, SWT.RADIO);
			item.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/" + index + ".gif"));
			item.addSelectionListener(this);
			paletteItems.add(item);
		}
	}
	
	private void createActions() {
		addLayerAction = new Action("新建地图层...") {
			public void run() {
				onAddLayer();
			}
		};
		deleteLayerAction = new Action("删除") {
			public void run() {
				onDeleteLayer();
			}
		};
		moveUpAction = new Action("上移") {
			public void run() {
				onMoveUp();
			}
		};
		moveDownAction = new Action("下移") {
			public void run() {
				onMoveDown();
			}
		};
		setGroundAction = new Action("设为人物层") {
		    public void run() {
		        onSetGround();
		    }
		};
		setSkyAction = new Action("设为天空人物层") {
		    public void run() {
		        onSetSky();
		    }
		};
		
		forceAddOrderAction = new Action("按添加顺序绘制") {
		    public void run() {
		        onForceAddOrderDraw();
		    }
		};
		
	}
	
    // 取得当前选中的地图的索引
	private int getActiveMapIndex() {
		for (int i = 0; i < pageItems.size() && i < mapFile.getMaps().size(); i++) {
			if (pageItems.get(i).getSelection()) {
				return i;
			}
		}
		return -1;
	}
	
	// 取得当前选中的地图
	public GameMap getActiveMap() {
		for (int i = 0; i < pageItems.size() && i < mapFile.getMaps().size(); i++) {
			if (pageItems.get(i).getSelection()) {
				return mapFile.getMaps().get(i);
			}
		}
		return null;
	}
	
	// 添加新的地图层
	private void onAddLayer() {
		GameMap map = getActiveMap();
		if (map == null) {
			return;
		}
		
		NewMapLayerDialog dlg = new NewMapLayerDialog(getSite().getShell());
        if (dlg.open() == NewMapLayerDialog.OK) {
            IMapLayer newLayer = null;
            switch (dlg.getLayerType()) {
            case 0:
                newLayer = new AccurateMapLayer(map);
                break;
            case 1:
                newLayer = new BlurMapLayer(map);
                break;
            case 2:
                newLayer = new MapNPCLayer(map);
                break;
            }
            newLayer.setName(dlg.getLayerName());
//            map.layers.add(new AccurateMapLayer(map));
            map.layers.add(newLayer);
    		layerViewer.refresh();
    		int newrow = map.layers.size() - 1;
    		layerViewer.setChecked(new Integer(newrow), true);
    		layerTable.setSelection(newrow);
    		layerConfigChanged();
    		setDirty(true);
        }
	}
	
    // 删除当前选中层。
	private void onDeleteLayer() {
		GameMap map = getActiveMap();
		if (map == null) {
			return;
		}
		int sel = layerTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		if (map.layers.size() == 1) {
			MessageDialog.openError(getSite().getShell(), "错误", "开玩笑，一个地图至少要有一层吧！！。");
			return;
		}
		map.layers.remove(sel);
		layerViewer.refresh();
		if (sel >= map.layers.size()) {
			sel--;
		}
		layerTable.setSelection(sel);
		layerConfigChanged();
		setDirty(true);
	}
	
    // 当前选中层向上移动
	private void onMoveUp() {
		GameMap map = getActiveMap();
		if (map == null) {
			return;
		}
		int sel = layerTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		if (sel <= 1) {
			return;
		}
		IMapLayer layer = map.layers.remove(sel);
		map.layers.add(sel - 1, layer);
		layerViewer.refresh();
		layerTable.setSelection(sel - 1);
		layerConfigChanged();
		setDirty(true);
	}
	
	// 当前选中层向下移动
	private void onMoveDown() {
		GameMap map = getActiveMap();
		if (map == null) {
			return;
		}
		int sel = layerTable.getSelectionIndex();
		if (sel == -1) {
			return;
		}
		if (sel == 0 || sel == map.layers.size() - 1) {
			return;
		}
		IMapLayer layer = map.layers.remove(sel);
		map.layers.add(sel + 1, layer);
        layerViewer.refresh();
		layerTable.setSelection(sel + 1);
		layerConfigChanged();
		setDirty(true);
	}
	
	// 当前选中层设置为人物层
	private void onSetGround() {
	    GameMap map = getActiveMap();
        if (map == null) {
            return;
        }
        int sel = layerTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        map.groundLayer = map.layers.get(sel);
        layerViewer.refresh();
        layerTable.setSelection(sel);
        layerConfigChanged();
        setDirty(true);
	}
	
	// 当前选中层设置为天空人物层
	private void onSetSky() {
	    GameMap map = getActiveMap();
        if (map == null) {
            return;
        }
        int sel = layerTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        map.skyLayer = map.layers.get(sel);
        layerViewer.refresh();
        layerTable.setSelection(sel);
        layerConfigChanged();
        setDirty(true);
	}
	
	private void onForceAddOrderDraw() {
	    GameMap map = getActiveMap();
        if (map == null) {
            return;
        }
        int sel = layerTable.getSelectionIndex();
        if (sel == -1) {
            return;
        }
        
        IMapLayer selMapLayer = map.layers.get(sel);
        if (!selMapLayer.getForceAddOrderDraw()) {
        	int groundIndex = -1;
        	for (int i = 0; i < map.layers.size(); i++) {
        		if (map.groundLayer == map.layers.get(i)) {
        			groundIndex = i;
        			break;
        		}
        	}
        	if (groundIndex <= sel) {
        		MessageDialog.openError(getSite().getShell(), "错误", "人物层以上图层不能按加入顺序绘制，必须按Y顺序绘制。");
        		return;
        	}
        }
        
        selMapLayer.setForceAddOrderDraw(!selMapLayer.getForceAddOrderDraw());
        
        layerViewer.refresh();
        layerTable.setSelection(sel);
        layerConfigChanged();
        setDirty(true);
	}
	
	// 重新配置地图显示的层
	private void layerConfigChanged() {
	    mapView.setActiveLayer(getActiveLayer());
	    mapView.setShowLayers(getShowLayers());
	    mapView.refresh();
	}

	public void setFocus() {
		mapView.setFocus();
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			if (libChanged) {
				mapFile.saveLibFiles();
			}
			mapFile.save(sourceFile);
			libChanged = false;
			setDirty(false);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
			monitor.setCanceled(true);
		}
	}

	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		createActions();

		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		String filePath = Utils.urlToPath(url);
		sourceFile = new File(filePath);
		try {
			mapFile = new MapFile();
			mapFile.load(sourceFile);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(site.getShell(), "错误", "文件格式错误。\n"+e.toString());
		}
		stateMgr = new StateManager(30000000);
		saveState();
		
		imageCache.setPalette(null);
	}

	private boolean saveState() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mapFile.save(bos);
			byte[] newData = bos.toByteArray();
			stateMgr.push(newData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private void restoreState(byte[] data) {
		try {
			int sel = getActiveMapIndex();
			mapFile.load(new ByteArrayInputStream(data));
			if (sel >= mapFile.getMaps().size()) {
				sel = mapFile.getMaps().size() - 1;
			}
			refreshPageButtons();
			if (sel != -1) {
				pageItems.get(sel).setSelection(true);
			}
            sel = getActiveLayer();
			activePageChanged();
			layerViewer.refresh();
			if (sel < layerTable.getItemCount()) {
				layerTable.setSelection(sel);
			}
			sel = tileSelector.getSelectedFrame();
			if (sel <= mapFile.getTileImage().tileInfo.size()) {
				tileSelector.setSelectedFrame(sel);
			} else {
				tileSelector.setSelectedFrame(mapFile.getTileImage().tileInfo.size());
			}
			tileSelector.redraw();
			npcSelector.onContentChanged();
			sel = npcSelector.getSelectedFrame();
			if (sel < mapFile.getAnimates().getAnimateCount()) {
				npcSelector.setSelectedFrame(sel);
			} else {
				npcSelector.setSelectedFrame(mapFile.getAnimates().getAnimateCount() - 1);
			}
			npcSelector.redraw();
			landformSelector.onContentChanged();
			sel = landformSelector.getSelectedFrame();
			if (sel < mapFile.getLandforms().size()) {
			    landformSelector.setSelectedFrame(sel);
			} else {
			    landformSelector.setSelectedFrame(mapFile.getLandforms().size() - 1);
			}
			thumbView.redraw();
			layerConfigChanged();
			dirty = true;
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void undo() {
		if (!canUndo()) {
			return;
		}
		restoreState(stateMgr.getUndoData());
		firePropertyChange(PROPERTY_CANUNDO);
		firePropertyChange(PROPERTY_CANREDO);
	}
	
	public void redo() {
		if (!canRedo()) {
			return;
		}
		restoreState(stateMgr.getRedoData());
		firePropertyChange(PROPERTY_CANUNDO);
		firePropertyChange(PROPERTY_CANREDO);
	}

	public boolean isDirty() {
		return dirty;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	private void setDirty(boolean value) {
		if (value) {
			if (saveState()) {
				firePropertyChange(PROPERTY_CANUNDO);
				firePropertyChange(PROPERTY_CANREDO);
				dirty = value;
				firePropertyChange(PROP_DIRTY);
			}
		} else {
			dirty = value;
			firePropertyChange(PROP_DIRTY);
		}
	}

	public void areaSelected(Object source) {
	}

	public void frameDoubleClicked(Object source, int frame) {
	}

	public void frameSelectionChanged(Object source, int newFrame) {
	}
	
	public void contentChanged(Object source) {
		if (source == mapView) {
			setDirty(true);
			thumbView.redraw();
		} else if (source == npcSelector) {
			setDirty(true);
			if (mapView.getTool() instanceof PickupTool) {
			    ((PickupTool)mapView.getTool()).clearSelection();
			}
			mapView.redraw();
		} else if (source == tileSelector) {
			setDirty(true);
			thumbView.redraw();
			mapView.refresh();
		} else if (source == landformSelector) {
		    setDirty(true);
		    thumbView.redraw();
		    mapView.refresh();
		}
	}

	public boolean canUndo() {
	    return stateMgr.canUndo();
	}
	
	public boolean canRedo() {
	    return stateMgr.canRedo();
	}

	/**
	 * 取得当前选中的层。
	 * @return
	 */
	public int getActiveLayer() {
		return layerTable.getSelectionIndex();
	}

	/**
	 * 取得层显示设置。
	 * @return
	 */
	public boolean[] getShowLayers() {
		GameMap map = getActiveMap();
		if (map == null) {
			return new boolean[0];
		}
		Object[] arr = layerViewer.getCheckedElements();
		StructuredSelection sels = (StructuredSelection)layerViewer.getSelection();
		Object selObj = sels.getFirstElement();
		boolean[] ret = new boolean[map.layers.size()];
		for (int i = 0; i < arr.length; i++) {
		    for (int j = 0; j < map.layers.size(); j++) {
		        if (map.layers.get(j) == arr[i]) {
		            ret[j] = true;
		            break;
		        }
		    }
		}
//        for (int j = 0; j < map.layers.size(); j++) {
//            if (map.layers.get(j) == selObj) {
//                ret[j] = true;
//                break;
//            }
//        }
		return ret;
	}

	/**
	 * 向当前地图文件的素材库中添加一帧图片。通过图片尺寸来区分添加的是贴图还是NPC。
	 * @param imgData
	 */
	public void addImage(int[][] imgData) {
		try {
			if (imgData.length == mapFile.getTileHeight() && imgData[0].length == mapFile.getTileWidth()) {
			    // 如果和贴图大小相等，则加为贴图
			    Object[][] cells = new Object[1][1];
			    cells[0][0] = imgData;
			    AccurateMapLayer tempLayer = new AccurateMapLayer(getActiveMap());
			    tempLayer.setSingle();
			    int oldCount = mapFile.getTileImage().tileInfo.size();
			    new ImportJob(this, cells, tempLayer, 0, false).run(null);
			    int newCount = mapFile.getTileImage().tileInfo.size();
			    if (newCount != oldCount) {
			        tileSelector.redraw();
			        setDirty(true);
			    }
                materialTabs.setSelection(0);
			} else {
			    // 如果和贴图大小不等，则在NPC动画文件中加入新的一帧图片
			    PipAnimateSet animates = mapFile.getAnimates();
			    createFrameOnAnimateSet(animates, imgData);
			    
				int newIndex = animates.getAnimateCount() - 1;
				npcSelector.setSelectedFrame(newIndex);
				npcSelector.onContentChanged();
				setDirty(true);
                materialTabs.setSelection(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
	}
	public void addImageRef(Object image, int[] frames, int tileWidth, int tileHeight) {
		String msg = "当前地图处于库模式.将用选定的图片自动创建一个动画文件.\n" +
				"是否创建?";
		boolean ret = MessageDialog.openConfirm(getSite().getShell(), "确认", msg);
		if(ret){
			FileDialog dlg = new FileDialog(getSite().getShell());
			dlg.setFilterExtensions(new String[]{"*.cts"});
			dlg.setFilterNames(new String[]{"动画文件(cts)"});
			dlg.setFilterPath(sourceFile.getAbsolutePath());
			String file;
			File saveToFile;
			while(true){
				file = dlg.open();
				if(file == null){
					return;
				}
				if(file.endsWith(".cts")==false){
					file += ".cts";
				}
				saveToFile = new File(file);
				if(saveToFile.exists()==false){
					break;
				}else{
					boolean overwrite = MessageDialog.openConfirm(getSite().getShell(), "确认", "" +
							"文件已经存在:\n" +
					saveToFile.getAbsolutePath()+		
					"\n是否覆盖?");
					if(overwrite){
						break;
					}
				}
			}
			PipAnimateSet animates = new PipAnimateSet();
//			PipImage pimg = new PipImage();
//			pimg.addFrame(imgData);
//			animates.addSourceFile("abc.pip", pimg);
			boolean succ = true;
			String fileName = saveToFile.getName().replace(".cts", "");
			for (int i = 0; i < frames.length; i++) {
				int[][] data = null;
				if(image instanceof Image){
					Image img = (Image)image;
					Rectangle bounds = img.getBounds();
					int cols = bounds.width / tileWidth;
					int cellx = frames[i] % cols;
					int celly = frames[i] / cols;
					data = ImageViewer.getImageData(img, new Rectangle(cellx * tileWidth, 
							celly * tileHeight, tileWidth, tileHeight));
				}else if(image instanceof PipImage){
					Image img = ((PipImage)image).getImageDraw(frames[i]).createSWTImage(getSite().getShell().getDisplay(), 0);
					data = ImageViewer.getImageData(img, img.getBounds());
					img.dispose();
				}
				try {
					createFrameOnAnimateSet(animates, data);
				} catch (Exception e) {
					e.printStackTrace();
					fileName = e.toString();
					succ = false;
					break;
				}
			}
			if(animates.getFileCount()==1){
				animates.setFileName(0, fileName+".pip");
			}else
			for(int i=0; i<animates.getFileCount();i++){
				animates.setFileName(i, fileName+i+".pip");
			}
			if(succ){
				try {
					animates.forceSave(saveToFile);
					MessageDialog.openInformation(getSite().getShell(), "提示", "创建成功!\n"+
							saveToFile.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
					MessageDialog.openError(getSite().getShell(), "错误", "保存文件出错:\n"+e.toString());
				}
			}else{
				MessageDialog.openError(getSite().getShell(), "错误", "创建动画出错.\n"+fileName);
			}
		}
	}
	private void createFrameOnAnimateSet(PipAnimateSet animates, int[][] imgData) throws Exception {

	    int[] index = addImageToAnimateSet(animates, imgData);
	    PipImage targetImage = animates.getSourceImage(index[0]);
	    
	    // 用这个新图片创建一个新的动画帧
	    PipAnimateFrame newFrame = animates.addFrame("NPC");
	    PipAnimateFramePiece newPiece = newFrame.addPiece(index[0], index[1]);
	    newPiece.setTransition(index[2]);
	    if (index[2] < 4) {
		    newPiece.setDx(-targetImage.getImageData(newPiece.getFrame()).getWidth() / 2);
            newPiece.setDy(-targetImage.getImageData(newPiece.getFrame()).getHeight());
	    } else {
	    	newPiece.setDx(-targetImage.getImageData(newPiece.getFrame()).getHeight() / 2);
            newPiece.setDy(-targetImage.getImageData(newPiece.getFrame()).getWidth());
	    }
	    
        // 用这个新的动画帧创建一个新的动画序列
        PipAnimate newAnimate = animates.addAnimate("NPC");
        newAnimate.addFrame(animates.getFrameCount() - 1);		
	}
	/**
	 * 向当前地图的NPC素材库中添加一个新的动画。
	 */
	public void addAnimate(PipAnimateSet animateSet, int animateIndex) {
		if(mapFile.isLibMode){
			try {
				TileView tv = (TileView)getSite().getPage().findView(TileView.ID);
				int hashCode = tv.curFileHashCode;
				mapFile.addAnimateRef(hashCode);
				ProjectParser.addFileRef(tv.curFilePath, getFilePath());
				npcSelector.onContentChanged();
	            setDirty(true);
	            materialTabs.setSelection(1);
			}catch(RepeatAddException rae){
				MessageDialog.openError(getSite().getShell(), "错误", rae.getMessage());
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "错误", e.toString());
				e.printStackTrace();
			}
			return;
		}
	    try {
//    	    // 把指定动画序列中所有用到的图片帧加入到地图素材中
    	    PipAnimateSet animates = mapFile.getAnimates();
    	    PipAnimate animate = animateSet.getAnimate(animateIndex);
    	    HashSet<Point> usedFrames = animate.getUsedFrames();
    	    HashMap<Point, int[]> pieceIDMap = reflectNewPieces(animates, animateSet, usedFrames);
    	    
    	    // 复制所有用到的动画帧
    	    HashMap<Integer, Integer> frameIDMap = animates.copyUsedFrame(animate, pieceIDMap);
            
            // 复制动画序列
            PipAnimate newAni = animates.addAnimate(animate.getName());
            newAni.fillWith(animate, frameIDMap);
//            use mapFile adding directly(won't check similar image)
//	    	mapFile.addAnimate(animateSet, animateIndex);
	    	
            int newIndex = animates.getAnimateCount() - 1;
            npcSelector.setSelectedFrame(newIndex);
            npcSelector.onContentChanged();
            setDirty(true);
            materialTabs.setSelection(1);
        } catch (Exception e) {
        	e.printStackTrace();
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
	}
	
	/**
	 * // 把animateSet动画序列中所有用到的图片帧加入到animates中
	 * @param animates 接收帧的动画集合
	 * @param animateSet 要放到接收动画里的动画
	 * @param usedFrames
	 * @param b
	 * @return
	 * @throws Exception
	 */
	private HashMap<Point, int[]> reflectNewPieces(PipAnimateSet animates, PipAnimateSet animateSet, HashSet<Point> usedFrames) throws Exception {
		HashMap<Point, int[]> pieceIDMap = new HashMap<Point, int[]>();
	    for (Point p1 : usedFrames) {
	        int[][] imgData = animateSet.getSourceImage(p1.x).getImageDraw(p1.y).getPixels(0);
	        int[] newID = addImageToAnimateSet(animates, imgData);
	        pieceIDMap.put(p1, newID);
	    }
	    return pieceIDMap;
	}
	/**
	 * 添加一个地形。
	 */
	public void addLandform(LandformImage landform) {
	    try {
	    	if (mapFile.getBlurTileWidth() != landform.getImageData(0).width ||
	    			mapFile.getBlurTileHeight() != landform.getImageData(0).height) {
	    		throw new Exception("地形贴图宽度和地图指定的贴图宽度不一致，不能添加。");
	    	}
	    	if(mapFile.isLibMode){
	    		TileView tv = (TileView)getSite().getPage().findView(TileView.ID);
				int hashCode = tv.curFileHashCode;
	    		mapFile.addLandFormRef(hashCode);
	    		ProjectParser.addFileRef(tv.curFilePath, sourceFile.getAbsolutePath());
	    		landformSelector.onContentChanged();
	            setDirty(true);
	            materialTabs.setSelection(2);
	    		return;
	    	}
    	    TileSet newLandform = new TileSet(true);
    	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	    DataOutputStream dos = new DataOutputStream(bos);
    	    landform.save(dos, true);
    	    dos.flush();
    	    newLandform.image.load(new ByteArrayInputStream(bos.toByteArray()));
    	    for (int i = 0; i < newLandform.image.getImgCount(); i++) {
    	        TileInfo ti = new TileInfo();
    	        ti.frameID = i;
    	        ti.thumbColor = 0;
    	        ti.transit = 0;
    	        ti.unpassable = false;
    	        newLandform.tileInfo.add(ti);
    	    }
    	    mapFile.getLandforms().add(newLandform);
    	    landformSelector.onContentChanged();
            setDirty(true);
            materialTabs.setSelection(2);
	    }catch(RepeatAddException re){
	    	MessageDialog.openError(getSite().getShell(), "错误", re.getLocalizedMessage());
        } catch (Exception e) {
        	e.printStackTrace();
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
	}
	/**
	 * 
	// 把图片数据加入到动画文件中。首先，查找所有已存在的帧，看是否有错误率在10%以内的<br/>
	// 帧，如果有，提示用户选择策略；如果用户选择加入新帧，则查找一个添加颜色数最少的<br/>
	// 图片加入。如果所有图片颜色数都满了，则创建一个新图片。<br/>
	// 返回的数组中包含新帧对应的图片索引、图片内帧号和翻转值。<br/>
	 * @param animates
	 * @param imgData
	 * @return
	 * @throws Exception
	 */
	private int[] addImageToAnimateSet(PipAnimateSet animates, int[][] imgData) throws Exception {
		SimilarImageResult  similarImg = animates.findSimilarImage(imgData);
		if(similarImg.perfectMatch){
			return similarImg.perfectOne;
		}
	    
	    // 如果有错误率在10%内的图片，提示用户选择其中一个还是创建新的帧
	    if (similarImg.candidates.size() > 0) {
	        ChooseMatchFrameDialog dlg = new ChooseMatchFrameDialog(getSite().getShell(), animates, imgData, similarImg.candidates);
	        if (dlg.open() == ChooseMatchFrameDialog.OK) {
	            return dlg.getSelectedFrame();
	        }
	    }
	    return animates.addImageToAnimateSetReal(imgData);
	}
	public void widgetDefaultSelected(SelectionEvent e) {}

	// 页面按钮触发事件。
	public void widgetSelected(SelectionEvent e) {
		for (int i = 0; i < pageItems.size(); i++) {
			if (pageItems.get(i) == e.getSource()) {
				activePageChanged();
				return;
			}
		}
		for (int i = 0; i < paletteItems.size(); i++) {
			if (paletteItems.get(i) == e.getSource()) {
				activePaletteChanged();
				return;
			}
		}
	}

	// 导入图片到当前地图的当前层。必须选中精确地图层才能执行此动作。
	private void onImport() {
		GameMap map = getActiveMap();
		if (map == null) {
			return;
		}
		int layerIndex = getActiveLayer();
		if (layerIndex < 0 || layerIndex >= map.layers.size()) {
		    MessageDialog.openInformation(getSite().getShell(), "导入", "请选中一个地图层。");
			return;
		}
		IMapLayer layer = map.layers.get(layerIndex);
		if (!(layer instanceof AccurateMapLayer)) {
		    MessageDialog.openInformation(getSite().getShell(), "导入", "此功能只能作用于精确地图层。");
		    return;
		}
		AccurateMapLayer alayer = (AccurateMapLayer)layer;
		ImportMapDialog dlg = new ImportMapDialog(getSite().getShell());
		if (dlg.open() != ImportMapDialog.OK) {
			return;
		}
		try {
			Image img = new Image(getSite().getShell().getDisplay(), dlg.getFileName());
			Rectangle rect = img.getBounds();
			if (rect.width != map.width || rect.height != map.height) {
				img.dispose();
				throw new Exception("图片大小不匹配，需要" + map.width + "x" + map.height + "。");
			}
			Object[][] cells = new Object[alayer.getLayerData().length][alayer.getLayerData()[0].length];
			int[][] allData = ImageViewer.getImageData(img, img.getBounds());
			for (int i = 0; i < cells.length; i++) {
				for (int j = 0; j < cells[0].length; j++) {
					int cx = j * map.parent.getTileWidth();
					int cy = i * map.parent.getTileHeight();
					int cw = map.parent.getTileWidth();
					int ch = map.parent.getTileHeight();
					int[][] thisData = new int[ch][cw];
					for (int ii = 0; ii < ch; ii++) {
						System.arraycopy(allData[cy + ii], cx, thisData[ii], 0, cw);
					}
					cells[i][j] = thisData;
				}
			}
			img.dispose();
			
			ImportJob job = new ImportJob(this, cells, alayer, dlg.getTolerance(), dlg.isIgnoreUnmatched());
			ProgressMonitorDialog progress = new ProgressMonitorDialog(getSite().getShell());
			progress.setCancelable(true);
			progress.run(true, true, job);
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
	}
	
	/**
	 *  添加新地图，弹出对话框让用户输入地图信息。 
	 */
	private void onAddPage() {
		if (mapFile.getMaps().size() == 9) { 
			MessageDialog.openError(getSite().getShell(), "错误", "一个关卡里你放这么多地图干啥？");
			return;
		}
		NewMapDialog dlg = new NewMapDialog(getSite().getShell(), this.mapFile);
		if(isLibMode()){
			dlg.setDefaultChooseType(1);
			dlg.setEnableChooseType(false);
		}
		if (dlg.open() == NewMapDialog.OK) {
			int mw = dlg.getMapWidth();
			int mh = dlg.getMapHeight();
			int mapType = dlg.getMapType();
			createNewMap(mw, mh, mapType);

			refreshPageButtons();
			for (int i = 0; i < pageItems.size(); i++) {
				pageItems.get(i).setSelection(i == pageItems.size() - 1);
			}
			this.activePageChanged();
			setDirty(true);
		}
	}
	
    // 创建新地图，新地图缺省具有下面4层：背景层（贴图）、地面层、人物层、天空层
	private void createNewMap(int width, int height, int type) {
        if(mapFile.isLibMode){
        	createNewLibMap(width, height, type);
        	return;
        }
		GameMap newmap = new GameMap(mapFile, width, height);
        if (type == 0) {
            AccurateMapLayer newLayer = new AccurateMapLayer(newmap);
            newLayer.setName("背景层");
            newmap.layers.add(newLayer);
        } else {
            BlurMapLayer newLayer = new BlurMapLayer(newmap);
            newLayer.setName("背景层");
            newmap.layers.add(newLayer);
        }
        MapNPCLayer newLayer = new MapNPCLayer(newmap);
        newLayer.setName("地面层");
        newmap.layers.add(newLayer);
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("人物层");
        newmap.layers.add(newLayer);
        newmap.groundLayer = newLayer;
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("天空层");
        newmap.layers.add(newLayer);
        
        mapFile.getMaps().add(newmap);
	}
	private void createNewLibMap(int width, int height, int type){
		GameMap newmap = new GameMap(mapFile, width, height);
        if (type == 0) {
            AccurateMapLayer newLayer = new AccurateMapLayer(newmap);
            newLayer.setName("背景层");
            newmap.layers.add(newLayer);
        } else {
            BlurMapLayer newLayer = new BlurMapLayer(newmap);
            newLayer.setName("背景层");
            newmap.layers.add(newLayer);
        }
        MapNPCLayer newLayer = new MapNPCLayer(newmap);
        newLayer.setName("地板层");
        newmap.layers.add(newLayer);
        
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("地毯层");
        newmap.layers.add(newLayer);
        
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("人物层");
        newmap.layers.add(newLayer);
        
        newmap.groundLayer = newLayer;
        
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("屋顶层");
        newmap.layers.add(newLayer);
        
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("天空地板层");
        newmap.layers.add(newLayer);
        
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("飞行层");
        newmap.layers.add(newLayer);
        
        newmap.skyLayer = newLayer;
        
        newLayer = new MapNPCLayer(newmap);
        newLayer.setName("天顶层");
        newmap.layers.add(newLayer);
        
        mapFile.getMaps().add(newmap);
	}
	// 删除当前地图
	private void onDelPage() {
		if (mapFile.getMaps().size() == 1) {
			MessageDialog.openError(getSite().getShell(), "错误", "大哥，没有地图没法玩儿啊。");
			return;
		}
		if (!MessageDialog.openConfirm(getSite().getShell(), "删除地图", "你确认要删除这张地图吗？")) {
			return;
		}
		int sel = getActiveMapIndex();
		mapFile.getMaps().remove(sel);
		if (sel >= mapFile.getMaps().size()) {
			sel = mapFile.getMaps().size() - 1;
		}
		refreshPageButtons();
		pageItems.get(sel).setSelection(true);
		activePageChanged();
		setDirty(true);
	}
	
	// 选中地图改变事件。
	private void activePageChanged() {
		GameMap map = getActiveMap();
		layerViewer.setInput(map);
		layerViewer.refresh();
		layerViewer.setAllChecked(true);
		mapView.setInput(map);
		updateTool();
		mapView.refresh();
		thumbView.setInput(map);
		thumbView.redraw();
	}
	
	// 更新地图工具
	private void updateTool() {
	    if (pickupItem.getSelection()) {
            mapView.setTool(new PickupTool(mapView, tileSelector));
	    } else if (tileItem.getSelection()) {
            mapView.setTool(new TileTool(mapView, tileSelector));
	    } else if (npcItem.getSelection()) {
            mapView.setTool(new MapNPCTool(mapView, npcSelector));
	    } else if (passableItem.getSelection()) {
            if(this.mapFile.isLibMode){
            	mapView.setTool(new TileConfigTool(mapView, TileConfigTool.CONFIG_MASK_GROUND, false, "红色区域表示不可通过"));
            }else{
            	mapView.setTool(new PassableTool(mapView, tileSelector));
            }
	    }else if( comNPCItem!=null && comNPCItem.getSelection() ){
	    	mapView.setTool(new MultiAnimNPCTool(mapView, tileSelector));
	    }else if( realPlayItem!=null && realPlayItem.getSelection() ){
	    	mapView.setTool(new RealPlayTool(mapView, tileSelector));
	    }else if( safeAreaTool!=null && safeAreaTool.getSelection() ){
        	mapView.setTool(new TileConfigTool(mapView, TileConfigTool.CONFIG_MASK_SAFE_AREA, false, "红色区域表示安全区"));
	    }else if( tileConfigTool!=null && tileConfigTool.getSelection() ){
	    	mapView.setTool(new TileConfigTool(mapView, TileConfigTool.CONFIG_MASK_SKY, false, "红色区域表示不可通过"));
	    } else if (thumbItem.getSelection()) {
	        mapView.setTool(new ThumbTool(mapView, tileSelector));
	    } else if (landformItem.getSelection()) {
	        mapView.setTool(new LandformTool(mapView, landformSelector));
	    } else if (eyesightItem.getSelection()) {
	    	if(this.mapFile.isLibMode){
	    		mapView.setTool(new TileConfigTool(mapView, TileConfigTool.CONFIG_MASK_SIGHT, true, "红色区域表示视线遮挡"));
	    	}else{
	    		mapView.setTool(new EyesightTool(mapView));
	    	}
	    } else if (windowItem.getSelection()) {
	    	mapView.setTool(new WindowViewTool(mapView));
	    } else if (this.emulateItem.getSelection()) {
	    	File f = new File(System.getProperty("user.home"), "imageworkshop/role.cts");
	    	if (!f.exists()) {
	    		MessageDialog.openError(getSite().getShell(), "错误", "要使用此工具，请先在用户目录下准备imageworkshop/role.cts动画文件。");
	    		return;
	    	}
	    	try {
	    		mapView.setTool(new EmulateWalkTool(mapView));
	    		if (!playAnimate) {
	    			playAnimate = true;
	    			playItem.setSelection(true);
	    		}
	    	} catch (Exception e) {
	    		MessageDialog.openError(getSite().getShell(), "错误", e.toString());
	    	}
	    }
	}
	
	/**
	 * 导入完成处理。
	 */
	public void onImportFinished() {
        mapView.refresh();
        thumbView.redraw();
        tileSelector.redraw();
        setDirty(true);
	}
	
	// 驱动动画
	public void run() {
        while (!disposed) {
            if (this.playAnimate) {
                mapView.step();
                npcSelector.step();
                try {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            try {
                            	if(mapView.isDisposed()==false){
                            		mapView.redraw();
                            	}
                            	if(npcSelector.isDisposed()==false){
                            		npcSelector.redraw();
                            	}
                            } catch (Exception e) {
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
	}

	/*
	 * 合并另外一个地图文件中的所有地图到本地图文件中。包括动画、地形都需要合并。精确地图不能合并。
	 */
	private void onMerge() {
		// 选择文件
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		dlg.setFilterExtensions(new String[] { "*.map" });
		dlg.setFilterNames(new String[] { "地图文件(*.map)" });
		String file = dlg.open();
		if (file == null) {
			return;
		}
		
		// 载入文件
		MapFile newMap;
		try {
			newMap = new MapFile();
			newMap.load(new File(file));
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", "文件格式错误。");
			return;
		}
		
		// 检查：精确地图不能合并
		if (newMap.getTileImage().tileInfo.size() != 0) {
			MessageDialog.openError(getSite().getShell(), "错误", "不能合并带有精确贴图块的地图。");
			return;
		}
		for (GameMap map : newMap.getMaps()) {
			if (map.layers.size() > 0 && map.layers.get(0) instanceof AccurateMapLayer) {
				MessageDialog.openError(getSite().getShell(), "错误", "不能合并精确地图。");
				return;
			}
		}
		
		// 合并地形
		Map<Integer, Integer> landformIDMap = new HashMap<Integer, Integer>();
		try {
			for (int i = 0; i < newMap.getLandforms().size(); i++) {
				boolean found = false;
				String landformData = TileSet.imageToText(newMap.getLandforms().get(i).image);
				for (int j = 0; j < mapFile.getLandforms().size(); j++) {
					String compData = TileSet.imageToText(mapFile.getLandforms().get(j).image);
					if (landformData.equals(compData)) {
						found = true;
						landformIDMap.put(i, j);
						break;
					}
				}
				if (!found) {
					mapFile.getLandforms().add(newMap.getLandforms().get(i));
					landformIDMap.put(i, mapFile.getLandforms().size() - 1);
				}
			}
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", "合并地形失败。");
			return;
		}
		
		// 合并NPC动画
		int existNpcCount = mapFile.getAnimates().getAnimateCount();
		for (int i = 0; i < newMap.getAnimates().getAnimateCount(); i++) {
			addAnimate(newMap.getAnimates(), i);
		}
		
		// 合并NPC扩展描述信息
		for (long npcID : newMap.getNPCs().keySet()) {
			NPCImageInfo info = newMap.getNPCs().get(npcID);
			mapFile.getNPCs().put(npcID + existNpcCount, info);
		}
		
		// 合并地图
		for (GameMap gameMap : newMap.getMaps()) {
			for (IMapLayer layer : gameMap.layers) {
				if (layer instanceof BlurMapLayer) {
					BlurMapLayer bml = (BlurMapLayer)layer;
					if (bml.getBaseLandform() != -1) {
						int newBase = landformIDMap.get(bml.getBaseLandform());
						bml.setBaseLandform(newBase);
					}
					byte[][] mapData = bml.getLayerData();
					for (int i = mapData.length - 1; i >= 0; i--) {
						for (int j = mapData[i].length - 1; j >= 0; j--) {
							if (mapData[i][j] != -1) {
								int a = mapData[i][j];
								Integer b = landformIDMap.get(a);
								mapData[i][j] = (byte)b.intValue();
							}
						}
					}
				} else if (layer instanceof MapNPCLayer) {
					MapNPCLayer mnl = (MapNPCLayer)layer;
					for (MapNPC npc : mnl.getNpcs()) {
						npc.animate += existNpcCount;
					}
				}
			}
			mapFile.getMaps().add(gameMap);
			gameMap.parent = mapFile;
		}
		
		refreshPageButtons();
		this.activePageChanged();
		setDirty(true);
	}

    /*
     * 放大所有地图中的元素一倍（包括场景、NPC、地形等等）。
     */
    private void onEnlarge() {
    	try {
	        mapFile.enlarge();
	        setDirty(true);
	        libChanged = true;
	        imageCache.clear();
	        mapView.refresh();
	        npcSelector.refresh();
	        tileSelector.redraw();
	        landformSelector.refresh();
    	} catch (Exception e) {
    		MessageDialog.openError(getSite().getShell(), "错误", e.toString());
    	}
    }

    /*
     * 缩小所有地图中的元素一倍（包括场景、NPC、地形等等）。
     */
    private void onSmaller() {
    	try {
	        mapFile.smaller();
	        setDirty(true);
	        libChanged = true;
	        imageCache.clear();
	        mapView.refresh();
	        npcSelector.refresh();
	        tileSelector.redraw();
	        landformSelector.refresh();
    	} catch (Exception e) {
    		e.printStackTrace();
    		MessageDialog.openError(getSite().getShell(), "错误", e.toString());
    	}
    }
    
    /*
     * 把地图的一部分截取出来，复制出一张新的地图。
     */
    private void onDup() {
    	GameMap map = getActiveMap();
    	if (map == null) {
    		return;
    	}
    	DupMapDialog dlg = new DupMapDialog(getSite().getShell());
    	dlg.mapWidth = map.width;
    	dlg.mapHeight = map.height;
    	if (dlg.open() == DupMapDialog.OK) {
    		GameMap newmap = map.dup();
    		newmap.cut(dlg.x, dlg.y, dlg.width, dlg.height);
			mapFile.getMaps().add(newmap);

			refreshPageButtons();
			for (int i = 0; i < pageItems.size(); i++) {
				pageItems.get(i).setSelection(i == pageItems.size() - 1);
			}
			this.activePageChanged();
			setDirty(true);
    	}
    }
    
    /*
     * 把当前的地图导出成一个图片文件。
     */
    private void onExport() {
		GameMap map = getActiveMap();
		if (map == null) {
			return;
		}

		// 选择文件
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
		dlg.setFilterExtensions(new String[] { "*.png" });
		dlg.setFilterNames(new String[] { "PNG图片文件(*.png)" });
		String file = dlg.open();
		if (file == null) {
			return;
		}
		
		// 创建内存图片
		Image img = new Image(getSite().getShell().getDisplay(), map.width, map.height);
		GC gc = new GC(img);
		this.mapView.drawMapOnBuffer(gc);
		gc.dispose();
		
		// 保存文件
		try {
	        PngEncoder enc = new PngEncoder(img);
	        FileOutputStream fos = new FileOutputStream(file);
	        enc.encode32(fos, false);
	        fos.close();
	        MessageDialog.openInformation(getSite().getShell(), "导出", "导出成功！");
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
		}
		img.dispose();
    }
 
    /*
     * 增加一个调色板。
     */
    private void onAddPalette() {
		// 选择文件
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.OPEN);
		dlg.setFilterExtensions(new String[] { "*.png", "*.pip" });
		dlg.setFilterNames(new String[] { "PNG文件(*.png)", "PIP文件(*.pip)" });
		String file = dlg.open();
		if (file == null) {
			return;
		}

		// 载入PNG图片，取得调色板
		int[] palette;
		if (file.toLowerCase().endsWith(".pip")) {
			PipImage image = null;
			try {
				image = new PipImage();
				image.load(file);
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getSite().getShell(), "错误", "文件格式错误。\n"+e);
				return;
			}
			if (image.isTrueColor()) {
				MessageDialog.openError(getSite().getShell(), "错误", "不支持从真彩色PIP图片中提取调色板。");
				return;
			}
			int[] pal = image.getImagePalettes().get(0).getPalette();
			palette = new int[pal.length];
			System.arraycopy(pal, 0, palette, 0, pal.length);
		} else {
			Image image = null;
			try {
				image = new Image(getSite().getShell().getDisplay(), file);
			} catch (Exception e) {
				e.printStackTrace();
				MessageDialog.openError(getSite().getShell(), "错误", "文件格式错误。\n"+e);
				return;
			}
			int[][] imgData = ImageViewer.getImageData(image, image.getBounds());
			Set<Integer> colorSet = new HashSet<Integer>();
			for (int[] line : imgData) {
				for (int pixel : line) {
					colorSet.add(pixel);
				}
			}
			Object[] arr = colorSet.toArray();
			palette = new int[arr.length];
			for (int i = 0; i < palette.length; i++) {
				palette[i] = ((Integer)arr[i]).intValue();
			}
		}
		
		// 添加新调色板
		mapFile.refPalettes.add(palette);
		refreshPaletteButtons();
		activePaletteChanged();
		setDirty(true);
    }
    
    /*
     * 删除当前选中调色板。
     */
    private void onDelPalette() {
    	int paletteIndex = getActivePalette();
    	if (paletteIndex > 0) {
    		mapFile.refPalettes.remove(paletteIndex - 1);
    		refreshPaletteButtons();
    		paletteItems.get(0).setSelection(true);
    		activePaletteChanged();
    		setDirty(true);
    	}
    }
    
    /*
     * 取得当前选中的调色板
     */
	public int getActivePalette() {
		for (int i = 0; i < paletteItems.size(); i++) {
			if (paletteItems.get(i).getSelection()) {
				return i;
			}
		}
		return 0;
	}
	
    /*
     * 选中调色板改变事件。
     */
	private void activePaletteChanged() {
		int paletteIndex = getActivePalette();
		if (paletteIndex == 0) {
			mapView.setRefPalette(null);
		} else {
			mapView.setRefPalette(mapFile.refPalettes.get(paletteIndex - 1));
		}
		mapView.refresh();
	}
}
