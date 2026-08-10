package com.pip.sanguo.data.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.util.Settings;

public class GenerateComplexSkillAndBuff extends Task{
    protected String source;
    protected String target;

    public void execute() throws BuildException {
        ProjectData proj = new ProjectData();
        try {
            proj.load(new File(source));
            Settings.exportClassDir = new File(target);
            Settings.skillPackage = "peony.game.skill";
            Settings.buffPackage = "peony.game.buff";
            proj.generateBuffClasses("utf8");
            proj.generateSkillClasses("utf8");
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
