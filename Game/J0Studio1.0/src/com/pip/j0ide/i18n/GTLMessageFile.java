package com.pip.j0ide.i18n;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pip.gtl.codegen.GTLProgGenerator;
import com.pip.gtl.decompiler.GTLDeCompiler;

/**
 * 一个GTL语言的消息资源类。
 * @author lighthu
 */
public class GTLMessageFile implements IMessageFile {
	protected Map<String, String> existStrings;
	protected Map<String, String> newStrings;
	protected Set<String> varNameSet;
	protected int nextStringID;
	
	protected File source;
	protected String encoding;
	protected List<String> tokens;
	protected int tokenPos;
	
	/**
	 * 读入一个Message类。
	 * @param source
	 * @param encoding
	 * @throws IOException
	 */
	public GTLMessageFile(File source, String encoding) throws IOException {
		// 把源代码解析成token
		this.source = source;
		this.encoding = encoding;
		tokens = JavaGTLTokenizer.parse(source, encoding);
		tokenPos = 0;
		
		// 找出所有的String常量
		varNameSet = new HashSet<String>();
		existStrings = new HashMap<String, String>();
		while (true) {
			if (seek("#define") == null) {
				break;
			}
			String varName = seek(null);
			if (varName == null) {
				throw new IOException("文件格式错误：#define后没有变量名。");
			}
			String varValue = seek(null);
			if (varValue == null || !varValue.startsWith("\"") || !varValue.endsWith("\"")) {
				throw new IOException("文件格式错误：#define后没有字符串常量。");
			}
			varValue = GTLProgGenerator.translateStringConstant(varValue);
			existStrings.put(varValue, varName);
			varNameSet.add(varName);
		}
		
		newStrings = new HashMap<String, String>();
	}
	
	/**
	 * 保存到文件。
	 * @throws IOException
	 */
	public void save() throws IOException {
		// 如果有新增的字符串，加入到token列表中
		if (newStrings.size() > 0) {
			List<String> addLines = new ArrayList<String>();
			for (String varValue : newStrings.keySet()) {
				String varName = newStrings.get(varValue);
				existStrings.put(varValue, varName);
				varValue = GTLDeCompiler.reverseConv(varValue);
				addLines.add("#define " + varName + " \"" + varValue + "\"\r\n");
			}
			newStrings.clear();
			String[] arr = new String[addLines.size()];
			addLines.toArray(arr);
			Arrays.sort(arr);
			for (String line : arr) {
				tokens.add(line);
			}
		}
		
		// 保存到文件
		JavaGTLTokenizer.save(source, encoding, tokens);
	}
	
	/**
	 * 取得新增字符串数量。
	 * @return
	 */
	public int getNewCount() {
		return newStrings.size();
	}
	
	private String generateKey() {
		String t = String.valueOf(nextStringID++);
		while (t.length() < 5) {
			t = "0" + t;
		}
		return "STRING_CONSTANT_" + t;
	}
	
	/**
	 * 把一个字符串转换为国际化表示。
	 * @param value 带""前后缀的字符串常量
	 * @return 如果这个字符串不需要国际化，返回null。
	 */
	public String input(String value) {
		String realValue = GTLProgGenerator.translateStringConstant(value);
		if (!I18NLexUtil.isI18NRelated(realValue)) {
			return null;
		}
		if (existStrings.containsKey(realValue)) {
			return existStrings.get(realValue);
		}
		if (newStrings.containsKey(realValue)) {
			return newStrings.get(realValue);
		}
		
		// 生成新的字符串
		String key = generateKey();
		while (true) {
			if (!varNameSet.contains(key)) {
				break;
			}
			key = generateKey();
		}
		varNameSet.add(key);
		newStrings.put(realValue, key);
		return key;
	}
	
	/*
	 * 从token流中找出指定的token。
	 * @param find 如果不为null，则只查找精确匹配的token；如果为null，则查找非空token。
	 * @return 如果没找到合适的token，返回null。 
	 */
	private String seek(String find) {
		while (tokenPos < tokens.size()) {
			String t = tokens.get(tokenPos);
			tokenPos++;
			if (find != null && find.equals(t)) {
				return t;
			} else if (find == null && t.trim().length() > 0) {
				return t;
			}
		}
		return null;
	}
}
