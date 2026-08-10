package test.equip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pip.image.workshop.editor.BodyDef;
import com.pipimage.image.EquipHookMap;
import com.pipimage.image.PipAni4AniFramePiece;
import com.pipimage.image.PipAnimateSet;

/**
 * 批量生成客户端需要的资源：ctn, hkc, eqpc
 * @author ybai
 *
 */
public class GenerateClienEquiptRes {	
	private List<BodyDef> hkList = new ArrayList<BodyDef>();
	private List<EquipHookMap> eqpList = new ArrayList<EquipHookMap>();
	private List<PipAnimateSet> ctsList = new ArrayList<PipAnimateSet>();

	private List<File> hkFiles = new ArrayList<File>();
	private List<File> eqpFiles = new ArrayList<File>();
	private List<File> ctsFiles = new ArrayList<File>();
	
	private List<PipAnimateSet> hkAniSetList = new ArrayList<PipAnimateSet>();
	private List<PipAnimateSet> eqpAniSetList = new ArrayList<PipAnimateSet>();
	private ArrayList<ArrayList<PipAnimateSet>> bodyPasListList = new ArrayList<ArrayList<PipAnimateSet>>();
	
	public static void main(String[] args) {
		GenerateClienEquiptRes ge = new GenerateClienEquiptRes();
		String path = null;
		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\怪\\男";
//		ge.doReSave(path);
//		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\怪\\女";
//		ge.doReSave(path);
//		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\妖\\男";
//		ge.doReSave(path);
		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\妖\\女";
//		ge.doReSave(path);
//		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\人\\男";
//		ge.doReSave(path);
//		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\人\\女";
//		ge.doReSave(path);
//		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\仙\\男";
//		ge.doReSave(path);
//		
//		path = "E:\\xyproject\\workspace\\Xiyou1.0-Data\\data\\avatar\\仙\\女";
//		ge.doReSave(path);
		
		
		path = "C:\\男";
		ge.deleteFrame(path, 9);
	}
	
	//删除素体动画中的某一帧
	private void deleteFrame(String path, int frame) {
		init();
		loadFiles(new File(path));
		
		BodyDef bd = hkList.get(0);
		PipAni4AniFramePiece[][] oldEmb = bd.embedHookPieces;
		PipAnimateSet pas = hkAniSetList.get(0);
		bd.embedHookPieces(pas);
		
		pas.removeFrame(frame);	
					
		PipAni4AniFramePiece[][] emb = new PipAni4AniFramePiece[oldEmb.length - 1][];
		
		int kk = 0;
		for(int j=0; j<oldEmb.length; j++) {
			if(j != frame) {
				//不包含，说明是要保留的
				emb[kk] = oldEmb[j];
				kk ++;
			}
		}
		bd.embedHookPieces = emb;
		try {
			bd.save(pas, hkFiles.get(0).getAbsolutePath());
			pas.save(ctsFiles.get(0), true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doReSave(String path) {
		init();
		loadFiles(new File(path));		
		reSaveAll();	
	}

	
	private void init() {
		hkList.clear();
		eqpList.clear();
		ctsList.clear();
		
		hkFiles.clear();
		eqpFiles.clear();
		ctsFiles.clear();

		hkAniSetList.clear();
		eqpAniSetList.clear();
		bodyPasListList.clear();
	}
	
	//重新保存为所有客户端需要的资源
	private void reSaveAll() {
		for(int i=0; i<hkList.size(); i++) {
			try {
				hkList.get(i).save(hkAniSetList.get(i), hkFiles.get(i).getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for(int i=0; i<eqpList.size(); i++) {
			try {
				eqpList.get(i).save(eqpFiles.get(i).getAbsolutePath(), bodyPasListList.get(i), eqpAniSetList.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		
		for(int i=0; i<ctsList.size(); i++) {
			try {
//				ctsList.get(i).save(ctsFiles.get(i), true);
				String path = ctsFiles.get(i).getAbsolutePath();
				path = path.substring(0, path.length() - 1) + "n";
				ctsList.get(i).save(new File(path), false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//load所有需要的资源
	private void loadFiles(File dir) {	
		File[] files = dir.listFiles();
		
		if(files!= null && files.length > 0) {
			for(File file : files) {
				if(file.isDirectory()) {
					loadFiles(file);
				} else {
					if(file.getName().endsWith("hk")) {
						BodyDef bodyDef = new BodyDef();
						try {
							bodyDef.loadHooks(file);
							hkList.add(bodyDef);
							hkFiles.add(file);
							
							File ctsFile = new File(file.getParent(), bodyDef.ctsFile);
							PipAnimateSet pas = new PipAnimateSet();
							pas.load(ctsFile);
							PipAnimateSet animateSet = pas;
//							bodyDef.embedHookPieces(animateSet);
							hkAniSetList.add(animateSet);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} else if(file.getName().endsWith("eqp")) {
//						EquipHookMap equipHookMap = new EquipHookMap();
//						try {
//							equipHookMap.load(file.getAbsolutePath());
//							eqpList.add(equipHookMap);
//							eqpFiles.add(file);
//							
//							PipAnimateSet pas = new PipAnimateSet();
//							pas.load(new File(file.getParent(), "equ.cts"));
//							PipAnimateSet equipAniSet = pas;
//							
//							int bodyCnt = equipHookMap.getBodyCnt();
//							ArrayList<PipAnimateSet> bodyAniSets = new ArrayList<PipAnimateSet>();
//							for(int i=0; i<bodyCnt; i++){
//								String hookFileName = equipHookMap.getHookFileName(i);
//								BodyDef bodyDef = new BodyDef();
//								pas = loadBody(file.getParentFile().getAbsoluteFile() + File.separator, hookFileName, bodyDef);
//								bodyAniSets.add(pas);
//								int hookId = equipHookMap.getHookId(i);
//								bodyDef.embedHookPieces(pas, hookId );
//								equipHookMap.doEquip(pas, equipAniSet, i);
//							}
//							
//							bodyPasListList.add(bodyAniSets);
//							eqpAniSetList.add(equipAniSet);
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					} else if(file.getName().endsWith("cts")) {
						PipAnimateSet pas = new PipAnimateSet();
						try {
							pas.load(file);
							ctsList.add(pas);
							ctsFiles.add(file);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			}
		}		
	
	}
	
	
	/**
	 * 加载素体(.hk)文件,返回此素体关联的形象动画组
	 * @param dir
	 * @param hookFilePath
	 * @param bodyDef
	 * @return
	 * @throws IOException
	 */
	private PipAnimateSet loadBody(String dir, String hookFilePath, BodyDef bodyDef) throws IOException {
		if(!dir.endsWith(File.separator)){
			dir += File.separator;
		}
		bodyDef.loadHooks(dir+hookFilePath);
		String bodyCtsName;
		bodyCtsName = bodyDef.ctsFile;
		PipAnimateSet pas = new PipAnimateSet();
		pas.load(new File(new File(dir + hookFilePath).getParentFile(), bodyCtsName));
		return pas;
	}
}
