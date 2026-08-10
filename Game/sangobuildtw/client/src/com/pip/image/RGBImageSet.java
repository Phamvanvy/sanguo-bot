package com.pip.image;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;

import com.pip.common.Tool;


public class RGBImageSet{
    private static final byte[] HEAD = {
                    'P', 'I', 'P'
    }; // 文件头
    private static final byte[] PALETTE_HEAD = {
                    'P', 'L', 'T', 'E'
    }; // 调色板块头
    private static final byte[] DATA_HEAD = {
                    'D', 'A', 'T', 'A'
    }; // 数据块头
    private static final byte[] DUNZ_HEAD = {
                    'D', 'U', 'N', 'Z'
    }; // 压缩数据块头

    private int[][] palette; // 调色板数据
    private int[] frameInfo; // 图块描述信息
    private byte[][] frameData; // 图块数据
    private int[] frameCollision; // 图块碰撞区域

    private Object[] buffer; // 缓存的RGB数据，int[]
    private Hashtable transBuffer; // 图片缓存，key是int，高16位为帧，低16位为翻转值，值是RGBImage

    /**
     * 创建PipImage对象。为了通用，本类只提供从InputStream创建的方法，避免受某些系统中访问文件内
     * 存泄漏BUG的影响。对于这类问题的处理由调用程序负责。
     * @param is 存储PipImage文件内容的流
     * @throws IOException
     */
    public RGBImageSet(InputStream is) throws IOException{
        load(new DataInputStream(is));
        buffer = new Object[palette.length * frameData.length];
        for(int i = 0; i < palette.length; i++){
            for(int j = 0; j < frameData.length; j++){
                buffer[i * frameData.length + j] = make(i, j);
            }
        }
        transBuffer = new Hashtable();
    }

    /**
     * 绘制帧。
     * @param g 绘图环境
     * @param frame 帧序号，第x个调色板的第y个图块的序号是x*m+y，其中m是图块数量
     * @param x
     * @param y
     * @param trans 翻转模式，取值参见MIDP2.0规范
     */
    public void draw(RGBGraphics g, int frame, int x, int y, int trans){
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
    public void draw(RGBGraphics g, int frame, int x, int y, int trans, int anchor){
        try{
            RGBImage drawData = getFrameData(frame, trans);
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
            g.drawImage(drawData, x, y, Graphics.TOP | Graphics.LEFT);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 得到一帧图片的组装图片数据。如果缓存中已经有了，则从缓存中取。
     */
    private RGBImage getFrameData(int frame, int trans){
        int id = (frame << 16) | trans;
        RGBImage ret = (RGBImage)transBuffer.get(new Integer(id));
        if(ret != null){
            return ret;
        }
        int[] data = (int[])buffer[frame];
        int f = frame % frameInfo.length;
        int w = (frameInfo[f] >> 10) & 0x3FF;
        int h = frameInfo[f] & 0x3FF;
        data = transit(data, w, h, trans);
        if(trans >= 4){
            int t = h;
            h = w;
            w = t;
        }
        ret = new RGBImage(data, w, h);
        transBuffer.put(new Integer(id), ret);
        return ret;
    }

    /**
     * 组装一帧图片的数据。
     * @param frame 帧序号，第x个调色板的第y个图块的序号是x*m+y，其中m是图块数量
     * @return RGB数据，NGage是int[]，其他是Image
     */
    private Object make(int p, int f){
        int w = (frameInfo[f] >> 10) & 0x3FF;
        int h = frameInfo[f] & 0x3FF;
        int[] rgb = new int[w * h];
        int[] usePal = palette[p];
        byte[] useData = frameData[f];

        int id = 0;
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                rgb[id] = usePal[useData[id] & 0xff];
                id++;
            }
        }
        return rgb;
    }

    /**
     * 从流中载入图片数据。
     */
    private void load(DataInputStream dis) throws IOException{
        // 跳过文件头，不做检查
        dis.skip(3);

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

    /**
     * 保存图片数据。在某些特定场合可能需要把解压缩后的图片数据缓存起来。注意，对于采用fullbuffer的进行，
     * 不能调用此方法，因为原始图片数据已经被摧毁了。
     */
    public void save(DataOutputStream dos) throws IOException{
        // 文件头
        dos.write(HEAD);

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
    private static final int lighter(int rgb, int v){
        int[] c = parseRGB(rgb);
        c[1] += v * 3;
        c[2] += v * 3;
        c[3] += v * 3;
        return toRGB(c[0], c[1], c[2], c[3]);
    }

    /**
     * 对一个颜色值进行处理，使它变得更暗。
     */
    private static final int darker(int rgb, int v){
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
