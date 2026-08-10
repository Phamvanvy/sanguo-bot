package com.pip.sanguo.editor.clientevent;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.clientEvent.EventItem;
import com.pip.sanguo.editor.quest.RichTextDialog;

/**
 * @author bqzhang
 *
 */
public class EventGroupDialog  extends Dialog {
    private Combo comboPromptType;      //提示类型
    
    private Label notesParam;           //参数注释
    
    private Text textPromptParam;       //参数
    private Combo comboActionType;      //玩家动作
    
    private Text textSignKey;           //操作指引文字--按键版
    private Text textSignTouch;          //操作指引文字--触摸版
    
    private Text textEventNoticeKey;    //按键版
    private Text textEventNoticeTouch;  //触摸版
    
    private EventItem event;
    
    /**
     * 选中项目id
     * @return
     */
    public EventItem getSelectedObject() {
        
        return event;
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public EventGroupDialog(Shell parentShell, EventItem event) {
        super(parentShell);
        this.event = event; 
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("提示类型：");
        comboPromptType = new Combo(container, SWT.READ_ONLY);
        comboPromptType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboPromptType.setItems(EventItem.EVENT_PROMPT);
        comboPromptType.setVisibleItemCount(15);
        comboPromptType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if(event.promptType != comboPromptType.getSelectionIndex()){
                    event.promptType = comboPromptType.getSelectionIndex();
                    setShowParam();
                }
            }
        });
        
        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("参数内容说明：");
        notesParam = new Label(container, SWT.NONE);
        final GridData gd_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        notesParam.setLayoutData(gd_1);
        
        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("参数：");
        textPromptParam = new Text(container, SWT.BORDER);
        final GridData gd_eventSubValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textPromptParam.setLayoutData(gd_eventSubValue);
        
        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("后续动作：");
        comboActionType = new Combo(container, SWT.READ_ONLY);
        comboActionType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboActionType.setItems(EventItem.EVENT_ACTIONS);
        comboActionType.setVisibleItemCount(15);
        comboActionType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if(event.actionType != comboActionType.getSelectionIndex()){
                    event.actionType = comboActionType.getSelectionIndex();
                    setShowParam();
                }
            }
        });
        
        final Label label_sign = new Label(container, SWT.NONE);
        label_sign.setText("按键版指引提示：");
        textSignKey = new Text(container, SWT.BORDER);
        final GridData gd_eventSignValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textSignKey.setLayoutData(gd_eventSignValue);
        
        final Label label_signTouch = new Label(container, SWT.NONE);
        label_signTouch.setText("触摸版指引提示：");
        textSignTouch = new Text(container, SWT.BORDER);
        final GridData gd_eventSignTouch = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textSignTouch.setLayoutData(gd_eventSignTouch);
        
        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("按键版描述：");
        textEventNoticeKey = new Text(container, SWT.BORDER);
        final GridData gd_dropRate1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textEventNoticeKey.setLayoutData(gd_dropRate1);
        gd_dropRate1.widthHint = 200;
        gd_dropRate1.heightHint = 45;
        final Button editHintButton = new Button(container, SWT.NONE);
        editHintButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                RichTextDialog dlg = new RichTextDialog(getShell(), null);
                dlg.setText(textEventNoticeKey.getText());
                if (dlg.open() == Dialog.OK) {
                    textEventNoticeKey.setText(dlg.getText());
                }
            }
        });
        editHintButton.setText("...");
        
        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("触摸版描述：");
        textEventNoticeTouch = new Text(container, SWT.BORDER);
        final GridData gd_dropRate12 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textEventNoticeTouch.setLayoutData(gd_dropRate12);
        gd_dropRate12.widthHint = 200;
        gd_dropRate12.heightHint = 45;
        final Button editHintButton2 = new Button(container, SWT.NONE);
        editHintButton2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                RichTextDialog dlg = new RichTextDialog(getShell(), null);
                dlg.setText(textEventNoticeTouch.getText());
                if (dlg.open() == Dialog.OK) {
                    textEventNoticeTouch.setText(dlg.getText());
                }
            }
        });
        editHintButton2.setText("...");
        
        initValues();
        
        return container;
    }
    
    
    /**
     * 初始化界面 各项 数值
     */
    public void initValues(){
        //comboEventType.select(event.eventType);
        comboPromptType.select(event.promptType);
        textPromptParam.setText(event.promptParam);
        comboActionType.select(event.actionType);
        textEventNoticeKey.setText(event.eventDesKey);
        textEventNoticeTouch.setText(event.eventDesTouch);
        textSignKey.setText(event.signDescKey);
        textSignTouch.setText(event.signDescTouch);
        setShowParam();
    }
    
    public void setShowParam(){
        //设置 条件是否需要参数
        textPromptParam.setVisible(true);
        notesParam.setText("对应操作的参数(可以为空)");
        boolean isParam = false;
        //参数作用注释
        switch(event.promptType){
            case EventItem.PROMPT_TYPE_NOTICE:          //引导说明
            case EventItem.PROMPT_TYPE_NOTICE_SIMPLE:   //简单文字提示
            case EventItem.PROMPT_TYPE_BACK:            //返回
            case EventItem.PROMPT_TYPE_QUESTS:          //任务引导
            case EventItem.PROMPT_TYPE_SELECT_QUEST:    //任务列表中的任务
            case EventItem.PROMPT_TYPE_MENU:            //菜单
            case EventItem.PROMPT_TYPE_ITEM_REDGRASS:   //使用血瓶引导
            case EventItem.PROMPT_TYPE_ITEM_BLUEGRASS:  //使用蓝瓶引导
                notesParam.setText("空");
                isParam = true;
                break;
            case EventItem.PROMPT_TYPE_SELECT_MENU:     //菜单中的菜单项
                notesParam.setText("*菜单项ID");
                break;
            case EventItem.PROMPT_TYPE_SELECT_ITEM:     //包格中的物品
                notesParam.setText("*物品ID");
                break;
            case EventItem.PROMPT_TYPE_SELECT_EQUIP:    //装备栏中的装备位
                notesParam.setText("例0|7 [0表示类型(0-人物1-坐骑2-随从);7表示装备位的索引]");
                break;
            case EventItem.PROMPT_TYPE_SELECT_HORSE:    //坐骑列表中的坐骑
                notesParam.setText("*暂为空，坐骑选择列表中的第一项");
                break;
            case EventItem.PROMPT_TYPE_SELECT_ATTENDANT: //随从列表中的随从
                notesParam.setText("*暂为空，游戏中选择列表中的第一项");
                break;
            case EventItem.PROMPT_TYPE_DELAY_TIME:       //引导事件延迟
                notesParam.setText("*延迟时间，单位--秒");
                break;
            case EventItem.PROMPT_TYPE_SELECT_SKILL:     //技能栏中的技能
                notesParam.setText("技能栏的索引--从1开始");
                break;
            case EventItem.PROMPT_SKILL_ADD_POINT:       //技能加点界面技能格
                notesParam.setText("技能加点界面被动技能的索引,从1开始");
                break;
            case EventItem.PROMPT_SKILL_QUICK_GRID:     //技能快捷栏
                notesParam.setText("技能快捷配置界面，要配置的第一列中的位置索引，从1开始");
                break;
            case EventItem.PROMPT_CONFIRM_CHANGE:       //确认选项选择
                notesParam.setText("二次确认选项,现在默认是确定");
                break;
            case EventItem.PROMPT_ATT_ADD_POINT:        //属性加点
                notesParam.setText("参数为“-1”时为全部加点(按键版除外)，其他为加一点");
                break;
            case EventItem.PROMPT_SET_QUICK_SKILL:      //技能快捷配置引导(新UI)
                notesParam.setText("参数例：类型0-物品1技能|物品或技能ID|快捷栏索引从0开始");
                break;
            case EventItem.PROMPT_MAINFRAME_TOP_MENU:   //菜单界面顶部菜单项(新UI)
                notesParam.setText("*菜单索引(从0开始),例:3--坐骑管理");
                break;
            case EventItem.PROMPT_MAINFRAME_LEFT_MENU:   //菜单界面左侧菜单项(新UI)
                notesParam.setText("*菜单索引(从0开始),例:2--擂台");
                break;
            default:
                notesParam.setText("空");
                isParam = true;
                break;
        }
        if(isParam){
            switch(event.actionType){
                case EventItem.ACTION_TYPE_MOVE:
                    notesParam.setText("参数例：地图ID|X坐标,Y坐标|npcId");
                    break;
                case EventItem.ACTION_TYPE_CHANGE_TARGET:
                case EventItem.ACTION_TYPE_TARGET_FINISH:
                    notesParam.setText("参数例：NPCId|地图ID|X坐标,Y坐标|任务ID (已在客户端统一实现)");
                    break;
                case EventItem.ACTION_TYPE_UP_EQUIP:
                    notesParam.setText("装备的物品ID");
                    break;
                case EventItem.ACTION_TYPE_SET_QUICK:
                    notesParam.setText("技能ID(已在客户端统一实现)");
                    break;
                case EventItem.ACTION_TYPE_OPEN_MENU:
                    notesParam.setText("item(背包); menu(菜单)*注:只有新UI用到");
                    break;
            }
        }
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(644, 320);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("事件子项设定");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            //event.eventType = comboEventType.getSelectionIndex();
            event.promptType = comboPromptType.getSelectionIndex();
            event.promptParam = textPromptParam.getText();
            event.actionType = comboActionType.getSelectionIndex();
            event.eventDesKey = textEventNoticeKey.getText();
            event.eventDesTouch = textEventNoticeTouch.getText();
            event.signDescKey = textSignKey.getText();
            event.signDescTouch = textSignTouch.getText();
        }
        super.buttonPressed(buttonId);
    }
}
