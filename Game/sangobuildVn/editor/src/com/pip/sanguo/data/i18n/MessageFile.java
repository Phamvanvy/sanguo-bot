package com.pip.sanguo.data.i18n;

import java.io.File;
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

    public MessageFile(File file, String lang1, String lang2) throws Exception {
        fromLang = lang1;
        toLang = lang2;
        sourceFile = file;
        load();
        generateSearchTable();
    }
    
    // 生成rawTextSearchTable
    private void generateSearchTable() {
        searchTable = new HashMap<String, String>();
        rawTextSearchTable = new HashMap<String, List<String>>();
        for (int i = 0; i < pageTitles.length; i++) {
            List<String> texts1 = (List<String>) pageTexts1[i];
            List<String> texts2 = (List<String>) pageTexts2[i];
            for (int j = 0; j < texts1.size(); j++) {
                String raw = texts1.get(j);
                String trans = texts2.get(j);
                searchTable.put(raw, trans);
                
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
        if (supportSmartMatch) {
            // 去掉控制字符看是否有匹配
            String tmp = removeControlData(src);
            if (rawTextSearchTable.containsKey(tmp)) {
                String raw = rawTextSearchTable.get(tmp).get(0);    // 原始字符串
                String trans = searchTable.get(raw);                // 原始字符串的翻译
                return autoTranslate(src, raw, trans);
            }
        }
        
        // 支持简繁体自动转换
        if ("zh_CN".equals(fromLang) && "zh_TW".equals(toLang)) {
            return BIG5toGBK.convertGB2BIG5(src);
        } else {
            return src;
        }
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
