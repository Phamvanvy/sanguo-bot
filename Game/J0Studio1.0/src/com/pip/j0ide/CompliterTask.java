package com.pip.j0ide;

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

import com.pip.gtl.codegen.GTLFunctionCallGenerator;
import com.pip.gtl.codegen.syscall.SystemFunctionManager;
import com.pip.gtl.compiler.GTLCompiler;
import com.pip.gtl.decompiler.ETFDebugInfo;
import com.pip.j0ide.data.Model;
import com.pip.j0ide.data.ProjectData;
import com.pip.j0ide.data.Variable;
import com.pip.util.Utils;

public class CompliterTask extends Task{
	String workPath = null;
	String distPath = null;
	String polishPath = null;
	
	private File[] sourceFiles;
	private ProjectData project;
	
	public void execute() throws BuildException {
		// 加载项目信息
		File workDir = new File(workPath);
		project = new ProjectData();
		try {
			project.load(workDir);
		} catch (Exception e) {
			System.out.println("工作目录不正确");
			e.printStackTrace();
            return;
		}
		// 初始化function
		File linkDir = project.getLinkDir();
    	if (linkDir != null) {
    		SystemFunctionManager.configure(new File[] {
    				new File(workPath, "gtl/functions.properties"),
    				new File(linkDir, "core_functions.properties")
    		});
    	} else {
    		SystemFunctionManager.configure(new File[] {
    				new File(workPath, "gtl/functions.properties")
    		});
    	}
    	
		// 加载所有GTL文件
		List<File> allFiles = new ArrayList<File>();
        findAllGTL(workDir, allFiles);
        if (allFiles.size() == 0) {
        	String msg = "没有找到需要编译的文件.\n" +
        			"请检查<全局变量><GTLVersion>设置是否正确.\n" +
        			"如果没有此变量,请新建,并设置为脚本里的VERSION值.";
            System.out.println(">>>" + msg);
            return;
        }
        sourceFiles = new File[allFiles.size()];
        allFiles.toArray(sourceFiles);
        complite();
    }
	
	public void complite() {
		// 获取机型参数
		Model[] devices = project.getModels();
		System.out.println("项目路径：" + workPath);
		// 编译每个机型的所有脚本
		for (int i = 0; i < devices.length; i++) {
			Model device = devices[i];
			HashMap<String, String> params = new HashMap<String, String>();
			for (int j = 0; j < project.variables.size(); j++) {
				params.put(project.variables.get(j).name, project.variables.get(j).value);
			}
			for (int j = 0; j < device.variables.size(); j++) {
				params.put(device.variables.get(j).name, device.variables.get(j).value);
			}
			if(!complite2(device.id, device.device, params)) {
				break;
			}
		}
	}
	
	//编译一个机型的所有脚本
	public boolean complite2(String deviceID, String deviceName, HashMap<String, String> params) {
		boolean hasError = false;
		Set<File> compiledList = new HashSet<File>();
		Map<File, ETFDebugInfo> etdCache = new HashMap<File, ETFDebugInfo>();
		System.out.println("开始编译：" + deviceID + " : " + deviceName);
		for (int i = 0; i < sourceFiles.length; i++) {
			if (hasError) {
				return false;
			}
			File sourceFile = sourceFiles[i];
			String msg = "编译" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), deviceID) + "  成功！";
//        	try {
//        		Application.getInstance().getConsole().syncPrintln("正在编译" + sourceFile.getName() + "/" + device.id + "...");
//        	} catch (Exception e) {
//        	}
			long startTime = System.currentTimeMillis();
	        try {
	        	if (compiledList.contains(sourceFile)) {
					System.out.println("已编译脚本：" + sourceFile.getName());
					continue;
				}
	            GTLCompiler comp = new GTLCompiler(polishPath, deviceID, deviceName, params);
	            //检测是否需要进行revision代码限定
	            boolean limitRevision = false;
	            String rv = params.get("Revision");
	            if(rv != null){
	                for(Variable v : project.targetsLimit){
	                    if(rv.equals(v.name) && v.value.equals("true")){
	                        limitRevision = true;
	                        break;
	                    }
	                }
	            }
	            if(limitRevision){
	                comp.addLimitRevision(rv);
	            }
	            
	            comp.setTargetPath(new File(distPath, deviceID));
	            comp.compile(sourceFile, true, compiledList, etdCache);
	            if(comp.limitRevisionNeedPass()){
	                msg = "由于没有指定Revision的相关代码，跳过" + String.format("%5s%-30s%-15s", "", sourceFile.getName(), deviceID);
	            }
	        } catch (Exception e) {
	        	msg = "Error:"+sourceFile.getName()+"\n"+e.toString();
                hasError = true;
                e.printStackTrace();
	        }
        	try {
        	    long endTime = System.currentTimeMillis();
        		System.out.println(msg + " 耗时：" + (endTime - startTime) + "ms");
        	} catch (Exception e) {
        	}
		}
		return true;
	}
	
	private void findAllGTL(File dir, List<File> output) {
		String projectGTLVersion = "1";
		for(Variable var:project.variables){
			if(var.name.equals("GTLVersion")){
				projectGTLVersion = var.value;
				break;
			}
		}
	    File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        if (files[i].isDirectory()) {
	            findAllGTL(files[i], output);
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
//	                	System.out.println("Project GTL Verion "+projectGTLVersion+" miss math:"+files[i].getName());
	                }
	            } catch (Exception e) {
	            }
	        }
	    }
	}
	
	public void setWorkPath(String sourcePath) {
		this.workPath = sourcePath;
	}

	public void setDistPath(String distPath) {
		this.distPath = distPath;
	}

	public void setPolishPath(String polishPath) {
		this.polishPath = polishPath;
	}


	public static final void main(String[] args) {
		CompliterTask cmp = new CompliterTask();
		cmp.setWorkPath("/Volumes/Data/Develop_PiP-Engine/Tools/XuanYuan1.0-Data/dataui");
		cmp.setDistPath("/Volumes/Data/temp/scripts/");
		cmp.setPolishPath("/Volumes/Tools/Java/J2ME-Polish2.0.3");
		cmp.execute();
	}
}
