package com.pipimage.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.pipimage.data.ImageDescription;


public class Utils{
    /**
     * 拷贝源文件到目标文件。
     */
    public static void copyFile(File src, File dest) throws IOException{
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try{
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] data = new byte[256];
            int len;
            while((len = bis.read(data)) >= 0){
                if(len == 0){
                    continue;
                }
                bos.write(data, 0, len);
            }
            bos.flush();
        }catch(IOException e){
            throw e;
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch(IOException e){
                }
            }
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }

    public static void copyList(List src, List dest){
        for(int i = 0; i < src.size(); i++){
            Object srcObj = src.get(i);
            dest.add(srcObj);
        }
    }

    public static void depthCopyList(List src, List dest){
        for(int i = 0; i < src.size(); i++){
            Object srcObj = src.get(i);
            dest.add(copyObject(srcObj));
        }
    }

    public static Object copyObject(Object src){
        try{
            Object destObj = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(bos);
            obs.writeObject(src);
            obs.flush();

            byte[] data = bos.toByteArray();

            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            destObj = ois.readObject();
            return destObj;
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 载入文件内容到字符数组。
     */
    public static byte[] loadFileData(File src) throws IOException{
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(src);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] data = new byte[256];
            int len;
            while((len = bis.read(data)) >= 0){
                if(len == 0){
                    continue;
                }
                bos.write(data, 0, len);
            }
            return bos.toByteArray();
        }catch(IOException e){
            throw e;
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch(IOException e){
                }
            }
        }
    }

    /**
     * 载入文件内容到字符串，采用GBK编码。
     */
    public static String loadFileContent(File src) throws IOException{
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(src);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] data = new byte[256];
            int len;
            while((len = bis.read(data)) >= 0){
                if(len == 0){
                    continue;
                }
                bos.write(data, 0, len);
            }
            return new String(bos.toByteArray(), "GBK");
        }catch(IOException e){
            throw e;
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch(IOException e){
                }
            }
        }
    }

    /**
     * 保存数据到文件。
     */
    public static void saveFileData(File dest, byte[] data) throws IOException{
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(dest);
            fos.write(data);
        }catch(IOException e){
            throw e;
        }finally{
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }

    /**
     * 保存字符串到文件，采用GBK编码。
     */
    public static void saveFileContent(File dest, String content) throws IOException{
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(dest);
            fos.write(content.getBytes("GBK"));
        }catch(IOException e){
            throw e;
        }finally{
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }

    /**
     * 转换字符串到UTF格式。
     */
    public static byte[] toUTF(String s){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(s);
            dos.close();
            return bos.toByteArray();
        }catch(Exception e){
            return new byte[0];
        }
    }

    /**
     * 计算CRC。每两字节一组进行异或。
     */
    public static short CRC(byte[] data){
        byte byte1 = 0, byte2 = 0;
        for(int i = 0; i < data.length; i += 2){
            if(i < data.length - 1){
                byte1 ^= data[i];
                byte2 ^= data[i + 1];
            }else{
                byte1 ^= data[i];
            }
        }
        return (short)((byte1 << 8) | (byte2 & 0xFF));
    }

    /**
     * 执行一个命令，并把标准输出和标准错误的内容保存到字符串中返回。
     */
    public static String executeCommand(String cmd, File dir) throws IOException{
        Process p = Runtime.getRuntime().exec(cmd, null, dir);
        StringBuffer buf = new StringBuffer();
        new CommandOutputReader(p.getInputStream(), buf).start();
        new CommandOutputReader(p.getErrorStream(), buf).start();
        int retCode = -1;
        try{
            retCode = p.waitFor();
        }catch(InterruptedException e){
        }
        p.destroy();
        if(retCode == 0){
            return null;
        }else{
            return buf.toString();
        }
    }

    /**
     * 执行一个命令。
     */
    public static void executeCommand(String cmd, String arg, File dir) throws IOException{
        Runtime.getRuntime().exec("\"" + cmd + "\" " + arg, null, dir);
    }

    private static class CommandOutputReader extends Thread{
        private InputStream input;
        private StringBuffer output;

        public CommandOutputReader(InputStream is, StringBuffer buf){
            input = is;
            output = buf;
        }

        public void run(){
            try{
                InputStreamReader isr = new InputStreamReader(input, "GBK");
                int ch;
                while((ch = isr.read()) != -1){
                    synchronized(output){
                        output.append((char)ch);
                    }
                }
            }catch(IOException e){
            }
        }
    }

    /**
     * 删除一个目录以及该目录下的所有文件。
     */
    public static void deleteDir(File dir){
        File[] children = dir.listFiles();
        for(int i = 0; i < children.length; i++){
            if(children[i].isDirectory()){
                deleteDir(children[i]);
            }else{
                children[i].delete();
            }
        }
        dir.delete();
    }

    /**
     * 把一个异常转换为字符串。
     */
    public static String toString(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 翻转图像数据
     * @param trans
     * @return
     */
    public static byte[] transData(byte[] data, int width, int trans){
        byte[] ret = new byte[data.length];
        System.arraycopy(data, 0, ret, 0, ret.length);
        int height = ret.length / width;
        if((trans & ImageDescription.T_HORIZONTAL) != 0){
            for(int h = 0; h < height; h++)
                for(int w = 0; w < width / 2; w++){
                    byte tmp = ret[h * width + w];
                    ret[h * width + w] = ret[h * width + width - 1 - w];
                    ret[h * width + width - 1 - w] = tmp;
                }
        }

        if((trans & ImageDescription.T_VERTICAL) != 0){
            for(int h = 0; h < height / 2; h++){
                byte[] tmp = new byte[width];
                System.arraycopy(ret, h * width, tmp, 0, width);
                System.arraycopy(ret, (height - h - 1) * width, ret, h * width, width);
                System.arraycopy(tmp, 0, ret, (height - h - 1) * width, width);
            }
        }

        return ret;
    }
}
