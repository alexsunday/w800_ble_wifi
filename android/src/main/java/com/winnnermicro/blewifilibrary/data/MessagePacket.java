package com.winnnermicro.blewifilibrary.data;

import com.winnnermicro.blewifilibrary.utils.AESUtils;

public class MessagePacket {
    private static final String TAG = "BleWiFiClient";
    public static final int PACKET_FLAG_ENCRYPT_MASK = 0x80;
    public static final int PACKET_FLAG_ACK_MASK = 0x40;
    public static final int PACKET_FLAG_LAST_PACKET_MASK = 0x20;

    public static final int PACKET_CMD_CONFIG_STA = 0x0A;
    public static final int PACKET_CMD_CONFIG_AP = 0x0B;
    public static final int PACKET_CMD_CONFIG_APSTA = 0x0C;
    public static final int PACKET_CMD_QUERY_COFNIG = 0x0D;
    public static final int PACKET_CMD_WIFI_SCAN = 0x0E;
    public static final int PACKET_CMD_KEY_EXCHANGE = 0x0F;
    public static final int PACKET_CMD_ACK = 0x10;

    public static final int PACKET_RESP_CMD_CONFIG_STA = 0x8A;
    public static final int PACKET_RESP_CMD_CONFIG_AP = 0x8B;
    public static final int PACKET_RESP_CMD_CONFIG_APSTA = 0x8C;
    public static final int PACKET_RESP_CMD_QUERY_COFNIG = 0x8D;
    public static final int PACKET_RESP_CMD_WIFI_SCAN = 0x8E;
    public static final int PACKET_RESP_CMD_KEY_EXCHANGE = 0x8F;
    public static final int PACKET_RESP_CMD_ACK = 0x90;

    public static final int PACKET_TLV_TYPE_AP_SSID = 0x01;
    public static final int PACKET_TLV_TYPE_AP_PASSWORD = 0x02;
    public static final int PACKET_TLV_TYPE_AP_BSSID = 0x03;
    public static final int PACKET_TLV_TYPE_SOFT_AP_SSID = 0x04;
    public static final int PACKET_TLV_TYPE_SOFT_AP_PASSWORD = 0x05;
    public static final int PACKET_TLV_TYPE_SOFT_AP_MAX_CN= 0x06;
    public static final int PACKET_TLV_TYPE_SOFT_AP_AUTH_MODE = 0x07;
    public static final int PACKET_TLV_TYPE_SOFT_AP_CHANNEL = 0x08;
    public static final int PACKET_TLV_TYPE_PUB_KEY = 0x09;

    public static final int PACKET_RESP_TLV_TYPE_ERROR_ID = 0x81;
    public static final int PACKET_RESP_TLV_TYPE_IP_ADDR = 0x82;
    public static final int PACKET_RESP_TLV_TYPE_MAC = 0x83;
    public static final int PACKET_RESP_TLV_TYPE_SOFTAP_IP_ADDR = 0x84;
    public static final int PACKET_RESP_TLV_TYPE_SOFTAP_MAC = 0x85;
    public static final int PACKET_RESP_TLV_TYPE_WIFI_SCAN_RESULT = 0x86;
    public static final int PACKET_RESP_TLV_TYPE_PUB_ENC_KEY = 0x87;

    public byte cmd;
    public byte seq;
    public byte flag;
    public byte no;
    public byte[] payload;
    public byte[] packageInfo;

    public void buildPacket(int seq, boolean ack) {
        PackManager packManager = new PackManager();
        packageInfo = new byte[payload.length + 5];
        packageInfo[0] = this.cmd;
        packageInfo[1] = MessagePacket.intToByte(seq);
        if(ack){
            this.flag |= PACKET_FLAG_ACK_MASK;
        }
        packageInfo[2] = this.flag;
        packageInfo[3] = this.no;
        System.arraycopy(payload, 0, packageInfo, 4, payload.length);
        packageInfo[4+payload.length] = packManager.calCRC8(packageInfo, packageInfo.length - 1, 0);
    }

    public static MessagePacket parsePacket(byte[] data){
        if(data == null || data.length <= 5)
        {
            return null;
        }
        PackManager packManager = new PackManager();
        MessagePacket packet = null;
        if(packManager.calCRC8(data, data.length - 1, 0) == data[data.length - 1])
        {
            packet = new MessagePacket();
            packet.cmd = data[0];
            packet.seq = data[1];
            packet.flag = data[2];
            packet.no = data[3];
            packet.payload = new byte[data.length-5];
            System.arraycopy(data, 4, packet.payload, 0, data.length-5);
        }
        return packet;
    }
    public static byte intToByte(int x) {
        return (byte) (x & 0xFF);
    }
    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

}
