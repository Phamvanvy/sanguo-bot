package com.pip.uieditor.model;

import com.pip.uieditor.util.ObjectUtil;

public class ImageData {
	
	private String file;
	private int frame;
	
	public ImageData(String file, int frame) {
		this.file = file;
		this.frame = frame;
	}
	
	public String getFile() {
		return this.file;
	}
	
	public int getFrame() {
		return frame;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setFrame(int frame) {
		this.frame = frame;
	}
	
	@Override
	public String toString() {
		if(file == null)
			return "";
		return file + frame;
	}
	
	public ImageData getCopy() {
		return new ImageData(this.file, this.frame);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof ImageData) {
			ImageData data = (ImageData)o;
			return ObjectUtil.equals(file, data.file) && this.frame == data.frame;
		}
		return false;
	}
}
