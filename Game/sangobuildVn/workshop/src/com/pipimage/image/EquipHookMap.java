/**
 * 
 */
package com.pipimage.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pip.util.Utils;

/**
 * @author jhkang
 *
 */
public class EquipHookMap {
	
	protected String equipCtsName;
	
	private List<String> hookFileNames = new ArrayList<String>();

	/**
	 * use byte value
	 */
	private List<Integer> hookIds = new ArrayList<Integer>();

	/**
	 * list第N个个元素表是对第N个素体动画的装备组装信息;<br/>
	 * 组装信息是对该素体动画的每一帧,应该使用装备动画集合的第几个动画
	 */
	public List<byte[]>frame2equipAniIdxes = new ArrayList<byte[]>();

	public int getHookId(int index) {
		return hookIds.get(index);
	}

	public void addHookId(int selHookId) {
		this.hookIds.add(selHookId);
	}

	public String getEquipCtsName() {
		return equipCtsName;
	}
	public int getBodyCnt(){
		return hookFileNames.size();
	}
	
	public void setEquipCtsName(String equipCtsName) {
		this.equipCtsName = equipCtsName;
	}

	public String getHookFileName(int idx) {
		return hookFileNames.get(idx);
	}

	public void addHookFileName(String hookFileName) {
		this.hookFileNames.add(hookFileName);
	}
	
	/**
	 * @param filePath
	 * @param bodyPasList 挂载了装备的形象动画组
	 * @throws IOException
	 */
	public void save(String filePath, List<PipAnimateSet> bodyPasList, PipAnimateSet equipPas) throws IOException{
		saveEqp(filePath, bodyPasList, equipPas, true);		
		saveEqp(filePath, bodyPasList, equipPas, false);
		
	}
	
	public static void make4client(File f, String akName) throws Exception{
		if(f.exists()==false){
			throw new FileNotFoundException(f.toString());
		}
		byte[] bdata = Utils.loadFileData(f);
		bdata = new byte[10];
		ByteArrayInputStream bis = new ByteArrayInputStream(bdata);
		DataInputStream dis = new DataInputStream(bis);
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(f.getParent(), "equ.eqpc")));
		try{
			dis.readUTF();		// 跳过CTS文件名
			dos.write(dis.readByte());   // 素体文件数
			String oldName = dis.readUTF();		// 忽略hk文件
			dos.writeUTF(akName);   // 写出hk文件
			byte[] b = new byte[dis.available()];
			dis.readFully(b );
			dos.write(b);
		}catch(Exception e){
			throw e;
		}finally{
			dos.close();
			dis.close();
		}
	}
	
	public static void make4client(File f, HashMap<String, String> Hk2AkNames) throws Exception{
		if(f.exists()==false){
			throw new FileNotFoundException(f.toString());
		}
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(f.getParent(), "equ.eqpc")));
		try{
			dis.readUTF();		// 跳过CTS文件名
			byte count = dis.readByte();
			dos.write(count);   // 素体文件数
			for(int i = 0; i< count; i++)
			{
				String oldName = dis.readUTF();
				File hkFile = new File(f.getParent(),oldName);
				dos.writeUTF(Hk2AkNames.get(hkFile.getCanonicalPath()));//根据.hk找出对应的.ak
				int b = dis.readByte();
				dos.writeByte(b);
				short frameCount = dis.readShort();
				dos.writeShort(frameCount);
				for(int j = 0; j<frameCount; j++){
					int frame = dis.readByte();
					dos.writeByte(frame);
				}
			}
		}catch(Exception e){
			throw e;
		}finally{
			dos.close();
			dis.close();
		}
	}
	
	/**
	 * 修改一个eqp文件以及对应的eqpc文件中引用的hk文件名。
	 * @param eqpFile eqp文件全路径或者eqpc文件全路径
	 * @param hkOldName 旧的hk文件名（可能带有相对路径）
	 * @param hkNewName 新的hk文件名（可能带有相对路径）
	 * @return 如果eqp文件引用的这个hk文件，返回true。如果没有，返回false。
	 */
	public static boolean changeHkName(File eqpFile, String hkOldName, String hkNewName) throws IOException {
		byte[] orgData = Utils.loadFileData(eqpFile);
		boolean isEqpc = eqpFile.getName().toLowerCase().endsWith(".eqpc");
		boolean hit = false;
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(orgData));
		ByteArrayOutputStream bos = new ByteArrayOutputStream(orgData.length + 100);
		DataOutputStream dos = new DataOutputStream(bos);
		
		if (!isEqpc) {
			dos.writeUTF(dis.readUTF());
		}
		int cnt = dis.readByte();
		dos.writeByte(cnt);
		for (int i = 0; i < cnt; i++) {
			String hookFileName = dis.readUTF();
			if (hookFileName.equals(hkOldName)) {
				hit = true;
				hookFileName = hkNewName;
			}
			dos.writeUTF(hookFileName);
			dos.writeByte(dis.readByte());   // hookid
			int frameCnt = dis.readShort();
			dos.writeShort(frameCnt);
			byte[] hookData = new byte[frameCnt];
			dis.readFully(hookData);
			dos.write(hookData);
		}
		dos.flush();
		
		if (hit) {
			Utils.saveFileData(eqpFile, bos.toByteArray());
			System.out.println("file changed: " + eqpFile);
		}
		return hit;
	}
	
	/**
	 * 检查一个eqp文件是否引用了某一个hk文件。
	 * @param eqpFile eqp文件全路径
	 * @param hkPath hk文件相对eqp文件的路径
	 * @return 如果eqp文件引用的这个hk文件，返回true。如果没有，返回false。
	 */
	public static boolean checkHkRef(File eqpFile, String hkName) throws IOException {
		byte[] orgData = Utils.loadFileData(eqpFile);
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(orgData));
		
		dis.readUTF();
		int cnt = dis.readByte();
		for (int i = 0; i < cnt; i++) {
			String hookFileName = dis.readUTF();
			if (hookFileName.equals(hkName)) {
				return true;
			}
			dis.readByte();		// hookid
			int frameCnt = dis.readShort();
			dis.skipBytes(frameCnt);
		}
		return false;
	}
	
	/**
	 * 当eqp文件或eqpc文件更换位置后，修改其中引用的hk文件路径。
	 * @param targetPath 文件新位置
	 * @param sourcePath 文件旧位置
	 * @param fileMap 文件新旧路径关联表，如果旧的hk文件也挪动了位置，则使用新的位置
	 * @return 如果eqp文件被改变，返回true，否则返回false。
	 */
	public static boolean updateHkPath(String targetPath, String sourcePath, Map<String, String> fileMap) throws IOException {
		File eqpFile = new File(targetPath);
		byte[] orgData = Utils.loadFileData(eqpFile);
		boolean isEqpc = eqpFile.getName().toLowerCase().endsWith(".eqpc");
		boolean hit = false;
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(orgData));
		ByteArrayOutputStream bos = new ByteArrayOutputStream(orgData.length + 100);
		DataOutputStream dos = new DataOutputStream(bos);
		
		if (!isEqpc) {
			dos.writeUTF(dis.readUTF());
		}
		int cnt = dis.readByte();
		dos.writeByte(cnt);
		File sourceFile = new File(sourcePath);
		for (int i = 0; i < cnt; i++) {
			String hookFileName = dis.readUTF();
			
			// 处理hk文件转换
			String hkPath = new File(sourceFile.getParentFile(), hookFileName).getCanonicalPath();
			if (fileMap.containsKey(hkPath)) {
				hkPath = fileMap.get(hkPath);
			}
			if (new File(hkPath).exists()) {
				String newPath = Utils.getRelatePath(hkPath, targetPath);
				if (!newPath.equals(hookFileName)) {
					hit = true;
					hookFileName = newPath;
				}
			}
			dos.writeUTF(hookFileName);
			dos.writeByte(dis.readByte());   // hookid
			int frameCnt = dis.readShort();
			dos.writeShort(frameCnt);
			byte[] hookData = new byte[frameCnt];
			dis.readFully(hookData);
			dos.write(hookData);
		}
		dos.flush();
		
		if (hit) {
			Utils.saveFileData(eqpFile, bos.toByteArray());
			System.out.println("file changed: " + eqpFile);
		}
		return hit;
	}
	
	/**
	 * 修改此方法时请同时修改make4client方法
	 * @param filePath
	 * @param bodyPasList
	 * @param equipPas
	 * @param isEqp .eqp if true, otherwise .eqpc(for client)
	 * @throws IOException
	 */
	private void saveEqp(String filePath, List<PipAnimateSet> bodyPasList, PipAnimateSet equipPas, boolean isEqp) throws IOException{
		if(isEqp == false) {
			filePath += "c";
		}
		
		FileOutputStream fos = new FileOutputStream(filePath);
		DataOutputStream dos = new DataOutputStream(fos);
		if(isEqp) {
			dos.writeUTF(equipCtsName);
		}
//		else {
//			dos.writeUTF(equipCtsName.substring(0, equipCtsName.length() - 2) + "n");			
//		}
		dos.writeByte(hookFileNames.size());
		int idx=0;
		for(String fname:hookFileNames){
			dos.writeUTF(fname);
			int hookId = hookIds.get(idx);
			dos.writeByte(hookId);
			int cnt;
			PipAnimateSet pas = bodyPasList.get(idx);
			cnt = pas.getFrameCount();
			dos.writeShort(cnt);
			for(int i=0; i<cnt; i++){
				PipAnimateFrame frame = pas.getFrame(i);
				PipAni4AniFramePiece hook = frame.getHook(hookId);
				PipAnimate pa = hook.getBindAnimate();
				if(pa == null){
					dos.writeByte(-1);
				}else{
					dos.writeByte(equipPas.animates.indexOf(pa));
				}
			}
			idx++;
		}
		dos.close();
	}
	
	/*
	 * 修改此函数时请相应的修改FileRenamer里的相关内容
	 */
	public void load(String filePath) throws IOException{
		FileInputStream fis = new FileInputStream(filePath);
		DataInputStream dis = new DataInputStream(fis);
		try{
		equipCtsName = dis.readUTF();
			int cnt = dis.readByte();
			for (int i = 0; i < cnt; i++) {
				hookFileNames.add(dis.readUTF());
				hookIds.add(new Integer(dis.readByte()));
				int frameCnt = dis.readShort();
				byte[] frame2equipAniIdx = new byte[frameCnt];
				for (int j = 0; j < frameCnt; j++) {
					frame2equipAniIdx[j] = dis.readByte();
				}
				frame2equipAniIdxes.add(frame2equipAniIdx);
			}
		} finally {
			dis.close();
		}
	}
	
	/**
	 * 在<b>已经嵌入</b>了挂接点的body动画上绑定装备.
	 * @param bodyPas
	 * @param equipPas
	 */
	public void doEquip(PipAnimateSet bodyPas, PipAnimateSet equipPas, int bodyPasIdx){
		int cnt = bodyPas.getFrameCount();
		int hookId = hookIds.get(bodyPasIdx);
		byte[] frame2equipAniIdx = frame2equipAniIdxes.get(bodyPasIdx);
		for(int i=0; i<cnt; i++){
			PipAnimateFrame frame = bodyPas.getFrame(i);
			PipAni4AniFramePiece hook = frame.getHook(hookId);
			if(i>=frame2equipAniIdx.length){
				continue;
			}
			if(frame2equipAniIdx[i]>=0){
				if(equipPas != null){
					PipAnimate pa = equipPas.getAnimate(frame2equipAniIdx[i]);
					hook.bindAnimate(pa);
				}else{
					hook.bindAnimate(null);
				}
			}else{
				hook.bindAnimate(null);
			}
		}
	}
	
	/**
	 * 在编辑时替换用到的装备动画对象。
	 */
	public void changeEquipAni(List<PipAnimateSet> bodyPasList, PipAnimateSet oldSet, PipAnimateSet newSet) {
		for (int i = 0; i < hookFileNames.size(); i++) {
			int hookId = hookIds.get(i);
			int cnt;
			PipAnimateSet pas = bodyPasList.get(i);
			cnt = pas.getFrameCount();
			for(int j = 0; j < cnt; j++){
				PipAnimateFrame frame = pas.getFrame(j);
				PipAni4AniFramePiece hook = frame.getHook(hookId);
				PipAnimate pa = hook.getBindAnimate();
				if (pa != null) {
					int idx = oldSet.animates.indexOf(pa);
					if (idx >= 0 && idx < newSet.animates.size()) {
						hook.bindAnimate(newSet.animates.get(idx));
					}
				}
			}
		}
	}

	public void remove(int idx) {
		hookIds.remove(idx);
		hookFileNames.remove(idx);
		frame2equipAniIdxes.remove(idx);
	}

	public boolean hasBody(String bodyHkName) {
		return hookFileNamesIndexOf(bodyHkName)>=0;
	}

	public void doEquip(PipAnimateSet bodyAniSet, PipAnimateSet equipPas, String bodyHkName) {
		doEquip(bodyAniSet, equipPas, hookFileNamesIndexOf(bodyHkName));
	}

	public int getBodyIndex(String bodyHkName) {
		return hookFileNamesIndexOf(bodyHkName);
	}
	
	private int hookFileNamesIndexOf(String bodyHkName) {
		int ret = -1;
		int i = 0;
		for(String name : hookFileNames) {
			if(name.endsWith(bodyHkName)) {
				ret = i;
				break;
			}
			i++;
		}
		
		return ret;
	}
}
