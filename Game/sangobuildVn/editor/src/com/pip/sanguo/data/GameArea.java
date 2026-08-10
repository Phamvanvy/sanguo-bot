package com.pip.sanguo.data;

import java.util.Arrays;

import org.jdom.Element;

import com.pip.mapeditor.data.MapFile;

/**
 * 一个区域（关卡）的描述信息。一个关卡占用一个目录，里面应该有2个文件：地图文件game.map和描述文件info.xml。
 */
public class GameArea extends DataObject {
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 关卡对应的目录。
     */
    public java.io.File source;
    /**
     * 载入的地图文件（仅用于服务器模式）
     */
    private MapFile mapFile;
    /**
     * 载入的关卡信息文件（仅用于服务器模式）
     */
    private GameAreaInfo areaInfo; 

    public GameArea(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }
    
    public String toString() {
        return id + ": " + title;
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void update(DataObject obj) {
        GameArea oo = (GameArea)obj;
        id = oo.id;
        source = oo.source;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        mapFile = null;
        areaInfo = null;
    }
    
    public DataObject duplicate() {
        GameArea ret = new GameArea(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        GameArea oo = (GameArea)obj;
        try {
            byte[] b1 = getMapFile().toByteArray();
            byte[] b2 = oo.getMapFile().toByteArray();
            if (!Arrays.equals(b1, b2)) {
                return true;
            }
            b1 = getAreaInfo().toByteArray();
            b2 = oo.getAreaInfo().toByteArray();
            if (!Arrays.equals(b1, b2)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        source = new java.io.File(owner.baseDir, "Areas/" + elem.getAttributeValue("source"));
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
    
    public Element save() {
        Element ret = new Element("area");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("source", source.getName());
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
     * 得到关卡对应的地图文件内容（仅用于服务器模式）。
     */
    public MapFile getMapFile() {
    	if (!owner.serverMode) {
    		throw new IllegalArgumentException();
    	}
    	if (mapFile == null) {
    		try {
	    		mapFile = new MapFile();
	    		mapFile.load(new java.io.File(source, "game.map"));
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	return mapFile;
    }
    
    /**
     * 得到关卡对应的地图信息文件（仅用于服务器模式）。
     */
    public GameAreaInfo getAreaInfo() {
    	if (!owner.serverMode) {
    		throw new IllegalArgumentException();
    	}
    	if (areaInfo == null) {
    		try {
	    		areaInfo = new GameAreaInfo(this);
	    		areaInfo.load();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	return areaInfo;
    }
}
