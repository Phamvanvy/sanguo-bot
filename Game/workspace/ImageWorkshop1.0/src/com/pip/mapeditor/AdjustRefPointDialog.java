package com.pip.mapeditor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.pip.mapeditor.data.NPCImageInfo;
import com.pip.util.AutoSelectAll;
import com.pipimage.image.PipAnimate;

/**
 * 调整动画NPC的参考点。
 * @author lighthu
 */
public class AdjustRefPointDialog extends Dialog {
	private PipAnimate animate;
	private RefPointEditor editArea;
	private Point offset;

    /**
	 * Create the dialog
	 * @param parentShell
	 * @param animate 目标动画序列
	 */
	public AdjustRefPointDialog(Shell parentShell, PipAnimate animate) {
		super(parentShell);
		this.animate = animate;
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		editArea = new RefPointEditor(container, SWT.NONE);
		editArea.setInput(animate);
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "确定",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"取消", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		return new Point(483, 541);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("调整参考点");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
		    this.offset = editArea.getOffset();
		}
		super.buttonPressed(buttonId);
	}
	
	public Point getOffset() {
	    return this.offset;
	}
}
