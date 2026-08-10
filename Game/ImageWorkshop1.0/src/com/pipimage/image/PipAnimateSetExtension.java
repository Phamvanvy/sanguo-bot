package com.pipimage.image;

/*
 * 动画文件中扩展信息的通用接口。
 */
public interface PipAnimateSetExtension {
	/**
	 * 取得扩展信息类型（4字符字符串）。
	 * @return
	 */
	public String getTypeID();
	/**
	 * 转换为字节表示。
	 * @return
	 */
	public byte[] toByteArray() throws Exception;
	/**
	 * 从字节读取。
	 */
	public void fromByteArray(byte[] data) throws Exception;
}
