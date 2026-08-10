package com.pip.image.workshop.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.pip.image.workshop.GenericChooseDialog;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.utils.ImageUtil;

public class ImageFrameReplaceDialog extends Dialog implements ImageFrameReplaceViewer.EventListener {
	protected List<Integer> targetFrames = new ArrayList<Integer>();
	protected ImageFrameReplaceViewer[] viewers;
	protected PipAnimateSet animateSet;
	protected int imageID;
	protected List<int[][]> frameData;
	protected Image[] frameImages;
	protected int[][] replaces;
	protected double[][] replaceMatchRate;
	
	public int opmode;				 // 0 - 取消，1 - 合并一个图块，2 - 合并多个图块

	// 合并多个图块的参数
	public List<Integer> replaceSourceFrames;		// 被合并的图块
	public List<Integer> replaceTargetFrames;	 	// 替换成这个图块
	public List<Integer> adjustXs;					// 替换后X方向调整值
	public List<Integer> adjustYs;					// 替换后Y方向调整值
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public ImageFrameReplaceDialog(Shell parentShell, PipAnimateSet animateSet,
			int imageID, List<int[][]> frameData, Image[] frames, int[][] rs, double[][] rmrs) {
		super(parentShell);
		this.animateSet = animateSet;
		this.imageID = imageID;
		this.frameData = frameData;
		this.frameImages = frames;
		this.replaces = rs;
		this.replaceMatchRate = rmrs;
		for (int i = 0; i < frames.length; i++) {
			if (this.replaces[i].length > 0) {
				targetFrames.add(i);
			}
		}
		if (targetFrames.size() == 0) {
			throw new IllegalArgumentException();
		}

		// 按匹配度排序
		int[] arr = new int[targetFrames.size()];
		for (int i = 0; i < targetFrames.size(); i++) {
			arr[i] = targetFrames.get(i);
		}
		for (int i = 0; i < arr.length - 1; i++) {
			for (int j = i + 1; j < arr.length - 1; j++) {
				double r1 = replaceMatchRate[arr[i]][0];
				double r2 = replaceMatchRate[arr[j]][0];
				if (r1 < r2) {
					int tmp = arr[i];
					arr[i] = arr[j];
					arr[j] = tmp;
				}
			}
		}

		// 如果帧数过多，去掉多余的，只留50帧
		if (arr.length > 50) {
			int[] newarr = new int[50];
			System.arraycopy(arr, 0, newarr, 0, 50);
			arr = newarr;
		}

		targetFrames.clear();
		for (int i = 0; i < arr.length; i++) {
			targetFrames.add(arr[i]);
		}
	}

	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		final ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLocation(0, 0);
		composite.setSize(445, 300);
		scrolledComposite.setContent(composite);

		viewers = new ImageFrameReplaceViewer[targetFrames.size()];
		for (int i = 0; i < targetFrames.size(); i++) {
			int tf = targetFrames.get(i);
			viewers[i] = new ImageFrameReplaceViewer(composite, SWT.NONE, tf, frameImages, replaces[tf], replaceMatchRate[tf]);
			viewers[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			viewers[i].setListener(this);
		}
		composite.pack();

		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "批量优化", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "关闭", true);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1084, 779);
	}
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("图块合并");
	}
	
	// 点击某一个替换预览按钮
	public void onPreviewReplace(ImageFrameReplaceViewer source, int sourceFrame, int targetFrame) {
		int[] off = new int[2];
		ImageUtil.compareFrame(frameData.get(sourceFrame), frameData.get(targetFrame), off);
		ImageFrameReplacePreviewDialog dlg = new ImageFrameReplacePreviewDialog(getShell(), animateSet,
				imageID, sourceFrame, targetFrame, off[0], off[1]);
		if (dlg.open() == Dialog.OK) {
			opmode = 1;
			replaceSourceFrames = new ArrayList<Integer>();
			replaceTargetFrames = new ArrayList<Integer>();
			adjustXs = new ArrayList<Integer>();
			adjustYs = new ArrayList<Integer>();
			replaceSourceFrames.add(sourceFrame);
			replaceTargetFrames.add(targetFrame);
			adjustXs.add(off[0]);
			adjustYs.add(off[1]);
			close();
		}
	}
	
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// 批量优化，选择容错百分比
			GenericChooseDialog dlg = new GenericChooseDialog(getShell(), "容错选项", "请选择最低匹配度：", 
				new String[] { "99.99%", "99.9%", "99%", "98%", "97%", "96%", "95%", "自定义..." });
			if (dlg.open() == Dialog.OK) { 
				int index = dlg.getSelectionIndex();
				double[] paramArr = new double[] { 0.9999, 0.999, 0.99, 0.98, 0.97, 0.96, 0.95, 0.0 };
				double minRate = paramArr[index];
				if (minRate == 0.0) {
					// 选择了自定义
					InputDialog idlg = new InputDialog(getShell(), "输入", "请输入最低匹配度（0-100）：", "95", new IInputValidator() {
						public String isValid(String value) {
							try {
								double dv = Double.parseDouble(value);
								if (dv < 0 || dv >= 100) {
									return "请输入0-100的数字。";
								}
							} catch (Exception e) {
								return "请输入正确的数字。";
							}
							return null;
						}
					});
					if (idlg.open() != Dialog.OK) {
						return;
					}
					minRate = Double.parseDouble(idlg.getValue()) / 100;
				}
				
				// 找出所有存在相似度超过指定阈值的图块替换方案
				replaceSourceFrames = new ArrayList<Integer>();
				replaceTargetFrames = new ArrayList<Integer>();
				adjustXs = new ArrayList<Integer>();
				adjustYs = new ArrayList<Integer>();
				Set<Integer> replacedFrames = new HashSet<Integer>();
				for (int i = 0; i < targetFrames.size(); i++) {
					int fr = targetFrames.get(i);
					int[] repIDs = replaces[fr];
					double[] repRates = replaceMatchRate[fr];
					for (int j = 0; j < repIDs.length; j++) {
						if (replacedFrames.contains(repIDs[j])) {
							continue;
						}
						if (repRates[j] >= minRate) {
							replaceSourceFrames.add(fr);
							replaceTargetFrames.add(repIDs[j]);
							int[] off = new int[2];
							ImageUtil.compareFrame(frameData.get(fr), frameData.get(repIDs[j]), off);
							adjustXs.add(off[0]);
							adjustYs.add(off[1]);
							replacedFrames.add(fr);
							break;
						}
					}
				}
				if (replaceSourceFrames.size() == 0) {
					MessageDialog.openError(getShell(), "错误", "没有符合条件可以被合并的图块。");
					return;
				}
				opmode = 2;
			} else {
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
}
