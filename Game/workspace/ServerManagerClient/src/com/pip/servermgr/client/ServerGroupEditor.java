package com.pip.servermgr.client;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import com.pip.servermgr.client.ClientPlugin;

import com.pip.servermgr.data.HttpUtils;
import com.pip.servermgr.data.ServerGroup;
import com.swtdesigner.ResourceManager;

public class ServerGroupEditor extends EditorPart {
	public static final String ID = "com.pip.servermgr.client.ServerGroupEditor"; //$NON-NLS-1$
	private ServerGroup group;
	private DataStatusPanel dataStatusPanel;
	
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		ScrolledComposite container = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		Composite container2 = new Composite(container, SWT.NONE);
		container2.setLayout(new RowLayout());
		container.setContent(container2);

		if (!"ipd".equals(group.type)) {
			dataStatusPanel = new DataStatusPanel(container2, group);
		}
		for (int i = 0; i < group.servers.length; i++) {
			if ("ipd".equals(group.servers[i].type)) {
				new IpdStatusPanel(container2, group.servers[i]);
			} else {
				new ServerStatusPanel(container2, group.servers[i]);
			}
		}
		Point pt = getSite().getShell().getSize();
		container2.setSize(pt.x * 7 / 10, 1000);
		container.getVerticalBar().setPageIncrement(300);
		container.getHorizontalBar().setPageIncrement(300);
		
		this.setPartName(this.getEditorInput().getName());
	}
	
	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Do the Save operation
	}

	@Override
	public void doSaveAs() {
		// Do the Save As operation
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		// Initialize the editor part
		setSite(site);
		setInput(input);
		group = ((ServerGroupInput)input).group;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
