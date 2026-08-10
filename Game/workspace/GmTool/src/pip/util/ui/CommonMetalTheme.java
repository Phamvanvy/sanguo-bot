package pip.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

public class CommonMetalTheme extends DefaultMetalTheme {
    public static final ColorUIResource TH_BACK = new ColorUIResource(0, 74, 147);
    public static final ColorUIResource TITLE_BACK = new ColorUIResource(204, 204, 255);
    public static final ColorUIResource CONFIG_BACK = new ColorUIResource(255, 255, 255);
    public static final FontUIResource CONFIGTITLE_FONT = new FontUIResource("Dialog", Font.BOLD, 20);
    public static final FontUIResource TITLE2_FONT = new FontUIResource("Dialog", Font.BOLD, 16);
    public static final ColorUIResource CONFIGINPUT_BACK = new ColorUIResource(255, 255, 255);
    public static final ColorUIResource DEFAULT_BACK = new ColorUIResource(255, 255, 255);
    public static final ColorUIResource HINT_BACK = new ColorUIResource(204, 204, 255);
    public static final ColorUIResource HINT_COLOR = new ColorUIResource(0, 0, 0);
    public static final FontUIResource HINT_FONT = new FontUIResource("Dialog", Font.PLAIN, 12);
    public static final ColorUIResource HINT_BORDER = new ColorUIResource(102, 102, 153);
    public static final FontUIResource METER_TITLE_FONT = new FontUIResource("Dialog", Font.PLAIN, 15);
    public static final ColorUIResource METER_TITLE_BACK = new ColorUIResource(0, 74, 147);//new ColorUIResource(26, 53, 120);
    public static final ColorUIResource METER_TITLE_COLOR = new ColorUIResource(Color.white);
    public static final int SPLITTER_SIZE = 8;
    public static final int DIALOG_MARGIN = 6;
    public static final int BUTTON_GAP = 6;
    private FontUIResource controlFont;
    private FontUIResource systemFont;
    private FontUIResource userFont;
    private FontUIResource smallFont;

    /**
     * The table header border class in SGIL. Default implementation in Swing
     * use control colors. This class uses different colors according to color
     * of current table header.
     */
    @SuppressWarnings("serial")
    public static class SGILTableHeaderBorder extends AbstractBorder
        implements UIResource {
        protected Insets editorBorderInsets = new Insets(2, 2, 2, 0);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);

            g.setColor(c.getBackground().darker().darker());
            g.drawLine(w - 1, 0, w - 1, h - 1);
            g.drawLine(1, h - 1, w - 1, h - 1);
            g.setColor(c.getBackground().brighter().brighter());
            g.drawLine(0, 0, w - 2, 0);
            g.drawLine(0, 0, 0, h - 2);

            g.translate(-x, -y);
        }

        public Insets getBorderInsets(Component c) {
            return editorBorderInsets;
        }
    }

    /**
     * The tool bar border class in SGIL.
     */
    @SuppressWarnings("serial")
    public static class SGILToolBarBorder extends AbstractBorder
        implements UIResource, SwingConstants {
        protected Insets borderInsets = new Insets(2, 13, 2, 2);

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate( x, y );
            if (((JToolBar)c).getOrientation() == HORIZONTAL) {
                g.setColor(c.getBackground().brighter().brighter());
                g.drawLine(4, 2, 4, c.getSize().height - 4);
                g.drawLine(7, 2, 7, c.getSize().height - 4);
                g.setColor(c.getBackground().darker().darker());
                g.drawLine(5, 2, 5, c.getSize().height - 4);
                g.drawLine(8, 2, 8, c.getSize().height - 4);
            } else {
                g.setColor(c.getBackground().brighter().brighter());
                g.drawLine(2, 4, c.getSize().width - 4, 4);
                g.drawLine(2, 7, c.getSize().width - 4, 7);
                g.setColor(c.getBackground().darker().darker());
                g.drawLine(2, 5, c.getSize().width - 4, 5);
                g.drawLine(2, 8, c.getSize().width - 4, 8);
            }
            g.translate( -x, -y );
        }

        public Insets getBorderInsets(Component c) {
            Insets margin = ((JToolBar)c).getMargin();

            if (margin == null) {
                return borderInsets;
            } else {
                return new Insets(2 + margin.top, 13 + margin.left,
                                  2 + margin.bottom, 2 + margin.right);
            }
        }
    }


    /**
     * Constructor.
     */
    public CommonMetalTheme() {
        controlFont = new FontUIResource("Dialog", Font.PLAIN, 12);
        systemFont = new FontUIResource("Dialog", Font.PLAIN, 12);
        userFont = new FontUIResource("Dialog", Font.PLAIN, 12);
        smallFont = new FontUIResource("Dialog", Font.PLAIN, 10);
    }

    /**
     * Install a UI class as Swing default. To install a UI class, the following
     * three steps should be performed: 1. Register the class name, key is the
     * UI entry; 2. Register the Class object, key is the class name; 3. Register
     * the Method object to create a new UI, key is the Class object.
     * @param table The property table.
     * @param key The key of UI entry.
     * @param className The full qualified class name of the UI class.
     */
    public static void installUIClass(UIDefaults table, String key,
                                      String className) {
        try {
            Class cls = Class.forName(className);
            Method mtd = cls.getMethod("createUI", new Class[] { JComponent.class });

            table.put(key, className);
            table.put(className, cls);
            table.put(cls, mtd);
        } catch (Exception e) {
        }
    }

    /**
     * Get color of system text. We use black in SGIL Suite.
     */
    public ColorUIResource getSystemTextColor() {
        return getBlack();
    }

    /**
     * Add custom entries in property table.
     */
    public void addCustomEntriesToTable(UIDefaults table) {
        // Register UI for JTree.
        installUIClass(table, "TreeUI", "com.pip.maped.uiutils.WindowsTreeUI");

        // Register border and color of TableHeader.

        table.put("TableHeader.cellBorder", new SGILTableHeaderBorder());
        table.put("TableHeader.background", TH_BACK);
        table.put("TableHeader.foreground", new ColorUIResource(255, 255, 255));

        // Register border of JToolBar.
        //table.put("ToolBar.border", new SGILToolBarBorder());

        // Register UI for JToolBar.
        installUIClass(table, "ToolBarUI", "com.pip.maped.uiutils.MetalToolBarUIEx");

        // Register UI for buttons in JToolBar.
        //installUIClass(table, "ButtonUI", "com.everlasting.sgil.util.FloatButtonUI");

        // Register UI for separators in JToolBar.
        installUIClass(table, "ToolBarSeparatorUI", "com.pip.maped.uiutils.EtchedToolBarSeparatorUI");
        table.put("ToolBar.separatorSize", new Dimension(20, 20));
        table.put("ScrollBar.width", new Integer(16));

        // Force using chinese in JOptionPane.
        table.put("OptionPane.okButtonText", UiRES.okButtonText);
        table.put("OptionPane.yesButtonText", UiRES.yesButtonText);
        table.put("OptionPane.noButtonText", UiRES.noButtonText);
        table.put("OptionPane.cancelButtonText", UiRES.cancelButtonText);
        table.put("FileChooser.newFolderErrorText", UiRES.newFolderErrorText);
        table.put("FileChooser.fileDescriptionText", UiRES.fileDescriptionText);
        table.put("FileChooser.directoryDescriptionText", UiRES.directoryDescriptionText);
        table.put("FileChooser.saveButtonText", UiRES.saveButtonText);
        table.put("FileChooser.openButtonText", UiRES.openButtonText);
        table.put("FileChooser.cancelButtonText2", UiRES.cancelButtonText2);
        table.put("FileChooser.helpButtonText", UiRES.helpButtonText);
        table.put("FileChooser.saveButtonToolTipText", UiRES.saveButtonToolTipText);
        table.put("FileChooser.openButtonToolTipText", UiRES.openButtonToolTipText);
        table.put("FileChooser.cancelButtonToolTipText", UiRES.cancelButtonToolTipText);
        table.put("FileChooser.updateButtonToolTipText", UiRES.updateButtonToolTipText);
        table.put("FileChooser.helpButtonToolTipText", UiRES.helpButtonToolTipText);
        table.put("FileChooser.acceptAllFileFilterText", UiRES.acceptAllFileFilterText);
        table.put("FileChooser.lookInLabelText", UiRES.lookInLabelText);
        table.put("FileChooser.fileNameLabelText", UiRES.fileNameLabelText);
        table.put("FileChooser.filesOfTypeLabelText", UiRES.filesOfTypeLabelText);
        table.put("FileChooser.upFolderToolTipText", UiRES.upFolderToolTipText);
        table.put("FileChooser.upFolderAccessibleName", UiRES.upFolderAccessibleName);
        table.put("FileChooser.homeFolderToolTipText", UiRES.homeFolderToolTipText);
        table.put("FileChooser.homeFolderAccessibleName", UiRES.homeFolderAccessibleName);
        table.put("FileChooser.newFolderToolTipText", UiRES.newFolderToolTipText);
        table.put("FileChooser.listViewButtonToolTipText", UiRES.listViewButtonToolTipText);
        table.put("FileChooser.listViewButtonAccessibleName", UiRES.listViewButtonAccessibleName);
        table.put("FileChooser.detailsViewButtonToolTipText", UiRES.detailsViewButtonToolTipText);
        table.put("FileChooser.detailsViewButtonAccessibleName", UiRES.detailsViewButtonAccessibleName);
    }

    /**
     * Override the method in super class.
     */
    public FontUIResource getSubTextFont() {
        return smallFont;
    }

    /**
     * Override the method in super class.
     */
    public FontUIResource getSystemTextFont() {
        return systemFont;
    }

    /**
     * Override the method in super class.
     */
    public FontUIResource getUserTextFont() {
        return userFont;
    }

    /**
     * Override the method in super class.
     */
    public FontUIResource getWindowTitleFont() {
        return controlFont;
    }

    /**
     * Override the method in super class.
     */
    public FontUIResource getControlTextFont() {
        return controlFont;
    }

    /**
     * Override the method in super class.
     */
    public FontUIResource getMenuTextFont() {
        return controlFont;
    }
}
