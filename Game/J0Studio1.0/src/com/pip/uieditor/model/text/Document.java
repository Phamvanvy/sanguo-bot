package com.pip.uieditor.model.text;

import java.util.Vector;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;

import com.pip.j0ide.Settings;

public class Document {
	
	String content = null;
	
	private Vector<Paragraph> paragraphs = null;
	
	public Document() {
		paragraphs = new Vector<Paragraph>(3);
	}
	
	protected void parse(String content) {
		if(content == null) {
			this.paragraphs.removeAllElements();
			this.content = null;
			return;
		}
		if (Settings.textStyle == 0) {
			RichTextParser parser = new RichTextParser(content);
			DefaultRichTextVisitor visitor = new DefaultRichTextVisitor(this);
			try {
				parser.parser(visitor);
				this.paragraphs = visitor.getParagraphs();
				this.content = visitor.getContent();
			} catch (Exception e) {
				this.paragraphs = new Vector();
				this.content = "";
				e.printStackTrace();
			}			
		} else {
			Parser parser = new Parser(this, content);
			parser.parse();
			this.content = parser.getContent();
			this.paragraphs = parser.getParagraphs();
		}
	}
	
	public void setText(String text) {
		parse(text);
	}
	
	
	public String subString(int start, int end) {
		return this.content.substring(start, end);
	}

	public char charAt(int index) {
		return this.content.charAt(index);
	}
	
	public Paragraph getParagraph(int index) {
		return (Paragraph)paragraphs.elementAt(index);
	}
	
	public int getParagraphCount() {
		return this.paragraphs.size();
	}
	
	public static class FormatContext {
		public int lineWidth;
		public Line line;
		public int offset;
		public FontData font;
		public Vector views;
		public GC gc;
		
		public FormatContext(GC gc, int lineWidth, FontData font) {
			this.gc  = gc;
			this.lineWidth = lineWidth;
			this.line = new Line(0, 0);
			this.font = font;
			this.views = new Vector();
		}
		
		public void addView(View view) {
			views.addElement(view);
		}
		
		public Vector getViews() {
			return this.views;
		}
		
		public boolean incOffset(int width) {
			if(offset == 0) {
				offset += width;
				return true;
			}
			int v = offset + width;
			if(v <= lineWidth) {
				offset = v;
				return true;
			}
			return false;
		}
		
		public Line newLine() {
			this.line = new Line(this.line.line + 1, 0);
			offset = 0;
			return this.line;
		}
	}
	
	public static class Line {
		
		public int line;
		public int height;
		
		public Line(int line, int height) {
			this.line = line;
			this.height = height;
		}
	}
}
