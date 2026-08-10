package com.pip.uieditor.editor;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.uieditor.model.AnchorPoint;
import com.pip.uieditor.util.AnchorUtil;

public class AnchorPointDialog extends Dialog {
	private Text txtAnchorPoints;

	private List<AnchorPoint> anchorPoints = null;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AnchorPointDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public void setData(List<AnchorPoint> anchorPoints) {
		this.anchorPoints = anchorPoints;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		txtAnchorPoints = new Text(container, SWT.BORDER | SWT.MULTI);
		
		if(anchorPoints != null) {
			txtAnchorPoints.setText(AnchorUtil.anchorPointListToText(anchorPoints));
		}
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(483, 407);
	}
	
	@Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	String s = txtAnchorPoints.getText().trim();
        	try {
				this.anchorPoints = AnchorUtil.textToAnchorPointList(s);
			} catch (Exception e) {
            	MessageDialog.openError(getShell(), "¥ÌŒÛ", "∏Ò Ω¥ÌŒÛ");
            	return;
			}
        }
        super.buttonPressed(buttonId);
    }
	
	public List<AnchorPoint> getAnchorPoint() {
		return this.anchorPoints;
	}
}
