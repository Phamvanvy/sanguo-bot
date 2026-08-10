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

import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.skill.ChooseSkillIconDialog;
import com.pipimage.image.PipImage;

public class IconChooser extends Composite {
    
    private int iconIndex = -1;
    private int iconImage = 0;
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
        gridLayout.numColumns = 4;
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
        
        final Button chooseButton2 = new Button(this, SWT.NONE);
        chooseButton2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        chooseButton2.setText("...");
        chooseButton2.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                onChooseIcon2();
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
    
    public int getIconImage(){
        return iconImage;
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
            setIcon(frameIndex, Item.ICON_IMAGE_ABILITY);
            listener.setDirty(true);
        }
    }
    
    /**
     * 调出图标选择对话框 新增图片2012-05-25
     */
    private void onChooseIcon2(){
        ChooseSkillIconDialog iconChoose;
        if (this.type == 0) {
            iconChoose = new ChooseSkillIconDialog(Display.getCurrent().getActiveShell(), EditorApplication.getProj().skillIcon2);
        } else if (this.type == 1) {
            iconChoose = new ChooseSkillIconDialog(Display.getCurrent().getActiveShell(), EditorApplication.getProj().weaponIcon);
        } else {
            iconChoose = new ChooseSkillIconDialog(Display.getCurrent().getActiveShell(), EditorApplication.getProj().talismanIcon);
        }
        if(iconChoose.open() == IDialogConstants.OK_ID){
            int frameIndex = iconChoose.getSelectedIconIndex();
            setIcon(frameIndex, Item.ICON_IMAGE_ABILITY2);
            listener.setDirty(true);
        }
    }
    
    /**
     * 设置选中图标预览显示及文字显示
     * @param index
     */
    public void setIcon(int index, int image){
        iconIndex = index;
        iconImage = image;
        PipImage pimg;
        if (this.type == 0) {
            if(iconImage == Item.ICON_IMAGE_ABILITY2){
                pimg = EditorApplication.getProj().skillIcon2;
            }else{
                pimg = EditorApplication.getProj().skillIcon;
            }
            
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
