package com.pip.image.workshop;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import com.pip.image.workshop.WorkshopPlugin;
import com.swtdesigner.ResourceManager;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private Action projectViewAction;
	private Action optimizeColorsAction;
	private Action openLogDirAction;
	private Action viewTileViewAction;
	private Action viewTileLibraryAction;
	private Action viewDirectoryAction;
	private Action chooseImageEditorAction;
	private IWorkbenchAction newWizardDropDownAction;
	private Action redoAction;
	private Action undoAction;
	private Action sizeAction6;
	private Action sizeAction5;
	private Action sizeAction4;
	private Action sizeAction3;
	private Action sizeAction2;
	private Action sizeAction1;
	private IWorkbenchAction closeAllSavedAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction saveAsAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction closeAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchWindow window;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		this.window = window;
		
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		{
			closeAction = ActionFactory.CLOSE.create(window);
			register(closeAction);
		}
		{
			closeAllAction = ActionFactory.CLOSE_ALL.create(window);
			register(closeAllAction);
		}
		{
			saveAction = ActionFactory.SAVE.create(window);
			register(saveAction);
		}
		{
			saveAsAction = ActionFactory.SAVE_AS.create(window);
			register(saveAsAction);
		}
		{
			saveAllAction = ActionFactory.SAVE_ALL.create(window);
			register(saveAllAction);
		}
		{
			closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(window);
			register(closeAllSavedAction);
		}

		sizeAction1 = new Action("176x208") {
			public void run() {
				WorkshopApplication.setScreenSize(176, 208);
			}
		};

		sizeAction2 = new Action("240x320") {
			public void run() {
				WorkshopApplication.setScreenSize(240, 320);
			}
		};

		sizeAction3 = new Action("208x208") {
			public void run() {
				WorkshopApplication.setScreenSize(208, 208);
			}
		};

		sizeAction4 = new Action("128x128") {
			public void run() {
				WorkshopApplication.setScreenSize(128, 128);
			}
		};

		sizeAction5 = new Action("128x160") {
			public void run() {
				WorkshopApplication.setScreenSize(128, 160);
			}
		};

		sizeAction6 = new Action("320x240") {
			public void run() {
				WorkshopApplication.setScreenSize(320, 240);
			}
		};

		undoAction = new Action("&Undo") {
			public void run() {
				this.firePropertyChange("chosen", this, this);
			}
		};
		undoAction.setEnabled(false);
		undoAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/undo_edit(1).gif"));
		undoAction.setDisabledImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/undo_edit.gif"));
		undoAction.setAccelerator(SWT.CTRL | 'z');

		redoAction = new Action("&Redo") {
			public void run() {
				this.firePropertyChange("chosen", this, this);
			}
		};
		redoAction.setEnabled(false);
		redoAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/redo_edit(1).gif"));
		redoAction.setDisabledImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/redo_edit.gif"));
		redoAction.setAccelerator(SWT.CTRL | 'y');
		{
			newWizardDropDownAction = ActionFactory.NEW_WIZARD_DROP_DOWN.create(window);
			register(newWizardDropDownAction);
		}

		chooseImageEditorAction = new Action("&Choose Image Editor...") {
			public void run() {
				onChooseImageEditor();
			}
		};

		viewDirectoryAction = new Action("Resource Explorer") {
			public void run() {
				try {
					window.getActivePage().showView(DirectoryView.ID);
				} catch (Exception e) {
				}
			}
		};
		viewDirectoryAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/items.gif"));

		viewTileLibraryAction = new Action("Tile Library") {
			public void run() {
				try {
					window.getActivePage().showView(TileLibView.ID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		viewTileLibraryAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/tilelib.gif"));

		viewTileViewAction = new Action("Tile Viewer") {
			public void run() {
				try {
					window.getActivePage().showView(TileView.ID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		viewTileViewAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/tiles.gif"));

		optimizeColorsAction = new Action("Optimize PNG Colors...") {
			public void run() {
				new PNGColorOptimizer().run(window.getShell());
			}
		};
		
		openLogDirAction = new Action("打开日志目录"){
			public void run(){
				String dir = Settings.logDir;
				String cmd = "explorer.exe \"" + dir + "\"";
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (Exception e) {
                }
			}
		};
		
		projectViewAction = new Action("ProjectView"){
			public void run(){
				try {
					window.getActivePage().showView(ProjectView.ID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		projectViewAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(WorkshopPlugin.getDefault(), "icons/project.gif"));
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);

		fileMenu.add(optimizeColorsAction);
		fileMenu.add(openLogDirAction);

		fileMenu.add(new Separator());

		fileMenu.add(saveAction);

		fileMenu.add(saveAsAction);

		fileMenu.add(saveAllAction);

		fileMenu.add(new Separator());

		fileMenu.add(closeAction);

		fileMenu.add(closeAllAction);

		fileMenu.add(closeAllSavedAction);

		fileMenu.add(new Separator());
		fileMenu.add(exitAction);

		final MenuManager menuManager = new MenuManager("&Edit",
				IWorkbenchActionConstants.M_EDIT);
		menuBar.add(menuManager);

		menuManager.add(undoAction);

		menuManager.add(redoAction);

		final MenuManager sizeMenu = new MenuManager("&Screen Size");
		menuManager.add(sizeMenu);

		sizeMenu.add(sizeAction1);

		sizeMenu.add(sizeAction2);

		sizeMenu.add(sizeAction3);

		sizeMenu.add(sizeAction4);

		sizeMenu.add(sizeAction5);

		sizeMenu.add(sizeAction6);

		menuManager.add(chooseImageEditorAction);

		final MenuManager viewMenu = new MenuManager("&View");
		menuBar.add(viewMenu);

		viewMenu.add(viewDirectoryAction);

		viewMenu.add(viewTileLibraryAction);

		viewMenu.add(viewTileViewAction);
		
		viewMenu.add(projectViewAction);
	}

    protected void fillCoolBar(ICoolBarManager coolBar) {
    	final ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
    	coolBar.add(toolBarManager);

    	toolBarManager.add(saveAction);

    	toolBarManager.add(new Separator());

    	toolBarManager.add(undoAction);

    	toolBarManager.add(redoAction);
    }
    
    private void onChooseImageEditor() {
    	ChooseImageEditorDialog dlg = new ChooseImageEditorDialog(window.getShell());
    	dlg.setCmd(Settings.imageEditor);
    	dlg.setArg(Settings.imageEditorArg);
    	if (dlg.open() == ChooseImageEditorDialog.OK) {
    		Settings.imageEditor = dlg.getCmd();
    		Settings.imageEditorArg = dlg.getArg();
    	}
    }
}
