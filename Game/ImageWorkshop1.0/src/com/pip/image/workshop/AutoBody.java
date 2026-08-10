package com.pip.image.workshop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.image.workshop.editor.BodyDef;
import com.pip.image.workshop.editor.ImageViewer;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;

/**
 * 自动生成素体和装备文件的工具。
 * @author lighthu
 */
public class AutoBody {
	public static void main(String[] args) throws Exception {
		if (true) {
			String file = "D:\\temp2\\小版女1917\\201210091917\\0\\s-leg l z\\s-9_long rang attack_s-leg l z_00066.png";
			int[] pos1 = findRefPoint(new File(file));
			file = "D:\\temp2\\小版女1917\\201210091917\\0\\s-head\\s-9_long rang attack_s-head_00069.png";
			int[] pos2 = findRefPoint(new File(file));
			System.out.println(pos1[0] + "," + pos1[1]);
			System.out.println(pos2[0] + "," + pos2[1]);
			return;
		}
		File rootDir = new File("C:\\Users\\lighthu\\Desktop\\测试自动挂接点工具\\nvren3");
		int frameCount = rootDir.listFiles()[0].listFiles()[0].listFiles().length;
		String[] partNames = rootDir.list();
		
		// 创建一个空的动画文件，包含这些帧和一个动画序列
		PipAnimateSet pas = new PipAnimateSet();
		for (int i = 0; i < frameCount; i++) {
			pas.addFrame(String.valueOf(i));
		}
		PipAnimate pa = pas.addAnimate("0");
		for (int i = 0; i < frameCount; i++) {
			pa.addFrame(i);
		}
		pas.save(new File(rootDir, "atk.cts"), true);
		
		// 创建一个素体文件对应次动画文件，并从参考图片中获取参考点坐标
		BodyDef body = new BodyDef();
		body.ctsFile = "atk.cts";
		for (int i = 0; i < partNames.length; i++) {
			PipAni4AniFramePiece hp = new PipAni4AniFramePiece(null);
			hp.name = partNames[i];
			hp.setImageID(i);
			body.hooks.add(hp);
			body.curHooksIdSet.add(i);
			body.id2name.put(i, hp.name);
		}
		body.embedHookPieces = new PipAni4AniFramePiece[frameCount][];
		for (int i = 0; i < frameCount; i++) {
			body.embedHookPieces[i] = new PipAni4AniFramePiece[partNames.length];
			for (int j = 0; j < partNames.length; j++) {
				PipAni4AniFramePiece hookPiece = new PipAni4AniFramePiece(null);
				hookPiece.setImageID(j);
				int[] xy = findHookPos(new File(rootDir, partNames[j]), i);
				hookPiece.setRealDx(xy[0]);
				hookPiece.setRealDy(xy[1]);
				hookPiece.setFrame(j);
				hookPiece.name = partNames[j];
				body.embedHookPieces[i][j] = hookPiece;
			}
		}
		body.embedHookPieces(pas);
		body.save(pas, new File(rootDir, "atk.hk").getAbsolutePath());
		
		// 生成每个部件的pip,cts和eqp文件
		for (int i = 0; i < partNames.length; i++) {
			generateEqp(new File(rootDir, partNames[i]), partNames[i], i);
		}
	}
	
	private static int[] findHookPos(File partDir, int index) throws Exception {
		String[] fnames = new File(partDir, "qiu").list();
		Arrays.sort(fnames);
		File picFile = new File(partDir, "qiu/" + fnames[index]);
		Image newImg = new Image(null, picFile.getAbsolutePath());
		int[][] rawData = ImageViewer.getImageData(newImg, new Rectangle(0, 0, newImg.getBounds().width, newImg.getBounds().height));
		newImg.dispose();
		
		// 最接近蓝色的点是挂接点位置，最接近红色的点是基准点位置
		int[] hookPos = findColorPoint(rawData, 0xFF0000FF);
		int[] basePos = findColorPoint(rawData, 0xFFFF0000);
		
		return new int[] { hookPos[0] - basePos[0], hookPos[1] - basePos[1] };
	}
	
	private static void generateEqp(File dir, String name, int hookID) throws Exception {
		// 首先提取出所有的pip图素
		String[] fnames = new File(dir, name).list();
		Arrays.sort(fnames);
		PipImage img = new PipImage();
		img.setSupportMoreColors(true);
		int[][] fetchPos = new int[fnames.length][];
		for (int i = 0; i < fnames.length; i++) {
			fetchPos[i] = fetchFrameImage(new File(dir, name + "/" + fnames[i]), img);
		}
		img.save(new File(dir, "equ.pip"));
		
		// 计算每个图块相对于挂接点的位置，生成cts
		PipAnimateSet pas = new PipAnimateSet();
		pas.addSourceFile(new File(dir, "equ.pip").getAbsolutePath());
		for (int i = 0; i < fnames.length; i++) {
			File picFile = new File(dir, "qiu/" + fnames[i]);
			
			// 最接近蓝色的点是挂接点位置，最接近红色的点是基准点位置
			int[] hookPos = findHookPoint(picFile);
			
			PipAnimateFrame frame = pas.addFrame(String.valueOf(i));
			PipAnimateFramePiece piece = frame.addPiece(0, i);
			piece.setDx(fetchPos[i][0] - hookPos[0]);
			piece.setDy(fetchPos[i][1] - hookPos[1]);
			
			PipAnimate pa = pas.addAnimate(String.valueOf(i));
			pa.addFrame(i);
		}
		pas.save(new File(dir, "equ.cts"), true);
		
		// 生成eqp文件
		EquipHookMap eqp = new EquipHookMap();
		eqp.setEquipCtsName("equ.cts");
		eqp.addHookFileName("../atk.hk");
		eqp.addHookId(hookID);
		byte[] map = new byte[fnames.length];
		for (int i = 0; i < fnames.length; i++) {
			map[i] = (byte)i;
		}
		eqp.frame2equipAniIdxes.add(map);
		PipAnimateSet hkCts = new PipAnimateSet();
		hkCts.load(new File(dir.getParentFile(), "atk.cts"));
		BodyDef bodyDef = new BodyDef();
		bodyDef.loadHooks(new File(dir.getParentFile(), "atk.hk"));
		bodyDef.embedHookPieces(hkCts);
		List<PipAnimateSet> list = new ArrayList<PipAnimateSet>();
		list.add(hkCts);
		eqp.doEquip(hkCts, pas, 0);
		eqp.save(new File(dir, "equ.eqp").getAbsolutePath(), list, pas);
	}
	
	/**
	 * 从一个图片中搜索装备挂接点的位置（纯蓝）。
	 * @param data
	 * @return
	 */
	public static int[] findHookPoint(int[][] data) {
		return findColorPoint(data, 0xFF0000FF);
	}
	
	/**
	 * 从一个图片中搜索装备挂接点相对于世界原点的坐标。
	 * @param data
	 * @return
	 */
	public static int[] getHookPoint(File picFile) {
		Image newImg = new Image(null, picFile.getAbsolutePath());
		int[][] rawData = ImageViewer.getImageData(newImg, new Rectangle(0, 0, newImg.getBounds().width, newImg.getBounds().height));
		newImg.dispose();
		int[] hookPos = findHookPoint(rawData);
		int[] refPos = findRefPoint(rawData);
		hookPos[0] -= refPos[0];
		hookPos[1] -= refPos[1];
		return hookPos;
	}
	
	/**
	 * 从一个图片中搜索装备挂接点的位置（纯蓝）。
	 * @param data
	 * @return
	 */
	public static int[] findHookPoint(File picFile) {
		Image newImg = new Image(null, picFile.getAbsolutePath());
		int[][] rawData = ImageViewer.getImageData(newImg, new Rectangle(0, 0, newImg.getBounds().width, newImg.getBounds().height));
		newImg.dispose();
		return findHookPoint(rawData);
	}
	
	/**
	 * 从一个图片中搜索世界原点的位置（纯红）。
	 * @param data
	 * @return
	 */
	public static int[] findRefPoint(int[][] data) {
		return findColorPoint(data, 0xFFFF0000);
	}
	
	/**
	 * 从一个图片中搜索装备挂接点的位置（纯蓝）。
	 * @param data
	 * @return
	 */
	public static int[] findRefPoint(File picFile) {
		Image newImg = new Image(null, picFile.getAbsolutePath());
		int[][] rawData = ImageViewer.getImageData(newImg, new Rectangle(0, 0, newImg.getBounds().width, newImg.getBounds().height));
		newImg.dispose();
		return findRefPoint(rawData);
	}
	
	/*
	 * 从一个图片中搜索最接近某个颜色的点
	 * @param data
	 * @param color
	 * @return
	 */
//	private static int[] findColorPoint(int[][] data, int color) {
//		int a = (color >> 24) & 0xFF;
//		int r = (color >> 16) & 0xFF;
//		int g = (color >> 8) & 0xFF;
//		int b = color & 0xFF;
//		int x = -1;
//		int y = -1;
//		int maxValue = Integer.MAX_VALUE;
//		for (int i = 0; i < data.length; i++) {
//			for (int j = 0 ; j < data[i].length; j++) {
//				int c = data[i][j];
//				int aa = (c >> 24) & 0xFF;
//				int rr = (c >> 16) & 0xFF;
//				int gg = (c >> 8) & 0xFF;
//				int bb = c & 0xFF;
//				if (Math.abs(a - aa) > 128) {
//					continue;
//				}
//				int v = /*(Math.abs(a - aa) + 64) * */(Math.abs(r - rr) + Math.abs(g - gg) + Math.abs(b - bb));
//				if (v < maxValue) {
//					maxValue = v;
//					x = j;
//					y = i;
//				}
//			}
//		}
//		return new int[] { x, y };
//	}
	
	/*
	 * 从一个图片中搜索最接近某个颜色的点
	 * @param data
	 * @param color
	 * @return
	 */
	private static int[] findColorPoint(int[][] data, int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int minx = -1;
		int miny = -1;
		int maxx = -1;
		int maxy = -1;
		int maxValue = Integer.MAX_VALUE;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0 ; j < data[i].length; j++) {
				int c = data[i][j];
				int aa = (c >> 24) & 0xFF;
				int rr = (c >> 16) & 0xFF;
				int gg = (c >> 8) & 0xFF;
				int bb = c & 0xFF;
				if ((r == 0xFF && Math.abs(rr - gg) > 50 && Math.abs(rr - bb) > 50) ||
						(b == 0xFF && Math.abs(bb - rr) > 50 && Math.abs(bb - gg) > 50)) {
					if (minx == -1 || minx > j) {
						minx = j;
					}
					if (miny == -1 || miny > i) {
						miny = i;
					}
					if (maxx == -1 || maxx < j) {
						maxx = j;
					}
					if (maxy == -1 || maxy < i) {
						maxy = i;
					}
				}
			}
		}
		return new int[] { (minx + maxx) / 2, (miny + maxy) / 2 };
	}
	
	/**
	 * 从一个PNG文件中提取不透明的部分，加入到一个PIP文件中。
	 * @param img 包含图块的PNG文件
	 * @param target 目标PIP对象
	 * @return 提取的部分在原图中的坐标。
	 * @throws Exception
	 */
	public static int[] fetchFrameImage(File img, PipImage target) throws Exception {
		Image newImg = new Image(null, img.getAbsolutePath());
		int[][] rawData = ImageViewer.getImageData(newImg, new Rectangle(0, 0, newImg.getBounds().width, newImg.getBounds().height));
		Rectangle untransArea = findUntransparentArea(rawData);
		rawData = ImageViewer.getImageData(newImg, untransArea);
		newImg.dispose();
		
		target.addFrame(rawData);
		return new int[] { untransArea.x, untransArea.y };
	}
	
	/*
	 * 分析一个图片的像素数据，找出包含不透明像素的最小范围。
	 * @param rawData
	 * @return
	 */
	public static Rectangle findUntransparentArea(int[][] rawData) {
		// 从4个方向去检测数据，去掉透明区域后找出有效区域
		int x1 = 0;
		int y1 = 0;
		int x2 = rawData[0].length - 1;
		int y2 = rawData.length - 1;
		while (x1 < x2) {
			if (!isEmpty(rawData, false, x1)) {
				break;
			}
			x1++;
		}
		while (x2 > x1) {
			if (!isEmpty(rawData, false, x2)) {
				break;
			}
			x2--;
		}
		while (y1 < y2) {
			if (!isEmpty(rawData, true, y1)) {
				break;
			}
			y1++;
		}
		while (y2 > y1) {
			if (!isEmpty(rawData, true, y2)) {
				break;
			}
			y2--;
		}
		return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}
	
	/*
	 * 判断图片中某一行或某一列是否全部透明。
	 * @param data 图片RGB数据
	 * @param isRow true表示判断一行
	 * @param index 行号或列号
	 */
	private static boolean isEmpty(int[][] data, boolean isRow, int index) {
		if (isRow) {
			for (int i = 0; i < data[index].length; i++) {
				if ((data[index][i] & 0xFF000000) != 0) {
					return false;
				}
			}
		} else {
			for (int i = 0; i < data.length; i++) {
				if ((data[i][index] & 0xFF000000) != 0) {
					return false;
				}
			}
		}
		return true;
	}
}
