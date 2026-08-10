package com.pip.uieditor.commands;


import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import com.pip.uieditor.model.Container;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.Widget;
import com.pip.uieditor.model.classic.type.Type;

/**
 * 新建Widget的Command。
 * @author Jeffrey
 *
 */
public class WidgetCreateCommand extends Command {
	
	private Container parent;
	private Widget widget;
	private Point location;
	private Dimension size;
	
	public WidgetCreateCommand() {
		super();
	}

	@Override
	public void execute() {
		redo();
	}
	
	public void setContainer(Container parent) {
		this.parent = parent;
	}
	
	public void setWidget(Widget widget) {
		this.widget = widget;
	}
	
	public void setLocation(Point location) {
		this.location = location;
	}
	
	public void setSize(Dimension size) {
		this.size = size;
	}

	@Override
	public void redo() {
		
		Point p = parent.getAbsoluteLocation();
		if(size == null) {
			size = new Dimension(20, 10);
		}
		widget.setLocation(location.translate(-p.x - parent.getClientAreaX() , -p.y - parent.getClientAreaY()));
		widget.setSize(size);
		widget.setName(newName(widget, parent));
		this.parent.addChild(widget);
	}
	
	/**
	 * 寻找一个合适的Container，这个Container的bounds能够容纳新建的Bounds，如果最后没找到，就用Screen做Container
	 * @param con
	 * @return
	 */
	protected Container findContainer(Container con) {
		if(con instanceof Screen)
			return con;
		if(con.getAbsoluteBounds().contains(new Rectangle(location, size))) {
			return con;
		} else {
			return findContainer((Container)con.getParent());
		}
	}

	@Override
	public void undo() {
		this.parent.removeChild(widget);
	}
	
	protected String newName(Widget widget, Widget parent) {
		String defaultName = widget.getDefaultName();
		int i = 1;
		while(true) {
			String name = defaultName + "_" + (i++);
			if(parent.getScreen().findWidget(name) == null) {
				return name;
			}
		}
	
	}
}
