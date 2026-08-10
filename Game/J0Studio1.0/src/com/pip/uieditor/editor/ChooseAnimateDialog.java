package com.pip.uieditor.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import swing2swt.layout.BorderLayout;

import com.pip.image.workshop.editor.TileLibSelector;
import com.pip.j0ide.Settings;
import com.pip.uieditor.model.AnimateData;
import com.pip.util.FileExtensionFilter;
import com.pipimage.image.PipAnimateSet;

public class ChooseAnimateDialog extends Dialog {

	
	private ListViewer lstFiles;
	
	private TileLibSelector animateSelector;
	
	private Combo cbMacro;
	
	private File selectedFile;
	private int selectedIndex;
	private String macro;
	private boolean first = true;	
	
	static FileExtensionFilter resourceFilter = new FileExtensionFilter(new String[]{"ctn"}, false);
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ChooseAnimateDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public void setData(AnimateData data) {
		if (data != null) {
			String file = data.getFile();
			if(MacroManager.instance().isMacro(file)) {
				macro = file;
				file = MacroManager.instance().findFileName(file);
			}			
			selectedFile = new File(Settings.uiAnimateDir, file);
			selectedIndex = data.getIndex();
		}
	}
	
	public File getSelectedFile() {
		return this.selectedFile;
	}
	
	public int getSelectedIndex() {
		return animateSelector.getSelectedFrames()[0];
	}

	public String getMacro() {
		return macro;
	}
	
	private void refreshMacros(File file) {
		String fileName = file.getName();
		ArrayList<String> macros = MacroManager.instance().findMacros(fileName);
		cbMacro.removeAll();
		if(!first) {
			macro = null;
		} else {
			first = false;
		}
		if(macros != null && macros.size() > 0) {
			for(int i = 0; i < macros.size(); i++) {
				cbMacro.add(macros.get(i));
			}
		}
		if(macro != null) {
			int index = 0;
			if((index = macros.indexOf(macro)) != -1) {
				cbMacro.select(index);
			}else {
				if(cbMacro.getItemCount() != 0) {
					cbMacro.select(0);
				}
			}
		} else {
			if(cbMacro.getItemCount() != 0) {
				cbMacro.select(0);
			}			
		}
	}	

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new BorderLayout(0, 0));
		
		lstFiles = new ListViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		List list = lstFiles.getList();
		list.setLayoutData(BorderLayout.WEST);
		lstFiles.setContentProvider(new FileListContentProvider());
		lstFiles.setLabelProvider(new FileListLabelProvider());
		lstFiles.setInput(Settings.uiResourceDir);
		lstFiles.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				if(sel.isEmpty()) {
					animateSelector.setInput(null, 16 ,16);
				} else {
					File file = (File)sel.getFirstElement();
					PipAnimateSet image = new PipAnimateSet();
					try {
						image.load(file, true);
						selectedFile = file;
						animateSelector.setInput(image, 16, 16);
						animateSelector.redraw();
						refreshMacros(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		animateSelector = new TileLibSelector(container, SWT.NONE);
		animateSelector.setLayoutData(BorderLayout.CENTER);
		
		cbMacro = new Combo(container, SWT.NONE);
		cbMacro.setLayoutData(BorderLayout.SOUTH);
		
		if(selectedFile != null) {
			lstFiles.setSelection(new StructuredSelection(selectedFile));
			animateSelector.setSelectedFrame(this.selectedIndex);
		}
		
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

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(609, 473);
	}

	
	@Override
	protected void okPressed() {
		if(this.selectedFile == null || animateSelector.getSelectedFrames().length == 0) {
       	MessageDialog.openError(getShell(), "错误", "请选择一个有效的动画资源。");
        	return;
		}
		if(cbMacro.getSelectionIndex() != -1 && cbMacro.getItemCount() != 0) {
			macro = cbMacro.getItem(cbMacro.getSelectionIndex());
		}
		super.okPressed();
	}
	
	static class  FileListContentProvider implements IStructuredContentProvider {
		
		@Override
		public void dispose() {
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			File file = (File)inputElement;
			if(file.isDirectory()) {
			    File[] tmp = file.listFiles(resourceFilter);
			    if (tmp == null) {
			    	return new Object[0];
			    }
			    Arrays.sort(tmp);
				return tmp;
			} else {
				return new Object[0];
			}
		}
	}
	
	static class FileListLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			File file = (File)element;
			return file.getName();
		}
		
	}
}
