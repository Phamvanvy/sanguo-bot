package com.pip.gtleditor;

import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;

public class GTLEditorViewer extends ProjectionViewer {
    private GTLEditorImpl editor;
    
    public GTLEditorViewer(GTLEditorImpl editor, Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
        this.editor = editor;
    }
    
    public GTLEditorImpl getEditor() {
        return editor;
    }
    
    public void resetPresentation() {
        fPresentationReconciler.install(this);
    }
}
