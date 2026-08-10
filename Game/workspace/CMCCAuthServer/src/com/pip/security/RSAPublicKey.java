package com.pip.security;

import java.io.*;

public final class RSAPublicKey {
    private final BigInteger n, e;


    public RSAPublicKey(BigInteger n, BigInteger e) {
        this.n = n;
        this.e = e;
    }

    public RSAPublicKey(byte[] data) throws Exception {
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

            n = new BigInteger(1, ndata);
            e = new BigInteger(1, edata);
        } catch (Exception e) {
            throw new Exception("bad key data");
        }
    }

    public BigInteger getModulus() {
        return this.n;
    }

    public BigInteger getPublicExponent() {
        return this.e;
    }

    public byte[] getEncoded() {
        byte[] ndata = n.toByteArray();
        byte[] edata = e.toByteArray();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.write('n');
            dos.writeShort(ndata.length);
            dos.write(ndata);
            dos.write('e');
            dos.writeShort(edata.length);
            dos.write(edata);
        } catch (Exception e) {
        }
        return bos.toByteArray();
    }
}
