package com.winnnermicro.blewifilibrary.model;

import android.util.SparseArray;

import com.winnnermicro.blewifilibrary.data.MessagePacket;
import com.winnnermicro.blewifilibrary.utils.HexUtils;

public class BleWiFiConfigStaResult extends BleWiFiBaseResult {

    private String mac;
    private String ipAddress;

    public BleWiFiConfigStaResult(SparseArray<byte[]> objects){
        super(objects);
        byte[] typePayload = objects.get(MessagePacket.PACKET_RESP_TLV_TYPE_MAC);
        if(typePayload != null){
            setMac(HexUtils.formatHexString(typePayload, ':'));
        }
        typePayload = objects.get(MessagePacket.PACKET_RESP_TLV_TYPE_IP_ADDR);
        if(typePayload != null){
            setIpAddress(String.format("%d.%d.%d.%d", typePayload[0] & 0xFF, typePayload[1] & 0xFF, typePayload[2] & 0xFF, typePayload[3] & 0xFF));
        }
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
