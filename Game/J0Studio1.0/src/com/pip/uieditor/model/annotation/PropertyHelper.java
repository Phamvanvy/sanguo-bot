package com.pip.uieditor.model.annotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.PropertyDescriptor;

public class PropertyHelper {
	
	
	public static PropertyDescriptor[] createPropertyDescriptors(Class<?> klass) {
		List<PropertyDescriptor> l = new ArrayList<PropertyDescriptor>();
		Field[] fields = getFields(klass);
		for(Field field : fields) {
			Property pro = field.getAnnotation(Property.class);
			if(pro != null) {
				String id = field.getName();
				String name = pro.name();
				if("".equals(name)) {
					name = upperCaseFirstChar(field.getName());
				}
				String category = pro.category();
				if("".equals(category)) {
					category = null;
				}
				Class<? extends PropertyDescriptor> type = pro.type();
				try {
					PropertyDescriptor descriptor = type.getConstructor(Object.class, String.class).newInstance(id, name);
					descriptor.setCategory(category);
					l.add(descriptor);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return l.toArray(new PropertyDescriptor[l.size()]);
	}
	
	protected static Field[] getFields(Class<?> klass) {
		List<Field> ret = new ArrayList<Field>();
		while(klass != null) {
			Field[] fields = klass.getDeclaredFields();
			for(Field field: fields) {
				ret.add(field);
			}
			klass = klass.getSuperclass();
		}
		return ret.toArray(new Field[ret.size()]);
	}
	
	private static String upperCaseFirstChar(String s) {
		if(!Character.isUpperCase(s.charAt(0))) {
			char[] cs = s.toCharArray();
			cs[0] = Character.toUpperCase(cs[0]);
			return new String(cs);
		}
		return s;
	}
	
}
