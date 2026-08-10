package com.pip.uieditor.model.classic.type;



/**
 * 一个属性的定义，包括属性的Id以及缺省值以及类型。
 * 属性的Id由属性所属的组和属性的名字共同组成，中间由.分隔，比如Size.width
 * @author Jeffrey
 *
 */
public class PropertyDef {
	
	private String category, name;
	private Object defaultValue;
	private PropertyType propertyType;
	private Type type;
	private String fieldName;
	
	public PropertyDef(Type type, PropertyType propertyType, String category,
			String name, Object defaultValue) {
		this.type = type;
		this.propertyType = propertyType;
		this.category = category;
		this.name = name;
		this.defaultValue = defaultValue;
		type.addPropertyDef(this);
	}
	
	
	
	public String getFieldName() {
		return fieldName;
	}


	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isNative() {
		return this.fieldName != null;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public String getId() {
		return category +"."+ name;
	}
	
	public PropertyType getPropertyType() {
		return this.propertyType;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	
}
