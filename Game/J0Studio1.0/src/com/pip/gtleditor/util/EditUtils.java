package com.pip.gtleditor.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.pip.j0ide.Application;

public class EditUtils {
	
	public static void createEmptyTaskUI(File source) throws IOException{
		FileOutputStream fos = null;
		
		File template = new File(Application.getInstance().getProjectData().getGTLDir()+"/dev/ui_template.gtl");
		if(template.exists()){
			try{
				copyFile(template,source);
			}catch(Exception e){
				System.out.println("模板错误");
			}
		}else{
	        try{
	            fos = new FileOutputStream(source);
	            OutputStreamWriter osw = new OutputStreamWriter(new BufferedOutputStream(fos), "GBK");
	            PrintWriter pw = new PrintWriter(osw, true);
	            int gtlVersion = Integer.parseInt(Application.getInstance().getProjectData().getGTLVersion());
	            pw.println("#include \"..\\general.gtl\"");
	            pw.println();
	            pw.println("VERSION " + gtlVersion + ";" + "//根据项目不同编辑");
	            pw.println("ID 0;");
	            pw.println("ATTRIBUTE 128;//根据脚本需求修改");
	            pw.print("NAME \"");
	            String name = source.getName();
	            pw.print(name.substring(0, name.length() - 4));
	            pw.println("\";");
	            pw.print("DESCRIPTION \"");
	            pw.println("\"; //TODO 输入脚本描述信息（必填）");
	            pw.println("");
	            pw.println("DATA{");
	            pw.println("\t //全局变量");
	            pw.println("}");
	            pw.println("");
	            if (gtlVersion >= 4)
	            	pw.println("void FUNCTION init() {");
	            else
	            	pw.println("FUNCTION init() {");	            	
	            pw.println("\t //初始化");
	            pw.println("}");
	            pw.println("");
	            if (gtlVersion >= 4)
	            	pw.println("void FUNCTION cycle() {}");
	            else
	            	pw.println("FUNCTION cycle() {}");
	            pw.println("");
	            if (gtlVersion >= 4)
	            	pw.println("void FUNCTION destroy() {}");
	            else
	            	pw.println("FUNCTION destroy() {}");
	            pw.println("");
	            if (gtlVersion >= 4)
	            	pw.println("void FUNCTION cycleUI() {}");
	            else
	            	pw.println("FUNCTION cycleUI() {}");	            		        	         
	            pw.println("");
	            if (gtlVersion >= 4)
	            	pw.println("void FUNCTION event() {");
	            else 
	            	pw.println("FUNCTION event() {");
	            pw.println("\t //处理event事件");
	            pw.println("}");
	            pw.println("");
	            if (gtlVersion >= 4)	            
	            	pw.println("void FUNCTION paint() {");
	            else 
	            	pw.println("FUNCTION paint() {");
	            pw.println("	//#if TouchScreen == true\t ");
	            pw.println("	RemoveAllButtons();\t ");
	            pw.println("	//#endif\t ");
	            pw.println("	Object g = Realize(GetSystemGraphics());\t ");
	            pw.println("	free g;\t ");
	            pw.println("}");
	            pw.println("");
	            if (gtlVersion >= 4)
	            	pw.println("void FUNCTION processPacket() {");	        	
	            else
	            	pw.println("FUNCTION processPacket() {");	        		            	
	            pw.println("	UWAPSegment segment = Realize(GetNextPacket());\t ");
	            pw.println("	int type = UWAP_GetType(segment);\t ");
	            pw.println("	int serial = UWAP_GetSerial(segment);\t ");
	            pw.println("	free segment;\t ");
	            pw.println("}");
	            pw.println("");
	            pw.close();
	        }catch(IOException e){
	            throw e;
	        }finally{
	            try{
	                if(fos != null){
	                    fos.close();
	                }
	            }catch(IOException e){
	            }
	        }
		}
    }
	
	
	/**
     * 拷贝源文件到目标文件。
     */
    public static void copyFile(File src, File dest) throws IOException{
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try{
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            byte[] data = new byte[256];
            int len;
            while((len = fis.read(data)) >= 0){
                if(len == 0){
                    continue;
                }
                fos.write(data, 0, len);
            }
        }catch(IOException e){
            throw e;
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch(IOException e){
                }
            }
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }

}
