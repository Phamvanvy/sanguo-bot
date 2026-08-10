package com.pip.uieditor.model.text;

import java.util.LinkedList;
import java.util.Vector;

import org.eclipse.swt.graphics.Color;

import pip.ImageSet;

import com.pip.j0ide.Settings;
import com.pip.uieditor.model.ImageData;
import com.pip.uieditor.model.RGBUtil;


public class DefaultRichTextVisitor implements RichTextVisitor {
	
	private static final Color DEFAULT_TEXT_COLOR = new Color(null, 255, 255, 255);
	private static final Color DEFAULT_SHADOW_COLOR = new Color(null, 255, 255, 255);
	
	private LinkedList<Tag> tags;
	private Document doc;
	private Paragraph currentParagraph;
	private Link currentLink;
	private StringBuilder content;
	private Vector<Paragraph> paragraphs;
	private LinkedList<Integer> textColors;
	private LinkedList<Integer> shadowColors;
	private int underLine = 0;
	private int numberText = 0;
	private Color defaultTextColor = DEFAULT_TEXT_COLOR;
	private Color defaultShadowColor = DEFAULT_SHADOW_COLOR;
	
	public DefaultRichTextVisitor(Document doc) {
		this.doc = doc;
		this.tags = new LinkedList<Tag>();
		this.currentParagraph = new Paragraph();
		this.content = new StringBuilder(1024);
		this.paragraphs = new Vector();
		this.textColors = new LinkedList<Integer>();
		this.shadowColors = new LinkedList<Integer>();
	}

	@Override
	public void text(String text) {
		System.out.printf("Text[%s]\n", text);
		int pos = 0;
		StringBuilder sb =  new StringBuilder(128);
		while(pos < text.length()) {
			char c = text.charAt(pos);
			if(c == '\n') {
				addText(sb.toString());
				this.paragraphs.addElement(this.currentParagraph);
				this.currentParagraph = new Paragraph();
			}
			if(c == '\\' && (pos + 1) < text.length() && text.charAt(pos + 1) == 'n') {
				pos++;
				addText(sb.toString());
				this.paragraphs.addElement(this.currentParagraph);
				this.currentParagraph = new Paragraph();
			} else {
				sb.append(c);
			}
			pos++;
//			sb.append(c);
		}
		if(sb.length() > 0) {
			addText(sb.toString());
		}
	}
	
	private void addText(String text) {
		if (text.length() > 0) {
			if (numberText > 0) {
				if (Settings.numberImageFile != null
						&& Settings.numberImageFile.exists()
						&& Settings.numberImageFile.isFile()) {
					for (int i = 0; i < text.length(); i++) {
						char c = text.charAt(i);
						int number = c - '0';
						if (number >= 0 && number <= 9) {
							ImageData image = new ImageData(
									Settings.numberImageFile.getName(),
									Settings.numberImageMaps[number]);
							ImageElement element = new ImageElement(doc, image, 0, 1.0f, 0);
							this.currentParagraph.addElement(element);
						}
					}
				}
			} else {
				int begin = content.length();
				content.append(text);
				int end = content.length();
				Color textColor = getCurrentTextColor();
				Color shadowColor = getCurrentShadowColor();
				Link link = getCurrentLink();
				TextElement element = new TextElement(doc, begin, end,
						textColor, shadowColor, isShadowText() ? 1 : -1, null);
				element.setLink(link);
				this.currentParagraph.addElement(element);				
			}
		}
	}
	
	private Color getCurrentShadowColor() {
		if(shadowColors.size() > 0) {
			return new Color(null,  RGBUtil.intToRGB(shadowColors.getLast()));
		}
		return null;
	}
	
	private boolean isUnderLine() {
		return underLine > 0;
	}
	
	private Link getCurrentLink() {
		return currentLink;
	}
	
	private boolean isShadowText() {
		return shadowColors.size() > 0;
	}
	
	private Color getCurrentTextColor() {
		if(textColors.size() > 0) {
			return new Color(null,  RGBUtil.intToRGB(textColors.getLast()));
		}
		return null;
	}


	@Override
	public void braceTag(String content) {
		//{#VarUIRes,bgindex,VarUIRes,fgindex}图形格式，以#开头。{#背景资源名, 背景资源index, 前景资源名, 前景资源index}
		if(content.startsWith("#")) { 
			String[] args = content.split("\\,");
			if(args.length != 2 && args.length != 4)
				throw new IllegalArgumentException();
			String file = args[0].substring(1);
			int index = Integer.parseInt(args[1]);
			System.out.printf("File[%s],Index[%d]\n", file, index);
			ImageSet image = null;
			try {
				ImageData data = new ImageData(file, index);
				ImageElement element = new ImageElement(doc, data, 0, 1.0f, 0);
				this.currentParagraph.addElement(element);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public void tagBegin(char type, String attribute) {
		System.out.printf("TagBegin[%c]Attribute[%s]\n", type, attribute);
		if(type=='L') {
			currentLink = new Link(attribute);
		} else if(type == 'c') {
			textColors.add(Integer.parseInt(attribute, 16));
		} else if( type == 'u') {
			underLine++;
		} else if( type == 'd') {
			shadowColors.add(Integer.parseInt(attribute, 16));
		} else if( type == 'i') {
			numberText++;
		}
		
	}
	

	@Override
	public void tagEnded(char type) {
		System.out.printf("TagEnded[%c]\n", type);
		if(type=='L') {
			currentLink = null;
		} else if(type == 'c') {
			textColors.removeLast();
		} else if(type == 'u') {
			underLine--;
		} else if(type == 'd') {
			shadowColors.removeLast();
		} else if(type == 'i') {
			numberText--;
		}
	}

	@Override
	public void ended() {
		System.out.printf("Ended\n");
		if(this.currentParagraph.getElementCount() > 0) {
			this.paragraphs.addElement(this.currentParagraph);
		}
	}
	
	public Vector<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public String getContent() {
		return this.content.toString();
	}
	
	static class Tag {
		public char  type;
		public String attribute;
		
		public Tag(char type, String attribute) {
			this.type = type;
			this.attribute = attribute;
		}
	}
	
	public static void main(String[] args) throws Exception{
		String s = "<cff00ff>发疯</c>的原\n因<i>1</i>";
		RichTextParser parser = new RichTextParser(s);
		DefaultRichTextVisitor visitor = new DefaultRichTextVisitor(null);
		parser.parser(visitor);
		System.out.println("ok");
	}
}

