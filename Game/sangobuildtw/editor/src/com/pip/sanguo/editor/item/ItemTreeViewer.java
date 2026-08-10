package com.pip.sanguo.editor.item;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerRow;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.util.Constants;
import com.swtdesigner.SWTResourceManager;

public class ItemTreeViewer extends TreeViewer {
    public ItemTreeViewer(Composite parent, int style) {
        super(parent, style);
        getTree().setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        getTree().setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    }
    
    protected ViewerRow getViewerRowFromItem(Widget item) {
        ViewerRow ret = super.getViewerRowFromItem(item);
        TreeItem ti = (TreeItem)item;
        Object obj = ti.getData();
        if (obj instanceof Item) {
            int clr = Constants.QUALITY_COLOR[((Item)obj).quality];
            ti.setForeground(SWTResourceManager.getColor(clr >> 16, (clr >> 8) & 0xFF, clr & 0xFF));
        }
        return ret;
    }
}
