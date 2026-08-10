package com.pip.sanguo.editor.property;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.BookChapter;

public class BookBasicAttrAdvanceDialog extends Dialog{
    
    private Text textLevel;
    private Text textValue;
    private Text textTime;
    private BookChapter attrAdvance;

    /**
     * Create the dialog
     * @param parentShell
     */
    public BookBasicAttrAdvanceDialog(Shell parentShell, BookChapter attr) {
        super(parentShell);
        attrAdvance = attr;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("等级：");

        textLevel = new Text(container, SWT.BORDER);
        textLevel.setText("1");
        final GridData gd_textLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textLevel.setLayoutData(gd_textLevel);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("取值：");

        textValue = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textValue.setText("0");
        } else {
            textValue.setText(String.valueOf(attrAdvance.value));
        }
        final GridData gd_textValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textValue.setLayoutData(gd_textValue);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("时间：");

        textTime = new Text(container, SWT.BORDER);
        if (attrAdvance == null) {
            textTime.setText("0");
        } else {
            textTime.setText(String.valueOf(attrAdvance.time ));
        }
        final GridData gd_textTime = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTime.setLayoutData(gd_textTime);

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
        return new Point(500, 375);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("新等级");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            try {
                attrAdvance.level = Integer.parseInt(textLevel.getText());
            } catch (Exception e) {
                attrAdvance.level = 1;
            }
            try {
                attrAdvance.value = Integer.parseInt(textValue.getText());
            } catch (Exception e) {
                attrAdvance.value = 0;
            }
            try {
                attrAdvance.time = Integer.parseInt(textTime.getText());
            } catch (Exception e) {
                attrAdvance.time = 0;
            }
        }
        super.buttonPressed(buttonId);
    }

}
