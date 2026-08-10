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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * A content outline page which always represents the content of the
 * connected editor in 10 segments.
 */
public class GTLContentOutlinePage extends ContentOutlinePage {

	/**
	 * A segment element.
	 */
	protected static class Segment implements Comparable {
		public String name;
		public Position position;

		public Segment(String name, Position position) {
			this.name= name;
			this.position= position;
		}

		public String toString() {
			return name;
		}
		
		public int compareTo(Object o) {
			if (o == null) {
				return 1;
			} else if (!(o instanceof Segment)) {
				return -1;
			}
			Segment seg = (Segment)o;
			int type1, type2;
			String name1, name2;
			if (name.startsWith("DATA")) {
				type1 = 0;
				name1 = "";
			} else if (name.startsWith("STRUCT")) {
				type1 = 50;
				name1 = name.substring(6);
			} else {
				type1 = 100;
				name1 = name;
			}
			if (seg.name.startsWith("DATA")) {
				type2 = 0;
				name2 = "";
			} else if (seg.name.startsWith("STRUCT")) {
				type2 = 50;
				name2 = seg.name.substring(6);
			} else {
				type2 = 100;
				name2 = seg.name;
			}
			if (type1 > type2) {
				return 1;
			} else if (type1 < type2) {
				return -1;
			} else {
			    //Modified by leo (function names order by string cause bother)
				//return name1.compareTo(name2);
			    return 0;
			}
		}
	}

	/**
	 * Divides the editor's document into ten segments and provides elements for them.
	 */
	protected class ContentProvider implements ITreeContentProvider {

		protected final static String SEGMENTS= "__java_segments"; //$NON-NLS-1$
		protected IPositionUpdater fPositionUpdater= new DefaultPositionUpdater(SEGMENTS);
		protected List fContent= new ArrayList(10);

		protected void parse(IDocument document) {

			int lines= document.getNumberOfLines();

			for (int line= 0; line < lines; line ++) {
				try {
					int lineStart = document.getLineOffset(line);
					int lineLen = document.getLineLength(line);
					String lineStr = document.get(lineStart, lineLen);
					if (lineStr.indexOf("FUNCTION ") >= 0) {
						Position p = new Position(lineStart, lineLen);
						document.addPosition(SEGMENTS, p);
						int pos = lineStr.indexOf("FUNCTION ") + 9;
						lineStr = lineStr.substring(pos).trim();
						pos = lineStr.indexOf(')');
						if (pos != -1) {
							lineStr = lineStr.substring(0, pos + 1);
						}
						fContent.add(new Segment(lineStr, p));
					} else if (lineStr.indexOf("DATA ") >= 0) {
						Position p = new Position(lineStart, lineLen);
						document.addPosition(SEGMENTS, p);
						fContent.add(new Segment("DATA", p));
					} else if (lineStr.indexOf("STRUCT ") >= 0) {
						Position p = new Position(lineStart, lineLen);
						document.addPosition(SEGMENTS, p);
						int pos = lineStr.indexOf("STRUCT ") + 7;
						lineStr = lineStr.substring(pos).trim();
						if (lineStr.endsWith("{")) {
							lineStr = lineStr.substring(0, lineStr.length() - 1);
						}
						fContent.add(new Segment("STRUCT " + lineStr, p));
					}
				} catch (Exception e) {
				}
			}
			Object[] arr = fContent.toArray();
			java.util.Arrays.sort(arr);
			fContent.clear();
			fContent.addAll(java.util.Arrays.asList(arr));
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput != null) {
				IDocument document= fDocumentProvider.getDocument(oldInput);
				if (document != null) {
					try {
						document.removePositionCategory(SEGMENTS);
					} catch (BadPositionCategoryException x) {
					}
					document.removePositionUpdater(fPositionUpdater);
				}
			}

			fContent.clear();

			if (newInput != null) {
				IDocument document= fDocumentProvider.getDocument(newInput);
				if (document != null) {
					document.addPositionCategory(SEGMENTS);
					document.addPositionUpdater(fPositionUpdater);

					parse(document);
				}
			}
		}

		/*
		 * @see IContentProvider#dispose
		 */
		public void dispose() {
			if (fContent != null) {
				fContent.clear();
				fContent= null;
			}
		}

		/*
		 * @see IContentProvider#isDeleted(Object)
		 */
		public boolean isDeleted(Object element) {
			return false;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object element) {
			return fContent.toArray();
		}

		/*
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			return element == fInput;
		}

		/*
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof Segment)
				return fInput;
			return null;
		}

		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object element) {
			if (element == fInput)
				return fContent.toArray();
			return new Object[0];
		}
	}

	protected Object fInput;
	protected IDocumentProvider fDocumentProvider;
	protected ITextEditor fTextEditor;

	/**
	 * Creates a content outline page using the given provider and the given editor.
	 * 
	 * @param provider the document provider
	 * @param editor the editor
	 */
	public GTLContentOutlinePage(IDocumentProvider provider, ITextEditor editor) {
		super();
		fDocumentProvider= provider;
		fTextEditor= editor;
	}
	
	/* (non-Javadoc)
	 * Method declared on ContentOutlinePage
	 */
	public void createControl(Composite parent) {

		super.createControl(parent);

		TreeViewer viewer= getTreeViewer();
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.addSelectionChangedListener(this);

		if (fInput != null)
			viewer.setInput(fInput);
	}
	
	/* (non-Javadoc)
	 * Method declared on ContentOutlinePage
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);

		if(cursorChange){
			cursorChange = false;
			return;
		}
		
		ISelection selection= event.getSelection();
		if (selection.isEmpty())
			fTextEditor.resetHighlightRange();
		else {
			Segment segment= (Segment) ((IStructuredSelection) selection).getFirstElement();
			int start= segment.position.getOffset();
			int length= segment.position.getLength();
			try {
				fTextEditor.setHighlightRange(start, length, true);
			} catch (IllegalArgumentException x) {
				fTextEditor.resetHighlightRange();
			}
		}
	}
	
	/**
	 * Sets the input of the outline page
	 * 
	 * @param input the input of this outline page
	 */
	public void setInput(Object input) {
		fInput= input;
		update();
	}
	
	/**
	 * Updates the outline page.
	 */
	public void update() {
		TreeViewer viewer= getTreeViewer();

		if (viewer != null) {
			Control control= viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(fInput);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
	
	public String funcName;
	private boolean cursorChange = false;
	
	public String getFuncName(){
		return funcName;
	}
	
	public void setCursorChange(){
		cursorChange = true;
	}
	
	public void setCursorFunc(String funcName){
		this.funcName = funcName;
		Runnable runnable = new Runnable(){
			public String name = getFuncName();
			public void run(){
				TreeViewer viewer= getTreeViewer();
				if(viewer != null){
					if(name != null){
						ISelection selection = viewer.getSelection();
						Segment segment= (Segment) ((IStructuredSelection) selection).getFirstElement();
						if(segment != null && segment.name.indexOf(name) >= 0){
							return;
						}
						Tree object = viewer.getTree();
						TreeItem[] items = object.getItems();
						for(int i=0; i<items.length; i++){
							if(items[i].getText().indexOf(name) >= 0){
								setCursorChange();
								viewer.setSelection(new StructuredSelection((Segment)items[i].getData()));
								break;
							}
						}
					}
				}
			}
		};
		getSite().getShell().getDisplay().syncExec(runnable);
	}
	
	public TreeViewer getViewer(){
		return getTreeViewer();
	}
}
