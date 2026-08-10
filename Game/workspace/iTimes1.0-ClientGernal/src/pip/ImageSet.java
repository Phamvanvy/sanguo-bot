package pip;


import javax.microedition.lcdui.*;
import java.io.*;


//#if polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

public class ImageSet{
    //#if DrawMethod == DrawPixels
    //# public static final int[] NOKIA_TRANSFORM_LOOKUP = {
    //# 0,
    //# DirectGraphics.FLIP_VERTICAL,
    //# DirectGraphics.FLIP_HORIZONTAL,
    //# DirectGraphics.ROTATE_180
    //# };
    //#endif

    //#if DrawMethod == SetClip
    //# public Image frame;
    //#elif DrawMethod == DrawPixels
    //# public Object[] nokiaFrames;
    //#elif DrawMethod == DrawRegion
    public Image frame;
    //#else
    //# public Image[] frames;
    //#endif

    public byte[] pos;
    public byte[] desc;
    public short[][] collision;
    public byte width, height;
    public byte type;
    public byte childs[][];
    public int[] miniMapColor;

    public static int[] COLOR_TABLE = new int[]{
                    0x000000, 0x808080, 0xC0C0C0, 0xFFFFFF, 0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF, 0x6fBBF9, 0xFF00FF, 0xFFFF80, 0x00FF80, 0x80FFFF, 0x8080FF, 0xFF0080, 0xFF8040
    };

    public ImageSet(){
    }

    //#if (DrawMethod == SetClip) || (DrawMethod == DrawRegion)
    public int getFrameLength(){
        if(type == 1 || type == 3){
            return pos.length;
        }else{
            return pos.length / 4;
        }
    }

    //#endif

    public int getHeight(int index){
        if(type == 1 || type == 3){
            return height;
        }else{
            return pos[index * 4 + 3] & 0xFF;
        }
    }

    public int getWidth(int index){
        if(type == 1 || type == 3){
            return width;
        }else{
            return pos[index * 4 + 2] & 0xFF;
        }
    }

    public int getMiniMapColor(int index){
        if(index < 0 || index > miniMapColor.length - 1){
            return 0;
        }

        if(type == (byte)2){
            return 0;
        }else{
            return miniMapColor[index];
        }
    }

    public void drawFrame(Graphics g, int index, int x, int y, int anchor){
        //#if DrawMethod == SetClip
        //# int oldx = g.getClipX();
        //# int oldy = g.getClipY();
        //# int oldw = g.getClipWidth();
        //# int oldh = g.getClipHeight();
        //# int w = 0,h = 0,offx = 0,offy = 0;

        //# if(type == 1 || type == 3){
        //#     w = width;
        //#     h = height;
        //#     int wc = frame.getWidth()/width;        
        //#     int p = pos[index] & 0xff;        
        //#     offy = p/wc*height;
        //#     offx = p%wc*width;
        //# }else if(type == 2){
        //#     offx = pos[index*4]&0xFF;
        //#     offy = pos[index*4+1]&0xFF;
        //#     w = pos[index*4+2];
        //#     h = pos[index*4+3];
        //# }

        //# if((anchor & Graphics.BOTTOM) != 0){
        //#     y -= h;
        //# }
        //# if((anchor & Graphics.VCENTER) != 0){
        //#     y -= h / 2;
        //# }
        //# if((anchor & Graphics.HCENTER) != 0){
        //#     x -= w / 2;
        //# }
        //# if((anchor & Graphics.RIGHT) != 0){
        //#     x -= w;
        //# }
        // drawClipTrans(g, x-offx, y-offy, w, h, x, y, desc[index] & 0x07);
        //# g.setClip(x,y,w,h);
        //# g.drawImage(frame,x-offx,y-offy,Graphics.LEFT|Graphics.TOP);
        //# g.setClip(oldx,oldy,oldw,oldh);
        //#elif DrawMethod == DrawPixels
        //# int w,h;
        //# if(type==1|| type == 3){
        //#     w = width;
        //#     h = height;
        //# }
        //# else{
        //#     w = pos[index*4+2];
        //#     h = pos[index*4+3];
        //# }
        //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        //# if((anchor & Graphics.BOTTOM) != 0){
        //#     y -= h;
        //# }
        //# if((anchor & Graphics.HCENTER) != 0){
        //#     y -= h / 2;
        //# }
        //# if((anchor & Graphics.VCENTER) != 0){
        //#     x -= w / 2;
        //# }
        //# if((anchor & Graphics.RIGHT) != 0){
        //#     x -= w;
        //# }
        //# if (type == 1|| type == 3){
        //# int transform = NOKIA_TRANSFORM_LOOKUP[desc[index] & 0x03];
        //# dg.drawPixels((short[])nokiaFrames[pos[index]], true, 0, w, x, y,
        //# w, h, transform,
        //# dg.TYPE_USHORT_4444_ARGB);
        //# } else {
        //#    dg.drawPixels((short[])nokiaFrames[index], true, 0, w, x, y,
        //#                  w, h, 0, dg.TYPE_USHORT_4444_ARGB);
        //# }
        //#elif DrawMethod == DrawRegion
        int w = 0, h = 0, offx = 0, offy = 0;
        if(type == 1 || type == 3){
            w = width;
            h = height;
            int wc = frame.getWidth() / width;
            int p = pos[index] & 0xff;
            offy = p / wc * height;
            offx = p % wc * width;
        }else if(type == 2){
            offx = pos[index * 4] & 0xFF;
            offy = pos[index * 4 + 1] & 0xFF;
            w = pos[index * 4 + 2];
            h = pos[index * 4 + 3];
        }
        g.drawRegion(frame, offx, offy, w, h, desc[index] & 0x07, x, y, anchor);
        //#else
        //# g.drawImage(frames[index], x, y, anchor);
        //#endif
    }

    public static ImageSet createImageSet(Image image, DataInputStream stream, boolean createDesc) throws IOException{
        ImageSet ret = new ImageSet();

        if(stream == null){
            //#if DrawMethod == SetClip
            //# ret.frame = image;
            //#elif DrawMethod == DrawPixels
            //#elif DrawMethod == DrawRegion
            ret.frame = image;
            //#else
            //# ret.frames[0] = image;
            //#endif

            return ret;
        }
        byte type = stream.readByte(); //类型
        int count = stream.readByte() & 0xFF; //tile的数量
        byte[] desc = null;
        int[] miniMapColor = null;
        byte[] fatherInfo;
        byte[][] childs;
        byte[] indexes = null;
        indexes = new byte[count];
        ret.type = type;

        //#if DrawMethod == DrawImage
        //# Image[] tiles = new Image[count];
        //#elif DrawMethod == DrawPixels
        //# Object[] tiles = new Object[count];
        //#endif

        if(type == (byte)1){
            desc = new byte[count];
            miniMapColor = new int[count];
            byte tileWidth = stream.readByte();
            byte tileHeight = stream.readByte();

            for(int i = 0; i < count; i++){
                indexes[i] = stream.readByte();
                desc[i] = stream.readByte();
                miniMapColor[i] = COLOR_TABLE[((desc[i] >> 3) & 0x0F)];
            }

            //#if DrawMethod == DrawPixels
            //# int wc = image.getWidth()/tileWidth;
            //# DirectGraphics dg = DirectUtils.getDirectGraphics(image.getGraphics());
            //# for(int i=0;i<count;i++){
            //# int index = indexes[i] & 0xFF;
            //# if (tiles[index] != null) {
            //# continue;
            //# }
            //# int xx = index%wc;
            //# int yy = index/wc;
            //# tiles[index] = new short[tileWidth * tileHeight];
            //# dg.getPixels((short[])tiles[index], 0, tileWidth, xx * tileWidth,
            //# yy * tileHeight, tileWidth, tileHeight,
            //# dg.TYPE_USHORT_4444_ARGB);
            //# }
            //# ret.nokiaFrames = tiles;
            //# ret.desc = desc;
            //#elif DrawMethod == DrawRegion
            ret.desc = desc;
            ret.frame = image;
            //#elif DrawMethod == DrawImage
            //# int wc = image.getWidth() / tileWidth;
            //# for(int i = 0; i < count; i++){
            //# int index = indexes[i] & 0xff;
            //# int transform = desc[i] & 0x03; //计算翻转
            //# int xx = index % wc;
            //# int yy = index / wc;
            //# try{
            //#     tiles[i] = Image.createImage(image, xx * tileWidth, yy * tileHeight, tileWidth, tileHeight, transform);
            //# }catch(Exception e){
            //#debug
            //#     e.printStackTrace();
            //# }
            //# }

            //# ret.frames = tiles;
            //#else
            //# ret.frame = image;
            //#endif

            ret.pos = indexes;
            ret.width = tileWidth;
            ret.height = tileHeight;
        }else if(type == (byte)2){
            byte[] desc0 = new byte[count * 4];
            byte[] desc1 = new byte[count];
            short[][] coll = new short[count][4];
            for(int i = 0; i < count; i++){
                desc0[i * 4] = stream.readByte(); //x offset
                desc0[i * 4 + 1] = stream.readByte();//y offset
                desc0[i * 4 + 2] = stream.readByte();//width
                desc0[i * 4 + 3] = stream.readByte();//height
                desc1[i] = stream.readByte();//desc
                if(stream.readByte() == 1){
                    for(int ci = 0; ci < 4; ci++)
                        coll[i][ci] = (short)(stream.readByte() /*& 0xff*/);
                }else{
                    coll[i] = null;
                }
            }

            //#if DrawMethod == DrawImage
            //# for(int i = 0; i < count; i++){
            //# tiles[i] = Image.createImage(image, desc0[i * 4] & 0xFF, desc0[i * 4 + 1] & 0xFF, desc0[i * 4 + 2] & 0xFF, desc0[i * 4 + 3] & 0xFF, desc1[i] & 0x03);
            //# }

            //# ret.frames = tiles;
            //#elif DrawMethod == DrawPixels
            //# DirectGraphics dg = DirectUtils.getDirectGraphics(image.getGraphics());
            //# for (int i = 0; i < count; i++) {
            //# int tx = desc0[i * 4] & 0xFF;
            //# int ty = desc0[i * 4 + 1] & 0xFF;
            //# int tw = desc0[i * 4 + 2] & 0xFF;
            //# int th = desc0[i * 4 + 3] & 0xFF;
            //# tiles[i] = new short[tw * th];
            //# dg.getPixels((short[])tiles[i], 0, tw, tx, ty, tw, th,
            //# dg.TYPE_USHORT_4444_ARGB);
            //# }
            //# ret.nokiaFrames = tiles;
            //#elif DrawMethod == DrawRegion
            desc = desc1;
            ret.frame = image;
            //#else
            //# ret.frame = image;
            //#endif

            ret.pos = desc0;
            ret.collision = coll;
        }else if(type == (byte)3){
            desc = new byte[count];
            miniMapColor = new int[count];
            fatherInfo = new byte[count];
            childs = new byte[count][];
            byte tileWidth = stream.readByte();
            byte tileHeight = stream.readByte();
            short tileInfo;

            for(int i = 0; i < count; i++){
                indexes[i] = stream.readByte();
                tileInfo = stream.readShort();
                desc[i] = (byte)((tileInfo >> 8) & 0xFF);
                miniMapColor[i] = COLOR_TABLE[((desc[i] >> 3) & 0x0F)];
                fatherInfo[i] = (byte)(tileInfo & 0xFF);
            }

            //计算孩子tile信息
            for(int i = 0; i < count; i++){
                int c = 0; //孩子数量

                for(int j = i; j < count; j++){
                    if(fatherInfo[j] == i){
                        c++;
                    }
                }

                if(c > 0){
                    childs[i] = new byte[c];

                    int idx = 0;

                    for(int j = i; j < count; j++){
                        if(fatherInfo[j] == i){
                            childs[i][idx++] = (byte)(j & 0xFF);
                        }
                    }
                }else{
                    childs[i] = null;
                }
            }

            //#if DrawMethod == DrawPixels
            //# int wc = image.getWidth()/tileWidth;
            //# DirectGraphics dg = DirectUtils.getDirectGraphics(image.getGraphics());
            //# for(int i=0;i<count;i++){
            //# int index = indexes[i] & 0xFF;
            //# if (tiles[index] != null) {
            //# continue;
            //# }
            //# int xx = index%wc;
            //# int yy = index/wc;
            //# tiles[index] = new short[tileWidth * tileHeight];
            //# dg.getPixels((short[])tiles[index], 0, tileWidth, xx * tileWidth,
            //# yy * tileHeight, tileWidth, tileHeight,
            //# dg.TYPE_USHORT_4444_ARGB);
            //# }
            //# ret.nokiaFrames = tiles;
            //# ret.desc = desc;
            //#elif DrawMethod == DrawImage

            //# int wc = image.getWidth() / tileWidth;

            //# for(int i = 0; i < count; i++){
            //#     int index = indexes[i];
            //#     int transform = desc[i] & 0x03; //计算翻转
            //#     int xx = index % wc;
            //#     int yy = index / wc;
            //#     tiles[i] = Image.createImage(image, xx * tileWidth, yy * tileHeight, tileWidth, tileHeight, transform);
            //# }

            //# ret.frames = tiles;
            //#else
            ret.frame = image;
            //#endif

            ret.pos = indexes;
            ret.width = tileWidth;
            ret.height = tileHeight;

            ret.childs = childs;
        }

        if(createDesc){
            ret.desc = desc;
            ret.miniMapColor = miniMapColor;
        }

        return ret;
    }

    /*    public static ImageSet createImageSet(Image image, DataInputStream stream, boolean[] usage) throws IOException{
     ImageSet ret = new ImageSet();
     byte type = stream.readByte(); //类型
     int count = stream.readByte(); //tile的数量
     byte[] desc = null;
     byte[] indexes = null;
     indexes = new byte[count];
     ret.type = type; // 必须为1

     //#if DrawMethod == DrawImage
     Image[] tiles = new Image[count];
     //#elif DrawMethod == DrawPixels
     //# Object[] tiles = new Object[count];
     //#endif

     desc = new byte[count];
     byte tileWidth = stream.readByte();
     byte tileHeight = stream.readByte();

     for(int i = 0; i < count; i++){
     indexes[i] = stream.readByte();
     desc[i] = stream.readByte();
     }

     //#if DrawMethod == DrawPixels
     //# int wc = image.getWidth()/tileWidth;
     //# DirectGraphics dg = DirectUtils.getDirectGraphics(image.getGraphics());
     //# for(int i=0;i<count;i++){
     //#     int index = indexes[i] & 0xFF;
     //#     if (tiles[index] != null || !usage[i]) {
     //# continue;
     //#     }
     //#     int xx = index%wc;
     //#     int yy = index/wc;
     //#     tiles[index] = new short[tileWidth * tileHeight];
     //#     dg.getPixels((short[])tiles[index], 0, tileWidth, xx * tileWidth,
     //# yy * tileHeight, tileWidth, tileHeight,
     //# dg.TYPE_USHORT_4444_ARGB);
     //# }
     //# ret.nokiaFrames = tiles;
     //# ret.desc = desc;
     //#elif DrawMethod == DrawImage

     int wc = image.getWidth() / tileWidth;

     for(int i = 0; i < count; i++){
     if(!usage[i]){
     continue;
     }

     int index = indexes[i];
     int transform = desc[i] & 0x03; //计算翻转
     int xx = index % wc;
     int yy = index / wc;
     tiles[i] = Image.createImage(image, xx * tileWidth, yy * tileHeight, tileWidth, tileHeight, transform);
     }

     ret.frames = tiles;
     //#else
     //# ret.frame = image;
     //#endif

     ret.pos = indexes;
     ret.width = tileWidth;
     ret.height = tileHeight;
     ret.desc = desc;

     return ret;
     }
     */
    public boolean hasChildren(){
        if(type != 3)
            return false;

        for(int i = 0; i < childs.length; i++){
            if(childs[i] != null)
                return true;
        }
        return false;

    }

    //#if "${DrawMethod}" == "SetClip"
    //# public Image mirrorImg; // 水平翻转后的图片
    //# public static final int TRANS_NONE = 0; // 不翻转
    //# public static final int TRANS_MIRROR = 2; // 水平翻转
    //# public static final int TRANS_MIRROR_ROT180 = 1; // 垂直翻转
    //# public static final int TRANS_ROT180 = 3; // 顺时针旋转180度 = 水平 + 垂直翻转
    //# public static final int TRANS_MIRROR_ROT270 = 4; // 水平翻转 + 顺时针旋转270度
    //# public static final int TRANS_ROT90 = 5; // 顺时针旋转90度
    //# public static final int TRANS_ROT270 = 6; // 顺时针旋转270度
    //# public static final int TRANS_MIRROR_ROT90 = 7; // 水平翻转 + 顺时针旋转90度 = 45度镜像

    //# public void drawClipTrans(Graphics g, int frameX, int frameY, int frameWidth, int frameHeight, int dx, int dy, int trans){
    //# int cx = g.getClipX();
    //# int cy = g.getClipY();
    //# int cw = g.getClipWidth();
    //# int ch = g.getClipHeight();
    //# switch(trans){
    //#     case TRANS_NONE:
    //#         g.setClip(dx, dy, frameWidth, frameHeight);
    //#         g.drawImage(frame, dx - frameX, dy - frameY, Graphics.TOP | Graphics.LEFT);
    //#         break;
    //#     case TRANS_MIRROR:
    //#         if(mirrorImg == null){
    //#             for(int i = 0; i < frameWidth; i++){
    //#                 g.setClip(dx + i, dy, 1, frameHeight);
    //#                 g.drawImage(frame, dx - frameX - frameWidth + i * 2 + 1, dy - frameY, Graphics.TOP | Graphics.LEFT);
    //#             }
    //#         }else{
    //#             g.setClip(dx, dy, frameWidth, frameHeight);
    //#             int nx = mirrorImg.getWidth() - frameX - frameWidth;
    //#             g.drawImage(mirrorImg, dx - nx, dy - frameY, Graphics.TOP | Graphics.LEFT);
    //#         }
    //#         break;
    //#     case TRANS_MIRROR_ROT180:
    //#         for(int i = 0; i < frameHeight; i++){
    //#             g.setClip(dx, dy + i, frameWidth, 1);
    //#             g.drawImage(frame, dx - frameX, dy - frameY - frameHeight + i * 2 + 1, Graphics.TOP | Graphics.LEFT);
    //#         }
    //#         break;
    //#     case TRANS_MIRROR | TRANS_MIRROR_ROT180:
    //#         if(mirrorImg != null){
    //#             int nx = mirrorImg.getWidth() - frameX - frameWidth;
    //#             for(int i = 0; i < frameHeight; i++){
    //#                 g.setClip(dx, dy + i, frameWidth, 1);
    //#                 g.drawImage(mirrorImg, dx - nx, dy - frameY - frameHeight + i * 2 + 1, Graphics.TOP | Graphics.LEFT);
    //#             }
    //#             break;
    //#         }
    //#     default:
    //#         g.setClip(dx, dy, frameWidth, frameHeight);
    //#         g.drawImage(frame, dx - frameX, dy - frameY, Graphics.TOP | Graphics.LEFT);
    //#         break;
    //# }
    //# g.setClip(cx, cy, cw, ch);
    //# }
    //#endif 
}
