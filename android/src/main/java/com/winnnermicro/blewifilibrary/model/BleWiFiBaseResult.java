package com.winnnermicro.blewifilibrary.model;

import android.util.SparseArray;

import com.winnnermicro.blewifilibrary.data.MessagePacket;

public class BleWiFiBaseResult {

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_INVALID_PARAMS = 1;
    public static final int STATUS_PASSWORD = 2;
    public static final int STATUS_DHCP_IP= 3;
    public static final int STATUS_WIFI_SCAN= 4;
    public static final int STATUS_NEGOTIATE_SECRET_KEY=5;
    public static final int STATUS_GATT_WRITE=6;
    public static final int STATUS_TIMEOUT=7;
    public static final int STATUS_BT_POWER_OFF=8;
    public static final int STATUS_LOCATION_DISABLE=9;

    private int status;
    public BleWiFiBaseResult(SparseArray<byte[]> objects){
        byte[] typePayload = objects.get(MessagePacket.PACKET_RESP_TLV_TYPE_ERROR_ID);
        if(typePayload != null){
            status = (typePayload[0] & 0xFF);
        }
    }
    public int getStatus() {
        return status;
    }

}
