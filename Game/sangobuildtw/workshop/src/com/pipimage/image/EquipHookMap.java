/**
 * 
 */
package com.pipimage.image;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	public static void make4client(File f) throws Exception{
		if(f.exists()==false){
			throw new FileNotFoundException(f.toString());
		}
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(f.getParent(), "equ.eqpc")));
		try{
			dis.readUTF();
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
