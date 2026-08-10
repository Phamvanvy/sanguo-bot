package com.pip.sanguo.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.IProjectDataListener;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.item.ItemTreeViewer;
import com.pip.sanguo.editor.util.DataTypeManager;
import com.pip.sanguo.editor.util.Settings;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;

/**
 * 项目中所有类型的数据对象的列表。这个View用一个CTabFolder来划分数据类型，每一个Tab表示
 * 一类数据。
 * @author lighthu
 */
public class DataListView extends ViewPart implements ModifyListener, IFileModificationListener, IProjectDataListener {
    /**
     * 数据对象表格文本提供者。每个表格包含一类数据，包括ID和名字两列。
     * @author lighthu
     */
    class DataObjectLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof String) {
                if (columnIndex == 0) {
                    return (String)element;
                } else {
                    return "";
                }
            } else if (element instanceof DataObjectCategory) {
                if (columnIndex == 0) {
                    String ret = ((DataObjectCategory)element).name;
                    if ("".equals(ret)) {
                        ret = "<未分类>";
                    }
                    return ret;
                } else {
                    return "";
                }
            } else if (element instanceof DataObject) {
                DataObject dobj = (DataObject)element;
                if (columnIndex == 0) {
                    return String.valueOf(dobj.id);
                } else if (columnIndex == 1) {
                    return dobj.getTitle();
                } else {
                    return dobj.getComments();
                }
            } else {
                return element.toString();
            }
        }
        
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if(element instanceof DataObjectCategory || element instanceof String) {
                    return EditorPlugin.getDefault().getImageRegistry().get("itemtype");
                } else {
                    return EditorPlugin.getDefault().getImageRegistry().get("item");
                }
            } 
            return null;
        }
    }
    
    /**
     * 数据对象树形表格数据提供者。每个表格包含一类数据。
     * @author lighthu
     */
    class ProjectDataListProvider implements ITreeContentProvider {
        private Class dataClass;
        private String filterText;

        public ProjectDataListProvider(Class cls) {
            dataClass = cls;
        }
        
        public void setFilterText(String text) {
            filterText = text;
        }
        
        public Object[] getElements(Object inputElement) {
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            if (filterText == null || filterText.length() == 0) {
                // 如果没有设置过滤，则显示所有的分类
                List cates = new ArrayList(proj.getCategoryListByType(dataClass));
                cates.add("新建分类...");
                return cates.toArray();
            } else {
                // 如果设置了过滤，则只显示过滤后又内容的分类
                List cates = new ArrayList(proj.getCategoryListByType(dataClass));
                String ft = filterText.toLowerCase();
                Iterator itor = cates.iterator();
                while (itor.hasNext()) {
                    DataObjectCategory cate = (DataObjectCategory)itor.next();
                    boolean match = false;
                    for (DataObject dobj : cate.objects) {
                        if (dobj.toString().toLowerCase().contains(ft)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        itor.remove();
                    }
                }
                cates.add("新建分类...");
                return cates.toArray();
            }
        }
        
        public void dispose() {}
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

		public Object[] getChildren(Object parentElement) {
		    if (parentElement instanceof DataObjectCategory) {
		        DataObjectCategory cate = (DataObjectCategory)parentElement;
		        if (filterText == null || filterText.length() == 0) {
		            return cate.objects.toArray();
		        } else {
		            String ft = filterText.toLowerCase();
		            List<DataObject> matchList = new ArrayList<DataObject>();
		            for (DataObject dobj : cate.objects) {
		                if (dobj.toString().toLowerCase().contains(ft)) {
                            matchList.add(dobj);
                        }
		            }
		            return matchList.toArray();
		        }
		    }
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
		    return getChildren(element) != null;
		}
    }
    
    // 包含所有数据类型的面板
    private CTabFolder dataTypeTabFolder;
    // 各数据类型的Tab
    private CTabItem[] dataTypeTabs;
    // 各数据类型的表格
    private TreeViewer[] dataTreeViewers;
    // 各数据类型的表格
    private Tree[] dataListTree;
    // 各数据类型的过滤输入框
    private Text[] dataListFilterText;

    // 命令：切换项目目录
    private Action switchProjectAction;
    // 命令：新建/删除数据对象
    private Action newAction, deleteAction;
    public static final String ID = "com.pip.sanguo.editor.DataListView"; //$NON-NLS-1$

    /**
     * Create contents of the view part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        createActions();

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        dataTypeTabFolder = new CTabFolder(container, SWT.NONE);
        
        // 创建所有数据类型的Tab
        int typeCount = DataTypeManager.editableClasses.length;
        dataTypeTabs = new CTabItem[typeCount];
        dataTreeViewers = new TreeViewer[typeCount];
        dataListTree = new Tree[typeCount];
        dataListFilterText = new Text[typeCount];
        for (int i = 0; i < typeCount; i++) {
            // 创建Tab对象 
            dataTypeTabs[i] = new CTabItem(dataTypeTabFolder, SWT.NONE);
            dataTypeTabs[i].setText(DataTypeManager.getTypeName(DataTypeManager.editableClasses[i]));
            Composite tabComp = new Composite(dataTypeTabFolder, SWT.NONE);
            GridLayout gd_comp = new GridLayout(1, true);
            gd_comp.marginBottom = 0;
            gd_comp.marginHeight = 0;
            gd_comp.marginLeft = 0;
            gd_comp.marginRight = 0;
            gd_comp.marginTop = 0;
            gd_comp.marginWidth = 0;
            gd_comp.verticalSpacing = 0;
            gd_comp.horizontalSpacing = 0;
            tabComp.setLayout(gd_comp);
            
            //创建过滤输入框
            dataListFilterText[i] = new Text(tabComp, SWT.BORDER);
            GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, false);
            dataListFilterText[i].setLayoutData(gd_text);
            dataListFilterText[i].addModifyListener(this);

            //创建树对象
            if (DataTypeManager.editableClasses[i] == Item.class || DataTypeManager.editableClasses[i] == Equipment.class) {
                dataTreeViewers[i] = new ItemTreeViewer(tabComp, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
            } else {
                dataTreeViewers[i] = new TreeViewer(tabComp, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
                dataTreeViewers[i].getTree().setLinesVisible(true);
            }
            dataTreeViewers[i].setLabelProvider(new DataObjectLabelProvider());
            dataTreeViewers[i].setContentProvider(new ProjectDataListProvider(DataTypeManager.editableClasses[i]));
            dataTreeViewers[i].addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event) {
                    StructuredSelection sel = (StructuredSelection)event.getSelection();
                    if (sel.isEmpty()) {
                        return;
                    }
                    Object obj = sel.getFirstElement();
                    if (obj instanceof DataObject) {
                        editObject((DataObject)obj);
                    } else if (obj instanceof DataObjectCategory) {
                        expandOrCollapseNode((TreeViewer)event.getViewer(), obj);
                    } else if (obj instanceof String) {
                        // 新建分类
                        InputDialog dlg = new InputDialog(getSite().getShell(), "新建分类", "请输入新分类的名称：", "新分类", new IInputValidator() {
                            public String isValid(String newText) {
                                if (newText.trim().length() == 0) {
                                    return "分类名称不能为空。";
                                } else {
                                    return null;
                                }
                            }
                        });
                        if (dlg.open() != InputDialog.OK) {
                            return;
                        }
                        String newname = dlg.getValue();
                        int clsIndex = dataTypeTabFolder.getSelectionIndex();
                        ProjectData proj = EditorApplication.getInstance().getProjectData();
                        try {
                            Class cls = DataTypeManager.editableClasses[clsIndex];
                            proj.newCategory(cls, newname);
                            dataTreeViewers[clsIndex].refresh();
                        } catch (Exception e) {
                            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
                        }
                    }
                }
            });
            dataListTree[i] = dataTreeViewers[i].getTree();
            dataListTree[i].setHeaderVisible(true);
            GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true);
            dataListTree[i].setLayoutData(gd_tree);
            
            // 设置DND
            setupDragAndDrop(dataTreeViewers[i]);
            
            // 设置树表格列
            final TreeColumn column1 = new TreeColumn(dataListTree[i], SWT.LEFT);
			column1.setWidth(200);
			column1.setText("ID");
			
			final TreeColumn column2 = new TreeColumn(dataListTree[i], SWT.LEFT);
			column2.setWidth(200);
			column2.setText("标题");

            final TreeColumn column3 = new TreeColumn(dataListTree[i], SWT.LEFT);
            column3.setWidth(200);
            column3.setText("备注");
			
			// 创建右键菜单
            MenuManager mgr = new MenuManager();
            mgr.add(newAction);
            mgr.add(deleteAction);
            Menu menu = mgr.createContextMenu(dataListTree[i]);
            dataListTree[i].setMenu(menu);

            // 设置表格内容
			dataTreeViewers[i].setInput(new Object());
			
            // 把表格加入Tab
            dataTypeTabs[i].setControl(tabComp);
        }

        initializeToolBar();
        initializeMenu();
        EditorApplication.getProj().setDataListener(this);
        watchDataFiles();
    }
    
    private void expandOrCollapseNode(TreeViewer viewer, Object node) {
        if (viewer.getExpandedState(node)) {
            viewer.collapseToLevel(node, 1);
        } else {
            viewer.expandToLevel(node, 1);
        }
    }

    /**
     * Create the actions
     */
    private void createActions() {

        switchProjectAction = new Action("切换工作目录...") {
            public void run() {
                switchProject();
            }
        };
        
        newAction = new Action("新建(&N)...") {
            public void run() {
                onNew();
            }
        };
        
        deleteAction = new Action("删除(&D)") {
            public void run() {
                onDelete();
            }
        };
        // Create the actions
    }

    /**
     * Initialize the toolbar
     */
    private void initializeToolBar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
    }

    /**
     * Initialize the menu
     */
    private void initializeMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

        menuManager.add(switchProjectAction);
    }

    @Override
    public void setFocus() {
        // Set the focus
        dataTypeTabFolder.setFocus();
    }

    // 弹出对话框选择新的数据目录。
    private void switchProject() {
        if (!getSite().getWorkbenchWindow().getActivePage().closeAllEditors(true)) {
            return;
        }
        ProjectData prj = EditorApplication.getInstance().getProjectData();
        DirectoryDialog dlg = new DirectoryDialog(getSite().getShell());
        dlg.setFilterPath(prj.baseDir.getAbsolutePath());
        dlg.setText("选择目录");
        dlg.setMessage("请选择项目目录：");
        String newPath = dlg.open();
        if (newPath != null) {
            Settings.workingDir = new java.io.File(newPath);
            try {
                prj.load(new java.io.File(newPath));
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "载入数据错误", e.toString());
            }
            prj.setDataListener(this);
            watchDataFiles();
            checkData();
            refresh();
        }
    }
    
    /**
     * 检查数据完整性（ID不能重复）。
     */
    public void checkData() {
        ProjectData prj = EditorApplication.getInstance().getProjectData();
        for (Class cls : ProjectData.supportDataClasses) {
            String clsName = DataTypeManager.getTypeName(cls);
            if (clsName == null) {
                clsName = cls.getName();
            }
            StringBuilder sb = new StringBuilder();
            List<DataObject> list = prj.getDataListByType(cls);
            Set<Integer> idSet = new HashSet<Integer>();
            for (DataObject obj : list) {
                if (idSet.contains(obj.id)) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(clsName + ": " + obj.id + "重复");
                } else {
                    idSet.add(obj.id);
                }
            }
            if (sb.length() > 0) {
                MessageDialog.openError(getSite().getShell(), "警告", sb.toString());
            }
        }
    }
    
    /**
     * 刷新数据列表。
     */
    public void refresh() {
        for (ContentViewer viewer : dataTreeViewers) {
            viewer.refresh();
        }
    }
    
    /**
     * 刷新某一类型的数据列表。
     */
    public void refresh(Class cls) {
        for (int i = 0; i < DataTypeManager.editableClasses.length; i++) {
            if (DataTypeManager.editableClasses[i] == cls) {
                dataTreeViewers[i].refresh();
                break;
            }
        }
    }
    
    /**
     * 刷新一个对象的显示。
     */
    public void refresh(DataObject obj) {
        Class clazz = obj.getClass();
        for (int i = 0; i < DataTypeManager.editableClasses.length; i++) {
            if (DataTypeManager.editableClasses[i] == clazz) {
                dataTreeViewers[i].refresh(obj);
                break;
            }
        }
    }
    
    /**
     * 打开一个数据对象的编辑器。
     */
    public void editObject(DataObject obj) {
        String editorID = DataTypeManager.getEditorID(obj.getClass());
        try {
            getSite().getWorkbenchWindow().getActivePage().openEditor(new DataObjectInput(obj), editorID);
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", "打开编辑器失败，原因：\n" + e.toString());
        }
    }
    
    /**
     * 强制停止编辑某个对象。
     */
    private void stopEdit(DataObject obj) {
        IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().findEditor(new DataObjectInput(obj));
        if (editor != null) {
            getSite().getWorkbenchWindow().getActivePage().closeEditor(editor, false);
        }
    }
    
    // 新建当前类型的数据对象。
    private void onNew() {
        try {
            int tabIndex = dataTypeTabFolder.getSelectionIndex();
            Runnable wizard = DataTypeManager.getCreateWizard(DataTypeManager.editableClasses[tabIndex]);
            wizard.run();
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", "创建对象失败，原因：\n" + e.toString());
        }
    }
    
    // 删除当前选中的数据对象。
    private void onDelete() {
        // 取得选中的对象
        int tabIndex = dataTypeTabFolder.getSelectionIndex();
        StructuredSelection sel = (StructuredSelection)dataTreeViewers[tabIndex].getSelection();
        if (sel.isEmpty()) {
            return;
        }
        Object[] selObjs = sel.toArray();
        
        // 找出所有依赖于选中对象的数据对象
        List<DataObject> relateObjects = EditorApplication.getInstance().getProjectData().findRelateObjects(selObjs);
        
        // 提示用户确认删除
        StringBuffer buf = new StringBuffer();
        buf.append("请确认是否删除以下数据对象：\n");
        for (Object obj : selObjs) {
            if(obj instanceof Item){
                /* 物品 */
                buf.append("物品");
                buf.append(": ");
                buf.append(obj.toString());
                buf.append("\n");
            }
            else if(obj instanceof Equipment){
                /* 装备 */
                buf.append("装备 ");
                buf.append(": ");
                buf.append(obj.toString());
                buf.append("\n");
            }
            else{
                /* 普通类型 */
                buf.append(DataTypeManager.getTypeName(obj.getClass()));
                buf.append(": ");
                buf.append(obj.toString());
                buf.append("\n");
            }
        }
        if (relateObjects.size() > 0) {
            buf.append("下列相关对象也将一起被删除：\n");
            for (DataObject obj : relateObjects) {
                buf.append(DataTypeManager.getTypeName(obj.getClass()));
                buf.append(": ");
                buf.append(obj.toString());
                buf.append("\n");
            }
        }
        buf.setLength(buf.length() - 1);
        if (!MessageDialog.openConfirm(getSite().getShell(), "删除确认", buf.toString())) {
            return;
        }
        
        // 删除所有选中对象和相关对象
        for (Object obj : selObjs) {
            DataObject dobj = (DataObject)obj;
            stopEdit(dobj);
            EditorApplication.getInstance().getProjectData().deleteObject(dobj);
        }
        for (DataObject obj : relateObjects) {
            stopEdit(obj);
            EditorApplication.getInstance().getProjectData().deleteObject(obj);
        }
        try {
            EditorApplication.getInstance().getProjectData().saveAll();
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", "保存数据失败，原因：\n" + e.toString());
        }
        
        // 刷新列表
        refresh();
    }
    
    /**
     * 取得选中的对象。
     * @return
     */
    public Object[] getSelectedObjects() {
        int tabIndex = dataTypeTabFolder.getSelectionIndex();
        StructuredSelection sel = (StructuredSelection)dataTreeViewers[tabIndex].getSelection();
        return sel.toArray();
    }
    
    /**
     * 过滤器文本内容修改。
     */
    public void modifyText(ModifyEvent e) {
        int index = -1;
        for (int i = 0; i < dataListFilterText.length; i++) {
            if (dataListFilterText[i] == e.widget) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return;
        }
        String newText = dataListFilterText[index].getText();
        ProjectDataListProvider provider = (ProjectDataListProvider)dataTreeViewers[index].getContentProvider();
        provider.setFilterText(newText);
        dataTreeViewers[index].setSelection(new StructuredSelection());
        dataTreeViewers[index].refresh(true);
    }
    
    /*
     * 监控项目中的所有数据文件，以防误存覆盖。
     */
    protected void watchDataFiles() {
        FileWatcher.unwatch(this);
        for (Class cls : ProjectData.supportDataClasses) {
            File f = EditorApplication.getProj().getDataFile(cls);
            if (f.exists()) {
                FileWatcher.watch(f, this);
            }
        }
    }
    
    /**
     * 当编辑器保存数据时，暂时取消监听，以避免没有必要的警告。
     */
    public void saveStart(Class cls) {
        File f = EditorApplication.getProj().getDataFile(cls);
        if (f.exists()) {
            FileWatcher.unwatch(f, this);
        }
    }
    
    public void saveEnd(Class cls) {
        File f = EditorApplication.getProj().getDataFile(cls);
        if (f.exists()) {
            FileWatcher.watch(f, this);
        }
    }
    
    /**
     * 文件监控通知。
     */
    public void fileModified(File f) {
        for (Class cls : ProjectData.supportDataClasses) {
            File f1 = EditorApplication.getProj().getDataFile(cls);
            if (f1.equals(f)) {
                getSite().getShell().getDisplay().asyncExec(new DataChangedHandler(cls));
                break;
            }
        }
    }
    
    class DataChangedHandler implements Runnable {
        private Class changedClass;
        
        public DataChangedHandler(Class cls) {
            changedClass = cls;
        }
        
        public void run() {
            String dataName = DataTypeManager.getTypeName(changedClass);
            if (dataName == null) {
                dataName = changedClass.getName();
            }
            String msg = dataName + "数据被外部程序改变，是否重载？\n" +
                "注：如果选择是，所有已打开的编辑窗口将被关闭，数据不会保存；如果选择否，" +
                "那么在你继续编辑并保存数据时，将有可能覆盖别人的修改！";
            if (MessageDialog.openConfirm(getSite().getShell(), "警告", msg) == false) {
                return;
            }
            try {
                // 重载
                ProjectData proj = EditorApplication.getProj();
                proj.load(proj.baseDir);
                
                // 刷新列表
                for (TreeViewer tv : dataTreeViewers) {
                    tv.refresh();
                }
                
                // 关闭所有编辑器
                IEditorReference[] refs = getSite().getWorkbenchWindow().getActivePage().getEditorReferences();
                for (IEditorReference ref : refs) {
                    IEditorPart editor = ref.getEditor(false);
                    if (editor != null) {
                        getSite().getWorkbenchWindow().getActivePage().closeEditor(editor, false);
                    }
                }
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
    }
    
    /*
     * 设置拖动支持。
     */
    private void setupDragAndDrop(TreeViewer viewer) {
        Tree tree = viewer.getTree();
        final DragSource treeDragSource = new DragSource(tree, DND.DROP_MOVE);
        treeDragSource.addDragListener(new DragSourceAdapter() {
            /**
             * 判断是否允许拖动。任意对象都可以拖动。分类不允许拖动。
             */
            public void dragStart(DragSourceEvent event) {
                Object[] sels = getSelectedObjects();
                if (sels.length == 0) {
                    event.doit = false;
                } else {
                    event.doit = false;
                    for (int i = 0; i < sels.length; i++) {
                        if (sels[i] instanceof DataObject) {
                            event.doit = true;
                            break;
                        }
                    }
                }
            }
            
            /**
             * 设置拖动数据，一行一个对象，格式为：类名:id。
             */
            public void dragSetData(DragSourceEvent event) {
                Object[] sels = getSelectedObjects();
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < sels.length; i++) {
                    if (i > 0) {
                        buf.append("\n");
                    }
                    if (sels[i] instanceof DataObject) {
                        buf.append(sels[i].getClass().getName() + ":" + ((DataObject)sels[i]).id);
                    }
                }
                event.data = buf.toString();
            }
            
            public void dragFinished(DragSourceEvent event) {
            }
        });
        treeDragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });

        final DropTarget treeDropTarget = new DropTarget(tree, DND.DROP_MOVE);
        treeDropTarget.addDropListener(new DropTargetAdapter() {
            public void dragEnter(DropTargetEvent event) {
            }
            public void dragLeave(DropTargetEvent event) {
            }
            public void dragOperationChanged(DropTargetEvent event) {
            }
            /**
             * 检查当前目标是否允许拖放。允许拖动到一个新分类中（加到最后），或者拖动到一个指定对象（插入到前面）。
             */
            public void dragOver(DropTargetEvent event) {
                event.feedback = DND.FEEDBACK_NONE | DND.FEEDBACK_SCROLL;
                event.detail = DND.DROP_NONE;
                if (event.item != null) {
                    TextTransfer textTransfer = TextTransfer.getInstance();
                    String data = (String)textTransfer.nativeToJava(event.currentDataType);
                    if (data == null) {
                        return;
                    }
                    TreeItem titem = (TreeItem)event.item;
                    Object targetObj = titem.getData();
                    if (targetObj instanceof DataObjectCategory || targetObj instanceof DataObject) {
                        event.detail = DND.DROP_MOVE;
                    }
                }
            }
            /**
             * 拖动结束。
             */
            public void drop(DropTargetEvent event) {
                if (event.data == null || event.item == null) {
                    return;
                }
                TreeItem titem = (TreeItem)event.item;
                Object targetObj = titem.getData();
                String data = (String)event.data;
                String[] items = data.split("\n");
                try {
                    boolean changed = false;
                    Class changedClass = null;
                    ProjectData proj = EditorApplication.getProj();
                    if (targetObj instanceof DataObjectCategory) {
                        // 拖动到一个分类的最后
                        DataObjectCategory targetCate = (DataObjectCategory)targetObj;
                        for (String line : items) {
                            String[] sec = line.split(":");
                            Class cls = Class.forName(sec[0]);
                            DataObject dobj = proj.findObject(cls, Integer.parseInt(sec[1]));
                            if (dobj != null && !targetCate.name.equals(dobj.categoryName)) {
                                proj.changeObjectCategory(dobj, targetCate);
                                changed = true;
                                changedClass = cls;
                            }
                        }
                    } else if (targetObj instanceof DataObject) {
                        // 拖动到一个对象的前面
                        DataObject tobj = (DataObject)targetObj;
                        changedClass = tobj.getClass();
                        DataObjectCategory targetCate = proj.findCategory(changedClass, tobj.categoryName);
                        int index = targetCate.objects.indexOf(tobj);
                        for (String line : items) {
                            String[] sec = line.split(":");
                            Class cls = Class.forName(sec[0]);
                            DataObject dobj = proj.findObject(cls, Integer.parseInt(sec[1]));
                            if (dobj == null || dobj == tobj) {
                                continue;
                            }
                            if (!targetCate.name.equals(dobj.categoryName)) {
                                proj.changeObjectCategory(dobj, targetCate);
                            }
                            int oldIndex = targetCate.objects.indexOf(dobj);
                            if (oldIndex < index) {
                                targetCate.objects.remove(dobj);
                                index--;
                                targetCate.objects.add(index, dobj);
                            } else {
                                targetCate.objects.remove(dobj);
                                targetCate.objects.add(index, dobj);
                            }
                            index++;
                            changed = true;
                        }
                    }
                    if (changed) {
                        proj.saveDataList(changedClass);
                        refresh();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            public void dropAccept(DropTargetEvent event) {
            }
        });
        treeDropTarget.setTransfer(new Transfer[] {TextTransfer.getInstance()});
    }

    public static void tryEditObject(DataObject obj) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView) page.findView(DataListView.ID);
        if (view != null) {
            view.editObject(obj);
        }
    }
}
