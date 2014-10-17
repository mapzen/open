package com.mapzen.open.util;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class SimpleCrypt {
    private SecretKeySpec sks;
    static {
        try {
            System.loadLibrary("leyndo");
        } catch (UnsatisfiedLinkError e) {
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
                throw e;
            }
        }
    }

    public SimpleCrypt() {
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(getSalt().getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {
            Logger.e("encryption failure: " + e.getMessage());
        }
    }

    public native String getSalt();

    public String encode(String phrase) {
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(phrase.getBytes());
        } catch (Exception e) {
            Logger.e("encryption failure: " + e.getMessage());
        }
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    public String decode(String gargle) {
        byte[] encodedBytes = Base64.decode(gargle, Base64.DEFAULT);
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, sks);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            Logger.e("encryption failure: " + e.getMessage());
        }
        return new String(decodedBytes);
    }
}
