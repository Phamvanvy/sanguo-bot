package com.pip.j0ide;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchInFilesDialog extends Dialog {
	private Text textSearch;
	private Button matchCaseButton, matchWholeWordButton;

	private String searchText;
	private boolean matchCase;
	private boolean matchWholeWord;
	private boolean useRegExp;
    private Button buttonRegExp;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public SearchInFilesDialog(Shell parentShell) {
		super(parentShell);
	}

	public String getSearchText() {
		return searchText;
	}

	public boolean isMatchCase() {
		return matchCase;
	}

	public boolean isMatchWholeWord() {
		return matchWholeWord;
	}
	
	public boolean isUseRegExp() {
	    return useRegExp;
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label label = new Label(container, SWT.NONE);
		label.setText("搜索：");

		textSearch = new Text(container, SWT.BORDER);
		final GridData gd_textSearch = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textSearch.setLayoutData(gd_textSearch);

		matchCaseButton = new Button(container, SWT.CHECK);
		final GridData gd_matchCaseButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		matchCaseButton.setLayoutData(gd_matchCaseButton);
		matchCaseButton.setText("匹配大小写");

		matchWholeWordButton = new Button(container, SWT.CHECK);
		final GridData gd_matchWholeWordButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		matchWholeWordButton.setLayoutData(gd_matchWholeWordButton);
		matchWholeWordButton.setText("全字匹配");

		buttonRegExp = new Button(container, SWT.CHECK);
		final GridData gd_buttonRegExp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		buttonRegExp.setLayoutData(gd_buttonRegExp);
		buttonRegExp.setText("正则表达式");
		//
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
		return new Point(500, 185);
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			searchText = textSearch.getText();
			matchCase = matchCaseButton.getSelection();
			matchWholeWord = matchCaseButton.getSelection();
			useRegExp = buttonRegExp.getSelection();
		}
		super.buttonPressed(buttonId);
	}
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("搜索GTL");
    }
}
