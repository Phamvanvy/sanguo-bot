package com.pip.image.workshop.editor;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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

import com.pip.image.workshop.AutoBody;
import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.util.SWTUtils;
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
					SWTUtils.showError(getSite().getShell(), "错误", e1);
				}
			}
		});
		editItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/editimage.gif"));
		editItem.setText("编辑图片");

		final ToolItem fetchPointItem = new ToolItem(toolBar, SWT.PUSH);
		fetchPointItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onFetchPoint();
			}
		});
		fetchPointItem.setImage(ResourceManager.getPluginImage(WorkshopPlugin.getDefault(), "icons/split_large.gif"));
		fetchPointItem.setText("识别挂接点");
		
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
			SWTUtils.showError(getSite().getShell(), "错误", "文件格式错误。", e);
		}
		
		this.setPartName(imageFile.getName());
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	private void onFetchPoint() {
		int[][] rawData = ImageViewer.getImageData(image, new Rectangle(0, 0, image.getBounds().width, image.getBounds().height));
		int[] hookPos = AutoBody.findHookPoint(rawData);
		int[] refPos = AutoBody.findRefPoint(rawData);
		String msg = "世界原点：" + refPos[0] + "," + refPos[1] + "\n挂接点：" + hookPos[0] + "," + hookPos[1];
		MessageDialog.openInformation(getSite().getShell(), "信息", msg);
	}
}
