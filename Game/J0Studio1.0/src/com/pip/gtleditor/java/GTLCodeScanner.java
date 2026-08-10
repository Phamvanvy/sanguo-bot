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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;

import com.pip.gtl.codegen.GTLFunctionCallGenerator;
import com.pip.gtl.codegen.syscall.SystemFunctionManager;
import com.pip.gtl.compiler.GTLPreCompiler;
import com.pip.gtleditor.GTLEditorImpl;
import com.pip.gtleditor.GTLEditorParserListener;
import com.pip.gtleditor.GTLEditorViewer;
import com.pip.gtleditor.util.*;

/**
 * A Java code scanner.
 */
public class GTLCodeScanner extends RuleBasedScanner implements GTLEditorParserListener {

	private static String[] fgKeywords= { "this", "define", "library", "include", "break", "continue", "else", "if", "while", "LIBRARY", "VERSION", "ID", "ATTRIBUTE", "NAME", "DESCRIPTION", "DATA", "return", "FUNCTION", "CALLBACK", "new", "free", "STRUCT", "extends", "for", "switch", "case", "default", "instanceof", "delegate" }; //$NON-NLS-36$ //$NON-NLS-35$ //$NON-NLS-34$ //$NON-NLS-33$ //$NON-NLS-32$ //$NON-NLS-31$ //$NON-NLS-30$ //$NON-NLS-29$ //$NON-NLS-28$ //$NON-NLS-27$ //$NON-NLS-26$ //$NON-NLS-25$ //$NON-NLS-24$ //$NON-NLS-23$ //$NON-NLS-22$ //$NON-NLS-21$ //$NON-NLS-20$ //$NON-NLS-19$ //$NON-NLS-18$ //$NON-NLS-17$ //$NON-NLS-16$ //$NON-NLS-15$ //$NON-NLS-14$ //$NON-NLS-13$ //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	private static String[] fgTypes= { "boolean", "byte", "int", "short", "String", "Sprite", "ImageSet", "Stream", "UWAPSegment", "Vector", "Hashtable", "Object", "void" }; //$NON-NLS-1$ //$NON-NLS-5$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-2$

	private static String[] fgConstants= { "FALSE", "NULL", "TRUE", "G_DELEGATE" }; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
	
	private EndOfLineRule lineCommentRule;
	private SingleLineRule stringRule;
	private WhitespaceRule whitespaceRule;
	private WordRule keywordsRule;
	private IToken keyword, type, string, comment, other, sysfunc, global, macro, userfunc;
	private GTLEditorViewer viewer;
	private HashSet<String> lastGlobalSet = new HashSet<String>();

	/**
	 * Creates a Java code scanner with the given color provider.
	 * 
	 * @param provider the color provider
	 */
	public GTLCodeScanner(GTLColorProvider provider, ISourceViewer viewer) {
	    this.viewer = (GTLEditorViewer)viewer;
	    
		keyword = new Token(new TextAttribute(provider.getColor(GTLColorProvider.KEYWORD), null, SWT.BOLD, null));
		type = new Token(new TextAttribute(provider.getColor(GTLColorProvider.TYPE), null, SWT.BOLD, null));
		string = new Token(new TextAttribute(provider.getColor(GTLColorProvider.STRING), null, SWT.BOLD, null));
		comment = new Token(new TextAttribute(provider.getColor(GTLColorProvider.SINGLE_LINE_COMMENT)));
		other = new Token(new TextAttribute(provider.getColor(GTLColorProvider.DEFAULT)));
		sysfunc = new Token(new TextAttribute(provider.getColor(GTLColorProvider.SYSFUNC), null, SWT.BOLD, null));
        userfunc = new Token(new TextAttribute(provider.getColor(GTLColorProvider.USERFUNC), null, SWT.ITALIC, null));
        global = new Token(new TextAttribute(provider.getColor(GTLColorProvider.GLOBAL)));
        macro = new Token(new TextAttribute(provider.getColor(GTLColorProvider.MACRO), null, SWT.BOLD, null));

		// Add rule for single line comments.
		lineCommentRule = new EndOfLineRule("//", comment);

		// Add rule for strings and character constants.
		stringRule = new SingleLineRule("\"", "\"", string, '\\');

		// Add generic whitespace rule.
		whitespaceRule = new WhitespaceRule(new GTLWhitespaceDetector());

		// Add word rule for keywords, types, and constants.
		keywordsRule = new WordRule(new GTLWordDetector(), other);
		createDefaultKeywordsRule(keywordsRule);
		
		setRules(new IRule[] { lineCommentRule, stringRule, whitespaceRule, keywordsRule });
		((GTLEditorViewer)viewer).getEditor().addParserListener(this);
	}
	
	private void createDefaultKeywordsRule(WordRule rule) {
	    for (int i= 0; i < fgKeywords.length; i++) {
	        rule.addWord(fgKeywords[i], keyword);
        }           
        for (int i= 0; i < fgTypes.length; i++)
            rule.addWord(fgTypes[i], type);
        for (int i= 0; i < fgConstants.length; i++)
            rule.addWord(fgConstants[i], type);
        
        String[] funcs = SystemFunctionManager.getAllFunctionNames();
        for (int i = 0; i < funcs.length; i++)
            rule.addWord(funcs[i], sysfunc);
	}
	
	public void parseOver(GTLEditorImpl editor) {
	    GTLPreCompiler parser = editor.getParser();
	    WordRule newKeywordsRule = new WordRule(new GTLWordDetector(), other);
        createDefaultKeywordsRule(newKeywordsRule);
        HashSet<String> lastVars = new HashSet<String>();
        HashSet<String> newVars = new HashSet<String>();
        lastVars.addAll(lastGlobalSet);
	    for (GTLPreCompiler.VariableDef var : parser.getGlobalVars()) {
	        newKeywordsRule.addWord(var.name, global);
	        lastVars.remove(var.name);
	        newVars.add(var.name);
	    }
	    for (GTLPreCompiler.FunctionDef func : parser.getFunctions()) {
	        if (func.id >= 0) {
    	        newKeywordsRule.addWord(func.name, userfunc);
                lastVars.remove(func.name);
                newVars.add(func.name);
	        }
	    }
	    for (String m: parser.getMacros()) {
	        newKeywordsRule.addWord(m, macro);
            lastVars.remove(m);
            newVars.add(m);
        }
	    
	    if (newVars.size() != lastGlobalSet.size() || !lastVars.isEmpty()) {
	        // 发现有变量改变了，重新刷新显示
    	    lastGlobalSet = newVars;
    	    keywordsRule = newKeywordsRule;
    	    setRules(new IRule[] { lineCommentRule, stringRule, whitespaceRule, newKeywordsRule });
    	    
    	    if(viewer.getControl() != null) {
    	    	viewer.getControl().getDisplay().asyncExec(new Runnable() {
    	    		public void run() {
    	    			viewer.resetPresentation();
    	    		}
    	    	});    	    	
    	    }
	    }
	}
}
