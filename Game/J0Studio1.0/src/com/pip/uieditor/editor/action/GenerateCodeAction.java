package com.pip.uieditor.editor.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

import com.pip.j0ide.Application;
import com.pip.j0ide.DirectoryView;
import com.pip.uieditor.editor.FrameEditor;
import com.pip.uieditor.editor.GenerateCodeDialog;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.code.CodeGenerator;
import com.pip.util.Utils;

public class GenerateCodeAction extends WorkbenchPartAction {
	
	public static final String ID = "com.pip.uieditor.editor.action.generatecode";
	
	
	public GenerateCodeAction(IWorkbenchPart editor) {
		super(editor);
		initUI();
	}
	
	protected void initUI() {
		setId(ID);
		setText("Generate");
		setToolTipText("Generate");
	}
	
	@Override
	public void run() {
		FrameEditor editor = (FrameEditor)getWorkbenchPart();
		FileStoreEditorInput input = (FileStoreEditorInput)editor.getEditorInput();
		File f = new File(input.getURI());
		String filename = f.getName();
		if(filename.endsWith(".ui")) {
			filename = filename.substring(0, filename.length() - 3);
		}
		GenerateCodeDialog dlg = new GenerateCodeDialog(getWorkbenchPart().getSite().getShell());
		dlg.setFilename(filename);
		if(dlg.open() == Window.OK) {
			String versionString = Application.getInstance().getProjectData().getGTLVersion();
			int version = 3;
			if(versionString != null) {
				version = Integer.parseInt(versionString);
			}
			filename = dlg.getFilename();
			if(!filename.endsWith(".gtl")) {
				filename += ".gtl";
			}
			String prefix = dlg.getPrefix();
			if(prefix.length() == 0)
				prefix = null;
			Screen screen  = ((FrameEditor)getWorkbenchPart()).getScreen();
			CodeGenerator generator = new CodeGenerator();
			String s = generator.generate(screen, version, prefix);
			
			File newFile = new File(f.getParent(), filename);
			boolean fileExists = false;
			if (newFile.exists()) {
				fileExists =  true;
			}
			try {
		        FileOutputStream fos = null;
		        try{
		            fos = new FileOutputStream(newFile);
		            fos.write(s.getBytes( Application.getInstance().getProjectData().sourceEncoding));
		        }catch(IOException e){
		            throw e;
		        }finally{
		            if(fos != null){
		                try{
		                    fos.close();
		                }catch(IOException e){
		                }
		            }
		        }
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(
						newFile.toURI());
				IDE.openEditorOnFileStore(getWorkbenchPart().getSite()
						.getWorkbenchWindow().getActivePage(), fileStore);
				if(!fileExists) {
					DirectoryView view = (DirectoryView)getWorkbenchPart().getSite().getPage().findView(DirectoryView.ID);
					view.refreshAll();
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.print(s);
		}
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}
	
	
}
