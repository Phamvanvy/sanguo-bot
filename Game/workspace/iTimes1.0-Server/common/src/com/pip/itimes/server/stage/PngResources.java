package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PngResources {

    private File pngDir;
    private Map files = new HashMap();

    public PngResources(File pngDir) throws Exception {
        this.pngDir = pngDir;
        load();
    }


    public void load() throws Exception{
        File[] resources = pngDir.listFiles();
        for(int i=0;i<resources.length;i++){
            String fileName = resources[i].getName();
            String baseName = FilenameUtils.getBaseName(fileName);
            String ext = FilenameUtils.getExtension(fileName);
            PngResourceData resource = (PngResourceData)files.get(baseName);
            if(resource==null){
                resource = new PngResourceData();
                resource.name = baseName;
                files.put(baseName,resource);
            }
            if("p".equals(ext)){
                FileInputStream fs = new FileInputStream(resources[i]);
                byte[] png = IOUtils.toByteArray(fs);
                fs.close();
                resource.png = png;
            }
            else if("s".equals(ext)){
                FileInputStream fs = new FileInputStream(resources[i]);
                byte[] desc = IOUtils.toByteArray(fs);
                fs.close();
                resource.desc = desc;
            }else if("pip".equals(ext)){
                FileInputStream fs = new FileInputStream(resources[i]);

                fs = new FileInputStream(resources[i]);
                byte[] pipImg = IOUtils.toByteArray(fs);
                fs.close();
                resource.pipImg = pipImg;
            }
        }
    }


    public PngResourceData getPngResourceData(String name){
        return (PngResourceData)files.get(name);
    }

}
