package com.pip.uieditor.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchSizeAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import com.pip.j0ide.Application;
import com.pip.uieditor.editor.action.AttachScriptAction;
import com.pip.uieditor.editor.action.CopyAction;
import com.pip.uieditor.editor.action.DeleteRegionAction;
import com.pip.uieditor.editor.action.DownRegionAction;
import com.pip.uieditor.editor.action.GenerateCodeAction;
import com.pip.uieditor.editor.action.MatchRegionAction;
import com.pip.uieditor.editor.action.PasteAction;
import com.pip.uieditor.editor.action.UpRegionAction;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.code.CodeGenerator;
import com.pip.uieditor.model.persist.PersistMapping;
import com.pip.uieditor.model.util.ScreenModelReader;
import com.pip.uieditor.model.util.ScreenModelWriter;
import com.pip.uieditor.palette.PaletteFactory;
import com.pip.uieditor.parts.PartFactory;
import com.pip.uieditor.parts.PartTreeFactory;
import com.pip.util.Utils;

public class FrameEditor extends GraphicalEditorWithPalette implements PropertyChangeListener{

	public static final String ID = "com.pip.uieditor.model.editor.FrameEditor"; //$NON-NLS-1$

	private Screen screen;
	
	public FrameEditor() {
		setEditDomain(new DefaultEditDomain(this));	
	}

	@Override
	public void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
		getGraphicalViewer().setRootEditPart(root);
		getGraphicalViewer().setEditPartFactory(new PartFactory());
		getGraphicalViewer().setKeyHandler(new GraphicalViewerKeyHandler(getGraphicalViewer()));
		
		IAction action = new ToggleGridAction(getGraphicalViewer());
		getActionRegistry().registerAction(action);
		getSelectionActions().add(action.getId());
		
		
		action = new ZoomInAction(root.getZoomManager());
		getActionRegistry().registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(action);
		getSelectionActions().add(action.getId());
		
		getSelectionSynchronizer().addViewer(getGraphicalViewer());
//		action = new ToggleSnapToGeometryAction(getGraphicalViewer());
//		getActionRegistry().registerAction(action);
//		getSelectionActions().add(action.getId());
//		
//		action = new ToggleRulerVisibilityAction(getGraphicalViewer());
//		getActionRegistry().registerAction(action);
//		getSelectionActions().add(action.getId());
	}
	
	@Override
	protected void initializeGraphicalViewer() {
		getGraphicalViewer().setContents(screen);
	}
	

	@Override
	public void setFocus() {
		getGraphicalViewer().getControl().setFocus();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		FileStoreEditorInput fInput = (FileStoreEditorInput)getEditorInput();
		URI uri = fInput.getURI();
		File file = new File(uri);
		try {
			saveScreenModel(new BufferedOutputStream(new FileOutputStream(file)));
			getCommandStack().markSaveLocation();
			screen.clearDirty();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSaveAs() {

	}
	
	

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(Screen.PROPERTY_DIRTY)) {
			firePropertyChange(PROP_DIRTY);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		FileStoreEditorInput fInput = (FileStoreEditorInput)input;
		URI uri = fInput.getURI();
		setPartName(fInput.getName());
		try {
			File file = new File(uri);
			this.screen = readScreenModel(new BufferedInputStream(new FileInputStream(file)));
			this.screen.clearDirty();
			this.screen.addPropertyChangeListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isDirty() {
		if(screen == null)
			return false;
		return screen.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		return PaletteFactory.createPalette();
	}
	
	@Override
	public Object getAdapter(Class type) {
		if (type == IPropertySheetPage.class) {
			return new DefaultPropertySheetPage(getCommandStack(),
					getActionRegistry().getAction(ActionFactory.UNDO.getId()),
					getActionRegistry().getAction(ActionFactory.REDO.getId()));
		}
		if (type == IContentOutlinePage.class)
			return new FrameOutlinePage(new TreeViewer());
		if (type == ZoomManager.class)
			 return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		return super.getAdapter(type);
	}
	

	@Override
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action = null;
		
		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new MatchHeightAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new MatchWidthAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new MatchSizeAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new MatchRegionAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new CopyAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new PasteAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new GenerateCodeAction(this);
		getActionRegistry().registerAction(action);
		
		action = new AttachScriptAction((IWorkbenchPart) this);
		getActionRegistry().registerAction(action);
//		action = new DeleteRegionAction((IWorkbenchPart)this);
//		registry.registerAction(action);
//		getSelectionActions().add(action.getId());
		
	}

	protected static Screen readScreenModel(InputStream stream) {
		try{
			return new ScreenModelReader().read(stream, PersistMapping.getDefault());
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void saveScreenModel(OutputStream stream) {
		try {
			new ScreenModelWriter().write(stream, screen, PersistMapping.getDefault());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Screen getScreen() {
		return screen;
	}
	
	
	
	@Override
	public void dispose() {
		this.screen.removePropertyChangeListener(this);
		super.dispose();
	}



	public class FrameOutlinePage extends ContentOutlinePage {
		
		
		public FrameOutlinePage(EditPartViewer viewer) {
			super(viewer);
		}
		
		public void createControl(Composite parent) {
			getViewer().createControl(parent);
			getViewer().setEditDomain(getEditDomain());
			getViewer().setEditPartFactory(new PartTreeFactory());
//			ContextMenuProvider cmProvider = new ShapesEditorContextMenuProvider(
//					getViewer(), getActionRegistry());
//			getViewer().setContextMenu(cmProvider);
//			getSite().registerContextMenu(
//					"org.eclipse.gef.examples.shapes.outline.contextmenu",
//					cmProvider, getSite().getSelectionProvider());
			MenuManager manager = new MenuManager();
			
			IAction action = new DeleteRegionAction(getViewer());
			manager.add(action);
			action = new DownRegionAction(getViewer());
			manager.add(action);
			action = new UpRegionAction(getViewer());
			manager.add(action);
			getViewer().setContextMenu(manager);
			getSelectionSynchronizer().addViewer(getViewer());
			getViewer().setContents(screen);

//			getSelectionActions().add(action.getId());
		}
		
		
		public void dispose() {
			getSelectionSynchronizer().removeViewer(getViewer());
			super.dispose();
		}

		public Control getControl() {
			return getViewer().getControl();
		}
	}
	
	public static class GenerateCodeJob implements IRunnableWithProgress {
		
		private List<File> files;
		private int index;
		
		public GenerateCodeJob(List<File> files) {
			this.files = files;
		}
		
		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
			monitor.beginTask("开始生成...", files.size());
			while(!monitor.isCanceled() && index < files.size()) {
				File file = files.get(index);
				monitor.setTaskName("正在处理文件:" + file.getName());
				generateCode(file);
				index++;
				monitor.worked(1);
			}
			monitor.done();
		}
		
		private void generateCode(File file) {
			String fileName = file.getName();
			fileName = fileName.substring(0, fileName.length() - 3) + ".gtl";
			File newFile = new File(file.getParent(), fileName);
			boolean fileExists = newFile.exists();
			try {
				String versionString = Application.getInstance().getProjectData().getGTLVersion();
				int version = 3;
				if(versionString != null) {
					version = Integer.parseInt(versionString);
				}
				Screen screen = readScreenModel(new FileInputStream(file));
				if(screen != null) {
					CodeGenerator generator = new CodeGenerator();
					String s = generator.generate(screen, version ,null);
			        FileOutputStream fos = null;
			        try{
			            fos = new FileOutputStream(newFile);
			            fos.write(s.getBytes( Application.getInstance().getProjectData().sourceEncoding));
			        }catch(IOException e){
			            throw e;
			        }finally{
			            if(fos != null){
			                try{
			                    fos.close();
			                }catch(IOException e){
			                }
			            }
			        }
//					Utils.saveFileContent(newFile, s, Application.getInstance().getProjectData().sourceEncoding);
					System.out.println(file.getName() + " ok.");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
