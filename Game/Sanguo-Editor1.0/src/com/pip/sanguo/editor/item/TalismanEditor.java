package com.pip.sanguo.editor.item;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.pip.sanguo.data.TalismanBasicAttrAdvance;
import com.pip.sanguo.data.TalismanType;
import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestRewardItem;
import com.pip.sanguo.data.quest.QuestRewardSet;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.data.quest.pqe.ExpressionList;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.ChooseDropGroupDialog;
import com.pip.sanguo.editor.property.ChooseSkillDialog;
import com.pip.sanguo.editor.property.TalismanBasicAttrAdvanceDialog;
import com.pip.sanguo.editor.quest.QuestRewardItemDialog;
import com.pip.sanguo.editor.quest.QuestTargetDialog;
import com.pip.sanguo.editor.util.IconChooser;

public class TalismanEditor extends DefaultDataObjectEditor {

    private Text textRecastReset;
    private Text textJewelReset;
    private Text textPassiveSkillGroup;
    private Text textActiveSkillGroup;
    private Text textExpRatio;
    private Text textAppetite;
    private Text textPassiveSkill;
    private Table table_1;
    private Combo comboClass;
    private Text textSkill;
    private Text textNeedLevel;
    private Text textName;
    private Text textID;
    private IconChooser chooserIcon;
    private Button buttonSkillChoose;
    private Button buttonAddActive;
    private Button buttonPassiveSkill;
    private Button buttonAddPassive;
    private TableViewer tableViewer_1;
    public static final String ID = "com.pip.sanguo.editor.item.TalismanEditor"; //$NON-NLS-1$
    
    
    /**
     * 法宝提升属性的列文本：五列分别为:等级,力量,敏捷,体力,智力,
     * 生命,精力,暴击,命中,物闪,法闪,物攻,法攻,护甲,法防,回血,回气,最后一行显示新建选项。
     */
    class BasicAttrTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof String) {
                if (columnIndex == 0) {
                    return "新等级...";
                } else {
                    return "";
                }
            } else {
                TalismanBasicAttrAdvance attr = (TalismanBasicAttrAdvance)element;
                if (columnIndex == 0) {
                    return String.valueOf(attr.level); 
                } else if (columnIndex == 1) {
                    return String.valueOf(attr.str);
                } else if (columnIndex == 2) {
                    return String.valueOf(attr.agi);
                } else if (columnIndex == 3) {
                    return String.valueOf(attr.sta);
                } else if (columnIndex == 4) {
                    return String.valueOf(attr.wis);
                } else if (columnIndex == 5) {
                    return String.valueOf(attr.hp);
                } else if (columnIndex == 6) {
                    return String.valueOf(attr.mp);
                } else if (columnIndex == 7) {
                    return String.valueOf(attr.crit);
                } else if (columnIndex == 8) {
                    return String.valueOf(attr.hit);
                } else if (columnIndex == 9) {
                    return String.valueOf(attr.dodge);
                } else if (columnIndex == 10) {
                    return String.valueOf(attr.magicDodge);
                } else if (columnIndex == 11) {
                    return String.valueOf(attr.atk);
                } else if (columnIndex == 12) {
                    return String.valueOf(attr.magicAtk);
                } else if (columnIndex == 13) {
                    return String.valueOf(attr.armor);
                } else if (columnIndex == 14) {
                    return String.valueOf(attr.magicArmor);
                } else if (columnIndex == 15) {
                    return String.valueOf(attr.hpResume);
                } else if (columnIndex == 16) {
                    return String.valueOf(attr.mpResume);
                } else if (columnIndex == 17) {
                    return String.valueOf(attr.speed);
                } else if (columnIndex == 18) {
                    return String.valueOf(attr.antiCrit);
                } else if (columnIndex == 19) {
                    return String.valueOf(attr.duration);
                } else {
                    return String.valueOf(attr.exp);
                }
            }
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    /**
     * 法宝提升属性的内容：每个等级一行，最后一行用空串表示新建选项。
     */
    class BasicAttrTableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            TalismanType talisman = (TalismanType)inputElement;
            Object[] ret = new Object[talisman.basicAttrAdvances.size() + 1];
            talisman.basicAttrAdvances.toArray(ret);
            ret[talisman.basicAttrAdvances.size()] = "";
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        System.currentTimeMillis();
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("名称：");

        textName = new Text(container, SWT.BORDER);
        final GridData gd_textName = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textName.setLayoutData(gd_textName);
        textName.addModifyListener(this);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("需求等级：");

        textNeedLevel = new Text(container, SWT.BORDER);
        textNeedLevel.setText("0");
        final GridData gd_textNeedLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textNeedLevel.setLayoutData(gd_textNeedLevel);
        textNeedLevel.addModifyListener(this);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setLayoutData(new GridData());
        label_7.setText("饱食度：");

        textAppetite = new Text(container, SWT.BORDER);
        textAppetite.setText("1000");
        final GridData gd_textAppetite = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAppetite.setLayoutData(gd_textAppetite);
        textAppetite.addModifyListener(this);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("职业：");

        comboClass = new Combo(container, SWT.NONE);
        comboClass.select(4);
        comboClass.setItems(new String[] {"武将", "刺客", "谋士", "方士", "不限"});
        final GridData gd_comboClass = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboClass.setLayoutData(gd_comboClass);
        comboClass.addModifyListener(this);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("灵气转化系数：");

        textExpRatio = new Text(container, SWT.BORDER);
        textExpRatio.setText("100");
        final GridData gd_textExpRatio = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textExpRatio.setLayoutData(gd_textExpRatio);
        textExpRatio.addModifyListener(this);

        final Label label_11 = new Label(container, SWT.NONE);
        label_11.setText("宝石重置：");

        textJewelReset = new Text(container, SWT.BORDER);
        textJewelReset.setText("1");
        final GridData gd_textJewelReset = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textJewelReset.setLayoutData(gd_textJewelReset);
        textJewelReset.addModifyListener(this);

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setText("重铸重置：");

        textRecastReset = new Text(container, SWT.BORDER);
        textRecastReset.setText("1");
        final GridData gd_textRecastReset = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textRecastReset.setLayoutData(gd_textRecastReset);
        textRecastReset.addModifyListener(this);

        final Label label_9 = new Label(container, SWT.NONE);
        label_9.setLayoutData(new GridData());
        label_9.setText("主动技能：");

        textSkill = new Text(container, SWT.BORDER);
        final GridData gd_textSkill = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSkill.setLayoutData(gd_textSkill);
        textSkill.addModifyListener(this);

        buttonSkillChoose = new Button(container, SWT.NONE);
        buttonSkillChoose.setLayoutData(new GridData());
        buttonSkillChoose.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                editText(1);
            }
        });
        buttonSkillChoose.setText("选择技能");
        new Label(container, SWT.NONE);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setText("主动技能组：");

        textActiveSkillGroup = new Text(container, SWT.BORDER);
        final GridData gd_textActiveSkillGroup = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textActiveSkillGroup.setLayoutData(gd_textActiveSkillGroup);
        textActiveSkillGroup.addModifyListener(this);

        buttonAddActive = new Button(container, SWT.NONE);
        buttonAddActive.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                editText(3);
            }
        });
        buttonAddActive.setText("添加技能");
        new Label(container, SWT.NONE);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("被动技能：");

        textPassiveSkill = new Text(container, SWT.BORDER);
        final GridData gd_textPassiveSkill = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPassiveSkill.setLayoutData(gd_textPassiveSkill);
        textPassiveSkill.addModifyListener(this);

        buttonPassiveSkill = new Button(container, SWT.NONE);
        buttonPassiveSkill.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                editText(2);
            }
        });
        buttonPassiveSkill.setText("选择技能");
        new Label(container, SWT.NONE);

        final Label label_10 = new Label(container, SWT.NONE);
        label_10.setText("被动技能组：");

        textPassiveSkillGroup = new Text(container, SWT.BORDER);
        final GridData gd_textPassiveSkillGroup = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPassiveSkillGroup.setLayoutData(gd_textPassiveSkillGroup);
        textPassiveSkillGroup.addModifyListener(this);

        buttonAddPassive = new Button(container, SWT.NONE);
        buttonAddPassive.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                editText(4);
            }
        });
        buttonAddPassive.setText("添加技能");
        new Label(container, SWT.NONE);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setLayoutData(new GridData());
        label_6.setText("形象：");

        chooserIcon = new IconChooser(container, SWT.NONE, 2);
        chooserIcon.setHandler(this);
        final GridData gd_chooserIcon = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_chooserIcon.widthHint = 206;
        chooserIcon.setLayoutData(gd_chooserIcon);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label_16 = new Label(container, SWT.NONE);
        label_16.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        label_16.setText("属性成长：(初始以及升级后各项基本属性的提升)");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        tableViewer_1 = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewer_1.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection sel = (IStructuredSelection)tableViewer_1.getSelection();
                if (!sel.isEmpty()) {
                    onDoubleClick(tableViewer_1, sel.getFirstElement());
                }
            }
        });
        tableViewer_1.setLabelProvider(new BasicAttrTableLabelProvider());
        tableViewer_1.setContentProvider(new BasicAttrTableContentProvider());
        table_1 = tableViewer_1.getTable();
        table_1.setLinesVisible(true);
        table_1.setHeaderVisible(true);
        final GridData gd_table_1 = new GridData(SWT.LEFT, SWT.FILL, true, true, 4, 1);
        gd_table_1.widthHint = 1000;
        table_1.setLayoutData(gd_table_1);
        table_1.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                event.doit = !handleKey(tableViewer_1, event.keyCode, event.stateMask);
            }
        });

        final TableColumn levelColumn_1 = new TableColumn(table_1, SWT.NONE);
        levelColumn_1.setWidth(46);
        levelColumn_1.setText("等级");

        final TableColumn strColumn = new TableColumn(table_1, SWT.NONE);
        strColumn.setWidth(40);
        strColumn.setText("力量");

        final TableColumn agiColumn = new TableColumn(table_1, SWT.NONE);
        agiColumn.setWidth(40);
        agiColumn.setText("敏捷");

        final TableColumn staColumn = new TableColumn(table_1, SWT.NONE);
        staColumn.setWidth(40);
        staColumn.setText("体力");

        final TableColumn intColumn = new TableColumn(table_1, SWT.NONE);
        intColumn.setWidth(40);
        intColumn.setText("智力");

        final TableColumn hpColumn = new TableColumn(table_1, SWT.NONE);
        hpColumn.setWidth(40);
        hpColumn.setText("生命");

        final TableColumn mpColumn = new TableColumn(table_1, SWT.NONE);
        mpColumn.setWidth(40);
        mpColumn.setText("精力");

        final TableColumn critColumn = new TableColumn(table_1, SWT.NONE);
        critColumn.setWidth(40);
        critColumn.setText("暴击");

        final TableColumn hitColumn = new TableColumn(table_1, SWT.NONE);
        hitColumn.setWidth(40);
        hitColumn.setText("命中");

        final TableColumn dodgeColumn = new TableColumn(table_1, SWT.NONE);
        dodgeColumn.setWidth(40);
        dodgeColumn.setText("物闪");

        final TableColumn magicDodgeColumn = new TableColumn(table_1, SWT.NONE);
        magicDodgeColumn.setWidth(40);
        magicDodgeColumn.setText("法闪");

        final TableColumn atkColumn = new TableColumn(table_1, SWT.NONE);
        atkColumn.setWidth(40);
        atkColumn.setText("物攻");

        final TableColumn magicAtkColumn = new TableColumn(table_1, SWT.NONE);
        magicAtkColumn.setWidth(40);
        magicAtkColumn.setText("法攻");

        final TableColumn defColumn = new TableColumn(table_1, SWT.NONE);
        defColumn.setWidth(40);
        defColumn.setText("护甲");

        final TableColumn magicDefColumn = new TableColumn(table_1, SWT.NONE);
        magicDefColumn.setWidth(40);
        magicDefColumn.setText("法防");

        final TableColumn hpResumeColumn = new TableColumn(table_1, SWT.NONE);
        hpResumeColumn.setWidth(40);
        hpResumeColumn.setText("回血");

        final TableColumn manaResumeColumn = new TableColumn(table_1, SWT.NONE);
        manaResumeColumn.setWidth(40);
        manaResumeColumn.setText("回气");

        final TableColumn speedColumn = new TableColumn(table_1, SWT.NONE);
        speedColumn.setWidth(40);
        speedColumn.setText("速度");


        final TableColumn antiCritColumn = new TableColumn(table_1, SWT.NONE);
        antiCritColumn.setWidth(40);
        antiCritColumn.setText("免暴");

        final TableColumn durationColumn = new TableColumn(table_1, SWT.NONE);
        durationColumn.setWidth(40);
        durationColumn.setText("耐久");
        
        final TableColumn expColumn = new TableColumn(table_1, SWT.NONE);
        expColumn.setWidth(40);
        expColumn.setText("经验");
        tableViewer_1.setInput(this.getEditObject());
        
        // 设置初始值
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    /**
     * 设置初始值
     */
    public void updateView() {
        TalismanType ret = (TalismanType)editObject;
        textID.setText(String.valueOf(ret.id));
        textName.setText(ret.title);
        comboClass.select(ret.clazz);
        textAppetite.setText(String.valueOf(ret.appetite));
        textExpRatio.setText(String.valueOf(ret.expRatio));
        textNeedLevel.setText(String.valueOf(ret.playerLevel));
        String activeSkill = SkillConfig.toString(EditorApplication.getProj(), ret.skillID);
        if (!activeSkill.equals("无效技能")) {
            textSkill.setText(activeSkill);
        }
        if (ret.skillGroup != null && ret.skillGroup.length > 0) {
            String asg = "";
            for (int i = 0; i < ret.skillGroup.length; i++) {
                String info = SkillConfig.toString(EditorApplication.getProj(), ret.skillGroup[i]);
                if (i < ret.skillGroup.length - 1) {
                    asg += info + ",";
                } else {
                    asg += info;
                }
            }
            textActiveSkillGroup.setText(asg);
        }
        String passiveSkill = SkillConfig.toString(EditorApplication.getProj(), ret.passiveSkillID);
        if (!passiveSkill.equals("无效技能")) {
            textPassiveSkill.setText(passiveSkill);
        }
        if (ret.passiveSkillGroup != null && ret.passiveSkillGroup.length > 0) {
            String psg = "";
            for (int i = 0; i < ret.passiveSkillGroup.length; i++) {
                String info = SkillConfig.toString(EditorApplication.getProj(), ret.passiveSkillGroup[i]);
                if (i < ret.passiveSkillGroup.length - 1) {
                    psg += info + ",";
                } else {
                    psg += info;
                }
            }
            textPassiveSkillGroup.setText(psg);
        }
        chooserIcon.setIcon(ret.iconID, Item.ICON_IMAGE_ABILITY);
    }
    
    /**
     * 保存当前编辑器数据
     */
    protected void saveData() throws Exception {
        TalismanType ret = (TalismanType)editObject;
        try {
            ret.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID");
        }
        ret.title = ret.title;
        ret.clazz = comboClass.getSelectionIndex();
        try {
            ret.playerLevel = Integer.parseInt(textNeedLevel.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的玩家等级");
        }
        try {
        } catch (Exception e) {
            throw new Exception("请输入正确的法宝最高等级");
        }
        try {
            ret.appetite = Integer.parseInt(textAppetite.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的法宝饱食度");
        }
        try {
            ret.expRatio = Integer.parseInt(textExpRatio.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的灵气转化系数");
        }
        try {
            ret.jewelResetTimes = Integer.parseInt(textJewelReset.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的宝石熔炼重置次数");
        }
        try {
            ret.recastResetTimes = Integer.parseInt(textRecastReset.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的属性重铸重置次数");
        }
        
        if (textSkill.getText() != null && textSkill.getText().length() > 0) {
            String[] skillInfo = textSkill.getText().split(":");
            int skillId = 0;
            if (skillInfo != null && skillInfo.length > 0) {
                try {
                    skillId = Integer.parseInt(skillInfo[0]);
                } catch (Exception e) {
                    throw new Exception("请选择正确的技能");
                }
            }
            if (skillId != 0) {
                ret.skillID = skillId;
            }
        }
        
        String activeGroup = textActiveSkillGroup.getText();
        if (activeGroup != null && activeGroup.length() > 0) {
            String[] agInfo = activeGroup.split(",");
            ret.skillGroup = new int[agInfo.length];
            for (int i = 0; i < agInfo.length; i++) {
                String[] ag = agInfo[i].split(":");
                ret.skillGroup[i] = Integer.parseInt(ag[0]);
            }
        }
        
        if (textPassiveSkill.getText() != null && textPassiveSkill.getText().length() > 0) {
            String[] passiveSkillInfo = textPassiveSkill.getText().split(":");
            int passiveSkillId = 0;
            if (passiveSkillInfo != null) {
                try {
                    passiveSkillId = Integer.parseInt(passiveSkillInfo[0]);
                } catch (Exception e) {
                    throw new Exception("请选择正确的被动技能");
                }
            }
            if (passiveSkillId != 0) {
                ret.passiveSkillID = passiveSkillId;
            }
        }
        String passiveGroup = textPassiveSkillGroup.getText();
        if (passiveGroup != null && passiveGroup.length() > 0) {
            String[] pgInfo = passiveGroup.split(",");
            ret.passiveSkillGroup = new int[pgInfo.length];
            for (int i = 0; i < pgInfo.length; i++) {
                String[] pg = pgInfo[i].split(":");
                ret.passiveSkillGroup[i] = Integer.parseInt(pg[0]);
            }
        }
        
        ret.iconID = chooserIcon.getIconIndex();
    }

    protected void editText(int type) {
        ChooseSkillDialog dlg = new ChooseSkillDialog(textSkill.getShell());
        int skillID = ((TalismanType)editObject).skillID;
        dlg.setSelectedSkill(skillID);
        if (dlg.open() == Dialog.OK) {
            skillID = dlg.getSelectedSkill();
            switch (type) {
                case 1:
                    textSkill.setText(SkillConfig.toString(EditorApplication.getProj(), skillID));
                    break;
                case 2:
                    textPassiveSkill.setText(SkillConfig.toString(EditorApplication.getProj(), skillID));
                    break;
                case 3:
                    String asg = "";
                    if (textActiveSkillGroup.getText() != null && textActiveSkillGroup.getText().length() > 0) {
                        asg += textActiveSkillGroup.getText() + "," + 
                        SkillConfig.toString(EditorApplication.getProj(), skillID);
                    } else {
                        asg = SkillConfig.toString(EditorApplication.getProj(), skillID);
                    }
                    textActiveSkillGroup.setText(asg);
                    break;
                case 4:
                    String psg = "";
                    if (textPassiveSkillGroup.getText() != null && textPassiveSkillGroup.getText().length() > 0) {
                        psg += textPassiveSkillGroup.getText() + "," + 
                        SkillConfig.toString(EditorApplication.getProj(), skillID);
                    } else {
                        psg = SkillConfig.toString(EditorApplication.getProj(), skillID);
                    }
                    textPassiveSkillGroup.setText(psg);
                    break;
            }
        }
    }
    
    /**
     * 处理Table控件的特殊按键事件。
     * @param viewer 事件来源
     * @param keyCode 键码
     * @param mask 掩码
     * @return 如果不希望这个事件被控件处理，返回true。
     */
    private boolean handleKey(Object viewer, int keyCode, int mask) {
       if (viewer == tableViewer_1) {
            if (keyCode == SWT.DEL) {
                // 在法宝属性提升表中按DEL键删除选中的任务目标
                int sel = table_1.getSelectionIndex();
                if (sel != -1 && sel < getTalisman().basicAttrAdvances.size()) {
                    getTalisman().basicAttrAdvances.remove(sel);
                    tableViewer_1.refresh();
                    setDirty(true);
                    table_1.setSelection(sel);
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理Table控件的双击事件。
     * @param viewer 事件来源
     * @param sel 当前选中对象
     */
    private void onDoubleClick(Object viewer, Object sel) {
        if (viewer == tableViewer_1) {
            if ("".equals(sel)) {
                // 新建目标
                TalismanBasicAttrAdvance attr = new TalismanBasicAttrAdvance(getTalisman());
                if (new TalismanBasicAttrAdvanceDialog(getSite().getShell(), attr).open() == Dialog.OK) {
                    getTalisman().basicAttrAdvances.add(attr);
                    tableViewer_1.refresh();
                    setDirty(true);
                }
            } else {
                // 编辑选中目标
                TalismanBasicAttrAdvance attr = (TalismanBasicAttrAdvance)sel;
                if (new TalismanBasicAttrAdvanceDialog(getSite().getShell(), attr).open() == Dialog.OK) {
                    tableViewer_1.refresh(attr);
                    setDirty(true);
                }
            }
        }
    }
    
    /**
     * 当前编辑的法宝
     * @return 法宝
     */
    private TalismanType getTalisman() {
        return (TalismanType)getEditObject();
    }
}
