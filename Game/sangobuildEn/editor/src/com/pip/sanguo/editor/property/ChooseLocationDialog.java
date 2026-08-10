package com.pip.sanguo.editor.property;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Condition;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mapeditor.MapViewer;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.tool.IMapEditTool;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.area.GameAreaEditor;
import com.pip.sanguo.editor.area.GameMapViewer;
import com.pip.sanguo.editor.quest.GameAreaCache;
import com.pipimage.image.PipAnimate;
import com.swtdesigner.SWTResourceManager;

public class ChooseLocationDialog extends Dialog {
    private HashMap<GameArea, MapFile> mapCache = new HashMap<GameArea, MapFile>();
    
    private Text text;
    private String searchCondition;

    class TreeLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof ProjectData) {
                return "项目";
            }
            if (element instanceof GameArea) {
                return element.toString();
            }
            if (element instanceof GameMapInfo) {
                return ((GameMapInfo)element).id + ": " + ((GameMapInfo)element).name;
            }
            return super.getText(element);
        }
        public Image getImage(Object element) {
            if (element instanceof GameArea) {
                return WorkshopPlugin.getDefault().getImageRegistry().get("animate");
            } else if (element instanceof GameMapInfo) {
                return WorkshopPlugin.getDefault().getImageRegistry().get("map");
            }
            return null;
        }
    }
    class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ProjectData) {
                // 根节点是ProjectData，第一层子节点是所有的游戏关卡
                List<DataObject> list = ((ProjectData)parentElement).getDataListByType(GameArea.class);
                List<DataObject> retList = new ArrayList<DataObject>();
                for (int i = 0; i < list.size(); i++) {
                    if (getChildren(list.get(i)).length > 0) {
                        retList.add(list.get(i));
                    }
                }
                return retList.toArray();
            } else if (parentElement instanceof GameArea) {
                // 关卡的下一层节点是游戏场景
                GameAreaInfo areaInfo = GameAreaCache.getAreaInfo(((GameArea)parentElement).id);
                if (areaInfo == null) {
                    return new Object[0];
                } else {
                    List<GameMapInfo> retList = new ArrayList<GameMapInfo>();
                    for (GameMapInfo mi : areaInfo.maps) {
                        if (matchCondition(mi)) {
                            retList.add(mi);
                        }
                    }
                    return retList.toArray();
                }
            }
            return new Object[0];
        }
        public Object getParent(Object element) {
            if (element instanceof ProjectData) {
                return null;
            } else if (element instanceof GameArea) {
                return ((GameArea)element).owner;
            } else if (element instanceof GameMapInfo) {
                return ((GameMapInfo)element).owner;
            }
            return null;
        }
        public boolean hasChildren(Object element) {
            return (element instanceof ProjectData || element instanceof GameArea || element instanceof GameMapInfo);
        }
    }
    private TreeViewer treeViewer;
    private Tree tree;
    private int[] location = new int[3];
    private GameMapViewer mapViewer;
    private GameMapInfo mapInfo;
    private MapFile mapFile;
    private GameMap gameMap;
    
    public int[] getLocation() {
        return location;
    }

    public void setLocation(int[] sel) {
        this.location = sel;
    }
    
    private boolean matchCondition(GameMapInfo map) {
        if (searchCondition == null || searchCondition.length() == 0) {
            return true;
        }
        if (map.name.indexOf(searchCondition) >= 0 || String.valueOf(map.getGlobalID()).indexOf(searchCondition) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseLocationDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("查找：");

        text = new Text(container, SWT.BORDER);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                searchCondition = text.getText();
                StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
                Object selObj = sel.isEmpty() ? null : sel.getFirstElement();
                treeViewer.refresh();
                treeViewer.expandAll();
                if (selObj != null) {
                    sel = new StructuredSelection(selObj);
                    treeViewer.setSelection(sel);
                }
            }
        });
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        treeViewer = new TreeViewer(container, SWT.BORDER);
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                StructuredSelection sel = (StructuredSelection)event.getSelection();
                if (sel.isEmpty()) {
                    return;
                }
                Object selObj = sel.getFirstElement();
                if (treeViewer.getExpandedState(selObj)) {
                    treeViewer.collapseToLevel(selObj, 1);
                } else {
                    treeViewer.expandToLevel(selObj, 1);
                }
            }
        });
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                updateMapViewer();
            }
        });
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.setContentProvider(new TreeContentProvider());
        tree = treeViewer.getTree();
        final GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd_tree.heightHint = 400;
        tree.setLayoutData(gd_tree);
        treeViewer.setInput(EditorApplication.getInstance().getProjectData());
        treeViewer.expandAll();
        
        mapViewer = new GameMapViewer(container, SWT.NONE);
        GridData gd_mapViewer = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd_mapViewer.heightHint = 500;
        mapViewer.setLayoutData(gd_mapViewer);
        
        try {
            // 查找这个地图在tree中的位置
            GameMapInfo map = GameMapInfo.findByID(EditorApplication.getProj(), location[0]);
            if (map != null) {
                searchCondition = map.name;
                text.setText(searchCondition);
                text.selectAll();
                StructuredSelection sel = new StructuredSelection(map);
                treeViewer.setSelection(sel);
            } else {
            	//linux下，如果不执行这句，会看不到mapViewer
            	treeViewer.collapseAll();
            }
        } catch (Exception e) {
        }

        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(920, 800);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择场景");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
            if (sel.isEmpty() || !(sel.getFirstElement() instanceof GameMapInfo)) {
                location = new int[] { -1, 0, 0 };
            }
        }
        super.buttonPressed(buttonId);
    }
    
    private void updateMapViewer() {
        StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
        if (sel.isEmpty() || !(sel.getFirstElement() instanceof GameMapInfo)) {
            mapInfo = null;
            mapFile = null;
            gameMap = null;
            mapViewer.setInput(null, null);
            mapViewer.setTool(null);
            mapViewer.redraw();
            return;
        }
        try {
            mapInfo = (GameMapInfo)sel.getFirstElement();
            mapFile = mapCache.get(mapInfo.owner);
            if (mapFile == null) {
                mapFile = new MapFile();
                mapFile.load(new File(mapInfo.owner.source, "game.map"));
            }
            gameMap = mapFile.getMaps().get(mapInfo.id);
            mapViewer.setInput(mapFile.getMaps().get(mapInfo.id), mapInfo);
            mapViewer.setTool(new PickupLocationTool());
            mapViewer.redraw();
        } catch (Exception e) {
            MessageDialog.openError(getShell(), "错误", e.toString());
        }
    }

    /**
     * 设置位置工具。
     * @author lighthu
     */
    class PickupLocationTool implements IMapEditTool {
        // 是否正在拖动
        private boolean isDragging;
        // 最近一次检测到的鼠标位置
        private int lastX, lastY;
    
        /**
         * 缺省构造方法
         */
        public PickupLocationTool() {
        }
        
        /**
         * 鼠标按下事件，当前鼠标点中位置被选择。
         * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
         * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
         * @param mask 按键状态掩码
         */
        public void mouseDown(int x, int y, int mask) {
            isDragging = true;
            location[0] = mapInfo.getGlobalID();
            location[1] = x;
            location[2] = y;
            mapViewer.redraw();
        }
        
        /**
         * 鼠标抬起事件。
         * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
         * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
         * @param mask 按键状态掩码
         */
        public void mouseUp(int x, int y, int mask) {
            isDragging = false;
            location[0] = mapInfo.getGlobalID();
            location[1] = x;
            location[2] = y;
            mapViewer.redraw();
        }
        
        /**
         * 鼠标移动事件。被拖动的NPC跟随移动。
         * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
         * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
         */
        public void mouseMove(int x, int y) {
            lastX = x;
            lastY = y;
            if (isDragging) {
                location[1] = x;
                location[2] = y;
            }
            mapViewer.redraw();
        }
        
        /**
         * 绘制当前工具
         * @param gc
         */
        public void draw(GC gc) {
            // 绘制当前位置
            if (location[0] == mapInfo.getGlobalID()) {
                Image img = EditorPlugin.getDefault().getImageRegistry().get("flag");
                Point pt = new Point(location[1], location[2]);
                mapViewer.map2screen(pt);
                gc.drawImage(img, pt.x - 10, pt.y - 33);
                
                String str = mapInfo.name + "(" + location[1] + "," + location[2] + ")";
                Point size = mapViewer.getSize();
                Point ts = gc.textExtent(str);
                gc.setForeground(MapViewer.invert(mapViewer.getBackground()));
                gc.setBackground(mapViewer.getBackground());
                gc.drawRectangle(1, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
                gc.drawText(str, 4, size.y - ts.y - 5);
            }
    
            // 绘制座标
            String coordStr = lastX + "," + lastY + "," + (lastX / gameMap.parent.getCellSize()) + "," + (lastY / gameMap.parent.getCellSize());
            Point size = mapViewer.getSize();
            Point ts = gc.textExtent(coordStr);
            gc.setForeground(MapViewer.invert(mapViewer.getBackground()));
            gc.setBackground(mapViewer.getBackground());
            gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
            gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
        }
        
        /**
         * 键按下事件。
         */
        public void onKeyDown(int keyCode) {}
        
        /**
         * 键松开事件。
         */
        public void onKeyUp(int keyCode) {}

        /**
         * 得到工具右键菜单。
         */
        public Menu getMenu() {
            return null;
        }

        public void mouseDoubleClick(int x, int y) {
            // TODO Auto-generated method stub
            
        }
    }
}
