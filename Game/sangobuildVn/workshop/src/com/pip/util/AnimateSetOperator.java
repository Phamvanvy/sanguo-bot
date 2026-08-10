package com.pip.util;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import com.pipimage.image.ColorsExceedException;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipAnimateSet.SimilarImageResult;

public class AnimateSetOperator extends PipAnimateSet {
	/**
	 * // 把animateSet动画序列中所有用到的图片帧加入到animates中
	 * @param animates 接收帧的动画集合
	 * @param animateSet 要放到接收动画里的动画
	 * @param usedFrames
	 * @param b
	 * @return
	 * @throws Exception
	 */
	public HashMap<Point, int[]> reflectNewPieces(PipAnimateSet animateSet, HashSet<Point> usedFrames) throws ColorsExceedException {
		HashMap<Point, int[]> pieceIDMap = new HashMap<Point, int[]>();
	    for (Point p1 : usedFrames) {
	        int[][] imgData = animateSet.getSourceImage(p1.x).getImageDraw(p1.y).getPixels(0);
	        int[] newID = null;
	        SimilarImageResult  similarImg = this.findSimilarImage(imgData);
			if(similarImg.perfectMatch){
				newID = similarImg.perfectOne;
			}else{
				if (animateSet.getSourceImage(p1.x).isTrueColor()) {
					newID = addImageToAnimateSetReal(imgData, -1, null, 0);
				} else {
					newID = addImageToAnimateSetReal(imgData, -1, animateSet.getSourceImage(p1.x), p1.y);
				}
			}
	        pieceIDMap.put(p1, newID);
	    }
	    return pieceIDMap;
	}
	/**
	 * 合并动画
	 * @param animateSet
	 * @param animateIndex
	 * @throws ColorsExceedException
	 */
	public void addAnimate(PipAnimateSet animateSet, int animateIndex) throws ColorsExceedException{
		// 把指定动画序列中所有用到的图片帧加入到地图素材中
	    PipAnimate animate = animateSet.getAnimate(animateIndex);
	    HashSet<Point> usedFrames = animate.getUsedFrames();
	    HashMap<Point, int[]> pieceIDMap = reflectNewPieces(animateSet, usedFrames);
	    
	    // 复制所有用到的动画帧
	    HashMap<Integer, Integer> frameIDMap = copyUsedFrame(animate, pieceIDMap);
        
        // 复制动画序列
        PipAnimate newAni = addAnimate(animate.getName());
        newAni.fillWith(animate, frameIDMap);
	}
	
	public void saveSelfAndPip(File ctsFile, boolean fullName) throws Exception{
		save(ctsFile, fullName);
		String ctsName = ctsFile.getName();
		ctsName = ctsName.substring(0, ctsName.lastIndexOf(".") - 1);
		int cnt = getFileCount();
		for(int i=0; i<cnt; i++){
			String pipName = getFileName(i);
//			pipName = ctsName+pipName;
			File pipFile = new File(ctsFile.getParent(), pipName);
			getSourceImage(i).save(pipFile, true);
		}
	}
	
	public static void main(String[] args) throws Exception{
		String path = "E:/work/西游/美术/256光效整合-10-18/冰蓝/";
		mergeCtsInDir(path);
	}
	
	public static void mergeCtsInDir(String path) throws Exception{
		File dir = new File(path);
		AnimateSetOperator oper = new AnimateSetOperator();
		for(File ctsFile:dir.listFiles()){
			if(ctsFile.getName().endsWith(".cts")==false){
				continue;
			}
			PipAnimateSet pas = new PipAnimateSet();
			pas.load(ctsFile);
			int cnt = pas.getAnimateCount();
			for(int i=0; i<cnt; i++){
				oper.addAnimate(pas, i);
			}
		}
		if(oper.getAnimateCount()>0){
			dir = new File(dir, "merged");
			if(dir.exists()==false){
				dir.delete();
			}
			dir.mkdir();
			File mergedCts = new File(dir, "merged.cts");
			oper.saveSelfAndPip(mergedCts, true);
			System.out.println("AnimateSetOperator.main() merged "+oper.getAnimateCount());
			System.out.println(mergedCts);
		}else{
			System.out.println("AnimateSetOperator.main() merged 0");
		}
		System.out.println("AnimateSetOperator.main() done");
	}
}
