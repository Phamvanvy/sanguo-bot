package pip.gm.fw;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class DoubleClickZoomerAdapter implements MouseListener {
	private Container parentPanel;
	private Container rootPanel;
	private Component rootComponent;
	private Component zoomComponent;
	/**  */
	public DoubleClickZoomerAdapter(Container borderLayoutedPp, Component zoomableComponent) {
		zoomComponent = zoomableComponent;
		parentPanel = zoomableComponent.getParent();
		rootPanel = borderLayoutedPp;
		if (borderLayoutedPp != null && borderLayoutedPp.getComponentCount() == 1) {
			rootComponent = (Component)borderLayoutedPp.getComponent(0);
		}
	}
	public void zoomSwitch() {
		 Container p = zoomComponent.getParent();
		 if (p == rootPanel) {
			 rootPanel.remove(zoomComponent);
			 rootPanel.add(BorderLayout.CENTER, rootComponent);
			 parentPanel.add(zoomComponent);
			 rootPanel.validate();
			 rootPanel.repaint();
		 } else if (p == parentPanel && rootPanel.getComponentCount() == 1) {
			 Component switchComponent = rootPanel.getComponent(0);
			 if (switchComponent == rootComponent) {
				 parentPanel.remove(zoomComponent);
				 rootPanel.remove(rootComponent);
				 rootPanel.add(BorderLayout.CENTER, zoomComponent);
				 rootPanel.validate();
				 rootPanel.repaint();
			 }
		 }
	}
	 public void mouseClicked(MouseEvent e) {
		 if (rootComponent != null && e.getClickCount() > 1) {
			 zoomSwitch();
		 }
	 }
	 public void mousePressed(MouseEvent e) {}
	 public void mouseReleased(MouseEvent e) {}
	 public void mouseEntered(MouseEvent e) {}
	 public void mouseExited(MouseEvent e) {}
}
