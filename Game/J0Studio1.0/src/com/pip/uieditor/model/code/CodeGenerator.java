package com.pip.uieditor.model.code;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import com.pip.uieditor.model.AnchorPoint;
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
import com.pip.uieditor.model.Region;
import com.pip.uieditor.model.Screen;
import com.pip.uieditor.model.Slider;
import com.pip.uieditor.model.StringRegion;
import com.pip.uieditor.model.TabBar;
import com.pip.uieditor.model.TabButton;
import com.pip.uieditor.model.Table;
import com.pip.uieditor.model.TableColumn;
import com.pip.uieditor.model.TextArea;
import com.pip.uieditor.model.TextField;
import com.pip.uieditor.model.Widget;
import com.pip.uieditor.model.text.RichTextParser;
import com.pip.uieditor.util.ObjectUtil;

public class CodeGenerator {
	
	public CodeGenerator() {
		   
	}
	
	public String generate(Screen screen, int version, String prefix) {
		if(prefix == null)
			prefix = "";
		StringBuilder sb = new StringBuilder();
		generateDataSegment(screen, sb);
		if(version >= 4) {
			sb.append(String.format("void FUNCTION %sinitUI() {\n", prefix));
		} else {
			sb.append(String.format("FUNCTION %sinitUI() {\n", prefix));
		}
		sb.append("  Object region;\n");
		if(screen.hashChild(Slider.class)) {
			sb.append("  Object widget;\n");
		}
		for(int i = 0; i < screen.getChildCount(); i++) {
			generateWidget(screen.getChild(i), sb);
		}
		sb.append("}\n");
		
		if(version >= 4) {
			sb.append(String.format("void FUNCTION %sdestroyUI() {\n", prefix));
		} else {
			sb.append(String.format("FUNCTION destroyUI() {\n", prefix));
		}
		for(int i = 0; i < screen.getChildCount(); i++) {
			generateDestoryWidget(screen.getChild(i), sb);
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	protected void generateDestoryWidget(Widget widget, StringBuilder sb) {
		int canvas = widget.getOwnerCanvas();
		if(canvas == Widget.CANVAS_UI)
			sb.append(String.format("  UIManager_RemoveWidget(%s);\n",widget.getName()));
		if(canvas == Widget.CANVAS_SCREEN) 
			sb.append(String.format("  UIManager_RemoveWidgetWithCanvas(\"SCREEN\", %s);\n",widget.getName()));
	}
	
	protected void generateDataSegment(Screen screen, StringBuilder sb) {
		sb.append("DATA {\n");
		for(int i = 0; i < screen.getChildCount(); i++) {
			Widget widget = screen.getChild(i);
			if(widget instanceof Container) {
				generateContainerDataSegment((Container)widget, sb);
			} else {
				generateWidgetDataSegment(widget, sb);
			}
		}
		sb.append("}\n");
	}
	
	protected void generateContainerDataSegment(Container con, StringBuilder sb) {
		sb.append(String.format("  Object %s;\n", con.getName()));
		for (int i = 0; i < con.getChildCount(); i++) {
			Widget widget = con.getChild(i);
			if (widget instanceof Container) {
				generateContainerDataSegment((Container) widget, sb);
			} else {
				generateWidgetDataSegment(widget, sb);
			}
		}
	}
	
	protected void generateWidgetDataSegment(Widget widget, StringBuilder sb) {
		sb.append(String.format("  Object %s;\n", widget.getName()));
	}
	
	protected void generateWidget(Widget widget, StringBuilder sb) {
		if(widget instanceof Frame) {
			generateFrame((Frame)widget, sb);
		} else if( widget instanceof Container) {
			generateContainer((Container)widget, sb);
		} else if(widget instanceof Button) {
			generateButton((Button)widget, sb);
		} else if(widget instanceof Label) {
			generateLabel((Label)widget, sb);
		} else if(widget instanceof Table)  {
			generateTable((Table)widget, sb);
		} else if(widget instanceof Icon) {
			generateIcon((Icon)widget, sb);
		} else if(widget instanceof CheckBox) {
			generateCheckBox((CheckBox)widget, sb);
		} else if(widget instanceof Grid) {
			generateGrid((Grid)widget, sb);
		} else if(widget instanceof Dialog) {
			generateDialog((Dialog)widget, sb);
		} else if(widget instanceof TabBar) {
			generateTabBar((TabBar)widget, sb);
		} else if(widget instanceof TextArea) {
			generateTextArea((TextArea)widget, sb);
		} else if(widget instanceof TextField) {
			generateTextField((TextField)widget, sb);
		} else if(widget instanceof PageGrid) {
			generatePageGrid((PageGrid)widget, sb);
		} else if(widget instanceof Slider) {
			generateSlider((Slider)widget, sb);
		}
		generateScroll(widget, sb);
		generateDebugInfo(widget, sb);
		generateFlipFactor(widget, Widget.PROPERTY, sb);
	}
	
	protected void generateDebugInfo(Widget widget, StringBuilder sb) {
		sb.append(String.format("  Widget_SetDebugInfo(%s, \"%s\");\n", widget.getName(), widget.getName()));
	}
	
	protected void generateSlider(Slider sld, StringBuilder sb) {
		String variable = sld.getName();
		sb.append(String.format("  %s = Slider_Create();\n", variable));
		generateWidgetLayoutData(sld, sb);
		generateRegions(sld, Slider.PROTOTYPE, sb, variable);
		sb.append(String.format("  Slider_SetMinMax(%s, %d, %d);\n", variable, sld.getMin(), sld.getMax()));
		sb.append(String.format("  Slider_SetThumbSize(%s, %d, %d);\n", variable, sld.getThumbWidth(), sld.getThumbHeight()));
		sb.append(String.format("  widget = Slider_GetHead(%s);\n", variable));
		generateRegions(sld.getHead(), Label.PROTOTYPE, sb, "widget");
		sb.append(String.format("  widget = Slider_GetTail(%s);\n", variable));
		generateRegions(sld.getTail(), Label.PROTOTYPE, sb, "widget");
		sb.append(String.format("  widget = Slider_GetThumb(%s);\n", variable));
		generateRegions(sld.getThumb(), Button.PROTOTYPE, sb, "widget");
		addToParent(sld, sb);
	}
	
	protected void generateTextField(TextField tf, StringBuilder sb) {
		String variable = tf.getName();
		sb.append(String.format("  %s = TextField_Create();\n", variable));
		generateWidgetLayoutData(tf, sb);
		generateRegions(tf, TextField.PROTOTYPE, sb, variable);
		generateAblitiy(tf, TextField.PROTOTYPE, sb);
		if(tf.getMaxLen() != TextField.PROTOTYPE.getMaxLen()) {
			sb.append(String.format("  TextField_SetMaxLen(%s, %d);\n", variable, tf.getMaxLen()));
		}
		addToParent(tf, sb);
	}
	
	protected void generateAblitiy(Widget widget, Widget prototype, StringBuilder sb) {
		if(widget.isLongClickable() != prototype.isLongClickable()) {
			sb.append(String.format("  Widget_SetLongClickable(%s, %s);\n", widget.getName(), getBooleanString(widget.isLongClickable())));
		}
	}
	
	protected void generateFlipFactor(Widget widget, Widget property,
			StringBuilder sb) {
		if (widget.getHorizontalFlipFactor() != property
				.getHorizontalFlipFactor()) {
			sb.append(String.format(
					"  Widget_SetHorizontalFlipFactor(%s, %d);\n",
					widget.getName(),
					(int) (widget.getHorizontalFlipFactor() * 100)));
		}
		if(widget.getVerticalFlipFactor() != property.getVerticalFlipFactor()) {
			sb.append(String.format(
					"  Widget_SetVerticalFlipFactor(%s, %d);\n",
					widget.getName(),
					(int) (widget.getVerticalFlipFactor() * 100)));			
		}
	}
	
	protected void generateScroll(Widget widget, StringBuilder sb) {
		if(widget.isHorizontalScrollBarEnabled() || widget.isVerticalScrollBarEnabled()) {
			sb.append(String.format("  Widget_SetScrollBarWidth(%s, %d);\n", widget.getName(), widget.getScrollBarWidth()));
			sb.append(String.format("  Widget_SetScrollBarColor(%s, %s);\n", widget.getName(), toHexString(widget.getScrollBarColor().toInt())));
		}
	}
	
	protected void generatePageGrid(PageGrid grid, StringBuilder sb) {
		String variable = grid.getName();
		sb.append(String.format("  %s = PageGrid_Create();\n", variable));
		generateWidgetLayoutData(grid, sb);
		generateRegions(grid, PageGrid.PROTOTYPE, sb, variable);
		generateAblitiy(grid, PageGrid.PROTOTYPE, sb);
		sb.append(String.format("  PageGrid_SetPageStyle(%s, %s);\n", variable, getPageGridStyle(grid.getStyle())));
		addToParent(grid, sb);
	}
	
	protected String getPageGridStyle(int style) {
		if(style == 0)
			return "PAGE_GIRD_STYLE_VERTICAL";
		if(style == 1)
			return "PAGE_GRID_STYLE_HORIZONTAL";
		throw new IllegalArgumentException();
	}
	
	protected void generateGrid(Grid grid, StringBuilder sb) {
		String variable = grid.getName();
		sb.append(String.format("  %s = Grid_Create();\n", variable));
		generateWidgetLayoutData(grid, sb);
		generateRegions(grid, Grid.PROTOTYPE, sb, variable);
		generateAblitiy(grid, Grid.PROTOTYPE, sb);
		sb.append(String.format("  Grid_SetColumnCount(%s, %d);\n", variable, grid.getColumnCount()));
		sb.append(String.format("  Grid_SetCellSize(%s, %d, %d);\n", variable, grid.getCellHeight(), grid.getCellHeight()));
		addToParent(grid, sb);
	}
	
	protected void generateTable(Table table, StringBuilder sb) {
		String variable = table.getName();
		int columnCount = table.getSubWidgetCount();
		sb.append(String.format("  %s = Table_Create();\n", variable));
		sb.append(String.format("  Table_SetColumnCount(%s, %d);\n", variable, columnCount));
		generateWidgetLayoutData(table, sb);
		generateRegions(table, Table.PROTOTYPE, sb, variable);
		generateAblitiy(table, Table.PROTOTYPE, sb);
		sb.append(String.format("  Table_SetRowHeight(%s, %d);\n", variable, table.getRowHeight()));
		for (int i = 0; i < columnCount; i++) {
			TableColumn column = (TableColumn) table.getSubWidget(i);
			sb.append(String.format(
					"  Table_SetColumnInfo(%s, %d, %d, %s, %s, %d, %s);\n", variable,
					i, column.getPreferredWidth(), getBooleanString(column.isFlexible()), 
					getAnchorEnumString(column.getAnchor()), column.getXoffset(),
					getBooleanString(column.isCanPush()) ));
		}
		if (table.isKeepSelection()) {
			sb.append(String.format("  Table_SetKeepSelection(%s, TRUE);\n",variable));
		}
		addToParent(table, sb);
	}
	
	protected void generateTabBar(TabBar tabBar, StringBuilder sb) {
		String variable = tabBar.getName();
		sb.append(String.format("  %s = TabBar_Create(%d);\n", variable, tabBar.getStyle()));
		generateWidgetLayoutData(tabBar, sb);
		generateRegions(tabBar, TabBar.PROTOTYPE, sb, variable);
		sb.append(String.format("  TabBar_SetGap(%s, %d);\n", variable, tabBar.getGap()));
		for(int i = 0; i < tabBar.getChildCount(); i++) {
			TabButton tb = (TabButton)tabBar.getChild(i);
			String var = variable + "tb" + i;
			sb.append(String.format("  Object %s = CheckBox_Create();\n", var));
			generateRegions(tb, TabButton.PROTOTYPE, sb, var);
			sb.append(String.format("  Widget_SetBounds(%s, %d, %d, %d, %d);\n", var,  0, 0, tb.getSize().width, tb.getSize().height));
			sb.append(String.format("  TabBar_AddButton(%s, %s);\n", variable, var));
		}
		addToParent(tabBar, sb);
	}
	
	protected void generateTextArea(TextArea ta, StringBuilder sb) {
		String variable = ta.getName();
		sb.append(String.format("  %s = TextArea_Create();\n", ta.getName()));
		generateWidgetLayoutData(ta, sb);
		generateRegions(ta, TextArea.PROTOTYPE, sb, variable);
		generateAblitiy(ta, TextArea.PROTOTYPE, sb);
		TextArea prototype = ta.PROTOTYPE;
		if(!ObjectUtil.equals(ta.getTextColor(), prototype.getTextColor())) {
			sb.append(String.format("  TextArea_SetTextColor(%s, %s);\n", variable, toHexString(ta.getTextColor().toInt())));
		}
		if(!ObjectUtil.equals(ta.getShadowColor(), prototype.getShadowColor())) {
			sb.append(String.format("  TextArea_SetShadowColor(%s, %s);\n", variable, toHexString(ta.getShadowColor().toInt())));
		}
		if(!ObjectUtil.equals(ta.getLinkColor(), prototype.getLinkColor())) {
			sb.append(String.format("  TextArea_SetLinkColor(%s, %s);\n", variable, toHexString(ta.getLinkColor().toInt())));
		}
		if(ta.getLineGap() != prototype.getLineGap()) {
			sb.append(String.format("  TextArea_SetLineGap(%s, %d);\n", variable, ta.getLineGap()));
		}
		if(ta.isShadow() != prototype.isShadow()) {
			sb.append(String.format("  TextArea_SetShadow(%s, %s);\n", variable, getBooleanString(ta.isShadow())));
		}
		if(!ObjectUtil.equals(ta.getContent(), prototype.getContent())) {
			sb.append(String.format("  TextArea_SetText(%s, \"%s\");\n", variable, RichTextParser.escapeString(ta.getContent())));
		}
		if(!ObjectUtil.equals(ta.getFontName(), prototype.getFontName())) {
			sb.append(String.format("  TextArea_SetFont(%s, \"%s\");\n", variable, ta.getFontName()));
		}
		addToParent(ta, sb);
	}
	
	protected void generateButton(Button button, StringBuilder sb) {
		String variable = button.getName();
		sb.append(String.format("  %s = Button_Create();\n", button.getName()));
		generateWidgetLayoutData(button, sb);
		generateRegions(button, Button.PROTOTYPE, sb, variable);
		generateAblitiy(button, Button.PROTOTYPE, sb);
		addToParent(button, sb);
	}
	
	protected void generateCheckBox(CheckBox checkBox, StringBuilder sb) {
		String variable = checkBox.getName();
		sb.append(String.format("  %s = CheckBox_Create();\n", checkBox.getName()));
		generateWidgetLayoutData(checkBox, sb);
		generateRegions(checkBox, Button.PROTOTYPE, sb, variable);
		generateAblitiy(checkBox, Button.PROTOTYPE, sb);
		addToParent(checkBox, sb);
	}
	
	protected void generateContainer(Container container, StringBuilder sb) {
		sb.append(String.format("  %s = Container_Create();\n", container.getName()));
		generateFormLayout(container, sb);
		if(!isTopWidget(container))
			generateWidgetLayoutData(container, sb);
		else 
			generateWidgetBounds(container, sb);
		generateRegions(container, Container.PROTOTYPE, sb, container.getName());
		addToParent(container, sb);
		for(Widget widget : container.getChildren()) {
			generateWidget(widget, sb);
		}
		if(container.isScrollHorizontal() != Container.PROTOTYPE.isScrollHorizontal()) {
			sb.append(String.format("  Widget_SetHorizontalScrollBarEnabled(%s, %s);\n", container.getName(), getBooleanString(container.isScrollHorizontal())));
		}
		if(container.isScrollVertical() != Container.PROTOTYPE.isScrollVertical()) {
			sb.append(String.format("  Widget_SetVerticalScrollBarEnabled(%s, %s);\n", container.getName(), getBooleanString(container.isScrollVertical())));
		}
		if(container.isScrollPage() != Container.PROTOTYPE.isScrollPage()) {
			sb.append(String.format("  Widget_SetScrollPage(%s, %s);\n", container.getName(), getBooleanString(container.isScrollPage())));
		}
	}
	
	protected void generateDialog(Dialog dlg, StringBuilder sb) {
		sb.append(String.format("  %s = Dialog_Create();\n", dlg.getName()));
		generateFormLayout(dlg, sb);
		if(!isTopWidget(dlg))
			generateWidgetLayoutData(dlg, sb);
		else
			generateWidgetBounds(dlg, sb);
		generateRegions(dlg, Dialog.PROTOTYPE, sb, dlg.getName());
		addToParent(dlg, sb);
		for(Widget widget : dlg.getChildren()) {
			generateWidget(widget, sb);
		}
	}
	
	protected void generateFrame(Frame frame, StringBuilder sb) {
		sb.append(String.format("  %s = Frame_Create();\n", frame.getName()));
		generateFormLayout(frame, sb);
		if(!isTopWidget(frame))
			generateWidgetLayoutData(frame, sb);
		else
			generateWidgetBounds(frame, sb);
		generateRegions(frame, Frame.PROTOTYPE, sb, frame.getName());
		addToParent(frame, sb);
		for(Widget widget : frame.getChildren()) {
			generateWidget(widget, sb);
		}
	}
	
	protected void generateWidgetBounds(Widget widget, StringBuilder sb) {
		Rectangle rect = widget.getBounds();
		sb.append(String.format("  Widget_SetBounds(%s, %d, %d, %d, %d);\n", widget.getName(), rect.x, rect.y, rect.width, rect.height));
	}
	
	protected void generateFormLayout(Container con, StringBuilder sb) {
//		sb.append(String.format(
//				"  Container_SetLayout(%s, FormLayout_Create(%d, %d));\n",
//				con.getName(), con.getClientAreaWidth(),
//				con.getClientAreaHeight()));
	}
	
	protected void generateWidgetLayoutData(Widget widget, StringBuilder sb) {
//		sb.append(String
//				.format("  Widget_SetLayoutData(%s, FormData_Create(%d, %d, %d, %d, %s, %s));\n", widget.getName(),
//						widget.getLocation().x, widget.getLocation().y,
//						widget.getSize().width, widget.getSize().height,
//						getBooleanString(widget.isRelocation()),
//						getBooleanString(widget.isResize())));
		generateWidgetBounds(widget, sb);
	}

	protected void addToParent(Widget widget, StringBuilder sb) {
		if(isTopWidget(widget)) {
			if(widget.isInitAddToParent()) {
				if(widget.getOwnerCanvas() == Widget.CANVAS_UI) {
					sb.append(String.format("  UIManager_AddWidget(%s);\n", widget.getName()));
				} else {
					sb.append(String.format("  UIManager_AddWidgetWithCanvas(\"SCREEN\", %s);\n", widget.getName()));
				}
			}
		} else {
			if(widget.isInitAddToParent()) {
				sb.append(String.format("  Container_AddChild(%s, %s);\n", widget.getParent().getName(), widget.getName()));
			}
		}
		if(!widget.isInitVisible()) {
			sb.append(String.format("  Widget_SetVisible(%s, FALSE);\n", widget.getName()));
		}
	}
	
	protected void generateLabel(Label label ,StringBuilder sb) {
		String variable = label.getName();
		sb.append(String.format("  %s = Label_Create();\n", label.getName()));
		generateWidgetLayoutData(label, sb);
		generateRegions(label, Label.PROTOTYPE, sb, variable);
		generateAblitiy(label, Label.PROTOTYPE, sb);
		addToParent(label, sb);
	}
	
	protected void generateIcon(Icon icon, StringBuilder sb) {
		String variable = icon.getName();
		sb.append(String.format("  %s = Icon_Create();\n", icon.getName()));
		generateWidgetLayoutData(icon, sb);
		generateRegions(icon, Icon.PROTOTYPE, sb, variable);
		generateAblitiy(icon, Icon.PROTOTYPE, sb);
		addToParent(icon, sb);
		
	}
	
	protected void generateRegions(Widget widget, Widget prototype, StringBuilder sb, String variable) {
		for(int i = 0; i < widget.getRegionCount(); i++) {
			Region region = widget.getRegion(i);
			Region prototypeRegion = prototype.getRegion(region.getId());
			if(prototypeRegion == null) {
				prototypeRegion = getPrototypeRegion(region);
			}
			generateRegion(region, prototypeRegion, sb, variable);
		}
	}
	
	protected Region getPrototypeRegion(Region region) {
		if(region instanceof StringRegion)
			return StringRegion.PROTOTYPE;
		if(region instanceof ImageRegion)
			return ImageRegion.PROTOTYPE;
		if(region instanceof ColorRegion)
			return ColorRegion.PROTOTYPE;
		if(region instanceof AnimateRegion)
			return AnimateRegion.PROTOTYPE;
		if(region instanceof ModelRegion)
			return ModelRegion.PROTOTYPE;
		if(region instanceof CustomeRegion)
			return CustomeRegion.PROTOTYPE;
		if(region instanceof ExtendedRegion)
			return ExtendedRegion.PROTOTYPE;
		if(region instanceof GameSpriteRegion)
			return GameSpriteRegion.PROPERTY;
		throw new IllegalArgumentException();
	}
	
	protected void generateRegion(Region region, Region prototype, StringBuilder sb, String variable) {
		if(prototype != null && (region.isRequire() != prototype.isRequire()))
			throw new RuntimeException();
		if(!region.generateEquals(prototype)) {
			if (region.isRequire()) {
				sb.append(String.format(
						"  region = Widget_GetRegion(%s, \"%s\");\n", variable,
						region.getId()));
			} else {
				if (region instanceof ExtendedRegion) {
					sb.append(String.format("  region = %sRegion_Create(\"%s\");\n",
							((ExtendedRegion)region).getPrefix(), region.getId()));
				} else {
					sb.append(String.format("  region = %s_Create(\"%s\");\n",
							region.getClass().getSimpleName(), region.getId()));
				}
				sb.append(String.format("  Widget_AddRegion(%s, %s, region);\n", variable, getLayerEnumString(region.getLayer())));
			}
			if(region instanceof ImageRegion) {
				generateImageRegion((ImageRegion)region, (ImageRegion)prototype, sb, variable);
			} else if(region instanceof ColorRegion) {
				generateColorRegion((ColorRegion)region, (ColorRegion)prototype, sb, variable);
			} else if(region instanceof StringRegion) {
				generateStringRegion((StringRegion)region, (StringRegion)prototype, sb, variable);
			} else if(region instanceof AnimateRegion) {
				generateAnimateRegion((AnimateRegion)region, (AnimateRegion)prototype, sb, variable);
			} else if(region instanceof ModelRegion) {
				generateModelRegion((ModelRegion)region, (ModelRegion)prototype, sb, variable);
			} else if(region instanceof CustomeRegion) {
				generateCustomeRegion((CustomeRegion)region, (CustomeRegion)prototype, sb, variable);
			} else if(region instanceof GameSpriteRegion) {
				generateGameSpriteRegion((GameSpriteRegion)region, (GameSpriteRegion)prototype, sb, variable);
			}
			if(region.isVisible() != prototype.isVisible()) {
				sb.append(String.format("  Region_SetVisible(region, %s);\n", getBooleanString(region.isVisible())));
			}
			if(region.getVisibleMask() != prototype.getVisibleMask() ) {
				sb.append(String.format("  Region_SetVisibleState(region, %d, %d);\n", region.getVisibleMask(), region.getVisbleFlag()));
			}
			if (!region.isRequire()) {
				List<AnchorPoint> l = region.getAnchorPoints();
				for (AnchorPoint ap : l) {
					sb.append(String.format(
							"  Region_AddAnchor(region, %s, %s, %d, %d);\n",
							getAnchorEnumString(ap.getAnchor()), getAnchorEnumString(ap.getRelativeAnchor()),
							ap.getOffsetX(), ap.getOffsetY()));
				}
			} else {
				if (!Region.anchorPointsEquals(region.getAnchorPoints(),
						prototype.getAnchorPoints())) {
					List<AnchorPoint> l = region.getAnchorPoints();
					for (int i = 0; i < l.size(); i++) {
						AnchorPoint ap = l.get(i);
						if (i == 0) {
							sb.append(String
									.format("  Region_SetAnchor(region, %s, %s, %d, %d);\n",
											getAnchorEnumString(ap.getAnchor()),
											getAnchorEnumString(ap.getRelativeAnchor()),
											ap.getOffsetX(), ap.getOffsetY()));
						} else {
							sb.append(String
									.format("  Region_AddAnchor(region, %s, %s, %d, %d);\n",
											getAnchorEnumString(ap.getAnchor()),
											getAnchorEnumString(ap.getRelativeAnchor()),
											ap.getOffsetX(), ap.getOffsetY()));
						}
					}
				}
			}
		}
	}
	
	protected void generateColorRegion(ColorRegion region, ColorRegion prototype, StringBuilder sb, String variable) {
		//if(!ObjectUtil.equals(region.getColor(), prototype.getColor())) {
			sb.append(String.format("  ColorRegion_SetColor(region, %s);\n", toHexString(region.getColor().toInt())));
		//}
	}
	
	protected void generateStringRegion(StringRegion region, StringRegion prototype, StringBuilder sb, String variable) {
		if(!ObjectUtil.equals(region.getColor(), prototype.getColor())) {
			sb.append(String.format("  StringRegion_SetColor(region, %s);\n", toHexString(region.getColor().toInt())));
		}
		if(!ObjectUtil.equals(region.getShadowColor(), prototype.getShadowColor())) {
			sb.append(String.format("  StringRegion_SetShadowColor(region, %s);\n", toHexString(region.getShadowColor().toInt())));
		}
		if(region.isLineWrap() != prototype.isLineWrap()) {
			sb.append(String.format("  StringRegion_SetLineWrap(region, %s);\n", getBooleanString(region.isLineWrap())));
		}
		if(region.isShadow() != prototype.isShadow()) {
			sb.append(String.format("  StringRegion_SetShadow(region, %s);\n", getBooleanString(region.isShadow())));
		}
		if(!ObjectUtil.equals(region.getLinkColor(), prototype.getLinkColor())) {
			sb.append(String.format("  StringRegion_SetLinkColor(region, %s);\n", toHexString(region.getLinkColor().toInt())));
		}
		if(region.getLineGap() != prototype.getLineGap()) {
			sb.append(String.format("  StringRegion_SetLineGap(region, %d);\n", region.getLineGap()));
		}
		if(!ObjectUtil.equals(region.getText(), prototype.getText())) {
			sb.append(String.format("  StringRegion_SetText(region, \"%s\");\n", RichTextParser.escapeString(region.getText())));
		}
		if(!ObjectUtil.equals(region.getFontName(), prototype.getFontName())) {
			sb.append(String.format("  StringRegion_SetFont(region, \"%s\");\n", region.getFontName()));
		}

	}
	
	protected void generateImageRegion(ImageRegion region, ImageRegion prototype, StringBuilder sb, String variable) {
		if (!ObjectUtil.equals(region.getImageData(), prototype.getImageData())) {
			sb.append(String.format(
					"  ImageRegion_SetImageFile(region, \"%s\", %d, %d);\n", region
							.getImageData().getFile(), region.getImageData()
							.getFrame(), region.getTrans()));
		}
		if(region.getMode() != prototype.getMode()) {
			sb.append(String.format("  ImageRegion_SetMode(region, %s);\n", getDrawModeString(region.getMode())));
		}
	}
	
	protected void generateAnimateRegion(AnimateRegion region, AnimateRegion prototype, StringBuilder sb, String variable) {
		if (!ObjectUtil.equals(region.getAnimateData(), prototype.getAnimateData())) {
			sb.append(String.format(
					"  AnimateRegion_SetAnimateFile(region, \"%s\", %d);\n", region
							.getAnimateData().getFile(), region.getAnimateData().getIndex()));
		}
		if(region.isLoop() != prototype.isLoop()) {
			sb.append(String.format("  AnimateRegion_SetLoop(region, %s);\n", getBooleanString(region.isLoop())));
		}
		if(!region.getHookPoint().equals(prototype.getHookPoint())) {
			sb.append(String.format("  AnimateRegion_SetHookPoint(region, %d, %d);\n", region.getHookPoint().x, region.getHookPoint().y));
		}
		if(region.getHookAnchor() != prototype.getHookAnchor()) {
			sb.append(String.format("  AnimateRegion_SetHookAnchor(region, %s);\n", getAnchorEnumString(region.getHookAnchor())));
		}
		if(region.getScale() != 100) {
			sb.append(String.format("  AnimateRegion_SetScale(region, %d, %d);\n", region.getScale(), region.getScale()));
		}
	}
	
	protected void generateModelRegion(ModelRegion region, ModelRegion prototype, StringBuilder sb, String variable) {
		if(!region.getHookPoint().equals(prototype.getHookPoint())) {
			sb.append(String.format("  ModelRegion_SetHookPoint(region, %d, %d);\n", region.getHookPoint().x, region.getHookPoint().y));
		}
		if(region.getHookAnchor() != prototype.getHookAnchor()) {
			sb.append(String.format("  ModelRegion_SetHookAnchor(region, %s);\n", getAnchorEnumString(region.getHookAnchor())));
		}
	}
	
	protected void generateGameSpriteRegion(GameSpriteRegion region, GameSpriteRegion prototype, StringBuilder sb, String variable) {
//		if(region.getAlignment() != prototype.getAlignment()) {
//			sb.append(String.format("  GameSpriteRegion_SetAlignment(region, %s);\n", getAnchorEnumString(region.getAlignment())));
//		}
		if(region.getScale() != prototype.getScale()) {
			sb.append(String.format("  GameSpriteRegion_SetScale(region, %d);\n",(int)(region.getScale() * 100)));
		}
	}
	
	protected void generateCustomeRegion(CustomeRegion region, CustomeRegion prototype, StringBuilder sb, String varaible) {
		
	}
	
	protected String getBooleanString(boolean value) {
		return value ? "TRUE" : "FALSE";
	}
	
	protected boolean isTopWidget(Widget widget) {
		Widget parent = widget.getParent();
		return parent instanceof Screen;
	}

	protected static final String[] LAYER_ENUM = {"LAYER_BACKGROUND", "LAYER_BORDER", "LAYER_ARTWORK", "LAYER_OVERLAY"};
	
	protected String getLayerEnumString(int layer) {
		return LAYER_ENUM[layer];
	}
	
	protected String toHexString(int value) {
//		return String.valueOf(value);
		return "0x" + Integer.toHexString(value);
	}
	
	protected static final String[] ANCHOR_ENUM = {
		"ANCHOR_CENTER",
		"ANCHOR_TOPLEFT",
		"ANCHOR_TOP",
		"ANCHOR_TOPRIGHT",
		"ANCHOR_RIGHT",
		"ANCHOR_BOTTOMRIGHT",
		"ANCHOR_BOTTOM",
		"ANCHOR_BOTTOMLEFT",
		"ANCHOR_LEFT",
	};
	
	protected String getAnchorEnumString(int anchor) {
		return ANCHOR_ENUM[anchor];
	}
	
	protected static final String[] DRAW_MODE = {
		"DRAW_MODE_NONE",
		"DRAW_MODE_FILL",
		"DRAW_MODE_SCALE",
	};
	
	protected String getDrawModeString(int mode) {
		return DRAW_MODE[mode];
	}
}
