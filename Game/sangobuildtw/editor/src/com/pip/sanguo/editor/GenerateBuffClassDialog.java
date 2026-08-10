package com.pip.sanguo.editor;

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

public class GenerateBuffClassDialog extends Dialog {

    private Text textPrefix;
    private Text textPackage;
    private Text textFolder;
    
    public String prefix;
    public String packageName;
    public String folder;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public GenerateBuffClassDialog(Shell parentShell) {
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
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("目标目录：");

        textFolder = new Text(container, SWT.BORDER);
        final GridData gd_textFolder = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFolder.setLayoutData(gd_textFolder);

        final Button button = new Button(container, SWT.NONE);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.OPEN);
                dlg.setFilterPath(textFolder.getText());
                String dir = dlg.open();
                if (dir != null) {
                    textFolder.setText(dir);
                }
            }
        });
        button.setText("浏览...");

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("包名：");

        textPackage = new Text(container, SWT.BORDER);
        final GridData gd_textPackage = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textPackage.setLayoutData(gd_textPackage);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("类名前缀：");

        textPrefix = new Text(container, SWT.BORDER);
        final GridData gd_textPrefix = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textPrefix.setLayoutData(gd_textPrefix);
        
        textPrefix.setText(prefix);
        textFolder.setText(folder);
        textPackage.setText(packageName);
        
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
        return new Point(500, 168);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("生成Class");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            prefix = textPrefix.getText();
            folder = textFolder.getText();
            packageName = textPackage.getText().trim();
            File f = new File(folder);
            if (!f.exists() || !f.isDirectory()) {
                MessageDialog.openError(getShell(), "错误", "目标目录不正确。");
                return;
            }
            if (packageName.length() == 0) {
                MessageDialog.openError(getShell(), "错误", "必须输入包名。");
                return;
            }
            if (prefix.length() == 0) {
                MessageDialog.openError(getShell(), "错误", "必须输入类名前缀。");
                return;
            }
        }
        super.buttonPressed(buttonId);
    }
}
