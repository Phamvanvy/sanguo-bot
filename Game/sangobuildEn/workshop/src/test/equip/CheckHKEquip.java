package test.equip;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

import com.pip.image.workshop.editor.BodyDef;
import com.pip.util.Point;
import com.pip.util.Utils;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrame;
import com.pipimage.image.PipAnimateFramePiece;
import com.pipimage.image.PipAnimateSet;

/**
 * 删除不需要的cts中的动画，以及关联的素体，装备
 * 
 * @author ybai
 *
 */
public class CheckHKEquip {
	
	public static void main(String[] args) {
		CheckHKEquip equip = new CheckHKEquip();
		
		String dir = "E:\\f\\调整后种族\\怪\\男";		
		String ctsFile = "atk.cts";			
//		equip.checkCtsRef(dir, ctsFile);
				
//		equip.init();
//		dir = "E:\\f\\调整后种族\\妖\\男";
//		equip.checkCtsRef(dir, ctsFile);
//		
//		equip.init();
//		dir = "E:\\f\\调整后种族\\妖\\女";
//		equip.checkCtsRef(dir, ctsFile);
//		
//		equip.init();
//		dir = "E:\\f\\调整后种族\\怪\\男";
//		equip.checkCtsRef(dir, ctsFile);
//		
		equip.init();
		dir = "E:\\f\\调整后种族\\怪\\女";
		equip.checkCtsRef(dir, ctsFile);
		
//		equip.init();
//		dir = "E:\\f\\调整后种族\\仙\\男";
//		equip.checkCtsRef(dir, ctsFile);
//		
//		equip.init();
//		dir = "E:\\f\\调整后种族\\仙\\女";
//		equip.checkCtsRef(dir, ctsFile);
//		
//		equip.init();
//		dir = "E:\\f\\调整后种族\\人\\男";
//		equip.checkCtsRef(dir, ctsFile);
//		
//		equip.init();
//		dir = "E:\\f\\调整后种族\\人\\女";
//		equip.checkCtsRef(dir, ctsFile);
		
	}
	
	private List hkOrEqps = new ArrayList();
	private List<File> heFiles = new ArrayList<File>();
	private PipAnimateSet aniSet;
	
	private void init() {
		hkOrEqps.clear();
		heFiles.clear();
		aniSet = null;
	}
	
	public void checkCtsRef(String dir, String ctsFile) {
		hkOrEqps.clear();
		heFiles.clear();
		
		//素体动画
		aniSet = new PipAnimateSet();
		try {
			aniSet.load(new File(dir + "//" + ctsFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File pathDir = new File(dir);		
		File[] files = pathDir.listFiles();
		checkCtsRef2(files, ctsFile);
		
		List<PipAnimateSet> aniSetList = new ArrayList<PipAnimateSet>();
		aniSetList.add(aniSet);
		
		int i = 0;
		for(Object obj : hkOrEqps) {
			if(obj instanceof BodyDef) {
				//必须只有一个素体
				BodyDef bodyDef = (BodyDef)obj;
				
				PipAni4AniFramePiece[][] oldEmb = bodyDef.embedHookPieces;
				   			
				bodyDef.embedHookPieces(aniSet);				
				
				cleanCts(aniSet, dir + "//" + ctsFile, true);
				
				PipAni4AniFramePiece[][] emb = new PipAni4AniFramePiece[oldEmb.length - hasRmoveFrames.size()][];
				
				int kk = 0;
				for(int j=0; j<oldEmb.length; j++) {
					if(hasRmoveFrames.contains(j) == false) {
						//不包含，说明是要保留的
						emb[kk] = oldEmb[j];
						kk ++;
					}
				}
				bodyDef.embedHookPieces = emb;
				try {
					bodyDef.save(aniSet, heFiles.get(i).getAbsolutePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("素体：" + heFiles.get(i).getAbsolutePath());
				
			} else if(obj instanceof EquipHookMap) {
				EquipHookMap ehm = (EquipHookMap)obj;
				
				PipAnimateSet equipPas = new PipAnimateSet();
				try {
//					adjustEqp(heFiles.get(i).getParent() + "\\equ.cts", true);
					equipPas.load(new File(heFiles.get(i).getParent() + "\\equ.cts"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				cleanCts(equipPas, heFiles.get(i).getParent() + "\\equ.cts", false);
								
				List<byte[]>frame2equipAniIdxes = new ArrayList<byte[]>();
				int kk = 0;			
				try {
					for(byte[] indxes : ehm.frame2equipAniIdxes) {
						kk = 0;
						byte[] newBytes = new byte[indxes.length - hasRmoveFrames.size()];
						
						for(int j=0; j<indxes.length; j++) {
							if(hasRmoveFrames.contains(j) == false) {
								//不包含，说明是要保留的
								newBytes[kk] = (byte)((indxes[j] - 1) / 2); //指定的帧序变了，所以这个做特殊计算
								kk ++;
							}
						}		
						
						frame2equipAniIdxes.add(newBytes);
					}
					ehm.frame2equipAniIdxes = frame2equipAniIdxes;
					
					//绑定到素体				
					System.out.println("装备：" + heFiles.get(i).getAbsolutePath());
					ehm.doEquip(aniSet, equipPas, 0);		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("错误：" + heFiles.get(i).getParent() + "\\equ.cts");
					e.printStackTrace();
				}
				
				try{
					ehm.save(heFiles.get(i).getAbsolutePath(), aniSetList, equipPas);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			i ++;
		}
	}
	
	public void adjustEqp(String equipFilePath, boolean silent){
		File f = new File(equipFilePath);		
		try {
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			String prefix = "../../../";
			if(f.getParentFile().getName().matches("\\d+")){
				prefix = "../../";
			}
			try {
				String equipCtsName = dis.readUTF();
				dos.writeUTF("equ.cts");
				int cnt = dis.readByte();
				dos.writeByte(cnt);
				for (int i = 0; i < cnt; i++) {
					String hkName = dis.readUTF();
					if(new File(prefix + "atk.hk").exists() == false) {
						prefix += "../";
					}
					dos.writeUTF(prefix + "atk.hk");
					int hookId = dis.readByte();
					dos.writeByte(hookId);
					int frameCnt = dis.readShort();
					dos.writeShort(frameCnt);
					for (int j = 0; j < frameCnt; j++) {
						int mid = dis.readByte();
						dos.writeByte(mid);
					}
				}
			} finally {
				dis.close();
				dos.flush();
			}
			Utils.saveFileData(f, bos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	ArrayList hasRmoveFrames = new ArrayList();
	//删除cts中的向右的动画
	private void cleanCts(PipAnimateSet pipAniSet, String ctsFile, boolean clear) {
		if(clear) {
			hasRmoveFrames.clear();
		}
		
		//删除素体动画中的动画和帧
		int count = pipAniSet.getAnimateCount();
		
		for(int i = count - 1; i>=0; i--) {
			if(i == 12) {
				int ii = 0;
			}
			if(i % 2 == 0) {
				PipAnimate ani = pipAniSet.getAnimate(i);
				
				//记录动画引用的帧				
				if(clear) {
					for (int m = 0; m < ani.getFrameCount(); m++) {
						if(hasRmoveFrames.contains(ani.getFrame(m).getFrame()) == false) {
							hasRmoveFrames.add(ani.getFrame(m).getFrame());			    		
						}
					}					
				}
			    
			}
			
		}
		
		for(int i = count - 1; i>=0; i--) {
			if(i % 2 == 0) {
				PipAnimate ani = pipAniSet.getAnimate(i);
				
				//删除动画引用的帧							    
			    for (int m = 0; m < ani.getFrameCount(); m++) {
//			    	hasRmoveFrames.add(ani.getFrame(m).getFrame());	
			    	pipAniSet.removeFrame(ani.getFrame(m).getFrame());
		    		m--;
			    }
			    
			    pipAniSet.removeAnimate(i);
			    
			}
			
		}
		
		try {
			pipAniSet.save(new File(ctsFile), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * 检查cts在hk和eqp文件中的引用
	 * @param files
	 * @param ctsFile
	 */
	private void checkCtsRef2(File[] files, String ctsFile) {
		if(files!= null && files.length > 0) {
			for(File file : files) {
				if(file.isDirectory()) {
					checkCtsRef2(file.listFiles(), ctsFile);
				} else {
					if(file.getName().endsWith("hk")) {
						BodyDef bodyDef = new BodyDef();
						try {
							bodyDef.loadHooks(file);							
							hkOrEqps.add(bodyDef);
							heFiles.add(file);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} else if(file.getName().endsWith("eqp")) {
						EquipHookMap equipHookMap = new EquipHookMap();
						try {
							equipHookMap.load(file.getAbsolutePath());
							hkOrEqps.add(equipHookMap);
							heFiles.add(file);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			}
		}
		
	}
}
