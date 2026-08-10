package com.pip.j0ide;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchWindow;

import com.pip.gtl.codegen.GTLFunctionCallGenerator;
import com.pip.gtl.codegen.syscall.SystemFunctionManager;
import com.pip.gtl.remotedebugger.GTLDebugManager;
import com.pip.gtl.remotedebugger.GTLDebugServer;

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
		configurer.setInitialSize(new Point(1200, 800));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setTitle("J0集成开发工具 - "+Settings.workingDir);
		configurer.addEditorAreaTransfer(FileTransfer.getInstance());
		configurer.configureEditorAreaDropListener(new FileDropTargetAdapter(configurer.getWindow()));
	}
	
	public void postWindowCreate() {
		super.postWindowCreate();
        Shell shell = getWindowConfigurer().getWindow().getShell();
        try{
	        Image img = Activator.getImageDescriptor("icons/alt_window_32.gif").createImage();
	        if(img != null){
	        	shell.setImage(img);
	        }
        }catch(Exception e){
        	
        }
        shell.setMaximized(true);
        try {
        	Application.getInstance().getProjectData().load(Settings.workingDir);
        	File linkDir = Application.getInstance().getProjectData().getLinkDir();
        	if (linkDir != null) {
        		SystemFunctionManager.configure(new File[] {
        				new File(Settings.workingDir, "gtl/functions.properties"),
        				new File(linkDir, "core_functions.properties")
        		});
        	} else {
        		SystemFunctionManager.configure(new File[] {
        				new File(Settings.workingDir, "gtl/functions.properties")
        		});
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        	MessageDialog.openError(null, "载入数据错误", e.toString());
        }
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		dm.setDisplay(Display.getCurrent());

        // handle arguments
        String[] args = Application.getInstance().getArgs();
        for (int i = 0; i < args.length; i++) {
        	try {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(args[i]));
				IDE.openEditorOnFileStore(getWindowConfigurer().getWindow().getActivePage(), fileStore);
			} catch (Exception e) {
			}
        }
        
        IWorkbenchWindow win = getWindowConfigurer().getWindow();
        WorkbenchWindow wwin = (WorkbenchWindow)win;
        wwin.getMenuManager().updateAll(true);
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
