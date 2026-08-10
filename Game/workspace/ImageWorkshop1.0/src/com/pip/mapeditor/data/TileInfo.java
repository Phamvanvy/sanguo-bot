package com.pip.mapeditor.data;

/**
 * 贴图图片描述。因为PipImage本身不支持翻转帧的存储，所以需要用一个单独的文件来描述图片翻转信息。
 * @author lighthu
 */
public class TileInfo {
    /** 此帧对应图片文件中的实际帧序号 */
    public int frameID;
    /** 翻转值 */
    public int transit;
    /** 缩略图颜色 */
    public int thumbColor;
    /** 不允许通过标志 */
    public boolean unpassable;
    
    public void copyFrom(TileInfo src) {
        frameID = src.frameID;
        transit = src.transit;
        thumbColor = src.thumbColor;
        unpassable = src.unpassable;
    }
}
