package com.pip.sanguo.editor.shop;

import java.util.HashSet;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jdom.Element;

import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.TeleportSet;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.property.ItemCellEditor;
import com.pip.sanguo.editor.property.LocationCellEditor;
import com.pip.util.AutoSelectAll;

public class TeleportSetEditor extends DefaultDataObjectEditor {
    /*
     * 传送列表编辑控制。
     */
    class ItemListCellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            if (element instanceof TeleportSet.Teleport) {
                TeleportSet.Teleport item = (TeleportSet.Teleport)element;
                int index = Integer.parseInt(property.substring(1));
                if (item.mapID == 0) {
                    return index == 0;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        
        public Object getValue(Object element, String property) {
            if (element instanceof TeleportSet.Teleport) {
                TeleportSet.Teleport item = (TeleportSet.Teleport)element;
                int index = Integer.parseInt(property.substring(1));
                switch (index) {
                case 0:    // 目标位置
                    return new int[] { item.mapID, item.x, item.y };
                case 1:    // 传送名称
                    return item.name;
                case 2:    // 金钱价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_MONEY));
                case 3:    // i币价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_IMONEY));
                case 4:    // 荣誉价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_HONOR));
                case 5:    // 附加条件
                {
                    return item;
                }
                case 6:    // 经验价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_EXP));
                default:
                    return "";
                }
            }
            return "";
        }

        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            if (ti.getData() instanceof TeleportSet.Teleport) {
                TeleportSet.Teleport item = (TeleportSet.Teleport)ti.getData();
                int index = Integer.parseInt(property.substring(1));
                switch (index) {
                case 0:    // 目标位置
                {
                    int[] newValue = (int[])value;
                    if (item.mapID != 0) {
                        // 修改
                        if (item.mapID == newValue[0] && item.x == newValue[1] && item.y == newValue[2]) {
                            return;
                        }
                        item.mapID = newValue[0];
                        item.x = newValue[1];
                        item.y = newValue[2];
                        itemListViewer.update(item, null);
                    } else {
                        // 添加
                        if (newValue[0] == 0) {
                            return;
                        }
                        item = new TeleportSet.Teleport();
                        item.mapID = newValue[0];
                        item.x = newValue[1];
                        item.y = newValue[2];
                        item.name = GameMapInfo.toString(EditorApplication.getProj(), item.mapID);
                        ((TeleportSet)editObject).items.add(item);
                        itemListViewer.refresh();
                    }
                    setDirty(true);
                    break;
                }
                case 1:    // 名字
                {
                    if (!item.name.equals(value)) {
                        item.name = (String)value;
                        itemListViewer.update(item, null);
                        setDirty(true);
                    }
                    break;
                }
                case 2:    // 金钱价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_MONEY, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 3:    // i币价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_IMONEY, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 4:    // 荣誉价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_HONOR, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 5:    // 附加条件
                {
                    if (value != null) {
                        itemListViewer.update(item, null);
                        setDirty(true);
                    }
                    break;
                }
                case 6:    // 经验价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_EXP, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break; 
                }
                }
            }
        }
    }
    /*
     * 传送门列表文本提供者。
     */
    class ItemListLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            TeleportSet.Teleport item = (TeleportSet.Teleport) element;
            if (item.mapID == 0) {
                // 这一行是用于新增的，特殊处理
                if (columnIndex == 0) {
                    return "点这里添加...";
                } else {
                    return "";
                }
            }
            switch (columnIndex) {
            case 0:    // 目标位置
                return GameMapInfo.locationToString(EditorApplication.getProj(), new int[] { item.mapID, item.x, item.y }, false);
            case 1:    // 名称
                return item.name;
            case 2:    // 金钱价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_MONEY));
            case 3:    // i币价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_IMONEY));
            case 4:    // 荣誉价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_HONOR));
            case 5:    // 附加条件
            {
                return Shop.BuyRequirement.toString(item.requirements);
            }
            case 6:    // 经验价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_EXP));
            default:
                return "";
            }
        }

        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return EditorPlugin.getDefault().getImageRegistry().get("empty");
            }
            return null;
        }
    }

    /*
     * 商店物品列表内容提供者。
     */
    class ItemListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            TeleportSet ts = (TeleportSet)inputElement;
            Object[] ret = new Object[ts.items.size() + 1];
            ts.items.toArray(ret);
            TeleportSet.Teleport newItem = new TeleportSet.Teleport();
            ret[ret.length - 1] = newItem;
            return ret;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private TableViewer itemListViewer;
    private Table itemList;
    private TableColumn locationColumn, nameColumn, moneyPriceColumn, imoneyPriceColumn, honorPriceColumn, conditionColumn, expPriceColumn;
    private Text textDesc;
    private Text textTitle;
    private Text textID;
    public static final String ID = "com.pip.sanguo.editor.shop.TeleportSetEditor"; //$NON-NLS-1$

    /**
     * Create contents of the editor part
     * 
     * @param parent
     */                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("标题：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("描述：");

        textDesc = new Text(container, SWT.BORDER);
        final GridData gd_textDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textDesc.setLayoutData(gd_textDesc);
        textDesc.addFocusListener(AutoSelectAll.instance);
        textDesc.addModifyListener(this);

        itemListViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        itemListViewer.setLabelProvider(new ItemListLabelProvider());
        itemListViewer.setContentProvider(new ItemListContentProvider());
        itemList = itemListViewer.getTable();
        itemList.setLinesVisible(true);
        itemList.setHeaderVisible(true);
        final GridData gd_itemList = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        itemList.setLayoutData(gd_itemList);
        itemList.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if ((event.stateMask & SWT.SHIFT) != 0) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        onMoveUpItem();
                        event.doit = false;
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        onMoveDownItem();
                        event.doit = false;
                    }
                }
                if (event.keyCode == SWT.DEL) {
                    onDelItem();
                }
            }
        });

        locationColumn = new TableColumn(itemList, SWT.NONE);
        locationColumn.setWidth(160);
        locationColumn.setText("目标位置");

        nameColumn = new TableColumn(itemList, SWT.NONE);
        nameColumn.setWidth(160);
        nameColumn.setText("显示标题");

        moneyPriceColumn = new TableColumn(itemList, SWT.NONE);
        moneyPriceColumn.setWidth(60);
        moneyPriceColumn.setText("金钱价格");

        imoneyPriceColumn = new TableColumn(itemList, SWT.NONE);
        imoneyPriceColumn.setWidth(60);
        imoneyPriceColumn.setText("i币价格");

        honorPriceColumn = new TableColumn(itemList, SWT.NONE);
        honorPriceColumn.setWidth(60);
        honorPriceColumn.setText("荣誉价格");

        conditionColumn = new TableColumn(itemList, SWT.NONE);
        conditionColumn.setWidth(300);
        conditionColumn.setText("附加条件");
        
        expPriceColumn = new TableColumn(itemList, SWT.NONE);
        expPriceColumn.setWidth(60);
        expPriceColumn.setText("经验价格");
        
        itemListViewer.setColumnProperties(new String[] {
                "c0", "c1", "c2", "c3", "c4", "c5", "c6"
        });
        itemListViewer.setCellModifier(new ItemListCellModifier());
        itemListViewer.setCellEditors(new CellEditor[] {
                new LocationCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new BuyRequirementsCellEditor(itemList),
                new TextCellEditor(itemList)
        });

        // 设置初始值
        TeleportSet dataDef = (TeleportSet) editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDesc.setText(dataDef.description);
        itemListViewer.setInput(dataDef);

        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        TeleportSet dataDef = (TeleportSet) editObject;
        
        // 读取输入：对象ID、标题、描述
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDesc.getText();

        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
    }

    /*
     * 保存undo状态
     */
    protected Object saveState() {
        return editObject.save();
    }

    /*
     * 恢复保存的转台
     */
    protected void loadState(Object stateObj) {
        editObject.load((Element) stateObj);
        itemListViewer.refresh();
    }

    /**
     * 取得一个物品指定类型货币的价格。
     * @param item 物品
     * @param type 货币类型
     * @return 如果一个需求项设置为扣除，且货币类型匹配，返回其数量。
     */
    private int getItemPrice(TeleportSet.Teleport item, int type) {
        for (Shop.BuyRequirement req : item.requirements) {
            if (req.type == type && req.deduct) {
                return req.amount;
            }
        }
        return 0;
    }
    
    /**
     * 设置一个物品的指定类型货币的价格。
     * @param item 物品
     * @param type 货币类型
     * @param value 新价格
     * @return 如果价格和原来的相同，返回false
     */
    private boolean setItemPrice(TeleportSet.Teleport item, int type, int value) {
        for (Shop.BuyRequirement req : item.requirements) {
            if (req.type == type && req.deduct) {
                if (req.amount == value) {
                    return false;
                } else {
                    req.amount = value;
                    return true;
                }
            }
        }
        if (value == 0) {
            return false;
        } else {
            Shop.BuyRequirement req = new Shop.BuyRequirement();
            req.type = type;
            req.amount = value;
            req.deduct = true;
            item.requirements.add(req);
            return true;
        }
    }
    
    /*
     * 把当前选中的物品上移
     */
    private void onMoveUpItem() {
        int sel = itemList.getSelectionIndex();
        if (sel > 0) {
            TeleportSet shop = (TeleportSet)editObject;
            TeleportSet.Teleport item = shop.items.remove(sel);
            shop.items.add(sel - 1, item);
            itemListViewer.refresh();
            itemList.setSelection(sel - 1);
            setDirty(true);
        }
    }
    
    /*
     * 把当前选中的物品下移
     */
    private void onMoveDownItem() {
        int sel = itemList.getSelectionIndex();
        TeleportSet shop = (TeleportSet)editObject;
        if (sel != -1 && sel < shop.items.size() - 1) {
            TeleportSet.Teleport item = shop.items.remove(sel);
            shop.items.add(sel + 1, item);
            itemListViewer.refresh();
            itemList.setSelection(sel + 1);
            setDirty(true);
        }
    }
    
    /*
     * 删除选中的物品
     */
    private void onDelItem() {
        int sel = itemList.getSelectionIndex();
        TeleportSet shop = (TeleportSet)editObject;
        if (sel != -1 && sel < shop.items.size()) {
            shop.items.remove(sel);
            itemListViewer.refresh();
            itemList.setSelection(sel);
            setDirty(true);
        }
    }
}
