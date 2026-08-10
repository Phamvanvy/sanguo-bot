package com.pip.sanguo.editor.area;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.jdom.Document;

import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.ImageViewerListener;
import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.pkg.PackageFile;
import com.pip.sanguo.data.pkg.PackageFileItem;
import com.pip.sanguo.data.pkg.PackageUtils;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestRewardSet;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.quest.GameAreaCache;
import com.pip.sanguo.editor.wizard.NewQuestWizard;
import com.pip.util.AutoSelectAll;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pipimage.utils.Utils;
import com.swtdesigner.ResourceManager;

public class GameAreaEditor extends DefaultDataObjectEditor implements ImageViewerListener, SelectionListener, Runnable, IFileModificationListener {
    class QuestListContentProvider implements IStructuredContentProvider {
        // 支持2种input，Integer表示mapid，GameMapNPC表示NPC
        public Object[] getElements(Object inputElement) {
            java.util.List retList = new ArrayList();
            if (inputElement instanceof Integer) {
                int mapID = ((Integer)inputElement).intValue();
                for (Quest q : sceneQuests) {
                    if (q.type == 1) {
                        retList.add(q);
                    } else {
                        if (q.startNPC != -1 && (q.startNPC >> 12) == mapID) {
                            retList.add(q);
                        } else if (q.finishNPC != -1 && (q.finishNPC >> 12) == mapID) {
                            retList.add(q);
                        }
                    }
                }
                retList.add("新建场景任务...");
            } else if (inputElement instanceof GameMapNPC) {
                int npcID = ((GameMapNPC)inputElement).getGlobalID();
                for (Quest q : sceneQuests) {
                    if (q.type == 0) {
                        if (q.startNPC != -1 && q.startNPC == npcID) {
                            retList.add(q);
                        } else if (q.finishNPC != -1 && q.finishNPC == npcID) {
                            retList.add(q);
                        }
                    }
                }
                retList.add("新建任务...");
            }
            return retList.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class NPCTemplateListProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Object[] ret = ((ProjectData)inputElement).getDataListByType(NPCTemplate.class).toArray();
            Arrays.sort(ret, new Comparator<Object>() {
                public int compare(java.lang.Object arg0, java.lang.Object arg1) {
                    NPCTemplate npc1 = (NPCTemplate)arg0;
                    NPCTemplate npc2 = (NPCTemplate)arg1;
                    if (npc1.id < npc2.id) {
                        return -1;
                    } else if (npc1.id == npc2.id) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                
                public boolean equals(java.lang.Object arg0) {
                    return false;
                }
            });
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private Composite propertyContainer;
    private ListViewer npcTemplateListViewer;
    private List npcTemplateList;
    private Text textSource;
    private Text textDescription;
    private Text textTitle;
    private Text textID;
    public static final String ID = "com.pip.sanguo.editor.AreaEditor"; //$NON-NLS-1$

    private MapFile mapFile;
    private MapFile mapFileLarge;
    private GameAreaInfo areaInfo;

    private GameMapViewer mapView;
    private ToolBar pageToolBar;
    private ArrayList<ToolItem> pageItems;
    private PropertySheetViewer propEditor;
    private ListViewer questListViewer;
    private List questList;

    private java.util.List<Quest> sceneQuests = new ArrayList<Quest>();
    
    private boolean playAnimate;
    private boolean disposed;
    private Thread animateThread;
    private Display display;

    private ToolItem pickupItem, npcItem, patrolPathItem, exitItem;
    
    private ToolItem rangeItem;
    
    private FileDialog mapFileDialog;
    
    
    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        final CTabFolder tabFolder = new CTabFolder(container, SWT.BOTTOM);

        final CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
        tabItem1.setText("场景编辑");
        tabFolder.setSelection(tabItem1);

        final CTabItem tabItem2 = new CTabItem(tabFolder, SWT.NONE);
        tabItem2.setText("基本信息");

        final Composite composite2 = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 2;
        composite2.setLayout(gridLayout_1);
        tabItem2.setControl(composite2);

        final Label label = new Label(composite2, SWT.NONE);
        label.setText("ID：");

        textID = new Text(composite2, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(composite2, SWT.NONE);
        label_1.setText("标题：");

        textTitle = new Text(composite2, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(composite2, SWT.NONE);
        label_2.setText("描述：");

        textDescription = new Text(composite2, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textDescription.setLayoutData(gd_textDescription);
        textDescription.addFocusListener(AutoSelectAll.instance);
        textDescription.addModifyListener(this);

        final Label label_3 = new Label(composite2, SWT.NONE);
        label_3.setText("目录：");

        textSource = new Text(composite2, SWT.BORDER);
        textSource.setEditable(false);
        final GridData gd_textSource = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSource.setLayoutData(gd_textSource);
        textSource.addFocusListener(AutoSelectAll.instance);
        textSource.addModifyListener(this);

        final Composite tabContainer1 = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.numColumns = 2;
        tabContainer1.setLayout(gridLayout);
        tabItem1.setControl(tabContainer1);

        final ToolBar toolBar = new ToolBar(tabContainer1, SWT.VERTICAL);
        toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        pickupItem = new ToolItem(toolBar, SWT.RADIO);
        pickupItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateTool();
            }
        });
        pickupItem.setToolTipText("选择工具");
        pickupItem.setSelection(true);
        pickupItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/pickup.gif"));

        npcItem = new ToolItem(toolBar, SWT.RADIO);
        npcItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateTool();
            }
        });
        npcItem.setToolTipText("NPC工具");
        npcItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/npc.gif"));

        exitItem = new ToolItem(toolBar, SWT.RADIO);
        exitItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/exit.gif"));
        exitItem.setToolTipText("传送点工具");
        exitItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateTool();
            }
        });
        
        patrolPathItem = new ToolItem(toolBar, SWT.RADIO);
        patrolPathItem.setToolTipText("巡逻路径工具");
        patrolPathItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateTool();
            }
        });
        patrolPathItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/path.gif"));
        
        final ToolItem newItemToolItem = new ToolItem(toolBar, SWT.PUSH);
        newItemToolItem.setDisabledImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/sep.gif"));
        newItemToolItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/sep.gif"));
        newItemToolItem.setEnabled(false);

        final ToolItem playItem = new ToolItem(toolBar, SWT.CHECK);
        playItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                playAnimate = playItem.getSelection();
            }
        });
        playItem.setToolTipText("播放动画");
        playItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/play.gif"));

        final ToolItem hideNPCToolItem = new ToolItem(toolBar, SWT.CHECK);
        hideNPCToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                mapView.setShowMapNPC(!hideNPCToolItem.getSelection());
            }
        });
        hideNPCToolItem.setToolTipText("隐藏地图NPC");
        hideNPCToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/shownpc.gif"));

        final ToolItem newItemToolItem_1 = new ToolItem(toolBar, SWT.PUSH);
        newItemToolItem_1.setEnabled(false);
        newItemToolItem_1.setDisabledImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/sep.gif"));
        newItemToolItem_1.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/sep.gif"));

        final ToolItem editMapToolItem = new ToolItem(toolBar, SWT.PUSH);
        editMapToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                onEditMap();
            }
        });
        editMapToolItem.setToolTipText("编辑地图");
        editMapToolItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/editmap.gif"));

        final ToolItem exportToolItem = new ToolItem(toolBar, SWT.PUSH);
        exportToolItem.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		onExport();
        	}
        });
        exportToolItem.setToolTipText("导出资源");
        exportToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/disk.gif"));

        final ToolItem pathFinderToolItem = new ToolItem(toolBar, SWT.PUSH);
        pathFinderToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                mapView.setTool(new PathFinderTool(GameAreaEditor.this, mapView));
            }
        });
        pathFinderToolItem.setToolTipText("测试路径查找");
        pathFinderToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/passable.gif"));

        final ToolItem eyesightToolItem = new ToolItem(toolBar, SWT.PUSH);
        eyesightToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                mapView.setTool(new TestEyesightTool(GameAreaEditor.this, mapView));
            }
        });
        eyesightToolItem.setToolTipText("测试视线遮挡");
        eyesightToolItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/eyesight.gif"));
        
        rangeItem = new ToolItem(toolBar, SWT.PUSH);
        rangeItem.setToolTipText("NPC视野及追击范围显示");
        rangeItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mapView.setShowNpcRange(!mapView.getShowNpcRange());
                mapView.redraw();
            }
        });
        rangeItem.setImage(ResourceManager.getPluginImage(EditorPlugin.getDefault(), "icons/mapeditor/rangeinfo.gif"));

        final ToolItem viewEnlargeItem = new ToolItem(toolBar, SWT.PUSH);
        viewEnlargeItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                int index = getActiveMapIndex();
                if (index == -1) {
                    return;
                }
                try {
                    GameMap map = mapFileLarge.getMaps().get(index);
                    GameMapInfo mi = areaInfo.maps.get(index);
                    ViewMapDialog dlg = new ViewMapDialog(getSite().getShell());
                    dlg.setData(map, mi);
                    dlg.open();
                } catch (Exception e1) {
                    MessageDialog.openError(getSite().getShell(), "错误", e.toString());
                }
            }
        });
        viewEnlargeItem.setToolTipText("查看放大版本地图");
        viewEnlargeItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/enlarge.gif"));
        
        final SashForm sashForm = new SashForm(tabContainer1, SWT.NONE);

        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        mapView = new GameMapViewer(sashForm, SWT.NONE);
        mapView.setImageViewerListener(this);
        
        final Composite rightPanel = new Composite(sashForm, SWT.NONE);
        final GridLayout gridLayout_4 = new GridLayout();
        gridLayout_4.marginWidth = 0;
        gridLayout_4.marginHeight = 0;
        gridLayout_4.horizontalSpacing = 0;
        rightPanel.setLayout(gridLayout_4);

        propertyContainer = new Composite(rightPanel, SWT.NONE);
        propertyContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final GridLayout gridLayout_2 = new GridLayout();
        gridLayout_2.verticalSpacing = 0;
        gridLayout_2.marginWidth = 0;
        gridLayout_2.marginHeight = 0;
        gridLayout_2.horizontalSpacing = 0;
        propertyContainer.setLayout(gridLayout_2);
        
        propEditor = new PropertySheetViewer(propertyContainer, SWT.BORDER, false);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        propEditor.setRootEntry(rootEntry);
        propEditor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData)propEditor.getControl().getLayoutData()).exclude = false;
        
        npcTemplateListViewer = new ListViewer(propertyContainer, SWT.BORDER | SWT.V_SCROLL);
        npcTemplateListViewer.setContentProvider(new NPCTemplateListProvider());
        npcTemplateListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                updateTool();
            }
        });
        npcTemplateList = npcTemplateListViewer.getList();
        npcTemplateList.setBounds(0, 0,127, 324);
        final GridData gd_npcTemplateList = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_npcTemplateList.exclude = true;
        npcTemplateList.setLayoutData(gd_npcTemplateList);
        npcTemplateListViewer.setInput(EditorApplication.getInstance().getProjectData());
        
        propertyContainer.layout();

        questListViewer = new ListViewer(rightPanel, SWT.V_SCROLL | SWT.BORDER);
        questListViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent arg0) {
                StructuredSelection sel = (StructuredSelection)questListViewer.getSelection();
                if (sel.isEmpty()) {
                    return;
                }
                Object selObj = sel.getFirstElement();
                try {
                    if ("新建场景任务...".equals(selObj)) {
                        new NewQuestWizard(1, getEditObject().id).run();
                       
                        // 刷新列表
                        findQuests();
                        questListViewer.refresh();
                    } else if ("新建任务...".equals(selObj)) {
                        new NewQuestWizard(0, ((GameMapNPC)questListViewer.getInput()).getGlobalID()).run();

                        // 刷新列表
                        findQuests();
                        questListViewer.refresh();
                    } else {
                        Quest q = (Quest)selObj;
                        DataListView.tryEditObject(q);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    MessageDialog.openError(getSite().getShell(), "错误", e1.toString());
                }
            }
        });
        questListViewer.setContentProvider(new QuestListContentProvider());
        questList = questListViewer.getList();
        final GridData gd_questList = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd_questList.heightHint = 200;
        questList.setLayoutData(gd_questList);

        sashForm.setWeights(new int[] {3, 1 });
        new Label(tabContainer1, SWT.NONE);
        
        pageToolBar = new ToolBar(tabContainer1, SWT.NONE);
        pageToolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        display = getSite().getShell().getDisplay();
        animateThread = new Thread(this);
        animateThread.start();
        
        // 设置初始值
        GameArea dataDef = (GameArea)editObject;
        createPageButtons();
        activePageChanged();
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDescription.setText(dataDef.description);
        textSource.setText(dataDef.source.getAbsolutePath());

        final Composite composite = new Composite(composite2, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
        final GridLayout gridLayout_3 = new GridLayout();
        gridLayout_3.numColumns = 2;
        composite.setLayout(gridLayout_3);

        final Button buttonImportMap = new Button(composite, SWT.NONE);
        buttonImportMap.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                importMapFile();
            }
        });
        buttonImportMap.setText("更新地图文件...");
        final GridData gd_buttonImportMap = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_buttonImportMap.widthHint = 200;
        buttonImportMap.setLayoutData(gd_buttonImportMap);

        final Button buttonImportMap2 = new Button(composite, SWT.NONE);
        buttonImportMap2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                importLargeMapFile();
            }
        });
        final GridData gd_buttonImportMap2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_buttonImportMap2.widthHint = 200;
        buttonImportMap2.setLayoutData(gd_buttonImportMap2);
        buttonImportMap2.setText("更新放大版本地图文件...");

        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        
        // 载入地图文件和地图信息文件
        try {
            GameArea dataDef = (GameArea)editObject;
            mapFile = new MapFile();
            File mapf = new File(dataDef.source, "game.map");
            mapFile.load(mapf);
            File mapfl = new File(dataDef.source, "game_l.map");
            if (mapfl.exists()) {
                mapFileLarge = new MapFile();
                mapFileLarge.load(mapfl);
            } else {
                mapFileLarge = new MapFile();
                mapFileLarge.load(mapf);
                mapFileLarge.enlarge();
                mapFileLarge.save(mapfl);
            }
            if (mapFile.getMaps().size() != mapFileLarge.getMaps().size()) {
                throw new Exception("大地图文件和小地图文件地图数量不一致。");
            }
            areaInfo = new GameAreaInfo(dataDef);
            if (new File(dataDef.source, "info.xml").exists()) {
                areaInfo.load();
            } else {
                areaInfo.save();
            }
            verifyMapInfo();

            // 监控文件的变化
            FileWatcher.watch(mapf, this);
            FileWatcher.watch(mapfl, this);
            
            // 找出关联任务
            findQuests();
        } catch (Exception e) {
            e.printStackTrace();
            throw new PartInitException("关卡格式错误。", e);
        }
    }
    
    // 修正地图描述信息，使其和地图文件一致
    private void verifyMapInfo() {
        GameArea dataDef = (GameArea)editObject;
        if (areaInfo.maps.size() > mapFile.getMaps().size()) {
            while (areaInfo.maps.size() > mapFile.getMaps().size()) {
                areaInfo.maps.remove(areaInfo.maps.size() - 1);
            }
        } else if (areaInfo.maps.size() < mapFile.getMaps().size()) {
            while (areaInfo.maps.size() < mapFile.getMaps().size()) {
                GameMapInfo newInfo = new GameMapInfo(dataDef);
                newInfo.name = "未命名场景";
                areaInfo.maps.add(newInfo);
            }
        }
        for (int i = 0; i < areaInfo.maps.size(); i++) {
            areaInfo.maps.get(i).id = i;
        }
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        GameArea dataDef = (GameArea)editObject;
        GameAreaCache.clearAreaInfo(dataDef.id);
        
        // 读取输入：对象ID、标题、描述
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDescription.getText();
        
        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
        
        areaInfo.save();
        
        // 保存客户端下载文件以维持版本号
        PackageFile pkgtemp = new PackageFile();
        pkgtemp.setName(String.valueOf(dataDef.id));
        pkgtemp.setVersion(0);
        PackageUtils.makeClientPackage(dataDef, mapFile, areaInfo, pkgtemp, 1.0f);
        pkgtemp.save(new File(dataDef.source, "client.pkg"));
        
        pkgtemp = new PackageFile();
        pkgtemp.setName(String.valueOf(dataDef.id));
        pkgtemp.setVersion(0);
        PackageUtils.makeClientPackage(dataDef, mapFileLarge, areaInfo, pkgtemp, 2.0f);
        pkgtemp.save(new File(dataDef.source, "client_l.pkg"));
    }

    /**
     * 保存当前编辑状态成一个对象。派生类应覆盖此方法。
     */
    protected Object saveState() {
        try {
            return areaInfo.saveToXML();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 根据以前保存的编辑状态恢复当前编辑状态。派生类应覆盖此方法。
     */
    protected void loadState(Object stateObj) {
        try {
            areaInfo.loadFromXML((Document)stateObj);
        } catch (Exception e) {
        }
        activePageChanged();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        disposed = true;
        try {
            animateThread.join();
        } catch (Exception e) {
        }
        FileWatcher.unwatch(this);
    }

    // 创建地图页签
    private void createPageButtons() {
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
    
    // 刷新地图页签
    private void refreshPageButtons() {
        boolean resetSel = false;
        while (pageItems.size() > mapFile.getMaps().size()) {
            int index = pageItems.size() - 1;
            ToolItem ti = pageItems.get(index);
            if (ti.getSelection()) {
                resetSel = true;
            }
            ti.dispose();
            pageItems.remove(index);
        }
        while (pageItems.size() < mapFile.getMaps().size()) {
            int index = pageItems.size();
            ToolItem item = new ToolItem(pageToolBar, SWT.RADIO);
            item.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/mapeditor/" + (index + 1) + ".gif"));
            item.addSelectionListener(this);
            pageItems.add(item);
        }
        if (resetSel && pageItems.size() > 0) {
            pageItems.get(0).setSelection(true);
        }
        pageToolBar.layout();
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

    // 更新地图工具
    private void updateTool() {
        if (pickupItem.getSelection()) {
            // 显示属性窗口
            ((GridData)npcTemplateList.getLayoutData()).exclude = true;
            npcTemplateList.setVisible(false);
            ((GridData)propEditor.getControl().getLayoutData()).exclude = false;
            propEditor.getControl().setVisible(true);
            propertyContainer.layout();
            
            // 创建拾取工具
            mapView.setTool(new GamePickupTool(this, mapView));
        } else if (npcItem.getSelection()) {
            // 显示NPC列表
            ((GridData)npcTemplateList.getLayoutData()).exclude = false;
            npcTemplateList.setVisible(true);
            ((GridData)propEditor.getControl().getLayoutData()).exclude = true;
            propEditor.getControl().setVisible(false);
            propertyContainer.layout();
            
            // 创建NPC工具
            StructuredSelection sel = (StructuredSelection)npcTemplateListViewer.getSelection();
            if (sel.isEmpty()) {
                mapView.setTool(null);
            } else {
                NPCTemplate template = (NPCTemplate)sel.getFirstElement();
                mapView.setTool(new GameMapNPCTool(this, mapView, template));
            }
        } else if (exitItem.getSelection()) {
            // 创建出口工具
            mapView.setTool(new GameMapExitTool(this, mapView));
        } else if (patrolPathItem.getSelection()) {
            // 创建巡逻路径工具
            mapView.setTool(new GamePatrolPathTool(this, mapView));
        }
        setEditingObject(null);
    }
    
    /**
     * 设置当前显示属性的对象。
     * @param obj 当前选中的对象，null表示什么都没有选中
     */
    public void setEditingObject(GameMapObject obj) {
        if (obj == null) {
            if (getActiveMapIndex() != -1) {
                propEditor.setInput(new Object[] { new GameMapPropertySource(this, areaInfo.maps.get(getActiveMapIndex())) });
                Integer mid = areaInfo.maps.get(getActiveMapIndex()).getGlobalID();
                if (questListViewer.getInput() == null || !questListViewer.getInput().equals(mid)) {
                    questListViewer.setInput(areaInfo.maps.get(getActiveMapIndex()).getGlobalID());
                }
            } else {
                propEditor.setInput(new Object[0]);
                questListViewer.setInput(null);
            }
        } else if (obj instanceof GameMapExit) {
            propEditor.setInput(new Object[] { new GameMapExitPropertySource(this, (GameMapExit)obj) });
            questListViewer.setInput(null);
        } else if (obj instanceof GameMapNPC) {
            propEditor.setInput(new Object[] { new GameMapNPCPropertySource(this, (GameMapNPC)obj) });
            questListViewer.setInput(obj);
        }
    }
    
    public void areaSelected(Object source) {}

    public void frameDoubleClicked(Object source, int frame) {}

    public void frameSelectionChanged(Object source, int newFrame) {}
    
    public void contentChanged(Object source) {
        if (source == mapView) {
            setDirty(true);
        }
    }

    public void widgetDefaultSelected(SelectionEvent e) {}
    
    // 页面按钮触发事件。
    public void widgetSelected(SelectionEvent e) {
        activePageChanged();
    }
    
    // 选中地图改变事件。
    private void activePageChanged() {
        GameMap map = getActiveMap();
        mapView.setInput(map, areaInfo.maps.get(getActiveMapIndex()));
        updateTool();
        mapView.refresh();
    }
    
    /**
     * 设置修改标志，但不影响UNDO Buffer。
     */
    public void setDirtyWithoutUndo() {
        lockUndoBuffer = true;
        setDirty(true);
        lockUndoBuffer = false;
    }
    
    /**
     * 文本修改后设置修改标志。
     */
    public void modifyText(final ModifyEvent e) {
        setDirtyWithoutUndo();
    }
    
    // 编辑地图文件
    private void onEditMap() {
        GameArea dataDef = (GameArea)editObject;
        File mapf = new File(dataDef.source, "game.map");
        
        // 检查是否已经打开编辑器，如果已经打开则激活，否则打开
        IEditorPart editor = null;
        IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((mapf.getAbsolutePath())));
        FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        editor = page.findEditor(input);
        if (editor != null) {
            page.activate(editor);
        } else {
            try {
                page.openEditor(input, MapEditor.ID);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
    }
    
    // 导出地图文件
    private void onExport() {
    	DirectoryDialog dlg = new DirectoryDialog(getSite().getShell());
        dlg.setText("导出关卡包");
        dlg.setMessage("请选择导出目录：");
        String newPath = dlg.open();
        if (newPath != null) {
        	try {
	        	PackageFile pkgf = new PackageFile();
	            PackageUtils.makeClientPackage((GameArea)getEditObject(), mapFile, areaInfo, pkgf, 1.0f);
	            for (int i = 0; i < pkgf.getFileCount(); i++) {
	            	PackageFileItem item = pkgf.getFile(i);
	            	Utils.saveFileData(new File(newPath, item.name), item.data);
	            }
	            
	            File largeDir = new File(newPath, "large");
	            largeDir.mkdirs();
	            pkgf = new PackageFile();
                PackageUtils.makeClientPackage((GameArea)getEditObject(), mapFileLarge, areaInfo, pkgf, 2.0f);
                for (int i = 0; i < pkgf.getFileCount(); i++) {
                    PackageFileItem item = pkgf.getFile(i);
                    Utils.saveFileData(new File(largeDir, item.name), item.data);
                }
        	} catch (Exception e) {
        		MessageDialog.openError(this.getSite().getShell(), "错误", e.toString());
        	}
        }
    }
    
    /*
     * 导入新的地图文件。
     */
    private void importMapFile() {
        if (mapFileDialog == null) { 
            mapFileDialog = new FileDialog(getSite().getShell(), SWT.OPEN);
            mapFileDialog.setFilterExtensions(new String[] { "*.map" });
            mapFileDialog.setFilterNames(new String[] { "地图文件(*.map)" });
        }
        String path = mapFileDialog.open();
        if (path != null) {
            try {
                GameArea dataDef = (GameArea)editObject;
                File mapf = new File(dataDef.source, "game.map");
                MapFile mf = new MapFile();
                mf.load(new File(path));
                mf.save(mapf);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
    }
    
    /*
     * 导入放大版本的地图文件。
     */
    private void importLargeMapFile() {
        if (mapFileDialog == null) { 
            mapFileDialog = new FileDialog(getSite().getShell(), SWT.OPEN);
            mapFileDialog.setFilterExtensions(new String[] { "*.map" });
            mapFileDialog.setFilterNames(new String[] { "地图文件(*.map)" });
        }
        String path = mapFileDialog.open();
        if (path != null) {
            try {
                GameArea dataDef = (GameArea)editObject;
                File mapf = new File(dataDef.source, "game_l.map");
                MapFile mf = new MapFile();
                mf.load(new File(path));
                mf.save(mapf);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
    }
    
    public void fileModified(File f) {
        GameArea dataDef = (GameArea)editObject;
        File mapf = new File(dataDef.source, "game.map");
        File mapfl = new File(dataDef.source, "game_l.map");
        if (mapf.equals(f)) {
            // 地图文件变化，重新载入
            MapFile newMapFile = new MapFile();
            try {
                newMapFile.load(mapf);
                mapFile = newMapFile;
                verifyMapInfo();
            } catch (Exception e) {
            }
            display.asyncExec(new Runnable() {
                public void run() {
                    refreshPageButtons();
                    activePageChanged();
                    setDirty(true);
                }
            });
        } else if (mapfl.equals(f)) {
            // 大地图文件变化，重新载入
            MapFile newMapFile = new MapFile();
            try {
                newMapFile.load(mapfl);
                mapFileLarge = newMapFile;
            } catch (Exception e) {
            }
        }
    }
    
    // 驱动动画
    public void run() {
        while (!disposed) {
            if (this.playAnimate) {
                mapView.step();
                try {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            try {
                                mapView.redraw();
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
     * 找出和当前编辑的关卡有关联的任务。
     */
    protected void findQuests() {
        sceneQuests.clear();
        GameArea ga = (GameArea)getEditObject();
        java.util.List<DataObject> quests = ga.owner.getDataListByType(Quest.class);
        for (DataObject dobj : quests) {
            Quest q = (Quest)dobj;
            if (q.type == 1) {
                if (q.areaID == ga.id) {
                    sceneQuests.add(q);
                }
            } else {
                if (q.startNPC != -1 && (q.startNPC >> 16) == ga.id) {
                    sceneQuests.add(q);
                } else if (q.finishNPC != -1 && (q.startNPC >> 16) == ga.id) {
                    sceneQuests.add(q);
                }
            }
        }
    }
}
