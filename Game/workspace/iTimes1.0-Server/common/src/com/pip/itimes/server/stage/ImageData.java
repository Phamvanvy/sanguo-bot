package com.pip.itimes.server.stage;

public class ImageData {



    private byte[] pfile;
    private byte[] sfile;

    public ImageData(byte[] pfile,byte[] sfile) {
        this.pfile = pfile;
        this.sfile = sfile;
    }

    public byte[] getSfile() {
        return sfile;
    }

    public void setPfile(byte[] pfile) {
        this.pfile = pfile;
    }

    public void setSfile(byte[] sfile) {
        this.sfile = sfile;
    }

    public byte[] getPfile() {
        return pfile;
    }
}
