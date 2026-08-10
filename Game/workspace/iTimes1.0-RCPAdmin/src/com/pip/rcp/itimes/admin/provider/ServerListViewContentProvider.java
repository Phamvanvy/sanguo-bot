package com.pip.rcp.itimes.admin.provider;


import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class ServerListViewContentProvider implements IStructuredContentProvider{
    public Object[] getElements(Object inputElement){
        if(inputElement instanceof List){
            return ((List)inputElement).toArray();
        }

        return null;
    }

    public void dispose(){
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
    }

}
