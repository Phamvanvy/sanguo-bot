package com.pip.sanguo.editor;

import java.io.File;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.pip.image.workshop.DirectoryView;
import com.pip.image.workshop.TileLibView;
import com.pip.image.workshop.TileView;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.sanguo.data.PlayerLocationMapMaker;
import com.pip.sanguo.data.WorldMapDataForExcel;
import com.pip.sanguo.data.WorldMapDataForExcelFlash;
import com.pip.sanguo.data.i18n.I18NProcessor;
import com.pip.sanguo.data.i18n.LocaleConfig;
import com.pip.sanguo.editor.equipment.EquipmentExportToExcel;
import com.pip.sanguo.editor.item.DropGroupExportToExcel;
import com.pip.sanguo.editor.item.ItemExportToExcel;
import com.pip.sanguo.editor.quest.AreaQuestChatExportToExcel;
import com.pip.sanguo.editor.quest.QuestExportToExcel;
import com.pip.sanguo.editor.shop.IshopExportToExcel;
import com.pip.sanguo.editor.util.MapExportPng;
import com.pip.sanguo.editor.util.Settings;
import com.pip.sanguo.editor.wizard.AutoNewObjectWizard;
import com.pipimage.utils.Utils;
import com.swtdesigner.ResourceManager;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
    private Action userLocationAction;
    private Action exportStageAction;
    private Action i18nCodeAction2;
    private Action i18nCodeAction;
    private Action i18nDataAction;
    private Action generatePackageAction;
    private Action cleanGabageAction;
    private Action updatepriceAction;
    private Action updateAllNpcmapAction;
    private Action generateMapListAction;
    private Action generateSkillClassesAction;
    private Action generateBuffClassesAction;
    private Action generateVersionAction;
    /**生成世界地图选项*/
    private Action generateWorldMapAction;
    /**生成世界地图信息选项*/
    private Action generateWorldMapInfoAction;
    /**生成Flash版世界地图信息选项*/
    private Action generateFlashWorldMapInfoAction;
    /**导出任务信息选项*/
    private Action exportQuestIndexForExcel;
    /**导出场景任务对话选项*/
    private Action exportAreaQuestChatIndexForExcel;
    /**导出物品信息选项*/
    private Action exportItemIndexForExcel;
    /**导出物品信息选项（带图标)*/
    private Action exportItemIndexForExcelWithIcon;
    /**导出装备信息选项*/
    private Action exportEquipmentIndexForExcel;
    /**导出装备信息选项（带图标）*/
    private Action exportEquipmentIndexForExcelWithIcon;
    /**导出卖场选项*/
    private Action exportIshopIndexForExcel;
    /**导出掉落组选项*/
    private Action exportDropGroupIndexForExcel;
    /**导出NPC掉落选项*/
    private Action exportNpcDropIndexForExcel;
    /** 导出所有地图png图片  */
    private Action exportMapPng;
    /** 自动生成物品 */
    private Action AutoNewItem;
    /** 自动生成任务 */
    private Action AutoNewQuest;
    /** 自动生成掉落组*/
    private Action AutoNewDropGroup;
    
    
    private IWorkbenchAction openPerspectiveDialogAction;
    private IWorkbenchAction closeAllPerspectivesAction;
    private IWorkbenchAction closePerspectiveAction;
    private IWorkbenchAction resetPerspectiveAction;
    private IWorkbenchAction savePerspectiveAction;
    private IWorkbenchAction editActionSetsAction;
    private Action viewDataListViewAction;
    private Action viewDirectoryAction;
    private Action viewTileViewAction;
    private Action viewTileLibraryAction;

    private Action redoAction;
    private Action undoAction;
    private IWorkbenchAction closeAllSavedAction;
    private IWorkbenchAction saveAllAction;
    private IWorkbenchAction saveAsAction;
    private IWorkbenchAction saveAction;
    private IWorkbenchAction closeAllAction;
    private IWorkbenchAction closeAction;
    private IWorkbenchAction exitAction;
    private IWorkbenchWindow mainWindow;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    protected void makeActions(IWorkbenchWindow window) {
        this.mainWindow = window;
        
        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        {
            closeAction = ActionFactory.CLOSE.create(window);
            register(closeAction);
        }
        {
            closeAllAction = ActionFactory.CLOSE_ALL.create(window);
            register(closeAllAction);
        }
        {
            saveAction = ActionFactory.SAVE.create(window);
            register(saveAction);
        }
        {
            saveAsAction = ActionFactory.SAVE_AS.create(window);
            register(saveAsAction);
        }
        {
            saveAllAction = ActionFactory.SAVE_ALL.create(window);
            register(saveAllAction);
        }
        {
            closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(window);
            register(closeAllSavedAction);
        }

        undoAction = new Action("&Undo") {
            public void run() {
                this.firePropertyChange("chosen", this, this);
            }
        };
        undoAction.setEnabled(false);
        undoAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/undo_edit(1).gif"));
        undoAction.setDisabledImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/undo_edit.gif"));
        undoAction.setAccelerator(SWT.CTRL | 'z');

        redoAction = new Action("&Redo") {
            public void run() {
                this.firePropertyChange("chosen", this, this);
            }
        };
        redoAction.setEnabled(false);
        redoAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/redo_edit(1).gif"));
        redoAction.setDisabledImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/redo_edit.gif"));
        redoAction.setAccelerator(SWT.CTRL | 'y');

        viewDataListViewAction = new Action("项目") {
            public void run() {
                try {
                    mainWindow.getActivePage().showView(DataListView.ID);
                } catch (Exception e) {
                }
            }
        };
        viewDataListViewAction.setHoverImageDescriptor(ResourceManager.getPluginImageDescriptor(EditorPlugin.getDefault(), "icons/project.gif"));

        viewDirectoryAction = new Action("资源浏览器") {
            public void run() {
                try {
                    mainWindow.getActivePage().showView(DirectoryView.ID);
                } catch (Exception e) {
                }
            }
        };
        viewDirectoryAction.setHoverImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/items.gif"));
        
        viewTileLibraryAction = new Action("贴图素材库") {
            public void run() {
                try {
                    mainWindow.getActivePage().showView(TileLibView.ID);
                } catch (Exception e) {
                }
            }
        };
        viewTileLibraryAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/tilelib.gif"));

        viewTileViewAction = new Action("贴图预览") {
            public void run() {
                try {
                    mainWindow.getActivePage().showView(TileView.ID);
                } catch (Exception e) {
                }
            }
        };
        viewTileViewAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/tiles.gif"));
        {
            editActionSetsAction = ActionFactory.EDIT_ACTION_SETS.create(window);
            register(editActionSetsAction);
        }
        {
            savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE.create(window);
            register(savePerspectiveAction);
        }
        {
            resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
            register(resetPerspectiveAction);
        }
        {
            closePerspectiveAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
            register(closePerspectiveAction);
        }
        {
            closeAllPerspectivesAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create(window);
            register(closeAllPerspectivesAction);
        }
        {
            openPerspectiveDialogAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(window);
            register(openPerspectiveDialogAction);
        }

        generateVersionAction = new Action("自动生成fileversion.xml...") {
            public void run() {
                try {
                    EditorApplication.getProj().generateResourceVersionXML();
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageDialog.openInformation(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };

        generateBuffClassesAction = new Action("自动生成BUFF实现类...") {
            public void run() {
                GenerateBuffClassDialog dlg = new GenerateBuffClassDialog(mainWindow.getShell());
                dlg.folder = Settings.exportClassDir.getAbsolutePath();
                dlg.packageName = Settings.buffPackage;
                dlg.prefix = Settings.buffClassPrefix;
                if (dlg.open() == Dialog.OK) {
                    Settings.exportClassDir = new File(dlg.folder);
                    Settings.buffPackage = dlg.packageName;
                    Settings.buffClassPrefix = dlg.prefix;
                    try {
                        EditorApplication.getProj().generateBuffClasses("GBK");
                        MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageDialog.openInformation(mainWindow.getShell(), "错误", e.toString());
                    }
                }
            }
        };

        generateSkillClassesAction = new Action("自动生成技能实现类...") {
            public void run() {
                GenerateBuffClassDialog dlg = new GenerateBuffClassDialog(mainWindow.getShell());
                dlg.folder = Settings.exportClassDir.getAbsolutePath();
                dlg.packageName = Settings.skillPackage;
                dlg.prefix = Settings.skillClassPrefix;
                if (dlg.open() == Dialog.OK) {
                    Settings.exportClassDir = new File(dlg.folder);
                    Settings.skillPackage = dlg.packageName;
                    Settings.skillClassPrefix = dlg.prefix;
                    try {
                        EditorApplication.getProj().generateSkillClasses("GBK");
                        MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageDialog.openInformation(mainWindow.getShell(), "错误", e.toString());
                    }
                }
            }
        };

        generateMapListAction = new Action("导出场景列表...") {
            public void run() {
                FileDialog dlg = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                dlg.setFilterExtensions(new String[] { "*.txt", "*.*" });
                dlg.setFilterNames(new String[] { "文本文件(*.txt)", "所有文件(*.*)" });
                String outFile = dlg.open();
                if (outFile != null) {
                    try {
                        String text = EditorApplication.getProj().generateMapList();
                        Utils.saveFileContent(new File(outFile), text);
                        MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                    } catch (Exception e) {
                        MessageDialog.openInformation(mainWindow.getShell(), "错误", e.toString());
                    }
                }
            }
        };

        updateAllNpcmapAction = new Action("检查/更新所有NPC引用和地图引用...") {
            public void run() {
                try {
                    EditorApplication.getProj().validateMixedText();
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageDialog.openInformation(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };

        updatepriceAction = new Action("更新所有装备价格/耐久...") {
            public void run() {
                try {
                    EditorApplication.getProj().updateEquipmentPrices();
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageDialog.openInformation(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };

        cleanGabageAction = new Action("清理没有用到的资源...") {
            public void run() {
                try {
                    EditorApplication.getProj().cleanGabage(mainWindow.getShell());
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageDialog.openInformation(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };
        
        exportQuestIndexForExcel = new Action("导出任务信息..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    QuestExportToExcel qete = new QuestExportToExcel();
                    qete.saveQuestToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportAreaQuestChatIndexForExcel = new Action("导出场景任务对话..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    AreaQuestChatExportToExcel qete = new AreaQuestChatExportToExcel();
                    qete.saveQuestToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportItemIndexForExcel = new Action("导出物品信息..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    ItemExportToExcel export = new ItemExportToExcel(false);
                    export.saveItemToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportItemIndexForExcelWithIcon = new Action("导出物品信息(带图标)..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    ItemExportToExcel export = new ItemExportToExcel(true);
                    export.saveItemToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportEquipmentIndexForExcel = new Action("导出装备信息..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    EquipmentExportToExcel export = new EquipmentExportToExcel(false);
                    export.saveEquipmentToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportEquipmentIndexForExcelWithIcon = new Action("导出装备信息(带图标)..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    EquipmentExportToExcel export = new EquipmentExportToExcel(true);
                    export.saveEquipmentToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportIshopIndexForExcel = new Action("导出卖场信息..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    IshopExportToExcel export = new IshopExportToExcel();
                    export.saveIshopToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportDropGroupIndexForExcel = new Action("导出掉落组信息..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    DropGroupExportToExcel export = new DropGroupExportToExcel();
                    export.saveDropGroupToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportNpcDropIndexForExcel = new Action("导出NPC掉落信息..."){
            public void run(){
                FileDialog fd = new FileDialog(mainWindow.getShell(), SWT.SAVE);
                fd.setFilterExtensions(new String[] { "*.xls", "*.*" });
                String inFile = fd.open();
                System.out.println("inFile===" + inFile);
                if(inFile != null){
                    NpcDropExportToExcel export = new NpcDropExportToExcel();
                    export.saveNpcTemplateToExcel(inFile);
                }
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        exportMapPng = new Action("导出所有地图Png..."){
            public void run(){
                MapExportPng.exportMapPng();
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        AutoNewItem = new Action("自动生成物品..."){
            public void run(){
                AutoNewObjectWizard.newItem(mainWindow.getShell());
            }
        };
        
        AutoNewQuest = new Action("自动生成任务..."){
            public void run(){
                AutoNewObjectWizard.newQuest(mainWindow.getShell());
            }
        };
        
        AutoNewDropGroup = new Action("自动生成掉落组..."){
            public void run(){
                AutoNewObjectWizard.newDropGroup(mainWindow.getShell());
            }
        };
        
        generatePackageAction = new Action("生成客户端下载包...") {
            public void run() {
                EditorApplication.getProj().makeClientPackages();
                MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
            }
        };
        
        generateWorldMapAction = new Action("生成世界地图下载包..."){
            public void run(){
                FileDialog dlg = new FileDialog(mainWindow.getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] { "*.map", "*.*" });
                dlg.setFilterNames(new String[] { "地图文件(*.map)", "所有文件(*.*)" });
                String inFile = dlg.open();
                if(inFile != null){
                    File mapf = new File(inFile);
                    EditorApplication.getProj().makeWorldMapPackages(mapf);
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                }
            }
        };
        
        generateWorldMapInfoAction = new Action("生成世界地图详细信息数据"){
            public void run(){
                FileDialog dlg = new FileDialog(mainWindow.getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] { "*.xls", "*.*" });
                dlg.setFilterNames(new String[] { "地图信息文件(*.xls)", "所有文件(*.*)" });
                String inFile = dlg.open();
                if(inFile != null){
                    WorldMapDataForExcel.save(inFile);
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                }
            }
        };
        
        generateFlashWorldMapInfoAction = new Action("生成Flash版世界地图详细信息数据") {
            public void run(){
                FileDialog dlg = new FileDialog(mainWindow.getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] { "*.xls", "*.*" });
                dlg.setFilterNames(new String[] { "地图信息文件(*.xls)", "所有文件(*.*)" });
                String inFile = dlg.open();
                if(inFile != null){
                    WorldMapDataForExcelFlash.save(inFile);
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                }
            }
        };

        i18nDataAction = new Action("处理项目数据...") {
            public void run() {
                try {
                    List<LocaleConfig> locales = LocaleConfig.getLocales(EditorApplication.getProj());
                    if (locales.size() == 0) {
                        throw new Exception("没有配置其他语言。");
                    }
                    GenericChooseDialog dlg = new GenericChooseDialog(mainWindow.getShell(), "选择语言", locales);
                    if (dlg.open() == Dialog.OK) {
                        LocaleConfig locale = (LocaleConfig)dlg.getSelection();
                        I18NProcessor proc = new I18NProcessor(EditorApplication.getProj(), locale);
                        proc.process(true);
                        MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                    }
                } catch (Throwable e) {
                    MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };

        i18nCodeAction = new Action("处理源代码...") {
            public void run() {
                try {
                    List<LocaleConfig> locales = LocaleConfig.getLocales(EditorApplication.getProj());
                    if (locales.size() == 0) {
                        throw new Exception("没有配置其他语言。");
                    }
                    DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
                    dlg.setText("源代码目录");
                    dlg.setMessage("请选择源代码目录：");
                    String newPath = dlg.open();
                    if (newPath == null) {
                        return;
                    }
                    GenericChooseDialog dlg2 = new GenericChooseDialog(mainWindow.getShell(), "选择语言", locales);
                    if (dlg2.open() == Dialog.OK) {
                        LocaleConfig locale = (LocaleConfig)dlg2.getSelection();
                        I18NProcessor proc = new I18NProcessor(new File(newPath), locale);
                        proc.process(true);
                        MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                    }
                } catch (Throwable e) {
                    MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };

        i18nCodeAction2 = new Action("处理源代码(仅提取)...") {
            public void run() {
                try {
                    List<LocaleConfig> locales = LocaleConfig.getLocales(EditorApplication.getProj());
                    if (locales.size() == 0) {
                        throw new Exception("没有配置其他语言。");
                    }
                    DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
                    dlg.setText("源代码目录");
                    dlg.setMessage("请选择源代码目录：");
                    String newPath = dlg.open();
                    if (newPath == null) {
                        return;
                    }
                    GenericChooseDialog dlg2 = new GenericChooseDialog(mainWindow.getShell(), "选择语言", locales);
                    if (dlg2.open() == Dialog.OK) {
                        LocaleConfig locale = (LocaleConfig)dlg2.getSelection();
                        I18NProcessor proc = new I18NProcessor(new File(newPath), locale);
                        proc.process(false);
                        MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                    }
                } catch (Throwable e) {
                    MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };

        exportStageAction = new Action("导出关卡文件...") {
            public void run() {
                try {
                    DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
                    dlg.setText("目标目录");
                    dlg.setMessage("请选择导出目录：");
                    String newPath = dlg.open();
                    if (newPath == null) {
                        return;
                    }
                    
                    EditorApplication.getProj().exportStages(true, new File(newPath));
                    MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                } catch (Throwable e) {
                    MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
                }
            }
        };

        userLocationAction = new Action("绘制用户分布图...") {
            public void run() {
                FileDialog dlg = new FileDialog(mainWindow.getShell(), SWT.OPEN);
                dlg.setFilterExtensions(new String[] { "*.txt", "*.*" });
                dlg.setFilterNames(new String[] { "文本文件(*.txt)", "所有文件(*.*)" });
                String inFile = dlg.open();
                if(inFile != null) {
                    try {
                        new PlayerLocationMapMaker(new File(inFile)).make();
                        MessageDialog.openInformation(mainWindow.getShell(), "成功", "操作成功！");
                    } catch (Throwable e) {
                        e.printStackTrace();
                        MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
                    }
                }
            }
        };
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        menuBar.add(fileMenu);

        final MenuManager menuManager_3 = new MenuManager("世界地图");
        fileMenu.add(menuManager_3);
        
        menuManager_3.add(generateWorldMapInfoAction);
        
        menuManager_3.add(generateWorldMapAction);
        
        menuManager_3.add(generateFlashWorldMapInfoAction);

        final MenuManager menuManager_1 = new MenuManager("资源管理");
        fileMenu.add(menuManager_1);

        menuManager_1.add(generateVersionAction);

        menuManager_1.add(exportStageAction);

        menuManager_1.add(new Separator());

        menuManager_1.add(generateMapListAction);

        menuManager_1.add(generatePackageAction);

        menuManager_1.add(generateBuffClassesAction);

        menuManager_1.add(generateSkillClassesAction);

        menuManager_1.add(updateAllNpcmapAction);

        menuManager_1.add(updatepriceAction);

        menuManager_1.add(cleanGabageAction);
        
        menuManager_1.add(exportQuestIndexForExcel);
        
        menuManager_1.add(exportAreaQuestChatIndexForExcel);
        
        menuManager_1.add(exportItemIndexForExcel);
        menuManager_1.add(exportItemIndexForExcelWithIcon);
        
        menuManager_1.add(exportEquipmentIndexForExcel);
        menuManager_1.add(exportEquipmentIndexForExcelWithIcon);
        
        menuManager_1.add(exportIshopIndexForExcel);
        menuManager_1.add(exportDropGroupIndexForExcel);
        menuManager_1.add(exportNpcDropIndexForExcel);

        menuManager_1.add(userLocationAction);
        
        menuManager_1.add(exportMapPng);        

        final MenuManager menuManager_2 = new MenuManager("国际化");
        fileMenu.add(menuManager_2);

        menuManager_2.add(i18nDataAction);

        menuManager_2.add(i18nCodeAction);

        menuManager_2.add(i18nCodeAction2);
        
        final MenuManager menuManager_4 = new MenuManager("自动生成");
        fileMenu.add(menuManager_4);

        menuManager_4.add(AutoNewItem);

        menuManager_4.add(AutoNewQuest);
        
        menuManager_4.add(AutoNewDropGroup);

        fileMenu.add(new Separator());

        fileMenu.add(saveAction);

        fileMenu.add(saveAsAction);

        fileMenu.add(saveAllAction);

        fileMenu.add(new Separator());

        fileMenu.add(closeAction);

        fileMenu.add(closeAllAction);

        fileMenu.add(closeAllSavedAction);

        fileMenu.add(new Separator());
        fileMenu.add(exitAction);

        final MenuManager menuManager = new MenuManager("&Edit",
                IWorkbenchActionConstants.M_EDIT);
        menuBar.add(menuManager);

        menuManager.add(undoAction);

        menuManager.add(redoAction);

        final MenuManager viewMenu = new MenuManager("&View");
        menuBar.add(viewMenu);

        viewMenu.add(viewDataListViewAction);  
        viewMenu.add(viewDirectoryAction);
        viewMenu.add(viewTileLibraryAction);
        viewMenu.add(viewTileViewAction);
    }

    protected void fillCoolBar(ICoolBarManager coolBar) {
        final ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        coolBar.add(toolBarManager);

        toolBarManager.add(saveAction);

        toolBarManager.add(new Separator());

        toolBarManager.add(undoAction);

        toolBarManager.add(redoAction);

        toolBarManager.add(new Separator());

        toolBarManager.add(openPerspectiveDialogAction);
    }
}
