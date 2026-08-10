package com.pip.uieditor.commands;

import org.eclipse.gef.commands.Command;

/**
 * 因为在Palette中选中一个控件以后，将会一直触发创建的Request，只有当鼠标拉出一个矩形区域的时候才应该生成创建Widget的Command。
 * 其他时候都应该返回这个Command
 * @author Jeffrey
 *
 */
public class UnableCreateCommand extends Command {
	
	public final static  UnableCreateCommand INSTANCE = new UnableCreateCommand();
	
	private  UnableCreateCommand() {
		
	}
	
	@Override
	public void execute() {
	}

	@Override
	public boolean canUndo() {
		return false;
	}
}
