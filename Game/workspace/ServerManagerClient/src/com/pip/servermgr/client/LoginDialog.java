package com.pip.servermgr.client;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.HttpUtils;

public class LoginDialog extends Dialog {

	private Combo comboServer;
	private Text textPassword;
	private Text textUserName;
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public LoginDialog(Shell parentShell) {
		super(parentShell);
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
		label.setText("用户名：");

		textUserName = new Text(container, SWT.BORDER);
		final GridData gd_textUserName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textUserName.setLayoutData(gd_textUserName);
		String oldName = Settings.get("login_name");
		if (oldName != null) {
			textUserName.setText(oldName);
		}

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("密码：");

		textPassword = new Text(container, SWT.PASSWORD | SWT.BORDER);
		final GridData gd_textPassword = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textPassword.setLayoutData(gd_textPassword);

		final Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("服务器：");

		comboServer = new Combo(container, SWT.READ_ONLY);
		comboServer.setItems(new String[] {"昌平机房（代理方式）", "昌平机房（直连方式）", "世纪互联机房"});
		final GridData gd_comboServer = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboServer.setLayoutData(gd_comboServer);
		int sel = 0;
		try {
			sel = Integer.parseInt(Settings.get("serverIndex"));
		} catch (Exception e) {
		}
		comboServer.select(sel);
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "登录",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"退出", false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(366, 222);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("登录");
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			String userName = textUserName.getText();
			String password = textPassword.getText();
			Configuration.userName = userName;
			Configuration.password = password;
			switch (comboServer.getSelectionIndex()) {
			case 0:
				// 昌平机房（代理方式）
				HttpUtils.baseURL = HttpUtils.SERVER1;
				HttpUtils.proxyURL = HttpUtils.PROXY_URL;
				break;
			case 1:
				// 昌平机房（直连方式）
				HttpUtils.baseURL = HttpUtils.SERVER1;
				HttpUtils.proxyURL = null;
				break;
			case 2:
				// 世纪互联机房
				HttpUtils.baseURL = HttpUtils.SERVER2;
				HttpUtils.proxyURL = null;
				break;
			}
			try {
				Configuration.load();
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "错误", "登录失败。");
				return;
			}
			Settings.set("login_name", userName);
			Settings.set("serverIndex", String.valueOf(comboServer.getSelectionIndex()));
		}
		super.buttonPressed(buttonId);
	}
}
