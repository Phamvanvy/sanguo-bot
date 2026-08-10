package com.pip.uieditor.editor;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.j0ide.Settings;

public class UIEditorConfigDialog extends Dialog {
	private Text textResource;
	private Text textAnimate;
	private Text textImage;
	private Text textMap;
	private Button btnTextStyle;
	private Text textDefineFile;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public UIEditorConfigDialog(Shell parentShell) {
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
		gridLayout.numColumns = 10;
		new Label(container, SWT.NONE);
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setText("Resource Directory:");
		
		textResource = new Text(container, SWT.BORDER);
		GridData gd_textResource = new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1);
		gd_textResource.widthHint = 381;
		textResource.setLayoutData(gd_textResource);
		if(Settings.uiResourceDir != null) {
			textResource.setText(Settings.uiResourceDir.getAbsolutePath());
		}
		
		Button btnResouce = new Button(container, SWT.NONE);
		btnResouce.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setFilterPath(Settings.uiResourceDir.getAbsolutePath());
				dlg.setText("选择目录");
				dlg.setMessage("请选择UI资源目录：");
				String newPath = dlg.open();
				if (newPath != null) {
					textResource.setText(newPath);
				}				
			}
		});
		btnResouce.setText("...");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(container, SWT.NONE);
		lblNewLabel_1.setText("Animate Directory:");
		
		textAnimate = new Text(container, SWT.BORDER);
		textAnimate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		if(Settings.uiAnimateDir != null) {
			textAnimate.setText(Settings.uiAnimateDir.getAbsolutePath());
		}
		
		Button btnAnimate = new Button(container, SWT.NONE);
		btnAnimate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());
				dlg.setFilterPath(Settings.uiAnimateDir.getAbsolutePath());
				dlg.setText("选择目录");
				dlg.setMessage("请选择动画目录：");
				String newPath = dlg.open();
				if (newPath != null) {
					textAnimate.setText(newPath);
				}					
			}
		});
		btnAnimate.setText("...");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblNewLabel_2 = new Label(container, SWT.NONE);
		lblNewLabel_2.setText("Number Image File:");
		
		textImage = new Text(container, SWT.BORDER);
		textImage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		if(Settings.numberImageFile != null) {
			textImage.setText(Settings.numberImageFile.getAbsolutePath());
		}
		
		Button btnImage = new Button(container, SWT.NONE);
		btnImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell());
				dlg.setFilterPath(System.getProperty("user.dir"));
				dlg.setText("选择文件");
				String newFile = dlg.open();
				if(newFile != null) {
					textImage.setText(newFile);
				}
			}
		});
		btnImage.setText("...");
		new Label(container, SWT.NONE);
		
		Label lblNewLabel_3 = new Label(container, SWT.NONE);
		lblNewLabel_3.setText("Number Map:");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		textMap = new Text(container, SWT.BORDER);
		textMap.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 8, 1));
		if(Settings.numberImageMaps != null) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < Settings.numberImageMaps.length; i++) {
				if(i != 0)
					sb.append(',');
				sb.append(Settings.numberImageMaps[i]);
			}
			textMap.setText(sb.toString());
		}
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		btnTextStyle = new Button(container, SWT.CHECK);
		btnTextStyle.setText("Classic Text Style");
		btnTextStyle.setSelection(Settings.textStyle == 0 ? true : false);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblDefineFile = new Label(container, SWT.NONE);
		lblDefineFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDefineFile.setText("Define File:");
		
		textDefineFile = new Text(container, SWT.BORDER);
		textDefineFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 7, 1));
		if(Settings.uiDefineFile != null) {
			textDefineFile.setText(Settings.uiDefineFile.getAbsolutePath());
		}
		
		Button btnDefineFile = new Button(container, SWT.NONE);
		btnDefineFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell());
				dlg.setFilterPath(System.getProperty("user.dir"));
				dlg.setText("选择文件");
				String newFile = dlg.open();
				if(newFile != null) {
					textDefineFile.setText(newFile);
				}				
			}
		});
		btnDefineFile.setText("...");

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
	
	@Override
	protected void okPressed() {
		String strResouce = textResource.getText();
		if(strResouce == null || strResouce.length() == 0) {
	       	MessageDialog.openError(getShell(), "错误", "资源文件目录为空。");
        	return;			
		}
		File resourceDir = new File(strResouce);
		if(!resourceDir.exists()||!resourceDir.isDirectory()) {
	       	MessageDialog.openError(getShell(), "错误", "资源文件目录不存在。");
        	return;				
		}
		String strAnimate = textAnimate.getText();
		if(strAnimate == null || strAnimate.length() == 0) {
	       	MessageDialog.openError(getShell(), "错误", "动画文件目录为空。");
        	return;				
		}
		File animateDir = new File(strAnimate);
		if(!animateDir.exists()||!animateDir.isDirectory()) {
	       	MessageDialog.openError(getShell(), "错误", "动画文件目录不存在。");
        	return;				
		}
		String strImage = textImage.getText();
		if(strImage == null || strImage.length() == 0) {
	       	MessageDialog.openError(getShell(), "错误", "图片文字文件为空。");
        	return;				
		}
		File imageFile = new File(strImage);
		if(!imageFile.exists()||!imageFile.isFile()) {
	       	MessageDialog.openError(getShell(), "错误", "图片文字文件错误。");
        	return;			
		}
		String strMap = textMap.getText();
		if(strMap == null || strMap.length() == 0) {
	       	MessageDialog.openError(getShell(), "错误", "图片文字映射错误。");
        	return;				
		}
		String[] ss = strMap.split(",");
		if(ss.length != 10) {
	       	MessageDialog.openError(getShell(), "错误", "图片文字映射错误。");
        	return;				
		}
		String strDefine = textDefineFile.getText();
		File defineFile = null;
		if (strDefine != null && strDefine.length() > 0) {
			defineFile = new File(strDefine);
			if (!defineFile.exists() || !defineFile.isFile()) {
				MessageDialog.openError(getShell(), "错误", "宏定义文件错误。");
				return;
			}
		}
		int[] maps = new int[10];
		boolean success = true;
		for(int i = 0; i < ss.length; i++) {
			try {
				maps[i] = Integer.parseInt(ss[i]);
			} catch (NumberFormatException e) {
				success = false;
			}
		}
		if(!success) {
	       	MessageDialog.openError(getShell(), "错误", "图片文字映射错误。");
        	return;				
		}
		Settings.uiAnimateDir = animateDir;
		Settings.uiResourceDir = resourceDir;
		Settings.numberImageFile = imageFile;
		Settings.numberImageMaps = maps;
		Settings.textStyle = btnTextStyle.getSelection() ? 0 : 1;
		Settings.uiDefineFile = defineFile;
		if (defineFile != null) {
			MacroManager mm = new MacroManager();
			try {
				mm.load(defineFile);
				MacroManager.setInstance(mm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(735, 438);
	}

}
