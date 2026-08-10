package com.pip.sanguo.editor.skill;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.map.GameMapExitConstraints;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.EffectConfig;
import com.pip.sanguo.data.skill.EffectConfigSet;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.AutoSelectAll;

public class EditParamIndicatorDialog extends Dialog {
    private Combo comboParam;
    private Combo comboType;
    
    class SkillBuffListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (editObject.type == ParamIndicator.TYPE_BUFF_OWNER) {
                return EditorApplication.getProj().getDataListByType(BuffConfig.class).toArray();
            } else if (editObject.type == ParamIndicator.TYPE_BUFF_SOURCE) {
                List<DataObject> allSkills = EditorApplication.getProj().getDataListByType(BuffConfig.class);
                List<BuffConfig> ret = new ArrayList<BuffConfig>();
                for (DataObject dobj : allSkills) {
                    BuffConfig buff = (BuffConfig)dobj;
                    if (buff.buffType == BuffConfig.BUFF_TYPE_DYNAMIC) {
                        ret.add(buff);
                    }
                }
                return ret.toArray();
            } else {
                List<DataObject> allSkills = EditorApplication.getProj().getDataListByType(SkillConfig.class);
                List<SkillConfig> ret = new ArrayList<SkillConfig>();
                for (DataObject dobj : allSkills) {
                    SkillConfig skill = (SkillConfig)dobj;
                    if (skill.type == SkillConfig.TYPE_ATTACK || skill.type == SkillConfig.TYPE_AID || skill.type == SkillConfig.TYPE_RELIVE) {
                        ret.add(skill);
                    }
                }
                return ret.toArray();
            }
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private Combo skillCombo;
    
    private ParamIndicator editObject = new ParamIndicator();
    private ComboViewer skillViewer;
    
    public void setEditObject(ParamIndicator obj) {
        editObject.update(obj);
    }
    
    public ParamIndicator getEditObject() {
        return editObject;
    }
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public EditParamIndicatorDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("作用范围：");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                editObject.type = comboType.getSelectionIndex();
                skillViewer.refresh();
                skillCombo.select(0);
            }
        });
        comboType.setItems(new String[] {"施放技能时", "受到技能攻击/治疗时", "被加BUFF时", "给别人加BUFF时"});
        final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboType.setLayoutData(gd_comboType);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("技能/BUFF：");

        skillViewer = new ComboViewer(container, SWT.READ_ONLY);
        skillViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                StructuredSelection sel = (StructuredSelection)skillViewer.getSelection();
                if (sel.isEmpty()) {
                    comboParam.setItems(new String[0]);
                    return;
                }
                EffectConfigSet ecs;
                if (sel.getFirstElement() instanceof SkillConfig) {
                    ecs = ((SkillConfig)sel.getFirstElement()).effects;
                    editObject.id = ((SkillConfig)sel.getFirstElement()).id;
                    String[] paramNames = new String[ecs.getAllParams().size()];
                    for (int i = 0; i < paramNames.length; i++) {
                        paramNames[i] = ecs.getParamAt(i).getParamName();
                    }
                    comboParam.setItems(paramNames);
                } else {
                    ecs = ((BuffConfig)sel.getFirstElement()).effects;
                    editObject.id = ((BuffConfig)sel.getFirstElement()).id;
                    EffectConfig gc = ((BuffConfig)sel.getFirstElement()).getGeneralConfig();
                    String[] paramNames = new String[ecs.getAllParams().size() + gc.getParamCount()];
                    for (int i = 0; i < gc.getParamCount(); i++) {
                        paramNames[i] = gc.getParamName(i);
                    }
                    for (int i = gc.getParamCount(); i < paramNames.length; i++) {
                        paramNames[i] = ecs.getParamAt(i - gc.getParamCount()).getParamName();
                    }
                    comboParam.setItems(paramNames);
                }
            }
        });
        skillViewer.setContentProvider(new SkillBuffListContentProvider());
        skillCombo = skillViewer.getCombo();
        skillCombo.setVisibleItemCount(40);
        final GridData gd_skillCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
        skillCombo.setLayoutData(gd_skillCombo);
        skillViewer.setInput(new Object());

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("影响参数：");

        comboParam = new Combo(container, SWT.READ_ONLY);
        comboParam.setVisibleItemCount(20);
        final GridData gd_comboParam = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboParam.setLayoutData(gd_comboParam);
        
        comboType.select(editObject.type);
        if (editObject.type == ParamIndicator.TYPE_BUFF_OWNER ||
                editObject.type == ParamIndicator.TYPE_BUFF_SOURCE) {
            BuffConfig bc = (BuffConfig)EditorApplication.getProj().findObject(BuffConfig.class, editObject.id);
            if (bc != null) {
                skillViewer.setSelection(new StructuredSelection(bc));
            }
        } else {
            SkillConfig sc = (SkillConfig)EditorApplication.getProj().findObject(SkillConfig.class, editObject.id);
            if (sc != null) {
                skillViewer.setSelection(new StructuredSelection(sc));
            }
        }
        if (editObject.paramIndex >= 0 && editObject.paramIndex < comboParam.getItemCount()) {
            comboParam.select(editObject.paramIndex);
        }
        
        return container;
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
    protected Point getInitialSize() {
        return new Point(373, 218);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("技能/BUFF参数选择");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            if (skillViewer.getSelection().isEmpty()) {
                MessageDialog.openError(getShell(), "错误", "必须选择一个技能/BUFF。");
                return;
            }
            if (comboParam.getSelectionIndex() == -1) {
                MessageDialog.openError(getShell(), "错误", "必须选择一个参数。");
                return;
            }
            editObject.paramIndex = comboParam.getSelectionIndex();
        }
        super.buttonPressed(buttonId);
    }
}
