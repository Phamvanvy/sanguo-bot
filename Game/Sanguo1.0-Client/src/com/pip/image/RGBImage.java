package com.pip.image;


import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


public class RGBImage{
    private int[] data;
    private Object[] immData;
    private boolean immMode;
    private int width;
    private int height;
    private int foreGround = 0xFFFFFFFF;
    private int alpha = 0xFF;

    private int[] clearLine;

    private RGBImage(){
    }

    public static RGBImage createImage(int width, int height){
        RGBImage image = new RGBImage();

        image.clearLine = new int[width];

        image.width = width;
        image.height = height;
        image.data = new int[width * height];
        //image.clear();
        image.immMode = false;

        return image;
    }

    public static RGBImage createImage(String fname) throws IOException{
        Image img = Image.createImage(fname);

        return new RGBImage(img);
    }

    public static RGBImage createImage(byte[] in, int offset, int length) throws IOException{
        Image img = Image.createImage(in, offset, length);
        return new RGBImage(img);
    }

    public static RGBImage createRGBImage(int[] in, int width, int height, boolean alpha){
        RGBImage result = RGBImage.createImage(width, height);

        System.arraycopy(in, 0, result.data, 0, Math.min(in.length, result.data.length));

        if(!alpha){
            for(int i = 0; i < result.clearLine.length; i++){
                result.clearLine[i] = 0x00000000;
            }

            for(int i = 0; i < result.data.length; i++){
                result.data[i] |= 0xFF000000;
            }
        }

        return result;
    }

    public RGBImage(Image image){
        width = image.getWidth();
        height = image.getHeight();

        clearLine = new int[width];

        data = new int[width * height];
        image.getRGB(data, 0, width, 0, 0, width, height);
        immMode = true;

        makeImmData(data);
        data = null;
    }

    private RGBImage(Image image, int alpha){
        width = image.getWidth();
        height = image.getHeight();

        clearLine = new int[width];

        data = new int[width * height];
        image.getRGB(data, 0, width, 0, 0, width, height);
        immMode = true;

        makeImmData(data, alpha);
        data = null;
    }

    public RGBImage(int[] tmp, int w, int h){
        width = w;
        height = h;

        clearLine = new int[width];

        immMode = true;

        makeImmData(tmp, alpha);
        data = null;
    }

    public void setAlpha(int alpha){
        this.alpha = alpha;
    }

    private void makeImmData(int[] array, int alpha){
        int[] singleLine = new int[width];
        immData = new Object[height];

        for(int i = 0; i < height; i++){
            System.arraycopy(array, i * width, singleLine, 0, width);

            Vector line = new Vector();

            for(int j = 0; j < width;){
                if((singleLine[j] & 0xFF000000) == 0){
                    j++;
                    continue;
                }

                int k = j;

                for(; k < width; k++){
                    if((singleLine[k] & 0xFF000000) == 0){
                        break;
                    }
                }

                int size = k - j;

                int[] subData = new int[size + 2];
                subData[0] = j;
                subData[1] = size;

                System.arraycopy(singleLine, j, subData, 2, size);
                line.addElement(subData);

                j = k + 1;
            }

            if(line.size() > 0){
                Object[] tmp = new Object[line.size()];
                line.copyInto(tmp);
                immData[i] = tmp;
            }else{
                immData[i] = null;
            }
        }
    }

    private void makeImmData(int[] array){
        int[] singleLine = new int[width];
        immData = new Object[height];

        for(int i = 0; i < height; i++){
            System.arraycopy(array, i * width, singleLine, 0, width);

            Vector line = new Vector();

            for(int j = 0; j < width;){
                if(singleLine[j] == 0xFFFFFFFF || singleLine[j] == 0x00FFFFFF || singleLine[j] == 0){
                    j++;
                    continue;
                }

                int k = j;

                for(; k < width; k++){
                    if(singleLine[k] == 0xFFFFFFFF || singleLine[k] == 0x00FFFFFF || singleLine[k] == 0){
                        break;
                    }
                }

                int size = k - j;

                int[] subData = new int[size + 2];
                subData[0] = j;
                subData[1] = size;

                System.arraycopy(singleLine, j, subData, 2, size);
                line.addElement(subData);

                j = k + 1;
            }

            if(line.size() > 0){
                Object[] tmp = new Object[line.size()];
                line.copyInto(tmp);
                immData[i] = tmp;
            }else{
                immData[i] = null;
            }
        }
    }

    private void drawImmData(ImageRGBGraphics g, int x, int y, int src_x, int src_y, int src_w, int src_h){
        int cx = g.getClipX();
        int cy = g.getClipY();
        int cw = g.getClipWidth();
        int ch = g.getClipHeight();

        if(x > cx + cw || y > cy + ch){
            return;
        }

        int rx, ry, rw, rh;

        rx = x;
        ry = y;
        rw = Math.min(src_w, getWidth());
        rh = Math.min(src_h, getHeight());

        if(rx < cx){
            rw += rx - cx;
            src_x -= rx - cx;
            rx = cx;
        }

        if(ry < cy){
            rh += ry - cy;
            src_y -= ry - cy;
            ry = cy;
        }

        if(rx + rw > cx + cw){
            rw = cx + cw - rx;
        }

        if(ry + rh > cy + ch){
            rh = cy + ch - ry;
        }

        int tx = src_x;
        int ty = src_y;

        for(int i = 0; i < rh; i++){
            if(i + ty < 0 || i + ty >= immData.length || immData[i + ty] == null){
                continue;
            }else{
                Object[] data = (Object[])immData[i + ty];

                for(int j = 0; j < data.length; j++){
                    int[] subData = (int[])data[j];
                    int subx = subData[0];
                    int subw = subData[1];

                    if(subx < tx){
                        subw -= tx - subx;
                        subx = tx;
                    }

                    if(subx + subw > tx + rw){
                        subw -= (subx + subw) - (tx + rw);
                    }

                    if(subw < 0){
                        continue;
                    }

                    if(alpha < 0xFF){
                        for(int k = 0; k < subw; k++){
                            int tmp = (i + ry) * g.rgbImage.width + rx + subx - src_x + k;

                            g.rgbImage.data[tmp] = alphaColor(g.rgbImage.data[tmp], subData[2 + (subx - subData[0]) + k], alpha);
                        }
                    }else{
                        System.arraycopy(subData, 2 + (subx - subData[0]), g.rgbImage.data, (i + ry) * g.rgbImage.width + rx + subx - src_x, subw);
                    }
                }
            }
        }
    }

    private int alphaColor(int color1, int color2, int alpha){
        if(alpha == 0x80){
            int mc = (color1 + color2);

            return (((mc & 0xfefefe) + (((color1 ^ color2) ^ mc) & 0x1010100)) >>> 1);
        }else{
            int r1, g1, b1, r2, g2, b2, nr, ng, nb;

            r1 = (color1 >> 16) & 0xFF;
            g1 = (color1 >> 8) & 0xFF;
            b1 = color1 & 0xFF;

            r2 = (color2 >> 16) & 0xFF;
            g2 = (color2 >> 8) & 0xFF;
            b2 = color2 & 0xFF;

            nr = (r1 * (0xFF - alpha) + r2 * alpha) / 0xFF;
            ng = (g1 * (0xFF - alpha) + g2 * alpha) / 0xFF;
            nb = (b1 * (0xFF - alpha) + b2 * alpha) / 0xFF;

            nr = ((r1 + r2) >> 1);
            ng = ((g1 + g2) >> 1);
            nb = ((b1 + b2) >> 1);

            return (nr << 16) | (ng << 8) | nb | 0xFF000000;
        }
    }

    private void setForeground(int value){
        foreGround = value;
    }

    private int getForeground(){
        return foreGround;
    }

    public void clear(){
        for(int i = 0; i < height; i++){
            System.arraycopy(clearLine, 0, data, i * width, width);
        }
    }

    protected int[] getLine(int x, int y, int width){
        int tmp = y * this.width + x;

        if(tmp < 0 || width <= 0){
            return new int[0];
        }

        int[] result = new int[width];

        System.arraycopy(data, tmp, result, 0, width);

        return result;
    }

    private void setPoint(int x, int y){
        data[y * width + x] = foreGround;
    }

    protected void setLine(int[] inData, int x, int y){
        if(x < 0){
            x = 0;
        }

        if(y < 0){
            y = 0;
        }

        System.arraycopy(inData, 0, data, y * width + x, inData.length);
        //        for(int i = 0; i < inData.length; i++){
        //            if((inData[i] & 0xFF000000) != 0){
        //                data[y * width + x + i] = inData[i];
        //            }
        //        }
    }

    public RGBGraphics getGraphics(){
        if(immMode){
            return null;
        }else{
            return new ImageRGBGraphics(this);
        }
    }

    public void draw(Graphics g, int x, int y){
        g.drawRGB(data, 0, width, x, y, width, height, false);
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    class ImageRGBGraphics extends RGBGraphics{
        private RGBImage rgbImage;
        private Font font;

        private Image fontImage;

        public ImageRGBGraphics(RGBImage image){
            rgbImage = image;
            drawWidth = rgbImage.getWidth();
            drawHeight = rgbImage.getHeight();
            setClip(0, 0, drawWidth, drawHeight);
            font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        }

        public void copyArea(int x_src, int y_src, int width, int height, int x_dest, int y_dest, int anchor){
            for(int i = 0; i < height; i++){
                int[] subLine = rgbImage.getLine(x_src, y_src, width);
                rgbImage.setLine(subLine, correctX(x_dest, width, anchor), correctY(y_dest, height, anchor));
            }
        }

        public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle){
            System.out.println("not support drawArc");
        }

        public void drawChar(char character, int x, int y, int anchor){
            System.out.println("not support drawChare");
        }

        public void drawChars(char[] data, int offset, int length, int x, int y, int anchor){
            System.out.println("not support drawChars");
        }

        public void drawImage(RGBImage image, int x, int y, int anchor){
            int cx = correctX(x, image.getWidth(), anchor);
            int cy = correctY(y, image.getHeight(), anchor);

            saveClip();
            clipRect(cx, cy, image.getWidth(), image.getHeight());

            if(image.immMode){
                image.drawImmData(this, cx, cy, 0, 0, image.getWidth(), image.getHeight());
            }else{
                for(int i = deCorrectY(clipY, image.getHeight(), anchor) - y; i < clipY - cy + clipHeight; i++){
                    int[] subLine = null;
                    subLine = image.getLine(deCorrectX(clipX, image.getWidth(), anchor) - x, i, clipWidth);
                    rgbImage.setLine(subLine, cx, cy + i);
                }
            }

            restoreClip();
        }

        public void drawCircle(int x0, int y0, int radius){
            int f = 1 - radius;
            int ddF_x = 0;
            int ddF_y = -2 * radius;
            int x = 0;
            int y = radius;

            rgbImage.setPoint(x0, y0 + radius);
            rgbImage.setPoint(x0, y0 - radius);
            rgbImage.setPoint(x0 + radius, y0);
            rgbImage.setPoint(x0 - radius, y0);

            while(x < y){
                if(f >= 0){
                    y--;
                    ddF_y += 2;
                    f += ddF_y;
                }

                x++;
                ddF_x += 2;
                f += ddF_x + 1;

                rgbImage.setPoint(x0 + x, y0 + y);
                rgbImage.setPoint(x0 - x, y0 + y);
                rgbImage.setPoint(x0 + x, y0 - y);
                rgbImage.setPoint(x0 - x, y0 - y);
                rgbImage.setPoint(x0 + y, y0 + x);
                rgbImage.setPoint(x0 - y, y0 + x);
                rgbImage.setPoint(x0 + y, y0 - x);
                rgbImage.setPoint(x0 - y, y0 - x);
            }
        }

        public void drawLine(int x0, int y0, int x1, int y1){
            int tmp, deltax, deltay, error, ystep, x, y;
            boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);

            if(steep){
                tmp = x0;
                x0 = y0;
                y0 = tmp;
                tmp = x1;
                x1 = y1;
                y1 = tmp;
            }

            if(x0 > x1){
                tmp = x0;
                x0 = x1;
                x1 = tmp;
                tmp = y0;
                y0 = y1;
                y1 = tmp;
            }

            deltax = x1 - x0;
            deltay = Math.abs(y1 - y0);
            error = -deltax / 2;
            y = y0;

            if(y0 < y1){
                ystep = 1;
            }else{
                ystep = -1;
            }

            for(x = x0; x <= x1; x++){
                if(steep){
                    rgbImage.setPoint(y, x);
                }else{
                    rgbImage.setPoint(x, y);
                }

                error = error + deltay;

                if(error > 0){
                    y += ystep;
                    error -= deltax;
                }
            }
        }

        public void drawRect(int x, int y, int width, int height){
            width--;
            height--;

            drawLine(x, y, x + width, y);
            drawLine(x, y, x, y + height);
            drawLine(x + width, y, x + width, y + height);
            drawLine(x, y + height, x + width, y + height);
        }

        public void drawRegion(RGBImage image, int x_src, int y_src, int width, int height, int transform, int x_dest, int y_dest, int anchor){
            int cx = correctX(x_dest, width, anchor);
            int cy = correctY(y_dest, height, anchor);

            saveClip();
            clipRect(cx, cy, width, height);

            if(image.immMode){
                image.drawImmData(this, cx, cy, x_src, y_src, width, height);
            }else{
                for(int i = deCorrectY(clipY, height, anchor) - y_dest + y_src; i < y_src + (clipY - cy) + clipHeight; i++){
                    int[] subLine = image.getLine(deCorrectX(clipX, width, anchor) - x_dest + x_src, i, clipWidth);
                    rgbImage.setLine(subLine, cx, cy + i - y_src);
                }
            }

            restoreClip();
        }

        public void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height, boolean processAlpha){
            System.out.println("not support drawRGB");
        }

        public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight){
            System.out.println("not support drawRoundRect");
        }

        public void drawString(String str, int x, int y, int anchor){
            if(str == null || str.length() == 0){
                return;
            }

            fontImage = Image.createImage(font.stringWidth(str), font.getHeight());
            Graphics g = fontImage.getGraphics();

            g.setColor(0xFFFFFFFE);
            g.fillRect(0, 0, fontImage.getWidth(), fontImage.getHeight());
            g.setFont(font);
            g.setColor(rgbImage.getForeground());
            g.drawString(str, 0, 0, Graphics.TOP | Graphics.LEFT);

            RGBImage img = new RGBImage(fontImage, 0xFFFFFFFE);

            drawImage(img, x, y, anchor);
        }

        public void drawSubstring(String str, int offset, int len, int x, int y, int anchor){
            String subString = str.substring(offset, offset + len);
            drawString(subString, x, y, anchor);
        }

        public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle){
            System.out.println("not support fillArc");
        }

        public void fillRect(int x, int y, int width, int height){
            int[] tmp = new int[width];

            for(int i = 0; i < tmp.length; i++){
                tmp[i] = foreGround;
            }
            for(int i = 0; i < height; i++){
                rgbImage.setLine(tmp, x, y + i);

                //leo*** drawLine(x, y + i, x + width - 1, y + i);
            }
        }

        public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight){
            System.out.println("not support fillRoundRect");
        }

        public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3){
            drawLine(x1, y1, x2, y2);
            drawLine(x1, y1, x3, y3);
            drawLine(x2, y2, x3, y3);
        }

        public int getBlueComponent(){
            System.out.println("not support getBlueComponent");

            return 0;
        }

        public int getColor(){
            return rgbImage.getForeground();
        }

        public int getDisplayColor(int arg0){
            return rgbImage.getForeground();
        }

        public Font getFont(){
            return font;
        }

        public int getGrayScale(){
            System.out.println("not support getGrayScale");

            return 0;
        }

        public int getGreenComponent(){
            System.out.println("not support getGreenComponent");

            return 0;
        }

        public int getRedComponent(){
            System.out.println("not support getRedComponent");

            return 0;
        }

        public int getStrokeStyle(){
            System.out.println("not support getStrokeStyle");

            return 0;
        }

        public int getTranslateX(){
            System.out.println("not support getTranslateX");

            return 0;
        }

        public int getTranslateY(){
            System.out.println("not support getTranslateY");

            return 0;
        }

        public void setColor(int color){
            rgbImage.setForeground(color);
        }

        public void setFont(Font font){
            this.font = font;
            fontImage = Image.createImage(rgbImage.getWidth(), font.getHeight());
        }

        public void setGrayScale(int value){
            System.out.println("not support setGrayScale");
        }

        public void setStrokeStyle(int style){
            System.out.println("not support setStrokeStyle");
        }

        public void translate(int x, int y){
            System.out.println("not support translate");
        }
    }
}
