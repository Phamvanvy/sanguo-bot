package com.pip.image.workshop.editor;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.pip.image.workshop.DirectoryView;
import com.pip.util.AnimateSetOperator;
import com.pip.util.SWTUtils;

public class MergeCts {
	public static void mergeCts(IWorkbenchWindow window){
		DirectoryDialog dlg = new DirectoryDialog(window.getShell(), SWT.SINGLE);
		dlg.setMessage("请选择CTS所在的目录:");
		DirectoryView view = (DirectoryView) window.getActivePage().findView(DirectoryView.ID);
		if(view != null){
			Object obj = view.getSelectedObject();
			if(obj instanceof File){
				File f = ((File)obj);
				if(f.isFile()){
					f = f.getParentFile();
				}
				dlg.setFilterPath(f.getAbsolutePath());
			}
		}
		String ret = dlg.open();
		if(ret == null){
			return;
		}
		try {
			AnimateSetOperator.mergeCtsInDir(ret);
		} catch (Exception e) {
			SWTUtils.showError(window.getShell(), "错误", e);
		}
		MessageDialog.openInformation(window.getShell(), "OK", "成功合并:"+ret+"\n请刷新文件夹结构.merged文件夹内的是合并后的");
	}
}
