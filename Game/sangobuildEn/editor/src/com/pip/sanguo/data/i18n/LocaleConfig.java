package com.pip.sanguo.data.i18n;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.sanguo.data.ProjectData;
import com.pip.util.Utils;

/**
 * 一个本地语言版本的定义。
 * @author lighthu
 */
public class LocaleConfig {
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 语言ID，例如：zh_CN
     */
    public String id;
    /**
     * 文字编码。
     */
    public String encoding;
    /**
     * 显示标题。
     */
    public String title;
    /**
     * 字符串翻译文件路径。
     */
    public File messageFile;
    /**
     * 此语言特殊文件路径（下面的目录结构应该和data下的目录结构对应）。和special_resource不同，这个目录下
     * 的文件会在本地化之前拷贝到目标目录。
     */
    public File revisionResourceDir;
    /**
     * 此语言特殊文件路径（下面的目录结构应该和data下的目录结构对应）。
     */
    public File specialResourceDir;
    /**
     * 输出目录。
     */
    public File outputDir;
    
    public LocaleConfig(ProjectData owner) {
        this.owner = owner;
    }
    
    public void load(Element elem) {
        id = elem.getAttributeValue("id");
        encoding = elem.getAttributeValue("encoding");
        title = elem.getAttributeValue("title");
        String path = elem.getAttributeValue("message_file");
        messageFile = new File(owner.baseDir, path);
        path = elem.getAttributeValue("revision_resource_dir");
        revisionResourceDir = new File(owner.baseDir, path);
        path = elem.getAttributeValue("special_resource_dir");
        specialResourceDir = new File(owner.baseDir, path);
        path = elem.getAttributeValue("output_dir");
        outputDir = new File(owner.baseDir, path);
    }
    
    public static List<LocaleConfig> getLocales(ProjectData proj) throws Exception {
        List<LocaleConfig> ret = new ArrayList<LocaleConfig>();
        Document doc = Utils.loadDOM(new File(proj.baseDir, "i18n.xml"));
        List list = doc.getRootElement().getChildren("locale");
        for (int i = 0; i < list.size(); i++) {
            Element elem = (Element)list.get(i);
            LocaleConfig locale = new LocaleConfig(proj);
            locale.load(elem);
            ret.add(locale);
        }
        return ret;
    }
    
    public String toString() {
        return title;
    }
}
