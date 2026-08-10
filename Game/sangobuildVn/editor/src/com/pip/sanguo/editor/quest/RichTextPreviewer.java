package com.pip.sanguo.editor.quest;

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.pip.sanguo.editor.quest.QuestFlowViewer.ComputeSizeIterator;
import com.pip.sanguo.editor.quest.QuestFlowViewer.PaintIterator;
import com.swtdesigner.SWTResourceManager;

/**
 * 混合格式文件查看组件。
 * @author lighthu
 */
public class RichTextPreviewer extends Composite implements PaintListener {
    // 文本内容
    protected String text = "";
    // 缓存
    private Image bufferImg;
    
    public RichTextPreviewer(Composite parent, int style) {
        super(parent, SWT.NONE | SWT.NO_BACKGROUND);
        addPaintListener(this);
    }
    
    public void setText(String t) {
        text = t;
        redraw();
    }
    
    /**
     * 绘制控件。
     */
    public void paintControl(PaintEvent e) {
        GC bufferGC = null;
        try {
            Point size = getSize();
            if (bufferImg != null && (bufferImg.getBounds().width != size.x || bufferImg.getBounds().height != size.y)) {
                bufferImg.dispose();
                bufferImg = null;
            }
            if (bufferImg == null) {
                bufferImg = new Image(getDisplay(), size.x, size.y);
            }
            bufferGC = new GC(bufferImg);
            bufferGC.setClipping(0, 0, size.x, size.y);
            try {
                paintContent(bufferGC);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            e.gc.drawImage(bufferImg, 0, 0);
        } catch (Throwable e1) {
            e1.printStackTrace();
        } finally {
            if (bufferGC != null) {
                bufferGC.dispose();
            }
        }
    }

    /**
     * 在缓存上绘制内容。
     */
    public void paintContent(GC gc) {
        // 绘制背景
        Point size = getSize();
        gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        gc.setFont(getFont());
        gc.fillRectangle(0, 0, size.x, size.y);

        // 绘制文本
        int x = 4;
        int y = 4;
        List<TextSegment> segs = formatText(gc, text, size.x - 8);
        int lh = gc.getFontMetrics().getHeight() + 1;
        int currentLine = 0;
        int xpos = 0;
        for (TextSegment seg : segs) {
            if (seg.lineNo != currentLine) {
                currentLine++;
                xpos = 0;
            }
            int clr = seg.color;
            gc.setForeground(SWTResourceManager.getColor((clr >> 16) & 0xFF, (clr >> 8) & 0xFF, clr & 0xFF));
            gc.drawText(seg.content, x + xpos, y + lh * currentLine);
            xpos += gc.textExtent(seg.content).x;
        }
    }
    
    /**
     * 格式化文本。
     * @param gc
     * @param text
     * @param maxWidth
     * @return
     */
    private static List<TextSegment> formatText(GC gc, String text, int maxWidth) {
        char[] arr = text.toCharArray();
        int count = arr.length;
        List<TextSegment> ret = new ArrayList<TextSegment>();
        int currentLine = 0;
        int xpos = 0;
        List<String> tagStack = new ArrayList<String>();
        List<Integer> colorStack = new ArrayList<Integer>();
        int color = SWT.COLOR_BLACK;
        int state = 0;      // 0 - 普通文本、1 - 在标记中、2 - 在变量名中
        StringBuffer buf = new StringBuffer();
        
        for (int i = 0; i < count; i++) {
            char ch = arr[i];
            if (ch == '\r') {
                // 全局忽略\r，只考虑\n
                continue;
            }
            if (state == 0) {
                if (ch == '\n') {
                    // 换行，结束当前段
                    if (buf.length() > 0) {
                        ret.add(new TextSegment(currentLine, color, buf.toString()));
                        buf.setLength(0);
                    }
                    currentLine++;
                    xpos = 0;
                } else if (ch == '<') {
                    // 进入标记，结束当前段
                    if (buf.length() > 0) {
                        ret.add(new TextSegment(currentLine, color, buf.toString()));
                        buf.setLength(0);
                    }
                    state = 1;
                } else if (ch == '$' && i < count - 1 && arr[i + 1] == '{') {
                    // 进入变量名
                    state = 2;
                    i++;
                } else {
                    // 普通文本，计算宽度，如果到宽度了折行
                    int tw = gc.textExtent("" + ch).x;
                    if (tw + xpos > maxWidth) {
                        if (buf.length() > 0) {
                            ret.add(new TextSegment(currentLine, color, buf.toString()));
                            buf.setLength(0);
                        }
                        currentLine++;
                        xpos = tw;
                    } else {
                        xpos += tw;
                    }
                    buf.append(ch);
                }
            } else if (state == 1) {
                if (ch == '>') {
                    state = 0;
                    String tagName = buf.toString();
                    if (tagName.length() == 0) {
                        continue;
                    }
                    buf.setLength(0);
                    if (!tagName.startsWith("/")) {
                        // 处理起始标记
                        if (tagName.startsWith("c")) {
                            tagStack.add("c");
                            colorStack.add(color);
                            color = Integer.parseInt(tagName.substring(1), 16);
                        } else if (tagName.startsWith("i")) {
                            tagStack.add(tagName);
                            colorStack.add(color);
                            color = 0x00FF00;
                        } else if (tagName.startsWith("m")) {
                            tagStack.add(tagName);
                            colorStack.add(color);
                            color = 0xFFFF00;
                        } else if (tagName.startsWith("n")) {
                            tagStack.add(tagName);
                            colorStack.add(color);
                            color = 0xFF0000;
                            
                            // <n>123,左慈(许昌:12,12)</n>，向后查找到第一个,前的内容忽略
                            while (i < count && ch != ',') {
                                i++;
                                ch = arr[i];
                            }
                        } else if (tagName.startsWith("l")) {
                            tagStack.add(tagName);
                            colorStack.add(color);
                            color = 0xFF0000;
                            
                            // <l>123,许昌:12,12</l>，向后查找到第一个,前的内容忽略
                            while (i < count && ch != ',') {
                                i++;
                                ch = arr[i];
                            }
                        }
                    } else {
                        // 处理结束标记
                        tagName = tagName.substring(1);
                        int tss = tagStack.size();
                        if (tss == 0) {
                            continue;
                        }
                        String oldTag = tagStack.get(tss - 1);
                        if (!oldTag.equals(tagName)) {
                            continue;
                        }
                        color = colorStack.remove(tss - 1);
                        tagStack.remove(tss - 1);
                    }
                } else {
                    buf.append(ch);
                }
            } else if (state == 2) {
                if (ch == '}') {
                    // 变量显示为???
                    int tw = gc.textExtent("???").x;
                    if (tw + xpos > maxWidth) {
                        if (buf.length() > 0) {
                            ret.add(new TextSegment(currentLine, color, buf.toString()));
                            buf.setLength(0);
                        }
                        currentLine++;
                        xpos = tw;
                    } else {
                        xpos += tw;
                    }
                    buf.append("???");
                    state = 0;
                }
            }
        }
        if (state == 0 && buf.length() > 0) {
            ret.add(new TextSegment(currentLine, color, buf.toString()));
            buf.setLength(0);
        }
        return ret;
    }
    
    // 格式化后的文本段
    private static class TextSegment {
        public int lineNo;          // 行号
        public int color;           // 颜色
        public String content;      // 文本内容
        
        public TextSegment() {}
        
        public TextSegment(int l, int c, String con) {
            lineNo = l;
            color = c;
            content = con;
        }
    }
}
