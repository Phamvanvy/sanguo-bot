package com.pip.rcp.itimes.admin.provider;


import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.pip.rcp.itimes.admin.data.CommandData;


public class commandListViewLabelProvider implements ITableLabelProvider{

    public Image getColumnImage(Object element, int columnIndex){
        // TODO Auto-generated method stub
        return null;
    }

    public String getColumnText(Object element, int columnIndex){
        if(element instanceof CommandData){
            CommandData commandList = (CommandData)element;

            switch(columnIndex){
                case 0:
                    return commandList.getName();
                case 1:
                    return commandList.getCommand();
                case 2:
                    return commandList.isNeedConfirm()? "ÊÇ": "·ñ";
                case 3:
                    return String.valueOf(commandList.getParmCount());
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
