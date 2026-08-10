package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipImage;

public class EditSinglePieceDialog extends Dialog {
	private AnimateFrameEditor editor;
	private PipAnimateFrame frame;
	private List<PipAnimateFramePiece> pieces;
	
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public EditSinglePieceDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public void setData(PipAnimateFrame frame, List<PipAnimateFramePiece> pieces) {
		this.frame = frame;
		this.pieces = pieces;
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		container.setLayout(gridLayout);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		editor = new AnimateFrameEditor(composite, SWT.NONE);
		editor.setInput(frame);
		editor.setLockPieces(pieces);
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "¹Ø±Õ",
				true);
	}

	/**
	 * Return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		return new Point(426, 415);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("±à¼­Í¼¿é");
	}
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CLOSE_ID) {
		}
		super.buttonPressed(buttonId);
	}
}
