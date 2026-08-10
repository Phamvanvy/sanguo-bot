package com.pipimage.image;

public class JPEGMergeOption {
	public float quality = 0.5f;
	public int alphaBits = 4;
	public int borderWidth = 2;
	
	public JPEGMergeOption() {} 
	
	public JPEGMergeOption(float q, int a, int b) {
		quality = q;
		alphaBits = a;
		borderWidth = b;
	}
	
	public JPEGMergeOption dup() {
		JPEGMergeOption ret = new JPEGMergeOption();
		ret.quality = quality;
		ret.alphaBits = alphaBits;
		ret.borderWidth = borderWidth;
		return ret;
	}
}
