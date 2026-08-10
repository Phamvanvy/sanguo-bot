package canseereaditem;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class AnalyzeLog {
	//目标文件
	public static File preFile ;
	//分析后文件
	public static File orderFile;
	public static String fillText;
	private static Text text;
	private static String log;		//保存目录
	public static void main(String[] args) throws DocumentException{
		readitem.cycle=readitem.menuName.length;
		readitem.loadEqus();
		readitem.loadItem();
		readitem.loadSkill();
		readitem.loadRecipesNew();
		readitem.loadPetColor();
		readitem.initParseMenu();
		Display display = Display.getDefault();
		final Shell shell = new Shell();
		shell.setText("日志分析工具");
		GridLayout gShellLay = new GridLayout();
		shell.setLayout(gShellLay); 
		//构造一个Composite构件作为文本框和按键的容器 　　
		Composite panel = new Composite(shell,SWT.NONE); 
		//为Panel指定一个布局结构对象。 这里让Panel尽可能的占满Shell，也就是全部应用程序窗口的空间。 　　
		GridData gPanelData = new GridData(GridData.FILL_BOTH);
		panel.setLayoutData(gPanelData); 
		//为Panel也设置一个布局对象。文本框和按键将按这个布局对象来显示。
		GridLayout gPanelLay = new GridLayout();
		panel.setLayout(gPanelLay); 
		final MessageBox msgBox = new MessageBox(shell,SWT.OK);
		final Color bkColor = new Color(Display.getCurrent(),255,255,255);
		//生成文本框 
		text = new Text(panel,  SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		text.setBackground(bkColor);
		//为文本框指定一个布局结构对象，这里让文本框尽可能的占满Panel的空间。
		GridData gTextData = new GridData (GridData.FILL_BOTH);
		text.setLayoutData(gTextData); 
		
		Menu mainmMenu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mainmMenu);
		MenuItem fileItem = new MenuItem(mainmMenu, SWT.CASCADE);
		fileItem.setText("文件");
		MenuItem arrMenuItem[]=new MenuItem[readitem.cycle];
//		for(int i=0;i<readitem.cycle;i++){
//			arrMenuItem[i]=new MenuItem(mainmMenu,SWT.CASCADE);
//			arrMenuItem[i].setText(readitem.menuName[i]);
//		}
//		MenuItem selectFiletItem = new MenuItem(mainmMenu, SWT.CASCADE);
//		selectFiletItem.setText("选择分析内容");
		//文件下的子菜单
		Menu filemMenu = new Menu (shell, SWT.DROP_DOWN);
		fileItem.setMenu(filemMenu);
		MenuItem newFileItem = new MenuItem(filemMenu, SWT.CASCADE);
		newFileItem.setText("打开文件");
		newFileItem.addSelectionListener(new SelectionListener(){

		
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				//打开文件对话框
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setText("日志目录");
				dialog.setFilterPath(log);
				log= dialog.open();
				//System.out.println(log);
				if(log!=null){
					preFile = new File(log);
				}
			}

		
			
		});
		//文件的最后保存
		MenuItem saveItem = new MenuItem(filemMenu, SWT.CALENDAR);
		saveItem.setText("保存分析后的日志文件");
		saveItem.addSelectionListener(new SelectionListener(){

			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				//打开文件对话框
				if(preFile==null){
					msgBox.setMessage("请先打开一个日志文件。");
					msgBox.open();
					return;
				}
				readitem.showFlag = false;
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setText("保存分析后的日志");
				dialog.setFilterPath("D:/");
				String  fileName= dialog.open();
				if(fileName!=null)
					orderFile = new File(fileName);
				
				try {
					//readitem.read();
					readitem.readSelect(0,0, true);
					//text.setText("");
					msgBox.setMessage("成功保存到"+orderFile.getAbsolutePath());
					msgBox.open();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					//text.setText("");
					msgBox.setMessage("保存失败！");
					msgBox.open();
				} catch (DocumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					//text.setText("");
					msgBox.setMessage("保存失败！");
					msgBox.open();
				}
			}
		});
		//动态生成菜单
		for(int i=0;i<readitem.cycle;i++){
			Menu selectMenu = new Menu(shell,SWT.DROP_DOWN);
			MenuItem subMenuItem = new MenuItem(mainmMenu,SWT.CASCADE);
			subMenuItem.setText(readitem.menuName[i]);
			subMenuItem.setMenu(selectMenu);
			readitem.curMenuType=i;
			int maxSelect = readitem.getTitleNameLength(i)+1;
			for(int j=1;j<maxSelect;j++){
				MenuItem selectMenuItems =  new MenuItem(selectMenu, SWT.CASCADE);
				String selectString = readitem.getSelectTitle(i,j);
				selectMenuItems.setText(selectString);
				selectMenuItems.addSelectionListener(new SelectionListener(){
					private int type =readitem.curMenuType;
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						text.setText("");
						if(preFile==null){
							msgBox.setMessage("请先打开一个日志文件。");
							msgBox.open();
							return;
						}
						readitem.showFlag = true;
						
						String tempString = ((MenuItem)e.getSource()).getText();
						//System.out.println(tempString);
						int t = readitem.getSelectIndex(type,tempString);
						//System.out.println(i);
						try {
							readitem.parseMenuMap.get(type).init();
							readitem.readSelect(type,t,false);
							writeText(text.getText() + "\n计数器为:" + readitem.parseMenuMap.get(type).count);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (DocumentException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
				});
			}
		}
		
		//文本框 输出
		//text = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		shell.layout();
		shell.open();
		//text.setBounds(shell.getClientArea());
		while(!shell.isDisposed()){
			if(!display.readAndDispatch()){
				display.sleep();
			}
		}
		bkColor.dispose();
		display.dispose();
	}
	public static void consolePrintln(String s){
		if(!text.isDisposed()){
			text.insert(s);
		}
	}
	
	public static void writeText(String s){
		if(!text.isDisposed()){
			text.setText(s);
		}
	}
}
