package com.pip.j0ide;

import java.io.File;
import java.util.Vector;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.pip.j0ide.editors.EditAndGotoJob;

public class ErrorsView extends ViewPart implements Runnable {
	private class ErrorContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return errors.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	private class ErrorTableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			CompileError err = (CompileError)element;
			switch (columnIndex) {
			case 0:
				return err.gtlFile;
			case 1:
				return err.errorFile.getName();
			case 2:
				return String.valueOf(err.errorLine);
			case 3:
				return err.errorMessage;
			}
			return "";
		}
	}
	public static final String ID = "com.pip.j0ide.ErrorsView"; //$NON-NLS-1$
	private Table errorsTable;
	private TableViewer errorsTableViewer;
	
	protected Vector<CompileError> errors = new Vector<CompileError>();

	protected static class CompileError {
		public String gtlFile;
		public File errorFile;
		public int errorLine;
		public String errorMessage;
	}
	
	public ErrorsView() {
	}
	
	/**
	 * Create contents of the view part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new BorderLayout(0, 0));
		{
			errorsTableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
			errorsTable = errorsTableViewer.getTable();
			errorsTable.setHeaderVisible(true);
			errorsTable.setLayoutData(BorderLayout.CENTER);
			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(errorsTableViewer, SWT.NONE);
				TableColumn gtlColumn = tableViewerColumn.getColumn();
				gtlColumn.setWidth(100);
				gtlColumn.setText("GTL文件");
			}
			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(errorsTableViewer, SWT.NONE);
				TableColumn fileColumn = tableViewerColumn.getColumn();
				fileColumn.setWidth(100);
				fileColumn.setText("出错文件");
			}
			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(errorsTableViewer, SWT.NONE);
				TableColumn lineColumn = tableViewerColumn.getColumn();
				lineColumn.setWidth(100);
				lineColumn.setText("行号");
			}
			{
				TableViewerColumn tableViewerColumn = new TableViewerColumn(errorsTableViewer, SWT.NONE);
				TableColumn messageColumn = tableViewerColumn.getColumn();
				messageColumn.setWidth(400);
				messageColumn.setText("错误信息");
			}
			errorsTableViewer.setContentProvider(new ErrorContentProvider());
			errorsTableViewer.setLabelProvider(new ErrorTableLabelProvider());
			errorsTableViewer.addDoubleClickListener(new IDoubleClickListener() {
				 public void doubleClick(DoubleClickEvent evt) {
					 StructuredSelection sel = (StructuredSelection)errorsTableViewer.getSelection();
					 if (sel.isEmpty()) {
						 return;
					 }
					 CompileError err = (CompileError)sel.getFirstElement();
					 getSite().getShell().getDisplay().asyncExec(new EditAndGotoJob(err.errorFile, err.errorLine - 1, true));
				 }
			});
			errorsTableViewer.setInput(this);
		}

		createActions();
		initializeToolBar();
		initializeMenu();
		
		Application.getInstance().setErrorsView(this);
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
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

	public void setFocus() {
	}
	
	public void clear() {
		errors.clear();
		getSite().getShell().getDisplay().syncExec(this);
	}
	
	public void addError(String gtlFile, File errorFile, int errorLine, String errorMessage) {
		CompileError err = new CompileError();
		err.gtlFile = gtlFile;
		err.errorFile = errorFile;
		err.errorLine = errorLine;
		err.errorMessage = errorMessage;
		errors.add(err);
		getSite().getShell().getDisplay().syncExec(this);
	}
		
	public void run() {
		errorsTableViewer.refresh();
	}
}
