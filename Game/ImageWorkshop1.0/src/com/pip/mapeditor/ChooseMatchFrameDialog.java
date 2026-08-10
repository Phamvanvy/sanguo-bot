package com.pip.mapeditor;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.mango.jni.GLUtils;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.image.PipImageData;
import com.swtdesigner.ResourceManager;

public class ChooseMatchFrameDialog extends Dialog {
    private Combo combo;
    private Button[] choiceButtons;
    private Image newImage;
    private Image[] choiceImages;
    private PipAnimateSet animates;
    private int[][] newImageData;
    private ArrayList<int[]> candidates;
    private int[] selectedFrame;
    private boolean modified;
    
    public int[] getSelectedFrame() {
        return selectedFrame;
    }
    
    public boolean isModified() {
        return modified;
    }
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseMatchFrameDialog(Shell parentShell, PipAnimateSet animateSet, int[][] newData, ArrayList<int[]> candidates) throws Exception {
        super(parentShell);
        this.animates = animateSet;
        this.newImageData = newData;
        this.candidates = candidates;
        
        PipImage tmpImg = new PipImage();
        tmpImg.addFrame(newData);
        newImage = tmpImg.getImageDraw(0).createSWTImage(parentShell.getDisplay(), 0);
        
        int count = candidates.size();
        choiceImages = new Image[count];
        for (int i = 0; i < count; i++) {
            int[] pos = candidates.get(i);
            choiceImages[i] = animateSet.getSourceImage(pos[0]).getImageDraw(pos[1]).createSWTImage(parentShell.getDisplay(), pos[2]);
        }
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("新图片：");

        final Label label = new Label(container, SWT.NONE);
        label.setLayoutData(new GridData());
        label.setImage(newImage);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("匹配图片：");
        new Label(container, SWT.NONE);

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new RowLayout());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        choiceButtons = new Button[choiceImages.length];
        for (int i = 0; i < choiceImages.length; i++) {
            choiceButtons[i] = new Button(composite, SWT.RADIO);
            choiceButtons[i].setImage(choiceImages[i]);
        }

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("操作：");

        combo = new Combo(container, SWT.READ_ONLY);
        combo.setItems(new String[] {"用新图片替换旧图片", "使用旧图片"});
        combo.select(0);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        //
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
        return new Point(489, 373);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择匹配图片");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            // 用选中的图像替换
            selectedFrame = null;
            for (int i = 0; i < choiceButtons.length; i++) {
                if (choiceButtons[i].getSelection()) {
                    selectedFrame = candidates.get(i);
                    break;
                }
            }
            if (selectedFrame == null) {
                MessageDialog.openError(getShell(), "错误", "必须选择一个图片。");
                return;
            }
            int op = combo.getSelectionIndex();
            if (op == 0) {
                PipImage targetImg = animates.getSourceImage(selectedFrame[0]);
                if (selectedFrame[2] != 0) {
                	MessageDialog.openError(getShell(), "错误", "这种翻转模式不能覆盖原图片。");
                    return;
                }
                try {
                    targetImg.addFrame(newImageData);
                } catch (Exception e) {
                    MessageDialog.openError(getShell(), "错误", "目标图片颜色数超过256了。");
                    return;
                }
                PipImageData newFrame = targetImg.getImageDatas().get(targetImg.getImgCount() - 1);
                targetImg.getImageDatas().set(selectedFrame[1], newFrame);
                targetImg.getImageDatas().remove(targetImg.getImgCount() - 1);
                modified = true;
            } else {
                modified = false;
            }
        }
        freeResources();
        super.buttonPressed(buttonId);
    }

    private void freeResources() {
    	GLUtils.unloadImage(newImage);
        for (int i = 0; i < choiceImages.length; i++) {
        	GLUtils.unloadImage(choiceImages[i]);
        }
    }
}
