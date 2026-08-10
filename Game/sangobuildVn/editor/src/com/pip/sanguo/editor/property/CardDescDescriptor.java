/**
 * 
 */
package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * @author zlguo
 *
 */
public class CardDescDescriptor extends PropertyDescriptor {

    public CardDescDescriptor(Object id, String displayName) {
        super(id, displayName);
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        CardDescCellEditor cdce = new CardDescCellEditor(parent);
        return cdce;
    }
    
    public ILabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }
    
    class CardDescLableProvider extends LabelProvider{
        public String getText(Object element) {
            return super.getText(element);
        }
        
        
    }

}
