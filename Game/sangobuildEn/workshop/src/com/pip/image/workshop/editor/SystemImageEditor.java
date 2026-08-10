package com.pip.image.workshop.editor;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.util.Utils;
import com.swtdesigner.ResourceManager;

public class SystemImageEditor extends EditorPart {

	public static final String ID = "com.pip.image.workshop.editor.SystemImageEditor"; //$NON-NLS-1$

	private File imageFile;
	private Image image;
	private ImageViewer viewer;
	
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);

		final Composite viewerContainer = new Composite(container, SWT.NONE);
		viewerContainer.setLayout(new FillLayout());
		final GridData gd_viewerContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewerContainer.setLayoutData(gd_viewerContainer);

		final ToolBar toolBar = new ToolBar(container, SWT.NONE);

		final ToolItem editItem = new ToolItem(toolBar, SWT.PUSH);
		editItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					Runtime.getRuntime().exec(new String[] {
						Settings.imageEditor,
						java.text.MessageFormat.format(Settings.imageEditorArg, imageFile.getAbsolutePath())
					});
				} catch (Exception e1) {
					MessageDialog.openError(getSite().getShell(), "´íÎó", e1.toString());
				}
			}
		});
		editItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/editimage.gif"));
		editItem.setText("±à¼­Í¼Æ¬");
		
		viewer = new ImageViewer(viewerContainer, SWT.NONE);
		if (image != null) {
			viewer.setInput(image);
		}
	}

	public void setFocus() {
		// Set the focus
	}

	public void doSave(IProgressMonitor monitor) {
		// Do the Save operation
	}

	public void doSaveAs() {
		// Do the Save As operation
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// Initialize the editor part
		setSite(site);
		setInput(input);
		
		FileStoreEditorInput finput = (FileStoreEditorInput)getEditorInput();
		URI url = finput.getURI();
		String filePath = Utils.urlToPath(url);
		imageFile = new File(filePath);
		try {
			image = new Image(getSite().getShell().getDisplay(), filePath);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.openError(site.getShell(), "´íÎó", "ÎÄ¼þ¸ñÊ½´íÎó¡£\n"+e);
		}
		
		this.setPartName(imageFile.getName());
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
}
