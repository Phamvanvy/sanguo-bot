package com.pip.sanguo.data.ant.flash;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.util.Settings;

/**
 * flash三国国际化，处理技能和BUFF的ant任务。
 * 
 * @author Frank
 */
public class FlashI18nProcessSkillAndBuffTask extends Task{
    /** 国际化data目录的位置。 */
    protected String source;
    /** 服务器代码的目录（src目录）。 */
    protected String target;

    @Override
    public void execute() throws BuildException {
        ProjectData proj = new ProjectData();
        try {
            proj.load(new File(source));
            Settings.exportClassDir = new File(target);
            Settings.skillPackage = "peony.game.skill";
            Settings.buffPackage = "peony.game.buff";
            proj.generateBuffClasses("utf8");
            proj.generateSkillClasses("utf8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
    
    /**
     * 设置国际化data目录的位置。
     * @param source
     */
    public void setSource(String source){
        this.source = source;
    }
    
    /**
     * 设置服务器代码的目录。
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
    }
}
