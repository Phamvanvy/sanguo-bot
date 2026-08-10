package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

public class ViewResRefDialog extends Dialog {

	private AbstractImageViewer viewer;
	public ViewResRefDialog(Shell parentShell, AbstractImageViewer viewer) {
		super(parentShell);
		setBlockOnOpen(false);
		this.viewer = viewer;
	}
	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "确定",
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				"取消", false);
	}
	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		if(p.y>400){
			p.y = 400;
		}
//		return new Point(500,400); 
		return p;
	}
	private int itemCnt;
	private ScrolledComposite scrollComposite;
	private List<Text> names = new ArrayList<Text>();
	private Text filter;
	private int aniCnt;
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	protected Control createDialogArea(Composite parent) {
		Composite p = (Composite) super.createDialogArea(parent);
		p.setLayout(new FillLayout());
		scrollComposite = new ScrolledComposite(p, SWT.V_SCROLL);
		Composite container = new Composite(scrollComposite, SWT.NONE);

//	    for(int i=0;i<500; i++){
//		    final Label label = new Label(container, SWT.NONE);
//		    label.setText(i+"");
//	    }
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 5;
		container.setLayout(gridLayout);
		//表头文字
		String[] tableHead = new String[]{"N","资源名称","计数","操作"};
		gridLayout.numColumns = tableHead.length;
		
		//条件框
		filter = new Text(container, SWT.BORDER);
		filter.setText("请输入搜索条件");
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.widthHint = SWT.DEFAULT;
//		gridData.heightHint = SWT.DEFAULT;
		gridData.horizontalSpan = gridLayout.numColumns - 1;
		filter.setLayoutData(gridData);
		filter.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == 13){
					e.doit = false;
					return;
				}
				super.keyPressed(e);
			}
		});
		
		Button searchBtn = new Button(container, SWT.None);
		searchBtn.setText("搜索");
		searchBtn.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				doFiltering(); 
			}
		});
		
		//表头
		for(String title:tableHead){
			final Label label = new Label(container, SWT.NONE);
			label.setText(title);
		}
		//数据
		int i = 0;
		for(String name:refMap.keySet()){
			final Label label = new Label(container, SWT.NONE);
			label.setText(String.valueOf(i));
			addNameAndRefCnt(name, refMap.get(name).intValue(), container);
			addDelButton(i, container);
			i++;
		}
		itemCnt = i;
		scrollComposite.setContent(container);
		container.pack();
		scrollComposite.setMinSize(container.getSize().x,container.getSize().y);

	    // Expand both horizontally and vertically
		scrollComposite.getVerticalBar().setVisible(true);
	    scrollComposite.setExpandHorizontal(true);
	    scrollComposite.setExpandVertical(true); 
	    getShell().setText("查看动画资源引用 - 动画个数:"+aniCnt);
		return container;
	}
	protected void doFiltering() {
		Text find = null;
		String condition = filter.getText();
		for(Text t:names){
			if(t.getText().indexOf(condition)>=0){
				find = t;
				break;
			}
		}
		if(find == null){
			
		}else{
			int yy =find.getBounds().y - 40;
			if(yy<0){
				yy = 0;
			}
			find.setSelection(0, find.getText().length());
			find.setFocus();
//			scrollComposite.getVerticalBar().setSelection(yy);
//			scrollComposite.showControl(find);
			scrollComposite.setShowFocusedControl(true);
		}
	}
	private void addDelButton(int i, Composite container) {
		final Button delBtn = new Button(container, SWT.None);
		delBtn.setText("删除");
		delBtn.setData(new Integer(i));
//		if(refTimes!=null && refTimes.get(i)>0){
//			delBtn.setEnabled(false);
//		}else{
			delBtn.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent arg0) {
					doClick(delBtn.getData());
				}
			});
//		}		
	}
	private void addNameAndRefCnt(String name, int cnt, Composite container){
//		final Label label = new Label(container, SWT.NONE);
		final Text label = new Text(container, SWT.NONE);
		label.setText(name);
		label.setEditable(false);
		names.add(label);
		
		final Label labelT = new Label(container, SWT.NONE);
		if(cnt>=0){
			labelT.setText(cnt+"");
			aniCnt += cnt;
		}else{
			labelT.setText("");
		}
	}
	protected void doClick(Object data) {
		this.viewer.response(this, data);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("查看动画资源引用");
	}
	private HashMap<String, Integer> refMap;

	/**
	 * 注意使用了LinkedHashMap来保证entry的顺序,因为在做删除时用的是传回来的索引
	 * @return
	 * @throws Exception
	 */
	public void setRefMap(HashMap<String, Integer> refCnt) {
		this.refMap = refCnt;
	}
}
