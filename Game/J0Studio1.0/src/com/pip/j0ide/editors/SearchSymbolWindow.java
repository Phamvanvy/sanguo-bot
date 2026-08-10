package com.pip.j0ide.editors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.pip.gtl.compiler.GTLPreCompiler;
import com.pip.gtleditor.java.GTLCompletionProcessor;
import com.pip.j0ide.SearchGTLDialog;


public class SearchSymbolWindow implements Runnable {
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			GTLCompletionProcessor.initImages();
			if (element instanceof GTLPreCompiler.VariableDef) {
				return GTLCompletionProcessor.globalVarImg;
			} else if (element instanceof GTLPreCompiler.FunctionDef) {
				return GTLCompletionProcessor.userFuncImg;
			} else if (element instanceof String) {
				return GTLCompletionProcessor.macroImg;
			} else if (element instanceof GTLPreCompiler.StructDef) {
				return GTLCompletionProcessor.structImg;
			}
			return null;
		}
	}
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			GTLPreCompiler parser = (GTLPreCompiler)inputElement;
			// show global variables, macros and custom functions
			List retList = new ArrayList();
			for (GTLPreCompiler.VariableDef globalVar : parser.getGlobalVars()) {
				if (globalVar.name.toLowerCase().startsWith(filterText)) {
					retList.add(globalVar);
					if (firstMatch == null) {
						firstMatch = globalVar;
					}
				}
			}
			for (GTLPreCompiler.StructDef struct : parser.getStructs()) {
				if (struct.name.toLowerCase().startsWith(filterText)) {
					retList.add(struct);
					if (firstMatch == null) {
						firstMatch = struct;
					}
				}
			}
			for (GTLPreCompiler.FunctionDef func : parser.getFunctions()) {
				if (func.id < 0) {
					continue;
				}
				if (func.name.toLowerCase().startsWith(filterText)) {
					retList.add(func);
					if (firstMatch == null) {
						firstMatch = func;
					}
				}
			}
			for (String macro : parser.getMacros()) {
				if (macro.toLowerCase().startsWith(filterText)) {
					retList.add(macro);
					if (firstMatch == null) {
						firstMatch = macro;
					}
				}
			}
			return retList.toArray();
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private Shell shell;
	private GTLEditor editor;
	private GTLPreCompiler parser;
	private Text textSearch;
	private TableViewer tableViewer;
	private Table table;
	private String filterText = "";
	private Object firstMatch;
	private Display display;
	
	public SearchSymbolWindow(GTLEditor editor) {
		display = editor.getSite().getShell().getDisplay();
		shell = new Shell(editor.getSite().getShell(), SWT.ON_TOP | SWT.RESIZE);
		this.editor = editor;
		this.parser = editor.getParser();
		shell.addShellListener(new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				shell.close();
			}
		});
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing= 0;
		layout.horizontalSpacing = 0;
		shell.setLayout(layout);
		
		textSearch = new Text(shell, SWT.BORDER);
		textSearch.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				firstMatch = null;
				filterText = textSearch.getText().toLowerCase();
				tableViewer.refresh();
				new Thread(SearchSymbolWindow.this).start();
			}
		});
		textSearch.addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == 13) {
					fire();
					e.doit = false;
				}
			}
		});
		final GridData gd_textSearch = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textSearch.setLayoutData(gd_textSearch);

		tableViewer = new TableViewer(shell, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				fire();
			}
		});
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setContentProvider(new ContentProvider());
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == 13) {
					fire();
					e.doit = false;
				}
			}
		});

		final TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setWidth(390);

		new TableColumn(table, SWT.NONE);
		
		tableViewer.setInput(parser);
	}
	
	public void open() {
		shell.open();
		shell.setFocus();
		textSearch.setFocus();
	}
	
	public void setBounds(Rectangle rect) {
		shell.setBounds(rect);
	}
	
	public void run() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		if (firstMatch != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					StructuredSelection sel = new StructuredSelection(firstMatch);
					tableViewer.setSelection(sel);
				}
			});
		}
	}
	
	protected void fire() {
		StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
		if (!sel.isEmpty()) {
			Object obj = sel.getFirstElement();
			int lineNo;
			if (obj instanceof GTLPreCompiler.FunctionDef) {
				lineNo = ((GTLPreCompiler.FunctionDef)obj).lineNo;
			} else if (obj instanceof GTLPreCompiler.VariableDef) {
				lineNo = ((GTLPreCompiler.VariableDef)obj).lineNo;
			} else if (obj instanceof String) {
				lineNo = parser.getLineOfMacro((String)obj);
			} else if (obj instanceof GTLPreCompiler.StructDef) {
				lineNo = ((GTLPreCompiler.StructDef)obj).lineNo;
			} else {
				return;
			}
			File file = parser.getFileOfLine(lineNo);
			int fileLine = parser.getLineOfLine(lineNo);
			new EditAndGotoJob(file, fileLine, false).run();
			shell.close();
		}
	}
}
