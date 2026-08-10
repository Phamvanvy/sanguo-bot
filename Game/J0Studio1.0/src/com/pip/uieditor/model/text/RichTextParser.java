package com.pip.uieditor.model.text;

/**
 * 旧版（Mango用）富文本解释器。
 * @author light.hu
 */
public class RichTextParser {

	private String text;
	private int peek = -1;
	private int position = 0;
	private RichTextVisitor visitor;
	
	public RichTextParser(String text) {
		this.text = text;
	}
	
	public void parser(RichTextVisitor visitor) throws Exception {
		this.visitor = visitor;
		clear();
		peek();
		while(peek != -1) {
			switch(peek) {
			case '<':
				matchTag();
				break;
			case '{':
				matchBraceTag();
				break;
			default:
				matchText();
			}
		}
		ended();
	}
	
	private void matchText() throws Exception{
		StringBuilder sb = new StringBuilder(128);
		while(peek != -1 && peek != '<' && peek != '{') {
			sb.append((char)peek);
			peek();
		}
		text(sb.toString());
	}
	
	protected void text(String content) {
		visitor.text(content);
	}
	
	protected void ended() {
		visitor.ended();
	}
	
	private void matchBraceTag() throws Exception{
		match('{');
		StringBuilder sb = new StringBuilder(128);
		while(peek != '}') {
			if(peek == -1)
				throw new Exception();
			sb.append((char)peek);
			peek();
		}
		match('}');
		braceTag(sb.toString());
	}
	
	protected void braceTag(String content) {
		visitor.braceTag(content);
	}
	
	private void matchTag() throws Exception{
		match('<');
		if(peek == -1)
			throw new Exception();
		if(peek == '/') {
			matchEndTag();
		} else {
			char type = (char)peek;
			peek();
			String attribute = matchAttribute();
			tagBegin(type, attribute);
			match('>');
			matchText();
		}
	}
	
	private String matchAttribute() throws Exception{
		if(peek == -1)
			throw new Exception();
		StringBuilder attribute = new StringBuilder(128);
		while(peek != '>') {
			if(peek == -1)
				throw new Exception();
			attribute.append((char)peek);
			peek();
		}
		return attribute.toString();
	}
	
	private void matchEndTag() throws Exception {
		match('/');
		if(peek == -1)
			throw new Exception();
		tagEnded((char)peek);
		match(peek);
		match('>');
	}
	
	protected void tagEnded(char type) {
		visitor.tagEnded(type);
	}
	
	protected void tagBegin(char type, String attribute) {
		visitor.tagBegin(type, attribute);
	}
	
	private void match(int c) throws Exception{
		if(peek == c)
			peek();
		else 
			throw new Exception();
	}
	
	private void clear() {
		peek = -1;
		position = 0;
	}
	
	protected void peek() {
		if(position >= text.length()) {
			peek = -1;
		} else {
			peek = text.charAt(position++);
		}
	}

	/**
     * 带有转义字符的字符串变成普通字符串。
     */
    public static String parseEscapedString(String str) {
        StringBuffer buf = new StringBuffer();
        char[] data = str.toCharArray();
        for (int i = 0; i < data.length; i++) {
            char ch = data[i];
            if (ch == '\\' && i < data.length - 1) {
                switch (data[i + 1]) {
                case 'n':
                    buf.append("\n");
                    break;
                case 'r':
                    buf.append("\r");
                    break;
                case 't':
                    buf.append("\t");
                    break;
                default:
                    buf.append(data[i + 1]);
                    break;
                }
                i++;
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    /**
     * 把普通字符串变成带有转移字符的字符串。并且去掉\r。
     */
    public static String escapeString(String str) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
            case '\n':
                buf.append("\\n");
                break;
            case '\r':
                // buf.append("\\r");
                break;
            case '\t':
                buf.append("\\t");
                break;
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                buf.append(str.charAt(i));
                break;
            }
        }
        return buf.toString();
    }
}

