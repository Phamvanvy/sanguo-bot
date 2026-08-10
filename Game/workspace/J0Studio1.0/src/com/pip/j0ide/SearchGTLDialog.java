package com.pip.j0ide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

public class SearchGTLDialog extends Dialog implements Runnable {
	class TreeLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((File)element).getName();
		}
		public Image getImage(Object element) {
			File f = (File)element;
			String imageName;
			if (f.isDirectory()) {
				imageName = "folder";
			} else {
				imageName = "gtl";
			}
			if (imageName != null) {
				return Activator.getDefault().getImageRegistry().get(imageName);
			} else {
				return null;
			}
		}
	}
	
	class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public Object[] getChildren(Object parentElement) {
			File parent = (File)parentElement;
			if (parent.isFile()) {
				return new Object[0];
			}
			File[] children = parent.listFiles();
			List<File> list = new ArrayList<File>();
			for (File f : children) {
				if (f.getName().equals("CVS")) {
					continue;
				}
				if (f.isDirectory()) {
					if (getChildren(f).length > 0) {
						list.add(f);
					}
				} else if ((f.getName().toLowerCase().endsWith(".gtl") || f.getName().toLowerCase().endsWith(".h")) && f.getName().toLowerCase().indexOf(filterText) >= 0) {
					if (firstMatch == null) {
						firstMatch = f;
					}
					list.add(f);
				}
			}
			return list.toArray();
		}
		public Object getParent(Object element) {
			if (element.equals(baseDir)) {
				return null;
			} else {
				return ((File)element).getParentFile();
			}
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}
	
	private TreeViewer treeViewer;
	private Tree tree;
	private Text textCondition;
	private File baseDir;
	private String filterText = "";
	private File selectedFile;
	private File firstMatch;
	private Display display;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public SearchGTLDialog(Shell parentShell, File baseDir) {
		super(parentShell);
		this.baseDir = baseDir;
	}
	
	public File getSelectedFile() {
		return selectedFile;
	}
	
	public void run() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		if (firstMatch != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					treeViewer.expandToLevel(firstMatch, 1);
					StructuredSelection sel = new StructuredSelection(firstMatch);
					treeViewer.setSelection(sel);
					treeViewer.expandAll();
				}
			});
		}
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
		label.setText("ËÑË÷£º");

		textCondition = new Text(container, SWT.BORDER);
		textCondition.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				firstMatch = null;
				filterText = textCondition.getText().toLowerCase();
				treeViewer.refresh();
				new Thread(SearchGTLDialog.this).start();
			}
		});
		final GridData gd_textCondition = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textCondition.setLayoutData(gd_textCondition);

		treeViewer = new TreeViewer(container, SWT.BORDER);
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setContentProvider(new TreeContentProvider());
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		treeViewer.setInput(baseDir);
		treeViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent arg0) {
				buttonPressed(IDialogConstants.OK_ID);
			}
			
		});		
		display = this.getShell().getDisplay();
		return container;
	}

	/**
	 * Create contents of the button bar
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
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(458, 417);
	}
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
			if (sel.isEmpty()) {
				return;
			}
			File f = (File)sel.getFirstElement();
			if (f.isFile()) {
				selectedFile = f;
			} else {
				return;
			}
		}
		super.buttonPressed(buttonId);
	}

}
