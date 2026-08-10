package com.pip.uieditor.editor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.j0ide.Settings;

public class FontConfigDialog extends Dialog {
	
	public FontData smallFont, mediumFont, largeFont;
	private Text txtDefaultFont;

	private Text txtFont[] = new Text[10];
	
	private Button btnDefaultFont;
	private Button btnFont[] = new Button[10];
	
	

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FontConfigDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 3;
		new Label(container, SWT.NONE);
		
		btnDefaultFont = new Button(container, SWT.NONE);
		btnDefaultFont.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontDialog dlg = new FontDialog(getShell());
				dlg.setFontData(txtDefaultFont.getFont().getFontData()[0]);
				FontData font = dlg.open();
				if(font != null) {
					txtDefaultFont.setFont(new Font(null, font));
				}
			}
		});
		btnDefaultFont.setText("DefaultFont");
		
		txtDefaultFont = new Text(container, SWT.BORDER);
		txtDefaultFont.setText("\u638C\u4E0A\u660E\u73E0");
		txtDefaultFont.setEditable(false);
		GridData gd_txtDefaultFont = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtDefaultFont.widthHint = 142;
		txtDefaultFont.setLayoutData(gd_txtDefaultFont);
		new Label(container, SWT.NONE);
		
		for(int i = 0; i < 10; i++) {
			btnFont[i] = new Button(container, SWT.NONE);
			btnFont[i].addSelectionListener(new FontButtonListener(i));	
			btnFont[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnFont[i].setText("Font"+(i+1));		
		
			txtFont[i] = new Text(container, SWT.BORDER);
			txtFont[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			new Label(container, SWT.NONE);
		}
		
		int i = 0;
		for(Map.Entry<String, FontData> entry : Settings.fonts.entrySet()) {
			FontData font = entry.getValue();
			if(font != null) {
				txtFont[i].setFont(new Font(null, font));
				txtFont[i].setText(entry.getKey());
			}
			i++;
		}
		
//		btnFont[0] = new Button(container, SWT.NONE);
//		btnFont[0].addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont1.setFont(new Font(null, font));
//				}				
//			}
//		});
//		btnFont[0].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont[0].setText("Font1");
//		
//		txtFont[0] = new Text(container, SWT.BORDER);
//		txtFont[0].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont[1] = new Button(container, SWT.NONE);
//		btnFont[1].addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont2.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont[0].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont2.setText("Font2");
//		
//		txtFont2 = new Text(container, SWT.BORDER);
//		txtFont2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont3 = new Button(container, SWT.NONE);
//		btnFont3.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont3.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont3.setText("font3");
//		
//		txtFont3 = new Text(container, SWT.BORDER);
//		txtFont3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont4 = new Button(container, SWT.NONE);
//		btnFont4.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont4.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont4.setText("Font4");
//		
//		txtFont4 = new Text(container, SWT.BORDER);
//		txtFont4.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont5 = new Button(container, SWT.NONE);
//		btnFont5.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont5.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont5.setText("Font5");
//		
//		txtFont5 = new Text(container, SWT.BORDER);
//		txtFont5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont6 = new Button(container, SWT.NONE);
//		btnFont6.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont6.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont6.setText("Font6");
//		
//		txtFont6 = new Text(container, SWT.BORDER);
//		txtFont6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont7 = new Button(container, SWT.NONE);
//		btnFont7.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont7.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont7.setText("Font7");
//		
//		txtFont7 = new Text(container, SWT.BORDER);
//		txtFont7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont8 = new Button(container, SWT.NONE);
//		btnFont8.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont8.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont8.setText("Font8");
//		
//		txtFont8 = new Text(container, SWT.BORDER);
//		txtFont8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont9 = new Button(container, SWT.NONE);
//		btnFont9.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont9.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont9.setText("Font9");
//		
//		txtFont9 = new Text(container, SWT.BORDER);
//		txtFont9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
//		
//		btnFont10 = new Button(container, SWT.NONE);
//		btnFont10.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FontDialog dlg = new FontDialog(getShell());
//				FontData font = dlg.open();
//				if(font != null) {
//					txtFont10.setFont(new Font(null, font));
//				}					
//			}
//		});
//		btnFont10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//		btnFont10.setText("Font10");
//		
//		txtFont10 = new Text(container, SWT.BORDER);
//		txtFont10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		updateView();
		return container;
	}
	
	private void updateView() {
		txtDefaultFont.setFont(new Font(null, Settings.defaultFont));
		int i = 0; 
		for(Map.Entry<String, FontData> entry : Settings.fonts.entrySet()) {
			txtFont[i].setFont(new Font(null, entry.getValue()));
			txtFont[i].setText(entry.getKey());
			i++;
		}
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
		return new Point(530, 559);
	}

	@Override
	protected void okPressed() {
		Settings.defaultFont = txtDefaultFont.getFont().getFontData()[0];
		Map<String,FontData> fonts = new LinkedHashMap<String, FontData>();
		for(int i = 0; i < 10; i++) {
			String fontName = txtFont[i].getText();
			if(fontName != null && fontName.length() > 0) {
				FontData f = txtFont[i].getFont().getFontData()[0];
				fonts.put(fontName, f);
			}
		}
		Settings.fonts = fonts;		
		super.okPressed();
	}
	
	class FontButtonListener extends SelectionAdapter {
		
		private int index;
		
		public FontButtonListener(int index) {
			this.index = index;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			FontDialog dlg = new FontDialog(getShell());
			dlg.setFontData(txtFont[index].getFont().getFontData()[0]);
			FontData font = dlg.open();
			if(font != null) {
				txtFont[index].setFont(new Font(null, font));
			}				
		}		
	}
}
