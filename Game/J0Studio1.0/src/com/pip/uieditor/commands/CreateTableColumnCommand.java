package com.pip.uieditor.commands;

import org.eclipse.gef.commands.Command;

import com.pip.uieditor.model.Table;
import com.pip.uieditor.model.TableColumn;

public class CreateTableColumnCommand extends Command{
	
	private Table table;
	
	private TableColumn column;
	
	public void setTable(Table table) {
		this.table = table;
	}
	
	public void setTableColumn(TableColumn column) {
		this.column = column;
	}
	
	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {
		table.addTableColumn(column);
	}


	@Override
	public void undo() {
		table.removeTableColumn(column);
	}
}
