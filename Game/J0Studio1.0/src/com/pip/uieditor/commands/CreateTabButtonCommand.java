package com.pip.uieditor.commands;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.commands.Command;

import com.pip.uieditor.model.TabBar;
import com.pip.uieditor.model.TabButton;
import com.pip.uieditor.model.Widget;

public class CreateTabButtonCommand extends Command {
	
	private TabBar tabBar;
	
	private TabButton tabButton;

	
	public void setTabBar(TabBar tabBar) {
		this.tabBar = tabBar;
	}
	
	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		if(tabButton == null) {
			if(tabBar.getChildCount() > 0) {
				TabButton btn = (TabButton)tabBar.getChild(0);
				tabButton = btn.clone();
				String name = newName(tabButton, tabBar);
				tabButton.setName(name);
			} else {
				tabButton = new TabButton();
				String name = newName(tabButton, tabBar);
				tabButton.setName(name);
				tabButton.setSize(new Dimension(10, 10));
			}
		}
		tabBar.addChild(tabButton);
	}

	@Override
	public void undo() {
		tabBar.removeChild(tabButton);
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
