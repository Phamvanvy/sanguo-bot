package com.pip.uieditor.palette;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;

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
import com.pip.uieditor.model.Slider;
import com.pip.uieditor.model.StringRegion;
import com.pip.uieditor.model.TabBar;
import com.pip.uieditor.model.Table;
import com.pip.uieditor.model.TextArea;
import com.pip.uieditor.model.TextField;
import com.pip.uieditor.model.WidgetCreationFactory;
import com.pip.uieditor.tool.CreateRegionToolEntry;
import com.pip.uieditor.tool.TabButtonToolEntry;
import com.pip.uieditor.tool.TableColumnToolEntry;
import com.pip.uieditor.tool.WidgetSelectionToolEntry;

public class PaletteFactory {
	
	
	public static PaletteRoot createPalette() {
		PaletteRoot palette = new PaletteRoot();
		palette.add(createToolsGroup(palette));
		palette.add(createComponentsDrawer());
		return palette;
	}
	
	private static PaletteContainer createToolsGroup(PaletteRoot palette) {
		PaletteGroup toolGroup = new PaletteGroup("工具");

		ToolEntry tool = new SelectionToolEntry();
		toolGroup.add(tool);
		palette.setDefaultEntry(tool);
		
		toolGroup.add(new WidgetSelectionToolEntry());
		toolGroup.add(new MarqueeToolEntry());
		return toolGroup;
	}

	
	public static PaletteContainer createComponentsDrawer() {
		PaletteDrawer drawer = new PaletteDrawer("组件");
		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				"Container", "Container", new WidgetCreationFactory(
						Container.class), null, null);
		drawer.add(entry);
		entry = new CombinedTemplateCreationEntry(
				"Button", "Button", new WidgetCreationFactory(
						Button.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"CheckBox", "CheckBox", new WidgetCreationFactory(
						CheckBox.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"Label", "Label", new WidgetCreationFactory(
						Label.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"Icon", "Icon", new WidgetCreationFactory(
						Icon.class), null, null);
		drawer.add(entry);

		
		entry = new CombinedTemplateCreationEntry(
				"Frame", "Frame", new WidgetCreationFactory(
						Frame.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"Dialog", "Dialog", new WidgetCreationFactory(
						Dialog.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"Table", "Table", new WidgetCreationFactory(
						Table.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"TabBar", "TabBar", new WidgetCreationFactory(
						TabBar.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"TextArea", "TextArea", new WidgetCreationFactory(
						TextArea.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry(
				"TextField", "TextField", new WidgetCreationFactory(
						TextField.class), null, null);
		drawer.add(entry);
		
		
		entry = new CombinedTemplateCreationEntry("Grid", "Grid",
				new WidgetCreationFactory(Grid.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry("PageGrid", "PageGrid",
				new WidgetCreationFactory(PageGrid.class), null, null);
		drawer.add(entry);
		
		entry = new CombinedTemplateCreationEntry("Slider", "Slider",
				new WidgetCreationFactory(Slider.class), null, null);
		drawer.add(entry);
		
		drawer.add(new CreateRegionToolEntry(ColorRegion.class,"ColorRegion", "ColorRegion", null, null));
		drawer.add(new CreateRegionToolEntry(StringRegion.class,"StringRegion", "StringRegion", null, null));
		drawer.add(new CreateRegionToolEntry(ImageRegion.class,"ImageRegion", "ImageRegion", null, null));
		drawer.add(new CreateRegionToolEntry(AnimateRegion.class,"AnimateRegion", "AnimateRegion", null, null));
		drawer.add(new CreateRegionToolEntry(ModelRegion.class, "ModelRegion", "ModelRegion", null, null));
		drawer.add(new CreateRegionToolEntry(GameSpriteRegion.class, "GameSpriteRegion", "GameSpriteRegion", null, null));
		drawer.add(new CreateRegionToolEntry(CustomeRegion.class, "CustomeRegion", "CustomeRegion", null, null));
		drawer.add(new CreateRegionToolEntry(ExtendedRegion.class, "ExtendedRegion", "ExtendedRegion", null, null));
		
		drawer.add(new TableColumnToolEntry("Column", "Column", null, null));
		
		drawer.add(new TabButtonToolEntry("TabButton", "TabButton", null, null));
		
		return drawer;
	}
}
