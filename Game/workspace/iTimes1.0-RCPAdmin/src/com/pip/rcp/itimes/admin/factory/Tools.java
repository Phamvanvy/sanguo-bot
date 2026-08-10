package com.pip.rcp.itimes.admin.factory;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


public class Tools{
    public static Document loadDOM(File file) throws Exception{
        SAXBuilder sb = new SAXBuilder();
        sb.setValidation(false);
        Document doc = sb.build(file);

        return doc;
    }

    public static void saveDOM(Document doc, File file) throws Exception{
        FileOutputStream fos = null;
        try{
            XMLOutputter out = new XMLOutputter("    ", true, "GBK");
            fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            out.output(doc, bos);
            bos.flush();
        }catch(Exception e){
            throw e;
        }finally{
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                }
            }
        }
    }
}
