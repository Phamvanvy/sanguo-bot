package com.pip.sanguo.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Java语言词法分析器。这个类把一个Java程序分析成一个个的token。
 * @author lighthu
 */
public class JavaTokenizer {
    // 输入流
    protected Reader in;
    // 缓冲的最后一个字符，-1表示没有
    protected int cachedChar = -1;
    
    public JavaTokenizer(Reader r) {
        in = r;
    }
    
    /**
     * 从输入流中读取一个token。如果到流结尾，返回空串。
     * @return
     */
    public String read() throws IOException {
        StringBuilder sb = new StringBuilder();
        int state = 0;   // -1 - 结束循环、0 - 在空格中、1 - 在普通token中、2 - 遇到注释/、
                         // 3 - 进入行注释、4 - 进入普通注释、5 - 注释中遇到*、6 - 在字符串中、
                         // 7 - 在字符串中遇到\符号、8 - 在字符表达式中、9 - 在字符表达式中遇到\符号
        
        // 处理当前字符
        if (cachedChar == -1) {
            cachedChar = in.read();
            if (cachedChar == -1) {
                return sb.toString();
            }
        }
        char ch = (char)cachedChar;
        cachedChar = -1;
        sb.append(ch);
        if (Character.isWhitespace(ch)) {
            // 空格，查找空格
            state = 0;
        } else if (ch == '(' || ch == '{' || ch == '}' || ch == ')' || ch == ';' || ch == '*') {
            return sb.toString();
        } else if (ch == '/') {
            state = 2;
        } else if (ch == '"') {
            state = 6;
        } else if (ch == '\'') {
            state = 8;
        } else {
            state = 1;
        }
        
        while (state != -1) {
            int nch = in.read();
            if (nch == -1) {
                break;
            }
            switch (state) {
            case 0:   // 在空格中
                if (!Character.isWhitespace((char)nch)) {
                    cachedChar = nch;
                    state = -1;
                } else {
                    sb.append((char)nch);
                }
                break;
            case 1:  // 在普通token中
                if (Character.isWhitespace((char)nch) || nch == '(' || nch == '{' ||
                        nch == '}' || nch == ')' || nch == ';' || nch == '/' || nch == '*' ||
                        nch == '"' || nch == '\'') {
                    cachedChar = nch;
                    state = -1;
                } else {
                    sb.append((char)nch);
                }
                break;
            case 2:  // 遇到注释/
                if (nch == '/') {
                    sb.append((char)nch);
                    state = 3;
                } else if (nch == '*') {
                    sb.append((char)nch);
                    state = 4;
                } else {
                    cachedChar = nch;
                    state = -1;
                }
                break;
            case 3:  // 在行注释中
                if (nch == '\r' || nch == '\n') {
                    cachedChar = nch;
                    state = -1;
                } else {
                    sb.append((char)nch);
                }
                break;
            case 4:  // 在普通注释中
                if (nch == '*') {
                    sb.append((char)nch);
                    state = 5;
                } else {
                    sb.append((char)nch);
                }
                break;
            case 5:  // 在普通注释中遇到*
                if (nch == '/') {
                    sb.append((char)nch);
                    state = -1;
                } else if (nch != '*') {
                    sb.append((char)nch);
                    state = 4;
                } else {
                    sb.append((char)nch);
                }
                break;
            case 6:  // 在字符串中
                if (nch == '"') {
                    sb.append((char)nch);
                    state = -1;
                } else if (nch == '\\') {
                    sb.append((char)nch);
                    state = 7;
                } else {
                    sb.append((char)nch);
                }
                break;
            case 7:  // 字符串中遇到转义字符\
                sb.append((char)nch);
                state = 6;
                break;
            case 8:  // 在字符表达式中
                if (nch == '\'') {
                    sb.append((char)nch);
                    state = -1;
                } else if (nch == '\\') {
                    sb.append((char)nch);
                    state = 9;
                } else {
                    sb.append((char)nch);
                }
                break;
            case 9:  // 字符表达式中遇到转义字符\
                sb.append((char)nch);
                state = 8;
                break;
            }
        }
        return sb.toString();
    }
    
    /**
     * 从输入流中解析出所有token。
     * @param r
     * @return
     * @throws IOException
     */
    public static List<String> parse(Reader r) throws IOException {
        JavaTokenizer tokens = new JavaTokenizer(r);
        String token;
        List<String> ret = new ArrayList<String>();
        while ((token = tokens.read()).length() != 0) {
            ret.add(token);
        }
        return ret;
    }
    
    /**
     * 从文件中解析出所有的token。
     * @param f 文件
     * @param encoding 编码方式
     * @return 
     * @throws IOException
     */
    public static List<String> parse(File f, String encoding) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, encoding);
            BufferedReader br = new BufferedReader(isr);
            return parse(br);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
    
    /**
     * 把token流写入到源文件中。
     * @param f 文件
     * @param encoding 编码方式
     * @param tokens
     * @throws IOException
     */
    public static void save(File f, String encoding, List<String> tokens) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
            BufferedWriter bw = new BufferedWriter(osw);
            for (String s : tokens) {
                bw.write(s);
            }
            bw.flush();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}
