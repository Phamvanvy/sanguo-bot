package com.pipimage.image;

/**
 * 打包文件中的一个文件项。
 * @author lighthu
 */
public class PackageFileItem {
	/** 文件名称 */
	public String name;
	/** 文件数据 */
	public byte[] data;
	/**  引用资源HashCode值*/
	public int refHashCode;
}
