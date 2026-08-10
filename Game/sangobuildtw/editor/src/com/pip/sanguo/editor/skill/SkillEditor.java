package com.pip.sanguo.editor.skill;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.EffectConfig;
import com.pip.sanguo.data.skill.EffectConfigSet;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.IconChooser;
import com.pip.util.AutoSelectAll;

public class SkillEditor extends DefaultDataObjectEditor implements SelectionListener {
    class BuffListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            List<DataObject> buffs = EditorApplication.getProj().getDataListByType(BuffConfig.class);
            List<Object> ret = new ArrayList<Object>();
            SkillConfig dataDef = (SkillConfig)editObject;
            ret.add("无");
            for (DataObject dobj : buffs) {
                BuffConfig buff = (BuffConfig)dobj;
                if (buff.buffType == BuffConfig.BUFF_TYPE_STATIC) {
                    if (dataDef.type == SkillConfig.TYPE_BUFF && buff.isAreaBuff) {
                        ret.add(dobj);
                    } else if (dataDef.type == SkillConfig.TYPE_PASSIVE && !buff.isAreaBuff) {
                        ret.add(dobj);
                    }
                }
            }
            return ret.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private Combo comboPrepareAni;
    private Combo comboBuff;
    private SkillAnimationChooser textHitAni;
    private SkillAnimationChooser textPrepareAni;
    private SkillAnimationChooser textCastAni;
    private Combo comboCastAni;
    private Combo comboDamageType;
    private Text textCDGroup;
    private Combo comboClazz;
    private Combo comboMaxLevel;
    private Combo comboTargetType;
    private Combo comboType;
    public static final String ID = "com.pip.sanguo.editor.skill.SkillEditor"; //$NON-NLS-1$
    private boolean updating = false;
    private Text textDesc;
    private Text textTitle;
    private Text textID;
    private IconChooser iconChooser;
    private WeaponChooser weaponChooser;
    private EffectConfigSetEditor effectEditor;
    private Button buttonRideUse;
    private Button buttonVisible;
    private ComboViewer buffComboViewer;
    private Button buttonAutoLearn;

    @Override
    public void createPartControl(Composite parent) {
        System.currentTimeMillis();
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 12;
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

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("类型：");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setItems(new String[] {"主动攻击技能", "主动辅助技能", "被动技能", "光环技能", "复活技能"});
        comboType.setEnabled(false);
        final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboType.setLayoutData(gd_comboType);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("目标类型：");

        comboTargetType = new Combo(container, SWT.READ_ONLY);
        comboTargetType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    SkillConfig dataDef = (SkillConfig) editObject;
                    dataDef.targetType = comboTargetType.getSelectionIndex();
                    resetEffectEditor();
                    setDirty(true);
                }
            }
        });
        comboTargetType.setItems(new String[] {"单个目标", "自己", "目标附近群体", "自己附近群体"});
        final GridData gd_comboTargeType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboTargetType.setLayoutData(gd_comboTargeType);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("最高级别：");

        comboMaxLevel = new Combo(container, SWT.READ_ONLY);
        comboMaxLevel.setVisibleItemCount(20);
        comboMaxLevel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    SkillConfig dataDef = (SkillConfig) editObject;
                    int maxLevel = comboMaxLevel.getSelectionIndex() + 1;
                    dataDef.setMaxLevel(maxLevel);
                    resetEffectEditor();
                    setDirty(true);
                }
            }
        });
        comboMaxLevel.setItems(new String[] {"1级", "2级", "3级", "4级", "5级", "6级", "7级", "8级", "9级", "10级", "11级", "12级", "13级", "14级", "15级", "16级", "17级", "18级", "19级", "20级"});
        final GridData gd_comboMaxLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboMaxLevel.setLayoutData(gd_comboMaxLevel);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("职业：");

        comboClazz = new Combo(container, SWT.READ_ONLY);
        comboClazz.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        comboClazz.setItems(new String[] {"武将", "刺客", "谋士", "方士", "不可学习"});
        final GridData gd_comboClazz = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboClazz.setLayoutData(gd_comboClazz);
        
        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("描述：");

        textDesc = new Text(container, SWT.BORDER);
        final GridData gd_textDesc = new GridData(SWT.FILL, SWT.CENTER, false, false, 10, 1);
        gd_textDesc.widthHint = 200;
        textDesc.setLayoutData(gd_textDesc);
        textDesc.addFocusListener(AutoSelectAll.instance);
        textDesc.addModifyListener(this);

        final Button buttonDesc = new Button(container, SWT.NONE);
        buttonDesc.setLayoutData(new GridData());
        buttonDesc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                SkillConfig dataDef = (SkillConfig)editObject;
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
        buttonDesc.setText("测试描述");

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setLayoutData(new GridData());
        label_7.setText("图标：");
        
        iconChooser = new IconChooser(container, SWT.NONE, 0);
        iconChooser.setHandler(this);
        final GridData gd_iconChooser = new GridData(SWT.FILL, SWT.CENTER, true, false);
        iconChooser.setLayoutData(gd_iconChooser);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setLayoutData(new GridData());
        label_8.setText("冷却组：");

        textCDGroup = new Text(container, SWT.BORDER);
        textCDGroup.addModifyListener(this);
        final GridData gd_textCDGroup = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textCDGroup.setLayoutData(gd_textCDGroup);

        final Label label_9 = new Label(container, SWT.NONE);
        label_9.setLayoutData(new GridData());
        label_9.setText("效果类型：");

        comboDamageType = new Combo(container, SWT.READ_ONLY);
        comboDamageType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        comboDamageType.setVisibleItemCount(10);
        comboDamageType.setItems(new String[] {"物理", "法术", "抽蓝", "诅咒", "治疗", "回蓝", "增强"});
        final GridData gd_comboDamageType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboDamageType.setLayoutData(gd_comboDamageType);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Button buttonAnalyse = new Button(container, SWT.NONE);
        buttonAnalyse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                SkillAnalyseDialog dlg = new SkillAnalyseDialog(getSite().getShell());
                dlg.setSkill((SkillConfig)editObject);
                dlg.open();
            }
        });
        buttonAnalyse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        buttonAnalyse.setText("分析");

        weaponChooser = new WeaponChooser(container, SWT.NONE);
        weaponChooser.addModifyListener(this);
        final GridData gd_weaponChooser = new GridData(SWT.FILL, SWT.CENTER, true, false, 6, 1);
        weaponChooser.setLayoutData(gd_weaponChooser);

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 3;
        composite.setLayout(gridLayout_1);

        buttonRideUse = new Button(composite, SWT.CHECK);
        buttonRideUse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        buttonRideUse.setText("骑马可用");

        buttonVisible = new Button(composite, SWT.CHECK);
        buttonVisible.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        buttonVisible.setText("可装配");

        buttonAutoLearn = new Button(composite, SWT.CHECK);
        buttonAutoLearn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        buttonAutoLearn.setText("自动学习1级");

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setLayoutData(new GridData());
        label_12.setText("对应BUFF：");

        buffComboViewer = new ComboViewer(container, SWT.READ_ONLY);
        buffComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        buffComboViewer.setContentProvider(new BuffListContentProvider());
        comboBuff = buffComboViewer.getCombo();
        comboBuff.setVisibleItemCount(20);
        final GridData gd_comboBuff = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        comboBuff.setLayoutData(gd_comboBuff);
        buffComboViewer.setInput(this);

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setLayoutData(new GridData());
        label_13.setText("准备动画：");

        comboPrepareAni = new Combo(container, SWT.READ_ONLY);
        comboPrepareAni.setItems(new String[] { "无", "挥刀", "射箭", "施法1", "施法2", "施法3" });
        final GridData gd_textPrepareAni = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboPrepareAni.setLayoutData(gd_textPrepareAni);
        comboPrepareAni.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        comboPrepareAni.setVisibleItemCount(10);

        textPrepareAni = new SkillAnimationChooser(container, SWT.BORDER);
        textPrepareAni.addModifyListener(this);
        final GridData gd_textPrepareAni_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        textPrepareAni.setLayoutData(gd_textPrepareAni_1);
        new Label(container, SWT.NONE);

        final Label label_10 = new Label(container, SWT.NONE);
        label_10.setLayoutData(new GridData());
        label_10.setText("起手动画：");

        comboCastAni = new Combo(container, SWT.READ_ONLY);
        comboCastAni.setItems(new String[] { "无", "挥刀", "射箭", "施法1", "施法2", "施法3" });
        final GridData gd_textCastAni = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboCastAni.setLayoutData(gd_textCastAni);
        comboCastAni.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (!updating) {
                    setDirty(true);
                }
            }
        });
        comboCastAni.setVisibleItemCount(10);

        textCastAni = new SkillAnimationChooser(container, SWT.BORDER);
        textCastAni.addModifyListener(this);
        final GridData gd_textCastAni2 = new GridData(SWT.FILL, SWT.CENTER, false, false);
        textCastAni.setLayoutData(gd_textCastAni2);
        new Label(container, SWT.NONE);

        final Label label_11 = new Label(container, SWT.NONE);
        label_11.setLayoutData(new GridData());
        label_11.setText("命中动画：");

        textHitAni = new SkillAnimationChooser(container, SWT.BORDER);
        textHitAni.addModifyListener(this);
        final GridData gd_textHitAni = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textHitAni.setLayoutData(gd_textHitAni);

        effectEditor = new EffectConfigSetEditor(container, SWT.NONE);
        final GridData gd_effectEditor = new GridData(SWT.FILL, SWT.FILL, true, true, 12, 1);
        effectEditor.setLayoutData(gd_effectEditor);
        effectEditor.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                if (!updating) {
                    setDirty(true);
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
        SkillConfig dataDef = (SkillConfig) editObject;
        updating = true;
        updateControlState();
        
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDesc.setText(dataDef.description);

        iconChooser.setIcon(dataDef.iconID);
        weaponChooser.setWeapons(dataDef.requireWeapon);

        comboType.select(dataDef.type);
        comboTargetType.select(dataDef.targetType);
        comboMaxLevel.select(dataDef.maxLevel - 1);
        comboClazz.select(dataDef.clazz);
        textCDGroup.setText(String.valueOf(dataDef.cdGroup));
        comboDamageType.select(dataDef.damageType);
        
        // 准备动画和释放动画两个字段，其低2字节表示动作类型，高2字节表示附加动画ID
        short value = (short)dataDef.prepareAnimation;
        comboPrepareAni.select(value + 1);
        value = (short)(dataDef.prepareAnimation >> 16);
        textPrepareAni.setAnimationID(value);
        value = (short)dataDef.castAnimation;
        comboCastAni.select(value + 1);
        value = (short)(dataDef.castAnimation >> 16);
        textCastAni.setAnimationID(value);
        
        textHitAni.setAnimationID(dataDef.hitAnimation);
        comboBuff.select(0);
        for (int i = 1; i < comboBuff.getItemCount(); i++) {
            Object obj = buffComboViewer.getElementAt(i);
            if (obj instanceof BuffConfig && ((BuffConfig)obj).id == dataDef.passiveBuff) {
                comboBuff.select(i);
            }
        }
        buttonRideUse.setSelection(dataDef.rideUse);
        buttonVisible.setSelection(dataDef.visible);
        buttonAutoLearn.setSelection(dataDef.autoLearn);

        resetEffectEditor();
        
        updating = false;
    }
    
    private void resetEffectEditor() {
        SkillConfig dataDef = (SkillConfig) editObject;
        
        // 配置允许出现的效果
        if (dataDef.type == SkillConfig.TYPE_PASSIVE || dataDef.type == SkillConfig.TYPE_BUFF) {
            effectEditor.setAllowedEffects(new int[0]);
        } else if (dataDef.type == SkillConfig.TYPE_RELIVE) {
            effectEditor.setAllowedEffects(new int[] {
                EffectConfig.RELIVE_TARGET,
                EffectConfig.ADD_DEBUFF_ON_HIT
            });
        } else if (dataDef.type == SkillConfig.TYPE_AID) {
            effectEditor.setAllowedEffects(new int[] {
                EffectConfig.CHANGE_CURE_EFFECT,
                EffectConfig.CHANGE_THREAT,
                EffectConfig.CHANGE_MAGIC_CRIT,
                EffectConfig.ADD_DEBUFF_ON_HIT,
                EffectConfig.ADD_BUFF_ON_HIT,
                EffectConfig.DOUBLE_DAMAGE_ON_HIT,
                EffectConfig.CURE_TARGET,
                EffectConfig.CURE_TARGET_IGNORE_MAX,
                EffectConfig.DISPEL_DEBUFF,
                EffectConfig.DISPEL_ALL_DEBUFF,
             });
        } else if (dataDef.type == SkillConfig.TYPE_ATTACK) {
            effectEditor.setAllowedEffects(new int[] {
                EffectConfig.CHANGE_PHYICAL_AP,
                EffectConfig.CHANGE_MAGIC_AP,
                EffectConfig.CHANGE_WEAPON_ATK,
                EffectConfig.CHANGE_WEAPON_MATK,
                EffectConfig.CHANGE_THREAT,
                EffectConfig.CHANGE_PHYSICAL_HIT,
                EffectConfig.CHANGE_PHYSICAL_CRIT,
                EffectConfig.CHANGE_PHYSICAL_DODGE,
                EffectConfig.CHANGE_MAGIC_HIT,
                EffectConfig.CHANGE_MAGIC_CRIT,
                EffectConfig.CHANGE_MAGIC_DODGE,
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
                EffectConfig.DISPEL_BUFF,
                EffectConfig.DISPEL_ALL_BUFF,
                EffectConfig.INTERRUPT,
                EffectConfig.CHANGE_THREAT_TOTAL,
                EffectConfig.FIRST_THREAT_TEMP,
                EffectConfig.TRANSPORT_TO_ME,
                EffectConfig.TRANSPORT_TO_POS,
             });
        }
        
        EffectConfigSet newSet = new EffectConfigSet();
        newSet.setLevelCount(dataDef.effects.getLevelCount());
        newSet.addGeneralEffect(dataDef.getGeneralConfig());
        for (EffectConfig eff : dataDef.effects.getAllEffects()) {
            newSet.addEffect(eff);
        }
        effectEditor.setEditObject(newSet);
    }
    
    protected void updateControlState() {
        SkillConfig dataDef = (SkillConfig) editObject;
        boolean isPassive = dataDef.type == SkillConfig.TYPE_BUFF || dataDef.type == SkillConfig.TYPE_PASSIVE;
        comboBuff.setEnabled(isPassive);
        textHitAni.setEnabled(!isPassive);
        comboPrepareAni.setEnabled(!isPassive);
        comboCastAni.setEnabled(!isPassive);
        comboDamageType.setEnabled(!isPassive);
        textCDGroup.setEnabled(!isPassive);
        comboTargetType.setEnabled(!isPassive);
        buttonRideUse.setEnabled(!isPassive);
        buttonVisible.setEnabled(!isPassive);
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        SkillConfig dataDef = (SkillConfig) editObject;
        
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
        
        dataDef.iconID = iconChooser.getIconIndex();
        dataDef.requireWeapon = weaponChooser.getWeapons();

        dataDef.targetType = comboTargetType.getSelectionIndex();
        dataDef.clazz = comboClazz.getSelectionIndex();
        try {
            dataDef.cdGroup = Integer.parseInt(textCDGroup.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的冷却组。");
        }
        dataDef.damageType = comboDamageType.getSelectionIndex();
        
        // 准备动画和释放动画两个字段，其低2字节表示动作类型，高2字节表示附加动画ID
        int high = textPrepareAni.getAnimationID();
        int low = comboPrepareAni.getSelectionIndex() - 1;
        dataDef.prepareAnimation = (high << 16) | (low & 0xFFFF);
        high = textCastAni.getAnimationID();
        low = comboCastAni.getSelectionIndex() - 1;
        dataDef.castAnimation = (high << 16) | (low & 0xFFFF);
        
        dataDef.hitAnimation = textHitAni.getAnimationID();
        
        StructuredSelection sel = (StructuredSelection)buffComboViewer.getSelection();
        if (sel.isEmpty()) {
            dataDef.passiveBuff = -1;
        } else {
            Object obj = sel.getFirstElement();
            if (obj instanceof BuffConfig) {
                dataDef.passiveBuff = ((BuffConfig)obj).id;
            } else {
                dataDef.passiveBuff = -1;
            }
        }
        
        dataDef.rideUse = buttonRideUse.getSelection();
        dataDef.visible = buttonVisible.getSelection();
        dataDef.autoLearn = buttonAutoLearn.getSelection();

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
            setDirty(true);
        }
    }
}
