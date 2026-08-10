package com.pip.rcp.itimes.admin.provider;


import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.pip.rcp.itimes.admin.data.ServerData;


public class ServerListViewLabelProvider implements ITableLabelProvider{

    public Image getColumnImage(Object element, int columnIndex){
        // TODO Auto-generated method stub
        return null;
    }

    public String getColumnText(Object element, int columnIndex){
        if(element instanceof ServerData){
            ServerData serverList = (ServerData)element;

            switch(columnIndex){
                case 0:
                    return serverList.getIp();
                case 1:
                    return serverList.getPort();
                case 2:
                    return serverList.getDesc();
                case 3:
                    return serverList.getUser();
                case 4:
                    return serverList.getPassword();
            }
        }

        return null;
    }

    public void addListener(ILabelProviderListener listener){
        // TODO Auto-generated method stub

    }

    public void dispose(){
        // TODO Auto-generated method stub

    }

    public boolean isLabelProperty(Object element, String property){
        // TODO Auto-generated method stub
        return false;
    }

    public void removeListener(ILabelProviderListener listener){
        // TODO Auto-generated method stub

    }

}
