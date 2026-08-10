package com.pip.image.workshop.font;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.util.AutoSelectAll;
import com.pip.util.SWTUtils;

public class SaveFontOptionDialog extends Dialog {
	private Text saveWidthField;
	private Text saveHeightField;
	private Text whiteWidthField;
	private Text yOffsetField;
	private Combo cbCharsetType;

	public int saveWidth;
	public int saveHeight;
	public int whiteWidth;
	public int yOffset;
	public int charsetType;
	
	private int fontWidth;
	private int fontHeight;
	
    /**
	 * Create the dialog
	 * @param parentShell
	 */
	public SaveFontOptionDialog(Shell parentShell, int fontWidth, int fontHeight) {
		super(parentShell);
		this.fontWidth = fontWidth;
		this.fontHeight = fontHeight;
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label lbl1 = new Label(container, SWT.NONE);
		lbl1.setText("Save Width：");

		saveWidthField = new Text(container, SWT.BORDER);
		final GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		saveWidthField.setLayoutData(gd1);
		saveWidthField.addFocusListener(AutoSelectAll.instance);

		final Label lbl2 = new Label(container, SWT.NONE);
		lbl2.setText("Save Height：");

		saveHeightField = new Text(container, SWT.BORDER);
		final GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		saveHeightField.setLayoutData(gd1);
		saveHeightField.addFocusListener(AutoSelectAll.instance);

		final Label lbl3 = new Label(container, SWT.NONE);
		lbl3.setText("Whitespace Width: ");

		whiteWidthField = new Text(container, SWT.BORDER);
		final GridData gd3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		whiteWidthField.setLayoutData(gd3);
		whiteWidthField.addFocusListener(AutoSelectAll.instance);

		final Label lbl4 = new Label(container, SWT.NONE);
		lbl4.setText("Start Y: ");

		yOffsetField = new Text(container, SWT.BORDER);
		final GridData gd4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		yOffsetField.setLayoutData(gd4);
		yOffsetField.addFocusListener(AutoSelectAll.instance);
		
		final Label lbl5 = new Label(container, SWT.NONE);
		lbl5.setText("Charset Type: ");
		
		cbCharsetType = new Combo(container, SWT.READ_ONLY);
		cbCharsetType.setItems(new String[] {"Normal", "Korean", "Vietnam"});
		cbCharsetType.select(0);
		final GridData gd5 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		cbCharsetType.setLayoutData(gd5);
		
		saveWidthField.setText(String.valueOf(fontWidth));
		saveHeightField.setText(String.valueOf(fontHeight));
		whiteWidthField.setText("4");
		yOffsetField.setText("0");
		
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
		return new Point(223, 260);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Save Option");
	}
	
	protected String fetchInput() {
		try {
			saveWidth = Integer.parseInt(saveWidthField.getText());
			if (saveWidth < fontWidth) {
				return "Save width must be equal to or larger than font width.";
			}
			if (saveWidth > 255) {
				return "Save width is too large.";
			}
		} catch (Exception e) {
			return "Save width must be an integer.";
		}
		try {
			saveHeight = Integer.parseInt(saveHeightField.getText());
			if (saveHeight < fontHeight) {
				return "Save height must be equal to or larger than font height.";
			}
			if (saveHeight > 255) {
				return "Save height is too large.";
			}
		} catch (Exception e) {
			return "Save height must be an integer.";
		}
		try {
			whiteWidth = Integer.parseInt(whiteWidthField.getText());
			if (whiteWidth < 1) {
				return "Whitespace width is too small.";
			}
			if (whiteWidth > saveWidth) {
				return "Whitespace width must be equal to or less than font width.";
			}
		} catch (Exception e) {
			return "Whitespace width must be an integer.";
		}
		try {
			yOffset = Integer.parseInt(yOffsetField.getText());
			if (yOffset < 0) {
				return "Start Y can't be negative.";
			}
			if (yOffset > 10) {
				return "Start Y is too large.";
			}
		} catch (Exception e) {
			return "Start Y must be an integer.";
		}
		charsetType = cbCharsetType.getSelectionIndex();
		return null;
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			String msg = fetchInput();
			if (msg != null) {
				MessageDialog.openError(getShell(), "错误", msg);
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
}
