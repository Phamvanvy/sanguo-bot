package com.pip.sanguo.editor.item;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.util.AutoSelectAll;

public class EditDropItemCountDialog extends Dialog {

    private Text textMaxCount;
    private Text textMinCount;
    
    public int minCount;
    public int maxCount;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public EditDropItemCountDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("掉落数量：");

        textMinCount = new Text(container, SWT.BORDER);
        final GridData gd_textMinCount = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMinCount.setLayoutData(gd_textMinCount);
        textMinCount.addFocusListener(AutoSelectAll.instance);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("-");

        textMaxCount = new Text(container, SWT.BORDER);
        final GridData gd_textMaxCount = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMaxCount.setLayoutData(gd_textMaxCount);
        textMaxCount.addFocusListener(AutoSelectAll.instance);
        
        textMaxCount.setText(String.valueOf(maxCount));
        textMinCount.setText(String.valueOf(minCount));
        
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
        return new Point(307, 160);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("编辑掉落项目");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            try {
                minCount = Integer.parseInt(textMinCount.getText());
                if (minCount < 0) {
                    throw new Exception();
                }
            } catch (Exception e) {
                MessageDialog.openError(getShell(), "错误", "最小数量输入不正确。");
                return;
            }
            try {
                maxCount = Integer.parseInt(textMaxCount.getText());
                if (maxCount < 0) {
                    throw new Exception();
                }
            } catch (Exception e) {
                MessageDialog.openError(getShell(), "错误", "最大数量输入不正确。");
                return;
            }
            if (minCount > maxCount) {
                MessageDialog.openError(getShell(), "错误", "最小数量不能超过最大数量。");
                return;
            }
        }
        super.buttonPressed(buttonId);
    }
}
