package com.pip.j0ide.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.pip.gtl.codegen.CodeGenException;
import com.pip.gtl.codegen.GTLFunctionCallGenerator;
import com.pip.gtl.codegen.syscall.SystemFunctionManager;
import com.pip.gtl.compiler.GTLCompiler;
import com.pip.gtl.decompiler.ETFDebugInfo;
import com.pip.gtl.preprocess.GTLPreProcessor;
import com.pip.j0ide.Application;
import com.pip.j0ide.Settings;
import com.pip.j0ide.data.Model;
import com.pip.j0ide.data.ProjectData;
import com.pip.j0ide.data.Variable;
import com.pip.util.Utils;

public class CompileJ0Task extends Task {
	protected String revision;
	protected String target;
	protected String source;
	protected String polishDir;
	protected String linkDir;
    
    @Override
    public void execute() throws BuildException {
        ProjectData pd = new ProjectData();
        try {
        	pd.load(new File(source));
        	
        	if (linkDir != null) {
        		SystemFunctionManager.configure(new File[] {
        				new File(pd.getBaseDir(), "gtl/functions.properties"),
        				new File(linkDir, "core_functions.properties")
        		});
        		GTLPreProcessor.setFindDir(pd.getBaseDir(), new File(linkDir));
        	} else {
        		SystemFunctionManager.configure(new File[] {
        				new File(pd.getBaseDir(), "gtl/functions.properties")
        		});
        	}
        	
        	// 找出所有GTL文件
        	List<File> gtlFiles = new ArrayList<File>();
        	findAllGTL(pd, new File(source), gtlFiles);
        	
        	// 取出所有机型
        	Model[] models = pd.getModels();
        	
        	for (Model m : models) {
        		doCompile(pd, gtlFiles, m, revision);
        	}
        } catch (Throwable e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }
    
    protected void doCompile(ProjectData project, List<File> gtlFiles, Model device, String revision) throws Throwable {
    	// 获取机型参数
		HashMap<String, String> params = new HashMap<String, String>();
		for (int j = 0; j < project.variables.size(); j++) {
		    params.put(project.variables.get(j).name, project.variables.get(j).value);
		}
		for (int j = 0; j < device.variables.size(); j++) {
			params.put(device.variables.get(j).name, device.variables.get(j).value);
		}
		params.put("Revision", revision);

		// 逐个编译所有文件
		Set<File> compiledList = new HashSet<File>();
		Map<File, ETFDebugInfo> etdCache = new HashMap<File, ETFDebugInfo>();
		for (int i = 0; i < gtlFiles.size(); i++) {
			File sourceFile = gtlFiles.get(i);
			if (compiledList.contains(sourceFile)) {
			    continue;
			}
        	String msg = "编译" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), device.id) + "  成功！";
			long startTime = System.currentTimeMillis();
	        try {
	            GTLCompiler comp = new GTLCompiler(polishDir, device.id, device.device, params);
	            comp.setTargetPath(new File(target, device.id));
	            File file = comp.compile(sourceFile, true, compiledList, etdCache);
	            System.out.println(sourceFile);
	            if (file == null) {
	            	msg = "编译" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), device.id) +"  跳过！";
	            }
	        } catch (CodeGenException cge) {
	            msg = "Error:"+sourceFile.getName()+"\n"+cge.toString();
	        	throw cge;
	        } catch (Throwable e) {
	        	msg = "Error:"+sourceFile.getName()+"\n"+e.toString();
	        	throw e;
	        }
	        long endTime = System.currentTimeMillis();
	        System.out.println(msg + " 耗时：" + (endTime - startTime) + "ms");
		}
    }

    public void setSource(String source){
        this.source = source;
    }
    
    public void setRevision(String revision) {
    	this.revision = revision;
    }
    
    public void setTarget(String target) {
    	this.target = target;
    }
    
    public void setPolishDir(String dir) {
    	this.polishDir = dir;
    }
    
    public void setLinkDir(String dir) {
    	this.linkDir = dir;
    }
    
    private void findAllGTL(ProjectData project, File dir, List<File> output) {
		String projectGTLVersion = "1";
		for (Variable var : project.variables){
			if (var.name.equals("GTLVersion")){
				projectGTLVersion = var.value;
				break;
			}
		}
	    File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        if (files[i].isDirectory()) {
	            findAllGTL(project, files[i], output);
	        } else if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".gtl")) {
	            try {
	                String content = Utils.loadFileContent(files[i]);
	                boolean match = false;
	                BufferedReader br = new BufferedReader(new StringReader(content));
	                String line;
	                while ((line = br.readLine()) != null) {
	                	line = line.trim();
	                	if (!line.contains("VERSION")) {
	                		continue;
	                	}
	                	if (line.matches("\\s*VERSION\\s*" + projectGTLVersion + "\\s*;.*")) {
	                		match = true;
	                		break;
	                	}
	                }
	                if (match) {
	                    output.add(files[i]);
	                } else {
	                	System.out.println("Project GTL Verion "+projectGTLVersion+" miss math:"+files[i].getName());
	                }
	            } catch (Exception e) {
	            }
	        }
	    }
	}
}
