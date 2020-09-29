package com.winnnermicro.blewifilibrary.constants;

import java.util.UUID;

public class ConfigInfo {
    public static final UUID UUID_CHARARCTERISTIC_WRITE = UUID.fromString("00002abc-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARARCTERISTIC_READ = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //    public static final String UUID_CHARARCTERISTIC_WRITE = "00002b4f-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARARCTERISTIC_CUSTOM_WRITE = "00002b42-0000-1000-8000-00805f9b34fb";
//    public static final UUID UUID_CHARARCTERISTIC_READ = UUID.fromString("00002c50-0000-1000-8000-00805f9b34fb");
    //public static final String UUID_SERVICE_WIFT = "0000455a-0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_SERVICE_WIFT = UUID.fromString("00001824-0000-1000-8000-00805f9b34fb");

    public static final int MAX_LENGHT_SSID = 32;
    public static final int MAX_LENGHT_PWD = 64;
    public static final int MAX_LENGHT_CUSTOMDATA = 64;

    public static final int MAX_ACK_TIMEOUT = 2;
}
