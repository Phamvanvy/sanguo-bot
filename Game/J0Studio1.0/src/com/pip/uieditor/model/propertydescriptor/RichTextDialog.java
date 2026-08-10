package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;

import com.pip.j0ide.Settings;

public class RichTextDialog extends Dialog {
    private Text editor;
    private String text;
	private Text lblHint;
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public RichTextDialog(Shell parentShell) {
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
        container.setLayout(gridLayout);

        editor = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData gd_editor = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_editor.widthHint = 200;
        editor.setLayoutData(gd_editor);
        
        editor.setText(text);
        
        lblHint = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
        GridData gd_lblHint = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gd_lblHint.widthHint = 200;
        gd_lblHint.heightHint = 200;
        lblHint.setLayoutData(gd_lblHint);
        if (Settings.textStyle == 0) {
        	lblHint.setText("当前系统富文本格式为：Mango格式。你可以在UI编辑器设置界面修改富文本格式。\n" +
        			"格式说明：\n设置文字颜色：<cff0000>xxxx</c>\n" +
        			"设置文字阴影颜色：<dff00ff>xxx</d>\n图片数字：<i>12345</i>\n链接：<Lthis is my url>xxxx</L>\n" +
        			"<L>,<d>,<c>标签可以嵌套。");
        } else {
        	lblHint.setText("当前系统富文本格式为：Scryer格式。你可以在UI编辑器设置界面修改富文本格式。\n" +
        			"格式说明：\n设置文字格式：<text color=0xffff00 shadow=true/false shadowcolor=0xff00ff font=fontalias>xxx</text>。其中所有属性都是可选的。\n" +
        			"链接：<link color=0xffff00 shadow=true/false shadowcolor=0xff00ff font=fontalias url=\"this is my url\">xxx</link>。其中除了url以外的属性都是可选的。\n" +
        			"嵌入图片：<img file=a.pip index=2 trans=0 scale=0.5 hgap=3></img>。其中trans、scale和hgap属性是可选的。\n" +
        			"嵌入动画：<animate file=a.ctn index=1 scale=0.5 hgap=10></animate>。其中scale和hgap属性是可选的。\n" +
        			"<text>和<link>格式标签允许嵌套其他标签。");
        }
        
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
        return new Point(604, 549);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("富文本编辑器");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            text = editor.getText();
            text = text.replaceAll("\r", "");
        }
        super.buttonPressed(buttonId);
    }
}
