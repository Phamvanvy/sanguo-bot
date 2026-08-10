package com.pip.image;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;
import com.pip.sanguo.GameMain;


/**
 * A set of animation. The object contains two types of data: frames and animations.
 */
public class PipAnimateSet{
    // version number
    private byte version;
    // the images to compose frames
    private ImageSet[] sourceImages;
    // frame definistions
    // each frame contains a set of pieces and each piece has five parameters:
    // imageID(3bits), frame(8bits), transition(3bits), dx(9bits), dy(9bits)
    // all the five parameters are bounded into a 32-bits integer.
    //#if DoubleScreen == true
    //# private int[] frameData;
    //# private short[] frameData2;
    //#else
    private int[] frameData;
    //#endif
    private short[] framePos;
    private byte[] frameLen;
    // animate definitions
    // each animate contains a set of frames and each frame has four parameters: 
    // frame index(8bits), x offset(10bits), y offset(10bits), delay(4bits)
    // all the four parameters are bounded into a 32-bits integer.
    private int[] animateData;
    private short[] animatePos;
    private byte[] animateLen;
    //animate names
    private String[] sourceImageNames;
    //animate is ready (all images is ok)
    private boolean ready;
    
    private int[][] animateBox = null;

    /**
     * Create a set of animation. 
     * @param images source images from which to get pieces
     * @param def animation definition file content
     */
    public PipAnimateSet(ImageSet[] images, byte[] def){
        sourceImages = images;
        try{
            version = (byte)((def[0] >> 6) & 0x03);
            load(new DataInputStream(new ByteArrayInputStream(def)));
            
            if(ready){
                storeAnimatebox();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String[] getMissingImage(){
        Vector result = new Vector();

        for(int i = 0; i < sourceImageNames.length; i++){
            if(sourceImages[i] == null){
                result.addElement(sourceImageNames[i]);
            }
        }

        String[] tmp = new String[result.size()];
        result.copyInto(tmp);

        return tmp;
    }

    public String[] getAllImageName(){
        return sourceImageNames;
    }

    public void setImage(String name, ImageSet image){
        for(int i = 0; i < sourceImageNames.length; i++){
            if(sourceImageNames[i].equals(name) && sourceImages[i] == null){
                sourceImages[i] = image;

                break;
            }
        }

        for(int i = 0; i < sourceImages.length; i++){
            if(sourceImages[i] == null){
                return;
            }
        }

        ready = true;
        storeAnimatebox();
    }

    public boolean ready(){
        return ready;
    }
    
    private void storeAnimatebox(){
        int animateCount = getAnimateCount();
        animateBox = new int[animateCount][];
    }

    private void load(DataInputStream dis) throws IOException{
        framePos = new short[dis.readShort() & 0x3FFF];
        frameLen = new byte[framePos.length];
        //#if ModelID == AndroidAuto
        //# Object[] tmpArr = null;
        //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
    	//# {
        //# tmpArr = readIntArray_Large(dis, framePos, frameLen, version);
        //# frameData = (int[])tmpArr[0];
        //# frameData2 = (short[])tmpArr[1];
        //# }
        //# else
        //# {
        //#	frameData = readIntArray(dis, framePos, frameLen);
        //# }
        //#elif DoubleScreen == true
        //# Object[] tmpArr = readIntArray_Large(dis, framePos, frameLen, version);
        //# frameData = (int[])tmpArr[0];
        //# frameData2 = (short[])tmpArr[1];
        //#else
        frameData = readIntArray(dis, framePos, frameLen);
        //#endif
        animatePos = new short[dis.readByte() & 0xFF];
        animateLen = new byte[animatePos.length];
        //#if ModelID == AndroidAuto
        //# if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
    	//# {
        //# tmpArr = readIntArray_Large(dis, animatePos, animateLen, (byte)0);
        //# animateData = (int[])tmpArr[0];
        //# }
        //# else
        //# {
        //#	animateData = readIntArray(dis, animatePos, animateLen);	
        //# }
        //#elif DoubleScreen == true
        //# tmpArr = readIntArray_Large(dis, animatePos, animateLen, (byte)0);
        //# animateData = (int[])tmpArr[0];
        //#else
        animateData = readIntArray(dis, animatePos, animateLen);
        //#endif
        sourceImageNames = readStringArray(dis, true);

        if(sourceImages == null){
            sourceImages = new ImageSet[sourceImageNames.length];
        }else{
            ready = true;
        }
    }

    private String[] readStringArray(DataInputStream dis, boolean byteLength) throws IOException{
        int count;

        if(byteLength){
            count = dis.readByte() & 0xFF;
        }else{
            count = dis.readShort() & 0xFFFF;
        }

        String[] ret = new String[count];

        for(int i = 0; i < count; i++){
            ret[i] = Tool.readUTF(dis);
        }

        return ret;
    }

//#if ModelID == AndroidAuto
  //# private Object[] readIntArray_Large(DataInputStream dis, short[] start, byte[] len, byte ver) throws IOException 
    //# {
    //#  if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
	//#  {
    //#       int count = start.length;
    //#       int[][] ret = new int[count][];
    //#       short[][] ret1 = null;
    //#       if (ver == 2) {
    //#       ret1 = new short[count][];
    //#    }
    //#   int pos = 0;
    //#   for (int i = 0; i < count; i++) {
    //#       int count2 = dis.readByte() & 0xFF;
    //#       start[i] = (short)pos;
    //#       len[i] = (byte)count2;
    //#       ret[i] = new int[count2];
    //#       if (ver == 2) {
    //#           ret1[i] = new short[count2];
    //#       }
    //#       for (int j = 0; j < count2; j++) {
    //#           ret[i][j] = dis.readInt();
    //#           if (ver == 2) {
    //#               ret1[i][j] = dis.readShort();
    //#           }
    //#       }
    //#       pos += count2;
    //#    }
    //#    int[] ret2 = new int[pos];
    //#    short[] ret3 = null;
    //#    if (ver == 2) {
    //#       ret3 = new short[pos];
    //#    }
    //#    pos = 0;
    //#    for (int i = 0; i < count; i++) {
    //#       System.arraycopy(ret[i], 0, ret2, pos, ret[i].length);
    //#       if (ver == 2) {
    //#           System.arraycopy(ret1[i], 0, ret3, pos, ret1[i].length);
    //#       }
    //#       pos += ret[i].length;
    //#    }
    //#    return new Object[] { ret2, ret3 };
    //#  }
    //#  else
    //#  {
    //#   return null;
    //#  }
    //# } 
    //# private int[] readIntArray(DataInputStream dis, short[] start, byte[] len) throws IOException 
    //# {
    //#  if (!GameMain.getUIModel().equals(GameMain.ANDROID_LARGE))
	//#  {
    //#    int count = start.length;
    //#    int[][] ret = new int[count][];
    //#    int pos = 0;
    //#    for (int i = 0; i < count; i++) {
    //#        int count2 = dis.readByte() & 0xFF;
    //#        start[i] = (short)pos;
    //#        len[i] = (byte)count2;
    //#         ret[i] = new int[count2];
    //#        for (int j = 0; j < count2; j++) {
    //#            ret[i][j] = dis.readInt();
    //#        }
    //#        pos += count2;
    //#    }
    //#    int[] ret2 = new int[pos];
    //#    pos = 0;
    //#    for (int i = 0; i < count; i++) {
    //#        System.arraycopy(ret[i], 0, ret2, pos, ret[i].length);
    //#        pos += ret[i].length;
    //#    }
    //#    return ret2;
    //#  }
    //#  else
    //#  {
    //#   return null;
    //#  }
    //# }
//#elif DoubleScreen == true
    //# private Object[] readIntArray_Large(DataInputStream dis, short[] start, byte[] len, byte ver) throws IOException {
    //#       int count = start.length;
    //#       int[][] ret = new int[count][];
    //#       short[][] ret1 = null;
    //#       if (ver == 2) {
    //#       ret1 = new short[count][];
    //#   }
    //#   int pos = 0;
    //#   for (int i = 0; i < count; i++) {
    //#       int count2 = dis.readByte() & 0xFF;
    //#       start[i] = (short)pos;
    //#       len[i] = (byte)count2;
    //#       ret[i] = new int[count2];
    //#       if (ver == 2) {
    //#           ret1[i] = new short[count2];
    //#       }
    //#       for (int j = 0; j < count2; j++) {
    //#           ret[i][j] = dis.readInt();
    //#           if (ver == 2) {
    //#               ret1[i][j] = dis.readShort();
    //#           }
    //#       }
    //#       pos += count2;
    //#   }
    //#   int[] ret2 = new int[pos];
    //#   short[] ret3 = null;
    //#   if (ver == 2) {
    //#       ret3 = new short[pos];
    //#   }
    //#   pos = 0;
    //#   for (int i = 0; i < count; i++) {
    //#       System.arraycopy(ret[i], 0, ret2, pos, ret[i].length);
    //#       if (ver == 2) {
    //#           System.arraycopy(ret1[i], 0, ret3, pos, ret1[i].length);
    //#       }
    //#       pos += ret[i].length;
    //#   }
    //#   return new Object[] { ret2, ret3 };
    //# }
//#else
    private int[] readIntArray(DataInputStream dis, short[] start, byte[] len) throws IOException {
         int count = start.length;
         int[][] ret = new int[count][];
         int pos = 0;
         for (int i = 0; i < count; i++) {
             int count2 = dis.readByte() & 0xFF;
             start[i] = (short)pos;
             len[i] = (byte)count2;
             ret[i] = new int[count2];
             for (int j = 0; j < count2; j++) {
                 ret[i][j] = dis.readInt();
             }
             pos += count2;
         }
         int[] ret2 = new int[pos];
         pos = 0;
         for (int i = 0; i < count; i++) {
             System.arraycopy(ret[i], 0, ret2, pos, ret[i].length);
             pos += ret[i].length;
         }
         return ret2;
     }
//#endif
    
    /**
     * Draw single frame.
     * @param g graphics context
     * @param frame the frame index
     * @param x x draw position
     * @param y y draw position
     */
    public void drawFrame(Graphics g, int frame, int x, int y){
        int start = framePos[frame];
        int len = frameLen[frame] & 0xFF;
        for(int i = start; i < start + len; i++){
            int pd = frameData[i];
            int imageID;
            int iframe;
            int transition;
            int dx;
            int dy;
            if (version == 0) 
            {
                imageID = (pd >> 29) & 0x07;
                iframe = (pd >> 21) & 0xFF;
                transition = (pd >> 18) & 0x07;
                dx = (pd >> 9) & 0x1FF;
                if(dx > 255){
                    dx -= 512;
                }
                dy = pd & 0x1FF;
                if(dy > 255){
                    dy -= 512;
                }
            }
            else if (version == 1) 
            {
                imageID = (pd >> 27) & 0x1F;
                iframe = (pd >> 19) & 0xFF;
                transition = (pd >> 16) & 0x07;
                dx = (pd >> 8) & 0xFF;
                if(dx > 127){
                    dx -= 256;
                }
                dy = pd & 0xFF;
                if(dy > 127){
                    dy -= 256;
                }
            }
            //#if ModelID == AndroidAuto
            //# else if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE) && version == 2)
        	//# {
            //#   imageID = (pd >> 27) & 0x1F;
            //#   iframe = (pd >> 19) & 0xFF;
            //#   transition = (pd >> 16) & 0x07;
            //#   dx = pd & 0xFFFF;
            //#   if (dx > 32767) {
            //#       dx -= 65536;
            //#   }
            //#   dy = frameData2[i];	
            //# }
            //#elif DoubleScreen == true
            //#  else if (version == 2) 
            //# {
            //#   imageID = (pd >> 27) & 0x1F;
            //#   iframe = (pd >> 19) & 0xFF;
            //#   transition = (pd >> 16) & 0x07;
            //#   dx = pd & 0xFFFF;
            //#   if (dx > 32767) {
            //#       dx -= 65536;
            //#   }
            //#   dy = frameData2[i];
            //# }
            //#endif
             else 
             {
                continue;
             }
            sourceImages[imageID].drawFrame(g, iframe, x + dx, y + dy, transition);
        }
    }

    /**
     * Draw a frame in specified animation.
     * @param g graphics context
     * @param index the index of animation in the set
     * @param time current play time(in system ticks)
     * @param x x draw position
     * @param y y draw position
     */
    public void drawAnimateFrame(Graphics g, int index, int time, int x, int y) {
        int start = animatePos[index];
        int len = animateLen[index] & 0xFF;
        int tick = 0;
        for(int i = start; i < start + len; i++){
            int aniData = animateData[i];
            int delay = aniData & 0x0F;
            if(time >= tick && time < tick + delay){
                int fr = (aniData >> 24) & 0xFF;
                int dx = (aniData >> 14) & 0x3FF;
                if(dx > 511){
                    dx -= 1024;
                }
                int dy = (aniData >> 4) & 0x3FF;
                if(dy > 511){
                    dy -= 1024;
                }
                drawFrame(g, fr, x + dx, y + dy);
                break;
            }
            tick += delay;
        }
    }

    /**
     * Draw single frame with drawOffset.
     * @param g graphics context
     * @param frame the frame index
     * @param x x draw position
     * @param y y draw position
     */
    public void drawFrameWithOffset(Graphics g, int frame, int x, int y, int[][] drawOffset, ImageSet[] replaceImages){
        int start = framePos[frame];
        int len = frameLen[frame] & 0xFF;
        for(int i = start; i < start + len; i++){
            int pd = frameData[i];
            int imageID;
            int iframe;
            int transition;
            int dx;
            int dy;
            if (version == 0) 
            {
                imageID = (pd >> 29) & 0x07;
                iframe = (pd >> 21) & 0xFF;
                transition = (pd >> 18) & 0x07;
                dx = (pd >> 9) & 0x1FF;
                if(dx > 255){
                    dx -= 512;
                }
                dy = pd & 0x1FF;
                if(dy > 255){
                    dy -= 512;
                }
            } 
            else if (version == 1) 
            {
                imageID = (pd >> 27) & 0x1F;
                iframe = (pd >> 19) & 0xFF;
                transition = (pd >> 16) & 0x07;
                dx = (pd >> 8) & 0xFF;
                if(dx > 127){
                    dx -= 256;
                }
                dy = pd & 0xFF;
                if(dy > 127){
                    dy -= 256;
                }
            }
            //#if ModelID == AndroidAuto
            //# else if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE) && version == 2)
        	//# {
            //#   imageID = (pd >> 27) & 0x1F;
            //#   iframe = (pd >> 19) & 0xFF;
            //#   transition = (pd >> 16) & 0x07;
            //#   dx = pd & 0xFFFF;
            //#   if (dx > 32767) {
            //#       dx -= 65536;
            //#   }
            //#   dy = frameData2[i];    
            //# }
            //#elif DoubleScreen == true
            //#  else if (version == 2) 
            //# {
            //#   imageID = (pd >> 27) & 0x1F;
            //#   iframe = (pd >> 19) & 0xFF;
            //#   transition = (pd >> 16) & 0x07;
            //#   dx = pd & 0xFFFF;
            //#   if (dx > 32767) {
            //#       dx -= 65536;
            //#   }
            //#   dy = frameData2[i];
            //# }
            //#endif
             else 
             {
                continue;
             }

            if(drawOffset[imageID] == null){
                sourceImages[imageID].drawFrame(g, iframe, x + dx, y + dy, transition);
            }else{
                ImageSet targetImage = sourceImages[imageID];
                short targetImageId = (short)(drawOffset[imageID][iframe] >> 16);
                short targetFrameOffset = (short)(drawOffset[imageID][iframe] & 0xFFFF);

                if(targetFrameOffset < 0){
                    continue;
                }else{
                    iframe += targetFrameOffset;
                }

                if(targetImageId >= 0){
                    targetImage = replaceImages[targetImageId];
                }

                targetImage.drawFrame(g, iframe, x + dx, y + dy, transition);
            }
        }
    }

    /**
     * Draw a frame in specified animation with drawOffset.
     * @param g graphics context
     * @param index the index of animation in the set
     * @param time current play time(in system ticks)
     * @param x x draw position
     * @param y y draw position
     */
    public void drawAnimateFrameWithOffset(Graphics g, int index, int time, int x, int y, int[][] drawOffset, ImageSet[] replaceImages){
        int start = animatePos[index];
        int len = animateLen[index] & 0xFF;
        int tick = 0;
        for(int i = start; i < start + len; i++){
            int aniData = animateData[i];
            int delay = aniData & 0x0F;
            if(time >= tick && time < tick + delay){
                int fr = (aniData >> 24) & 0xFF;
                int dx = (aniData >> 14) & 0x3FF;
                if(dx > 511){
                    dx -= 1024;
                }
                int dy = (aniData >> 4) & 0x3FF;
                if(dy > 511){
                    dy -= 1024;
                }
                drawFrameWithOffset(g, fr, x + dx, y + dy, drawOffset, replaceImages);
                break;
            }
            tick += delay;
        }
    }

    /**
     * Get the length of an animation(in system ticks).
     * @param index index of animation in the set
     * @return length of animation(in system ticks)
     */
    public int getAnimateLength(int index){
        int start = animatePos[index];
        int len = animateLen[index] & 0xFF;
        int ret = 0;
        for (int i = start; i < start + len; i++){
            ret += animateData[i] & 0x0F;
        }
        return ret;
    }

    /**
     * 返回本动画文件中的动画个数
     * @return
     */
    public int getAnimateCount(){
        return animatePos.length;
    }

    /**
     * 返回指定动画拼接好后的显示区域
     * @param index 动画index
     * @return int[] [0]x [1]y [2]w [3]h
     */
    public int[] getAnimateBox(int index){
        if(animateBox[index] == null){
            int[] box = getAnimateSize(index);
            box[2] = box[2] - box[0];
            box[3] = box[3] - box[1];
            animateBox[index] = box;
        }

        int[] box = new int[4];
        System.arraycopy(animateBox[index], 0, box, 0, 4);
        return box;
    }

    /**
     * 返回指定动画拼接好后的显示区域
     * @param index 动画index
     * @return int[] [0]左上角x [1]左上角y [2]右下角x [3]右下角y
     */
    public int[] getAnimateSize(int index){
        int start = animatePos[index];
        int len = animateLen[index] & 0xFF;
        int[] box = new int[]{
                        Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
        };

        for (int i = start; i < start + len; i++){
            int aniData = animateData[i];
            int fr = (aniData >> 24) & 0xFF;
            int[] frameBox = getFrameSize(fr);

            int dx = (aniData >> 14) & 0x3FF;
            if(dx > 511){
                dx -= 1024;
            }
            int dy = (aniData >> 4) & 0x3FF;
            if(dy > 511){
                dy -= 1024;
            }

            frameBox[0] += dx;
            frameBox[1] += dy;
            frameBox[2] += dx;
            frameBox[3] += dy;

            Tool.mergeBox(box, frameBox);
        }

        return box;
    }

    /**
     * 返回制定动画帧的显示大小
     * @param frame 动画帧
     * @return int[] [0]左上角x [1]左上角y [2]右下角x [3]右下角y
     */
    private int[] getFrameSize(int frame){
        int start = framePos[frame];
        int len = frameLen[frame] & 0xFF;
        int[] box = new int[]{
                        Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE
        };

        for(int i = start; i < start + len; i++){
            int pd = frameData[i];
            int imageID;
            int iframe;
            int transition;
            int dx;
            int dy;
            
            if (version == 0) 
            {
                imageID = (pd >> 29) & 0x07;
                iframe = (pd >> 21) & 0xFF;
                transition = (pd >> 18) & 0x07;
                dx = (pd >> 9) & 0x1FF;
                if(dx > 255){
                    dx -= 512;
                }
                dy = pd & 0x1FF;
                if(dy > 255){
                    dy -= 512;
                }
            } 
            else if (version == 1) 
            {
                imageID = (pd >> 27) & 0x1F;
                iframe = (pd >> 19) & 0xFF;
                transition = (pd >> 16) & 0x07;
                dx = (pd >> 8) & 0xFF;
                if(dx > 127){
                    dx -= 256;
                }
                dy = pd & 0xFF;
                if(dy > 127){
                    dy -= 256;
                }
            }
            //#if ModelID == AndroidAuto
            //# else if (GameMain.getUIModel().equals(GameMain.ANDROID_LARGE) && version == 2)
        	//# {
            //#   imageID = (pd >> 27) & 0x1F;
            //#   iframe = (pd >> 19) & 0xFF;
            //#   transition = (pd >> 16) & 0x07;
            //#   dx = pd & 0xFFFF;
            //#   if (dx > 32767) {
            //#       dx -= 65536;
            //#   }
            //#   dy = frameData2[i];    
            //# }
            //#elif DoubleScreen == true
            //#  else if (version == 2) 
            //# {
            //#   imageID = (pd >> 27) & 0x1F;
            //#   iframe = (pd >> 19) & 0xFF;
            //#   transition = (pd >> 16) & 0x07;
            //#   dx = pd & 0xFFFF;
            //#   if (dx > 32767) {
            //#       dx -= 65536;
            //#   }
            //#   dy = frameData2[i];
            //# }
            //#endif
            else 
            {
                continue;
            }

            int anchor = Graphics.TOP | Graphics.LEFT;
            int w = sourceImages[imageID].getFrameWidth(iframe);
            int h = sourceImages[imageID].getFrameHeight(iframe);

            if(transition < 4){
                if((anchor & Graphics.HCENTER) > 0){
                    dx -= w / 2;
                }else if((anchor & Graphics.RIGHT) > 0){
                    dx -= w;
                }
                if((anchor & Graphics.VCENTER) > 0){
                    dy -= h / 2;
                }else if((anchor & Graphics.BOTTOM) > 0){
                    dy -= h;
                }
            }else{
                if((anchor & Graphics.HCENTER) > 0){
                    dx -= h / 2;
                }else if((anchor & Graphics.RIGHT) > 0){
                    dx -= h;
                }
                if((anchor & Graphics.VCENTER) > 0){
                    dy -= w / 2;
                }else if((anchor & Graphics.BOTTOM) > 0){
                    dy -= w;
                }
            }

            box[0] = Math.min(box[0], dx);
            box[1] = Math.min(box[1], dy);
            box[2] = Math.max(box[2], dx + w);
            box[3] = Math.max(box[3], dy + h);
        }

        return box;
    }

    /**
     * 返回某一ImageSet的图块数
     * @param imageId
     * @return
     */
    public int getImageFrameCount(int imageId){
        return sourceImages[imageId].getFrameCount();
    }

    /**
     * 更换某一ImageSet
     * @param imageId
     * @param imageSet
     */
    public void changeImageSet(int imageId, ImageSet imageSet){
        sourceImages[imageId] = imageSet;
    }

    /**
     * 返回动画文件中包含的ImageSet个数
     * @return
     */
    public int getImageCount(){
        return sourceImages.length;
    }
}
