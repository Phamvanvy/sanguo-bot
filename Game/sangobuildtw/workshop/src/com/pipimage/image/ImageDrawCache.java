package com.pipimage.image;

import java.util.*;

import org.eclipse.swt.graphics.Image;

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
    
    public ImageDrawCache(int cap) {
        capacity = cap;
    }
    
    public void add(PipImage img, int frame, int trans, Image swtImg) {
        Key newKey = new Key(img, frame, trans);
        if (searchTable.containsKey(newKey)) {
            searchTable.get(newKey).dispose();
            searchTable.put(newKey, swtImg);
        } else {
            if (keyList.size() >= capacity) {
                Key key = keyList.remove(0);
                Image obsoleteImage = searchTable.remove(key);
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
                searchTable.remove(key);
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
            img.dispose();
        }
    }
    
    public void clear() {
        keyList.clear();
        searchTable.clear();
    }
}
