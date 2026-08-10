package com.pip.image.workshop.editor;

public interface ImageViewerListener {
	void areaSelected(Object source);
	void frameSelectionChanged(Object source, int newFrame);
	void frameDoubleClicked(Object source, int frame);
	void contentChanged(Object source);
}
