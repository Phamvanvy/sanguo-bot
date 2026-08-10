package peony.util;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 可以通过一个Jar文件或一个目录载入类的ClassLoader。
 * @author lighthu
 */
public class FileClassLoader extends ClassLoader {
    // 定义哈希表（Hashtable）类型的变量，用于保存被载入的类数据。
    private Hashtable<String, Class> loadedClasses;
    // 目录模式：根目录
    private File rootPath;
    // Jar模式：文件内容
    private HashMap<String, byte[]> classFiles;

    /**
     * 指定目录或jar文件创建ClassLoader。
     * @param root 可以是一个目录，或者jar文件
     */
    public FileClassLoader(File root) throws Exception {
        if (root.isDirectory()) {
            rootPath = root;
        } else {
        	classFiles = new HashMap<String, byte[]>();
            loadJarContent(root);
        }
        loadedClasses = new Hashtable<String, Class>();
    }
    
    /*
     * 载入jar文件中的所有class文件内容。
     */
    private void loadJarContent(File f) throws Exception {
        JarFile jf = new JarFile(f);
        Enumeration<JarEntry> ee = jf.entries();
        while (ee.hasMoreElements()) {
            JarEntry je = ee.nextElement();
            if (je.getName().endsWith(".class")) {
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                InputStream is = jf.getInputStream(je);
                byte[] classData = getBytesFromInput(is);
                is.close();
                classFiles.put(className, classData);
            }
        }
        jf.close();
    }
    
    /*
     * 从输入流中读取所有内容。
     */
    private static byte[] getBytesFromInput(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int rd = 0;
        int len = 0;
        byte[] buf = new byte[64];
        while((rd = in.read(buf)) != -1){
            len += rd;
            out.write(buf, 0, rd);
        }
        byte[] rt = out.toByteArray();
        out.close();
        return rt;
    }

    @Override
	public synchronized Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class newClass;
        byte[] classData;

        // 检查要载入的类数据是否已经被保存在哈希表中。
        newClass = loadedClasses.get(className);
        
        // 如果类数据已经存在且resolve值为true，则解析它。
        if (newClass != null) {
            if (resolve) {
                resolveClass(newClass);
            }
            return newClass;
        }

        // 首先试图从本地系统类组中载入指定类。这是必须的，因为虚拟机将这个类载入后，在解析
        // 和执行它时所用到的任何其他类，如java.lang.System类等，均不再使用虚拟机的类载入器，
        // 而是调用我们自制的类载入器来加载
        try {
            newClass = findSystemClass(className);
            return newClass;
        } catch (ClassNotFoundException e) {
        }
        
        // 如果不是系统类，则试图从指定路径中载入
        try {
            // 用自定义方法载入类数据，存放于字节数组classData中。
            classData = getClassData(className);
            
            // 由字节数组所包含的数据建立一个class类型的对象。
            newClass = defineClass(className, classData, 0, classData.length);
            if (newClass == null) {
                throw new ClassNotFoundException(className);
            }
        } catch (Exception e) {
            throw new ClassNotFoundException(className);
        }
        
        // 如果类被正确载入，则将类数据保存在哈希表中，以备再次使用。
        loadedClasses.put(className, newClass);
        
        // 如果resolve值为true，则解析类数据。
        if (resolve) {
            resolveClass(newClass);
        }
        return newClass;
    }

    /**
     * 载入文件内容到字符数组。
     */
    public static byte[] loadFileData(File src) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(src);
            BufferedInputStream bis = new BufferedInputStream(fis);
            return getBytesFromInput(bis);
        } catch (IOException e) {
            throw e;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    //这个方法从网络中载入类数据。
    protected byte[] getClassData(String className) throws IOException {
        if (rootPath != null) {
            try {
                String subPath = className.replace('.', '/') + ".class";
                File file = new File(rootPath, subPath);
                return loadFileData(file);
            } catch (Exception e) {
                throw new IOException(className);
            }
        } else {
            return classFiles.get(className);
        }
    }
}
