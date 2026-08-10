package com.pip.j0ide.i18n;
import java.io.*;
import java.util.*;

public class GenericPreProcessor {
	private String encoding;
	
	public GenericPreProcessor(String encoding) {
		this.encoding = encoding;
	}
	
    public String process(File file) throws IOException {
    	return process(read(file));
    }

    public String process(String content) throws IOException {
    	return process(read(content));
    }
    
    public String process(String[] content) throws IOException {
        // 去掉#include，#library和#define前面的#，避免词法分析器出错
        for (int i = 0; i < content.length; i++) {
        	String line = content[i];
        	if (line.trim().startsWith("#")) {
        		int pos = line.indexOf('#');
        		content[i] = line.substring(0, pos) + " " + line.substring(pos + 1);
        	}
        }

        // 去除注释
        content = removeComments(content);

        // 处理宏替换
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < content.length; i++) {
        	buf.append(content[i]);
        	buf.append("\n");
        }
        return buf.toString();
    }
    
    // 读入一个文件的内容。目前固定使用GBK编码。
    private String[] read(File file) throws IOException {
        FileInputStream fis = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
        	fis = new FileInputStream(file);
        	BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding));
        	String line = null;
        	while ((line = br.readLine()) != null) {
        		list.add(line);
        	}
        } catch (IOException e) {
            throw e;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }

    // 从内存中读入。
    private String[] read(String content) throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        try {
        	BufferedReader br = new BufferedReader(new StringReader(content));
        	String line = null;
        	while ((line = br.readLine()) != null) {
        		list.add(line);
        	}
        } catch (IOException e) {
            throw e;
        }
        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }
    
    // 删除一个字符串里的注释，返回处理结果。注释可以是/**/对或者是//。
    private String[] removeComments(String[] content) {
        int status = 0;   // 0 - 不在注释中，1 - /**/注释中，2 - 在字符串中，3 - 在字符串中遇到\，4 - 在字符常量中，5 - 在字符常量中遇到\
        int contentCount = content.length;
        
        for (int i = 0; i < contentCount; i++) {
        	StringBuffer lineBuf = new StringBuffer();
        	char[] lineChars = content[i].toCharArray();
        	int lineCharCount = lineChars.length;
        	
        	for (int j = 0; j < lineCharCount; j++) {
	            char ch = lineChars[j];
	            if (status == 0) {
	                if (ch == '/' && j < lineCharCount - 1) {
	                    ch = lineChars[j + 1];
	                    if (ch == '*') {
	                        // 进入/**/注释
	                        status = 1;
	                        j++;
	                    } else if (ch == '/') {
	                        // 进入//注释，直接跳过本行后面所有内容
	                        break;
	                    } else {
	                    	lineBuf.append('/');
	                    }
	                } else if (ch == '"') {
	                	lineBuf.append(ch);
	                	status = 2;
	                } else if (ch == '\'') {
	                	lineBuf.append(ch);
	                	status = 4;
	                } else {
	                	lineBuf.append(ch);
	                }
	            } else if (status == 1) {
	                if (ch == '*' && j < lineCharCount - 1 && lineChars[j + 1] == '/') {
	                    status = 0;
	                    j++;
	                }
	            } else if (status == 2) {
	            	lineBuf.append(ch);
	            	if (ch == '\\') {
	            		status = 3;
	            	} else if (ch == '"') {
	            		status = 0;
	            	}
	            } else if (status == 3) {
	            	lineBuf.append(ch);
	            	status = 2;
	            } else if (status == 4) {
	            	lineBuf.append(ch);
	            	if (ch == '\\') {
	            		status = 5;
	            	} else if (ch == '\'') {
	            		status = 0;
	            	}
	            } else if (status == 5) {
	            	lineBuf.append(ch);
	            	status = 4;
	            }
        	}
        	content[i] = lineBuf.toString();
        }
        return content;
    }
}
