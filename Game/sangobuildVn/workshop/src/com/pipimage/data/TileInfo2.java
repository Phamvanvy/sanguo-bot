package com.pipimage.data;


/**
 * 用于版本2的图片描述文件，描述一个Tile的信息。
 */
public class TileInfo2{
    public int x;
    public int y;
    public int width;
    public int height;
    public int collX;
    public int collY;
    public int collWidth;
    public int collHeight;
    public byte collision;
    public byte param;
    public boolean alwaysOnTop;

    public int index = -1;

    public String toString(){
        String s = "";
        if(index != -1)
            s = index + ":";
        s += "位置[" + x + "," + y + "," + width + "," + height + "]";
        if(param == ImageDescription.T_HORIZONTAL){
            s += "(水平翻转)";
        }else if(param == ImageDescription.T_VERTICAL){
            s += "(垂直翻转)";
        }else if(param == ImageDescription.T_BOTH){
            s += "(旋转180度)";
        }
        return s;
    }
}
