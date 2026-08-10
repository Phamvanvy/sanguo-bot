package com.pip.j0ide.editors;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class EditAndGotoJob implements Runnable {
	File errorFile;
	int errorLine;
	boolean isError;
	
	public EditAndGotoJob(File f, int l, boolean isError) {
		errorFile = f;
		errorLine = l;
		this.isError = isError;
	}
	
	public void run() {
		try {
			IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(((File)errorFile).getAbsolutePath()));
			IEditorPart ed = IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileStore);
			if (ed != null && ed instanceof GTLEditor) {
				if (errorLine < 0) {
					errorLine = 0;
				}
				if (isError) {
					((GTLEditor)ed).setErrorLine(errorLine);
				}
				((GTLEditor)ed).jumpToLine(errorLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
