package com.pip.uieditor.model.persist;

import java.util.HashMap;

import com.pip.uieditor.model.AnimateRegion;
import com.pip.uieditor.model.Button;
import com.pip.uieditor.model.CheckBox;
import com.pip.uieditor.model.ColorRegion;
import com.pip.uieditor.model.Container;
import com.pip.uieditor.model.CustomeRegion;
import com.pip.uieditor.model.Dialog;
import com.pip.uieditor.model.ExtendedRegion;
import com.pip.uieditor.model.Frame;
import com.pip.uieditor.model.GameSpriteRegion;
import com.pip.uieditor.model.Grid;
import com.pip.uieditor.model.Icon;
import com.pip.uieditor.model.ImageRegion;
import com.pip.uieditor.model.Label;
import com.pip.uieditor.model.ModelRegion;
import com.pip.uieditor.model.PageGrid;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.Slider;
import com.pip.uieditor.model.StringRegion;
import com.pip.uieditor.model.TabBar;
import com.pip.uieditor.model.TabButton;
import com.pip.uieditor.model.Table;
import com.pip.uieditor.model.TableColumn;
import com.pip.uieditor.model.TextArea;
import com.pip.uieditor.model.TextField;
import com.pip.uieditor.model.UIObject;

public class PersistMapping {
	
	protected HashMap<String, Class<? extends UIObject>> name2klass = new HashMap<String, Class<? extends UIObject>>();
	protected HashMap<Class<? extends UIObject>,String> klass2name = new HashMap<Class<? extends UIObject>, String>();
	
	private static PersistMapping instance;
	
	public static PersistMapping getDefault() {
		if(instance != null)
			return instance;
		instance = new PersistMapping();
		instance.register("Screen", Screen.class);
		instance.register("Container", Container.class);
		instance.register("Label", Label.class);
		instance.register("Icon", Icon.class);
		instance.register("Button", Button.class);
		instance.register("CheckBox", CheckBox.class);
		instance.register("ImageRegion", ImageRegion.class);
		instance.register("StringRegion", StringRegion.class);
		instance.register("ColorRegion", ColorRegion.class);
		instance.register("CustomeRegion", CustomeRegion.class);
		instance.register("ExtendedRegion", ExtendedRegion.class);
		instance.register("AnimateRegion", AnimateRegion.class);
		instance.register("ModelRegion", ModelRegion.class);
		instance.register("GameSpriteRegion", GameSpriteRegion.class);
		instance.register("Frame", Frame.class);
		instance.register("Table", Table.class);
		instance.register("TableColumn", TableColumn.class);
		instance.register("Grid", Grid.class);
		instance.register("Dialog", Dialog.class);
		instance.register("TabBar", TabBar.class);
		instance.register("TabButton", TabButton.class);
		instance.register("TextArea", TextArea.class);
		instance.register("TextField", TextField.class);
		instance.register("PageGrid", PageGrid.class);
		instance.register("Slider", Slider.class);
		return instance;
	}
	
	public void register(String name, Class<? extends UIObject> klass) {
		name2klass.put(name, klass);
		klass2name.put(klass, name);
	}
	
	public String getMappingName(Class<? extends UIObject> klass) {
		return klass2name.get(klass);
	}
	
	public Class<? extends UIObject> getMappingKlass(String name) {
		return name2klass.get(name);
	}
}
