package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;

public class MultipleSelectionCombo extends Composite {

	Text _text = null;
	String[] _items = null;
	int[] _selection = null;
	Shell _floatShell = null;
	List _list = null;

	public MultipleSelectionCombo(Composite parent, String[] items,
			int[] selection, int style) {
		super(parent, style);
		_selection = selection;
		_items = items;
		init();
	}

	private void init() {
		GridLayout layout = new GridLayout();
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		_text = new Text(this, SWT.READ_ONLY);
		_text.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		_text.addMouseListener(new MouseAdapter() { 
			  public void mouseDown(MouseEvent event) { 
				  super.mouseDown(event); 
				  initFloatShell(); 
			  } 
		  }); 
	}
	
	private void initFloatShell() {
		Point p = _text.getParent().toDisplay(_text.getLocation());
		Point size = _text.getSize();
		Rectangle shellRect = new Rectangle(p.x, p.y + size.y, size.x, 0);
		_floatShell = new Shell(MultipleSelectionCombo.this.getShell(),
				SWT.NO_TRIM);

		GridLayout gl = new GridLayout();
		gl.marginBottom = 2;
		gl.marginTop = 2;
		gl.marginRight = 2;
		gl.marginLeft = 2;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		_floatShell.setLayout(gl);

		_list = new List(_floatShell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		for (String value : _items) {
			_list.add(value);
		}
		_list.setSelection(_selection); 
		GridData gd = new GridData(GridData.FILL_BOTH);
		_list.setLayoutData(gd);
		_floatShell.setSize(shellRect.width, 100);
		_floatShell.setLocation(shellRect.x, shellRect.y);
		_list.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent event) {
				super.mouseUp(event);
				_selection = _list.getSelectionIndices();
				if ((event.stateMask & SWT.CTRL) == 0) {
					_floatShell.dispose();
					displayText();
					notifyListeners(SWT.Selection, new Event());
				}
			}
		});
		_floatShell.addShellListener(new ShellAdapter() {
			public void shellDeactivated(ShellEvent arg0) {
				if (_floatShell != null && !_floatShell.isDisposed()) {
					_selection = _list.getSelectionIndices();
					_floatShell.dispose();
					displayText();
					notifyListeners(SWT.Selection, new Event());
				}
			}
		});
		_floatShell.open();
	}
	
	private void displayText() {
		if (_selection != null && _selection.length > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < _selection.length; i++) {
				if (i > 0)
					sb.append(",");
				sb.append(_items[_selection[i]]);
			}
			_text.setText(sb.toString());
		} else {
			_text.setText("");
		}
	}
	
	public int[] getSelectionIndices() {
		int[] ret = new int[_selection.length];
		System.arraycopy(_selection, 0, ret, 0, _selection.length);
		return ret;
	}
	
	public void removeAll() {
		this._items = null;
		this._selection = null;
	}
	
	public void setItmes(String[] itmes) {
		this._items = itmes;
		this._selection = null;
	}
	
	public void setSelectionIndices(int[] selection) {
		this._selection = selection;
	}
	
	public String getText() {
		return _text.getText();
	}
	
	public void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Selection,typedListener);
		addListener (SWT.DefaultSelection,typedListener);
	}
}
