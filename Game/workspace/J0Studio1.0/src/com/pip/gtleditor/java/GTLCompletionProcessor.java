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
package com.pip.gtleditor.java;


import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.pip.gtl.compiler.*;
import com.pip.gtl.preprocess.GTLPreProcessor;
import com.pip.gtleditor.GTLEditorImpl;

/**
 * Example Java completion processor.
 */
public class GTLCompletionProcessor implements IContentAssistProcessor {
	protected GTLEditorImpl editor;
	public static Image globalVarImg, localVarImg, memberImg, paramImg, systemFuncImg, userFuncImg, macroImg, structImg;

	/**
	 * Simple content assist tip closer. The tip is valid in a range
	 * of 5 characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

		protected int fInstallOffset;
		protected ITextViewer viewer;

		/*
		 * @see IContextInformationValidator#isContextInformationValid(int)
		 */
		public boolean isContextInformationValid(int offset) {
			if (offset < fInstallOffset) {
				return false;
			}
			String str = viewer.getTextWidget().getText(fInstallOffset, offset);
			if (str.indexOf(")") >= 0) {
				return false;
			}
			return true;
		}

		/*
		 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
		 */
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			this.viewer = viewer;
			fInstallOffset= offset;
		}
		
		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int, TextPresentation)
		 */
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	protected IContextInformationValidator fValidator= new Validator();

	public GTLCompletionProcessor(GTLEditorImpl editor) {
		this.editor = editor;
		initImages();
	}
	
	public static void initImages() {
		if (globalVarImg == null) {
			try {
				Display display = Display.getCurrent();
				globalVarImg = loadImage(display, "globalvar.gif");
				localVarImg = loadImage(display, "localvar.gif");
				memberImg = loadImage(display, "member.gif");
				paramImg = loadImage(display, "param.gif");
				systemFuncImg = loadImage(display, "systemfunc.gif");
				userFuncImg = loadImage(display, "userfunc.gif");
				macroImg = loadImage(display, "macro.gif");
				structImg = loadImage(display, "struct.gif");
			} catch (Exception e) {
			}
		}
	}
	
	private static Image loadImage(Display display, String name) throws Exception {
		InputStream stream = GTLCompletionProcessor.class.getResourceAsStream("/com/pip/gtleditor/java/" + name);
		try {
			return new Image(display, stream);
		} finally {
			stream.close();
		}
	}
	private int searchTailStartPos(String s){
		char ch;
		String str = s.trim();
		int pos = str.length() - 1;
		for(int i=str.length() - 1;i>=0;i--){
			ch = str.charAt(i);
			if(Character.isWhitespace(ch) || ch == '(' || ch == '[' || ch == '{'|| ch == '}'|| ch == ']'|| ch == ')'|| ch == '+'|| ch == '-'|| ch == '*'|| ch == '/'|| ch == '='|| ch == ','|| ch == ';'){
				break;
			} else {
				pos--;
			}
		}
		return pos;
	}
	private String getContext(String str){
		StringBuffer sb = new StringBuffer();
		char ch;
		String copy = str.trim();
		for(int i=copy.length() - 1;i>=0;i--){
			ch = copy.charAt(i);
			if(Character.isWhitespace(ch)){
				break;
			} else {
				sb.append(ch);
			}
		}
		return sb.reverse().toString();
	}
	private boolean isGlobal(String str){
		boolean b = false;
		if(str.endsWith(" ") || str.endsWith("\t") || str.endsWith("\n") || str.endsWith("(") || str.endsWith("[") || str.endsWith("{") || str.endsWith(")") || str.endsWith("]") || str.endsWith("}") || str.endsWith(",") || str.endsWith("<") || str.endsWith(">") || str.endsWith("+") || str.endsWith("-") || str.endsWith("*") || str.endsWith("/") || str.endsWith("=") || str.endsWith(";")){
			b = true;
		}
		return b;
	}
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		GTLPreCompiler parser = editor.getParser();
		if (parser == null) {
			return new ICompletionProposal[0];
		}
		
		// check context before current position
		int start = documentOffset - 100;
		if (start < 0) {
			start = 0;
		}
		List<ICompletionProposal> retList = new ArrayList<ICompletionProposal>();
		try {
			String context = viewer.getDocument().get(start, documentOffset - start);
			int dotPos;
			boolean hasNoDot = false;
			if(isGlobal(context)){
				context = context.trim();
				dotPos = context.length() - 1;
				hasNoDot = true;
			} else {
				context = getContext(context);
				dotPos = context.lastIndexOf('.');
				hasNoDot = dotPos == -1?true:false;
				if(hasNoDot){
					dotPos = searchTailStartPos(context);
				}
			}
			
			String filter = context.substring(dotPos + 1).toLowerCase();
			int flen = filter.length();
			if (dotPos > 0 && Character.isWhitespace(context.charAt(dotPos - 1))) {
				flen++;
			}
			if(dotPos >= 0 && dotPos < context.length())
				context = context.substring(0, dotPos);
			int lineEnd = context.indexOf('\n');
			if (lineEnd != -1) {
				context = context.substring(lineEnd);
			}
			List<GTLVarRef> contextRef = parseContext(context);
			if (contextRef == null || contextRef.size() == 0 || hasNoDot) {
				// show global vars and functions if no context is found
				GTLPreCompiler.VariableDef[] globals = parser.getGlobalVars();
				for (int i = 0; i < globals.length; i++) {
					String name = globals[i].name;
					if (!name.toLowerCase().startsWith(filter)) {
						continue;
					}
					ICompletionProposal prop = new CompletionProposal(name, documentOffset - flen, flen,
							name.length(), globalVarImg, globals[i].toString(), null, null);
					retList.add(prop);
				}
				
				// list functions & system functinos
				GTLPreCompiler.FunctionDef[] funcs = parser.getFunctions();
				for (int i = 0; i < funcs.length; i++) {
					if (!funcs[i].name.toLowerCase().startsWith(filter)) {
						continue;
					}
					String funcContext = funcs[i].getParamContext();
					IContextInformation info = new ContextInformation(funcContext, funcContext);
					String rep = funcs[i].getTemplate();
					ICompletionProposal prop = new CompletionProposal(rep, documentOffset - flen, flen,
							funcs[i].name.length() + 1, funcs[i].id < 0 ? systemFuncImg : userFuncImg, 
									funcs[i].toString(), info, null);
					retList.add(prop);
				}
				
				// list function params and local variables if in a function
				GTLPreCompiler.FunctionDef func = parser.findFunction(editor.getEditingFile(), viewer.getDocument().getLineOfOffset(documentOffset));
				if (func != null) {
					for (GTLPreCompiler.VariableDef param : func.params) {
						if (!param.name.toLowerCase().startsWith(filter)) {
							continue;
						}
						ICompletionProposal prop = new CompletionProposal(param.name, documentOffset - flen, flen,
							param.name.length(), paramImg, param.toString(), null, null);
						retList.add(prop);
					}
					for (GTLPreCompiler.VariableDef localvar : func.localVariables.values()) {
						if (!localvar.name.toLowerCase().startsWith(filter)) {
							continue;
						}
						ICompletionProposal prop = new CompletionProposal(localvar.name, documentOffset - flen, flen,
							localvar.name.length(), localVarImg, localvar.toString(), null, null);
						retList.add(prop);
					}
				}
				
				// list macros
				for (String macro : parser.getMacros()) {
					if (!macro.toLowerCase().startsWith(filter)) {
						continue;
					}
					ICompletionProposal prop = new CompletionProposal(macro, documentOffset - flen, flen,
							macro.length(), globalVarImg, macro, null, null);
					retList.add(prop);
				}
				
				// list structs
				for (GTLPreCompiler.StructDef stdef : parser.getStructs()) {
					String name = stdef.name;
					if (!name.toLowerCase().startsWith(filter)) {
						continue;
					}
					ICompletionProposal prop = new CompletionProposal(name, documentOffset - flen, flen,
							name.length(), structImg, stdef.toString(), null, null);
					retList.add(prop);
				}
			} else {
				// show members of struct
				GTLPreCompiler.StructDef currst = null;
				for (int i = 0; i < contextRef.size(); i++) {
					String varName = contextRef.get(i).variable.image;
					GTLPreCompiler.VariableDef var = null;
					if (currst == null) {
						// check if the variable is a global variable or local varialbe
						var = parser.findGlobalVar(varName);
						if (var == null) {
							GTLPreCompiler.FunctionDef func = parser.findFunction(editor.getEditingFile(), viewer.getDocument().getLineOfOffset(documentOffset));
							if (func != null) {
								var = func.findParam(varName);
								if (var == null) {
									var = func.localVariables.get(varName);
								}
							}
						}
					} else {
						// check if the variable is member of current struct
						var = currst.findMember(varName);
					}
					if (var == null) {
						throw new Exception("undefined member");
					}
					if ((var.type & 0x0F) == 4 && var.typeName != null) {
						currst = parser.findStruct(var.typeName);
					} else {
						currst = null;
					}
					if (currst == null) {
						throw new Exception("undefined struct");
					}
				}
				for (GTLPreCompiler.VariableDef member : currst.members) {
					if (!member.name.toLowerCase().startsWith(filter)) {
						continue;
					}
					ICompletionProposal prop = new CompletionProposal(member.name, documentOffset - flen, flen,
							member.name.length(), memberImg, member.toString(), null, null);
					retList.add(prop);
				}
			}
		} catch (Throwable e) {
			return new ICompletionProposal[0];
		}
		ICompletionProposal[] ret = new ICompletionProposal[retList.size()];
		retList.toArray(ret);
		Arrays.sort(ret, new ProposalComparator());
		return ret;
	}
	
	private static class ProposalComparator implements Comparator<ICompletionProposal> {
		public boolean equals(Object o) {
			return false;
		}
		
		public int compare(ICompletionProposal o1, ICompletionProposal o2) {
			return o1.getDisplayString().compareTo(o2.getDisplayString());
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		GTLPreCompiler parser = editor.getParser();
		if (parser == null) {
			return new IContextInformation[0];
		}
		
		// check context before current position
		int start = documentOffset - 100;
		if (start < 0) {
			start = 0;
		}
		GTLPreCompiler.FunctionDef hintFunc = null;
		try {
			String context = viewer.getDocument().get(start, documentOffset - start);
			int dotPos = context.lastIndexOf('(');
			if (dotPos == -1) {
				dotPos = context.length() - 1;
			}
			context = context.substring(0, dotPos);
			int lineEnd = context.indexOf('\n');
			if (lineEnd != -1) {
				context = context.substring(lineEnd);
			}
			List<GTLVarRef> contextRef = parseContext(context);
			if (contextRef != null && contextRef.size() == 1) {
				hintFunc = parser.findFunction(contextRef.get(0).variable.image);
			}
		} catch (Throwable e) {
			return new IContextInformation[0];
		}
		if (hintFunc == null) {
			return new IContextInformation[0];
		}
		IContextInformation[] result = new IContextInformation[1];
		result[0] = new ContextInformation(hintFunc.getParamContext(), hintFunc.getParamContext());
		return result;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '(' };
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public String getErrorMessage() {
		return null;
	}
	
	/*
	 * try to parse a reference context from tail of a string.
	 */
	protected List<GTLVarRef> parseContext(String context) {
		try {
			GTLPreProcessor proc = new GTLPreProcessor(null, true);
			context = proc.processInMemory(context);
	    	SimpleCharStream cs = new SimpleCharStream(new StringReader(context), 0, 0);
			GTLParserTokenManager tkm = new GTLParserTokenManager(cs);
			GTLParser parser = new GTLParser(tkm);
			List<GTLVarRef> retList = new ArrayList<GTLVarRef>();
			while (true) {
				try {
					GTLVarRef varRef = parser.VarRef();
					retList.add(varRef);
					Token tk = parser.getNextToken();
					if (tk.kind == 0) {
						break;
					} else if (tk.kind == GTLParserConstants.FULLSTOP) {
						continue;
					} else {
						retList.clear();
					}
				} catch (Exception ee) {
					retList.clear();
					if (parser.getNextToken().kind == 0) {
						break;
					}
				}
			}
			return retList;
		} catch (Throwable e) {
		}
		return null;
	}
}
