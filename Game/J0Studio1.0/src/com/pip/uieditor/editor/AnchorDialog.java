package com.pip.uieditor.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.BorderLayout;

import com.pip.uieditor.model.Anchor;
import com.pip.uieditor.model.AnchorPoint;
import org.eclipse.swt.widgets.Label;


public class AnchorDialog extends Dialog implements PaintListener{
	
	public static final int CENTER = 0;
	public static final int TOPLEFT = 1;
	public static final int TOP = 2;
	public static final int TOPRIGHT = 3;
	public static final int RIGHT = 4;
	public static final int BOTTOMRIGHT = 5;
	public static final int BOTTOM = 6;
	public static final int BOTTOMLEFT = 7;
	public static final int LEFT = 8;
	
	private static final String[] ANCHOR_STRING = {"无", "居中", "左上", "上", "右上", "右", "右下", "下", "左下", "左"};
	private static final int TOP_FLAG = 1;
	private static final int BOTTOM_FLAG = 2;
	private static final int LEFT_FLAG = 4;
	private static final int RIGHT_FLAG = 8;
	private static final int VCENTER_FLAG = 16;
	private static final int HCENTER_FLAG = 32;
	
	private static final int[] FLAGS = {
			HCENTER_FLAG | VCENTER_FLAG,
			TOP_FLAG | LEFT_FLAG, 
			TOP_FLAG | HCENTER_FLAG, 
			TOP_FLAG | RIGHT_FLAG, 
			RIGHT_FLAG | VCENTER_FLAG,
			RIGHT_FLAG | BOTTOM_FLAG, 
			BOTTOM_FLAG | HCENTER_FLAG, 
			LEFT_FLAG | BOTTOM_FLAG,
			LEFT_FLAG  | VCENTER_FLAG};
	
	
	private Text txOffset1;
	private Text txOffset2;
	private Text txOffset3;
	private Text txOffset4;
	private Combo cbAnchor1;
	private Combo cbAnchor2;
	private Combo cbAnchor3;
	private Combo cbAnchor4;
	Canvas cvs1;
	
	private List<AnchorPoint> anchorPoints;
	private Label lblNewLabel_3;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AnchorDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public void setAnchorPoints(List<AnchorPoint> anchorPoints) {
		this.anchorPoints = anchorPoints;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new BorderLayout(0, 0));
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(BorderLayout.CENTER);
		composite.setLayout(null);
		
		cbAnchor1 = new Combo(composite, SWT.READ_ONLY);
		cbAnchor1.setBounds(10, 46, 119, 25);
		cbAnchor1.setItems(ANCHOR_STRING);
		cbAnchor1.select(0);
		cbAnchor1.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cvs1.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				cvs1.redraw();
			}
		});
		
		cbAnchor2 = new Combo(composite, SWT.READ_ONLY);
		cbAnchor2.setBounds(135, 46, 119, 25);
		cbAnchor2.setItems(ANCHOR_STRING);
		cbAnchor2.select(0);
		cbAnchor2.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cvs1.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				cvs1.redraw();
			}
		});
		
		txOffset1 = new Text(composite, SWT.BORDER);
		txOffset1.setBounds(266, 46, 73, 23);
		txOffset1.setTextLimit(5);
		txOffset1.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				cvs1.redraw();
			}
		});
		
		txOffset2 = new Text(composite, SWT.BORDER);
		txOffset2.setBounds(345, 46, 73, 23);
		txOffset2.setTextLimit(5);
		txOffset2.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				cvs1.redraw();
			}
		});
		
		cbAnchor3 = new Combo(composite, SWT.READ_ONLY);
		cbAnchor3.setBounds(10, 88, 119, 25);
		cbAnchor3.setItems(ANCHOR_STRING);
		cbAnchor3.select(0);
		cbAnchor3.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cvs1.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				cvs1.redraw();
			}
		});
		
		cbAnchor4 = new Combo(composite, SWT.READ_ONLY);
		cbAnchor4.setBounds(135, 88, 119, 25);
		cbAnchor4.setItems(ANCHOR_STRING);
		cbAnchor4.select(0);
		cbAnchor4.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cvs1.redraw();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				cvs1.redraw();
			}
		});
		
		txOffset3 = new Text(composite, SWT.BORDER);
		txOffset3.setBounds(266, 88, 73, 23);
		txOffset3.setTextLimit(5);
		txOffset3.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				cvs1.redraw();
			}
		});
		
		txOffset4 = new Text(composite, SWT.BORDER);
		txOffset4.setBounds(345, 88, 73, 23);
		txOffset4.setTextLimit(5);
		
		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setBounds(10, 24, 61, 17);
		lblNewLabel.setText("\u951A\u70B9\uFF1A");
		
		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setBounds(135, 23, 73, 17);
		lblNewLabel_1.setText("\u7236\u63A7\u4EF6\u951A\u70B9\uFF1A");
		
		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setBounds(266, 24, 61, 17);
		lblNewLabel_2.setText("X\u504F\u79FB\uFF1A");
		
		lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.setBounds(345, 24, 61, 17);
		lblNewLabel_3.setText("Y\u504F\u79FB\uFF1A");
		
		Label labelModes = new Label(composite, SWT.NONE);
		labelModes.setBounds(10, 129, 61, 17);
		labelModes.setText("\u5E38\u7528\u6A21\u5F0F\uFF1A");
		
		Button buttonCenter = new Button(composite, SWT.NONE);
		buttonCenter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cbAnchor1.select(CENTER + 1);
				cbAnchor2.select(CENTER + 1);
				cbAnchor3.select(0);
				cbAnchor4.select(0);
				txOffset1.setText("0");
				txOffset2.setText("0");
				txOffset3.setText("0");
				txOffset4.setText("0");
				cvs1.redraw();
			}
		});
		buttonCenter.setBounds(77, 124, 80, 27);
		buttonCenter.setText("\u5C45\u4E2D");
		
		Button buttonFill = new Button(composite, SWT.NONE);
		buttonFill.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cbAnchor1.select(TOPLEFT + 1);
				cbAnchor2.select(TOPLEFT + 1);
				cbAnchor3.select(BOTTOMRIGHT + 1);
				cbAnchor4.select(BOTTOMRIGHT + 1);
				txOffset1.setText("0");
				txOffset2.setText("0");
				txOffset3.setText("0");
				txOffset4.setText("0");
				cvs1.redraw();
			}
		});
		buttonFill.setBounds(163, 124, 80, 27);
		buttonFill.setText("\u586B\u6EE1");
		
		Button buttonFillHorizon = new Button(composite, SWT.NONE);
		buttonFillHorizon.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cbAnchor1.select(LEFT + 1);
				cbAnchor2.select(LEFT + 1);
				cbAnchor3.select(0);
				cbAnchor4.select(0);
				txOffset1.setText("0");
				txOffset2.setText("0");
				txOffset3.setText("0");
				txOffset4.setText("0");
				cvs1.redraw();
			}
		});
		buttonFillHorizon.setBounds(249, 124, 80, 27);
		buttonFillHorizon.setText("\u9760\u5DE6");
		txOffset4.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				cvs1.redraw();
			}
		});
		
		cvs1 = new Canvas(container, SWT.NONE);
		cvs1.addPaintListener(this);
		cvs1.setLayoutData(BorderLayout.SOUTH);
		if (anchorPoints != null) {
			int size = anchorPoints.size();
			if (size > 2) {
				size = 2;
			}
			if (size >= 1) {
				AnchorPoint anchorPoint1 = anchorPoints.get(0);
				cbAnchor1.select(anchorPoint1.getAnchor() + 1);
				cbAnchor2.select(anchorPoint1.getRelativeAnchor() + 1);
				txOffset1.setText(anchorPoint1.getOffsetX() + "");
				txOffset2.setText(anchorPoint1.getOffsetY() + "");
			}
			if (size == 2) {
				AnchorPoint anchorPoint2 = anchorPoints.get(1);
				cbAnchor3.select(anchorPoint2.getAnchor() + 1);
				cbAnchor4.select(anchorPoint2.getRelativeAnchor() + 1);
				txOffset3.setText(anchorPoint2.getOffsetX() + "");
				txOffset4.setText(anchorPoint2.getOffsetY() + "");
			}
		}
		return container;
	}
	
	private boolean isAnchorPointsValid() {
		int anchor1 = cbAnchor1.getSelectionIndex() - 1;
		int anchor2 = cbAnchor2.getSelectionIndex() - 1;
		int anchor3 = cbAnchor3.getSelectionIndex() - 1;
		int anchor4 = cbAnchor3.getSelectionIndex() - 1;
		if(anchor1 == -1 && anchor2 != -1)
			return false;
		if(anchor1 != -1 && anchor2 == -1)
			return false;
		if(anchor3 == -1 && anchor4 != -1)
			return false;
		if(anchor3 != -1 && anchor4 == -1)
			return false;
		if(anchor1 != -1 && anchor2 != -1) {
			return isNumberText(txOffset1) && isNumberText(txOffset2);
		}
		if(anchor3 != -1 && anchor4 != -1) {
			return isNumberText(txOffset3) && isNumberText(txOffset4);
		}
		return true;
	}
	
	
	private boolean isNumberText(Text text) {
		String val = text.getText();
		if(val.length() == 0)
			return true;
		try {
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private int getNumber(Text text) {
		String val = text.getText();
		if(val.length() == 0)
			return 0;
		return Integer.parseInt(val);
	}
	
	@Override
	protected void okPressed() {
		if(!isAnchorPointsValid()) {
        	MessageDialog.openError(getShell(), "错误", "输入错误");
        	return;
		}
		saveRetAnchorPoints();
		super.okPressed();
	}
	
	public List<AnchorPoint> getAnchorPoints() {
		return this.anchorPoints;
	}
	
	protected List<AnchorPoint> calcAnchorPoints() {
		List<AnchorPoint> anchorPoints = new ArrayList<AnchorPoint>(2);
		int anchor = cbAnchor1.getSelectionIndex() - 1;
		int relativeAnchor = cbAnchor2.getSelectionIndex() - 1;
		if(anchor != -1 && relativeAnchor != -1) {
			AnchorPoint anchorPoint = new AnchorPoint(anchor, relativeAnchor);
			anchorPoint.setOffset(new org.eclipse.draw2d.geometry.Point(getNumber(txOffset1), getNumber(txOffset2)));
			anchorPoints.add(anchorPoint);
		}
		anchor = cbAnchor3.getSelectionIndex() - 1;
		relativeAnchor = cbAnchor4.getSelectionIndex() - 1;
		if(anchor != -1 && relativeAnchor != -1) {
			AnchorPoint anchorPoint = new AnchorPoint(anchor, relativeAnchor);
			anchorPoint.setOffset(new org.eclipse.draw2d.geometry.Point(getNumber(txOffset3), getNumber(txOffset4)));
			anchorPoints.add(anchorPoint);
		}
		return anchorPoints;
	}
	
	protected void saveRetAnchorPoints() {
		this.anchorPoints = calcAnchorPoints();
	}
	
	

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(435, 316);
	}

	@Override
	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		gc.setLineWidth(2);
		gc.setForeground(ColorConstants.cyan);
		Rectangle rect = cvs1.getClientArea();
		gc.drawRectangle(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
		if(isAnchorPointsValid()) {
			List<AnchorPoint> l = calcAnchorPoints();
			if(l != null && l.size() > 0) {
				rect.x += 2;
				rect.y += 2;
				rect.width -= 4;
				rect.height -= 4;
				Rectangle r = layoutPreviewRegion(l, rect, rect.width / 3 , rect.height / 3);
				if(r != null && (r.width != 0 && r.height != 0)) {
					gc.setBackground(ColorConstants.gray);
					gc.fillRectangle(r);
				}
			}
			
		}
	}
	
	private Rectangle layoutPreviewRegion(List<AnchorPoint> aps,
			Rectangle containerBounds, int hitWidth, int hitHeight) {
		if(aps == null || aps.size() == 0) {
			return new Rectangle(containerBounds.x, containerBounds.y, 0, 0);
		}
		int flag = 0;
		int top = 0;
		int bottom = 0;
		int left = 0;
		int right = 0;
		int vcenter = 0;
		int hcenter = 0;
		for(int i = 0; i < aps.size(); i++) {
			AnchorPoint ap = aps.get(i);
			int f = FLAGS[ap.getAnchor()];
			if((flag & f) != 0)
				return null;
			flag |= f;
			Point p = calcRelativePoint(ap.getRelativeAnchor(), containerBounds, ap.getOffsetX(), ap.getOffsetY());
			switch(ap.getAnchor()) {
				case CENTER:
					vcenter = p.y;
					hcenter = p.x;
					break;
				case TOPLEFT:
					top = p.y;
					left = p.x;
					break;
				case TOP:
					top = p.y;
					hcenter = p.x;
					break;
				case TOPRIGHT:
					top = p.y;
					right = p.x;
					break;
				case RIGHT:
					right = p.x;
					vcenter = p.y;
					break;
				case BOTTOMRIGHT:
					right = p.x;
					bottom = p.y;
					break;
				case BOTTOM:
					bottom = p.y;
					hcenter = p.x;
					break;
				case BOTTOMLEFT:
					left = p.x;
					bottom = p.y;
					break;
				case LEFT:
					left = p.x;
					vcenter = p.y;
					break;
			}
		}
		if((flag & VCENTER_FLAG) != 0 && (flag & HCENTER_FLAG) != 0) {
			return new Rectangle(hcenter - hitWidth /2 , vcenter - hitHeight/2, hitWidth, hitHeight);
		}
		int x = 0;
		int y = 0;
		int width = hitWidth;
		int height = hitHeight;
		if((flag & LEFT_FLAG) != 0 && (flag & RIGHT_FLAG) != 0) {
			width = right - left;
		}
		if((flag & TOP_FLAG) != 0 && (flag & BOTTOM_FLAG) != 0) {
			height = bottom - top;
		}
		if((flag & LEFT_FLAG) != 0) {
			x = left;
		} else if((flag & RIGHT_FLAG) != 0) {
			x = right - width;
		} else if((flag & HCENTER_FLAG) != 0) {
			x = hcenter - width / 2;
		}
		if((flag & TOP_FLAG) != 0) {
			y = top;
		} else if((flag & BOTTOM_FLAG) != 0) {
			y = bottom - height;
		} else if((flag & VCENTER_FLAG) != 0) {
			y = vcenter - height / 2;
		}
		return new Rectangle(x, y, width, height);
	}
	
	protected Point calcRelativePoint(int relativeAnchor, Rectangle bounds, int offsetX, int offsetY) {
		int relX = 0;
		int relY = 0;
		switch(relativeAnchor) {
			case Anchor.TOPLEFT:
				relX = 0;
				relY = 0;
				break;
			case Anchor.TOP:
				relX = bounds.width / 2;
				relY = 0;
				break;
			case Anchor.TOPRIGHT:
				relX = bounds.width;
				relY = 0;
				break;
			case Anchor.RIGHT:
				relX = bounds.width;
				relY = bounds.height / 2;
				break;
			case Anchor.BOTTOMRIGHT:
				relX = bounds.width;
				relY = bounds.height;
				break;
			case Anchor.BOTTOM:
				relX = bounds.width / 2;
				relY = bounds.height;
				break;
			case Anchor.BOTTOMLEFT:
				relX = 0;
				relY = bounds.height;
				break;
			case Anchor.LEFT:
				relX = 0;
				relY = bounds.height / 2;
				break;
			case Anchor.CENTER:
				relX = bounds.width / 2;
				relY = bounds.height / 2;
				break;
			default:
				throw new IllegalArgumentException();
		}
		return new Point(bounds.x + relX + offsetX, bounds.y + relY + offsetY);
	}
}
