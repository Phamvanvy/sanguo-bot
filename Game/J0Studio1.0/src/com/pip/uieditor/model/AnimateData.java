package com.pip.uieditor.model;

import com.pip.uieditor.util.ObjectUtil;

public class AnimateData {
	private String file;
	private int index;
	
	public AnimateData(String file, int index) {
		this.file = file;
		this.index = index;
	}
	
	public String getFile() {
		return this.file;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		if(file == null)
			return "";
		return file + "," + index;
	}
	
	public AnimateData getCopy() {
		return new AnimateData(this.file, this.index);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof AnimateData) {
			AnimateData data = (AnimateData)o;
			return ObjectUtil.equals(file, data.file) && this.index == data.index;
		}
		return false;
	}
}
