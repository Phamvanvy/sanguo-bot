package com.pipimage.utils;

public class ImageUtil {

	/**
	 * 比较两个图像数据，计算匹配率
	 * @param data1
	 * @param data2
	 * @return
	 */
	public static double compareData(int[][] data1, int[][] data2) {
		if (data1.length != data2.length || data1[0].length != data2[0].length) {
			return 100.0;
		}
	    int totalCount = data1.length * data1[0].length;
	    double errorCount = 0;
	    for (int i = data1.length - 1; i >= 0; i--) {
	        for (int j = data1[0].length - 1; j >= 0; j--) {
	        	int p1 = data1[i][j];
	        	int p2 = data2[i][j];
	        	if (p1 == p2) {
	        		continue;
	        	}
	        	int a = ((p1 >> 24) & 0xFF) - ((p2 >> 24) & 0xFF);
	        	int r = ((p1 >> 16) & 0xFF) - ((p2 >> 16) & 0xFF);
	        	int g = ((p1 >> 8) & 0xFF) - ((p2 >> 8) & 0xFF);
	        	int b = (p1 & 0xFF) - (p2 & 0xFF);
	        	int diff = Math.abs(a) + Math.abs(r) + Math.abs(g) + Math.abs(b);
	        	if (diff > 60) {
	        		errorCount += 100.0;
	        	} else {
	        		errorCount += diff * 100.0 / 60;
	        	}
	        }
	    }
	    return errorCount / totalCount;
	}

}
