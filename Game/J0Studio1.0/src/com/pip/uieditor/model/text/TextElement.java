package com.pip.uieditor.model.text;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import com.pip.j0ide.Settings;
import com.pip.uieditor.model.text.Document.FormatContext;


public class TextElement implements Element{
	
	protected int startPosition, endPosition;
	protected int fontIndex = -1;
	
	protected Document doc;
	
	protected Link link;
	
	protected Color textColor, shadowColor;
	
	protected String fontName;
	
	protected FontData font;
	
	protected int shadow;
	
	public TextElement(Document doc, int startPosition, int endPosition, Color textColor, Color shadowColor, int shadow, String font) {
		this.doc = doc;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.textColor = textColor;
		this.shadowColor = shadowColor;
		this.shadow = shadow;
		this.fontName = font;
	}
	
	public int getStartPosition() {
		return this.startPosition;
	}
	
	public int getEndPosition() {
		return this.endPosition;
	}
	
	public void setFontIndex(int fontIndex) {
		this.fontIndex = fontIndex;
	}
	
	public int getFontIndex() {
		return this.fontIndex;
	}
	
	public String getName() {
		return Element.TEXT;
	}
	
	

	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}
	
	public Color getTextColor() {
		return this.textColor;
	}
	
	public Color getShadowColor() {
		return this.shadowColor;
	}
	
	public int getShadow() {
		return this.shadow;
	}

	@Override
	public Document getDocument() {
		return doc;
	}
	
	public String getContent() {
		return doc.subString(this.startPosition, this.endPosition);
	}
	
	public String getContent(int start, int end) {
		return doc.subString(start, end);
	}
	
	public FontData getFont(FormatContext context) {
		if(this.fontName != null && this.fontName.length() > 0) {
			FontData fd = Settings.fonts.get(this.fontName);
			if(fd != null) {
				return fd;
			}
		}
		if(context.font != null) {
			return context.font;
		}
		return Settings.defaultFont;
	}
	
	public FontData getFont() {
		return font;
	}

	@Override
	public void format(FormatContext context) {
		int position = this.startPosition;
		int start = this.startPosition;
		int viewWidth = 0;
		font = getFont(context);
		Font f = new Font(null, font);
		context.gc.setFont(f);
		int fontHeight = context.gc.getFontMetrics().getHeight();
		do{
			char ch = doc.charAt(position);
			if (ch == '\n') {
				if(context.line.height < fontHeight) {
					context.line.height = fontHeight;
				}
				TextView view = new TextView(this, context.line, viewWidth, fontHeight, start, position);
				context.addView(view);
				context.newLine();
				viewWidth = 0;
				start = position;
				break;
			}
			int w = context.gc.getAdvanceWidth(ch);
			if(context.incOffset(w)) {
				viewWidth += w;
				position++;
				if(context.line.height < fontHeight) {
					context.line.height = fontHeight;
				}
				continue;
			}
			if(start != position) {
				if(context.line.height < fontHeight) {
					context.line.height = fontHeight;
				}
				TextView view = new TextView(this, context.line, viewWidth, fontHeight, start, position);
				context.addView(view);
				context.newLine();
				viewWidth = 0;
				start = position;
			}
		} while(position < this.endPosition);
		if(start < position) {
			TextView view = new TextView(this, context.line, viewWidth, fontHeight, start, position);
			context.addView(view);
		}
		f.dispose();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Text]");
		sb.append(doc.subString(this.startPosition, this.endPosition));
		if(link != null) {
			sb.append("[link]").append(link.url);
		}
		return sb.toString();
	}
	
}

