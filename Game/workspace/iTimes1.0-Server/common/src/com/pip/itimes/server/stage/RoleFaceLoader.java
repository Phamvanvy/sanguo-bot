package com.pip.itimes.server.stage;

import java.io.File;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import java.io.FileInputStream;
import org.dom4j.Attribute;

import com.pip.itimes.server.util.Utils;

public class RoleFaceLoader {

    private File pngDir;

    private Map<String,byte[]> cache = new HashMap<String,byte[]>();

    public RoleFaceLoader(File pngDir) throws Exception {
        this.pngDir = pngDir;
        SAXReader reader = new SAXReader();
        Document doc = reader.read(pngDir);
        load(doc);
    }

    private void load(Document doc) throws Exception{
        Element root = doc.getRootElement();
        for(Iterator i=root.elementIterator("face");i.hasNext();){
            Element node = (Element)i.next();
            int face = Integer.parseInt(node.attributeValue("id"));
            String name = node.attributeValue("name");
            int price = Integer.parseInt(Utils.getWholeDataPrice(node.attributeValue("price")));
            Attribute att = node.attribute("consumecode");
            String consumeCode = null;
            if(att!=null){
                consumeCode = att.getValue();
                if(consumeCode.length()==0)
                    consumeCode = null;
            }
            int itemid = Integer.parseInt(node.attributeValue("itemid"));
            RoleFaceData roleFace = new RoleFaceData(face,name,price);
            roleFace.setItemId(itemid);
            String time = node.attributeValue("time");						//形象的时效时长
            if("-1".equals(time.trim())){
            	roleFace.setDuration(-1);
            	roleFace.setExpiration(-1);
            }else{
            	long timeL= Long.parseLong(time);
            	roleFace.setDuration(timeL);
            	roleFace.setExpiration(timeL);
            }
            String renew = node.attributeValue("renew");			//是否可以续费
            int renewInt = Integer.parseInt(renew);
            roleFace.setRenew(renewInt);
            String cost = node.attributeValue("cost");				//换装要花费的钱数
            int costInt = Integer.parseInt(cost);					
            roleFace.setCost(costInt);
            
            for(Iterator j=node.elementIterator("image");j.hasNext();){
                Element el = (Element)j.next();
                int type = Integer.parseInt(el.attributeValue("type"));
                String pfile = el.attributeValue("pfile");
                String sfile = el.attributeValue("sfile");
                byte[] pdata = getFileData(pfile);
                byte[] sdata = getFileData(sfile);
                ImageData image = new ImageData(pdata,sdata);
                if(type==RoleFaceData.WALK){
                    roleFace.setWalk(image);
                }
                else if(type==RoleFaceData.BATTLE){
                    roleFace.setBattle(image);
                }
                else if(type==RoleFaceData.PORTRAIT){
                    roleFace.setPortrait(image);
                }
                else if(type==RoleFaceData.EFFECT){
                    roleFace.setEffect(image);
                }else{
                    throw new IllegalArgumentException();
                }
                roleFace.setConsumeCode(consumeCode);
            }
            for(Iterator j=node.elementIterator("ctn");j.hasNext();){
                Element el = (Element)j.next();
                int type = Integer.parseInt(el.attributeValue("type"));
                String pfile = el.attributeValue("ctnfile");
                if(type == RoleFaceData.WALKANIMATE){
                	roleFace.setWalkAnimateName(pfile);
                }else if(type == RoleFaceData.BATTLEANIMATE){
                	roleFace.setBattleAnimateName(pfile);
                }
            }
            if(roleFace.check()){
                RoleFaces.addRoleFace(roleFace);
            }else{
                throw new IllegalArgumentException();
            }
        }
        cache.clear();
    }

    private byte[] getFileData(String fileName) throws Exception{
        byte[] ret = cache.get(fileName);
        if(ret!=null)
            return ret;
        else{
            if("null".equals(fileName)){
                return new byte[0];
            }else{
                String fullPath = FilenameUtils.concat(FilenameUtils.getFullPath(pngDir.getPath()), fileName);
                FileInputStream fs = new FileInputStream(fullPath);
                byte[] png = IOUtils.toByteArray(fs);
                fs.close();
                ret = png;
                cache.put(fileName, ret);
                return ret;
            }
        }
    }

}
