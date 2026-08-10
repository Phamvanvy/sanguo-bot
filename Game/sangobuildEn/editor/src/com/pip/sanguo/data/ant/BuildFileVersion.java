package com.pip.sanguo.data.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.sanguo.data.ProjectData;

public class BuildFileVersion extends Task {
    
    protected String source;
    
    @Override
    public void execute() throws BuildException {
        ProjectData pd = new ProjectData();
        try {
            pd.load(new File(source));
            pd.generateResourceVersionXML();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    public void setSource(String source){
        this.source = source;
    }
}
