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
import com.pip.sanguo.data.quest.pqe.ExpressionList;

public class ExpressionDialog extends Dialog {
    private QuestDesigner questDesigner;
    private String expression;
    private QuestInfo questInfo;
    
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public ExpressionDialog(Shell parentShell, QuestInfo qinfo) {
        super(parentShell);
        questInfo = qinfo;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);

        questDesigner = new QuestDesigner(container, SWT.NONE, questInfo);
        questDesigner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        questDesigner.setup(1, expression);
        
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
        return new Point(1004, 633);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("条件编辑器");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            expression = questDesigner.saveCondition();
        }
        super.buttonPressed(buttonId);
    }
    
    public static String open(Shell parentShell, String value, QuestInfo qinfo) {
        ExpressionDialog dlg = new ExpressionDialog(parentShell, qinfo);
        dlg.setExpression(value);
        if (dlg.open() == OK) {
            return dlg.getExpression();
        } else {
            return null;
        }
    }
}
