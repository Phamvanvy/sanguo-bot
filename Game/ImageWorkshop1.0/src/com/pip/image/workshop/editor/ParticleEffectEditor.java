package com.pip.image.workshop.editor;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.pip.image.workshop.Settings;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mango.jni.ParticleEffectPlayer;
import com.pip.mango.jni.ParticleSystemManager;
import com.pip.util.Utils;
import com.swtdesigner.ResourceManager;

/**
 * C版本粒子效果psdata文件查看。
 * @author light.hu
 */
public class ParticleEffectEditor extends EditorPart implements Runnable {
	class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object inputElement) {
			return rootNames;
		}
		
		public Object[] getChildren(Object parentElement) {
			String str = psManager.getMoNames((String)parentElement);
			if (str.length() == 0) {
				return new Object[0];
			} else {
				return str.split(",");
			}
		}
		
		public Object getParent(Object element) {
			return null;
		}
		
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}
	
	public static final String ID = "com.pip.image.workshop.editor.SystemImageEditor"; //$NON-NLS-1$

	private File psdataFile;
	private ParticleSystemManager psManager;
	private String[] rootNames;
	private ParticleEffectPlayer currentPlayer;
	private TreeViewer effectTreeViewer;
	private Tree effectTree;

	private MultiParticleEffectViewer viewer;
	
	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);

		effectTreeViewer = new TreeViewer(container, SWT.BORDER);
		effectTreeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				StructuredSelection sel = (StructuredSelection)effectTreeViewer.getSelection();
				if (!sel.isEmpty()) {
					String name = (String)sel.getFirstElement();
					if (currentPlayer != null) {
						currentPlayer.stop();
					}
					currentPlayer = new ParticleEffectPlayer(psManager, name, 0, 0, false);
					currentPlayer.setLoop(true);
					viewer.setInput(new ParticleEffectPlayer[] { currentPlayer });
				}
			}
		});
		effectTreeViewer.setContentProvider(new TreeContentProvider());
		effectTree = effectTreeViewer.getTree();
		final GridData gd_effectTree = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd_effectTree.widthHint = 250;
		effectTree.setLayoutData(gd_effectTree);
		effectTreeViewer.setInput(this);

		final Composite viewerContainer = new Composite(container, SWT.NONE);
		viewerContainer.setLayout(new FillLayout());
		final GridData gd_viewerContainer = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewerContainer.setLayoutData(gd_viewerContainer);
		
		viewer = new MultiParticleEffectViewer(viewerContainer, SWT.NONE);
		
		new Thread(this).start();
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
		psdataFile = new File(filePath);
		psManager = new ParticleSystemManager();
		psManager.loadTemplates(psdataFile.getAbsolutePath());
		rootNames = psManager.getRootNames().split(",");
		
		this.setPartName(psdataFile.getName());
	}

	@Override
	public void dispose() {
		super.dispose();
		if (currentPlayer != null) {
			currentPlayer.stop();
		}
		psManager.destroy();
		psManager = null;
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void run() {
		while (psManager != null) {
			try {
				Thread.sleep(Settings.animateFrameDelay);
				synchronized (psManager) {
					psManager.update(Settings.animateFrameDelay / 1000.0f);
				}
			} catch (Exception e) {
			}
		}
	}
}
