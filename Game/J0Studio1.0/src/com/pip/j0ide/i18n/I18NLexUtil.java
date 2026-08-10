package com.pip.j0ide.i18n;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pip.util.Utils;


public class I18NLexUtil {
	/**
	 * 找出一个文件中所有国际化冲突的字符串。
	 * @param file
	 * @return
	 */
	public static List<Token> findI18NRelatedString(String file, String encoding) {
		List<Token> ret = new ArrayList<Token>();
		if (isIgnoreFile(file, encoding)) {
    		return ret;
    	}
		try {
			String s = new GenericPreProcessor(encoding).process(new File(file));
			SimpleCharStream stream = new SimpleCharStream(new StringReader(s));
			GenericTokenManager mgr = new GenericTokenManager(stream);
			Token token = mgr.getNextToken();
			List<Token> tokenList = new ArrayList<Token>();
			while (token != null && token.kind != GenericParserConstants.EOF) {
				tokenList.add(token);
				token = mgr.getNextToken();
			}
			
			for (int i = 0; i < tokenList.size(); i++) {
				Token t = tokenList.get(i);
				// 带有汉字的字符串
				if ((t.image.startsWith("\'") || t.image.startsWith("\"")) && isI18NRelated(t.image)) {
					 if (checkI18n(tokenList, i)) {
						 ret.add(t);
					 }
				}
			}
		} catch (Throwable e) {
			System.err.println("Error in " + file);
			e.printStackTrace();
		}
		return ret;
	}
	
	/*
	 * 检查token列表中某一个字符串token是否用到了国际化冲突的模式。
	 */
	private static boolean checkI18n(List<Token> tokenList, int index) {
		if (index < tokenList.size() - 1 && tokenList.get(index + 1).image.equals("+")) {
			return true;
		}
		if (index > 0 && tokenList.get(index - 1).image.equals("+")) {
			return true;
		}
		int embedLevel = 0;
		int funcStartIndex = -1;
		for (int i = index - 1; i >= 0; i--) {
			if (tokenList.get(i).image.equals(")")) {
				embedLevel++;
			} else if (tokenList.get(i).image.equals("(")) {
				embedLevel--;
				if (embedLevel == -1) {
					funcStartIndex = i;
					break;
				}
			}
		}
		if (funcStartIndex > 0) {
			Token t = tokenList.get(funcStartIndex - 1);
			if (t.image.equals("append") || t.image.equals("String_Append")) {
				return true;
			}
		}
		return false;
	}
	
	
	
	/**
     * 判断一个字符串是否需要国际化。
     * @param str 字符串内容
     * @return 如果此字符串中包含中文字符，返回true。
     */
    public static boolean isI18NRelated(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch >= 0x4E00 && ch <= 0x9FA5) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 把一个源代码文件中的i18n文本提取到消息文件中，并把源代码中的字符串修改为到消息文件的引用。
     * @param file 支持java和gtl
     * @param encoding
     * @param mf
     * @throws IOException
     */
    public static void fetchI18NMessages(String file, String encoding, IMessageFile mf) throws IOException {
    	if (isIgnoreFile(file, encoding)) {
    		return;
    	}
        List<String> tokens = JavaGTLTokenizer.parse(new File(file), encoding);
        boolean changed = false;
        for (int i = 0; i < tokens.size(); i++) {
            String tk = tokens.get(i);
            if (tk.startsWith("\"") && tk.endsWith("\"")) {
            	String newStr = mf.input(tk);
                if (newStr != null) {
                    tokens.set(i, newStr);
                    changed = true;
                }
            }
        }
        if (changed) {
        	JavaGTLTokenizer.save(new File(file), encoding, tokens);
        }
    }
    
    public static boolean isIgnoreFile(String file, String encoding) {
    	try {
    		String rawContent = Utils.loadFileContent(new File(file), encoding);
    		if (rawContent.contains("//I18N-IGNORE")) {
    			return true;
    		} else {
    			return false;
    		}
    	} catch (Exception e) {
    		return true;
    	}
    }
}
