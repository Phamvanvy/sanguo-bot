package com.pip.sanguo.data.pkg;

import java.io.*;
import java.util.*;

/**
 * 打包文件类。
 */
public class PackageFile {
	/** 包裹名字 */
	protected String name;
	/** 版本号 */
	protected int version;
	/** 包含的文件 */
	protected ArrayList<PackageFileItem> files;

	/**
	 * 构造一个空的打包文件。 
	 */
	public PackageFile() {
		files = new ArrayList<PackageFileItem>();
	}
	
	/**
	 * 从指定文件中载入。
	 * @param f 打包文件路径
	 * @throws IOException
	 */
	public void load(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		load(fis);
		fis.close();
	}
	
	/**
	 * 从InputStream中载入。
	 * @param is
	 * @throws IOException
	 */
	public void load(InputStream is) throws IOException {
		files.clear();
		DataInputStream dis = new DataInputStream(is);
		name = dis.readUTF();
		version = dis.readInt();
		int fileCount = dis.readShort();
		String[] fileNames = new String[fileCount];
		for (int i = 0; i < fileCount; i++) {
            fileNames[i] = dis.readUTF();
        }
		for (int i = 0; i < fileCount; i++) {
			int length = dis.readInt();
			if (length > 1000000) {
				throw new IOException("too large file");
			}
			byte[] fileData = new byte[length];
			dis.readFully(fileData);
			PackageFileItem item = new PackageFileItem();
			item.name = fileNames[i];
			item.data = fileData;
			files.add(item);
        }
	}
	
	/**
	 * 保存到文件。
	 * @param f
	 * @throws IOException
	 */
	public void save(File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		save(fos);
		fos.close();
	}
	
	/**
	 * 保存到OutputStream。
	 * @param os
	 * @throws IOException
	 */
	public void save(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(name);
		//System.out.println("name:::" + name);
		dos.writeInt(version);
		//System.out.println("version:::" + version);
		int fileCount = files.size();
		dos.writeShort(fileCount);
		//System.out.println("fileCount::" + fileCount);
		for (int i = 0; i < fileCount; i++) {
			dos.writeUTF(files.get(i).name);
			//System.out.println("filesName:::" + files.get(i).name);
		}
		for (int i = 0; i < fileCount; i++) {
			byte[] fileData = files.get(i).data;
			dos.writeInt(fileData.length);
			// System.out.println("fileData.length::" + fileData.length);
			dos.write(fileData);
		}
	}

	/**
	 * 列举打包文件中包含的文件。
	 */
	public PackageFileItem[] getFiles() {
		PackageFileItem[] ret = new PackageFileItem[files.size()];
		files.toArray(ret);
		return ret;
	}

	/**
	 * 取得打包文件中包含的文件数。
	 */
	public int getFileCount() {
		return files.size();
	}

	/**
	 * 得到所有文件大小的和。
	 */
	public int getTotalSize() {
	    int ret = 0;
	    for (PackageFileItem pfi : files) {
	        ret += pfi.data.length;
	    }
	    return ret;
	}
	
	/**
	 * 根据下标取得一个文件。
	 */
	public PackageFileItem getFile(int index) {
		return files.get(index);
	}
	
	/**
	 * 查找一个文件。
	 */
	public byte[] findFile(String name) {
		for (PackageFileItem pki : files) {
			if (pki.name.equals(name)) {
				return pki.data;
			}
		}
		return null;
	}
	
	/**
	 * 把打包文件中的一个文件改名。
	 * @param index 文件下标
	 * @param newname 新文件名
	 */
	public void renameFile(int index, String newname) {
		files.get(index).name = newname;
	}
	
	/**
	 * 向打包文件中添加一个文件。
	 * @param name 文件名
	 * @param data 文件数据
	 */
	public void addFile(String name, byte[] data) {
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).name.equals(name)) {
				files.get(i).data = data;
				return;
			}
		}
		PackageFileItem newItem = new PackageFileItem();
		newItem.name = name;
		newItem.data = data;
		files.add(newItem);
	}
	
	/**
	 * 删除一个文件。
	 * @param index 文件下标
	 */
	public void removeFile(int index) {
		files.remove(index);
	}

	/**
	 * 取得打包文件名。
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置打包文件名。
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 取得版本号。
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * 设置版本号。
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	/**
	 * 克隆这个文件。
	 */
	public PackageFile clone() {
	    PackageFile ret = new PackageFile();
	    ret.name = name;
	    ret.version = version;
	    for (PackageFileItem item : files) {
	        PackageFileItem newItem = new PackageFileItem();
	        newItem.name = item.name;
	        newItem.data = new byte[item.data.length];
	        System.arraycopy(item.data, 0, newItem.data, 0, item.data.length);
	        ret.files.add(newItem);
	    }
	    return ret;
	}
}
