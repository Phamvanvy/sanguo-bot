package com.pip.sanguo.editor.clientevent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.clientEvent.ClientEvent;
import com.pip.sanguo.data.clientEvent.EventItem;
import com.pip.sanguo.data.clientEvent.EventTrigger;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.clientevent.trigger.IExprEvent;
import com.pip.sanguo.editor.quest.RichTextPreviewer;
import com.pip.util.AutoSelectAll;
import com.pip.util.IFileModificationListener;

public class ClientEventEditor extends DefaultDataObjectEditor  implements SelectionListener, Runnable,
        IFileModificationListener {
    
    public String title;
    public Point stdPoint = new Point(10, 10);
    public int currFocusIndex = 0;
    public static Color imgBoxColor;
    public java.util.List<Rectangle> imageRects = new ArrayList<Rectangle>();
    
    ClientEvent ce;
    
    public static final String ID = "com.pip.sanguo.editor.clientevent.ClientEventEditor";
    
    /**
     * 上移
     */
    public static final int MOVE_UP     = 0;
    /**
     * 下移
     */
    public static final int MOVE_DOWN   = 1;
    
    /**
     * 基本属性页
     */
    private PropertySheetViewer propEditor;
    private Display display;
    
    public static List<Rank> ranks;
    
    private Text textID;
    private Text textTitle;
    private Text textTime;
    
    private Text textSuitLvlMin;    //事件级别下限
    private Text textSuitLvlMax;    //事件级别上限
    private Combo comboType;        //事件类型(世界触发和条件触发)
    private Combo comboUiType;      //机型UI版本(蓝色新UI一(待废弃), java旧UI, 黄色新UI二, 全部通用, 废弃)
    private Combo comboFaction;      //阵营
    
    private RichTextPreviewer preTrigger;   //引导条件预览
    private TableViewer triggerViewer;      //第一种条件
    private Table triggerTable;
    private TableViewer triggerViewer2;     //第二种条件
    private Table triggerTable2;
    
    private RichTextPreviewer previewer;    //引导事件预览
    private TableViewer eventViewer;        //事件列表
    private Table eventTable;
    
    /**
     * 上移
     */
    private Action moveUpAction;
    
    /**
     * 下移
     */
    private Action moveDownAction;
    
    /**
     * Create contents of the editor part
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        ClientEvent obj = (ClientEvent) editObject;
        ce = obj;
        display = getSite().getShell().getDisplay();

        // 底板
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        composite.setLayout(gridLayout);
        
        creatActions();
        
        final Label idLabel = new Label(composite, SWT.NONE);
        idLabel.setText("ID：");
        textID = new Text(composite, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false,  2, 1);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);
        
        final Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData());
        label.setText("标题：");
        textTitle = new Text(composite, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);
        
        final Label labelTime = new Label(composite, SWT.NONE);
        labelTime.setLayoutData(new GridData());
        labelTime.setText("间隔时间：");
        textTime = new Text(composite, SWT.BORDER);
        final GridData gd_textTime = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textTime.setLayoutData(gd_textTime);
        textTime.addFocusListener(AutoSelectAll.instance);
        textTime.addModifyListener(this);
        
        final Label labelType = new Label(composite, SWT.NONE);
        labelType.setLayoutData(new GridData());
        labelType.setText("事件类型：");
        comboType = new Combo(composite, SWT.READ_ONLY);
        final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboType.setLayoutData(gd_comboType);
        comboType.setItems(new String[] {"条件触发", "世界触发"});
        comboType.addModifyListener(this);
        comboType.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setDirty(true);
            }
        });
        
        final Composite compSuit = new Composite(composite, SWT.NONE);
        compSuit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        final GridLayout gridSuit = new GridLayout();
        gridSuit.numColumns = 6;
        compSuit.setLayout(gridSuit);
        
        final Label labelSuit = new Label(compSuit, SWT.NONE);
        labelSuit.setLayoutData(new GridData());
        labelSuit.setText("适合级别：");
        textSuitLvlMin = new Text(compSuit, SWT.BORDER);
        final GridData gd_textSuit1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textSuitLvlMin.setLayoutData(gd_textSuit1);
        textSuitLvlMin.addFocusListener(AutoSelectAll.instance);
        textSuitLvlMin.addModifyListener(this);
        
        final Label labelSuit1 = new Label(compSuit, SWT.NONE);
        labelSuit1.setLayoutData(new GridData());
        labelSuit1.setText("-");
        textSuitLvlMax = new Text(compSuit, SWT.BORDER);
        final GridData gd_textSuit2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textSuitLvlMax.setLayoutData(gd_textSuit2);
        textSuitLvlMax.addFocusListener(AutoSelectAll.instance);
        textSuitLvlMax.addModifyListener(this);
        
        final Label labelUIType = new Label(composite, SWT.NONE);
        labelUIType.setLayoutData(new GridData());
        labelUIType.setText("机型版本：");
        comboUiType = new Combo(composite, SWT.READ_ONLY);
        final GridData gd_UIType = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboUiType.setLayoutData(gd_UIType);
        comboUiType.setItems(new String[]{"第一版新UI-蓝色(待废弃)", "java旧UI", "第二版新UI-黄色", "通用(不包括第一版新UI)", "废弃"});
        comboUiType.addModifyListener(this);
        comboUiType.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setDirty(true);
            }
        });
        
        final Label labelFaction = new Label(composite, SWT.NONE);
        labelFaction.setLayoutData(new GridData());
        labelFaction.setText("阵营：");
        comboFaction = new Combo(composite, SWT.READ_ONLY);
        final GridData gd_faction = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboFaction.setLayoutData(gd_faction);
        comboFaction.setItems(new String[]{"所有", "魏国", "蜀国", "吴国"});
        comboFaction.addModifyListener(this);
        comboFaction.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setDirty(true);
            }
        });
        //new Label(composite, SWT.NONE);
        //new Label(composite, SWT.NONE);
        //new Label(composite, SWT.NONE);
        
        final Composite comp = new Composite(composite, SWT.NONE);
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
        final GridLayout grid = new GridLayout();
        grid.numColumns = 6;
        comp.setLayout(grid);
        final Label label_5 = new Label(comp, SWT.NONE);
        label_5.setText("条件预览：");
        new Label(comp, SWT.NONE);

        final Label label_6 = new Label(comp, SWT.NONE);
        label_6.setText("第一种条件：");
        new Label(comp, SWT.NONE);
        
        final Label label_7 = new Label(comp, SWT.NONE);
        label_7.setText("第二种条件：");
        new Label(comp, SWT.NONE);
        
        final Composite composite_1 = new Composite(comp, SWT.NONE);
        
        final GridData gd_textPre = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd_textPre.widthHint = 200;
        composite_1.setLayoutData(gd_textPre);
        composite_1.setLayout(new FillLayout());
        preTrigger = new RichTextPreviewer(composite_1, SWT.NONE);
        
        triggerViewer = new TableViewer(comp, SWT.FULL_SELECTION | SWT.BORDER);
        triggerViewer.setLabelProvider(new TriggerTableLabelProvider());
        triggerViewer.setContentProvider(new TriggerTableContentProvider());
        triggerTable = triggerViewer.getTable();
        triggerTable.setHeaderVisible(true);
        triggerTable.setLinesVisible(true);
        triggerViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection sel = (IStructuredSelection)triggerViewer.getSelection();
                if (!sel.isEmpty()) {
                    onDoubleClick(triggerViewer, sel.getFirstElement());
                }
            }
        });
        triggerTable.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                event.doit = !handleKey(triggerViewer, event.keyCode, event.stateMask);
            }
        });
        
        final GridData gd_targetTable = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        triggerTable.setLayoutData(gd_targetTable);
        
        final TableColumn conditionColumn = new TableColumn(triggerTable, SWT.NONE);
        conditionColumn.setWidth(150);
        conditionColumn.setText("条件");
        
        triggerViewer2 = new TableViewer(comp, SWT.FULL_SELECTION | SWT.BORDER);
        triggerViewer2.setLabelProvider(new TriggerTableLabelProvider());
        triggerViewer2.setContentProvider(new TriggerTableContentProvider2());
        triggerTable2 = triggerViewer2.getTable();
        triggerTable2.setHeaderVisible(true);
        triggerTable2.setLinesVisible(true);
        triggerViewer2.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection sel = (IStructuredSelection)triggerViewer2.getSelection();
                if (!sel.isEmpty()) {
                    onDoubleClick(triggerViewer2, sel.getFirstElement());
                }
            }
        });
        triggerTable2.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                event.doit = !handleKey(triggerViewer2, event.keyCode, event.stateMask);
            }
        });
        final GridData gd_targetTable2 = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        triggerTable2.setLayoutData(gd_targetTable2);
        
        final TableColumn conditionColumn2 = new TableColumn(triggerTable2, SWT.NONE);
        conditionColumn2.setWidth(150);
        conditionColumn2.setText("条件");
        
        final Label label_8 = new Label(comp, SWT.NONE);
        label_8.setText("事件预览：");
        new Label(comp, SWT.NONE);
        
        final Label label_10 = new Label(comp, SWT.NONE);
        label_10.setText("事件：");
        new Label(comp, SWT.NONE);
        new Label(comp, SWT.NONE);
        new Label(comp, SWT.NONE);
        
        final Composite composite_2 = new Composite(comp, SWT.NONE);
        composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        composite_2.setLayout(new FillLayout());
        previewer = new RichTextPreviewer(composite_2, SWT.NONE);
        
        //事件编辑
        eventViewer = new TableViewer(comp, SWT.FULL_SELECTION | SWT.BORDER);
        eventViewer.setLabelProvider(new EventTableLabelProvider());
        eventViewer.setContentProvider(new EventTableContentProvider());
        eventTable = eventViewer.getTable();
        eventTable.setHeaderVisible(true);
        eventTable.setLinesVisible(true);
        eventViewer.addDoubleClickListener(new IDoubleClickListener(){
            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection sel = (IStructuredSelection)eventViewer.getSelection();
                if (!sel.isEmpty()) {
                    onDoubleClick(eventViewer, sel.getFirstElement());
                }
            }
        });
        eventTable.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                event.doit = !handleKey(eventViewer, event.keyCode, event.stateMask);
            }
        });
        eventTable.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                actionChanged();
            }
        });
        eventTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

        final TableColumn countSubColumn = new TableColumn(eventTable, SWT.NONE);
        countSubColumn.setWidth(150);
        countSubColumn.setText("引导提示类型");
        final TableColumn effectSubColumn = new TableColumn(eventTable, SWT.NONE);
        effectSubColumn.setWidth(100);
        effectSubColumn.setText("提示参数");
        final TableColumn effectColumn = new TableColumn(eventTable, SWT.NONE);
        effectColumn.setWidth(100);
        effectColumn.setText("玩家操作");
        final TableColumn noticeColumn = new TableColumn(eventTable, SWT.NONE);
        noticeColumn.setWidth(200);
        noticeColumn.setText("按键版描述");
        final TableColumn noticeColumn2 = new TableColumn(eventTable, SWT.NONE);
        noticeColumn2.setWidth(200);
        noticeColumn2.setText("触摸版描述");
        //事件编辑   end
        
        ClientEvent dataDef = (ClientEvent)editObject;
        triggerViewer.setInput(this.getEditObject());
        triggerViewer2.setInput(this.getEditObject());
        eventViewer.setInput(this.getEditObject());
        textTitle.setText(dataDef.title);
        textID.setText(String.valueOf(dataDef.id));
        textTime.setText(String.valueOf(dataDef.restartTime));
        textSuitLvlMin.setText(String.valueOf(dataDef.suitLvlMin));
        textSuitLvlMax.setText(String.valueOf(dataDef.suitLvlMax));
        comboType.select(dataDef.type);
        comboUiType.select(dataDef.uiType);
        comboFaction.select(dataDef.faction);
        updatePreview();
        updatePreTrigger();
        setDirty(false);
        setPartName(obj.title);
        saveStateToUndoBuffer();
    }
    
    private void creatActions(){
        moveUpAction = new Action("上移") {
            public void run() {
                eventMove(MOVE_UP);
            }
        };
        moveDownAction = new Action("下移") {
            public void run() {
                eventMove(MOVE_DOWN);
            }
        };
    }
    
    private void actionChanged() {
        int sel = eventTable.getSelectionIndex();
        if (sel == -1) {
            eventViewer.setInput(null);
        } else {
            eventViewer.setInput(this.getEditObject());
        }
        MenuManager mgr = new MenuManager();
        if (sel != -1) {
            mgr.add(moveUpAction);
            mgr.add(moveDownAction);
        }
        Menu menu = mgr.createContextMenu(eventTable);
        eventTable.setMenu(menu);
    }
    
    /**
     * 引导事件预览
     */
    private void updatePreview() {
        ClientEvent dataDef = (ClientEvent)editObject;
        StringBuilder sb = new StringBuilder();
        
        // 事件预览
        for (EventItem event : dataDef.eventItems) {
            sb.append("<cff0000>---></c><c00bb00>" + EventItem.EVENT_PROMPT[event.promptType]);
            sb.append(":  </c><c000000>");
            switch(event.promptType){
                case EventItem.PROMPT_TYPE_NOTICE:
                case EventItem.PROMPT_TYPE_NOTICE_SIMPLE:
                    break;
                case EventItem.PROMPT_TYPE_SELECT_MENU:
                    sb.append(event.promptParam);
                    break;
                case EventItem.PROMPT_TYPE_SELECT_ITEM:
                    Item item = dataDef.owner.findItemOrEquipment(Integer.parseInt(event.promptParam));
                    if(item == null){
                        sb.append("没有找到物品");
                    }else{
                        sb.append(item.title);
                    }
                    break;
                case EventItem.PROMPT_TYPE_SELECT_EQUIP:
//                    Item item2 = dataDef.owner.findEquipment(Integer.parseInt(event.promptParam));
//                    if(item2 == null){
//                        sb.append("没有找到装备");
//                    }else{
//                        sb.append(item2.title);
//                    }
                    break;
                case EventItem.PROMPT_TYPE_BACK:
                case EventItem.PROMPT_TYPE_SELECT_HORSE:
                case EventItem.PROMPT_TYPE_SELECT_ATTENDANT:
                case EventItem.PROMPT_TYPE_QUESTS:
                    break;
                case EventItem.PROMPT_TYPE_DELAY_TIME:
                    sb.append("延迟时间--"+event.promptParam);
                    break;
                case EventItem.PROMPT_TYPE_SELECT_QUEST:
                case EventItem.PROMPT_TYPE_MENU:
                    break;
                case EventItem.PROMPT_TYPE_SELECT_SKILL:
                    sb.append("第"+event.promptParam+"个技能栏(快捷键 "+(Integer.parseInt(event.promptParam)+1)+")");
                    break;
            }
            sb.append("</c>\n");
        }
        previewer.setText(sb.toString());
    }
    
    /**
     * 引导条件预览
     */
    private void updatePreTrigger() {
        ClientEvent dataDef = (ClientEvent)editObject;
        StringBuilder sb = new StringBuilder();
        
        // 事件条件预览
        sb.append("<cff0000>第一种事件条件</c>\n");
        sb.append("<c00bb00>");
        for (EventTrigger event : dataDef.triggers) {
            IExprEvent exprEvent = event.exprEvent;
            sb.append(exprEvent.getName()+"\n");
        }
        sb.append("</c>");
        
        sb.append("\n<cff0000>第二种事件条件</c>\n");
        sb.append("<c00bb00>");
        for (EventTrigger event : dataDef.triggers2) {
            IExprEvent exprEvent = event.exprEvent;
            sb.append(exprEvent.getName()+"\n");
        }
        sb.append("</c>");
        preTrigger.setText(sb.toString());
    }
    
    /**
     * 处理Table和Tree控件的双击事件。
     * @param viewer 事件来源
     * @param sel 当前选中对象
     */
    private void onDoubleClick(Object viewer, Object sel) {
        if(viewer == eventViewer){
            if ("".equals(sel)) {
                // 新建目标
                EventItem newEvent = new EventItem(getClientEvent());
                EventGroupDialog dropDialog = new EventGroupDialog(getSite().getShell(), newEvent);
                if (dropDialog.open() == Dialog.OK) {
                    getClientEvent().eventItems.add(newEvent);
                    eventViewer.refresh();
                    setDirty(true);
                    updatePreview();
                }
            } else {
                // 编辑选中目标
                EventItem selGroup = (EventItem)sel;
                EventGroupDialog dropDialog = new EventGroupDialog(getSite().getShell(), selGroup);
                if(dropDialog.open() == IDialogConstants.OK_ID){
                    EventItem selNewGroup = dropDialog.getSelectedObject();
                    if (selNewGroup.equals(selGroup)) {
                        selGroup.update(selNewGroup);
                        eventViewer.refresh();
                        setDirty(true);
                        updatePreview();
                    }
                }
            }
        }else if (viewer == triggerViewer) {
            if ("".equals(sel)) {
//                if(getClientEvent().triggers.size() > 0){
//                    return;
//                }
                // 新建目标
                EventTrigger newEvent = new EventTrigger(getClientEvent());
                EventTriggerDialog dropDialog = new EventTriggerDialog(getSite().getShell(), newEvent, EventTriggerDialog.TYPE_CLIENT_GUID);
                if (dropDialog.open() == Dialog.OK) {
                    getClientEvent().triggers.add(newEvent);
                    triggerViewer.refresh();
                    setDirty(true);
                    updatePreTrigger();
                }
            } else {
                // 编辑选中目标
                EventTrigger selGroup = (EventTrigger)sel;
                EventTriggerDialog dropDialog = new EventTriggerDialog(getSite().getShell(), selGroup, EventTriggerDialog.TYPE_CLIENT_GUID);
                if(dropDialog.open() == IDialogConstants.OK_ID){
                    EventTrigger selNewGroup = dropDialog.getSelectedObject();
                    if (selNewGroup.equals(selGroup)) {
                        selGroup.update(selNewGroup);
                        triggerViewer.refresh();
                        setDirty(true);
                        updatePreTrigger();
                    }
                }
            }
        }else if (viewer == triggerViewer2) {
            if ("".equals(sel)) {
                // 新建目标
                EventTrigger newEvent = new EventTrigger(getClientEvent());
                EventTriggerDialog dropDialog = new EventTriggerDialog(getSite().getShell(), newEvent, EventTriggerDialog.TYPE_CLIENT_GUID);
                if (dropDialog.open() == Dialog.OK) {
                    getClientEvent().triggers2.add(newEvent);
                    triggerViewer2.refresh();
                    setDirty(true);
                    updatePreTrigger();
                }
            } else {
                // 编辑选中目标
                EventTrigger selGroup = (EventTrigger)sel;
                EventTriggerDialog dropDialog = new EventTriggerDialog(getSite().getShell(), selGroup, EventTriggerDialog.TYPE_CLIENT_GUID);
                if(dropDialog.open() == IDialogConstants.OK_ID){
                    EventTrigger selNewGroup = dropDialog.getSelectedObject();
                    if (selNewGroup.equals(selGroup)) {
                        selGroup.update(selNewGroup);
                        triggerViewer2.refresh();
                        setDirty(true);
                        updatePreTrigger();
                    }
                }
            }
        }
    }
    
    /**
     * 处理Table和Tree控件的特殊按键事件。
     * @param viewer 事件来源
     * @param keyCode 键码
     * @param mask 掩码
     * @return 如果不希望这个事件被控件处理，返回true。
     */
    private boolean handleKey(Object viewer, int keyCode, int mask) {
        if(viewer == eventViewer){
            if (keyCode == SWT.DEL) {
                // 在事件中按DEL键删除选中的事件动作
                int sel = eventTable.getSelectionIndex();
                if (sel != -1 && sel < getClientEvent().eventItems.size()) {
                    getClientEvent().eventItems.remove(sel);
                    eventViewer.refresh();
                    setDirty(true);
                    eventTable.setSelection(sel);
                    updatePreview();
                }
                return true;
            }
        }else if(viewer == triggerViewer){
            if (keyCode == SWT.DEL) {
                // 在事件中按DEL键删除选中的事件动作
                int sel = triggerTable.getSelectionIndex();
                if (sel != -1 && sel < getClientEvent().triggers.size()) {
                    getClientEvent().triggers.remove(sel);
                    triggerViewer.refresh();
                    setDirty(true);
                    triggerTable.setSelection(sel);
                    updatePreTrigger();
                }
                return true;
            }
        }else if(viewer == triggerViewer2){
            if (keyCode == SWT.DEL) {
                // 在事件中按DEL键删除选中的事件动作
                int sel = triggerTable2.getSelectionIndex();
                if (sel != -1 && sel < getClientEvent().triggers2.size()) {
                    getClientEvent().triggers2.remove(sel);
                    triggerViewer2.refresh();
                    setDirty(true);
                    triggerTable2.setSelection(sel);
                    updatePreTrigger();
                }
                return true;
            }
        }
        return false;
    }
    
    // 当前编辑的事件。
    private ClientEvent getClientEvent() {
        return (ClientEvent)getEditObject();
    }
    
    /**
     * 事件触发条件的列文本：
     */
    class TriggerTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof String) {
                if (columnIndex == 0) {
                    return "新条件...";
                } else {
                    return "";
                }
            } else {
                EventTrigger trigger = (EventTrigger)element;
                if (columnIndex == 0) {
                    if(trigger.exprEvent == null){
                        return "无";
                    }else{
                        return trigger.exprEvent.getName(); 
                    }
                }
                return "";
            }
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    class ListContentProvider implements IStructuredContentProvider{
        public Object[] getElements(Object inputElement) {
            List el = (List)inputElement;
            return el.toArray();
        }
        public void dispose() {}
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    }
    
    class ListLabelProvider implements ILabelProvider{
        public Image getImage(Object element) {
            return null;
        }

        public String getText(Object element) {
            return element.toString();
        }

        public void addListener(ILabelProviderListener listener) {}
        public void dispose() {}
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        public void removeListener(ILabelProviderListener listener) {}
    }
    
    /**
     * 引导事件项上移或下移
     * movePoint    移动方向
     */
    public void eventMove(int movePoint){
        int sel = eventTable.getSelectionIndex();
        if(sel == -1){
            return;
        }
        
        if(movePoint == MOVE_UP){
            if(sel > 0){
                EventItem event = getClientEvent().eventItems.get(sel);
                getClientEvent().eventItems.remove(sel);
                getClientEvent().eventItems.add(sel-1, event);
                sel = sel-1;
            }
        }else if(movePoint == MOVE_DOWN){
            int lastFocus = getClientEvent().eventItems.size() - 1;
            if(sel < lastFocus){
                EventItem event = getClientEvent().eventItems.get(sel);
                getClientEvent().eventItems.remove(sel);
                getClientEvent().eventItems.add(sel+1, event);
                sel = sel+1;
            }
        }
        
        eventViewer.refresh();
        setDirty(true);
        eventTable.setSelection(sel);
        updatePreview();
    }
    
    /**
     * 事件触发条件的内容：每个目标一行，最后一行用空串表示新建选项。
     */
    class TriggerTableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            ClientEvent clientEvent = (ClientEvent)inputElement;
            Object[] ret = new Object[clientEvent.triggers.size() + 1];
            clientEvent.triggers.toArray(ret);
            ret[clientEvent.triggers.size()] = "";
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    /**
     * 事件触发条件的内容：每个目标一行，最后一行用空串表示新建选项。
     */
    class TriggerTableContentProvider2 implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            ClientEvent clientEvent = (ClientEvent)inputElement;
            Object[] ret = new Object[clientEvent.triggers2.size() + 1];
            clientEvent.triggers2.toArray(ret);
            ret[clientEvent.triggers2.size()] = "";
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    class EventTableLabelProvider extends LabelProvider implements ITableLabelProvider{
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof String) {
                if (columnIndex == 0) {
                    return "新事件...";
                } else {
                    return "";
                }
            } else {
                EventItem event = (EventItem)element;
                switch(columnIndex){
                    case 0:{/* 提示类型 */
                        return EventItem.EVENT_PROMPT[((EventItem)element).promptType];
                    }
                    case 1:{/* 提示参数 */
                        return String.valueOf(event.promptParam);
                    }
                    case 2:{/* 玩家操作*/
                        return EventItem.EVENT_ACTIONS[((EventItem)element).actionType];
                    }
                    case 3:{/* 事件描述 按键版 */
                        return event.eventDesKey;
                    }
                    case 4:{/* 事件描述 触摸版*/
                        return event.eventDesTouch;
                    }
                }
            }
            return "";
        }
        
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public void addListener(ILabelProviderListener listener) {}

        public void dispose() {}

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        public void removeListener(ILabelProviderListener listener) {}
    }
    
    class EventTableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            ClientEvent clientEvent = (ClientEvent)inputElement;
            Object[] ret = new Object[clientEvent.eventItems.size() + 1];
            clientEvent.eventItems.toArray(ret);
            ret[clientEvent.eventItems.size()] = "";
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        ClientEvent dataDef = (ClientEvent)editObject;

        // 读取输入：对象ID、标题、描述、类型、级别、价格、俸禄、阵营、增益效果
        try {
            dataDef.id = Integer.parseInt(textID.getText());
            dataDef.restartTime = Integer.parseInt(textTime.getText().trim());
            dataDef.suitLvlMin = Integer.parseInt(textSuitLvlMin.getText().trim());
            dataDef.suitLvlMax = Integer.parseInt(textSuitLvlMax.getText().trim());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID或时间。");
        }
        
        dataDef.title = textTitle.getText().trim();
        dataDef.type = comboType.getSelectionIndex();
        dataDef.uiType = comboUiType.getSelectionIndex();
        dataDef.faction = comboFaction.getSelectionIndex();
        
        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
    }
    
    public void run() {
        // TODO Auto-generated method stub
    }
    public void widgetDefaultSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
    }
    public void widgetSelected(SelectionEvent e) {
        // TODO Auto-generated method stub
    }
    public void fileModified(File f) {
        // TODO Auto-generated method stub
    }
}
