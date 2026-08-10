/**
 * 
 */
package com.pip.image.workshop.editor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;

/**
 * @author jhkang
 * 
 */
public class BodyDef {

	public PipAni4AniFramePiece[][] embedHookPieces;
	public ArrayList<PipAni4AniFramePiece> hooks;
	public Set<Integer> curHooksIdSet;
	/**
	 * 形象动画文件名称
	 */
	public String ctsFile;

	private HashMap<Integer, String> id2name = new HashMap<Integer, String>();

	public BodyDef() {
		hooks = new ArrayList<PipAni4AniFramePiece>();
		curHooksIdSet = new HashSet<Integer>();
	}

	public void loadHooks(File f) throws IOException {
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		loadHooks(dis);
		dis.close();
	}
	public void loadHooks(String filepath) throws IOException {
		hooks.clear();
		loadHooks(new File(filepath));
	}

	public void loadHooks(DataInputStream dis) throws IOException {
		ctsFile = dis.readUTF();
		byte hookCnt = dis.readByte();
		for (int i = 0; i < hookCnt; i++) {
			byte id = dis.readByte();
			String name = dis.readUTF();
			PipAni4AniFramePiece aniPiece = new PipAni4AniFramePiece(null);
			aniPiece.name = name;
			aniPiece.setImageID(id);
			hooks.add(aniPiece);
			curHooksIdSet.add(aniPiece.getImageID());
			id2name.put(new Integer(id), name);
		}
		short frameCnt = dis.readShort();
		embedHookPieces = new PipAni4AniFramePiece[frameCnt][];
		for (int i = 0; i < frameCnt; i++) {
			embedHookPieces[i] = new PipAni4AniFramePiece[hookCnt];
			for (int j = 0; j < hookCnt; j++) {
				PipAni4AniFramePiece hookPiece = new PipAni4AniFramePiece(null);
				hookPiece.setImageID(dis.readByte());
				hookPiece.setRealDx(dis.readByte());
				hookPiece.setRealDy(dis.readByte());
				hookPiece.setFrame(dis.readByte());// 这里将此图块在所有图块中的位置记录在frame值中
				hookPiece.name = id2name.get(new Integer(hookPiece.getImageID()));
				embedHookPieces[i][j] = hookPiece;
			}
		}
	}

	public String[] getHookNames() {
		String[] ret = new String[hooks.size()];
		int i = 0;
		for (PipAni4AniFramePiece p : hooks) {
			ret[i] = p.name;
			i++;
		}
		return ret;
	}

	public void embedHookPieces(PipAnimateSet animateSet) {
		embedHookPieces(animateSet, -1);
	}

	/**
	 * 嵌入完毕后数据会置null,所以只能调用一次
	 * @param bodyAniSet
	 * @param visibleHookId
	 * 			if -1 use default visible(true) else visible = imageId == value<br/>
	 * 			if -2 all invisible
	 */
	public void embedHookPieces(PipAnimateSet bodyAniSet, int visibleHookId) {
		int i = 0;
		checkNewAddedFrame(bodyAniSet);
		for (PipAni4AniFramePiece[] hooksInFrame : embedHookPieces) {
			if(i>=bodyAniSet.getFrameCount()){
				break;
			}
			PipAnimateFrame frame = bodyAniSet.getFrame(i);
			for (PipAni4AniFramePiece hook : hooksInFrame) {
				if(visibleHookId == -2){
					hook.setVisible(false);
				}else if (visibleHookId >= 0) {
					hook.setVisible(hook.getImageID() == visibleHookId);
				}
				hook.setParent(frame);
				frame.addPieceAt(hook.getFrame(), hook);
				hook.setFrame(0xFF);
			}
			hooksInFrame = null;
			i++;
		}
		embedHookPieces = null;
	}

	/**
	 * 可能动画集合有修改,新增了帧,那么需要为这些帧填上挂接点
	 * @param bodyAniSet
	 */
	private void checkNewAddedFrame(PipAnimateSet bodyAniSet) {
		int frameCnt = bodyAniSet.getFrameCount();
		int embedFrameCnt = embedHookPieces.length;
		if(embedFrameCnt == 0 || frameCnt == embedFrameCnt){
			return;
		}
		if(frameCnt<embedFrameCnt){
			throw new IllegalArgumentException("动画集合中的帧可能被删除过.");
		}
		int hookCnt = embedHookPieces[0].length;
		PipAni4AniFramePiece[][] existPieces = embedHookPieces;
		embedHookPieces = new PipAni4AniFramePiece[frameCnt][];
		System.arraycopy(existPieces, 0, embedHookPieces, 0, embedFrameCnt);
		Integer hookIds[] = new Integer[curHooksIdSet.size()];
		curHooksIdSet.toArray(hookIds);
		for (int i = embedFrameCnt; i < frameCnt; i++) {
			embedHookPieces[i] = new PipAni4AniFramePiece[hookCnt];
			for (int j = 0; j < hookCnt; j++) {
				PipAni4AniFramePiece hookPiece = new PipAni4AniFramePiece(null);
				hookPiece.setImageID(hookIds[j]);
				hookPiece.setRealDx((hookPiece.getWidth()+2)*(1+hookIds[j]));
				hookPiece.setRealDy(5);
				hookPiece.setFrame(0);// 这里将此图块在所有图块中的位置记录在frame值中
				hookPiece.name = id2name.get(new Integer(hookPiece.getImageID()));
				embedHookPieces[i][j] = hookPiece;
			}
		}
	}

	public void save(PipAnimateSet bodyAni, String file) throws Exception{
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
		save(bodyAni, dos, true);
		dos.close();
		
		dos = new DataOutputStream(new FileOutputStream(file + "c"));
		save(bodyAni, dos, false);
		dos.close();
	}
	public void save(PipAnimateSet bodyAni, DataOutputStream dos, boolean isHk) throws Exception{
		if(isHk) {
			dos.writeUTF(ctsFile);
		}
//		else {
//			dos.writeUTF(ctsFile.substring(0, ctsFile.length() - 2) + "n");			
//		}
		
		dos.writeByte(hooks.size());
		if(isHk) {
			for(PipAni4AniFramePiece aniPiece:hooks){
				dos.writeByte(aniPiece.getImageID());
				dos.writeUTF(aniPiece.name);				
			}
		}
		if(bodyAni == null){
			dos.writeShort(0);
			dos.close();
			return;
		}
		
		dos.writeShort(bodyAni.getFrameCount());
		
		for(int i=0; i<bodyAni.getFrameCount(); i++){
			PipAnimateFrame frame = bodyAni.getFrame(i);
			int findHookCnt = 0;
			for(int j=0; j<frame.getPieceCount(); j++){
				PipAnimateFramePiece piece = frame.getPiece(j);
				if(piece instanceof PipAni4AniFramePiece){
					PipAni4AniFramePiece hook = (PipAni4AniFramePiece) piece;
					dos.writeByte(piece.getImageID());
					dos.writeByte(hook.getRealDx());
					dos.writeByte(hook.getRealDy());
					dos.writeByte(j);
					findHookCnt++;
				}
			}
			if(findHookCnt != hooks.size()){
				throw new Exception("hook count miss match:"+frame.getName());
			}
		}		
	}
	
	public void saveState(PipAnimateSet bodyAni, DataOutputStream dos) throws Exception{
		save(bodyAni, dos, true);
		bodyAni.saveState(dos);
	}
	
	public void restoreState(PipAnimateSet bodyAni, DataInputStream dis) throws IOException{
		hooks.clear();
		loadHooks(dis);
		bodyAni.restoreState(dis);
		embedHookPieces(bodyAni);
	}
}
