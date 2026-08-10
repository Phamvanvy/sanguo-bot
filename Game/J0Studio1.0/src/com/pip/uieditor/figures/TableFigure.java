package com.pip.uieditor.figures;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import com.pip.uieditor.model.Table;

public class TableFigure extends WidgetFigure {

	public TableFigure(Table widget) {
		super(widget);
	}
	
	public void paintFigure(Graphics gc) {
		super.paintFigure(gc);
		Table table = (Table)widget;
		if (table.getSubWidgetCount() > 0) {
			int count = this.getClientArea().height / table.getRowHeight();
			gc.translate(getBounds().x + getInsets().left, getBounds().y
					+ getInsets().top);
			for (int i = 0; i < count; i++) {
				gc.setForegroundColor(ColorConstants.black);
				Rectangle rect = getClientArea();
				int y = rect.y + table.getRowHeight() * (i + 1);
				gc.drawLine(rect.x, y, rect.x + rect.width, y);
			}
			gc.translate(0, 0);
		}
	}
	
}
