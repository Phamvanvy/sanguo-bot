package com.pipimage.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.pip.util.Point;
import com.pip.util.Utils;
import com.pipimage.utils.ImageUtil;

/**
 * 动画特效。动画特效依托于一个动画文件提供粒子效果，利用轨迹、生存周期等参数对组合粒子形成特效动画。
 */
public class PipParticleEffectSet {
    // version number
    protected byte version;
	// The effect file path
	protected File originalFile;
	// The animate file path
	protected File animateFile;
	// The loaded animation
	protected PipAnimateSet sourceAnimate;
	// Effect list
	protected ArrayList<PipParticleEffect> effects;
	
	public PipParticleEffectSet() {
		version = 0;
		effects = new ArrayList<PipParticleEffect>();
	}
	
	public byte getVersion() {
	    return version;
	}
	
	public File getOriginalFile() {
		return originalFile;
	}

	public void setOriginalFile(File originalFile) {
		this.originalFile = originalFile;
	}

	public File getAnimateFile() {
		return animateFile;
	}
	
	public void setAnimateFile(File f) throws Exception {
		animateFile = f;
		sourceAnimate = new PipAnimateSet();
		sourceAnimate.load(f);
	}
	
	public void setAnimateFileName(File f) {
		animateFile = f;
	}
	
	public PipAnimateSet getSourceAnimate() {
		return sourceAnimate;
	}
	
	public int getEffectCount() {
		return effects.size();
	}
	
	public PipParticleEffect getEffect(int index) {
		return effects.get(index);
	}
	
	public void removeEffect(int index) {
		effects.remove(index);
	}
	
	public int getEffectIndex(PipParticleEffect pa){
		return effects.indexOf(pa);
	}
	
	public void addEffect(PipParticleEffect pa) {
		effects.add(pa);
	}
	
	public void load(File file) throws Exception {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		originalFile = file;
		if (file.length() == 0) {
			return;
		}
		Document doc = com.pip.util.Utils.loadDOM(file);
		load(doc);
	}
	
	public void load(Document doc) throws Exception {
		Element root = doc.getRootElement();
		byte version = (byte)Integer.parseInt(root.getChild("version").getTextTrim());
		String ctsName = root.getChild("animatefile").getTextTrim();
		setAnimateFile(new File(originalFile.getParentFile(), ctsName));
		List list = root.getChildren("effect");
		effects.clear();
		for (Object obj : list) {
			effects.add(loadEffect((Element)obj));
		}
	}
	
	private PipParticleEffect loadEffect(Element elem) throws Exception {
		PipParticleEffect ret = new PipParticleEffect();
		ret.title = elem.getChild("title").getTextTrim();
		try {
			ret.startTick = Integer.parseInt(elem.getChildText("starttick"));
			ret.stopTick = Integer.parseInt(elem.getChildText("stoptick"));
		} catch (Exception e) {
		}
		List list = elem.getChildren("particleset");
		for (Object obj : list) {
			ret.particleSets.add(loadParticleSet((Element)obj));
		}
		return ret;
	}
	
	private PipParticleSet loadParticleSet(Element elem) throws Exception {
		PipParticleSet ret = new PipParticleSet();
		ret.title = elem.getChildText("title");
		ret.startTime = Integer.parseInt(elem.getChildText("starttime"));
		ret.generateCount = Integer.parseInt(elem.getChildText("generatecount"));
		try {
			ret.generateCountRange = Integer.parseInt(elem.getChildText("generatecountrange"));
		} catch (Exception e) {
		}
		ret.generateInterval = Integer.parseInt(elem.getChildText("generateinterval"));
		ret.generateTimes = Integer.parseInt(elem.getChildText("generatetimes"));
		ret.x = Integer.parseInt(elem.getChildText("x"));
		ret.y = Integer.parseInt(elem.getChildText("y"));
		ret.xrange = Integer.parseInt(elem.getChildText("xrange"));
		ret.yrange = Integer.parseInt(elem.getChildText("yrange"));
		ret.particleID = Integer.parseInt(elem.getChildText("particleid"));
		ret.liveTime = Integer.parseInt(elem.getChildText("livetime"));
		ret.liveTimeRange = Integer.parseInt(elem.getChildText("livetimerange"));
		
		Element pathElem = elem.getChild("path");
		String pathType = pathElem.getAttributeValue("type");
		ret.path = (PipParticlePath)Class.forName(pathType).newInstance();
		List list = pathElem.getChildren("param");
		for (int i = 0; i < list.size(); i++) {
			ret.path.setParam(i, Double.parseDouble(((Element)list.get(i)).getTextTrim()));
		}
		return ret;
	}
	
	public void save(File file) throws Exception {
		Document doc = saveToDOM();
		com.pip.util.Utils.saveDOM(doc, file);
	}
	
	private Document saveToDOM() throws Exception {
		Element root = new Element("effectset");
        Document doc = new Document(root);
        
        Element elem = new Element("version");
        elem.setText(String.valueOf(version));
        root.addContent(elem);
        
        elem = new Element("animatefile");
        elem.setText(animateFile.getName());
        root.addContent(elem);
        
        for (PipParticleEffect effect : effects) {
        	root.addContent(saveEffect(effect));
        }
        return doc;
	}
	
	private Element saveEffect(PipParticleEffect effect) {
		Element ret = new Element("effect");
		
		Element elem = new Element("title");
		elem.setText(effect.title);
		ret.addContent(elem);
		
		elem = new Element("starttick");
		elem.setText(String.valueOf(effect.startTick));
		ret.addContent(elem);

		elem = new Element("stoptick");
		elem.setText(String.valueOf(effect.stopTick));
		ret.addContent(elem);
		
		for (PipParticleSet pset : effect.particleSets) {
			ret.addContent(saveParticleSet(pset));
		}
		return ret;
	}
	
	private Element saveParticleSet(PipParticleSet pset) {
		Element ret = new Element("particleset");
		
		Element elem = new Element("title");
		elem.setText(pset.title);
		ret.addContent(elem);
		
		elem = new Element("starttime");
		elem.setText(String.valueOf(pset.startTime));
		ret.addContent(elem);
		
		elem = new Element("generatecount");
		elem.setText(String.valueOf(pset.generateCount));
		ret.addContent(elem);
		
		elem = new Element("generatecountrange");
		elem.setText(String.valueOf(pset.generateCountRange));
		ret.addContent(elem);

		elem = new Element("generateinterval");
		elem.setText(String.valueOf(pset.generateInterval));
		ret.addContent(elem);
		
		elem = new Element("generatetimes");
		elem.setText(String.valueOf(pset.generateTimes));
		ret.addContent(elem);
		
		elem = new Element("x");
		elem.setText(String.valueOf(pset.x));
		ret.addContent(elem);
		
		elem = new Element("y");
		elem.setText(String.valueOf(pset.y));
		ret.addContent(elem);
		
		elem = new Element("xrange");
		elem.setText(String.valueOf(pset.xrange));
		ret.addContent(elem);
		
		elem = new Element("yrange");
		elem.setText(String.valueOf(pset.yrange));
		ret.addContent(elem);
		
		elem = new Element("particleid");
		elem.setText(String.valueOf(pset.particleID));
		ret.addContent(elem);
		
		elem = new Element("livetime");
		elem.setText(String.valueOf(pset.liveTime));
		ret.addContent(elem);
		
		elem = new Element("livetimerange");
		elem.setText(String.valueOf(pset.liveTimeRange));
		ret.addContent(elem);
		
		elem = new Element("path");
		elem.addAttribute("type", pset.path.getClass().getName());
		ret.addContent(elem);
		for (int i = 0; i < pset.path.getParamCount(); i++) {
			Element paramElem = new Element("param");
			paramElem.setText(String.valueOf(pset.path.getParam(i)));
			elem.addContent(paramElem);
		}
		return ret;
	}

	public void saveClientFormat(File file) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		saveClientFormat(dos);
		dos.flush();
		com.pip.util.Utils.saveFileData(file, bos.toByteArray());
	}
	
	private void saveClientFormat(DataOutputStream dos) throws Exception {
		dos.write("PEF".getBytes("ASCII"));
		dos.writeByte(version);
		String animateSetName = animateFile.getName();
		animateSetName = animateSetName.substring(0, animateSetName.length() - 1) + "n";
		dos.writeUTF(animateSetName);
        dos.writeByte(effects.size());
        for (PipParticleEffect effect : effects) {
        	saveEffectClientFormat(dos, effect);
        }
	}
	
	private void saveEffectClientFormat(DataOutputStream dos, PipParticleEffect effect) throws Exception {
		dos.writeShort(effect.startTick);
		dos.writeShort(effect.stopTick);
		dos.writeByte(effect.particleSets.size());
		for (PipParticleSet pset : effect.particleSets) {
			saveParticleSetClientFormat(dos, pset);
		}
	}
	
	private void saveParticleSetClientFormat(DataOutputStream dos, PipParticleSet pset) throws Exception {
		dos.writeShort(pset.startTime);
		dos.writeShort(pset.generateCount);
		dos.writeShort(pset.generateCountRange);
		dos.writeShort(pset.generateInterval);
		dos.writeShort(pset.generateTimes);
		dos.writeShort(pset.x);
		dos.writeShort(pset.y);
		dos.writeShort(pset.xrange);
		dos.writeShort(pset.yrange);
		dos.writeShort(pset.particleID);
		dos.writeShort(pset.liveTime);
		dos.writeShort(pset.liveTimeRange);
		dos.writeShort(pset.generateCount);
		dos.writeUTF(pset.path.getClass().getSimpleName());
		dos.writeByte(pset.path.getParamCount());
		for (int i = 0; i < pset.path.getParamCount(); i++) {
			dos.writeInt((int)(pset.path.getParam(i) * 100.0));
		}
	}
	
	public void restoreState(DataInputStream dis) throws Exception {
		Document doc = Utils.loadDOM(dis);
		load(doc);
	}

	public void saveState(DataOutputStream dos) throws Exception {
		Document doc = saveToDOM();
		Utils.saveDOM(doc, dos);
	}
	
	public PipParticleEffectPlayer getPlayer(int index, boolean[] visible) {
		PipParticleEffect eff = effects.get(index);
		return new PipParticleEffectPlayer(sourceAnimate, eff.generateParticles(visible), eff.startTick, eff.stopTick);
	}
	
	/**
	 * 把粒子效果用cts文件格式展现出来。
	 * @return
	 */
	public PipAnimateSet toAnimateSet() {
		PipAnimateSet ret = new PipAnimateSet();
		for (int i = 0; i < sourceAnimate.getFileCount(); i++) {
			ret.addSourceFile(sourceAnimate.getFileName(i), sourceAnimate.getSourceImage(i));
		}
		for (int i = 0; i < effects.size(); i++) {
			// 每个效果生成一个动画序列
			PipParticleEffect eff = effects.get(i);
			PipParticle[] particles = eff.generateParticles(null);
			int currentTime = -1;
			int particlePointer = 0;
			List<PipParticle> activeParticles = new ArrayList<PipParticle>();
			PipAnimate newAnimate = ret.addAnimate(effects.get(i).title);
			while (particlePointer < particles.length || activeParticles.size() > 0) {
				// 建立时间轴
				currentTime++;
				
				// 检查当前时间激活的新粒子
				while (particlePointer < particles.length && particles[particlePointer].startTime == currentTime) {
					activeParticles.add(particles[particlePointer]);
					particlePointer++;
				}
				
				// 移除播放完成的粒子
				for (int j = 0; j < activeParticles.size(); j++) {
					int time = currentTime - activeParticles.get(j).startTime;
					if (time >= activeParticles.get(j).path.length) {
						activeParticles.remove(j);
						j--;
					}
				}
				
				// 检查是否在播放范围内
				if (eff.startTick != -1 && currentTime < eff.startTick) {
					continue;
				}
				if (eff.stopTick != -1 && currentTime >= eff.stopTick) {
					continue;
				}
				
				// 所有活跃粒子拼成一个frame
				PipAnimateFrame newFrame = ret.addFrame(newAnimate.getName());
				for (PipParticle particle : activeParticles) {
					int time = currentTime - particle.startTime;
					if (particle.path[time][0] == -1000) {
						continue;
					}
			    	PipAnimate ani = sourceAnimate.getAnimate(particle.particleID);
			    	int animateFrame = ani.getFrameAtTime(time);
			    	PipAnimateFrameRef fref = ani.getFrame(animateFrame);
			    	PipAnimateFrame pframe = fref.realize();
			    	for (int k = 0; k < pframe.getPieceCount(); k++) {
			    		PipAnimateFramePiece p = pframe.getPiece(k);
			    		PipAnimateFramePiece newp = new PipAnimateFramePiece(newFrame);
			    		newp.setImageID(p.getImageID());
			    		newp.setFrame(p.getFrame());
			    		newp.setTransition(p.getTransition());
			    		newp.setDx(p.getDx() + fref.getDx() + particle.path[time][0]);
			    		newp.setDy(p.getDy() + fref.getDy() + particle.path[time][1]);
			    		newFrame.addPiece(newp);
			    	}
				}
				newAnimate.addFrame(ret.getFrameCount() - 1);
			}
		}
		return ret;
	}
}
