/**
 * 
 */
package com.pip.image.workshop;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author jhkang
 *
 */
public class FrameView extends ViewPart {

	/**
	 * 
	 */
	public FrameView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		SashForm sashFormLeft = new SashForm(sashForm, SWT.VERTICAL);
		sashFormLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		SashForm sashFormRight = new SashForm(sashFormLeft, SWT.VERTICAL);
		sashFormRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		

		sashFormRight.setWeights(new int[] { 1, 1 });

		sashFormLeft.setWeights(new int[] { 1, 1 });

		sashForm.setWeights(new int[] { 2, 1 });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
