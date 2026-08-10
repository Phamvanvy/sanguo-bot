package com.pip.sanguo.data.ant;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.i18n.I18NProcessor;
import com.pip.sanguo.data.i18n.LocaleConfig;

public class DealScriptsTask extends Task {
    protected String source;
    protected String target;
    protected String localeID;

    public void execute() throws BuildException {
        ProjectData proj = new ProjectData();
        try {
            proj.load(new File(source));
            List<LocaleConfig> locales = LocaleConfig.getLocales(proj);
            
            LocaleConfig locale = null;
            // 查找locale。
            for (int i = 0; i < locales.size(); i ++) {
                if (locales.get(i).id.equals(localeID)) {
                    locale = locales.get(i);
                    break;
                }
            }
            
            // 若locale为空，则抛出异常。
            if (locale == null) {
                throw new BuildException("未找到指定locale，请检查data目录下的i18n.xml中是否包含id为（" + localeID + "）的locale。");
            }
            System.out.println("source:"+source+" dest:"+target+" id:"+localeID);
            I18NProcessor proc = new I18NProcessor(new File(target), locale,I18NProcessor.MODE_SCRIPT);
            proc.process(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    
    public void setLocaleID(String localeID) {
        this.localeID = localeID;
    }
}
