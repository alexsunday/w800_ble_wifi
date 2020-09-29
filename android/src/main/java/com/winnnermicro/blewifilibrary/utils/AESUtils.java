package com.winnnermicro.blewifilibrary.utils;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {
    private static boolean initialized = false;
    public static void initialize(){
        if (initialized) return;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        initialized = true;
    }
    //  AES-128 数据加密
    public static byte[] Encrypt(byte[] sSrc, byte[] sKey){
        try{
            //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            initialize();
            SecretKeySpec skeySpec = new SecretKeySpec(sKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc);
            return encrypted;
        }catch(Exception ex){
            return null;
        }
    }

    //  AES-128 数据解密
    public static byte[] Decrypt(byte[] sSrc, byte[] sKey){
        try{
            initialize();
            SecretKeySpec skeySpec = new SecretKeySpec(sKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] dncrypted = cipher.doFinal(sSrc);
            return dncrypted;
        }catch(Exception ex){
            return null;
        }
    }
}
