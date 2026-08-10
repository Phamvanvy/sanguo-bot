package pip;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Image;


public class CacheImageSet{
    public int hitCount = 0;
    public int imageID;
    public int type;
    public ImageSet data = null;
    public byte[] pdata;
    public byte[] sdata;

    public ImageSet getImageSet(){
        return getImageSet(true);
    }

    public ImageSet getImageSet(boolean addHitCount){
        //#debug
        World.log("get from cache ", true);

        if(addHitCount){
            addHitCount();
        }

        if(data == null){
            Image img = Image.createImage(pdata, 0, pdata.length);
            DataInputStream sfile = new DataInputStream(new ByteArrayInputStream(sdata));

            try{
                data = ImageSet.createImageSet(img, sfile, true);
            }catch(Exception e){
                //#debug
                e.printStackTrace();
            }
        }

        return data;
    }

    public void addHitCount(){
        World.cacheChanged = true;
        hitCount++;
    }

    public void clearHitCount(){
        hitCount = 0;
    }

    public void save(DataOutputStream dos){
        try{
            dos.writeInt(imageID);
            dos.writeInt(type);
            dos.writeInt(pdata.length);
            dos.write(pdata);
            dos.writeInt(sdata.length);
            dos.write(sdata);
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }
    }

    public void load(DataInputStream dis){
        try{
            imageID = dis.readInt();
            type = dis.readInt();
            pdata = new byte[dis.readInt()];
            dis.read(pdata);
            sdata = new byte[dis.readInt()];
            dis.read(sdata);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
