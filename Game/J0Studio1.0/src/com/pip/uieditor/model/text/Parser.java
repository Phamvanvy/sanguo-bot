package com.pip.uieditor.model.text;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.pip.j0ide.Settings;
import com.pip.uieditor.model.AnimateData;
import com.pip.uieditor.model.ImageData;
import com.pip.uieditor.model.RGBUtil;

/**
 * 新版（Scryer用）富文本解释器。
 * @author jlin
 */
public class Parser {
	
	private Document doc;
	private String text;
	
	private int position;
	
	private int peek = -1;
	
	private int docPosition;
	
	private Vector tags = null;
	
	private Paragraph currentParagraph;
	
	private StringBuilder result = null;
	
	private Vector textStyles;
	
	private Vector paragraphs;
	
	private Link currentLink;
	
	public Parser(Document doc, String text) {
		this.doc = doc;
		this.text = text;
		this.tags = new Vector();
		this.currentParagraph = new Paragraph();
		this.result = new StringBuilder(text.length());
		this.textStyles = new Vector();
		this.textStyles.addElement(new TextStyle(null, null, null, -1));
		this.paragraphs = new Vector();
		peek();
	}
	
	public Vector<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public String getContent() {
		return this.result.toString();
	}
	
	public void parse() {
		while (peek != -1) {
			switch (peek) {
			case '<':
				matchTag();
				break;
			default:
				matchText();
			}
		}
		if(currentParagraph != null && currentParagraph.getElementCount() != 0) {
			paragraphs.addElement(currentParagraph);
		}
		doc.content = this.result.toString();
	}
	
	private void matchTag() {
		match('<');
		if(peek == '/') {
			match('/');
			String tagName = matchTagName();
			match('>');
			processTagEnd(tagName);
			return;
		}
		String tagName = matchTagName();
		Tag tag = new Tag(tagName);
		do {
			if(peek == '/') {
				match('/');
				match('>');
				processTagBegin(tag);
				processTagEnd(tag);
				return;
			}
			if (peek == '>') {
				match('>');
				break;
			} else {
				match(' ');
				String key = matchKey();
				System.out.println(key);
				match('=');
				boolean matchedSlash = false;
				if(peek == '\"') {
					matchedSlash = true;
					match('\"');
				}
				String value = matchValue();
				System.out.println(value);
				if(matchedSlash) {
					match('\"');
				}
				tag.addAttribute(key, value);
			}
		} while (true);
		processTagBegin(tag);
	}
	
	private void processTagBegin(Tag tag) {
		System.out.println("processTagBegin:" + tag.name);
		tags.addElement(tag);
		if (tag.name.equalsIgnoreCase("text")) {
			Color textColor = findTextColor(tag.getAttribute("color"));
			Color shadowColor = findShadowColor(tag.getAttribute("shadowcolor"));
			int shadow = findShadow(tag.getAttribute("shadow"));
			String font = findTextFont(tag.getAttribute("font"));
			TextStyle style = new TextStyle(font, textColor, shadowColor, shadow);
			textStyles.addElement(style);
		} else if (tag.name.equalsIgnoreCase("img")) {
			String file = tag.getAttribute("file");
			int index = parseInt(tag.getAttribute("index"), 0);
			int trans = parseInt(tag.getAttribute("trans"), 0);
			float scale = parseFloat(tag.getAttribute("scale"), 1.0f);
			int hgap = parseInt(tag.getAttribute("hgap"), 0);
			if (file != null) {
				ImageElement element = new ImageElement(doc, new ImageData(file, index), trans, scale, hgap);
				element.setLink(currentLink);
				currentParagraph.addElement(element);
			}
		} else if (tag.name.equalsIgnoreCase("animate")) {
			String file = tag.getAttribute("file");
			int index = parseInt(tag.getAttribute("index"), 0);
			float scale = parseFloat(tag.getAttribute("scale"), 1.0f);
			int hgap = parseInt(tag.getAttribute("hgap"), 0);
			if (file != null) {
				AnimateElement element = new AnimateElement(doc, new AnimateData(file, index), scale, hgap);
				element.setLink(currentLink);
				currentParagraph.addElement(element);
			}
		} else if(tag.name.equalsIgnoreCase("link")) {
			Color textColor = findTextColor(tag.getAttribute("color"));
			Color shadowColor = findShadowColor(tag.getAttribute("shadowcolor"));
			int shadow = findShadow(tag.getAttribute("shadow"));
			String font = findTextFont(tag.getAttribute("font"));
			TextStyle style = new TextStyle(font, textColor, shadowColor, shadow);
			textStyles.addElement(style);
			currentLink = new Link(tag.getAttribute("url"));
		}
	}
	
	private String findTextFont(String value) {
		if(value == null) {
			for(int i = textStyles.size() -1 ; i >=0; i--) {
				TextStyle ts = (TextStyle)textStyles.elementAt(i);
				if(ts.font != null) {
					return ts.font;
				}
			}	
			return null;
		} else {
			return value;
		}
	}
	
	private int findShadow(String value) {
		if(value == null) {
			for(int i = textStyles.size() -1 ; i >=0; i--) {
				TextStyle ts = (TextStyle)textStyles.elementAt(i);
				if(ts.shadow != -1) {
					return ts.shadow;
				}
			}
			return -1;
		} else {
			if(value.equalsIgnoreCase("true"))
				return 1;
			if(value.equalsIgnoreCase("false"))
				return 0;
			return -1;
		}
	}
	
	private Color findTextColor(String value) {
		Color color = parseColor(value);
		if(color == null) {
			for(int i = textStyles.size() -1 ; i >=0; i--) {
				TextStyle ts = (TextStyle)textStyles.elementAt(i);
				if(ts.textColor !=  color) {
					color = ts.textColor;
				}
			}
		}
		return color;
	}
	
	private Color findShadowColor(String value) {
		Color color = parseColor(value);
		if(color == null) {
			for(int i = textStyles.size() -1 ; i >=0; i--) {
				TextStyle ts = (TextStyle)textStyles.elementAt(i);
				if(ts.shadowColor !=  color) {
					color = ts.shadowColor;
				}
			}
		}
		return color;
	}
	
	private void processTagEnd(String tagName) {
		Tag tag = (Tag)tags.elementAt(tags.size() - 1);
		if(!tag.name.equals(tagName))
			throw new IllegalStateException();
		processTagEnd(tag);
	}
	
	private void processTagEnd(Tag tag) {
		System.out.println("processTagEnd:" + tag.name);
		tags.removeElementAt(tags.size() - 1);
		if(tag.name.equalsIgnoreCase("text")) {
			textStyles.removeElementAt(textStyles.size() - 1);
		} else if(tag.name.equalsIgnoreCase("link")) {
			textStyles.removeElementAt(textStyles.size() - 1);
			currentLink = null;
		}
	}
	
	
	private int parseInt(String value, int defaultValue) {
		if(value == null)
			return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	private float parseFloat(String value, float defaultValue) {
		if(value == null)
			return defaultValue;
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
	
	private Color parseColor(String value) {
		if(value == null)
			return null;
		if(value.startsWith("0x") || value.startsWith("0X")) {
			try {
				return new Color(Display.getCurrent(), RGBUtil.intToRGB(Integer.parseInt(value.substring(2), 16)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			return new Color(Display.getCurrent(), RGBUtil.intToRGB(Integer.parseInt(value)));
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private void match(char c) {
		if(c == peek) 
			peek();
		else
			throw new IllegalStateException();
	}
	
	private String matchKey() {
		StringBuilder sb = new StringBuilder();
		sb.append((char)peek);
		peek();
		while(peek != '=') {
			sb.append((char)peek);
			peek();
		}
		return sb.toString();
	}
	
	private String matchValue() {
		StringBuilder sb = new StringBuilder();
		sb.append((char)peek);
		peek();
		while(peek != '\"' && peek != ' ' && peek != '>') {
			sb.append((char)peek);
			peek();
		}
		return sb.toString();
	}
	
	private String matchTagName() {
		StringBuilder sb = new StringBuilder();
		sb.append((char)peek);
		peek();
		while(peek != ' ' && peek != '>') {
			sb.append((char)peek);
			peek();
		}
		return sb.toString();
	}
	
	
	private void matchText() {
		Vector vec = new Vector(5);
		StringBuilder sb = new StringBuilder();
		do{
			switch(peek) {
				case '\n':
					sb.append('\n');
					vec.addElement(sb.toString());
					sb = new StringBuilder();
					peek();
					break;
				case '\\':
					match('\\');
					if(peek == 'n') {
						peek = '\n';
						break;
					}
					if(peek == -1) {
						if(sb.length() > 0) {
							vec.addElement(sb.toString());
							sb = new StringBuilder();
						}
						break;
					}
					int c = getEscapeCharacter(peek);
					if(c != -1) {
						sb.append((char)c);
					}
					peek();
					break;
				default:
					sb.append((char)peek);
					peek();
			}
		} while(peek != '<' && peek != -1);
		if(sb.length() > 0) {
			vec.addElement(sb.toString());
		}
		processText(vec);
	}
	
	private void processText(Vector strings) {
		TextStyle textStyle = (TextStyle) textStyles.elementAt(textStyles
				.size() - 1);

		for (int i = 0; i < strings.size(); i++) {
			int start = result.length();
			result.append((String) strings.elementAt(i));
			boolean newParagraph = result.charAt(result.length() - 1) == '\n';
			int end = newParagraph ? result
					.length() - 1 : result.length();
			TextElement element = new TextElement(doc, start, end, textStyle.textColor, textStyle.shadowColor, textStyle.shadow, textStyle.font);
			element.setLink(currentLink);
			currentParagraph.addElement(element);
			if(newParagraph)
				newParagraph();
		}
	}
	
	private void newParagraph() {
		paragraphs.addElement(currentParagraph);
		currentParagraph = new Paragraph();
	}
	
	private int getEscapeCharacter(int peek) {
		switch(peek) {
			case '\\':
				return '\\';
			case '>':
				return '>';
			case '<':
				return '<';
			default:
				return -1;
		}
	}
	
	protected void peek() {
		if(position >= text.length()) {
			peek = -1;
		} else {
			peek = text.charAt(position++);
		}
	}
	
	static class Tag{
		public String name;
		public Hashtable attributes = new Hashtable(3);
		
		public Vector content;
		
		public Tag(String name) {
			this.name = name;
		}
		
		public void addAttribute(String key, String value) {
			attributes.put(key, value);
		}
		
		public String getAttribute(String key) {
			return (String)attributes.get(key);
		}
		
	}
	
	
	static class TextStyle {
		public String font;
		public Color textColor;
		public Color shadowColor;
		public int shadow;
		
		public TextStyle(String font, Color textColor, Color shadowColor, int shadow) {
			this.font = font;
			this.textColor = textColor;
			this.shadowColor = shadowColor;
			this.shadow = shadow;
		}
	}
	
	public static void main(String[] args) {
		String text = "abcd\nskdfd<text>fjdisldsdhgsl</text>kjdljfkdsjaksdjafjklsda;kjfks<link url=\"aaa\"><img file=\"aaaaa.png\" url=\"cccc\"/>xxxxx</link>";
		Document doc = new Document();
		Parser parser = new Parser(doc, text);
		parser.parse();
		Vector paragraphs = parser.paragraphs;
		for(int i = 0; i < paragraphs.size(); i++) {
			Paragraph paragraph = (Paragraph)paragraphs.elementAt(i);
			for(int j = 0; j < paragraph.getElementCount(); j++) {
				Element element = paragraph.getElement(j);
				System.out.println("paragraph:" + i +"   " + element.toString());
			}
		}
	}
}

