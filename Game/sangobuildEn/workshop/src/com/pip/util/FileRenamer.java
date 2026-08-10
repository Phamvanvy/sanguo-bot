package com.pip.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

import com.pip.image.workshop.Settings;
import com.pipimage.image.PipAnimateSet;

/**
 * 文件改名工具;会扫描同文件夹的其他文件,并做引用更改<br/>
 * 比如pip改名,会同时修改cts引用
 * @author jhkang
 *
 */
public class FileRenamer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "E:\\workspace\\ImageWorkshop1.0\\equipEditorDev\\";
		
//		boolean ret = rename(path+"weapon0.pip", path+"weapon.pip");
//		boolean ret = rename(path+"weapon.pip", path+"weapon0.pip");
		
//		boolean ret = rename(path+"male4.cts", path+"male3.cts");
		
//		boolean ret = rename(path+"equip_11.cts", path+"equip_1.cts");
//		rename(path+"male3.cts", path+"male4.cts");
		
		boolean ret = rename(path+"素体2.hk", path+"素体1.hk");
		
		if(ret == false){
			log("failed");
		}
	}
	
	private static String fromName;
	private static String toName;
	private static FileFilter affectedFileFilter;
	private static FileExtensionFilter ctsFilter = new FileExtensionFilter(new String[]{"cts"}, false);
	private static FileExtensionFilter eqpFilter = new FileExtensionFilter(new String[]{"eqp"}, false);
	private static FileExtensionFilter hkAndEqpFilter = new FileExtensionFilter(new String[]{"hk", "eqp"}, false);
	private static FixFileRef fixer;
	private static FixFileRef fixPipInCts = new FixPipInCts();
	private static FixFileRef fixCtsInEqpOrHk = new FixCtsInEqpOrHk();
	private static FixFileRef fixHkInEqp = new FixHkInEqp();
	private static BufferedWriter pw;
	
	public static boolean rename(String fromFullPath, String toFullPath){
		boolean ret = false;
		try{
			ret = renameSafe(fromFullPath, toFullPath);
		}catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter sp = new PrintWriter(sw);
			e.printStackTrace(sp);
			sp.close();
			log(sw.toString());
		}
		return ret;
	}
	private static boolean renameSafe(String fromFullPath, String toFullPath){
		File fromF = new File(fromFullPath);
		File toF = new File(toFullPath);
		
		//check rename
		if(fromF.renameTo(toF)==false){
			log("failed when check");
			return false;
		}
		toF.renameTo(fromF);
		//check end
		fromName = fromF.getName();
		toName = toF.getName();
		
		if(fromName.toLowerCase().endsWith(".pip")){
			fixer = fixPipInCts;
			affectedFileFilter = ctsFilter;
		}else if(fromName.toLowerCase().endsWith(".cts")){
			fixer = fixCtsInEqpOrHk;
			affectedFileFilter = hkAndEqpFilter;
		}else if(fromName.toLowerCase().endsWith(".hk")){
			fixer = fixHkInEqp;
			affectedFileFilter = eqpFilter;
		}else if(fromName.toLowerCase().endsWith(".eqp")){
			fixer = null;
			affectedFileFilter = null;	
		}else{
			log("=================================================");
			log(new Date());
			log("不支持的文件类型");
			return false;
		}
		
		log("=================================================");
		log(new Date()+" 重命名文件");
		logRename(fromFullPath, toFullPath);
		
		if(affectedFileFilter != null){
			walkDirectory(fromF.getParent());
		}
		log("引用文件更新完毕,开始重命名目标文件...");
		boolean ret = fromF.renameTo(toF);
		if(ret == false){
			log("目标文件重命名失败.");
		}else{
			log("重命名成功");
		}
		return ret;
	}

	private static void logRename(String fromFullPath, String toFullPath) {
		log("FROM:"+fromFullPath);
		log("TOOO:"+toFullPath);
		log("-------------------------------------------------");		
	}

	private static void logAffect(String fullPath){
		log("    Affect:"+fullPath);
	}
	static interface FixFileRef{
		public void fix(File ctsFile, String fromPip, String toPip) throws Exception;
	}
	static class FixHkInEqp implements FixFileRef{
		public void fix(File ctsFile, String fromPip, String toPip) throws Exception{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			
			boolean hit = false;
			
			FileInputStream fis = new FileInputStream(ctsFile);
			DataInputStream dis = new DataInputStream(fis);
			String equipCtsName = dis.readUTF();//
			dos.writeUTF(equipCtsName);
			int cnt = dis.readByte();
			dos.writeByte(cnt);
			for(int i=0; i<cnt; i++){
				String hookFileName = dis.readUTF();
				if(hookFileName.equals(fromPip)){
					dos.writeUTF(toPip);
					byte[] def = new byte[dis.available()];
					dis.readFully(def);
					dos.write(def);
					hit = true;
					break;
				}else{
					dos.writeUTF(hookFileName);
				}
				dos.writeByte(dis.readByte());//hookid
				int frameCnt = dis.readShort();
				dos.writeShort(frameCnt);
				byte[] d = new byte[frameCnt];
				dis.readFully(d);
				dos.write(d);
			}
			dis.close();
			dos.close();
			if(hit){
				byte[] data = bos.toByteArray();
				Utils.saveFileData(ctsFile, data);
				logAffect(ctsFile.getAbsolutePath());
			}
		}
	}
	static class FixCtsInEqpOrHk implements FixFileRef{
		public void fix(File ctsFile, String fromPip, String toPip) throws Exception{
			fixCtsInEqpOrHk(ctsFile, fromPip, toPip);
		}
	}
	public static void fixCtsInEqpOrHk(File ctsFile, String fromPip, String toPip) throws Exception{
		byte[] content = Utils.loadFileData(ctsFile);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(content));
		String ctsName = dis.readUTF();
		if(ctsName.equals(fromPip)){
			byte[] def = new byte[dis.available()];
			dis.readFully(def);
			dis.close();
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(byteArrayOut);
			dos.writeUTF(toPip);
			dos.write(def);
			dos.close();
			Utils.saveFileData(ctsFile, byteArrayOut.toByteArray());
			logAffect(ctsFile.getAbsolutePath());
		}else{
			dis.close();
		}
	}
	static class FixPipInCts implements FixFileRef{
		public void fix(File ctsFile, String fromPip, String toPip) throws Exception{
			PipAnimateSet pas = new PipAnimateSet();
			pas.load(ctsFile);
			boolean hit =false;
			int cnt = pas.getFileCount();
			for(int i=0; i<cnt; i++){
				String name = pas.getFileName(i);
				if(name.equals(fromPip)){
					pas.setFileName(i, toPip);
					hit = true;
				}
			}
			if(hit){
				pas.save(pas.getOriginalFile(), true);
				logAffect(ctsFile.getAbsolutePath());
			}
		}
	}
	
	private static void walkDirectory(String path){
		File f = new File(path);
		if(f.isFile()){
//			updateRef(f);
			try {
				fixer.fix(f, fromName, toName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(f.isDirectory()){
			File fileNames[] = f.listFiles( affectedFileFilter );
			for(int i=0; i<fileNames.length; i++){
				walkDirectory(fileNames[i].getAbsolutePath());
			}
		}
	}

	private static void log(Object info){
		System.out.println(info);
		try {
			pw.write(info.toString());
			pw.newLine();
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static{
		File logDir = new File(Settings.logDir);
		if(logDir.exists()==false){
			logDir.mkdir();
		}
		Calendar date = Calendar.getInstance();
		String renameLog = String.format("文件改名%d-%02d-%02d.txt", date.get(Calendar.YEAR), date.get(Calendar.MONTH)+1, date.get(Calendar.DATE));
		File logFile = new File(logDir, renameLog);
		try {
			pw = new BufferedWriter(new FileWriter(logFile, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
