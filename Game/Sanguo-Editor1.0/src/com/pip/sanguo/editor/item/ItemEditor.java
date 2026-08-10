package com.pip.sanguo.editor.item;

import java.awt.peer.LabelPeer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jdom.Element;

import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.ItemEffect;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.AutoSelectAll;

/**
 * 物品编辑器
 * @author Joy Yan
 *
 */
public class ItemEditor extends DefaultDataObjectEditor{

	public static final String ID = "com.pip.sanguo.editor.item.ItemEditor"; //$NON-NLS-1$
	
	private Composite container;
	private ItemEditComposite editComp;
	private GridData layoutData1;
	private JewelEditComposite jewelEditComp;
	private GridData layoutData2;
    
	
	public void createPartControl(Composite parent) {
	    
		container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        editComp = new ItemEditComposite(container, SWT.NONE, this);
        layoutData1 = new GridData(GridData.FILL, GridData.FILL, true, true);
        editComp.setLayoutData(layoutData1);
        jewelEditComp = new JewelEditComposite(container, SWT.NONE, this);
        layoutData2 = new GridData(GridData.FILL, GridData.FILL, true, true);
        jewelEditComp.setLayoutData(layoutData2);
        layoutData2.exclude = true;
        jewelEditComp.setVisible(false);
        
        // 设置初始值
		Item item = (Item)editObject;
		if (item.type == Item.TYPE_JEWEL) {
		    jewelEditComp.setVisible(true);
		    layoutData2.exclude = false;
		    editComp.setVisible(false);
		    layoutData1.exclude = true;
		    jewelEditComp.setInput(item);
		} else {
		    editComp.setInput(item);
		}

        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
	}
	
	/**
	 * 修改类型，可能需要改变编辑器。
	 * @param newType
	 */
	public void changeType(int newType) {
	    Item item = (Item)editObject;
	    item.type = newType;
	    if (item.type == Item.TYPE_JEWEL && !jewelEditComp.isVisible()) {
            jewelEditComp.setVisible(true);
            layoutData2.exclude = false;
            editComp.setVisible(false);
            layoutData1.exclude = true;
            container.layout();
            jewelEditComp.setInput(item);
	    } else if (item.type != Item.TYPE_JEWEL && !editComp.isVisible()) {
            jewelEditComp.setVisible(false);
            layoutData2.exclude = true;
            editComp.setVisible(true);
            layoutData1.exclude = false;
            container.layout();
            editComp.setInput(item);
	    }
	    setDirty(true);
	}
	
	/**
	 * editor初始化
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
	    super.init(site, input);
	}
	
	/**
	 * 保存当前修改的数据
	 */
	protected void saveData() throws Exception {
	    Item itemDataDef = (Item)editObject;
	    if (editComp.isVisible()) {
	        editComp.saveItemData(itemDataDef);
	    } else {
	        jewelEditComp.saveItemData(itemDataDef);
	    }
	}
	
	/**
	 * 保存事件处理
	 */
	public void doSave(IProgressMonitor monitor) {
        // Do the Save operation
        try {
            saveData();
            // 保存对象属性并更新XML文件
            saveTarget.update(editObject);
            EditorApplication.getInstance().getProjectData().saveDataList(Item.class);
            setDirty(false);
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            DataListView view = (DataListView)page.findView(DataListView.ID);
            view.refresh(saveTarget);
        } catch (Exception e) {
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            monitor.setCanceled(true);
        }
    }
}
