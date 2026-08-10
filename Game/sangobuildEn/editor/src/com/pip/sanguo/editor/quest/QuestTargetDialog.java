package com.pip.sanguo.editor.quest;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.data.quest.pqe.ExpressionList;

public class QuestTargetDialog extends Dialog {
    private Text textDescription;
    private Text textHint;
    private QuestTarget target;
    private QuestDesigner questDesigner;
    private QuestInfo questInfo;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public QuestTargetDialog(Shell parentShell, QuestTarget target, QuestInfo qinfo) {
        super(parentShell);
        this.target = target;
        this.questInfo = qinfo;
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

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("描述：");

        textDescription = new Text(container, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textDescription.setLayoutData(gd_textDescription);
        
        textDescription.setText(target.description);

        final Button editTextButton = new Button(container, SWT.NONE);
        editTextButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                RichTextDialog dlg = new RichTextDialog(getShell(), questInfo);
                dlg.setText(textDescription.getText());
                if (dlg.open() == Dialog.OK) {
                    textDescription.setText(dlg.getText());
                }
            }
        });
        editTextButton.setText("...");

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("提示：");

        textHint = new Text(container, SWT.BORDER);
        final GridData gd_textHint = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHint.setLayoutData(gd_textHint);
        
        textHint.setText(target.hint);

        final Button editHintButton = new Button(container, SWT.NONE);
        editHintButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                RichTextDialog dlg = new RichTextDialog(getShell(), questInfo);
                dlg.setText(textHint.getText());
                if (dlg.open() == Dialog.OK) {
                    textHint.setText(dlg.getText());
                }
            }
        });
        editHintButton.setText("...");

        questDesigner = new QuestDesigner(container, SWT.NONE, questInfo);
        questDesigner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        questDesigner.setup(1, target.condition);
        
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(990, 610);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("任务目标");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	target.condition = questDesigner.saveCondition();
        	target.description = textDescription.getText();
        	target.hint = textHint.getText();
        }
        super.buttonPressed(buttonId);
    }
}
