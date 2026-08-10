package com.pip.j0ide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.gtl.decompiler.ETFDebugInfo;
import com.pip.gtl.decompiler.GTLDeCompiler;
import com.pip.gtl.remotedebugger.GTLDebugSession;
import com.pip.j0ide.data.ProjectData;
import com.pip.j0ide.editors.EditAndGotoJob;
import com.pip.util.Utils;

public class SearchFunctionDialog extends Dialog {
	private Text textFunction;
	private Text textETD;
	private Display display;
	private FileDialog fileDlg;
	private static String lastInput = null;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public SearchFunctionDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);

		final Label label = new Label(container, SWT.NONE);
		label.setText("ETD文件：");

		textETD = new Text(container, SWT.BORDER);
		final GridData gd_textETD = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textETD.setLayoutData(gd_textETD);
		if (lastInput != null) {
		    textETD.setText(lastInput);
		}

		final Button buttonBrowse = new Button(container, SWT.NONE);
		buttonBrowse.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(final SelectionEvent e) {
		        if (fileDlg == null) {
		            fileDlg = new FileDialog(getShell(), SWT.OPEN);
		            fileDlg.setFilterExtensions(new String[] { "*.etd" });
		            fileDlg.setFilterNames(new String[] { "调试信息文件(*.etd)" });
		            
		            ProjectData proj = Application.getInstance().getProjectData();
		            Settings.loadProjectSetting();
		            if (Settings.projectOutputPath.size() > 0) {
		                fileDlg.setFilterPath(Settings.projectOutputPath.get(0));
		            } else if (proj != null) {
		                fileDlg.setFilterPath(proj.getBaseDir().getAbsolutePath());
		            }
		        }
		        String file = fileDlg.open();
		        if (file != null) {
		            textETD.setText(file);
		            lastInput = file;
		        }
		    }
		});
		buttonBrowse.setText("浏览...");

		final Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("函数ID：");

		textFunction = new Text(container, SWT.BORDER);
		final GridData gd_textFunction = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		textFunction.setLayoutData(gd_textFunction);
		
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
		return new Point(458, 140);
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
		    String etdFileName = textETD.getText().trim();
		    if (etdFileName.length() == 0) {
		        MessageDialog.openError(getShell(), "错误", "请选择一个ETD文件。");
		        return;
		    }
		    if (!etdFileName.toLowerCase().endsWith(".etd")) {
		        MessageDialog.openError(getShell(), "错误", "请选择一个ETD文件。");
                return;
		    }
		    etdFileName = etdFileName.substring(0, etdFileName.length() - 4);
		    File etfFile = new File(etdFileName + ".etf.gz");
		    if (!etfFile.exists()) {
		        MessageDialog.openError(getShell(), "错误", "找不到ETF文件。");
                return;
		    }
		    ETFDebugInfo debugInfo;
		    try {
                debugInfo = new GTLDeCompiler().decompile(etfFile);
		    } catch (Exception e) {
		        MessageDialog.openError(getShell(), "错误", e.toString());
                return;
		    }
		    int func;
		    try {
		        func = Integer.parseInt(textFunction.getText());
		        if (func < 0) {
		            throw new Exception();
		        }
		    } catch (Exception e) {
		        MessageDialog.openError(getShell(), "错误", "请输入正确的函数ID。");
                return;
		    }
		    try {
		        doSearch(debugInfo, func);
		    } catch (Exception e) {
		        MessageDialog.openError(getShell(), "错误", e.toString());
                return;
		    }
		}
		super.buttonPressed(buttonId);
	}
	
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("搜索函数");
    }
    
    private void doSearch(ETFDebugInfo debugInfo, int funcID) throws Exception {
        int[] lineNums = (int[])debugInfo.lineNumTable[funcID];
        int lineNum = lineNums[0];
        int[] lineInfo = debugInfo.lineMapping[lineNum];
        String refFile = debugInfo.referFiles[lineInfo[0]];
        int fileLine = lineInfo[1];
        int pos = refFile.replace('\\', '/').lastIndexOf('/');
        if (pos != -1) {
            refFile = refFile.substring(pos + 1);
        }
        List<File> fs = new ArrayList<File>();
        ProjectData proj = Application.getInstance().getProjectData();
        searchInProject(new File(proj.getBaseDir(), "gtl"), refFile, fs); 
        if (fs.size() == 0) {
            throw new Exception("文件未找到：" + refFile + "。");
        } else if (fs.size() > 1) {
            throw new Exception("文件有多个：" + refFile + "。");   
        }
        display.asyncExec(new EditAndGotoJob(fs.get(0), fileLine, true));
    }
    
    private void searchInProject(File dir, String name, List<File> output) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                searchInProject(files[i], name, output);
            } else if (files[i].isFile() && files[i].getName().equalsIgnoreCase(name)) {
                output.add(files[i]);
            }
        }
    }
}
