package com.pip.image.workshop;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		Settings.loadSetting();

		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(true);
		configurer.setInitialSize(new Point(1200, 800));
		configurer.setShowStatusLine(true);
		configurer.setTitle("Press5图片制作工具      版本：1.1.0216");
		configurer.addEditorAreaTransfer(FileTransfer.getInstance());
		configurer.configureEditorAreaDropListener(new FileDropTargetAdapter(configurer.getWindow()));
	}

	public void postWindowCreate() {
		super.postWindowCreate();
        Shell shell = getWindowConfigurer().getWindow().getShell();
        shell.setMaximized(true);
        shell.setImage(WorkshopPlugin.getImageDescriptor("icons/editimage.gif").createImage());
        // handle arguments
        String[] args = WorkshopApplication.getInstance().getArgs();
        for (int i = 0; i < args.length; i++) {
        	try {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(args[i]));
				IDE.openEditorOnFileStore(getWindowConfigurer().getWindow().getActivePage(), fileStore);
			} catch (Exception e) {
			}
        }
	}
	
	public void postWindowClose() {
		super.postWindowClose();
		Settings.saveSetting();
	}

	static class FileDropTargetAdapter extends DropTargetAdapter {
		IWorkbenchWindow window;
		
		public FileDropTargetAdapter(IWorkbenchWindow window) {
			this.window = window;
		}

		public void dragOver(DropTargetEvent event) {
			event.feedback = DND.FEEDBACK_NONE | DND.FEEDBACK_SCROLL;
			event.detail = DND.DROP_NONE;
			FileTransfer transfer = FileTransfer.getInstance();
			Object data = transfer.nativeToJava(event.currentDataType);
			if (data != null) {
				event.detail = DND.DROP_COPY;
			}
		}
	
		public void drop(DropTargetEvent event) {
			if (event.data == null) {
				return;
			}
			FileTransfer transfer = FileTransfer.getInstance();
			Object data = transfer.nativeToJava(event.currentDataType);
			if (data == null) {
				return;
			}
			String[] files = (String[])event.data;
			for (int i = 0; i < files.length; i++) {
				try {
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(files[i]));
					IDE.openEditorOnFileStore(window.getActivePage(), fileStore);
				} catch (Exception e) {
				}
			}
		}
	}
}
