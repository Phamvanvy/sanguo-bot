package com.pip.sanguo.editor.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropItem;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.SubDropGroup;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.util.Constants;

/**
 * 掉落组编辑器
 * 
 * @author Joy Yan
 * 
 */
public class DropGroupEditor extends DefaultDataObjectEditor implements ISelectionChangedListener, SelectionListener {
    private Text textDropRate;
    private Text textMaxMonsterLevel;
    private Text textMinMonsterLevel;
    class ItemTreeLabelProvider extends LabelProvider {
        public String getText(Object element) {
            return super.getText(element);
        }

        public Image getImage(Object element) {
            if (element instanceof DataObjectCategory) {
                return EditorPlugin.getDefault().getImageRegistry().get("itemtype");
            } else if (element instanceof Item) {
                return EditorPlugin.getDefault().getImageRegistry().get("item");
            } else if (element instanceof DropGroup) {
                return EditorPlugin.getDefault().getImageRegistry().get("dropgroup");
            }
            return null;
        }
    }

    class ItemTreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public Object[] getChildren(Object element) {
            ProjectData proj = EditorApplication.getProj();
            if (element instanceof Integer) {
                // 根节点，类型
                switch (((Integer)element).intValue()) {
                case DropItem.DROP_TYPE_ITEM:
                    return proj.getCategoryListByType(Item.class).toArray();
                case DropItem.DROP_TYPE_EQUI:
                    return proj.getCategoryListByType(Equipment.class).toArray();
                case DropItem.DROP_TYPE_DROPGROUP:
                    return proj.getCategoryListByType(DropGroup.class).toArray();
                }
            } else if (element instanceof DataObjectCategory) {
                // 分类
                return ((DataObjectCategory)element).objects.toArray();
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            if (element instanceof DataObjectCategory) {
                Class cls = ((DataObjectCategory)element).dataClass;
                if (cls == Item.class) {
                    return new Integer(DropItem.DROP_TYPE_ITEM);
                } else if (cls == Equipment.class) {
                    return new Integer(DropItem.DROP_TYPE_EQUI);
                } else if (cls == DropGroup.class) {
                    return new Integer(DropItem.DROP_TYPE_DROPGROUP);
                }
            } else if (element instanceof Equipment) {
                return ((Equipment)element).owner.findCategory(Equipment.class, ((Equipment)element).categoryName);
            } else if (element instanceof Item) {
                return ((Item)element).owner.findCategory(Equipment.class, ((Item)element).categoryName);
            } else if (element instanceof DropGroup) {
                return ((DropGroup)element).owner.findCategory(DropGroup.class, ((DropGroup)element).categoryName);
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }
    }

    public static final String ID = "com.pip.sanguo.editor.item.DropGroupEditor"; //$NON-NLS-1$

    private static final String[] COMBO_DROP_TYPE = { "世界掉落", "普通掉落" };
    private static final String[] COMBO_ITEM_TYPE = { "1.物品", "2.装备", "3.掉落组", "4.金钱", "5.经验值" };

    // 基本属性

    // 掉落组ID
    private Text textID;
    // 掉落组名称
    private Text textTitle;
    // 掉落组掉落数量下限
    private Text textQuantityMin;
    // 掉落组掉落数量上限
    private Text textQuantityMax;
    // 掉落组类型 0:世界掉落 1.普通掉落
    private Combo comboDropType;

    // 等级列表编辑

    // 等级范围列表
    private ListViewer levelRangeList;
    // 掉落等级范围上限
    private Text textLevelMax;
    // 掉落等级范围下限
    private Text textLevelMin;
    // 掉落职业限制
    private Combo comboJob;
    // 添加一个等级范围组
    private Button buttonAddGroup;
    // 删除一个等级范围组
    private Button buttonDelGroup;
    // 更新一个等级范围组
    private Button buttonUpdateGroup;

    // 掉落物品列表

    // 显示掉落组权重的标签
    private Label subGroupWeight;
    // 掉落物品列表
    private DropGroupItemListEditor dropItemEditor;

    // 搜索添加物品界面
    
    // 掉落类型选择： 1.物品 2.装备 3.掉落组 4.金钱 5.经验
    private Combo comboType;
    // 添加数量
    // 候选物品列表
    private TreeViewer itemTreeViewer;
    private Tree itemTree;
    // 添加物品按钮
    private Button buttonAddDropItem;
    private Composite worldDropComposite;
    private Button cbValid;


    /**
     * editor初始化
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
    }

    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 1;
        gridLayout.numColumns = 9;
        container.setLayout(gridLayout);

        // 基本属性

        final Label label = new Label(container, SWT.NONE);
        label.setText("掉落组ID：");

        textID = new Text(container, SWT.BORDER);
        textID.setEditable(false);
        textID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("掉落组名称：");

        textTitle = new Text(container, SWT.BORDER);
        textTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("掉落数量：");

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        final GridLayout gridLayout_3 = new GridLayout();
        gridLayout_3.numColumns = 3;
        gridLayout_3.verticalSpacing = 0;
        gridLayout_3.marginWidth = 0;
        gridLayout_3.marginHeight = 0;
        gridLayout_3.horizontalSpacing = 0;
        composite.setLayout(gridLayout_3);

        textQuantityMin = new Text(composite, SWT.BORDER);
        textQuantityMin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textQuantityMin.addModifyListener(this);

        final Label label_3 = new Label(composite, SWT.NONE);
        label_3.setText("-");

        textQuantityMax = new Text(composite, SWT.BORDER);
        textQuantityMax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textQuantityMax.addModifyListener(this);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("掉落组类型：");

        comboDropType = new Combo(container, SWT.READ_ONLY);
        comboDropType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
//                int type = comboDropType.getSelectionIndex();
//                GridData gd = (GridData)worldDropComposite.getLayoutData();
//                if (type == DropGroup.GROUP_TYPE_NORMAL) {
//                    gd.exclude = true;
//                } else {
//                    gd.exclude = false;
//                }
//                worldDropComposite.getParent().layout();
            }
        });
        comboDropType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboDropType.setItems(COMBO_DROP_TYPE);
        comboDropType.addModifyListener(this);

        cbValid = new Button(container, SWT.CHECK);
        cbValid.setText("有效");
        cbValid.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });

        worldDropComposite = new Composite(container, SWT.NONE);
        final GridData gd_worldDropComposite = new GridData(SWT.FILL, SWT.FILL, false, false, 9, 1);
        worldDropComposite.setLayoutData(gd_worldDropComposite);
        final GridLayout gridLayout_6 = new GridLayout();
        gridLayout_6.numColumns = 7;
        worldDropComposite.setLayout(gridLayout_6);

        final Label label_7 = new Label(worldDropComposite, SWT.NONE);
        label_7.setText("适用怪物级别下限(含)：");

        textMinMonsterLevel = new Text(worldDropComposite, SWT.BORDER);
        final GridData gd_textMinMonsterLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMinMonsterLevel.setLayoutData(gd_textMinMonsterLevel);
        textMinMonsterLevel.addModifyListener(this);

        final Label label_8 = new Label(worldDropComposite, SWT.NONE);
        label_8.setText("适用怪物级别上限(含)：");

        textMaxMonsterLevel = new Text(worldDropComposite, SWT.BORDER);
        final GridData gd_textMaxMonsterLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMaxMonsterLevel.setLayoutData(gd_textMaxMonsterLevel);
        textMaxMonsterLevel.addModifyListener(this);

        final Label label_9 = new Label(worldDropComposite, SWT.NONE);
        label_9.setText("掉落率：");

        textDropRate = new Text(worldDropComposite, SWT.BORDER);
        final GridData gd_textDropRate = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textDropRate.setLayoutData(gd_textDropRate);
        textDropRate.addModifyListener(this);

        final Label label_10 = new Label(worldDropComposite, SWT.NONE);
        label_10.setText("%%");

        final SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 9, 1));

        // 级别列表编辑

        final Composite compositeLevel = new Composite(sashForm, SWT.NONE);
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 4;
        compositeLevel.setLayout(gridLayout_1);

        final Label label_5 = new Label(compositeLevel, SWT.NONE);
        label_5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        label_5.setText("等级列表：");

        levelRangeList = new ListViewer(compositeLevel, SWT.BORDER);
        levelRangeList.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
        levelRangeList.setContentProvider(new ListContentProvider());
        levelRangeList.addSelectionChangedListener(this);

        final Label label_6 = new Label(compositeLevel, SWT.NONE);
        label_6.setText("级别：");

        textLevelMin = new Text(compositeLevel, SWT.BORDER);
        textLevelMin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label label_13 = new Label(compositeLevel, SWT.NONE);
        label_13.setText("-");

        textLevelMax = new Text(compositeLevel, SWT.BORDER);
        textLevelMax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label label_14 = new Label(compositeLevel, SWT.NONE);
        label_14.setLayoutData(new GridData());
        label_14.setText("职业：");

        comboJob = new Combo(compositeLevel, SWT.READ_ONLY);
        comboJob.setItems(Constants.COMBO_JOB_EXT);
        final GridData gd_comboJob = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        comboJob.setLayoutData(gd_comboJob);
        comboJob.select(0);

        final Composite composite_1 = new Composite(compositeLevel, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
        final GridLayout gridLayout_5 = new GridLayout();
        gridLayout_5.marginWidth = 0;
        gridLayout_5.numColumns = 3;
        composite_1.setLayout(gridLayout_5);

        buttonAddGroup = new Button(composite_1, SWT.NONE);
        buttonAddGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        buttonAddGroup.setText("添加");
        buttonAddGroup.addSelectionListener(this);

        buttonUpdateGroup = new Button(composite_1, SWT.NONE);
        buttonUpdateGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        buttonUpdateGroup.setText("更新");
        buttonUpdateGroup.addSelectionListener(this);

        buttonDelGroup = new Button(composite_1, SWT.NONE);
        buttonDelGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        buttonDelGroup.setText("删除");
        buttonDelGroup.addSelectionListener(this);

        // 掉落列表编辑

        final Composite compositeDrop = new Composite(sashForm, SWT.NONE);
        final GridLayout gridLayout_2 = new GridLayout();
        gridLayout_2.numColumns = 2;
        compositeDrop.setLayout(gridLayout_2);

        subGroupWeight = new Label(compositeDrop, SWT.NONE);
        subGroupWeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        subGroupWeight.setText("掉落列表：");

        final Button buttonAverage = new Button(compositeDrop, SWT.NONE);
        buttonAverage.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                dropItemEditor.average();
            }
        });
        final GridData gd_buttonAverage = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        buttonAverage.setLayoutData(gd_buttonAverage);
        buttonAverage.setText("均分掉落率");

        final ScrolledComposite scrolledComposite = new ScrolledComposite(compositeDrop, SWT.V_SCROLL);
        scrolledComposite.getVerticalBar().setPageIncrement(500);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        dropItemEditor = new DropGroupItemListEditor(scrolledComposite, SWT.NONE); 
        dropItemEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        dropItemEditor.addModifyListener(this);
        
        scrolledComposite.setContent(dropItemEditor);

        // 添加物品界面

        final Composite compositeItemList = new Composite(sashForm, SWT.NONE);
        final GridLayout gridLayout_4 = new GridLayout();
        gridLayout_4.numColumns = 2;
        compositeItemList.setLayout(gridLayout_4);

        final Label label_11 = new Label(compositeItemList, SWT.NONE);
        label_11.setText("掉落类型：");

        comboType = new Combo(compositeItemList, SWT.READ_ONLY);
        comboType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboType.setItems(COMBO_ITEM_TYPE);
        comboType.select(DropItem.DROP_TYPE_ITEM);
        comboType.addSelectionListener(this);

        itemTreeViewer = new ItemTreeViewer(compositeItemList, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
        itemTreeViewer.setLabelProvider(new ItemTreeLabelProvider());
        itemTreeViewer.setContentProvider(new ItemTreeContentProvider());
        itemTree = itemTreeViewer.getTree();
        final GridData gd_itemTree = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        itemTree.setLayoutData(gd_itemTree);
        itemTreeViewer.setInput(new Integer(DropItem.DROP_TYPE_ITEM));

        buttonAddDropItem = new Button(compositeItemList, SWT.NONE);
        final GridData gd_buttonAddDropItem = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        buttonAddDropItem.setLayoutData(gd_buttonAddDropItem);
        buttonAddDropItem.setText("添加");
        buttonAddDropItem.addSelectionListener(this);

        sashForm.setWeights(new int[] { 154, 482, 285 });

        DropGroup dropGroup = (DropGroup) editObject;
        setCurrentData(dropGroup);
        if (dropGroup.subGroup.size() > 0) {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(100);
                        getSite().getShell().getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                levelRangeList.setSelection(new StructuredSelection(((DropGroup)editObject).subGroup.get(0)));
                                dropItemEditor.redraw();
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }.start();
        }

        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }

    /**
     * 将一个掉落组的数据显示到界面元素
     * 
     * @param group
     *            掉落组数据对象
     */
    private void setCurrentData(DropGroup group) {
        textID.setText(String.valueOf(group.id));
        textTitle.setText(group.title);
        textQuantityMin.setText(String.valueOf(group.quantityMin));
        textQuantityMax.setText(String.valueOf(group.quantityMax));
        comboDropType.select(group.groupType);
        textMinMonsterLevel.setText(String.valueOf(group.minMonsterLevel));
        textMaxMonsterLevel.setText(String.valueOf(group.maxMonsterLevel));
        textDropRate.setText(String.valueOf(group.dropRate));
        cbValid.setSelection(group.valid);

        levelRangeList.setInput(group.subGroup);
    }
    
    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        DropGroup dataDef = (DropGroup)editObject;
        dataDef.title = textTitle.getText();
        try {
            dataDef.quantityMin = Integer.parseInt(textQuantityMin.getText());
            dataDef.quantityMax = Integer.parseInt(textQuantityMax.getText());
        } catch (Exception e) {
            throw new Exception("数量输入格式错误。");
        }
        dataDef.groupType = comboDropType.getSelectionIndex();
        if (dataDef.groupType == DropGroup.GROUP_TYPE_WORLD) {
            try {
                dataDef.minMonsterLevel = Integer.parseInt(textMinMonsterLevel.getText());
                dataDef.maxMonsterLevel = Integer.parseInt(textMaxMonsterLevel.getText());
                dataDef.dropRate = Integer.parseInt(textDropRate.getText());
            } catch (Exception e) {
                throw new Exception("世界掉落组数据输入错误。");
            }
        }
        dataDef.valid = cbValid.getSelection();
    }

    /**
     * 根据用户指定级别，显示当前指定等级范围的掉落组
     * 
     * @param subDrop
     */
    private void setSubDropGroup(SubDropGroup subDrop) {
        if (subDrop == null) {
            dropItemEditor.setInput(new SubDropGroup());
            return;
        }

        textLevelMax.setText(String.valueOf(subDrop.levelMax));
        textLevelMin.setText(String.valueOf(subDrop.levelMin));
        comboJob.select(subDrop.job + 1);

        dropItemEditor.setInput(subDrop);
    }

    /**
     * 显示错误信息提示框
     * 
     * @param message
     */
    private void showMessage(String message) {
        MessageDialog.openError(getSite().getShell(), "提示！", message);
    }

    /**
     * 列表选择事件
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (event.getSource() == levelRangeList) {
            /* 等级范围选择列表 */
            IStructuredSelection seleted = (IStructuredSelection) event.getSelection();
            SubDropGroup subDropGroup = (SubDropGroup) seleted.getFirstElement();
            setSubDropGroup(subDropGroup);
        }
    }

    private void hideControl(Control obj) {
        obj.setVisible(false);
        ((GridData) obj.getLayoutData()).exclude = true;
        obj.getParent().layout();
    }

    private void showControl(Control obj) {
        obj.setVisible(true);
        ((GridData) obj.getLayoutData()).exclude = false;
        obj.getParent().layout();
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    /**
     * 按钮和类型选择框消息处理
     */
    public void widgetSelected(SelectionEvent e) {
        try {
            if (e.getSource() == buttonAddGroup) {
                /* 添加等级范围 */
                onAddGroup();
            } else if (e.getSource() == buttonDelGroup) {
                /* 删除等级范围 */
                onDeleteGroup();
            } else if (e.getSource() == buttonUpdateGroup) {
                /* 更新等级范围 */
                onUpdateGroup();
            } else if (e.getSource() == comboType) {
                /* 掉落类型修改，更新列表 */
                int type = comboType.getSelectionIndex();
                if (type == DropItem.DROP_TYPE_ITEM || type == DropItem.DROP_TYPE_EQUI
                        || type == DropItem.DROP_TYPE_DROPGROUP) {
                    showControl(itemTree);
                    itemTreeViewer.setInput(new Integer(comboType.getSelectionIndex()));
                } else {
                    hideControl(itemTree);
                }
            } else if (e.getSource() == buttonAddDropItem) {
                /* 添加掉落项目 */
                onAddItem();
            }
        } catch (Exception e1) {
            showMessage("错误\n" + e1.toString());
        }
    }

    /**
     * 执行添加等级范围消息处理
     */
    private void onAddGroup() {
        int levelMin = Integer.parseInt(textLevelMin.getText());
        int levelMax = Integer.parseInt(textLevelMax.getText());
        int job = comboJob.getSelectionIndex() - 1;

        DropGroup group = (DropGroup) editObject;

        if (levelMin > levelMax) {
            showMessage("输入的等级上限小于下限！");
            return;
        }

        if (levelMin < DropGroup.LEVEL_MIN || levelMax > DropGroup.LEVEL_MAX) {
            showMessage("等级超出限制范围！");
            return;
        }

        if (!group.isRangeValid(levelMin, levelMax, job, -1)) {
            showMessage("等级范围和现有重合！");
            return;
        }

        SubDropGroup sub = new SubDropGroup();
        sub.levelMax = levelMax;
        sub.levelMin = levelMin;
        sub.job = job;
        group.subGroup.add(sub);
        
        levelRangeList.refresh();
        levelRangeList.setSelection(new StructuredSelection(sub));

        setDirty(true);
    }

    /**
     * 删除等级范围消息处理
     */
    private void onDeleteGroup() {
        IStructuredSelection selected = (IStructuredSelection) levelRangeList.getSelection();
        SubDropGroup subGroup = (SubDropGroup) selected.getFirstElement();
        if (subGroup != null) {
            ((DropGroup) editObject).subGroup.remove(subGroup);
            levelRangeList.refresh();
            levelRangeList.setSelection(new StructuredSelection());

            setDirty(true);
        }
    }

    /**
     * 更新等级范围消息处理
     */
    private void onUpdateGroup() {
        IStructuredSelection selected = (IStructuredSelection) levelRangeList.getSelection();
        int index = levelRangeList.getList().getSelectionIndex();
        SubDropGroup subGroup = (SubDropGroup) selected.getFirstElement();
        if (subGroup == null) {
            return;
        }
        
        int levelMin = Integer.parseInt(textLevelMin.getText());
        int levelMax = Integer.parseInt(textLevelMax.getText());
        int job = comboJob.getSelectionIndex() - 1;

        if (levelMin > levelMax) {
            showMessage("输入的等级上限小于下限！");
            return;
        }

        /* 判断等级有没有超过系统限制 */
        if (levelMin < DropGroup.LEVEL_MIN || levelMax > DropGroup.LEVEL_MAX) {
            showMessage("等级超出限制范围！");
            return;
        }

        /* 判断等级范围是否有重合 */
        DropGroup group = (DropGroup) editObject;
        if (!group.isRangeValid(levelMin, levelMax, job, index)) {
            showMessage("等级范围和现有重合！");
            return;
        }

        subGroup.levelMax = levelMax;
        subGroup.levelMin = levelMin;
        subGroup.job = job;
        
        levelRangeList.refresh();

        setDirty(true);
    }
    
    /*
     * 向当前选中的子掉落组中添加一个掉落物品。
     * @param group 掉落组
     * @param owner 子掉落组
     * @param type 掉落项目类型
     * @param obj 掉落对象，物品，装备或掉落组
     */
    private void tryAddDropItem(DropGroup group, SubDropGroup owner, int type, DataObject obj) throws Exception {
        if (obj instanceof DropGroup) {
            if (!group.isGroupValid((DropGroup)obj)) {
                throw new Exception("有递归调用的掉落组！");
            }
        }

        DropItem drop = owner.getNewDropItem();
        drop.dropID = obj.id;
        drop.dropType = type;
        drop.quantityMax = 1;
        drop.quantityMin = 1;
        drop.dropWeight = 0;
        drop.dropObj = obj;
        owner.dropGroup.add(drop);
    }

    /**
     * 添加一个掉落物品
     */
    private void onAddItem() {
        int dropType = comboType.getSelectionIndex();

        // 查找选中的子掉落组
        IStructuredSelection selSubGroup = (IStructuredSelection) levelRangeList.getSelection();
        SubDropGroup selection = (SubDropGroup) selSubGroup.getFirstElement();
        if (selection == null) {
            showMessage("请选择一个有效的等级范围！");
            return;
        }
        
        // 查找选中的物品/装备/掉落组对象
        if (dropType == DropItem.DROP_TYPE_ITEM || dropType == DropItem.DROP_TYPE_EQUI || dropType == DropItem.DROP_TYPE_DROPGROUP) {
            StructuredSelection sel = (StructuredSelection)itemTreeViewer.getSelection();
            Iterator itor = sel.iterator();
            while (itor.hasNext()) {
                Object obj = itor.next();
                if (obj instanceof Item || obj instanceof DropGroup) {
                    try {
                        tryAddDropItem((DropGroup)editObject, selection, dropType, (DataObject)obj);
                    } catch (Exception e) {
                        showMessage(e.getMessage());
                    }
                }
            }
        } else {
            DropItem drop = selection.getNewDropItem();
            drop.dropID = -1;
            drop.dropType = dropType;
            drop.quantityMax = 1;
            drop.quantityMin = 1;
            drop.dropWeight = 0;
            drop.dropObj = null;
            selection.dropGroup.add(drop);
        }

        dropItemEditor.setInput(selection);

        setDirty(true);
    }

    /**
     * 等级范围列表数据内容提供类
     */
    class ListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                List inputData = (List) inputElement;
                return inputData.toArray();
            }
            return null;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    /**
     * 掉落物品列表数据内容提供类
     * 
     */
    class ItemListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                ProjectData projData = EditorApplication.getInstance().getProjectData();
                List<DropItem> inputData = (List<DropItem>)inputElement;
                
                /* 根据掉落物品类型，组织列表数据 */
                for (DropItem drop : inputData) {
                    switch (drop.dropType) {
                    case DropItem.DROP_TYPE_ITEM: /* 掉落物品 */
                        if (drop.dropObj == null) {
                            drop.dropObj = projData.findItem(drop.dropID);
                        }
                        break;
                    case DropItem.DROP_TYPE_EQUI: /* 掉落装备 */
                        if (drop.dropObj == null) {
                            drop.dropObj = projData.findEquipment(drop.dropID);
                        }
                        break;
                    case DropItem.DROP_TYPE_DROPGROUP: /* 掉落掉落组 */
                        if (drop.dropObj == null) {
                            drop.dropObj = projData.findObject(DropGroup.class, drop.dropID);
                        }
                        break;
                    }
                }
                return inputData.toArray();
            }
            return new Object[0];
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

}
