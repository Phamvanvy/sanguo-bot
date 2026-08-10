package com.pip.sanguo.editor.item;

import java.awt.Color;
import java.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.EquipmentPrefix;
import com.pip.sanguo.data.item.DropItem;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.SubDropGroup;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.skill.DescriptionPattern;
import com.pip.sanguo.editor.util.Constants;
import com.pip.util.SWTUtils;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

/**
 * 图形化拖拽式掉落组物品掉落率编辑器。
 * @author lighthu
 */
public class DropGroupItemListEditor extends Canvas implements PaintListener, DisposeListener {
    private final static int MARGIN = 4;
    private final static int ROW_HEIGHT = 20;
    
    // 编辑对象
    private SubDropGroup group;
    
    // 锁定的掉落项的ID组合（不是索引，而是DropItem的ID）
    private Set<Integer> lockIDs = new HashSet<Integer>();
    
    // 各条目显示临时数据
    private Rectangle[] barBounds;
    private int[] barWidth;
    private double topLimit = 1.0;
    
    // 拖动项目信息
    private int dragIndex;
    private Point dragStartPoint;
    private int dragStartWidth;
    
    // 缓存图片
    private Image bufferImg;
    // 删除按钮图片
    private Image deleteImg = EditorPlugin.getDefault().getImageRegistry().get("delete");
    
    private ModifyListener listener;
    
    public DropGroupItemListEditor(Composite parent, int style) {
        super(parent, style | SWT.NO_BACKGROUND);
        addPaintListener(this);
        setBackground(parent.getBackground());

        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                if (dragIndex == -1) {
                    // 没有拖拽，改变鼠标指针
                    int anchor = getAnchorAt(e.x, e.y);
                    if (anchor == -1) {
                        setCursor(ImageViewer.cursorArrow);
                    } else {
                        setCursor(ImageViewer.getCursorOfAnchor(1));
                    }
                } else {
                    // 正在拖拽，设置新比例
                    int neww = dragStartWidth + e.x - dragStartPoint.x;
                    if (neww < 0) {
                        neww = 0;
                    } else if (neww > barBounds[dragIndex].width) {
                        neww = barBounds[dragIndex].width;
                    }
                    if (neww != barWidth[dragIndex]) {
                        double newp = topLimit * neww / barBounds[dragIndex].width;
                        group.setWeightByPercent(dragIndex, newp, lockIDs);
                        fireModified();
                    }
                    redraw();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (e.button == 1) {
                    int anchor = getAnchorAt(e.x, e.y);
                    if (anchor != -1 && !lockIDs.contains(group.dropGroup.get(anchor).id)) {
                        dragIndex = anchor;
                        dragStartPoint = new Point(e.x, e.y);
                        dragStartWidth = barWidth[dragIndex];
                    }
                } else if (e.button == 3) {
                    for (int i = 0; i < barBounds.length; i++) {
                        if (barBounds[i].contains(e.x, e.y)) {
                            DropItem item = group.dropGroup.get(i);
                            if (lockIDs.contains(item.id)) {
                                lockIDs.remove(item.id);
                            } else {
                                lockIDs.add(item.id);
                            }
                            redraw();
                        }
                    }
                }
            }
            
            public void mouseUp(MouseEvent e) {
                if (e.button == 1) {
                    if (dragIndex != -1) {
                        dragIndex = -1;
                    } else {
                        int iw = deleteImg.getBounds().width;
                        int ih = deleteImg.getBounds().height;
                        for (int i = 0; i < barBounds.length; i++) {
                            Rectangle r = barBounds[i];
                            Rectangle rect = new Rectangle(r.x + r.width + MARGIN, r.y + (r.height - ih) / 2, iw, ih);
                            if (rect.contains(e.x, e.y)) {
                                onDeleteItem(i);
                                break;
                            }
                        }
                    }
                }
            }
            
            public void mouseDoubleClick(MouseEvent e) {
                if (e.button == 1) {
                    for (int i = 0; i < barBounds.length; i++) {
                        if (barBounds[i].contains(e.x, e.y)) {
                            onEdit(i);
                        }
                        Rectangle rect = new Rectangle(MARGIN, barBounds[i].y, barBounds[i].x - MARGIN, barBounds[i].height);
                        if (rect.contains(e.x, e.y)) {
                            onEditCount(i);
                        }
                    }
                }
            }
        });
        addDisposeListener(this);
    }
    
    public void setInput(SubDropGroup g) {
        if (this.group == g) {
            // 没有改变对象，但列表内容可能改变
            Set<Integer> tmpSet = new HashSet<Integer>();
            for (DropItem item : g.dropGroup) {
                if (lockIDs.contains(item.id)) {
                    tmpSet.add(item.id);
                }
            }
            this.group = g;
            lockIDs = tmpSet;
        } else {
            // 新对象
            this.group = g;
            lockIDs.clear();
        }
        dragIndex = -1;
        
        // 重新计算控件大小
        int height = this.group.dropGroup.size() * (ROW_HEIGHT + MARGIN) + MARGIN;
        Point ps = this.getParent().getSize();
        if (height < ps.y) {
            setSize(ps.x, height);
        } else {
            setSize(ps.x - 20, height);
        }
        
        redraw();
    }
    
    // 手工编辑一个掉落项目的掉落率
    private void onEdit(int index) {
        DropItem item = group.dropGroup.get(index);
        if (lockIDs.contains(item.id)) {
            return;
        }
        InputDialog dlg = new InputDialog(getShell(), "设置", "请输入百分比", String.valueOf(item.dropRate * 100.0), new IInputValidator() {
            public String isValid(String newText) {
                try {
                    double f = Double.parseDouble(newText);
                    if (f < 0.0) {
                        return "不能小于0。";
                    } else if (f > 100.0) {
                        return "不能大于100%。";
                    }
                    return null;
                } catch (Exception e) {
                    return "格式错误。 ";
                }
            }
        });
        if (dlg.open() == Dialog.OK) {
            double newper = Double.parseDouble(dlg.getValue()) / 100.0;
            group.setWeightByPercent(index, newper, lockIDs);
            fireModified();
            redraw();
        }
    }
    
    // 编辑一个掉落项目的数量
    private void onEditCount(int index) {
        DropItem item = group.dropGroup.get(index);
        EditDropItemCountDialog dlg = new EditDropItemCountDialog(getShell());
        dlg.minCount = item.quantityMin;
        dlg.maxCount = item.quantityMax;
        if (dlg.open() == Dialog.OK) {
            item.quantityMax = dlg.maxCount;
            item.quantityMin = dlg.minCount;
            fireModified();
            redraw();
        }
    }
    
    // 删除一个掉落项
    private void onDeleteItem(int index) {
        group.setWeightByPercent(index, 0.0, lockIDs);
        DropItem item = group.dropGroup.get(index);
        group.dropGroup.remove(item);
        lockIDs.remove(item.id);
        fireModified();
        redraw();
    }
    
    /**
     * 所有未锁定掉落项目均分掉落率。
     */
    public void average() {
        group.averageWeight(lockIDs);
        fireModified();
        redraw();
    }
    
    private int getAnchorAt(int x, int y) {
        for (int i = 0; i < barBounds.length; i++) {
            Rectangle r = barBounds[i];
            int w = barWidth[i];
            Rectangle rect = new Rectangle(r.x + w - 4, r.y, 8, r.height);
            if (rect.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
    
    public void addModifyListener(ModifyListener l) {
        listener = l;
    }
    
    private void fireModified() {
        if (listener != null) {
            Event e = new Event();
            e.widget = this;
            ModifyEvent event = new ModifyEvent(e);
            listener.modifyText(event);
        }
    }
    
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
            bufferGC.setBackground(getBackground());
            bufferGC.fillRectangle(0, 0, size.x, size.y);
            
            layoutBars(bufferGC, size);
            paintHeader(bufferGC);
            paintBars(bufferGC);
            paintPercents(bufferGC);
            
            e.gc.drawImage(bufferImg, 0, 0);
        } catch (Throwable e1) {
            e1.printStackTrace();
        } finally {
            if (bufferGC != null) {
                bufferGC.dispose();
            }
        }
    }
    
    private void paintHeader(GC gc) {
        for (int i = 0; i < group.dropGroup.size(); i++) {
            DropItem item = group.dropGroup.get(i);
            String t = item.toString();
            int th = gc.textExtent(t).y;
            if (item.dropType == DropItem.DROP_TYPE_ITEM || item.dropType == DropItem.DROP_TYPE_EQUI) {
                int color = Constants.QUALITY_COLOR[((Item)item.dropObj).quality];
                gc.setForeground(SWTResourceManager.getColor(color >> 16, (color >> 8) & 0xFF, color & 0xFF));
            } else {
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
            }
            gc.drawText(t, 2, barBounds[i].y + (barBounds[i].height - th) / 2);
        }
    }
    
    private void paintBars(GC gc) {
        gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        int ih = deleteImg.getBounds().height;
        for (int i = 0; i < group.dropGroup.size(); i++) {
            DropItem item = group.dropGroup.get(i);
            gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            gc.fillRectangle(barBounds[i].x + 1, barBounds[i].y + 1, barBounds[i].width - 1, barBounds[i].height - 1);
            if (lockIDs.contains(item.id)) {
                SWTUtils.drawRoundRect(gc, barBounds[i], barWidth[i], 0xDADADA, 0xF9F9F9, 0xE6E6E6);
            } else {
                SWTUtils.drawRoundRect(gc, barBounds[i], barWidth[i], 0x9ADFFE, 0xF2F9FE, 0xD6F0FD);
            }
            
            gc.drawImage(deleteImg, barBounds[i].x + barBounds[i].width + MARGIN, barBounds[i].y + (barBounds[i].height - ih) / 2);
        }
    }
    
    private void paintPercents(GC gc) {
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        for (int i = 0; i < group.dropGroup.size(); i++) {
            DropItem item = group.dropGroup.get(i);
            String t = DescriptionPattern.formatPercent(item.dropRate * 100.0);
            int th = gc.textExtent(t).y;
            int tx = barBounds[i].x + (barBounds[i].width - gc.textExtent(t).x) / 2;
            gc.drawText(t, tx, barBounds[i].y + (barBounds[i].height - th) / 2, true);
        }
    }
    
    private void layoutBars(GC gc, Point size) {
        int tw = 0;
        double maxPer = 0.0;
        for (int i = 0; i < group.dropGroup.size(); i++) {
            DropItem item = group.dropGroup.get(i);
            int w = gc.textExtent(item.toString()).x;
            if (w > tw) {
                tw = w;
            }
            if (item.dropRate > maxPer) {
                maxPer = item.dropRate;
            }
        }
        if (maxPer == 0.0) {
            maxPer = 1.0;
        } else if (maxPer > 0.50f) {
            maxPer = 1.0;
        } else {
            maxPer *= 2;
        }
        topLimit = maxPer;
        
        int bw = size.x - tw - 2 * MARGIN - 20 - deleteImg.getBounds().width - 2 * MARGIN;
        int bh = ROW_HEIGHT;
        barBounds = new Rectangle[group.dropGroup.size()];
        barWidth = new int[group.dropGroup.size()];
        for (int i = 0; i < group.dropGroup.size(); i++) {
            DropItem item = group.dropGroup.get(i);
            barBounds[i] = new Rectangle(tw + 2 * MARGIN, MARGIN + (ROW_HEIGHT + MARGIN) * i, bw, bh);
            barWidth[i] = (int)(item.dropRate * bw / topLimit);
        }
    }
    
    public void widgetDisposed(DisposeEvent e) {
        if (bufferImg != null) {
            bufferImg.dispose();
        }
    }
}
