package com.pip.j0ide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.pip.j0ide.editors.GTLEditor;

public class SearchResultView extends ViewPart {
	class TreeLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof File) {
				return ((File)element).getName();
			} else {
				return element.toString();
			}
		}
		public Image getImage(Object element) {
			if (element instanceof File) {
				return Activator.getDefault().getImageRegistry().get("file");
			} else {
				return Activator.getDefault().getImageRegistry().get("member");
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
			if (parentElement instanceof SearchResultView) {
				List<File> files = new ArrayList<File>();
				for (SearchResult sr : searchResults) {
					if (!files.contains(sr.file)) {
						files.add(sr.file);
					}
				}
				return files.toArray();
			} else if (parentElement instanceof File) {
				List<SearchResult> ret = new ArrayList<SearchResult>();
				for (SearchResult sr : searchResults) {
					if (sr.file.equals(parentElement)) {
						ret.add(sr);
					}
				}
				return ret.toArray();
			} else {
				return new Object[0];
			}
		}
		public Object getParent(Object element) {
			if (element instanceof SearchResultView) {
				return null;
			} else if (element instanceof File) {
				return SearchResultView.this;
			} else if (element instanceof SearchResult) {
				return ((SearchResult)element).file;
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}
	
	public static class SearchResult {
		public File file;
		public int lineNo;
		public int column;
		public int length;
		public String lineContent;
		
		public String toString() {
			return lineNo + ": " + lineContent;
		}
	}
	
	public static List<SearchResult> searchResults = new ArrayList<SearchResult>();
	
	private TreeViewer treeViewer;
	private Tree tree;
	public static final String ID = "com.pip.j0ide.SearchResultView"; //$NON-NLS-1$

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);

		treeViewer = new TreeViewer(container, SWT.BORDER);
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				Object selObj = sel.getFirstElement();
				if (selObj instanceof SearchResult) {
					gotoLine((SearchResult)selObj);
				} else {
					if (treeViewer.getExpandedState(selObj)) {
						treeViewer.collapseToLevel(selObj, 1);
					} else {
						treeViewer.expandToLevel(selObj, 1);
					}
				}
			}
		});
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setContentProvider(new TreeContentProvider());
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setInput(this);
		//
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void refresh() {
		treeViewer.refresh();
		treeViewer.expandAll();
	}
	
	private void gotoLine(SearchResult result) {
		try {
			IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(result.file.getAbsolutePath()));
			IEditorPart ed = IDE.openEditorOnFileStore(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileStore);
			if (ed != null && ed instanceof GTLEditor) {
				GTLEditor ged = (GTLEditor)ed;
				ged.jumpToLine(result.lineNo);
				if (result.length > 0) {
					int lh = ged.getOffsetAtLine(result.lineNo);
					ged.selectAndReveal(lh + result.column, result.length);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
