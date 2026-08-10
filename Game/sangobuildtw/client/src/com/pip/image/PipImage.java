package com.pip.image;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.pip.common.Tool;
import com.pip.sanguo.GameMain;
import com.pip.util.SortHashtable;

//#ifdef polish.api.nokia-ui
//# import com.nokia.mid.ui.*;
//#endif

/**
 * 分块图片类。一个图片对象可以包含多个调色板和多个图块。如果有n个调色板，m个图块，则图片中相当于有
 * n*m帧；其中第x个调色板的第y个图块的序号是x*m+y。
 * 
 * 在PipImage中，图像数据是按像素值在调色板中的索引进行存储，在需要绘制时再转换为RGB数据进行绘图。
 * 整个过程分为两个步骤：组装（把索引图像数据转换为RGB数据）和绘图（包括翻转）。在缺省模式下，组装
 * 是在绘制时才进行的，这种方式在某些机型上可能会因为绘制性能太差而无法接受。所以，在不同的机型上我
 * 们需要使用不同的组装策略，分为3种：
 * 1. 实时组装，不缓存任何RGB数据
 * 2. 优化缓存，缓存最近使用的1帧或几帧的RGB数据
 * 3. 完全创建，构造时创建好所有的RGB数据，并丢弃原始数据节省内存。这种方式耗费内存非常大，只适用
 * 于索爱K700这样拥有单独的图片内存空间的机型。
 * 系统可以在J2ME-Polish中定义PipImageMode参数来配置采用哪种组装策略，有3个取值：realtime(
 * 实时组装), halfbuffer(优化缓存), fullbuffer(完全创建)。在halfbuffer模式下，还可以用参数
 * PipImageBufferSize定义缓存大小。
 * 
 * 在本类中，一个调色板用一个int[]数组来保存。一个图块的数据用一个byte[]数组来保存，图块的描述信
 * 息被压缩到2个int中存储：第一个int从高到低存储翻转(4位)，引用帧(8位)，宽度(10位)，高度(10位);
 * 第二个int存储碰撞区域，从高到低分别是：x，y，宽度，高度，各8位。。
 */
public class PipImage{
    private static final byte[] HEAD = {
                    'P', 'I', 'P'
    }; // 文件头（不可变色）
    private static final byte[] HEAD_E = {
                    'P', 'I', 'E'
    }; // 文件头（可变色）
    private static final byte[] HEAD_M = {
                    'P', 'I', 'M'
    }; // 文件头（整合png）
    private static final byte[] HEAD_C = {
                    'P', 'J', 'P'
    }; // 文件头（16位色不可变色）
    private static final byte[] HEAD_CE = {
                    'P', 'J', 'E'
    }; // 文件头（16位色可变色）
    
    private static final byte[] PALETTE_HEAD = {
                    'P', 'L', 'T', 'E'
    }; // 调色板块头
    private static final byte[] DATA_HEAD = {
                    'D', 'A', 'T', 'A'
    }; // 数据块头
    private static final byte[] DUNZ_HEAD = {
                    'D', 'U', 'N', 'Z'
    }; // 压缩数据块头
    //#if polish.api.nokia-ui
    //#     public static final int TRANS_NONE = 0;
    //#     public static final int TRANS_MIRROR_ROT180 = 1;
    //#     public static final int TRANS_MIRROR = 2;
    //#     public static final int TRANS_ROT180 = 3;
    //#     public static final int TRANS_MIRROR_ROT270 = 4;
    //#     public static final int TRANS_ROT90 = 5;
    //#     public static final int TRANS_ROT270 = 6;
    //#     public static final int TRANS_MIRROR_ROT90 = 7;
    //#     public static final int[] TRANS_MAP = { 
    //#         0, 
    //#         DirectGraphics.FLIP_VERTICAL, 
    //#         DirectGraphics.FLIP_HORIZONTAL, 
    //#         DirectGraphics.FLIP_VERTICAL | DirectGraphics.FLIP_HORIZONTAL,
    //#         DirectGraphics.FLIP_HORIZONTAL | DirectGraphics.ROTATE_270,
    //#         DirectGraphics.ROTATE_90,
    //#         DirectGraphics.ROTATE_270,
    //#         DirectGraphics.FLIP_HORIZONTAL | DirectGraphics.ROTATE_90 
    //#     };
    //#endif

    private boolean canChangeColor;
    private int[][] palette; // 调色板数据
    private int[] frameInfo; // 图块描述信息
    private byte[][] frameData; // 图块数据
    private boolean mergeMode;
    ////#if DoubleScreen == true
    private int bytesPerPixel = 1;
    ////#endif
    private Image[] mergeImage; //合并png模式的数据图片
    private int[] frameCollision; // 图块碰撞区域

    //#if PipImageMode == halfbuffer
    private static int nextImageID = 0;
    private int imageID;
    // 缓存的图片数据，key是imgid和帧id的拼接值，value是make的结果，最后访问的在最后
    private static SortHashtable buffer = new SortHashtable();
    private static int bufferedArea = 0; // 已缓存的图片的总面积
  //#ifdef buildtest
    private static int maxBufferArea = 20000;  // 最大缓存面积
  //#endif
    //#= private static int maxBufferArea = ${PipImageBufferSize};
    //#elif PipImageMode == fullbuffer
    //# private Object[] buffer;         // 缓存的RGB数据，NGage是int[]，其他是Image
    //#endif
    //#if TransitMethod == CreateImageWithBuffer
    //# private Hashtable transBuffer; // 翻转图片缓存
    //#endif

    // 下面几个变量用于设置下次绘图时的特殊颜色变化操作，这个功能在fullbuffer情况下不起作用。
    private static final byte COLOR_OP_NONE = 0; // 不变化
    private static final byte COLOR_OP_LIGHTER = 1; // 颜色变亮
    private static final byte COLOR_OP_DARKER = 2; // 颜色变暗
    private static final byte COLOR_OP_MASK = 3; // 颜色掩码
    private static final byte COLOR_OP_CHANGE = 4; // 变换颜色
    private static final byte COLOR_OP_GRAY = 5; // 变为灰度显示
    private byte nextColorOp;
    private int colorParam1;
    private int colorParam2;

    /**
     * 创建PipImage对象。为了通用，本类只提供从InputStream创建的方法，避免受某些系统中访问文件内
     * 存泄漏BUG的影响。对于这类问题的处理由调用程序负责。
     * @param is 存储PipImage文件内容的流
     * @throws IOException
     */
    public PipImage(InputStream is) throws IOException {
        //#if PipImageMode == halfbuffer
        synchronized (buffer) {
            imageID = nextImageID;
            nextImageID++;
            if (nextImageID > 30000) {
                nextImageID = 0;
            }
        }
        //#endif
        
        load(new DataInputStream(is));
        
        if(mergeMode){
            return;
        }
        
        //#if PipImageMode == fullbuffer
        //# buffer = new Object[palette.length * frameData.length];
        //# for (int i = 0; i < palette.length; i++) {
            //# for (int j = 0; j < frameData.length; j++) {
                //# buffer[i * frameData.length + j] = make(i, j);
            //# }
        //# }
        //# frameData = null;
        //# palette = new int[palette.length][];
        //#endif
    }

    /**
     * 是否为合并png图片格式
     * @return
     */
    public boolean isMergeImage(){
        return mergeMode;
    }
    
    /**
     * 取得合并png图片
     * @return
     */
    public Image[] getMergeImage(){
        return mergeImage;
    }
    
    /**
     * 取得合并模式的frame定义数据
     * @return
     */
    public int[] getMergeFrameInfo(){
        return frameInfo;
    }
    
    /**
     * 绘制帧。
     * @param g 绘图环境
     * @param frame 帧序号，第x个调色板的第y个图块的序号是x*m+y，其中m是图块数量
     * @param x
     * @param y
     * @param trans 翻转模式，取值参见MIDP2.0规范
     */
    public void draw(Graphics g, int frame, int x, int y, int trans){
        draw(g, frame, x, y, trans, Graphics.LEFT | Graphics.TOP);
    }

    /**
     * 绘制帧。
     * @param g 绘图环境
     * @param frame 帧序号，第x个调色板的第y个图块的序号是x*m+y，其中m是图块数量
     * @param x
     * @param y
     * @param trans 翻转模式，取值参见MIDP2.0规范
     * @param anchor 链接点，取值参见MIDP1.0规范
     */
    public void draw(Graphics g, int frame, int x, int y, int trans, int anchor){
        Object drawData = getFrameData(frame);
        int fid = frame % frameInfo.length;
        int w = (frameInfo[fid] >> 10) & 0x3FF;
        int h = frameInfo[fid] & 0x3FF;
        if(trans < 4){
            if((anchor & Graphics.HCENTER) > 0){
                x -= w / 2;
            }else if((anchor & Graphics.RIGHT) > 0){
                x -= w;
            }
            if((anchor & Graphics.VCENTER) > 0){
                y -= h / 2;
            }else if((anchor & Graphics.BOTTOM) > 0){
                y -= h;
            }
        }else{
            if((anchor & Graphics.HCENTER) > 0){
                x -= h / 2;
            }else if((anchor & Graphics.RIGHT) > 0){
                x -= h;
            }
            if((anchor & Graphics.VCENTER) > 0){
                y -= w / 2;
            }else if((anchor & Graphics.BOTTOM) > 0){
                y -= w;
            }
        }
        //#if !polish.midp2
        //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);
        //# dg.drawPixels((int[])drawData, true, 0, w, x, y, w, h, TRANS_MAP[trans], DirectGraphics.TYPE_INT_8888_ARGB);
        //#else
        //#if ModelID == Nokia7610
        // Nokia7610的createRGBImage不能处理超过4K的数据，所以对于超过4K的数据要特殊处理
        if(drawData instanceof Image[]){
            Image[] imgs = (Image[])drawData;
            
            for (int i  = 0;  i< imgs.length; i++) {
                if (trans == 0) {
                     g.drawImage(imgs[i], x, y, Graphics.TOP | Graphics.LEFT);
                } else {
                    int tw = imgs[i].getWidth();
                    int th = imgs[i].getHeight();
                    
                    //#if TransitMethod == NokiaUI
                    //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);
                    //# dg.drawImage(imgs[i], x, y, Graphics.TOP | Graphics.LEFT, TRANS_MAP[trans]);
                    //#elif TransitMethod == CreateImageWithBuffer
                        //# if (transBuffer == null) {
                            //# transBuffer = new Hashtable();
                        //# }
                        //# Integer key = new Integer((trans << 24) | (frame << 12) | i);
                        //# Image frameImg = (Image)transBuffer.get(key);
                        //# if (frameImg == null) {
                            //# frameImg = Image.createImage(imgs[i], 0, 0, tw, th, trans);
                            //# transBuffer.put(key, frameImg);
                        //# }
                        //# g.drawImage(frameImg, x, y, Graphics.TOP | Graphics.LEFT);
                    //#elif TransitMethod == CreateImage
                        //# Image frameImg = Image.createImage(imgs[i], 0, 0, w, h, trans);
                        //# g.drawImage(frameImg, x, y, Graphics.TOP | Graphics.LEFT);
                    //#else
                    g.drawRegion(imgs[i], 0, 0, tw, th, trans, x, y, Graphics.TOP | Graphics.LEFT);
                    //#endif
                }

                y += imgs[i].getHeight();
            }
            return;
        }
        //#endif
        if (trans == 0) {
            g.drawImage((Image)drawData, x, y, Graphics.TOP | Graphics.LEFT);
        } else {
            //#if TransitMethod == NokiaUI
            //# DirectGraphics dg = DirectUtils.getDirectGraphics(g);
            //# dg.drawImage((Image)drawData, x, y, Graphics.TOP | Graphics.LEFT, TRANS_MAP[trans]);
            //#elif TransitMethod == CreateImageWithBuffer
                //# if (transBuffer == null) {
                    //# transBuffer = new Hashtable();
                //# }
                //# Integer key = new Integer((trans << 24) | frame);
                //# Image frameImg = (Image)transBuffer.get(key);
                //# if (frameImg == null) {
                    //# frameImg = Image.createImage((Image)drawData, 0, 0, w, h, trans);
                    //# transBuffer.put(key, frameImg);
                //# }
                //# g.drawImage(frameImg, x, y, Graphics.TOP | Graphics.LEFT);
            //#elif TransitMethod == CreateImage
                //# Image frameImg = Image.createImage((Image)drawData, 0, 0, w, h, trans);
                //# g.drawImage(frameImg, x, y, Graphics.TOP | Graphics.LEFT);
            //#else
            g.drawRegion((Image)drawData, 0, 0, w, h, trans, x, y, Graphics.TOP | Graphics.LEFT);
            //#endif
        }
        //#endif
    }

    /**
     * 得到一帧图片的组装图片数据。如果缓存中已经有了，则从缓存中取。
     * @param frame 帧序号，第x个调色板的第y个图块的序号是x*m+y，其中m是图块数量
     * @return RGB数据，NGage是int[]，其他是Image
     */
    private Object getFrameData(int frame){
        //#if PipImageMode == halfbuffer
        // 如果有变色操作，不缓存
        int fid = frame % frameInfo.length;
        int pid = frame / frameInfo.length;
        if (nextColorOp != COLOR_OP_NONE) {
            return make(pid, fid);
        }
        
        // 在缓存中查找，如果有，则把缓存移动到队列最后，并返回缓存
        Integer key = new Integer((imageID << 16) | frame);
        Object ret = buffer.get(key);
        if (ret != null) {
            buffer.put(key, ret);
            return ret;
        }
        
        // 如果缓存中没有，创建并加入缓存
        ret = make(pid, fid);
        int newArea = getArea(ret);
        synchronized(buffer) {
            // 如果缓存过大了，从第一个开始删除，直到缓存空或者够大
            Object[] keys = null;
            int deleteIndex = 0;
            while (bufferedArea + newArea > maxBufferArea) {
                if (keys == null) {
                    keys = buffer.keys();
                }
                if (deleteIndex >= keys.length) {
                    break;
                }
                Object oldData = buffer.get(keys[deleteIndex]);
                bufferedArea -= getArea(oldData);
                buffer.remove(keys[deleteIndex]);
                deleteIndex++;
            }
            
            // 新数据加入缓存
            bufferedArea += newArea;
            buffer.put(key, ret);
        }
        return ret;
        //#elif PipImageMode == fullbuffer
        //# return buffer[frame];
        //#else
        //# int fid = frame % frameInfo.length;
        //# int pid = frame / frameInfo.length;
        //# return make(pid, fid);
        //#endif
    }
    
    /*
     * 计算一个图片数据的面积。
     */
    private int getArea(Object data) {
        if (data instanceof int[]) {
            return ((int[])data).length;
        } else if (data instanceof Image[]) {
            Image[] arr = (Image[])data;
            int ret = 0;
            for (int i = 0; i < arr.length; i++) {
                ret += arr[i].getWidth() * arr[i].getHeight();
            }
            return ret;
        } else {
            Image img = (Image)data;
            return img.getWidth() * img.getHeight();
        }
    }

    /**
     * 组装一帧图片的数据。
     * @param frame 帧序号，第x个调色板的第y个图块的序号是x*m+y，其中m是图块数量
     * @return RGB数据，NGage是int[]，其他是Image
     */
    private Object make(int p, int f){
        //TODO delete
        GameMain.makeTimes++;
        
        int w = (frameInfo[f] >> 10) & 0x3FF;
        int h = frameInfo[f] & 0x3FF;
        int[] rgb = new int[w * h];
        int[] usePal = palette[p];
        byte[] useData = frameData[f];

        if(nextColorOp != COLOR_OP_NONE){
            usePal = performColorOp(usePal);
        }

        int id = 0;
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                ////#if DoubleScreen == true
                if (bytesPerPixel == 1) {
            	     rgb[id] = usePal[useData[id] & 0xff];
            	     id++;
            	} else {
            	     int clr = ((useData[id * 2] & 0xFF) << 8) | (useData[id * 2 + 1] & 0xFF);
            	     rgb[id] = usePal[clr];
            	     id++;
            	}
                ////#else
                //rgb[id] = usePal[useData[id] & 0xff];
                //id++;
                ////#endif
            }
        }
        //#if !polish.midp2
        //# return rgb;
        //#elif ModelID == Nokia7610
        // 7610版本有严重BUG，createRGBImage时数据长度超过4K将会导致手机死机，所以对于超过4K的数据要特殊处理
        if (w * h > 3600) {
            int h2 = 3600 / w;
            Image[] ret = new Image[(h + h2 - 1) / h2];
            int rdx = 0;
            for (int s = 0; s < h; s += h2) {
                int start = s * w;
                int h3 = h2;
                if (s + h3 > h) {
                    h3 = h - s;
                }
                int len = h3 * w;
                int[] tarr = new int[len];
                System.arraycopy(rgb, start, tarr, 0, len);
            	ret[rdx++] = Image.createRGBImage(tarr, w, h3, true);
            }
            
            return ret;
        } else {
            return Image.createRGBImage(rgb, w, h, true);
        }
        //#else
        //# return Image.createRGBImage(rgb, w, h, true);
        //#endif
    }

    /**
     * 从流中载入图片数据。
     */
    private void load(DataInputStream dis) throws IOException{
        // 跳过文件头，不做检查
        byte[] head = new byte[3];
        dis.read(head);
        
        if(head[2] == HEAD_M[2]){
            mergeMode = true;
            
            int frameDataLength = dis.readUnsignedShort();
            byte[] frameGzipData = new byte[frameDataLength];
            dis.read(frameGzipData);
            
            ByteArrayInputStream fbis = new ByteArrayInputStream(Tool.inflate(frameGzipData));
            DataInputStream fdis = new DataInputStream(fbis);
            
            int frameCount = fdis.readUnsignedByte();
            frameInfo = new int[frameCount * 2];
            
            for(int i = 0; i < frameCount; i++){
                int frameX = fdis.readUnsignedShort();
                int frameY = fdis.readUnsignedShort();
                int frameWidth = fdis.readUnsignedByte();
                int frameHeight = fdis.readUnsignedByte();
                
                frameInfo[i << 1] = (frameX << 16) | frameY;
                frameInfo[(i << 1) + 1] = (frameWidth << 16) | frameHeight;
            }
            
            fdis.close();
            
            int imgCount = dis.readByte() & 0xFF;
            byte[] palette = new byte[dis.readShort() & 0xFFFF];
            dis.readFully(palette);
            mergeImage = new Image[imgCount];
            for (int i = 0; i < imgCount; i++) {
                int dataLen = dis.readShort() & 0xFFFF;
                byte[] pngData = new byte[palette.length + dataLen];
                dis.read(pngData, 0, 33);
                System.arraycopy(palette, 0, pngData, 33, palette.length);
                dis.read(pngData, 33 + palette.length, dataLen - 33);
                mergeImage[i] = Image.createImage(pngData, 0, pngData.length);
            }
        }else{
            mergeMode = false;
            
            if(head[2] == HEAD_E[2]){
                canChangeColor = true;
            }else{
                canChangeColor = false;
            }
            
            ////#if DoubleScreen == true
             if (head[1] == HEAD_C[1]) {
              bytesPerPixel = 2;
           } else {
              bytesPerPixel = 1;
           }
            ////#endif
    
            // 读取调色板数据
            int c = dis.readByte() & 0xff;
            palette = new int[c][];
            for(int i = 0; i < c; i++){
                palette[i] = readPalette(dis);
            }
    
            // 读取图块数据
            int size = dis.readByte() & 0xff;
            frameInfo = new int[size];
            frameData = new byte[size][];
            frameCollision = new int[size];
            for(int i = 0; i < size; i++){
                readFrame(dis, i);
            }
        }
    }

    /**
     * 保存图片数据。在某些特定场合可能需要把解压缩后的图片数据缓存起来。注意，对于采用fullbuffer的进行，
     * 不能调用此方法，因为原始图片数据已经被摧毁了。
     */
    public void save(DataOutputStream dos) throws IOException{
        // 文件头
        if (canChangeColor) {
            dos.write(HEAD_E);
        }else{
            dos.write(HEAD);
        }

        // 调色板数据
        dos.writeByte(palette.length);
        for(int i = 0; i < palette.length; i++){
            writePalette(dos, palette[i]);
        }

        // 图块数据
        dos.writeByte(frameInfo.length);
        for(int i = 0; i < frameInfo.length; i++){
            writeFrame(dos, i);
        }
    }

    /**
     * 得到某一帧图片的宽度。
     */
    public int getWidth(int frame){
        int ff = frame % frameInfo.length;
        return (frameInfo[ff] >> 10) & 0x3FF;
    }

    /**
     * 得到某一帧图片的高度。
     */
    public int getHeight(int frame){
        int ff = frame % frameInfo.length;
        return frameInfo[ff] & 0x3FF;
    }

    /**
     * 得到可绘制的帧总数量。
     */
    public int getFrameCount(){
        return frameInfo.length * palette.length;
    }

    /**
     * 得到不计算调色板重复的帧数量。
     */
    public int getBlockCount(){
        return frameInfo.length;
    }

    /**
     * 得到调色板数量。
     */
    public int getPaletteCount(){
        return palette.length;
    }

    /**
     * 替换某一个调色板。
     */
    public void replacePalette(int index, int[] data){
        palette[index] = data;
    }

    /**
     * 读取一个调色板。
     */
    private int[] readPalette(DataInputStream dis) throws IOException{
        int len = dis.readInt();
        int[] ret = new int[len];
        dis.skip(4);
        for(int i = 0; i < ret.length; i++){
            ret[i] = dis.readInt();
        }
        return ret;
    }

    /**
     * 写出一个调色板。
     */
    private void writePalette(DataOutputStream dos, int[] pdata) throws IOException{
        dos.writeInt(pdata.length);
        dos.write(PALETTE_HEAD);
        for(int i = 0; i < pdata.length; i++){
            dos.writeInt(pdata[i]);
        }
    }

    /**
     * 读取一个图块数据。
     */
    private void readFrame(DataInputStream dis, int index) throws IOException{
        int len = dis.readInt() - 6;
        byte[] head = new byte[4];
        dis.readFully(head);

        int flip = dis.readByte(); // flip flag, ignored
        int frame = dis.readByte(); // frame index, ignored
        int width = dis.readShort();
        int height = dis.readShort();
        int collision = 0;
        byte c = dis.readByte(); // collision flag, ignored
        if(c == 1){
            collision = dis.readInt();
        }
        byte[] data = new byte[len];
        dis.readFully(data);
        if(head[1] == 'A'){
            data = Tool.inflate(data);
        }

        frameData[index] = data;
        frameInfo[index] = ((flip & 0x07) << 28) | ((frame & 0xFF) << 20) | ((width & 0x3FF) << 10) | (height & 0x3FF);
        frameCollision[index] = collision;
    }

    /**
     * 写出一个图块数据。
     */
    private void writeFrame(DataOutputStream dos, int index) throws IOException{
        dos.writeInt(frameData[index].length + 6);
        dos.write(DUNZ_HEAD);
        int info = frameInfo[index];
        dos.writeByte(info >> 28);
        dos.writeByte(info >> 20);
        dos.writeShort((info >> 10) & 0x3FF);
        dos.writeShort(info & 0x3FF);
        if(frameCollision[index] == 0){
            dos.writeByte(0);
        }else{
            dos.writeByte(0);
            dos.writeInt(frameCollision[index]);
        }
        dos.write(frameData[index]);
    }

    /**
     * 把ARGB值拼成一个int值。
     */
    private static final int toRGB(int a, int r, int g, int b){
        if(a > 255)
            a = 255;
        if(a < 0)
            a = 0;
        if(r > 255)
            r = 255;
        if(g > 255)
            g = 255;
        if(b > 255)
            b = 255;
        if(r < 0)
            r = 0;
        if(g < 0)
            g = 0;
        if(b < 0)
            b = 0;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * 把一个int值拆成ARGB值。
     */
    private static final int[] parseRGB(int rgb){
        int aa = (rgb >> 24) & 0xFF;
        int rr = (rgb >> 16) & 0xFF;
        int gg = (rgb >> 8) & 0xFF;
        int bb = rgb & 0xFF;
        return new int[]{
                        aa, rr, gg, bb
        };
    }

    /**
     * 对一个颜色值进行处理，使它变得更亮。
     */
    public static final int lighter(int rgb, int v){
        int[] c = parseRGB(rgb);
        c[1] += v * 3;
        c[2] += v * 3;
        c[3] += v * 3;
        return toRGB(c[0], c[1], c[2], c[3]);
    }

    /**
     * 对一个颜色值进行处理，使它变得更暗。
     */
    public static final int darker(int rgb, int v){
        int[] c = parseRGB(rgb);
        c[1] -= v * 3;
        c[2] -= v * 3;
        c[3] -= v * 3;
        return toRGB(c[0], c[1], c[2], c[3]);
    }

    /**
     * 对一个颜色值进行处理，使它变成灰度。
     */
    private static final int gray(int rgb){
        int[] c = parseRGB(rgb);
        int y = (c[1] * 299 / 1000) + (c[2] * 587 / 1000) + (c[3] * 114 / 1000);
        c[1] = y;
        c[2] = y;
        c[3] = y;
        return toRGB(c[0], c[1], c[2], c[3]);
    }

    /**
     * 使下一次使用本对象绘图时变得更亮。
     */
    public void lighter(int value){
        nextColorOp = COLOR_OP_LIGHTER;
        colorParam1 = value;
    }

    /**
     * 使下一次使用本对象绘图时变得更暗。
     */
    public void darker(int value){
        nextColorOp = COLOR_OP_DARKER;
        colorParam1 = value;
    }

    /**
     * 使下一次使用本对象绘图时加上一个掩码。
     */
    public void mask(int value){
        nextColorOp = COLOR_OP_MASK;
        colorParam1 = value;
    }

    /**
     * 使下一次使用本对象绘图时把一个颜色替换为另外一个颜色。
     */
    public void changeColor(int src, int dest){
        nextColorOp = COLOR_OP_CHANGE;
        colorParam1 = src;
        colorParam2 = dest;
    }

    /**
     * 使下一次使用本对象绘图时变为灰度显示。
     */
    public void gray(){
        nextColorOp = COLOR_OP_GRAY;
    }

    /**
     * 对一个调色板数据执行下一次颜色变换操作，并返回新的调色板数据。
     */
    private int[] performColorOp(int[] pal){
        int[] ret = new int[pal.length];
        for(int i = 0; i < ret.length; i++){
            switch(nextColorOp){
                case COLOR_OP_LIGHTER:
                    ret[i] = lighter(pal[i], colorParam1);
                    break;
                case COLOR_OP_DARKER:
                    ret[i] = darker(pal[i], colorParam1);
                    break;
                case COLOR_OP_MASK:
                    ret[i] = pal[i] | colorParam1;
                    break;
                case COLOR_OP_CHANGE:
                    if(pal[i] == colorParam1){
                        ret[i] = colorParam2;
                    }else{
                        ret[i] = pal[i];
                    }
                    break;
                case COLOR_OP_GRAY:
                    ret[i] = gray(pal[i]);
                    break;
            }
        }
        nextColorOp = COLOR_OP_NONE;
        return ret;
    }

    /**
     * 对一个图片的RGB数据进行翻转操作。本方法目前没有被使用。
     * @param rgb 图片数据，本方法不会修改原始数据
     * @param w 图片宽度
     * @param h 图片高度
     * @param trans 翻转类型，取值参见MIDP2.0规范
     * @return 翻转后的图片RGB数据
     */
    public static int[] transit(int[] rgb, int w, int h, int trans){
        if(trans == 0){
            return rgb;
        }
        int[][] ret;
        if(trans < 4){
            ret = new int[h][w];
        }else{
            ret = new int[w][h];
        }
        int srcpos = 0;
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                switch(trans){
                    case 1:
                        ret[h - 1 - y][x] = rgb[srcpos];
                        break;
                    case 2:
                        ret[y][w - 1 - x] = rgb[srcpos];
                        break;
                    case 3:
                        ret[h - 1 - y][w - 1 - x] = rgb[srcpos];
                        break;
                    case 4:
                        ret[x][y] = rgb[srcpos];
                        break;
                    case 5:
                        ret[x][h - 1 - y] = rgb[srcpos];
                        break;
                    case 6:
                        ret[w - 1 - x][y] = rgb[srcpos];
                        break;
                    case 7:
                        ret[w - 1 - x][h - 1 - y] = rgb[srcpos];
                        break;
                }
                srcpos++;
            }
        }
        int[] ret2 = new int[w * h];
        int tarpos = 0;
        for(int i = 0; i < ret.length; i++){
            System.arraycopy(ret[i], 0, ret2, tarpos, ret[i].length);
            tarpos += ret[i].length;
        }
        return ret2;
    }
}
