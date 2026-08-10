package com.pip.security;

import java.io.*;

public final class RSAPrivateKey {
    private final BigInteger n, e, d;


    public RSAPrivateKey(BigInteger n, BigInteger e, BigInteger d) {
        this.n = n;
        this.e = e;
        this.d = d;
    }

    public RSAPrivateKey(byte[] data) throws Exception {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            if (dis.read() != (byte)'n') {
                throw new Exception("wrong key format.");
            }
            short nlen = dis.readShort();
            byte[] ndata = new byte[nlen];
            dis.read(ndata);
            if (dis.read() != (byte)'e') {
                throw new Exception("wrong key format.");
            }
            int elen = dis.readShort();
            byte[] edata = new byte[elen];
            dis.read(edata);
            if (dis.read() != (byte)'d') {
                throw new Exception("wrong key format.");
            }
            int dlen = dis.readShort();
            byte[] ddata = new byte[dlen];
            dis.read(ddata);

            n = new BigInteger(1, ndata);
            e = new BigInteger(1, edata);
            d = new BigInteger(1, ddata);
        } catch (Exception e) {
            throw new Exception("bad key data");
        }
    }

    public BigInteger getModulus() {
        return this.n;
    }

    public BigInteger getPrivateExponent() {
        return this.d;
    }

    public byte[] getEncoded() {
        byte[] ndata = n.toByteArray();
        byte[] edata = e.toByteArray();
        byte[] ddata = d.toByteArray();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.write('n');
            dos.writeShort(ndata.length);
            dos.write(ndata);
            dos.write('e');
            dos.writeShort(edata.length);
            dos.write(edata);
            dos.write('d');
            dos.writeShort(ddata.length);
            dos.write(ddata);
        } catch (Exception e) {
        }
        return bos.toByteArray();
    }
}
