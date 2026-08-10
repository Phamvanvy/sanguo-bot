package com.pip.sanguo.editor.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
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
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.AutoSelectAll;

public class EditGameMapExitConstraintsDialog extends Dialog {
    private Combo comboFaction;
    private Text textRequirePropertyValue;
    private Text textRequireProperty;
    class RankListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            List list = new ArrayList();
            list.add("无");
            list.addAll(EditorApplication.getProj().getDictDataListByType(Rank.class));
            return list.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private Combo rankCombo;
    private Text textMaxLevel;
    private Text textMinLevel;
    
    private GameMapExitConstraints editObject;
    private ComboViewer rankViewer;
    private Button buttonAllowBattle;
    private Label labelRequireQuest;
    private Label labelRequireFinishQuest;
    
    private static String[] FAC_NAMES = new String[] {"不限制","魏国","蜀国","吴国","非魏国","非蜀国","非吴国"};
    private static int[] FAC_IDS = new int[] { -1, 1, 2, 3, -2, -3, -4 };
    
    public void setEditObject(GameMapExitConstraints obj) {
        editObject = obj;
    }
    
    public GameMapExitConstraints getEditObject() {
        return editObject;
    }
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public EditGameMapExitConstraintsDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setText("限制阵营：");

        comboFaction = new Combo(container, SWT.READ_ONLY);
        comboFaction.setVisibleItemCount(10);
        comboFaction.setItems(FAC_NAMES);
        final GridData gd_comboFaction = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboFaction.setLayoutData(gd_comboFaction);
        for (int i = 0; i < FAC_IDS.length; i++) {
            if (FAC_IDS[i] == editObject.allowFaction) {
                comboFaction.select(i);
            }
        }
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label = new Label(container, SWT.NONE);
        label.setText("最小级别：");

        textMinLevel = new Text(container, SWT.BORDER);
        final GridData gd_textMinLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMinLevel.setLayoutData(gd_textMinLevel);
        textMinLevel.addFocusListener(AutoSelectAll.instance);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("最大级别：");

        textMaxLevel = new Text(container, SWT.BORDER);
        final GridData gd_textMaxLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMaxLevel.setLayoutData(gd_textMaxLevel);
        textMaxLevel.addFocusListener(AutoSelectAll.instance);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("最低军衔：");

        rankViewer = new ComboViewer(container, SWT.READ_ONLY);
        rankViewer.setContentProvider(new RankListContentProvider());
        rankCombo = rankViewer.getCombo();
        rankCombo.setVisibleItemCount(20);
        final GridData gd_rankCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);
        rankCombo.setLayoutData(gd_rankCombo);
        rankViewer.setInput(new Object());

        buttonAllowBattle = new Button(container, SWT.CHECK);
        final GridData gd_buttonAllowBattle = new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1);
        buttonAllowBattle.setLayoutData(gd_buttonAllowBattle);
        buttonAllowBattle.setText("允许战斗状态通过");

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("拥有任务：");

        labelRequireQuest = new Label(container, SWT.NONE);
        final GridData gd_labelRequireQuest = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        labelRequireQuest.setLayoutData(gd_labelRequireQuest);

        final Button button = new Button(container, SWT.NONE);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                ChooseQuestDialog dlg = new ChooseQuestDialog(getShell());
                if (dlg.open() == ChooseQuestDialog.OK) {
                    editObject.requireQuest = dlg.getSelectedQuest();
                    updateQuestName();
                }
            }
        });
        button.setText("选择...");
        
        textMinLevel.setText(String.valueOf(editObject.minLevel));
        textMaxLevel.setText(String.valueOf(editObject.maxLevel));
        if (editObject.minRank == -1) {
            rankCombo.select(0);
        } else {
            Rank rank = (Rank)EditorApplication.getProj().findDictObject(Rank.class, editObject.minRank);
            rankViewer.setSelection(new StructuredSelection(new Object[] { rank }));
        }
        buttonAllowBattle.setSelection(editObject.allowBattle);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("完成任务：");

        labelRequireFinishQuest = new Label(container, SWT.NONE);
        final GridData gd_labelRequireFinishQuest = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        labelRequireFinishQuest.setLayoutData(gd_labelRequireFinishQuest);
        labelRequireFinishQuest.setText("无");

        final Button button_1 = new Button(container, SWT.NONE);
        button_1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                ChooseQuestDialog dlg = new ChooseQuestDialog(getShell());
                if (dlg.open() == ChooseQuestDialog.OK) {
                    editObject.requireFinishQuest = dlg.getSelectedQuest();
                    updateQuestName();
                }
            }
        });
        button_1.setText("选择...");

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("检查属性：");

        textRequireProperty = new Text(container, SWT.BORDER);
        final GridData gd_textRequireProperty = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textRequireProperty.setLayoutData(gd_textRequireProperty);
        textRequireProperty.addFocusListener(AutoSelectAll.instance);
        textRequireProperty.setText(editObject.requireProperty);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("达到");

        textRequirePropertyValue = new Text(container, SWT.BORDER);
        final GridData gd_textRequirePropertyValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textRequirePropertyValue.setLayoutData(gd_textRequirePropertyValue);
        textRequirePropertyValue.addFocusListener(AutoSelectAll.instance);
        textRequirePropertyValue.setText(String.valueOf(editObject.requirePropertyValue));
        
        updateQuestName();

        return container;
    }
    
    private void updateQuestName() {
        if (editObject.requireQuest == -1) {
            labelRequireQuest.setText("无");
        } else {
            Quest quest = (Quest)EditorApplication.getProj().findObject(Quest.class, editObject.requireQuest);
            if (quest == null) {
                labelRequireQuest.setText("无效任务");
            } else {
                labelRequireQuest.setText(quest.toString());
            }
        }
        if (editObject.requireFinishQuest == -1) {
            labelRequireFinishQuest.setText("无");
        } else {
            Quest quest = (Quest)EditorApplication.getProj().findObject(Quest.class, editObject.requireFinishQuest);
            if (quest == null) {
                labelRequireFinishQuest.setText("无效任务");
            } else {
                labelRequireFinishQuest.setText(quest.toString());
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
    protected Point getInitialSize() {
        return new Point(581, 364);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("出口限制");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            editObject.allowFaction = FAC_IDS[comboFaction.getSelectionIndex()];
            try {
                editObject.minLevel = Integer.parseInt(textMinLevel.getText());
            } catch (Exception e) {
                MessageDialog.openError(getShell(), "错误", "请输入正确的最小级别。");
                return;
            }
            try {
                editObject.maxLevel = Integer.parseInt(textMaxLevel.getText());
            } catch (Exception e) {
                MessageDialog.openError(getShell(), "错误", "请输入正确的最大级别。");
                return;
            }
            StructuredSelection sel = (StructuredSelection)rankViewer.getSelection();
            if (sel.isEmpty()) {
                editObject.minRank = -1;
            } else {
                Object obj = sel.getFirstElement();
                if (obj instanceof Rank) {
                    editObject.minRank = ((Rank)obj).id;
                } else {
                    editObject.minRank = -1;
                }
            }
            editObject.allowBattle = buttonAllowBattle.getSelection();
            editObject.requireProperty = textRequireProperty.getText();
            if (editObject.requireProperty.length() > 0 && 
                    !editObject.requireProperty.startsWith("__PLAYER_") && 
                    !editObject.requireProperty.startsWith("__TONG_") && 
                    !editObject.requireProperty.startsWith("__FACTION_") && 
                    !editObject.requireProperty.startsWith("__WORLD_") && 
                    !editObject.requireProperty.startsWith("__PARTY_")) {
                MessageDialog.openError(getShell(), "错误", "可使用的合法属性包括：__PLAYER_xxx，__TONG_xxx，__FACTION_xxx，__WORLD_xxx，__PARTY_xxx");
                return;
            }
            try {
                editObject.requirePropertyValue = Integer.parseInt(textRequirePropertyValue.getText());
            } catch (Exception e) {
                MessageDialog.openError(getShell(), "错误", "请输入正确的数值。");
                return;
            }
        }
        super.buttonPressed(buttonId);
    }
}
