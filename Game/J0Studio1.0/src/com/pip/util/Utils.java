package com.pip.util;

import java.io.*;
import java.net.URI;
import java.util.*;

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
            byte[] data = new byte[256];
            int len;
            while((len = fis.read(data)) >= 0){
                if(len == 0){
                    continue;
                }
                fos.write(data, 0, len);
            }
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
    	return loadFileContent(src, "GBK");
    }
    
    /**
     * 载入文件内容到字符串，采用指定编码。
     */
    public static String loadFileContent(File src, String encoding) throws IOException{
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
            return new String(bos.toByteArray(), encoding);
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
     * 把一个字符串版本号转换为整数。
     */
    public static int parseVersionString(String str) {
    	String[] secs = str.split("\\.");
    	return Integer.parseInt(secs[0]) * 10000 + Integer.parseInt(secs[1]) * 100 + Integer.parseInt(secs[2]);
    }
    
    /**
     * 把一个整数版本号转换为字符串。
     */
    public static String versionToString(int version) {
    	int v1 = version / 10000;
    	int v2 = (version % 10000) / 100;
    	int v3 = version % 100;
    	return v1 + "." + v2 + "." + v3;
    }
    
    /**
     * 把一个指向本地文件的url转换为文件路径。
     * @param url
     * @return
     */
    public static String urlToPath(URI url) {
    	String path = url.getPath();
    	if (path.indexOf(':') >= 0) {
    		path = path.substring(1);
    	}
    	return path;
    }
    
    /**
	 * 找出一个目录中的所有文件（包括目录自己）。
	 * @param dir 根目录
	 * @param suffix 扩展名，带.符号，必须小写；null表示不过滤
	 * @param saveSet 保存找出的文件路径
	 */
	public static void findFilesInDir(File dir, String suffix, Set<String> saveSet) {
		if (suffix == null) {
			saveSet.add(dir.getAbsolutePath());
		}
		File[] children = dir.listFiles();
		for (File child : children) {
			if (child.isFile()) {
				if (suffix == null || child.getName().toLowerCase().endsWith(suffix)) {
					saveSet.add(child.getAbsolutePath());
				}
			} else if (child.isDirectory()) {
				findFilesInDir(child, suffix, saveSet);
			}
		}
	}
}