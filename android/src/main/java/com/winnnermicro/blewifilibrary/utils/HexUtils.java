package com.winnnermicro.blewifilibrary.utils;

public class HexUtils {

    public static String formatHexString(byte[] data, Character sepChar) {
        if (data == null || data.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
            if (sepChar != null && i < data.length - 1)
                sb.append(sepChar);
        }
        return sb.toString().trim();
    }
}
