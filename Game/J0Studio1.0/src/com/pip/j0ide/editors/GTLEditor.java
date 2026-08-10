package com.pip.j0ide.editors;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import com.pip.gtl.codegen.CodeGenException;
import com.pip.gtl.codegen.GTLFunctionCallGenerator;
import com.pip.gtl.codegen.GTLProgGenerator;
import com.pip.gtl.codegen.syscall.SystemFunctionManager;
import com.pip.gtl.codegen.syscall.SystemFunctionPrototype;
import com.pip.gtl.compiler.GTLCompiler;
import com.pip.gtl.compiler.GTLPreCompiler;
import com.pip.gtl.compiler.GTLPreCompiler.StructDef;
import com.pip.gtl.decompiler.ETFDebugInfo;
import com.pip.gtl.remotedebugger.GTLDebugManager;
import com.pip.gtl.remotedebugger.GTLDebugServer;
import com.pip.gtl.remotedebugger.GTLDebugSession;
import com.pip.gtl.remotedebugger.VariableItem;
import com.pip.gtl.remotedebugger.ui.VariableView;
import com.pip.gtleditor.GTLEditorImpl;
import com.pip.gtleditor.GTLTextHover;
import com.pip.j0ide.Activator;
import com.pip.j0ide.Application;
import com.pip.j0ide.ConsoleView;
import com.pip.j0ide.ErrorsView;
import com.pip.j0ide.Settings;
import com.pip.j0ide.data.Model;
import com.pip.j0ide.data.ProjectData;
import com.pip.j0ide.data.Variable;
import com.pip.util.Utils;

public class GTLEditor extends GTLEditorImpl {
	private Action compileAction;
	private Action addWatchAction;
	
	/**
	 * 一个编译工作过程。不同机型可以用不同线程来编译。
	 */
	public static class CompilerJob implements IRunnableWithProgress {
		private File[] sourceFiles;
		private Model[] targetDevices;
		private File outputDir;
		private int maxThreads;
		private boolean forceCompile;
		private boolean forceReturnCheck;
		
		private AtomicInteger currentModelIndex = new AtomicInteger(0);
		private AtomicInteger finishedCount = new AtomicInteger(0);
		private Thread[] workingThreads;
		private static boolean canceled = false;
		private boolean hasError = false;
		
		/*
		 * 一个工作线程，负责一个机型的编译工作。
		 */
		private class WorkingThread extends Thread {
			public void run() {
				while (!canceled) {
					// 获取一个没有被分配的机型，如果所有机型都被分配了，退出线程
					int modelIndex = currentModelIndex.getAndIncrement();
					if (modelIndex >= targetDevices.length) {
						break;
					}

					// 获取机型参数
					Model device = targetDevices[modelIndex];
    				HashMap<String, String> params = new HashMap<String, String>();
    				ProjectData proj = Application.getInstance().getProjectData();
    				for (int j = 0; j < proj.variables.size(); j++) {
    				    params.put(proj.variables.get(j).name, proj.variables.get(j).value);
    				}
    				for (int j = 0; j < device.variables.size(); j++) {
    					params.put(device.variables.get(j).name, device.variables.get(j).value);
    				}

    				// 逐个编译所有文件
					Set<File> compiledList = new HashSet<File>();
					Map<File, ETFDebugInfo> etdCache = new HashMap<File, ETFDebugInfo>();
					for (int i = 0; i < sourceFiles.length; i++) {
						if (canceled) {
							break;
						}
						File sourceFile = sourceFiles[i];
	    				if (compiledList.contains(sourceFile)) {
	    					finishedCount.incrementAndGet();
	    				    continue;
	    				}
//	    	        	try {
//	    	        		Application.getInstance().getConsole().syncPrintln("正在编译" + sourceFile.getName() + "/" + device.id + "...");
//	    	        	} catch (Exception e) {
//	    	        	}
	    	        	String msg = "编译" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), device.id) + "  成功！";
	    				long startTime = System.currentTimeMillis();
	    		        try {
	    		            GTLCompiler comp = new GTLCompiler(Settings.polishDir.getAbsolutePath(), device.id, device.device, params);
	    		            
	    		            //检测是否需要进行revision代码限定
	    		            boolean limitRevision = false;
	    		            String rv = params.get("Revision");
	    		            if(rv != null){
	    		                for(Variable v : proj.targetsLimit){
	    		                    if(rv.equals(v.name) && v.value.equals("true")){
	    		                        limitRevision = true;
	    		                        break;
	    		                    }
	    		                }
	    		            }
	    		            if(limitRevision){
	    		                comp.addLimitRevision(rv);
	    		            }
	    		            
	    		            comp.setTargetPath(new File(outputDir, device.id));
	    		            File file = comp.compile(sourceFile, forceCompile, compiledList, etdCache);
	    		            System.out.println(sourceFile);
	    		            if(comp.limitRevisionNeedPass()){
	    		                msg = "由于没有指定Revision的相关代码，跳过" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), device.id);
	    		            } else if (comp.isIgnored()) {
	    		            	msg = "检测到没有改变，跳过" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), device.id);
	    		            }
	    		            if(file == null){
	    		            	msg = "编译" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), device.id) +"  跳过！";
	    		            }
	    		        }catch (CodeGenException cge) {
	    		            if (!hasError) {
	    		                Application.getInstance().getConsole().getSite().getShell().getDisplay().asyncExec(new EditAndGotoJob(cge.getFile(), cge.getErrorLine() - 1, true));
	    		            }
	    		            msg = "Error:"+sourceFile.getName()+"\n"+cge.toString();
	    		        	hasError = true;
	    		        	cge.printStackTrace();
	    		        	String emsg = cge.getMessage();
	    		        	if (emsg.contains("\n")) {
	    		        		emsg = emsg.substring(0, emsg.indexOf("\n"));
	    		        	}
	    		        	Application.getInstance().getErrorsView().addError(sourceFile.getName(), cge.getFile(), cge.getErrorLine(), emsg);
	    		        } catch (Throwable e) {
	    		        	msg = "Error:"+sourceFile.getName()+"\n"+e.toString();
	                        hasError = true;
	                        e.printStackTrace();
	                        Application.getInstance().getErrorsView().addError(sourceFile.getName(), sourceFile, 1, e.toString());
	    		        }
	    	        	try {
	    	        	    long endTime = System.currentTimeMillis();
	    	        		Application.getInstance().getConsole().syncPrintln(msg + " 耗时：" + (endTime - startTime) + "ms");
	    	        	} catch (Exception e) {
	    	        	}
	    	        	finishedCount.incrementAndGet();
					}
				}
				String finalS = "======= 编译成功  ========";
	            if(hasError){
	            	finalS = "======= 出现错误  ========";
	            }
	        	try {
	        		Application.getInstance().getConsole().syncPrintln(finalS);
	        	} catch (Exception e) {
	        	}
			}
		}
		
		public CompilerJob(File[] file, Model[] models, File od, int maxThreads, boolean force, boolean returnCheck) {
			sourceFiles = file;
			targetDevices = models;
			outputDir = od;
			this.maxThreads = maxThreads;
			if (this.maxThreads > models.length) {
				this.maxThreads = models.length;
			}
			forceCompile = force;
			forceReturnCheck = returnCheck;
			canceled = false;
		}
		
		public void run(IProgressMonitor monitor) {
			monitor.beginTask("开始编译...", targetDevices.length * sourceFiles.length);
			
			// 创建多个工作线程
			workingThreads = new Thread[maxThreads];
			for (int i = 0; i < maxThreads; i++) {
				workingThreads[i] = new WorkingThread();
				workingThreads[i].start();
			}
			
			// 循环等待线程结束
			int lastFinishCount = 0;
			while (!monitor.isCanceled()) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				int newCount = finishedCount.get();
				if (newCount > lastFinishCount) {
					monitor.worked(newCount - lastFinishCount);
					lastFinishCount = newCount;
					if (newCount >= targetDevices.length * sourceFiles.length) {
						break;
					}
				}
				monitor.setTaskName(maxThreads + "个线程正在编译..." + newCount + "/" + (targetDevices.length * sourceFiles.length));
			}
			canceled = true;
            monitor.done();
            canceled = true;
		}
	}
		
	public GTLEditor() {
		super();
		
		compileAction = new Action("&Compile...") {
			public void run() {
				onCompile();
			}
		};
		compileAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("compile"));
		
		addWatchAction = new Action("Add Watch") {
			public void run() {
				onAddWatch();
			}
		};
	}
	
	public void dispose() {
		super.dispose();
	}

	public void onCompile() {
		if (!getSite().getPage().saveEditor(this, false)) {
			return;
		}
		FileStoreEditorInput input = (FileStoreEditorInput)getEditorInput();
		URI url = input.getURI();
		String gtlFile = Utils.urlToPath(url);

		ChooseModelDialog modelDlg = new ChooseModelDialog(getSite().getShell());
		modelDlg.setDefaultPath(new File(gtlFile).getParent());
		if (modelDlg.open() != IDialogConstants.OK_ID) {
			return;
		}
		String outputPath = modelDlg.getOutputPath();
		File targetDir = new File(gtlFile).getParentFile();
		if (outputPath.length() > 0) {
			targetDir = ChooseModelDialog.resolveFile(targetDir, outputPath);
		}
		Model[] targetModels = modelDlg.getChoosenModels();
		boolean force = modelDlg.isForceCompile();
		boolean returnCheck = modelDlg.isForceReturnCheck();
		
		// 显示并清空Output窗口
		try {
			getSite().getWorkbenchWindow().getActivePage().showView(ErrorsView.ID);
			Application.getInstance().getErrorsView().clear();
			getSite().getWorkbenchWindow().getActivePage().showView(ConsoleView.ID);
			Application.getInstance().getConsole().clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//拷贝.info
        try {
			modelDlg.copyDotInfo();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		CompilerJob job = new CompilerJob(new File[] { new File(gtlFile) }, targetModels, targetDir, Integer.parseInt(Settings.compileThreadCount), force, returnCheck);
		ProgressMonitorDialog progress = new ProgressMonitorDialog(getSite().getShell());
		progress.setCancelable(true);
		try {
			progress.run(true, true, job);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onAddWatch() {
		String str = this.getSourceViewer().getTextWidget().getSelectionText();
		if (str == null || str.trim().isEmpty()) {
			return;
		}
		VariableView view = (VariableView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(VariableView.ID);
		if (view != null) {
			view.addWatch(str.trim());
		}
	}

	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		menu.add(compileAction);
		menu.add(addWatchAction);
	}
	
	/**
	 * 显示一个搜索符号的窗口。
	 */
	public void onSearchObject() {
		if (this.getParser() == null) {
			return;
		}
		
		Rectangle rect = this.getSourceViewer().getTextWidget().getBounds();
		Point topLeft = this.getSourceViewer().getTextWidget().toDisplay(rect.x, rect.y);
		rect.x = topLeft.x;
		rect.y = topLeft.y;
		int xoff = (rect.width - 400) / 2;
		int yoff = (rect.height - 400) / 2;
		rect.x += xoff;
		rect.y += yoff;
		rect.width = 400;
		rect.height = 400;
		
		try {
			SearchSymbolWindow win = new SearchSymbolWindow(this);
			win.setBounds(rect);
			win.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected Object findDeclaration(String name, GTLPreCompiler.FunctionDef func) {
		GTLPreCompiler parser = getParser();
		
		// search system functions
		Object ret = SystemFunctionManager.getFunction(name);
		if (ret != null) {
			return ret;
		}
		
		// search global variables, macros and custom functions
		for (GTLPreCompiler.VariableDef globalVar : parser.getGlobalVars()) {
			if (globalVar.name.equals(name)) {
				return globalVar;
			}
		}
		
		StructDef struct2 = parser.findStruct(name);
		if(struct2 != null) {
			return struct2;
		}
		
//		for (StructDef struct : parser.getStructs()) {
//			if (struct.name.equals(name)) {
//				return struct;
//			}
//		}
		
		for (GTLPreCompiler.FunctionDef func1 : parser.getFunctions()) {
			if (func1.id < 0) {
				continue;
			}
			if (func1.name.equals(name)) {
				return func1;
			}
		}
		for (String macro : parser.getMacros()) {
			if (macro.equals(name)) {
				return macro;
			}
		}
		
		// list function params and local variables if in a function
		if (func != null) {
			for (GTLPreCompiler.VariableDef param : func.params) {
				if (param.name.equals(name)) {
					return param;
				}
			}
			for (GTLPreCompiler.VariableDef localvar : func.localVariables.values()) {
				if (localvar.name.equals(name)) {
					return localvar;
				}
			}
		}
		return null;
	}
	
	public void onOpenDeclaration() {
		if (getParser() == null) {
			return;
		}
		int offset = this.getSourceViewer().getSelectedRange().x;
		GTLPreCompiler parser = getParser();
		try {
			GTLPreCompiler.FunctionDef func = parser.findFunction(getEditingFile(), getSourceViewer().getDocument().getLineOfOffset(offset));
			int[] pt = GTLTextHover.getWord(this.getSourceViewer().getDocument(), offset);
			String varName = this.getSourceViewer().getDocument().get(pt[0], pt[1] - pt[0]);
			Object obj = findDeclaration(varName, func);
			int lineNo;
			if (obj instanceof SystemFunctionPrototype) {
				File file = ((SystemFunctionPrototype)obj).srcFile;
				int fileLine = ((SystemFunctionPrototype)obj).charIndex;
				new EditAndGotoJob(file, fileLine, false).run();
				return;
			} else if (obj instanceof GTLPreCompiler.FunctionDef) {
				lineNo = ((GTLPreCompiler.FunctionDef)obj).lineNo;
			} else if (obj instanceof GTLPreCompiler.VariableDef) {
				lineNo = ((GTLPreCompiler.VariableDef)obj).lineNo;
			} else if(obj instanceof GTLPreCompiler.StructDef) {
				lineNo = ((GTLPreCompiler.StructDef)obj).lineNo;
			} else if (obj instanceof String) {
				lineNo = parser.getLineOfMacro((String)obj);
			} else {
				return;
			}
			File file = parser.getFileOfLine(lineNo);
			int fileLine = parser.getLineOfLine(lineNo);
			new EditAndGotoJob(file, fileLine, false).run();
		} catch (Exception e) {
		}
	}
	
	public void onToggleBreakpoint() {
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		if (dm != null) {
			try {
				int offset = getSourceViewer().getSelectedRange().x;
				int line = getSourceViewer().getDocument().getLineOfOffset(offset);
				dm.toggleBreakpoint(getEditingFile().getAbsolutePath(), line);
				StyledText text = getSourceViewer().getTextWidget();
				int liney = text.getLocationAtOffset(offset).x;
				int lineHei = text.getLineHeight(offset);
				text.redraw(0, liney, text.getClientArea().width, lineHei, false);
				bpRuler.redraw();
			} catch (Exception e) {
			}
		}
	}
	
	public void onMarkComment() {
		IDocument doc = this.getSourceViewer().getDocument();		
		int offsetStart = this.getSourceViewer().getSelectedRange().x;
		int length = this.getSourceViewer().getSelectedRange().y;
		int offsetEnd = offsetStart + length;
		
		try {
			int startLine = doc.getLineOfOffset(offsetStart);
			int endLine = doc.getLineOfOffset(offsetEnd);
			if(startLine == endLine) {
				IRegion iregion = doc.getLineInformation(startLine);
				String selDoc = doc.get(iregion.getOffset(), iregion.getLength());
				
				if(selDoc.trim().startsWith("//")) {
					selDoc = selDoc.substring(0, selDoc.indexOf("//")) + selDoc.trim().substring(2);
				} else {
					selDoc = "//" + selDoc;
				}
				doc.replace(iregion.getOffset(), iregion.getLength(), selDoc);
				
			} else {				
				//先检查是否都注释或者 注释都放开
				boolean isMarkComment = false;
				for(int i=startLine; i<= endLine; i++) {
					IRegion iregion = doc.getLineInformation(i);
					String selDoc = doc.get(iregion.getOffset(), iregion.getLength());
					
					//只要有一行没有注释就都 标记为注释
					if(selDoc.trim().startsWith("//") == false) {
						isMarkComment = true;
					}
				}

				StringBuffer sb = new StringBuffer();
				for(int i=startLine; i<= endLine; i++) {
					IRegion iregion = doc.getLineInformation(i);
					String selDoc = doc.get(iregion.getOffset(), iregion.getLength());
					if(isMarkComment) {
						sb.append("//");
						sb.append(selDoc);
						if(i != endLine) {
							//恢复换行符
							sb.append(doc.getLineDelimiter(i));
						}
					} else {
						selDoc = selDoc.substring(0, selDoc.indexOf("//")) + selDoc.substring(selDoc.indexOf("//") + 2);
						sb.append(selDoc);
						if(i != endLine) {
							//恢复换行符
							sb.append(doc.getLineDelimiter(i));
						}
					}					
				}
				
				IRegion iregion = doc.getLineInformation(startLine);
				int start = iregion.getOffset();
				iregion = doc.getLineInformation(endLine);
				int len = iregion.getOffset() + iregion.getLength() - start;
				doc.replace(start, len, sb.toString());

				if(isMarkComment) {
					this.getSourceViewer().setSelectedRange(offsetStart, length + (endLine - startLine + 1)*2);	
				} else {
					this.getSourceViewer().setSelectedRange(offsetStart, length - (endLine - startLine + 1)*2);
				}			
			}
			
			
		} catch (Exception e) {
		}
	}
	
	public String getHintInfo(int offset) {
		if (parser == null) {
			return null;
		}
		
		try {
			GTLPreCompiler.FunctionDef func = parser.findFunction(getEditingFile(), getSourceViewer().getDocument().getLineOfOffset(offset));
			int[] pt = GTLTextHover.getWord(this.getSourceViewer().getDocument(), offset);
			String varName = this.getSourceViewer().getDocument().get(pt[0], pt[1] - pt[0]);
			
			// 判断是否宏定义
			String macro = parser.getMacroReplacement(varName);
			if (macro != null) {
				return macro;
			}
			
			// 尝试从当前调试环境中提取值
			GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
			GTLDebugSession session = dm.getActiveSessionObj();
			if (session != null) {
				for (VariableItem var1 : session.getVariables()) {
					if (var1.name.equals(varName)) {
						return VariableView.getVariableString(session, var1);
					}
				}
			}
			
			// 查找变量或者函数名定义
			Object obj = findDeclaration(varName, func);
			int lineNo;
			if (obj instanceof SystemFunctionPrototype) {
				// 系统函数
				return ((SystemFunctionPrototype)obj).comments;
			} else if (obj instanceof GTLPreCompiler.FunctionDef) {
				lineNo = ((GTLPreCompiler.FunctionDef)obj).lineNo;
			} else if (obj instanceof GTLPreCompiler.VariableDef) {
				lineNo = ((GTLPreCompiler.VariableDef)obj).lineNo;
			} else if(obj instanceof GTLPreCompiler.StructDef) {
				lineNo = ((GTLPreCompiler.StructDef)obj).lineNo;
			} else {
				return null;
			}
			return parser.getComments(lineNo);
		} catch (Exception e) {
			return null;
		}
	}
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		fEncodingSupport.setEncoding(Application.getInstance().getProjectData().sourceEncoding);
	}
}
