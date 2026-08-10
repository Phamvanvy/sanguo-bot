package com.pipimage.image;

import java.util.*;
import java.io.*;

import com.pip.util.Point;

import com.pipimage.utils.ImageUtil;
import com.pipimage.utils.Utils;

/**
 * A set of animation.
 */
public class PipAnimateSet {
    // version number
    protected byte version;
	// The animate file path
	protected File originalFile;
	// Names of source files
	protected ArrayList<String> sourceFiles;
	// Loaded image object from source files
	protected ArrayList<PipImage> sourceImages;
	// Modify flag of source files
	protected ArrayList<Boolean> sourceFileModifyFlags;
	// Animations
	protected ArrayList<PipAnimate> animates;
	// Frame definitions
	protected ArrayList<PipAnimateFrame> frames;
	
	public PipAnimateSet() {
		sourceFiles = new ArrayList<String>();
		sourceImages = new ArrayList<PipImage>();
		sourceFileModifyFlags = new ArrayList<Boolean>();
		animates = new ArrayList<PipAnimate>();
		frames = new ArrayList<PipAnimateFrame>();
	}
	
	public byte getVersion() {
	    return version;
	}
	
	public int getFileCount() {
		return sourceFiles.size();
	}
	
	public String getFileName(int index) {
		return sourceFiles.get(index);
	}
	
	public File getSourceFile(int index) {
		return new File(originalFile.getParentFile(), sourceFiles.get(index));
	}
	
	public PipImage getSourceImage(int index) {
		return sourceImages.get(index);
	}
	
	public void setFileName(int index, String newname) {
		sourceFiles.set(index, newname);
	}
	
	public void renameSourceFile(int index, String newname) throws Exception {
	    File srcFile = getSourceFile(index);
	    File destFile = new File(originalFile.getParentFile(), newname);
	    if (destFile.exists()) {
	        throw new Exception("目标文件已经存在了。");
	    }
	    Utils.copyFile(srcFile, destFile);
	    srcFile.delete();
	    sourceFiles.set(index, newname);
	}
	
	public void removeSourceFile(int index) {
		sourceFiles.remove(index);
		sourceImages.remove(index);
		sourceFileModifyFlags.remove(index);
		for (int i = 0; i < frames.size(); i++) {
			frames.get(i).adjustImageIndex(index);
		}
	}
	
	public void swapSourceFile(int index1, int index2){
	    String sourceFile1 = sourceFiles.get(index1);
	    String sourceFile2 = sourceFiles.get(index2);
	    sourceFiles.set(index1, sourceFile2);
	    sourceFiles.set(index2, sourceFile1);
	    
	    PipImage sourceImage1 = sourceImages.get(index1);
	    PipImage sourceImage2 = sourceImages.get(index2);
	    sourceImages.set(index1, sourceImage2);
	    sourceImages.set(index2, sourceImage1);
	    
	    Boolean sourceFileModifyFlag1 = sourceFileModifyFlags.get(index1);
	    Boolean sourceFileModifyFlag2 = sourceFileModifyFlags.get(index2);
	    sourceFileModifyFlags.set(index1, sourceFileModifyFlag2);
	    sourceFileModifyFlags.set(index2, sourceFileModifyFlag1);
	    
	    for (int i = 0; i < frames.size(); i++) {
            frames.get(i).swapImageIndex(index1, index2);
        }
	}

	public void setFileModified(int image) {
		this.sourceFileModifyFlags.set(image, Boolean.TRUE);
	}
	
	public void merge(PipAnimateSet newSet, int[] frameMapping, int[] animateMapping) {
	    // merge file list
	    HashMap<Integer, Integer> fileIDMap = new HashMap<Integer, Integer>();
	    for (int i = 0; i < newSet.sourceFiles.size(); i++) {
	    	String file = newSet.sourceFiles.get(i);
	    	int foundIndex = -1;
	    	for (int j = 0; j < sourceFiles.size(); j++) {
	    		if (sourceFiles.get(j).equals(file)) {
	    			foundIndex = j;
	    			break;
	    		}
	    	}
	    	if (foundIndex == -1) {
		        sourceFiles.add(newSet.sourceFiles.get(i));
		        sourceImages.add(newSet.sourceImages.get(i));
		        sourceFileModifyFlags.add(Boolean.FALSE);
		        fileIDMap.put(i, sourceFiles.size() - 1);
	    	} else {
	    		fileIDMap.put(i, foundIndex);
	    	}
	    }
	    
	    // merge frames
	    for (int i = 0; i < newSet.frames.size(); i++) {
	        PipAnimateFrame frame = newSet.frames.get(i);
	        PipAnimateFrame target;
	        if (frameMapping[i] == 0) {
	            target = this.addFrame(frame.getName());
	            frameMapping[i] = frames.size() - 1;
	        } else {
	            target = frames.get(frameMapping[i] - 1);
	            frameMapping[i]--;
	        }
	        for (int j = 0; j < frame.getPieceCount(); j++) {
	            PipAnimateFramePiece piece = frame.getPiece(j);
	            PipAnimateFramePiece newPiece = target.addPiece(fileIDMap.get(piece.getImageID()), piece.getFrame());
	            newPiece.setDx(piece.getDx());
	            newPiece.setDy(piece.getDy());
	            newPiece.setTransition(piece.getTransition());
	        }
	    }
	    
	    // merge animates
	    for (int i = 0; i < newSet.animates.size(); i++) {
	        PipAnimate animate = newSet.animates.get(i);
	        PipAnimate target;
            if (animateMapping[i] == 0) {
                target = this.addAnimate(animate.getName());
                animateMapping[i] = animates.size() - 1;
                for (int j = 0; j < animate.getFrameCount(); j++) {
                    PipAnimateFrameRef frameRef = animate.getFrame(j);
                    PipAnimateFrameRef newRef = target.addFrame(frameMapping[frameRef.getFrame()]);
                    newRef.setDx(frameRef.getDx());
                    newRef.setDy(frameRef.getDy());
                    newRef.setDelay(frameRef.getDelay());
                }
            } else {
                target = animates.get(animateMapping[i] - 1);
                animateMapping[i]--;
            }
        }
	}
	
	public void adjustSourceFrame(int image, Map<Integer, Integer> frameMap) {
		for (int i = 0; i < frames.size(); i++) {
			frames.get(i).adjustFrameIndex(image, frameMap);
		}
	}
	
	public void replaceFile(int index, String fpath, PipImage img) {
	    sourceFiles.set(index, new File(fpath).getName());
	    sourceImages.set(index, img);
	}
	
	public void addSourceFile(String fname) throws IOException {
		PipImage newImg = new PipImage();
		newImg.load(fname);
		sourceFiles.add(new File(fname).getName());
		sourceImages.add(newImg);
		sourceFileModifyFlags.add(Boolean.FALSE);
	}
	
	public void addSourceFile(String fname, PipImage image) {
	    sourceFiles.add(fname);
	    sourceImages.add(image);
	    sourceFileModifyFlags.add(Boolean.FALSE);
	}
	
	public int getAnimateCount() {
		return animates.size();
	}
	
	public PipAnimate getAnimate(int index) {
		return animates.get(index);
	}
	
	public void removeAnimate(int index) {
		animates.remove(index);
	}
	
	public int getAnimateIndex(PipAnimate pa){
		return animates.indexOf(pa);
	}
	
	/**
	 * This is for libMode image workshop to display multiple cts file<br/>
	 * Added animate is persistent on disk;
	 * @param anim
	 */
	public void addAnimate(PipAnimate anim){
		animates.add(anim);
	}
	/**
	 * It's for libMode image workshop
	 */
	public int hashCode;
	
	public PipAnimate addAnimate(String name) {
		PipAnimate newObj = new PipAnimate(this);
		newObj.setName(name);
		animates.add(newObj);
		return newObj;
	}

	public void swapAnimates(int index1, int index2) {
	    PipAnimate animateTemp = animates.get(index1);
	    animates.set(index1, animates.get(index2));
	    animates.set(index2, animateTemp);
    }
	
	public void dupAnimate(int index, boolean addToTail) {
	    PipAnimate ani = animates.get(index);
	    PipAnimate dupAni = new PipAnimate(this);
	    try {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(bos);
	        ani.save(dos, true);
	        dos.close();
	        dupAni.load(new DataInputStream(new ByteArrayInputStream(bos.toByteArray())));
	    } catch (IOException e) {
	    }
	    if (addToTail) {
	        animates.add(dupAni);
	    } else {
	        animates.add(index + 1, dupAni);
	    }
	}
	
	public PipAnimateFrame getFrame(int index) {
		return frames.get(index);
	}
	
	public int getFrameCount() {
		return frames.size();
	}
	
	public void removeFrame(int index) {
		frames.remove(index);
		for (int i = 0; i < animates.size(); i++) {
			animates.get(i).onFrameRemoved(index);
		}
	}
	
	public void swapFrames(int index1, int index2) {
	    PipAnimateFrame frameTemp = frames.get(index1);
	    frames.set(index1, frames.get(index2));
	    frames.set(index2, frameTemp);
        for (int i = 0; i < animates.size(); i++) {
            animates.get(i).onFrameSwap(index1, index2);
        }
	}
	
	public PipAnimateFrame addFrame(String name) {
		PipAnimateFrame frame = new PipAnimateFrame(this);
		frame.setName(name);
		frames.add(frame);
		return frame;
	}
	
	public void dupFrame(int[] indices) {
	    int insertPos = indices[indices.length - 1] + 1;
	    for (int i = 0; i < indices.length; i++, insertPos++) {
	        frames.add(insertPos, (PipAnimateFrame)frames.get(indices[i]).clone());
	        for (int j = 0; j < animates.size(); j++) {
                animates.get(j).onFrameAdded(insertPos);
            }
	    }
	}

	public void load(File file) throws IOException {
		FileInputStream fis = null;
		try {
			if(!file.exists()){
				throw new FileNotFoundException(file.getAbsolutePath());
			}
			originalFile = file;
			if (file.length() == 0) {
				return;
			}
			fis = new FileInputStream(file);
			load(new BufferedInputStream(fis));
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
	
	public void load(InputStream is) throws IOException {
		load(new DataInputStream(is));
	}
	
	private void loadSourceFiles(DataInputStream dis) throws IOException {
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<PipImage> images = new ArrayList<PipImage>();
		ArrayList<Boolean> flags = new ArrayList<Boolean>();
		
		byte firstByte = dis.readByte();
		version = (byte)((firstByte >> 6) & 0x03);
		int fcount = firstByte & 0x3F;
		for (int i = 0; i < fcount; i++) {
			String fname = dis.readUTF();
			File f = new File(originalFile.getParentFile(), fname);
			PipImage pimage = new PipImage();
			pimage.load(f.getAbsolutePath());
			files.add(fname);
			images.add(pimage);
			flags.add(Boolean.FALSE);
		}
		this.sourceFiles = files;
		this.sourceImages = images;
		this.sourceFileModifyFlags = flags;
	}
	
	public void load(DataInputStream dis) throws IOException {
		loadSourceFiles(dis);
		
		ArrayList<PipAnimateFrame> frs = new ArrayList<PipAnimateFrame>();
		ArrayList<PipAnimate> anis = new ArrayList<PipAnimate>();
		
		int fcount = dis.readShort() & 0xFFFF;
		for (int i = 0; i < fcount; i++) {
			PipAnimateFrame fr = new PipAnimateFrame(this);
			fr.load(dis);
			frs.add(fr);
		}
		
		int acount = dis.readByte() & 0xFF;
		for (int i = 0; i < acount; i++) {
			PipAnimate ani = new PipAnimate(this);
			ani.load(dis);
			anis.add(ani);
		}
		this.frames = frs;
		this.animates = anis;
	}
	
	public void loadCTSFile(DataInputStream dis) throws IOException {
	    byte firstByte = dis.readByte();
        version = (byte)((firstByte >> 6) & 0x03);
        int fcount = firstByte & 0x3F;
        for (int i = 0; i < fcount; i++) {
            dis.readUTF();
        }
        ArrayList<PipAnimateFrame> frs = new ArrayList<PipAnimateFrame>();
        ArrayList<PipAnimate> anis = new ArrayList<PipAnimate>();
        
        fcount = dis.readShort() & 0xFFFF;
        for (int i = 0; i < fcount; i++) {
            PipAnimateFrame fr = new PipAnimateFrame(this);
            fr.load(dis);
            frs.add(fr);
        }
        
        int acount = dis.readByte() & 0xFF;
        for (int i = 0; i < acount; i++) {
            PipAnimate ani = new PipAnimate(this);
            ani.load(dis);
            anis.add(ani);
        }
        this.frames = frs;
        this.animates = anis;
    }
	
	public void save(File file, boolean saveFullName) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			save(fos, saveFullName);
			
			for (int i = 0; i < sourceFiles.size(); i++) {
				if (sourceFileModifyFlags.get(i).booleanValue()) {
					sourceImages.get(i).save(this.getSourceFile(i));
					sourceFileModifyFlags.set(i, Boolean.FALSE);
				}
			}
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	/**
	 * 把CTS文件以及相关的PIP文件强制保存到另外的文件(目录)中。
	 * @param file
	 * @throws IOException
	 */
	public void forceSave(File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            save(fos, true);
            
            File dir = file.getParentFile();
            for (int i = 0; i < sourceFiles.size(); i++) {
                sourceImages.get(i).save(new File(dir, sourceFiles.get(i)));
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
	}
	
	public void save(OutputStream os, boolean saveFullName) throws IOException {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    version = 0;
	    for (; version <= 3; version++) {
    	    try {
    	        save(dos, saveFullName);
    	        break;
    	    } catch (IOException ioe) {
    	    	if ( version == 3 ) {
    	    		throw ioe;
    	    	}
    	        bos.reset();
    	        dos = new DataOutputStream(bos);
    	    }
	    }
	    dos.flush();
	    os.write(bos.toByteArray());
	}
	
	public void save(DataOutputStream dos, boolean saveFullName) throws IOException {
	    if (sourceFiles.size() >= 64) {
            throw new IOException("不能超过64个文件。");
        }
        if (frames.size() >= 1024) {
            throw new IOException("不能超过1024帧。");
        }
        if (animates.size() >= 256) {
            throw new IOException("不能超过256个动画序列。");
        }
		if (saveFullName) {
			dos.writeByte((version << 6) | sourceFiles.size());
			for (int i = 0; i < sourceFiles.size(); i++) {
				dos.writeUTF(sourceFiles.get(i));
			}
	        dos.writeShort(frames.size());
		} else {
			short head = (short) ((version << 14) | frames.size());
			dos.writeShort(head);
		}
		for (int i = 0; i < frames.size(); i++) {
			frames.get(i).save(dos, saveFullName);
		}
		dos.writeByte(animates.size());
		for (int i = 0; i < animates.size(); i++) {
			animates.get(i).save(dos, saveFullName);
		}
        if (!saveFullName) {
            dos.writeByte(sourceFiles.size());
            for (int i = 0; i < sourceFiles.size(); i++) {
                dos.writeUTF(sourceFiles.get(i));
            }
        }
	}

	public File getOriginalFile() {
		return originalFile;
	}

	public void setOriginalFile(File originalFile) {
		this.originalFile = originalFile;
	}

	public void restoreState(DataInputStream dis) throws IOException {
		ArrayList<String> files = new ArrayList<String>();
		ArrayList<PipImage> images = new ArrayList<PipImage>();
		ArrayList<Boolean> flags = new ArrayList<Boolean>();
		
		byte firstByte = dis.readByte();
        version = (byte)((firstByte >> 6) & 0x03);
		int fcount = firstByte & 0x3F;
		for (int i = 0; i < fcount; i++) {
			String fname = dis.readUTF();
			byte[] imgData = new byte[dis.readInt()];
			dis.readFully(imgData);
			PipImage pimage = new PipImage();
			pimage.load(new ByteArrayInputStream(imgData));
			boolean modify = dis.readBoolean();
			files.add(fname);
			images.add(pimage);
			flags.add(modify ? Boolean.TRUE : Boolean.FALSE);
		}
		this.sourceFiles = files;
		this.sourceImages = images;
		this.sourceFileModifyFlags = flags;
		
		ArrayList<PipAnimateFrame> frs = new ArrayList<PipAnimateFrame>();
		ArrayList<PipAnimate> anis = new ArrayList<PipAnimate>();
		
		fcount = dis.readShort() & 0xFFFF;
		for (int i = 0; i < fcount; i++) {
			PipAnimateFrame fr = new PipAnimateFrame(this);
			fr.load(dis);
			frs.add(fr);
		}
		
		int acount = dis.readByte() & 0xFF;
		for (int i = 0; i < acount; i++) {
			PipAnimate ani = new PipAnimate(this);
			ani.load(dis);
			anis.add(ani);
		}
		this.frames = frs;
		this.animates = anis;
	}

	public void saveState(DataOutputStream dos) throws IOException {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos1 = new DataOutputStream(bos);
        for (version = 0; version <= 3; version++) {
            try {
                saveStateImpl(dos1);
                break;
            } catch (IOException ioe) {
                if (version == 3) {
                	break;
                }
                bos.reset();
                dos1 = new DataOutputStream(bos);
            }
        }
        dos1.flush();
        dos.write(bos.toByteArray());
	}
	
	private void saveStateImpl(DataOutputStream dos) throws IOException {
	    if (sourceFiles.size() >= 64) {
	        throw new IOException("不能超过64个文件。");
	    }
	    if (frames.size() >= 32768) {
	        throw new IOException("不能超过32768帧。");
	    }
        if (animates.size() >= 256) {
            throw new IOException("不能超过256个动画序列。");
        }
		dos.writeByte((version << 6) | sourceFiles.size());
		for (int i = 0; i < sourceFiles.size(); i++) {
			dos.writeUTF(sourceFiles.get(i));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			boolean value = sourceImages.get(i).isMergeMode();
			sourceImages.get(i).setMergeMode(false);
			sourceImages.get(i).save(new DataOutputStream(bos), false);
			sourceImages.get(i).setMergeMode(value);
			byte[] imgData = bos.toByteArray();
			dos.writeInt(imgData.length);
			dos.write(imgData);
			dos.writeBoolean(this.sourceFileModifyFlags.get(i).booleanValue());
		}
		dos.writeShort(frames.size());
		for (int i = 0; i < frames.size(); i++) {
			frames.get(i).save(dos, true);
		}
		dos.writeByte(animates.size());
		for (int i = 0; i < animates.size(); i++) {
			animates.get(i).save(dos, true);
		}
	}
	
	public List<Integer> findUnusedFrames() {
	    boolean[] flags = new boolean[frames.size()];
        for (PipAnimate animate : animates) {
            for (PipAnimateFrameRef ref : animate.frames) {
                flags[ref.frame] = true;
            }
        }
        List<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] == false) {
                ret.add(i);
            }
        }
        return ret;
	}
	
	public List<Integer> findUnusedPiece(int imageID) {
	    boolean[] flags = new boolean[this.sourceImages.get(imageID).getImgCount()];
	    for (PipAnimateFrame frame : frames) {
	        for (PipAnimateFramePiece piece : frame.pieces) {
	            if (piece.getImageID() == imageID) {
	                flags[piece.getFrame()] = true;
	            }
	        }
	    }
	    List<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] == false) {
                ret.add(i);
            }
        }
        return ret;
	}
	
	public void dupAndHflip(int index) {
	    PipAnimate oldAni = this.animates.get(index);
	    PipAnimate newAni = new PipAnimate(this);
	    newAni.setName("翻转" + oldAni.getName());
	    HashMap<Integer, Integer> frameMap = new HashMap<Integer, Integer>();
	    for (PipAnimateFrameRef fr : oldAni.frames) {
	        int f = fr.getFrame();
	        if (frameMap.containsKey(f)) {
	            f = frameMap.get(f);
	        } else {
	            // 复制帧
	            PipAnimateFrame newFrame = (PipAnimateFrame)frames.get(f).clone();
	            newFrame.setName("翻转" + newFrame.getName());
	            newFrame.hflip();
	            frames.add(newFrame);
	            int nf = this.frames.size() - 1;
	            frameMap.put(f, nf);
	            f = nf;
	        }
	        newAni.addFrame(f);
	        PipAnimateFrameRef newfr = newAni.getFrame(newAni.getFrameCount() - 1);
	        newfr.setDx(-fr.getDx());
	        newfr.setDy(fr.getDy());
	        newfr.setDelay(fr.getDelay());
	    }
	    animates.add(newAni);
	}
	
	/**
	 * 整体放大一倍。
	 */
	public void enlarge(boolean withFile) {
	    if (withFile) {
    	    for (int i = 0; i < sourceImages.size(); i++) {
    	        sourceImages.get(i).enlarge();
    	        setFileModified(i);
    	    }
	    }
	    for (PipAnimate ani : animates) {
	        ani.enlarge();
	    }
	    for (PipAnimateFrame frame : frames) {
	        frame.enlarge();
	    }
	}
	
	/**
	 * 整体缩小一倍。
	 */
	public void smaller(boolean withFile) {
	    if (withFile) {
    	    for (int i = 0; i < sourceImages.size(); i++) {
    	        sourceImages.get(i).smaller();
    	        setFileModified(i);
    	    }
	    }
	    for (PipAnimate ani : animates) {
	        ani.smaller();
	    }
	    for (PipAnimateFrame frame : frames) {
	        frame.smaller();
	    }
	}
	
	/**
	 * 查找所有错误率在10%以内的图片的返回值<br/>
	 * {@link PipAnimateSet.SimilarImageResult#perfectMatch}<br/>
	 * {@link PipAnimateSet.SimilarImageResult#perfectOne}<br/>
	 * {@link PipAnimateSet.SimilarImageResult#candidates}<br/>
	 * @author jhkang
	 *
	 */
	public class SimilarImageResult{
		/**
		 * 是否有完全匹配的图片
		 */
		public boolean perfectMatch;
		/**
		 * 新帧对应的图片索引、图片内帧号和翻转值。<br/>
		 * {@link #perfectMatch} 为true时有效
		 */
		public int[] perfectOne;
		/**
		 * 错误率在10%以内的图片<br/>
		 * <code>int[]</code>对应的图片索引、图片内帧号和翻转值。
		 */
		public ArrayList<int[]> candidates;
	}
	
	/**
	 * 查找所有错误率在10%以内的图片。{@link PipAnimateSet.SimilarImageResult}
	 * @param imgData
	 * @return SimilarImageResult 
	 */
	public SimilarImageResult findSimilarImage(int[][] imgData){
		SimilarImageResult ret = new SimilarImageResult();
		// 查找所有错误率在10%以内的图片。
		ret.candidates = new ArrayList<int[]>();
	    for (int i = 0; i < getFileCount(); i++) {
	    	if(ret.perfectMatch){
	    		break;
	    	}
	        PipImage img = getSourceImage(i);
	        for (int j = 0; j < img.getImgCount(); j++) {
	        	if(ret.perfectMatch){
		    		break;
		    	}
	            PipImageData id = img.getImageData(j);
	            if (id.getWidth() == imgData[0].length && id.getHeight() == imgData.length) {
	            	for (int k = 0; k < 4; k++) {
			            int[][] fdata = img.getImageDraw(j).getPixels(k);
			            double errorRate = ImageUtil.compareData(imgData, fdata);
			            if (errorRate == 0.0) {
			            	ret.perfectMatch = true;
			            	ret.perfectOne = new int[] { i, j, k };
			            	break;
			            } else if (errorRate < 10) {
			            	ret.candidates.add(new int[] { i, j, k });
			            }
		            }
	            } else if (id.getWidth() == imgData.length && id.getHeight() == imgData[0].length) {
	            	for (int k = 4; k < 8; k++) {
			            int[][] fdata = img.getImageDraw(j).getPixels(k);
			            double errorRate = ImageUtil.compareData(imgData, fdata);
			            if (errorRate == 0.0) {
			            	ret.perfectMatch = true;
			            	ret.perfectOne = new int[] { i, j, k };
			            	break;
			            } else if (errorRate < 10) {
			            	ret.candidates.add(new int[] { i, j, k });
			            }
		            }
	            }
	        }
	    }
		return ret;
	}
	
	/**
	 * if block count in an image large than 255, then create new one
	 * @param imgData
	 * @return
	 * @throws ColorsExceedException
	 */
	public int[] addImageToAnimateSetReal(int[][] imgData) throws ColorsExceedException {
		return addImageToAnimateSetReal(imgData, 255, null, 0);
	}
	
	/**
	 * set maxBlockCount less than 0 to disable  maxBlockCount check;
	 * @param imgData
	 * @param maxBlockCount
	 * @return
	 * @throws ColorsExceedException
	 */
	public int[] addImageToAnimateSetReal(int[][] imgData, int maxBlockCount, PipImage srcImage, int srcFrame) throws ColorsExceedException {
	    // 查找颜色匹配最多（不存在颜色最少），并且加入新图片后颜色不超过256的一个图片
	    HashSet<Integer> candidateImages = new HashSet<Integer>();
	    for (int i = 0; i < getFileCount(); i++) {
	    	candidateImages.add(i);
	    }
	    while (candidateImages.size() > 0) {
	        int bestMatch = -1, unmatchCount = 1000000;
	        for (int i : candidateImages) {
	            int unmatch = getSourceImage(i).findUnmatchColors(imgData);
	            if (unmatch < unmatchCount) {
	                bestMatch = i;
	                unmatchCount = unmatch;
	            }
	        }
	        if (bestMatch != -1) {
	        	candidateImages.remove(bestMatch);
	            try {
	                PipImage targetImage = getSourceImage(bestMatch);
	                if(maxBlockCount>0&& targetImage.getImgCount()==maxBlockCount){
	                	continue;
	                }
	                targetImage.addFrame(imgData, srcImage, srcFrame);
	                return new int[] { bestMatch, targetImage.getImgCount() - 1, 0 };
	            } catch (Exception e) {
	            }
	        }
	    }
	    
	    // 如果添加失败，则创建一个新的图片
	    PipImage targetImage = new PipImage();
//	    targetImage.trueColor = true;
	    targetImage.supportMoreColors = true;
	    targetImage.addFrame(imgData, srcImage, srcFrame);
	    int id = 1;
	    while (true) {
	        boolean exists = false;
	        for (int i = 0; i < getFileCount(); i++) {
	            if (getFileName(i).equals(id + ".pip")) {
	                exists = true;
	                break;
	            }
	        }
	        if (!exists) {
	            break;
	        }
	        id++;
	    }
	    addSourceFile(id + ".pip", targetImage);
	    return new int[] { getFileCount() - 1, 0, 0 };
	}

	/**
	 * 将aniamte中的帧加入到animates中去
	 * @param animate
	 * @param pieceIDMap
	 * @return
	 */
	public HashMap<Integer, Integer> copyUsedFrame(PipAnimate animate, HashMap<Point, int[]> pieceIDMap) {
		HashMap<Integer, Integer> frameIDMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < animate.getFrameCount(); i++) {
	        PipAnimateFrameRef afr = animate.getFrame(i);
	        int oldIndex = afr.getFrame();
	        PipAnimateFrame af = afr.realize();
	        PipAnimateFrame newf = addFrame(af.getName());
	        for (int j = 0; j < af.getPieceCount(); j++) {
	            PipAnimateFramePiece oldp = af.getPiece(j);
	            int[] newpid = pieceIDMap.get(new Point(oldp.getImageID(), oldp.getFrame()));
	            PipAnimateFramePiece newp = newf.addPiece(newpid[0], newpid[1]);
	            newp.setTransition(oldp.getTransition() ^ newpid[2]);
	            newp.setDx(oldp.getDx());
	            newp.setDy(oldp.getDy());
	        }
	        frameIDMap.put(oldIndex, getFrameCount() - 1);
	    }		
		return frameIDMap;
	}

//	public static void main(String []args) throws Exception{
//		String file = "E:\\workspace\\Xiyou-Editor1.0\\data\\pipLib\\动画\\台阶类\\台阶.cts";
//		PipAnimateSet pas = new PipAnimateSet();
//		pas.load(new File(file));
//		
//		int cutAnimIdx = 1;
//		
//		//remove animate
//		PipAnimate pa = pas.getAnimate(cutAnimIdx);
//		pas.animates.clear();
//		pas.addAnimate(pa);
//		
//		//make used frames and pipImgs
//		Hashtable<Integer, Integer> usedImgs = new Hashtable<Integer, Integer>();
//		Hashtable<PipAnimateFrame, PipAnimateFrame> usedFrames = new Hashtable<PipAnimateFrame, PipAnimateFrame>();
//		for(int j=0; j<pa.getFrameCount(); j++){
//			PipAnimateFrame frameInAni = pa.getFrame(j).realize();
//			usedFrames.put(frameInAni, frameInAni);
//			for(PipAnimateFramePiece piece:frameInAni.pieces){
//				usedImgs.put(new Integer(piece.getImageID()), new Integer(0));
//			}
//		}
//		
//		//remove animateFrame
//		for(int i=0; i<pas.getFrameCount(); i++){
//			PipAnimateFrame frameInSet = pas.getFrame(i);
//			if(usedFrames.containsKey(frameInSet)==false){
//				pas.removeFrame(i);
//				i --;
//			}
//		}
//		
//		//remove pip image
//		for(int i=0; i<pas.getFileCount(); i++){
//			if(usedImgs.containsKey(new Integer(i))==false){
//				pas.sourceFiles.remove(i);
//				i--;
//			}
//		}
//		
//		
//		
//		int autoIdx = 0;
//		File f = new File(file.replaceAll("\\.cts$", "_z"+cutAnimIdx+".cts"));
//		while(true){
//			if(f.exists()==false){
//				break;
//			}
//			f = new File(file.replaceAll("\\.cts$", "_p"+cutAnimIdx+"_"+autoIdx+".cts"));
//			autoIdx ++;
//		}
//		pas.save(f, true);
//		System.out.println("done");
//	}
}
