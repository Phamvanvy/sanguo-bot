package com.pip.sanguo.performancetest;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.pip.sanguo.performancetest.client.ClientManager;
import com.pip.sanguo.performancetest.client.SanguoClient;
import com.pip.sanguo.performancetest.client.iTimesClient;

public class View extends ViewPart{
    public static final String ID = "PerformanceTest.view";

    private TableViewer viewer;

    /**
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content (like Task List, for
     * example).
     */
    class ViewContentProvider implements IStructuredContentProvider{
        public void inputChanged(Viewer v, Object oldInput, Object newInput){
        }

        public void dispose(){
        }

        public Object[] getElements(Object parent){
            return ((List) parent).toArray();
        }
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider{
        public String getColumnText(Object obj, int index){
            SanguoClient client = (SanguoClient) obj;

            switch(index){
                case 0:
                    return client.getAccountName();
                case 1:
                    return client.getAccountPassword();
                case 2:
                    return client.getActorName();
                case 3:
                    return client.getStatus();
                case 4:
                    return client.getSendInfo();
                case 5:
                    return client.getRecvInfo();
                case 6:
                    return String.valueOf(client.getPositionCount());
                case 7:
                    return String.valueOf(client.getAttackCount());
                case 8:
                    return String.valueOf(client.getChatCount());
                case 9:
                    return String.valueOf(client.getTimeGap());
                case 10:
                    return client.getRunTime();
                case 11:
                    return client.getAvgBytes();
            }

            return null;
        }

        public Image getColumnImage(Object obj, int index){
            return null;
        }

        public Image getImage(Object obj){
            return null;
        }
    }

    public void loadClients(){
        try{
            viewer.refresh();
        }catch(Exception e){
            MessageDialog.openError(getSite().getShell(), "客户端", "读取客户端数据失败");
            e.printStackTrace();
        }
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent){
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER);
        
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());

        Table table = viewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final TableColumn tableColumnAccName = new TableColumn(table, SWT.NONE);
        tableColumnAccName.setWidth(100);
        tableColumnAccName.setText("帐户名");

        final TableColumn tableColumnAccPass = new TableColumn(table, SWT.NONE);
        tableColumnAccPass.setWidth(100);
        tableColumnAccPass.setText("密码");

        final TableColumn tableColumnActorName = new TableColumn(table, SWT.NONE);
        tableColumnActorName.setWidth(100);
        tableColumnActorName.setText("角色名");

        final TableColumn tableColumnStatus = new TableColumn(table, SWT.NONE);
        tableColumnStatus.setWidth(100);
        tableColumnStatus.setText("状态");
        
        final TableColumn tableColumnSendBytes = new TableColumn(table, SWT.NONE);
        tableColumnSendBytes.setWidth(100);
        tableColumnSendBytes.setText("发送流量");
        
        final TableColumn tableColumnRecvBytes = new TableColumn(table, SWT.NONE);
        tableColumnRecvBytes.setWidth(100);
        tableColumnRecvBytes.setText("接收流量");
        
        final TableColumn tableColumnPostionCount = new TableColumn(table, SWT.NONE);
        tableColumnPostionCount.setWidth(100);
        tableColumnPostionCount.setText("move包数量");
        
        final TableColumn tableColumnAttackCount = new TableColumn(table, SWT.NONE);
        tableColumnAttackCount.setWidth(100);
        tableColumnAttackCount.setText("战斗包数量");
        
        final TableColumn tableColumnChatCount = new TableColumn(table, SWT.NONE);
        tableColumnChatCount.setWidth(100);
        tableColumnChatCount.setText("聊天包数量");
        
        final TableColumn tableColumnTimeGap = new TableColumn(table, SWT.NONE);
        tableColumnTimeGap.setWidth(100);
        tableColumnTimeGap.setText("网络延时");
        
        final TableColumn tableColumnRunTime = new TableColumn(table, SWT.NONE);
        tableColumnRunTime.setWidth(100);
        tableColumnRunTime.setText("运行时间");
        
        final TableColumn tableColumnAvgBytes = new TableColumn(table, SWT.NONE);
        tableColumnAvgBytes.setWidth(100);
        tableColumnAvgBytes.setText("平均流量");

        viewer.setInput(ClientManager.getClients());
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus(){
        viewer.getControl().setFocus();
    }
}