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

/**
 * 提问选项列表编辑器。选项列表是用\n分隔的多个字符串，我们用一个不可编辑文本对象和一个按钮组成这个编辑器。按下按钮将在
 * 一个弹出的对话框中编辑选项列表。
 */
public class OptionListCellEditor extends CellEditor {
    protected Composite container;
    protected Text text;
    protected Button button;
    protected String listText;

    public OptionListCellEditor(Composite parent) {
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
                OptionListCellEditor.this.focusLost();
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
                OptionListCellEditor.this.focusLost();
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
        return listText;
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
        listText = (String)value;
        text.setText(listText.replace("\n", "，"));
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
        return getClass() != OptionListCellEditor.class;
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
        OptionListDialog dlg = new OptionListDialog(text.getShell());
        dlg.setListText(listText);
        if (dlg.open() == Dialog.OK) {
            listText = dlg.getListText();
            text.setText(listText.replace("\n", "，"));
            fireApplyEditorValue();
            deactivate();
        }
    }

    public static class OptionListDialog extends Dialog {
        private Text text;
        private String listText;
        
        public String getListText() {
            return listText;
        }
    
        public void setListText(String listText) {
            this.listText = listText;
        }
    
        /**
         * Create the dialog
         * @param parentShell
         */
        public OptionListDialog(Shell parentShell) {
            super(parentShell);
        }
    
        /**
         * Create contents of the dialog
         * @param parent
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            final GridLayout gridLayout = new GridLayout();
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 0;
            container.setLayout(gridLayout);
    
            final Label label = new Label(container, SWT.NONE);
            label.setText("选项列表（每行一个选项）:");
    
            text = new Text(container, SWT.MULTI | SWT.BORDER);
            text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            text.setText(listText);
            
            return container;
        }
    
        /**
         * Create contents of the button bar
         * @param parent
         */
        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID, "确定", true);
            createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
        }
    
        /**
         * Return the initial size of the dialog
         */
        @Override
        protected Point getInitialSize() {
            return new Point(272, 340);
        }
        
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText("选项编辑");
        }
        
        protected void buttonPressed(int buttonId) {
            if (buttonId == IDialogConstants.OK_ID) {
                listText = text.getText().trim();
            }
            super.buttonPressed(buttonId);
        }
    }
}
