package com.pip.image.workshop.font;

import static java.lang.Integer.parseInt;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.pip.mapeditor.CellMapViewer;
import com.pip.mapeditor.data.CellMap;
import com.pip.util.Rectangle;
import com.pip.util.SWTUtils;
import com.swtdesigner.SWTResourceManager;

public class EditCharDialog extends Dialog {
	private CellMapViewer charEditor;
	private CellMap editBuffer;
	private int editingChar;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public EditCharDialog(Shell parentShell, FontData fdata, int ch) {
		super(parentShell);
		
		editingChar = ch;
		byte[] charData = fdata.charPixels.get(ch);
		editBuffer = new CellMap(fdata.width, fdata.height, 2);
		for (int y = 0; y < fdata.height; y++) {
			System.arraycopy(charData, y * fdata.width, editBuffer.data[y], 0, fdata.width);
		}
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.marginTop = 2;
		gridLayout.marginHeight = 2;
		container.setLayout(gridLayout);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new FillLayout());
		
		charEditor = new CellMapViewer(composite, SWT.NONE);
		charEditor.setInput(editBuffer);
		
		return container;
	}
	
	/**
	 * Create contents of the button bar
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
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(588, 581);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("±à¼­×Ö·û0x" + Integer.toHexString(editingChar));
	}
	
	public byte[] getCharData() {
		byte[] ret = new byte[editBuffer.width * editBuffer.height];
		for (int y = 0; y < editBuffer.height; y++) {
			System.arraycopy(editBuffer.data[y], 0, ret, y * editBuffer.width, editBuffer.width);
		}
		return ret;
	}
}
