package com.pip.security;

import java.util.Random;

public class SecureRandom extends Random {
    public SecureRandom() {
        super(System.currentTimeMillis());
    }

    public void nextBytes(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)nextInt();
        }
    }
}
