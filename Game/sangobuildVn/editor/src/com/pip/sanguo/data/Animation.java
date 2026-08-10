package com.pip.sanguo.data;

import java.io.File;

import org.jdom.*;

import com.pipimage.utils.Utils;

/**
 * 游戏动画对象。
 * @author lighthu
 */
public class Animation extends DataObject {
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 动画文件。
     */
    public java.io.File source;
    /**
     * 放大版本动画文件。
     */
    public java.io.File largeSource;
    /**
     * 载入的CTN文件内容（仅用于服务器模式）。
     */
    public byte[] ctnData;
    public byte[] largeCtnData;

    public Animation(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }

    public boolean equals(Object o){
        return this == o;
    }
    
    public String toString() {
        return id + ": " + title;
    }

    public void update(DataObject obj) {
        Animation oo = (Animation)obj;
        id = oo.id;
        source = oo.source;
        largeSource = oo.largeSource;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
    }
    
    public DataObject duplicate() {
        Animation ret = new Animation(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        Animation oo = (Animation)obj;
        return !source.equals(oo.source);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        String sourceName = elem.getAttributeValue("source");
        if (sourceName != null) {
            source = new java.io.File(owner.baseDir, "Animations/" + sourceName);
        }
        sourceName = elem.getAttributeValue("largesource");
        if (sourceName != null) {
            largeSource = new java.io.File(owner.baseDir, "Animations/" + sourceName);
        }
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("description");
        if(description == null){
            description = "";
        }
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
    }
    
    private String toRelative(String path) {
        String basePath = new java.io.File(owner.baseDir, "Animations").getAbsolutePath();
        path = path.substring(basePath.length() + 1);
        path = path.replace('\\', '/');
        return path;
    }
    
    public Element save() {
        Element ret = new Element("animation");
        ret.addAttribute("id", String.valueOf(id));
        if (source != null) {
            ret.addAttribute("source", toRelative(source.getAbsolutePath()));
        }
        if (largeSource != null) {
            ret.addAttribute("largesource", toRelative(largeSource.getAbsolutePath()));
        }
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
    
    /**
     * 取得对应的CTN文件的内容（仅用于服务器模式）。
     */
    public byte[] getCTNData(String model) {
        if (!owner.isUseLarge(model)) {
        	if (ctnData == null) {
        		String fullPath = source.getAbsolutePath();
        		File ctnFile = new File(fullPath.substring(0, fullPath.length() - 1) + "n");
        		try {
        			ctnData = Utils.loadFileData(ctnFile);
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
        	return ctnData;
        } else {
        	if (largeCtnData == null) {
                String fullPath = largeSource.getAbsolutePath();
                File ctnFile = new File(fullPath.substring(0, fullPath.length() - 1) + "n");
                try {
                    largeCtnData = Utils.loadFileData(ctnFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return largeCtnData;
        }
    }
}
