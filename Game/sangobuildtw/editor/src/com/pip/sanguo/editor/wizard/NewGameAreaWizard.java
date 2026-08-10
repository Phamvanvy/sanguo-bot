package com.pip.sanguo.editor.wizard;

import java.io.*;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.EFSUtil;
import com.pipimage.utils.Utils;

/**
 * 创建新关卡的向导。
 * @author lighthu
 */
public class NewGameAreaWizard implements Runnable {
    public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        DataListView view = (DataListView)page.findView(DataListView.ID);

        // 询问新关卡的名称
        InputDialog dlg = new InputDialog(shell, "新建关卡", "请输入关卡名称：", "新关卡", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "关卡名称不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() != InputDialog.OK) {
            return;
        }
        
        // 选择一个地图文件
        FileDialog fdlg = new FileDialog(shell, SWT.OPEN);
        fdlg.setFilterExtensions(new String[] { "*.map", "*.*" });
        fdlg.setFilterNames(new String[] { "地图文件(*.map)", "所有文件(*.*)" });
        String file = fdlg.open();
        if (file == null) {
            return;
        }
        
        // 检查地图文件是否有效
        try {
            MapFile mf = new MapFile();
            mf.load(new File(file));
            if (mf.getMaps().size() == 0) {
                MessageDialog.openError(shell, "错误", "地图文件中至少需要包含一张地图。");
                return;
            }
        } catch (Exception e) {
            MessageDialog.openError(shell, "错误", e.toString());
            return;
        }
        
        String newname = dlg.getValue();
        try {
            // 创建新的AreaDef对象
            ProjectData proj = EditorApplication.getInstance().getProjectData();
            GameArea newArea = (GameArea)proj.newObject(GameArea.class);
            newArea.title = newname;
            String dirName = String.valueOf(newArea.id);
            int nextID = 1;
            while (new File(proj.baseDir, "Areas/" + dirName).exists()) {
                dirName = newArea.id + "_" + nextID;
                nextID++;
            }
            newArea.source = new File(proj.baseDir, "Areas/" + dirName);
            newArea.source.mkdirs();
            
            // 拷贝地图文件
            EFSUtil.copyFile(new File(file), new File(newArea.source, "game.map"));
            
            // 刷新关卡列表并开始编辑新关卡
            if (view != null) {
                view.refresh(GameArea.class);
                view.editObject(newArea);
            }
            
            // 保存本类型数据列表
            proj.saveDataList(GameArea.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
