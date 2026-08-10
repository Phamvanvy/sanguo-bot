package com.pip.sanguo.editor.property;

import java.text.MessageFormat; // Not using ICU to support standalone JFace scenario

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.Utils;

/**
 * 选择多个坐骑类型的编辑器。
 */
public class HorseTypeCellEditor extends CellEditor {
    protected Composite container;
    protected Text text;
    protected Button button;
    protected int[] selectedTypes = new int[0];

    public HorseTypeCellEditor(Composite parent) {
        super(parent, SWT.NONE);
    }

    protected Control createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout gd = new GridLayout(2, false);
        gd.horizontalSpacing = gd.verticalSpacing = 0;
        gd.marginWidth = gd.marginHeight = 0;
        container.setLayout(gd);
        
        text = new Text(container, SWT.SINGLE);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text.setEditable(false);
        text.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });
        text.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                HorseTypeCellEditor.this.focusLost();
            }
        });
        text.setFont(parent.getFont());
        text.setBackground(parent.getBackground());
        text.setText("");//$NON-NLS-1$
        
        button = new Button(container, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        button.setText("...");
        button.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    e.doit = false;
                }
            }
        });
        button.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                HorseTypeCellEditor.this.focusLost();
            }
        });
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                editText();
            }
        });
        
        return container;
    }

    /**
     * The <code>TextCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method returns
     * the text string.
     *
     * @return the text string
     */
    protected Object doGetValue() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedTypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(selectedTypes[i]);
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    protected void doSetFocus() {
        if (button != null) {
            button.setFocus();
        }
    }

    /**
     * The <code>TextCellEditor</code> implementation of
     * this <code>CellEditor</code> framework method accepts
     * a text string (type <code>String</code>).
     *
     * @param value a text string (type <code>String</code>)
     */
    protected void doSetValue(Object value) {
        Assert.isTrue(text != null && (value instanceof String));
        String[] secs = Utils.splitString((String)value, ',');
        if (secs.length == 0 && secs[0].length() == 0) {
            secs = new String[0];
        }
        selectedTypes = new int[secs.length];
        for (int i = 0; i < secs.length; i++) {
            selectedTypes[i] = Integer.parseInt(secs[i]);
        }
        setShowText();
    }
    
    private void setShowText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedTypes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            HorseType ht = (HorseType)EditorApplication.getProj().findObject(HorseType.class, selectedTypes[i]);
            sb.append(ht.title);
        }
        text.setText(sb.toString());
    }

    /**
     * Since a text editor field is scrollable we don't
     * set a minimumSize.
     */
    public LayoutData getLayoutData() {
        return new LayoutData();
    }

    /**
     * The <code>TextCellEditor</code>  implementation of this 
     * <code>CellEditor</code> method returns <code>true</code> if 
     * the current selection is not empty.
     */
    public boolean isCopyEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getSelectionCount() > 0;
    }

    /**
     * Returns <code>true</code> if this cell editor is
     * able to perform the select all action.
     * <p>
     * This default implementation always returns 
     * <code>false</code>.
     * </p>
     * <p>
     * Subclasses may override
     * </p>
     * @return <code>true</code> if select all is possible,
     *  <code>false</code> otherwise
     */
    public boolean isSelectAllEnabled() {
        if (text == null || text.isDisposed()) {
            return false;
        }
        return text.getCharCount() > 0;
    }

    /**
     * The <code>TextCellEditor</code> implementation of this
     * <code>CellEditor</code> method copies the
     * current selection to the clipboard. 
     */
    public void performCopy() {
        text.copy();
    }

    /**
     * The <code>TextCellEditor</code> implementation of this
     * <code>CellEditor</code> method selects all of the
     * current text. 
     */
    public void performSelectAll() {
        text.selectAll();
    }

    /**
     * This implementation of
     * {@link CellEditor#dependsOnExternalFocusListener()} returns false if the
     * current instance's class is TextCellEditor, and true otherwise.
     * Subclasses that hook their own focus listener should override this method
     * and return false. See also bug 58777.
     * 
     * @since 3.4
     */
    protected boolean dependsOnExternalFocusListener() {
        return getClass() != HorseTypeCellEditor.class;
    }

    /**
     * Processes a focus lost event that occurred in this cell editor.
     * <p>
     * The default implementation of this framework method applies the current
     * value and deactivates the cell editor. Subclasses should call this method
     * at appropriate times. Subclasses may also extend or reimplement.
     * </p>
     */
    protected void focusLost() {
        if (!text.isFocusControl() && !button.isFocusControl()) {
            if (isActivated()) {
                fireApplyEditorValue();
                deactivate();
            }
        }
    }
    
    protected void editText() {
        ChooseHorseTypeDialog dlg = new ChooseHorseTypeDialog(text.getShell());
        dlg.setSelectedTypes(selectedTypes);
        if (dlg.open() == Dialog.OK) {
            selectedTypes = dlg.getSelectedTypes();
            setShowText();
            fireApplyEditorValue();
            deactivate();
        }
    }
}
