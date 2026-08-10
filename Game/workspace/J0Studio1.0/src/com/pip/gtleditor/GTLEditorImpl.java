/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.pip.gtleditor;


import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.pip.gtl.compiler.GTLPreCompiler;
import com.pip.gtl.remotedebugger.GTLDebugManager;
import com.pip.gtl.remotedebugger.GTLDebugServer;
import com.pip.gtl.remotedebugger.GTLDebugSession;
import com.pip.gtl.remotedebugger.VariableItem;
import com.pip.gtl.remotedebugger.ui.VariableView;
import com.pip.util.Utils;
import com.swtdesigner.SWTResourceManager;


/**
 * Java specific text editor.
 */
public class GTLEditorImpl extends TextEditor implements ITextListener, Runnable {
	private class DefineFoldingRegionAction extends TextEditorAction {

		public DefineFoldingRegionAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
			super(bundle, prefix, editor);
		}
		
		private IAnnotationModel getAnnotationModel(ITextEditor editor) {
			return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
		}
		
		/*
		 * @see org.eclipse.jface.action.Action#run()
		 */
		public void run() {
			ITextEditor editor= getTextEditor();
			ISelection selection= editor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection= (ITextSelection) selection;
				if (!textSelection.isEmpty()) {
					IAnnotationModel model= getAnnotationModel(editor);
					if (model != null) {
						
						int start= textSelection.getStartLine();
						int end= textSelection.getEndLine();
						
						try {
							IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
							int offset= document.getLineOffset(start);
							int endOffset= document.getLineOffset(end + 1);
							Position position= new Position(offset, endOffset - offset);
							model.addAnnotation(new ProjectionAnnotation(), position);
						} catch (BadLocationException x) {
							// ignore
						}
					}
				}
			}
		}
	}
	
	/** The outline page */
	private GTLContentOutlinePage fOutlinePage;
	/** The projection support */
	private ProjectionSupport fProjectionSupport;
	/** error line number */
	private int errorLine;
	/** break point manager */
	protected GTLBreakpointRuler bpRuler;
	/** the parser thread */
	private Thread parserThread;
	/** the parser object */
	private GTLPreCompiler parser;
	/** whether text changed since last parsing */
	private boolean textDirty = true;
	/** display object */
	private Display display;
	/** parser listeners */
	private java.util.List<GTLEditorParserListener> parserListeners = new ArrayList<GTLEditorParserListener>();

	/**
	 * Default constructor.
	 */
	public GTLEditorImpl() {
		super();
		errorLine = -1;
		parserThread = new Thread(this);
		parserThread.start();
	}
	
	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method extend the 
	 * actions to add those specific to the receiver
	 */
	protected void createActions() {
		super.createActions();
		
		IAction a= new TextOperationAction(GTLEditorMessages.getResourceBundle(), "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a); //$NON-NLS-1$
		
		a= new TextOperationAction(GTLEditorMessages.getResourceBundle(), "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);  //$NON-NLS-1$
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a); //$NON-NLS-1$
		
		a= new DefineFoldingRegionAction(GTLEditorMessages.getResourceBundle(), "DefineFoldingRegion.", this); //$NON-NLS-1$
		setAction("DefineFoldingRegion", a); //$NON-NLS-1$
	}
	
	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs any extra 
	 * disposal actions required by the java editor.
	 */
	public void dispose() {
		if (fOutlinePage != null)
			fOutlinePage.setInput(null);
		if (bpRuler != null) {
			bpRuler.dispose();
		}
		parserThread = null;
		display = null;
		super.dispose();
	}
	
	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs any extra 
	 * revert behavior required by the java editor.
	 */
	public void doRevertToSaved() {
		super.doRevertToSaved();
		if (fOutlinePage != null)
			fOutlinePage.update();
	}
	
	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs any extra 
	 * save behavior required by the java editor.
	 * 
	 * @param monitor the progress monitor
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (fOutlinePage != null)
			fOutlinePage.update();
	}
	
	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs any extra 
	 * save as behavior required by the java editor.
	 */
	public void doSaveAs() {
		super.doSaveAs();
		if (fOutlinePage != null)
			fOutlinePage.update();
	}
	
	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs sets the 
	 * input of the outline page after AbstractTextEditor has set input.
	 * 
	 * @param input the editor input
	 * @throws CoreException in case the input can not be set
	 */ 
	public void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fOutlinePage != null)
			fOutlinePage.setInput(input);
		display = getSite().getShell().getDisplay();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "ContentAssistProposal"); //$NON-NLS-1$
		addAction(menu, "ContentAssistTip"); //$NON-NLS-1$
		addAction(menu, "DefineFoldingRegion");  //$NON-NLS-1$
	}
	
	/** The <code>JavaEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method performs gets
	 * the java content outline page if request is for a an 
	 * outline page.
	 * 
	 * @param required the required type
	 * @return an adapter for the required type or <code>null</code>
	 */ 
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage= new GTLContentOutlinePage(getDocumentProvider(), this);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
		}
		
		if (fProjectionSupport != null) {
			Object adapter= fProjectionSupport.getAdapter(getSourceViewer(), required);
			if (adapter != null)
				return adapter;
		}
		
		return super.getAdapter(required);
	}
		
	/* (non-Javadoc)
	 * Method declared on AbstractTextEditor
	 */
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new GTLSourceViewerConfiguration(this));
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		fAnnotationAccess= createAnnotationAccess();
		fOverviewRuler= createOverviewRuler(getSharedColors());
		
		ISourceViewer viewer= new GTLEditorViewer(this, parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		
		return viewer;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
		fProjectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		
		bpRuler = new GTLBreakpointRuler(this, viewer, getVerticalRuler());
		bpRuler.setGTLDebugManager(GTLDebugServer.getInstance().getDebugManager());

		GTLEditorPainter pp = new GTLEditorPainter(this, viewer);
		pp.setErrorColor(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		pp.setBreakpointColor(SWTResourceManager.getColor(0xEC, 0xE9, 0xB8));
		pp.setActiveBreakpointColor(SWTResourceManager.getColor(0xC6, 0xDB, 0xAE));
		viewer.addPainter(pp);
		viewer.addTextListener(this);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int, int)
	 */
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer= getSourceViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}
	
	public void textChanged(TextEvent event) {
		errorLine = -1;
		textDirty = true;
	}

	public int getErrorLine() {
		return errorLine;
	}

	public void setErrorLine(int errorLine) {
		this.errorLine = errorLine;
	}
	
	public void jumpToLine(int lineNum) {
		SourceViewer sv = (SourceViewer)this.getSourceViewer();
		int startLine = sv.getTopIndex();
		int endLine = sv.getBottomIndex();
		if (lineNum >= startLine && lineNum <= endLine) {
			// needn't scroll
		} else if (lineNum < startLine) {
			// scroll up
			sv.setTopIndex(lineNum);
		} else {
			// scroll down
			sv.setTopIndex(lineNum + startLine - endLine);
		}
		int lineStart = sv.getTextWidget().getOffsetAtLine(lineNum);
		sv.setSelectedRange(lineStart, 0);
	}
	
	public int getOffsetAtLine(int num) {
		SourceViewer sv = (SourceViewer)this.getSourceViewer();
		return sv.getTextWidget().getOffsetAtLine(num);
	}
	
	public GTLBreakpointRuler getBreakpointRuler() {
		return bpRuler;
	}
	
	public void refresh() {
		SourceViewer sv = (SourceViewer)this.getSourceViewer();
		getBreakpointRuler().redraw();
		sv.getTextWidget().redraw();
	}
	
	public GTLPreCompiler getParser() {
		return parser;
	}
	
	public File getEditingFile() {
		FileStoreEditorInput input = (FileStoreEditorInput)getEditorInput();
		URI url = input.getURI();
		String gtlFile = Utils.urlToPath(url);
		return new File(gtlFile);
	}
	
	public void run() {
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
		while (parserThread != null) {
			if (textDirty && display != null) {
				GTLPreCompiler newparser = new GTLPreCompiler();
				ISourceViewer viewer = getSourceViewer();
				IDocument doc = null;
				if (viewer != null) {
					doc = viewer.getDocument();
				}
				File edFile = getEditingFile();
				if (edFile != null && doc != null) {
					try {
						newparser.parse(edFile, doc.get(0, doc.getLength()));
					} catch (Throwable e) {
						e.printStackTrace();
					}
					if (newparser.isSucc()) {
						parser = newparser;
						for (GTLEditorParserListener l : parserListeners) {
						    l.parseOver(GTLEditorImpl.this);
						}
					}
					textDirty = false;
				} else {
					try {
						Thread.sleep(200);
					} catch (Exception e) {
					}
					continue;
				}
			}
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}
		}
	}
	
	public String getHintInfo(String var) {
		if (parser == null) {
			return null;
		}
		String macro = parser.getMacroReplacement(var);
		if (macro != null) {
			return macro;
		}
		
		// 尝试从当前调试环境中提取值
		GTLDebugManager dm = GTLDebugServer.getInstance().getDebugManager();
		GTLDebugSession session = dm.getActiveSessionObj();
		if (session == null) {
			return null;
		}
		for (VariableItem var1 : session.getVariables()) {
			if (var1.name.equals(var)) {
				return VariableView.getVariableString(session, var1);
			}
		}

		return null;
	}
	
	public void addParserListener(GTLEditorParserListener l) {
	    parserListeners.add(l);
	}
}
