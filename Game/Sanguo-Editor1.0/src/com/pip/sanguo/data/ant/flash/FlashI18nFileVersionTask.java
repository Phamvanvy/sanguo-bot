package com.pip.sanguo.data.ant.flash;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.sanguo.data.ProjectData;

/**
 * flash三国国际化生成file version的ant任务。由于相关的资源都已经打包到客户端中，所以目前不需要用到此任务。
 * 
 * @author Frank
 */
public class FlashI18nFileVersionTask extends Task {
    protected String source;

    public void execute() throws BuildException {
        ProjectData proj = new ProjectData();
        try {
            proj.load(new File(source));
            proj.generateResourceVersionXML();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    public void setSource(String source) {
        this.source = source;
    }
    
}
