package com.pip.sanguo.data.ant;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.i18n.I18NProcessor;
import com.pip.sanguo.data.i18n.LocaleConfig;

public class DealSeverCode extends Task {
    protected String source;
    protected String target;

    public void execute() throws BuildException {
        ProjectData proj = new ProjectData();
        try {
            proj.load(new File(source));
            List<LocaleConfig> locales = LocaleConfig.getLocales(proj);
            I18NProcessor proc = new I18NProcessor(new File(target), locales.get(0));
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
}
