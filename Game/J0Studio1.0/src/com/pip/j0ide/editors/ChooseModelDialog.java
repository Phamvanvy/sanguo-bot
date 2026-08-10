package com.pip.j0ide.editors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.pip.j0ide.Activator;
import com.pip.j0ide.Application;
import com.pip.j0ide.Settings;
import com.pip.j0ide.data.Model;
import com.pip.j0ide.data.ProjectData;
import com.pip.j0ide.data.Variable;

public class ChooseModelDialog extends Dialog {
	private Combo comboPath;
	private Combo comboRevision;
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return Application.getInstance().getProjectData().getModels();
		}
		
		public void dispose() {}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	class ListLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return element.toString();
		}
		public Image getImage(Object element) {
			return Activator.getDefault().getImageRegistry().get("model");
		}
	}
	
	private List list;
	private String defaultPath = "";
	private String outputPath = "";
	private static Model[] choosenModels = new Model[0];
	private String revision = "PiP";
	private Button buttonForce;
	private Button buttonReturnCheck;
	private static boolean forceCompile;
	private static boolean forceReturnCheck;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ChooseModelDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label lblModel = new Label(container, SWT.NONE);
		lblModel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblModel.setText("目标机型(至少选择1个)(&T)：");

		final ListViewer modelList = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		modelList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				getButton(IDialogConstants.OK_ID).setEnabled(list.getSelectionCount() > 0);
			}
		});
		modelList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				buttonPressed(IDialogConstants.OK_ID);
			}
		});
		modelList.setLabelProvider(new ListLabelProvider());
		modelList.setContentProvider(new ContentProvider());
		modelList.setInput(new Object());
		list = modelList.getList();
		final GridData gd_list = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_list.heightHint = 0;
		gd_list.widthHint = 0;
		list.setLayoutData(gd_list);

		final Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		label.setText("输出目录(空表示当前目录)(&O)：");

		comboPath = new Combo(container, SWT.NONE);
		final GridData gd_comboPath = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboPath.setLayoutData(gd_comboPath);
		
		final Button browseButton = new Button(container, SWT.NONE);
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onSelectPath();
			}
		});
		browseButton.setText("浏览(&B)...");
		
		final Label labelRevision = new Label(container, SWT.NONE);
		labelRevision.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		labelRevision.setText("选择Revision：");
		
		comboRevision = new Combo(container, SWT.READ_ONLY);
		final GridData gd_comboRevision = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboRevision.setLayoutData(gd_comboRevision);
		String revision = "";
		ProjectData pd = Application.getInstance().getProjectData();
		File dataui = pd.getBaseDir();
		for (int i = 0; i < pd.variables.size(); i++) {
			Variable v = pd.variables.get(i);
			if(v.name.equals("Revision")){
				revision = v.value;
				this.revision = revision;
				break;
			}
		}
		String[] revisions = new String[pd.targets.size()];
		for (int i = 0; i < pd.targets.size(); i++) {
			Variable v = pd.targets.get(i);
			revisions[i] = v.name;
		}
		comboRevision.setItems(revisions);
		for (int i = 0; i < revisions.length; i++) {
			if(revision.equals(revisions[i])){
				comboRevision.select(i);
				break;
			}
		}
		
		comboRevision.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo)e.widget;
				String revision = combo.getItem(combo.getSelectionIndex());
				System.out.println("revision:"+revision);
				ProjectData pd = Application.getInstance().getProjectData();
				for (int i = 0; i < pd.variables.size(); i++) {
					Variable v = pd.variables.get(i);
					if(v.name.equals("Revision")){
						v.value = revision;
						ChooseModelDialog.this.revision = revision;
						break;
					}
				}
				try {
					//保存xml
					pd.save();
					//修改目标目录
					File dataui = pd.getBaseDir();
					System.out.println("dataui:"+dataui);
					File targetDir = null;
					String targetPath = getTargetPath(dataui,revision);
					if(!targetPath.equals("")){
						targetDir = new File(targetPath);
						if(!targetDir.exists()){
							targetDir.mkdirs();
						}
						outputPath = targetDir.getAbsolutePath();
						comboPath.setText(outputPath);
						comboPath.redraw();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		Model[] allModels = Application.getInstance().getProjectData().getModels();
		for (int i = 0; i < allModels.length; i++) {
			for (int j = 0; j < choosenModels.length; j++) {
				if (allModels[i].id.equals(choosenModels[j].id)) {
					list.select(i);
					break;
				}
			}
		}
		
		// 初始化可选路径表
		Settings.loadProjectSetting();
		if (Settings.projectOutputPath.size() == 0) {
		    comboPath.setItems(new String[] { defaultPath });
		} else {
		    String[] sels = new String[Settings.projectOutputPath.size()];
		    Settings.projectOutputPath.toArray(sels);
		    comboPath.setItems(sels);
		}
        comboPath.select(0);
        
		String targetPath = getTargetPath(dataui, revision);
		if(!targetPath.equals("")){
			outputPath = targetPath;
			comboPath.setText(outputPath);
			comboPath.redraw();
		}

		buttonForce = new Button(container, SWT.CHECK);
		final GridData gd_buttonForce = new GridData();
		buttonForce.setLayoutData(gd_buttonForce);
		buttonForce.setText("强制编译");
		buttonForce.setSelection(forceCompile);
		
// 		应轩辕要求，临时设置返回值检查开关，后期版本该检查为必须		
//		buttonReturnCheck = new Button(container, SWT.CHECK);
//		final GridData gd_buttonReturnCheck = new GridData();
//		buttonReturnCheck.setLayoutData(gd_buttonReturnCheck);
//		buttonReturnCheck.setText("返回值检查");
//		buttonReturnCheck.setSelection(forceReturnCheck);
		
		return container;
	}
	
	/**
	 * 拷贝.info
	 * @param revision
	 * @throws Exception 
	 */
	public void copyDotInfo() throws Exception{
		// Light20130801: 废掉这个隐藏的逻辑，各版本可以有不同的.info文件
		if (true) {
			return;
		}
		
		//拷贝所有的Lib用的.info文件以保持与pip版本的一致性
		if(revision.equals("PiP")){
			return;
		}
		ProjectData pd = Application.getInstance().getProjectData();
		File dataui = pd.getBaseDir();
		if(!dataui.exists()){
			throw new Exception("错误的工作目录");
		}
		String pipScriptsPath = getTargetPath(dataui,"PiP");
		File pipScripts = new File(pipScriptsPath);
		File[] fs = pipScripts.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if(fs[i].isDirectory()){
				File[] infos = fs[i].listFiles(new LibInfoFilter());
				for (int j = 0; j < infos.length; j++) {
					String path = fs[i].getAbsolutePath();
					String modelid = path.substring(path.lastIndexOf(File.separatorChar));
					File modeliddir = new File(outputPath,modelid);
					if(modeliddir.exists()){
						try {
							copyFile(infos[j], new File(outputPath + File.separatorChar + modelid + File.separatorChar + infos[j].getName()));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private String getTargetPath(File dataui,String revision){
		String targetPath = "";
		ProjectData pd = Application.getInstance().getProjectData();
		for (int i = 0; i < pd.targets.size(); i++) {
			Variable v = pd.targets.get(i);
			if(revision.equals(v.name)){
				targetPath = new File(dataui.getParent(), v.value).getAbsolutePath();
				break;
			}
		}
		return targetPath;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "确定",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"取消", false);
		
		getButton(IDialogConstants.OK_ID).setEnabled(list.getSelectionCount() > 0);
	}

	/**
	 * Return the initial size of the dialog
	 */
	protected Point getInitialSize() {
		return new Point(502, 422);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("选择机型");
	}
	
	private Model[] getInput() {
		Model[] allModels = Application.getInstance().getProjectData().getModels();
		int[] indices = list.getSelectionIndices();
		Model[] ret = new Model[indices.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = allModels[indices[i]];
		}
		return ret;
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			Model[] ms = getInput();
			if (ms.length == 0) {
				MessageDialog.openError(this.getParentShell(), "错误", "请至少选择一个机型。");
				return;
			}
			choosenModels = ms;
			outputPath = comboPath.getText().trim();
			if (outputPath.length() > 0) {
				File f = resolveFile(new File(defaultPath), outputPath);
				if (!f.exists()) {
					MessageDialog.openError(this.getParentShell(), "错误", "目标目录不存在。");
					return;
				} else if (!f.isDirectory()) {
					MessageDialog.openError(this.getParentShell(), "错误", "目标目录错误。");
					return;
				}
			}
			Settings.projectOutputPath.remove(outputPath);
			Settings.projectOutputPath.add(0, outputPath);
			Settings.saveProjectSetting();
			forceCompile = buttonForce.getSelection();
//	 		应轩辕要求，临时设置返回值检查开关，后期版本该检查为必须			
//			forceReturnCheck = buttonReturnCheck.getSelection();
		}
		super.buttonPressed(buttonId);
	}
	
	public static void copyFile(File src, File dest) throws IOException{
        FileInputStream fis = null;
        FileOutputStream fos = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try{
            fis = new FileInputStream(src);
            byte[] data = new byte[256];
            int len;
            while((len = fis.read(data)) >= 0){
                if(len == 0){
                    continue;
                }
                bos.write(data, 0, len);
            }
        }catch(IOException e){
            throw e;
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch(IOException e){
                }
            }
        }
        
        try{
            fos = new FileOutputStream(dest);
            fos.write(bos.toByteArray(), 0, bos.size());
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
    }
	
    // 解析包含文件路径。包含文件路径可能是相对路径，也可能是绝对路径。
    public static File resolveFile(File ref, String path) {
        // 检查是否绝对路径
        if (File.separatorChar == '/') {
            if (path.charAt(0) == '/') {
                return new File(path);
            }
        } else if (path.length() > 1 && path.charAt(1) == ':') {
            return new File(path);
        }

        // 解析相对路径
        String[] secs = path.split("/|\\\\");
        for (int i = 0; i < secs.length; i++) {
            if (secs[i].equals(".")) {
                continue;
            } else if (secs[i].equals("..")) {
                ref = ref.getParentFile();
            } else {
                ref = new File(ref, secs[i]);
            }
        }
        return ref;
    }

	private void onSelectPath() {
		String path = comboPath.getText().trim();
		File defaultDir = new File(defaultPath);
		if (path.length() > 0) {
			File f = resolveFile(defaultDir, path);
			if (f.exists() && f.isDirectory()) {
				defaultDir = f;
			}
		}
		DirectoryDialog dlg = new DirectoryDialog(this.getShell());
		dlg.setFilterPath(defaultDir.getAbsolutePath());
		dlg.setText("选择输出目录");
		dlg.setMessage("请选择编译目标目录：");
		String str = dlg.open();
		if (str != null) {
			comboPath.setText(str);
		}
	}

	public Model[] getChoosenModels() {
		return choosenModels;
	}
	
	public boolean isForceCompile() {
		return forceCompile;
	}
	
	public boolean isForceReturnCheck() {
		return forceReturnCheck;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setDefaultPath(String defaultPath) {
		this.defaultPath = defaultPath;
	}
	
	class LibInfoFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			if(name.endsWith(".info")){
				return true;
			}
			return false;
		}
		
	}
}
