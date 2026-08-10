package com.pip.sanguo.editor;

import java.io.File;
import java.text.DecimalFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChooseMatchFileDialog extends Dialog {
    public String newFileName;
    public String[] matchFileNames;
    private Button[] matchFileButtons;
    public double[] matchRate;
    public int chosenIndex = -1;
    
    private static final DecimalFormat format = new DecimalFormat("###.##%");
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseMatchFileDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout());

        final Label hintLabel = new Label(container, SWT.NONE);
        hintLabel.setText("以下文件和" + newFileName + "内容相同，请选择：");

        matchFileButtons = new Button[matchFileNames.length];
        boolean found = false;
        for (int i = 0; i < matchFileNames.length; i++) {
            matchFileButtons[i] = new Button(container, SWT.RADIO);
            matchFileButtons[i].setText(matchFileNames[i] + "(" + format.format(matchRate[i]) + ")");
            if (!Character.isDigit(matchFileNames[i].charAt(0))) {
                matchFileButtons[i].setSelection(true);
                found = true;
            }
        }
        if (!found) {
            matchFileButtons[0].setSelection(true);
        }
        
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
        return new Point(367, 428);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择文件");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            for (int i = 0; i < matchFileButtons.length; i++) {
                if (matchFileButtons[i].getSelection()) {
                    chosenIndex = i;
                    break;
                }
            }
            if (chosenIndex == -1) {
                MessageDialog.openError(getShell(), "错误", "请选择一个文件。");
                return;
            }
        }
        super.buttonPressed(buttonId);
    }
}
