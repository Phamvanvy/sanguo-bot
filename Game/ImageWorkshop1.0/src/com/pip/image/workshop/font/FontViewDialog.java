package com.pip.image.workshop.font;

import static java.lang.Integer.parseInt;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.pip.util.Rectangle;
import com.pip.util.SWTUtils;
import com.swtdesigner.SWTResourceManager;

public class FontViewDialog extends Dialog {
	private FontViewer fontViewer;
	private FontData editingFont;
	private Label labelInfo;
	
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public FontViewDialog(Shell parentShell) {
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
		gridLayout.numColumns = 4;
		gridLayout.horizontalSpacing = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.marginTop = 2;
		gridLayout.marginHeight = 2;
		container.setLayout(gridLayout);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		composite.setLayout(new FillLayout());
		
		fontViewer = new FontViewer(composite, SWT.NONE);

		final Button buttonLoad = new Button(container, SWT.NONE);
		buttonLoad.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.OPEN);
				dlg.setMessage("选择字体目录");
				String path = dlg.open();
				if (path == null) {
					return;
				}
				FontData fdata = new FontData();
				try {
					fdata.load(new File(path));
					fontViewer.setInput(fdata);
					editingFont = fdata;
					labelInfo.setText("字体宽度：" + fdata.width + ", 字体高度：" + fdata.height);
				} catch (Exception e1) {
					SWTUtils.showError(getShell(), "错误", e1);
				}
			}
		});
		buttonLoad.setText("加载字体...");

		final Button buttonMake = new Button(container, SWT.NONE);
		buttonMake.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				try {
					// 选择字体
					FontDialog fdlg = new FontDialog(getShell());
					org.eclipse.swt.graphics.FontData fontData = fdlg.open();
					if (fontData == null) {
						return;
					}
					
					// 输入透明度阈值
					InputDialog idlg = new InputDialog(getShell(), "透明度阈值", "请输入透明度阈值(0-255)，0表示所有半透明色都作为不透明处理", "128", new IInputValidator() {
						public String isValid(String value) {
							try {
								int v = Integer.parseInt(value);
								if (v < 0 || v > 255) {
									throw new Exception();
								}
							} catch (Exception e) {
								return "请输入0-255的整数。";
							}
							return null;
						}
					});
					if (idlg.open() != InputDialog.OK) {
						return;
					}
					int valve = Integer.parseInt(idlg.getValue());
					
					FontData bitmapFont = new FontData();
					
					// 创建临时图片
					Font font = new Font(getShell().getDisplay(), fontData);
					Image bufImg = new Image(getShell().getDisplay(), 100, 100);
					
					// 取得字符参数
					GC gc = new GC(bufImg);
					gc.setFont(font);
					bitmapFont.height = gc.getFontMetrics().getHeight();
					bitmapFont.width = gc.textExtent("国").x;
					gc.dispose();
					
					// 逐个生成字符数据
					for (int ch = 0; ch < 65536; ch++) {
						if ((ch >= 0x0020 && ch <= 0x0451) ||
							(ch >= 0x1E3F && ch <= 0x2642) ||
							(ch >= 0x2E81 && ch <= 0x9FA5) ||
							(ch >= 0xAC00 && ch <= 0xE864) ||
							(ch >= 0xF92C && ch <= 0xFA29) ||
							(ch >= 0xFE30 && ch <= 0xFFE5)) {
							int[] pixels = makeCharGlyph(bufImg, font, ch, bitmapFont.width, bitmapFont.height);
							bitmapFont.addChar(ch, pixels, valve);
						}
					}
					bufImg.dispose();
					font.dispose();
					fontViewer.setInput(bitmapFont);
					editingFont = bitmapFont;
					labelInfo.setText("字体宽度：" + bitmapFont.width + ", 字体高度：" + bitmapFont.height);
				} catch (Exception e1) {
					SWTUtils.showError(getShell(), "错误", e1);
				}
			}
		});
		buttonMake.setText("制作字体...");

		labelInfo = new Label(container, SWT.NONE);
		labelInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Button saveButton = new Button(container, SWT.NONE);
		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				onSaveFont();
			}
		});
		saveButton.setText("保存字体...");
		
		return container;
	}
	
	private int[] makeCharGlyph(Image bufImg, Font font, int ch, int width, int height) {
		// 图片填充白色底
		GC gc = new GC(bufImg);
		gc.setFont(font);
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 0, width, height);
		
		String str = String.valueOf((char)ch);
		gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		gc.drawString(str, 0, 0/*gc.getFontMetrics().getAscent()*/, true);
		
		gc.dispose();
		
		ImageData imageData = bufImg.getImageData();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			imageData.getPixels(0, y, width, pixels, y * width);
		}
		for (int i = 0; i < pixels.length; i++) {
			int clr = pixels[i];
    		int r = ((clr & imageData.palette.redMask) >> -(imageData.palette.redShift)) & 0xFF;
    		int g = ((clr & imageData.palette.greenMask) >> -(imageData.palette.greenShift)) & 0xFF;
			int b = ((clr & imageData.palette.blueMask) >> -(imageData.palette.blueShift)) & 0xFF;
			pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
		}
		return pixels;
	}

	/**
	 * Create contents of the button bar
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
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(961, 676);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("字体工具");
	}

	private void onSaveFont() {
		if (editingFont == null) {
			return;
		}
		
		SaveFontOptionDialog dlg1 = new SaveFontOptionDialog(getShell(), editingFont.width, editingFont.height);
		if (dlg1.open() != Dialog.OK) {
			return;
		}
		
		// 选择输出目录
		DirectoryDialog dlg = new DirectoryDialog(getShell(), SWT.SAVE);
		dlg.setMessage("选择保存目录");
		String path = dlg.open();
		if (path == null) {
			return;
		}
		
		try {
			editingFont.save(new File(path), dlg1.saveWidth, dlg1.saveHeight, dlg1.whiteWidth, dlg1.yOffset, dlg1.charsetType);
			MessageDialog.openInformation(getShell(), "成功", "字体文件保存成功。");
		} catch (Exception e) {
			SWTUtils.showError(getShell(), "错误", e);
		}
	}
}
