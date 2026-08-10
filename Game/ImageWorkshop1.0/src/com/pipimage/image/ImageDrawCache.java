package com.pipimage.image;

import java.util.*;

import org.eclipse.swt.graphics.Image;

import com.pip.mango.jni.GLUtils;

/**
 * 缓存Pip图片绘制总产生的临时系统Image对象，以提高绘图效率。
 * @author lighthu
 */
public class ImageDrawCache {
    protected static class Key {
        PipImage img;
        int frame;
        int transition;
        
        public Key(PipImage img, int f, int t) {
            this.img = img;
            frame = f;
            transition = t;
        }
        
        public int hashCode() {
            return img.hashCode() + frame + transition;
        }
        
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Key)) {
                return false;
            }
            Key oo = (Key)o;
            return img == oo.img && frame == oo.frame && transition == oo.transition;
        }
    };
    protected List<Key> keyList = new ArrayList<Key>();
    protected Map<Key, Image> searchTable = new HashMap<Key, Image>();
    protected int capacity;
    protected int[] palette;
    
    public ImageDrawCache(int cap) {
        capacity = cap;
    }
    
    public void setPalette(int[] p) {
    	if (palette != p) {
	    	palette = p;
	    	clear();
    	}
    }
    
    public int[] getPalette() {
    	return palette;
    }
    
    public void add(PipImage img, int frame, int trans, Image swtImg) {
        Key newKey = new Key(img, frame, trans);
        if (searchTable.containsKey(newKey)) {
            GLUtils.unloadImage(searchTable.get(newKey));
            searchTable.put(newKey, swtImg);
        } else {
            if (keyList.size() >= capacity) {
                Key key = keyList.remove(0);
                Image obsoleteImage = searchTable.remove(key);
                GLUtils.unloadImage(obsoleteImage);
                obsoleteImage.dispose();
            }
            keyList.add(newKey);
            searchTable.put(newKey, swtImg);
        }
    }
    
    public Image get(PipImage img, int frame, int trans) {
        Key newKey = new Key(img, frame, trans);
        return searchTable.get(newKey);
    }
    
    public void remove(PipImage img) {
        for (int i = 0; i < keyList.size(); i++) {
            Key key = keyList.get(i);
            if (key.img == img) {
                keyList.remove(i);
                i--;
                Image bufferImg = searchTable.remove(key);
                GLUtils.unloadImage(bufferImg);
                bufferImg.dispose();
            }
        }
    }
    
    public void remove(PipAnimateSet ani) {
        for (int i = 0; i < ani.getFileCount(); i++) {
            remove(ani.getSourceImage(i));
        }
    }
    
    protected void finalize() throws Throwable {
        for (Image img : searchTable.values()) {
    		GLUtils.unloadImage(img);
        }
    }
    
    public void clear() {
    	for (Image img : searchTable.values()) {
    		GLUtils.unloadImage(img);
        }
        keyList.clear();
        searchTable.clear();
    }
}
