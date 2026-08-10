package com.pip.uieditor.model;

import org.eclipse.gef.requests.CreationFactory;

import com.pip.uieditor.model.classic.type.Type;

public class WidgetCreationFactory implements CreationFactory {
	
	protected Class type;
	
	public WidgetCreationFactory( Class type) {
		this.type = type;
	}
	
	@Override
	public Object getNewObject() {
		try {
			return type.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}

	@Override
	public Class getObjectType() {
		return type;
	}

}
