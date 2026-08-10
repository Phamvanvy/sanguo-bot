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
 * 一个Java语言的消息资源类。
 * @author lighthu
 */
public class JavaMessageFile implements IMessageFile {
	protected String packageName;
	protected String className;
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
	public JavaMessageFile(File source, String encoding) throws IOException {
		// 把源代码解析成token
		this.source = source;
		this.encoding = encoding;
		tokens = JavaGTLTokenizer.parse(source, encoding);
		tokenPos = 0;
		
		// 从token中找出包名
		if (seek("package") == null) {
			throw new IOException("文件格式错误：未定义package。");
		}
		packageName = seek(null);
		if (packageName == null) {
			throw new IOException("文件格式错误：未定义package。");
		}
		
		// 从token中找出类名
		if (seek("class") == null) {
			throw new IOException("文件格式错误：未定义class。");
		}
		className = seek(null);
		if (className == null) {
			throw new IOException("文件格式错误：未定义class。");
		}
		
		// 找出所有的String常量
		varNameSet = new HashSet<String>();
		existStrings = new HashMap<String, String>();
		while (true) {
			if (seek("String") == null) {
				break;
			}
			String varName = seek(null);
			if (varName == null) {
				throw new IOException("文件格式错误：String后没有变量名。");
			}
			if (seek("=") == null) {
				throw new IOException("文件格式错误：String后没有=号。");
			}
			String varValue = seek(null);
			if (varValue == null || !varValue.startsWith("\"") || !varValue.endsWith("\"")) {
				throw new IOException("文件格式错误：String后没有字符串常量。");
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
			int insertPos = tokens.size() - 1;
			while (!tokens.get(insertPos).equals("}")) {
				insertPos--;
			}
			List<String> addLines = new ArrayList<String>();
			for (String varValue : newStrings.keySet()) {
				String varName = newStrings.get(varValue);
				existStrings.put(varValue, varName);
				varValue = GTLDeCompiler.reverseConv(varValue);
				addLines.add("    public static String " + varName + " = \"" + varValue + "\";\r\n");
			}
			String[] arr = new String[addLines.size()];
			addLines.toArray(arr);
			Arrays.sort(arr);
			for (String line : arr) {
				tokens.add(insertPos, line);
				insertPos++;
			}
			newStrings.clear();
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
		return "STRING_" + t;
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
			return packageName + "." + className + "." + existStrings.get(realValue);
		}
		if (newStrings.containsKey(realValue)) {
			return packageName + "." + className + "." + newStrings.get(realValue);
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
		return packageName + "." + className + "." + key;
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
