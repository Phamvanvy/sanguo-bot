package com.pip.sanguo.data.ant.flash;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.i18n.I18NProcessor;
import com.pip.sanguo.data.i18n.LocaleConfig;

/**
 * flash三国国际化，处理data的ant任务。
 * 
 * @author Frank
 */
public class FlashI18nProcessDataTask extends Task {
    /** 标准data目录的位置。 */
    protected String source;
    /** 国际化版本的 localeID，具体定义请参见data目录下的i18n.xml。 */
    protected String localeID;
    
    @Override
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
            
            I18NProcessor proc = new I18NProcessor(proj, locale);
            proc.process(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
    
    /**
     * 设置标准data目录的位置。
     * @param source
     */
    public void setSource(String source){
        this.source = source;
    }
    
    /**
     * 设置国际化版本的localeID。
     * @param localeID
     */
    public void setLocaleID(String localeID) {
        this.localeID = localeID;
    }
}