package com.pip.sanguo.editor.attendant;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.pip.sanguo.data.AttendantType;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.AnimatePreviewer;
import com.pip.sanguo.editor.util.Constants;
import com.pip.util.AutoSelectAll;

/**
 * 随从编辑器
 * @author dchen
 */
public class AttendantTypeEditor extends DefaultDataObjectEditor {
    
    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            List<DataObject> skills = EditorApplication.getProj().getDataListByType(SkillConfig.class);
            List<Object> ret = new ArrayList<Object>();
            ret.add("无");
            for (int i = 0; i < skills.size(); i++) {
                SkillConfig sc = (SkillConfig)skills.get(i);
                ret.add(sc);
            }
            return ret.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    class ContentProvider_1 implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return ((ProjectData)inputElement).getDataListByType(Animation.class).toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private Text textMagicAP;
    private Text textMagicArmor2;
    private Text textMagicArmor;
    private Text textMagicAP2;
    private Text critical;
    private Text critical2;
    private Text spellcritical;
    private Text spellcritical2;
    private Text decritical;
    private Text decritical2;
    private Text textPhyAP2;
    private Text textPhyAP;
    private Text textAGI2;
    private Text textINT2;
    private Text textSTA2;
    private Text textSTR2;
    private Text textArmor2;
    private Text textArmor;
    private Text textMP2;
    private Text textHP2;
    private ComboViewer comboImage;
    private Combo comboType;
    private Text textINT;
    private Text textAGI;
    private Text textSTR;
    private Text textSTA;
    private Combo comboQuality;
    private Combo comboSkill;
    private Text textMP;
    private Text textHP;
    private Text textDescription;
    private Text textduration;
    private Text textdistance;
    private Text texteat;
    private Text textTitle;
    private Text textID;
    private Combo comboImageCtrl;
    private AnimatePreviewer previewer;
    private Button buttonSkill1;
    private Button buttonSkill2;
    private Button buttonSkill3;
    private Button buttonSkill4;
    private Button buttonSkill5;
    private Button buttonSkill6;
    private Button buttonCanSkill1;
    private Button buttonCanSkill2;
    private Button buttonCanSkill3;
    private Button buttonCanSkill4;
    private Button buttonCanSkill5;
    private Button buttonCanSkill6;
    private ComboViewer comboSkillViewer1;
    private ComboViewer comboSkillViewer2;
    private ComboViewer comboSkillViewer3;
    private ComboViewer comboSkillViewer4;
    private ComboViewer comboSkillViewer5;
    private ComboViewer comboSkillViewer6;
    private Combo comboSkill1;
    private Combo comboSkill2;
    private Combo comboSkill3;
    private Combo comboSkill4;
    private Combo comboSkill5;
    private Combo comboSkill6;
    private ComboViewer comboSkillViewer7;
    private Combo comboSkill7;
    
    public static final String ID = "com.pip.sanguo.editor.attendant.AttendantTypeEditor"; //$NON-NLS-1$

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 9;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("名称：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("描述：");

        textDescription = new Text(container, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textDescription.setLayoutData(gd_textDescription);
        textDescription.addFocusListener(AutoSelectAll.instance);
        textDescription.addModifyListener(this);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("图片：");

        comboImage = new ComboViewer(container, SWT.READ_ONLY);
        comboImage.setContentProvider(new ContentProvider_1());
        comboImage.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
                updatePreviewer();
            }
        });
        comboImageCtrl = comboImage.getCombo();
        comboImageCtrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboImageCtrl.setVisibleItemCount(10);
        comboImage.setInput(EditorApplication.getProj());

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("性别：");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setItems(new String[] {"男", "女", "中性"});
        comboType.addModifyListener(this);
        comboType.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setDirty(true);
            }
        });

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setLayoutData(new GridData());
        label_5.setText("品质：");

        String[] items = Constants.ATTENDANTQUALITY;

        comboQuality = new Combo(container, SWT.READ_ONLY);
        comboQuality.setVisibleItemCount(50);
        comboQuality.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboQuality.setItems(items);
        comboQuality.addModifyListener(this);
        comboQuality.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setQuality(comboQuality.getSelectionIndex());
                setDirty(true);
            }
        });
        
        new Label(container, SWT.NONE);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        label_6.setText("生命：");

        textHP = new Text(container, SWT.BORDER);
        textHP.setEditable(false);
        final GridData gd_textHP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHP.setLayoutData(gd_textHP);
        textHP.addFocusListener(AutoSelectAll.instance);
        textHP.addModifyListener(this);

        textHP2 = new Text(container, SWT.BORDER);
        final GridData gd_textHP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHP2.setLayoutData(gd_textHP2);
        textHP2.addFocusListener(AutoSelectAll.instance);
        textHP2.addModifyListener(this);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setText("法力：");

        textMP = new Text(container, SWT.BORDER);
        textMP.setEditable(false);
        final GridData gd_textMP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMP.setLayoutData(gd_textMP);
        textMP.addFocusListener(AutoSelectAll.instance);
        textMP.addModifyListener(this);

        textMP2 = new Text(container, SWT.BORDER);
        final GridData gd_textMP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMP2.setLayoutData(gd_textMP2);
        textMP2.addFocusListener(AutoSelectAll.instance);
        textMP2.addModifyListener(this);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setText("护甲：");

        textArmor = new Text(container, SWT.BORDER);
        textArmor.setEditable(false);
        final GridData gd_textArmor = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArmor.setLayoutData(gd_textArmor);
        textArmor.addFocusListener(AutoSelectAll.instance);
        textArmor.addModifyListener(this);

        textArmor2 = new Text(container, SWT.BORDER);
        final GridData gd_textArmor2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArmor2.setLayoutData(gd_textArmor2);
        textArmor2.addFocusListener(AutoSelectAll.instance);
        textArmor2.addModifyListener(this);

        final Label label_9 = new Label(container, SWT.NONE);
        label_9.setLayoutData(new GridData());
        label_9.setText("力量：");

        textSTR = new Text(container, SWT.BORDER);
        textSTR.setEditable(false);
        final GridData gd_textSTR = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTR.setLayoutData(gd_textSTR);
        textSTR.addFocusListener(AutoSelectAll.instance);
        textSTR.addModifyListener(this);

        textSTR2 = new Text(container, SWT.BORDER);
        final GridData gd_textSTR2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTR2.setLayoutData(gd_textSTR2);
        textSTR2.addFocusListener(AutoSelectAll.instance);
        textSTR2.addModifyListener(this);

        final Label label_10 = new Label(container, SWT.NONE);
        label_10.setLayoutData(new GridData());
        label_10.setText("耐力：");

        textSTA = new Text(container, SWT.BORDER);
        textSTA.setEditable(false);
        final GridData gd_textSTA = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTA.setLayoutData(gd_textSTA);
        textSTA.addFocusListener(AutoSelectAll.instance);
        textSTA.addModifyListener(this);

        textSTA2 = new Text(container, SWT.BORDER);
        final GridData gd_textSTA2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTA2.setLayoutData(gd_textSTA2);
        textSTA2.addFocusListener(AutoSelectAll.instance);
        textSTA2.addModifyListener(this);
        
        final Label label_11 = new Label(container, SWT.NONE);
        label_11.setText("法防：");

        textMagicArmor = new Text(container, SWT.BORDER);
        textMagicArmor.setEditable(false);
        final GridData gd_textMagicArmor = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicArmor.setLayoutData(gd_textMagicArmor);
        textMagicArmor.addFocusListener(AutoSelectAll.instance);
        textMagicArmor.addModifyListener(this);

        textMagicArmor2 = new Text(container, SWT.BORDER);
        final GridData gd_textMagicArmor2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicArmor2.setLayoutData(gd_textMagicArmor2);
        textMagicArmor2.addFocusListener(AutoSelectAll.instance);
        textMagicArmor2.addModifyListener(this);

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setLayoutData(new GridData());
        label_12.setText("敏捷：");

        textAGI = new Text(container, SWT.BORDER);
        textAGI.setEditable(false);
        final GridData gd_textAGI = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAGI.setLayoutData(gd_textAGI);
        textAGI.addFocusListener(AutoSelectAll.instance);
        textAGI.addModifyListener(this);

        textAGI2 = new Text(container, SWT.BORDER);
        final GridData gd_textAGI2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAGI2.setLayoutData(gd_textAGI2);
        textAGI2.addFocusListener(AutoSelectAll.instance);
        textAGI2.addModifyListener(this);

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setText("武器物攻：");

        textPhyAP = new Text(container, SWT.BORDER);
        textPhyAP.setEditable(false);
        final GridData gd_textPhyAP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPhyAP.setLayoutData(gd_textPhyAP);
        textPhyAP.addFocusListener(AutoSelectAll.instance);
        textPhyAP.addModifyListener(this);

        textPhyAP2 = new Text(container, SWT.BORDER);
        final GridData gd_textPhyAP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPhyAP2.setLayoutData(gd_textPhyAP2);
        textPhyAP2.addFocusListener(AutoSelectAll.instance);
        textPhyAP2.addModifyListener(this);

        final Label label_14 = new Label(container, SWT.NONE);
        label_14.setText("武器法攻：");

        textMagicAP = new Text(container, SWT.BORDER);
        textMagicAP.setEditable(false);
        final GridData gd_textMagicAP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicAP.setLayoutData(gd_textMagicAP);
        textMagicAP.addFocusListener(AutoSelectAll.instance);
        textMagicAP.addModifyListener(this);

        textMagicAP2 = new Text(container, SWT.BORDER);
        final GridData gd_textMagicAP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicAP2.setLayoutData(gd_textMagicAP2);
        textMagicAP2.addFocusListener(AutoSelectAll.instance);
        textMagicAP2.addModifyListener(this);
        
        final Label label_15 = new Label(container, SWT.NONE);
        label_15.setLayoutData(new GridData());
        label_15.setText("智力：");

        textINT = new Text(container, SWT.BORDER);
        textINT.setEditable(false);
        final GridData gd_textINT = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textINT.setLayoutData(gd_textINT);
        textINT.addFocusListener(AutoSelectAll.instance);
        textINT.addModifyListener(this);

        textINT2 = new Text(container, SWT.BORDER);
        final GridData gd_textINT2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textINT2.setLayoutData(gd_textINT2);
        textINT2.addFocusListener(AutoSelectAll.instance);
        textINT2.addModifyListener(this);
        
        final Label label_16 = new Label(container, SWT.NONE);
        label_16.setText("物理暴击率：");
        
        critical = new Text(container, SWT.BORDER);
        critical.setEditable(false);
        final GridData gd_critical = new GridData(SWT.FILL, SWT.CENTER, true, false);
        critical.setLayoutData(gd_critical);
        critical.addFocusListener(AutoSelectAll.instance);
        critical.addModifyListener(this);
        
        critical2 = new Text(container, SWT.BORDER);
        final GridData gd_critical2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        critical2.setLayoutData(gd_critical2);
        critical2.addFocusListener(AutoSelectAll.instance);
        critical2.addModifyListener(this);
        
        final Label label_17 = new Label(container, SWT.NONE);
        label_17.setText("法术暴击率：");
        
        spellcritical = new Text(container, SWT.BORDER);
        spellcritical.setEditable(false);
        final GridData gd_spellcritical = new GridData(SWT.FILL, SWT.CENTER, true, false);
        spellcritical.setLayoutData(gd_spellcritical);
        spellcritical.addFocusListener(AutoSelectAll.instance);
        spellcritical.addModifyListener(this);
        
        spellcritical2 = new Text(container, SWT.BORDER);
        final GridData gd_spellcritical2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        spellcritical2.setLayoutData(gd_spellcritical2);
        spellcritical2.addFocusListener(AutoSelectAll.instance);
        spellcritical2.addModifyListener(this);
        
        final Label label_18 = new Label(container, SWT.NONE);
        label_18.setText("免暴率：");
        
        decritical = new Text(container, SWT.BORDER);
        decritical.setEditable(false);
        final GridData gd_decritical = new GridData(SWT.FILL, SWT.CENTER, true, false);
        decritical.setLayoutData(gd_decritical);
        decritical.addFocusListener(AutoSelectAll.instance);
        decritical.addModifyListener(this);
        
        decritical2 = new Text(container, SWT.BORDER);
        final GridData gd_decritical2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        decritical2.setLayoutData(gd_decritical2);
        decritical2.addFocusListener(AutoSelectAll.instance);
        decritical2.addModifyListener(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Label label_19 = new Label(container, SWT.NONE);
        label_19.setText("技能槽：");
        
        buttonSkill1 = new Button(container, SWT.CHECK);
        final GridData gd_buttonPartyModel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonSkill1.setLayoutData(gd_buttonPartyModel);
        buttonSkill1.setText("是否激活");
        buttonSkill1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).skillSwitch[0] = buttonSkill1.getSelection();
                setDirty(true);
            }
        });
        
        buttonCanSkill1 = new Button(container, SWT.CHECK);
        final GridData gd_buttonCanPartyModel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonCanSkill1.setLayoutData(gd_buttonCanPartyModel);
        buttonCanSkill1.setText("是否可激活");
        buttonCanSkill1.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).canActive[0] = buttonCanSkill1.getSelection();
                setDirty(true);
            }
        });
        
        final Label label_20 = new Label(container, SWT.NONE);
        label_20.setText("技能：");
        comboSkillViewer1 = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer1.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer1.setContentProvider(new ContentProvider());
        comboSkill1 = comboSkillViewer1.getCombo();
        comboSkill1.setVisibleItemCount(30);
        final GridData gd_comboSkill = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboSkill1.setLayoutData(gd_comboSkill);
        comboSkillViewer1.setInput(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Label label_21 = new Label(container, SWT.NONE);
        label_21.setText("技能槽：");
        
        buttonSkill2 = new Button(container, SWT.CHECK);
        final GridData gd_buttonPartyModel1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonSkill2.setLayoutData(gd_buttonPartyModel1);
        buttonSkill2.setText("是否激活");
        buttonSkill2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).skillSwitch[1] = buttonSkill2.getSelection();
                setDirty(true);
            }
        });
        
        buttonCanSkill2 = new Button(container, SWT.CHECK);
        final GridData gd_buttonCanPartyModel1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonCanSkill2.setLayoutData(gd_buttonCanPartyModel1);
        buttonCanSkill2.setText("是否可激活");
        buttonCanSkill2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).canActive[1] = buttonCanSkill2.getSelection();
                setDirty(true);
            }
        });
        
        final Label label_22 = new Label(container, SWT.NONE);
        label_22.setText("技能：");
        comboSkillViewer2 = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer2.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer2.setContentProvider(new ContentProvider());
        comboSkill2 = comboSkillViewer2.getCombo();
        comboSkill2.setVisibleItemCount(30);
        final GridData gd_comboSkill1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboSkill2.setLayoutData(gd_comboSkill1);
        comboSkillViewer2.setInput(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Label label_23 = new Label(container, SWT.NONE);
        label_23.setText("技能槽：");
        
        buttonSkill3 = new Button(container, SWT.CHECK);
        final GridData gd_buttonPartyModel2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonSkill3.setLayoutData(gd_buttonPartyModel2);
        buttonSkill3.setText("是否激活");
        buttonSkill3.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).skillSwitch[2] = buttonSkill3.getSelection();
                setDirty(true);
            }
        });
        
        buttonCanSkill3 = new Button(container, SWT.CHECK);
        final GridData gd_buttonCanPartyModel2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonCanSkill3.setLayoutData(gd_buttonCanPartyModel2);
        buttonCanSkill3.setText("是否可激活");
        buttonCanSkill3.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).canActive[2] = buttonCanSkill3.getSelection();
                setDirty(true);
            }
        });
        
        final Label label_24 = new Label(container, SWT.NONE);
        label_24.setText("技能：");
        comboSkillViewer3 = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer3.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer3.setContentProvider(new ContentProvider());
        comboSkill3 = comboSkillViewer3.getCombo();
        comboSkill3.setVisibleItemCount(30);
        final GridData gd_comboSkill2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboSkill3.setLayoutData(gd_comboSkill2);
        comboSkillViewer3.setInput(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Label label_28 = new Label(container, SWT.NONE);
        label_28.setText("技能槽：");
        
        buttonSkill4 = new Button(container, SWT.CHECK);
        final GridData gd_buttonPartyModel3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonSkill4.setLayoutData(gd_buttonPartyModel3);
        buttonSkill4.setText("是否激活");
        buttonSkill4.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).skillSwitch[3] = buttonSkill4.getSelection();
                setDirty(true);
            }
        });
        
        buttonCanSkill4 = new Button(container, SWT.CHECK);
        final GridData gd_buttonCanPartyModel3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonCanSkill4.setLayoutData(gd_buttonCanPartyModel3);
        buttonCanSkill4.setText("是否可激活");
        buttonCanSkill4.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).canActive[3] = buttonCanSkill4.getSelection();
                setDirty(true);
            }
        });
        
        final Label label_29 = new Label(container, SWT.NONE);
        label_29.setText("技能：");
        comboSkillViewer4 = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer4.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer4.setContentProvider(new ContentProvider());
        comboSkill4 = comboSkillViewer4.getCombo();
        comboSkill4.setVisibleItemCount(30);
        final GridData gd_comboSkill3 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboSkill4.setLayoutData(gd_comboSkill3);
        comboSkillViewer4.setInput(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Label label_30 = new Label(container, SWT.NONE);
        label_30.setText("技能槽：");
        
        buttonSkill5 = new Button(container, SWT.CHECK);
        final GridData gd_buttonPartyModel4 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonSkill5.setLayoutData(gd_buttonPartyModel4);
        buttonSkill5.setText("是否激活");
        buttonSkill5.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).skillSwitch[4] = buttonSkill5.getSelection();
                setDirty(true);
            }
        });
        
        buttonCanSkill5 = new Button(container, SWT.CHECK);
        final GridData gd_buttonCanPartyModel4 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonCanSkill5.setLayoutData(gd_buttonCanPartyModel4);
        buttonCanSkill5.setText("是否可激活");
        buttonCanSkill5.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).canActive[4] = buttonCanSkill5.getSelection();
                setDirty(true);
            }
        });
        
        final Label label_31 = new Label(container, SWT.NONE);
        label_31.setText("技能：");
        comboSkillViewer5 = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer5.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer5.setContentProvider(new ContentProvider());
        comboSkill5 = comboSkillViewer5.getCombo();
        comboSkill5.setVisibleItemCount(30);
        final GridData gd_comboSkill4 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboSkill5.setLayoutData(gd_comboSkill4);
        comboSkillViewer5.setInput(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Label label_32 = new Label(container, SWT.NONE);
        label_32.setText("技能槽：");
        
        buttonSkill6 = new Button(container, SWT.CHECK);
        final GridData gd_buttonPartyModel5 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonSkill6.setLayoutData(gd_buttonPartyModel5);
        buttonSkill6.setText("是否激活");
        buttonSkill6.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).skillSwitch[5] = buttonSkill6.getSelection();
                setDirty(true);
            }
        });
        
        buttonCanSkill6 = new Button(container, SWT.CHECK);
        final GridData gd_buttonCanPartyModel5 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        buttonCanSkill6.setLayoutData(gd_buttonCanPartyModel5);
        buttonCanSkill6.setText("是否可激活");
        buttonCanSkill6.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((AttendantType)getEditObject()).canActive[5] = buttonCanSkill6.getSelection();
                setDirty(true);
            }
        });
        
        final Label label_33 = new Label(container, SWT.NONE);
        label_33.setText("技能：");
        comboSkillViewer6 = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer6.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer6.setContentProvider(new ContentProvider());
        comboSkill6 = comboSkillViewer6.getCombo();
        comboSkill6.setVisibleItemCount(30);
        final GridData gd_comboSkill5 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboSkill6.setLayoutData(gd_comboSkill5);
        comboSkillViewer6.setInput(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        final Label label_25 = new Label(container, SWT.NONE);
        label_25.setText("发呆间歇(毫秒)：");
        textduration = new Text(container, SWT.BORDER);
        final GridData gd_textDuration = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textduration.setLayoutData(gd_textDuration);
        textduration.addFocusListener(AutoSelectAll.instance);
        textduration.addModifyListener(this);
        
        final Label label_26 = new Label(container, SWT.NONE);
        label_26.setText("生命链接系数（%）：");
        textdistance = new Text(container, SWT.BORDER);
        final GridData gd_textDistance = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textdistance.setLayoutData(gd_textDistance);
        textdistance.addFocusListener(AutoSelectAll.instance);
        textdistance.addModifyListener(this);
        
        final Label label_27 = new Label(container, SWT.NONE);
        label_27.setText("食量(秒)：");
        texteat = new Text(container, SWT.BORDER);
        final GridData gd_textEate = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        texteat.setLayoutData(gd_textEate);
        texteat.addFocusListener(AutoSelectAll.instance);
        texteat.addModifyListener(this);
        
        final Label label_50 = new Label(container, SWT.NONE);
        label_50.setLayoutData(new GridData());
        label_50.setText("普攻类型：");

        String[] skills = Constants.ATTENDANSKILL;

        comboSkill = new Combo(container, SWT.READ_ONLY);
        comboSkill.setVisibleItemCount(50);
        comboSkill.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboSkill.setItems(skills);
        comboSkill.addModifyListener(this);
        comboSkill.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setSkill(comboSkill.getSelectionIndex());
                setDirty(true);
            }
        });
        
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
//        new Label(container, SWT.NONE);
        
        final Label label_51 = new Label(container, SWT.NONE);
        label_51.setText("特殊技能：");
        comboSkillViewer7 = new ComboViewer(container, SWT.READ_ONLY);
        comboSkillViewer7.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
            }
        });
        comboSkillViewer7.setContentProvider(new ContentProvider());
        comboSkill7 = comboSkillViewer7.getCombo();
        comboSkill7.setVisibleItemCount(30);
        final GridData gd_comboSkill7 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboSkill7.setLayoutData(gd_comboSkill7);
        comboSkillViewer7.setInput(this);
        
        final Group groupPreview = new Group(container, SWT.NONE);
        groupPreview.setText("预览");
        final GridData gd_groupPreview = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 2);
        gd_groupPreview.widthHint = 254;
        gd_groupPreview.heightHint = 254;
        groupPreview.setLayoutData(gd_groupPreview);
        groupPreview.setLayout(new FillLayout());
        
        previewer = new AnimatePreviewer(groupPreview, SWT.NONE);
        previewer.setListVisible(false);
        previewer.setEditEnable(false);
        
        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 5;
        composite.setLayout(gridLayout_1);
        
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    private void setQuality(int qualityIndex){
        int quality = qualityIndex + 1;
        AttendantType dataDef = (AttendantType)editObject;
        dataDef.qulity = quality;
        dataDef.initData();
        updateView();
    }
    
    private void setSkill(int skillIndex){
        int skill = skillIndex;
        AttendantType dataDef = (AttendantType)editObject;
        dataDef.skillType = skill;
        dataDef.initData();
        updateView();
    }
    
    private void updatePreviewer() {
        StructuredSelection sel = (StructuredSelection)comboImage.getSelection();
        Animation imgDef = (Animation)sel.getFirstElement();
        if (imgDef != null) {
            previewer.setAnimateFile(imgDef.source);
        }
    }
    
    private void updateView() {
        // 设置初始值
        AttendantType dataDef = (AttendantType)editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDescription.setText(dataDef.description);
        comboQuality.select(dataDef.qulity-1<0 ? 0 : dataDef.qulity-1);
        comboSkill.select(dataDef.skillType<0 ? 0 : dataDef.skillType);
        if (dataDef.image != null) {
            comboImageCtrl.select(dataDef.owner.getObjectIndex(dataDef.image));
        }
        if (dataDef.skillId[0] == 0) {
            comboSkill1.select(0);
        } else {
            SkillConfig skill = (SkillConfig)dataDef.owner.findObject(SkillConfig.class, dataDef.skillId[0]);
            if (skill == null) {
                comboSkill1.select(0);
            } else {
                comboSkillViewer1.setSelection(new StructuredSelection(skill));
            }
        }
        if (dataDef.skillId[1] == 0) {
            comboSkill2.select(0);
        } else {
            SkillConfig skill = (SkillConfig)dataDef.owner.findObject(SkillConfig.class, dataDef.skillId[1]);
            if (skill == null) {
                comboSkill2.select(0);
            } else {
                comboSkillViewer2.setSelection(new StructuredSelection(skill));
            }
        }
        if (dataDef.skillId[2] == 0) {
            comboSkill3.select(0);
        } else {
            SkillConfig skill = (SkillConfig)dataDef.owner.findObject(SkillConfig.class, dataDef.skillId[2]);
            if (skill == null) {
                comboSkill3.select(0);
            } else {
                comboSkillViewer3.setSelection(new StructuredSelection(skill));
            }
        }
        if (dataDef.skillId[3] == 0) {
            comboSkill4.select(0);
        } else {
            SkillConfig skill = (SkillConfig)dataDef.owner.findObject(SkillConfig.class, dataDef.skillId[3]);
            if (skill == null) {
                comboSkill4.select(0);
            } else {
                comboSkillViewer4.setSelection(new StructuredSelection(skill));
            }
        }
        if (dataDef.skillId[4] == 0) {
            comboSkill5.select(0);
        } else {
            SkillConfig skill = (SkillConfig)dataDef.owner.findObject(SkillConfig.class, dataDef.skillId[4]);
            if (skill == null) {
                comboSkill5.select(0);
            } else {
                comboSkillViewer5.setSelection(new StructuredSelection(skill));
            }
        }
        if (dataDef.specialSkillIds[0]==0) {
            comboSkill7.select(0);
        } else {
            SkillConfig skill = (SkillConfig)dataDef.owner.findObject(SkillConfig.class, dataDef.specialSkillIds[0]);
            if (skill == null) {
                comboSkill7.select(0);
            } else {
                comboSkillViewer7.setSelection(new StructuredSelection(skill));
            }
        }
        
        comboType.select(dataDef.sex);
        textHP.setText(String.valueOf(dataDef.getStandardHP()));
        textHP2.setText(String.valueOf(dataDef.hp - dataDef.getStandardHP()));
        textMP.setText(String.valueOf(dataDef.getStandardMP()));
        textMP2.setText(String.valueOf(dataDef.mp - dataDef.getStandardMP()));
        textArmor.setText(String.valueOf(dataDef.getStandardArmor()));
        textArmor2.setText(String.valueOf(dataDef.armor - dataDef.getStandardArmor()));
        textMagicArmor.setText(String.valueOf(dataDef.getStandardMagicArmor()));
        textMagicArmor2.setText(String.valueOf(dataDef.magicArmor - dataDef.getStandardMagicArmor()));
        textSTR.setText(String.valueOf(dataDef.getStandardSTR()));
        textSTR2.setText(String.valueOf(dataDef.str - dataDef.getStandardSTR()));
        textSTA.setText(String.valueOf(dataDef.getStandardSTA()));
        textSTA2.setText(String.valueOf(dataDef.sta - dataDef.getStandardSTA()));
        textINT.setText(String.valueOf(dataDef.getStandardINT()));
        textINT2.setText(String.valueOf(dataDef.inte - dataDef.getStandardINT()));
        textAGI.setText(String.valueOf(dataDef.getStandardAGI()));
        textAGI2.setText(String.valueOf(dataDef.agi - dataDef.getStandardAGI()));
        textPhyAP.setText(dataDef.getStandardWeaponAP1() + "-" + dataDef.getStandardWeaponAP2());
        textPhyAP2.setText(String.valueOf(dataDef.weaponAP2 - dataDef.getStandardWeaponAP2()));
        textMagicAP.setText(String.valueOf(dataDef.getStandardWeaponMagicAP()));
        textMagicAP2.setText(String.valueOf(dataDef.weaponMagicAP - dataDef.getStandardWeaponMagicAP()));
        critical.setText(String.valueOf(dataDef.getStandardCritical()));
        critical2.setText(String.valueOf(dataDef.critical-dataDef.getStandardCritical()));
        spellcritical.setText(String.valueOf(dataDef.getStandardSpellcritical()));
        spellcritical2.setText(String.valueOf(dataDef.spellcritical-dataDef.getStandardSpellcritical()));
        decritical.setText(String.valueOf(dataDef.getStandardDecritical()));
        decritical2.setText(String.valueOf(dataDef.decritical-dataDef.getStandardDecritical()));
        textduration.setText(String.valueOf(dataDef.duration));
        textdistance.setText(String.valueOf(dataDef.distance));
        texteat.setText(String.valueOf(dataDef.eat));
        buttonSkill1.setSelection(dataDef.skillSwitch[0]);
        buttonSkill2.setSelection(dataDef.skillSwitch[1]);
        buttonSkill3.setSelection(dataDef.skillSwitch[2]);
        buttonSkill4.setSelection(dataDef.skillSwitch[3]);
        buttonSkill5.setSelection(dataDef.skillSwitch[4]);
        buttonSkill6.setSelection(dataDef.skillSwitch[5]);
        buttonCanSkill1.setSelection(dataDef.canActive[0]);
        buttonCanSkill2.setSelection(dataDef.canActive[1]);
        buttonCanSkill3.setSelection(dataDef.canActive[2]);
        buttonCanSkill4.setSelection(dataDef.canActive[3]);
        buttonCanSkill5.setSelection(dataDef.canActive[4]);
        buttonCanSkill6.setSelection(dataDef.canActive[5]);
        updatePreviewer();
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        AttendantType dataDef = (AttendantType)editObject;
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDescription.getText();
        try {       
            StructuredSelection sel = (StructuredSelection)comboImage.getSelection();
            dataDef.image = (Animation)sel.getFirstElement();
            if (dataDef.image == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception("请选择一个图片。");
        }
        dataDef.sex = comboType.getSelection().x;
        try {
            dataDef.hp = dataDef.getStandardHP() + Integer.parseInt(textHP2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的生命。");
        }
        try {
            dataDef.mp = dataDef.getStandardMP() + Integer.parseInt(textMP2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的法力。");
        }
        try {
            dataDef.armor = dataDef.getStandardArmor() + Integer.parseInt(textArmor2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的法力。");
        }
        try {
            dataDef.magicArmor = dataDef.getStandardMagicArmor() + Integer.parseInt(textMagicArmor2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的法力。");
        }
        try {
            dataDef.str = dataDef.getStandardSTR() + Integer.parseInt(textSTR2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的力量。");
        }
        try {
            dataDef.sta = dataDef.getStandardSTA() + Integer.parseInt(textSTA2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的耐力。");
        }
        try {
            dataDef.agi = dataDef.getStandardAGI() + Integer.parseInt(textAGI2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的敏捷。");
        }
        try {
            dataDef.inte = dataDef.getStandardINT() + Integer.parseInt(textINT2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的智力。");
        }
        try {
            dataDef.weaponAP1 = dataDef.getStandardWeaponAP1() + Integer.parseInt(textPhyAP2.getText());
            dataDef.weaponAP2 = dataDef.getStandardWeaponAP2() + Integer.parseInt(textPhyAP2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的武器物理攻击力。");
        }
        try {
            dataDef.weaponMagicAP = dataDef.getStandardWeaponMagicAP() + Integer.parseInt(textMagicAP2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的武器法术攻击力。");
        }
        try {
            dataDef.critical = dataDef.getStandardCritical() + Integer.parseInt(critical2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的物理暴击。");
        }
        try {
            dataDef.spellcritical = dataDef.getStandardSpellcritical() + Integer.parseInt(spellcritical2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的法术暴击。");
        }
        try {
            dataDef.decritical = dataDef.getStandardDecritical() + Integer.parseInt(decritical2.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的免暴。");
        }
        try {
            dataDef.duration = Integer.parseInt(textduration.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的发呆时间。");
        }
        try {
            dataDef.distance = Integer.parseInt(textdistance.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的攻击距离。");
        }
        try {
            dataDef.eat = Integer.parseInt(texteat.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的食量。");
        }
        dataDef.sex = comboType.getSelectionIndex();
        dataDef.qulity = comboQuality.getSelectionIndex() + 1;
        dataDef.skillType = comboSkill.getSelectionIndex();
        dataDef.skillSwitch[0] = buttonSkill1.getSelection();
        dataDef.skillSwitch[1] = buttonSkill2.getSelection();
        dataDef.skillSwitch[2] = buttonSkill3.getSelection();
        dataDef.skillSwitch[3] = buttonSkill4.getSelection();
        dataDef.skillSwitch[4] = buttonSkill5.getSelection();
        dataDef.skillSwitch[5] = buttonSkill6.getSelection();
        dataDef.canActive[0] = buttonCanSkill1.getSelection();
        dataDef.canActive[1] = buttonCanSkill2.getSelection();
        dataDef.canActive[2] = buttonCanSkill3.getSelection();
        dataDef.canActive[3] = buttonCanSkill4.getSelection();
        dataDef.canActive[4] = buttonCanSkill5.getSelection();
        dataDef.canActive[5] = buttonCanSkill6.getSelection();
        StructuredSelection sel1 = (StructuredSelection)comboSkillViewer1.getSelection();
        Object skill1 = sel1.getFirstElement();
        if (skill1 instanceof SkillConfig) {
            try {
                dataDef.skillId[0] = ((SkillConfig)skill1).id;
            }
            catch (Exception e) {
                dataDef.skillId[0] = 0;
            }
        } else {
            dataDef.skillId[0] = 0;
        }
        StructuredSelection sel2 = (StructuredSelection)comboSkillViewer2.getSelection();
        Object skill2 = sel2.getFirstElement();
        if (skill2 instanceof SkillConfig) {
            try {
                dataDef.skillId[1] = ((SkillConfig)skill2).id;
            }
            catch (Exception e) {
                dataDef.skillId[1] = 0;
            }
        } else {
            dataDef.skillId[1] = 0;
        }
        StructuredSelection sel3 = (StructuredSelection)comboSkillViewer3.getSelection();
        Object skill3 = sel3.getFirstElement();
        if (skill3 instanceof SkillConfig) {
            try {
                dataDef.skillId[2] = ((SkillConfig)skill3).id;
            }
            catch (Exception e) {
                dataDef.skillId[2] = 0;
            }
        } else {
            dataDef.skillId[2] = 0;
        }
        StructuredSelection sel4 = (StructuredSelection)comboSkillViewer4.getSelection();
        Object skill4 = sel4.getFirstElement();
        if (skill3 instanceof SkillConfig) {
            try {
                dataDef.skillId[3] = ((SkillConfig)skill4).id;
            }
            catch (Exception e) {
                dataDef.skillId[3] = 0;
            }
        } else {
            dataDef.skillId[3] = 0;
        }
        StructuredSelection sel5 = (StructuredSelection)comboSkillViewer5.getSelection();
        Object skill5 = sel5.getFirstElement();
        if (skill3 instanceof SkillConfig) {
            try {
                dataDef.skillId[4] = ((SkillConfig)skill5).id;
            }
            catch (Exception e) {
                dataDef.skillId[4] = 0;
            }
        } else {
            dataDef.skillId[4] = 0;
        }
        StructuredSelection sel6 = (StructuredSelection)comboSkillViewer6.getSelection();
        Object skill6 = sel6.getFirstElement();
        if (skill6 instanceof SkillConfig) {
            try {
                dataDef.skillId[5] = ((SkillConfig)skill6).id;
            }
            catch (Exception e) {
                dataDef.skillId[5] = 0;
            }
        } else {
            dataDef.skillId[5] = 0;
        }
        StructuredSelection sel7 = (StructuredSelection)comboSkillViewer7.getSelection();
        Object skill7 = sel7.getFirstElement();
        if (skill7 instanceof SkillConfig) {
            try {
                dataDef.specialSkillIds[0] = ((SkillConfig)skill7).id;
            }
            catch (Exception e) {
                dataDef.specialSkillIds[0] = 0;
            }
        } else {
            dataDef.specialSkillIds[0] = 0;
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
    
}
