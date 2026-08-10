package com.pip.uieditor.model.propertydescriptor;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MultipleSelectionComboCellEditor extends CellEditor {
	
	private MultipleSelectionCombo comboBox;
	
	private String[] items;
	
	private int[] selection;
	
	private static final int defaultStyle = SWT.NONE;

	public MultipleSelectionComboCellEditor() {
		setStyle(defaultStyle);
	}

	public MultipleSelectionComboCellEditor(Composite parent, String[] items) {
		this(parent, items, defaultStyle);
	}

	public MultipleSelectionComboCellEditor(Composite parent, String[] items, int style) {
		super(parent, style);
		setItems(items);
	}
	
	
	public void setItems(String[] items) {
		Assert.isNotNull(items);
		this.items = items;
		populateComboBoxItems();
	}
	
	private void populateComboBoxItems() {
		if (comboBox != null && items != null) {
			comboBox.setItmes(items);
			setValueValid(true);
		}
	}
	
	@Override
	protected Control createControl(Composite parent) {
		comboBox = new MultipleSelectionCombo(parent, items, null, getStyle());
		comboBox.setFont(parent.getFont());

		populateComboBoxItems();

		comboBox.addKeyListener(new KeyAdapter() {
			// hook key pressed - see PR 14201
			public void keyPressed(KeyEvent e) {
				keyReleaseOccured(e);
			}
		});

		comboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				selection = comboBox.getSelectionIndices();
				applyEditorValueAndDeactivate();
			}

			public void widgetSelected(SelectionEvent event) {
				selection = comboBox.getSelectionIndices();
				applyEditorValueAndDeactivate();
			}
		});

		comboBox.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE
						|| e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		});

		comboBox.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				MultipleSelectionComboCellEditor.this.focusLost();
			}
		});
		return comboBox;
	}

	@Override
	protected Object doGetValue() {
		return selection;
	}

	@Override
	protected void doSetFocus() {
		comboBox.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		selection = (int[])value;
		comboBox.setSelectionIndices(selection);
	}
	
	protected void focusLost() {
		if (isActivated()) {
			applyEditorValueAndDeactivate();
		}
	}
	
	void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		selection = comboBox.getSelectionIndices();
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);

		if (!isValid) {
			if (selection != null) {
				for (int i = 0; i < selection.length; i++) {
					if (items.length > 0 && selection[i] >= 0
							&& selection[i] < items.length) {
						setErrorMessage(MessageFormat.format(getErrorMessage(),
								new Object[] { items[selection[i]] }));
					} else {
						setErrorMessage(MessageFormat.format(getErrorMessage(),
								new Object[] { comboBox.getText() }));
					}
				}
			}
		}

		fireApplyEditorValue();
		deactivate();
	}
	
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\u001b') { // Escape character
			fireCancelEditor();
		} else if (keyEvent.character == '\t') { // tab key
			applyEditorValueAndDeactivate();
		}
	}
}
