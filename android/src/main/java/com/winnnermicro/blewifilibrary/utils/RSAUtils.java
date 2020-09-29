package com.winnnermicro.blewifilibrary.utils;

import android.util.Log;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class RSAUtils {

    private static final String TAG = "BleWiFiClient";
    public static final String KEY_ALGORITHM = "RSA";
    //public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    //获得公钥
    public static byte[] getPublicKey(Map<String, Object> keyMap) throws Exception {
        //获得map中的公钥对象 转为key对象
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        //byte[] publicKey = key.getEncoded();
        //编码返回字符串
        return (key.getEncoded());
    }

    //获得私钥
    public static byte[] getPrivateKey(Map<String, Object> keyMap) throws Exception {
        //获得map中的私钥对象 转为key对象
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        //byte[] privateKey = key.getEncoded();
        //编码返回字符串
        return (key.getEncoded());
    }

    //map对象中存放公私钥
    public static Map<String, Object> initKey() throws Exception {
        //获得对象 KeyPairGenerator 参数 RSA 1024个字节
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);
        //通过对象 KeyPairGenerator 获取对象KeyPair
        KeyPair keyPair = keyPairGen.generateKeyPair();

        //通过对象 KeyPair 获取RSA公私钥对象RSAPublicKey RSAPrivateKey
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //公私钥对象存入map中
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    public static byte[] decryptByRsaPrikey(Map<String, Object> keyMap, byte[] cipherText) {
        try {
            AESUtils.initialize();
            Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", "BC");
            //开始解密
            cipher.init(Cipher.DECRYPT_MODE, (RSAPrivateKey)keyMap.get(PRIVATE_KEY));
            //Log.d(TAG,"Receive aes encrypted key: " + HexUtils.formatHexString(cipherText, ' '));
            byte[] plainText = cipher.doFinal(cipherText);
            //Log.d(TAG,"aes key: " + HexUtils.formatHexString(plainText, ' '));
            return plainText;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
    public static byte[] encryptByPubkey( Map<String, Object> keyMap, byte[] cipherText ) {
        try {
            //RSA加密
            AESUtils.initialize();
            Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, (RSAPublicKey)keyMap.get(PUBLIC_KEY));
            byte[] outStr = cipher.doFinal(cipherText);
            return outStr;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
