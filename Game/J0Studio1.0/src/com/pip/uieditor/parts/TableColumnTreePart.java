package com.pip.uieditor.parts;

import java.beans.PropertyChangeEvent;

import com.pip.uieditor.model.TableColumn;

public class TableColumnTreePart extends UIObjectTreePart {
	
	
	public TableColumnTreePart(TableColumn tableColumn) {
		super(tableColumn);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
	}
	
	@Override
	protected String getText() {
		return "Column";
	}

}
