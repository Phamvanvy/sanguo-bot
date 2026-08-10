package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.editor.EditorApplication;

public class CardPropertyDescriptor extends PropertyDescriptor{

    public CardPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new CardCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new CardProvider();
    }

    public static class CardProvider extends LabelProvider {
        public String getText(Object element) {
            int cardId = ((Integer)element).intValue();
            Card card = EditorApplication.getInstance().getProjectData().findCard(cardId);
            if(card != null){                
                return card.title;
            }
            else{
                return "ÎÞÐ§µÄ¿¨Æ¬";
            }
        }
    }

}
