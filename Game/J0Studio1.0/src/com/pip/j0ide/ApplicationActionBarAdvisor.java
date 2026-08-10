package com.pip.j0ide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.pip.gtl.remotedebugger.GTLDebugServer;
import com.pip.gtl.remotedebugger.ui.DebugSessionView;
import com.pip.gtl.remotedebugger.ui.MemoryView;
import com.pip.gtl.remotedebugger.ui.VariableView;
import com.pip.j0ide.editors.GTLEditor;
import com.pip.j0ide.i18n.GTLMessageFile;
import com.pip.j0ide.i18n.I18NLexUtil;
import com.pip.j0ide.i18n.IMessageFile;
import com.pip.j0ide.i18n.JavaMessageFile;
import com.pip.j0ide.i18n.Token;
import com.pip.uieditor.editor.AnchorPointDialog;
import com.pip.uieditor.editor.FontConfigDialog;
import com.pip.uieditor.editor.UIEditorConfigDialog;
import com.pip.uieditor.model.AnchorPoint;
import com.pip.util.Utils;
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
	private Action convertEncodingAction;
	private Action fetchStringsToAction;
	private Action searchI18nRelatedAction;
	private Action locateLastEipAction;
	private Action searchFunctionAction;
	private Action searchEipAction;
	private Action toggleBreakpointAction;
	private Action debugStopAction;
	private Action debugPauseAction;
	private Action debugStepOutAction;
	private Action debugStepOverAction;
	private Action debugStepAction;
	private Action debugRunAction;
	private Action openDeclarationAction;
	private Action markCommentAction;
	private Action setCompileThreadCountAction;
	private Action searchInFilesAction;
	private Action searchObjectAction;
	private Action searchGtlAction;
	private Action viewMemoryView;
	private Action viewVariableView;
	private Action viewDebugAction;
	private Action stopServerAction;
	private Action startServerAction;
	private Action viewOutlineAction;
	private Action viewOutputAction;
	private Action viewErrorsAction;
	private Action viewPropertySheetAction;
	private Action viewDirectoryAction;
	
	private IWorkbenchAction showViewMenuAction;
	private Action polishAction;
//	private Action uiResourceAction;
//	private Action uiAnimateAction;
	private Action uiEditorConfigAction;
	private Action fontConfigAction;
	private Action selectResourceAction;
	private IWorkbenchAction forwardHistoryAction;
	private IWorkbenchAction backwardHistoryAction;
	private IWorkbenchAction forwardAction;
	private IWorkbenchAction previousAction;
	private IWorkbenchAction upAction;
	private IWorkbenchAction backAction;
	private IWorkbenchAction nextAction;
	private IWorkbenchAction goIntoAction;
	private Action compileAction;
	private IWorkbenchAction findAction;
	private IWorkbenchAction selectAllAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction undoAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction closeAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction saveAction;
	
	private IWorkbenchWindow mainWindow;

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
		
		mainWindow = window;

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);
		{
			closeAction = ActionFactory.CLOSE.create(window);
			register(closeAction);
		}
		{
			closeAllAction = ActionFactory.CLOSE_ALL.create(window);
			register(closeAllAction);
		}
		{
			undoAction = ActionFactory.UNDO.create(window);
			register(undoAction);
		}
		{
			redoAction = ActionFactory.REDO.create(window);
			register(redoAction);
		}
		{
			copyAction = ActionFactory.COPY.create(window);
			register(copyAction);
		}
		{
			pasteAction = ActionFactory.PASTE.create(window);
			register(pasteAction);
		}
		{
			cutAction = ActionFactory.CUT.create(window);
			register(cutAction);
		}
		{
			deleteAction = ActionFactory.DELETE.create(window);
			register(deleteAction);
		}
		{
			selectAllAction = ActionFactory.SELECT_ALL.create(window);
			register(selectAllAction);
		}
		{
			findAction = ActionFactory.FIND.create(window);
			register(findAction);
		}

		compileAction = new Action("&Compile...") {
			public void run() {
				IEditorPart editor = mainWindow.getActivePage().getActiveEditor();
				if (editor != null && editor instanceof GTLEditor) {
					((GTLEditor)editor).onCompile();
				}
			}
		};
		compileAction.setEnabled(false);
		compileAction.setAccelerator(SWT.CTRL | SWT.SHIFT | 'B');
		compileAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("compile"));
		{
			goIntoAction = ActionFactory.GO_INTO.create(window);
			register(goIntoAction);
		}
		{
			nextAction = ActionFactory.NEXT.create(window);
			register(nextAction);
		}
		{
			backAction = ActionFactory.BACK.create(window);
			register(backAction);
		}
		{
			upAction = ActionFactory.UP.create(window);
			register(upAction);
		}
		{
			previousAction = ActionFactory.PREVIOUS.create(window);
			register(previousAction);
		}
		{
			forwardAction = ActionFactory.FORWARD.create(window);
			register(forwardAction);
		}
		{
			backwardHistoryAction = ActionFactory.BACKWARD_HISTORY.create(window);
			register(backwardHistoryAction);
		}
		{
			forwardHistoryAction = ActionFactory.FORWARD_HISTORY.create(window);
			register(forwardHistoryAction);
		}

		polishAction = new Action("Configure J2ME Polish...") {
			public void run() {
				DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
				dlg.setFilterPath(Settings.polishDir.getAbsolutePath());
				dlg.setText("选择目录");
				dlg.setMessage("请选择J2ME Polish 2.0根目录：");
				String newPath = dlg.open();
				if (newPath != null) {
					Settings.polishDir = new File(newPath);
				}
			}
		};
		
//		uiResourceAction = new Action("Configure UI Resource...") {
//			public void run() {
//				DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
//				dlg.setFilterPath(Settings.uiResourceDir.getAbsolutePath());
//				dlg.setText("选择目录");
//				dlg.setMessage("请选择UI资源目录：");
//				String newPath = dlg.open();
//				if (newPath != null) {
//					Settings.uiResourceDir = new File(newPath);
//				}
//			}
//		};
//		
//		uiAnimateAction = new Action("Configure Animate...") {
//			public void run()  {
//				DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
//				dlg.setFilterPath(Settings.uiAnimateDir.getAbsolutePath());
//				dlg.setText("选择目录");
//				dlg.setMessage("请选择动画资源目录：");
//				String newPath = dlg.open();
//				if (newPath != null) {
//					Settings.uiAnimateDir = new File(newPath);
//				}
//				
//			}
//		};
		
		fontConfigAction =  new Action("Config Font...") {
			public void run() {
				FontConfigDialog dlg = new FontConfigDialog(mainWindow.getShell());
				dlg.open();
//				if(dlg.open() == Window.OK) {
//					Settings.smallFont = dlg.smallFont;
//					Settings.mediumFont = dlg.mediumFont;
//					Settings.largeFont = dlg.largeFont;
//				}
			}
		};
		
		uiEditorConfigAction = new Action("Config UIEditor...") {
			public void run() {
				UIEditorConfigDialog dlg = new UIEditorConfigDialog(mainWindow.getShell());
				dlg.open();
			}
		};
		
		selectResourceAction = new Action("Select Resource") {
			public void run() {
				AnchorPointDialog dlg = new AnchorPointDialog(mainWindow.getShell());
				dlg.setData(new ArrayList<AnchorPoint>());
				dlg.open();
			}
		};
		
		{
			showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(window);
			register(showViewMenuAction);
		}

		viewDirectoryAction = new Action("Resource Explorer") {
			public void run() {
				try {
					window.getActivePage().showView(DirectoryView.ID);
				} catch (Exception e) {
				}
			}
		};
		viewDirectoryAction.setHoverImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/items.gif"));

		viewOutputAction = new Action("Console") {
			public void run() {
				try {
					window.getActivePage().showView(ConsoleView.ID);
				} catch (Exception e) {
				}
			}
		};
		viewOutputAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/output.gif"));

		viewErrorsAction = new Action("Errors") {
			public void run() {
				try {
					window.getActivePage().showView(ErrorsView.ID);
				} catch (Exception e) {
				}
			}
		};
		viewErrorsAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/errors.gif"));
		
		viewOutlineAction = new Action("Outline") {
			public void run() {
				try {
					window.getActivePage().showView(IPageLayout.ID_OUTLINE);
				} catch (Exception e) {
				}
			}
		};
		viewOutlineAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/outline.gif"));
		
		viewPropertySheetAction = new Action("Properties") {
			public void run() {
				try {
					window.getActivePage().showView(IPageLayout.ID_PROP_SHEET);
				} catch (Exception e) {
				}
			}
		};
		
		startServerAction = new Action("&Start Debug Server") {
			public void run() {
				if (GTLDebugServer.getInstance().isActive()) {
					return;
				}
				try {
					GTLDebugServer.getInstance().start();
					startServerAction.setEnabled(false);
					stopServerAction.setEnabled(true);
				} catch (Exception e) {
					String msg = "启动服务器失败：" + e.toString();
					MessageDialog.openError(mainWindow.getShell(), "错误", msg);
				}
			}
		};

		stopServerAction = new Action("S&top Debug Server") {
			public void run() {
				if (!GTLDebugServer.getInstance().isActive()) {
					return;
				}
				GTLDebugServer.getInstance().stop();
				startServerAction.setEnabled(true);
				stopServerAction.setEnabled(false);
			}
		};
		stopServerAction.setEnabled(false);
		
		viewDebugAction = new Action("Debug Sessions") {
			public void run() {
				try {
					window.getActivePage().showView(DebugSessionView.ID);
				} catch (Exception e) {
				}
			}
		};
		viewDebugAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/debug.gif"));
		
		try {
			GTLDebugServer.getInstance().start();
			startServerAction.setEnabled(false);
			stopServerAction.setEnabled(true);
		} catch (Exception e) {
		}

		viewVariableView = new Action("Variables") {
			public void run() {
				try {
					window.getActivePage().showView(VariableView.ID);
				} catch (Exception e) {
				}
			}
		};
		viewVariableView.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/variable.gif"));

		viewMemoryView = new Action("Memory") {
			public void run() {
				try {
					window.getActivePage().showView(MemoryView.ID);
				} catch (Exception e) {
				}
			}
		};
		viewMemoryView.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/memory.gif"));
	
		searchGtlAction = new Action("Search GTL...") {
			public void run() {
				DirectoryView view = (DirectoryView)window.getActivePage().findView(DirectoryView.ID);
				view.searchGTL();
			}
		};
		searchGtlAction.setAccelerator(SWT.CTRL | 'G');

		searchObjectAction = new Action("Search Object...") {
			public void run() {
				IEditorPart editor = mainWindow.getActivePage().getActiveEditor();
				if (editor != null && editor instanceof GTLEditor) {
					((GTLEditor)editor).onSearchObject();
				}
			}
		};
		searchObjectAction.setEnabled(false);
		searchObjectAction.setAccelerator(SWT.CTRL | 'O');

		searchInFilesAction = new Action("Search In Files...") {
			public void run() {
				DirectoryView view = (DirectoryView)window.getActivePage().findView(DirectoryView.ID);
				view.searchInFiles();
			}
		};
		searchInFilesAction.setAccelerator(SWT.CTRL | SWT.SHIFT | 'G');

		
		markCommentAction = new Action("Mark Comment") {
			public void run() {
				IEditorPart editor = mainWindow.getActivePage().getActiveEditor();
				if (editor != null && editor instanceof GTLEditor) {
					((GTLEditor)editor).onMarkComment();
				}
			}
		};
		markCommentAction.setAccelerator(SWT.CTRL + '/');
		
		openDeclarationAction = new Action("Open Declaration...") {
			public void run() {
				IEditorPart editor = mainWindow.getActivePage().getActiveEditor();
				if (editor != null && editor instanceof GTLEditor) {
					((GTLEditor)editor).onOpenDeclaration();
				}
			}
		};
		openDeclarationAction.setAccelerator(SWT.F3);
		
		setCompileThreadCountAction = new Action("Compile Thread Count") {
			public void run() {
		        IInputValidator validator = new IInputValidator() {
		            public String isValid(String newText) {
		            	try{
		            		Integer.parseInt(newText);
		            		return null;
		            	} catch(Exception e) {
		            		return "Not a nubmer";
		            	}		                
		            }
		          };
				InputDialog input = new InputDialog(mainWindow.getShell(),
						"Compile Thread Count", "Please input compiling thread count here:",
						Settings.compileThreadCount, validator);
				if(input.open()== Window.OK) {
					Settings.compileThreadCount = input.getValue();
				}
						  
			}
		};
		setCompileThreadCountAction.setAccelerator(SWT.CTRL | 'T');

		debugRunAction = new Action("Run") {
			public void run() {
				DebugSessionView view = (DebugSessionView)mainWindow.getActivePage().findView(DebugSessionView.ID);
				if (view != null) {
					view.onRun();
				}
			}
		};
		debugRunAction.setAccelerator(SWT.F8);
		debugRunAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/run.gif"));
		debugRunAction.setEnabled(false);

		debugStepAction = new Action("Step Into") {
			public void run() {
				DebugSessionView view = (DebugSessionView)mainWindow.getActivePage().findView(DebugSessionView.ID);
				if (view != null) {
					view.onStep();
				}
			}
		};
		debugStepAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/step.gif"));
		debugStepAction.setEnabled(false);
		debugStepAction.setAccelerator(SWT.F4);

		debugStepOverAction = new Action("Step Over") {
			public void run() {
				DebugSessionView view = (DebugSessionView)mainWindow.getActivePage().findView(DebugSessionView.ID);
				if (view != null) {
					view.onStepOver();
				}
			}
		};
		debugStepOverAction.setAccelerator(SWT.F6);
		debugStepOverAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/stepover.gif"));
		debugStepOverAction.setEnabled(false);

		debugStepOutAction = new Action("Step Out") {
			public void run() {
				DebugSessionView view = (DebugSessionView)mainWindow.getActivePage().findView(DebugSessionView.ID);
				if (view != null) {
					view.onStepOut();
				}
			}
		};
		debugStepOutAction.setAccelerator(SWT.F7);
		debugStepOutAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/stepout.gif"));
		debugStepOutAction.setEnabled(false);

		debugPauseAction = new Action("Pause") {
			public void run() {
				DebugSessionView view = (DebugSessionView)mainWindow.getActivePage().findView(DebugSessionView.ID);
				if (view != null) {
					view.onPause();
				}
			}
		};
		debugPauseAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/pause.gif"));
		debugPauseAction.setEnabled(false);

		debugStopAction = new Action("Stop") {
			public void run() {
				DebugSessionView view = (DebugSessionView)mainWindow.getActivePage().findView(DebugSessionView.ID);
				if (view != null) {
					view.onStop();
				}
			}
		};
		debugStopAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/stop.gif"));
		debugStopAction.setEnabled(false);

		toggleBreakpointAction = new Action("Toggle Breakpoint") {
			public void run() {
				IEditorPart editor = mainWindow.getActivePage().getActiveEditor();
				if (editor != null && editor instanceof GTLEditor) {
					((GTLEditor)editor).onToggleBreakpoint();
				}
			}
		};
		toggleBreakpointAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/togglebreakpoint.gif"));
		toggleBreakpointAction.setEnabled(false);
		toggleBreakpointAction.setAccelerator(SWT.F9);

        searchEipAction = new Action("Search EIP...") {
            public void run() {
                SearchEipDialog dlg = new SearchEipDialog(mainWindow.getShell());
                dlg.open();
            }
        };

        searchFunctionAction = new Action("Search Function...") {
            public void run() {
                SearchFunctionDialog dlg = new SearchFunctionDialog(mainWindow.getShell());
                dlg.open();
            }
        };

		locateLastEipAction = new Action("Locate Last EIP...") {
			public void run() {
				DebugSessionView view = (DebugSessionView)mainWindow.getActivePage().findView(DebugSessionView.ID);
				if (view != null) {
					view.locateLastEIP();
				}
			}
		};

		searchI18nRelatedAction = new Action("Search I18N Related Strings...") {
			public void run() {
				DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
				dlg.setFilterPath(Settings.workingDir.getAbsolutePath());
				dlg.setText("选择目录");
				dlg.setMessage("请选择源代码所在目录：");
				String newPath = dlg.open();
				if (newPath == null) {
					return;
				}
				
				// 选择编码
				List<String> options = new ArrayList<String>();
				options.add("GBK");
				options.add("UTF-8");
				GenericChooseDialog dlg2 = new GenericChooseDialog(mainWindow.getShell(), "选择文件编码", options);
				if (dlg2.open() != Dialog.OK) {
					return;
				}
				String encoding = (String)dlg2.getSelection();
				
				SearchResultView.searchResults.clear();
				Set<String> fileSet = new HashSet<String>();
				File root = new File(newPath);
				Utils.findFilesInDir(root, ".java", fileSet);
				Utils.findFilesInDir(root, ".gtl", fileSet);
				Utils.findFilesInDir(root, ".h", fileSet);
				Utils.findFilesInDir(root, ".as", fileSet);
				String[] files = new String[fileSet.size()];
				fileSet.toArray(files);
				Arrays.sort(files);
				for (int i = 0; i < files.length; i++) {
					List<Token> tks = I18NLexUtil.findI18NRelatedString(files[i], encoding);
					for (Token t : tks) {
						SearchResultView.SearchResult sr = new SearchResultView.SearchResult();
    					sr.file = new File(files[i]);
    					sr.lineNo = t.beginLine - 1;
    					sr.column = t.beginColumn;
    					sr.length = 0;
    					sr.lineContent = t.image;
    					SearchResultView.searchResults.add(sr);
					}
					if (tks.size() > 0) {
						SearchResultView view = (SearchResultView)mainWindow.getActivePage().findView(SearchResultView.ID);
						if (view == null) {
							try {
								view = (SearchResultView)mainWindow.getActivePage().showView(SearchResultView.ID);
							} catch (Exception e) {
							}
						}
						if (view != null) {
							view.refresh();
							mainWindow.getActivePage().activate(view);
						}
					}
				}
			}
		};

		fetchStringsToAction = new Action("Fetch Strings To Message File...") {
			public void run() {
				// 选择目录
				DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
				dlg.setFilterPath(Settings.workingDir.getAbsolutePath());
				dlg.setText("选择目录");
				dlg.setMessage("请选择源代码所在目录：");
				String newPath = dlg.open();
				if (newPath == null) {
					return;
				}
				
				// 选择编码
				List<String> options = new ArrayList<String>();
				options.add("GBK");
				options.add("UTF-8");
				GenericChooseDialog dlg2 = new GenericChooseDialog(mainWindow.getShell(), "选择文件编码", options);
				if (dlg2.open() != Dialog.OK) {
					return;
				}
				String encoding = (String)dlg2.getSelection();
				
				// 自动判断是java文件还是gtl文件
				Set<String> fileSet = new HashSet<String>();
				File root = new File(newPath);
				Utils.findFilesInDir(root, ".java", fileSet);
				boolean isJava;
				if (fileSet.isEmpty()) {
					isJava = false;
					Utils.findFilesInDir(root, ".gtl", fileSet);
					Utils.findFilesInDir(root, ".h", fileSet);
				} else {
					isJava = true;
				}
				if (fileSet.isEmpty()) {
					MessageDialog.openError(mainWindow.getShell(), "错误", "目录下没有源代码文件。");
					return;
				}
				
				try {
					// 选择消息文件
					IMessageFile mf;
					File mFile;
					if (isJava) {
						FileDialog fdlg = new FileDialog(mainWindow.getShell(), SWT.OPEN);
						fdlg.setFilterPath(newPath);
						fdlg.setFilterExtensions(new String[] { "*.java" });
						fdlg.setFilterNames(new String[] { "Java消息文件(*.java)" });
						String path = fdlg.open();
						if (path == null) {
							return;
						}
						mFile = new File(path);
						mf = new JavaMessageFile(mFile, encoding);
					} else {
						FileDialog fdlg = new FileDialog(mainWindow.getShell(), SWT.OPEN);
						fdlg.setFilterPath(newPath);
						fdlg.setFilterExtensions(new String[] { "*.h" });
						fdlg.setFilterNames(new String[] { "GTL消息文件(*.h)" });
						String path = fdlg.open();
						if (path == null) {
							return;
						}
						mFile = new File(path);
						mf = new GTLMessageFile(mFile, encoding);
					}
					
					for (String srcf : fileSet) {
						if (new File(srcf).equals(mFile)) {
							continue;
						}
						if (new File(srcf).getName().startsWith("AutoGenerated")) {
							continue;
						}
						if (new File(srcf).getName().startsWith("messages_") && 
								new File(srcf).getName().endsWith(".h")) {
							continue;
						}
						try {
							I18NLexUtil.fetchI18NMessages(srcf, encoding, mf);
						} catch (Exception e) {
							MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
						}
					}
					
					if (mf.getNewCount() > 0) {
						int count = mf.getNewCount();
						mf.save();
						MessageDialog.openInformation(mainWindow.getShell(), "完成", "处理完成，新增" + count + "条消息。");
					} else {
						MessageDialog.openInformation(mainWindow.getShell(), "完成", "处理完成，没有新增消息。");
					}
				} catch (Exception e) {
					MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
				}
			}
		};

		convertEncodingAction = new Action("Convert Encoding Of Source Code...") {
			public void run() {
				// 选择目录
				DirectoryDialog dlg = new DirectoryDialog(mainWindow.getShell());
				dlg.setFilterPath(Settings.workingDir.getAbsolutePath());
				dlg.setText("选择目录");
				dlg.setMessage("请选择源代码所在目录：");
				String newPath = dlg.open();
				if (newPath == null) {
					return;
				}
				
				// 选择编码
				List<String> options = new ArrayList<String>();
				options.add("GBK");
				options.add("UTF-8");
				GenericChooseDialog dlg2 = new GenericChooseDialog(mainWindow.getShell(), "选择文件编码", options);
				if (dlg2.open() != Dialog.OK) {
					return;
				}
				String sourceEncoding = (String)dlg2.getSelection();
				
				// 选择目标编码
				dlg2 = new GenericChooseDialog(mainWindow.getShell(), "选择目标编码", options);
				if (dlg2.open() != Dialog.OK) {
					return;
				}
				String targetEncoding = (String)dlg2.getSelection();
				
				// 处理java，h，gtl文件
				Set<String> fileSet = new HashSet<String>();
				File root = new File(newPath);
				Utils.findFilesInDir(root, ".java", fileSet);
				Utils.findFilesInDir(root, ".gtl", fileSet);
				Utils.findFilesInDir(root, ".h", fileSet);
				if (fileSet.isEmpty()) {
					MessageDialog.openError(mainWindow.getShell(), "错误", "目录下没有源代码文件。");
					return;
				}
				
				try {
					for (String srcf : fileSet) {
						byte[] data = Utils.loadFileData(new File(srcf));
						String content = new String(data, sourceEncoding);
						data = content.getBytes(targetEncoding);
						Utils.saveFileData(new File(srcf), data);
					}
				} catch (Exception e) {
					MessageDialog.openError(mainWindow.getShell(), "错误", e.toString());
				}
			}
		};
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);

		fileMenu.add(polishAction);
		
//		fileMenu.add(uiResourceAction);
//		
//		fileMenu.add(uiAnimateAction);
		fileMenu.add(uiEditorConfigAction);
		
		fileMenu.add(fontConfigAction);
		
		fileMenu.add(selectResourceAction);

		fileMenu.add(searchI18nRelatedAction);

		fileMenu.add(fetchStringsToAction);

		fileMenu.add(convertEncodingAction);

		fileMenu.add(new Separator());
		fileMenu.add(saveAction);

		fileMenu.add(closeAction);

		fileMenu.add(closeAllAction);

		fileMenu.add(new Separator());
		fileMenu.add(exitAction);

		final MenuManager menuManager = new MenuManager("&Edit");
		menuBar.add(menuManager);

		menuManager.add(undoAction);

		menuManager.add(redoAction);

		menuManager.add(new Separator());

		menuManager.add(copyAction);

		menuManager.add(pasteAction);

		menuManager.add(cutAction);

		menuManager.add(new Separator());

		menuManager.add(deleteAction);

		menuManager.add(selectAllAction);

		menuManager.add(findAction);

		final MenuManager navigateMenu = new MenuManager("&Navigate");
		menuBar.add(navigateMenu);

		navigateMenu.add(goIntoAction);

		final MenuManager menuManager_2 = new MenuManager("Go To");
		navigateMenu.add(menuManager_2);

		menuManager_2.add(nextAction);

		menuManager_2.add(backAction);

		menuManager_2.add(upAction);

		navigateMenu.add(new Separator());

		navigateMenu.add(previousAction);

		navigateMenu.add(forwardAction);

		navigateMenu.add(new Separator());

		navigateMenu.add(backwardHistoryAction);

		navigateMenu.add(forwardHistoryAction);

		final MenuManager gtlMenu = new MenuManager("&GTL");
		menuBar.add(gtlMenu);

		gtlMenu.add(compileAction);

		gtlMenu.add(searchGtlAction);

		gtlMenu.add(searchInFilesAction);

		gtlMenu.add(searchObjectAction);

		gtlMenu.add(markCommentAction);
		
		gtlMenu.add(openDeclarationAction);
		
		gtlMenu.add(setCompileThreadCountAction);

		final MenuManager debugMenu = new MenuManager("&Debug");
		menuBar.add(debugMenu);

		debugMenu.add(startServerAction);

		debugMenu.add(stopServerAction);

		debugMenu.add(new Separator());

		debugMenu.add(debugRunAction);

		debugMenu.add(debugStepAction);

		debugMenu.add(debugStepOverAction);

		debugMenu.add(debugStepOutAction);

		debugMenu.add(debugPauseAction);

		debugMenu.add(debugStopAction);

		debugMenu.add(new Separator());

		debugMenu.add(toggleBreakpointAction);

		debugMenu.add(searchEipAction);

		debugMenu.add(searchFunctionAction);

		debugMenu.add(locateLastEipAction);

		final MenuManager viewMenu = new MenuManager("&View");
		menuBar.add(viewMenu);

		viewMenu.add(viewDirectoryAction);

		viewMenu.add(viewOutputAction);
		
		viewMenu.add(viewOutlineAction);
		
		viewMenu.add(viewPropertySheetAction);

		viewMenu.add(viewDebugAction);

		viewMenu.add(viewVariableView);

		viewMenu.add(viewMemoryView);

		viewMenu.add(viewErrorsAction);
	}

    protected void fillCoolBar(ICoolBarManager coolBar) {
    	final ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
    	coolBar.add(toolBarManager);

    	toolBarManager.add(saveAction);

    	toolBarManager.add(compileAction);

    	toolBarManager.add(new Separator());

    	toolBarManager.add(debugRunAction);

    	toolBarManager.add(debugStepAction);

    	toolBarManager.add(debugStepOverAction);

    	toolBarManager.add(debugStepOutAction);

    	toolBarManager.add(debugPauseAction);

    	toolBarManager.add(debugStopAction);

    	toolBarManager.add(toggleBreakpointAction);
    }   
}
