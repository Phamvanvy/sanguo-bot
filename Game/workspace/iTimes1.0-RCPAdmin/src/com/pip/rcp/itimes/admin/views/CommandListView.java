package com.pip.rcp.itimes.admin.views;


import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import com.pip.rcp.itimes.admin.data.CommandData;
import com.pip.rcp.itimes.admin.editors.ServerWindowEditor;
import com.pip.rcp.itimes.admin.factory.CommandListFactory;
import com.pip.rcp.itimes.admin.provider.commandListViewContentProvider;
import com.pip.rcp.itimes.admin.provider.commandListViewLabelProvider;
import com.pip.rcp.itimes.admin.wizards.CommandWizard;


public class CommandListView extends ViewPart{
    public static final String ID = "com.pip.rcp.itimes.admin.views.CommandListView";

    private TableViewer viewer;
    private List<CommandData> commandList = new Vector<CommandData>();

    public void createPartControl(Composite parent){
        final Composite composite = new Composite(parent, SWT.BORDER);
        composite.setLayout(new GridLayout());
        viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER);

        Table table = viewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        viewer.setContentProvider(new commandListViewContentProvider());
        viewer.setLabelProvider(new commandListViewLabelProvider());

        MenuManager popupMenuManager = new MenuManager();
        Menu popupMenu = popupMenuManager.createContextMenu(viewer.getTable());
        viewer.getTable().setMenu(popupMenu);
        getSite().registerContextMenu(popupMenuManager, viewer);

        final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
        nameTableColumn.setWidth(100);
        nameTableColumn.setText("名称");

        final TableColumn commandTableColumn = new TableColumn(table, SWT.NONE);
        commandTableColumn.setWidth(100);
        commandTableColumn.setText("命令");

        final TableColumn confirmTableColumn = new TableColumn(table, SWT.NONE);
        confirmTableColumn.setWidth(100);
        confirmTableColumn.setText("需要确认");

        final TableColumn numOfParmTableColumn = new TableColumn(table, SWT.NONE);
        numOfParmTableColumn.setWidth(100);
        numOfParmTableColumn.setText("参数个数");

        viewer.setInput(commandList);
        initializeToolBar();

        viewer.addDoubleClickListener(new IDoubleClickListener(){
            public void doubleClick(final DoubleClickEvent event){
                TableItem[] items = viewer.getTable().getSelection();

                if(getSite().getWorkbenchWindow().getActivePage().getActiveEditor() instanceof ServerWindowEditor){
                    ServerWindowEditor editor = (ServerWindowEditor)getSite().getWorkbenchWindow().getActivePage().getActiveEditor();

                    if(items.length > 0){
                        CommandData selectCommand = (CommandData)items[0].getData();

                        if(selectCommand.getParmCount() > 0){
                            CommandWizard wizard = new CommandWizard(selectCommand, (ServerWindowEditor)getSite().getWorkbenchWindow().getActivePage().getActiveEditor());

                            wizard.init(getViewSite().getWorkbenchWindow().getWorkbench(), null);
                            WizardDialog dialog = new WizardDialog(getViewSite().getShell(), wizard);
                            dialog.open();
                        }else{
                            boolean exec = true;

                            if(selectCommand.isNeedConfirm()){
                                if(!MessageDialog.openConfirm(null, "执行命令", "确实要在服务器 [" + editor.getTitle() + "] 上执行 [" + selectCommand.getName() + " : " + selectCommand.getCommand() + "] 吗？")){
                                    exec = false;
                                }
                            }

                            if(exec){
                                editor.fireCommand(selectCommand.getCommand(), false);
                            }
                        }
                    }
                }
            }
        });
    }

    public void loadCommandData(){
        try{
            List<CommandData> tmp = CommandListFactory.loadCommandList();

            commandList.clear();

            for(int i = 0; i < tmp.size(); i++){
                commandList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "命令列表", "读取命令列表数据失败");

            e.printStackTrace();
        }
    }

    public boolean commandExist(CommandData command){
        return commandList.contains(command);
    }

    public void addCommand(CommandData command){
        try{
            List<CommandData> tmp = new Vector<CommandData>(commandList);
            tmp.add(command);
            CommandListFactory.saveCommandList(tmp);

            commandList.clear();

            for(int i = 0; i < tmp.size(); i++){
                commandList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "命令列表", "保存命令列表数据失败");

            e.printStackTrace();
        }
    }

    public void removeCommand(CommandData command){
        try{
            List<CommandData> tmp = new Vector<CommandData>(commandList);
            tmp.remove(command);
            CommandListFactory.saveCommandList(tmp);

            commandList.clear();

            for(int i = 0; i < tmp.size(); i++){
                commandList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "命令列表", "保存命令列表数据失败");

            e.printStackTrace();
        }
    }

    public void modifyCommand(CommandData oldCommand, CommandData newCommand){
        try{
            List<CommandData> tmp = new Vector<CommandData>(commandList);

            int index = tmp.indexOf(oldCommand);
            tmp.remove(oldCommand);

            if(index >= 0){
                tmp.add(index, newCommand);
            }else{
                tmp.add(newCommand);
            }

            CommandListFactory.saveCommandList(tmp);

            commandList.clear();

            for(int i = 0; i < tmp.size(); i++){
                commandList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "命令列表", "保存命令列表数据失败");

            e.printStackTrace();
        }
    }

    public void setFocus(){
        viewer.getControl().setFocus();
    }

    private void initializeToolBar(){
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
    }
}
