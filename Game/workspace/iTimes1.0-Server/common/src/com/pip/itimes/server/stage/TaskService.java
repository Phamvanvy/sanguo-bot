package com.pip.itimes.server.stage;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.io.FilenameUtils;
import java.io.FileInputStream;
import com.pip.gtl.etf.ETFFile;
import com.pip.gtl.etf.ETFUtil;
import java.io.*;
import java.io.*;
import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TaskService {

    private Logger log = Logger.getLogger(TaskService.class);

    private File taskDir;
    private Map etfs = new HashMap();
    private Map<Integer,Integer> def = new HashMap<Integer,Integer>();
    private File tipsFile;
    private TaskTips tips = new TaskTips();
    private Map<Integer, List<TaskTracePoint>> taskTracePoints = new HashMap<Integer, List<TaskTracePoint>>();

    private Pattern pattern = Pattern.compile("\\{#(\\d+)\\}");

    public TaskService(File taskDir,File tipsFile) throws Exception{
        this.taskDir = taskDir;
        this.tipsFile = tipsFile;
        loadTasks();
        loadTaskDef();
        loadTaskTips();
    }

    public void loadTasks() {
        Map newEfts = new HashMap();
        File[] etfFiles = taskDir.listFiles();
        for (int i = 0; i < etfFiles.length; i++) {
            File file = etfFiles[i];
            String fileName = file.getName();
            if (file.isFile() &&
                "etf".equals(FilenameUtils.getExtension(fileName))) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(file);
                    log.info("load task:" + fileName);
                    ETFFile etf = ETFFile.load(is);
                    ETFUtil.analyzeETFCode(etf);
                    String name = FilenameUtils.getBaseName(fileName);
                    newEfts.put(name, etf);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (is != null)
                        try {
                            is.close();
                        } catch (IOException ex1) {
                        }
                }
            }
        }
        etfs = newEfts;
    }

    public void loadTaskDef() throws Exception{
        File f = new File(taskDir,"index.xml");
        SAXReader reader = new SAXReader();
        Document doc = reader.read(f);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("task");i.hasNext();){
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            int level = 0;
            if(node.attributeValue("level")!=null){
                level = Integer.parseInt(node.attributeValue("level"));
            }
            def.put(id,level);
            
            // 载入寻路点
            Iterator i2 = node.elementIterator("tracepoint");
            List<TaskTracePoint> tpList = new ArrayList<TaskTracePoint>();
            while (i2.hasNext()) {
            	Element elem2 = (Element)i2.next();
            	String tpname = elem2.attributeValue("name");
            	short tpmap = Short.parseShort(elem2.attributeValue("targetmap"));
            	short tpx = Short.parseShort(elem2.attributeValue("targetx"));
            	short tpy = Short.parseShort(elem2.attributeValue("targety"));
            	short gx = Short.parseShort(elem2.attributeValue("gridx"));
            	short gy = Short.parseShort(elem2.attributeValue("gridy"));
            	tpList.add(new TaskTracePoint(tpname, tpmap, tpx, tpy, gx, gy));
            }
            if (tpList.size() > 0) {
            	taskTracePoints.put(id, tpList);
            }
        }
    }

    public void loadTaskTips() throws Exception{
        tips.clear();
        SAXReader reader = new SAXReader();
        Document doc = reader.read(tipsFile);
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("tasktip");i.hasNext();){
            Element node = (Element)i.next();
            int id = Integer.parseInt(node.attributeValue("id"));
            for(Iterator j=node.elementIterator("level");j.hasNext();){
                Element e = (Element)j.next();
                int begin = Integer.parseInt(e.attributeValue("begin"));
                int end = Integer.parseInt(e.attributeValue("end"));
                String msg = e.attributeValue("message");
                if(msg.trim().length()==0)
                    msg = null;
                List<String> l = new ArrayList<String>();
                for(Iterator k=e.elementIterator("message");k.hasNext();){
                    Element mE = (Element)k.next();
                    l.add(mE.attributeValue("data"));
                }
                String[] messages = new String[l.size()];
                l.toArray(messages);
                tips.addTip(id,begin,end,msg,messages);
            }
        }
    }


    public int getTaskLevel(int taskId){
        Integer level = def.get(taskId);
        if(level==null)
            return 0;
        return level.intValue();
    }

//    public ETFFile findETF(short stageId){
//        String stageName = stageId+"_v0";
//        return (ETFFile)etfs.get(stageName);
//    }

    public ETFFile fineETF(short id,String[] args){
        String name = id+"_v0";
        ETFFile etf = (ETFFile)etfs.get(name);
        etf = ETFUtil.clone(etf);
        if(etf!=null){
            for(int i=0;i<args.length;i++){
                String p = "{"+i+"}";
                replace(etf,p,args[i]);
            }
            return etf;
        }
        return null;
    }

    public ETFFile findETF(short stageId,int level){
        String stageName = stageId+"_v0";
        ETFFile etf = (ETFFile)etfs.get(stageName);
        etf = ETFUtil.clone(etf);
        if(stageId>=30000)
            return etf;
        if(etf!=null){
            for(int i=0;i<etf.stringTable.length;i++){
                String ss = etf.stringTable[i];
                Matcher matcher = pattern.matcher(ss);
                StringBuffer sb = new StringBuffer();
                while(matcher.find()){
                    int id = Integer.parseInt(matcher.group(1));
                    String t = tips.getTip(id,level);
                    if(t!=null){
                        matcher.appendReplacement(sb,t);
                    }
                }
                matcher.appendTail(sb);
                etf.stringTable[i] = sb.toString();
            }
        }
      return etf;
    }

    private void replace(ETFFile etf,String p,String s){
        for(int i=0;i<etf.stringTable.length;i++){
            String ss = etf.stringTable[i];
            if(ss.equals(p))
                etf.stringTable[i] = s;
        }
    }

    public ETFFile[] findETFs(short[] stageIds,int level){
        ETFFile[] ret = new ETFFile[stageIds.length];
        for(int i=0;i<stageIds.length;i++){
            ret[i] = findETF(stageIds[i],level);
        }
        return ret;
    }
    
    /**
     * 获得一个任务的所有寻路点。
     * @param taskID
     * @return 如果这个任务没有寻路点，返回null。
     */
    public List<TaskTracePoint> getTracePoints(int taskID) {
    	return taskTracePoints.get(taskID);
    }
}
