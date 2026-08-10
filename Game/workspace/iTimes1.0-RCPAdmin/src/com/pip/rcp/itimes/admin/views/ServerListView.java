package com.pip.rcp.itimes.admin.views;


import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.pip.rcp.itimes.admin.data.ServerData;
import com.pip.rcp.itimes.admin.editors.ServerWindowEditor;
import com.pip.rcp.itimes.admin.factory.ServerListFactory;
import com.pip.rcp.itimes.admin.inputs.ServerWindowEditorInput;
import com.pip.rcp.itimes.admin.provider.ServerListViewContentProvider;
import com.pip.rcp.itimes.admin.provider.ServerListViewLabelProvider;


public class ServerListView extends ViewPart{
    public static final String ID = "com.pip.rcp.itimes.admin.views.ServerListView";

    private TableViewer viewer;
    private List<ServerData> serverList = new Vector<ServerData>();

    public void createPartControl(Composite parent){
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER);

        Table table = viewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer.setContentProvider(new ServerListViewContentProvider());
        viewer.setLabelProvider(new ServerListViewLabelProvider());

        MenuManager popupMenuManager = new MenuManager();
        Menu popupMenu = popupMenuManager.createContextMenu(viewer.getTable());
        viewer.getTable().setMenu(popupMenu);
        getSite().registerContextMenu(popupMenuManager, viewer);

        final TableColumn tableColumnIP = new TableColumn(table, SWT.NONE);
        tableColumnIP.setWidth(100);
        tableColumnIP.setText("服务器地址");

        final TableColumn tableColumnPort = new TableColumn(table, SWT.NONE);
        tableColumnPort.setWidth(100);
        tableColumnPort.setText("服务器端口");

        final TableColumn tableColumnDesc = new TableColumn(table, SWT.NONE);
        tableColumnDesc.setWidth(100);
        tableColumnDesc.setText("服务器说明");

        final TableColumn tableColumnUser = new TableColumn(table, SWT.NONE);
        tableColumnUser.setWidth(100);
        tableColumnUser.setText("登陆用户名");

        final TableColumn tableColumnPassword = new TableColumn(table, SWT.NONE);
        tableColumnPassword.setWidth(100);
        tableColumnPassword.setText("登陆密码");

        viewer.setInput(serverList);
        initializeToolBar();

        viewer.addDoubleClickListener(new IDoubleClickListener(){
            public void doubleClick(final DoubleClickEvent event){
                ServerWindowEditorInput input = new ServerWindowEditorInput(serverList.get(viewer.getTable().getSelectionIndex()));

                try{
                    getSite().getWorkbenchWindow().getActivePage().openEditor(input, ServerWindowEditor.ID);
                }catch(Exception e1){
                    e1.printStackTrace();
                }
            }
        });
    }

    public void loadServerData(){
        try{
            List<ServerData> tmp = ServerListFactory.loadServerList();

            serverList.clear();

            for(int i = 0; i < tmp.size(); i++){
                serverList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "服务器列表", "读取服务器列表数据失败");

            e.printStackTrace();
        }
    }

    public boolean serverExist(ServerData server){
        return serverList.contains(server);
    }

    public void addServer(ServerData server){
        try{
            List<ServerData> tmp = new Vector<ServerData>(serverList);
            tmp.add(server);
            ServerListFactory.saveServerList(tmp);

            serverList.clear();

            for(int i = 0; i < tmp.size(); i++){
                serverList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "服务器列表", "保存服务器列表数据失败");

            e.printStackTrace();
        }
    }

    public void removeServer(ServerData server){
        try{
            List<ServerData> tmp = new Vector<ServerData>(serverList);
            tmp.remove(server);
            ServerListFactory.saveServerList(tmp);

            serverList.clear();

            for(int i = 0; i < tmp.size(); i++){
                serverList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "服务器列表", "保存服务器列表数据失败");

            e.printStackTrace();
        }
    }

    public void modifyServer(ServerData oldServer, ServerData newServer){
        try{
            List<ServerData> tmp = new Vector<ServerData>(serverList);

            int index = tmp.indexOf(oldServer);
            tmp.remove(oldServer);

            if(index >= 0){
                tmp.add(index, newServer);
            }else{
                tmp.add(newServer);
            }

            ServerListFactory.saveServerList(tmp);

            serverList.clear();

            for(int i = 0; i < tmp.size(); i++){
                serverList.add(tmp.get(i));
            }

            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(null, "服务器列表", "保存服务器列表数据失败");

            e.printStackTrace();
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus(){
        viewer.getControl().setFocus();
    }

    private void initializeToolBar(){
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
    }
}