package pip.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
/**
 * 浮动的弹出式帮助框.
 */
@SuppressWarnings("serial")
public class JPanelPopup extends JPanel {
    // Border color.
    protected Color borderColor = null;
    // Hint string.
    protected String hint = "";
    // The desired position of the popup when shows next time.
    private int desiredLocationX, desiredLocationY;


    /**
     * Create a popup panel instance.
     */
    public JPanelPopup() {
        super();
        setBackground(CommonMetalTheme.HINT_BACK);
        setForeground(CommonMetalTheme.HINT_COLOR);
        setFont(CommonMetalTheme.HINT_FONT);
        borderColor = CommonMetalTheme.HINT_BORDER;
    }

    /**
     * Pack the panel to best size.
     */
    public void pack() {
        setSize(getPreferredSize());
    }

    /**
     * Get the parent into which the popup panel is added.
     *
     * @param comp The target component of the tooltip
     * @return The docking parent.
     */
    protected Container getDockingParent(Component comp) {
        for (Container p = comp.getParent(); p != null; p = p.getParent()) {
            if (p instanceof JRootPane) {
                if (p.getParent() instanceof JInternalFrame) {
                    continue;
                }
                return ((JRootPane)p).getLayeredPane();
            } else if (p instanceof Window) {
                return p;
            }
        }
        return comp.getParent();
    }

    /**
     * Show the popup panel.
     *
     * @param invoker The parent component of the tooltip
     */
    public void show(Component invoker) {
        Container parent = null;
        if (invoker != null) {
            parent = getDockingParent(invoker);
            Point p = convertScreenLocationToParent(parent, desiredLocationX, desiredLocationY);
            this.setLocation(p.x, p.y);
            if (parent instanceof JLayeredPane) {
                ((JLayeredPane)parent).add(this, JLayeredPane.POPUP_LAYER, 0);
            } else {
                parent.add(this);
            }
        }
    }

    /**
     * Hide the popup panel.
     */
    public void setVisible(boolean value) {
        if (value == false) {
            Container parent = getParent();
            Rectangle r = this.getBounds();
            if (parent != null) {
                parent.remove(this);
                parent.repaint(r.x, r.y, r.width, r.height);
            }
        } else {
            super.setVisible(true);
        }
    }

    /**
     * Convert a point in screen coordinate to a relative coordinate. This
     * method can adjust the location to ensure the popup panel don't exceed
     * the bounds of the parent.
     *
     * @param parent The parent of the relative coordinate
     * @param x The X value of the point
     * @param y The Y value of the point
     * @return The point value after converting
     */
    protected Point convertScreenLocationToParent(Container parent, int x, int y) {
        Point pp = parent.getLocationOnScreen();

        // Adjust the position

        int tx = x - pp.x;
        if (tx + getWidth() > parent.getWidth()) {
            tx = parent.getWidth() - getWidth();
        }
        int ty = y - pp.y;
        if (ty + getHeight() > parent.getHeight()) {
            ty = parent.getHeight() - getHeight();
        }
        return new Point(tx, ty);
    }

    /**
     * Set the location of the popup panel. If the panel was already shown, this
     * method set the location directly, or else this action is deferred to next
     * time the popup is shown.
     *
     * @param x The X value of the location, in screen coordinate
     * @param y The Y value of the location, in screen coordinate
     */
    public void setLocationOnScreen(int x,int y) {
        Container parent = getParent();
        if (parent != null) {
            Point p = convertScreenLocationToParent(parent, x, y);
            this.setLocation(p.x, p.y);
        } else {
            desiredLocationX = x;
            desiredLocationY = y;
        }
    }

    /**
     * Get current hint string.
     */
    public String getHint() {
        return hint;
    }

    /**
     * Set hint string.
     */
    public void setHint(String str) {
        hint = str;
    }

    /**
     * Paint the popup panel.
     */
    public void paintComponent(Graphics g) {
        Color temp = g.getColor();
        g.setColor(getBackground());
        Dimension size = getSize();
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(borderColor);
        g.drawRect(0, 0, size.width - 1, size.height - 1);
        g.setColor(getForeground());
        String[] lines = hint.split("\\r\\n|\\n");
        FontMetrics fm = this.getFontMetrics(getFont());
        int x = 3;
        int y = 2 + fm.getAscent();
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], x, y);
            y += fm.getHeight();
        }
        g.setColor(temp);
    }

    /**
     * Get the best size of the popup label.
     */
    public Dimension getPreferredSize() {
        String[] lines = hint.split("\\r\\n|\\n");
        FontMetrics fm = this.getFontMetrics(getFont());
        int wid = 0;
        int hei = 4 + lines.length * fm.getHeight();
        for (int i = 0; i < lines.length; i++) {
            int lw = fm.stringWidth(lines[i]);
            if (lw > wid) {
                wid = lw;
            }
        }
        wid += 6;
        return new Dimension(wid, hei);
    }
}
