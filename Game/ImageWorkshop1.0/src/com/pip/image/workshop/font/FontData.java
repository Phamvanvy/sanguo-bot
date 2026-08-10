package com.pip.image.workshop.font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pip.util.Utils;

public class FontData {
	public int width;
	public int height;
	
	// key是字符ID，value是字符的像素值，每个像素一个byte，0表示透明，1表示颜色；每个byte[]的长度应该等于width*height
	public Map<Integer, byte[]> charPixels = new HashMap<Integer, byte[]>();;
	
	/**
	 * 设置一个字符的像素。
	 * @param ch
	 * @param pixels 白底黑色的字符图像，宽度高度必须等于width, height
	 * @param valve 透明度阈值，大于这个阈值认为是不透明
	 */
	public void addChar(int ch, int[] pixels, int valve) {
		if (pixels.length != width * height) {
			throw new IllegalArgumentException();
		}
		byte[] pdata = new byte[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			int p = pixels[i] & 0xFFFFFF;
			if (p == 0x000000) {
				// 黑色像素
				pdata[i] = (byte)1;
			} else if (p == 0xFFFFFF) {
				// 白色像素
				pdata[i] = (byte)0;
			} else {
				// 中间像素，计算透明度，越接近白色表示透明度越高
				int r = (p >> 16) & 0xFF;
				int g = (p >> 8) & 0xFF;
				int b = p & 0xFF;
				
				// 用简略算法计算亮度，亮度小于50%的算作黑色点，否则算作白色点
				int l = (r + g + b) / 3;
				l = 255 - l;
				if (l > valve) {
					pdata[i] = (byte)1;
				} else {
					pdata[i] = (byte)0;
				}
			}
		}
		charPixels.put(ch, pdata);
	}
	
	public void load(File dir) throws IOException {
		File[] files = dir.listFiles();
		for (File f : files) {
			if (!f.isFile()) {
				continue;
			}
			String n = f.getName();
			if (!n.startsWith("font") || !n.endsWith(".data")) {
				continue;
			}
			
			// 名字格式为font_0x1E3F_0x2642_h14_w16.data，从名字中读取字体宽度，高度和适用范围
			String[] secs = n.substring(0, n.lastIndexOf('.')).split("_");
			int start = Integer.parseInt(secs[1].substring(2), 16);
			int end = Integer.parseInt(secs[2].substring(2), 16);
			width = Integer.parseInt(secs[4].substring(1));
			height = Integer.parseInt(secs[3].substring(1));
			int lineBytes = (width + 7) / 8;
			int charBytes = lineBytes * height;
			int charCount = end - start + 1;
			int addBytes = width <= 16 ? 1 : 2;
			int expectBytes = (charBytes + addBytes) * charCount;
			if (expectBytes != f.length()) {
				throw new IOException("无效的字体文件，期望" + expectBytes + "字节，实际" + f.length() + "字节。");
			}
			
			byte[] fdata = Utils.loadFileData(f);
			int pos = 0;
			byte[] cps = new byte[charBytes];
			for (int ch = start; ch <= end; ch++) {
				System.arraycopy(fdata, pos + addBytes, cps, 0, charBytes);
				pos += addBytes + charBytes;
				charPixels.put(ch, convertPixels(cps));
			}
		}
	}
	
	/*
	 * 把按位存储的字节数据展开成一像素一字节的格式。
	 */
	private byte[] convertPixels(byte[] cps) {
		int lineBytes = (width + 7) / 8;
		byte[] ret = new byte[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int index1 = y * lineBytes + x / 8;
				int index2 = x % 8;
				byte p = cps[index1];
				ret[y * width + x] = (byte)((p >> (7 - index2)) & 0x01);
			}
		}
		return ret;
	}
	
	public void save(File dir, int writeWidth, int writeHeight, int whiteWidth, int yOffset, int charsetType) throws IOException {
		if (charsetType == 2) {
			// Vietnam charset
			saveSection(dir, writeWidth, writeHeight, 0x0020, 0x0A00, whiteWidth, yOffset);
		} else {
			// other
			saveSection(dir, writeWidth, writeHeight, 0x0020, 0x0451, whiteWidth, yOffset);
		}
		saveSection(dir, writeWidth, writeHeight, 0x1E3F, 0x2642, whiteWidth, yOffset);
		saveSection(dir, writeWidth, writeHeight, 0x2E81, 0x9FA5, whiteWidth, yOffset);
		if (charsetType == 1) {
			// korean charset
			saveSection(dir, writeWidth, writeHeight, 0xAC00, 0xE864, whiteWidth, yOffset);
		} else {
			// normal charset
			saveSection(dir, writeWidth, writeHeight, 0xE76C, 0xE864, whiteWidth, yOffset);
		}
		saveSection(dir, writeWidth, writeHeight, 0xF92C, 0xFA29, whiteWidth, yOffset);
		saveSection(dir, writeWidth, writeHeight, 0xFE30, 0xFFE5, whiteWidth, yOffset);
	}

	private void saveSection(File dir, int writeWidth, int writeHeight, int startChar, int stopChar, int whiteWidth, int yOffset) throws IOException {
		String fname = String.format("font_0x%04X_0x%04X_h%d_w%d.data", startChar, stopChar, writeHeight, writeWidth);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int ch = startChar; ch <= stopChar; ch++) {
			// 先写出这个字符数据的第一个非空列号，和最后一个非空列号，如果字体宽度<=16，2个列号拼成1个字节，否则各用一个字节
			byte[] charData = charPixels.get(ch);
			int beginCol = findBeginColumn(charData);
			int endCol = findEndColumn(charData);
			if (beginCol == 0 && endCol == 0) {
				endCol = whiteWidth - 1;
			}
			if (writeWidth <= 16) {
				bos.write((beginCol << 4) | (endCol));
			} else {
				bos.write(beginCol);
				bos.write(endCol);
			}
			
			// 把字符数据，每8位拼成一个字节写出，每行按字节对齐
			int lineBytes = (width + 7) / 8;
			int writeLineBytes = (writeWidth + 7) / 8;
			for (int y = yOffset; y < writeHeight + yOffset; y++) {
				if (y >= height) {
					// 超出字符实际宽度的行，写空
					for (int t = 0; t < writeLineBytes; t++) {
						bos.write(0);
					}
					continue;
				}
				for (int x = 0; x < width; x += 8) {
					int t = 0;
					for (int xx = 0; xx < 8; xx++) {
						if (xx + x >= width) {
							break;
						}
						if (charData[y * width + x + xx] != 0) {
							t |= 1 << (7 - xx);
						}
					}
					bos.write(t);
				}
				
				// 如果写出宽度和实际宽度相比，多出了一个字节，写空
				for (int t = lineBytes; t < writeLineBytes; t++) {
					bos.write(0);
				}
			}
		}
		Utils.saveFileData(new File(dir, fname), bos.toByteArray());
	}
	
	private int findBeginColumn(byte[] data) {
		for (int c = 0; c < width; c++) {
			boolean empty = true;
			for (int r = 0; r < height; r++) {
				if (data[r * width + c] != 0) {
					empty = false;
					break;
				}
			}
			if (!empty) {
				return c;
			}
		}
		return 0;
	}
	
	private int findEndColumn(byte[] data) {
		for (int c = width - 1; c >= 0; c--) {
			boolean empty = true;
			for (int r = 0; r < height; r++) {
				if (data[r * width + c] != 0) {
					empty = false;
					break;
				}
			}
			if (!empty) {
				return c;
			}
		}
		return 0;
	}
	
	public static void main(String[] args) throws IOException {
		FontData fd = new FontData();
		fd.load(new File("D:/workspace/sanguo/Sanguo1.0-C/dev/iPhone/Sanguo/en_US/NewUI/iPhone/PearlinPalmHeroRes/iOSNewUI/fontdata"));
	}
}
