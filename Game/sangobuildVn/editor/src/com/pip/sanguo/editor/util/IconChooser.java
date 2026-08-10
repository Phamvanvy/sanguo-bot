package com.pip.sanguo.editor.util;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.skill.ChooseSkillIconDialog;
import com.pipimage.image.PipImage;

public class IconChooser extends Composite {
    
    private int iconIndex = -1;
    private Image currentIcon;
    private int type = 0;//0-ablility.pip 1-weapon.pip
    
    /**
     * 编辑器dirty状态监听
     */
    private DefaultDataObjectEditor listener;
    /**
     * 图标预览
     */
    private Label iconPreview;
    private Text textIconIndex;
    public IconChooser(Composite parent, int style, int type) {
        super(parent, SWT.NONE);
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(final DisposeEvent e) {
                if (currentIcon != null) {
                    currentIcon.dispose();
                    currentIcon = null;
                }
            }
        });
        
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        gridLayout.marginHeight = 0;
        setLayout(gridLayout);
        
        iconPreview = new Label(this, SWT.NONE);
        final GridData gd_iconPreview = new GridData(83, SWT.DEFAULT);
        iconPreview.setLayoutData(gd_iconPreview);
        iconPreview.setText("");
        iconPreview.setAlignment(SWT.CENTER);

        textIconIndex = new Text(this, SWT.BORDER);
        textIconIndex.setEditable(false);
        final GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textIconIndex.setLayoutData(gd_text_1);
        
        final Button chooseButton = new Button(this, SWT.NONE);
        chooseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        chooseButton.setText("...");
        chooseButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                onChooseIcon();
            }
        });
        
        this.type = type;
    }
    
    public void setHandler(DefaultDataObjectEditor handle) {
        this.listener = handle;
    }
    
    public int getIconIndex(){
        return iconIndex;
    }
    
    /**
     * 调出图标选择对话框
     */
    private void onChooseIcon(){
        
        ChooseSkillIconDialog iconChoose;
        if (this.type == 0) {
            iconChoose = new ChooseSkillIconDialog(Display.getCurrent().getActiveShell(), EditorApplication.getProj().skillIcon);
        } else if (this.type == 1) {
            iconChoose = new ChooseSkillIconDialog(Display.getCurrent().getActiveShell(), EditorApplication.getProj().weaponIcon);
        } else {
            iconChoose = new ChooseSkillIconDialog(Display.getCurrent().getActiveShell(), EditorApplication.getProj().talismanIcon);
        }
        if(iconChoose.open() == IDialogConstants.OK_ID){
            int frameIndex = iconChoose.getSelectedIconIndex();
            setIcon(frameIndex);
            listener.setDirty(true);
        }
    }
    
    /**
     * 设置选中图标预览显示及文字显示
     * @param index
     */
    public void setIcon(int index){
        iconIndex = index;
        PipImage pimg;
        if (this.type == 0) {
            pimg = EditorApplication.getProj().skillIcon;
        } else if (type == 1) {
            pimg = EditorApplication.getProj().weaponIcon;
        } else {
            pimg = EditorApplication.getProj().talismanIcon;
        }
        if (currentIcon != null) {
            currentIcon.dispose();
            currentIcon = null;
        }
        if (iconIndex == -1) {
            iconPreview.setImage(null);
        } else {
            currentIcon = pimg.getImageDraw(index).createSWTImage(Display.getCurrent().getActiveShell().getDisplay(), 0);
            iconPreview.setImage(currentIcon);
        }
        textIconIndex.setText("索引:"+index);
    }
}
