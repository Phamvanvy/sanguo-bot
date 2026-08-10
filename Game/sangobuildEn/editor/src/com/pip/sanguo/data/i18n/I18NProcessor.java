package com.pip.sanguo.data.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.pip.gtl.etf.ETFFile;
import com.pip.sanguo.data.ProjectData;
import com.pipimage.utils.Utils;

/**
 * 实际执行I18N操作的类。提供两类操作：项目数据文件和Java源代码。
 * @author lighthu
 */
public class I18NProcessor {
    public static final int MODE_PROJECT = 1;
    public static final int MODE_JAVA = 2;
    
    private int opMode;
    private ProjectData project;
    private File rootDir;
    private LocaleConfig targetLocale;
    
    /**
     * 初始化一个I18NProcessor用以本地化一个项目。
     * @param proj 项目
     * @param locale 目标语言
     */
    public I18NProcessor(ProjectData proj, LocaleConfig locale) {
        opMode = MODE_PROJECT;
        project = proj;
        targetLocale = locale;
    }
    
    /**
     * 初始化一个I18NProcessor用以本地化一个目录下的所有Java代码。
     * @param root 项目src目录
     * @param locale 目标语言
     */
    public I18NProcessor(File root, LocaleConfig locale) {
        opMode = MODE_JAVA;
        rootDir = root;
        targetLocale = locale;
    }
    
    /**
     * 执行本地化处理。
     */
    public void process(boolean change) throws Exception {
        if (opMode == MODE_PROJECT) {
            processProject(change);
        } else if (opMode == MODE_JAVA) {
            processJava(change);
        }
    }
    
    /*
     * 本地化一个项目。
     */
    private void processProject(boolean change) throws Exception {
        // 第一步，把本项目中的所有文件复制到输出目录
        targetLocale.outputDir.mkdirs();
        Set<String> excludes = new HashSet<String>();
        excludes.add("CVS");
        excludes.add("Branches");
        excludes.add("client_pkg");
        copyFiles(project.baseDir, targetLocale.outputDir, excludes, true);
        
        // 第二步，拷贝此版本特殊文件（需要在本地化之前拷贝的）到目标目录中
        copyFiles(targetLocale.revisionResourceDir, targetLocale.outputDir, excludes, false);

        // 第三步，载入消息文件，对目标目录执行本地化
        MessageFile mf = new MessageFile(targetLocale.messageFile, "zh_CN", targetLocale.id);
        ProjectData newProj = new ProjectData();
        newProj.load(targetLocale.outputDir);
        System.setProperty("pip_xml_encoding", targetLocale.encoding);
        String[][] newStrs;
        if (change) {
            newStrs = I18NUtils.doI18N(newProj, mf);
        } else {
            newStrs = I18NUtils.findI18NRelatedStrings(newProj, mf);
        }
        System.setProperty("pip_xml_encoding", "GBK");
        if (newStrs.length > 0) {
            for (String[] s : newStrs) {
                mf.addString(s[0], s[1]);
            }
            mf.save();
        }
        
        // 第四步，拷贝此版本特殊文件到目标目录中
        copyFiles(targetLocale.specialResourceDir, targetLocale.outputDir, excludes, false);
        
        // 第五步，重新生成client.pkg
        newProj.makeClientPackages();
    }
    
    //忽略文件列表
    private boolean canFileOpt(String name){
        boolean b = true;
        // 忽略etd
        if (name.endsWith(".etd")) {
            b = false;
        } else if (name.endsWith(".pkg")) {
            b = false;
        } else if (name.equals("fileversion.xml")) {
            b = false;
        } else if (name.equals("client.data")) {
            b = false;
        } else if (name.equals("client_pkg.xml")) {
            b=false;
        }
        return b;
    }
    
    /*
     * 清除目标目录以及子目录中的所有文件（保留CVS目录）。
     */
    private void clearDir(File dir, Set<String> excludes) {
        List<File> cache = new ArrayList<File>();
        cache.add(dir);
        while (cache.size() > 0) {
            File d = cache.remove(0);
            File[] ffs = d.listFiles();
            for (File ff : ffs) {
                if (ff.isFile()) {
                    ff.delete();
                } else if (!excludes.contains(ff.getName())) {
                    cache.add(ff);
                }
            }
        }
    }
    
    /*
     * 复制目录下所有文件到目标目录（跳过CVS目录）。
     * @param src 源目录
     * @param dest 目标目录
     * @param excludes 排除目录
     * @param sync 是否同步模式（在同步模式下，所有目标目录中多余的文件将被删除）
     */
    private void copyFiles(File src, File dst, Set<String> excludes, boolean sync) {
        List<String> cache = new ArrayList<String>();
        cache.add(".");
        while (cache.size() > 0) {
            String path = cache.remove(0);
            File d1 = new File(src, path);
            File d2 = new File(dst, path);
            
            // 找出目标目录中的所有文件
            d2.mkdirs();
            String[] ffs2 = d2.list();
            Set<String> targetSet = new HashSet<String>();
            for (String f : ffs2) {
                targetSet.add(f);
            }
            
            // 拷贝源目录到目标目录
            File[] ffs = d1.listFiles();
            for (File ff : ffs) {
                targetSet.remove(ff.getName());
                if (ff.isFile()) {
                    if(canFileOpt(ff.getName())){
                        try {
                            Utils.copyFile(ff, new File(d2, ff.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (!excludes.contains(ff.getName())) {
                    cache.add(path + "/" + ff.getName());
                }
            }
            
            // 删除目标目录中多余文件
            if (sync) {
                for (String name : targetSet) {
                    if (!excludes.contains(name)) {
                        File f = new File(d2, name);
                        if (f.isFile()) {
                            f.delete();
                        } else {
                            clearDir(f, excludes);
                        }
                    }
                }
            }
        }
    }
    
    /*
     * 本地化一个目录下所有的Java文件和AS文件。
     */
    private void processJava(boolean change) throws Exception {
        MessageFile mf = new MessageFile(targetLocale.messageFile, "zh_CN", targetLocale.id);

        // 处理Java文件
        String[][] newStrs;
        if (change) {
            newStrs = I18NUtils.doI18NJava(rootDir, mf, "GBK", targetLocale.encoding);
        } else {
            newStrs = I18NUtils.findI18NRelatedJavaStrings(rootDir, mf, "GBK", targetLocale.encoding);
        }
        if (newStrs.length > 0) {
            for (String[] s : newStrs) {
                mf.addString(s[0], s[1]);
            }
            mf.save();
        }

        // 处理AS文件
        if (change) {
            newStrs = I18NUtils.doI18NActionScript(rootDir, mf, "UTF-8", targetLocale.encoding);
        } else {
            newStrs = I18NUtils.findI18NRelatedActionScriptStrings(rootDir, mf, "UTF-8", targetLocale.encoding);
        }
        if (newStrs.length > 0) {
            for (String[] s : newStrs) {
                mf.addString(s[0], s[1]);
            }
            mf.save();
        }
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            File sf = new File("c:\\1.etf.gz");
            FileInputStream fis = new FileInputStream(sf);
            GZIPInputStream gis = new GZIPInputStream(fis);
            ETFFile etf = ETFFile.load(gis);
            fis.close();
            boolean changed = false;
            for (int i = 0; i < etf.stringTable.length; i++) {
                if (etf.stringTable[i] != null) {
                    System.out.println(etf.stringTable[i]);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
