package com.pip.sanguo.editor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.editor.util.AnimatePreviewer;
import com.pip.util.AutoSelectAll;
import com.pip.util.EFSUtil;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.utils.Utils;

public class AnimationEditor extends DefaultDataObjectEditor {
    private Text textLargeSource;
    private Text textSource;
    private Text textDescription;
    private Text textTitle;
    private Text textID;
    private AnimatePreviewer previewer;
    private AnimatePreviewer previewerLarge;
    public static final String ID = "com.pip.sanguo.editor.AnimationEditor"; //$NON-NLS-1$
    private static FileDialog browseDialog;

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("名称：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("描述：");

        textDescription = new Text(container, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textDescription.setLayoutData(gd_textDescription);
        textDescription.addFocusListener(AutoSelectAll.instance);
        textDescription.addModifyListener(this);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("文件：");

        textSource = new Text(container, SWT.BORDER);
        textSource.setEditable(false);
        final GridData gd_textSource = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textSource.setLayoutData(gd_textSource);

        final Button buttonBrowse = new Button(container, SWT.NONE);
        buttonBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        buttonBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                onBrowse();
            }
        });
        buttonBrowse.setText("浏览...");

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("大版本：");

        textLargeSource = new Text(container, SWT.BORDER);
        textLargeSource.setEditable(false);
        final GridData gd_textLargeSource = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textLargeSource.setLayoutData(gd_textLargeSource);

        final Button buttonBrowseLarge = new Button(container, SWT.NONE);
        buttonBrowseLarge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        buttonBrowseLarge.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                onBrowseLarge();
            }
        });
        buttonBrowseLarge.setText("浏览...");

        final Button buttonAuto = new Button(container, SWT.NONE);
        buttonAuto.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                onEnlarge();
            }
        });
        buttonAuto.setText("自动放大");

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("预览：");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        previewer = new AnimatePreviewer(container, SWT.NONE);
        final GridData gd_previewer = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        gd_previewer.heightHint = 100;
        previewer.setLayoutData(gd_previewer);
        
        previewerLarge = new AnimatePreviewer(container, SWT.NONE);
        final GridData gd_previewerLarge = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        gd_previewerLarge.heightHint = 100;
        previewerLarge.setLayoutData(gd_previewerLarge);
        
        // 设置初始值
        Animation dataDef = (Animation)editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDescription.setText(dataDef.description);
        if (dataDef.source != null) {
            textSource.setText(dataDef.source.getAbsolutePath());
            previewer.setAnimateFile(dataDef.source);
        }
        if (dataDef.largeSource != null) {
            textLargeSource.setText(dataDef.largeSource.getAbsolutePath());
            previewerLarge.setAnimateFile(dataDef.largeSource);
        }

        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        Animation dataDef = (Animation)editObject;
        
        // 读取输入：对象ID、标题、描述
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDescription.getText();
        
        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
    }
    
    // 显示对话框选择动画文件。
    private void onBrowse() {
        Animation dataDef = (Animation)editObject;
        if (browseDialog == null) {
            browseDialog = new FileDialog(getSite().getShell(), SWT.OPEN);
            browseDialog.setFilterExtensions(new String[] { "*.cts", "*.*" });
            browseDialog.setFilterNames(new String[] { "动画文件(*.cts)", "所有文件(*.*)" });
            if (dataDef.source != null) {
                browseDialog.setFilterPath(dataDef.source.getParent());
            }
        }
        String file = browseDialog.open();
        if (file != null) {
            // 把动画文件和相关的ctn文件和pip文件都拷贝到项目目录中
            File newFile;
            try {
                newFile = copyAnimateFile(new File(file), false, false);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
                return;
            }
            if (newFile.equals(dataDef.source)) {
                return;
            }
            dataDef.source = newFile;
            textSource.setText(newFile.getAbsolutePath());
            previewer.setAnimateFile(newFile);
            setDirty(true);
        }
    }
    
    // 显示对话框选择动画文件。
    private void onBrowseLarge() {
        Animation dataDef = (Animation)editObject;
        if (browseDialog == null) {
            browseDialog = new FileDialog(getSite().getShell(), SWT.OPEN);
            browseDialog.setFilterExtensions(new String[] { "*.cts", "*.*" });
            browseDialog.setFilterNames(new String[] { "动画文件(*.cts)", "所有文件(*.*)" });
            if (dataDef.source != null) {
                browseDialog.setFilterPath(dataDef.source.getParent());
            }
        }
        String file = browseDialog.open();
        if (file != null) {
            // 把动画文件和相关的ctn文件和pip文件都拷贝到项目目录中
            File newFile;
            try {
                newFile = copyAnimateFile(new File(file), true, false);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
                return;
            }
            if (newFile.equals(dataDef.largeSource)) {
                return;
            }
            dataDef.largeSource = newFile;
            textLargeSource.setText(newFile.getAbsolutePath());
            previewerLarge.setAnimateFile(newFile);
            setDirty(true);
        }
    }
    
    // 放大小版本图片到大版本
    private void onEnlarge() {
        enlargeOneAnimation((Animation)editObject);
        textLargeSource.setText(((Animation)editObject).largeSource.getAbsolutePath());
        previewerLarge.setAnimateFile(((Animation)editObject).largeSource);
        setDirty(true);
    }
    
    private void enlargeOneAnimation(Animation dataDef) {
        try {
            // 先把小版本的动画文件拷贝到临时目录
            File tmpDir = File.createTempFile("_ani", ".temp");
            tmpDir.delete();
            tmpDir.mkdirs();
            PipAnimateSet ani = new PipAnimateSet();
            ani.load(dataDef.source);
            ani.enlarge(true);
            ani.setOriginalFile(new File(tmpDir, ani.getOriginalFile().getName()));
            ani.save(ani.getOriginalFile(), true);
            
            // 把动画文件和相关的ctn文件和pip文件都拷贝到项目目录中
            File newFile;
            try {
                newFile = copyAnimateFile(ani.getOriginalFile(), true, true);
            } catch (Exception e) {
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
                return;
            }
            if (newFile.equals(dataDef.largeSource)) {
                return;
            }
            dataDef.largeSource = newFile;
            
            Utils.deleteDir(tmpDir);
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
    }

    // 把动画文件和相关的ctn文件和pip文件都拷贝到Animations目录中
    private File copyAnimateFile(File f, boolean large, boolean keepName) throws Exception {
        // 尝试载入动画文件
        PipAnimateSet tempAnimate = new PipAnimateSet();
        tempAnimate.load(f);
        
        // 如果动画文件已经在Animations目录里，则不用拷贝了
        File targetDir = new File(EditorApplication.getProj().baseDir, large ? "Animations/2x" : "Animations");
        if (targetDir.equals(f.getParentFile())) {
            return f;
        }
        
        // 载入已有的所有pip图片内容
        List<File> existImageFiles = new ArrayList<File>();
        List<PipImage> existImages = new ArrayList<PipImage>();
        File[] fs = targetDir.listFiles();
        for (File ff : fs) {
        	if (ff.isFile() && ff.getName().toLowerCase().endsWith(".pip")) {
        	    PipImage img = new PipImage();
        	    try {
        	        img.load(ff.getAbsolutePath());
        	        existImageFiles.add(ff);
        	        existImages.add(img);
        	    } catch (Exception e) {
        	    }
        	}
        }
        
        // 拷贝所有pip文件，并按规则重命名
        HashMap<String, String> fileNameMap = new HashMap<String, String>();
        int fc = tempAnimate.getFileCount();
        for (int i = 0; i < fc; i++) {
        	String name = tempAnimate.getFileName(i);
        	if (fileNameMap.containsKey(name)) {
        		continue;
        	}
        	
        	// 载入需要导入的PIP文件，和已有的所有图片进行比较
            File src = tempAnimate.getSourceFile(i);
            PipImage srcImg = new PipImage();
            srcImg.load(src.getAbsolutePath());
            ArrayList<String> matchFileNames = new ArrayList<String>();
            ArrayList<Integer> matchFileIndices = new ArrayList<Integer>();
            ArrayList<Double> matchFileRates = new ArrayList<Double>();
            for (int j = existImages.size() - 1; j >= 0; j--) {
                double rate = srcImg.compare(existImages.get(j));
                if (rate > 0.95) {
            	    matchFileNames.add(existImageFiles.get(j).getName());
            	    matchFileIndices.add(j);
            	    matchFileRates.add(rate);
            	} else if (existImageFiles.get(j).getName().equals(name)) {
            	    matchFileNames.add(existImageFiles.get(j).getName());
                    matchFileIndices.add(j);
                    matchFileRates.add(rate);
            	}
            }
            int matchIndex = -1;
            if (matchFileNames.size() > 0) {
                // 询问是否使用旧文件
                ChooseMatchFileDialog dlg = new ChooseMatchFileDialog(getSite().getShell());
                dlg.newFileName = src.getName();
                dlg.matchFileNames = new String[matchFileNames.size()];
                matchFileNames.toArray(dlg.matchFileNames);
                dlg.matchRate = new double[matchFileNames.size()];
                for (int j = 0; j < matchFileNames.size(); j++) {
                    dlg.matchRate[j] = matchFileRates.get(j);
                }
                if (dlg.open() == Dialog.OK) {
                    matchIndex = matchFileIndices.get(dlg.chosenIndex);
                }
            }
            
            // 如果有相同文件，则使用旧文件，否则创建新文件
            if (matchIndex == -1) {
            	int index = 0;
            	String newName = src.getName();
            	if (!keepName) {
            	    newName = editObject.id + "_" + index + ".pip";
            	}
            	while (new File(targetDir, newName).exists()) {
            		index++;
            		newName = editObject.id + "_" + index + ".pip";
            	}
            	EFSUtil.copyFile(src, new File(targetDir, newName));
            	fileNameMap.put(name, newName);
            } else {
            	fileNameMap.put(name, existImageFiles.get(matchIndex).getName());
            }
        }
        
        // 修改引用文件表中的文件名
        for (int i = 0; i < tempAnimate.getFileCount(); i++) {
        	tempAnimate.setFileName(i, fileNameMap.get(tempAnimate.getFileName(i)));
        }
        
        // 保存修改后的CTS文件和CTN文件
        int index = 0;
        String newName = editObject.id + ".cts";
    	while (new File(targetDir, newName).exists()) {
    		index++;
    		newName = editObject.id + "_" + index + ".cts";
    	}
    	tempAnimate.save(new File(targetDir, newName), true);
    	tempAnimate.save(new File(targetDir, newName.substring(0, newName.length() - 1) + "n"), false);
    	return new File(targetDir, newName);
    }
}
