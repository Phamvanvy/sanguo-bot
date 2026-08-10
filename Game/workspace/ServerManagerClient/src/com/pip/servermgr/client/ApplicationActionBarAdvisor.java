package com.pip.servermgr.client;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.ide.FileStoreEditorInput;

import com.pip.servermgr.data.Configuration;
import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.report.UserReportEditor;
import com.pip.servermgr.report.UserReportInput;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private Action userDataAnaAction;
	private Action logAnaAction;
	private Action modifyPasswordAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchWindow window;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.
		
		this.window = window;

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);

		modifyPasswordAction = new Action("修改密码...") {
			public void run() {
				modifyPassword();
			}
		};

		logAnaAction = new Action("日志分析...") {
			public void run() {
				FileDialog dlg = new FileDialog(window.getShell(), SWT.OPEN);
				dlg.setFilterNames(new String[] { "日志文件(*.log)", "所有文件(*.*)" });
				dlg.setFilterExtensions(new String[] { "*.log", "*.*" });
				String path = dlg.open();
				if (path == null) {
					return;
				}
				IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path((path)));
				FileStoreEditorInput input = new FileStoreEditorInput(fileStore);
				try {
					window.getActivePage().openEditor(input, AdvancedAnalyseEditor.ID);
				} catch (Exception e) {
					e.printStackTrace();
					MessageDialog.openError(window.getShell(), "错误", e.toString());
				}
			}
		};

		userDataAnaAction = new Action("用户数据分析...") {
			public void run() {
				FileDialog dlg = new FileDialog(window.getShell(), SWT.OPEN);
				dlg.setFilterNames(new String[] { "所有文件(*.*)" });
				dlg.setFilterExtensions(new String[] { "*.*" });
				String path = dlg.open();
				if (path == null) {
					return;
				}
				
				UserReportInput input = new UserReportInput(new File(path));
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, UserReportEditor.ID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);

		fileMenu.add(modifyPasswordAction);

		fileMenu.add(logAnaAction);

		fileMenu.add(userDataAnaAction);
		fileMenu.add(exitAction);
	}

	private void modifyPassword() {
		InputDialog dlg = new InputDialog(window.getShell(), "修改密码", "请输入旧密码：", "", null);
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		if (!Configuration.password.equals(dlg.getValue())) {
			MessageDialog.openError(window.getShell(), "错误", "密码错误。");
			return;
		}
		dlg = new InputDialog(window.getShell(), "修改密码", "请输入新密码：", "", null);
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		String pass1 = dlg.getValue();
		dlg = new InputDialog(window.getShell(), "修改密码", "请重复输入新密码：", "", null);
		if (dlg.open() != InputDialog.OK) {
			return;
		}
		String pass2 = dlg.getValue();
		if (!pass1.equals(pass2)) {
			MessageDialog.openError(window.getShell(), "错误", "两遍输入的密码不一致。");
			return;
		}
		try {
			HttpUtils.modifyPassword(pass1);
			MessageDialog.openInformation(window.getShell(), "成功", "密码修改成功。");
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), "错误", "密码修改失败。");
		}
	}
}
