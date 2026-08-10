package com.pip.image.workshop;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import com.pip.util.SWTUtils;
import com.pip.util.Utils;
import com.pipimage.image.CompressTextureOption;
import com.pipimage.image.CompressedTextureHandler;
import com.pipimage.png.PngEncoder;
import com.pipimage.utils.GZIP;

public class CompressedTextureHandlerImpl implements CompressedTextureHandler {
	public Image decodeTexture(String format, byte[] textureData, int width, int height) throws IOException {
		if (format.equals(CompressTextureOption.PVRTC_4BPP)) {
			// 把pvrtc纹理数据解压缩，保存到一个临时文件中
			File tmpPvrtcFile = File.createTempFile("_iws_", ".pvr");
			Utils.saveFileData(tmpPvrtcFile, GZIP.inflate(textureData));
			
			// 生成临时的png文件
			File tmpOutFile = File.createTempFile("_iws_", ".png");
			tmpOutFile.delete();
			
			// 调用pvrtextool命令把pvrtc文件转换为png文件
			String cmd = String.format("\"%s\" -d -f8888 -i\"%s\" -o\"%s\"", Settings.pvrTexTool, 
					tmpPvrtcFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tmpPvrtcFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists()) {
				throw new IOException("转换失败，请检查PVRTexTool，出错命令：" + cmd);
			}
			
			// 读取目标文件
			Image ret = new Image(null, tmpOutFile.getAbsolutePath());
			tmpOutFile.delete();
			return ret;
		} else if (format.equals(CompressTextureOption.PVRTC_4BPP2)) {
			// 解压缩数据包括2部分：原始图片和透明色数据
			byte[] unzipData = GZIP.inflate(textureData);
			int headerLen = ((unzipData[1] & 0xFF) << 8) | (unzipData[0] & 0xFF);
			byte[] imgData = new byte[headerLen + width * height / 2];
			byte[] transData = new byte[unzipData.length - imgData.length];
			System.arraycopy(unzipData, 0, imgData, 0, imgData.length);
			System.arraycopy(unzipData, imgData.length, transData, 0, transData.length);
			
			// 把pvrtc纹理数据解压缩，保存到一个临时文件中
			File tmpPvrtcFile = File.createTempFile("_iws_", ".pvr");
			Utils.saveFileData(tmpPvrtcFile, imgData);
			
			// 生成临时的png文件
			File tmpOutFile = File.createTempFile("_iws_", ".png");
			tmpOutFile.delete();
			
			// 调用pvrtextool命令把pvrtc文件转换为png文件
			String cmd = String.format("\"%s\" -d -f8888 -i\"%s\" -o\"%s\"", Settings.pvrTexTool, 
					tmpPvrtcFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tmpPvrtcFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists()) {
				throw new IOException("转换失败，请检查PVRTexTool，出错命令：" + cmd);
			}
			
			// 读取目标文件和透明色文件，组合起来形成最终文件
			Image srcImg = new Image(null, tmpOutFile.getAbsolutePath());
			int[][] srcPixels = SWTUtils.getImageData(srcImg, new com.pip.util.Rectangle(0, 0, srcImg.getBounds().width, srcImg.getBounds().height));
			srcImg.dispose();
			for (int i = 0; i < srcPixels.length; i++) {
				for (int j = 0; j < srcPixels[i].length; j++) {
					int a = transData[i * width + j] & 0xFF;
					srcPixels[i][j] &= 0xFFFFFF;
					srcPixels[i][j] |= a << 24;
				}
			}
			Image ret = SWTUtils.createImage(srcPixels);
			tmpOutFile.delete();
			return ret;
		} else if (format.equals(CompressTextureOption.ETC1)) {
			// 把ETC纹理数据解压缩，保存到一个临时文件中
			File tempEtcFile = File.createTempFile("_iws_", ".etc");
			Utils.saveFileData(tempEtcFile, GZIP.inflate(textureData));
			
			// 生成临时的png文件
			File tmpOutFile = File.createTempFile("_iws_", ".png");
			tmpOutFile.delete();
			
			// 调用etc1tool命令把etc文件转换为png文件
			String cmd = String.format("\"%s\" \"%s\" --decode -o \"%s\"", Settings.etcTool, 
					tempEtcFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tempEtcFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists()) {
				throw new IOException("转换失败，请检查etc1tool，出错命令：" + cmd);
			}
			
			// 读取目标文件
			Image ret = new Image(null, tmpOutFile.getAbsolutePath());
			tmpOutFile.delete();
			return ret;
		} else if (format.equals(CompressTextureOption.ETC2)) {
			// 解压缩数据包括2部分：原始图片和透明色数据
			byte[] unzipData = GZIP.inflate(textureData);
			byte[] imgData = new byte[unzipData.length / 2];
			byte[] transData = new byte[unzipData.length / 2];
			System.arraycopy(unzipData, 0, imgData, 0, imgData.length);
			System.arraycopy(unzipData, imgData.length, transData, 0, transData.length);
			
			// 把ETC纹理数据解压缩，保存到一个临时文件中
			File tempEtcFile = File.createTempFile("_iws_", ".etc");
			Utils.saveFileData(tempEtcFile, imgData);
			File tempTransFile = File.createTempFile("_iws_", ".etc");
			Utils.saveFileData(tempTransFile, transData);
			
			// 生成临时的png文件
			File tmpOutFile = File.createTempFile("_iws_", ".png");
			tmpOutFile.delete();
			File tmpTransOutFile = File.createTempFile("_iws_", ".png");
			tmpTransOutFile.delete();
			
			// 调用etc1tool命令把etc文件转换为png文件
			String cmd = String.format("\"%s\" \"%s\" --decode -o \"%s\"", Settings.etcTool, 
					tempEtcFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tempEtcFile.delete();
			cmd = String.format("\"%s\" \"%s\" --decode -o \"%s\"", Settings.etcTool, 
					tempTransFile.getAbsolutePath(), tmpTransOutFile.getAbsolutePath());
			p = Runtime.getRuntime().exec(cmd, null, tmpTransOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tempTransFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists() || !tmpTransOutFile.exists()) {
				throw new IOException("转换失败，请检查etc1tool，出错命令：" + cmd);
			}
			
			// 读取目标文件和透明色文件，组合起来形成最终文件
			Image srcImg = new Image(null, tmpOutFile.getAbsolutePath());
			Image transImg = new Image(null, tmpTransOutFile.getAbsolutePath());
			if (srcImg.getBounds().width != transImg.getBounds().width || srcImg.getBounds().height != transImg.getBounds().height) {
				throw new IOException("转换失败，原始图片和透明度图片大小不一致");
			}
			int[][] srcPixels = SWTUtils.getImageData(srcImg, new com.pip.util.Rectangle(0, 0, srcImg.getBounds().width, srcImg.getBounds().height));
			int[][] transPixels = SWTUtils.getImageData(transImg, new com.pip.util.Rectangle(0, 0, transImg.getBounds().width, transImg.getBounds().height));
			srcImg.dispose();
			transImg.dispose();
			for (int i = 0; i < srcPixels.length; i++) {
				for (int j = 0; j < srcPixels[i].length; j++) {
					int a = transPixels[i][j] & 0xFF;
					srcPixels[i][j] &= 0xFFFFFF;
					srcPixels[i][j] |= a << 24;
				}
			}
			Image ret = SWTUtils.createImage(srcPixels);
			tmpOutFile.delete();
			tmpTransOutFile.delete();
			return ret;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public byte[] encodeTexture(String format, Image image) throws IOException {
		if (format.equals(CompressTextureOption.PVRTC_4BPP)) {
			// 把image数据保存为png格式，写入一个临时文件中
			File tmpPngFile = File.createTempFile("_iws_", ".png");
			PngEncoder enc = new PngEncoder(image);
			FileOutputStream fos = new FileOutputStream(tmpPngFile);
			enc.encode32(fos, false);
			fos.close();
			
			// 生成临时的pvrtc文件
			File tmpOutFile = File.createTempFile("_iws_", ".pvr");
			tmpOutFile.delete();
			
			// 调用pvrtextool命令把png文件转换为pvrtc文件
			String cmd = String.format("\"%s\" -fpvrtc4 -i\"%s\" -o\"%s\"", Settings.pvrTexTool, 
					tmpPngFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tmpPngFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists()) {
				throw new IOException("转换失败，请检查PVRTexTool，出错命令：" + cmd);
			}
			
			// 读取生成的文件内容
			byte[] ret = Utils.loadFileData(tmpOutFile);
			tmpOutFile.delete();
			
			// 进行GZIP压缩
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            DataOutputStream zdos = new DataOutputStream(zos);
            zdos.write(ret);
            zdos.close();
            ret = bos.toByteArray();
			return ret;
		} else if (format.equals(CompressTextureOption.PVRTC_4BPP2)) {
			// 把透明度数据和原始图片数据分开成2个图片
			int[][] srcPixels = SWTUtils.getImageData(image, new com.pip.util.Rectangle(0, 0, image.getBounds().width, image.getBounds().height));
			byte[] transData = new byte[srcPixels.length * srcPixels[0].length];
			for (int i = 0; i < srcPixels.length; i++) {
				for (int j = 0; j < srcPixels[i].length; j++) {
					int a = (srcPixels[i][j] >> 24) & 0xFF;
					transData[i * srcPixels[0].length + j] = (byte)a;
				}
			}
			
			// 把image数据保存为png格式，写入一个临时文件中
			File tmpPngFile = File.createTempFile("_iws_", ".png");
			PngEncoder enc = new PngEncoder(image);
			FileOutputStream fos = new FileOutputStream(tmpPngFile);
			enc.encode32(fos, false);
			fos.close();
			
			// 生成临时的pvrtc文件
			File tmpOutFile = File.createTempFile("_iws_", ".pvr");
			tmpOutFile.delete();
			
			// 调用pvrtextool命令把png文件转换为pvrtc文件
			String cmd = String.format("\"%s\" -fpvrtc4 -i\"%s\" -o\"%s\"", Settings.pvrTexTool, 
					tmpPngFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tmpPngFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists()) {
				throw new IOException("转换失败，请检查PVRTexTool，出错命令：" + cmd);
			}
			
			// 读取生成的文件内容，2个文件拼接在一起
			byte[] srcData = Utils.loadFileData(tmpOutFile);
			tmpOutFile.delete();
			byte[] allData = new byte[srcData.length + transData.length];
			System.arraycopy(srcData, 0, allData, 0, srcData.length);
			System.arraycopy(transData, 0, allData, srcData.length, transData.length);
			
			// 进行GZIP压缩
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            DataOutputStream zdos = new DataOutputStream(zos);
            zdos.write(allData);
            zdos.close();
            byte[] ret = bos.toByteArray();
			return ret;
		} else if (format.equals(CompressTextureOption.ETC1)) {
			// 把image数据保存为png格式，写入一个临时文件中
			File tmpPngFile = File.createTempFile("_iws_", ".png");
			PngEncoder enc = new PngEncoder(image);
			FileOutputStream fos = new FileOutputStream(tmpPngFile);
			enc.encode32(fos, false);
			fos.close();
			
			// 生成临时的etc文件
			File tmpOutFile = File.createTempFile("_iws_", ".etc");
			tmpOutFile.delete();
			
			// 调用etc1tool命令把png文件转换为etc文件
			String cmd = String.format("\"%s\" \"%s\" --encode -o \"%s\"", Settings.etcTool, 
					tmpPngFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tmpPngFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists()) {
				throw new IOException("转换失败，请检查etc1tool，出错命令：" + cmd);
			}
			
			// 读取生成的文件内容
			byte[] ret = Utils.loadFileData(tmpOutFile);
			tmpOutFile.delete();
			
			// 进行GZIP压缩
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            DataOutputStream zdos = new DataOutputStream(zos);
            zdos.write(ret);
            zdos.close();
            ret = bos.toByteArray();
			return ret;
		} else if (format.equals(CompressTextureOption.ETC2)) {
			// 把透明度数据和原始图片数据分开成2个图片
			int[][] srcPixels = SWTUtils.getImageData(image, new com.pip.util.Rectangle(0, 0, image.getBounds().width, image.getBounds().height));
			int[][] transPixels = new int[srcPixels.length][srcPixels[0].length];
			for (int i = 0; i < srcPixels.length; i++) {
				for (int j = 0; j < srcPixels[i].length; j++) {
					int a = (srcPixels[i][j] >> 24) & 0xFF;
					transPixels[i][j] = (a << 24) | (a << 16) | (a << 8) | a;
				}
			}
			Image transImage = SWTUtils.createImage(transPixels);
			
			// 把image数据保存为png格式，写入一个临时文件中
			File tmpPngFile = File.createTempFile("_iws_", ".png");
			PngEncoder enc = new PngEncoder(image);
			FileOutputStream fos = new FileOutputStream(tmpPngFile);
			enc.encode32(fos, false);
			fos.close();
			File tmpTransFile = File.createTempFile("_iws_", ".png");
			enc = new PngEncoder(transImage);
			fos = new FileOutputStream(tmpTransFile);
			enc.encode32(fos, false);
			fos.close();
			transImage.dispose();
			
			// 生成临时的etc文件
			File tmpOutFile = File.createTempFile("_iws_", ".etc");
			tmpOutFile.delete();
			File tmpTransOutFile = File.createTempFile("_iws_", ".etc");
			tmpTransOutFile.delete();
			
			// 调用etc1tool命令把png文件转换为etc文件
			String cmd = String.format("\"%s\" \"%s\" --encode -o \"%s\"", Settings.etcTool, 
					tmpPngFile.getAbsolutePath(), tmpOutFile.getAbsolutePath());
			Process p = Runtime.getRuntime().exec(cmd, null, tmpOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tmpPngFile.delete();
			cmd = String.format("\"%s\" \"%s\" --encode -o \"%s\"", Settings.etcTool, 
					tmpTransFile.getAbsolutePath(), tmpTransOutFile.getAbsolutePath());
			p = Runtime.getRuntime().exec(cmd, null, tmpTransOutFile.getParentFile());
			try {
				p.waitFor();
			} catch (Exception e) {
			}
			tmpTransFile.delete();
			
			// 检查目标文件，不存在表示失败
			if (!tmpOutFile.exists() || !tmpTransOutFile.exists()) {
				throw new IOException("转换失败，请检查etc1tool，出错命令：" + cmd);
			}
			
			// 读取生成的文件内容，2个文件拼接在一起
			byte[] srcData = Utils.loadFileData(tmpOutFile);
			tmpOutFile.delete();
			byte[] transData = Utils.loadFileData(tmpTransOutFile);
			tmpTransOutFile.delete();
			if (srcData.length != transData.length) {
				throw new IOException("转换失败，原始图片和透明度图片大小不一致");
			}
			byte[] allData = new byte[srcData.length + transData.length];
			System.arraycopy(srcData, 0, allData, 0, srcData.length);
			System.arraycopy(transData, 0, allData, srcData.length, transData.length);
			
			// 进行GZIP压缩
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            DataOutputStream zdos = new DataOutputStream(zos);
            zdos.write(allData);
            zdos.close();
            byte[] ret = bos.toByteArray();
			return ret;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public void writeInt(DataOutputStream dos, int value){
		try {
			dos.write((value & 0x000000FF));
			dos.write((value & 0x0000FF00) >> 8);
			dos.write((value & 0x00FF0000) >> 16);
			dos.write((value & 0xFF000000) >> 24);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
