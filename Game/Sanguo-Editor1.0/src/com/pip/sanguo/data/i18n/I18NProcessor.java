package com.pip.sanguo.data.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

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
    public static final int MODE_SCRIPT = 3;
    
    private int opMode;
    private ProjectData project;
    private File rootDir;
    private LocaleConfig targetLocale;
    public String sourceEncoding = "GBK";
    
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
     * 初始化一个I18NProcessor用以本地化一个目录下的所有Java代码或脚本。
     * @param root 项目src目录
     * @param locale 目标语言
     */
    public I18NProcessor(File root, LocaleConfig locale, int mode) {
        opMode = mode;
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
            processJava(sourceEncoding, change);
        } else if (opMode == MODE_SCRIPT) {
            processScript(change);
        }
    }
    
    public void processInThread(Shell shell,boolean change) {
        ThreadI18N ti = new ThreadI18N(shell,this,change);
        Thread thread = new Thread(ti);
        thread.start();
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
        excludes.add("scripts");
        copyFiles(project.baseDir, targetLocale.outputDir, excludes, true);
        
        // 第二步，拷贝此版本特殊文件（需要在本地化之前拷贝的）到目标目录中
        for (Iterator<String> iterator = excludes.iterator(); iterator.hasNext();) {
            String string = iterator.next();
            if(string.equals("scripts")){
                iterator.remove();
                break;
            }
        }
        copyFiles(targetLocale.revisionResourceDir, targetLocale.outputDir, excludes, false);

        // 第三步，载入消息文件，对目标目录执行本地化
        MessageFile mf = new MessageFile(targetLocale.dataMessageFile, "zh_CN", targetLocale.id);
        ProjectData newProj = new ProjectData();
        newProj.load(targetLocale.outputDir);
        System.setProperty("pip_xml_encoding", targetLocale.encoding);
        String[][] newStrs;
        if (change) {
            newStrs = I18NUtils.doI18N(newProj, mf);
        } else {
            newStrs = I18NUtils.findI18NRelatedStrings(newProj, mf);
        }
        if (newStrs.length > 0) {
            for (String[] s : newStrs) {
                mf.addString(s[0], s[1]);
            }
            mf.save();
        }
        
        // 校正所有NPC引用和位置引用
        newProj.validateMixedText(false);
        System.setProperty("pip_xml_encoding", "GBK");
        
        // 对脚本单独执行处理
        if ("zh_TW".equals(targetLocale.id)) {
            // 2011-12-06 Light：只有台服版本才按旧方法处理脚本；其他所有国际化版本都采用新方法对脚本源代码进行国际化处理。
            mf = new MessageFile(targetLocale.sourceCodeMessageFile, "zh_CN", targetLocale.id);
            if (change) {
                newStrs = I18NUtils.doI18NScript(newProj.baseDir, mf);
            } else {
                newStrs = I18NUtils.findI18NRelatedScriptStrings(newProj.baseDir, mf);
            }
            if (newStrs.length > 0) {
                for (String[] s : newStrs) {
                    mf.addString(s[0], s[1]);
                }
                mf.save();
            }
        }
        
        // 第四步，拷贝此版本特殊文件到目标目录中
        copyFiles(targetLocale.specialResourceDir, targetLocale.outputDir, excludes, false);
        
        // 第五步，重新生成client.pkg
        newProj.makeClientPackages();
    }
    
    //忽略文件列表
    public static boolean needETD = false;
    private static boolean canFileOpt(String name){
        boolean b = true;
        // 忽略etd
        if(name.endsWith(".etd") && !needETD){
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
    private static void clearDir(File dir, Set<String> excludes) {
        List<File> cache = new ArrayList<File>();
        cache.add(dir);
        while (cache.size() > 0) {
            File d = cache.remove(0);
            File[] ffs = d.listFiles();
            if(ffs != null){
                for (File ff : ffs) {
                    if (ff.isFile()) {
                        ff.delete();
                    } else if (!excludes.contains(ff.getName())) {
                        cache.add(ff);
                    }
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
    public static void copyFiles(File src, File dst, Set<String> excludes, boolean sync) {
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
                            // 2011-12-06 Light: 性能优化，对于完全相等的文件，比较其内容，完全一样则跳过。
                            byte[] data1 = Utils.loadFileData(ff);
                            File tf = new File(d2, ff.getName());
                            if (ff.length() == tf.length()) {
                                byte[] data2 = Utils.loadFileData(tf);
                                if (Arrays.equals(data1, data2)) {
                                    continue;
                                }
                            }
                            Utils.saveFileData(tf, data1);
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
    private void processJava(String sourceEncoding, boolean change) throws Exception {
        MessageFile mf = new MessageFile(targetLocale.sourceCodeMessageFile, "zh_CN", targetLocale.id);

        // 处理Java文件
        String[][] newStrs;
        boolean needSave = false;
        if (change) {
            newStrs = I18NUtils.doI18NJava(rootDir, mf, sourceEncoding, targetLocale.encoding);
        } else {
            newStrs = I18NUtils.findI18NRelatedJavaStrings(rootDir, mf, sourceEncoding, targetLocale.encoding);
        }
        if (newStrs.length > 0) {
            for (String[] s : newStrs) {
                mf.addString(s[0], s[1]);
            }
            needSave = true;
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
            needSave = true;
        }

        // 处理XML文件
        if (change) {
            newStrs = I18NUtils.doI18NXML(rootDir, mf, "UTF-8", targetLocale.encoding);
        } else {
            newStrs = I18NUtils.findI18NRelatedXMLStrings(rootDir, mf, "UTF-8", targetLocale.encoding);
        }
        if (newStrs.length > 0) {
            for (String[] s : newStrs) {
                mf.addString(s[0], s[1]);
            }
            needSave = true;
        }
        if (needSave) {
            mf.save();
        }
    }
    
    private void processScript(boolean change) throws Exception {
        MessageFile mf = new MessageFile(targetLocale.sourceCodeMessageFile, "zh_CN", targetLocale.id);
        I18NUtils.doI18NScriptSpecifiedDir(rootDir, mf);
    }
    
    class ThreadI18N implements Runnable {
        I18NProcessor processor;
        boolean change;
        Shell shell;
        public ThreadI18N(Shell shell,I18NProcessor processor,boolean change){
            this.shell = shell;
            this.processor = processor;
            this.change = change;
        }
        @Override
        public void run() {
            try {
                processor.process(change);
                shell.getDisplay().syncExec(new Runnable(){
                    public void run(){
                        MessageDialog.openInformation(shell, "成功", "操作成功！");
                    }
                }); 
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    public static void main(String[] args) throws Exception {
       String locale = "zh_TW";
        String base = "d:\\workspace\\Sanguo1.0-Data\\data_"+locale+"\\";
        
         /* *ProjectData pd = new ProjectData();
        
        
        I18NProcessor.needETD = true;
        
        pd.baseDir = new File(base + "data");
        pd.serverMode = false;
        
        pd.load(new File(base + "data"));
        
        Set<String> excludes = new HashSet<String>();
        excludes.add("CVS");
        excludes.add("Branches");
        excludes.add("client_pkg");
        //excludes.add(".pkg");
        //excludes.add(".cvsignore");
        File src = new File(base + "revision_resources\\scripts");
        File dest = new File(base + "data\\scripts");
        copyFiles(src, dest, excludes, true);
        
     // 第三步，载入消息文件，对目标目录执行本地化
        File msg = new File(base + "messages_sourcecode.xls");
        MessageFile mf = new MessageFile(msg, "zh_CN", locale);
        System.setProperty("pip_xml_encoding", "UTF-8");
        String[][] newStrs;
        
        // 对脚本单独执行处理
        if (true) {
            newStrs = I18NUtils.doI18NScript(pd.baseDir, mf);
        } else {
            newStrs = I18NUtils.findI18NRelatedScriptStrings(pd.baseDir, mf);
        }
//        if (newStrs.length > 0) {
//            for (String[] s : newStrs) {
//                mf.addString(s[0], s[1]);
//            }
//            mf.save();
//        }
         */
     // TODO Auto-generated method stub
        try {
            File sf = new File(base + "\\data\\scripts\\AndroidLarge\\ui_about_AndroidLarge.etf.gz");
            //File sf = new File("E:\\eclipse\\workspace\\Sanguo1.0-Data\\data_vi_VN\\data\\client_pkg\\NokiaS60V3\\ui_mainmenu_NokiaS60V3.etf.gz");
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
