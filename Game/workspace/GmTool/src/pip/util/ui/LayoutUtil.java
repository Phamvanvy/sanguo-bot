package pip.util.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
/**
 * 仅仅是为了节省typing做的小工具
 *
 */
public class LayoutUtil {
	public double weightx = 1.0D;
	public double weighty = 1.0D;
	public int anchor = 10;
	public int fill = 1;
	 
    public GridBagConstraints getConstrains(int x, int y, int w, int h) {
        return new GridBagConstraints(x, y, w, h, weightx, weighty, anchor,  fill, new Insets(1, 1, 1, 1), 0, 0);
    }

    public GridBagConstraints getConstrains(int x, int y, int w, int h, double wx, double wy) {
        return new GridBagConstraints(x, y, w, h, wx, wy, anchor,  fill, new Insets(1, 1, 1, 1), 0, 0);
    }
    public JPanel getRightAlignText(String text) {
    	JPanel jp = new JPanel(new BorderLayout());
    	jp.add(BorderLayout.EAST, new JLabel(text));
    	return jp;
    }
    public JPanel getRightAlignComponent(Component text) {
    	JPanel jp = new JPanel(new BorderLayout());
    	jp.add(BorderLayout.EAST, text);
    	return jp;
    }
    class ConstrainsScope {
    	int y;
    	int x;
    	int w = 1;
    	int h = 1;
    	public ConstrainsScope(int x, int y) {
    		this.y = y;
    		this.x = x;
    	}
    	public void expandTo(int x, int y) {
    		if (this.x > x) {
    			int k = this.x - x;
    			this.x = x;
    			w += k;
    		} else if (this.x < x) {
    			int k = x - this.x + 1;
    			if (k > w) {
    				w = k;
    			}
    		}
    		if (this.y > y) {
    			int k = this.y - y;
    			this.y = y;
    			h += k;
    		} else if (this.y < y) {
    			int k = y - this.y + 1;
    			if (k > h) {
    				h = k;
    			}
    		}
    	}
		public GridBagConstraints getConttains() {
			return getConstrains(x, y, w, h);
		}
    }
    public void layoutGridBagComponents(Container container, Component [][]components) {
    	HashMap<Component,ConstrainsScope> data = new HashMap<Component,ConstrainsScope>(); 
    	for (int y = 0; y < components.length; y++) {
    		if (components[y] != null) {
    			for (int x = 0; x < components[y].length; x++) {
    				Component com = components[y][x];
    				if (com != null) {
    					ConstrainsScope scope = data.get(com);
    					if (scope == null) {
    						scope = new ConstrainsScope(x, y);
    						data.put(com, scope);
    					} else {
    						scope.expandTo(x, y);
    					}
    				}
    			}
    		}
    	}
    	container.setLayout(new GridBagLayout());
    	for (Component com : data.keySet()) {
    		container.add(com, data.get(com).getConttains());
    	}
    }
    public static void setWindowCenterlize(Window win) {
    	Toolkit kit = Toolkit.getDefaultToolkit();   
        Dimension screenSize = kit.getScreenSize();  
        int w = win.getWidth();
        int h = win.getHeight();
        win.setLocation((screenSize.width - w)>>1, (screenSize.height - h)>>1);
    }
    public static void setWindowCentrallize(Window win, Window superWin) {
//    	System.out.println(win + "(" + superWin.getX() + "+((" + superWin.getWidth() + " - " + win.getWidth() + ")>>1), (" + superWin.getY() + 
//    			" + ((" + superWin.getHeight() + "-" + win.getHeight() + ")>>1))");
    	win.setLocation(superWin.getX() + ((superWin.getWidth() - win.getWidth())>>1), superWin.getY() + ((superWin.getHeight() - win.getHeight()) >> 1));
    }

}
