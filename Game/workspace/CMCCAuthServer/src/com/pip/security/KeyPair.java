package com.pip.security;

public class KeyPair {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;


    public KeyPair(RSAPublicKey pubk, RSAPrivateKey prik) {
        publicKey = pubk;
        privateKey = prik;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }
}