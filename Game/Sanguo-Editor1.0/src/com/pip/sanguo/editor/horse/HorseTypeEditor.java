package com.pip.sanguo.editor.horse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.NPCType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.EquipmentAttribute;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.ai.AIRuleConfig;
import com.pip.sanguo.editor.ai.EditAIRuleDialog;
import com.pip.sanguo.editor.equipment.PrefixAttributesEditor;
import com.pip.sanguo.editor.property.ChooseDropGroupDialog;
import com.pip.sanguo.editor.quest.RichTextPreviewer;
import com.pip.sanguo.editor.skill.DescriptionPattern;
import com.pip.sanguo.editor.util.AnimatePreviewer;
import com.pip.sanguo.editor.util.Constants;
import com.pip.util.AutoSelectAll;

public class HorseTypeEditor extends DefaultDataObjectEditor {

    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            List<DataObject> skills = EditorApplication.getProj().getDataListByType(SkillConfig.class);
            List<Object> ret = new ArrayList<Object>();
            ret.add("无");
            for (int i = 0; i < skills.size(); i++) {
                SkillConfig sc = (SkillConfig)skills.get(i);
                if (sc.type == SkillConfig.TYPE_PASSIVE) {
                    ret.add(sc);
                }
            }
            return ret.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private Combo comboSkill;
    private Text textShowName;
    private Text textMaxSkill;
    private Text textEatingRate;
    private Text textLevelValue;
    private Text textMergeValue;
    private Text textInitValue;
    private Text textSummonTime;
    private Text textDescription;
    private Text textTitle;
    private Text textID;
    private HorseTypeAttributesEditor attrEditor;
    private RichTextPreviewer previewer;
    
    public static final String ID = "com.pip.sanguo.editor.horse.HorseTypeEditor"; //$NON-NLS-1$
    private ComboViewer comboSkillViewer;

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("名称：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setText("显示名称：");

        textShowName = new Text(container, SWT.BORDER);
        final GridData gd_textShowName = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textShowName.setLayoutData(gd_textShowName);
        textShowName.addFocusListener(AutoSelectAll.instance);
        textShowName.addModifyListener(this);

        String[] items = new String[Constants.LEVEL_EXP.length];
        for (int i = 0; i < Constants.LEVEL_EXP.length; i++) {
            items[i] = String.valueOf(i);
        }

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("描述：");

        textDescription = new Text(container, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
        textDescription.setLayoutData(gd_textDescription);
        textDescription.addFocusListener(AutoSelectAll.instance);
        textDescription.addModifyListener(this);

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setText("召唤时间(毫秒)：");

        textSummonTime = new Text(container, SWT.BORDER);
        final GridData gd_textSummonTime = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSummonTime.setLayoutData(gd_textSummonTime);
        textSummonTime.addFocusListener(AutoSelectAll.instance);
        textSummonTime.addModifyListener(this);

        final Label label_14 = new Label(container, SWT.NONE);
        label_14.setText("初始价值：");

        textInitValue = new Text(container, SWT.BORDER);
        final GridData gd_textInitValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textInitValue.setLayoutData(gd_textInitValue);
        textInitValue.addFocusListener(AutoSelectAll.instance);
        textInitValue.addModifyListener(this);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("级别价值：");

        textLevelValue = new Text(container, SWT.BORDER);
        final GridData gd_textLevelValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textLevelValue.setLayoutData(gd_textLevelValue);
        textLevelValue.addFocusListener(AutoSelectAll.instance);
        textLevelValue.addModifyListener(this);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("食量：");

        textEatingRate = new Text(container, SWT.BORDER);
        final GridData gd_textEatingRate = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textEatingRate.setLayoutData(gd_textEatingRate);
        textEatingRate.addFocusListener(AutoSelectAll.instance);
        textEatingRate.addModifyListener(this);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        label_6.setText("(1点饱食度能跑的时间，秒)");

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("最大技能数量：");

        textMaxSkill = new Text(container, SWT.BORDER);
        final GridData gd_textMaxSkill = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMaxSkill.setLayoutData(gd_textMaxSkill);
        textMaxSkill.addFocusListener(AutoSelectAll.instance);
        textMaxSkill.addModifyListener(this);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setText("特殊技能：");

        comboSkillViewer = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer.setContentProvider(new ContentProvider());
        comboSkill = comboSkillViewer.getCombo();
        comboSkill.setVisibleItemCount(30);
        final GridData gd_comboSkill = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        comboSkill.setLayoutData(gd_comboSkill);
        comboSkillViewer.setInput(this);
        
        final Label label_9 = new Label(container, SWT.NONE);
        label_9.setText("合成价值：");

        textMergeValue = new Text(container, SWT.BORDER);
        final GridData gd_textMergeValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMergeValue.setLayoutData(gd_textMergeValue);
        textMergeValue.addFocusListener(AutoSelectAll.instance);
        textMergeValue.addModifyListener(this);

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new FillLayout());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

        attrEditor = new HorseTypeAttributesEditor(composite, SWT.NONE);
        attrEditor.addModifyListener(this);
        
        final Composite composite_1 = new Composite(container, SWT.NONE);
        composite_1.setLayout(new FillLayout());
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        previewer = new RichTextPreviewer(composite_1, SWT.NONE);
        
        // 设置初始值
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    private void updateView() {
        // 设置初始值
        HorseType dataDef = (HorseType)editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDescription.setText(dataDef.description);
        textShowName.setText(dataDef.showName);
        textInitValue.setText(String.valueOf(dataDef.initValue));
        textLevelValue.setText(String.valueOf(dataDef.levelValue));
        textMergeValue.setText(String.valueOf(dataDef.mergeValue));
        textSummonTime.setText(String.valueOf(dataDef.summonTime));
        textEatingRate.setText(String.valueOf(dataDef.eatingRate / 1000));
        textMaxSkill.setText(String.valueOf(dataDef.maxSkill));
        if (dataDef.specialSkill == 0) {
            comboSkill.select(0);
        } else {
            SkillConfig skill = (SkillConfig)dataDef.owner.findObject(SkillConfig.class, dataDef.specialSkill);
            if (skill == null) {
                comboSkill.select(0);
            } else {
                comboSkillViewer.setSelection(new StructuredSelection(skill));
            }
        }
        
        attrEditor.setInput(dataDef);
        
        updatePreview();
    }
    
    private void updatePreview() {
        HorseType dataDef = (HorseType)editObject;
        StringBuilder sb = new StringBuilder();
        int[] reportLevel = new int[] { 1, 25, 50, 75, 100 };
        
        for (int i = 0; i < reportLevel.length; i++) {
            if (i > 0) {
                sb.append("\n \n");
            }
            float[] attr = dataDef.generateAttributes(reportLevel[i], 0);
            sb.append(reportLevel[i] + "级\n");
            for (int j = 0; j < HorseType.ATTR_NAMES.length; j++) {
                if (attr[j] >= 1.0f) {
                    sb.append("<c00bb00>" + HorseType.ATTR_NAMES[j] + " +" + ((int)attr[j]) + "</c> ");
                }
            }
        }
        previewer.setText(sb.toString());
    }
    
    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        HorseType dataDef = (HorseType)editObject;

        // 读取输入：对象ID、标题、描述、类型、级别、价格、俸禄、阵营、增益效果
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDescription.getText();
        dataDef.showName = textShowName.getText().trim();
        if (dataDef.showName.length() == 0) {
            throw new Exception("请输入游戏内显示名称。");
        }
        try {
            dataDef.summonTime = Integer.parseInt(textSummonTime.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的召唤时间。");
        }
        try {
            dataDef.initValue = Float.parseFloat(textInitValue.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的初始价值。");
        }
        try {
            dataDef.levelValue = Float.parseFloat(textLevelValue.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的级别价值。");
        }
        try {
            dataDef.mergeValue = Float.parseFloat(textMergeValue.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的合成价值。");
        }
        try {
            dataDef.eatingRate = Integer.parseInt(textEatingRate.getText()) * 1000;
        } catch (Exception e) {
            throw new Exception("请输入正确的食量。");
        }
        try {
            dataDef.maxSkill = Integer.parseInt(textMaxSkill.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的技能数量。");
        }
        
        StructuredSelection sel = (StructuredSelection)comboSkillViewer.getSelection();
        Object skill = sel.getFirstElement();
        if (skill instanceof SkillConfig) {
            dataDef.specialSkill = ((SkillConfig)skill).id;
        } else {
            dataDef.specialSkill = 0;
        }

        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
    }

    public void modifyText(final ModifyEvent e) {
        super.modifyText(e);
        if (e.getSource() == textInitValue) {
            try {
                ((HorseType)editObject).initValue = Float.parseFloat(textInitValue.getText());
                updatePreview();
            } catch (Exception e1) {
            }
        } else if (e.getSource() == textLevelValue) {
            try {
                ((HorseType)editObject).levelValue = Float.parseFloat(textLevelValue.getText());
                updatePreview();
            } catch (Exception e1) {
            }
        } else if (e.getSource() == textMergeValue) {
            try {
                ((HorseType)editObject).mergeValue = Float.parseFloat(textMergeValue.getText());
                updatePreview();
            } catch (Exception e1) {
            }
        } else if (e.getSource() == attrEditor) {
            updatePreview();
        }
    }
}
