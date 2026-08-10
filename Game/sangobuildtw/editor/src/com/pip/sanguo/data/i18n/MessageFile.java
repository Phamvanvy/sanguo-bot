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
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * 本类处理Excel格式的字符串对应表。
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

    public MessageFile(File file, String lang1, String lang2) throws Exception {
        fromLang = lang1;
        toLang = lang2;
        sourceFile = file;
        load();
    }
    
    private void load() throws Exception {
        Workbook workbook = Workbook.getWorkbook(sourceFile);
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
                } else {
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
            Workbook srcbook = Workbook.getWorkbook(sourceFile);
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
                lbl = new Label(1, i, autoTranslate(newTexts.get(i)));
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
        HashMap<String, String> ret = new HashMap<String, String>();
        for (int i = 0; i < pageTitles.length; i++) {
            List<String> texts1 = (List<String>)pageTexts1[i];
            List<String> texts2 = (List<String>)pageTexts2[i];
            for (int j = 0; j < texts1.size(); j++) {
                ret.put(texts1.get(j), texts2.get(j));
            }
        }
        return ret;
    }
    
    public String autoTranslate(String src) {
        if ("zh_CN".equals(fromLang) && "zh_TW".equals(toLang)) {
            return BIG5toGBK.convertGB2BIG5(src);
        } else {
            return src;
        }
    }
}
