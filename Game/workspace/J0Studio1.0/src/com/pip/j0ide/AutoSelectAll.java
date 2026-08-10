package com.pip.j0ide;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

public class AutoSelectAll implements FocusListener {
	public static AutoSelectAll instance = new AutoSelectAll();
	public void focusGained(FocusEvent e) {
		if (e.getSource() instanceof Text) {
			((Text)e.getSource()).selectAll();
		}
	}
	public void focusLost(FocusEvent e) {}
}
