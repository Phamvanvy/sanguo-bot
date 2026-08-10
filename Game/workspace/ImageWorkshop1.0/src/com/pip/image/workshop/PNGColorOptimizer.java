package com.pip.image.workshop;

import java.io.*;
import java.util.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.pip.image.workshop.editor.ImageViewer;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;
import com.pipimage.png.PngEncoder;

/**
 * 这个类可以把多个PNG文件合并到一个文件中，用系统编辑器打开，供图片优化。
 * @author lighthu
 */
public class PNGColorOptimizer implements IFileModificationListener {
	protected List<File> sourceFiles;
	protected List<Rectangle> sourceRects;
	protected File tempFile;
	protected int totalWidth;
	protected int maxHeight;
	protected Shell shell;
	
	public void run(Shell shell) {
		this.shell = shell;
		
		// 选择文件并载入
		FileDialog dlg = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		dlg.setFilterExtensions(new String[] { "*.png", "*.*" });
		dlg.setFilterNames(new String[] { "PNG图片文件(*.png)", "所有文件(*.*)" });
		if (dlg.open() == null) {
			return;
		}
		String dir = dlg.getFilterPath();
		String[] fileNames = dlg.getFileNames();
		sourceFiles = new ArrayList<File>();
		List<Image> sourceImages  = new ArrayList<Image>();
		for (int i = 0; i < fileNames.length; i++) {
			File theFile = new File(dir, fileNames[i]);
			try {
				Image newImg = new Image(shell.getDisplay(), theFile.getAbsolutePath());
				sourceFiles.add(theFile);
				sourceImages.add(newImg);
			} catch (Exception e) {
			}
		}
		
		// 把所有的PNG文件拼接成一个大的PNG文件
		sourceRects = new ArrayList<Rectangle>();
		int x = 0;
		totalWidth = 0;
		maxHeight = 0;
		for (Image img : sourceImages) {
			Rectangle size = img.getBounds();
			sourceRects.add(new Rectangle(x, 0, size.width, size.height));
			totalWidth += size.width;
			x += size.width;
			if (size.height > maxHeight) {
				maxHeight = size.height;
			}
		}
		int[][] tempData = new int[maxHeight][totalWidth];
		for (int i = 0; i < sourceImages.size(); i++) {
			Image img = sourceImages.get(i);
			int[][] imgData = ImageViewer.getImageData(img, img.getBounds());
			Rectangle rect = sourceRects.get(i);
			for (int j = 0; j < imgData.length; j++) {
				System.arraycopy(imgData[j], 0, tempData[j + rect.y], rect.x, imgData[j].length);
			}
		}
		try {
			ImageData id = rgb2image(tempData);
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { id };
			tempFile = new File(dir, "iws_temp.png");
			loader.save(tempFile.getAbsolutePath(), SWT.IMAGE_PNG);

			// 打开编辑器编辑文件
			tempFile.deleteOnExit();
			Runtime.getRuntime().exec(new String[] {
				Settings.imageEditor,
				java.text.MessageFormat.format(Settings.imageEditorArg, tempFile.getAbsolutePath())
			});
			FileWatcher.watch(tempFile, this);
		} catch (Exception e) {
			MessageDialog.openError(shell, "错误", e.toString());
		}
	}
	
	private ImageData rgb2image(int[][] rgb) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(rgb.length * rgb[0].length * 4);
			ByteArrayOutputStream bos2 = new ByteArrayOutputStream(rgb.length * rgb[0].length);
			DataOutputStream dos = new DataOutputStream(bos);
			for (int i = 0; i < rgb.length; i++) {
				for (int j = 0; j < rgb[i].length; j++) {
					dos.writeInt(rgb[i][j]);
					bos2.write((rgb[i][j] >> 24) & 0xFF);
				}
			}
			dos.flush();
			PaletteData pal = new PaletteData(0xFF0000, 0xFF00, 0xFF);
			ImageData id = new ImageData(rgb[0].length, rgb.length, 32, pal, 4, bos.toByteArray());
			id.alphaData = bos2.toByteArray();
			return id;
		} catch (Exception e) {
			return null;
		}
	}

	public void fileModified(File f) {
		if (!f.equals(tempFile)) {
			return;
		}
		try {
			Image newImg = new Image(shell.getDisplay(), f.getAbsolutePath());
			if (newImg.getBounds().width != totalWidth || newImg.getBounds().height != maxHeight) {
				throw new Exception("图片尺寸改变了");
			}
			
			for (int i = 0; i < sourceFiles.size(); i++) {
				int[][] frameData = ImageViewer.getImageData(newImg, sourceRects.get(i));
				ImageData id = rgb2image(frameData);
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] { id };
				loader.save(sourceFiles.get(i).getAbsolutePath(), SWT.IMAGE_PNG);
			}
		} catch (Exception e) {
			shell.getDisplay().asyncExec(new ErrorShower(e.toString()));
		}
	}
	
	private class ErrorShower implements Runnable {
		private String msg;
		
		public ErrorShower(String m) {
			msg = m;
		}
		
		public void run() {
			MessageDialog.openError(shell, "错误", msg);
		}
	}
}
