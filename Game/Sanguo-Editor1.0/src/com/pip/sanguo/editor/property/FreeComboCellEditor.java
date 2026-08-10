package com.pip.sanguo.editor.property;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 带列表并且允许自由输入的属性编辑器。
 * @author lighthu
 */
public class FreeComboCellEditor extends CellEditor {
    protected String[] items;
    protected String[] values;
    protected String selection;
    protected CCombo comboBox;
    
    /**
     * 构造方法。
     * @param parent
     * @param titles 标题列表
     * @param values 值列表
     */
    public FreeComboCellEditor(Composite parent, String[] titles, String[] values) {
        super(parent, SWT.NONE);
        this.values = values;
        setItems(titles);
    }
    
    /**
     * 设置列表框选项。
     */
    public void setItems(String[] items) {
        Assert.isNotNull(items);
        this.items = items;
        populateComboBoxItems();
    }

    /**
     * 创建编辑控件。
     */
    protected Control createControl(Composite parent) {

        comboBox = new CCombo(parent, SWT.NONE);
        comboBox.setFont(parent.getFont());
        comboBox.setVisibleItemCount(20);

        populateComboBoxItems();

        comboBox.addKeyListener(new KeyAdapter() {
            // hook key pressed - see PR 14201
            public void keyPressed(KeyEvent e) {
                keyReleaseOccured(e);
            }
        });

        comboBox.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event) {
                applyEditorValueAndDeactivate();
            }

            public void widgetSelected(SelectionEvent event) {
                selection = values[comboBox.getSelectionIndex()];
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
                FreeComboCellEditor.this.focusLost();
            }
        });
        return comboBox;
    }


    /**
     * 取得编辑器修改后的当前值。
     */
    protected Object doGetValue() {
        return selection;
    }

    /**
     * 焦点获得事件。
     */
    protected void doSetFocus() {
        comboBox.setFocus();
    }

    /**
     * The <code>ComboBoxCellEditor</code> implementation of this
     * <code>CellEditor</code> framework method sets the minimum width of the
     * cell. The minimum width is 10 characters if <code>comboBox</code> is
     * not <code>null</code> or <code>disposed</code> else it is 60 pixels
     * to make sure the arrow button and some text is visible. The list of
     * CCombo will be wide enough to show its longest item.
     */
    public LayoutData getLayoutData() {
        LayoutData layoutData = super.getLayoutData();
        if ((comboBox == null) || comboBox.isDisposed()) {
            layoutData.minimumWidth = 60;
        } else {
            // make the comboBox 10 characters wide
            GC gc = new GC(comboBox);
            layoutData.minimumWidth = (gc.getFontMetrics()
                    .getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }

    /**
     * 设置当前值。
     */
    protected void doSetValue(Object value) {
        Assert.isTrue(comboBox != null && (value instanceof String));
        selection = (String)value;
        comboBox.setText(selection);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(selection)) {
                comboBox.select(i);
                break;
            }
        }
    }

    /**
     * Updates the list of choices for the combo box for the current control.
     */
    private void populateComboBoxItems() {
        if (comboBox != null && items != null) {
            comboBox.removeAll();
            for (int i = 0; i < items.length; i++) {
                comboBox.add(items[i], i);
            }

            setValueValid(true);
            if (items.length > 0) {
                selection = values[0];
            } else {
                selection = "";
            }
        }
    }

    /**
     * Applies the currently selected value and deactivates the cell editor
     */
    void applyEditorValueAndDeactivate() {
        // must set the selection before getting value
        selection = comboBox.getText();
        int sel = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(selection) || items[i].equals(selection)) {
                selection = values[i];
                sel = i;
                break;
            }
        }
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);

        if (!isValid) {
            // Only format if the 'index' is valid
            if (items.length > 0 && sel >= 0 && sel < items.length) {
                // try to insert the current value into the error message.
                setErrorMessage(MessageFormat.format(getErrorMessage(),
                        new Object[] { items[sel] }));
            } else {
                // Since we don't have a valid index, assume we're using an
                // 'edit'
                // combo so format using its text value
                setErrorMessage(MessageFormat.format(getErrorMessage(),
                        new Object[] { comboBox.getText() }));
            }
        }

        fireApplyEditorValue();
        deactivate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.CellEditor#focusLost()
     */
    protected void focusLost() {
        if (isActivated()) {
            applyEditorValueAndDeactivate();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt.events.KeyEvent)
     */
    protected void keyReleaseOccured(KeyEvent keyEvent) {
        if (keyEvent.character == '\u001b') { // Escape character
            fireCancelEditor();
        } else if (keyEvent.character == '\t') { // tab key
            applyEditorValueAndDeactivate();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.CellEditor#activate(org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent)
     */
    public void activate(ColumnViewerEditorActivationEvent activationEvent) {
        super.activate(activationEvent);
        getControl().getDisplay().asyncExec(new Runnable() {

            public void run() {
                ((CCombo) getControl()).setListVisible(true);
            }

        });
    }
}
