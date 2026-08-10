package com.pip.sanguo.data.i18n;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.quest.Quest;
import com.pipimage.utils.Utils;

/**
 * 本类处理Excel格式的字符串对应表。
 * 
 * @author lighthu
 */
public class MessageFile {
    /*
     * 源语言。
     */
    private String fromLang;
    /*
     * 目标语言。
     */
    private String toLang;
    /*
     * 页列表。
     */
    private String[] pageTitles;
    /*
     * 各页未翻译的字符串。
     */
    private List[] pageTexts1;
    /*
     * 各页翻译后的字符串。
     */
    private List[] pageTexts2;
    /*
     * 每个字符串的来源。
     */
    private List[] pageTextSources;
    /*
     * 源文件。
     */
    private File sourceFile;
    /*
     * 新增的字符串。
     */
    private List<String> newTexts;
    /*
     * 新增字符串的来源。
     */
    private List<String> newTextSources;
    /*
     * 所有字符串到翻译结果的映射。
     */
    private Map<String, String> searchTable;
    /*
     * 去掉控制数据以后的字符串表。用这个方式来做模糊匹配翻译。这个表里，key是去掉控制数据的字符串，value是原始字符串（可能有多个）。
     */
    private Map<String, List<String>> rawTextSearchTable;
    /*
     * 所有没有通过合法性检查的条目，对应其出错的原因。
     */
    private Map<String, String> errorItems;
    private Map<String, String> errorItemSource;
    private Map<String, String> errorItemMessage;

    public MessageFile(File file, String lang1, String lang2) throws Exception {
        System.out.println("load: " + file);
        fromLang = lang1;
        toLang = lang2;
        sourceFile = file;
        load();
        generateSearchTable();
        reportErrors();
    }
    
    // 生成rawTextSearchTable
    private void generateSearchTable() {
        searchTable = new HashMap<String, String>();
        rawTextSearchTable = new HashMap<String, List<String>>();
        errorItems = new HashMap<String, String>();
        errorItemSource = new HashMap<String, String>();
        errorItemMessage = new HashMap<String, String>();
        HashMap<String, String> cacheMap = new HashMap<String, String>();
        HashMap<String, String> cacheLineMap = new HashMap<String, String>();
        for (int i = 0; i < pageTitles.length; i++) {
            List<String> texts1 = (List<String>) pageTexts1[i];
            List<String> texts2 = (List<String>) pageTexts2[i];
            List<String> texts3 = (List<String>) pageTextSources[i];
            for (int j = 0; j < texts1.size(); j++) {
                String raw = texts1.get(j);
                String trans = texts2.get(j);
                String source = texts3.get(j);
                searchTable.put(raw, trans);
                
                // 查错
                String error = checkError(i, j, raw, trans, source, cacheMap, cacheLineMap);
                if (error != null) {
                    errorItems.put(raw, "页：" + pageTitles[i] + "\n行号：" + (j + 1) + "\n来源：" + source + "\n" + error);
                    errorItemSource.put(raw, source);
                    errorItemMessage.put(raw, error);
                }
                
                // 去掉控制数据
                String tmp = removeControlData(raw);
                if (tmp != null) {
                    List<String> list = rawTextSearchTable.get(tmp);
                    if (list == null) {
                        list = new ArrayList<String>();
                        rawTextSearchTable.put(tmp, list);
                    }
                    list.add(raw);
                }
            }
        }
    }
    
    /**
     * 在标准输出报告错误。
     */
    public void reportErrors() {
        for (String error : errorItems.keySet()) {
            System.err.println(error + "\n" + searchTable.get(error) + "\n" + errorItems.get(error));
            System.err.println("--------------------------------------------------------------------------------------------------------------------------------------------------");
        }
    }
    
    /**
     * 发现的错误个数。
     * @return
     */
    public int getErrorCount() {
        return errorItems.size();
    }
    
    /**
     * 把错误输出到一个excel文件中。这个文件可以不存在。
     * @param file
     */
    public void reportErrorToExcel(String file) throws Exception {
        // 生成一个空的excel文件
        InputStream is = getClass().getResourceAsStream("/com/pip/sanguo/data/i18n/empty_sheet.xls");
        byte[] buf = new byte[256];
        FileOutputStream fos = new FileOutputStream(file);
        int len;
        while ((len = is.read(buf)) != -1) {
            if (len > 0) {
                fos.write(buf, 0, len);
            }
        }
        fos.close();
        is.close();
        
        // 在这个excel的第一页写内容
        WorkbookSettings set = new WorkbookSettings();
        set.setEncoding("ISO-8859-1");
        Workbook newbook = Workbook.getWorkbook(new File(file), set);
        WritableWorkbook writebook = Workbook.createWorkbook(new File(file), newbook);
        WritableSheet outsheet = writebook.getSheet(0);
        int lineNum = 0;
        for (String error : errorItems.keySet()) {
            String errorTrans = searchTable.get(error);
            String errorSource = errorItemSource.get(error);
            String errorMessage = errorItemMessage.get(error);
            outsheet.addCell(new Label(0, lineNum, error));
            outsheet.addCell(new Label(1, lineNum, errorTrans));
            outsheet.addCell(new Label(2, lineNum, errorSource));
            outsheet.addCell(new Label(3, lineNum, errorMessage));
            lineNum++;
        }
        writebook.write();
        writebook.close();
    }
    
    /*
     * 检查一个翻译条目是否存在可能的翻译错误。检查的规则包括：
     * 1. 如果原始字符串中有|或者\n，那么按这些字符串拆分检查，每个区段的翻译都不能为空（除非原始字符串是空）。
     * 2. 每个翻译条目trim以后都不能为空。
     * 3. 如果原始字符串中出现了${xxx}变量，这一部分必须在翻译后的字符串中也原样出现（和自动翻译结果一样）。
     * 4. 如果source为Quest Variable，那么不能翻译（和自动翻译结果一样）。
     * 5. 可能的翻译不一致（在某个地方翻译成A，在另外一个多行的地方翻译成B）。
     * 6. 如果原始字符串中出现了<n></n>和<l></l>标记，那么必须保证翻译后的文字这部分的格式也是正确的。
     * <n>id,name(mapname:x,y)</n>
     * <l>mapid,mapname:x,y</l>
     * <i>xxx</i>
     */
    private String checkError(int pageIndex, int lineNum, String raw, String trans, String source, Map<String, String> cacheMap, Map<String, String> cacheLineMap) {
        if (raw.trim().isEmpty()) {
            return "词条为空";
        }
        if (source.equals("Quest Variable")) {
            String t = autoTranslate(raw, false);
            if (!trans.equals(t)) {
                return "任务变量不能被翻译";
            }
        }
        String[] lines = raw.split("\n|\\|");
        String[] tlines = trans.split("\n|\\|");
        if (lines.length != tlines.length) {
            return "翻译前后行数不一致";
        }
        for (int i = 0; i < lines.length; i++) {
            String r = lines[i];
            String t = tlines[i];
            if (r.trim().isEmpty() && !t.trim().isEmpty()) {
                return "翻译前为空，翻译后不为空";
            }
            if (!r.trim().isEmpty() && t.trim().isEmpty()) {
                return "翻译前不为空，翻译后为空";
            }
            
            // 检查是否和以前出现的翻译不一致
            if (cacheMap.containsKey(r) && !t.equals(cacheMap.get(r))) {
                return r + "和其他地方翻译不一致：" + cacheLineMap.get(r);
            }
            cacheMap.put(r, t);
            cacheLineMap.put(r, "页：" + pageTitles[pageIndex] + " 行：" + (lineNum + 1));
            
            // 检查可能出现的变量
            List<String> varList1 = findVariables(r);
            List<String> varList2 = findVariables(t);
            if (varList1.size() != varList2.size()) {
                return "变量翻译不一致";
            }
            for (int j = 0; j < varList1.size(); j++) {
                String tt = autoTranslate(varList1.get(j), false);
                for (int k = 0; k < varList2.size(); k++) {
                    if (varList2.get(k).equals(tt)) {
                        varList2.remove(k);
                        break;
                    }
                }
            }
            if (varList2.size() != 0) {
                return "变量翻译不一致";
            }
            
            // 检查可能出现的NPC引用
            varList1 = findNPCRef(r);
            varList2 = findNPCRef(t);
            if (varList1.size() != varList2.size()) {
                return "NPC引用翻译不一致";
            }
            for (int j = 0; j < varList2.size(); j++) {
                if (!checkNPCRef(varList2.get(j))) {
                    return "NPC引用格式不正确: " + varList2.get(j);
                }
            }
            
            // 检查可能出现的<i>
            varList1 = findNumber(r);
            varList2 = findNumber(t);
            if (varList1.size() != varList2.size()) {
                return "数字标示翻译不一致";
            }
            
            // 检查可能出现的<c>
            varList1 = findColor(r);
            varList2 = findColor(t);
            if (varList1.size() != varList2.size()) {
                return "颜色标示翻译不一致";
            }
            
           // 检查可能出现的LOCATION
            varList1 = findLocationRef(r);
            varList2 = findLocationRef(t);
            if (varList1.size() != varList2.size()) {
                return "位置引用翻译不一致";
            }
            for (int j = 0; j < varList2.size(); j++) {
                if (!checkLocationRef(varList2.get(j))) {
                    return "位置引用格式不正确: " + varList2.get(j);
                }
            }
        }
        return null;
    }
    
    // 检查NPC引用格式是否正确，必须是<n>id,name(mapname:x,y)</n>
    private boolean checkNPCRef(String refText) {
        try {
            int pos1 = refText.indexOf(',');
            if (pos1 == -1) {
                return false;
            }
            int pos2 = refText.indexOf('(', pos1 + 1);
            if (pos2 == -1) {
                return false;
            }
            int pos3 = refText.indexOf(':', pos2 + 1);
            if (pos3 == -1) {
                return false;
            }
            int pos4 = refText.indexOf(',', pos3 + 1);
            if (pos4 == -1) {
                return false;
            }
            int pos5 = refText.indexOf(')', pos4 + 1);
            if (pos5 == -1) {
                return false;
            }
            String sec1 = refText.substring(0, pos1);
            String sec2 = refText.substring(pos1 + 1, pos2);
            String sec3 = refText.substring(pos2 + 1, pos3);
            String sec4 = refText.substring(pos3 + 1, pos4);
            String sec5 = refText.substring(pos4 + 1, pos5);
            String sec6 = refText.substring(pos5 + 1);
            if (!isNumber(sec1) || !isNumber(sec4) || !isNumber(sec5) || sec6.length() > 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isNumber(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if ((ch >= '0' && ch <= '9') || ch == '-') {
            } else {
                return false;
            }
        }
        return true;
    }
    
    // 检查位置引用格式是否正确，必须是<l>mapid,mapname:x,y</l>
    private boolean checkLocationRef(String refText) {
        try {
            int pos1 = refText.indexOf(',');
            if (pos1 == -1) {
                return false;
            }
            int pos2 = refText.indexOf(':', pos1 + 1);
            if (pos2 == -1) {
                return false;
            }
            int pos3 = refText.indexOf(',', pos2 + 1);
            if (pos3 == -1) {
                return false;
            }
            String sec1 = refText.substring(0, pos1);
            String sec2 = refText.substring(pos1 + 1, pos2);
            String sec3 = refText.substring(pos2 + 1, pos3);
            String sec4 = refText.substring(pos3 + 1);
            if (!isNumber(sec1) || !isNumber(sec3) || !isNumber(sec4)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<String> findColor(String text) {
        List<String> list = new ArrayList<String>();
        int pos = text.indexOf("<c");
        while (pos != -1) {
            int pos2 = text.indexOf("</c>", pos);
            if (pos2 == -1) {
                break;
            }
            list.add(text.substring(pos, pos2 + 4));
            pos = text.indexOf("<c", pos2);
        }
        return list;
    }
    
    private List<String> findNumber(String text) {
        List<String> list = new ArrayList<String>();
        int pos = text.indexOf("<i>");
        while (pos != -1) {
            int pos2 = text.indexOf("</i>", pos);
            if (pos2 == -1) {
                break;
            }
            list.add(text.substring(pos + 3, pos2));
            pos = text.indexOf("<i>", pos2);
        }
        return list;
    }
    
    private List<String> findLocationRef(String text) {
        List<String> list = new ArrayList<String>();
        int pos = text.indexOf("<l>");
        while (pos != -1) {
            int pos2 = text.indexOf("</l>", pos);
            if (pos2 == -1) {
                break;
            }
            list.add(text.substring(pos + 3, pos2));
            pos = text.indexOf("<l>", pos2);
        }
        return list;
    }
    
    private List<String> findNPCRef(String text) {
        List<String> list = new ArrayList<String>();
        int pos = text.indexOf("<n>");
        while (pos != -1) {
            int pos2 = text.indexOf("</n>", pos);
            if (pos2 == -1) {
                break;
            }
            list.add(text.substring(pos + 3, pos2));
            pos = text.indexOf("<n>", pos2);
        }
        return list;
    }
    
    private List<String> findVariables(String text) {
        List<String> list = new ArrayList<String>();
        int pos = text.indexOf("${");
        while (pos != -1) {
            int pos2 = text.indexOf("}", pos);
            if (pos2 == -1) {
                break;
            }
            list.add(text.substring(pos, pos2 + 1));
            pos = text.indexOf("${", pos2);
        }
        return list;
    }
    
    /*
     * 去掉一个字符串里的控制数据。控制数据包括：
     * <n>和</n>之间的部分
     * <l>和</l>之间的部分
     * ${和}之间的部分
     * 所有非中文字符0x4E00到0x9FA5之间
     */
    private String removeControlData(String str) {
        str = removePart(str, "<n>", "</n>");
        str = removePart(str, "<l>", "</l>");
        str = removePart(str, "${", "}");
        return removeNonCNChars(str);
    }
    
    /*
     * 移除一个字符串中的非汉字字符。
     */
    private String removeNonCNChars(String str) {
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch >= 0x4E00 && ch <= 0x9FA5) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    /*
     * 移除一个字符串中所有在某两个模板中间的部分。
     */
    private String removePart(String str, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder(str.length());
        int start = 0;
        int pos = str.indexOf(prefix, start);
        while (pos != -1) {
            int pos2 = str.indexOf(suffix, pos + prefix.length());
            if (pos2 == -1) {
                break;
            }
            sb.append(str.substring(start, pos));
            start = pos2 + suffix.length();
            pos = str.indexOf(prefix, start);
        }
        sb.append(str.substring(start));
        return sb.toString();
    }

    private void load() throws Exception {
        WorkbookSettings set = new WorkbookSettings();
        set.setEncoding("ISO-8859-1");
        Workbook workbook = Workbook.getWorkbook(sourceFile, set);
        Sheet[] sheets = workbook.getSheets();
        pageTitles = new String[sheets.length];
        pageTexts1 = new List[sheets.length];
        pageTexts2 = new List[sheets.length];
        pageTextSources = new List[sheets.length];
        newTexts = new ArrayList<String>();
        newTextSources = new ArrayList<String>();
        for (int i = 0; i < sheets.length; i++) {
            Sheet sheet = sheets[i];
            pageTitles[i] = sheet.getName();
            int rows = sheet.getRows();
            List<String> texts1 = new ArrayList<String>();
            List<String> texts2 = new ArrayList<String>();
            List<String> texts3 = new ArrayList<String>();
            for (int j = 0; j < rows; j++) {
                String text1 = sheet.getCell(0, j).getContents();
                text1 = text1.replaceAll("\r\n", "\n");
                String text2 = sheet.getCell(1, j).getContents();
                text2 = text2.replaceAll("\r\n", "\n");
                texts1.add(text1);
                texts2.add(text2);
                if (sheet.getColumns() > 2) {
                    texts3.add(sheet.getCell(2, j).getContents());
                }
                else {
                    texts3.add("");
                }
            }
            pageTexts1[i] = texts1;
            pageTexts2[i] = texts2;
            pageTextSources[i] = texts3;
        }
        workbook.close();
    }

    public void save() throws Exception {
        if (newTexts.size() > 0) {
            WorkbookSettings set = new WorkbookSettings();
            set.setEncoding("ISO-8859-1");
            Workbook srcbook = Workbook.getWorkbook(sourceFile, set);
            WritableWorkbook workbook = Workbook.createWorkbook(sourceFile, srcbook);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
            String name = sdf.format(new Date());
            int id = 1;
            while (true) {
                String nname = name + "_" + id;
                if (workbook.getSheet(nname) == null) {
                    break;
                }
                id++;
            }
            WritableSheet sheet = workbook.createSheet(name + "_" + id, 0);
            for (int i = 0; i < newTexts.size(); i++) {
                Label lbl = new Label(0, i, newTexts.get(i));
                sheet.addCell(lbl);
                lbl = new Label(1, i, autoTranslate(newTexts.get(i), !newTextSources.get(i).equals("Quest Variable")));
                sheet.addCell(lbl);
                lbl = new Label(2, i, newTextSources.get(i));
                sheet.addCell(lbl);
            }
            workbook.write();
            workbook.close();
        }
    }

    public void addString(String str, String cause) {
        newTexts.add(str);
        newTextSources.add(cause);
    }

    public Map<String, String> getMap() {
        return searchTable;
    }

    /**
     * 自动翻译一个字符串。
     * @param src
     * @return
     */
    public String autoTranslate(String src, boolean supportSmartMatch) {
        // 支持简繁体自动转换
        if ("zh_CN".equals(fromLang) && "zh_TW".equals(toLang)) {
            return BIG5toGBK.convertGB2BIG5(src);
        }
        supportSmartMatch = false;   // 暂时关闭自动翻译功能
        if (supportSmartMatch) {
            // 去掉控制字符看是否有匹配
            String tmp = removeControlData(src);
            if (rawTextSearchTable.containsKey(tmp)) {
                String raw = rawTextSearchTable.get(tmp).get(0);    // 原始字符串
                String trans = searchTable.get(raw);                // 原始字符串的翻译
                return autoTranslate(src, raw, trans);
            }
        }
        
        return src;
    }
    
    /*
     * 尝试用一个参考翻译来自动翻译一个新字符串。
     * @param newText 新字符串
     * @param refText 参考字符串，这个字符串去掉控制字符后和新字符串相同
     * @param refTrans 参考字符串的翻译
     * @return
     */
    private String autoTranslate(String newText, String refText, String refTrans) {
        return refText + "\n" + refTrans;
    }
    
    public static void merge(String newFile, String oldFile) throws Exception {
        MessageFile f1 = new MessageFile(new File(oldFile), "zh_CN", "en_US");
        Map<String, String> transMap = f1.getMap();

        WorkbookSettings set = new WorkbookSettings();
        set.setEncoding("ISO-8859-1");
        Workbook newbook = Workbook.getWorkbook(new File(newFile), set);
        WritableWorkbook writebook = Workbook.createWorkbook(new File(newFile), newbook);
        for (WritableSheet outsheet : writebook.getSheets()) {
            int rows = outsheet.getRows();
            for (int j = 0; j < rows; j++) {
                String text1 = outsheet.getCell(0, j).getContents();
                if (transMap.containsKey(text1)) {
                    Label lbl = new Label(1, j, transMap.get(text1));
                    outsheet.addCell(lbl);
                }
            }
        }
        writebook.write();
        writebook.close();
    }
    
    /**
     * 校正文件中出现的所有NPC引用和位置引用。
     * @param file
     * @throws Exception
     */
    public static void adjustRef(ProjectData proj, String file) throws Exception {
        WorkbookSettings set = new WorkbookSettings();
        set.setEncoding("ISO-8859-1");
        Workbook newbook = Workbook.getWorkbook(new File(file), set);
        WritableWorkbook writebook = Workbook.createWorkbook(new File(file), newbook);
        for (WritableSheet outsheet : writebook.getSheets()) {
            int rows = outsheet.getRows();
            for (int j = 0; j < rows; j++) {
                String text2 = outsheet.getCell(1, j).getContents();
                try {
                    String newText = Quest.validateMixedText(proj, text2);
                    if (!text2.equals(newText)) {
                        Label lbl = new Label(1, j, newText);
                        outsheet.addCell(lbl);
                    }
                } catch (Exception e) {
                    System.err.println("错误：" + text2);
                }
            }
        }
        writebook.write();
        writebook.close();
    }

    public static void main(String[] args) throws Exception {
        MessageFile f1 = new MessageFile(new File("C:\\Users\\lighthu\\Desktop\\messages_trans.xls"), "zh_CN", "en_US");
        File sourceFile = new File("C:\\Users\\lighthu\\Desktop\\messages.xls");
        File destFile = new File("C:\\Users\\lighthu\\Desktop\\messages2.xls");

        Workbook srcbook = Workbook.getWorkbook(sourceFile);
        Workbook destbook = Workbook.getWorkbook(destFile);
        WritableWorkbook writebook = Workbook.createWorkbook(destFile, destbook);
        Sheet[] sheets = srcbook.getSheets();
        Map<String, String> existMap = f1.getMap();
        int found = 0;
        for (int i = 0; i < sheets.length; i++) {
            Sheet sheet = sheets[i];
            WritableSheet outsheet = writebook.createSheet(sheet.getName(), i);
            int rows = sheet.getRows();
            for (int j = 0; j < rows; j++) {
                String text1 = sheet.getCell(0, j).getContents();
                text1 = text1.replaceAll("\r\n", "\n");
                String text2 = sheet.getCell(1, j).getContents();
                text2 = text2.replaceAll("\r\n", "\n");
                String text3 = sheet.getCell(2, j).getContents();

                if (!text3.equals("Quest Variable")) {
                    if (existMap.containsKey(text1) && !existMap.get(text1).equals(text1)) {
                        text2 = existMap.get(text1);
                        System.out.println("found: " + text1);
                        found++;
                    } else {
                        text3 = "未找到匹配" + text3;
                    }
                }
                
                Label lbl = new Label(0, j, text1);
                outsheet.addCell(lbl);
                lbl = new Label(1, j, text2);
                outsheet.addCell(lbl);
                lbl = new Label(2, j, text3);
                outsheet.addCell(lbl);
            }
        }
        System.out.println("total: " + found);
        srcbook.close();
        writebook.write();
        writebook.close();
    }
}
