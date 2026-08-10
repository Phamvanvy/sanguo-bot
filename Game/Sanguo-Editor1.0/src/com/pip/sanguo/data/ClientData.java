package com.pip.sanguo.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.util.Utils;

public class ClientData {
    private File baseDir;
    // 分支，null表示pip版本
    private String branch;
    private HashMap<File, Integer> fileVersions;

    private static class PackageDefine {
        public String target;
        public String uimodel;
        // client.data格式版本，如果是2，则最前面4个字节是版本号。
        public int dataVersion;
        public boolean needPkg; //是否需要把关卡包也打入资源
        public String[] files;
        public boolean[] need;
        public File[] srcFile;
        public File[] targetFile;
        public String[] usedFileName;       
        public String[][] pkgFileName;
        
        public Hashtable<String, Boolean> fileNeedTable;
        public byte[] clientData;
        public byte[] pkgData; //关卡包数据
    }
    private PackageDefine[] packageDefs;
    private Hashtable<String, PackageDefine> packageDefTable;

    private static final String CLIENT_DATA_FILE = "client.data";
    private static final String PKG_DATA_FILE = "pkg.data";    
    private ProjectData pd;
    
    public ClientData(ProjectData pd, File baseDir, String branch) throws Exception{
        this.pd = pd;
        this.branch = branch;
        this.baseDir = baseDir;
        loadDefine();
        makeClientDataNames();
    }
    
    public ClientData(ProjectData pd, File baseDir, HashMap<File, Integer> fileVersions, String branch) throws Exception{
        this.pd = pd;
        this.branch = branch;
        this.baseDir = baseDir;
        this.fileVersions = fileVersions;
        loadDefine();
    }

    private Map<String,String> fileNames = new HashMap<String,String>();
    private List<String> duplicates = new ArrayList<String>();
    
    private void loadDefine() throws Exception {
        Document doc1;
        if (branch == null) {
            doc1 = Utils.loadDOM(new File(baseDir, "./client_pkg.xml"));
        } else {
            doc1 = Utils.loadDOM(new File(baseDir, "./Branches/" + branch + "/client_pkg.xml"));
        }
        List list = doc1.getRootElement().getChildren("package");
        packageDefs = new PackageDefine[list.size()];
        packageDefTable = new Hashtable<String, PackageDefine>();
        duplicates.clear();
        
        for (int i = 0; i < list.size(); i++) {
            packageDefs[i] = new PackageDefine();
            Element elem = (Element) list.get(i);
            packageDefs[i].target = elem.getAttributeValue("target");
            if (branch != null) {
                packageDefs[i].target = "Branches/" + branch + "/" + packageDefs[i].target;
            }
            packageDefs[i].uimodel = elem.getAttributeValue("uimodel");
            packageDefs[i].dataVersion = Integer.parseInt(elem.getAttributeValue("dataversion"));
            String pkgFiles = elem.getAttributeValue("pkgfiles");
            if(pkgFiles != null) {
                packageDefs[i].needPkg = true;
                this.makePkgFileName(packageDefs[i], pkgFiles);                
            }
            List list2 = elem.getChildren("file");
            packageDefs[i].files = new String[list2.size()];
            //检查文件是否有重定义的
            fileNames.clear();
            for (Object object : list2) {
                Element e = (Element)object;
                String path = e.getAttributeValue("path");
                if(path != null && !path.equals("")){
                    String name = path.substring(path.lastIndexOf('/') + 1, path.length());
                    if(!fileNames.containsKey(name)){
                        fileNames.put(name, name);
                    } else {
                        duplicates.add(branch != null?branch+":":""+packageDefs[i].target + ":" + path);
                    }
                }
            }
            packageDefs[i].need = new boolean[list2.size()];
            packageDefs[i].fileNeedTable = new Hashtable<String, Boolean>();
            for (int j = 0; j < list2.size(); j++) {
                Element elem2 = (Element) list2.get(j);
                packageDefs[i].files[j] = elem2.getAttributeValue("path");
                packageDefs[i].need[j] = "true".equals(elem2.getAttributeValue("need"));
            }
            packageDefs[i].srcFile = new File[list2.size()];
            packageDefs[i].targetFile = new File[list2.size()];
            packageDefs[i].usedFileName = new String[list2.size()];
            try {
                packageDefs[i].clientData = Utils.loadFileData(new File(new File(baseDir, packageDefs[i].target), CLIENT_DATA_FILE));
            } catch (Exception e) {
                packageDefs[i].clientData = new byte[0];
            }
            if (packageDefs[i].dataVersion == 2) {
                // 版本2的文件头有4个字节版本号（0占位），下载时需要去掉
                if (packageDefs[i].clientData.length < 4) {
                    packageDefs[i].clientData = new byte[0];
                } else {
                    byte[] btemp = new byte[packageDefs[i].clientData.length - 4];
                    System.arraycopy(packageDefs[i].clientData, 4, btemp, 0, btemp.length);
                    packageDefs[i].clientData = btemp;
                }
            }
            
            packageDefTable.put(packageDefs[i].uimodel, packageDefs[i]);
        }
        
        if(duplicates.size() > 0){
            StringBuilder sb = new StringBuilder();
            sb.append("重复定义:\n");
            for (String fileName : duplicates) {
                sb.append(fileName);
                sb.append("\n");
            }
            throw new Exception(sb.toString());
        }
    }
    
    public byte[] getClientData(String model){
        PackageDefine pkgDef = packageDefTable.get(model);
        if(pkgDef != null){
            return pkgDef.clientData;
        }
        
        return null;
    }
    
    public byte[] getPkgData(String model){
        PackageDefine pkgDef = packageDefTable.get(model);
        if(pkgDef != null){
            return pkgDef.pkgData;
        }
        
        return null;
    }
    
    public Hashtable<String, Boolean> getAllClientNeedTable(String model){
        PackageDefine pkgDef = packageDefTable.get(model);
        if(pkgDef != null){
            return pkgDef.fileNeedTable;
        }
        
        return null;
    }
    
    /**
     * 根据文件名查找最适合的文件路径。
     * @param model 机型 
     * @param fileName 文件名
     * @return 服务器相对路径，如果找不到，返回null。
     */
    public String getMatchPath(String model, String fileName) {
        PackageDefine pkgDef = packageDefTable.get(model);
        if (pkgDef == null) {
            return null;
        }
        for (String path : pkgDef.files) {
            int lastPos = path.lastIndexOf('/');
            String p = path;
            if (lastPos != -1) {
                p = p.substring(lastPos + 1);
            }
            if (p.equals(fileName)) {
                return path;
            }
        }
        return null;
    }
    
    public boolean isClientNeedResource(String name, String model){
        PackageDefine pkgDef = packageDefTable.get(model);
        if(pkgDef != null){
            Boolean needed = pkgDef.fileNeedTable.get(name);
            if(needed != null){
                return needed.booleanValue();
            }
        }
        return false;
    }
    
    public void makeClientData() throws Exception {
        for (int i = 0; i < packageDefs.length; i++) {
            makeClientData(packageDefs[i]);
        }
    }
    
    public void makePkgData() throws Exception {
        for (int i = 0; i < packageDefs.length; i++) {
            if(packageDefs[i].needPkg) {
                makePkgData(packageDefs[i]);    
            }            
        }
    }
    
    private void makeClientDataNames() throws Exception{
        for(int k = 0; k < packageDefs.length; k++){
            PackageDefine pdef = packageDefs[k];
            
            for (int i = 0; i < pdef.files.length; i++) {
                String fname = pdef.files[i];
                File srcFile;
                if (fname.endsWith(".etf")) {
                    pdef.usedFileName[i] = fname;
                } else {
                    srcFile = new File(baseDir, fname);
                    pdef.usedFileName[i] = srcFile.getName();
                }
                
                pdef.fileNeedTable.put(pdef.usedFileName[i], new Boolean(pdef.need[i]));
            }
        }        
    }
    
    private void makeClientData(PackageDefine pdef) throws Exception {
        // 第一步，清空目标目录
        File targetDir = new File(baseDir, pdef.target);
        File[] oldFiles = targetDir.listFiles();
        if (oldFiles != null) {
            for (File f : oldFiles) {
                if (f.isFile() && !f.getName().equals(".cvsignore")) {
                    f.delete();
                }
            }
        }
        
        // 第二步，拷贝所有配置的文件到目标目录，注意etf文件的名字需要根据机型修改
        for (int i = 0; i < pdef.files.length; i++) {
            String fname = pdef.files[i];
            File srcFile;
            File tgtFile;
            if (fname.endsWith(".etf")) {
                String sname = fname.substring(0, fname.length() - 4);
                if (branch == null) {
                    srcFile = new File(baseDir, "scripts/" + pdef.uimodel + "/" +
                            sname + "_" + pdef.uimodel + ".etf.gz");
                } else {
                    srcFile = new File(baseDir, "Branches/" + branch + "/scripts/" + pdef.uimodel + "/" +
                            sname + "_" + pdef.uimodel + ".etf.gz");
                }
                tgtFile = new File(targetDir, srcFile.getName());
                pdef.usedFileName[i] = fname;
            } else {
                srcFile = new File(baseDir, fname);
                tgtFile = new File(targetDir, srcFile.getName());
                pdef.usedFileName[i] = srcFile.getName();
            }
            Utils.copyFile(srcFile, tgtFile);
            pdef.srcFile[i] = srcFile;
            pdef.targetFile[i] = tgtFile;
        }
        
        // 第三步，生成client.data文件，放到scripts和client_pkg目录下的机型目录里。
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        makeClientResourceDataFile(dos, pdef);
        dos.close();
        Utils.saveFileData(new File(targetDir, CLIENT_DATA_FILE), bos.toByteArray());
        if (branch == null) {
            Utils.saveFileData(new File(baseDir, "scripts/" + pdef.uimodel + "/" + CLIENT_DATA_FILE), bos.toByteArray());
        } else {
            Utils.saveFileData(new File(baseDir, "Branches/" + branch + "/scripts/" + pdef.uimodel + "/" + CLIENT_DATA_FILE), bos.toByteArray());
        }
    }

    private void makeClientResourceDataFile(DataOutputStream dos, PackageDefine pdef) throws Exception {
        // 客户端jar包已内置资源
        if (pdef.dataVersion == 2) {
            dos.writeInt(0);
        }
        int needCount = 0;
        for (int i = 0; i < pdef.files.length; i++) {
            if (pdef.need[i]) {
                needCount++;
            }
        }
        dos.writeInt(needCount);
        for (int i = 0; i < pdef.files.length; i++) {
            if (!pdef.need[i]) {
                continue;
            }
            dos.writeUTF(pdef.usedFileName[i]);
            dos.writeInt(fileVersions.get(pdef.srcFile[i]));
            if (pdef.dataVersion == 2) {
                dos.writeInt((int)pdef.targetFile[i].length());
            }
        }
    }
    
    private static Map<String,String> areaId2dir = null;
    @SuppressWarnings("unchecked")
    private void makePkgFileName(PackageDefine pdef, String pkgFiles) {
        if(pkgFiles.equals("all")) {
            List<DataObject> list = pd.getDataListByType(GameArea.class);
            int dataLength = list.size();
            Vector<String> pkgFileNameVec = new Vector<String>();
            Vector<String> pkgFileNameVec2 = new Vector<String>(); 

            String fileName;
            for(int i=0; i<dataLength; i++) {
                GameArea area = (GameArea)list.get(i);
                if (pd.isUseLarge(pdef.uimodel)) {
                    fileName = baseDir + "/Areas/" + area.source.getName() + "/client_l.pkg";
                } else {
                    fileName = baseDir + "/Areas/" + area.source.getName() + "/client.pkg";
                }
                if(new File(fileName).exists()) {
                    pkgFileNameVec.add(fileName);
                    pkgFileNameVec2.add(area.id + ".pkg");
                }
            }
            pdef.pkgFileName = new String[2][pkgFileNameVec.size()];
            pkgFileNameVec.copyInto(pdef.pkgFileName[0]);
            pkgFileNameVec2.copyInto(pdef.pkgFileName[1]);
            
        } else if(pkgFiles.equals("")){
            pdef.pkgFileName = new String[2][0];
        } else {
            if(areaId2dir == null){
                areaId2dir = new HashMap<String,String>();
                try {
                    Document doc = Utils.loadDOM(new File(baseDir, "Areas/index.xml"));
                    List<Element> children = doc.getRootElement().getChildren("area");
                    for (int i = 0; i < children.size(); i++) {
                        Element e = children.get(i);
                        areaId2dir.put(e.getAttributeValue("id"), e.getAttributeValue("source"));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String[] fileNames = Utils.splitString(pkgFiles, ',');
            int dataLength = fileNames.length;
            pdef.pkgFileName = new String[2][dataLength];
            String realFileName = "";
            for(int i=0; i<dataLength; i++) {
                realFileName = areaId2dir.get(fileNames[i]);
                if(realFileName == null){
                    realFileName = fileNames[i];
                }
                if (pd.isUseLarge(pdef.uimodel)) {
                    pdef.pkgFileName[0][i] = baseDir + "/Areas/" + realFileName + "/client_l.pkg";
                } else {
                    pdef.pkgFileName[0][i] = baseDir + "/Areas/" + realFileName + "/client.pkg";
                }
                pdef.pkgFileName[1][i] = fileNames[i] + ".pkg";
            }
        }
    }
    
    //pkg.data
    private void makePkgData(PackageDefine pdef) throws Exception {
        File targetDir = new File(baseDir, pdef.target);
        int fileCount = pdef.pkgFileName[0].length;
        
        for(int i=0; i<fileCount; i++) {
            try {
            String pkgSrcFileName = pdef.pkgFileName[0][i];
            String pkgTargetFileName = pdef.pkgFileName[1][i];
            File pkgFile = new File(pkgSrcFileName);
            if(pkgFile.exists()) {
                try {
                    String targetPkgFile = targetDir + "/" + pkgTargetFileName;
                    Utils.copyFile(pkgFile, new File(targetPkgFile));
                    if (branch == null) {
                        Utils.copyFile(pkgFile, new File(baseDir, "scripts/" + pdef.uimodel + "/" + pkgTargetFileName));
                    } else {
                        Utils.copyFile(pkgFile, new File(baseDir, "Branches/" + branch + "/scripts/" + pdef.uimodel + "/" + pkgTargetFileName));
                    }
                    System.out.println("copy " + pkgSrcFileName + " -> " + targetDir + "/" + pkgTargetFileName);                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }    
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("copied " + fileCount + " files.");        
        
        // 第三步，生成client.data文件，放到scripts和client_pkg目录下的机型目录里。
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        makePkgResourceDataFile(dos, pdef);
        dos.close();
        Utils.saveFileData(new File(targetDir, PKG_DATA_FILE), bos.toByteArray());
        if (branch == null) {
            Utils.saveFileData(new File(baseDir, "scripts/" + pdef.uimodel + "/" + PKG_DATA_FILE), bos.toByteArray());
        } else {
            Utils.saveFileData(new File(baseDir, "Branches/" + branch + "/scripts/" + pdef.uimodel + "/" + PKG_DATA_FILE), bos.toByteArray());
        }
    }
    
    private void makePkgResourceDataFile(DataOutputStream dos, PackageDefine pdef) throws Exception {
        // 客户端jar包已内置资源
        if (pdef.dataVersion == 2) {
            dos.writeInt(0);
        }
        
        // 包含need=false的资源
        int unnessCount = 0;
        for (int i = 0; i < pdef.files.length; i++) {
            if (!pdef.need[i]) {
                unnessCount++;
            }
        }
        
        dos.writeInt(pdef.pkgFileName[0].length + unnessCount);
        for (int i = 0; i < pdef.pkgFileName[0].length; i++) {            
            File pkgFile = new File(pdef.pkgFileName[0][i]);
            dos.writeUTF(pdef.pkgFileName[1][i]);
            if(!fileVersions.containsKey(pkgFile)){
                System.out.println(pkgFile.getAbsolutePath());
            }
            dos.writeInt(fileVersions.get(pkgFile));  
            if (pdef.dataVersion == 2) {
                File targetDir = new File(baseDir, pdef.target);
                String pkgTargetFileName = pdef.pkgFileName[1][i];
                String targetPkgFile = targetDir + "/" + pkgTargetFileName;
                dos.writeInt((int)new File(targetPkgFile).length());
            }    
        }
        for (int i = 0; i < pdef.files.length; i++) {
            if (pdef.need[i]) {
                continue;
            }
            dos.writeUTF(pdef.usedFileName[i]);
            dos.writeInt(fileVersions.get(pdef.srcFile[i]));
            if (pdef.dataVersion == 2) {
                dos.writeInt((int)pdef.targetFile[i].length());
            }
        }
    }
}
