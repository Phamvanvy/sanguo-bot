package com.pip.uieditor.model.classic.type;


public class EnumPropertyType implements PropertyType{

	private String[] items;
	
	public EnumPropertyType(String[] items) {
		this.items = items;
	}
	
	public String getId() {
		return "enum";
	}
	
	@Override
	public String to(Object value) {
		return items[((Integer)value).intValue()];
	}

	@Override
	public Object from(String s) {
		for(int i = 0 ;i < items.length; i++) {
			if(items[i].equals(s))
				return Integer.valueOf(i);
		}
		throw new IllegalArgumentException();
	}
	
	public String[] getItems() {
		return this.items;
	}
}
