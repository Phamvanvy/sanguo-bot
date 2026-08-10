package com.pip.sanguo.editor.skill;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
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
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.EffectConfig;
import com.pip.sanguo.data.skill.EffectConfigSet;
import com.pip.sanguo.data.skill.Effect_MultiAdd;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.property.ItemCellEditor;
import com.pip.sanguo.editor.util.IconChooser;
import com.pip.util.AutoSelectAll;

public class BuffEditor extends DefaultDataObjectEditor implements SelectionListener {
    private Combo comboMergeStrategy;
    private IconChooser iconChooser;
    private Combo comboMaxLevel;
    private Text textDesc;
    private Text textTitle;
    private Text textID;
    private Button buttonGood, buttonDispelable;
    public static final String ID = "com.pip.sanguo.editor.skill.BuffEditor"; //$NON-NLS-1$
    private WeaponChooser weaponChooser;
    private EffectConfigSetEditor effectEditor;
    private boolean updating = false;
    private Button areaBuffButton;
    private Button buttonOffline;
    private Label typeLabel;
    private Button buttonKeepOnDie;
    private Button buttonNeedWeight;//是否需要权重
    private Text textWeight;//权重编辑框
    
    public static boolean canEditTextWeight=true;

    /**
     * Create contents of the editor part
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 10;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setText("类型：");

        typeLabel = new Label(container, SWT.NONE);
        final GridData gd_typeLabel = new GridData(SWT.FILL, SWT.CENTER, false, false);
        typeLabel.setLayoutData(gd_typeLabel);
        typeLabel.setText("未知类型");

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("标题：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("最高级别：");

        comboMaxLevel = new Combo(container, SWT.READ_ONLY);
        comboMaxLevel.setVisibleItemCount(20);
        comboMaxLevel.setItems(new String[] {"1级", "2级", "3级", "4级", "5级", "6级", "7级", "8级", "9级", "10级", "11级", "12级", "13级", "14级", "15级", "16级", "17级", "18级", "19级", "20级"});
        final GridData gd_comboMaxLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboMaxLevel.setLayoutData(gd_comboMaxLevel);
        comboMaxLevel.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                if (!updating) {
                    BuffConfig dataDef = (BuffConfig) editObject;
                    int maxLevel = comboMaxLevel.getSelectionIndex() + 1;
                    dataDef.setMaxLevel(maxLevel);
                    resetEffectEditor();
                    setDirty(true);
                }
            }
        });

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("图标：");

        iconChooser = new IconChooser(container, SWT.NONE, 0);
        iconChooser.setHandler(this);
        final GridData gd_iconChooser = new GridData(SWT.FILL, SWT.CENTER, true, false);
        iconChooser.setLayoutData(gd_iconChooser);

        buttonGood = new Button(container, SWT.CHECK);
        buttonGood.setLayoutData(new GridData());
        buttonGood.setText("是否良性");
        buttonGood.addSelectionListener(this);

        buttonDispelable = new Button(container, SWT.CHECK);
        buttonDispelable.setLayoutData(new GridData());
        buttonDispelable.setText("可驱散");
        buttonDispelable.addSelectionListener(this);

        areaBuffButton = new Button(container, SWT.CHECK);
        areaBuffButton.setLayoutData(new GridData());
        areaBuffButton.setText("光环");
        areaBuffButton.addSelectionListener(this);

        buttonKeepOnDie = new Button(container, SWT.CHECK);
        final GridData gd_buttonKeepOnDie = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        buttonKeepOnDie.setLayoutData(gd_buttonKeepOnDie);
        buttonKeepOnDie.setText("死亡后保持");
        buttonKeepOnDie.addSelectionListener(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("描述：");

        textDesc = new Text(container, SWT.BORDER);
        final GridData gd_textDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1);
        gd_textDesc.widthHint = 300;
        textDesc.setLayoutData(gd_textDesc);
        textDesc.addFocusListener(AutoSelectAll.instance);
        textDesc.addModifyListener(this);

        final Button buttonTestDesc = new Button(container, SWT.NONE);
        buttonTestDesc.setLayoutData(new GridData());
        buttonTestDesc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                BuffConfig dataDef = (BuffConfig)editObject;
                DescriptionPattern pat = new DescriptionPattern(dataDef);
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < dataDef.maxLevel; i++) {
                    if (i > 0) {
                        buf.append("\r\n");
                    }
                    buf.append(pat.generate(i + 1));
                }
                MessageDialog.openInformation(getSite().getShell(), "描述信息", buf.toString());
            }
        });
        buttonTestDesc.setText("测试描述");

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setLayoutData(new GridData());
        label_5.setText("合并逻辑：");

        comboMergeStrategy = new Combo(container, SWT.READ_ONLY);
        comboMergeStrategy.setItems(new String[] {"总是不合并", "可叠加3层", "高级覆盖低级", "同来源覆盖", "总是覆盖"});
        final GridData gd_comboMergeStrategy = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboMergeStrategy.setLayoutData(gd_comboMergeStrategy);
        comboMergeStrategy.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });

        weaponChooser = new WeaponChooser(container, SWT.NONE);
        weaponChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 10, 1));
        weaponChooser.addModifyListener(this);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("效果：");
        
        
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);

        buttonOffline = new Button(container, SWT.CHECK);
        final GridData gd_buttonOffline = new GridData();
        buttonOffline.setLayoutData(gd_buttonOffline);
        buttonOffline.setText("即使下线也计时");
        buttonOffline.addSelectionListener(this);
        
        
        
        //权重
        buttonNeedWeight = new Button(container, SWT.CHECK);
//        final GridData gd_buttonKeepOnDie = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
//        buttonKeepOnDie.setLayoutData(gd_buttonKeepOnDie);
        buttonNeedWeight.setText("是否需要权重");
        buttonNeedWeight.setVisible(false);
        buttonNeedWeight.setSelection(false);
        
        final Label label_Weight = new Label(container, SWT.NONE);
        label_Weight.setText("权重：");
        label_Weight.setVisible(false);
        textWeight = new Text(container, SWT.BORDER);
        final GridData gd_textWeight = new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1);
        gd_textWeight.widthHint = 300;
        textWeight.setLayoutData(gd_textDesc);
        textWeight.addFocusListener(AutoSelectAll.instance);
        textWeight.setVisible(false);
        textWeight.setEnabled(false);
        textWeight.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e) {
                if (!updating) {
                    setDirty(true);
                    if(canEditTextWeight){
                        String[] weights=textWeight.getText().split("; ");
                        for(String weight:weights){
                            if(weight.length()==0){
                                continue;
                            }
                            int type=Integer.parseInt(weight.substring(weight.indexOf(":")+1).split(",")[0]);
                            effectEditor.updateWeight(type, weight.substring(weight.indexOf(":")+1).split(",")[1], weight.substring(weight.indexOf(":")+1).split(",")[2]);
                        }
                    }else{
                        canEditTextWeight=true; 
                    }
                }
            }
        });

        buttonNeedWeight.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
                textWeight.setEnabled(buttonNeedWeight.getSelection());
            }
        });
        
        
        BuffConfig dataDef = (BuffConfig) editObject;
        if(dataDef.buffType==BuffConfig.BUFF_TYPE_EQUIP){//如果是装备buff才显示权重框
            buttonNeedWeight.setVisible(true);
            textWeight.setVisible(true);
            label_Weight.setVisible(true);
        }

        effectEditor = new EffectConfigSetEditor(container, SWT.NONE);
        final GridData gd_effectEditor = new GridData(SWT.FILL, SWT.FILL, true, true, 10, 1);
        effectEditor.setLayoutData(gd_effectEditor);
        effectEditor.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                if (!updating) {
                    setDirty(true);
                    if(textWeight!=null&&textWeight.isEnabled()&&textWeight.isVisible()){
                        StringBuffer sb=new StringBuffer();
                        for(String str:effectEditor.getWeight().values()){
                            int type=Integer.parseInt(str.split(",")[0]);
                            String name=EffectConfig.TYPE_NAMES[type][0];
                            sb.append(name+":"+str+" ");
                        }
                        textWeight.setText(sb.toString());
                        canEditTextWeight=false;
                    }
                }
            }
        });

        updateView();
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    /*
     * 把数据中的值设置到界面上
     */
    protected void updateView() {
        // 设置初始值
        BuffConfig dataDef = (BuffConfig) editObject;
        updating = true;
        if (dataDef.buffType == BuffConfig.BUFF_TYPE_STATIC) {
            comboMergeStrategy.setEnabled(false);
            buttonGood.setEnabled(false);
            buttonDispelable.setEnabled(false);
            areaBuffButton.setEnabled(true);
            iconChooser.setEnabled(dataDef.isAreaBuff);
        } else if (dataDef.buffType == BuffConfig.BUFF_TYPE_EQUIP) {
            comboMergeStrategy.setEnabled(true);
            buttonGood.setEnabled(false);
            buttonDispelable.setEnabled(false);
            areaBuffButton.setEnabled(false);
            iconChooser.setEnabled(false);
        } else {
            comboMergeStrategy.setEnabled(true);
            buttonGood.setEnabled(true);
            buttonDispelable.setEnabled(true);
            areaBuffButton.setEnabled(false);
            iconChooser.setEnabled(true);
        }
        textID.setText(String.valueOf(dataDef.id));
        switch (dataDef.buffType) {
        case BuffConfig.BUFF_TYPE_DYNAMIC:
            typeLabel.setText("临时BUFF");
            break;
        case BuffConfig.BUFF_TYPE_STATIC:
            typeLabel.setText("永久BUFF");
            break;
        case BuffConfig.BUFF_TYPE_EQUIP:
            typeLabel.setText("装备BUFF");
            break;
        }
        if(dataDef.hadWeight){
            buttonNeedWeight.setEnabled(true);
            textWeight.setEnabled(true);
            buttonNeedWeight.setSelection(true);
            
            String[] weights=dataDef.weights.split(";");
            for(String weight:weights){
                if(weight.length()==0){
                    continue;
                }
                int type=Integer.parseInt(weight.substring(weight.indexOf(":")+1).split(",")[0]);
                effectEditor.updateWeight(type, weight.substring(weight.indexOf(":")+1).split(",")[1], weight.substring(weight.indexOf(":")+1).split(",")[2]);
            }
            StringBuffer sb=new StringBuffer();
            for(String str:effectEditor.getWeight().values()){
                int type=Integer.parseInt(str.split(",")[0]);
                String name=EffectConfig.TYPE_NAMES[type][0];
                sb.append(name+":"+str+" ");
            }
            
            textWeight.setText(sb.toString());
            effectEditor.setWeight(dataDef.weights);
        }
        
        textTitle.setText(dataDef.title);
        textDesc.setText(dataDef.description);
        comboMergeStrategy.select(dataDef.mergeStrategy);
        iconChooser.setIcon(dataDef.iconID, Item.ICON_IMAGE_ABILITY);
        comboMaxLevel.select(dataDef.maxLevel - 1);
        buttonGood.setSelection(dataDef.good);
        buttonDispelable.setSelection(dataDef.dispelable);
        buttonKeepOnDie.setSelection(dataDef.keepOnDie);
        areaBuffButton.setSelection(dataDef.isAreaBuff);
        weaponChooser.setWeapons(dataDef.requireWeapon);
        buttonOffline.setSelection(dataDef.updateEvenOffline);

        resetEffectEditor();
        updating = false;
    }
    
    private void resetEffectEditor() {
        BuffConfig dataDef = (BuffConfig) editObject;
        
        effectEditor.setAllowedEffects(new int[] {
            EffectConfig.CHANGE_PHYICAL_AP,
            EffectConfig.CHANGE_MAGIC_AP,
            EffectConfig.CHANGE_BASIC_MAGIC_AP,
            EffectConfig.CHANGE_MAGIC_HEAL,
            EffectConfig.CHANGE_WEAPON_ATK,
            EffectConfig.CHANGE_WEAPON_MATK,
            EffectConfig.CHANGE_THREAT,
            EffectConfig.CHANGE_ARMOR,
            EffectConfig.CHANGE_MAGIC_ARMOR,
            EffectConfig.CHANGE_PHYSICAL_HIT,
            EffectConfig.CHANGE_PHYSICAL_CRIT,
            EffectConfig.CHANGE_PHYSICAL_DODGE,
            EffectConfig.CHANGE_MAGIC_HIT,
            EffectConfig.CHANGE_MAGIC_CRIT,
            EffectConfig.CHANGE_MAGIC_DODGE,
            EffectConfig.CHANGE_PHYSICAL_HIT_RATE,
            EffectConfig.CHANGE_PHYSICAL_CRIT_RATE,
            EffectConfig.CHANGE_PHYSICAL_DODGE_RATE,
            EffectConfig.CHANGE_MAGIC_HIT_RATE,
            EffectConfig.CHANGE_MAGIC_CRIT_RATE,
            EffectConfig.CHANGE_MAGIC_DODGE_RATE,
            EffectConfig.REDUCE_PHYSICAL_DAMAGE,
            EffectConfig.REDUCE_MAGIC_DAMAGE,
            EffectConfig.CHANGE_MP_RENEW,
            EffectConfig.CHANGE_HP_RENEW,
            EffectConfig.CHANGE_SPEED,
            EffectConfig.ADD_SPEED,
            EffectConfig.CHANGE_MAXHP,
            EffectConfig.CHANGE_BASIC_HP,
            EffectConfig.CHANGE_BASIC_MP,
            EffectConfig.CHANGE_CURE_EFFECT,
            EffectConfig.CHANGE_STA,
            EffectConfig.CHANGE_AGI,
            EffectConfig.CHANGE_STR,
            EffectConfig.CHANGE_INT,
            EffectConfig.APPEND_MAGIC_DAMAGE,
            EffectConfig.IGNORE_ARMOR,
            EffectConfig.IGNORE_MAGIC_ARMOR,
            EffectConfig.ADD_MP_ON_HIT,
            EffectConfig.ADD_DEBUFF_ON_HIT,
            EffectConfig.ADD_BUFF_ON_HIT,
            EffectConfig.FIRST_THREAT_ON_HIT,
            EffectConfig.FEAR_ON_HIT,
            EffectConfig.SLOW_ON_HIT,
            EffectConfig.PARALYZE_ON_HIT,
            EffectConfig.STAY_ON_HIT,
            EffectConfig.DUMB_ON_HIT,
            EffectConfig.REPEAT_ON_HIT,
            EffectConfig.DOUBLE_DAMAGE_ON_HIT,
            EffectConfig.DEC_MP_ON_HIT,
            EffectConfig.ADD_HP_ON_HIT,
            EffectConfig.TWO_HIT_ON_HIT,
            EffectConfig.DOUBLE_HEAL_ON_HIT,
            EffectConfig.RELIVE_TARGET,
            EffectConfig.IMMUNE_PHYICAL_ATTACK,
            EffectConfig.IMMUNE_MAGIC_ATTACK,
            EffectConfig.IMMUNE_SLOW_ATTACK,
            EffectConfig.IMMUNE_FEAR,
            EffectConfig.IMMUNE_DUMB,
            EffectConfig.IMMUNE_PARALYZE,
            EffectConfig.IMMUNE_STAY,
            EffectConfig.IMMUNE_BREAKATTACK,
            EffectConfig.COUNTER_ATTACK,
            EffectConfig.BOUNCE,
            EffectConfig.ADD_DEBUFF_ON_HITED,
            EffectConfig.ADD_BUFF_ON_HITED,
            EffectConfig.SLOW_ON_HITED,
            EffectConfig.CHANGE_MP_USE,
            EffectConfig.SET_VARIABLE,
            EffectConfig.HOT,
            EffectConfig.DOT,
            EffectConfig.MPHOT,
            EffectConfig.MPDOT,
            EffectConfig.MP_SHIELD,
            EffectConfig.VAMPIRE_ON_HIT,
            EffectConfig.CHANGE_CD_TIME,
            EffectConfig.CHANGE_DISTANCE,
            EffectConfig.CHANGE_ACT_TIME,
            EffectConfig.CHANGE_RANGE,
            EffectConfig.CANNOT_MOVE,
            EffectConfig.HP_ACTIVE_BUFF,
            EffectConfig.CRIT_ACTIVE_BUFF,
            EffectConfig.CRITED_ACTIVE_BUFF,
            EffectConfig.LIMIT_EFFECT_TIMES,
            EffectConfig.LIMIT_SKILL,
            EffectConfig.LIMIT_MAP,
            EffectConfig.CHANGE_BATTLE_PHYICAL_AP,
            EffectConfig.CHANGE_BATTLE_MAGIC_AP,
            EffectConfig.CHANGE_BATTLE_WEAPON_ATK,
            EffectConfig.CHANGE_BATTLE_WEAPON_MATK,
            EffectConfig.CHANGE_BATTLE_PHYSICAL_HIT,
            EffectConfig.CHANGE_BATTLE_PHYSICAL_CRIT,
            EffectConfig.CHANGE_BATTLE_PHYSICAL_DODGE,
            EffectConfig.CHANGE_BATTLE_PHYSICAL_CRITED,
            EffectConfig.CHANGE_BATTLE_MAGIC_HIT,
            EffectConfig.CHANGE_BATTLE_MAGIC_CRIT,
            EffectConfig.CHANGE_BATTLE_MAGIC_DODGE,
            EffectConfig.CHANGE_BATTLE_MAGIC_CRITED,
            EffectConfig.CHANGE_BATTLE_ARMOR,
            EffectConfig.CHANGE_BATTLE_MAGIC_ARMOR,
            EffectConfig.CHANGE_EXP_RATE,
            EffectConfig.CHANGE_HORSE_EXP_RATE,
            EffectConfig.CHANGE_MONEY_RATE,
            EffectConfig.CHANGE_PARAM,
            EffectConfig.REMOVE_ON_BATTLE_END,
            EffectConfig.ADD_NATUAL_ENHANCE,
            EffectConfig.ADD_JEWEL_ENHANCE,
            EffectConfig.ADD_STAR_ENHANCE,
            EffectConfig.ADD_HP_ENHANCE,
            EffectConfig.ADD_MP_ENHANCE,
            EffectConfig.ADD_FEARDEBUFF,
            EffectConfig.ADD_PARALYZEDEBUFF,
            EffectConfig.ADD_STAYDEBUFF,
            EffectConfig.ADD_DUMBDEBUFF,
            EffectConfig.FEAR_ADD_BUFF_OWNER,
            EffectConfig.PARALYZE_ADD_BUFF_OWNER,
            EffectConfig.DUMB_ADD_BUFF_OWNER,
            EffectConfig.STAY_ADD_BUFF_OWNER,
            EffectConfig.CHANGE_REWARD_RATE,
            EffectConfig.NOTEFFECT_MAP,
            EffectConfig.BUFFOVER_ADDBUFF,
            EffectConfig.DYNAMIC_CHANGE_PHYICAL_AP,
            EffectConfig.DYNAMIC_CHANGE_MAGIC_AP,
            EffectConfig.IGNORE_PHYSICS_IMMUNE,
            EffectConfig.IGNORE_MAGIC_IMMUNE,
            EffectConfig.CHANGE_PEOPLEEXP_RATE_BY_TONG_SKILL,
            EffectConfig.CHANGE_HRSEEXP_RATE_BY_TONG_SKILL,
            EffectConfig.IGNORE_IGNORE_ARMOR,
            EffectConfig.IGNORE_IIGNORE_MAGIC_ARMOR,
            EffectConfig.ADD_PLAYER_JEWELENHANCE,
            EffectConfig.ADD_HORSE_JEWELENHANCE,
            EffectConfig.ATTENDANTONHIT_ADDBUFF_OWNER,
            EffectConfig.CHANGE_LASTTIME,
            EffectConfig.CHANGE_CRITED,
            EffectConfig.ANTI_DAMAGE,
        });
        
        EffectConfigSet newSet = new EffectConfigSet();
        newSet.setLevelCount(dataDef.effects.getLevelCount());
        newSet.addGeneralEffect(dataDef.getGeneralConfig());
        for (EffectConfig eff : dataDef.effects.getAllEffects()) {
            newSet.addEffect(eff);
        }
        effectEditor.setEditObject(newSet);
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        BuffConfig dataDef = (BuffConfig) editObject;
        
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
        
        dataDef.hadWeight=buttonNeedWeight.getSelection();//是否有权重
        if(dataDef.hadWeight){
            StringBuffer sb=new StringBuffer();
            String[] weightsTemp=textWeight.getText().split("; ");
            for(String weight:weightsTemp){
                weight=weight.substring(weight.indexOf(":")+1);
                sb.append(weight+";");
            }
            dataDef.weights=sb.toString();
        }
        
        dataDef.mergeStrategy = comboMergeStrategy.getSelectionIndex();
        dataDef.iconID = iconChooser.getIconIndex();
        dataDef.good = buttonGood.getSelection();
        dataDef.dispelable = buttonDispelable.getSelection();
        dataDef.keepOnDie = buttonKeepOnDie.getSelection();
        dataDef.isAreaBuff = areaBuffButton.getSelection();
        if (dataDef.buffType == BuffConfig.BUFF_TYPE_STATIC) {
            if (!dataDef.isAreaBuff) {
                dataDef.iconID = -1;
            }
        } else if (dataDef.buffType == BuffConfig.BUFF_TYPE_EQUIP) {
            dataDef.isAreaBuff = false;
            dataDef.iconID = -1;
        }
        dataDef.requireWeapon = weaponChooser.getWeapons();
        dataDef.updateEvenOffline = buttonOffline.getSelection();

        dataDef.effects.clear();
        for (EffectConfig eff : effectEditor.getEditObject().getAllEffects()) {
            if (eff.getType() != -1) {
                dataDef.effects.addEffect(eff);
            }
        }

        // 测试生成代码
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        dataDef.generateJava(pw, "a", "a");
    }

    /*
     * 保存undo状态
     */
    protected Object saveState() {
        try {
            saveData();
        } catch (Exception e) {
        }
        return editObject.save();
    }

    /*
     * 恢复保存的转台
     */
    protected void loadState(Object stateObj) {
        editObject.load((Element) stateObj);
        updateView();
    }

    /**
     * 文本修改后设置修改标志。
     */
    public void modifyText(final ModifyEvent e) {
        if (!updating) {
            setDirty(true);
        }
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
        if (!updating) {
            if (e.getSource() == areaBuffButton) {
                boolean isAreaBuff = areaBuffButton.getSelection();
                if (isAreaBuff) {
                    iconChooser.setEnabled(true);
                    comboMergeStrategy.select(BuffConfig.MERGE_LEVEL);
                } else {
                    iconChooser.setEnabled(false);
                    comboMergeStrategy.select(BuffConfig.MERGE_LEVEL);
                }
            }
            setDirty(true);
        }
    }
}
