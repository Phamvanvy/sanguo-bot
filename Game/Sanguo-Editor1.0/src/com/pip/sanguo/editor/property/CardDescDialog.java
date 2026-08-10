package com.pip.sanguo.editor.property;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
/**
 * ±‡º≠ø®∆¨√Ë ˆ
 * @author zlguo
 *
 */
public class CardDescDialog extends Dialog {

    private Text text;
    String desc = "";
    /**
     * Create the dialog
     * @param parentShell
     */
    public CardDescDialog(Shell parentShell,String txt) {
        super(parentShell);
        desc = txt;
        createDialogArea(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout());

        text = new Text(container, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
        text.setText(desc);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                CardDescDialog.this.desc = text.getText();
            }
        });
        //
        text.redraw();
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
    
    /**
     * »°µ√√Ë ˆ
     * @return
     */
    public String getText(){
        return desc;
    }
    
    /**
     * …Ë÷√√Ë ˆ
     * @param txt
     */
    public void setText(String txt){
        text.setText(txt);
        text.redraw();
    }

}
