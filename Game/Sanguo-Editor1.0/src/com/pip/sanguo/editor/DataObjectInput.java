package com.pip.sanguo.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.item.Item;

/**
 * 用于编辑一个数据对象的输入对象。
 * @author lighthu
 */
public class DataObjectInput implements IEditorInput {
    protected DataObject dataObject;;

    public DataObjectInput(DataObject m) {
        dataObject = m;
    }
    
    public Object getAdapter(Class adapter) {
        return null;
    }

    public boolean exists() {
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return EditorPlugin.getDefault().getImageRegistry().getDescriptor("dataobj");
    }

    public String getName() {
        return dataObject.toString();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return dataObject.toString();
    }
    
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DataObjectInput)) {
            return false;
        }
        return dataObject.equals(((DataObjectInput)o).dataObject);
    }
}
